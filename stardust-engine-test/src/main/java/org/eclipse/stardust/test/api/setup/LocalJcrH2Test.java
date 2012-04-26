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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class LocalJcrH2Test extends ExternalResource
{
   private static final Log LOGGER = LogFactory.getLog(LocalJcrH2Test.class);
   
   private static final H2Server DBMS = new H2Server();
   private static final SpringAppContext SPRING_APP_CTX = new SpringAppContext();

   private static boolean locked;

   /**
    * <p>
    * Sets up the local Spring test environment with an H2 DB and JCR support, i.e.
    * <ul>
    *    <li>starts the DBMS,</li>
    *    <li>creates the Audit Trail DB schema, and</li>
    *    <li>bootstraps the Spring Application Context.</li>
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
      
      LOGGER.info("---> Setting up the test environment ...");

      DBMS.start();
      DBMS.createSchema();
      SPRING_APP_CTX.bootstrap();
      
      LOGGER.info("<--- ... setup of test environment done.");
   }
   
   /**
    * <p>
    * Tears down the local Spring test environment with an H2 DB and JCR support, i.e.
    * <ul>
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
      
      LOGGER.info("---> Tearing down the test environment ...");

      SPRING_APP_CTX.close();
      DBMS.stop();
      
      LOGGER.info("<--- ... teardown of test environment done.");
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
}
