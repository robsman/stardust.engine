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
package org.eclipse.stardust.test.workflow;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DaemonHome;
import org.eclipse.stardust.test.api.util.DaemonHome.DaemonType;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class ResubmissionTest
{
   private static final String RESUBMISSION_ID = "Resubmission";
   private static final String PROCESS_ID = RESUBMISSION_ID;
   private static final String MODEL_NAME = PROCESS_ID;
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private AdministrationService administrationService;

   private QueryService queryService;

   @Before
   public void setup()
   {
      administrationService = serviceFactory.getAdministrationService();
      queryService = serviceFactory.getQueryService();
      DaemonHome.startDaemon(administrationService, DaemonType.EVENT_DAEMON);
   }

   @After
   public void teardown()
   {
      DaemonHome.stopAllRunningDaemons(administrationService);
   }

   @Test
   public void testStartProcess() throws Exception
   {
      WorkflowService wfService = serviceFactory.getWorkflowService();
      wfService.startProcess(PROCESS_ID, null, true);
      ActivityInstance activityInstance = findFirstAliveActivityInstance();
      wfService.bindActivityEventHandler(activityInstance.getOID(), RESUBMISSION_ID);
      activityInstance = findFirstAliveActivityInstance();
      assertEquals(ActivityInstanceState.Hibernated, activityInstance.getState());
      Thread.sleep(10000);
      activityInstance = findFirstAliveActivityInstance();
      assertEquals(ActivityInstanceState.Suspended, activityInstance.getState());
      
      final PerformingParticipantFilter filter = PerformingParticipantFilter.forParticipant(
            DepartmentInfoDetails.getParticipant(null, ModelParticipantInfo.ADMINISTRATOR));
      
      assertEquals(1, getActivityInstancesCount(filter));
   }

   private ActivityInstance findFirstAliveActivityInstance()
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive();
      return queryService.findFirstActivityInstance(aiQuery);
   }

   
   private long getActivityInstancesCount(final FilterCriterion filter)
   {
      final ActivityInstanceQuery ai = new ActivityInstanceQuery();
      ai.where(filter);
      return queryService.getActivityInstancesCount(ai);
   }
}