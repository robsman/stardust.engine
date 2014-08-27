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
package org.eclipse.stardust.engine.core.preferences.permissions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;

/**
 * Contains all global permission ids which can be changed at runtime using the API.
 *
 * @author roland.stamm
 *
 */
public class GlobalPermissionConstants extends RuntimePermissionConstants
{
   private GlobalPermissionConstants()
   {
     // utility class
   }

   /**
    * permission to flush the cache
    */
   public static final String GLOBAL_CONTROL_PROCESS_ENGINE = ExecutionPermission.Id.controlProcessEngine.name();

   /**
    * permission to deploy a process model to the engine
    */
   public static final String GLOBAL_DEPLOY_PROCESS_MODEL = ExecutionPermission.Id.deployProcessModel.name();

   /**
    * permission to suspend an activity of another user
    */
   public static final String GLOBAL_FORCE_SUSPEND = ExecutionPermission.Id.forceSuspend.name();

   /**
    * permission to start, stop and query the state of the daemons
    */
   public static final String GLOBAL_MANAGE_DAEMONS = ExecutionPermission.Id.manageDaemons.name();

   /**
    * permission to modify the AuditTrail database
    */
   public static final String GLOBAL_MODIFY_AUDIT_TRAIL = ExecutionPermission.Id.modifyAuditTrail.name();

   /**
    * permission to modify departments
    */
   public static final String GLOBAL_MODIFY_DEPARTMENTS = ExecutionPermission.Id.modifyDepartments.name();

   /**
    * permission to modify user data such as email, account, etc.
    */
   public static final String GLOBAL_MODIFY_USER_DATA = ExecutionPermission.Id.modifyUserData.name();

   /**
    * permission to query statistics on the audittrail database
    */
   public static final String GLOBAL_READ_AUDIT_TRAIL_STATISTICS = ExecutionPermission.Id.readAuditTrailStatistics.name();

   /**
    * permission to create cases
    */
   public static final String GLOBAL_CREATE_CASE = ExecutionPermission.Id.createCase.name();

   /**
    * permission to read department information
    */
   public static final String GLOBAL_READ_DEPARTMENTS = ExecutionPermission.Id.readDepartments.name();

   /**
    * permission to access data contained in the model
    */
   public static final String GLOBAL_READ_MODEL_DATA = ExecutionPermission.Id.readModelData.name();

   /**
    * permission to access user data such as email, account, etc.
    */
   public static final String GLOBAL_READ_USER_DATA = ExecutionPermission.Id.readUserData.name();

   /**
    * permission to reset the password of an user
    */
   public static final String GLOBAL_RESET_USER_PASSWORD = ExecutionPermission.Id.resetUserPassword.name();

   /**
    * permission to change user grants
    */
   public static final String GLOBAL_MANAGE_AUTHORIZATION = ExecutionPermission.Id.manageAuthorization.name();

   /**
    * permission to change user deputies
    */
   public static final String GLOBAL_MANAGE_DEPUTIES = ExecutionPermission.Id.manageDeputies.name();

   /**
    * permission to run the recovery
    */
   public static final String GLOBAL_RUN_RECOVERY = ExecutionPermission.Id.runRecovery.name();

   /**
    * permission to save preferences in own user scope
    */
   public static final String GLOBAL_SAVE_OWN_USER_SCOPE_PREFERENCES = ExecutionPermission.Id.saveOwnUserScopePreferences.name();

   /**
    * permission to save preferences in own realm scope
    */
   public static final String GLOBAL_SAVE_OWN_REALM_SCOPE_PREFERENCES = ExecutionPermission.Id.saveOwnRealmScopePreferences.name();

   /**
    * permission to save preferences in own partition scope
    */
   public static final String GLOBAL_SAVE_OWN_PARTITION_SCOPE_PREFERENCES = ExecutionPermission.Id.saveOwnPartitionScopePreferences.name();

   /**
    * permission to join a process instance
    */
   public static final String GLOBAL_JOIN_PROCESS_INSTANCE = ExecutionPermission.Id.joinProcessInstance.name();

   /**
    * permission to spawn a peer process instance
    */
   public static final String GLOBAL_SPAWN_PEER_PROCESS_INSTANCE = ExecutionPermission.Id.spawnPeerProcessInstance.name();

   /**
    * permission to spawn a sub process instance
    */
   public static final String GLOBAL_SPAWN_SUB_PROCESS_INSTANCE = ExecutionPermission.Id.spawnSubProcessInstance.name();

   public static final Set<String> globalPermissionIds = CollectionUtils.newSet();

   static
   {
      Field[] fields = GlobalPermissionConstants.class.getFields();
      for (Field field : fields)
      {
         // consider only static fields which name begins with GLOBAL and where the type is String
         if(field.getName().startsWith("GLOBAL") && String.class.equals(field.getType()) &&
               Modifier.isStatic(field.getModifiers()))
         {
            try
            {
               globalPermissionIds.add((String)field.get(null));
            }
            catch (Exception e)
            {
               Logger trace = LogManager.getLogger(GlobalPermissionConstants.class);
               trace.error("unknown field in GlobalPermissionConstants", e);
            }
         }
      }
   }
}
