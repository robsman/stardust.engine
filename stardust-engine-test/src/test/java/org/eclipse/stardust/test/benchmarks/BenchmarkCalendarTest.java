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
import static org.eclipse.stardust.test.benchmarks.BenchmarkTestUtils.deployBenchmark;
import static org.eclipse.stardust.test.benchmarks.BenchmarkTestUtils.deployCalendar;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.StartOptions;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * Class to test benchmark functionality with time off calendar conditions.
 *
 * @author Roland.Stamm
 *
 */
public class BenchmarkCalendarTest
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

   private static final String BENCHMARK_PROCESS = "BenchmarkedProcess";

   // All timeoff blocked since 26th may 2015
   private static final String TIMEOFF_CALENDAR = "timeOffCalendar-d76edddf-361f-4423-8f70-de8d72b1d277.json";

   // References the prior all blocked calendar
   private static final String REFERENCING_TIMEOFF_CALENDAR = "timeOffCalendar-4aba2223-5500-4e22-b209-8e13c1aff4c4.json";

   // Empty calendar
   private static final String NO_TIMEOFF_CALENDAR = "timeOffCalendar-892d7804-3eac-4202-96f3-a27d050e0d6c.json";

   private static final String BENCHMARK1_ARTIFACT_ID = "calendar1.benchmark";

   private static final String BENCHMARK2_ARTIFACT_ID = "calendar2.benchmark";

   private static final String BENCHMARK3_ARTIFACT_ID = "calendar3.benchmark";


   @Before
   public void setup()
   {
      deployCalendar(TIMEOFF_CALENDAR, serviceFactory);
      deployCalendar(REFERENCING_TIMEOFF_CALENDAR, serviceFactory);
      deployCalendar(NO_TIMEOFF_CALENDAR, serviceFactory);

      deployBenchmark(BENCHMARK1_ARTIFACT_ID, serviceFactory);
      deployBenchmark(BENCHMARK2_ARTIFACT_ID, serviceFactory);
      deployBenchmark(BENCHMARK3_ARTIFACT_ID, serviceFactory);
   }

   @Test
   public void activityBenchmarkCalendar1TimeOff()
   {

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(null, true, BENCHMARK1_ARTIFACT_ID));

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(instance.getBenchmarkResult().getCategory(), 0);
   }

   @Test
   public void activityBenchmarkCalendar2ReferencedTimeOff()
   {

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(null, true, BENCHMARK2_ARTIFACT_ID));

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(instance.getBenchmarkResult().getCategory(), 0);
   }

   @Test
   public void activityBenchmarkCalendar3NoTimeOff()
   {

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(null, true, BENCHMARK3_ARTIFACT_ID));

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(instance.getBenchmarkResult().getCategory(), 0);
   }

}
