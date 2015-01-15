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

import static java.lang.Boolean.TRUE;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_2;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.DATA_ID_AUDIT_TRAIL_PERSISTENCE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.DATA_ID_AUDIT_TRAIL_PERSISTENCE_1;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.DATA_ID_AUDIT_TRAIL_PERSISTENCE_2;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.DATA_ID_BIG_STRUCT_DATA;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.DATA_ID_TRANSIENT_ROUTE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.DATA_PATH_BIG_STRING_DATA;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.IN_DATA_PATH_BIG_STRING_DATA;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.OUT_DATA_PATH_FAIL;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ABORT_PROCESS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_DEFERRED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_ENGINE_DEFAULT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_IMMEDIATE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_TRANSIENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_BIG_STRUCTURED_DATA;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_DEFERRED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_ENGINE_DEFAULT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_IMMEDIATE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_MULTIPLE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_TRANSIENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_DATA_ACCESS_PRIOR_TO_AND_SPLIT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_FORKED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_FORKED_FAIL;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_FROM_TRANSIENT_TO_NON_TRANSIENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_IMPLICIT_AND_JOIN;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ISOLATED_QUERY_PROCESS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_MANUAL_ACTIVITY;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_MANUAL_TRIGGER;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_MULTIPLE_RETRY;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_NON_FORKED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_NON_FORKED_FAIL;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_PULL_EVENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_RECOVERY;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_REPEAT_LOOP;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ROLLBACK;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SPLIT_DEFAULT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SPLIT_DEFERRED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SPLIT_IMMEDIATE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SPLIT_SPLIT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SPLIT_TRANSIENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SPLIT_XOR_JOIN;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SUB_SUB_PROCESS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_TRANSIENT_NON_TRANSIENT_ROUTE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_TRANSIENT_VIA_JMS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_TRIGGER_PROCESS_EVENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_WAITING_PROCESS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_WHILE_LOOP;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_NAME_TIMER_TRIGGER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.DataCopyOptions;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions;
import org.eclipse.stardust.engine.api.runtime.SubprocessSpawnInfo;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.MultipleTryInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.DaemonHome;
import org.eclipse.stardust.test.api.util.DaemonHome.DaemonType;
import org.eclipse.stardust.test.api.util.Log4jLogMessageBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.api.util.WaitTimeout;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * <p>
 * Tests whether transient processes are <b>not</b> written to the database in case of
 * regular completion, but only in cases where an error during process execution occurs.
 * </p>
 *
 * <p>
 * Plus, this class makes sure that non-transient processes are not affected by the
 * <i>Transient Process Instance</i> functionality.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TransientProcessInstanceTest extends AbstractTransientProcessInstanceTest
{
   private static final String PI_IS_TRANSIENT_BPM_RT_ERROR_ID = "BPMRT03840";

   private static final String SPAWN_LINK_COMMENT = "<Spawn Link Comment>";

   private static final String JOIN_PI_COMMENT = "<Join PI Comment>";

   private static final String CASE_PI_NAME = "<Case PI Name>";

   private static final String CASE_PI_DESCRIPTION = "<Case PI Description>";

   private static final String ASSERTION_MSG_PI_STATE_CHECK = " - process instance state check";

   private static final String ASSERTION_MSG_HAS_ENTRY_IN_DB = " - process instance entry in database";

   private static final String ASSERTION_MSG_NO_SERIAL_AT_QUEUES = " - no serial activity thread queues";

   private static final String ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY = " - is transient process instance storage empty";

   private static final String ASSERTION_MSG_USER_DETAILS_LEVEL_CHECK = " - user details level check";

   private static final String ASSERTION_MSG_USER_PROPS_ARE_EMPTY = " - starting user properties are empty";

   private static final String ASSERTION_MSG_EXCEPTION_ID_CHECK = " - exception ID check";

   private static final String ASSERTION_MSG_AUDIT_TRAIL_PERSISTENCE_CHECK = " - audit trail persistence check";

   private static final String ASSERTION_MSG_STARTING_USER_CHECK = " - starting user check";

   private static final String ASSERTION_MSG_SHOULD_NOT_BE_REACHED = " - line should not be reached";


   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   @BeforeClass
   public static void setUpOnce()
   {
      System.setProperty(HAZELCAST_LOGGING_TYPE_KEY, HAZELCAST_LOGGING_TYPE_VALUE);
   }

   @AfterClass
   public static void tearDownOnce()
   {
      System.clearProperty(HAZELCAST_LOGGING_TYPE_KEY);
   }

   @Before
   public void setUp()
   {
      final GlobalParameters params = GlobalParameters.globals();
      params.set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 0);
      params.set(JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 0);
      params.set(KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER, SpringAppContextHazelcastJcaConnectionFactoryProvider.class.getName());
      params.set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, true);

      appMayComplete = false;
      FirstTryFailsApp.firstTime = true;

      dropTransientProcessInstanceStorage();
      dropSerialActivityThreadQueues();
   }

   public TransientProcessInstanceTest()
   {
      super(testClassSetup);
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of non-forked processes.
    * </p>
    */
   @Test
   public void testNonTransientProcessWithoutForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of forked processes.
    * </p>
    */
   @Test
   public void testNonTransientProcessWithForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of non-forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testNonTransientProcessFailsWithoutForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED_FAIL, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Interrupted));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testNonTransientProcessFailsWithForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED_FAIL, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether non-transient processes can be executed successfully and
    * that they're written to the database.
    * </p>
    */
   @Test
   public void testNonTransientProcessSplitScenario() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient processes are <b>not</b> written to the database
    * in case of non-forked processes.
    * </p>
    */
   @Test
   public void testTransientProcessWithoutForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient processes are <b>not</b> written to the database
    * in case of forked processes.
    * </p>
    */
   @Test
   public void testTransientProcessWithForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient processes are written to the database
    * in case of non-forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testTransientProcessFailsWithoutForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED_FAIL, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Interrupted));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient processes are written to the database
    * in case of forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testTransientProcessFailsWithForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED_FAIL, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient processes can be executed successfully and
    * that they're <b>not</b> written to the database.
    * </p>
    */
   @Test
   public void testTransientProcessSplitScenario() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient processes can be executed successfully and
    * that they're <b>not</b> written to the database.
    * </p>
    */
   @Test
   public void testTransientProcessSplitSplitScenario() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_SPLIT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that the transient processes are <b>not</b> written to the
    * transient process in-memory storage in case of transaction rollback.
    * </p>
    */
   @Test
   public void testRollbackScenario()
   {
      enableTransientProcessesSupport();

      try
      {
         sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ROLLBACK, null, true);
         fail("Tx is marked for rollback and therefore cannot be committed.");
      }
      catch (final UnexpectedRollbackException ignored)
      {
         /* expected */
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that the process instance data is removed from the in-memory storage
    * for transient process instances as soon as the process instance is completed.
    * </p>
    */
   @Test
   public void testCleanup() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether a process instance started transiently is able to complete transiently,
    * if it only hits activities permitting transient execution, even though there's a route
    * in the model containing an interactive activity.
    * </p>
    */
   @Test
   public void testStartTransientlyCompleteTransiently() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_TRANSIENT_NON_TRANSIENT_ROUTE, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether a process instance started transiently is able to complete non-transiently,
    * if it hits an activity that does not permit transient execution.
    * </p>
    */
   @Test
   public void testStartTransientlyCompleteNonTransiently() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, ?> data = Collections.singletonMap(DATA_ID_TRANSIENT_ROUTE, Boolean.FALSE);
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_TRANSIENT_NON_TRANSIENT_ROUTE, data, true);

      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi.getProcessID()));
      final ActivityInstance completedAi = sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, completedAi.getProcessInstance().getState(), is(ProcessInstanceState.Completed));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether a process instance started transiently is able to complete non-transiently,
    * if it hits an activity that does not permit transient execution. Plus, it tests whether the
    * already scheduled serial activity threads are properly converted to 'common' concurrent
    * activity threads (<i>System Queue</i>).
    * </p>
    */
   @Test
   public void testFromTransientToNonTransient() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FROM_TRANSIENT_TO_NON_TRANSIENT, null, true);

      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(ActivityInstanceState.Suspended));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether a transient processes does not schedule a serial activity thread
    * if it can be completed in one transaction.
    * </p>
    */
   @Test
   public void testTransientProcessDoesNotScheduleSerialActivityThread() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether a transient processes (exposing a <i>JMS</i> trigger) can be started
    * via the <i>Application Queue</i>.
    * </p>
    */
   @Test
   public void testTransientProcessViaAppQueue() throws Exception
   {
      enableTransientProcessesSupport();

      startProcessViaJms(PROCESS_DEF_ID_TRANSIENT_VIA_JMS);
      final long piOid = receiveProcessInstanceCompletedMessage();

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(piOid), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether deferred persist (see
    * {@link org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AuditTrailPersistence#DEFERRED})
    * works correctly.
    * </p>
    */
   @Test
   public void testTransientProcessSplitScenarioDeferredPersist() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#ENGINE_DEFAULT} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.
    * </p>
    */
   @Test
   public void testGlobalOverrideDefaultWithOff() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFAULT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#ENGINE_DEFAULT} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.
    * </p>
    */
   @Test
   public void testGlobalOverrideDefaultWithTransient() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFAULT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#ENGINE_DEFAULT} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.
    * </p>
    */
   @Test
   public void testGlobalOverrideDefaultWithDeferred() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFAULT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * No override ({@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}) for {@link AuditTrailPersistence#ENGINE_DEFAULT}.
    * </p>
    */
   @Test
   public void testNoGlobalOverrideForDefault() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFAULT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#TRANSIENT} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.
    * </p>
    */
   @Test
   public void testGlobalOverrideTransientWithOff() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#TRANSIENT} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.
    * </p>
    */
   @Test
   public void testGlobalOverrideTransientWithTransient() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#TRANSIENT} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.
    * </p>
    */
   @Test
   public void testGlobalOverrideTransientWithDeferred() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * No override ({@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}) for {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testNoGlobalOverrideForTransient() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#DEFERRED} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.
    * </p>
    */
   @Test
   public void testGlobalOverrideDeferredWithOff() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#DEFERRED} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.
    * </p>
    */
   @Test
   public void testGlobalOverrideDeferredWithTransient() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#DEFERRED} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.
    * </p>
    */
   @Test
   public void testGlobalOverrideDeferredWithDeferred() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * No override ({@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}) for {@link AuditTrailPersistence#DEFERRED}.
    * </p>
    */
   @Test
   public void testNoGlobalOverrideForDeferred() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#IMMEDIATE} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_OFF}.
    * </p>
    */
   @Test
   public void testGlobalOverrideImmediateWithOff() throws Exception
   {
      disableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_IMMEDIATE, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#IMMEDIATE} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT}.
    * </p>
    */
   @Test
   public void testGlobalOverrideImmediateWithTransient() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_IMMEDIATE, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * Override {@link AuditTrailPersistence#IMMEDIATE} with {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.
    * </p>
    */
   @Test
   public void testGlobalOverrideImmediateWithDeferred() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_IMMEDIATE, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether the global override for Audit Trail Persistence works correctly:
    * No override ({@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}) for {@link AuditTrailPersistence#IMMEDIATE}.
    * </p>
    */
   @Test
   public void testNoGlobalOverrideForImmediate() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_IMMEDIATE, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that subprocess invocations do not disrupt transient process instance execution.
    * </p>
    */
   @Test
   public void testTransientSubProcesses() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SUB_SUB_PROCESS, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that while loops do not disrupt transient process instance execution.
    * </p>
    */
   @Test
   public void testTransientProcessWhileLoop() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WHILE_LOOP, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that repeat loops do not disrupt transient process instance execution.
    * </p>
    */
   @Test
   public void testTransientProcessRepeatLoop() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_REPEAT_LOOP, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for XOR joins as well.
    * </p>
    */
   @Test
   public void testTransientProcessSplitXorJoin() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_XOR_JOIN, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#ENGINE_DEFAULT} when the starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessEngineDefault() throws Exception
   {
      // TODO [CRNT-26302] adapt test case

      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_ENGINE_DEFAULT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(subPiOid), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#TRANSIENT} when the starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessTransient() throws Exception
   {
      // TODO [CRNT-26302] adapt test case

      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(subPiOid), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#DEFERRED} when the starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessDeferred() throws Exception
   {
      // TODO [CRNT-26302] adapt test case

      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(subPiOid), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#IMMEDIATE} when the starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessImmediate() throws Exception
   {
      // TODO [CRNT-26302] adapt test case

      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_IMMEDIATE, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(subPiOid), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#ENGINE_DEFAULT} when the starting process instance is
    * {@link AuditTrailPersistence#DEFERRED}.
    * </p>
    */
   @Test
   public void testDeferredProcessAsyncSubprocessEngineDefault() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#TRANSIENT} when the starting process instance is
    * {@link AuditTrailPersistence#DEFERRED}.
    * </p>
    */
   @Test
   public void testDeferredProcessAsyncSubprocessTransient() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#DEFERRED} when the starting process instance is
    * {@link AuditTrailPersistence#DEFERRED}.
    * </p>
    */
   @Test
   public void testDeferredProcessAsyncSubprocessDeferred() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#IMMEDIATE} when the starting process instance is
    * {@link AuditTrailPersistence#DEFERRED}.
    * </p>
    */
   @Test
   public void testDeferredProcessAsyncSubprocessImmediate() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#ENGINE_DEFAULT} when the starting process instance is
    * {@link AuditTrailPersistence#IMMEDIATE}.
    * </p>
    */
   @Test
   public void testImmediateProcessAsyncSubprocessEngineDefault() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#TRANSIENT} when the starting process instance is
    * {@link AuditTrailPersistence#IMMEDIATE}.
    * </p>
    */
   @Test
   public void testImmediateProcessAsyncSubprocessTransient() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#DEFERRED} when the starting process instance is
    * {@link AuditTrailPersistence#IMMEDIATE}.
    * </p>
    */
   @Test
   public void testImmediateProcessAsyncSubprocessDeferred() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * being {@link AuditTrailPersistence#IMMEDIATE} when the starting process instance is
    * {@link AuditTrailPersistence#IMMEDIATE}.
    * </p>
    */
   @Test
   public void testImmediateProcessAsyncSubprocessImmediate() throws Exception
   {
      // TODO [CRNT-26302] implement test case
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that concurrent execution of transient process instances works correctly.
    * </p>
    */
   @Test
   public void testTransientProcessConcurrentExecution() throws Exception
   {
      enableTransientProcessesSupport();

      final int nThreads = 100;

      final Set<ProcessExecutor> processExecutors = initProcessExecutors(nThreads, sf.getWorkflowService());

      final List<Future<Long>> piOids = executeProcesses(nThreads, processExecutors);

      ProcessInstanceStateBarrier.instance().setTimeout(new WaitTimeout(1, TimeUnit.MINUTES));
      for (final Future<Long> f : piOids)
      {
         ProcessInstanceStateBarrier.instance().await(f.get(), ProcessInstanceState.Completed);

         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(f.get()), is(false));
      }
      ProcessInstanceStateBarrier.instance().setTimeout(null);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that aborting of a transient process instance works correctly, i.e. the process instance will
    * be persisted.
    * </p>
    */
   @Test
   public void testTransientProcessAbort() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ABORT_PROCESS, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.</b>
    * </p>
    *
    * <p>
    * Tests that querying for a completed process instance with Audit Trail Persistence
    * {@link AuditTrailPersistence#DEFERRED} does not cause any problems (see CRNT-26532).
    * </p>
    */
   @Test
   public void testQueryForCompletedDeferredProcessInstance() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));

      sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAll());
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests that <i>Hazelcast</i>'s restriction that there's a strict 1:1 relationship between
    * thread and transaction does not cause any harm, i.e. our workaround works correctly (see CRNT-26544).
    * </p>
    */
   @Test
   public void testMultipleTransactionsPerThread() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ISOLATED_QUERY_PROCESS, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether implicit AND joins work correctly (see CRNT-26559).
    * </p>
    */
   @Test
   public void testImplicitAndJoinProcess() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_IMPLICIT_AND_JOIN, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether triggering a process instance via an event action works correctly.
    * </p>
    */
   @Test
   public void testProcessInstanceTriggerEvent() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_TRIGGER_PROCESS_EVENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long triggeredPiOid = receiveProcessInstanceCompletedMessage();

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(triggeredPiOid), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether executing a process instance with a manual trigger works correctly.
    * </p>
    */
   @Test
   public void testWithManualTrigger() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MANUAL_TRIGGER, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_STARTING_USER_CHECK, pi.getStartingUser().getAccount(), equalTo(MOTU));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether the propagation of the <i>Audit Trail Persistence</i> runtime attribute works correctly.
    * </p>
    */
   @Test
   public void testClientSideProperty() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));

      final AuditTrailPersistence persistence = (AuditTrailPersistence) pi.getRuntimeAttributes().get(AuditTrailPersistence.class.getName());
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_AUDIT_TRAIL_PERSISTENCE_CHECK, persistence, is(AuditTrailPersistence.TRANSIENT));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether starting the process instance asynchronously works correctly.
    * </p>
    */
   @Test
   public void testStartTransientProcessAsync() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT_SPLIT, null, false);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /* no need to test for spawning transient subprocess instances from a non-transient process instance                      */
   /* since the former inherits the Audit Trail Persistence property from the latter (root process determines transientness) */

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether spawning (subprocess) from a transient process instance is rejected with the correct exception. Using
    * API {@link WorkflowService#spawnSubprocessInstances(long, List)}.
    * </p>
    */
   @Test
   public void testSpawnSubprocess1FromTransientProcessInstanceIsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      final SubprocessSpawnInfo spawnInfo = new SubprocessSpawnInfo(PROCESS_DEF_ID_FORKED, false, Collections.<String, Object>emptyMap());
      final List<SubprocessSpawnInfo> spawnInfos = Collections.singletonList(spawnInfo);
      try
      {
         sf.getWorkflowService().spawnSubprocessInstances(pi.getOID(), spawnInfos);
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /* no need to test for spawning transient subprocess instances from a non-transient process instance                      */
   /* since the former inherits the Audit Trail Persistence property from the latter (root process determines transientness) */

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether spawning (subprocess) from a transient process instance is rejected with the correct exception. Using
    * API {@link WorkflowService#spawnSubprocessInstance(long, String, boolean, Map)}.
    * </p>
    */
   @Test
   public void testSpawnSubprocess2FromTransientProcessInstanceIsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      try
      {
         sf.getWorkflowService().spawnSubprocessInstance(pi.getOID(), PROCESS_DEF_ID_FORKED, false, Collections.<String, Object>emptyMap());
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether spawning (peer process) a transient process instance is rejected with the correct exception. Using
    * API {@link WorkflowService#spawnPeerProcessInstance(long, String, boolean, Map, boolean, String)}.
    * </p>
    */
   @Test
   public void testSpawnTransientPeerProcessInstance1IsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MANUAL_ACTIVITY, null, true);

      try
      {
         sf.getWorkflowService().spawnPeerProcessInstance(pi.getOID(), PROCESS_DEF_ID_FORKED, false, Collections.<String, Serializable>emptyMap(), true, SPAWN_LINK_COMMENT);
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(pi.getOID()));
         sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether spawning (peer process) from a transient process instance is rejected with the correct exception. Using
    * API {@link WorkflowService#spawnPeerProcessInstance(long, String, boolean, Map, boolean, String)}.
    * </p>
    */
   @Test
   public void testSpawnPeerProcessFromTransientProcessInstance1IsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      try
      {
         sf.getWorkflowService().spawnPeerProcessInstance(pi.getOID(), PROCESS_DEF_ID_MANUAL_ACTIVITY, false, Collections.<String, Serializable>emptyMap(), true, SPAWN_LINK_COMMENT);
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether spawning (peer process) a transient process instance is rejected with the correct exception. Using
    * API {@link WorkflowService#spawnPeerProcessInstance(long, String, SpawnOptions)}.
    * </p>
    */
   @Test
   public void testSpawnTransientPeerProcessInstance2IsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MANUAL_ACTIVITY, null, true);

      final SpawnOptions spawnOptions = new SpawnOptions(null, true, SPAWN_LINK_COMMENT, DataCopyOptions.DEFAULT);
      try
      {
         sf.getWorkflowService().spawnPeerProcessInstance(pi.getOID(), PROCESS_DEF_ID_FORKED, spawnOptions);
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(pi.getOID()));
         sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether spawning (peer process) from a transient process instance is rejected with the correct exception. Using
    * API {@link WorkflowService#spawnPeerProcessInstance(long, String, SpawnOptions)}.
    * </p>
    */
   @Test
   public void testSpawnPeerProcessFromTransientProcessInstance2IsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      final SpawnOptions spawnOptions = new SpawnOptions(null, true, SPAWN_LINK_COMMENT, DataCopyOptions.DEFAULT);
      try
      {
         sf.getWorkflowService().spawnPeerProcessInstance(pi.getOID(), PROCESS_DEF_ID_MANUAL_ACTIVITY, spawnOptions);
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether creating a case containing a transient process instance is rejected with the correct exception.
    * </p>
    */
   @Test
   public void testCreateCaseIsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      try
      {
         sf.getWorkflowService().createCase(CASE_PI_NAME, CASE_PI_DESCRIPTION, new long[] { pi.getOID() });
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether joining a transient process instance to a case is rejected with the correct exception.
    * </p>
    */
   @Test
   public void testJoinCaseIsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance nonTransientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MANUAL_ACTIVITY, null, true);
      final ProcessInstance casePi = sf.getWorkflowService().createCase(CASE_PI_NAME, CASE_PI_DESCRIPTION, new long[] { nonTransientPi.getOID() });
      final ProcessInstance transientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      try
      {
         sf.getWorkflowService().joinCase(casePi.getOID(), new long[] { transientPi.getOID() });
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(nonTransientPi.getOID()));
         sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
         ProcessInstanceStateBarrier.instance().await(nonTransientPi.getOID(), ProcessInstanceState.Completed);
         ProcessInstanceStateBarrier.instance().await(casePi.getOID(), ProcessInstanceState.Completed);

         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(transientPi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(nonTransientPi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(casePi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(transientPi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether joining a transient process instance to a process instance is rejected with the correct exception.
    * </p>
    */
   @Test
   public void testJoinTransientProcessInstanceToProcessInstanceIsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance nonTransientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MANUAL_ACTIVITY, null, true);
      final ProcessInstance transientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      try
      {
         sf.getWorkflowService().joinProcessInstance(transientPi.getOID(), nonTransientPi.getOID(), JOIN_PI_COMMENT);
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(nonTransientPi.getOID()));
         sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
         ProcessInstanceStateBarrier.instance().await(nonTransientPi.getOID(), ProcessInstanceState.Completed);

         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(transientPi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(nonTransientPi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(transientPi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether joining a process instance to a transient process instance is rejected with the correct exception.
    * </p>
    */
   @Test
   public void testJoinProcessInstanceToTransientProcessInstanceIsIllegal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance nonTransientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MANUAL_ACTIVITY, null, true);
      final ProcessInstance transientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_WAITING_PROCESS, null, true);

      try
      {
         sf.getWorkflowService().joinProcessInstance(nonTransientPi.getOID(), transientPi.getOID(), JOIN_PI_COMMENT);
         fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      finally
      {
         final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(nonTransientPi.getOID()));
         sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
         ProcessInstanceStateBarrier.instance().await(nonTransientPi.getOID(), ProcessInstanceState.Completed);

         appMayComplete = true;
         ProcessInstanceStateBarrier.instance().await(transientPi.getOID(), ProcessInstanceState.Completed);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(nonTransientPi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(transientPi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether execution a transient process is working correctly without exposing the in-memory storage
    * comprising the transient process instances, i.e. setting {@link KernelTweakingProperties#TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE}
    * to <code>false</code>.
    * </p>
    */
   @Test
   public void testTransientProcessExecutionWithoutExposingInMemStorage() throws Exception
   {
      disableInMemStorageExposal();

      testTransientProcessSplitSplitScenario();
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether the engine complains via a log message that pull event bindings for transient process instances
    * cannot be processed and will be ignored.
    * </p>
    */
   @Test
   public void testPullEventsAreOmitted() throws Exception
   {
      enableTransientProcessesSupport();

      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.WARN);
      barrier.registerWithLog4j();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_PULL_EVENT, null, true);

      barrier.waitForLogMessage("Event binding .* applies to a transient process instance .*", new WaitTimeout(5, TimeUnit.SECONDS));
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient process instances whose processing failed are left in a state that
    * allows for running a recovery.
    * </p>
    *
    * <p>
    * <b>From time to time, this test case is failing. See CRNT-34036 for details.</b>
    * </p>
    */
   @Test
   public void testRecovery() throws Exception
   {
      enableTxPropagation();
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_RECOVERY, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      sf.getWorkflowService().setOutDataPath(pi.getOID(), OUT_DATA_PATH_FAIL, Boolean.FALSE);
      sf.getAdministrationService().recoverProcessInstance(pi.getOID());

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient process instances whose processing failed are left in a state that
    * allows for running a recovery. This test case especially tests whether that holds true for
    * activity threads that are retriggered multiple times (see {@link MultipleTryInterceptor}.
    * </p>
    */
   @Test
   public void testRecoveryForActivityThreadWithRetry() throws Exception
   {
      enableTxPropagation();
      enableOneSystemQueueConsumerRetry();
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_RECOVERY, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      sf.getWorkflowService().setOutDataPath(pi.getOID(), OUT_DATA_PATH_FAIL, Boolean.FALSE);
      sf.getAdministrationService().recoverProcessInstance(pi.getOID());

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether transient process execution is <b>not</b> cancelled, if the first attempt to execute the process instance
    * fails, but the second succeeds and a {@link MultipleTryInterceptor} is configured appropriately.
    * </p>
    */
   @Test
   public void testMultipleRetry() throws Exception
   {
      enableTxPropagation();
      enableOneSystemQueueConsumerRetry();
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MULTIPLE_RETRY, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED}.</b>
    * </p>
    *
    * <p>
    * Tests whether the process and activity instance information persisted into the audit trail database is complete
    * for <i>Audit Trail Persistence</i> {@link AuditTrailPersistence#DEFERRED}.
    * </p>
    */
   @Test
   public void testDeferredPersistentCompleteness() throws Exception
   {
      overrideTransientProcessesSupport(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      final ProcessInstance persistedPi = sf.getWorkflowService().getProcessInstance(pi.getOID());
      assertPiInfoIsComplete(persistedPi, true);

      final ActivityInstances persistedAis = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findForProcessInstance(pi.getOID()));
      for (final ActivityInstance a : persistedAis)
      {
         assertAiInfoIsComplete(a);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Tests whether the process and activity instance information persisted into the audit trail database is complete
    * for the case that <i>Audit Trail Persistence</i> needs to be switched from
    * {@link AuditTrailPersistence#TRANSIENT} to {@link AuditTrailPersistence#IMMEDIATE}.
    * </p>
    */
   @Test
   public void testPersistentCompletenessWhenSwitchingFromTransientToImmediate() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED_FAIL, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      final ProcessInstance persistedPi = sf.getWorkflowService().getProcessInstance(pi.getOID());
      assertPiInfoIsComplete(persistedPi, false);

      final ActivityInstances persistedAis = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findForProcessInstance(pi.getOID()));
      for (final ActivityInstance a : persistedAis)
      {
         assertAiInfoIsComplete(a);
      }

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Transient process execution: asserts that no preferences are fetched for the starting user.
    * </p>
    */
   @Test
   public void testNoPreferencesAreFetched() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      final Map<String, Object> properties = pi.getStartingUser().getAllProperties();
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_USER_PROPS_ARE_EMPTY, properties.isEmpty(), is(TRUE));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Transient process execution: asserts that the {@link UserDetailsLevel} of the starting user is
    * {@link UserDetailsLevel#Minimal}.
    * </p>
    */
   @Test
   public void testDetailsLevelIsMinimal() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      final UserDetailsLevel userDetailsLevel = pi.getStartingUser().getDetailsLevel();
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_USER_DETAILS_LEVEL_CHECK, userDetailsLevel, equalTo(UserDetailsLevel.Minimal));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Transient process execution: asserts that the information whether the starting user is an administrator
    * cannot be determined.
    * </p>
    */
   @Test(expected = IllegalStateException.class)
   public void testAdminCannotBeDetermined() throws Exception
   {
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      pi.getStartingUser().isAdministrator();
      fail(ASSERTION_MSG_SHOULD_NOT_BE_REACHED);
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensure that timer triggers do also work for transient process definitions.
    * </p>
    */
   @Test
   public void testTimerTriggerDaemonAlsoConsidersTransientProcessDefinitions() throws Exception
   {
      enableTransientProcessesSupport();

      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.INFO);
      barrier.registerWithLog4j();

      DaemonHome.startDaemon(sf.getAdministrationService(), DaemonType.TIMER_TRIGGER_DAEMON);
      final Daemon daemon = DaemonHome.getDaemon(sf.getAdministrationService(), DaemonType.TIMER_TRIGGER_DAEMON);
      assertNotNull(daemon.getLastExecutionTime());

      final String logMsgRegex = "State change .*" + PROCESS_DEF_NAME_TIMER_TRIGGER + ".* Active-->Completed\\.";
      barrier.waitForLogMessage(logMsgRegex, new WaitTimeout(10, TimeUnit.SECONDS));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that the transient process instance is processed correctly, even though
    * {@link KernelTweakingProperties#TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE}
    * is set to <code>false</code>, i.e. the in-memory storage is <b>not</b> exposed.
    * </p>
    *
    * <p>
    * See also <a href="https://www.csa.sungard.com/jira/browse/CRNT-30812">CRNT-30812</a>.
    * </p>
    */
   @Test
   public void testDataAccessPriorToAndSplitHavingInMemStorageExposalDisabled() throws Exception
   {
      enableTransientProcessesSupport();
      disableInMemStorageExposal();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_DATA_ACCESS_PRIOR_TO_AND_SPLIT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#ENGINE_DEFAULT} to
    * {@link AuditTrailPersistence#ENGINE_DEFAULT} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromEngineDefaultToEngineDefault() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.ENGINE_DEFAULT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_ENGINE_DEFAULT, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#ENGINE_DEFAULT} to
    * {@link AuditTrailPersistence#TRANSIENT} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromEngineDefaultToTransient() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.TRANSIENT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_ENGINE_DEFAULT, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#ENGINE_DEFAULT} to
    * {@link AuditTrailPersistence#DEFERRED} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromEngineDefaultToDeferred() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.DEFERRED.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_ENGINE_DEFAULT, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#ENGINE_DEFAULT} to
    * {@link AuditTrailPersistence#IMMEDIATE} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromEngineDefaultToImmediate() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.IMMEDIATE.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_ENGINE_DEFAULT, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#TRANSIENT} to
    * {@link AuditTrailPersistence#ENGINE_DEFAULT} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromTransientToEngineDefault() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.ENGINE_DEFAULT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_TRANSIENT, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#TRANSIENT} to
    * {@link AuditTrailPersistence#TRANSIENT} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromTransientToTransient() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.TRANSIENT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_TRANSIENT, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#TRANSIENT} to
    * {@link AuditTrailPersistence#DEFERRED} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromTransientToDeferred() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.DEFERRED.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_TRANSIENT, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#TRANSIENT} to
    * {@link AuditTrailPersistence#IMMEDIATE} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromTransientToImmediate() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.IMMEDIATE.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_TRANSIENT, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#DEFERRED} to
    * {@link AuditTrailPersistence#ENGINE_DEFAULT} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromDeferredToEngineDefault() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.ENGINE_DEFAULT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_DEFERRED, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#DEFERRED} to
    * {@link AuditTrailPersistence#TRANSIENT} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromDeferredToTransient() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.TRANSIENT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_DEFERRED, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#DEFERRED} to
    * {@link AuditTrailPersistence#DEFERRED} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromDeferredToDeferred() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.DEFERRED.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_DEFERRED, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(false));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#DEFERRED} to
    * {@link AuditTrailPersistence#IMMEDIATE} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromDeferredToImmediate() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.IMMEDIATE.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_DEFERRED, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#IMMEDIATE} to
    * {@link AuditTrailPersistence#ENGINE_DEFAULT} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromImmediateToEngineDefault() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.ENGINE_DEFAULT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_IMMEDIATE, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#IMMEDIATE} to
    * {@link AuditTrailPersistence#TRANSIENT} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromImmediateToTransient() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.TRANSIENT.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_IMMEDIATE, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#IMMEDIATE} to
    * {@link AuditTrailPersistence#DEFERRED} is denied.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromImmediateToDeferred() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.DEFERRED.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_IMMEDIATE, auditTrailPersistenceData, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode from {@link AuditTrailPersistence#IMMEDIATE} to
    * {@link AuditTrailPersistence#IMMEDIATE} works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceFromImmediateToImmediate() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = Collections.singletonMap(DATA_ID_AUDIT_TRAIL_PERSISTENCE, AuditTrailPersistence.IMMEDIATE.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_IMMEDIATE, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures that changing the {@link AuditTrailPersistence} mode multiple times (from {@link AuditTrailPersistence#TRANSIENT} to
    * {@link AuditTrailPersistence#DEFERRED} and finally to {@link AuditTrailPersistence#IMMEDIATE}) works correctly.
    * </p>
    */
   @Test
   public void testChangeAuditTrailPersistenceMultipleTimes() throws Exception
   {
      enableTransientProcessesSupport();

      final Map<String, String> auditTrailPersistenceData = newHashMap();
      auditTrailPersistenceData.put(DATA_ID_AUDIT_TRAIL_PERSISTENCE_1, AuditTrailPersistence.DEFERRED.name());
      auditTrailPersistenceData.put(DATA_ID_AUDIT_TRAIL_PERSISTENCE_2, AuditTrailPersistence.IMMEDIATE.name());
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_MULTIPLE, auditTrailPersistenceData, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_CHANGE_AUDIT_TRAIL_PERSISTENCE_2);
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      appMayComplete = true;

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    *
    * <p>
    * Ensures we're able to cope with a big {@link String} (> 65535 bytes) in a <i>Structured Data</i>, which is written to
    * the {@link TransientProcessInstanceStorage} via {@link ByteArrayBlobBuilder#writeString(String)} (see CRNT-32492).
    * </p>
    */
   @Test
   public void testWriteBigStructuredData() throws Exception
   {
      enableTransientProcessesSupport();

      final StringBuffer sb = new StringBuffer();
      for (int i = 0; i < 65535 + 1; i++)
      {
         sb.append(i % 10);
      }
      final String bigStringData = sb.toString();

      final Map<String, Object> bigStructData = newHashMap();
      bigStructData.put(DATA_PATH_BIG_STRING_DATA, bigStringData);

      final Map<String, Object> data = newHashMap();
      data.put(DATA_ID_BIG_STRUCT_DATA, bigStructData);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_BIG_STRUCTURED_DATA, data, true);
      final String actual = (String) sf.getWorkflowService().getInDataPath(pi.getOID(), IN_DATA_PATH_BIG_STRING_DATA);

      assertThat(actual, equalTo(bigStringData));

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_NO_SERIAL_AT_QUEUES, noSerialActivityThreadQueues(), is(true));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_TRANSIENT_PI_STORAGE_EMPTY, isTransientProcessInstanceStorageEmpty(), is(true));
   }
}
