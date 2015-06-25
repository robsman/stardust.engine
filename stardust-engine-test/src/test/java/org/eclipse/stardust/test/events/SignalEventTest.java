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
package org.eclipse.stardust.test.events;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.extensions.events.signal.SignalMessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.JmsConstants;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.h2.api.Trigger;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class SignalEventTest
{
   /* package-private */static final String SIGNAL_EVENTS_MODEL_NAME = "SignalEventsTestModel";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR,
         ForkingServiceMode.JMS, SIGNAL_EVENTS_MODEL_NAME);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @BeforeClass
   public static void setUpOnce() throws SQLException
   {
      initSignalMessageTrigger();
   }

   @Before
   public void init()
   {
      Parameters.instance().set(JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 0);
   }

   @Test
   public void testManuallySentSignalEventIsReceivedByTwoParallelSignalAcceptors() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}TwoSignalsParallel", null, true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "LeftSignal");
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "RightSignal");

      sendSignalEvent("Signal1");

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testSignalEventSentByProcessInstanceIsReceivedByTwoParallelSignalAcceptors() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}TwoSignalsParallel", null, true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "LeftSignal");
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "RightSignal");

      ProcessInstance pi = wfs.startProcess("{SignalEventsTestModel}SendSignal", null, true);
      aiStateChangeBarrier.awaitForId(pi.getOID(), "StartActivity");
      wfs.setOutDataPath(pi.getOID(), "Data_1Path", "Horst");
      wfs.setOutDataPath(pi.getOID(), "Data_2Path", "Klaus");
      ActivityInstance ai = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi.getOID(), "StartActivity"));
      wfs.activateAndComplete(ai.getOID(), null, null);

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testSignalEventSentByProcessInstanceIsReceivedBySignalAcceptorHavingPredicates() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // start signal acceptor process and initialize with predicate data
      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}SignalWithPredicateData", null, true);
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "SignalAcceptorActivity");
      ActivityInstance signalAcceptorAi = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(rootProcess.getOID(), "SignalAcceptorActivity"));
      assertThat(signalAcceptorAi.getState(), equalTo(ActivityInstanceState.Hibernated));
      wfs.setOutDataPath(rootProcess.getOID(), "PredicateData_1Path", "Klaus");
      wfs.setOutDataPath(rootProcess.getOID(), "PredicateData_2Path", "Horst");

      // fire signal the signal acceptor is *not* waiting for
      ProcessInstance pi1 = wfs.startProcess("{SignalEventsTestModel}SendSignal", null, true);
      aiStateChangeBarrier.awaitForId(pi1.getOID(), "StartActivity");
      wfs.setOutDataPath(pi1.getOID(), "Data_1Path", "Klaus");
      wfs.setOutDataPath(pi1.getOID(), "Data_2Path", "Paul");
      ActivityInstance ai1 = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi1.getOID(), "StartActivity"));
      wfs.activateAndComplete(ai1.getOID(), null, null);
      piStateChangeBarrier.await(pi1.getOID(), ProcessInstanceState.Completed);

      // signal hasn't been accepted
      try
      {
         piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
         fail();
      }
      catch (final TimeoutException e) { /* expected */ }

      // fire signal the signal acceptor is waiting for
      ProcessInstance pi2 = wfs.startProcess("{SignalEventsTestModel}SendSignal", null, true);
      aiStateChangeBarrier.awaitForId(pi2.getOID(), "StartActivity");
      wfs.setOutDataPath(pi2.getOID(), "Data_1Path", "Klaus");
      wfs.setOutDataPath(pi2.getOID(), "Data_2Path", "Horst");
      ActivityInstance ai2 = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi2.getOID(), "StartActivity"));
      wfs.activateAndComplete(ai2.getOID(), null, null);
      piStateChangeBarrier.await(pi2.getOID(), ProcessInstanceState.Completed);

      // signal has been accepted, assert that output data is correct
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
      Object outputData = wfs.getInDataPath(rootProcess.getOID(), "OutputDataPath");
      assertThat(outputData, instanceOf(String.class));
      assertThat((String) outputData, equalTo("Horst"));
   }

   @Test
   public void testSignalEventSentByProcessInstanceIsReceivedBySignalAcceptorHavingOnePredicate() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // start signal acceptor process and initialize with predicate data
      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}SignalWithPredicateDate", null, true);
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "SignalAcceptorActivity");
      ActivityInstance signalAcceptorAi = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(rootProcess.getOID(), "SignalAcceptorActivity"));
      assertThat(signalAcceptorAi.getState(), equalTo(ActivityInstanceState.Hibernated));
      wfs.setOutDataPath(rootProcess.getOID(), "PredicateData_1Path", "Klaus");

      // fire a signal the signal acceptor is waiting for
      ProcessInstance pi = wfs.startProcess("{SignalEventsTestModel}SendSignal", null, true);
      aiStateChangeBarrier.awaitForId(pi.getOID(), "StartActivity");
      wfs.setOutDataPath(pi.getOID(), "Data_1Path", "Klaus");
      wfs.setOutDataPath(pi.getOID(), "Data_2Path", "Horst");
      ActivityInstance ai = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi.getOID(), "StartActivity"));
      wfs.activateAndComplete(ai.getOID(), null, null);
      piStateChangeBarrier.await(pi.getOID(), ProcessInstanceState.Completed);

      // signal has been accepted, assert that output data is correct
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
      Object outputData = wfs.getInDataPath(rootProcess.getOID(), "OutputDataPath");
      assertThat(outputData, Matchers.instanceOf(String.class));
      assertThat((String) outputData, Matchers.equalTo("Horst"));
   }

   @Test
   public void testSignalTriggerParameterMappings() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // fire a signal the signal trigger is waiting for
      ProcessInstance pi = wfs.startProcess("{SignalEventsTestModel}SendTriggerSignal", null, true);
      aiStateChangeBarrier.awaitForId(pi.getOID(), "StartActivity");
      wfs.setOutDataPath(pi.getOID(), "Data_1Path", "Klaus");
      wfs.setOutDataPath(pi.getOID(), "Data_2Path", "Horst");
      ActivityInstance ai = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi.getOID(), "StartActivity"));
      wfs.activateAndComplete(ai.getOID(), null, null);
      piStateChangeBarrier.await(pi.getOID(), ProcessInstanceState.Completed);

      // await trigger process completion
      long triggerPiOid = receiveProcessInstanceCompletedMessage("SignalJMSTrigger");

      // assert message parameters
      Object obj1 = wfs.getInDataPath(triggerPiOid, "TriggerData_1Path");
      assertThat(obj1, Matchers.instanceOf(String.class));
      assertThat((String) obj1, Matchers.equalTo("Klaus"));

      Object obj2 = wfs.getInDataPath(triggerPiOid, "TriggerData_2Path");
      assertThat(obj2, Matchers.instanceOf(String.class));
      assertThat((String) obj2, Matchers.equalTo("Horst"));
   }

   @Test
   public void testOutOfOrderSignal() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // send signal before process has been started
      sendSignalEvent("Signal1");

      // start process to receive signal
      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}TwoSignalsParallel", null, true);
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "LeftSignal");
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "RightSignal");

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testOutOfOrderSignalWithPredicateData() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // fire signal the signal acceptor will be waiting for
      SignalMessageBeanTrigger.initLatch();
      ProcessInstance pi1 = wfs.startProcess("{SignalEventsTestModel}SendSignal", null, true);
      aiStateChangeBarrier.awaitForId(pi1.getOID(), "StartActivity");
      wfs.setOutDataPath(pi1.getOID(), "Data_1Path", "Klaus");
      wfs.setOutDataPath(pi1.getOID(), "Data_2Path", "Horst");
      ActivityInstance ai1 = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi1.getOID(), "StartActivity"));
      wfs.activateAndComplete(ai1.getOID(), null, null);
      piStateChangeBarrier.await(pi1.getOID(), ProcessInstanceState.Completed);

      // wait until signal has been persisted
      waitUntilSignalMessageHasBeenWrittenToDb();

      // start signal acceptor process and initialize with predicate data
      final Map<String, Object> data = CollectionUtils.newHashMap();
      data.put("PredicateData_1", "Klaus");
      data.put("PredicateData_2", "Horst");
      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}SignalWithPredicateData", data, true);

      // signal has been accepted, assert that output data is correct
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
      Object outputData = wfs.getInDataPath(rootProcess.getOID(), "OutputDataPath");
      assertThat(outputData, Matchers.instanceOf(String.class));
      assertThat((String) outputData, Matchers.equalTo("Horst"));
   }

   @Test
   public void testOutOfOrderSignalWithNotMatchingPredicateData() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // fire signal the signal acceptor will *not* be waiting for
      SignalMessageBeanTrigger.initLatch();
      ProcessInstance pi1 = wfs.startProcess("{SignalEventsTestModel}SendSignal", null, true);
      aiStateChangeBarrier.awaitForId(pi1.getOID(), "StartActivity");
      wfs.setOutDataPath(pi1.getOID(), "Data_1Path", "Klaus");
      wfs.setOutDataPath(pi1.getOID(), "Data_2Path", "Paul");
      ActivityInstance ai1 = qs.findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi1.getOID(), "StartActivity"));
      wfs.activateAndComplete(ai1.getOID(), null, null);
      piStateChangeBarrier.await(pi1.getOID(), ProcessInstanceState.Completed);

      // wait until signal has been persisted
      waitUntilSignalMessageHasBeenWrittenToDb();

      // start signal acceptor process and initialize with predicate data
      final Map<String, Object> data = CollectionUtils.newHashMap();
      data.put("PredicateData_1", "Klaus");
      data.put("PredicateData_2", "Horst");
      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}SignalWithPredicateData", data, true);

      // signal hasn't been accepted
      try
      {
         piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
         fail();
      }
      catch (final TimeoutException e) { /* expected */ }
   }

   @Test
   public void testPastSignalMayBeIgnored() throws Exception
   {
      // fire signal 'Signal1'
      sendSignalEvent("Signal1");

      // start process waiting for signal
      ProcessInstance pi = sf.getWorkflowService().startProcess("{SignalEventsTestModel}SignalAcceptorIgnoringPastSignals", null, true);

      // past signal hasn't been accepted
      try
      {
         ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
         fail();
      }
      catch (final TimeoutException e) { /* expected */ }

      // fire signal 'Signal1'
      sendSignalEvent("Signal1");

      // fresh signal has been accepted
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
   }

   private void sendSignalEvent(final String signalName) throws JMSException, TimeoutException, InterruptedException, SQLException
   {
      SignalMessageBeanTrigger.initLatch();

      JmsTemplate jmsTemplate = new JmsTemplate(testClassSetup.queueConnectionFactory());

      jmsTemplate.send(testClassSetup.queue(JmsProperties.APPLICATION_QUEUE_NAME_PROPERTY), new MessageCreator()
         {
            public Message createMessage(Session session) throws JMSException
            {
               MapMessage message = session.createMapMessage();
               message.setStringProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_KEY, signalName);
               message.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER, PredefinedConstants.DEFAULT_PARTITION_ID);
               return message;
            }
         });

      waitUntilSignalMessageHasBeenWrittenToDb();
   }

   private long receiveProcessInstanceCompletedMessage(String processId) throws JMSException
   {
      final Queue queue = testClassSetup.queue(JmsConstants.TEST_QUEUE_NAME_PROPERTY);
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setReceiveTimeout(10000L);

      final Message message = jmsTemplate.receive(queue);
      if (message == null)
      {
         throw new JMSException("Timeout while receiving.");
      }
      long piOid = message.getLongProperty(DefaultMessageHelper.PROCESS_INSTANCE_OID_HEADER);
      assertThat(sf.getWorkflowService().getProcessInstance(piOid).getProcessID(), equalTo(processId));

      return piOid;
   }

   private static void initSignalMessageTrigger() throws SQLException
   {
      executeSqlStatement("CREATE TRIGGER signal_message_trigger AFTER INSERT ON signal_message CALL \"org.eclipse.stardust.test.events.SignalEventTest$SignalMessageBeanTrigger\"");
   }

   private static void waitUntilSignalMessageHasBeenWrittenToDb() throws SQLException, InterruptedException, TimeoutException
   {
      /* row has been inserted ... */
      boolean success = SignalMessageBeanTrigger.countDownLatch().await(10, TimeUnit.SECONDS);
      if ( !success)
      {
         throw new TimeoutException("Signal Message still hasn't been written to DB.");
      }

      /* ... now we need to wait for the tx to be committed (thread calling the trigger does no have a tx attached) */
      executeSqlStatement("SELECT * FROM signal_message FOR UPDATE");
   }

   private static void executeSqlStatement(String sqlStmt) throws SQLException
   {
      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = testClassSetup.dataSource().getConnection();
         stmt = connection.createStatement();
         stmt.execute(sqlStmt);
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
   }

   public static final class SignalMessageBeanTrigger implements Trigger
   {
      public static volatile CountDownLatch COUNT_DOWN_LATCH;

      public static void initLatch()
      {
         COUNT_DOWN_LATCH = new CountDownLatch(1);
      }

      public static CountDownLatch countDownLatch()
      {
         return COUNT_DOWN_LATCH;
      }

      @Override
      public void fire(Connection conn, Object[] oldRow, Object[] newRow)
      {
         if (COUNT_DOWN_LATCH != null)
         {
            COUNT_DOWN_LATCH.countDown();
         }
      }

      @Override
      public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
      {
         /* nothing to do */
      }

      @Override
      public void close()
      {
         /* nothing to do */
      }

      @Override
      public void remove()
      {
         /* nothing to do */
      }
   }
}
