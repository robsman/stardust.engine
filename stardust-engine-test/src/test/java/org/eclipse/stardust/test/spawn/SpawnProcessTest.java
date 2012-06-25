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
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
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
 * TODO javadoc
 * </p>
 * 
 * @author Roland.Stamm
 * @version $Revision$
 */
public class SpawnProcessTest
{
   /* package-private */ static final String MODEL_NAME = "SpawnProcessModel";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1SyncSeperateCopy",
            null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "InputData2", true, null);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1SyncSeperateCopy",
            null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "InputData2", true, null);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1SyncSeperateCopy",
            null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "newValue");
      wfs.suspendToDefaultPerformer(ai.getOID(), null, map);

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "InputData2", true, null);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1SyncSeperateCopy",
            null, true);

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
            ai.getProcessInstanceOID(), "InputData1", true, map2);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());

      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      // Main assert
      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      Serializable inDataValue = wfs.getInDataValue(spawnAi.getOID(), null, "Primitive1");
      Assert.assertEquals("notNewValue", inDataValue);

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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1SyncSeperateCopy",
            null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      wfs.suspendToDefaultPerformer(ai.getOID(), null, null);

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(
            ai.getProcessInstanceOID(), "Interrupt", true, null);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
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
   public void testShowDocumentData()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1SyncSeperateCopy",
            null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      wfs.suspendToDefaultPerformer(ai.getOID(), null, null);

      Map<String, Serializable > map = new HashMap<String, Serializable>();
      map.put("Doc_1", getDoc(sf, pi));
      // Spawn process
      wfs.spawnSubprocessInstance(ai.getProcessInstanceOID(), "ShowDoc", true, map);
   }

   // ************************************
   // **             ASYNC              **
   // ************************************

   @Test
   public void testCompleteSpawnProcessFromAsyncSubprocess() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
      QueryService qs = sf.getQueryService();

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1Async", null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(subprocessInstance.getOID());
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
            ai.getProcessInstanceOID(), "InputData2", true, null);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1Async", null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(subprocessInstance.getOID());
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
            ai.getProcessInstanceOID(), "InputData2", true, null);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1Async", null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(subprocessInstance.getOID());
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
            ai.getProcessInstanceOID(), "InputData2", true, null);

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

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
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

      ProcessInstance pi = wfs.startProcess("StartInputSubprocess1Async", null, true);

      // Get ai from subprocess.
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Assert.assertNull("Async subprocesses have no hierarchy relation.", ai);

      // Query for ai from async subprocess
      ProcessInstance subprocessInstance = null;
      if (ai == null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess("InputData1");
         subprocessInstance = qs.findFirstProcessInstance(query);

         ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(subprocessInstance.getOID());
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
            ai.getProcessInstanceOID(), "InputData2", true, null);

      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnSubprocessInstance.getOID());
      
      ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(spawnSubprocessInstance.getOID());
      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      query.where(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      ActivityInstance spawnAi = qs.findFirstActivityInstance(query);

      Assert.assertNotNull(
            "Interactive activityInstance in state suspended exists in spawned process.",
            spawnAi);

      Object processAttachments = wfs.getInDataPath(spawnAi.getOID(), "PROCESS_ATTACHMENTS");
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
