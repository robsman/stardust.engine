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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions.SpawnMode;
import org.eclipse.stardust.engine.core.runtime.beans.EventDaemon;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;

/**
 * <p>
 * This class contains tests for the <i>Spawn Process</i> functionality,
 * which allows for ad hoc spawning of process instances (refer to the Stardust documentation
 * for details about <i>Spawn Process</i>).
 * </p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class SpawnPeerInsertProcessTest
{
   public static final String MODEL_NAME = "SpawnProcessModel";
   public static final String MODEL_NAME2 = "MultiInstance";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_NAME, MODEL_NAME2);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Before
   public void before()
   {
      ProcessInstanceStateBarrier.instance().cleanUp();
   }

   @Test
   public void testInsertProcess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);
   }

   @Test
   public void testInsertTwoOnSameProcess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);
      ProcessInstance peer2 = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      ProcessInstanceStateBarrier.instance().await(peer2.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);
      assertProcessInstanceLinkExists(peer2.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);
      assertActivityInstanceExists(peer2.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check if still halted.
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.HALTED);

      completeActivityInstances(peer2.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer2.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);
   }

   @Test
   public void testInsertOnInsertedProcess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      // needed because it waits until activity is created and suspended. Else it is not found by halting janitor.
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // Spawn from spawned process
      ProcessInstance peer2 = wfs.spawnPeerProcessInstance(
            peer.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer2.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);
      assertProcessInstanceLinkExists(peer2.getOID(), peer.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer2.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer2.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer2.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active (peer)
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);
      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active (pi)
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);
   }


   @Test
   public void testInsertInterruptedProcess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}Interrupt", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Interrupted));
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.INTERRUPTED);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for halted -> interrupted
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.INTERRUPTED);
   }

   @Test
   public void testInsertHibernatedProcess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}Hibernate", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));
      ActivityInstance ai = assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HIBERNATED);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();
      ActivityInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for pi halted -> active
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      // check for ai halted -> Hibernated
      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Hibernated);
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HIBERNATED);
   }

   /**
    * Tests that event action triggered by event daemon is working if previously halted processes is resumed.
    */
   @Test
   public void testInsertWithTimerEvent() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();
      sf.getAdministrationService().startDaemon(EventDaemon.ID, true);

      // Timer event puts activity into hibernation after 3sec
      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}TimerEvent", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.SUSPENDED);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      ActivityInstance ai = assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.SUSPENDED);

      // check for timer fired (3sec)
      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Hibernated);
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HIBERNATED);
   }

   /**
    * Tests that event action triggered by event daemon is ignored for halted processes.
    */
   @Test
   public void testInsertWithTimerEventWhileHalted() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();
      sf.getAdministrationService().startDaemon(EventDaemon.ID, true);

      // Timer event puts activity into hibernation after 3sec
      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}TimerEvent", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.SUSPENDED);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      doWait(3000);

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      ActivityInstance ai = assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.SUSPENDED);

      // check for timer not fired
      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Suspended);
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.SUSPENDED);
   }

   @Test
   public void testInsertMultiInstance() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Object> multi = CollectionUtils.newMap();
      //
      multi.put("List", createMultiInstanceList(5));

      Map<String, Object> inputData = CollectionUtils.newMap();
      inputData.put("Multi", multi);

      ProcessInstance pi = wfs.startProcess("{MultiInstance}Main", new StartOptions(inputData , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{MultiInstance}Sub2", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);


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
   }

   // ************** UTILS ***************

   private Object createMultiInstanceList(int count)
   {
      List<String> list = CollectionUtils.newList();

      for (int i = 0; i < count; i++)
      {
         list.add(""+i);
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

      ProcessInstanceDetailsPolicy detailsPolicy = new ProcessInstanceDetailsPolicy(ProcessInstanceDetailsLevel.Default);
      detailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);

      query.setPolicy(detailsPolicy);

      ProcessInstance pi = qs.findFirstProcessInstance(query);

      List<ProcessInstanceLink> linkedProcessInstances = pi.getLinkedProcessInstances();
      Assert.assertNotNull(linkedProcessInstances);

      boolean contained = false;
      for (ProcessInstanceLink processInstanceLink : linkedProcessInstances)
      {
         if (sourcePiOid == processInstanceLink.getSourceOID() &&
               linkType.getId().equals(processInstanceLink.getLinkType().getId()))
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
      ActivityInstances ais = qs.getAllActivityInstances(query);

      assertThat(ais.size(), is(count));

      for (ActivityInstance ai : ais)
      {
         wfs.activateAndComplete(ai.getOID(), PredefinedConstants.DEFAULT_CONTEXT, Collections.<String, Object>emptyMap());
      }
      ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(0));

   }

   private ActivityInstance assertActivityInstanceExists(long piOid, String activityId, int state) throws IllegalStateException, TimeoutException, InterruptedException
   {
      ActivityInstanceStateBarrier.instance().awaitAlive(piOid);

      QueryService qs = sf.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(piOid);
      query.where(ActivityInstanceQuery.STATE.isEqual(state));
      ActivityInstance ai = qs.findFirstActivityInstance(query);
      Assert.assertEquals(activityId, ai.getActivity().getId());
//      Assert.assertEquals(ImplementationType.Manual, ai.getActivity().getImplementationType());
      return ai;
   }

}
