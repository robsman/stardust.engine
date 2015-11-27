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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2.GlobalPermissionSpecificService;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Default;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Id;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Scope;

public final class ClientPermission
{
// private static final Logger trace = LogManager.getLogger(ClientPermission.class);

   private static final String DENY_PREFIX = "deny:";

   static final ClientPermission NULL = new ClientPermission(null, Scope.model, new Default[] {Default.ADMINISTRATOR},
         new Default[] {}, true, true, false, new Id[] {});

   private static final Map<Method, ClientPermission> permissionCache;

   // activity scope permissions
   public static final ClientPermission ABORT_ACTIVITY_INSTANCES;
   public static final ClientPermission DELEGATE_TO_OTHER;
   public static final ClientPermission PERFORM_ACTIVITY;
   public static final ClientPermission READ_ACTIVITY_INSTANCE_DATA;
   public static final ClientPermission QUERY_ACTIVITY_INSTANCE_DATA;

   // process scope permissions
   public static final ClientPermission ABORT_PROCESS_INSTANCES;
   public static final ClientPermission MODIFY_CASE;
   public static final ClientPermission MODIFY_PROCESS_INSTANCES;
   public static final ClientPermission READ_PROCESS_INSTANCE_DATA;

   // data scope permissions
   public static final ClientPermission MODIFY_DATA_VALUE;
   public static final ClientPermission READ_DATA_VALUE;

   // global scope permissions
   public static final ClientPermission CONTROL_PROCESS_ENGINE;
   public static final ClientPermission DEPLOY_PROCESS_MODEL;
   public static final ClientPermission MANAGE_AUTHORIZATION;
   public static final ClientPermission MANAGE_DAEMONS;
   public static final ClientPermission MODIFY_AUDIT_TRAIL;
   public static final ClientPermission MODIFY_AUDIT_TRAIL_UNCHANGEABLE;
   public static final ClientPermission MODIFY_DEPARTMENTS;
   public static final ClientPermission MODIFY_USER_DATA;
   public static final ClientPermission READ_AUDIT_TRAIL_STATISTICS;
   public static final ClientPermission READ_DEPARTMENTS;
   public static final ClientPermission READ_MODEL_DATA;
   public static final ClientPermission READ_USER_DATA;
   public static final ClientPermission RUN_RECOVERY;
   public static final ClientPermission SAVE_OWN_PARTITION_SCOPE_PREFERENCES;
   public static final ClientPermission SAVE_OWN_REALM_SCOPE_PREFERENCES;

   static {
      permissionCache = new HashMap<Method, ClientPermission>();

      // temporary map
      HashMap<ExecutionPermission, ClientPermission> map = new HashMap<ExecutionPermission, ClientPermission>();

      // cache permissions for all public methods.
      Class[] classes = new Class[] {WorkflowService.class, QueryService.class, UserService.class,
            DocumentManagementService.class, AdministrationService.class, GlobalPermissionSpecificService.class};
      for (Class<?> cls : classes)
      {
         initializeClass(map, cls);
      }

      // additional permission used by delegation
      initializePermission(map, ActivityInstanceBean.class, "delegateToParticipant", IModelParticipant.class, IDepartment.class, IDepartment.class);

      // named permissions
      ABORT_ACTIVITY_INSTANCES = initializePermission(map, WorkflowService.class, "abortActivityInstance", long.class);
      ABORT_PROCESS_INSTANCES = initializePermission(map, AdministrationService.class, "abortProcessInstance", long.class);
      CONTROL_PROCESS_ENGINE = initializePermission(map, AdministrationService.class, "flushCaches");
      DELEGATE_TO_OTHER = initializePermission(map, WorkflowService.class, "delegateToDefaultPerformer", long.class);
      DEPLOY_PROCESS_MODEL = initializePermission(map, AdministrationService.class, "deployModel", List.class, DeploymentOptions.class);
      MANAGE_DAEMONS = initializePermission(map, AdministrationService.class, "getDaemon", String.class, boolean.class);
      MODIFY_AUDIT_TRAIL = initializePermission(map, AdministrationService.class, "cleanupRuntimeAndModels");
      MODIFY_AUDIT_TRAIL_UNCHANGEABLE = initializePermission(map, AdministrationService.class, "deleteProcesses", List.class);
      MODIFY_CASE = initializePermission(map, WorkflowService.class, "joinCase", long.class, long[].class);
      MODIFY_DATA_VALUE = initializePermission(map, IProcessInstance.class, "setOutDataValue", IData.class, String.class, Object.class);
      MODIFY_DEPARTMENTS = initializePermission(map, AdministrationService.class, "modifyDepartment", long.class, String.class, String.class);
      MODIFY_PROCESS_INSTANCES = initializePermission(map, AdministrationService.class, "setProcessInstancePriority", long.class, int.class);
      MODIFY_USER_DATA = initializePermission(map, UserService.class, "modifyUser", User.class);
      PERFORM_ACTIVITY = initializePermission(map, WorkflowService.class, "activate", long.class);
      QUERY_ACTIVITY_INSTANCE_DATA = initializePermission(map, QueryService.class, "getAllActivityInstances", ActivityInstanceQuery.class);
      READ_ACTIVITY_INSTANCE_DATA = initializePermission(map, WorkflowService.class, "getActivityInstance", long.class);
      READ_AUDIT_TRAIL_STATISTICS = initializePermission(map, AdministrationService.class, "getAuditTrailHealthReport");
      READ_DEPARTMENTS = initializePermission(map, AdministrationService.class, "getDepartment", long.class);
      READ_DATA_VALUE = initializePermission(map, IProcessInstance.class, "getInDataValue", IData.class, String.class);
      READ_MODEL_DATA = initializePermission(map, QueryService.class, "getModel", long.class);
      READ_PROCESS_INSTANCE_DATA = initializePermission(map, WorkflowService.class, "getProcessInstance", long.class);
      READ_USER_DATA = initializePermission(map, UserService.class, "getUser", long.class);
      RUN_RECOVERY = initializePermission(map, AdministrationService.class, "recoverRuntimeEnvironment");
      SAVE_OWN_PARTITION_SCOPE_PREFERENCES = initializePermission(map, AdministrationService.class, "setGlobalPermissions", RuntimePermissions.class);

      // named permissions without corresponding method annotation
      MANAGE_AUTHORIZATION = initializePermission(map, GlobalPermissionSpecificService.class, "getManageAuthorizationPermission");
      SAVE_OWN_REALM_SCOPE_PREFERENCES = initializePermission(map, GlobalPermissionSpecificService.class, "getSaveOwnRealmScopePreferencesPermission");
   }

