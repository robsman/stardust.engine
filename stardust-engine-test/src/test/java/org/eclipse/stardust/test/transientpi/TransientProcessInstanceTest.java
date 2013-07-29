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
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.DATA_ID_TRANSIENT_ROUTE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.OUT_DATA_PATH_FAIL;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ABORT_PROCESS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_DEFERRED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_ENGINE_DEFAULT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_IMMEDIATE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ASYNC_SUBPROCESS_TRANSIENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_FORKED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_FORKED_FAIL;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_FROM_TRANSIENT_TO_NON_TRANSIENT;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_IMPLICIT_AND_JOIN;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_ISOLATED_QUERY_PROCESS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_MANUAL_ACTIVITY;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_MANUAL_TRIGGER;
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
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.sql.DataSource;
import javax.transaction.SystemException;

import org.apache.log4j.Level;
import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
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
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.SerialActivityThreadData;
import org.eclipse.stardust.engine.core.runtime.beans.SerialActivityThreadWorkerCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.MultipleTryInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DaemonHome;
import org.eclipse.stardust.test.api.util.DaemonHome.DaemonType;
import org.eclipse.stardust.test.api.util.JmsConstants;
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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.jta.JtaTransactionManager;

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
 * @version $Revision$
 */
public class TransientProcessInstanceTest
{
   private static final String PI_BLOBS_HOLDER_FIELD_NAME = "piBlobsHolder";
   
   private static final String PERSISTENT_TO_ROOT_PI_FIELD_NAME = "persistentToRootPi";
   
   private static final String ROOT_PI_TO_PI_BLOB_FIELD_NAME = "rootPiToPiBlob";
   
   private static final String PI_IS_TRANSIENT_BPM_RT_ERROR_ID = "BPMRT03840";
   
   private static final String APP_MAY_COMPLETE = "APP_MAY_COMPLETE";
   
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
   
