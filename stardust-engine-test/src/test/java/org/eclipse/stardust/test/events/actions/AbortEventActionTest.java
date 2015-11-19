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
package org.eclipse.stardust.test.events.actions;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IActivityInstanceMonitor;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DaemonHome;
import org.eclipse.stardust.test.api.util.DaemonHome.DaemonType;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class AbortEventActionTest
{
   public static final String ABORT_EVENTS_MODEL_NAME = "AbortEventTestModel";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, ABORT_EVENTS_MODEL_NAME);

   private static final String PROCESS_DEF_ID_1 = "ProcessDefinition1";

   private static final String PROCESS_DEF_ID_2 = "ProcessDefinition2";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private WorkflowService workflowService;

   private QueryService queryService;

   private long daemonExecutionInterval;

   private AdministrationService administrationService;

   @Before
   public void setUp() throws Exception
   {
      daemonExecutionInterval = Parameters.instance().getInteger(
            "event.daemon.Periodicity", 5) * 1000;
      workflowService = sf.getWorkflowService();
      queryService = sf.getQueryService();
      administrationService = sf.getAdministrationService();
      DaemonHome.startDaemon(administrationService, DaemonType.EVENT_DAEMON);
   }

   @After
   public void tearDown() throws Exception
   {
      DaemonHome.stopAllRunningDaemons(administrationService);
   }

   /**
    * <p>
    * See also <a
    * href="https://www.csa.sungard.com/jira/browse/CRNT-29578">CRNT-29578</a>.
    * </p>
    */
   @Test
   public void testEventActionReschedulingAfterIllegalStateChangeException()
         throws Exception
   {
      // IllegalArgumentException is thrown by AIStateMonitor during processing event
      // action
      GlobalParameters.globals().set(
            MonitoringUtils.class.getName() + ".ActivityInstanceMonitorMediator",
            new AIStateMonitor());

      workflowService.startProcess(PROCESS_DEF_ID_1, null, true);
      
      // the event action is processing the first time and runs into an
      // IllegalStateChangeException
      Thread.sleep(daemonExecutionInterval);
      try
      {
         findFirstAbortedActivityInstance();
         fail();
      }
      catch (ObjectNotFoundException e)
      {
      }

      // the event action retries successfully
      Thread.sleep(daemonExecutionInterval * 3);
      ActivityInstance ai = findFirstAbortedActivityInstance();
      assertNotNull(ai);
   }

   @Test
   public void testEventActionReschedulingAfterLockingIssue() throws Exception
   {
      ProcessInstance pi = workflowService.startProcess(PROCESS_DEF_ID_2, null, true);
      ActivityInstance ai = workflowService
            .activateNextActivityInstanceForProcessInstance(pi.getOID());
      workflowService.suspend(ai.getOID(), null);

      // lock the ai so the AbortActivityEventAction will run into a ConcurrencyException
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      session.lock(ActivityInstanceBean.class, ai.getOID());

      // wait some time so the event processing starts and the AbortActivityEventAction
      // will be executed (and fails with ConcurrencyException)
      Thread.sleep(daemonExecutionInterval * 2);

      // unlock the ai so AbortActivityEventAction can succeed
      session.rollback();
      Thread.sleep(daemonExecutionInterval * 2);

      // the concurrency action is propagated to the event daemon,
      // the event action is retried, succeeds and the process IS aborted
      ActivityInstance aiAborted = findFirstAbortedActivityInstance();
      assertNotNull(aiAborted);
   }

   private ActivityInstance findFirstAbortedActivityInstance()
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery
            .findInState(ActivityInstanceState.Aborted);
      return queryService.findFirstActivityInstance(aiQuery);
   }

   // an activity monitor that helps reproducing illegal state change during event action
   // processing
   private static final class AIStateMonitor implements IActivityInstanceMonitor
   {
      private boolean hasThrownException = false;

      @Override
      public void activityInstanceStateChanged(IActivityInstance activity, int newState)
      {
         if (newState == ActivityInstanceState.ABORTING)
         {
            if (!hasThrownException)
            {
               hasThrownException = true;
               throw new IllegalStateChangeException(ActivityInstanceState.Completed,
                     ActivityInstanceState.Aborting);
            }
         }
      }
   }
}
