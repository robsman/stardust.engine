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

import static org.eclipse.stardust.common.CollectionUtils.copyList;
import static org.eclipse.stardust.common.CollectionUtils.newMap;
import static org.eclipse.stardust.common.CollectionUtils.newSet;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.permissions.PermissionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Default;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Id;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Scope;

public class AuthorizationContext
{
   static final String[] EMPTY = {};

   private static final String[] ALL_PERMISSIONS = {Authorization2.ALL};
   private static final Id[] IMPLIED = {};

   private ClientPermission permission;

   private IUser user;

   private List<IModel> models;
   private ModelElement modelElement;
   private IProcessInstance processInstance;
   private IActivityInstance activityInstance;

   private long scopeProcessInstanceOid;
   private long currentPerformer;
   private long currentUserPerformer;
   private long department;

   private String[] grants;
   private ModelManager modelManager;
   private boolean administratorOverride;

   private AuthorizationContext dependency;

   private Map<IModelParticipant, Boolean> checkedParticipants = newMap();
   private Map<DeptKey, IDepartment> depts = newMap();
   private Set<IOrganization> restricted;

   private String[] permissionIds;
   private Map<Pair<String, String>, String> prefetchedValues = CollectionUtils.newMap();
   private boolean prefetchDataAvailable;
   private Map<IDepartment, List<IDepartment>> subdepartments = CollectionUtils.newMap();

   private AuthorizationContext(ClientPermission permission)
   {
      this.permission = permission;
      if (permission != null)
      {
         Id id = permission.id();
         Scope scope = permission.scope();
         Id[] implied = permission == null ? IMPLIED : permission.implied();
         permissionIds = new String[1 + implied.length];
         permissionIds[0] = Authorization2.PREFIX + (scope == Scope.workitem ? Scope.activity : scope) + '.' + id;
         for (int i = 1; i < permissionIds.length; i++)
         {
            permissionIds[i] = Authorization2.PREFIX + (scope == Scope.workitem ? Scope.activity : scope) + '.' + implied[i - 1];
         }

         administratorOverride = permission.administratorOverride();
         if (administratorOverride)
         {
            ModelManager modelManager = getModelManager();
            IModel activeModel = modelManager.findActiveModel();
            if (null == activeModel)
            {
               administratorOverride = getUser().hasRole(PredefinedConstants.ADMINISTRATOR_ROLE);
            }
            else
            {
               administratorOverride = checkRole(activeModel.findParticipant(PredefinedConstants.ADMINISTRATOR_ROLE));
            }
         }

         if (id == Id.readActivityInstanceData && (scope == Scope.activity || scope == Scope.workitem))
         {
            AuthorizationContext ctx = create(ClientPermission.READ_PROCESS_INSTANCE_DATA);
            if (!ctx.isAdminOverride())
            {
               dependency = ctx;
            }
         }
      }
   }

   private void clear()
   {
      models = null;
      modelElement = null;
      processInstance = null;
      activityInstance = null;
      scopeProcessInstanceOid = 0;
      currentPerformer = 0;
      currentUserPerformer = 0;
      department = 0;
      grants = null;
   }

   public void setModelElementData(ModelElement modelElement)
   {
      clear();
      internalSetModelElementData(modelElement);
   }

   private void internalSetModelElementData(ModelElement modelElement)
   {
      this.modelElement = modelElement;
      models = Collections.singletonList(modelElement instanceof IModel
            ? (IModel) modelElement
            : (IModel) modelElement.getModel());
      if (dependency != null && modelElement instanceof IActivity)
      {
         dependency.setModelElementData(((IActivity) modelElement).getParent());
      }
   }

   public void setModels(List<IModel> models)
   {
      clear();
      this.modelElement = models.get(0);
      this.models = models;
   }

   public void setModel(long modelOid)
   {
      ModelManager mm = getModelManager();
      setModelElementData(mm.findModel(modelOid));
   }

