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
package org.eclipse.stardust.test.workflow;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DaemonHome;
import org.eclipse.stardust.test.api.util.DaemonHome.DaemonType;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This test class contains functional tests for handling transient users that are used if
 * process is not started by a user but e.g. by a timer.
 * </p>
 * 
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class TransientUsersWorkflowTest
{
   private static final String MODEL_NAME = "TransientUserWorkflowModel";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private AdministrationService administrationService;

   @Before
   public void setup()
   {
      administrationService = serviceFactory.getAdministrationService();
   }

   /*
    * Tests if process can be completed if it was started by a timer and a note was added
    * by a transient user executing a plain java application.
    * 
    * <p> See also <a
    * href="https://www.csa.sungard.com/jira/browse/CRNT-37305">CRNT-37305</a>. </p>
    */
   @Test
   public void testHandleTransientUsersPINotes() throws InterruptedException
   {
      DaemonHome.startDaemon(administrationService, DaemonType.TIMER_TRIGGER_DAEMON);
      Thread.sleep(10000);
      DaemonHome.stopDaemon(administrationService, DaemonType.TIMER_TRIGGER_DAEMON);
      QueryService qs = serviceFactory.getQueryService();
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      ProcessInstance processInstance = qs.findFirstProcessInstance(query);
      ProcessInstanceState state = processInstance.getState();
      assertEquals(ProcessInstanceState.Completed, state);
   }

}