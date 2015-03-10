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

import java.util.Arrays;

import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.spi.jms.IJmsResourceProvider;
import org.eclipse.stardust.test.api.util.TestModels;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.impl.H2Server;
import org.eclipse.stardust.test.impl.SpringAppContext;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.context.ApplicationContext;

/**
 * <p>
 * A JUnit test class needs to
 * <ul>
 *   <li>declared a field of this class and</li>
 *   <li>annotate this field with {@linkplain org.junit.ClassRule}</li>
 * </ul>
 * in order to be able to execute tests in a local Spring environment, using an H2 DB and having
 * JCR support. By doing so the needed setup and teardown will be done automatically.
 * </p>
 *
 * <p>
 * The setup and teardown will only be done, if the class is not locked. Locking can
 * be used by a test suite to chain some test classes without having the effort
 * of test environment setup and teardown after every single test class.
 * </p>
 *
 * <p>
 * This class is responsible for the test class setup whereas {@link TestSuiteSetup}
 * deals with test suite setup and {@link TestMethodSetup} deals with test method setup.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TestClassSetup extends ExternalResource
{
   private static final Log LOG = LogFactory.getLog(TestClassSetup.class);

   private static final String DATA_SOURCE_FACTORY_BEAN_ID = "xaAuditTrailConnectionFactory";
   private static final String JMS_RESOURCE_PROVIDER_BEAN_ID = "jmsResourceResolver";

   private static boolean locked;

   private final H2Server dbms;
   private final SpringAppContext springAppCtx;

   private final DeploymentOptions deploymentOptions;
   private final String[] modelNames;
   private final UsernamePasswordPair userPwdPair;
   private final ForkingServiceMode forkingServiceMode;

   private ServiceFactory sf;

   private DataSource ds;
   private IJmsResourceProvider jmsResourceProvider;

   private Class<?> testClass;

   /**
    * <p>
    * Initializes the object with the username password pair and the models to deploy. Furthermore, it specifies which forking service
    * mode to use.
    * </p>
    *
    * @param userPwdPair the credentials of the user in whose context the setup will be done; must not be null
    * @param forkingServiceMode the forking service's mode (JMS or non-JMS)
    * @param modelNames the names of the models to deploy; may be null or empty
    */
   public TestClassSetup(final UsernamePasswordPair userPwdPair, final ForkingServiceMode forkingServiceMode, final String ... modelNames)
   {
      if (userPwdPair == null)
      {
         throw new NullPointerException("User password pair must not be null.");
      }
      if (forkingServiceMode == null)
      {
         throw new NullPointerException("Forking service mode must not be null.");
      }
      if (modelNames == null || modelNames.length == 0)
      {
         LOG.debug("No model to deploy specified.");
      }

      this.userPwdPair = userPwdPair;
      this.forkingServiceMode = forkingServiceMode;
      this.deploymentOptions = null;
      this.modelNames = (modelNames != null) ? modelNames : new String[0];

      this.dbms = new H2Server();
      this.springAppCtx = new SpringAppContext();
   }

   /**
    * <p>
    * Initializes the object with the username password pair and the models to deploy (including optional deployment options).
    * Furthermore, it specifies which forking service mode to use.
    * </p>
    *
    * @param userPwdPair the credentials of the user in whose context the setup will be done; must not be null
    * @param forkingServiceMode the forking service's mode (JMS or non-JMS)
    * @param models the {@link TestModels} to deploy, must not be {@code null}
    */
   public TestClassSetup(final UsernamePasswordPair userPwdPair, final ForkingServiceMode forkingServiceMode, final TestModels models)
   {
      if (userPwdPair == null)
      {
         throw new NullPointerException("User password pair must not be null.");
      }
      if (forkingServiceMode == null)
      {
         throw new NullPointerException("Forking service mode must not be null.");
      }
      if (models == null)
      {
         throw new NullPointerException("Deployables must not be null.");
      }

      this.userPwdPair = userPwdPair;
      this.forkingServiceMode = forkingServiceMode;
      this.deploymentOptions = models.deploymentOptions();
      this.modelNames = models.modelNames();

      this.dbms = new H2Server();
      this.springAppCtx = new SpringAppContext();
   }

   /* (non-Javadoc)
    * @see org.junit.rules.ExternalResource#apply(org.junit.runners.model.Statement, org.junit.runner.Description)
    */
   @Override
   public Statement apply(final Statement base, final Description description)
   {
      this.testClass = description.getTestClass();

      return super.apply(base, description);
   }

   /**
    * <p>
    * Sets up the local Spring test environment with an H2 DB and JCR support, i.e.
    * <ul>
    *    <li>starts the DBMS,</li>
    *    <li>creates the Audit Trail DB schema,</li>
    *    <li>bootstraps the Spring Application Context, and</li>
    *    <li>deploys the given models.</li>
    * </ul>
    * </p>
    *
    * <p>
    * The setup will not be done, if the class is locked.
    * </p>
    *
    * @throws TestRtEnvException if an exception occurs during test environment setup
    */
   @Override
   protected void before() throws TestRtEnvException
   {
      if (locked)
      {
         return;
      }

      LOG.info("---> Setting up the test environment ...");

      dbms.init();
      dbms.start();
      dbms.createSchema();
      springAppCtx.bootstrap(forkingServiceMode, testClass);

      sf = ServiceFactoryLocator.get(userPwdPair.username(), userPwdPair.password());
      if (modelNames.length > 0)
      {
         LOG.debug("Trying to deploy model(s) '" + Arrays.asList(modelNames) + "'.");
         RtEnvHome.deploy(sf.getAdministrationService(), deploymentOptions, modelNames);
      }

      LOG.info("<--- ... setup of test environment done.");
   }

   /**
    * <p>
    * Tears down the local Spring test environment with an H2 DB and JCR support, i.e.
    * <ul>
    *    <li>cleans up the runtime (including all models),</li>
    *    <li>closes the Spring Application Context, and</li>
    *    <li>stops the DBMS.</li>
    * </ul>
    * </p>
    *
    * <p>
    * The teardown will not be done, if the class is locked.
    * </p>
    *
    * @throws TestRtEnvException if an exception occurs during test environment teardown
    */
   @Override
   protected void after() throws TestRtEnvException
   {
      if (locked)
      {
         return;
      }

      LOG.info("---> Tearing down the test environment ...");

      sf.close();
      sf = null;

      springAppCtx.close();
      /* no need to drop the schema as the database content */
      /* is gone anyway as soon as the DBMS is stopped      */
      dbms.stop();

      LOG.info("<--- ... teardown of test environment done.");
   }

   /**
    * <p>
    * Locks the test environment, i.e. the test environment can neither be
    * set up nor teared down.
    * </p>
    */
   public static void lockTestEnv()
   {
      locked = true;
   }

   /**
    * <p>
    * Unlocks the test environment, i.e. the test environment can either be
    * set up or teared down.
    * </p>
    */
   public static void unlockTestEnv()
   {
      locked = false;
   }

   /**
    * <p>
    * Allows for retrieving the data source of the database backing this test setup
    * in order to directly execute SQL statements.
    * </p>
    *
    * @throws IllegalStateException if the data source cannot be obtained from the <i>Spring</i> Application Context
    *
    * @return the data source of the database backing this test setup
    */
   public DataSource dataSource()
   {
      if (ds == null)
      {
         ds = springAppCtx.appCtx().getBean(DATA_SOURCE_FACTORY_BEAN_ID, DataSource.class);

         if (ds == null)
         {
            throw new IllegalStateException("Data Source cannot be obtained from Spring Application Context.");
         }
      }

      return ds;
   }

   /**
    * <p>
    * Allows for retrieving the queue connection factory of this test setup, if any.
    * </p>
    *
    * @throws IllegalStateException if the queue connection factory cannot be obtained from the Spring Application Context
    *    or JMS is not in use in this test setup
    *
    * @return the queue connection factory of this test setup
    */
   public QueueConnectionFactory queueConnectionFactory()
   {
      if (jmsResourceProvider == null)
      {
         jmsResourceProvider = initJmsResourceProvider();
      }

      final QueueConnectionFactory queueCf = jmsResourceProvider.resolveQueueConnectionFactory(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);
      if (queueCf == null)
      {
         throw new IllegalStateException("Queue connection factory cannot be obtained.");
      }
      return queueCf;
   }

   /**
    * <p>
    * Allows for retrieving a queue with the given name, if any.
    * </p>
    *
    * @throws IllegalStateException if the queue cannot be obtained from the Spring Application Context
    *    or JMS is not in use in this test setup
    *
    * @param name the name of the queue to be returned
    *
    * @return the requested queue
    */
   public Queue queue(final String name)
   {
      if (jmsResourceProvider == null)
      {
         jmsResourceProvider = initJmsResourceProvider();
      }

      final Queue queue = jmsResourceProvider.resolveQueue(name);
      if (queue == null)
      {
         throw new IllegalStateException("Queue cannot be obtained.");
      }
      return queue;
   }

   /**
    * <p>
    * Allows for retrieving the {@link ForkingServiceMode} chosen for this test setup.
    * </p>
    *
    * @return the {@link ForkingServiceMode} chosen for this test setup
    */
   public ForkingServiceMode forkingServiceMode()
   {
      return forkingServiceMode;
   }

   /* package-private */ void setTestClass(final Class<?> testClass)
   {
      this.testClass = testClass;
   }

   /* package-private */ ApplicationContext appCtx()
   {
      return springAppCtx.appCtx();
   }

   private IJmsResourceProvider initJmsResourceProvider()
   {
      if (forkingServiceMode != ForkingServiceMode.JMS)
      {
         throw new IllegalStateException("JMS is not in use in this test setup.");
      }

      final IJmsResourceProvider result = springAppCtx.appCtx().getBean(JMS_RESOURCE_PROVIDER_BEAN_ID, IJmsResourceProvider.class);

      if (result == null)
      {
         throw new IllegalStateException("JMS Resource Provider cannot be obtained from Spring Application Context.");
      }

      return result;
   }

   /**
    * <p>
    * Represents the means the engine uses to implement forking
    * <ul>
    *    <li><i>JMS</i> &ndash; use <i>JMS</i> for forking new processes</li>
    *    <li>Native Threading &ndash; use native threading for forking new processes</li>
    * </ul>
    * </p>
    *
    * <p>
    * Native threading does increase the test performance much, but does not allow for <i>JMS</i>
    * support, of course.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static enum ForkingServiceMode
   {
      /**
       * use <i>JMS</i> for forking new processes
       */
      JMS,

      /**
       * use native threading for forking new processes
       */
      NATIVE_THREADING
   }
}
