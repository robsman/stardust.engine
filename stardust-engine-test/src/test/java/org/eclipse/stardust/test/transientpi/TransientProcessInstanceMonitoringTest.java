/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.transientpi;

import static org.eclipse.stardust.test.api.monitoring.Operation.SELECT;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.ALTERNATIVE_IMPL_MODEL_ID;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_BIG_DATA_ACCESS;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_FORKED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_MULTIPLE_RETRY;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_NON_FORKED;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_PROCESS_INTERFACE;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SUB_SUB_PROCESS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.ModelDeploymentBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelRefBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserParticipantLink;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.test.api.monitoring.DatabaseOperationMonitoring;
import org.eclipse.stardust.test.api.monitoring.TableOperation;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class makes sure that we're aware of every single database access when executing transient
 * process instances: we monitor each and every database access and the tests only pass if they
 * only contain the ones (including frequency) defined as acceptable.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceMonitoringTest extends AbstractTransientProcessInstanceTest
{
   /**
    * needed during service incovation to determine user in whose behalf the operation should be performed
    */
   private static final TableOperation SELECT_WFUSER_REALM = new TableOperation(SELECT, UserRealmBean.TABLE_NAME);

   /**
    * needed during service incovation to determine user in whose behalf the operation should be performed
    */
   private static final TableOperation SELECT_WORKFLOWUSER = new TableOperation(SELECT, UserBean.TABLE_NAME);

   /**
    * we have to accept it since we have no means at this time to determine whether we're in the context of a
    * transient process instance execution (needs to be decided <b>before</b> entering the service call method)
    */
   private static final TableOperation SELECT_USER_PARTICIPANT = new TableOperation(SELECT, UserParticipantLink.TABLE_NAME);

   /**
    * TODO CRNT-32506 - acceptable for process interface scenarios?
    */
   private static final TableOperation SELECT_MODEL_DEP = new TableOperation(SELECT, ModelDeploymentBean.TABLE_NAME);

   /**
    * TODO CRNT-32506 - acceptable for process interface scenarios?
    */
   private static final TableOperation SELECT_MODEL_REF = new TableOperation(SELECT, ModelRefBean.TABLE_NAME);


   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_ID, ALTERNATIVE_IMPL_MODEL_ID);

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
   public void setUp() throws SQLException
   {
      final GlobalParameters params = GlobalParameters.globals();
      params.set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 0);
      params.set(JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 0);
      params.set(KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER, SpringAppContextHazelcastJcaConnectionFactoryProvider.class.getName());
      params.set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, true);
      params.set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ON);

      dropTransientProcessInstanceStorage();
      dropSerialActivityThreadQueues();

      initMonitoring(sf);
   }

   @After
   public void tearDown() throws SQLException
   {
      endMonitoring();
   }

   public TransientProcessInstanceMonitoringTest()
   {
      super(testClassSetup);
   }

   /**
    * <p>
    * This scenario comprises transient process instance execution <b>without</b> fork-on-traversal.
    * </p>
    */
   @Test
   public void testTransientProcessInstanceWithoutForkOnTraversal()
   {
      sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);

      DatabaseOperationMonitoring.instance().assertExactly(SELECT_WFUSER_REALM.times(1),
                                                           SELECT_WORKFLOWUSER.times(1),
                                                           SELECT_USER_PARTICIPANT.times(1));
   }

   /**
    * <p>
    * This scenario comprises transient process instance execution <b>with</b> fork-on-traversal.
    * </p>
    */
   @Test
   public void testTransientProcessInstanceWithForkOnTraversal() throws Exception
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      DatabaseOperationMonitoring.instance().assertExactly(SELECT_WFUSER_REALM.times(1),
                                                           SELECT_WORKFLOWUSER.times(1),
                                                           SELECT_USER_PARTICIPANT.times(1));
   }

   /**
    * <p>
    * This scenario comprises transient process instance execution invoking a sub-process having
    * multiple XOR-splits.
    * </p>
    */
   @Test
   public void testTransientProcessInstanceWithSubProcessHavingSubProcessWithSplits() throws Exception
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SUB_SUB_PROCESS, null, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      DatabaseOperationMonitoring.instance().assertExactly(SELECT_WFUSER_REALM.times(1),
                                                           SELECT_WORKFLOWUSER.times(1),
                                                           SELECT_USER_PARTICIPANT.times(1));
   }

   /**
    * <p>
    * This scenario comprises transient process instance execution including a retry, i.e. the first time
    * an application invocation fails, the second time it passes.
    * </p>
    */
   @Test
   public void testTransientProcessInstanceWithMultipleRetry() throws Exception
   {
      enableTxPropagation();
      enableOneSystemQueueConsumerRetry();
      enableTransientProcessesSupport();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_MULTIPLE_RETRY, null, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      DatabaseOperationMonitoring.instance().assertExactly(SELECT_WFUSER_REALM.times(1),
                                                           SELECT_WORKFLOWUSER.times(1),
                                                           SELECT_USER_PARTICIPANT.times(1));
   }

   /**
    * <p>
    * This scenario comprises transient process instance execution writing a big data that is split up
    * before being written to the database and concatenated when being read from the database.
    * </p>
    */
   @Test
   public void testTransientProcessInstanceWithBigData() throws Exception
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_BIG_DATA_ACCESS, null, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      DatabaseOperationMonitoring.instance().assertExactly(SELECT_WFUSER_REALM.times(1),
                                                           SELECT_WORKFLOWUSER.times(1),
                                                           SELECT_USER_PARTICIPANT.times(1));
   }

   /**
    * <p>
    * This scenario comprises execution of a transient process instance whose process definition
    * declares a process interface. The standard process interface implementation is used.
    * </p>
    */
   @Test
   public void testTransientProcessInstanceDeclaringProcessInterfaceStandardImpl() throws Exception
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_PROCESS_INTERFACE, null, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      DatabaseOperationMonitoring.instance().assertExactly(SELECT_WFUSER_REALM.times(1),
                                                           SELECT_WORKFLOWUSER.times(1),
                                                           SELECT_USER_PARTICIPANT.times(1),
                                                           SELECT_MODEL_REF.times(2),
                                                           SELECT_MODEL_DEP.times(2));
   }

   /**
    * <p>
    * This scenario comprises execution of a transient process instance whose process definition
    * declares a process interface. An alternative process interface implementation is used.
    * </p>
    */
   @Test
   public void testTransientProcessInstanceDeclaringProcessInterfaceAlternativeImpl() throws Exception
   {
      final int modelOid = setUpAlternativePrimaryImplementation();

      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_PROCESS_INTERFACE, null, true);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      DatabaseOperationMonitoring.instance().assertExactly(SELECT_WFUSER_REALM.times(1),
                                                           SELECT_WORKFLOWUSER.times(1),
                                                           SELECT_USER_PARTICIPANT.times(1),
                                                           SELECT_MODEL_REF.times(2),
                                                           SELECT_MODEL_DEP.times(2));

      tearDownAlternativePrimaryImplementation(modelOid);
   }

   private int setUpAlternativePrimaryImplementation() throws SQLException
   {
      /* stop monitoring for doing reconfiguration we do not want to monitor */
      DatabaseOperationMonitoring.instance().dropMonitoringTriggers(testClassSetup.dataSource());

      /* determine the model containing the process definition whose primary implementation we'd like to change */
      final Models models = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_ID));
      assertThat(models.size(), equalTo(1));
      final int modelOid = models.get(0).getModelOID();

      /* set primary implementation to the alternative one */
      sf.getAdministrationService().setPrimaryImplementation(modelOid, TransientProcessInstanceModelConstants.PROCESS_DEF_ID_PROCESS_INTERFACE_UNQUALIFIED, ALTERNATIVE_IMPL_MODEL_ID, null);

      /* restart monitoring */
      DatabaseOperationMonitoring.instance().createMonitoringTriggers(testClassSetup.dataSource());

      return modelOid;
   }

   private void tearDownAlternativePrimaryImplementation(final int modelOid)
   {
      /* reset primary implementation to standard implementation */
      sf.getAdministrationService().setPrimaryImplementation(modelOid, TransientProcessInstanceModelConstants.PROCESS_DEF_ID_PROCESS_INTERFACE_UNQUALIFIED, null, null);
   }
}
