/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.transientpi;

import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;

/**
 * <p>
 * This class contains constants related to the model used for tests
 * dealing with the <i>Transient Process Instance</i> functionality.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
/* package-private */ class TransientProcessInstanceModelConstants
{
   /**
    * the ID of the model comprising the transient process definitions
    */
   /* package-private */ static final String MODEL_ID = "TransientProcessInstanceModel";

   /**
    * the ID of the model comprising an alternative implementation for a process interface
    * declared in {@link #MODEL_ID}
    */
   /* package-private */ static final String ALTERNATIVE_IMPL_MODEL_ID = "ProcessInterfaceAlternativeImplementationModel";


   /**
    * the process definition model ID prefix for model {@link #MODEL_ID}
    */
   /* package-private */ static final String MODEL_ID_PREFIX = "{" + MODEL_ID + "}";


   /**
    * the ID of the non-forked process definition
    */
   /* package-private */ static final String PROCESS_DEF_ID_NON_FORKED = MODEL_ID_PREFIX + "Non_forkedProcess";

   /**
    * the ID of the forked process definition
    */
   /* package-private */ static final String PROCESS_DEF_ID_FORKED = MODEL_ID_PREFIX + "ForkedProcess";

   /**
    * the ID of the non-forked process definition that fails
    */
   /* package-private */ static final String PROCESS_DEF_ID_NON_FORKED_FAIL = MODEL_ID_PREFIX + "Non_forkedProcessFail";

   /**
    * the ID of the forked process definition that fails
    */
   /* package-private */ static final String PROCESS_DEF_ID_FORKED_FAIL = MODEL_ID_PREFIX + "ForkedProcessFail";

   /**
    * the ID of the process definition that contains an AND split and is executed transiently
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_TRANSIENT = MODEL_ID_PREFIX + "SplitProcessTransient";

   /**
    * the ID of the process definition that contains two AND splits
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_SPLIT = MODEL_ID_PREFIX + "SplitSplitProcess";

   /**
    * the ID of the process definition that enforces a rollback
    */
   /* package-private */ static final String PROCESS_DEF_ID_ROLLBACK = MODEL_ID_PREFIX + "RollbackProcess";

   /**
    * the ID of the process definition that has a transient and a non-transient route
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_NON_TRANSIENT_ROUTE = MODEL_ID_PREFIX + "TransientAndNon_transientRoute";

   /**
    * the ID of the process definition that switches from transient to non-transient execution
    */
   /* package-private */ static final String PROCESS_DEF_ID_FROM_TRANSIENT_TO_NON_TRANSIENT = MODEL_ID_PREFIX + "FromTransientToNon_transient";

   /**
    * the ID of the process definition that can be started via JMS
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_VIA_JMS = MODEL_ID_PREFIX + "TransientProcessViaJMSTrigger";

   /**
    * the ID of the process definition that contains an AND split and uses deferred persist
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_DEFERRED = MODEL_ID_PREFIX + "SplitProcessDeferredPersist";

   /**
    * the ID of the process definition that contains an AND split and uses default persist
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_DEFAULT = MODEL_ID_PREFIX + "SplitProcessDefaultPersist";

   /**
    * the ID of the process definition that contains an AND split and uses immediate persist
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_IMMEDIATE = MODEL_ID_PREFIX + "SplitProcessImmediatePersist";

   /**
    * the ID of the process definition that contains two subprocesses, one of them having another subprocess
    */
   /* package-private */ static final String PROCESS_DEF_ID_SUB_SUB_PROCESS = MODEL_ID_PREFIX + "Sub_Sub_Process";

   /**
    * the ID of the process definition that contains a while loop
    */
   /* package-private */ static final String PROCESS_DEF_ID_WHILE_LOOP = MODEL_ID_PREFIX + "WhileLoop";

   /**
    * the ID of the process definition that contains a repeat loop
    */
   /* package-private */ static final String PROCESS_DEF_ID_REPEAT_LOOP = MODEL_ID_PREFIX + "RepeatLoop";

   /**
    * the ID of the process definition that contains an AND split and an XOR join
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_XOR_JOIN = MODEL_ID_PREFIX + "SplitXORJoin";

   /**
    * the ID of the {@link AuditTrailPersistence#TRANSIENT} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#ENGINE_DEFAULT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_PROCESS_ASYNC_SUBPROCESS_ENGINE_DEFAULT = MODEL_ID_PREFIX + "TransientProcessAsyncSubprocessEngineDefault";

   /**
    * the ID of the {@link AuditTrailPersistence#TRANSIENT} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#TRANSIENT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_PROCESS_ASYNC_SUBPROCESS_TRANSIENT = MODEL_ID_PREFIX + "TransientProcessAsyncSubprocessTransient";

   /**
    * the ID of the {@link AuditTrailPersistence#TRANSIENT} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#DEFERRED}
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_PROCESS_ASYNC_SUBPROCESS_DEFERRED = MODEL_ID_PREFIX + "TransientProcessAsyncSubprocessDeferred";

   /**
    * the ID of the {@link AuditTrailPersistence#TRANSIENT} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#IMMEDIATE}
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_PROCESS_ASYNC_SUBPROCESS_IMMEDIATE = MODEL_ID_PREFIX + "TransientProcessAsyncSubprocessImmediate";

   /**
    * the ID of the {@link AuditTrailPersistence#DEFERRED} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#ENGINE_DEFAULT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_DEFERRED_PROCESS_ASYNC_SUBPROCESS_ENGINE_DEFAULT = MODEL_ID_PREFIX + "DeferredProcessAsyncSubprocessEngineDefault";

   /**
    * the ID of the {@link AuditTrailPersistence#DEFERRED} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#TRANSIENT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_DEFERRED_PROCESS_ASYNC_SUBPROCESS_TRANSIENT = MODEL_ID_PREFIX + "DeferredProcessAsyncSubprocessTransient";

   /**
    * the ID of the {@link AuditTrailPersistence#DEFERRED} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#DEFERRED}
    */
   /* package-private */ static final String PROCESS_DEF_ID_DEFERRED_PROCESS_ASYNC_SUBPROCESS_DEFERRED = MODEL_ID_PREFIX + "DeferredProcessAsyncSubprocessDeferred";

   /**
    * the ID of the {@link AuditTrailPersistence#DEFERRED} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#IMMEDIATE}
    */
   /* package-private */ static final String PROCESS_DEF_ID_DEFERRED_PROCESS_ASYNC_SUBPROCESS_IMMEDIATE = MODEL_ID_PREFIX + "DeferredProcessAsyncSubprocessImmediate";

   /**
    * the ID of the {@link AuditTrailPersistence#IMMEDIATE} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#ENGINE_DEFAULT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_IMMEDIATE_PROCESS_ASYNC_SUBPROCESS_ENGINE_DEFAULT = MODEL_ID_PREFIX + "ImmediateProcessAsyncSubprocessEngineDefault";

   /**
    * the ID of the {@link AuditTrailPersistence#IMMEDIATE} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#TRANSIENT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_IMMEDIATE_PROCESS_ASYNC_SUBPROCESS_TRANSIENT = MODEL_ID_PREFIX + "ImmediateProcessAsyncSubprocessTransient";

   /**
    * the ID of the {@link AuditTrailPersistence#IMMEDIATE} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#DEFERRED}
    */
   /* package-private */ static final String PROCESS_DEF_ID_IMMEDIATE_PROCESS_ASYNC_SUBPROCESS_DEFERRED = MODEL_ID_PREFIX + "ImmediateProcessAsyncSubprocessDeferred";

   /**
    * the ID of the {@link AuditTrailPersistence#IMMEDIATE} process definition that contains an asynchronous subprocess with {@link AuditTrailPersistence#IMMEDIATE}
    */
   /* package-private */ static final String PROCESS_DEF_ID_IMMEDIATE_PROCESS_ASYNC_SUBPROCESS_IMMEDIATE = MODEL_ID_PREFIX + "ImmediateProcessAsyncSubprocessImmediate";

   /**
    * the ID of the process definition that contains an activity aborting the whole process instance
    */
   /* package-private */ static final String PROCESS_DEF_ID_ABORT_PROCESS = MODEL_ID_PREFIX + "AbortProcess";

   /**
    * the ID of the process definition that contains an activity doing an isolated query (in a new tx)
    */
   /* package-private */ static final String PROCESS_DEF_ID_ISOLATED_QUERY_PROCESS = MODEL_ID_PREFIX + "IsolatedQueryProcess";

   /**
    * the ID of the process definition that contains an implicit AND join
    */
   /* package-private */ static final String PROCESS_DEF_ID_IMPLICIT_AND_JOIN = MODEL_ID_PREFIX + "ImplicitANDJoin";

   /**
    * the ID of the process definition that triggers a new process instance via an event action
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRIGGER_PROCESS_EVENT = MODEL_ID_PREFIX + "TriggerProcessEvent";

   /**
    * the ID of the process definition that contains a manual trigger
    */
   /* package-private */ static final String PROCESS_DEF_ID_MANUAL_TRIGGER = MODEL_ID_PREFIX + "ManualTrigger";

   /**
    * the ID of the process definition that contains a manual activity
    */
   /* package-private */ static final String PROCESS_DEF_ID_MANUAL_ACTIVITY = MODEL_ID_PREFIX + "ManualActivity";

   /**
    * the ID of the process definition that contains an activity waiting for some time
    */
   /* package-private */ static final String PROCESS_DEF_ID_WAITING_PROCESS = MODEL_ID_PREFIX + "WaitingProcess";

   /**
    * the ID of the process definition containing a pull event binding
    */
   /* package-private */ static final String PROCESS_DEF_ID_PULL_EVENT = MODEL_ID_PREFIX + "PullEvent";

   /**
    * the ID of the process definition used for recovery testing
    */
   /* package-private */ static final String PROCESS_DEF_ID_RECOVERY = MODEL_ID_PREFIX + "Recovery";

   /**
    * the ID of the process definition used for multiple retry testing
    */
   /* package-private */ static final String PROCESS_DEF_ID_MULTIPLE_RETRY = MODEL_ID_PREFIX + "MultipleRetry";

   /**
    * the ID of the process definition accessing data prior to an AND split
    */
   /* package-private */ static final String PROCESS_DEF_ID_DATA_ACCESS_PRIOR_TO_AND_SPLIT = MODEL_ID_PREFIX + "DataAccessPriorToANDSplit";

   /**
    * the ID of the process definition accessing big data
    */
   /* package-private */ static final String PROCESS_DEF_ID_BIG_DATA_ACCESS = MODEL_ID_PREFIX + "BigDataAccess";

   /**
    * the ID of the process definition declaring a process interface
    */
   /* package-private */ static final String PROCESS_DEF_ID_PROCESS_INTERFACE = MODEL_ID_PREFIX + "ProcessInterface";

   /**
    * the unqualified ID of the process definition declaring a process interface
    */
   /* package-private */ static final String PROCESS_DEF_ID_PROCESS_INTERFACE_UNQUALIFIED = "ProcessInterface";

   /**
    * the ID of the process definition allowing to change the audit trail persistence mode during process execution,
    * originally audit trail persistence mode is {@link AuditTrailPersistence#ENGINE_DEFAULT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_ENGINE_DEFAULT = MODEL_ID_PREFIX + "ChangeAuditTrailPersistence_EngineDefault";

   /**
    * the ID of the process definition allowing to change the audit trail persistence mode during process execution,
    * originally audit trail persistence mode is {@link AuditTrailPersistence#TRANSIENT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_TRANSIENT = MODEL_ID_PREFIX + "ChangeAuditTrailPersistence_Transient";

   /**
    * the ID of the process definition allowing to change the audit trail persistence mode during process execution,
    * originally audit trail persistence mode is {@link AuditTrailPersistence#DEFERRED}
    */
   /* package-private */ static final String PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_DEFERRED = MODEL_ID_PREFIX + "ChangeAuditTrailPersistence_Deferred";

   /**
    * the ID of the process definition allowing to change the audit trail persistence mode during process execution,
    * originally audit trail persistence mode is {@link AuditTrailPersistence#IMMEDIATE}
    */
   /* package-private */ static final String PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_IMMEDIATE = MODEL_ID_PREFIX + "ChangeAuditTrailPersistence_Immediate";

   /**
    * the ID of the process definition allowing to change the audit trail persistence mode during process execution multiple times,
    * originally audit trail persistence mode is {@link AuditTrailPersistence#TRANSIENT}
    */
   /* package-private */ static final String PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_MULTIPLE = MODEL_ID_PREFIX + "ChangeAuditTrailPersistence_MultipleTimes";

   /**
    * the ID of the process definition handling big structured data
    */
   /* package-private */ static final String PROCESS_DEF_ID_BIG_STRUCTURED_DATA = MODEL_ID_PREFIX + "BigStructuredData";


   /**
    * the name of the process definition started by a timer trigger
    */
   /* package-private */ static final String PROCESS_DEF_NAME_TIMER_TRIGGER = "Timer Trigger Process";


   /**
    * the name of the activity changing the audit trail persistence mode
    */
   /* package-private */ static final String ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE = "ChangeAuditTrailPersistenceActivity";

   /**
    * the name of the second activity changing the audit trail persistence mode
    */
   /* package-private */ static final String ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_2 = "ChangeAuditTrailPersistenceActivity_2";


   /**
    * the ID of the data in model 'TransientAndNon_transientRoute' determining whether the transient or
    * the non-transient route should be taken
    */
   /* package-private */ static final String DATA_ID_TRANSIENT_ROUTE = "TransientRoute";

   /**
    * the ID of the data determining the audit trail persistence mode to change to
    */
   /* package-private */ static final String DATA_ID_AUDIT_TRAIL_PERSISTENCE = "AuditTrailPersistence";

   /**
    * the ID of the data determining the first audit trail persistence mode to change to
    */
   /* package-private */ static final String DATA_ID_AUDIT_TRAIL_PERSISTENCE_1 = "AuditTrailPersistence_1";

   /**
    * the ID of the data determining the second audit trail persistence mode to change to
    */
   /* package-private */ static final String DATA_ID_AUDIT_TRAIL_PERSISTENCE_2 = "AuditTrailPersistence_2";

   /**
    * the ID of the big structured data
    */
   /* package-private */ static final String DATA_ID_BIG_STRUCT_DATA = "BigStructuredData";

   /**
    * the SDT data path of the big {@link String} data in {@link #DATA_ID_BIG_STRUCT_DATA}
    */
   /* package-private */ static final String DATA_PATH_BIG_STRING_DATA = "myString";

   /**
    * the in data path of the big {@link String} data in {@link #DATA_ID_BIG_STRUCT_DATA}
    */
   /* package-private */ static final String IN_DATA_PATH_BIG_STRING_DATA = "BigStringData";

   /**
    * the out data path for the primitive data 'Fail'
    */
   /* package-private */ static final String OUT_DATA_PATH_FAIL = "OutDataPathFail";
}