   /* package-private */ static final String HAZELCAST_LOGGING_TYPE_KEY = "hazelcast.logging.type";
   /* package-private */ static final String HAZELCAST_LOGGING_TYPE_VALUE = "log4j";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_ID);
   
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
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, false);
      params.set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 0);
      params.set(JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 0);
      params.set(KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER, SpringAppContextHazelcastJcaConnectionFactoryProvider.class.getName());
      params.set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, true);
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
    * ({@link AuditTrailPersistence#ENGINE_DEFAULT}) such that it causes the whole process instance graph 
    * to change to {@link AuditTrailPersistence#IMMEDIATE}. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessEngineDefault() throws Exception
   {
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
    * ({@link AuditTrailPersistence#TRANSIENT}) such that it causes the whole process instance graph 
    * to change to {@link AuditTrailPersistence#IMMEDIATE}. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessTransient() throws Exception
   {
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
    * ({@link AuditTrailPersistence#DEFERRED}) such that it causes the whole process instance graph 
    * to change to {@link AuditTrailPersistence#IMMEDIATE}. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessDeferred() throws Exception
   {
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
    * ({@link AuditTrailPersistence#IMMEDIATE}) such that it causes the whole process instance graph 
    * to change to {@link AuditTrailPersistence#IMMEDIATE}. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Test
   public void testTransientProcessAsyncSubprocessImmediate() throws Exception
   {
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
      
      ProcessInstanceStateBarrier.setTimeout(new WaitTimeout(1, TimeUnit.MINUTES));
      for (final Future<Long> f : piOids)
      {
         ProcessInstanceStateBarrier.instance().await(f.get(), ProcessInstanceState.Completed);
         
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(f.get()), is(false));
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
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
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
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
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
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(pi.getOID()));
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
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
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
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(pi.getOID()));
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
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
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
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
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
    * Tests whether joining a transient process instance to a case is rejected with the correct exception.
    * </p>
    */
   @Test
   public void testJoinCaseIsIllegal() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance nonTransientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MANUAL_ACTIVITY, null, true);
      final ProcessInstance casePi = sf.getWorkflowService().createCase(CASE_PI_NAME, CASE_PI_DESCRIPTION, new long[] { nonTransientPi.getOID() });
      final ProcessInstance transientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);
      
      try
      {
         sf.getWorkflowService().joinCase(casePi.getOID(), new long[] { transientPi.getOID() });
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }

      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(nonTransientPi.getOID()));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(nonTransientPi.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(casePi.getOID(), ProcessInstanceState.Completed);

      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
      ProcessInstanceStateBarrier.instance().await(transientPi.getOID(), ProcessInstanceState.Completed);
      
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
      final ProcessInstance transientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);
      
      try
      {
         sf.getWorkflowService().joinProcessInstance(transientPi.getOID(), nonTransientPi.getOID(), JOIN_PI_COMMENT);
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(nonTransientPi.getOID()));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(nonTransientPi.getOID(), ProcessInstanceState.Completed);
      
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
      ProcessInstanceStateBarrier.instance().await(transientPi.getOID(), ProcessInstanceState.Completed);
      
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
      final ProcessInstance transientPi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);
      
      try
      {
         sf.getWorkflowService().joinProcessInstance(nonTransientPi.getOID(), transientPi.getOID(), JOIN_PI_COMMENT);
         fail();
      }
      catch (final IllegalOperationException e)
      {
         assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_EXCEPTION_ID_CHECK, e.getError().getId(), equalTo(PI_IS_TRANSIENT_BPM_RT_ERROR_ID));
      }
      
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findForProcessInstance(nonTransientPi.getOID()));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      ProcessInstanceStateBarrier.instance().await(nonTransientPi.getOID(), ProcessInstanceState.Completed);
      
      final Parameters params = Parameters.instance();
      params.set(APP_MAY_COMPLETE, true);
      ProcessInstanceStateBarrier.instance().await(transientPi.getOID(), ProcessInstanceState.Completed);
      
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
      fail();
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
   public void testTimerTriggerDaemonIgnoresTransientProcessDefinitions() throws Exception
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
   
   private boolean hasEntryInDbForPi(final long oid) throws SQLException
   {
      final DataSource ds = testClassSetup.dataSource();
      final boolean result;
      
      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT * FROM PUBLIC.PROCESS_INSTANCE WHERE OID = " + oid);
         result = rs.first();
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
      
      return result;
   }
   
   private boolean noSerialActivityThreadQueues()
   {
      final Map<Long, SerialActivityThreadData> map = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SerialActivityThreadWorkerCarrier.SERIAL_ACTIVITY_THREAD_MAP_ID);
      return map.isEmpty();
   }
   
   private void startProcessViaJms(final String processId)
   {
      final Queue queue = testClassSetup.queue(JmsProperties.APPLICATION_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setSessionTransacted(true);
      jmsTemplate.send(queue, new MessageCreator()
      {
         @Override
         public Message createMessage(final Session session) throws JMSException
         {
            final MapMessage msg = session.createMapMessage();
            msg.setStringProperty(DefaultMessageHelper.PROCESS_ID_HEADER, processId);
            
            return msg;
         }
      });
   }
   
   private long receiveProcessInstanceCompletedMessage() throws JMSException
   {
      final Queue queue = testClassSetup.queue(JmsConstants.TEST_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setReceiveTimeout(5000L);
      
      final Message message = jmsTemplate.receive(queue);
      if (message == null)
      {
         throw new JMSException("Timeout while receiving.");
      }
      return message.getLongProperty(DefaultMessageHelper.PROCESS_INSTANCE_OID_HEADER);
   }
   
   /* package-private */ static Set<ProcessExecutor> initProcessExecutors(final int nThreads, final WorkflowService wfService)
   {
      final String processId = PROCESS_DEF_ID_SPLIT_SPLIT;

      final Set<ProcessExecutor> processExecutors = new HashSet<ProcessExecutor>();
      for (int i=0; i<nThreads; i++)
      {
         final ProcessExecutor pe = new ProcessExecutor(wfService, processId);
         processExecutors.add(pe);
      }
      
      return processExecutors;
   }
   
   /* package-private */ static List<Future<Long>> executeProcesses(final int nThreads, final Set<ProcessExecutor> processExecutors) throws InterruptedException
   {
      final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
      
      final List<Future<Long>> piOids = executor.invokeAll(processExecutors);
      executor.shutdown();
      boolean terminatedGracefully = executor.awaitTermination(10, TimeUnit.SECONDS);
      if ( !terminatedGracefully)
      {
         throw new IllegalStateException("Executor hasn't been terminated gracefully.");
      }
      
      return piOids;
   }
   
   private void enableTransientProcessesSupport()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ON);
      
      dropTransientProcessInstanceStorage();
   }
   
   private void overrideTransientProcessesSupport(final String override)
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, override);
      
      dropTransientProcessInstanceStorage();
   }   
   
   private void disableTransientProcessesSupport()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF);
   }
   
   private void disableInMemStorageExposal()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, false);
   }
   
   private void enableTxPropagation()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION, KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION_ALWAYS);
   }
   
   private void enableOneSystemQueueConsumerRetry()
   {
      final Parameters params = Parameters.instance();
      params.set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 2);
   }
   
   private void dropTransientProcessInstanceStorage()
   {
      getPersistentToRootPiMap().clear();
      getRootPiToBlobMap().clear();
   }
   
   private boolean isTransientProcessInstanceStorageEmpty()
   {
      final Map<?, ?> persistentToRootPiMap = getPersistentToRootPiMap();
      final Map<?, ?> rootPiToBlobMap = getRootPiToBlobMap();
      
      return persistentToRootPiMap.isEmpty() && rootPiToBlobMap.isEmpty();
   }
   
   private Map<?, ?> getPersistentToRootPiMap()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      return (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, PERSISTENT_TO_ROOT_PI_FIELD_NAME);
   }

   private Map<?, ?> getRootPiToBlobMap()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      return (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, ROOT_PI_TO_PI_BLOB_FIELD_NAME);
   }
   
   private void assertPiInfoIsComplete(final ProcessInstance pi, final boolean considerTerminationTime)
   {
      assertThat(pi.getDetailsLevel(), notNullValue());
      assertThat(pi.getDetailsOptions(), notNullValue());
      assertThat(pi.getModelElementID(), notNullValue());
      assertThat(pi.getModelElementOID(), not(0));
      assertThat(pi.getModelOID(), not(0));
      assertThat(pi.getOID(), not(0L));
      assertThat(pi.getParentProcessInstanceOid(), not(0L));
      assertThat(pi.getPriority(), not(-1));
      assertThat(pi.getProcessID(), notNullValue());
      assertThat(pi.getProcessName(), notNullValue());
      assertThat(pi.getRootProcessInstanceOID(), not(0L));
      assertThat(pi.getScopeProcessInstance(), notNullValue());
      assertThat(pi.getScopeProcessInstanceOID(), not(0L));
      assertThat(pi.getStartingUser(), notNullValue());
      assertThat(pi.getStartTime(), notNullValue());
      assertThat(pi.getState(), notNullValue());
      if (considerTerminationTime)
      {
         assertThat(pi.getTerminationTime(), notNullValue());
      }
   }

   private void assertAiInfoIsComplete(final ActivityInstance ai)
   {
      assertThat(ai.getActivity(), notNullValue());
      assertThat(ai.getLastModificationTime(), notNullValue());
      assertThat(ai.getModelElementID(), notNullValue());
      assertThat(ai.getModelElementOID(), not(0));
      assertThat(ai.getModelOID(), not(0));
      assertThat(ai.getOID(), not(0L));
      assertThat(ai.getProcessDefinitionId(), notNullValue());
      assertThat(ai.getProcessInstance(), notNullValue());
      assertThat(ai.getProcessInstanceOID(), not(0L));
      assertThat(ai.getStartTime(), notNullValue());
      assertThat(ai.getState(), notNullValue());
   }
   
   /**
    * <p>
    * This is the application used in the model that causes the process instance to fail
    * in order to investigate the behavior in case of failures.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class FailingApp
   {
      public void fail()
      {
         /* always throws an exception to test behavior in case of failures */
         throw new RuntimeException("expected");
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that simply succeeds.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class SucceedingApp
   {
      public void success()
      {
         /* nothing to do */
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that prevents the current transaction
    * from being committed.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class SettingRollbackOnlyApp
   {
      private static final String JTA_TX_MANAGER_SPRING_BEAN_ID = "jtaTxManager";
      
      public void setRollbackOnly() throws SystemException
      {
         final JtaTransactionManager txManager = SpringUtils.getApplicationContext().getBean(JTA_TX_MANAGER_SPRING_BEAN_ID, JtaTransactionManager.class);
         txManager.getUserTransaction().setRollbackOnly();
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that aborts the process instance.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class AbortingApp
   {
      public void abort(final long piOid)
      {
         new AdministrationServiceImpl().abortProcessInstance(piOid);
         throw new RuntimeException("Aborting process instance ... (expected exception)");
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that queries for the current process
    * instance in a new transaction.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class IsolatedQueryApp
   {
      public void queryIsolated(final long piOid)
      {
         final ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
         final ForkingService forkingService = factory.get();
         forkingService.isolate(new Action<Void>()
         {
            @Override
            public Void execute()
            {
               new WorkflowServiceImpl().getProcessInstance(piOid);
               return null;
            }
         });
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that waits for some time
    * in case it's not allowed to proceed.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class WaitingApp
   {
      public void doWait() throws InterruptedException
      {
         final Parameters params = Parameters.instance();
         boolean mayComplete = ((Boolean) params.get(APP_MAY_COMPLETE)).booleanValue();
         while ( !mayComplete)
         {
            Thread.sleep(1000);
            mayComplete = ((Boolean) params.get(APP_MAY_COMPLETE)).booleanValue();
         }
      }
   }
   
   /* package-private */ static final class ProcessExecutor implements Callable<Long>
   {
      private final WorkflowService wfService;
      private final String processId;
      
      public ProcessExecutor(final WorkflowService wfService, final String processId)
      {
         this.wfService = wfService;
         this.processId = processId;
      }
      
      @Override
      public Long call() throws Exception
      {
         final ProcessInstance pi = wfService.startProcess(processId, null, true);
         return pi.getOID();
      }
   }
}
