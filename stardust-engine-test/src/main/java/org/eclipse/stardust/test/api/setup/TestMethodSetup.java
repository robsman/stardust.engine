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
package org.eclipse.stardust.test.api.setup;

import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.spring.threading.FiFoJobManager;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;

/**
 * <p>
 * This class deals with test method setup, i.e. it cleans up the runtime and deletes all created users
 * after the test case execution. Plus, it logs the test method's name before entering and after leaving
 * the test method.
 * </p>
 *
 * <p>
 * This class is responsible for the test method setup whereas {@link TestClassSetup}
 * deals with test class setup and {@link TestSuiteSetup} deals with test suite setup.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TestMethodSetup extends ExternalResource
{
   private static final Log LOG = LogFactory.getLog(TestMethodSetup.class);

   private static final String LOG_EYE_CATCHER = "################## Test Method Boundary ##################";

   private static final String NATIVE_THREADING_JOB_MANAGER_BEAN_ID = "carnotAsyncJobManager";

   private final UsernamePasswordPair userPwdPair;
   private final TestClassSetup testClassSetup;

   private ServiceFactory sf;
   private String testMethodName;

   /**
    * <p>
    * Sets up a runtime configurer with the username password pair to use for test method setup.
    * </p>
    *
    * @param userPwdPair the credentials of the user used for test method setup; must not be null
    * @param testClassSetup the corresponding test class setup object; must not be null
    */
   public TestMethodSetup(final UsernamePasswordPair userPwdPair, final TestClassSetup testClassSetup)
   {
      if (userPwdPair == null)
      {
         throw new NullPointerException("User password pair must not be null.");
      }
      if (testClassSetup == null)
      {
         throw new NullPointerException("Test class setup must not be null.");
      }

      this.userPwdPair = userPwdPair;
      this.testClassSetup = testClassSetup;
   }

   /**
    * @return the service factory this object has been initialized with
    */
   protected final ServiceFactory serviceFactory()
   {
      return sf;
   }

   /**
    * @return the name of the test method currently executed
    */
   public final String testMethodName()
   {
      return testMethodName;
   }

   /* (non-Javadoc)
    * @see org.junit.rules.ExternalResource#apply(org.junit.runners.model.Statement, org.junit.runner.Description)
    */
   public final Statement apply(final Statement base, final Description description) {
      testMethodName = description.getMethodName();

      return super.apply(base, description);
   }

   /**
    * <p>
    * Does internal initialization.
    * </p>
    */
   @Override
   protected void before()
   {
      logBeforeTestMethod();

      setUpServiceFactory();
   }

   protected final void logBeforeTestMethod()
   {
      LOG.info(LOG_EYE_CATCHER);
      LOG.info("--> " + testMethodName);
   }

   protected final void setUpServiceFactory()
   {
      sf = ServiceFactoryLocator.get(userPwdPair.username(), userPwdPair.password());
   }

   /**
    * <p>
    * Cleans up the runtime (including user removal) without deleting the deployed models.
    * </p>
    */
   @Override
   protected void after()
   {
      logRunningActivityThreads();
      RtEnvHome.cleanUpRuntime(sf.getAdministrationService());

      tearDownServiceFactory();

      logAfterTestMethod();
   }

   protected final void tearDownServiceFactory()
   {
      sf.close();
      sf = null;
   }

   protected final void logAfterTestMethod()
   {
      LOG.info("<-- " + testMethodName);
      LOG.info(LOG_EYE_CATCHER);
   }

   private void logRunningActivityThreads()
   {
      if (testClassSetup.forkingServiceMode() == ForkingServiceMode.NATIVE_THREADING)
      {
         logRunningActivityThreadsForNativeThreading();
      }
      else if (testClassSetup.forkingServiceMode() == ForkingServiceMode.JMS)
      {
         logRunningActivityThreadsForJMS();
      }
      else
      {
         throw new IllegalStateException("Unknown forking service mode '" + testClassSetup.forkingServiceMode() + "'.");
      }
   }

   private void logRunningActivityThreadsForNativeThreading()
   {
      final FiFoJobManager jobManager = testClassSetup.getBean(NATIVE_THREADING_JOB_MANAGER_BEAN_ID, FiFoJobManager.class);
      final List<?> activeJobs = (List<?>) Reflect.getFieldValue(jobManager, "activeJobs");
      final List<?> scheduledJobs = (List<?>) Reflect.getFieldValue(jobManager, "scheduledJobs");

      for (final Object o : activeJobs)
      {
         logRunningActivityThread(o);
      }
      for (final Object o : scheduledJobs)
      {
         logRunningActivityThread(o);
      }
   }

   private void logRunningActivityThreadsForJMS()
   {
      final Queue queue = testClassSetup.queue(JmsProperties.SYSTEM_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.browse(queue, new BrowserCallback<Void>()
      {
         @Override
         public Void doInJms(final Session ignored, final QueueBrowser browser) throws JMSException
         {
            final Enumeration<?> ats = browser.getEnumeration();
            while (ats.hasMoreElements())
            {
               logRunningActivityThread(ats.nextElement());
            }

            return null;
         }
      });
   }

   private void logRunningActivityThread(final Object at)
   {
      LOG.warn("Still running activity thread found: " + at);
   }
}
