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
package org.eclipse.stardust.test.events;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class ErrorEventHierarchyTest
{
   /* package-private */static final String ERROR_EVENTS_HIERARCHY_MODEL_NAME = "ErrorEventHierarchyTest";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS,
         ERROR_EVENTS_HIERARCHY_MODEL_NAME);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Before
   public void init()
   {}

   @Test
   public void testThrownErrorEventIsReceivedByCallingActivity() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess("{ErrorEventHierarchyTest}HierarchyRootProcess",
            singletonMap("ProcessKey", Long.toString(System.nanoTime())), true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // failing sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "FailingProcess");

      ProcessInstance intermediateProcessLevel = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findForProcess("IntermediateProcessLevel"));

      ProcessInstance failingProcess = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findForProcess("HierarchyFailingProcess"));

      aiStateChangeBarrier.awaitForId(failingProcess.getOID(), "AwaitAbort");
      
      // error event was thrown
      aiStateChangeBarrier.awaitForId(failingProcess.getOID(), "ThrowError");

      // await error was received
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "ReceivedError");

      // trigger / await PI completion
      piStateChangeBarrier.await(failingProcess.getOID(), ProcessInstanceState.Aborted);

      piStateChangeBarrier.await(intermediateProcessLevel.getOID(), ProcessInstanceState.Aborted);      
      
      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
      
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findForProcessInstance(rootProcess.getOID());
      activityInstanceQuery.where(ActivityFilter.forProcess("NormalFlow", "HierarchyRootProcess"));
      assertEquals(0, sf.getQueryService().getActivityInstancesCount(activityInstanceQuery));
      
      activityInstanceQuery = ActivityInstanceQuery.findForProcessInstance(rootProcess.getOID());
      activityInstanceQuery.where(ActivityFilter.forProcess("ReceivedError", "HierarchyRootProcess"));
      assertEquals(1, sf.getQueryService().getActivityInstancesCount(activityInstanceQuery));
   }

}
