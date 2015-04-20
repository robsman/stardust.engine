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
package org.eclipse.stardust.test.camel.core.endpoint.activity;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class ActivityEndpointCompletionTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_ID = "CompleteActivityModel";

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test
   public void testManualActivityCompletionWithoutData() throws Exception
   {
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CompleteActivityModel}TestManualActivityCompletionWithoutData", null, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "DoSomething");
      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "ManualActivity");

      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);

      ActivityInstance manualActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "ManualActivity")).get(0);

      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, manualActivity.getState());

      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().await(manualActivity.getOID(), ActivityInstanceState.Completed);

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testManualActivityCompletionWithData() throws Exception
   {
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CompleteActivityModel}TestManualActivityCompletionWithData", null, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "DoSomething");
      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "ManualActivity");

      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);

      ActivityInstance manualActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "ManualActivity")).get(0);

      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, manualActivity.getState());

      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().await(manualActivity.getOID(), ActivityInstanceState.Completed);

      // check data value
      String data = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "data");
      assertEquals("some value", data);

      // complete process
      ActivityInstance nextAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pInstance.getOID());
      sf.getWorkflowService().complete(nextAi.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testRootActivityCompletion() throws Exception
   {
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{CompleteActivityModel}TestRootActivityCompletion", null, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "DoSomething");
      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "RootActivity");

      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(pInstance.getOID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);

      ActivityInstance rootActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "RootActivity")).get(0);

      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Hibernated, rootActivity.getState());

      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().await(rootActivity.getOID(), ActivityInstanceState.Completed);

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testUIMashupCompletion() throws Exception
   {
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CompleteActivityModel}TestUIMashupCompletion", null, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "DoSomething");
      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "UIMashupActivity");

      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);

      ActivityInstance uIMashupActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "UIMashupActivity")).get(0);

      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, uIMashupActivity.getState());

      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().await(uIMashupActivity.getOID(), ActivityInstanceState.Completed);

      // check data value
      String data = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "data");
      assertEquals("some value", data);

      // complete process
      ActivityInstance nextAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pInstance.getOID());
      sf.getWorkflowService().complete(nextAi.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }
}
