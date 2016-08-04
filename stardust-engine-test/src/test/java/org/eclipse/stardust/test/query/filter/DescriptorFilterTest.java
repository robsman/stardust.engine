/**********************************************************************************
 * Copyright (c) 2015, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.query.filter;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains functional tests for using DescriptorFilter.
 * </p>
 *
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class DescriptorFilterTest
{
   public static final String MODEL_NAME = "DescriptorFilterModel";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   // @ClassRule
   // public static final DataClusterTestClassSetup testClassSetup = new
   // DataClusterTestClassSetup(
   // "descriptor-data-cluster.xml", ADMIN_USER_PWD_PAIR,
   // ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private WorkflowService wfService;

   private QueryService queryService;

   @Before
   public void before() throws Exception
   {
      wfService = serviceFactory.getWorkflowService();
      queryService = serviceFactory.getQueryService();
   }

   @After
   public void tearDown() throws Exception
   {}

   @Test
   public void testPiDescriptorLikeFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-2");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("A", "Test10%"));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("B", "Test11%"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.like("A", "Test10%"));
      addOrTerm.add(DescriptorFilter.like("B", "Test11%"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorPrimitivaDataLikeFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessPrimitiveA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "PrimitiveOutA", "Test10-1");

      ProcessInstance processB1 = wfService.startProcess("ProcessPrimitiveB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "PrimitiveOutA", "Test10-2");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("PrimitiveA", "Test10%"));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorLikeFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-2");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("A", "test10%", true));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(0, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("B", "Test11%", true));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.like("A", "Test10%", false));
      addOrTerm.add(DescriptorFilter.like("B", "Test11%", false));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorIsEqualFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-2");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("A", "Test10-1", true));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("B", "Test11-2", true));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.isEqual("A", "Test10-1", false));
      addOrTerm.add(DescriptorFilter.isEqual("B", "Test11-2", false));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorIsEqualFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-2");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("A", "test10-1", true));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(0, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("B", "Test11-2", true));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.isEqual("A", "Test10-1", false));
      addOrTerm.add(DescriptorFilter.isEqual("B", "Test11-2", false));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorBetweenFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstance processB3 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB3.getOID(), "OutA", 2);

      ProcessInstance processB4 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB4.getOID(), "OutB", 9);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("ANumber", 3, 6));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("BNumber", 5, 8));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.between("ANumber", 3, 6));
      addOrTerm.add(DescriptorFilter.between("BNumber", 5, 8));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorGreaterOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstance processB3 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB3.getOID(), "OutA", 2);

      ProcessInstance processB4 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB4.getOID(), "OutB", 9);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("ANumber", 3));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("BNumber", 6));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(3, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterOrEqual("ANumber", 3));
      addOrTerm.add(DescriptorFilter.greaterOrEqual("BNumber", 6));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(5, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorGreaterThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("ANumber", 3));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("BNumber", 5));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterThan("ANumber", 3));
      addOrTerm.add(DescriptorFilter.greaterThan("BNumber", 5));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(3, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      List<Integer> values = CollectionUtils.newList();
      values.add(4);
      values.add(5);
      values.add(6);
      values.add(7);
      values.add(8);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("ANumber", values));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("BNumber", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.in("ANumber", values));
      addOrTerm.add(DescriptorFilter.in("BNumber", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(3, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorNotInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      List<Integer> values = CollectionUtils.newList();
      values.add(1);
      values.add(2);
      values.add(3);
      values.add(8);
      values.add(9);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("ANumber", values));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("BNumber", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.notIn("ANumber", values));
      addOrTerm.add(DescriptorFilter.notIn("BNumber", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(3, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorLessOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("ANumber", 3));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("BNumber", 5));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessOrEqual("ANumber", 5));
      addOrTerm.add(DescriptorFilter.lessOrEqual("BNumber", 7));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorLessThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("ANumber", 3));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("BNumber", 5));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessThan("ANumber", 6));
      addOrTerm.add(DescriptorFilter.lessThan("BNumber", 8));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testPiDescriptorNotEqualFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "TestA123");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", "TestB123");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", "TestA123");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "TestB123");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notEqual("A", "TestB123"));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notEqual("B", "TestA123"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.notEqual("A", "TestB123"));
      addOrTerm.add(DescriptorFilter.notEqual("B", "TestA123"));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testAiDescriptorLikeFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-2");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("A", "Test10%"));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("B", "Test11%"));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.like("A", "Test10%"));
      addOrTerm.add(DescriptorFilter.like("B", "Test11%"));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorPrimitivaDataLikeFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessPrimitiveA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "PrimitiveOutA", "Test10-1");

      ProcessInstance processB1 = wfService.startProcess("ProcessPrimitiveB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "PrimitiveOutA", "Test10-2");

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("PrimitiveA", "Test10%"));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorLikeFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-2");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("A", "test10%", true));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(0, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("B", "Test11%", true));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.like("A", "Test10%", false));
      addOrTerm.add(DescriptorFilter.like("B", "Test11%", false));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorIsEqualFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-2");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("A", "Test10-1", true));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("B", "Test11-2", true));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.isEqual("A", "Test10-1", false));
      addOrTerm.add(DescriptorFilter.isEqual("B", "Test11-2", false));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorIsEqualFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-2");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("A", "test10-1", true));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(0, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("B", "Test11-2", true));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.isEqual("A", "Test10-1", false));
      addOrTerm.add(DescriptorFilter.isEqual("B", "Test11-2", false));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorBetweenFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstance processB3 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB3.getOID(), "OutA", 2);

      ProcessInstance processB4 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB4.getOID(), "OutB", 9);

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("ANumber", 3, 6));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("BNumber", 5, 8));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.between("ANumber", 3, 6));
      addOrTerm.add(DescriptorFilter.between("BNumber", 5, 8));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorGreaterOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstance processB3 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB3.getOID(), "OutA", 2);

      ProcessInstance processB4 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB4.getOID(), "OutB", 9);

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("ANumber", 3));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("BNumber", 6));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(3, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterOrEqual("ANumber", 3));
      addOrTerm.add(DescriptorFilter.greaterOrEqual("BNumber", 6));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(5, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorGreaterThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("ANumber", 3));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("BNumber", 5));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterThan("ANumber", 3));
      addOrTerm.add(DescriptorFilter.greaterThan("BNumber", 5));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(3, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      List<Integer> values = CollectionUtils.newList();
      values.add(4);
      values.add(5);
      values.add(6);
      values.add(7);
      values.add(8);

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("ANumber", values));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("BNumber", values));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.in("ANumber", values));
      addOrTerm.add(DescriptorFilter.in("BNumber", values));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(3, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorNotInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      List<Integer> values = CollectionUtils.newList();
      values.add(1);
      values.add(2);
      values.add(3);
      values.add(8);
      values.add(9);

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("ANumber", values));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("BNumber", values));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.notIn("ANumber", values));
      addOrTerm.add(DescriptorFilter.notIn("BNumber", values));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(3, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorLessOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("ANumber", 3));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("BNumber", 5));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessOrEqual("ANumber", 5));
      addOrTerm.add(DescriptorFilter.lessOrEqual("BNumber", 7));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorLessThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("ANumber", 3));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("BNumber", 5));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessThan("ANumber", 6));
      addOrTerm.add(DescriptorFilter.lessThan("BNumber", 8));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testAiDescriptorNotEqualFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "TestA123");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", "TestB123");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", "TestA123");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "TestB123");

      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notEqual("A", "TestB123"));
      ActivityInstances activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notEqual("B", "TestA123"));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(2, activityInstances.getSize());

      query = ActivityInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.notEqual("A", "TestB123"));
      addOrTerm.add(DescriptorFilter.notEqual("B", "TestA123"));
      activityInstances = queryService.getAllActivityInstances(query);
      assertEquals(4, activityInstances.getSize());
   }

   @Test
   public void testWlDescriptorLikeFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-2");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("A", "Test10%"));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("B", "Test11%"));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.like("A", "Test10%"));
      addOrTerm.add(DescriptorFilter.like("B", "Test11%"));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorPrimitivaDataLikeFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessPrimitiveA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "PrimitiveOutA", "Test10-1");

      ProcessInstance processB1 = wfService.startProcess("ProcessPrimitiveB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "PrimitiveOutA", "Test10-2");

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("PrimitiveA", "Test10%"));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());
   }


   @Test
   public void testWlDescriptorLikeFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-2");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("A", "test10%", true));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(0, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("B", "Test11%", true));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.like("A", "Test10%", false));
      addOrTerm.add(DescriptorFilter.like("B", "Test11%", false));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorIsEqualFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-2");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("A", "Test10-1", true));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("B", "Test11-2", true));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.isEqual("A", "Test10-1", false));
      addOrTerm.add(DescriptorFilter.isEqual("B", "Test11-2", false));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorIsEqualFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", "Test11-2");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "Test11-2");

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("A", "test10-1", true));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(0, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.isEqual("B", "Test11-2", true));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.isEqual("A", "Test10-1", false));
      addOrTerm.add(DescriptorFilter.isEqual("B", "Test11-2", false));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorBetweenFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstance processB3 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB3.getOID(), "OutA", 2);

      ProcessInstance processB4 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB4.getOID(), "OutB", 9);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("ANumber", 3, 6));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("BNumber", 5, 8));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.between("ANumber", 3, 6));
      addOrTerm.add(DescriptorFilter.between("BNumber", 5, 8));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorGreaterOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstance processB3 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB3.getOID(), "OutA", 2);

      ProcessInstance processB4 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB4.getOID(), "OutB", 9);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("ANumber", 3));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("BNumber", 6));
      activityInstances = wfService.getWorklist(query);
      assertEquals(3, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterOrEqual("ANumber", 3));
      addOrTerm.add(DescriptorFilter.greaterOrEqual("BNumber", 6));
      activityInstances = wfService.getWorklist(query);
      assertEquals(5, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorGreaterThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("ANumber", 3));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("BNumber", 5));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterThan("ANumber", 3));
      addOrTerm.add(DescriptorFilter.greaterThan("BNumber", 5));
      activityInstances = wfService.getWorklist(query);
      assertEquals(3, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      List<Integer> values = CollectionUtils.newList();
      values.add(4);
      values.add(5);
      values.add(6);
      values.add(7);
      values.add(8);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("ANumber", values));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("BNumber", values));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.in("ANumber", values));
      addOrTerm.add(DescriptorFilter.in("BNumber", values));
      activityInstances = wfService.getWorklist(query);
      assertEquals(3, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorNotInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      List<Integer> values = CollectionUtils.newList();
      values.add(1);
      values.add(2);
      values.add(3);
      values.add(8);
      values.add(9);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("ANumber", values));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("BNumber", values));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.notIn("ANumber", values));
      addOrTerm.add(DescriptorFilter.notIn("BNumber", values));
      activityInstances = wfService.getWorklist(query);
      assertEquals(3, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorLessOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("ANumber", 3));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("BNumber", 5));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessOrEqual("ANumber", 5));
      addOrTerm.add(DescriptorFilter.lessOrEqual("BNumber", 7));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorLessThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 2);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutA", 1);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 2);

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("ANumber", 3));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("BNumber", 5));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessThan("ANumber", 6));
      addOrTerm.add(DescriptorFilter.lessThan("BNumber", 8));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }

   @Test
   public void testWlDescriptorNotEqualFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "TestA123");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", "TestB123");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", "TestA123");

      ProcessInstance processB2 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", "TestB123");

      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notEqual("A", "TestB123"));
      Worklist activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notEqual("B", "TestA123"));
      activityInstances = wfService.getWorklist(query);
      assertEquals(2, activityInstances.getCumulatedSize());

      query = WorklistQuery.findCompleteWorklist();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.notEqual("A", "TestB123"));
      addOrTerm.add(DescriptorFilter.notEqual("B", "TestA123"));
      activityInstances = wfService.getWorklist(query);
      assertEquals(4, activityInstances.getCumulatedSize());
   }
}