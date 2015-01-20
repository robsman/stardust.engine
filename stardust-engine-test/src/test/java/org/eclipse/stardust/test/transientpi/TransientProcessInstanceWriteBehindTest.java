/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.transientpi;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.MODEL_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jms.AbstractJmsBytesMessageReader;
import org.eclipse.stardust.engine.core.persistence.jms.BlobReader;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobAuditTrailPersistor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.jms.core.JmsTemplate;

/**
 * <p>
 * Tests the <i>Write Behind</i> functionality.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/audit-trail-queue.app-ctx.xml")
public class TransientProcessInstanceWriteBehindTest extends AbstractTransientProcessInstanceTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   @Before
   public void setUp()
   {
      Parameters.instance().set(KernelTweakingProperties.ASYNC_WRITE, Boolean.TRUE);
   }

   @After
   public void tearDown()
   {
      Parameters.instance().set(KernelTweakingProperties.ASYNC_WRITE, Boolean.FALSE);
   }

   public TransientProcessInstanceWriteBehindTest()
   {
      super(testClassSetup);
   }

   @Test
   public void testSynchronousWriteBehind() throws Exception
   {
      Parameters.instance().set(KernelTweakingProperties.ASYNC_WRITE_VIA_JMS, Boolean.FALSE);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(TransientProcessInstanceModelConstants.PROCESS_DEF_ID_NON_FORKED, null, true);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
   }

   @Test
   public void testAsynchronousWriteBehind() throws Exception
   {
      Parameters.instance().set(KernelTweakingProperties.ASYNC_WRITE_VIA_JMS, Boolean.TRUE);

      final ProcessInstance pi = sf.getWorkflowService().startProcess(TransientProcessInstanceModelConstants.PROCESS_DEF_ID_NON_FORKED, null, true);
      writeFromQueueToAuditTrail(pi.getOID(), JmsProperties.AUDIT_TRAIL_QUEUE_NAME_PROPERTY);

      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_PI_STATE_CHECK, pi.getState(), is(ProcessInstanceState.Completed));
      assertThat(testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB, hasEntryInDbForPi(pi.getOID()), is(true));
   }

   private void writeFromQueueToAuditTrail(final long piOid, final String queueName) throws JMSException
   {
      final Queue queue = testClassSetup.queue(queueName);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setReceiveTimeout(5000L);

      final Message message = jmsTemplate.receive(queue);
      if (message == null)
      {
         throw new JMSException("Timeout while receiving.");
      }
      if ( !(message instanceof BytesMessage))
      {
         throw new UnsupportedOperationException("Can only read from bytes message.");
      }

      final ServiceCommand writeToAuditTrail = new WriteToAuditTrailCommand((BytesMessage) message);
      sf.getWorkflowService().execute(writeToAuditTrail);
   }

   private static final class WriteToAuditTrailCommand implements ServiceCommand
   {
      private static final long serialVersionUID = -1945946762667325417L;

      private final BytesMessage message;

      public WriteToAuditTrailCommand(final BytesMessage message)
      {
         this.message = message;
      }

      @Override
      public Serializable execute(final ServiceFactory sf)
      {
         final BlobReader reader = new AbstractJmsBytesMessageReader()
         {
            @Override
            protected BytesMessage nextBlobContainer() throws PublicException
            {
               return message;
            }
         };
         reader.nextBlob();

         final ProcessBlobAuditTrailPersistor persistor = new ProcessBlobAuditTrailPersistor();
         persistor.persistBlob(reader);
         persistor.writeIntoAuditTrail((Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL), 1);

         reader.close();

         return null;
      }
   }
}
