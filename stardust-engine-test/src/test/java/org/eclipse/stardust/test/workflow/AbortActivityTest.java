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

package org.eclipse.stardust.test.workflow;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests abort activity authorizations.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class AbortActivityTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String DEFAULT_ROLE_USER_ID = "u1";
   private static final String DEFAULT_ROLE_ID = "Role1";
   private static final String MODEL_NAME = "AbortActivityModel";
   private static final String PD_1_ID = "ProcessDefinition1";

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
    * <p>
    * Tests whether abort fails without authorization.</code>.
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void testAbortActivityInstanceSubHierarchy() throws Exception
   {
      userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(PD_1_ID);
      final ActivityInstance ai = adminSf.getQueryService().findFirstActivityInstance(aiQuery);
      
      userSf.getWorkflowService().abortActivityInstance(ai.getOID(), AbortScope.SubHierarchy);

      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Aborted);
      final ActivityInstance abortedAi = userSf.getWorkflowService().getActivityInstance(ai.getOID());
      assertThat(abortedAi, notNullValue());
      assertThat(abortedAi.getState(), equalTo(ActivityInstanceState.Aborted));
   }
   
   /**
    * <p>
    * Tests whether abort RootHierarchy succeeds.</code>.
    * </p>
    */
   @Test
   public void testAbortActivityInstanceRootHierarchy() throws Exception
   {
      userSf.getWorkflowService().startProcess(PD_1_ID, null, true);
      
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(PD_1_ID);
      final ActivityInstance ai = adminSf.getQueryService().findFirstActivityInstance(aiQuery);
      
      userSf.getWorkflowService().abortActivityInstance(ai.getOID(), AbortScope.RootHierarchy);

      ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Aborted);
      final ActivityInstance abortedAi = userSf.getWorkflowService().getActivityInstance(ai.getOID());
      assertThat(abortedAi, notNullValue());
      assertThat(abortedAi.getState(), equalTo(ActivityInstanceState.Aborted));
   }   
}