   public void setData(long processInstanceOid, long modelOid, long dataRtOid)
   {
      ModelManager mm = getModelManager();
      setModelElementData(mm.findData(modelOid, dataRtOid));
      processInstance = ProcessInstanceBean.findByOID(processInstanceOid);
      if (dependency != null)
      {
         dependency.setProcessInstance(processInstance);
      }
   }

   public void setData(IProcessInstance pi, IData data)
   {
      setProcessInstance(pi);
      if (dependency != null)
      {
         dependency.setProcessInstance(pi);
      }
      setModelElementData(data);
   }

   public void setProcessInstance(IProcessInstance pi)
   {
      clear();
      if (pi.isCaseProcessInstance())
      {
         IActivityInstance ai = ActivityInstanceBean.getDefaultGroupActivityInstance(pi);
         if (ai != null)
         {
            setActivityInstance(ai);
         }
      }
      internalSetModelElementData(pi.getProcessDefinition());
      processInstance = pi;
      this.scopeProcessInstanceOid = pi.getScopeProcessInstanceOID();
   }

   public void setProcessData(long scopeProcessInstanceOid, long processRtOid, long modelOid)
   {
      ModelManager mm = getModelManager();
      setModelElementData(mm.findProcessDefinition(modelOid, processRtOid));
      processInstance = null;
      this.scopeProcessInstanceOid = scopeProcessInstanceOid;
   }

   public void setActivityInstance(IActivityInstance ai)
   {
      setModelElementData(ai.getActivity());
      activityInstance = ai;
      currentPerformer = ai.getCurrentPerformerOID();
      currentUserPerformer = ai.getCurrentUserPerformerOID();
      department = ai.getCurrentDepartmentOid();
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (dependency != null || session.existsInCache(ProcessInstanceBean.class, ai.getProcessInstanceOID()))
      {
         processInstance = ai.getProcessInstance();
         if (dependency != null)
         {
            dependency.processInstance = processInstance;
         }
      }
   }

   public void setActivityData(long processInstanceOid, long activityRtOid,
         long modelOid, long currentPerformer, long currentUserPerformer, long department)
   {
      ModelManager mm = getModelManager();
      setModelElementData(mm.findActivity(modelOid, activityRtOid));
      this.currentPerformer = currentPerformer;
      this.currentUserPerformer = currentUserPerformer;
      if (processInstanceOid > 0)
      {
         if ( !prefetchDataAvailable && !getRestricted().isEmpty())
         {
            processInstance = ProcessInstanceBean.findByOID(processInstanceOid);
            if (dependency != null)
            {
               dependency.processInstance = processInstance;
            }
         }
      }
      this.department = department;
   }

   public void setActivityDataWithScopePi(long scopeProcessInstanceOid, long activityRtOid,
         long modelOid, long currentPerformer, long currentUserPerformer, long department)
   {
      ModelManager mm = getModelManager();
      setModelElementData(mm.findActivity(modelOid, activityRtOid));
      this.currentPerformer = currentPerformer;
      this.currentUserPerformer = currentUserPerformer;
      this.scopeProcessInstanceOid = scopeProcessInstanceOid;
      if (dependency != null)
      {
         dependency.scopeProcessInstanceOid = scopeProcessInstanceOid;
      }
      this.department = department;
   }

   public IUser getUser()
   {
      if (user == null)
      {
         user = SecurityProperties.getUser();
      }
      return user;
   }

   public void setUser(IUser user)
   {
      this.user = user;
   }

   public boolean isAdminOverride()
   {
      return administratorOverride;
   }

   public boolean checkRole(IModelParticipant role)
   {
      if (role == null)
      {
         return false;
      }
      Boolean checked = checkedParticipants.get(role);
      if (checked != null)
      {
         return checked.booleanValue();
      }
      IUser user = getUser();
      boolean authorized = role.isAuthorized(user);
      checkedParticipants.put(role, authorized);
      return authorized;
   }

   public String[] getGrants()
   {
      if (permission == null)
      {
         return EMPTY;
      }
      if (grants == null)
      {
         grants = getCachedGrants();
      }
      return grants;
   }

