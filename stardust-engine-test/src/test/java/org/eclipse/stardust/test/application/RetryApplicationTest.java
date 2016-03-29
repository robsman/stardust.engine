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
package org.eclipse.stardust.test.application;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests retry application attributes.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class RetryApplicationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "ApplicationRetry");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf);

   /**
    * Tests retry attributes are set for application.
    * 
    */
   @Test
   public void testRetryApplication()
   {      
      org.eclipse.stardust.test.workflow.application.RetryApplication.setCounter(0);
      adminSf.getWorkflowService().startProcess("ProcessDefinition_1", null, true);
      int counter = org.eclipse.stardust.test.workflow.application.RetryApplication.getCounter();
      
      assertThat(1, equalTo(counter));
   }
   
   /**
    * Tests retry attributes are set for engine.
    * 
    */
   @Test
   public void testRetryEngine()
   {
      org.eclipse.stardust.test.workflow.application.RetryApplication.setCounter(0);      
      adminSf.getWorkflowService().startProcess("ProcessDefinition_2", null, true);
      int counter = org.eclipse.stardust.test.workflow.application.RetryApplication.getCounter();
      
      assertThat(5, equalTo(counter));
   }  
}