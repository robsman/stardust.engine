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

public final class Permissions
{
   public static final String PREFIX = "authorization:";

   public static final String ACTIVITY_ABORT_ACTIVITY_INSTANCES = PREFIX + "activity.abortActivityInstances";
   public static final String ACTIVITY_DELEGATE_TO_OTHER = PREFIX + "activity.delegateToOther";
   public static final String ACTIVITY_MANAGE_EVENT_HANDLERS = PREFIX + "activity.manageEventHandlers";
   public static final String ACTIVITY_READ_ACTIVITY_INSTANCE_DATA = PREFIX + "activity.readActivityInstanceData";

   public static final String DATA_MODIFY_DATA_VALUES = PREFIX + "data.modifyDataValues";
   public static final String DATA_READ_DATA_VALUES = PREFIX + "data.readDataValues";

   public static final String MODEL_CONTROL_PROCESS_ENGINE = PREFIX + "model.controlProcessEngine";
   public static final String MODEL_DEPLOY_PROCESS_MODEL = PREFIX + "model.deployProcessModel";
   public static final String MODEL_MANAGE_DAEMONS = PREFIX + "model.manageDaemons";
   public static final String MODEL_MODIFY_AUDIT_TRAIL = PREFIX + "model.modifyAuditTrail";
   public static final String MODEL_MODIFY_USER_DATA = PREFIX + "model.modifyUserData";
   public static final String MODEL_READ_AUDIT_TRAIL_STATISTICS = PREFIX + "model.readAuditTrailStatistics";
   public static final String MODEL_READ_MODEL_DATA = PREFIX + "model.readModelData";
   public static final String MODEL_READ_USER_DATA = PREFIX + "model.readUserData";
   public static final String MODEL_MANAGE_AUTHORIZATION = PREFIX + "model.manageAuthorization";
   public static final String MODEL_RUN_RECOVERY = PREFIX + "model.runRecovery";
   public static final String MODEL_FORCE_SUSPEND = PREFIX + "model.forceSuspend";
   public static final String MODEL_SAVE_OWN_USER_SCOPE_PREFERENCES = PREFIX + "model.saveOwnUserScopePreferences";
   public static final String MODEL_SAVE_OWN_REALM_SCOPE_PREFERENCES = PREFIX + "model.saveOwnRealmScopePreferences";
   public static final String MODEL_SAVE_OWN_PARTITION_SCOPE_PREFERENCES = PREFIX + "model.saveOwnPartitionScopePreferences";

   public static final String PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES = PREFIX + "processDefinition.abortProcessInstances";
   public static final String PROCESS_DEFINITION_DELETE_PROCESS_INSTANCES = PREFIX + "processDefinition.deleteProcessInstances";
   public static final String PROCESS_DEFINITION_MANAGE_EVENT_HANDLERS = PREFIX + "processDefinition.manageEventHandlers";
   public static final String PROCESS_DEFINITION_MODIFY_PROCESS_INSTANCES = PREFIX + "processDefinition.modifyProcessInstances";
   public static final String PROCESS_DEFINITION_READ_PROCESS_INSTANCE_DATA = PREFIX + "processDefinition.readProcessInstanceData";

   private Permissions() {}
}
