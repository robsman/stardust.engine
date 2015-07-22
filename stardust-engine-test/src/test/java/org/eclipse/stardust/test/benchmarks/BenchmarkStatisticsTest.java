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

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.engine.api.runtime.StartOptions;
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

   private StartOptions startOptions_withBenchmark;

   private StartOptions startOptions_withoutBenchmark;

   private static final String BENCHMARK_PROCESS = "{BenchmarksModel}BenchmarkedProcess";

   private static final String BENCHMARK_PROCESS_W_SUB = "{BenchmarksModel}BenchmarkedParentProcess";

   private static final String BENCHMARK_ACTIVITY = "{BenchmarksModel}BenchmarkedActivity";

   private static final String BENCHMARK_REF = "benchmarksTest.benchmark";

   @Before
   public void setup()
   {
      BenchmarkTestUtils.deployBenchmark("benchmarksTest.benchmark", serviceFactory);

      startOptions_withBenchmark = new StartOptions(null, true, BENCHMARK_REF);
      startOptions_withoutBenchmark = new StartOptions(null, true);
   }

@Test
   public void queryProcessBenchmarkStatisticsAfterDaemonRun()
   {

      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withoutBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      serviceFactory.getAdministrationService().startDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);

      DaemonExecutionState state = serviceFactory.getAdministrationService()
            .getDaemon(AdministrationService.BENCHMARK_DAEMON, false)
            .getDaemonExecutionState();

      assertEquals(DaemonExecutionState.OK, state);

      serviceFactory.getAdministrationService().stopDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);

      // query
      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery.forProcessIds(Collections.singleton(BENCHMARK_PROCESS));
      query.setSelectedBenchmarks(Collections.singletonList(getDeployedBenchmarkOid()));

      BenchmarkProcessStatistics stats = (BenchmarkProcessStatistics) serviceFactory.getQueryService().getAllProcessInstances(query);

      BenchmarkResults statisticsForBenchmark = stats.getBenchmarkResults().values().iterator().next();
      BenchmarkCategoryCounts benchmarkCategoryCounts = statisticsForBenchmark.getBenchmarkCategoryCountsForProcess(BENCHMARK_PROCESS);
      Map<Integer, Long> benchmarkCategoryCountMap = benchmarkCategoryCounts.getBenchmarkCategoryCount();
      // Counts only for one category
      Assert.assertEquals(1, benchmarkCategoryCountMap.size());
      // Category 3 has count of 3
      Assert.assertEquals(Long.valueOf(3L), benchmarkCategoryCountMap.get(3));

   }

   @Test
   public void queryActivityBenchmarkStatistics()
   {
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withoutBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      // query
      BenchmarkActivityStatisticsQuery query = BenchmarkActivityStatisticsQuery.forProcessId(BENCHMARK_PROCESS);
      query.setSelectedBenchmarks(Collections.singletonList(getDeployedBenchmarkOid()));

      BenchmarkActivityStatistics stats = (BenchmarkActivityStatistics) serviceFactory.getQueryService().getAllActivityInstances(query);

      BenchmarkResults statisticsForBenchmark = stats.getBenchmarkResults().values().iterator().next();
      BenchmarkCategoryCounts benchmarkCategoryCounts = statisticsForBenchmark.getBenchmarkCategoryCountsForActivity(BENCHMARK_PROCESS, BENCHMARK_ACTIVITY);
      Map<Integer, Long> benchmarkCategoryCountMap = benchmarkCategoryCounts.getBenchmarkCategoryCount();
      // Counts only for one category
      Assert.assertEquals(1, benchmarkCategoryCountMap.size());
      // Category 3 has count of 3
      Assert.assertEquals(Long.valueOf(3L), benchmarkCategoryCountMap.get(3));
   }

   private Long getDeployedBenchmarkOid()
   {
      DeployedRuntimeArtifacts runtimeArtifacts = serviceFactory.getQueryService().getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findActive(BENCHMARK_REF, BenchmarkDefinitionArtifactType.TYPE_ID, new Date()));
      if (runtimeArtifacts!= null && runtimeArtifacts.size() > 0)
      {
         return runtimeArtifacts.get(0).getOid();
      }
      Assert.fail("No deployed runtime artifact found.");
      return 0L;
   }

}
