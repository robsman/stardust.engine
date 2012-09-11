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

import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AuditTrailPersistence;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage;
import org.eclipse.stardust.engine.core.runtime.beans.SerialActivityThreadCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.SerialActivityThreadData;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.JmsConstants;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.api.util.WaitTimeout;
import org.junit.*;
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
   
   private static final String PI_BLOBS_FIELD_NAME = "piBlobs";
   
   private static final String PROCESS_EXECUTION_STATE = "Process.Execution.State";
   
   private static final String HAZELCAST_LOGGING_TYPE_KEY = "hazelcast.logging.type";
   private static final String HAZELCAST_LOGGING_TYPE_VALUE = "log4j";
   
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
      params.set(PROCESS_EXECUTION_STATE, ProcessExecutionState.NOT_STARTED);
      params.set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 0);
      params.set(JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 0);
      params.set(KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER, SpringAppContextHazelcastJcaConnectionFactoryProvider.class.getName());
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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

      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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

      assertThat(hasEntryInDbForPi(piOid), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    * 
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * ({@link AuditTrailPersistence#ENGINE_DEFAULT}) as well. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Ignore("No support for asynchronous subprocesses")
   @Test
   public void testTransientProcessAsyncSubprocessEngineDefault() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_ENGINE_DEFAULT, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(hasEntryInDbForPi(subPiOid), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    * 
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * ({@link AuditTrailPersistence#TRANSIENT}) as well. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Ignore("No support for asynchronous subprocesses")
   @Test
   public void testTransientProcessAsyncSubprocessTransient() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_TRANSIENT, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(hasEntryInDbForPi(subPiOid), is(false));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    * 
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * ({@link AuditTrailPersistence#DEFERRED}) as well. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Ignore("No support for asynchronous subprocesses")
   @Test
   public void testTransientProcessAsyncSubprocessDeferred() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_DEFERRED, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(hasEntryInDbForPi(subPiOid), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is {@link KernelTweakingProperties#SUPPORT_TRANSIENT_PROCESSES_ON}.</b>
    * </p>
    * 
    * <p>
    * Tests that transient process instance execution works for asynchronous subprocesses
    * ({@link AuditTrailPersistence#IMMEDIATE}) as well. The starting process instance is
    * {@link AuditTrailPersistence#TRANSIENT}.
    * </p>
    */
   @Ignore("No support for asynchronous subprocesses")
   @Test
   public void testTransientProcessAsyncSubprocessImmediate() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ASYNC_SUBPROCESS_IMMEDIATE, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      final long subPiOid = receiveProcessInstanceCompletedMessage();
      
      assertThat(hasEntryInDbForPi(pi.getOID()), is(false));
      assertThat(hasEntryInDbForPi(subPiOid), is(true));
      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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

      final Set<ProcessExecutor> processExecutors = initProcessExecutors(nThreads);

      final List<Future<Long>> piOids = executeProcesses(nThreads, processExecutors);
      
      ProcessInstanceStateBarrier.setTimeout(new WaitTimeout(1, TimeUnit.MINUTES));
      for (final Future<Long> f : piOids)
      {
         ProcessInstanceStateBarrier.instance().await(f.get(), ProcessInstanceState.Completed);
         
         assertThat(hasEntryInDbForPi(f.get()), is(false));
      }

      assertThat(noSerialActivityThreadQueues(), is(true));
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
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
      final Map<Long, SerialActivityThreadData> map = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SerialActivityThreadCarrier.SERIAL_ACTIVITY_THREAD_CARRIER_MAP_ID);
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
   
   private Set<ProcessExecutor> initProcessExecutors(final int nThreads)
   {
      final String processId = PROCESS_DEF_ID_SPLIT_SPLIT;
      final WorkflowService wfService = sf.getWorkflowService();

      final Set<ProcessExecutor> processExecutors = new HashSet<ProcessExecutor>();
      for (int i=0; i<nThreads; i++)
      {
         final ProcessExecutor pe = new ProcessExecutor(wfService, processId);
         processExecutors.add(pe);
      }
      
      return processExecutors;
   }
   
   private List<Future<Long>> executeProcesses(final int nThreads, final Set<ProcessExecutor> processExecutors) throws InterruptedException
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
   
   private void dropTransientProcessInstanceStorage()
   {
      final Map<?, ?> piBlobs = getPiBlobsMap();
      piBlobs.clear();
   }
   
   private boolean isTransientProcessInstanceStorageEmpty()
   {
      final Map<?, ?> piBlobs = getPiBlobsMap();
      return piBlobs.isEmpty();
   }
   
   private Map<?, ?> getPiBlobsMap()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      return (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, PI_BLOBS_FIELD_NAME);
   }
   
   /**
    * <p>
    * This is the application used in the model that causes the process instance to fail
    * in order to investigate the behavior in case of failures. In addition,
    * it sets a property so that one can determine that the process instance
    * is interrupted.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class FailingApp
   {
      public void fail()
      {
         final Parameters params = Parameters.instance();
         params.set(PROCESS_EXECUTION_STATE, ProcessExecutionState.INTERRUPTED);
         
         /* always throws an exception to test behavior in case of failures */
         throw new RuntimeException("expected");
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that simply succeeds. In addition, 
    * it sets a property so that one can determine that the process instance is completed.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class SucceedingApp
   {
      public void success()
      {
         final Parameters params = Parameters.instance();
         params.set(PROCESS_EXECUTION_STATE, ProcessExecutionState.COMPLETED);
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
   
   private static enum ProcessExecutionState { NOT_STARTED, COMPLETED, INTERRUPTED }
   
   private static final class ProcessExecutor implements Callable<Long>
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
