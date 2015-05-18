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

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
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

public class EscalationEventTest
{
   /* package-private */static final String ESCALATION_EVENTS_MODEL_NAME = "EscalationEventTest";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String ROOT_PROCESS = "{EscalationEventTest}EscalationRoot";

   private static final String INTERRUPTING_ROOT_PROCESS = "{EscalationEventTest}InterruptingEscalationRoot";

   private static final String THROWING_SUBPROCESS = "{EscalationEventTest}EscalatingSubprocess";

   private static final String MID_LEVEL_SUBPROCESS = "{EscalationEventTest}IntermediateLevel";

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR,
         ForkingServiceMode.JMS, ESCALATION_EVENTS_MODEL_NAME);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Before
   public void init()
   {}

   @Test
   public void testNonInterruptingEscalationFlowByEndEvent() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess(ROOT_PROCESS, singletonMap("throwIntermediate", Boolean.FALSE),
            true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // escalating sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelCaller");
      ProcessInstance escalatingProcessOne = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findAlive(THROWING_SUBPROCESS));

      // Throwing end event
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "EscalationEndEvent");

      // parallel escalation path on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelEscalationState");

      // continue normal flow (which is hibernated in order to keep the sub-process alive)
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "NormalFlow");
      ActivityInstanceQuery aiq = ActivityInstanceQuery.findForProcessInstance(rootProcess.getOID());
      aiq.where(ActivityFilter.forProcess("NormalFlow", THROWING_SUBPROCESS));
      ActivityInstance normalFlowInSubprocess = sf.getQueryService().findFirstActivityInstance(aiq);
      wfs.activate(normalFlowInSubprocess.getOID());
      wfs.complete(normalFlowInSubprocess.getOID(), null, null);

      // escalating process completes regularly
      piStateChangeBarrier.await(escalatingProcessOne.getOID(), ProcessInstanceState.Completed);

      // second escalating sub-process (two hierarchy levels) was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "TwoLevelCaller");
      ProcessInstance escalatingProcessTwo = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findAlive(THROWING_SUBPROCESS));

      // Throwing end event
      aiStateChangeBarrier.awaitForId(escalatingProcessTwo.getOID(), "EscalationEndEvent");

      // parallel escalation path on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "TwoLevelEscalationState");

      // continue normal flow (which is hibernated in order to keep the sub-process alive)
      aiStateChangeBarrier.awaitForId(escalatingProcessTwo.getOID(), "NormalFlow");
      aiq = ActivityInstanceQuery.findForProcessInstance(escalatingProcessTwo.getOID());
      aiq.where(ActivityFilter.forProcess("NormalFlow", THROWING_SUBPROCESS));
      normalFlowInSubprocess = sf.getQueryService().findFirstActivityInstance(aiq);
      wfs.activate(normalFlowInSubprocess.getOID());
      wfs.complete(normalFlowInSubprocess.getOID(), null, null);

      // normal flow end on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "EndEvent");

      // escalating process completes regularly
      piStateChangeBarrier.await(escalatingProcessTwo.getOID(), ProcessInstanceState.Completed);

      ProcessInstance midLevelProcess = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findForProcess(MID_LEVEL_SUBPROCESS));
      piStateChangeBarrier.await(midLevelProcess.getOID(), ProcessInstanceState.Completed);

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testNonInterruptingEscalationFlowByIntermediateThrowEvent() throws InterruptedException,
         TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess(ROOT_PROCESS, singletonMap("throwIntermediate", Boolean.TRUE),
            true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // escalating sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelCaller");
      ProcessInstance escalatingProcessOne = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findForProcess(THROWING_SUBPROCESS));

      // Throwing intermediate event
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "IntermediateThrowEvent");

      // Continues after throwing...
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "EndAfterIntermediateThrow");

      // parallel escalation path on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelEscalationState");

      // continue normal flow (which is hibernated in order to keep the sub-process alive)
      ActivityInstanceQuery aiq = ActivityInstanceQuery.findForProcessInstance(rootProcess.getOID());
      aiq.where(ActivityFilter.forProcess("NormalFlow", THROWING_SUBPROCESS));
      ActivityInstance normalFlowInSubprocess = sf.getQueryService().findFirstActivityInstance(aiq);
      wfs.activate(normalFlowInSubprocess.getOID());
      wfs.complete(normalFlowInSubprocess.getOID(), null, null);

      // escalating process completes regularly
      piStateChangeBarrier.await(escalatingProcessOne.getOID(), ProcessInstanceState.Completed);

      // second escalating sub-process (two hierarchy levels) was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "TwoLevelCaller");
      ProcessInstance escalatingProcessTwo = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findAlive(THROWING_SUBPROCESS));

      // Throwing intermediate event
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "IntermediateThrowEvent");

      // Continues after throwing...
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "EndAfterIntermediateThrow");

      // parallel escalation path on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "TwoLevelEscalationState");

      // continue normal flow (which is hibernated in order to keep the sub-process alive)
      aiStateChangeBarrier.awaitForId(escalatingProcessTwo.getOID(), "NormalFlow");
      aiq = ActivityInstanceQuery.findForProcessInstance(escalatingProcessTwo.getOID());
      aiq.where(ActivityFilter.forProcess("NormalFlow", THROWING_SUBPROCESS));
      normalFlowInSubprocess = sf.getQueryService().findFirstActivityInstance(aiq);
      wfs.activate(normalFlowInSubprocess.getOID());
      wfs.complete(normalFlowInSubprocess.getOID(), null, null);

      // normal flow end on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "EndEvent");

      // escalating process completes regularly
      piStateChangeBarrier.await(escalatingProcessTwo.getOID(), ProcessInstanceState.Completed);

      ProcessInstance midLevelProcess = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findForProcess(MID_LEVEL_SUBPROCESS));
      piStateChangeBarrier.await(midLevelProcess.getOID(), ProcessInstanceState.Completed);

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);

   }

   @Test
   public void testInterruptingEscalationFlowByEndEvent() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess(INTERRUPTING_ROOT_PROCESS,
            singletonMap("throwIntermediate", Boolean.FALSE), true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // escalating sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelCaller");
      ProcessInstance escalatingProcessOne = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findForProcess(THROWING_SUBPROCESS));

      // Throwing end event
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "EscalationEndEvent");

      // escalation path on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelEscalationState");

      // escalating process terminated
      piStateChangeBarrier.await(escalatingProcessOne.getOID(), ProcessInstanceState.Aborted);

      // no subsequent activities/subprocess after the aborted one
      assertEquals(1, sf.getQueryService().getProcessInstancesCount(
            ProcessInstanceQuery.findForProcess(THROWING_SUBPROCESS)));
      assertEquals(0, sf.getQueryService().getProcessInstancesCount(
            ProcessInstanceQuery.findForProcess(MID_LEVEL_SUBPROCESS)));

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);

   }

   @Test
   public void testInterruptingEscalationFlowByIntermediateEvent() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance rootProcess = wfs.startProcess(INTERRUPTING_ROOT_PROCESS,
            singletonMap("throwIntermediate", Boolean.TRUE), true);

      ActivityInstanceStateBarrier aiStateChangeBarrier = ActivityInstanceStateBarrier.instance();
      ProcessInstanceStateBarrier piStateChangeBarrier = ProcessInstanceStateBarrier.instance();

      // escalating sub-process was started
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelCaller");
      ProcessInstance escalatingProcessOne = sf.getQueryService().findFirstProcessInstance(
            ProcessInstanceQuery.findForProcess(THROWING_SUBPROCESS));

      // Throwing end event
      aiStateChangeBarrier.awaitForId(escalatingProcessOne.getOID(), "IntermediateThrowEvent");

      // escalation path on root level
      aiStateChangeBarrier.awaitForId(rootProcess.getOID(), "OneLevelEscalationState");

      // escalating process terminated
      piStateChangeBarrier.await(escalatingProcessOne.getOID(), ProcessInstanceState.Aborted);

      // no subsequent activities/subprocess after the aborted one
      assertEquals(1, sf.getQueryService().getProcessInstancesCount(
            ProcessInstanceQuery.findForProcess(THROWING_SUBPROCESS)));
      assertEquals(0, sf.getQueryService().getProcessInstancesCount(
            ProcessInstanceQuery.findForProcess(MID_LEVEL_SUBPROCESS)));

      // await root process completion
      piStateChangeBarrier.await(rootProcess.getOID(), ProcessInstanceState.Completed);

   }

}
