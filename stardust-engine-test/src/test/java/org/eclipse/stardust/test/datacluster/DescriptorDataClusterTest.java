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
package org.eclipse.stardust.test.datacluster;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.DataClusterTestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains functional tests for using descriptors and data clusters.
 * </p>
 * 
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class DescriptorDataClusterTest
{
   public static final String MODEL_NAME = "DescriptorDataClusterModel";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final DataClusterTestClassSetup testClassSetup = new DataClusterTestClassSetup(
         "descriptor-data-cluster.xml", ADMIN_USER_PWD_PAIR,
         ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
//   @ClassRule
//   public static final TestClassSetup testClassSetup = new TestClassSetup(
//         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private WorkflowService wfService;

   private static final String PROCESS_DEF_ID_1 = "ProcessDefinition1";

   private static final String PROCESS_DEF_ID_2 = "ProcessDefinition2";


   private QueryService queryService;

   @Before
   public void before() throws Exception
   {
      GlobalParameters.globals().set("Carnot.Engine.Tuning.Query.EvaluationProfile",
            "dataClusters");
      wfService = serviceFactory.getWorkflowService();
      queryService = serviceFactory.getQueryService();
   }

   @Test
   public void testDescriptorSearch()
   {
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      wfService.setOutDataPath(process.getOID(), "OutA", "testA");
      wfService.setOutDataPath(process.getOID(), "OutB", "testB");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("DataA", "%"));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("DataB", "%"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());
      
      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("DataA", "testA"));
      query.getFilter().add(DataFilter.like("DataB", "testB"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());
   }
   
   /**
    * <p>
    * See also <a href="https://www.csa.sungard.com/jira/browse/CRNT-39359">CRNT-39359</a>
    * </p>
    */
   @Test
   public void testStructDataDescriptorSearch()
   {
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      wfService.setOutDataPath(process.getOID(), "OutA", "testA");
      wfService.setOutDataPath(process.getOID(), "OutB", "testB");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("StructData", "A", "testA"));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("StructData", "B", "testB"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());
      
      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("StructData", "A", "testA"));
      query.getFilter().add(DataFilter.like("StructData", "B", "testB"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());
   }
   
   @Test
   public void testStructDataDescriptorSearchOnlyDC()
   {
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      wfService.setOutDataPath(process.getOID(), "OutA", "testA");
      wfService.setOutDataPath(process.getOID(), "OutC", "testC");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("StructData", "A", "testA"));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("StructData", "C", "testC"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());
      
      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DataFilter.like("StructData", "A", "testA"));
      query.getFilter().add(DataFilter.like("StructData", "C", "testC"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, processInstances.getSize());
   }

}