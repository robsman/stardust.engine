/**********************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.datacluster;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.SpawnOptions.SpawnMode;
import org.eclipse.stardust.test.api.setup.DataClusterTestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.Log4jLogMessageBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains data cluster tests using spawn processes in states 'Halting' and
 * 'Halted'.
 * </p>
 *
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class SpawnProcessDataClusterTest
{
   private static final String DATA_CLUSTER_CONFIG = "data-cluster-enable-states.xml";

   public static final String MODEL_NAME = "DataClusterModel";

   private static final String STRING_DATA_VAL = "TestValue";

   private static final String LONG_DATA = "aLong";

   private static final String INT_DATA = "anInt";

   private static final String STRING_DATA = "aString";

   private static final String STRING_DATA2 = "aString2";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final DataClusterTestClassSetup testClassSetup = new DataClusterTestClassSetup(
         DATA_CLUSTER_CONFIG, ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private WorkflowService wfService;

   private static final String PROCESS_DEF_ID_1 = "ProcessDefinition1";

   private static final String PROCESS_DEF_ID_6 = "ProcessDefinition6";

   private QueryService queryService;

   @Before
   public void before() throws Exception
   {
      GlobalParameters.globals().set("Carnot.Engine.Tuning.Query.EvaluationProfile",
            "dataClusters");
      GlobalParameters.globals().set(
            "Infinity.Engine.Tuning.Query.DescriptorPrefetchUseDataCluster", "true");
      GlobalParameters.globals().set("AuditTrail.UsePreparedStatements", "true");
      GlobalParameters.globals().set(
            "Carnot.Engine.Tuning.DB.slowStatementTracingThreshold", "0");

      wfService = serviceFactory.getWorkflowService();
      queryService = serviceFactory.getQueryService();
   }

   @Test
   public void testDataClusterWithEnableStateHalting() throws Exception
   {
      ProcessInstance pi = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activate(oid);
      wfService.suspendToDefaultPerformer(oid, null, datas);

      DataCopyOptions dataCopyOptions = new DataCopyOptions(true, null, null, true);
      SpawnOptions spawnOptions = new SpawnOptions(null, SpawnMode.HALT, null,
            dataCopyOptions);
      wfService.spawnPeerProcessInstance(pi.getOID(), PROCESS_DEF_ID_1, spawnOptions);

      ProcessInstanceQuery piQuery = ProcessInstanceQuery
            .findInState(ProcessInstanceState.Halting);
      piQuery.where(DataFilter.like(STRING_DATA, STRING_DATA_VAL));
      Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.INFO);
      barrier.registerWithLog4j();
      ProcessInstances pis = queryService.getAllProcessInstances(piQuery);
      assertEquals("Should find 1 halting process ", 1, pis.getSize());
      
      boolean hasLogMsg = false;
      List<String> logMessages = barrier.getLogMessages();
      for (String logMessage : logMessages)
      {
         if (logMessage.indexOf("INNER JOIN PUBLIC.dv_mqt01") > 0)
         {
            hasLogMsg = true;
         }
      }
      assertTrue(hasLogMsg);
   }

   @Test
   public void testDataClusterWithEnableStateHalted() throws Exception
   {
      ProcessInstance pi = wfService.startProcess(PROCESS_DEF_ID_6, null, true);
      ActivityInstance startAI = wfService
            .activateNextActivityInstanceForProcessInstance(pi.getOID());
      assertThat(startAI.getActivity().getId(), is("Start"));
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA2, STRING_DATA_VAL);
      startAI = wfService.suspendToDefaultPerformer(startAI.getOID(), null, datas);
      wfService.activate(startAI.getOID());

      SpawnOptions spawnOptions = new SpawnOptions(null, SpawnMode.HALT, null, null);
      ProcessInstance peer = wfService.spawnPeerProcessInstance(pi.getOID(),
            PROCESS_DEF_ID_1, spawnOptions);
      ProcessInstanceStateBarrier.instance().await(peer.getOID(),
            ProcessInstanceState.Active);
      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Halted);

      ProcessInstanceQuery piQuery = ProcessInstanceQuery
            .findInState(ProcessInstanceState.Halted);
      piQuery.where(DataFilter.like(STRING_DATA2, STRING_DATA_VAL));
      Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.INFO);
      barrier.registerWithLog4j();
      ProcessInstances pis = queryService.getAllProcessInstances(piQuery);
      assertEquals("Should find 1 halted process", 1, pis.getSize());

      boolean hasLogMsg = false;
      List<String> logMessages = barrier.getLogMessages();
      for (String logMessage : logMessages)
      {
         if (logMessage.indexOf("INNER JOIN PUBLIC.dv_mqt02") > 0)
         {
            hasLogMsg = true;
         }
      }
      assertTrue(hasLogMsg);
   }

   private long findFirstAliveActivityInstanceOid(String processID)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(processID);
      final ActivityInstance ai = queryService.findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }
}