   public static void initializeClass(
         HashMap<ExecutionPermission, ClientPermission> map, Class< ? > cls)
   {
      for (Method method : cls.getMethods())
      {
         initializePermission(map, method);
      }
   }

   private static ClientPermission initializePermission(HashMap<ExecutionPermission, ClientPermission> map, Class<?> cls, String name, Class<?>... args)
   {
      try
      {
         return initializePermission(map, cls.getMethod(name, args));
      }
      catch (Exception e)
      {
         // ignore
      }
      return NULL;
   }

   private static ClientPermission initializePermission(HashMap<ExecutionPermission, ClientPermission> map, Method method)
   {
      ClientPermission cp = NULL;
      ExecutionPermission permission = method.getAnnotation(ExecutionPermission.class);
      if (permission == null)
      {
         System.err.println("Missing permission for: " + method);
         cp = null;
      }
      else if (permission.id() != ExecutionPermission.Id.none)
      {
         cp = map.get(permission);
         if (cp == null)
         {
            cp = new ClientPermission(permission);
            map.put(permission, cp);
         }
      }

      if (!permissionCache.containsKey(method))
      {
         permissionCache.put(method, cp);
      }
      return cp;
   }

   private final Id id;
   private final Scope scope;
   private final Default[] defaults;
   private final Default[] fixed;
   private final boolean changeable;
   private final boolean administratorOverride;
   private final boolean defer;
   private final Id[] implied;

   private final String uniqueKey;

   private List<String> allowedIds;
   private List<String> deniedIds;

   public ClientPermission(ExecutionPermission permission)
   {
      this(permission.id(), permission.scope(), permission.defaults(), permission.fixed(),
            permission.changeable(), permission.administratorOverride(), permission.defer(), permission.implied());
   }

   public ClientPermission clone(Id newId)
   {
      return new ClientPermission(newId, scope, defaults, fixed, changeable, administratorOverride, defer, implied);
   }

   private ClientPermission(Id id, Scope scope, Default[] defaults, Default[] fixed,
         boolean changeable, boolean administratorOverride, boolean defer, Id[] implied)
   {
      this.id = id;
      this.scope = scope;
      this.defaults = defaults;
      this.changeable = changeable;
      this.administratorOverride = administratorOverride;
      this.fixed = fixed;
      this.implied = implied;
      this.defer = defer;
      this.uniqueKey = createUniqueKey();

      String[] allowedIds = new String[1 + implied.length];
      allowedIds[0] = createId(this.id);
      for (int i = 1; i < allowedIds.length; i++)
      {
         allowedIds[i] = createId(this.implied[i - 1]);
      }
      this.allowedIds = Collections.unmodifiableList(Arrays.asList(allowedIds));

      String[] deniedIds = new String[allowedIds.length];
      for (int i = 0; i < allowedIds.length; i++)
      {
         deniedIds[i] = DENY_PREFIX + allowedIds[i];
      }
      this.deniedIds = Collections.unmodifiableList(Arrays.asList(deniedIds));
   }

   protected String createId(Id id)
   {
      if (id == null)
      {
         return "";
      }
      switch (scope)
      {
      case model:
         return id.name();
      case workitem:
         return Scope.activity.name() + '.' + id.name();
      default:
         return scope.name() + '.' + id.name();
      }
   }

