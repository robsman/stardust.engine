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

import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;

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
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.eclipse.stardust.test.api.UserHome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests basic functionality regarding the workflow of activity instances.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ActivityInstanceWorkflowTest extends LocalJcrH2Test
{
   private static final String DEFAULT_ROLE_USER_ID = "u1";
   
   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(adminSf, MODEL_NAME);
   private final ClientServiceFactory userSf = new ClientServiceFactory(DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_USER_ID);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
   
   // TODO write test cases for abortActivityInstance()
   
   // TODO write test cases for getWorklist()
   
   // TODO write test cases for activateNextActivityInstance()
   
   // TODO write test cases for activateNextActivityInstanceForProcessInstance()
   
   /**
    * <p>
    * Tests whether the retrieval of an activity instance works correctly.
    * </p>
    */
   @Test
   public void testGetActivityInstance()
   {
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
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
      
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
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
   
   private ActivityInstance findFirstActivityInstanceFor(final long piOID)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findForProcessInstance(piOID);
      return adminSf.getQueryService().findFirstActivityInstance(aiQuery);
   }
}
