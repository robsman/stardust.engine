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
package org.eclipse.stardust.test.spawn;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
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
public class SpawnProcessCrossModelTest
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
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", true, null);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
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

   @Test
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
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", true, null);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
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

   @Test
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
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", true, null);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
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
   public void testDataMapOverDataCopyFromSyncSubprocess() throws Exception
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

      Map<String, Serializable> map2 = new HashMap<String, Serializable>();
      map2.put("Primitive1", "notNewValue");

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData1", true, map2);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
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
      wfs.activateAndComplete(ai.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("All three processes should be completed", 3,
            allProcessInstances.getSize());
   }

   @Test
   public void testInterruptSpawnedProcess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1SyncSeperateCopy",
            getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      wfs.suspendToDefaultPerformer(ai.getOID(), null, null);

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}Interrupt2", true, null);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ProcessInstanceStateBarrier.instance().await(spawnSubprocessInstance.getOID(), ProcessInstanceState.Interrupted);
      Assert.assertEquals("SpawnPi should be in state Interrupted.", ProcessInstanceState.INTERRUPTED, getPiWithHierarchy(spawnSubprocessInstance, qs).getState().getValue());
      Assert.assertEquals("RootPi should be in state Active.", ProcessInstanceState.ACTIVE, getPiWithHierarchy(pi, qs).getState().getValue());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.INTERRUPTED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      // Main assert
      Assert.assertNotNull(
            "Interactive activityInstance in state interrupted exists in spawned process.",
            spawnAi);
   }

   @Test
   public void testShowDocumentData() throws InterruptedException, TimeoutException
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{SpawnProcessModel}StartInputSubprocess1SyncSeperateCopy",
            getStartMap(), true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      wfs.suspendToDefaultPerformer(ai.getOID(), null, null);

      Map<String, Serializable > map = new HashMap<String, Serializable>();
      map.put("Doc_1", getDoc(sf, pi));
      // Spawn process
      final ProcessInstance spawnedPi = wfs.spawnSubprocessInstance(ai.getProcessInstanceOID(), "{SpawnProcessModel2}ShowDoc2", true, map);
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
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", true, null);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
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
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", true, null);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.abortActivityInstance(spawnAi.getOID(), AbortScope.SubHierarchy);
      ActivityInstanceStateBarrier.instance().await(spawnAi.getOID(), ActivityInstanceState.Aborted);
      wfs.abortActivityInstance(ai.getOID(), AbortScope.SubHierarchy);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(subprocessInstance.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(spawnSubprocessInstance.getOID(), ProcessInstanceState.Completed);

      ProcessInstances allProcessInstances = qs.getAllProcessInstances(ProcessInstanceQuery.findCompleted());
      Assert.assertEquals("All three processes should be completed", 3,
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
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", true, null);

      Assert.assertTrue("Are not the same PI",
            ai.getProcessInstanceOID() != spawnSubprocessInstance.getOID());
      Assert.assertEquals("Spawned and parent should have same rootProcessInstanceOid.",
            ai.getProcessInstance().getRootProcessInstanceOID(),
            spawnSubprocessInstance.getRootProcessInstanceOID());
      Assert.assertTrue(
            "Spawned and parent should have different scopeProcessInstanceOid.",
            ai.getProcessInstance().getScopeProcessInstanceOID() != spawnSubprocessInstance.getScopeProcessInstanceOID());
      Assert.assertEquals("The processOid used for spawning should be the parent.",
            ai.getProcessInstanceOID(),
            getPiWithHierarchy(spawnSubprocessInstance, qs).getParentProcessInstanceOid());

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      wfs.abortActivityInstance(spawnAi.getOID(), AbortScope.RootHierarchy);

      ProcessInstanceStateBarrier.instance().await(subprocessInstance.getOID(), ProcessInstanceState.Aborted);

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
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "{SpawnProcessModel2}InputData3", true, null);

      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
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

   private ProcessInstance getPiWithHierarchy(ProcessInstance spawnSubprocessInstance, QueryService qs)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();

      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);

      ProcessInstanceDetailsPolicy pidp = new ProcessInstanceDetailsPolicy(
            ProcessInstanceDetailsLevel.Default);
      pidp.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
      query.setPolicy(pidp);
      query.where(ProcessInstanceQuery.OID.isEqual(spawnSubprocessInstance.getOID()));
      return qs.findFirstProcessInstance(query);
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
