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

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.Date;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class MultipleStartEventsTest
{
   /* package-private */static final String START_EVENTS_MODEL_NAME = "MultipleStartEventsTest";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS,
         START_EVENTS_MODEL_NAME);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Before
   public void init()
   {}

   @Test
   public void testViaStartEvent1RoutetProperty() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess("MultipleStartEventsTest",
            singletonMap("StartEventId", "BPMN_JMSTrigger1"), true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // Start first technical activity
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "StartEventDispatcher");

      // failing sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "StartActivity1");

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);

      //
      Assert.assertEquals(sf.getQueryService().getActivityInstancesCount(ActivityInstanceQuery.findForProcessInstance(rootProcess.getOID())), 2);
   }

   @Test
   public void testViaStartEvent2RoutetProperty() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess("MultipleStartEventsTest",
            singletonMap("StartEventId", "BPMN_JMSTrigger2"), true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // Start first technical activity
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "StartEventDispatcher");

      // failing sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "StartActivity2");

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);

      //
      Assert.assertEquals(sf.getQueryService().getActivityInstancesCount(ActivityInstanceQuery.findForProcessInstance(rootProcess.getOID())), 2);
   }

   @Test
   public void testViaStartEvent3RoutetProperty() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess("MultipleStartEventsTest",
            singletonMap("StartEventId", "BPMN_JMSTrigger3"), true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // Start first technical activity
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "StartEventDispatcher");

      // failing sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "StartActivity3");

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);

      //
      Assert.assertEquals(sf.getQueryService().getActivityInstancesCount(ActivityInstanceQuery.findForProcessInstance(rootProcess.getOID())), 2);
   }

   @Test
   public void testStartEvent1ViaJMS() throws InterruptedException, TimeoutException, JMSException{

	  sendMessages("MyTest", "BPMN_JMSTrigger1");
      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
	  ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();
      Thread.sleep(2000);
	  ProcessInstance processInstance = sf.getQueryService().findFirstProcessInstance(ProcessInstanceQuery.findForProcess("MultipleStartEventsTest"));
      aiStateChangeBarrier.awaitForId(processInstance.getOID(), "StartActivity1");
	  piStateChangeBarrier.await(processInstance.getOID(), ProcessInstanceState.Completed);
      Assert.assertEquals(sf.getQueryService().getActivityInstancesCount(ActivityInstanceQuery.findForProcessInstance(processInstance.getOID())), 2);
   }

   @Test
   public void testStartEvent2ViaJMS() throws InterruptedException, TimeoutException, JMSException{

	  sendMessages("MyTest", "BPMN_JMSTrigger2");
      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
	  ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();
      Thread.sleep(2000);
	  ProcessInstance processInstance = sf.getQueryService().findFirstProcessInstance(ProcessInstanceQuery.findForProcess("MultipleStartEventsTest"));
      aiStateChangeBarrier.awaitForId(processInstance.getOID(), "StartActivity2");
	  piStateChangeBarrier.await(processInstance.getOID(), ProcessInstanceState.Completed);
      Assert.assertEquals(sf.getQueryService().getActivityInstancesCount(ActivityInstanceQuery.findForProcessInstance(processInstance.getOID())), 2);
   }

   @Test
   public void testStartEvent3ViaJMS() throws InterruptedException, TimeoutException, JMSException{

	  sendMessages("MyTest", "BPMN_JMSTrigger3");
      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
	  ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();
      Thread.sleep(2000);
	  ProcessInstance processInstance = sf.getQueryService().findFirstProcessInstance(ProcessInstanceQuery.findForProcess("MultipleStartEventsTest"));
      aiStateChangeBarrier.awaitForId(processInstance.getOID(), "StartActivity3");
	  piStateChangeBarrier.await(processInstance.getOID(), ProcessInstanceState.Completed);
      Assert.assertEquals(sf.getQueryService().getActivityInstancesCount(ActivityInstanceQuery.findForProcessInstance(processInstance.getOID())), 2);
   }

   private void sendMessages(String messageId, final String startEventName)
         throws JMSException
   {
      JmsTemplate jmsTemplate = new JmsTemplate(testClassSetup.queueConnectionFactory());

      final StringBuilder payload = new StringBuilder();
      payload.append("Message [").append(messageId).append("] sent at: ")
            .append(new Date());

      jmsTemplate.send(testClassSetup.queue("jms/CarnotApplicationQueue"),
            new MessageCreator()
            {
               public Message createMessage(Session session) throws JMSException
               {
                  TextMessage message = session.createTextMessage(payload.toString());
                  message.setStringProperty("StartEventId", startEventName);
                  return message;
               }
            });
   }
}
