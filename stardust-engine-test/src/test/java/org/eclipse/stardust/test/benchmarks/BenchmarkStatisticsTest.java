/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.test.benchmarks;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.*;

import javax.xml.namespace.QName;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery.Option;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.query.statistics.api.*;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * Class to test benchmark statistics queries.
 *
 * @author Roland.Stamm
 *
 */
public class BenchmarkStatisticsTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup) 
         {
            @Override
            protected void after()
            {
               logRunningActivityThreads();
      
               tearDownServiceFactory();
      
               logAfterTestMethod();
            }
         };

   public final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   public static final TestServiceFactory setupServiceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BenchmarksModel");

   @ClassRule
   public static final TestRule classRuleChain = RuleChain.outerRule(testClassSetup)
      .around(setupServiceFactory);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private Map<String, BusinessObject> businessObjects;

   private static final String MODEL_PREFIX = "BenchmarksModel";

   private static final String BENCHMARK_PROCESS =
         new QName(MODEL_PREFIX, "BenchmarkedProcess").toString();

   private static final String BENCHMARK_PARENT_PROCESS =
         new QName(MODEL_PREFIX, "BenchmarkedParentProcess").toString();
   private static final String BENCHMARK_SUB_PROCESS =
         new QName(MODEL_PREFIX, "BenchmarkedSubProcess").toString();

   private static final String BENCHMARK_ACTIVITY =
         new QName(MODEL_PREFIX, "BenchmarkedActivity").toString();

   private static final String BENCHMARK_REF = "benchmarksTest.benchmark";

   private static final String qualifiedCategoryBusinessObjectId =
         new QName(MODEL_PREFIX, "BaseCategoryData").toString();
   private static final String qualifiedGroupBusinessObjectId =
         new QName(MODEL_PREFIX, "BaseGroupData").toString();
   private static final String qualifiedGroupWithoutNameBusinessObjectId =
         new QName(MODEL_PREFIX, "BaseGroupWithoutNameData").toString();

   private static final String qualifiedBusinessDateId =
         new QName(MODEL_PREFIX, PredefinedConstants.BUSINESS_DATE).toString();
   
   @BeforeClass
   public static void setup() throws Throwable
   {
      BenchmarkTestUtils.deployBenchmark("benchmarksTest.benchmark", setupServiceFactory);

      StartOptions startOptions_withBenchmark = new StartOptions(Collections.singletonMap(
            PredefinedConstants.BUSINESS_DATE, Calendar.getInstance().getTimeInMillis()), true, BENCHMARK_REF);
      StartOptions startOptions_withoutBenchmark = new StartOptions(null, true);

      // 2 with benchmark
      setupServiceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      setupServiceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      // 1 without benchmark
      setupServiceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withoutBenchmark);

      // 1 with benchmark (total of 3 now)
      setupServiceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      // 1 aborted
      ProcessInstance pi = setupServiceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);
      setupServiceFactory.getAdministrationService().abortProcessInstance(pi.getOID());

      

      Map<String, Serializable> businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(1));
      businessObjectData.put("Name", "CategoryA");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(Integer.valueOf(1),
            Integer.valueOf(2))));

      setupServiceFactory.getWorkflowService().createBusinessObjectInstance(
         qualifiedCategoryBusinessObjectId, businessObjectData);

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(2));
      businessObjectData.put("Name", "CategoryB");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(Integer.valueOf(3))));

      setupServiceFactory.getWorkflowService().createBusinessObjectInstance(
         qualifiedCategoryBusinessObjectId, businessObjectData);

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(1));
      businessObjectData.put("Name", "GroupA");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(Integer.valueOf(1),
            Integer.valueOf(2))));

      setupServiceFactory.getWorkflowService().createBusinessObjectInstance(
         qualifiedGroupBusinessObjectId, businessObjectData);

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(2));
      businessObjectData.put("Name", "GroupB");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(2), Integer.valueOf(3))));

      setupServiceFactory.getWorkflowService().createBusinessObjectInstance(
         qualifiedGroupBusinessObjectId, businessObjectData);
      
      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(111));
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(110))));

      setupServiceFactory.getWorkflowService().createBusinessObjectInstance(
            qualifiedGroupWithoutNameBusinessObjectId, businessObjectData);

      //                      | GroupA | GroupB | CategoryA | CategoryB | GroupWithoutName(111)
      // BaseA                |   x    |        |     x     |           |
      // BaseB                |   x    |    x   |     x     |           |
      // BaseC                |        |    x   |           |    x      |
      // BaseWithoutName(110) |        |        |           |           |       x
      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(1));
      businessObjectData.put("Name", "BaseA");
      businessObjectData.put("Groups", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(1))));
      businessObjectData.put("Categories", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(1))));

      Map<String, Serializable> processData = CollectionUtils.newMap();
      processData.put(PredefinedConstants.BUSINESS_DATE, Calendar.getInstance().getTimeInMillis());
      processData.put("BaseData", (Serializable)businessObjectData);

      startOptions_withBenchmark = new StartOptions(processData, true, BENCHMARK_REF);
      setupServiceFactory.getWorkflowService().startProcess(
            BENCHMARK_PARENT_PROCESS, startOptions_withBenchmark);

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(2));
      businessObjectData.put("Name", "BaseB");
      businessObjectData.put("Groups", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(1), Integer.valueOf(2))));
      businessObjectData.put("Categories", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(1))));

      processData = CollectionUtils.newMap();
      processData.put(PredefinedConstants.BUSINESS_DATE, Calendar.getInstance().getTimeInMillis());
      processData.put("BaseData", (Serializable)businessObjectData);

      startOptions_withBenchmark = new StartOptions(processData, true, BENCHMARK_REF);
      setupServiceFactory.getWorkflowService().startProcess(
            BENCHMARK_PARENT_PROCESS, startOptions_withBenchmark);

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(3));
      businessObjectData.put("Name", "BaseC");
      businessObjectData.put("Groups", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(2))));
      businessObjectData.put("Categories", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(2))));

      processData = CollectionUtils.newMap();
      processData.put(PredefinedConstants.BUSINESS_DATE, Calendar.getInstance().getTimeInMillis());
      processData.put("BaseData", (Serializable)businessObjectData);

      startOptions_withBenchmark = new StartOptions(processData, true, BENCHMARK_REF);
      setupServiceFactory.getWorkflowService().startProcess(
            BENCHMARK_PARENT_PROCESS, startOptions_withBenchmark);

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(110));
      businessObjectData.put("Groups", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(111))));
      
      processData = CollectionUtils.newMap();
      processData.put(PredefinedConstants.BUSINESS_DATE, Calendar.getInstance().getTimeInMillis());
      processData.put("BaseWithoutNameData", (Serializable)businessObjectData);
      
      startOptions_withBenchmark = new StartOptions(processData, true, BENCHMARK_REF);
      setupServiceFactory.getWorkflowService().startProcess(
            BENCHMARK_PARENT_PROCESS, startOptions_withBenchmark);
      
      // Business Object           |  BaseA  |     BaseB   | BaseC | 110 |
      // BenchmarkValue            | On Time | Almost Late | Late  |
      // BenchmarkedParentProcess  |    1    |       1     |   1   |  1  |
      // BenchmarkedSubProcess     |    2    |       2     |   2   |  2  |
            
      runDaemon(setupServiceFactory);
   }
   
   @Before
   public void setupTestCase()
   {
      businessObjects = CollectionUtils.newMap();
      
      String qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseData").toString();
      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      BusinessObjects bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      businessObjects.put("BaseData", bos.get(0));

      qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseGroupData").toString();
      businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));
      
      bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      businessObjects.put("BaseGroupData", bos.get(0));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByProcesses()
   {
      // Query
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessIds(Collections.singleton(BENCHMARK_PROCESS));

      // Do not add state filter to include aborted and completed.
      // query.where(ProcessStateFilter.ALIVE);

      // Only for the selected benchmark.
      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid(serviceFactory)));

      // Only for BUSINESS_DATE today.
      query.where(DataFilter.between(qualifiedBusinessDateId, getCurrentDayStart(),
            getCurrentDayEnd()));

      BenchmarkProcessStatistics stats = (BenchmarkProcessStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);

      BenchmarkCategoryCounts benchmarkCategoryCounts = stats
            .getBenchmarkCategoryCountsForProcess(BENCHMARK_PROCESS);
      Map<Integer, Long> benchmarkCategoryCountMap = benchmarkCategoryCounts
            .getBenchmarkCategoryCount();

      // Counts only exist for one category, because no benchmark process does not have
      // business date.
      Assert.assertEquals(1, benchmarkCategoryCountMap.size());
      // Category 3 has count of 3
      Assert.assertEquals(Long.valueOf(3L), benchmarkCategoryCountMap.get(3));

      Assert.assertEquals(1, stats.getAbortedCountForProcess(BENCHMARK_PROCESS));
      Assert.assertEquals(0, stats.getCompletedCountForProcess(BENCHMARK_PROCESS));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjectsWithPk()
   {
      ProcessDefinition benchmarkProcess = serviceFactory.getQueryService()
            .getProcessDefinition(BENCHMARK_PARENT_PROCESS);

      // Validate BenchmarkProcessStatisticsQuery if primary key values are not passed
      // and no groupBy is specified
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessesAndBusinessObject(Collections.singleton(benchmarkProcess),
                  businessObjects.get("BaseData"), new HashSet<Serializable>(Arrays.asList(
                        Integer.valueOf(1),
                        Integer.valueOf(2))));

      attachDefaultBenchmarkCondition(query);
      
      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);
      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(1)));
      Assert.assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
      Set<String> filterValues = stats.getFilterValues(null);
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB"));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 1), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 2), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 3), is(equalTo(0l)));
   }
   
   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjectsWithPkOnSubProcesses()
   {
      ProcessDefinition benchmarkProcess = serviceFactory.getQueryService()
            .getProcessDefinition(BENCHMARK_SUB_PROCESS);
      
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessesAndBusinessObject(Collections.singleton(benchmarkProcess),
                  businessObjects.get("BaseData"), new HashSet<Serializable>(Arrays.asList(
                        Integer.valueOf(1),
                        Integer.valueOf(2))));

      attachDefaultBenchmarkCondition(query);

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);
      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(1)));
      Assert.assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
      Set<String> filterValues = stats.getFilterValues(null);
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB"));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 1), is(equalTo(2l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 2), is(equalTo(2l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 3), is(equalTo(0l)));
   }
   
   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjectsWithoutName()
   {
      BusinessObject baseWithoutNameData = null;
      BusinessObject baseGroupWithoutNameData = null;

      ProcessDefinition benchmarkProcess = serviceFactory.getQueryService()
            .getProcessDefinition(BENCHMARK_PARENT_PROCESS);

      String qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseWithoutNameData").toString();
      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);

      BusinessObjects bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseWithoutNameData = bos.get(0);

      qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseGroupWithoutNameData").toString();
      businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      
      bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseGroupWithoutNameData = bos.get(0);

      // Validate BenchmarkProcessStatisticsQuery if primary key values are not passed
      // and no groupBy is specified
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessesAndBusinessObject(Collections.singleton(benchmarkProcess),
                  baseWithoutNameData, new HashSet<Serializable>(Arrays.asList(
                        Integer.valueOf(110))),
                  baseGroupWithoutNameData, new HashSet<Serializable>(Arrays.asList(
                        Integer.valueOf(111))));

      attachDefaultBenchmarkCondition(query);

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);
      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(1)));
      Assert.assertThat(groupByValues, hasItem("111"));
      Set<String> filterValues = stats.getFilterValues(null);
      Assert.assertThat(filterValues.size(), is(equalTo(1)));
      Assert.assertThat(filterValues, hasItems("110"));
      Assert.assertThat(stats.getBenchmarkCategoryCount("111", "110", 0), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("111", "BaseA", 0), is(equalTo(0l)));

      Assert.assertThat(stats.getBenchmarkCategoryCount("111", null, 0), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 0), is(equalTo(1l)));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjects()
   {
      ProcessDefinition benchmarkProcess = serviceFactory.getQueryService()
            .getProcessDefinition(BENCHMARK_PARENT_PROCESS);

      // Validate BenchmarkProcessStatisticsQuery if primary key values are not passed
      // and no groupBy is specified
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessesAndBusinessObject(Collections.singleton(benchmarkProcess),
                  businessObjects.get("BaseData"), /*new HashSet<Serializable>(Arrays.asList(
                        Integer.valueOf(1),
                        Integer.valueOf(2),
                        Integer.valueOf(3)))*/ Collections.<Serializable>emptySet());

      attachDefaultBenchmarkCondition(query);

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);
      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(1)));
      Assert.assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
      Set<String> filterValues = stats.getFilterValues(null);
      Assert.assertThat(filterValues.size(), is(equalTo(3)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB", "BaseC"));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 1), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 2), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 3), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseD", 0), is(equalTo(0l)));

      // Validate BenchmarkProcessStatisticsQuery if no primary key values are passed but
      // a groupBy is set
      query = BenchmarkProcessStatisticsQuery.forProcessesAndBusinessObject(
            Collections.<ProcessDefinition>emptySet(),
            businessObjects.get("BaseData"), Collections.<Serializable>emptySet(),
            businessObjects.get("BaseGroupData"), Collections.<Serializable>emptySet());

      attachDefaultBenchmarkCondition(query);

      stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);
      
      groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(2)));
      Assert.assertThat(groupByValues, hasItems("GroupA", "GroupB"));
      filterValues = stats.getFilterValues("GroupA");
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB"));
      filterValues = stats.getFilterValues("GroupB");
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseB", "BaseC"));

      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 1), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 3), is(equalTo(3l)));

      // group count
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 1), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 3), is(equalTo(3l)));

      // total count
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 1), is(equalTo(3l)));
      // Base B which is used in exactly 3 process instances is part of GroupA and GroupB;
      // So the final count is not 6 but 3
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 3), is(equalTo(3l)));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjectsWithGroupBy()
   {
      // Validate BenchmarkProcessStatisticsQuery if primary key values are passed
      // for the filter Business Object
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery.forProcessesAndBusinessObject(
            Collections.<ProcessDefinition>emptySet(),
            businessObjects.get("BaseData"), new HashSet<Serializable>(Arrays.asList(1, 2)),
            businessObjects.get("BaseGroupData"), Collections.<Serializable>emptySet());
      
      attachDefaultBenchmarkCondition(query);

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);

      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(2)));
      Assert.assertThat(groupByValues, hasItems("GroupA", "GroupB"));
      Set<String> filterValues = stats.getFilterValues("GroupA");
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB"));
      filterValues = stats.getFilterValues("GroupB");
      Assert.assertThat(filterValues.size(), is(equalTo(1)));
      Assert.assertThat(filterValues, hasItems("BaseB"));

      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 1), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 3), is(equalTo(0l)));

      // group count
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 1), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 3), is(equalTo(0l)));

      // total count
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 1), is(equalTo(3l)));
      // Base B which is used in exactly 3 process instances is part of GroupA and GroupB;
      // So the final count is not 6 but 3
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 3), is(equalTo(0l)));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjectsWithGroupByAndPk()
   {
      // Validate BenchmarkProcessStatisticsQuery if primary key values are passed
      // for the filter and groupBy Business Object
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery.forProcessesAndBusinessObject(
            Collections.<ProcessDefinition>emptySet(),
            businessObjects.get("BaseData"), new HashSet<Serializable>(Arrays.asList(1, 2)),
            businessObjects.get("BaseGroupData"), new HashSet<Serializable>(Arrays.asList(1)));
      
      attachDefaultBenchmarkCondition(query);

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);

      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(1)));
      Assert.assertThat(groupByValues, hasItems("GroupA"));
      Set<String> filterValues = stats.getFilterValues("GroupA");
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB"));
      filterValues = stats.getFilterValues("GroupB");
      Assert.assertThat(filterValues.size(), is(equalTo(0)));

      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 1), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 3), is(equalTo(0l)));

      // group count
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 1), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 3), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 3), is(equalTo(0l)));

      // total count
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 1), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 2), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 3), is(equalTo(0l)));
   }

   @Test
   public void queryActivityBenchmarkStatistics()
   {
      // Query
      BenchmarkActivityStatisticsQuery query = BenchmarkActivityStatisticsQuery
            .forProcessId(BENCHMARK_PROCESS);

      // Do not add state filter to include aborted and completed.
      // query.where(ActivityStateFilter.ALIVE);

      // Only for the selected benchmark.
      query.where(BenchmarkActivityStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid(serviceFactory)));

      // Only for BUSINESS_DATE today.
      query.where(DataFilter.between(qualifiedBusinessDateId, getCurrentDayStart(),
            getCurrentDayEnd()));

      BenchmarkActivityStatistics stats = (BenchmarkActivityStatistics) serviceFactory
            .getQueryService().getAllActivityInstances(query);

      BenchmarkCategoryCounts benchmarkCategoryCounts = stats
            .getBenchmarkCategoryCountsForActivity(BENCHMARK_PROCESS, BENCHMARK_ACTIVITY);
      Map<Integer, Long> benchmarkCategoryCountMap = benchmarkCategoryCounts
            .getBenchmarkCategoryCount();

      // Counts only exist for one category, because no benchmark process does not have
      // business date.
      Assert.assertEquals(1, benchmarkCategoryCountMap.size());
      // Category 3 has count of 3
      Assert.assertEquals(Long.valueOf(3L), benchmarkCategoryCountMap.get(3));

      //
      Assert.assertEquals(1,
            stats.getAbortedCountForActivity(BENCHMARK_PROCESS, BENCHMARK_ACTIVITY));
      Assert.assertEquals(0,
            stats.getCompletedCountForActivity(BENCHMARK_PROCESS, BENCHMARK_ACTIVITY));
   }

   private static void runDaemon(ServiceFactory serviceFactory)
   {
      // Daemon execution
      serviceFactory.getAdministrationService().startDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);

      DaemonExecutionState state = serviceFactory.getAdministrationService()
            .getDaemon(AdministrationService.BENCHMARK_DAEMON, false)
            .getDaemonExecutionState();

      assertEquals(DaemonExecutionState.OK, state);
      
      // Wait for the benchmark.dameon until it has computed all benchmarkValues
      QueryService qService = serviceFactory.getQueryService();
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid(serviceFactory)));
      query.where(ProcessInstanceQuery.BENCHMARK_VALUE.lessThan(1));
      query.where(DataFilter.in("BaseData", "Ident", Arrays.asList(1, 2, 3)));
      
      long count = 1; 
      while(count > 0)
      {
         count = qService.getProcessInstancesCount(query);
         try
         {
            Thread.sleep(500);
         }
         catch (InterruptedException e)
         {
         }
      }
      
      serviceFactory.getAdministrationService().stopDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);
   }

   private static Long getDeployedBenchmarkOid(ServiceFactory serviceFactory)
   {
      DeployedRuntimeArtifacts runtimeArtifacts = serviceFactory.getQueryService()
            .getRuntimeArtifacts(
                  DeployedRuntimeArtifactQuery.findActive(BENCHMARK_REF,
                        BenchmarkDefinitionArtifactType.TYPE_ID, new Date()));
      if (runtimeArtifacts != null && runtimeArtifacts.size() > 0)
      {
         return runtimeArtifacts.get(0).getOid();
      }
      Assert.fail("No deployed runtime artifact found.");
      return 0L;
   }

   private static Serializable getCurrentDayEnd()
   {
      Calendar now = Calendar.getInstance();

      now.set(Calendar.HOUR_OF_DAY, 23);
      now.set(Calendar.MINUTE, 59);
      now.set(Calendar.MILLISECOND, 0);

      return now.getTime();// .getTime();//.getTime();
   }

   private static Serializable getCurrentDayStart()
   {
      Calendar now = Calendar.getInstance();

      now.set(Calendar.HOUR_OF_DAY, 0);
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.MILLISECOND, 0);

      return now.getTime();// .getTime();//.getTime();
   }
   
   private void attachDefaultBenchmarkCondition(BenchmarkProcessStatisticsQuery query)
   {
      // Only for the selected benchmark.
      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid(serviceFactory)));

      // Only for BUSINESS_DATE today.
      query.where(DataFilter.between(qualifiedBusinessDateId, getCurrentDayStart(),
            getCurrentDayEnd()));
   }
}
