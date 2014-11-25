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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.ACTIVITY_ID_LAST_INTERACTIVE_ACTIVITY;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.PROCESS_DEF_ID_AI_PROCESSING_TIME;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.PROCESS_DEF_ID_PI_PROCESSING_TIME_A;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.query.statistics.ProcessingTimes;
import org.eclipse.stardust.engine.core.query.statistics.ProcessingTimes.ProcessingTime;
import org.eclipse.stardust.engine.core.query.statistics.QueryActivityInstanceProcessingTimeCommand;
import org.eclipse.stardust.engine.core.query.statistics.QueryProcessInstanceProcessingTimeCommand;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains tests for {@link QueryActivityInstanceProcessingTimeCommand} and
 * for {@link QueryProcessInstanceProcessingTimeCommand}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class QueryProcessingTimeTest
{
   private static final Long ONE_HUNDRED_MS = 100L;

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private Map<Long, Bound> aiOidToProcessingTimeExpectations = newHashMap();
   private Map<Long, Bound> piOidToProcessingTimeExpectations = newHashMap();


   @Test
   public void testAiProcessingTimeSeparatelyForEachAi()
   {
      final ActivityInstances ais = findAllAisFor(PROCESS_DEF_ID_AI_PROCESSING_TIME);

      for (final ActivityInstance ai : ais)
      {
         final ProcessingTimes result = (ProcessingTimes) sf.getWorkflowService().execute(new QueryActivityInstanceProcessingTimeCommand(ai.getOID()));
         assertNotNull(result);
         if (ai.getActivity().isInteractive())
         {
            assertThat(result.processingTimes().size(), equalTo(1));
            assertThatAiProcessingTimeIsCorrect(result.processingTimes().iterator().next());
         }
         else
         {
            assertThat(result.processingTimes().size(), equalTo(0));
         }
      }
   }

   @Test
   public void testAiProcessingTimeAsAWhole()
   {
      final ActivityInstances ais = findAllAisFor(PROCESS_DEF_ID_AI_PROCESSING_TIME);
      final Set<Long> oids = newHashSet();
      for (final ActivityInstance ai : ais)
      {
         oids.add(ai.getOID());
      }

      final ProcessingTimes result = (ProcessingTimes) sf.getWorkflowService().execute(new QueryActivityInstanceProcessingTimeCommand(oids));
      assertNotNull(result);
      assertThat(result.processingTimes().size(), equalTo(4));

      for (final ProcessingTime t : result.processingTimes())
      {
         assertThatAiProcessingTimeIsCorrect(t);
      }
   }

   @Test
   public void testPiProcessingTimeSeparatelyForEachPi()
   {
      final ProcessInstances pis = sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findForProcess(PROCESS_DEF_ID_PI_PROCESSING_TIME_A, false));
      for (final ProcessInstance pi : pis)
      {
         final ProcessingTimes result = (ProcessingTimes) sf.getWorkflowService().execute(new QueryProcessInstanceProcessingTimeCommand(pi.getOID()));
         assertNotNull(result);
         assertThat(result.processingTimes().size(), equalTo(1));
         assertThatPiProcessingTimeIsCorrect(result.processingTimes().iterator().next());
      }
   }

   @Test
   public void testPiProcessingTimeAsAWhole()
   {
      final ProcessInstances pis = sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findForProcess(PROCESS_DEF_ID_PI_PROCESSING_TIME_A, false));
      final Set<Long> oids = newHashSet();
      for (final ProcessInstance pi : pis)
      {
         oids.add(pi.getOID());
      }

      final ProcessingTimes result = (ProcessingTimes) sf.getWorkflowService().execute(new QueryProcessInstanceProcessingTimeCommand(oids));
      assertNotNull(result);
      assertThat(result.processingTimes().size(), equalTo(2));

      for (final ProcessingTime t : result.processingTimes())
      {
         assertThatPiProcessingTimeIsCorrect(t);
      }
   }

   @Before
   public void setUp() throws Exception
   {
      for (int i = 0; i < 2; i++)
      {
         startProcessDefAiProcessingTime();
         startProcessDefPiProcessingTime();
      }
   }

   @After
   public void tearDown()
   {
      aiOidToProcessingTimeExpectations.clear();
      piOidToProcessingTimeExpectations.clear();
   }

   private void startProcessDefAiProcessingTime() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_AI_PROCESSING_TIME, null, true);

      final ActivityInstance firstInteractiveAi = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(ActivityInstanceState.Suspended));
      aiOidToProcessingTimeExpectations.put(Long.valueOf(firstInteractiveAi.getOID()), new Bound(2 * ONE_HUNDRED_MS, 8 * ONE_HUNDRED_MS));
      sf.getWorkflowService().activate(firstInteractiveAi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().suspend(firstInteractiveAi.getOID(), null);
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().activate(firstInteractiveAi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(firstInteractiveAi.getOID(), null, null);

      ActivityInstanceStateBarrier.instance().awaitForId(pi.getOID(), ACTIVITY_ID_LAST_INTERACTIVE_ACTIVITY);

      final ActivityInstance lastInteractiveAi = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findInState(ActivityInstanceState.Suspended));
      aiOidToProcessingTimeExpectations.put(Long.valueOf(lastInteractiveAi.getOID()), new Bound(ONE_HUNDRED_MS));
      sf.getWorkflowService().activate(lastInteractiveAi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);

      /* do not complete AI/PI */
   }

   private void startProcessDefPiProcessingTime() throws InterruptedException, TimeoutException
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_PI_PROCESSING_TIME_A, null, true);
      piOidToProcessingTimeExpectations.put(Long.valueOf(pi.getOID()), new Bound(6 * ONE_HUNDRED_MS, 12 * ONE_HUNDRED_MS));

      final ActivityInstance aFirstAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(aFirstAi.getOID(), null, null);

      final ActivityInstance bFirstAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(bFirstAi.getOID(), null, null);

      final ActivityInstance cFirstAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(cFirstAi.getOID(), null, null);

      final ActivityInstance cLastAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(cLastAi.getOID(), null, null);

      final ActivityInstance bLastAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(bLastAi.getOID(), null, null);

      final ActivityInstance aLastAi = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Thread.sleep(ONE_HUNDRED_MS);
      sf.getWorkflowService().complete(aLastAi.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
   }

   private ActivityInstances findAllAisFor(final String processDefId)
   {
      final ActivityInstanceQuery query = new ActivityInstanceQuery();
      query.where(new ProcessDefinitionFilter(processDefId));
      return sf.getQueryService().getAllActivityInstances(query);
   }

   private void assertThatAiProcessingTimeIsCorrect(final ProcessingTime processingTime)
   {
      // TODO do not use this pretty vague lower-upper-bound approach, but determine the exact times by means of AIs' historical states
      final Bound bound = aiOidToProcessingTimeExpectations.get(processingTime.oid());

      assertThat(processingTime.processingTime(), greaterThanOrEqualTo(bound.lower()));
      assertThat(processingTime.processingTime(), lessThanOrEqualTo(bound.upper()));
   }

   private void assertThatPiProcessingTimeIsCorrect(final ProcessingTime processingTime)
   {
      // TODO do not use this pretty vague lower-upper-bound approach, but determine the exact times by means of AIs' historical states
      final Bound bound = piOidToProcessingTimeExpectations.get(processingTime.oid());

      assertThat(processingTime.processingTime(), greaterThanOrEqualTo(bound.lower()));
      assertThat(processingTime.processingTime(), lessThanOrEqualTo(bound.upper()));
   }

   private static final class Bound
   {
      private final long lower;
      private final Long upper;

      private final Long startTime;

      public Bound(final long lower, final long upper)
      {
         this.lower = lower;
         this.upper = Long.valueOf(upper);

         this.startTime = null;
      }

      public Bound(final long lower)
      {
         this.lower = lower;
         this.upper = null;

         this.startTime = Long.valueOf(new Date().getTime());
      }

      public long lower()
      {
         return lower;
      }

      public long upper()
      {
         if (upper == null)
         {
            final long maxProcessingTime = new Date().getTime() - startTime.longValue();
            return maxProcessingTime;
         }
         return upper.longValue();
      }

      @Override
      public String toString()
      {
         final StringBuilder sb = new StringBuilder();

         sb.append("lower: ").append(lower).append(", ");
         sb.append("upper: ").append(upper).append(", ");
         sb.append("startTime: ").append(startTime);

         return sb.toString();
      }
   }
}
