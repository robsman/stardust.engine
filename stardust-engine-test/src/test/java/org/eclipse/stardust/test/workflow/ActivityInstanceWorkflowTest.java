/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
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
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.DEFAULT_ROLE_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MANUAL_ACTIVITY_1_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MANUAL_ACTIVITY_2_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MY_STRING_DATA_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MY_STRING_IN_DATA_PATH_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.PD_1_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.PD_3_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributesImpl;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests basic functionality regarding the workflow of activity instances.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class ActivityInstanceWorkflowTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String DEFAULT_ROLE_USER_ID = "u1";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory userSf = new TestServiceFactory(new UsernamePasswordPair(DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_USER_ID));

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf)
                                          .around(userSf);

   @Before
   public void setUp()
   {
      UserHome.create(adminSf, DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_ID);
   }

   /**
    * Tests whether an interactive activity instance is suspended
    * after the process instance has been started.
    */
   @Test
   public void testInteractiveActivityInstanceIsSuspended()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      assertThat(ai.getState(), equalTo(ActivityInstanceState.Suspended));
   }

   /**
    * <p>
    * Tests whether the following happens when activating an activity instance:
    * <ul>
    *   <li>adding the activity instance to the user's worklist</li>
    *   <li>removing the activity instance from the original worklist</li>
    *   <li>state change to 'APPLICATION'</li>
    * </ul>
    * </p>
    */
   @Test
   public void testActivate()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      /* before activation */
      final Worklist wlBefore = userSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      assertThat(wlBefore.size(), is(0));
      final Worklist participantWlBefore = (Worklist) wlBefore.getSubWorklists().next();
      assertThat(participantWlBefore.size(), is(1));
      assertThat(ai.getState(), equalTo(ActivityInstanceState.Suspended));

      /* activating the activity instance */
      final ActivityInstance activatedAi = userSf.getWorkflowService().activate(ai.getOID());

      /* after activation */
      final Worklist wlAfter = userSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      assertThat(wlAfter.size(), is(1));
      final Worklist participantWlAfter = (Worklist) wlAfter.getSubWorklists().next();
      assertThat(participantWlAfter.size(), is(0));
      assertThat(activatedAi.getState(), equalTo(ActivityInstanceState.Application));
   }

   /**
    * <p>
    * Tests whether the activation throws the correct exception when the activity instance cannot
    * be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testActivateFailAiNotFound()
   {
      userSf.getWorkflowService().activate(-1);
   }

   /**
    * <p>
    * Tests whether the activation throws the correct exception when the user has insufficient
    * grants.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testActivateFailAccessForbidden()
   {
      final User user = userSf.getWorkflowService().getUser();
      UserHome.removeAllGrants(adminSf, user);

      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      userSf.getWorkflowService().activate(ai.getOID());
   }

   /**
    * <p>
    * Tests whether the activation throws the correct exception when the activity instance
    * is already terminated.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testActivateFailAiTerminated()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      userSf.getWorkflowService().activate(ai.getOID());
   }

   /**
    * <p>
    * Tests whether the completion of an activity instance works correctly.
    * </p>
    */
   @Test
   public void testComplete()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai.getOID());

      final ActivityInstance completedAi = userSf.getWorkflowService().complete(ai.getOID(), null, null);
      assertThat(completedAi.getActivity().getId(), equalTo(BasicWorkflowModelConstants.MANUAL_ACTIVITY_1_ID));
      assertThat(completedAi.getState(), equalTo(ActivityInstanceState.Completed));
   }

   /**
    * <p>
    * Tests whether the completion of an activity instance works correctly,
    * especially passing a value for the out data mapping.
    * </p>
    */
   @Test
   public void testCompletePassValue()
   {
      final String testText = "This is a test";

      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai.getOID());

      final Map<String, ?> data = Collections.singletonMap(MY_STRING_DATA_ID, testText);
      final ActivityInstance completedAi = userSf.getWorkflowService().complete(ai.getOID(), null, data);
      assertThat(completedAi.getActivity().getId(), equalTo(BasicWorkflowModelConstants.MANUAL_ACTIVITY_1_ID));
      assertThat(completedAi.getState(), equalTo(ActivityInstanceState.Completed));
      final String retrievedString = (String) userSf.getWorkflowService().getInDataPath(pi.getOID(), MY_STRING_IN_DATA_PATH_ID);
      assertThat(retrievedString, equalTo(testText));
   }

   /**
    * <p>
    * Tests whether the completion of an activity instance works correctly,
    * especially when indicating that the next activity instance should be activated afterwards.
    * </p>
    */
   @Test
   public void testCompleteActivateNextActivityInstance()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai.getOID());

      final ActivityCompletionLog aiLog = userSf.getWorkflowService().complete(ai.getOID(), null, null, WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE);
      final ActivityInstance completedAi = aiLog.getCompletedActivity();
      assertThat(completedAi.getActivity().getId(), equalTo(MANUAL_ACTIVITY_1_ID));
      assertThat(completedAi.getState(), equalTo(ActivityInstanceState.Completed));
      final ActivityInstance nextAi = aiLog.getNextForUser();
      assertThat(nextAi.getActivity().getId(), equalTo(MANUAL_ACTIVITY_2_ID));
      assertThat(nextAi.getState(), equalTo(ActivityInstanceState.Application));
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * is not active during activity instance completion.
    * </p>
    */
   @Test(expected = IllegalStateChangeException.class)
   public void testCompleteFailActivityInstanceNotActive()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      userSf.getWorkflowService().complete(ai.getOID(), null, null);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * cannot be found during activity instance completion.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testCompleteFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().complete(-1, null, null);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the passed value
    * is invalid during activity instance completion.
    * </p>
    */
   @Test(expected = InvalidValueException.class)
   public void testCompleteFailInvalidValue()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai.getOID());

      final Map<String, ?> data = Collections.singletonMap(MY_STRING_DATA_ID, -1);
      userSf.getWorkflowService().complete(ai.getOID(), null, data);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * cannot be completed due to insuffient grants of the performing user.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testCompleteFailInsufficientGrants()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      adminSf.getWorkflowService().activate(ai.getOID());

      final User user = userSf.getUserService().getUser();
      UserHome.removeAllGrants(adminSf, user);
      userSf.getWorkflowService().complete(ai.getOID(), null, null);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * is already terminated during activity instance completion.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testCompleteFailActivityInstanceTerminated()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      userSf.getWorkflowService().complete(ai.getOID(), null, null);
   }

   /**
    * <p>
    * Tests whether the activation and completion of an activity instance works correctly.
    * </p>
    */
   @Test
   public void testActivateAndComplete()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      final ActivityInstance completedAi = userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      assertThat(completedAi.getActivity().getId(), equalTo(BasicWorkflowModelConstants.MANUAL_ACTIVITY_1_ID));
      assertThat(completedAi.getState(), equalTo(ActivityInstanceState.Completed));
   }

   /**
    * <p>
    * Tests whether the activation and completion of an activity instance works correctly,
    * especially passing a value for the out data mapping.
    * </p>
    */
   @Test
   public void testActivateAndCompletePassValue()
   {
      final String testText = "This is a test";

      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      final Map<String, ?> data = Collections.singletonMap(MY_STRING_DATA_ID, testText);
      final ActivityInstance completedAi = userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, data);
      assertThat(completedAi.getActivity().getId(), equalTo(BasicWorkflowModelConstants.MANUAL_ACTIVITY_1_ID));
      assertThat(completedAi.getState(), equalTo(ActivityInstanceState.Completed));
      final String retrievedString = (String) userSf.getWorkflowService().getInDataPath(pi.getOID(), MY_STRING_IN_DATA_PATH_ID);
      assertThat(retrievedString, equalTo(testText));
   }

   /**
    * <p>
    * Tests whether the activation and completion of an activity instance works correctly,
    * especially when indicating that the next activity instance should be activated afterwards.
    * </p>
    */
   @Test
   public void testActivateAndCompleteActivateNextActivityInstance()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      final ActivityCompletionLog aiLog = userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null, WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE);
      final ActivityInstance completedAi = aiLog.getCompletedActivity();
      assertThat(completedAi.getActivity().getId(), equalTo(MANUAL_ACTIVITY_1_ID));
      assertThat(completedAi.getState(), equalTo(ActivityInstanceState.Completed));
      final ActivityInstance nextAi = aiLog.getNextForUser();
      assertThat(nextAi.getActivity().getId(), equalTo(MANUAL_ACTIVITY_2_ID));
      assertThat(nextAi.getState(), equalTo(ActivityInstanceState.Application));
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * cannot be found during activity instance activation and completion.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testActivateAndCompleteFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().activateAndComplete(-1, null, null);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the passed value
    * is invalid during activity instance activation and completion.
    * </p>
    */
   @Test(expected = InvalidValueException.class)
   public void testActivateAndCompleteFailInvalidValue()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      final Map<String, ?> data = Collections.singletonMap(MY_STRING_DATA_ID, -1);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, data);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * cannot be activated and completed due to insuffient grants of the performing user.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testActivateAndCompleteFailInsufficientGrants()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      final User user = userSf.getUserService().getUser();
      UserHome.removeAllGrants(adminSf, user);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * is already terminated during activity instance activation and completion.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testActivateAndCompleteFailActivityInstanceTerminated()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
   }

   /**
    * <p>
    * Tests whether suspending of an activity instance works correctly.
    * </p>
    */
   @Test
   public void testSuspend()
   {
      final WorklistQuery wlQuery = WorklistQuery.findCompleteWorklist();
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      final ActivityInstance activatedAi = userSf.getWorkflowService().activate(ai.getOID());
      final Worklist wlBefore = userSf.getWorkflowService().getWorklist(wlQuery);
      assertThat(wlBefore.size(), is(1));
      final Worklist participantWl2 = (Worklist) wlBefore.getSubWorklists().next();
      assertThat(participantWl2.size(), is(0));
      assertThat(activatedAi.getState(), equalTo(ActivityInstanceState.Application));

      final ActivityInstance suspendedAi = userSf.getWorkflowService().suspend(ai.getOID(), null);

      final Worklist wlAfter = userSf.getWorkflowService().getWorklist(wlQuery);
      assertThat(wlAfter.size(), is(0));
      final Worklist participantWl3 = (Worklist) wlAfter.getSubWorklists().next();
      assertThat(participantWl3.size(), is(1));
      assertThat(suspendedAi.getState(), equalTo(ActivityInstanceState.Suspended));
   }

   /**
    * <p>
    * Tests whether suspending of an activity instance works correctly,
    * especially passing a context data.
    * </p>
    */
   @Test
   public void testSuspendPassContextData()
   {
      final String testText = "This is a test.";

      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai.getOID());

      final Map<String, ?> data = Collections.singletonMap(MY_STRING_DATA_ID, testText);
      final ContextData ctxData = new ContextData(null, data);
      userSf.getWorkflowService().suspend(ai.getOID(), ctxData);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      final String retrievedText = (String) userSf.getWorkflowService().getInDataPath(pi.getOID(), MY_STRING_IN_DATA_PATH_ID);
      assertThat(retrievedText, equalTo(testText));
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * is already terminated during activity instance suspension.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testSuspendFailActivityInstanceTerminated()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      userSf.getWorkflowService().suspend(ai.getOID(), null);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the performing user
    * has insufficient grants during activity instance suspension.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testSuspendFailInsufficientGrants()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      adminSf.getWorkflowService().activate(ai.getOID());

      final User user = userSf.getUserService().getUser();
      UserHome.removeAllGrants(adminSf, user);
      userSf.getWorkflowService().suspend(ai.getOID(), null);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testSuspendFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().suspend(-1, null);
   }

   /**
    * <p>
    * Tests whether hibernation of an activity instance works correctly.
    * </p>
    */
   @Test
   public void testHibernate()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      final ActivityInstance hibernatedAi = userSf.getWorkflowService().hibernate(ai.getOID());

      assertThat(hibernatedAi.getState(), equalTo(ActivityInstanceState.Hibernated));
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * is already terminated during activity instance hibernation.
    * </p>
    */
   @Test(expected = IllegalStateChangeException.class)
   public void testHibernateFailActivityInstanceTerminated()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      userSf.getWorkflowService().hibernate(ai.getOID());
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance
    * cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testHibernateFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().hibernate(-1);
   }

   /**
    * <p>
    * Tests whether abortion of an activity instance works correctly for
    * abort scope <code>AbortScope.RootHierarchy</code>.
    * </p>
    */
   @Test
   public void testAbortActivityInstanceAbortScopeRootHierarchy() throws Exception
   {
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      adminSf.getWorkflowService().abortActivityInstance(ai.getOID(), AbortScope.RootHierarchy);

      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Aborted);
      final ActivityInstance abortedAi = userSf.getWorkflowService().getActivityInstance(ai.getOID());
      assertThat(abortedAi, notNullValue());
      assertThat(abortedAi.getState(), equalTo(ActivityInstanceState.Aborted));

      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Aborted);
      final ProcessInstance abortedPi = userSf.getWorkflowService().getProcessInstance(pi.getOID());
      assertThat(abortedPi, notNullValue());
      assertThat(abortedPi.getState(), equalTo(ProcessInstanceState.Aborted));
   }

   /**
    * <p>
    * Tests whether abortion of an activity instance works correctly for
    * abort scope <code>AbortScope.SubHierarchy</code>.
    * </p>
    */
   @Test
   public void testAbortActivityInstanceAbortScopeSubHierarchy() throws Exception
   {
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      userSf.getWorkflowService().abortActivityInstance(ai.getOID(), AbortScope.SubHierarchy);

      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Aborted);
      final ActivityInstance abortedAi = userSf.getWorkflowService().getActivityInstance(ai.getOID());
      assertThat(abortedAi, notNullValue());
      assertThat(abortedAi.getState(), equalTo(ActivityInstanceState.Aborted));

      final ProcessInstance abortedPi = userSf.getWorkflowService().getProcessInstance(pi.getOID());
      assertThat(abortedPi, notNullValue());
      assertThat(abortedPi.getState(), equalTo(ProcessInstanceState.Active));
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown if the activity instance
    * does not allow to be aborted during activity instance abortion.
    * </p>
    *
    * <p>
    * TODO re-enable as soon as CRNT-20630 has been resolved
    * </p>
    */
   @Ignore("CRNT-20630")
   @Test(expected = AccessForbiddenException.class)
   public void testActivateActivityInstanceFailNotAbortable()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai1 = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai1.getOID(), null, null);
      final ActivityInstance ai2 = findFirstAliveActivityInstanceFor(PD_1_ID);

      assertThat(ai2.getActivity().isAbortable(), is(false));
      adminSf.getWorkflowService().abortActivityInstance(ai2.getOID(), AbortScope.SubHierarchy);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown if the performing user
    * is not allowed to abort the activity instance.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testActivateActivityInstanceFailInsufficientGrants()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai1 = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai1.getOID(), null, null);
      final ActivityInstance ai2 = findFirstAliveActivityInstanceFor(PD_1_ID);

      userSf.getWorkflowService().abortActivityInstance(ai2.getOID(), AbortScope.SubHierarchy);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown if the activity instance
    * is already terminated during activity instance abortion.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testActivateActivityInstanceFailActivityInstanceTerminated()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      userSf.getWorkflowService().abortActivityInstance(ai.getOID(), AbortScope.SubHierarchy);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown if the activity instance
    * cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testActivateActivityInstanceFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().abortActivityInstance(-1);
   }

   /**
    * <p>
    * Tests whether the retrieval of a worklist works correctly:
    * Requesting a private worklist.
    * </p>
    */
   @Test
   public void testGetWorklistPrivateWorklist()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai1 = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai1.getOID());

      startProcess(PD_3_ID);
      final ActivityInstance ai2 = findFirstAliveActivityInstanceFor(PD_3_ID);
      userSf.getWorkflowService().activate(ai2.getOID());

      final Worklist worklist = userSf.getWorkflowService().getWorklist(WorklistQuery.findPrivateWorklist());
      assertThat(worklist.size(), is(2));
   }

   /**
    * <p>
    * Tests whether the retrieval of a worklist works correctly:
    * Requesting a private worklist with a limit.
    * </p>
    */
   @Test
   public void testGetWorklistPrivateWorklistLimit()
   {
      final int limit = 1;

      startProcess(PD_1_ID);
      final ActivityInstance ai1 = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai1.getOID());

      startProcess(PD_3_ID);
      final ActivityInstance ai2 = findFirstAliveActivityInstanceFor(PD_3_ID);
      userSf.getWorkflowService().activate(ai2.getOID());

      final Worklist worklist = userSf.getWorkflowService().getWorklist(WorklistQuery.findPrivateWorklist(limit));
      assertThat(worklist.size(), is(1));
   }

   /**
    * <p>
    * Tests whether the retrieval of a worklist works correctly:
    * Requesting the complete worklist.
    * </p>
    */
   @Test
   public void testGetWorklistCompleteWorklist()
   {
      startProcess(PD_1_ID);

      startProcess(PD_1_ID);
      final ActivityInstance ai1 = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activate(ai1.getOID());

      startProcess(PD_3_ID);
      final ActivityInstance ai2 = findFirstAliveActivityInstanceFor(PD_3_ID);
      userSf.getWorkflowService().activate(ai2.getOID());

      final Worklist worklist = userSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      assertThat(worklist.getCumulatedSize(), is(3));
      assertThat(worklist.size(), is(2));

      final Worklist participantWorklist = (Worklist) worklist.getSubWorklists().next();
      assertThat(participantWorklist.size(), is(1));
   }

   /**
    * <p>
    * Tests whether the activation of the next activity instance
    * by an empty worklist query works correctly: does not activate anything
    * and returns <code>null</code>.
    * </p>
    */
   @Test
   public void testActivateNextActivityInstanceByEmptyWorklistQuery()
   {
      final ActivityInstance nextAi = userSf.getWorkflowService().activateNextActivityInstance(WorklistQuery.findPrivateWorklist());
      assertThat(nextAi, nullValue());
   }

   /**
    * <p>
    * Tests whether the activation of the next activity instance
    * by an non-empty worklist query works correctly: activates and
    * returns the next activity instance.
    * </p>
    */
   @Test
   public void testActivateNextActivityInstanceByNonEmptyWorklistQuery()
   {
      startProcess(PD_1_ID);

      final ActivityInstance nextAi = userSf.getWorkflowService().activateNextActivityInstance(WorklistQuery.findCompleteWorklist());
      assertThat(nextAi, notNullValue());
      assertThat(nextAi.getActivity().getId(), equalTo(MANUAL_ACTIVITY_1_ID));
      assertThat(nextAi.getState(), equalTo(ActivityInstanceState.Application));
   }

   /**
    * <p>
    * Tests whether the activation of the next activity instance
    * by activity instance oid works correctly.
    * </p>
    */
   @Test
   public void testActivateNextActivityInstanceByAiOid()
   {
      // TODO remove as soon as CRNT-29836 has been resolved
      GlobalParameters.globals().set(KernelTweakingProperties.LAST_MODIFIED_TIMESTAMP_EPSILON, 20);

      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      userSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);

      final ActivityInstance nextAi = userSf.getWorkflowService().activateNextActivityInstance(ai.getOID());
      assertThat(nextAi, notNullValue());
      assertThat(nextAi.getActivity().getId(), equalTo(MANUAL_ACTIVITY_2_ID));
      assertThat(nextAi.getState(), equalTo(ActivityInstanceState.Application));
   }

   /**
    * <p>
    * Tests whether the correct exeception is thrown if the activity instance
    * cannot be found during activity instance activation.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testActivateNextActivityInstanceByAiOidFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().activateNextActivityInstance(-1);
   }

   /**
    * <p>
    * Tests whether the activation of the next activity instance
    * by process instance oid works correctly.
    * </p>
    */
   @Test
   public void testActivateNextActivityInstanceByPiOid()
   {
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance nextAi = userSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      assertThat(nextAi, notNullValue());
      assertThat(nextAi.getActivity().getId(), equalTo(MANUAL_ACTIVITY_1_ID));
      assertThat(nextAi.getState(), equalTo(ActivityInstanceState.Application));
   }

   /**
    * <p>
    * Tests whether the correct exeception is thrown if the process instance
    * cannot be found during activity instance activation.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testActivateNextActivityInstanceByPiOidFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(-1);
   }

   /**
    * <p>
    * Tests whether the retrieval of an activity instance works correctly.
    * </p>
    */
   @Test
   public void testGetActivityInstance()
   {
      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);

      final ActivityInstance retrievedAi = userSf.getWorkflowService().getActivityInstance(ai.getOID());
      assertThat(retrievedAi, notNullValue());
      assertThat(retrievedAi, equalTo(ai));
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testGetActivityInstanceFailActivityInstanceNotFound()
   {
      userSf.getWorkflowService().getActivityInstance(-1);
   }

   /**
    * <p>
    * Tests whether setting of activity instance attributes works correctly.
    * </p>
    */
   @Test
   public void testSetActivityInstanceAttributes()
   {
      final String testText = "This is a test.";

      startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstAliveActivityInstanceFor(PD_1_ID);
      final ActivityInstanceAttributes attributes = new ActivityInstanceAttributesImpl(ai.getOID());
      attributes.addNote(testText);
      userSf.getWorkflowService().setActivityInstanceAttributes(attributes);

      final ActivityInstance retrievedAi = userSf.getWorkflowService().getActivityInstance(ai.getOID());
      final ActivityInstanceAttributes retrievedAttributes = retrievedAi.getAttributes();
      assertThat(retrievedAttributes.getNotes().size(), is(1));
      final Note retrievedNote = retrievedAttributes.getNotes().get(0);
      assertThat(retrievedNote.getText(), equalTo(testText));
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testSetActivityInstanceAttributesFailActivityInstanceNotFound()
   {
      final ActivityInstanceAttributes attributes = new ActivityInstanceAttributesImpl(-1);
      userSf.getWorkflowService().setActivityInstanceAttributes(attributes);
   }

   /**
    * <p>
    * Tests whether the correct exception is thrown when the activity instance attribute
    * is <code>null</code>.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testSetActivityInstanceAttributesFailNullAttribute()
   {
      userSf.getWorkflowService().setActivityInstanceAttributes(null);
   }

   private ProcessInstance startProcess(final String processId)
   {
      return userSf.getWorkflowService().startProcess(processId, null, true);
   }

   private ActivityInstance findFirstAliveActivityInstanceFor(final String processId)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(processId);
      return adminSf.getQueryService().findFirstActivityInstance(aiQuery);
   }
}
