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
import java.util.concurrent.TimeoutException;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions.SpawnMode;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.beans.EventDaemon;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

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
   public void testInsertProcessUser1() throws TimeoutException, InterruptedException
   {
      UserHome.create(sf, "u1");
      ServiceFactory sf = ServiceFactoryLocator.get("u1","u1");
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, DataCopyOptions.DEFAULT);
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

      sf.close();
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
   public void testInsertThenAbortOnSameProcess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      ProcessInstance peer = wfs.spawnPeerProcessInstance(pi.getOID(),
            "{SpawnProcessModel}InputData1",
            new SpawnOptions(null, SpawnMode.HALT, null, null));

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.HALTED);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);
      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      ProcessInstance peer2 = wfs.spawnPeerProcessInstance(pi.getOID(),
            "{SpawnProcessModel}InputData1",
            new SpawnOptions(null, SpawnMode.ABORT, null, null));

      ProcessInstanceStateBarrier.instance().await(peer2.getOID(), ProcessInstanceState.Active);
      assertProcessInstanceLinkExists(peer2.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.UPGRADE);
      assertActivityInstanceExists(peer2.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.ABORTED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check if still aborted.
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.ABORTED);

      completeActivityInstances(peer2.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer2.getOID(), ProcessInstanceState.Completed);

      // check if still aborted.
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.ABORTED);
   }

   @Test(expected=IllegalOperationException.class)
   public void testInsertOnAbortedProcess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      wfs.abortProcessInstance(pi.getOID(), AbortScope.RootHierarchy);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);
      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.ABORTED);

      // Spawn process, should throw exception
      wfs.spawnPeerProcessInstance(pi.getOID(),
            "{SpawnProcessModel}InputData1",
            new SpawnOptions(null, SpawnMode.HALT, null, null));
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
    * Tests that event action triggered by event daemon is ignored for halted processes
    * but event daemon execution after resuming works.
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

      // check for timer fired (3sec)
      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Hibernated);
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HIBERNATED);
   }

   //@Test
   public void repeatedTestActivityStateChangeEvent() throws TimeoutException, InterruptedException
   {
      for (int i = 0; i < 10; i++)
      {
         sf.getAdministrationService().cleanupRuntime(true);
         ProcessInstanceStateBarrier.instance().cleanUp();
         ActivityInstanceStateBarrier.instance().cleanUp();

         System.err.println("*** " + (i + 1) + " ***");
         testActivityStateChangeEvent();
      }
   }

   @Test
   public void testActivityStateChangeEvent() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}ActivityStateChangeEvent", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      doWait(3000);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      doWait(2000);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "NAtoHalted", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(pi.getOID(), "SuspendedtoHalted", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(pi.getOID(), "HaltedtoNA", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(pi.getOID(), "HaltedtoSuspended", ActivityInstanceState.HALTED);
      assertActivityInstanceCount("ShowDoc", "DisplayDocData", 2, ActivityInstanceState.SUSPENDED);

      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      assertActivityInstanceExists(pi.getOID(), "NAtoHalted", ActivityInstanceState.SUSPENDED);
      assertActivityInstanceExists(pi.getOID(), "SuspendedtoHalted", ActivityInstanceState.SUSPENDED);
      assertActivityInstanceExists(pi.getOID(), "HaltedtoNA", ActivityInstanceState.SUSPENDED);
      assertActivityInstanceExists(pi.getOID(), "HaltedtoSuspended", ActivityInstanceState.SUSPENDED);

      // The barrier gets triggered before the database is actually committed.
      doWait(2000);

      assertActivityInstanceCount("ShowDoc", "DisplayDocData", 4, ActivityInstanceState.SUSPENDED);
   }

   @Test
   public void testActivityStateChangeEventScheduleActivityAction() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}ActivityStateChangeEvent2", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      doWait(3000);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      doWait(2000);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      assertActivityInstanceExists(pi.getOID(), "EventOnHaltedToHibernated", ActivityInstanceState.HALTED);
      assertActivityInstanceExists(pi.getOID(), "EventOnHaltedToSuspended", ActivityInstanceState.HALTED);

      assertActivityInstanceExists(peer.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      // check for halted -> active
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);
      assertActivityInstanceExists(pi.getOID(), "EventOnHaltedToHibernated", ActivityInstanceState.SUSPENDED);
      assertActivityInstanceExists(pi.getOID(), "EventOnHaltedToSuspended", ActivityInstanceState.SUSPENDED);
   }

   @Test
   public void testSlowPOJOAppCanComplete() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}Wait", new StartOptions(null , false));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // wait for application state, POJO waits for 5 sec.
      doWait(2000);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);


      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);


      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);

      assertActivityInstanceExists(pi.getOID(), "done", ActivityInstanceState.SUSPENDED);

   }

   @Test
   public void testSlowPOJOAppCanInterrupt() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}WaitAndInterrupt", new StartOptions(null , false));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // wait for application state, POJO waits for 5 sec then interrupts.
      doWait(1000);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);


      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      // Now POJO app has interrupted
      doWait(6000);


      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.HALTED);

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);

      doWait(6000);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);
      assertActivityInstanceExists(pi.getOID(), "Activity_1", ActivityInstanceState.INTERRUPTED);
   }

   //@Test
   public void repeatedTestSubProcessWorksAfterApplicationStateAICompleted() throws TimeoutException, InterruptedException
   {
      for (int i = 0; i < 10; i++)
      {
         System.err.println("*** Test # " + (i + 1) + " ***");
         testSubProcessWorksAfterApplicationStateAICompleted();
      }
   }

   @Test
   public void testSubProcessWorksAfterApplicationStateAICompleted() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}ComplexProcess", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Activate manual AI
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
      Assert.assertNotNull(ai);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);

      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      // Complete manual AI of main process. This halts the next subprocess ai.
      patientComplete(wfs, ai.getOID());

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);

      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);

