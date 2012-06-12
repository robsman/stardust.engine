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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.impl.H2Server;
import org.eclipse.stardust.test.impl.SpringAppContext;
import org.junit.rules.ExternalResource;

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
 * This class is responsible for the test class setup whereas {@link LocalJcrH2TestSuiteSetup}
 * deals with test suite setup and {@link TestMethodSetup} deals with test method setup.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class LocalJcrH2TestSetup extends ExternalResource
{
   private static final Log LOG = LogFactory.getLog(LocalJcrH2TestSetup.class);
   
   private static final H2Server DBMS = new H2Server();
   private static final SpringAppContext SPRING_APP_CTX = new SpringAppContext();

   private static boolean locked;

   private final String[] modelNames;
   private final UsernamePasswordPair userPwdPair;
   private final ForkingServiceMode forkingServiceMode;
   
   private ServiceFactory sf;
   
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
   public LocalJcrH2TestSetup(final UsernamePasswordPair userPwdPair, final ForkingServiceMode forkingServiceMode, final String ... modelNames)
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
      this.modelNames = (modelNames != null) ? modelNames : new String[0];
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

      DBMS.start();
      DBMS.createSchema();
      SPRING_APP_CTX.bootstrap(forkingServiceMode);
      
      sf = ServiceFactoryLocator.get(userPwdPair.username(), userPwdPair.password());
      if (modelNames.length > 0)
      {
         LOG.debug("Trying to deploy model(s) '" + Arrays.asList(modelNames) + "'.");
         RtEnvHome.deploy(sf.getAdministrationService(), modelNames);
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

      RtEnvHome.cleanUpRuntimeAndModels(sf.getAdministrationService());
      sf.close();
      sf = null;
      
      SPRING_APP_CTX.close();
      DBMS.stop();
      
      LOG.info("<--- ... teardown of test environment done.");
   }
   
   /**
    * Locks the test environment, i.e. the test environment can neither be
    * set up nor teared down.
    */
   public static void lockTestEnv()
   {
      locked = true;
   }
   
   /**
    * Unlocks the test environment, i.e. the test environment can either be
    * set up or teared down.
    */
   public static void unlockTestEnv()
   {
      locked = false;
   }
   
   /**
    * <p>
    * Represents the means the engine uses to implement forking
    * <ul>
    *    <li><i>JMS</i> &ndash; use <i>JMS</i> for forking new processes</li>
    *    <li><i>Native Threading</i> &ndash; use native threading for forking new processes</li>
    * </ul>
    * </p>
    * 
    * <p>
    * <i>Native Threading</i> does increase the test performance much, but does not allow for <i>JMS</i>
    * support, of course. 
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static enum ForkingServiceMode { JMS, NATIVE_THREADING }
}
