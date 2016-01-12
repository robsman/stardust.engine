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
package org.eclipse.stardust.test.camel.core.endpoint.process;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

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
public class ProcessContinueTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String[] MODEL_IDS = { "GetPropertiesInvokerModel", "PropertiesProducerModel", "ExternalXSDModel", "ProcessContinueModel", "CamelTestModel" };

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test
   public void testContinueProcessManualActivity() throws Exception
   {
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}TestContinueProcessManualActivity", null, true);
      // start process which will be completed by the CMD
      ProcessInstance pInstanceToBeCompleted = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}ManualActivityInProcessToBeContinued", null, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "DoSomething");
      ActivityInstanceStateBarrier.instance().awaitForId(pInstanceToBeCompleted.getOID(), "WaitActivity");

      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);

      ActivityInstance manualActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstanceToBeCompleted.getProcessID(), "WaitActivity")).get(0);

      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, manualActivity.getState());

      // run activity:complete command
      // ippProcessInstanceOid
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("ippProcessInstanceOid", String.valueOf(pInstanceToBeCompleted.getOID()));
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, data);

      ActivityInstanceStateBarrier.instance().await(manualActivity.getOID(), ActivityInstanceState.Completed);
   }

   @Test
   public void testContinueProcessUIMashup() throws Exception
   {
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}TestContinueProcessUIMashup", null, true);
     // start process which will be completed by the CMD
      ProcessInstance pInstanceToBeCompleted = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}UIMasupActivityInProcessToBeContinued", null, true);

      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "DoSomething");
      ActivityInstanceStateBarrier.instance().awaitForId(pInstanceToBeCompleted.getOID(), "UIMashup");

      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);

      ActivityInstance uIMashupActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstanceToBeCompleted.getProcessID(), "UIMashup")).get(0);

      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, uIMashupActivity.getState());

      // run activity:complete command
      // ippProcessInstanceOid
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("ippProcessInstanceOid", String.valueOf(pInstanceToBeCompleted.getOID()));
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, dataMap);

      ActivityInstanceStateBarrier.instance().await(uIMashupActivity.getOID(), ActivityInstanceState.Completed);

      // check data value
      String data = (String) sf.getWorkflowService().getInDataPath(pInstanceToBeCompleted.getOID(), "data");
      assertEquals("some value", data);

      // complete process
      ActivityInstance nextAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pInstanceToBeCompleted.getOID());
      sf.getWorkflowService().complete(nextAi.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }
}
