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
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Level;
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
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.Log4jLogMessageBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.api.util.WaitTimeout;
import org.hamcrest.Matchers;
import org.junit.Before;
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
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR,
         ForkingServiceMode.JMS, SIGNAL_EVENTS_MODEL_NAME);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Before
   public void init()
   {
      Parameters.instance().set(JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 0);
   }

   @Test
   public void testManuallySentSignalEventIsReceivedByTwoParallelSignalAcceptors() throws InterruptedException, TimeoutException, JMSException
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
   public void testSignalEventSentByProcessInstanceIsReceivedByTwoParallelSignalAcceptors() throws InterruptedException, TimeoutException, JMSException
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
      Log4jLogMessageBarrier logBarrier = new Log4jLogMessageBarrier(Level.WARN);
      logBarrier.registerWithLog4j();

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
      logBarrier.waitForLogMessage("JMS01001 - No message acceptors found for the message:\n.*", new WaitTimeout(10, TimeUnit.SECONDS));

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
      assertThat(outputData, Matchers.instanceOf(String.class));
      assertThat((String) outputData, Matchers.equalTo("Horst"));
   }

   // TODO - bpmn-2-events - test case for interrupting signal event

   private void sendSignalEvent(final String signalName) throws JMSException
   {
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
   }
}