   private String[] getDefaultGrants()
   {
      if (permission.changeable())
      {
         String def = null;
         Id permissionId = permission.id();
         Scope scope = permission.scope();
         if (scope == Scope.activity
               && permissionId == Id.abortActivityInstances)
         {
            def = Parameters.instance().getString(
                  "InfinityBpm.Engine.Authorization.DefaultPermissions.ActivityInstance.Abort");

         }
         else if (scope == Scope.processDefinition
               && permissionId == Id.abortProcessInstances)
         {
            def = Parameters.instance().getString(
                  "InfinityBpm.Engine.Authorization.DefaultPermissions.ProcessInstance.Abort");
         }
         if (!StringUtils.isEmpty(def))
         {
            String[] splited = def.split(",");
            for (int i = 0; i < splited.length; i++)
            {
               if (Authorization2.ALL.equals(splited[i]))
               {
                  return ALL_PERMISSIONS;
               }
            }
            return splited.length == 0 ? EMPTY : splited;
         }
      }

      Default[] defaults = permission.defaults();
      if (defaults.length == 0)
      {
         return EMPTY;
      }
      String[] result = new String[defaults.length];
      for (int i = 0; i < defaults.length; i++)
      {
         switch (defaults[i])
         {
         case ADMINISTRATOR:
            result[i] = PredefinedConstants.ADMINISTRATOR_ROLE;
            break;
         case ALL:
            return ALL_PERMISSIONS;
         case OWNER:
            boolean supportsOwner = modelElement != null &&
                  (!(modelElement instanceof IActivity) || ((IActivity) modelElement).isInteractive());
            result[i] = supportsOwner ? Authorization2.OWNER : PredefinedConstants.ADMINISTRATOR_ROLE;
            break;
         }
      }
      return result;
   }

   private String[] getCachedGrants()
   {
      String[] permissions = (String[]) modelElement.getRuntimeAttribute(permission.uniqueKey());
      if (permissions == null)
      {
         boolean isAll = false;
         List<String> grants = CollectionUtils.newList();
         for (int i = 0; i < permissionIds.length; i++)
         {
            String[] cached = getCachedGrants(permissionIds[i]);
            if (ALL_PERMISSIONS == cached)
            {
               isAll = true;
               break;
            }
            for (int j = 0; j < cached.length; j++)
            {
               grants.add(cached[j]);
            }
         }
         permissions = isAll ? ALL_PERMISSIONS : grants.toArray(new String[grants.size()]);
         modelElement.setRuntimeAttribute(permission.uniqueKey(), permissions);
      }
      return permissions;
   }

   private String[] getCachedGrants(String permissionId)
   {
      String[] permissions = null;
      if (permission.changeable())
      {
         permissions = getDefinedGrants(permissionId);
      }
      if (permissionId.equals(permissionIds[0]))
      {
         if (permissions == null || EMPTY == permissions)
         {
            permissions = getDefaultGrants();
         }
         if (ALL_PERMISSIONS != permissions)
         {
            Set<String> set = newSet();
            if (permissions.length > 0)
            {
               set.addAll(Arrays.asList(permissions));
            }
            Default[] fixed = this.permission.fixed();
            if (fixed.length > 0)
            {
               for (int i = 0; i < fixed.length; i++)
               {
                  switch (fixed[i])
                  {
                  case ADMINISTRATOR:
                     set.add(PredefinedConstants.ADMINISTRATOR_ROLE);
                     break;
                  case ALL:
                     permissions = ALL_PERMISSIONS;
                     break;
                  case OWNER:
                     set.add(Authorization2.OWNER);
                     break;
                  }
               }
            }
            if ((modelElement instanceof IProcessDefinition)
                  && (permission.scope() == ExecutionPermission.Scope.processDefinition)
                  && permissionIds[0].endsWith(ExecutionPermission.Id.startProcesses.name()))
            {
               for (ITrigger trigger : ((IProcessDefinition) modelElement).getTriggers())
               {
                  String grant = trigger.getStringAttribute(PredefinedConstants.PARTICIPANT_ATT);
                  if (grant != null && !grant.isEmpty())
                  {
                     set.add(grant);
                  }
               }
            }
            if (ALL_PERMISSIONS != permissions)
            {
               permissions = set.isEmpty() ? EMPTY : set.toArray(new String[set.size()]);
            }
         }
      }
      return permissions == null ? EMPTY : permissions;
   }

