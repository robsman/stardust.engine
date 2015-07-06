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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.StartOptions;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * Class to test benchmark functionality with time off calendar conditions.
 *
 * @author Roland.Stamm
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BenchmarkCalendarTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(
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

   // Every second day timeoff blocked since 26th may 2015
   private static final String TIMEOFF_CALENDAR = "timeOffCalendar-d76edddf-361f-4423-8f70-de8d72b1d277.json";

   // References the prior all blocked calendar
   private static final String REFERENCING_TIMEOFF_CALENDAR = "timeOffCalendar-4aba2223-5500-4e22-b209-8e13c1aff4c4.json";

   // Empty calendar
   private static final String NO_TIMEOFF_CALENDAR = "timeOffCalendar-892d7804-3eac-4202-96f3-a27d050e0d6c.json";

   // All timeoff blocked since 26th may 2015
   private static final String ALL_TIMEOFF_CALENDAR = "timeOffCalendar-d76edddf-361f-4423-8f70-de8d72b1d278.json";

   // Today timeoff (done by timestamp replacement while reading the file)
   private static final String TODAY_TIMEOFF_CALENDAR = "timeOffCalendar-32bf4126-8eec-4836-aaa8-fe5319532a07.json";

   private static final String BENCHMARK1_ARTIFACT_ID = "calendar1.benchmark";

   private static final String BENCHMARK2_ARTIFACT_ID = "calendar2.benchmark";

   private static final String BENCHMARK3_ARTIFACT_ID = "calendar3.benchmark";

   private static final String BENCHMARK4_ARTIFACT_ID = "calendar4.benchmark";

   private static final String BENCHMARK5_ARTIFACT_ID = "calendar5.benchmark";
   
   private static final String BENCHMARK6_ARTIFACT_ID = "calendar6.benchmark";

   @Test
   public void activityBenchmarkCalendar1TimeOff()
   {
      deployCalendar(TIMEOFF_CALENDAR, serviceFactory);
      deployBenchmark(BENCHMARK1_ARTIFACT_ID, serviceFactory);

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(getBusinessDateMap(), true, BENCHMARK1_ARTIFACT_ID));
      assertEquals(0, pi.getBenchmarkResult().getCategory());

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(0, instance.getBenchmarkResult().getCategory());
      assertNotEquals(-1, instance.getBenchmarkResult().getCategory());
   }

   @Test
   public void activityBenchmarkCalendar2ReferencedTimeOff()
   {
      deployCalendar(TIMEOFF_CALENDAR, serviceFactory);
      deployCalendar(REFERENCING_TIMEOFF_CALENDAR, serviceFactory);
      deployBenchmark(BENCHMARK2_ARTIFACT_ID, serviceFactory);

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(getBusinessDateMap(), true, BENCHMARK2_ARTIFACT_ID));
      assertEquals(0, pi.getBenchmarkResult().getCategory());

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(0, instance.getBenchmarkResult().getCategory());
      assertNotEquals(-1, instance.getBenchmarkResult().getCategory());
   }

   @Test
   public void activityBenchmarkCalendar3NoTimeOff()
   {
      deployCalendar(NO_TIMEOFF_CALENDAR, serviceFactory);
      deployBenchmark(BENCHMARK3_ARTIFACT_ID, serviceFactory);

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(getBusinessDateMap(), true, BENCHMARK3_ARTIFACT_ID));
      assertEquals(0, pi.getBenchmarkResult().getCategory());

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(0, instance.getBenchmarkResult().getCategory());
      assertNotEquals(-1, instance.getBenchmarkResult().getCategory());
   }

   @Test
   public void activityBenchmarkCalendar4UnlimitedTimeOff()
   {
      deployCalendar(ALL_TIMEOFF_CALENDAR, serviceFactory);
      deployBenchmark(BENCHMARK4_ARTIFACT_ID, serviceFactory);

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(getBusinessDateMap(), true, BENCHMARK4_ARTIFACT_ID));
      assertEquals(0, pi.getBenchmarkResult().getCategory());

      ProcessInstance pi2 = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(getBusinessDateMap(), true, BENCHMARK4_ARTIFACT_ID));
      assertEquals(0, pi2.getBenchmarkResult().getCategory());

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertEquals(-1, instance.getBenchmarkResult().getCategory());
   }

   @Test
   public void activityBenchmarkCalendar5TodayTimeOff()
   {
      deployCalendar(TODAY_TIMEOFF_CALENDAR, serviceFactory);
      deployBenchmark(BENCHMARK5_ARTIFACT_ID, serviceFactory);

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(getBusinessDateMap(), true, BENCHMARK5_ARTIFACT_ID));
      assertEquals(0, pi.getBenchmarkResult().getCategory());

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertEquals(1, instance.getBenchmarkResult().getCategory());
   }
   
   @Test
   public void activityBenchmarkCalendar6TodayTimeOff()
   {
      deployCalendar(TODAY_TIMEOFF_CALENDAR, serviceFactory);
      deployBenchmark(BENCHMARK6_ARTIFACT_ID, serviceFactory);

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, new StartOptions(getBusinessDateMap(), true, BENCHMARK6_ARTIFACT_ID));
      assertEquals(0, pi.getBenchmarkResult().getCategory());

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertEquals(1, instance.getBenchmarkResult().getCategory());
   }   

   private Map<String, Object> getBusinessDateMap()
   {
      Map<String, Object> data = CollectionUtils.newHashMap();

      Map sdMap = CollectionUtils.newHashMap();
      sdMap.put("TheDate", new Date());
      
      data.put("BUSINESS_DATE", Calendar.getInstance());
      data.put("DateSD", sdMap);
      return data;
   }

}
