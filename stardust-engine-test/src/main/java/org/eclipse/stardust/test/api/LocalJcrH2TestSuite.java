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
package org.eclipse.stardust.test.api;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * <p>
 * This class is to be subclassed by a test suite that wants to execute tests in
 * a local Spring environment, using an H2 DB and needing JCR support. By doing so
 * the needed setup and teardown will be done automatically.
 * </p>
 * 
 * <p>
 * This test suite leverages the fact that <code>LocalJcrH2Test</code> is 'lockable'
 * in order to chain some test classes without having the effort of test environment
 * setup and teardown after every single test class.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class LocalJcrH2TestSuite
{
   /**
    * <p>
    * Sets up the test environment (see {@link LocalJcrH2Test#setUpTestEnv()}) and locks
    * the test environment afterwards to prevent the individual test classes from setting
    * up the test environment themselves. 
    * </p>
    * 
    * @throws TestRtEnvException if an exception occurs during test environment setup
    */
   @BeforeClass
   public static void setUpTestEnv() throws TestRtEnvException
   {
      LocalJcrH2Test.setUpTestEnv();
      LocalJcrH2Test.lockTestEnv();
   }
   
   /**
    * <p>
    * Tears down the test environment (see {@link LocalJcrH2Test#tearDownTestEnv()}). It will
    * remove the test environment lock first which prevented the individual test classes from
    * tearing down the test environment themselves. 
    * </p>
    * 
    * @throws TestRtEnvException if an exceptions occurs during test environment teardown
    */
   @AfterClass
   public static void tearDownTestEnv() throws TestRtEnvException
   {
      LocalJcrH2Test.unlockTestEnv();
      LocalJcrH2Test.tearDownTestEnv();
   }
}
