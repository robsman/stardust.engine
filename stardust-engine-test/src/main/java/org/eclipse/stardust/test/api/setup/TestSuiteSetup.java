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

import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * <p>
 * A JUnit test suite needs to
 * <ul>
 *   <li>declared a field of this class and</li>
 *   <li>annotate this field with {@linkplain org.junit.ClassRule}</li>
 * </ul>
 * in order to be able to execute tests in a local Spring environment, using an H2 DB and having
 * JCR support. By doing so the needed setup and teardown will be done automatically.
 * </p>
 *
 * <p>
 * This test suite leverages the fact that <code>LocalJcrH2Test</code> is 'lockable'
 * in order to chain some test classes without having the effort of test environment
 * setup and teardown after every single test class.
 * </p>
 *
 * <p>
 * This class is responsible for the test suite setup whereas {@link TestClassSetup}
 * deals with test class setup and {@link TestMethodSetup} deals with test method setup.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TestSuiteSetup extends ExternalResource
{
   private final TestClassSetup testClassSetup;

   /**
    * <p>
    * Initializes the object with the given username password pair and the models to deploy. Furthermore, it specifies which forking service
    * mode to use.
    * </p>
    *
    * @param userPwdPair the credentials of the user to use for runtime setup; must not be null
    * @param forkingServiceMode the forking service's mode (JMS or non-JMS)
    * @param modelNames the names of the models to deploy; may be null or empty
    */
   public TestSuiteSetup(final UsernamePasswordPair userPwdPair, final ForkingServiceMode forkingServiceMode, final String ... modelNames)
   {
      testClassSetup = new TestClassSetup(userPwdPair, forkingServiceMode, modelNames);
   }

   /* (non-Javadoc)
    * @see org.junit.rules.ExternalResource#apply(org.junit.runners.model.Statement, org.junit.runner.Description)
    */
   @Override
   public Statement apply(final Statement base, final Description description)
   {
      testClassSetup.setTestClass(description.getTestClass());

      return super.apply(base, description);
   }

   /**
    * <p>
    * Sets up the test environment (see {@link TestClassSetup#before()}) and locks
    * the test environment afterwards to prevent the individual test classes from setting
    * up the test environment themselves.
    * </p>
    *
    * @throws TestRtEnvException if an exception occurs during test environment setup
    */
   @Override
   protected void before() throws TestRtEnvException
   {
      testClassSetup.before();
      TestClassSetup.lockTestEnv();
   }

   /**
    * <p>
    * Tears down the test environment (see {@link TestClassSetup#after()}). It will
    * remove the test environment lock first which prevented the individual test classes from
    * tearing down the test environment themselves.
    * </p>
    *
    * @throws TestRtEnvException if an exceptions occurs during test environment teardown
    */
   @Override
   protected void after() throws TestRtEnvException
   {
      TestClassSetup.unlockTestEnv();
      testClassSetup.after();
   }
}