   public String getPermissionId()
   {
      return permissionIds[0];
   }

   public ModelManager getModelManager()
   {
      if (modelManager == null)
      {
         modelManager = ModelManagerFactory.getCurrent();
      }
      return modelManager;
   }

   private String[] getDefinedGrants(String permission)
   {
      Set<String> set = newSet();
      if (this.permission.scope() == Scope.model &&
            Parameters.instance().getBoolean(SecurityProperties.AUTHORIZATION_USE_PREFERENCES_STORE_PROPERTY, true))
      {
         IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();
         if (preferenceStore != null)
         {
            List<String> values = PermissionUtils.getGlobalPermissionValues(preferenceStore, permission, false);
            for (String id : values)
            {
               if (Authorization2.ALL.equals(id))
               {
                  return ALL_PERMISSIONS;
               }
               else
               {
                  set.add((String) id);
               }
            }
         }
      }
      else if (modelElement != null)
      {
         Set<Entry<String, Object>> entrySet = modelElement.getAllAttributes().entrySet();
         for (Entry<String, Object> entry : entrySet)
         {
            String key = entry.getKey();
            if (key != null && key.startsWith(permission))
            {
               Object id = entry.getValue();
               if (id instanceof String)
               {
                  if (Authorization2.ALL.equals(id))
                  {
                     return ALL_PERMISSIONS;
                  }
                  else
                  {
                     set.add((String) id);
                  }
               }
            }
         }
      }
      return set.isEmpty() ? EMPTY : set.toArray(new String[set.size()]);
   }

   public ClientPermission getPermission()
   {
      return permission;
   }

   public boolean isUserPerformer()
   {
      return permission != null
          && (permission.scope() == Scope.activity
                || permission.scope() == Scope.workitem
                || permission.scope() == Scope.processDefinition
                   && processInstance != null
                   && processInstance.isCaseProcessInstance())
          && (currentUserPerformer != 0 || currentPerformer < 0);
   }

   public boolean userCanPerform()
   {
      if (currentPerformer < 0)
      {
         return UserUserGroupLink.find(-currentPerformer, getUser().getOID()) != null;
      }
      else if (currentUserPerformer != 0)
      {
         if (getUser() == null || getUser().getOID() == currentUserPerformer)
         {
            return true;
         }
         if (UserUtils.isDeputyOf(getUser(), currentUserPerformer))
         {
            UserUtils.setOnBehalfOf(currentUserPerformer);
            return true;
         }
      }
      return false;
   }

   public IModelParticipant getParticipant(String grant)
   {
      if (permission != null
       && Authorization2.OWNER.equals(grant))
      {
         if (modelElement instanceof IActivity && currentPerformer <= 0)
         {
            return ((IActivity) modelElement).getPerformer();
         }
         if (activityInstance != null && activityInstance.getCurrentPerformerOID() == currentPerformer)
         {
            return (IModelParticipant) activityInstance.getCurrentPerformer();
         }
         ModelManager mm = getModelManager();
         try
         {
            for (IModel model : models)
            {
               IModelParticipant participant = mm.findModelParticipant(model.getModelOID(), currentPerformer);
               if (participant != null)
               {
                  return participant;
               }
            }
         }
         catch (Exception ex)
         {
            // trace ? System.out.println(currentPerformer);
            return null;
         }
      }
      if (permission != null && PredefinedConstants.AUDITOR_ROLE.equals(grant))
      {
         List<IModel> pdModel = ModelManagerFactory.getCurrent().getModelsForId(
               PredefinedConstants.PREDEFINED_MODEL_ID);
         return pdModel.get(0).findParticipant(PredefinedConstants.AUDITOR_ROLE);
      }
      
      QName qualifiedGrant = QName.valueOf(grant);
      IModel model = getModel(qualifiedGrant.getNamespaceURI());
      return model == null ? null : model.findParticipant(qualifiedGrant.getLocalPart());
   }

