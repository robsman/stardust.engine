/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.RuntimeAttributeHolder;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityScope;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ModelScope;
import org.eclipse.stardust.engine.api.runtime.Permission;
import org.eclipse.stardust.engine.api.runtime.ProcessScope;
import org.eclipse.stardust.engine.api.runtime.Scope;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



/**
 * Utility class containing a selection of runtime static methods to check for permissions.
 * 
 * @author herinean
 * @version $Revision$
 */
public final class Authorization
{
   public static final String ALL = "__carnot_internal_all_permissions__";
   
   public static final String OWNER = "__carnot_internal_owner_permission__";

   private static final Object[] ALL_PERMISSIONS = {ALL};
   
//   private static final Object[] NO_PERMISSIONS = {};
   
   private static final int prefixLen = "authorization:".length();
   
   // no instantiation
   private Authorization() {}

   /**
    * Finds the participant in the active model corresponding to the given permission.
    * 
    * @param permission the attribute name of the permission
    * @return the participant defined for the permission in the active model or null
    *         if no permission is defined 
    */
   public static Object[] getActiveModelPermission(String permission, String defaultParticipant)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel activeModel = modelManager.findActiveModel();
      return activeModel == null ? null
            : getDefinedPermissions(permission, defaultParticipant, activeModel, activeModel, activeModel.getAllAttributes());
   }

   private static Object[] getDefinedPermissions(String permission,
         String defaultParticipant, IModel model, RuntimeAttributeHolder holder, Map attributes)
   {
      Object[] permissions = null;
      if (model != null)
      {
         permissions = (Object[]) holder.getRuntimeAttribute(permission);
         if (permissions == null)
         {
            HashSet set = new HashSet();
            for (Iterator<Map.Entry> i = attributes.entrySet().iterator(); i.hasNext();)
            {
               Map.Entry entry = i.next();
               Object key = entry.getKey();
               if (key instanceof String && ((String) key).startsWith(permission))
               {
                  String id = (String) entry.getValue();
                  if (id != null)
                  {
                     if (ALL.equals(id))
                     {
                        set.clear();
                        set.add(ALL);
                        break;
                     }
                     else if (OWNER.equals(id))
                     {
                        set.add(OWNER);
                     }
                     else
                     {
                        set.add(model.findParticipant(id));
                     }
                  }
               }
            }
            if (set.isEmpty() && defaultParticipant != null)
            {
               if (ALL.equals(defaultParticipant))
               {
                  set.add(ALL);
               }
               else if (OWNER.equals(defaultParticipant))
               {
                  set.add(OWNER);
               }
               else
               {
                  set.add(model.findParticipant(defaultParticipant));
               }
            }
            permissions = set.toArray();
            holder.setRuntimeAttribute(permission, permissions);
         }
      }
      return permissions;
   }

   public static Iterator<IModelParticipant> getAllModelParticipants(IUser user)
   {
      return new FilteringIterator<IModelParticipant>(user.getAllParticipants(), new Predicate<IModelParticipant>()
      {
         public boolean accept(IModelParticipant o)
         {
            return o instanceof IRole || o instanceof IOrganization;
         }
      });
   }

   public static boolean checkRole(IUser user, IModelParticipant role)
   {
      return role == null ? false : role.isAuthorized(user);
   }

   public static void checkPermission(Object[] roles, boolean isAuthorizationMandatory)
   {
      IUser user = SecurityProperties.getUser();
      
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel activeModel = modelManager.findActiveModel();
      if (null == activeModel)
      {
         if (isEmpty(roles) && user.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE))
         {
            return;
         }
      }
      else
      {
         // ALL is permitted
         if (!isEmpty(roles) && ALL == roles[0])
         {
            return;
         }
         
         // admins can do anything
         IModelParticipant admin = activeModel.findParticipant(PredefinedConstants.ADMINISTRATOR_ROLE);
         if (checkRole(user, admin))
         {
            return;
         }
      }
   
      // not admin, not special condition MOTU, then check specific permission.
      if (!isEmpty(roles))
      {
         for (int i = 0; i < roles.length; i++)
         {
            if (roles[i] instanceof IModelParticipant && checkRole(user, (IModelParticipant) roles[i]))
            {
               return;
            }
            if (roles[i] instanceof IUser && user.equals((IUser) roles[i]))
            {
               return;
            }
            if (roles[i] instanceof IUserGroup)
            {
               Iterator<IUserGroup> groups = user.getAllUserGroups(true);
               while (groups.hasNext())
               {
            	  if (((IUserGroup) roles[i]).equals(groups.next()))
            	  {
            		 return;
            	  }
               }
            }
         }
      }
      
      // oops, no matching permission.
      if (roles != null && roles.length > 0 || isAuthorizationMandatory)
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.AUTHx_AUTH_MISSING_GRANTS.raise(user.getOID(),
                     isEmpty(roles)
                           ? PredefinedConstants.ADMINISTRATOR_ROLE
                           : getObjectId(roles[0]), user.getAccount()));
      }
   }

   private static String getObjectId(Object role)
   {
	  if (role instanceof IModelParticipant)
	  {
         return ((IModelParticipant) role).getId();
	  }
	  return "OWNER";
   }
   
   private static boolean isEmpty(Object[] array)
   {
      return array == null || array.length == 0;
   }

   // returns the id of the role defined for the permission 
   public static Object[] getProcessPermission(long oid, String permission, String defaultParticipant)
   {
      IProcessInstance pi = ProcessInstanceBean.findByOID(oid);
      IProcessDefinition definition = pi.getProcessDefinition();
      return getDefinedPermissions(permission, defaultParticipant, (IModel) definition.getModel(),
            definition, definition.getAllAttributes());
   }
   
   // returns the id of the role defined for the permission 
   public static Object[] getActivityPermission(long oid, String permission, String defaultParticipant)
   {
      return getActivityPermission(oid, permission, defaultParticipant, null);
   }
   
   // returns the id of the role defined for the permission 
   public static Object[] getActivityPermission(long oid, String permission, String defaultParticipant, String fallbackParticipant)
   {
      IActivityInstance ai = ActivityInstanceBean.findByOID(oid);
      IActivity activity = ai.getActivity();
      if (OWNER.equals(defaultParticipant))
      {
         IModelParticipant performer = activity.getPerformer();
         defaultParticipant = performer == null ? fallbackParticipant : performer.getId();
      }
      Object[] permissions = getDefinedPermissions(permission, defaultParticipant, (IModel) activity.getModel(),
            activity, activity.getAllAttributes());
      Object[] defs = permissions.clone();
      // convert owner to real user/usergroup
      for (int i = 0; i < defs.length; i++)
      {
    	 if (OWNER.equals(defs[i]))
    	 {
    		defs[i] = ai.getCurrentUserPerformer();
    		if (defs[i] == null)
    		{
    		   defs[i] = ai.getCurrentPerformer();
    		}
            if (defs[i] == null)
            {
               defs[i] = defaultParticipant;
            }
    	 }
      }
      return defs;
   }
   
   public static boolean isAdmin(IUser user)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel activeModel = modelManager.findActiveModel();
      if (null == activeModel)
      {
         return user.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE);
      }
      return checkRole(user, activeModel.findParticipant(PredefinedConstants.ADMINISTRATOR_ROLE));
   }

   public static boolean isAdmin()
   {
      IUser user = SecurityProperties.getUser();
      return isAdmin(user);
   }

   // TODO: clarify if the permission shouldn't be at model level
   public static void filterProcessInstances(String permission, List<Long> piOids,
         List<Long> modelOids, List<Long> definitionOids)
   {
      if (isAdmin())
      {
         return;
      }
   
//      IUser user = SecurityProperties.getUser();
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      for (int i = piOids.size() - 1; i >= 0; i--)
      {
         long model = modelOids.get(i);
         long definition = definitionOids.get(i);
         IProcessDefinition pd = modelManager.findProcessDefinition(model, definition);
         if (pd != null)
         {
            if (!hasPermission(pd, permission))
            {
               piOids.remove(i);
            }
         }
      }
   }
   
   public static boolean hasPermission(IUser contextUser, IModel model, String permission)
   {
      if(model != null)
      {
         // administrators have all permissions
         if (isAdmin(contextUser))
         {
            return true;
         }
         Object[] permissions = getDefinedPermissions(permission, null, (IModel) model,
               model, model.getAllAttributes());
         return hasPermission(contextUser, permissions, null);
      }
      return true;
   }

   public static boolean hasPermission(IProcessInstance processInstance,
         String permission)
   {
      return hasPermission(processInstance.getProcessDefinition(), permission);
   }

   public static boolean hasPermission(IActivityInstance activityInstance,
         String permission)
   {
      IParticipant owner =  activityInstance.getCurrentUserPerformer();
      if (owner == null)
      {
         owner = activityInstance.getCurrentPerformer();
      }
      return hasPermission(activityInstance.getActivity(), permission, owner);
   }

   private static boolean hasPermission(IActivity activity, String permission, IParticipant owner)
   {
      IUser user = SecurityProperties.getUser();
      if (activity != null)
      {
         // administrators have all permissions
         if (isAdmin())
         {
            return true;
         }
         Object[] permissions = getDefinedPermissions(permission, null, (IModel) activity.getModel(),
               activity, activity.getAllAttributes());
         if(owner == null)
         {
            owner = activity.getPerformer();
         }
         return hasPermission(user, permissions, owner);
      }
      return true;
   }

   private static boolean hasPermission(IProcessDefinition process, String permission)
   {
      IUser user = SecurityProperties.getUser();
      if (process != null)
      {
         // administrators have all permissions
         if (isAdmin())
         {
            return true;
         }
         Object[] permissions = getDefinedPermissions(permission, null, (IModel) process.getModel(),
               process, process.getAllAttributes());
         return hasPermission(user, permissions, null);
      }
      return true;
   }

   private static boolean hasPermission(IUser user, Object[] permissions, IParticipant owner)
   {
      if (!isEmpty(permissions))
      {
         for (int i = 0; i < permissions.length; i++)
         {
            if (ALL.equals(permissions[i]))
            {
               return true;
            }
            else if (OWNER.equals(permissions[i])
                  && owner != null)
            {
               if(owner instanceof IModelParticipant
                     && checkRole(user, (IModelParticipant)owner))
               {
                  return true;
               }
               else if(owner instanceof IUser && 
                     owner.equals(user))
               {
                  return true;
               }
               else if(owner instanceof IUserGroup)
               {
                  Iterator ugIter = user.getAllUserGroups(true);
                  while(ugIter.hasNext())
                  {
                     if(ugIter.next().equals(owner))
                     {
                        return true;
                     }
                  }
               }
            }
            else if ((permissions[i] instanceof IModelParticipant)
                  && checkRole(user, (IModelParticipant) permissions[i]))
            {
               return true;
            }
         }
         return false;
      }
      return true;
   }

   public static boolean canFetchActivity(long modelOid, long activityRtOid, 
         Map<IProcessDefinition, Boolean> processPermissions)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IActivity activity = modelManager.findActivity(modelOid, activityRtOid);
      return checkActivityPermission(activity, processPermissions);
   }

   public static boolean canFetchActivity(IActivityInstance ai, 
         Map<IProcessDefinition, Boolean> processPermissions)
   {
      return checkActivityPermission(ai.getActivity(), processPermissions);
   }

   public static boolean checkActivityPermission(IActivity activity,
         Map<IProcessDefinition, Boolean> processPermissions)
   {
      boolean result = activity != null;
      if (result && (processPermissions != null))
      {
         IProcessDefinition process = activity.getProcessDefinition();
         Boolean processPermission = (Boolean) processPermissions.get(process);
         if (processPermission == null)
         {
            processPermission = hasPermission(process, Permissions.PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA)
               ? Boolean.TRUE : Boolean.FALSE;
            processPermissions.put(process, processPermission);
         }
         result = processPermission.booleanValue();
      }
      if (result)
      {
         result = hasPermission(activity, Permissions.ACTIVITY_READ_ACTIVITY_INSTANCE_DATA, null);
      }
      return result;
   }

   public static boolean canFetchProcess(long modelOid, long processRtOid)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IProcessDefinition process = modelManager.findProcessDefinition(modelOid, processRtOid);
      return process != null
         && hasPermission(process, Permissions.PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA);
   }

   public static boolean canFetchProcess(IProcessInstance pi)
   {
      IProcessDefinition process = pi.getProcessDefinition();
      return process != null
         && hasPermission(process, Permissions.PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA);
   }
   
   public static List<Permission> getAdministrationServicePermissions()
   {
      String[] activeModelPermissions = {
            Permissions.MODEL_DEPLOY_PROCESS_MODEL,
            Permissions.MODEL_MODIFY_AUDIT_TRAIL,
            Permissions.MODEL_RUN_RECOVERY,
            Permissions.MODEL_MANAGE_DAEMONS,
            Permissions.MODEL_CONTROL_PROCESS_ENGINE,
            Permissions.MODEL_READ_AUDIT_TRAIL_STATISTICS,
            Permissions.MODEL_FORCE_SUSPEND
      };
      String[] perProcessPermissions = {
            Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES,
            Permissions.PROCESS_DEFINITION_DELETE_PROCESS_INSTANCES,
            Permissions.PROCESS_DEFINITION_MODIFY_PROCESS_INSTANCES
      };
      
      List<Permission> perms = new ArrayList<Permission>();

      boolean guarded = Parameters.instance().getBoolean("AdministrationService.Guarded", true);
      addActiveModelPermissions(guarded, activeModelPermissions, perms);
      addProcessPermissions(guarded, perProcessPermissions, perms);
      
      return perms;
   }

   public static List<Permission> getUserServicePermissions()
   {
      String[] activeModelPermissions = {
            Permissions.MODEL_MODIFY_USER_DATA,
            Permissions.MODEL_MANAGE_AUTHORIZATION
      };
      
      List<Permission> perms = new ArrayList<Permission>();

      boolean guarded = Parameters.instance().getBoolean("UserService.Guarded", true);
      addActiveModelPermissions(guarded, activeModelPermissions, perms);
      
      return perms;
   }

   private static void addActivityPermissions(boolean guarded, String[] perActivityPermissions, List<Permission> perms)
   {
      boolean isAdmin = isAdmin();
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      
      for (int permissionCounter = 0; permissionCounter < perActivityPermissions.length; permissionCounter++)
      {
         List<Scope> scopes = new ArrayList<Scope>();
         for (Iterator models = modelManager.getAllModels(); models.hasNext();)
         {
            IModel model = (IModel) models.next();
            ModelScope modelScope = new ModelScope(model.getModelOID());
            ModelElementList definitions = model.getProcessDefinitions();
            for (int definitionCounter = 0; definitionCounter < definitions.size(); definitionCounter++)
            {
               IProcessDefinition pd = (IProcessDefinition) definitions.get(definitionCounter);
               ProcessScope processScope = new ProcessScope(modelScope, pd.getId());
               ModelElementList activities = pd.getActivities();
               for (int activityCounter = 0; activityCounter < activities.size(); activityCounter++)
               {
                  IActivity activity = (IActivity) activities.get(activityCounter);
                  if (isAdmin || !guarded || hasPermission(activity, perActivityPermissions[permissionCounter], null))
                  {
                     scopes.add(new ActivityScope(processScope, activity.getId()));
                  }
               }
            }
         }
         if (!scopes.isEmpty())
         {
            Permission perm = new Permission(perActivityPermissions[permissionCounter].substring(prefixLen),
                  scopes);
            perms.add(perm);
         }
      }
   }

   private static void addProcessPermissions(boolean guarded, String[] perProcessPermissions, List<Permission> perms)
   {
      boolean isAdmin = isAdmin();
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      
      for (int permissionCounter = 0; permissionCounter < perProcessPermissions.length; permissionCounter++)
      {
         List<Scope> allScopes = new ArrayList<Scope>();
         for (Iterator models = modelManager.getAllModels(); models.hasNext();)
         {
            IModel model = (IModel) models.next();
            ModelScope modelScope = new ModelScope(model.getModelOID());
            ModelElementList definitions = model.getProcessDefinitions();
            for (int definitionCounter = 0; definitionCounter < definitions.size(); definitionCounter++)
            {
               IProcessDefinition pd = (IProcessDefinition) definitions.get(definitionCounter);
               if (isAdmin || !guarded || hasPermission(pd, perProcessPermissions[permissionCounter]))
               {
                  allScopes.add(new ProcessScope(modelScope, pd.getId()));
               }
            }
         }
         if (!allScopes.isEmpty())
         {
            Permission perm = new Permission(perProcessPermissions[permissionCounter].substring(prefixLen),
                  allScopes);
            perms.add(perm);
         }
      }
   }

   private static void addActiveModelPermissions(boolean guarded, String[] activeModelPermissions, List<Permission> perms)
   {
      IUser user = SecurityProperties.getUser();
      boolean isAdmin = isAdmin();
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      
      IModel activeModel = modelManager.findActiveModel();
      if (activeModel != null)
      {
         ModelScope modelScope = new ModelScope(activeModel.getModelOID());
         for (int i = 0; i < activeModelPermissions.length; i++)
         {
            String permission = activeModelPermissions[i];
            if (isAdmin || !guarded || checkRole(user, permission))
            {
               Permission perm = new Permission(permission.substring(prefixLen),
                     Collections.singletonList((Scope) modelScope));
               perms.add(perm);
            }
         }
      }
   }

   private static boolean checkRole(IUser user, String permission)
   {
      Object[] permissions = getActiveModelPermission(permission, null /*TODO*/);
      return hasPermission(user, permissions, null);
   }

   public static List<Permission> getQueryServicePermissions()
   {
      String[] activeModelPermissions = {
            Permissions.MODEL_READ_AUDIT_TRAIL_STATISTICS,
            Permissions.MODEL_READ_USER_DATA,
            Permissions.MODEL_READ_MODEL_DATA
      };
      String[] perProcessPermissions = {
            Permissions.PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA
      };
      String[] perActivityPermissions = {
            Permissions.ACTIVITY_READ_ACTIVITY_INSTANCE_DATA
      };
      
      List<Permission> perms = new ArrayList<Permission>();

      boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true);
      addActiveModelPermissions(guarded, activeModelPermissions, perms);
      addProcessPermissions(guarded, perProcessPermissions, perms);
      addActivityPermissions(guarded, perActivityPermissions, perms);
      
      return perms;
   }

   public static List<Permission> getWorkflowServicePermissions()
   {
      String[] perProcessPermissions = {
            Permissions.PROCESS_DEFINITION_MANAGE_EVENT_HANDLERS,
            Permissions.PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA,
            Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES
      };
      String[] perActivityPermissions = {
            Permissions.ACTIVITY_MANAGE_EVENT_HANDLERS,
            Permissions.ACTIVITY_DELEGATE_TO_OTHER,
            Permissions.ACTIVITY_READ_ACTIVITY_INSTANCE_DATA,
            Permissions.ACTIVITY_ABORT_ACTIVITY_INSTANCES
      };
      
      List<Permission> perms = new ArrayList<Permission>();

      boolean guarded = Parameters.instance().getBoolean("WorkflowService.Guarded", true);
      addProcessPermissions(guarded, perProcessPermissions, perms);
      addActivityPermissions(guarded, perActivityPermissions, perms);
      
      return perms;
   }

   // returns the id of the role required to perform the method or null 
   public static Object[] getUserServicePermission(String method, Object[] args)
   {
      if ("getUser".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_READ_USER_DATA, ALL); 
      }
      if("modifyUser".equals(method))
      {
         if(Permissions.MODEL_MANAGE_AUTHORIZATION.equals(args[1]))
         {
            return getActiveModelPermission(Permissions.MODEL_MANAGE_AUTHORIZATION, 
                        PredefinedConstants.ADMINISTRATOR_ROLE);
         }
         else if(Permissions.MODEL_MODIFY_USER_DATA.equals(args[1]))
         {
            return getActiveModelPermission(Permissions.MODEL_MODIFY_USER_DATA, 
                  PredefinedConstants.ADMINISTRATOR_ROLE);
         }
      }
      return getActiveModelPermission(Permissions.MODEL_MODIFY_USER_DATA, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/);
   }
   
   // returns the id of the role required to perform the method or null 
   public static Object[] getAdministrationServicePermission(String method, Object[] args)
   {
      if ("deployModel".equals(method)
            || "overwriteModel".equals(method)
            || "modifyModel".equals(method)
            || "deleteModel".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_DEPLOY_PROCESS_MODEL, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("cleanupRuntime".equals(method)
            || "cleanupRuntimeAndModels".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_MODIFY_AUDIT_TRAIL, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("recoverRuntimeEnvironment".equals(method)
            || "recoverProcessInstance".equals(method)
            || "recoverProcessInstances".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_RUN_RECOVERY, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("getAllDaemons".equals(method)
            || "getDaemon".equals(method)
            || "startDaemon".equals(method)
            || "stopDaemon".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_MANAGE_DAEMONS, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("flushCaches".equals(method)/*
            || "getProperty".equals(method)
            || "setProperty".equals(method)*/)
      {
         return getActiveModelPermission(Permissions.MODEL_CONTROL_PROCESS_ENGINE, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("getAuditTrailHealthReport".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_READ_AUDIT_TRAIL_STATISTICS, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("abortProcessInstance".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         IProcessInstance instance = ProcessInstanceBean.findByOID(oid);
         if (instance != null)
         {
            return getProcessPermission(instance.getRootProcessInstanceOID(),
               Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES,
               getDefault(Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES)
               /*PredefinedConstants.ADMINISTRATOR_ROLE*/);
         }
      }
      if ("setProcessInstancePriority".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         IProcessInstance instance = ProcessInstanceBean.findByOID(oid);
         if (instance != null)
         {
            return getProcessPermission(instance.getRootProcessInstanceOID(),
               Permissions.PROCESS_DEFINITION_MODIFY_PROCESS_INSTANCES, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/);
         }
      }
      if ("forceSuspendToDefaultPerformer".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_FORCE_SUSPEND, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("getProfile".equals(method) || "setProfile".equals(method))
      {
         return ALL_PERMISSIONS;
      }
      return null;
   }

   // returns the id of the role required to perform the method or null 
   public static Object[] getWorkflowServicePermission(String method, Object[] args)
   {
      if ("abortProcessInstance".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         return getProcessPermission(oid,
               Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES,
               getDefault(Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES)
               /*PredefinedConstants.ADMINISTRATOR_ROLE*/);
      }
      if ("getProcessInstanceEventHandler".equals(method)
            || "bindProcessEventHandler".equals(method)
            || "unbindProcessEventHandler".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         return getProcessPermission(oid,
               Permissions.PROCESS_DEFINITION_MANAGE_EVENT_HANDLERS, ALL);
      }
      if ("getActivityInstanceEventHandler".equals(method)
            || "bindActivityEventHandler".equals(method)
            || "unbindActivityEventHandler".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         return getActivityPermission(oid,
               Permissions.ACTIVITY_MANAGE_EVENT_HANDLERS, ALL);
      }
      if ("abortActivityInstance".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         return getActivityPermission(oid,
               Permissions.ACTIVITY_ABORT_ACTIVITY_INSTANCES,
               getDefault(Permissions.ACTIVITY_ABORT_ACTIVITY_INSTANCES),
               PredefinedConstants.ADMINISTRATOR_ROLE);
      }
      if ("delegateToDefaultPerformer".equals(method)
            || "delegateToParticipant".equals(method)
            || "delegateToUser".equals(method)
            || "suspendToDefaultPerformer".equals(method)
            || "suspendToParticipant".equals(method)
            || "suspendToUser".equals(method)
            || "hibernate".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         return getActivityPermission(oid,
               Permissions.ACTIVITY_DELEGATE_TO_OTHER, ALL);
      }
      if ("getActivityInstance".equals(method))
      {
         long oid = ((Long) args[0]).longValue();
         return getActivityPermission(oid,
               Permissions.ACTIVITY_READ_ACTIVITY_INSTANCE_DATA, ALL);
      }
      return null;
   }

   // returns the id of the role required to perform the method or null 
   public static Object[] getQueryServicePermission(String method, Object[] args)
   {
      if ("findFirstLogEntry".equals(method)
            || "getAllLogEntries".equals(method)
            || "getLogEntriesCount".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_READ_AUDIT_TRAIL_STATISTICS, null /*PredefinedConstants.ADMINISTRATOR_ROLE*/); 
      }
      if ("findFirstUser".equals(method)
            || "findFirstUserGroup".equals(method)
            || "getAllUsers".equals(method)
            || "getAllUserGroups".equals(method)
            || "getUsersCount".equals(method)
            || "getUserGroupsCount".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_READ_USER_DATA, ALL); 
      }
      if ("getProcessDefinition".equals(method)
            || "getModel".equals(method)
            || "getModelAsXML".equals(method)
            || "getModelDescription".equals(method)
            || "getActiveModel".equals(method)
            || "getActiveModelDescription".equals(method)
            || "getAllAliveModelDescriptions".equals(method)
            || "getAllModelDescriptions".equals(method)
            || "getAllParticipants".equals(method)
            || "getAllProcessDefinitions".equals(method)
            || "getParticipant".equals(method)
            || "getProcessDefinition".equals(method)
            || "wasRedeployed".equals(method))
      {
         return getActiveModelPermission(Permissions.MODEL_READ_MODEL_DATA, ALL); 
      }
      return null;
   }

   public static boolean checkProcessAuthorization(long modelOid, long processRtOid,
         Map<RuntimeObjectKey, Boolean> processPermissions)
   {
      if (processPermissions == null)
      {
         // no test cache
         return canFetchProcess(modelOid, processRtOid);
      }
      RuntimeObjectKey proc = new RuntimeObjectKey(modelOid, processRtOid);
      Boolean processPermission = (Boolean) processPermissions.get(proc);
      if (processPermission == null)
      {
         processPermission = canFetchProcess(modelOid, processRtOid)
            ? Boolean.TRUE : Boolean.FALSE;
         processPermissions.put(proc, processPermission);
      }
      return processPermission;
   }
   
   private static class RuntimeObjectKey
   {
      private static final int PRIME = 31;

      private long modelOid;
      private long processRtOid;
      
      public RuntimeObjectKey(long modelOid, long processRtOid)
      {
         this.modelOid = modelOid;
         this.processRtOid = processRtOid;
      }

      public int hashCode()
      {
         int result = 1;
         result = PRIME * result + (int) (modelOid ^ (modelOid >>> 32));
         result = PRIME * result + (int) (processRtOid ^ (processRtOid >>> 32));
         return result;
      }

      public boolean equals(Object obj)
      {
         return this == obj || obj != null && getClass() == obj.getClass()
             && modelOid == ((RuntimeObjectKey) obj).modelOid
             && processRtOid == ((RuntimeObjectKey) obj).processRtOid;
      }
   }

   public static String getDefault(String permission)
   {
      String def = null;
      if (Permissions.ACTIVITY_ABORT_ACTIVITY_INSTANCES.equals(permission))
      {
         def = Parameters.instance().getString(
               "InfinityBpm.Engine.Authorization.DefaultPermissions.ActivityInstance.Abort",
               Authorization.OWNER);
      }
      else if (Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES.equals(permission))
      {
         def = Parameters.instance().getString(
               "InfinityBpm.Engine.Authorization.DefaultPermissions.ProcessInstance.Abort",
               null);
      }
      return def;
   }

}
