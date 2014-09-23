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

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.query.statistics.api.DateRange;
import org.eclipse.stardust.engine.core.query.statistics.api.StatisticsDateRangePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.PerformanceInInterval;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.ContributionInInterval;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.WorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatisticsQuery;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class StatisticsQueryTest
{

   protected static final String MODEL_NAME = "StatisticsQueryModel";

   private static final UsernamePasswordPair USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(USER_PWD_PAIR,
         testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(USER_PWD_PAIR,
         ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private QueryService qService;

   @Before
   public void setUp()
   {
      qService = sf.getQueryService();
   }

   @Test
   public void testUserWorktimeStatisticsQuery()
   {
      Date dayZero = new Date(0l);
      WorkflowService wfs = sf.getWorkflowService();
      long userOid = wfs.getUser().getOID();

      // start activate and suspend a process to create an activity instance history.
      ProcessInstance pi = wfs.startProcess("{StatisticsQueryModel}DoWork", null, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
      wfs.suspend(ai.getOID(), null);

      Contribution doWorkContribution = getWorktimeContributionForAdministrator(userOid, true);
      ContributionInInterval doWorkToday = doWorkContribution.getOrCreateContributionInInterval(DateRange.TODAY);
      assertTrue(dayZero.before(doWorkToday.getTimeSpent()));
      assertTrue(dayZero.before(doWorkToday.getTimeWaiting()));
      assertEquals(1, doWorkToday.getnAis());
      assertEquals(1, doWorkToday.getnPis());

      Contribution doWorkContribution2 = getWorktimeContributionForAdministrator(userOid, true);
      ContributionInInterval doWorkToday2 = doWorkContribution2.getOrCreateContributionInInterval(DateRange.TODAY);
      assertTrue(dayZero.before(doWorkToday2.getTimeSpent()));
      assertTrue(dayZero.before(doWorkToday2.getTimeWaiting()));

      assertTrue(doWorkToday.getTimeSpent().equals(doWorkToday2.getTimeSpent()));
      assertTrue(doWorkToday.getTimeWaiting().before(doWorkToday2.getTimeWaiting()));
   }

   @Test
   public void testUserWorktimeStatisticsQueryNoWaitTimeCalculation()
   {
      Date dayZero = new Date(0l);
      WorkflowService wfs = sf.getWorkflowService();
      long userOid = wfs.getUser().getOID();

      // start activate and suspend a process to create an activity instance history.
      ProcessInstance pi = wfs.startProcess("{StatisticsQueryModel}DoWork", null, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
      wfs.suspend(ai.getOID(), null);

      Contribution doWorkContribution = getWorktimeContributionForAdministrator(userOid, false);
      ContributionInInterval doWorkToday = doWorkContribution.getOrCreateContributionInInterval(DateRange.TODAY);
      assertTrue(dayZero.before(doWorkToday.getTimeSpent()));
      assertTrue(dayZero.equals(doWorkToday.getTimeWaiting()));
      assertEquals(1, doWorkToday.getnAis());
      assertEquals(1, doWorkToday.getnPis());

      Contribution doWorkContribution2 = getWorktimeContributionForAdministrator(userOid, false);
      ContributionInInterval doWorkToday2 = doWorkContribution2.getOrCreateContributionInInterval(DateRange.TODAY);
      assertTrue(dayZero.before(doWorkToday2.getTimeSpent()));
      assertTrue(dayZero.equals(doWorkToday2.getTimeWaiting()));

      assertTrue(doWorkToday.getTimeSpent().equals(doWorkToday2.getTimeSpent()));
      assertTrue(doWorkToday.getTimeWaiting().equals(doWorkToday2.getTimeWaiting()));
   }

   private Contribution getWorktimeContributionForAdministrator(long userOid, boolean calculateWaitTime)
   {
      UserWorktimeStatisticsQuery query = calculateWaitTime ? UserWorktimeStatisticsQuery.forAllUsers() : UserWorktimeStatisticsQuery.forAllUsersWithoutWaitTime();
      List<DateRange> dateRanges = new ArrayList<DateRange>();
      dateRanges.add(DateRange.TODAY);
      dateRanges.add(DateRange.YESTERDAY);
      dateRanges.add(DateRange.THIS_WEEK);
      dateRanges.add(DateRange.LAST_WEEK);
      dateRanges.add(DateRange.THIS_MONTH);
      dateRanges.add(DateRange.LAST_MONTH);
      // Second TODAY should not double the result for TODAY.
      dateRanges.add(DateRange.TODAY);
      query.setPolicy(new StatisticsDateRangePolicy(dateRanges));
      UserWorktimeStatistics userWorktimeStatistics = (UserWorktimeStatistics) qService.getAllUsers(query);
      assertNotNull(userWorktimeStatistics);

      WorktimeStatistics worktimeStatistics = userWorktimeStatistics.getWorktimeStatistics(userOid);

      Contribution doWorkContribution = worktimeStatistics.findContribution(
            "{StatisticsQueryModel}DoWork", ModelParticipantInfo.ADMINISTRATOR);
      return doWorkContribution;
   }

   @Test
   public void testUserPerformanceStatisticsQuery()
   {
      WorkflowService wfs = sf.getWorkflowService();
      long userOid = wfs.getUser().getOID();

      // start activate and complete a process to create an activity instance history.
      ProcessInstance pi = wfs.startProcess("{StatisticsQueryModel}DoWork", null, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
      wfs.complete(ai.getOID(), null, null);

      UserPerformanceStatistics.Contribution doWorkContribution = getPerformanceContributionForAdministrator(userOid);

      PerformanceInInterval doWorkToday = doWorkContribution.getOrCreatePerformanceInInterval(DateRange.TODAY);
      assertEquals(1, doWorkToday.getnAisCompleted());
      assertEquals(1, doWorkToday.getnPisAffected());
   }

   private UserPerformanceStatistics.Contribution getPerformanceContributionForAdministrator(
         long userOid)
   {
      UserPerformanceStatisticsQuery query = UserPerformanceStatisticsQuery.forAllUsers();
      List<DateRange> dateRanges = new ArrayList<DateRange>();
      dateRanges.add(DateRange.TODAY);
      dateRanges.add(DateRange.YESTERDAY);
      dateRanges.add(DateRange.THIS_WEEK);
      dateRanges.add(DateRange.LAST_WEEK);
      dateRanges.add(DateRange.THIS_MONTH);
      dateRanges.add(DateRange.LAST_MONTH);
      query.setPolicy(new StatisticsDateRangePolicy(dateRanges));
      UserPerformanceStatistics userPerformanceStatistics = (UserPerformanceStatistics) qService.getAllUsers(query);
      assertNotNull(userPerformanceStatistics);

      UserPerformanceStatistics.PerformanceStatistics performanceStatistics = userPerformanceStatistics.getStatisticsForUserAndProcess(
            userOid, "{StatisticsQueryModel}DoWork");

      UserPerformanceStatistics.Contribution contribution = performanceStatistics.findContribution(ModelParticipantInfo.ADMINISTRATOR);
      return contribution;
   }
}