   private IModel getModel(String namespaceURI)
   {
      if (isEmpty(namespaceURI))
      {
         return models.get(0);
      }
      for (IModel model : models)
      {
         if (model.getId().equals(namespaceURI))
         {
            return model;
         }
      }
      ModelManager mm = getModelManager();
      return mm.findActiveModel(namespaceURI);
   }

   public List<IModel> getModels()
   {
      return models;
   }

   public boolean supportsDepartments()
   {
      return permission != null
         && (permission.scope() == Scope.activity
          || permission.scope() == Scope.processDefinition
          || permission.scope() == Scope.workitem
          || permission.scope() == Scope.data);
   }

   public long getScopeProcessOid()
   {
      if (processInstance != null)
      {
         return processInstance.getScopeProcessInstanceOID();
      }
      else if (activityInstance != null)
      {
         return activityInstance.getProcessInstance().getScopeProcessInstanceOID();
      }
      return scopeProcessInstanceOid;
   }

   public long getDepartmentOid()
   {
      return department;
   }

   public IOrganization findOrganization(IDepartment department, IModelParticipant participant)
   {
      if (department == null)
      {
         return null;
      }
      long modelOid = participant.getModel().getModelOID();
      return DepartmentUtils.getOrganization(department, modelOid);
   }

   public static AuthorizationContext create(Method method)
   {
      return new AuthorizationContext(method == null ? null : ClientPermission.getPermission(method));
   }

   public static AuthorizationContext create(ClientPermission permission)
   {
      return new AuthorizationContext(permission);
   }

   public AuthorizationContext getDependency()
   {
      return dependency;
   }

   public ModelElement getModelElement()
   {
      return modelElement;
   }

   public boolean requiresNew()
   {
      return currentPerformer <= 0;
   }

   public boolean isDefaultCaseActivityInstance()
   {
      return activityInstance instanceof ActivityInstanceBean
            && ((ActivityInstanceBean) activityInstance).isDefaultCaseActivityInstance();
   }

