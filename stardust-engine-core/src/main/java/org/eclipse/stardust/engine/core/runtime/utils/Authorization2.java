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

import java.lang.reflect.Method;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.permissions.GlobalPermissionConstants;
import org.eclipse.stardust.engine.core.preferences.permissions.PermissionUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ExecutionPlan;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;

/**
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class Authorization2
{
   public static final String PREFIX = "authorization:";
   public static final String DENY_PREFIX = "authorization:deny:";
   public static final String ALL = "__carnot_internal_all_permissions__";
   public static final String OWNER = "__carnot_internal_owner_permission__";

   private static final String[] OWNER_SET = {OWNER};

   public static void checkPermission(Method method, Object[] args)
   {
      UserUtils.clearOnBehalfOf();

      AuthorizationContext context = AuthorizationContext.create(method);
      ClientPermission permission = context.getPermission();
      if (permission != null)
      {
         if (context.isAdminOverride())
         {
            return;
         }

         String requiredGrant = null;
         Authorization2Predicate authorizationPredicate = null;
         switch (permission.scope())
         {
         case activity:
            if (permission.defer())
            {
               authorizationPredicate = new ActivityInstanceAuthorization2Predicate(context);
            }
            else
            {
               long aiOid = getActivityInstanceOid(args[0]);
               if (ExecutionPermission.Id.abortActivityInstances == permission.id() &&
                     (args.length == 1 || AbortScope.RootHierarchy.equals(args[1])))
               {
                  // change context to process instance if you want to abort the complete process hierarchy
                  permission = ClientPermission.ABORT_PROCESS_INSTANCES;

                  IActivityInstance activityInstance = ActivityInstanceBean.findByOID(aiOid);
                  IProcessInstance processInstance = activityInstance.getProcessInstance();
                  IProcessInstance rootPi = processInstance.getRootProcessInstance();

                  context = AuthorizationContext.create(permission);
                  context.setProcessInstance(rootPi);
               }
               else
               {
                  // (fh) for performAdHocTransition methods, the last argument is boolean complete
                  // and requires additional permission check if false.
                  if (method.getName().equals("performAdHocTransition") && !(Boolean) args[args.length - 1])
                  {
                     permission = permission.clone(ExecutionPermission.Id.abortActivityInstances);
                     context = AuthorizationContext.create(permission);
                     TransitionTarget target = (TransitionTarget) args[args.length - 2];
                     if (target != null) // must not throw NPEs here, let them to come from the service implementation
                     {
                        ExecutionPlan plan = new ExecutionPlan(target);
                        aiOid = plan.getRootActivityInstanceOid();
                     }
                  }
                  context.setActivityInstance(ActivityInstanceBean.findByOID(aiOid));
               }
               requiredGrant = checkPermission(context);
            }
            break;
         case data:
            if (permission.defer())
            {
               authorizationPredicate = new DataAuthorization2Predicate(context);
            }
            else
            {
               if (args[0] instanceof String)
               {
                  QName qname = QName.valueOf((String) args[0]);
                  IModel model = ModelManagerFactory.getCurrent().findActiveModel(qname.getNamespaceURI());
                  if (model != null)
                  {
                     IData data = model.findData(qname.getLocalPart());
                     if (data != null)
                     {
                        context.setModelElementData(data);
                     }
                  }
               }
               // TODO: else ?
               requiredGrant = checkPermission(context);
            }
            break;
         case model:
            List<IModel> models = ModelManagerFactory.getCurrent().findActiveModels();
            if (models.isEmpty())
            {
               if ( !SecurityProperties.isInternalAuthorization()
                     || !(context.getUser().hasRole(PredefinedConstants.ADMINISTRATOR_ROLE)))
               {
                  requiredGrant = PredefinedConstants.ADMINISTRATOR_ROLE;
               }
            }
            else
            {
               // (fh) special case for grants modifications permission check
               if (method.getDeclaringClass().equals(UserService.class)
                     && method.getName().equals("modifyUser"))
               {
                  User user = (User) args[0];
                  if (UserUtils.isUserGrantOrGroupModified(user))
                  {
                     ClientPermission xpermission = ClientPermission.MANAGE_AUTHORIZATION;
                     AuthorizationContext xcontext = AuthorizationContext.create(xpermission);
                     xcontext.setModels(models);
                     requiredGrant = checkPermission(xcontext);
                     if (requiredGrant != null || !UserUtils.isUserDataModified(user))
                     {
                        break;
                     }
                  }

                  UserBean user_ = UserBean.findByOid(user.getOID());
                  if(SecurityProperties.isTeamLeader(user_))
                  {
                     break;
                  }
               }

               if (method.getDeclaringClass().equals(WorkflowService.class)
                     && method.getName().equals("joinProcessInstance"))
               {

                  ClientPermission xpermission = ClientPermission.ABORT_PROCESS_INSTANCES;
                  AuthorizationContext xcontext = AuthorizationContext.create(xpermission);
                  authorizationPredicate = new ProcessInstanceAuthorization2Predicate(
                        xcontext);
               }
               else if (method.getDeclaringClass().equals(WorkflowService.class)
                     && method.getName().equals("spawnPeerProcessInstance"))
               {
                  boolean abortProcess = false;
                  if (args.length > 4)
                  {
                     abortProcess = (Boolean) args[4];
                  }
                  else
                  {
                     SpawnOptions options = (SpawnOptions) args[2];
                     abortProcess = options.isAbortProcessInstance();
                  }
                  if (abortProcess)
                  {
                     ClientPermission xpermission = ClientPermission.ABORT_PROCESS_INSTANCES;
                     AuthorizationContext xcontext = AuthorizationContext.create(xpermission);
                     authorizationPredicate = new ProcessInstanceAuthorization2Predicate(xcontext);
                  }
               }

               if (method.getDeclaringClass().equals(AdministrationService.class)
                     && method.getName().equals("savePreferences"))
               {
                  List<Preferences> preferencesList;
                  if (args[0] instanceof List)
                  {
                     preferencesList = (List<Preferences>) args[0];
                  }
                  else
                  {
                     preferencesList = Collections.singletonList((Preferences) args[0]);
                  }

                  for (Preferences preferences : preferencesList)
                  {
                     PreferenceScope scope = null;
                     if (preferences != null)
                     {
                        scope = preferences.getScope();
                     }
                     if (PreferenceScope.REALM.equals(scope))
                     {
                        ClientPermission realmPermission = ClientPermission.SAVE_OWN_REALM_SCOPE_PREFERENCES;
                        AuthorizationContext realmContext = AuthorizationContext.create(realmPermission);
                        realmContext.setModels(models);
                        requiredGrant = checkPermission(realmContext);
                        if (requiredGrant != null)
                        {
                           break;
                        }
                     }
                     else if (PreferenceScope.PARTITION.equals(scope))
                     {
                        AuthorizationContext partitionContext = AuthorizationContext.create(
                              ClientPermission.SAVE_OWN_PARTITION_SCOPE_PREFERENCES);
                        partitionContext.setModels(models);
                        requiredGrant = checkPermission(partitionContext);
                        if (requiredGrant != null)
                        {
                           break;
                        }
                     }
                     else if ( !PreferenceScope.DEFAULT.equals(scope))
                     {
                        context.setModels(models);
                        requiredGrant = checkPermission(context);
                     }
                     break;
                  }
               }
               else
               {
                  context.setModels(models);
                  requiredGrant = checkPermission(context);
               }

               if (ExecutionPermission.Id.manageDeputies == permission.id())
               {
                  IUser user = context.getUser();
                  // 1st attribute is the user, 2nd deputy user
                  User userDetails = (User) args[0];

                  if(userDetails.getAccount().equals(user.getAccount())
                        && userDetails.getRealm().getId().equals(user.getRealm().getId()))
                  {
                     requiredGrant = null;
                  }
               }
            }
            break;
         case processDefinition:
            if (permission.defer())
            {
               authorizationPredicate = new ProcessInstanceAuthorization2Predicate(context);
            }
            else
            {
               if (args[0] instanceof String)
               {
                  IProcessDefinition pd = ModelUtils.getProcessDefinition((String) args[0]);
                  context.setModelElementData(pd);
               }
               else
               {
                  IProcessInstance pi = null;
                  if (args[0] instanceof ProcessInstanceAttributes)
                  {
                     ProcessInstanceAttributes pib = (ProcessInstanceAttributes) args[0];
                     pi = ProcessInstanceBean.findByOID(pib.getProcessInstanceOid());
                  }
                  else
                  {
                     pi = ProcessInstanceBean.findByOID((Long) args[0]);
                     if (ExecutionPermission.Id.abortProcessInstances == permission.id() &&
                           AbortScope.RootHierarchy.equals(args[args.length - 1]))
                     {
                        // change to root process instance if you want to abort the complete process hierarchy
                        pi = pi.getRootProcessInstance();
                     }
                  }
                  context.setProcessInstance(pi);
               }
               requiredGrant = checkPermission(context);
            }
            break;
         case workitem:
            if (permission.defer())
            {
               authorizationPredicate = new WorkItemAuthorization2Predicate(context);
            }
            else
            {
               // TODO: If necessary, then how to implement it?
            }
            break;
         }
         if (requiredGrant != null)
         {
            IUser user = context.getUser();
            throw new AccessForbiddenException(BpmRuntimeError.AUTHx_AUTH_MISSING_GRANTS.raise(
                  user.getOID(), String.valueOf(permission), user.getAccount()));
         }
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         rtEnv.setSecureContext(true);
         if (authorizationPredicate != null)
         {
            rtEnv.setAuthorizationPredicate(authorizationPredicate);
         }
      }
   }

   protected static long getActivityInstanceOid(Object arg)
   {
      if (arg instanceof Long)
      {
         return ((Long) arg).longValue();
      }
      else if (arg instanceof ActivityInstanceContextAware)
      {
         return ((ActivityInstanceContextAware) arg).getActivityInstanceOid();
      }
      throw new ObjectNotFoundException(
            BpmRuntimeError.ATDB_UNKNOWN_ACTIVITY_INSTANCE_OID.raise(arg));
   }

   public static boolean hasPermission(AuthorizationContext context)
   {
      if (context.isAdminOverride())
      {
         return true;
      }
      String requiredGrant = checkPermission(context);
      return requiredGrant == null;
   }

   private static String checkPermission(AuthorizationContext context)
   {
      // global override only for changeable permissions
      if (context.getPermission().changeable())
      {
         if (hasAnyOf(context.getPermission().getDeniedIds(), context))
         {
            // globally denied
            return PredefinedConstants.ADMINISTRATOR_ROLE;
         }
         if (hasAnyOf(context.getPermission().getAllowedIds(), context))
         {
            // globally allowed
            return null;
         }
      }
      String[] grants = context.getGrants();
      boolean ownerPresent = false;
      boolean allPresent = false;
      for (int i = 0; i < grants.length; i++)
      {
         if (ALL.equals(grants[i]))
         {
            allPresent = true;
         }
         if (OWNER.equals(grants[i]))
         {
            ownerPresent = true;
         }
      }
      AuthorizationContext dependent = context.getDependency();
      if (dependent != null)
      {
         String dependentGrant = checkPermission(dependent);
         if (dependentGrant != null)
         {
            if (ownerPresent)
            {
               grants = OWNER_SET;
            }
            else
            {
               return dependentGrant;
            }
         }
      }
      if (allPresent)
      {
         return null;
      }
      for (int i = 0; i < grants.length; i++)
      {
         if (hasGrant(grants[i], context))
         {
            return null;
         }
      }
      return grants.length == 0 ? PredefinedConstants.ADMINISTRATOR_ROLE : grants[0];
   }

   private static boolean hasAnyOf(List<String> permissions, AuthorizationContext context)
   {
      for (String id : permissions)
      {
         if (hasPermission(id, context))
         {
            return true;
         }
      }
      return false;
   }

   private static boolean hasPermission(String permissionId, AuthorizationContext context)
   {
      IPreferenceStorageManager preferenceStore = PreferenceStorageFactory.getCurrent();
      if (preferenceStore != null)
      {
         List<String> grants = PermissionUtils.getScopedGlobalPermissionValues(preferenceStore, permissionId, false);
         if (!grants.isEmpty())
         {
            for (String grant : grants)
            {
               if (hasGrant(grant, context))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   private static boolean hasGrant(String grant, AuthorizationContext context)
   {
      if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(grant))
      {
         if (context.getPermission().administratorOverride())
         {
            // we already know it's not admin
            return false;
         }
      }
      if (OWNER.equals(grant))
      {
         if (context.isUserPerformer())
         {
            return context.userCanPerform();
         }
      }
      IModelParticipant participant = context.getParticipant(grant);
      if (context.checkRole(participant))
      {
         if (!context.supportsDepartments() ||
               (!context.isPrefetchDataAvailable() && context.getScopeProcessOid() == 0))
         {
            return true;
         }
         List<IOrganization> restrictions = findRestricted(participant);
         if (restrictions.isEmpty())
         {
            return true;
         }
         else
         {
            long department = getTargetDepartmentOid(context, restrictions, context.requiresNew());
            Iterator<UserParticipantLink> links = context.getUser().getAllParticipantLinks();
            List<Pair<IDepartment, Long>> deps = CollectionUtils.newList();
            // make a first iteration to check for a "perfect" match
            while (links.hasNext())
            {
               UserParticipantLink link = links.next();
               IModelParticipant grantedParticipant = link.getParticipant();
               if (grantedParticipant == participant || participant.isAuthorized(grantedParticipant))
               {
                  IDepartment dptmt = link.getDepartment();
                  if (dptmt == null)
                  {
                     if (department == 0)
                     {
                        UserUtils.setOnBehalfOf(link.getOnBehalfOf());
                        return true;
                     }
                  }
                  else
                  {
                     if (department == dptmt.getOID())
                     {
                        UserUtils.setOnBehalfOf(link.getOnBehalfOf());
                        return true;
                     }
                     else
                     {
                        deps.add(new Pair(dptmt, link.getOnBehalfOf()));
                     }
                  }
               }
            }
            if (department != 0)
            {
               // no direct matches, check hierarchy now.
               IDepartment targetDepartment = DepartmentBean.findByOID(department);
               IOrganization targetOrganization = context.findOrganization(targetDepartment, participant);
               if (targetOrganization == restrictions.get(restrictions.size() - 1))
               {
                  for (Pair<IDepartment, Long> pair : deps)
                  {
                     IDepartment dptmt = pair.getFirst();
                     while (dptmt != null)
                     {
                        dptmt = dptmt.getParentDepartment();
                        if (dptmt != null && department == dptmt.getOID())
                        {
                           UserUtils.setOnBehalfOf(pair.getSecond());
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }
      return false;
   }

   public static long getTargetDepartmentOid(AuthorizationContext context,
         List<IOrganization> restrictions, boolean forceNew)
   {
      IOrganization targetOrganization = restrictions.get(restrictions.size() - 1);
      long department = context.getDepartmentOid();
      if (forceNew || department > 0)
      {
         IDepartment referenceDepartment = department == 0 ? null : DepartmentBean.findByOID(department);
         IOrganization referenceOrganization = context.findOrganization(referenceDepartment, targetOrganization);
         IDepartment targetDepartment = null;
         if (referenceOrganization == targetOrganization)
         {
            targetDepartment = referenceDepartment;
         }
         else if (DepartmentUtils.isChild(referenceOrganization, targetOrganization))
         {
            targetDepartment = findParentDepartment(context, referenceDepartment, targetOrganization);
         }
         if (targetDepartment == null)
         {
            long evaluatedDepartmentOid = evaluateData(context, restrictions);
            if (evaluatedDepartmentOid > 0 && DepartmentUtils.isChild(targetOrganization, referenceOrganization))
            {
               IDepartment evaluatedDepartment = DepartmentBean.findByOID(evaluatedDepartmentOid);
               if (DepartmentUtils.isChild(evaluatedDepartment, referenceDepartment))
               {
                  department = evaluatedDepartmentOid;
               }
               else
               {
                  department = referenceDepartment.getOID();
               }
            }
            else
            {
               department = evaluatedDepartmentOid;
            }
         }
         else
         {
            department = targetDepartment.getOID();
         }
      }
      return department;
   }

   private static IDepartment findParentDepartment(AuthorizationContext context, IDepartment department, IOrganization organization)
   {
      IOrganization org = context.findOrganization(department, organization);
      if (org == organization)
      {
         return department;
      }
      return findParentDepartment(context, department.getParentDepartment(), organization);
   }

   public static long evaluateData(AuthorizationContext context, List<IOrganization> restrictions)
   {
      IModel model = context.getModels().get(0);
      List<String> departmentIds = CollectionUtils.newList(restrictions.size());
      if (context.isPrefetchDataAvailable())
      {
         for (IOrganization restrictedParticipant : restrictions)
         {
            String dataId = restrictedParticipant.getStringAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);

            IData dataObject = model.findData(dataId);
            if (dataObject == null)
            {
               throw new InternalException("No data '" + dataId
                     + "' available for department retrieval.");
            }

            String qualifiedId = new QName(dataObject.getModel().getId(), dataId).toString();
            String dataPath = restrictedParticipant.getStringAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT);
            if (context.hasValue(qualifiedId, dataPath))
            {
               departmentIds.add(context.getValue(qualifiedId, dataPath));
            }
            else
            {
               departmentIds.add(context.getDefaultValue(dataObject));
            }
         }
      }
      else
      {
         List<IData> data = CollectionUtils.newList(restrictions.size());
         List<String> dataPaths = CollectionUtils.newList(restrictions.size());
         Set<IData> dataSet = CollectionUtils.newSet();
         for (IOrganization restrictedParticipant : restrictions)
         {
            String dataId = restrictedParticipant.getStringAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
            IData dataObject = model.findData(dataId);
            if (dataObject == null)
            {
               throw new InternalException("No data '" + dataId
                     + "' available for department retrieval.");
            }
            data.add(dataObject);
            dataSet.add(dataObject);
            dataPaths.add(restrictedParticipant.getStringAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT));
            departmentIds.add(null);
         }

         List<IDataValue> values = DataValueBean.findAllForProcessInstance(context.getScopeProcessOid(), model, dataSet);
         if (dataSet.size() != values.size())
         {
            throw new InternalException("Could not fetch all data values required for department retrieval.");
         }
         Map<IData, Object> mappedValues = CollectionUtils.newMap();
         for (IDataValue dataValue : values)
         {
            mappedValues.put(dataValue.getData(), dataValue.getValue());
         }
         for (int i = 0; i < data.size(); i++)
         {
            IData dataObject = data.get(i);
            String dataPath = dataPaths.get(i);
            Object value = mappedValues.get(dataObject);
            Object evaluate = evaluateDataPath(context, dataObject, dataPath, value);
            departmentIds.set(i, evaluate == null ? null : evaluate.toString());
         }
      }

      String deptId = departmentIds.get(0);
      if (StringUtils.isEmpty(deptId))
      {
         return 0;
      }
      IDepartment department = null;
      IOrganization org = restrictions.get(0);
      try
      {
         for (int i = 0; i < departmentIds.size(); i++)
         {
            String id = departmentIds.get(i);
            if (StringUtils.isEmpty(id))
            {
               break;
            }
            org = restrictions.get(i);
            department = context.findById(org, departmentIds.subList(0, i + 1), department);
         }
      }
      catch (ObjectNotFoundException ex)
      {
         // here to be ignored
      }
      return department == null ? 0 : department.getOID();
   }

   public static List<IOrganization> findRestricted(IModelParticipant participant)
   {
      List<IOrganization> restrictions = CollectionUtils.newList();
      findRestricted(restrictions, participant);
      Collections.reverse(restrictions);
      return restrictions;
   }

   private static Object evaluateDataPath(AuthorizationContext context,
            final IData dataObject, String dataPath, final Object dataValue)
      {
         ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(dataObject, dataPath);
         SymbolTable symbolTable = new SymbolTable()
         {
            public Object lookupSymbol(String name)
            {
               if (name.equals(dataObject.getId()))
               {
                  return dataValue;
               }
               return null;
            }

            public AccessPoint lookupSymbolType(String name)
            {
               if (name.equals(dataObject.getId()))
               {
                  return dataObject;
               }
               return null;
            }
         };
         AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(symbolTable, context.getScopeProcessOid());
         return evaluator.evaluate(dataObject, dataValue, dataPath, evaluationContext);
      }

   private static void findRestricted(List<IOrganization> restrictions, IModelParticipant participant)
   {
      if (participant instanceof IOrganization
            && participant.getBooleanAttribute(PredefinedConstants.BINDING_ATT))
      {
         restrictions.add((IOrganization) participant);
      }
      Iterator<IOrganization> organizations = participant.getAllOrganizations();
      if (organizations.hasNext())
      {
         findRestricted(restrictions, organizations.next());
      }
   }

   public static List<Permission> getPermissions(Class<? extends Service> cls)
   {
      List<Permission> perms = new ArrayList<Permission>();
      boolean guarded = isGuarded(cls);

      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel activeModel = modelManager.findActiveModel();
      Map<ExecutionPermission.Scope, Set<ExecutionPermission.Id>> processed = CollectionUtils.newMap();
      Map<ModelElement, Scope> scopesMap = CollectionUtils.newMap();
      Method[] methods = cls.getMethods();
      for (Method method : methods)
      {
         AuthorizationContext ctx = AuthorizationContext.create(method);
         ClientPermission permission = ctx.getPermission();
         if (permission != null)
         {
            if (!isPermissionProcessed(processed, permission))
            {
               List<Scope> scopes = new ArrayList<Scope>();
               for (Iterator models = modelManager.getAllModels(); models.hasNext();)
               {
                  IModel model = (IModel) models.next();
                  Scope modelScope = getScope(model, null, scopesMap);
                  if (permission.scope() == ExecutionPermission.Scope.model && activeModel != null)
                  {
                     addScope(guarded, ctx, scopes, activeModel, activeModel, modelScope);
                  }
                  else
                  {
                     ModelElementList definitions = model.getProcessDefinitions();
                     for (int definitionCounter = 0; definitionCounter < definitions.size(); definitionCounter++)
                     {
                        IProcessDefinition pd = (IProcessDefinition) definitions.get(definitionCounter);
                        Scope processScope = getScope(pd, modelScope, scopesMap);
                        if (permission.scope() == ExecutionPermission.Scope.processDefinition)
                        {
                           addScope(guarded, ctx, scopes, model, pd, processScope);
                        }
                        else if (permission.scope() == ExecutionPermission.Scope.activity)
                        {
                           ModelElementList activities = pd.getActivities();
                           for (int activityCounter = 0; activityCounter < activities.size(); activityCounter++)
                           {
                              ModelElement modelElement = activities.get(activityCounter);
                              Scope activityScope = getScope(modelElement, processScope, scopesMap);
                              addScope(guarded, ctx, scopes, model, modelElement, activityScope);
                           }
                        }
                     }
                  }
               }
               if (!scopes.isEmpty())
               {
                  String permissionId = ctx.getPermissionId();
                  Permission perm = new Permission(permissionId.substring(PREFIX.length()), scopes);
                  perms.add(perm);
               }
            }
         }
      }

      return perms;
   }

   private static void addScope(boolean guarded, AuthorizationContext ctx,
         List<Scope> scopes, IModel model, ModelElement modelElement, Scope scope)
   {
      if (guarded)
      {
         ctx.setModelElementData(modelElement);
         if (!hasPermission(ctx))
         {
            return;
         }
      }
      scopes.add(scope);
   }

   private static Scope getScope(ModelElement element, Scope parent,
         Map<ModelElement, Scope> scopesMap)
   {
      Scope scope = scopesMap.get(element);
      if (scope == null)
      {
         if (element instanceof IActivity && parent instanceof ProcessScope)
         {
            scope = new ActivityScope((ProcessScope) parent, ((IActivity) element).getId());
         }
         else if (element instanceof IProcessDefinition && parent instanceof ModelScope)
         {
            scope = new ProcessScope((ModelScope) parent, ((IProcessDefinition) element).getId());
         }
         else if (element instanceof IModel && parent == null)
         {
            scope = new ModelScope(((IModel) element).getModelOID());
         }
         if (scope != null)
         {
            scopesMap.put(element, scope);
         }
      }
      return scope;
   }

   private static boolean isGuarded(Class< ? extends Service> cls)
   {
      String serviceName = cls.getName();
      int ix = serviceName.lastIndexOf('.');
      if (ix > 0)
      {
         serviceName = serviceName.substring(ix + 1);
      }
      return Parameters.instance().getBoolean(serviceName + ".Guarded", true);
   }

   private static boolean isPermissionProcessed(Map<ExecutionPermission.Scope, Set<ExecutionPermission.Id>> processed,
         ClientPermission permission)
   {
      Set<ExecutionPermission.Id> ids = processed.get(permission.scope());
      if (ids == null)
      {
         ids = CollectionUtils.newSet();
         ids.add(permission.id());
         processed.put(permission.scope(), ids);
      }
      else
      {
         if (ids.contains(permission.id()))
         {
            return true;
         }
         else
         {
            ids.add(permission.id());
         }
      }
      return false;
   }

   public static List<Permission> getGlobalPermissions()
   {
      return getPermissions(GlobalPermissionSpecificService.class);
   }

   /**
    * Class is used to list all possible global permissions. With it the permissions
    * can be evaluated by <code>Authorization2</code> class.
    * @author sven.rottstock
    * @see GlobalPermissionConstants
    */
   static interface GlobalPermissionSpecificService extends Service
   {
      @ExecutionPermission(id=ExecutionPermission.Id.manageAuthorization)
      Permission getManageAuthorizationPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.manageDeputies)
      Permission getManageDeputiesPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.controlProcessEngine)
      Permission getControlProcessEnginePermission();

      @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
      Permission getDeployProcessModelPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.forceSuspend)
      Permission getForceSuspendPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.manageDaemons)
      Permission getManageDaemonsPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrail)
      Permission getModifyAuditTrailPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrailStatistics)
      Permission getModifyAuditTrailStatisticsPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.modifyDepartments)
      Permission getModifyDepartmentsPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.modifyUserData)
      Permission getModifyUserDataPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.modifyDmsData)
      Permission getModifyDmsDataPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.readAuditTrailStatistics)
      Permission getReadAuditTrailStatisticsPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.createCase)
      Permission getCreateCasePermission();

      @ExecutionPermission(id=ExecutionPermission.Id.readDepartments)
      Permission getReadDepartmentsPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.readModelData)
      Permission getReadModelDataPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.readUserData)
      Permission getReadUserDataPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.resetUserPassword)
      Permission getResetUserPasswordPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.runRecovery)
      Permission getRunRecoveryPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.saveOwnUserScopePreferences)
      Permission getSaveOwnUserScopePreferencesPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.saveOwnRealmScopePreferences)
      Permission getSaveOwnRealmScopePreferencesPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.saveOwnPartitionScopePreferences)
      Permission getSaveOwnPartitionScopePreferencesPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.joinProcessInstance)
      Permission getJoinProcessInstancePermission();

      @ExecutionPermission(id=ExecutionPermission.Id.spawnPeerProcessInstance)
      Permission getSpawnPeerProcessInstancePermission();

      @ExecutionPermission(id=ExecutionPermission.Id.spawnSubProcessInstance)
      Permission getSpawnSubProcessInstancePermission();

      @ExecutionPermission(id=ExecutionPermission.Id.deployRuntimeArtifact)
      Permission getDeployRuntimeArtifactPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.readRuntimeArtifact)
      Permission getReadRuntimeArtifactPermission();
   }
}