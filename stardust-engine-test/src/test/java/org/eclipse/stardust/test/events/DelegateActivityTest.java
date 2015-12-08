/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.events;

import static org.eclipse.stardust.engine.api.query.DeployedModelQuery.findForId;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import org.eclipse.stardust.test.api.util.UserHome;
import static org.junit.Assert.assertEquals;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DaemonHome;
import org.eclipse.stardust.test.api.util.DaemonHome.DaemonType;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class DelegateActivityTest
{
   private static final String OGRANIZATION_USER_ID = "u1";
   private static final String OGRANIZATION_ID = "Organization_1";   
   private static final String MODEL_NAME = "DelegateActivity";   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory userSf = new TestServiceFactory(new UsernamePasswordPair(OGRANIZATION_USER_ID, OGRANIZATION_USER_ID));   
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory).around(userSf);

   
   private AdministrationService administrationService;
   private QueryService queryService;
   
   private Participant org;
   private Participant role2;
   private Participant role1;
      
   @Before
   public void setup()
   {
      queryService = serviceFactory.getQueryService();
      administrationService = serviceFactory.getAdministrationService();
      
      UserHome.create(serviceFactory, OGRANIZATION_USER_ID, OGRANIZATION_ID);

      Models models = queryService.getModels(findForId(MODEL_NAME));
      
      org = queryService.getParticipant(models.get(0)
            .getModelOID(), OGRANIZATION_ID);

      role2 = queryService.getParticipant(models.get(0)
            .getModelOID(), "Role_2");
      
      role1 = queryService.getParticipant(models.get(0)
            .getModelOID(), "Role_1");
            
      DaemonHome.startDaemon(administrationService, DaemonType.EVENT_DAEMON);
   }

   @After
   public void teardown()
   {
      DaemonHome.stopAllRunningDaemons(serviceFactory.getAdministrationService());
   }

   /**
    * <p>
    * Tests Delegation.
    * </p>
    * 
    * @author Barry.Grotjahn
    * @version $Revision$
    */
   @Test
   public void testDelegateActivity() throws Exception
   {
      final PerformingParticipantFilter filterOrg = PerformingParticipantFilter.forParticipant(
            DepartmentInfoDetails.getParticipant(null, (ModelParticipantInfo) org));

      final PerformingParticipantFilter filterRole1 = PerformingParticipantFilter.forParticipant(
            DepartmentInfoDetails.getParticipant(null, (ModelParticipantInfo) role1));
            
      final PerformingParticipantFilter filterRole2 = PerformingParticipantFilter.forParticipant(
            DepartmentInfoDetails.getParticipant(null, (ModelParticipantInfo) role2));
      
      WorkflowService wfService = userSf.getWorkflowService();
      wfService.startProcess("ProcessDefinition_1", null, true);
      Thread.sleep(10000);
            
      assertEquals(0, getActivityInstancesCount(filterOrg));
      assertEquals(0, getActivityInstancesCount(filterRole1));
      assertEquals(1, getActivityInstancesCount(filterRole2));
   }

   private long getActivityInstancesCount(final FilterCriterion filter)
   {
      final ActivityInstanceQuery ai = new ActivityInstanceQuery();
      ai.where(filter);
      return queryService.getActivityInstancesCount(ai);
   }   
}