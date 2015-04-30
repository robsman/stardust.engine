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

import java.util.Date;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
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
   {}

   @Test
   public void testThrownSignalEventIsReceivedByTwoParallelSignalEvents() throws InterruptedException, TimeoutException, JMSException
   {
      WorkflowService wfs = sf.getWorkflowService();

      // TODO This test sometimes fails (sometimes it succeeds)

      ProcessInstance rootProcess = wfs.startProcess("{SignalEventsTestModel}TwoSignalsParallel", null, true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // failing sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "LeftSignal");
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "RightSignal");

      sendSignalEvent("Signal1");

      Thread.sleep(2000);

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
   }

   private void sendSignalEvent(final String signalName) throws JMSException
   {
      JmsTemplate jmsTemplate = new JmsTemplate(testClassSetup.queueConnectionFactory());

      final StringBuilder payload = new StringBuilder();
      payload.append("Message [").append(signalName).append("] sent at: ")
            .append(new Date());

      jmsTemplate.send(testClassSetup.queue("jms/CarnotApplicationQueue"),
            new MessageCreator()
            {
               public Message createMessage(Session session) throws JMSException
               {
                  TextMessage message = session.createTextMessage(payload.toString());
                  message.setStringProperty("stardust.bpmn.signal", signalName);
                  message.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER, "default");
                  return message;
               }
            });
   }

}