   public IDepartment synchronizeDepartment(IOrganization org, List<String> departmentIds)
   {
      String orgId = org.getId();
      long modelOid = org.getModel().getModelOID();

      DeptKey key = new DeptKey(orgId, departmentIds);
      IDepartment department = depts.get(key);
      if (department != null)
      {
         return department;
      }
      if (depts.containsKey(key))
      {
         int size = departmentIds.size();
         String id = departmentIds.get(size - 1);
         String parentId = size > 1 ?  departmentIds.get(size - 2) : null;
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DEPARTMENT_ID2.raise(id, parentId), id);
      }
      else
      {
         try
         {
            department = SynchronizationService.synchronizeDepartment(orgId, modelOid, departmentIds).getFirst();
            depts.put(key, department);
            return department;
         }
         catch (ObjectNotFoundException ex)
         {
            depts.put(key, null);
            throw ex;
         }
      }
   }

   private static class DeptKey
   {
      private String orgId;
      private List<String> subList;

      public DeptKey(String orgId, List<String> subList)
      {
         this.orgId = orgId;
         this.subList = copyList(subList);
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = prime + orgId.hashCode();
         for (int i = 0; i < subList.size(); i++)
         {
            String id = subList.get(i);
            if (id == null)
            {
               break;
            }
            result = prime * result + id.hashCode();
         }
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if (obj == null)
         {
            return false;
         }
         if (getClass() != obj.getClass())
         {
            return false;
         }
         DeptKey other = (DeptKey) obj;
         if (!orgId.equals(other.orgId))
         {
            return false;
         }
         if (subList.size() != other.subList.size())
         {
            return false;
         }
         for (int i = 0; i < subList.size(); i++)
         {
            if (!subList.get(i).equals(other.subList.get(i)))
            {
               return false;
            }
         }
         return true;
      }
   }

   public Collection<IOrganization> getRestricted()
   {
      if (restricted == null)
      {
         // collect from models
         restricted = newSet();
         IModel activeModel = getModelManager().findActiveModel();
         for (int i = 0; i < permissionIds.length; i++)
         {
            restricted.addAll(getRestricted(activeModel, permissionIds[i]));
         }
         Iterator<IModel> models = getModelManager().getAllModels();
         while (models.hasNext())
         {
            IModel model = models.next();
            if (model != activeModel)
            {
               for (int i = 0; i < permissionIds.length; i++)
               {
                  restricted.addAll(getRestricted(model, permissionIds[i]));
               }
            }
         }
         // intersect with user grants
         IUser user = getUser();
         Set<IOrganization> granted = newSet();
         Iterator<UserParticipantLink> grants = user.getAllParticipantLinks();
         while (grants.hasNext())
         {
            UserParticipantLink grant = grants.next();
            granted.addAll(Authorization2.findRestricted(grant.getParticipant()));
         }
         restricted.retainAll(granted);
         if (dependency != null)
         {
            restricted.addAll(dependency.getRestricted());
         }
      }

      return restricted;
   }

   private static Collection<IOrganization> getRestricted(IModel model, String permissionId)
   {
      if (model == null)
      {
         return Collections.emptyList();
      }
      String att = PredefinedConstants.ENGINE_SCOPE + permissionId;
      Set<IOrganization> restricted = (Set<IOrganization>) model.getRuntimeAttribute(att);
      if (restricted == null)
      {
         restricted = newSet();
         ModelElementList definitions = model.getProcessDefinitions();
         for (int i = 0; i < definitions.size(); i++)
         {
            IProcessDefinition definition = (IProcessDefinition) definitions.get(i);
            addRestricted(restricted, model, definition, permissionId);
            ModelElementList activities = definition.getActivities();
            for (int j = 0; j < activities.size(); j++)
            {
               IActivity activity = (IActivity) activities.get(j);
               addRestricted(restricted, model, activity, permissionId);
            }
         }
         ModelElementList datas = model.getData();
         for (int i = 0; i <  datas.size(); i++)
         {
            IData data = (IData) datas.get(i);
            addRestricted(restricted, model, data, permissionId);
         }
         model.setRuntimeAttribute(att, restricted);
      }
      return restricted;
   }

   private static void addRestricted(Set<IOrganization> restricted, IModel model, IdentifiableElement element, String permissionId)
   {
      Set<IModelParticipant> participants = newSet();
      Set<Entry<String, Object>> entrySet = element.getAllAttributes().entrySet();
      for (Entry<String, Object> entry : entrySet)
      {
         String key = entry.getKey();
         if (key != null && key.startsWith(permissionId))
         {
            Object id = entry.getValue();
            if (id instanceof String)
            {
               if (Authorization2.ALL.equals(id))
               {
                  return;
               }
               else if (Authorization2.OWNER.equals(id))
               {
                  if (element instanceof IActivity && ((IActivity) element).isInteractive())
                  {
                     participants.add(((IActivity) element).getPerformer());
                  }
               }
               else
               {
                  participants.add(model.findParticipant((String) id));
               }
            }
         }
      }
      for (IModelParticipant participant : participants)
      {
         restricted.addAll(Authorization2.findRestricted(participant));
      }
   }

   public void setPrefetchedDataValue(String dataId, String dataPath, String value)
   {
      prefetchedValues.put(new Pair<String, String>(dataId, dataPath), value);
      if (dependency != null)
      {
         dependency.setPrefetchedDataValue(dataId, dataPath, value);
      }
   }

   public boolean hasValue(String dataId, String dataPath)
   {
      return prefetchedValues.containsKey(new Pair<String, String>(dataId, dataPath));
   }

   public String getValue(String dataId, String dataPath)
   {
      return prefetchedValues.get(new Pair<String, String>(dataId, dataPath));
   }

   public String getDefaultValue(IData data)
   {
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      long scopeProcessOid = getScopeProcessOid();
      if (session.existsInCache(ProcessInstanceBean.class, new Long(scopeProcessOid)))
      {
         ProcessInstanceBean scopePI = (ProcessInstanceBean) session.findByOID(ProcessInstanceBean.class, scopeProcessOid);
         IDataValue dv = scopePI.getCachedDataValue(data.getId());
         if (dv != null)
         {
            Object value = dv.getValue();
            return value == null ? null : value.toString();
         }
      }
      if (PredefinedConstants.PRIMITIVE_DATA.equals(data.getType().getId()))
      {
         Object value = data.getAttribute(PredefinedConstants.DEFAULT_VALUE_ATT);
         if (value != null)
         {
            return value.toString();
         }
      }
      return null;
   }

   public void setPrefetchDataAvailable(boolean prefetchDataAvailable)
   {
      this.prefetchDataAvailable = prefetchDataAvailable;
      if (dependency != null)
      {
         dependency.setPrefetchDataAvailable(prefetchDataAvailable);
      }
   }

   public boolean isPrefetchDataAvailable()
   {
      return prefetchDataAvailable;
   }

   public List<IDepartment> getSubdepartments(IDepartment department)
   {
      List<IDepartment> list = subdepartments.get(department);
      if (list == null)
      {
         if (department != null)
         {
            Iterator<IDepartment> iterator = DepartmentHierarchyBean.findAllSubDepartments(department);
            while (iterator.hasNext())
            {
               IDepartment dptmt = iterator.next();
               if (dptmt != department)
               {
                  IDepartment parent = dptmt.getParentDepartment();
                  List<IDepartment> subs = subdepartments.get(parent);
                  if (subs == null)
                  {
                     subs = CollectionUtils.newList();
                     subdepartments.put(parent, subs);
                  }
                  if (!subs.contains(dptmt))
                  {
                     subs.add(dptmt);
                  }
               }
            }
         }
         list = subdepartments.get(department);
         if (list == null)
         {
            list = CollectionUtils.<IDepartment>newList();
            subdepartments.put(department, list);
         }
      }
      return list;
   }

   public IDepartment findById(IOrganization org, List<String> departmentIds, IDepartment parent)
   {
      String deptId = departmentIds.get(departmentIds.size() - 1);
      String orgId = org.getId();
      DeptKey key = new DeptKey(orgId, departmentIds);
      IDepartment department = depts.get(key);
      if (department != null)
      {
         return department;
      }
      List<IDepartment> subdepartments = getSubdepartments(parent);
      for (int j = 0; j < subdepartments.size(); j++)
      {
         IDepartment dep = subdepartments.get(j);
         if (deptId.equals(dep.getId()) && org == findOrganization(dep, org))
         {
            department = dep;
            break;
         }
      }
      if (department == null)
      {
         //try to get via id from the database
         try
         {
            department = DepartmentBean.findById(deptId, parent, org);
         }
         catch (ObjectNotFoundException ignored1)
         {
            //try to get from synchronization service
            try
            {
               department = synchronizeDepartment(org, departmentIds);
            }
            catch (ObjectNotFoundException ignored2)
            {
               //use null department for caching so no further lookups on db
               //will be performed
               department = IDepartment.NULL;
            }
         }
         subdepartments.add(department);
      }
      depts.put(key, department);

      //throw exception so the the authorization evaluation will use the parent department
      //for permission checks
      if(department == null || department == IDepartment.NULL)
      {
          throw new ObjectNotFoundException(
                  BpmRuntimeError.ATDB_UNKNOWN_DEPARTMENT_ID.raise(deptId, deptId));
      }
      return department;
   }
}