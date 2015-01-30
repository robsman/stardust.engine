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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Default;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Id;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Scope;

public final class ClientPermission
{
   private static final Logger trace = LogManager.getLogger(ClientPermission.class);

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
      Class[] classes = new Class[] {AdministrationService.class, QueryService.class, UserService.class, WorkflowService.class};
      for (Class<?> cls : classes)
      {
         for (Method method : cls.getMethods())
         {
            initializePermission(map, method);
         }
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
      MODIFY_DEPARTMENTS = initializePermission(map, AdministrationService.class, "modifyDepartment", long.class, String.class, String.class);
      MODIFY_PROCESS_INSTANCES = initializePermission(map, AdministrationService.class, "setProcessInstancePriority", long.class, int.class);
      MODIFY_USER_DATA = initializePermission(map, UserService.class, "modifyUser", User.class);
      PERFORM_ACTIVITY = initializePermission(map, WorkflowService.class, "activate", long.class);
      QUERY_ACTIVITY_INSTANCE_DATA = initializePermission(map, QueryService.class, "getAllActivityInstances", ActivityInstanceQuery.class);
      READ_ACTIVITY_INSTANCE_DATA = initializePermission(map, WorkflowService.class, "getActivityInstance", long.class);
      READ_AUDIT_TRAIL_STATISTICS = initializePermission(map, AdministrationService.class, "getAuditTrailHealthReport");
      READ_DEPARTMENTS = initializePermission(map, AdministrationService.class, "getDepartment", long.class);
      READ_MODEL_DATA = initializePermission(map, QueryService.class, "getModel", long.class);
      READ_PROCESS_INSTANCE_DATA = initializePermission(map, WorkflowService.class, "getProcessInstance", long.class);
      READ_USER_DATA = initializePermission(map, UserService.class, "getUser", long.class);
      RUN_RECOVERY = initializePermission(map, AdministrationService.class, "recoverRuntimeEnvironment");
      SAVE_OWN_PARTITION_SCOPE_PREFERENCES = initializePermission(map, AdministrationService.class, "setGlobalPermissions", RuntimePermissions.class);

      // named permissions without corresponding annotation
      MANAGE_AUTHORIZATION = NULL.clone(Id.manageAuthorization);
      SAVE_OWN_REALM_SCOPE_PREFERENCES = NULL.clone(Id.saveOwnRealmScopePreferences);
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
      if (permission != null)
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

   protected static ClientPermission getPermission(Method method)
   {
      ClientPermission cp = permissionCache.get(method);
      if (cp == null)
      {
         /*
         ExecutionPermission permission = method.getAnnotation(ExecutionPermission.class);
         cp = permission == null ? NULL : new ClientPermission(permission);
         permissionCache.put(method, cp);
         */
         if (trace.isDebugEnabled())
         {
            trace.debug("Missing permission for: " + method);
         }
      }
      return cp == NULL ? null : cp;
   }
}
