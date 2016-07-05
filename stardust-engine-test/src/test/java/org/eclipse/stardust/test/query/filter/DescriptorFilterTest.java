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
   public void testDescriptorLikeFilter()
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
   public void testDescriptorPrimitivaDataLikeFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessPrimitiveA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "PrimitiveOutA", "Test10-1");

      ProcessInstance processB1 = wfService.startProcess("ProcessPrimitiveB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "PrimitiveOutA", "Test10-1");

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.like("PrimitiveA", "Test10%"));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());
   }

   @Test
   public void testDescriptorLikeFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", "Test11-2");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", "Test10-1");

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
   public void testDescriptorIsEqualFilter()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", "Test11-2");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", "Test10-1");

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
   public void testDescriptorIsEqualFilterCaseSensitive()
   {
      ProcessInstance processA1 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", "Test10-1");

      ProcessInstance processB1 = wfService
            .startProcess("ProcessDefinitionB", null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", "Test11-2");

      ProcessInstance processA2 = wfService
            .startProcess("ProcessDefinitionA", null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", "Test10-1");

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
   public void testDescriptorBetweenFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 6);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("A", 3, 6));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.between("B", 5, 8));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.between("A", 3, 6));
      addOrTerm.add(DescriptorFilter.between("B", 5, 8));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testDescriptorGreaterOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 6);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("A", 3));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterOrEqual("B", 6));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterOrEqual("A", 3));
      addOrTerm.add(DescriptorFilter.greaterOrEqual("B", 6));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testDescriptorGreaterThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);
      // wfService.setOutDataPath(processA1.getOID(), "OutB", 3);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      // wfService.setOutDataPath(processA2.getOID(), "OutA", 4);
      wfService.setOutDataPath(processA2.getOID(), "OutB", 6);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutA", 5);
      // wfService.setOutDataPath(processB1.getOID(), "OutB", 7);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      // wfService.setOutDataPath(processB2.getOID(), "OutA", 5);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("A", 3));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.greaterThan("B", 5));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.greaterThan("A", 3));
      addOrTerm.add(DescriptorFilter.greaterThan("B", 5));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testDescriptorInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 6);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      List<Integer> values = CollectionUtils.newList();
      values.add(4);
      values.add(5);
      values.add(6);
      values.add(7);
      values.add(8);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("A", values));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.in("B", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.in("A", values));
      addOrTerm.add(DescriptorFilter.in("B", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testDescriptorNotInFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 6);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      List<Integer> values = CollectionUtils.newList();
      values.add(1);
      values.add(2);
      values.add(3);
      values.add(8);
      values.add(9);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("A", values));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.notIn("B", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.notIn("A", values));
      addOrTerm.add(DescriptorFilter.notIn("B", values));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   // @Test
   // public void testDescriptorNotAnyOfFilter()
   // {
   // List<Integer> values1 = CollectionUtils.newList();
   // values1.add(1);
   // values1.add(2);
   // values1.add(3);
   // values1.add(4);
   // values1.add(5);
   //
   // List<Integer> values2 = CollectionUtils.newList();
   // values1.add(5);
   // values1.add(6);
   // values1.add(7);
   // values1.add(8);
   // values1.add(9);
   //
   // ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionAList", null,
   // true);
   // wfService.setOutDataPath(processA1.getOID(), "OutA", values1);
   //
   // ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBList", null,
   // true);
   // wfService.setOutDataPath(processB1.getOID(), "OutB", values2);
   //
   // ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionAList", null,
   // true);
   // wfService.setOutDataPath(processA2.getOID(), "OutA", values1);
   //
   // ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBList", null,
   // true);
   // wfService.setOutDataPath(processB2.getOID(), "OutB", values2);
   //
   // values1 = CollectionUtils.newList();
   // values1.add(2);
   // values1.add(3);
   // values1.add(4);
   //
   // values2 = CollectionUtils.newList();
   // values2.add(6);
   // values2.add(7);
   // values2.add(8);
   //
   // ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
   // query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
   // query.where(DescriptorFilter.notAnyOf("A", values1));
   // ProcessInstances processInstances = queryService.getAllProcessInstances(query);
   // assertEquals(2, processInstances.getSize());
   //
   // query = ProcessInstanceQuery.findAlive();
   // query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
   // query.where(DescriptorFilter.notAnyOf("B", values2));
   // processInstances = queryService.getAllProcessInstances(query);
   // assertEquals(2, processInstances.getSize());

   // query = ProcessInstanceQuery.findAlive();
   // query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
   // FilterAndTerm filter = query.getFilter();
   // FilterOrTerm addOrTerm = filter.addOrTerm();
   // addOrTerm.add(DescriptorFilter.notAnyOf("A", values1));
   // addOrTerm.add(DescriptorFilter.notAnyOf("B", values2));
   // processInstances = queryService.getAllProcessInstances(query);
   // assertEquals(0, processInstances.getSize());
   // }

   @Test
   public void testDescriptorLessOrEqualFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 6);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("A", 5));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessOrEqual("B", 7));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessOrEqual("A", 5));
      addOrTerm.add(DescriptorFilter.lessOrEqual("B", 7));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testDescriptorLessThanFilter()
   {
      ProcessInstance processA1 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA1.getOID(), "OutA", 4);

      ProcessInstance processB1 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB1.getOID(), "OutB", 6);

      ProcessInstance processA2 = wfService.startProcess("ProcessDefinitionANumber",
            null, true);
      wfService.setOutDataPath(processA2.getOID(), "OutA", 5);

      ProcessInstance processB2 = wfService.startProcess("ProcessDefinitionBNumber",
            null, true);
      wfService.setOutDataPath(processB2.getOID(), "OutB", 7);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("A", 6));
      ProcessInstances processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      query.where(DescriptorFilter.lessThan("B", 8));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(2, processInstances.getSize());

      query = ProcessInstanceQuery.findAlive();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter();
      FilterOrTerm addOrTerm = filter.addOrTerm();
      addOrTerm.add(DescriptorFilter.lessThan("A", 6));
      addOrTerm.add(DescriptorFilter.lessThan("B", 8));
      processInstances = queryService.getAllProcessInstances(query);
      assertEquals(4, processInstances.getSize());
   }

   @Test
   public void testDescriptorNotEqualFilter()
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

}