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
import static org.junit.Assert.assertEquals;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsCollectionContaining.*;

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
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BenchmarksModel");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private Map<String, BusinessObject> businessObjects;

   private static final String MODEL_PREFIX = "BenchmarksModel";

   private static final String BENCHMARK_PROCESS =
         new QName(MODEL_PREFIX, "BenchmarkedProcess").toString();

   private static final String BENCHMARK_PARENT_PROCESS =
         new QName(MODEL_PREFIX, "BenchmarkedParentProcess").toString();

   private static final String BENCHMARK_ACTIVITY =
         new QName(MODEL_PREFIX, "BenchmarkedActivity").toString();

   private static final String BENCHMARK_REF = "benchmarksTest.benchmark";

   private static final String qualifiedCategoryBusinessObjectId =
         new QName(MODEL_PREFIX, "BaseCategoryData").toString();
   private static final String qualifiedGroupBusinessObjectId =
         new QName(MODEL_PREFIX, "BaseGroupData").toString();

   private static final String qualifiedBusinessDateId =
         new QName(MODEL_PREFIX, PredefinedConstants.BUSINESS_DATE).toString();

   @Before
   public void setup()
   {
      BenchmarkTestUtils.deployBenchmark("benchmarksTest.benchmark", serviceFactory);

      StartOptions startOptions_withBenchmark = new StartOptions(Collections.singletonMap(
            PredefinedConstants.BUSINESS_DATE, Calendar.getInstance().getTimeInMillis()), true, BENCHMARK_REF);
      StartOptions startOptions_withoutBenchmark = new StartOptions(null, true);

      // 2 with benchmark
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      // 1 without benchmark
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withoutBenchmark);

      // 1 with benchmark (total of 3 now)
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      // 1 aborted
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);
      serviceFactory.getAdministrationService().abortProcessInstance(pi.getOID());

      businessObjects = CollectionUtils.newMap();

      Map<String, Serializable> businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(1));
      businessObjectData.put("Name", "CategoryA");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(Integer.valueOf(1),
            Integer.valueOf(2))));

      businessObjects.put((String)businessObjectData.get("Name"),
            serviceFactory.getWorkflowService().createBusinessObjectInstance(
                  qualifiedCategoryBusinessObjectId, businessObjectData));

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(2));
      businessObjectData.put("Name", "CategoryB");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(Integer.valueOf(3))));

      businessObjects.put((String)businessObjectData.get("Name"),
            serviceFactory.getWorkflowService().createBusinessObjectInstance(
                  qualifiedCategoryBusinessObjectId, businessObjectData));

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(1));
      businessObjectData.put("Name", "GroupA");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(Integer.valueOf(1),
            Integer.valueOf(2))));

      businessObjects.put((String)businessObjectData.get("Name"),
            serviceFactory.getWorkflowService().createBusinessObjectInstance(
                  qualifiedGroupBusinessObjectId, businessObjectData));

      businessObjectData = CollectionUtils.newMap();
      businessObjectData.put("Ident", Integer.valueOf(2));
      businessObjectData.put("Name", "GroupB");
      businessObjectData.put("BaseObjects", new ArrayList<Integer>(Arrays.asList(
            Integer.valueOf(2), Integer.valueOf(3))));

      businessObjects.put((String)businessObjectData.get("Name"),
            serviceFactory.getWorkflowService().createBusinessObjectInstance(
                  qualifiedGroupBusinessObjectId, businessObjectData));

      //       | GroupA | GroupB | CategoryA | CategoryB
      // BaseA |   x    |        |     x     |
      // BaseB |   x    |    x   |     x     |
      // BaseC |        |    x   |           |    x
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
      serviceFactory.getWorkflowService().startProcess(
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
      serviceFactory.getWorkflowService().startProcess(
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
      serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PARENT_PROCESS, startOptions_withBenchmark);
   }

   @Test
   public void queryProcessBenchmarkStatisticsByProcesses()
   {
      runDaemon();

      // Query
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessIds(Collections.singleton(BENCHMARK_PROCESS));

      // Do not add state filter to include aborted and completed.
      // query.where(ProcessStateFilter.ALIVE);

      // Only for the selected benchmark.
      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid()));

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
      BusinessObject baseData = null;

      runDaemon();

      ProcessDefinition benchmarkProcess = serviceFactory.getQueryService()
            .getProcessDefinition(BENCHMARK_PARENT_PROCESS);

      String qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseData").toString();
      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      BusinessObjects bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseData = bos.get(0);

      // Validate BenchmarkProcessStatisticsQuery if primary key values are not passed
      // and no groupBy is specified
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessesAndBusinessObject(Collections.singleton(benchmarkProcess),
                  baseData, new HashSet<Serializable>(Arrays.asList(
                        Integer.valueOf(1),
                        Integer.valueOf(2))));

      // Only for the selected benchmark.
      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid()));

      // Only for BUSINESS_DATE today.
      query.where(DataFilter.between(qualifiedBusinessDateId, getCurrentDayStart(),
            getCurrentDayEnd()));

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);
      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(1)));
      Assert.assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
      Set<String> filterValues = stats.getFilterValues(null);
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB"));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 0), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 0), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseD", 0), is(equalTo(0l)));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjects()
   {
      BusinessObject baseData = null;

      runDaemon();

      ProcessDefinition benchmarkProcess = serviceFactory.getQueryService()
            .getProcessDefinition(BENCHMARK_PARENT_PROCESS);

      String qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseData").toString();
      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      BusinessObjects bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseData = bos.get(0);

      // Validate BenchmarkProcessStatisticsQuery if primary key values are not passed
      // and no groupBy is specified
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
            .forProcessesAndBusinessObject(Collections.singleton(benchmarkProcess),
                  baseData, /*new HashSet<Serializable>(Arrays.asList(
                        Integer.valueOf(1),
                        Integer.valueOf(2),
                        Integer.valueOf(3)))*/ Collections.<Serializable>emptySet());

      // Only for the selected benchmark.
      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid()));

      // Only for BUSINESS_DATE today.
      query.where(DataFilter.between(qualifiedBusinessDateId, getCurrentDayStart(),
            getCurrentDayEnd()));

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);
      Assert.assertNotNull(stats);
      Set<String> groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(1)));
      Assert.assertThat(groupByValues, hasItem(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE));
      Set<String> filterValues = stats.getFilterValues(null);
      Assert.assertThat(filterValues.size(), is(equalTo(3)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB", "BaseC"));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 0), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseB", 0), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseC", 0), is(equalTo(1l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, "BaseD", 0), is(equalTo(0l)));

      qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseGroupData").toString();
      businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      BusinessObject baseGroupData = bos.get(0);

      // Validate BenchmarkProcessStatisticsQuery if no primary key values are passed but
      // a groupBy is set
      query = BenchmarkProcessStatisticsQuery.forProcessesAndBusinessObject(
            Collections.<ProcessDefinition>emptySet(),
            baseData, Collections.<Serializable>emptySet(),
            baseGroupData, Collections.<Serializable>emptySet());

      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid()));

      stats = (BenchmarkBusinessObjectStatistics) serviceFactory
            .getQueryService().getAllProcessInstances(query);

      groupByValues = stats.getGroupByValues();
      Assert.assertThat(groupByValues.size(), is(equalTo(2)));
      Assert.assertThat(groupByValues, hasItems("GroupA", "GroupB"));
      filterValues = stats.getFilterValues("GroupA");
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseA", "BaseB"));
      filterValues = stats.getFilterValues("GroupB");
      Assert.assertThat(filterValues.size(), is(equalTo(2)));
      Assert.assertThat(filterValues, hasItems("BaseB", "BaseC"));

      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 0), is(equalTo(3l)));

      // group count for benchmarkValue 1
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 0), is(equalTo(6l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 0), is(equalTo(6l)));

      // group count for benchmarkValue 2
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 2), is(equalTo(0l)));

      // total count for benchmarkValue 1
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 0), is(equalTo(9l)));

      // total count for benchmarkValue 2
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 2), is(equalTo(0l)));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjectsWithGroupBy()
   {
      BusinessObject baseData = null;
      BusinessObject baseGroupData = null;

      runDaemon();

      String qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseData").toString();
      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      BusinessObjects bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseData = bos.get(0);

      qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseGroupData").toString();
      businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseGroupData = bos.get(0);

      // Validate BenchmarkProcessStatisticsQuery if primary key values are passed
      // for the filter Business Object
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery.forProcessesAndBusinessObject(
            Collections.<ProcessDefinition>emptySet(),
            baseData, new HashSet<Serializable>(Arrays.asList(1, 2)),
            baseGroupData, Collections.<Serializable>emptySet());
      // Only for the selected benchmark.
      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid()));

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

      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 0), is(equalTo(0l)));

      // group count for benchmarkValue 1
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 0), is(equalTo(6l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 0), is(equalTo(3l)));

      // group count for benchmarkValue 2
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 2), is(equalTo(0l)));

      // total count for benchmarkValue 1
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 0), is(equalTo(6l)));

      // total count for benchmarkValue 2
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 2), is(equalTo(0l)));
   }

   @Test
   public void queryProcessBenchmarkStatisticsByBusinessObjectsWithGroupByAndPk()
   {
      BusinessObject baseData = null;
      BusinessObject baseGroupData = null;

      runDaemon();

      String qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseData").toString();
      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      BusinessObjects bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseData = bos.get(0);

      qualifiedBusinessObjectId = new QName(MODEL_PREFIX, "BaseGroupData").toString();
      businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
            qualifiedBusinessObjectId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      bos = serviceFactory.getQueryService().getAllBusinessObjects(businessObjectQuery);
      baseGroupData = bos.get(0);

      // Validate BenchmarkProcessStatisticsQuery if primary key values are passed
      // for the filter and groupBy Business Object
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery.forProcessesAndBusinessObject(
            Collections.<ProcessDefinition>emptySet(),
            baseData, new HashSet<Serializable>(Arrays.asList(1, 2)),
            baseGroupData, new HashSet<Serializable>(Arrays.asList(1)));
      // Only for the selected benchmark.
      query.where(BenchmarkProcessStatisticsQuery.BENCHMARK_OID
            .isEqual(getDeployedBenchmarkOid()));

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

      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 1), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseA", 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseB", 0), is(equalTo(3l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", "BaseC", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseA", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseB", 0), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", "BaseC", 0), is(equalTo(0l)));

      // group count for benchmarkValue 1
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 0), is(equalTo(6l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 0), is(equalTo(0l)));

      // group count for benchmarkValue 2
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupA", null, 2), is(equalTo(0l)));
      Assert.assertThat(stats.getBenchmarkCategoryCount("GroupB", null, 2), is(equalTo(0l)));

      // total count for benchmarkValue 1
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 0), is(equalTo(6l)));

      // total count for benchmarkValue 2
      Assert.assertThat(stats.getBenchmarkCategoryCount(null, null, 2), is(equalTo(0l)));
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
            .isEqual(getDeployedBenchmarkOid()));

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

   private void runDaemon()
   {
      // Daemon execution
      serviceFactory.getAdministrationService().startDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);

      DaemonExecutionState state = serviceFactory.getAdministrationService()
            .getDaemon(AdministrationService.BENCHMARK_DAEMON, false)
            .getDaemonExecutionState();

      assertEquals(DaemonExecutionState.OK, state);

      serviceFactory.getAdministrationService().stopDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);
   }

   private Long getDeployedBenchmarkOid()
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
}
