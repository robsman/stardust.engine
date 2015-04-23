/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.query.statistics;

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode.NATIVE_THREADING;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.ACTIVITY_ID_LAST_INTERACTIVE_ACTIVITY;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.ACTIVITY_ID_WORK;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.MODEL_ID_PREFIX;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.PROCESS_DEF_ID_DO_WORK;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.PROCESS_DEF_ID_AI_PROCESSING_TIME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatistics.IActivityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistProcessFiltersQuery;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.ActivityStatisticsResult;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains tests for the following <i>Activity Instance Statistics Queries</i>
 * <ul>
 *    <li>{@link ActivityStatisticsQuery}</li>
 *    <li>{@link CriticalityStatisticsQuery}</li>
 *    <li>{@link OpenActivitiesStatisticsQuery}</li>
 *    <li>{@link WorklistProcessFiltersQuery}</li>
 * </ul>
 * </p>
 *
 * TODO add test cases for {@link CriticalityStatisticsQuery}
 * TODO add test cases for {@link OpenActivitiesStatisticsQuery}
 * TODO add test cases for {@link WorklistProcessFiltersQuery}
 *
 * @author Nicolas.Werlein
 */
public class ActivityInstanceStatisticsQueryTest
{
   private static final Long ONE_HUNDRED_MS = 100L;

   private static final int TOTAL_COUNT_PER_PD = 10;

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   @Test
   public void testRetrieveAiStatisticsForProcessIds()
   {
      final Set<String> processIds = newHashSet();
      processIds.add(PROCESS_DEF_ID_DO_WORK);
      processIds.add(PROCESS_DEF_ID_AI_PROCESSING_TIME);

      final ActivityStatisticsQuery query = ActivityStatisticsQuery.forProcessIds(processIds);
      setCriticalExecutionTimePolicy(query);
      final ActivityStatisticsResult result = (ActivityStatisticsResult) sf.getQueryService().getAllActivityInstances(query);

      assertThatStatisticsAreCorrect(result);
   }

   @Test
   public void testRetrieveAiStatisticsForProcessDefs()
   {
      final Set<ProcessDefinition> processDefs = newHashSet();
      processDefs.add(sf.getQueryService().getProcessDefinition(PROCESS_DEF_ID_DO_WORK));
      processDefs.add(sf.getQueryService().getProcessDefinition(PROCESS_DEF_ID_AI_PROCESSING_TIME));

      final ActivityStatisticsQuery query = ActivityStatisticsQuery.forProcesses(processDefs);
      setCriticalExecutionTimePolicy(query);
      final ActivityStatisticsResult result = (ActivityStatisticsResult) sf.getQueryService().getAllActivityInstances(query);

      assertThatStatisticsAreCorrect(result);
   }

   @Before
   public void setUp() throws InterruptedException, TimeoutException
   {
      for (int i = 0; i < TOTAL_COUNT_PER_PD; i++)
      {
         startProcessDefDoWork();
         startProcessDefProcessingTime();
      }
   }

   private void startProcessDefDoWork() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_DO_WORK, null, true);
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());

      /* do not complete AI/PI */
   }

   private void startProcessDefProcessingTime() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_AI_PROCESSING_TIME, null, true);

      final ActivityInstance firstInteractiveAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().suspend(firstInteractiveAi.getOID(), null);
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().activate(firstInteractiveAi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(firstInteractiveAi.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_LAST_INTERACTIVE_ACTIVITY);

      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());

      /* do not complete AI/PI */
   }

   private void setCriticalExecutionTimePolicy(final ActivityStatisticsQuery query)
   {
      query.setPolicy(CriticalExecutionTimePolicy.criticalityByDuration(0.33F, 0.5F, 0.66F));
   }

   private void assertThatStatisticsAreCorrect(final ActivityStatisticsResult result)
   {
      final IActivityStatistics workAiStats = result.getStatisticsForActivity(PROCESS_DEF_ID_DO_WORK, MODEL_ID_PREFIX + ACTIVITY_ID_WORK);
      assertForProcessDef(workAiStats);

      final IActivityStatistics lastInteractiveAiStats = result.getStatisticsForActivity(PROCESS_DEF_ID_AI_PROCESSING_TIME, MODEL_ID_PREFIX + ACTIVITY_ID_LAST_INTERACTIVE_ACTIVITY);
      assertForProcessDef(lastInteractiveAiStats);
   }

   private void assertForProcessDef(final IActivityStatistics stats)
   {
      final long lowCount = stats.getInstancesCount(ProcessInstancePriority.Low);
      final long normalCount = stats.getInstancesCount(ProcessInstancePriority.Normal);
      final long highCount = stats.getInstancesCount(ProcessInstancePriority.High);
      final long totalCount = stats.getTotalInstancesCount();
      assertThat(Long.valueOf(totalCount), equalTo(Long.valueOf(lowCount + normalCount + highCount)));
      assertThat(Long.valueOf(totalCount), equalTo(Long.valueOf(TOTAL_COUNT_PER_PD)));

      // TODO add additional assertions
   }

   /**
    * <p>
    * This is the application used in the test model that waits for some time.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static final class WaitingApp
   {
      public void doWait() throws InterruptedException
      {
         Thread.sleep(ONE_HUNDRED_MS);
      }
   }
}
