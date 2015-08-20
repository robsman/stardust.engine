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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessCumulationPolicy;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class contains tests for the <i>Spawn Process</i> functionality,
 * which allows for ad hoc spawning of process instances (refer to the Stardust documentation
 * for details about <i>Spawn Process</i>).
 * </p>
 *
 * @author Roland.Stamm
 */
public class SpawnPeerProcessCrossModelTest
{
   public static final String MODEL_NAME = "SpawnProcessModel";
   public static final String MODEL_NAME2 = "SpawnProcessModel2";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME, MODEL_NAME2);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   // ************************************
   // **             SYNC               **
   // ************************************

   @Test
   public void testSpawnIntoSubprocess() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      DataCopyOptions copyOptions = new DataCopyOptions(Collections.singletonList(TestDataValueConverter.class.getName()));
      SpawnOptions options = new SpawnOptions(null, true, null, copyOptions);
      options.getProcessStateSpec().addJumpTarget("InputData1", "InputData1");
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}ComplexProcess", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(peer.getOID());
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      query.setPolicy(ProcessCumulationPolicy.WITH_PI);
      ActivityInstance ai = qs.findFirstActivityInstance(query);
      Assert.assertEquals("InputData1", ai.getActivity().getId());
      Assert.assertEquals("ComplexProcess", ai.getProcessDefinitionId());
      Assert.assertEquals(ImplementationType.SubProcess, ai.getActivity().getImplementationType());

      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      piQuery.where(ProcessInstanceQuery.STARTING_ACTIVITY_INSTANCE_OID.isEqual(ai.getOID()));
      ProcessInstance sub = qs.findFirstProcessInstance(piQuery);
      Assert.assertEquals(ProcessInstanceState.Active, sub.getState());
      Assert.assertEquals("InputData1", sub.getProcessID());
      Assert.assertEquals(ProcessInstanceState.Active, sub.getState());

      query = ActivityInstanceQuery.findForProcessInstance(sub.getOID());
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      query.setPolicy(ProcessCumulationPolicy.WITH_PI);
      ai = qs.findFirstActivityInstance(query);
      Assert.assertEquals("InputData1", ai.getActivity().getId());
      Assert.assertEquals("InputData1", ai.getProcessDefinitionId());

      wfs.activateAndComplete(ai.getOID(), PredefinedConstants.DEFAULT_CONTEXT, Collections.<String, Object>emptyMap());
      ProcessInstanceStateBarrier.instance().await(sub.getOID(), ProcessInstanceState.Completed);

      ActivityInstanceStateBarrier.instance().awaitForId(peer.getOID(), "Left");
      ActivityInstanceStateBarrier.instance().awaitForId(peer.getOID(), "Right");

      query = ActivityInstanceQuery.findForProcessInstance(peer.getOID());
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      query.setPolicy(ProcessCumulationPolicy.WITH_PI);
      ActivityInstances ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(2));

      complete(wfs, ais);
      ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(1));

      complete(wfs, ais);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testSpawnIntoAndSplit() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(null, true));
      Assert.assertEquals(ProcessInstanceState.Active, pi.getState());

      // Spawn process
      SpawnOptions options = new SpawnOptions(null, true, null, null);
      options.getProcessStateSpec().addJumpTarget("Left");
      options.getProcessStateSpec().addJumpTarget("Right");
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}ComplexProcess", options);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(peer.getOID());
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      query.setPolicy(ProcessCumulationPolicy.WITH_PI);
      ActivityInstances ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(2));

      complete(wfs, ais);
      ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(1));

      complete(wfs, ais);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testCustomConverters() throws TimeoutException, InterruptedException
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      Map<String, Object> inputData = CollectionUtils.newMap();
      inputData.put("Primitive1", "test value");
      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}InputData1", new StartOptions(inputData , true));
      assertThat(pi.getState(), is(ProcessInstanceState.Active));

      // Spawn process
      DataCopyOptions copyOptions = new DataCopyOptions(Collections.singletonList(TestDataValueConverter.class.getName()));
      SpawnOptions options = new SpawnOptions(null, true, null, copyOptions);
      options.getProcessStateSpec().addJumpTarget("InputData1", "InputData1");
      ProcessInstance peer = wfs.spawnPeerProcessInstance(
            pi.getOID(), "{SpawnProcessModel}ComplexProcess", options);

      Object myStruct = wfs.getInDataPath(peer.getOID(), "MyStruct");
      assertThat(myStruct, is(instanceOf(Map.class)));
      assertThat(((Map<?, ?>) myStruct).get("myString"), is(inputData.get("Primitive1")));

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Active);

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(peer.getOID());
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      query.setPolicy(ProcessCumulationPolicy.WITH_PI);
      ActivityInstance ai = qs.findFirstActivityInstance(query);

      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      piQuery.where(ProcessInstanceQuery.STARTING_ACTIVITY_INSTANCE_OID.isEqual(ai.getOID()));
      ProcessInstance sub = qs.findFirstProcessInstance(piQuery);

      query = ActivityInstanceQuery.findForProcessInstance(sub.getOID());
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      query.setPolicy(ProcessCumulationPolicy.WITH_PI);
      ai = qs.findFirstActivityInstance(query);

      wfs.activateAndComplete(ai.getOID(), PredefinedConstants.DEFAULT_CONTEXT, Collections.<String, Object>emptyMap());
      ProcessInstanceStateBarrier.instance().await(sub.getOID(), ProcessInstanceState.Completed);

      ActivityInstanceStateBarrier.instance().awaitForId(peer.getOID(), "Left");
      ActivityInstanceStateBarrier.instance().awaitForId(peer.getOID(), "Right");

      query = ActivityInstanceQuery.findForProcessInstance(peer.getOID());
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      query.setPolicy(ProcessCumulationPolicy.WITH_PI);
      ActivityInstances ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(2));

      complete(wfs, ais);
      ais = qs.getAllActivityInstances(query);
      assertThat(ais.size(), is(1));

      complete(wfs, ais);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(), ProcessInstanceState.Completed);
   }

   @Test(expected=IllegalOperationException.class)
   public void testCompleteSpawnProcessFromSyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1SyncSeperateCopy",
            getStartMap(), true);

      // This makes the running PI a pi from an older model version.
      RtEnvHome.deployModel(sf.getAdministrationService(), null, MODEL_NAME);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", SpawnOptions.DEFAULT);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.activateAndComplete(spawnAi.getOID(), null, null);
      wfs.activateAndComplete(ai.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("All three processes should be completed", 3,
            allProcessInstances.getSize());
   }

   @Test(expected=IllegalOperationException.class)
   public void testAbortSpawnProcessFromSyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1SyncSeperateCopy",
            getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", SpawnOptions.DEFAULT);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.abortActivityInstance(spawnAi.getOID(), AbortScope.SubHierarchy);
      wfs.abortActivityInstance(ai.getOID(), AbortScope.SubHierarchy);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("All three processes should be completed", 3,
            allProcessInstances.getSize());
   }

   @Test(expected=IllegalOperationException.class)
   public void testFullAbortSpawnProcessFromSyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess(
            "{SpawnProcessModel}StartInputSubprocess1SyncSeperateCopy", getStartMap(),
            true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", SpawnOptions.DEFAULT);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.abortActivityInstance(spawnAi.getOID(), AbortScope.RootHierarchy);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted));
      Assert.assertEquals("All three processes should be aborted", 3,
            allProcessInstances.getSize());
   }

   @Test
   public void testDataMapOverDataCopyFromAsyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1Async",
            getStartMap(), true);

      // This makes the running PI a pi from an older model version.
      RtEnvHome.deployModel(sf.getAdministrationService(), null, MODEL_NAME);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("{SpawnProcessModel}InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAlive(subprocessInstance.getOID());
         ActivityInstanceQuery query2 = ActivityInstanceQuery.findForProcessInstance(subprocessInstance.getOID());
         query2.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
         ai = qs.findFirstActivityInstance(query2);
      }

      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      Map<String, Serializable> map2 = new HashMap<String, Serializable>();
      map2.put("Primitive1", "notNewValue");
      SpawnOptions spawnOptions = new SpawnOptions(null, true, "comment",
            new DataCopyOptions(true, null, map2, true));

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData1", spawnOptions);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertNotEquals("Spawned and parent should not have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertNotEquals("The processOid used for spawning should not be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      // Main assert
      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      // assert primitive is copied.
      Serializable inDataValue = wfs.getInDataValue(spawnAi.getOID(), null, "Primitive1");
      Assert.assertEquals("notNewValue", inDataValue);

      // assert structured data can be copied into similar but upgraded type definition.
      @SuppressWarnings("unchecked")
      Map<String,String> inDataValue2 = (Map<String, String>) wfs.getInDataValue(spawnAi.getOID(), null, "Struct1");
      Assert.assertEquals("myValue", inDataValue2.get("myString"));

      wfs.activateAndComplete(spawnAi.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("Two processes should be completed", 2,
            allProcessInstances.getSize());
   }

   @Test
   public void testInterruptSpawnedProcessFromAsyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1Async",
            getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("{SpawnProcessModel}InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAlive(subprocessInstance.getOID());
         ActivityInstanceQuery query2 = ActivityInstanceQuery.findForProcessInstance(subprocessInstance.getOID());
         query2.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
         ai = qs.findFirstActivityInstance(query2);
      }

      wfs.suspendToDefaultPerformer(ai.getOID(), null, null);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}Interrupt2", SpawnOptions.DEFAULT);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertNotEquals("Spawned and parent should not have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertNotEquals("The processOid used for spawning should not be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ProcessInstanceStateBarrier.instance().await(spawnPeerProcessInstance.getOID(), ProcessInstanceState.Interrupted);
      Assert.assertEquals("SpawnPi should be in state Interrupted.", ProcessInstanceState.INTERRUPTED, getPiWithHierarchy(spawnPeerProcessInstance, qs).getState().getValue());
      Assert.assertEquals("SubPi should be in state Aborted.", ProcessInstanceState.ABORTED, getPiWithHierarchy(subprocessInstance, qs).getState().getValue());
      Assert.assertEquals("RootPi should be in state Completed.", ProcessInstanceState.COMPLETED, getPiWithHierarchy(pi, qs).getState().getValue());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.INTERRUPTED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      // Main assert
      Assert.assertNotNull(
            "Interactive activityInstance in state interrupted exists in spawned process.",
            spawnAi);
   }

   @Test
   public void testShowDocumentDataFromAsyncSubprocess() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1Async",
            getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("{SpawnProcessModel}InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAlive(subprocessInstance.getOID());
         ActivityInstanceQuery query2 = ActivityInstanceQuery.findForProcessInstance(subprocessInstance.getOID());
         query2.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
         ai = qs.findFirstActivityInstance(query2);
      }

      wfs.suspendToDefaultPerformer(ai.getOID(), null, null);

      Map<String, Serializable > map = new HashMap<String, Serializable>();
      map.put("Doc_1", getDoc(sf, pi));
      SpawnOptions spawnOptions = new SpawnOptions(null, true, "comment",
            new DataCopyOptions(true, null, map, true));

      // Spawn process
      final ProcessInstance spawnedPi = wfs.spawnPeerProcessInstance(ai.getProcessInstanceOID(), "{SpawnProcessModel2}ShowDoc2", spawnOptions);
      ActivityInstanceStateBarrier.instance().awaitForId(spawnedPi.getOID(), "DisplayDocData");
   }

   // ************************************
   // **             ASYNC              **
   // ************************************

   @Test
   public void testCompleteSpawnProcessFromAsyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1Async", null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("{SpawnProcessModel}InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAlive(subprocessInstance.getOID());
         ActivityInstanceQuery query2 = ActivityInstanceQuery.findForProcessInstance(subprocessInstance.getOID());
         query2.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
         ai = qs.findFirstActivityInstance(query2);
      }

      attachToProcess(getDoc(sf, subprocessInstance), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", SpawnOptions.DEFAULT);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertNotEquals("Spawned and source should not have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertNotEquals("The processOid used for spawning should not be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ProcessInstanceStateBarrier.instance().await(subprocessInstance.getOID(), ProcessInstanceState.Aborted);
      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.activateAndComplete(spawnAi.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(spawnPeerProcessInstance.getOID(), ProcessInstanceState.Completed);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("Two processes should be completed", 2,
            allProcessInstances.getSize());
   }

   @Test
   public void testAbortSpawnProcessFromAsyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1Async",  getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("{SpawnProcessModel}InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAlive(subprocessInstance.getOID());
         ActivityInstanceQuery query2 = ActivityInstanceQuery.findForProcessInstance(subprocessInstance.getOID());
         query2.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
         ai = qs.findFirstActivityInstance(query2);
      }

      attachToProcess(getDoc(sf, subprocessInstance), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", SpawnOptions.DEFAULT);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertNotEquals("Spawned and source should not have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertNotEquals("The processOid used for spawning should not be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.abortActivityInstance(spawnAi.getOID(), AbortScope.SubHierarchy);
      ActivityInstanceStateBarrier.instance().await(spawnAi.getOID(), ActivityInstanceState.Aborted);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(subprocessInstance.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(spawnPeerProcessInstance.getOID(), ProcessInstanceState.Completed);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("Two processes should be completed", 2,
            allProcessInstances.getSize());
   }

   @Test
   public void testFullAbortSpawnProcessFromAsyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1Async",  getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("{SpawnProcessModel}InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAlive(subprocessInstance.getOID());
         ActivityInstanceQuery query2 = ActivityInstanceQuery.findForProcessInstance(subprocessInstance.getOID());
         query2.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
         ai = qs.findFirstActivityInstance(query2);
      }

      attachToProcess(getDoc(sf, subprocessInstance), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", SpawnOptions.DEFAULT);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnPeerProcessInstance.getOID());
      Assert.assertNotEquals("Spawned and source should not have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnPeerProcessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnPeerProcessInstance.getScopeProcessInstanceOID());
      Assert.assertNotEquals("The processOid used for spawning should not be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnPeerProcessInstance, qs).getParentProcessInstanceOid());

      ProcessInstanceStateBarrier.instance().await(subprocessInstance.getOID(), ProcessInstanceState.Aborted);
      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.abortActivityInstance(spawnAi.getOID(), AbortScope.RootHierarchy);

      ProcessInstanceStateBarrier.instance().await(spawnPeerProcessInstance.getOID(), ProcessInstanceState.Aborted);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted));
      Assert.assertEquals("Two processes should be aborted", 2,
            allProcessInstances.getSize());

      ProcessInstances allProcessInstances2 = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("One processe should be completed", 1,
            allProcessInstances2.getSize());
   }

   @Test
   public void testProcessAttachmentCopyFromAsyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1Async",  getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("{SpawnProcessModel}InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAlive(subprocessInstance.getOID());
         ActivityInstanceQuery query2 = ActivityInstanceQuery.findForProcessInstance(subprocessInstance.getOID());
         query2.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
         ai = qs.findFirstActivityInstance(query2);
      }

      attachToProcess(getDoc(sf, subprocessInstance), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnPeerProcessInstance = wfs.spawnPeerProcessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", SpawnOptions.DEFAULT);

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnPeerProcessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnPeerProcessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      Object processAttachments = wfs.getInDataPath(spawnAi.getProcessInstanceOID(), "PROCESS_ATTACHMENTS");
      if (processAttachments instanceof List)
      {
         @SuppressWarnings("unchecked")
         Document doc = ((List<Document>) processAttachments).get(0);
         Assert.assertNotNull("Doc should exist." ,doc);
         Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt", doc.getName());
      }
      else
      {
         Assert.fail("Should be List<Document> but is: "+ processAttachments);
      }
   }

   // ************************************
   // **             SYNC               **
   // ************************************

   private Map<String, ? > getStartMap()
   {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("Primitive2", "newValue");
      map.put("Struct1", Collections.singletonMap("myString", "myValue"));
      return map;
   }

   private ProcessInstance getPiWithHierarchy(ProcessInstance spawnPeerProcessInstance, QueryService qs)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();

      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);

      ProcessInstanceDetailsPolicy pidp = new ProcessInstanceDetailsPolicy(
            ProcessInstanceDetailsLevel.Default);
      pidp.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
      query.setPolicy(pidp);
      query.where(ProcessInstanceQuery.OID.isEqual(spawnPeerProcessInstance.getOID()));
      return qs.findFirstProcessInstance(query);
   }

   private void complete(WorkflowService wfs, ActivityInstances ais)
   {
      for (ActivityInstance ai : ais)
      {
         wfs.activateAndComplete(ai.getOID(), PredefinedConstants.DEFAULT_CONTEXT, Collections.<String, Object>emptyMap());
      }
   }

   private void attachToProcess(Document doc, ServiceFactory sf, long oid)
   {
      WorkflowService wfs = sf.getWorkflowService();

      Object processAttachments = wfs.getInDataPath(oid, "PROCESS_ATTACHMENTS");

      if (processAttachments == null)
      {
         processAttachments = new ArrayList<Document>();
      }

      if (processAttachments instanceof Collection)
      {
         @SuppressWarnings("unchecked")
         Collection<Document> processAttachmentsCollection = (Collection<Document>) processAttachments;

         Document oldDoc = getDocumentById(processAttachmentsCollection, doc);
         if (null != oldDoc)
         {
            processAttachmentsCollection.remove(oldDoc);
         }
         processAttachmentsCollection.add(doc);
      }

      wfs.setOutDataPath(oid, "PROCESS_ATTACHMENTS", processAttachments);
   }

   private Document getDocumentById(Collection<Document> processAttachments, Document v0)
   {
      for (Object doc : processAttachments)
      {
         if (doc instanceof Document)
         {
            if (((Document) doc).getId().equals(v0.getId()))
            {
               return (Document) doc;
            }
         }
      }
      return null;
   }

   private Document getDoc(ServiceFactory sf, ProcessInstance pi)
   {
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
      String s = defaultPath + "/process-attachments";

      DocumentManagementService dms = sf.getDocumentManagementService();
      DmsUtils.ensureFolderHierarchyExists(s, dms);

      Document document = dms.getDocument(s + "/spawnTest.txt");

      if (document == null)
      {
         DocumentInfo docInfo = new DmsDocumentBean();

         docInfo.setName("spawnTest.txt");
         docInfo.setContentType("text/plain");
         Map<String, String> map = newHashMap();
         map.put("myString", "myMetaDataString");
         docInfo.setProperties(map);
         document = dms.createDocument(s, docInfo);
      }

      return document;
   }

   public static final class PojoApp
   {
      public void interrupt()
      {
         /* always throws an exception to interrupt activity instance */
         throw new UnsupportedOperationException();
      }
   }
}
