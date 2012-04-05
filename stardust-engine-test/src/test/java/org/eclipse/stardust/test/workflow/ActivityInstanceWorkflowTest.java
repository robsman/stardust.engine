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

import static junit.framework.Assert.assertEquals;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.DEFAULT_ROLE_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.PD_1_ID;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
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
   private final ClientServiceFactory defaultRoleSf = new ClientServiceFactory(DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_USER_ID);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(defaultRoleSf);
   
   @Before
   public void setUp()
   {
      UserHome.create(adminSf, DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_ID);
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
      final Worklist wlBefore = defaultRoleSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      assertEquals(0, wlBefore.size());
      final Worklist participantWlBefore = (Worklist) wlBefore.getSubWorklists().next();
      assertEquals(1, participantWlBefore.size());
      assertEquals(ActivityInstanceState.Suspended, ai.getState());
      
      /* activating the activity instance */
      final ActivityInstance activatedAi = defaultRoleSf.getWorkflowService().activate(ai.getOID());

      /* after activation */
      final Worklist wlAfter = defaultRoleSf.getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist());
      assertEquals(1, wlAfter.size());
      final Worklist participantWlAfter = (Worklist) wlAfter.getSubWorklists().next();
      assertEquals(0, participantWlAfter.size());
      assertEquals(ActivityInstanceState.Application, activatedAi.getState());
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
      defaultRoleSf.getWorkflowService().activate(-1);
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
      final User user = defaultRoleSf.getWorkflowService().getUser();
      UserHome.removeAllGrants(adminSf, user);
      
      final ProcessInstance pi = startProcess(PD_1_ID);
      final ActivityInstance ai = findFirstActivityInstanceFor(pi.getOID());
      
      defaultRoleSf.getWorkflowService().activate(ai.getOID());
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
      defaultRoleSf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      
      defaultRoleSf.getWorkflowService().activate(ai.getOID());
   }
   
   // TODO write test cases for complete()

   // TODO write test cases for activateAndComplete()
   
   // TODO write test cases for suspend()
   
   // TODO write test cases for hibernate()
   
   // TODO write test cases for abortActivityInstance()
   
   // TODO write test cases for getWorklist()
   
   // TODO write test cases for activateNextActivityInstance()
   
   // TODO write test cases for activateNextActivityInstanceForProcessInstance()
   
   // TODO write test cases for getActivityInstance()
   
   // TODO write test cases for setActivityInstanceAttributes()
   
   private ProcessInstance startProcess(final String processId)
   {
      return defaultRoleSf.getWorkflowService().startProcess(processId, null, true);
   }
   
   private ActivityInstance findFirstActivityInstanceFor(final long piOID)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findForProcessInstance(piOID);
      return adminSf.getQueryService().findFirstActivityInstance(aiQuery);
   }
}
