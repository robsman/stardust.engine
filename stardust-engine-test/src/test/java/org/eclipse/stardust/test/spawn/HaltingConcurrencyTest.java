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
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions.SpawnMode;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

/**
 * <p>
 * This class contains tests for the process halt feature for concurrency issues.
 * </p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class HaltingConcurrencyTest
{
   public static final String MODEL_NAME = "SpawnProcessModel";

   public static final String MODEL_NAME2 = "MultiInstance";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_NAME, MODEL_NAME2);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Before
   public void before()
   {
      ProcessInstanceStateBarrier.instance().cleanUp();
   }

   @Test
   public void testInsertMultiInstanceAsyncSeperate() throws TimeoutException, InterruptedException
   {
      for (int i = 0; i < 10; i++)
      {
         System.out.println("************* Run "+i+" ***********");
         doTest();

         // allow async jms messages to be processed before next run.
         doWait(2000);
      }
   }

   public void doTest() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Object> multi = CollectionUtils.newMap();
      //
      multi.put("List", createMultiInstanceList(5));

      Map<String, Object> inputData = CollectionUtils.newMap();
      inputData.put("Multi", multi);

      ProcessInstance pi = wfs.startProcess("{MultiInstance}Main",
            new StartOptions(inputData, true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      ProcessInstanceStateBarrier.instance().cleanUp();

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(pi.getOID(),
            "{MultiInstance}Sub2", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(),
            ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(),
            PredefinedProcessInstanceLinkTypes.INSERT);

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Active);

      // wait for manual activity after completed multi-instance activity.
      int tryCount = 10;
      while (tryCount-- > 0)
      {
         try
         {
            wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
            assertActivityInstanceExists(pi.getOID(), "Manual",
                  ActivityInstanceState.APPLICATION);
            tryCount = 0;
         }
         catch (ObjectNotFoundException e)
         {
            doWait(1000);
            if (tryCount == 0)
            {
               Assert.fail("Expected AI 'Manual' not found");
            }
         }
      }
      // ensure there is no additional Manual activities
      doWait(2000);
      assertActivityInstanceCount(pi, "Manual", 1, ActivityInstanceState.APPLICATION);
      assertActivityInstanceCount(pi, "Manual", 0, ActivityInstanceState.SUSPENDED);
      assertActivityInstanceCount(pi, "Manual", 0, ActivityInstanceState.HALTED);
   }

   @Test
   public void testInsertMultiInstanceSyncSeperate() throws TimeoutException, InterruptedException
   {
      for (int i = 0; i < 10; i++)
      {
         System.out.println("************* Run "+i+" ***********");
         doTest3();

         // allow async jms messages to be processed before next run.
         doWait(2000);
      }
   }

   public void doTest3() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Object> multi = CollectionUtils.newMap();
      //
      multi.put("List", createMultiInstanceList(5));

      Map<String, Object> inputData = CollectionUtils.newMap();
      inputData.put("Multi", multi);

      ProcessInstance pi = wfs.startProcess("{MultiInstance}Main2",
            new StartOptions(inputData, true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(pi.getOID(),
            "{MultiInstance}Sub2", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(),
            ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(),
            PredefinedProcessInstanceLinkTypes.INSERT);

      ProcessInstanceStateBarrier.instance().cleanUp();
      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Active);

      // wait for manual activity after completed multi-instance activity.
      int tryCount = 10;
      while (tryCount-- > 0)
      {
         try
         {
            wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
            assertActivityInstanceExists(pi.getOID(), "Manual",
                  ActivityInstanceState.APPLICATION);
            tryCount = 0;
         }
         catch (ObjectNotFoundException e)
         {
            doWait(1000);
            if (tryCount == 0)
            {
               Assert.fail("Expected AI 'Manual' not found");
            }
         }
      }
      // ensure there is no additional Manual activities
      doWait(2000);
      assertActivityInstanceCount(pi, "Manual", 1, ActivityInstanceState.APPLICATION);
      assertActivityInstanceCount(pi, "Manual", 0, ActivityInstanceState.SUSPENDED);
      assertActivityInstanceCount(pi, "Manual", 0, ActivityInstanceState.HALTED);
   }

   @Test
   public void testAndSplit() throws TimeoutException, InterruptedException
   {
      for (int i = 0; i < 10; i++)
      {
         System.out.println("************* Run "+i+" ***********");
         doTest2();

         // allow async jms messages to be processed before next run.
         doWait(2000);
      }
   }

   private void doTest2() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}ActivityStateChangeEvent",
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

      getAI(pi.getOID(), "NAtoHalted", ActivityInstanceState.Halted);
      getAI(pi.getOID(), "SuspendedtoHalted", ActivityInstanceState.Halted);
      getAI(pi.getOID(), "HaltedtoNA", ActivityInstanceState.Halted);
      getAI(pi.getOID(), "HaltedtoSuspended", ActivityInstanceState.Halted);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(),
            PredefinedProcessInstanceLinkTypes.INSERT);


      // resume
      completeActivityInstances(peer.getOID(), 1);
   }

   // ************** UTILS ***************

   private ActivityInstance getAI(long oid, String activityId, ActivityInstanceState expectedState) throws IllegalStateException, TimeoutException, InterruptedException
   {
      ActivityInstanceStateBarrier.instance().awaitForId(oid, activityId);

      QueryService qs = sf.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(oid);
      query.where(ActivityFilter.forAnyProcess(activityId));

      int patientCounter = 5;
      ActivityInstances ais = null;
      while (patientCounter-- > 0)
      {
         ais = qs.getAllActivityInstances(query);

         if (ais.size() == 1)
         {
            patientCounter = 0;
         }
         else
         {
            doWait(1000);
         }
      }
      Assert.assertTrue(0 < ais.size());
      ActivityInstance ai = ais.get(0);

      Assert.assertEquals(activityId, ai.getActivity().getId());

      if (!expectedState.equals(ai.getState()))
      {
         // compensation needs to happen on disallowed operations e.g. if AI is not halted but PI hierarchy is.
         try
         {
            sf.getWorkflowService().activate(ai.getOID());
            Assert.fail("IllegalOperationException expected");
         }
         catch (IllegalOperationException e)
         {
            assertThat(e.getError().getId(), isOneOf("BPMRT08001", "BPMRT08002"));
            System.err.println(">> " + ai + ", " + sf.getWorkflowService().getActivityInstance(ai.getOID()).getState());
            ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Halted);
         }


         // re-retrieve to ensure compensation happened after activate call.
//         ActivityInstance ai2 = sf.getWorkflowService().getActivityInstance(ai.getOID());

         ais = qs.getAllActivityInstances(query);
         ActivityInstance ai2 = ais.get(0);
         Assert.assertEquals(expectedState, ai2.getState());
         return ai2;
      }
      else
      {
         // is correctly in expected state
         return ai;
      }
   }

   private Object createMultiInstanceList(int count)
   {
      List<String> list = CollectionUtils.newList();

      for (int i = 0; i < count; i++)
      {
         list.add("" + i);
      }
      return list;
   }

   private void doWait(int i)
   {
      try
      {
         Thread.sleep(i);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
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

   private void completeActivityInstances(long piOid, int count)
   {
      QueryService qs = sf.getQueryService();
      WorkflowService wfs = sf.getWorkflowService();

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(piOid);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));

      int patientCounter = 5;
      ActivityInstances ais = null;
      while (patientCounter-- > 0)
      {
         ais = qs.getAllActivityInstances(query);

         if (ais.size() == count)
         {
            patientCounter = 0;
         }
         else
         {
            doWait(1000);
         }
      }

      assertThat(ais.size(), is(count));

      for (ActivityInstance ai : ais)
      {
         wfs.activateAndComplete(ai.getOID(), PredefinedConstants.DEFAULT_CONTEXT,
               Collections.<String, Object> emptyMap());
      }
      ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(0));

   }

   private ActivityInstance assertActivityInstanceExists(long piOid, String activityId,
         int state) throws IllegalStateException, TimeoutException, InterruptedException
   {
      ActivityInstanceStateBarrier.instance().awaitAlive(piOid);

      QueryService qs = sf.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(piOid);
      query.where(ActivityFilter.forAnyProcess(activityId));
      query.where(ActivityInstanceQuery.STATE.isEqual(state));
      ActivityInstance ai = qs.findFirstActivityInstance(query);
      Assert.assertEquals(activityId, ai.getActivity().getId());
      return ai;
   }

   private void assertActivityInstanceCount(ProcessInstance pi, String activityId,
         int count, int state)
               throws IllegalStateException, TimeoutException, InterruptedException
   {
      QueryService qs = sf.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      query.where(ActivityFilter.forProcess(activityId, pi.getProcessID()));
      query.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      query.where(ActivityInstanceQuery.STATE.isEqual(state));
      ActivityInstances ai = qs.getAllActivityInstances(query);
      Assert.assertEquals(count, ai.getSize());
   }

}
