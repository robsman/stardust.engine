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


package org.eclipse.stardust.test.casepi;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains tests for the <i>Case Process Instance</i> functionality,
 * which allows for grouping process instances (refer to the Stardust documentation
 * for details about <i>Case Process Instances</i>).
 * </p>
 *
 * @author Roland.Stamm
 */
public class CaseProcessAbortTest
{
   public static final String MODEL_NAME = "CaseModelAbort";
   private static final String CASE_PROCESS1 = new QName(MODEL_NAME, "CaseProcess1").toString();
   private static final String CASE_PROCESS2 = new QName(MODEL_NAME, "CaseProcess2").toString();

   private static final String U1 = "u1";

   private static final UsernamePasswordPair USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private WorkflowService wfService;
   private QueryService queryService;

   @Before
   public void setUp()
   {
      wfService = sf.getWorkflowService();
      queryService = sf.getQueryService();

      UserHome.create(sf, U1, "Org1");
   }

   /**
    * Test abort root by AbortProcessEvent
    */
   @Test
   public void testCreateAbort() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);
      ProcessInstance caseProcess2 = wfService.startProcess(CASE_PROCESS2, null, true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      completeNext(caseProcess1);
      completeNext(caseProcess2);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(caseProcess2.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
   }

   protected void completeNext(ProcessInstance pi)
   {
      ActivityInstance ai = wfService.activateNextActivityInstanceForProcessInstance(pi.getOID());
      wfService.activateAndComplete(ai.getOID(), null, null);
   }
   
   @Test
   public void testCaseMergeCases()
   {
      ProcessInstance rootPI1 = wfService.startProcess(CASE_PROCESS1, null, true);
      ProcessInstance rootPI2 = wfService.startProcess(CASE_PROCESS2, null, true);

      long[] members = {rootPI1.getOID()};
      ProcessInstance casePi1 = wfService.createCase("Case1", "Creating Case1", members);

      long[] members2 = {rootPI2.getOID()};
      ProcessInstance casePi2 = wfService.createCase("Case2", "Creating Case2", members2);

      long[] srcGroups = {casePi2.getOID()};

      wfService.mergeCases(casePi1.getOID(), srcGroups, null);
   }
}