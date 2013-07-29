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
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * <p>
 * This class deals with test method setup, i.e. it cleans up the runtime and deletes all created users
 * after the test case execution. Plus, it logs the test method's name before entering and after leaving
 * the test method.
 * </p>
 * 
 * <p>
 * This class is responsible for the test method setup whereas {@link LocalJcrH2TestSetup}
 * deals with test class setup and {@link LocalJcrH2TestSuiteSetup} deals with test suite setup.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TestMethodSetup extends ExternalResource
{
   private static final Log LOG = LogFactory.getLog(TestMethodSetup.class);
   
   private static final String LOG_EYE_CATCHER = "################## Test Method Boundary ##################";
   
   private final UsernamePasswordPair userPwdPair;
   
   private ServiceFactory sf;
   
   private String testMethodName;
   
   /**
    * <p>
    * Sets up a runtime configurer with the username password pair to use for test method setup.
    * </p>
    * 
    * @param userPwdPair the credentials of the user used for test method setup; must not be null
    */
   public TestMethodSetup(final UsernamePasswordPair userPwdPair)
   {
      if (userPwdPair == null)
      {
         throw new NullPointerException("User password pair must not be null.");
      }
      
      this.userPwdPair = userPwdPair;
   }
   
   /**
    * @return the service factory this object has been initialized with
    */
   protected ServiceFactory serviceFactory()
   {
      return sf;
   }
   
   /**
    * @return the name of the test method currently executed
    */
   public String testMethodName()
   {
      return testMethodName;
   }
   
   /* (non-Javadoc)
    * @see org.junit.rules.ExternalResource#apply(org.junit.runners.model.Statement, org.junit.runner.Description)
    */
   public Statement apply(final Statement base, final Description description) {
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
      LOG.info(LOG_EYE_CATCHER);
      LOG.info("--> " + testMethodName);
      
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
      RtEnvHome.cleanUpRuntime(sf.getAdministrationService());
      
      sf.close();
      sf = null;
      
      LOG.info("<-- " + testMethodName);
      LOG.info(LOG_EYE_CATCHER);
   }
}