//      assertActivityInstanceExists(pi.getOID(), "left", ActivityInstanceState.SUSPENDED);
//      assertActivityInstanceExists(pi.getOID(), "right", ActivityInstanceState.SUSPENDED);
   }

   private void patientComplete(WorkflowService wfs, long oid)
   {
      int count = 10;

      while (count >0)
      {
         try
         {
         wfs.complete(oid, null, null);
         count = 0;
         }
         catch (Exception e)
         {
           e.printStackTrace();

           doWait(500);
           count--;
         }
      }

   }

   @Test
   public void testSubProcessWorksAfterApplicationStateAISuspended() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}ComplexProcess", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Activate manual AI
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
      Assert.assertNotNull(ai);

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}InputData1", options);


      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Halted);
      assertProcessInstanceLinkExists(peer.getOID(), pi.getOID(), PredefinedProcessInstanceLinkTypes.INSERT);

      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Halted);
      assertThat(wfs.getActivityInstance(ai.getOID()).getState(), is(ActivityInstanceState.Halted));
      // Suspending manual AI of main process only saves the data but nothing else.
      wfs.suspend(ai.getOID(), null);
      assertThat(wfs.getActivityInstance(ai.getOID()).getState(), is(ActivityInstanceState.Halted));

      // reset registered state changes before next steps
      ProcessInstanceStateBarrier.instance().cleanUp();

      completeActivityInstances(peer.getOID(), 1);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Active);

      // Complete manual AI of main process.
      wfs.activateAndComplete(ai.getOID(), null, null);

      assertActivityInstanceExists(pi.getOID(), "InputData1", ActivityInstanceState.SUSPENDED);
      assertActivityInstanceCount(pi.getProcessID(), "InputData1", 2, ActivityInstanceState.SUSPENDED);
      assertActivityInstanceCount("{SpawnProcessModel}InputData1", "InputData1", 1, ActivityInstanceState.SUSPENDED);
   }

   // ************** UTILS ***************

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

   private ActivityInstance assertActivityInstanceExists(long piOid, String activityId, int state) throws IllegalStateException, TimeoutException, InterruptedException
   {
      ActivityInstanceStateBarrier.instance().awaitAlive(piOid);

      QueryService qs = sf.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(piOid);
      query.where(ActivityFilter.forAnyProcess(activityId));
      query.where(ActivityInstanceQuery.STATE.isEqual(state));

      int patientCounter = 10;
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
      return ai;
   }


   private void assertActivityInstanceCount(String processId, String activityId, int count, int state) throws IllegalStateException, TimeoutException, InterruptedException
   {
      QueryService qs = sf.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      query.where(ActivityFilter.forProcess(activityId, processId));
      query.where(ActivityInstanceQuery.STATE.isEqual(state));
      ActivityInstances ai = qs.getAllActivityInstances(query);
      Assert.assertEquals(count, ai.getSize());
   }

}
