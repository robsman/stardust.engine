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
package org.eclipse.stardust.test.spawn;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions.SpawnMode;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains tests for API calls on a Halted process hierarchy.
 * </p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class HaltedStateApiOperationsTest
{
   public static final String MODEL_NAME = "SpawnProcessModel";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Before
   public void before()
   {
      ProcessInstanceStateBarrier.instance().cleanUp();
   }

   @Test(expected = IllegalOperationException.class)
   public void testActivate() throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         wfs.activate(ai.getOID());
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08001", e.getError().getId());
         throw e;
      }
   }

   @Test
   public void testActivateNext() throws TimeoutException, InterruptedException
   {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         Assert.assertNull(wfs.activateNextActivityInstance(ai.getOID()));
   }

   @Test(expected = IllegalOperationException.class)
   public void testComplete() throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         wfs.complete(ai.getOID(), null, null);
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08001", e.getError().getId());
         throw e;
      }
   }

   @Test
   public void testDelegate() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      wfs.delegateToDefaultPerformer(ai.getOID());
   }

   @Test(expected = IllegalOperationException.class)
   public void testHibernate() throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         wfs.hibernate(ai.getOID());
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08001", e.getError().getId());
         throw e;
      }
   }

   @Test(expected = IllegalOperationException.class)
   public void testSuspend() throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         wfs.suspend(ai.getOID(), null);
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08001", e.getError().getId());
         throw e;
      }
   }

   @Test(expected = IllegalOperationException.class)
   public void testSuspendToDefaultPerformer()
         throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         wfs.suspendToDefaultPerformer(ai.getOID());
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08001", e.getError().getId());
         throw e;
      }
   }

   @Test(expected=ObjectNotFoundException.class)
   public void testSetOutDataPath() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      wfs.setOutDataPath(ai.getProcessInstanceOID(), "bla", "test");
   }

   @Test(expected=ObjectNotFoundException.class)
   public void testBindProcessEventHandler() throws TimeoutException, InterruptedException
   {

      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      wfs.bindProcessEventHandler(ai.getProcessInstanceOID(), "test");

   }

   @Test(expected=ObjectNotFoundException.class)
   public void testBindActivityEventHandler()
         throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      wfs.bindActivityEventHandler(ai.getOID(), "test");
   }

   @Test
   public void testSetProcessInstanceAttributes()
         throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      ProcessInstance pi = wfs.getProcessInstance(ai.getProcessInstanceOID());
      ProcessInstanceAttributes attributes = pi.getAttributes();
      attributes.addNote("test");

      wfs.setProcessInstanceAttributes(attributes);
   }

   @Test
   public void testSetActivityInstanceAttributes()
         throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      wfs.setActivityInstanceAttributes(new ActivityInstanceAttributesImpl(ai.getOID()));
   }

   @Test(expected = IllegalOperationException.class)
   public void testCreateCase() throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         wfs.createCase("test", "test", new long[] {ai.getProcessInstanceOID()});
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08002", e.getError().getId());
         throw e;
      }
   }

   @Test(expected = IllegalOperationException.class)
   public void testSpawnSubprocess() throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         wfs.spawnSubprocessInstance(ai.getProcessInstanceOID(),
               "{SpawnProcessModel}InputData1", true, null);
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08002", e.getError().getId());
         throw e;
      }
   }

   @Test(expected = IllegalOperationException.class)
   public void testJoinProcess() throws TimeoutException, InterruptedException
   {
      try
      {
         WorkflowService wfs = sf.getWorkflowService();

         ActivityInstance ai = getHaltedAi();

         ProcessInstance newProcess = wfs.startProcess("{SpawnProcessModel}InputData1",
               null, true);

         wfs.joinProcessInstance(ai.getProcessInstanceOID(), newProcess.getOID(), "test");
      }
      catch (IllegalOperationException e)
      {
         Assert.assertEquals("BPMRT08002", e.getError().getId());
         throw e;
      }
   }

   @Test
   public void testAbortHaltedAIRootHierarchyAllowed() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      wfs.abortActivityInstance(ai.getOID(), AbortScope.RootHierarchy);

      ProcessInstanceStateBarrier.instance().await(ai.getProcessInstanceOID(), ProcessInstanceState.Aborted);
      assertActivityInstanceExists(ai.getProcessInstanceOID(), "InputData1", ActivityInstanceState.ABORTED);
   }

   @Test
   public void testAbortHaltedAISubHierarchyAllowed() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getHaltedAi();

      wfs.abortActivityInstance(ai.getOID(), AbortScope.SubHierarchy);

      ProcessInstanceStateBarrier.instance().await(ai.getProcessInstanceOID(), ProcessInstanceState.Completed);
      assertActivityInstanceExists(ai.getProcessInstanceOID(), "InputData1", ActivityInstanceState.ABORTED);
   }

   @Test
   public void testCompleteManualInApplicationStateAllowed()
         throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      Pair<ActivityInstance, ActivityInstance> ais = getApplicationStateAiInHaltedHiararchy();
      ActivityInstance ai = ais.getFirst();
      Assert.assertEquals(ActivityInstanceState.Application, ai.getState());

      wfs.complete(ai.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().await(ai.getOID(),
            ActivityInstanceState.Completed);

      ActivityInstance spawnedAi = ais.getSecond();
      Assert.assertEquals(ActivityInstanceState.Suspended, spawnedAi.getState());
      wfs.activateAndComplete(spawnedAi.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().await(spawnedAi.getOID(),
            ActivityInstanceState.Completed);

      // inserted process completed resumes halted process
      ProcessInstanceStateBarrier.instance().await(spawnedAi.getProcessInstanceOID(), ProcessInstanceState.Completed);
      // main process should also be completed because last AI is already completed.
      ProcessInstanceStateBarrier.instance().await(ai.getProcessInstanceOID(), ProcessInstanceState.Completed);


   }

   @Test
   public void testSuspendManualInApplicationStateAllowed()
         throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstance ai = getApplicationStateAiInHaltedHiararchy().getFirst();
      Assert.assertEquals(ActivityInstanceState.Application, ai.getState());

       wfs.suspend(ai.getOID(), null);

       ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Halted);
       assertActivityInstanceExists(ai.getProcessInstanceOID(), "InputData1", ActivityInstanceState.HALTED);
       ProcessInstanceStateBarrier.instance().await(ai.getProcessInstanceOID(), ProcessInstanceState.Halted);
   }

   // ************** UTILS ***************

   private ActivityInstance getHaltedAi()
         throws IllegalStateException, TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1",
            new StartOptions(null, true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(pi.getOID(),
            "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(),
            ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(),
            PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "InputData1",
            ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1",
            ActivityInstanceState.SUSPENDED);

      ActivityInstanceQuery findAlive = ActivityInstanceQuery
            .findAlive("{SpawnProcessModel}InputData1");
      findAlive.where(new ActivityStateFilter(ActivityInstanceState.Halted));
      ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(findAlive);
      return ai;
   }

   private Pair<ActivityInstance, ActivityInstance> getApplicationStateAiInHaltedHiararchy()
         throws IllegalStateException, TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1",
            new StartOptions(null, true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(pi.getOID(),
            "{SpawnProcessModel}InputData1", options);

//      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
//            ProcessInstanceState.Halting);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(),
            ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(),
            PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "InputData1",
            ActivityInstanceState.APPLICATION);
      assertActivityInstanceExists(peer.getOID(), "InputData1",
            ActivityInstanceState.SUSPENDED);

      ActivityInstanceQuery findAlive = ActivityInstanceQuery
            .findAlive("{SpawnProcessModel}InputData1");
      findAlive.where(new ActivityStateFilter(ActivityInstanceState.Suspended));
      ActivityInstance spawnedAi = sf.getQueryService().findFirstActivityInstance(findAlive);

      return new Pair<ActivityInstance, ActivityInstance>(ai, spawnedAi);
   }

   private void assertProcessInstanceLinkExists(long piOid, long sourcePiOid,
         PredefinedProcessInstanceLinkTypes linkType)
   {
      QueryService qs = sf.getQueryService();

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.where(ProcessInstanceQuery.OID.isEqual(piOid));

      ProcessInstanceDetailsPolicy detailsPolicy = new ProcessInstanceDetailsPolicy(
            ProcessInstanceDetailsLevel.Default);
      detailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);

      query.setPolicy(detailsPolicy);

      ProcessInstance pi = qs.findFirstProcessInstance(query);

      List<ProcessInstanceLink> linkedProcessInstances = pi.getLinkedProcessInstances();
      Assert.assertNotNull(linkedProcessInstances);

      boolean contained = false;
      for (ProcessInstanceLink processInstanceLink : linkedProcessInstances)
      {
         if (sourcePiOid == processInstanceLink.getSourceOID()
               && linkType.getId().equals(processInstanceLink.getLinkType().getId()))
         {
            contained = true;
         }
      }
      Assert.assertTrue(contained);
   }

   private ActivityInstance assertActivityInstanceExists(long piOid, String activityId,
         int state) throws IllegalStateException, TimeoutException, InterruptedException
   {
      ActivityInstanceStateBarrier.instance().awaitAlive(piOid);

      QueryService qs = sf.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(piOid);
      query.where(ActivityInstanceQuery.STATE.isEqual(state));
      ActivityInstance ai = qs.findFirstActivityInstance(query);
      Assert.assertEquals(activityId, ai.getActivity().getId());
      // Assert.assertEquals(ImplementationType.Manual,
      // ai.getActivity().getImplementationType());
      return ai;
   }

}