   private String createUniqueKey()
   {
      if (id == null)
      {
         return "";
      }
      StringBuilder sb = new StringBuilder();
      sb.append(Authorization2.PREFIX)
        .append(scope)
        .append('.')
        .append(id)
        .append(':');
      if (defaults != null && defaults.length > 0)
      {
         sb.append(Arrays.toString(defaults));
      }
      sb.append(':');
      if (fixed != null && fixed.length > 0)
      {
         sb.append(Arrays.toString(fixed));
      }
      sb.append(':')
        .append(changeable ? "T" : "F")
        .append(':')
        .append(administratorOverride ? "T" : "F")
        .append(':')
        .append(defer ? "T" : "F")
        .append(':');
      if (implied != null && implied.length > 0)
      {
         sb.append(Arrays.toString(implied));
      }
      return sb.toString();
   }

   public Id id()
   {
      return id;
   }

   public Scope scope()
   {
      return scope;
   }

   public Default[] defaults()
   {
      return defaults;
   }

   public Default[] fixed()
   {
      return fixed;
   }

   public boolean changeable()
   {
      return changeable;
   }

   public boolean administratorOverride()
   {
      return administratorOverride;
   }

   public boolean defer()
   {
      return defer;
   }

   public Id[] implied()
   {
      return implied;
   }

   public String uniqueKey()
   {
      return uniqueKey;
   }

   public String toString()
   {
      return scope.name() + '.' + id;
   }

   protected List<String> getAllowedIds()
   {
      return allowedIds;
   }

   protected List<String> getDeniedIds()
   {
      return deniedIds;
   }

   public static boolean isDeniedPermissionId(String permissionId)
   {
      return permissionId != null && permissionId.startsWith(DENY_PREFIX);
   }

   protected static ClientPermission getPermission(Method method)
   {
      ClientPermission cp = permissionCache.get(method);
      if (cp == null)
      {
         throw new InternalException("Unauthorized method access: " + method);
      }
      return cp == NULL ? null : cp;
   }

   public static Map<String, String> getGlobalDefaults()
   {
      HashMap<String, String> defaultPermissions = new HashMap<String, String>();
      Set<ExecutionPermission.Id> readOnlyPermissions = getReadOnlyPermissions();
      for (ClientPermission permission : permissionCache.values())
      {
         addDefaultPermission(defaultPermissions, permission);
         addDefaultAuditorPermission(defaultPermissions, permission, readOnlyPermissions);
      }
      addDefaultPermission(defaultPermissions, MANAGE_AUTHORIZATION);
      addDefaultPermission(defaultPermissions, SAVE_OWN_REALM_SCOPE_PREFERENCES);

      return defaultPermissions;
   }

   private static void addDefaultAuditorPermission(
         HashMap<String, String> defaultPermissions, ClientPermission permission,
         Set<ExecutionPermission.Id> readOnlyPermissions)
   {
      if (permission != NULL && permission.changeable() && permission.scope() != Scope.model)
      {
         String permissionId = permission.toString();
         if (!readOnlyPermissions.contains(permission.id()))
         {
            permissionId = DENY_PREFIX + permissionId;
         }
         defaultPermissions.put(permissionId, PredefinedConstants.QUALIFIED_AUDITOR_ID);
      }
   }

   private static Set<ExecutionPermission.Id> getReadOnlyPermissions()
   {
      Set<String> readOnlyPermissionIds = new HashSet<String>();
      Class<ExecutionPermission.Id> obj = ExecutionPermission.Id.class;
      Id[] ids = obj.getEnumConstants();
      Field[] fields = obj.getFields();
      for (Field field : fields)
      {
         if (field.isAnnotationPresent(ExecutionPermission.ReadOnly.class))
         {
            readOnlyPermissionIds.add(field.getName());
         }
      }
      Set<ExecutionPermission.Id> readOnlyPermissions = new HashSet<ExecutionPermission.Id>();
      for (ExecutionPermission.Id id : ids)
      {
         if (readOnlyPermissionIds.contains(id.name()))
         {
            readOnlyPermissions.add(id);
         }
      }
      return readOnlyPermissions;
   }

   protected static void addDefaultPermission(HashMap<String, String> defaultPermissions,
         ClientPermission permission)
   {
      if (permission != NULL && permission.changeable() && permission.scope() == Scope.model)
      {
         String id = permission.allowedIds.get(0);
         String existing = defaultPermissions.get(id);
         if (existing == null || PredefinedConstants.ADMINISTRATOR_ROLE.equals(existing))
         {
            Default[] defaults = permission.defaults();
            String name = getName(defaults == null || defaults.length == 0 ? null : defaults[0]);
            defaultPermissions.put(id, name);
         }
      }
   }

   private static String getName(Default def)
   {
      if (def != null)
      {
         switch (def)
         {
         case ALL: return Authorization2.ALL;
         case OWNER: return Authorization2.OWNER;
         }
      }
      return PredefinedConstants.ADMINISTRATOR_ROLE;
   }
}
