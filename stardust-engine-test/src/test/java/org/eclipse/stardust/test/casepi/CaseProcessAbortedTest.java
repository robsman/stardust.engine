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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains tests for the <i>Case Process Instance</i> functionality.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class CaseProcessAbortedTest
{
   public static final String MODEL_NAME = "CaseModelAborted";
   private static final String CASE_PROCESS1 = new QName(MODEL_NAME, "CaseProcess1").toString();
   private static final String CASE_PROCESS2 = new QName(MODEL_NAME, "CaseProcess2").toString();
   private static final String CASE_PROCESS3 = new QName(MODEL_NAME, "CaseProcess3").toString();   
   private static final String CASE_PROCESS4 = new QName(MODEL_NAME, "CaseProcess4").toString();

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
   }

   /**
    * Tests one case aborted, case PI is completed
    */
   @Test
   public void testCaseActive1() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      abortNext(caseProcess1);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
   }

   /**
    * Tests one case completed, case PI is completed
    */
   @Test
   public void testCaseActive2() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      completeNext(caseProcess1);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
   }

   /**
    * Tests two case, first aborted, second completed, case PI is completed
    */
   @Test   
   public void testCaseActive3() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);
      ProcessInstance caseProcess2 = wfService.startProcess(CASE_PROCESS2, null, true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      abortNext(caseProcess1);
      completeNext(caseProcess2);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(caseProcess2.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
   }

   /**
    * Tests two case, first completed, second aborted, case PI is completed
    */   
   @Test   
   public void testCaseActive4() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);
      ProcessInstance caseProcess2 = wfService.startProcess(CASE_PROCESS2, null, true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      completeNext(caseProcess1);
      abortNext(caseProcess2);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(caseProcess2.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
   }

   /**
    * Tests one case aborted, case PI is completed
    */
   @Test
   public void testCaseActive5() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      abortNext(caseProcess1);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
   }
   
   /**
    * Tests two case, first aborted, second aborted, case PI is completed
    */      
   @Test   
   public void testCaseActive6() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);
      ProcessInstance caseProcess2 = wfService.startProcess(CASE_PROCESS2, null, true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      abortNext(caseProcess1);
      abortNext(caseProcess2);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(caseProcess2.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
   }
   
   /**
    * Tests two case, first aborted, case PI is active
    */         
   @Test   
   public void testCaseActive7() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);
      ProcessInstance caseProcess2 = wfService.startProcess(CASE_PROCESS2, null, true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      abortNext(caseProcess1);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Active);
   }

   /**
    * Tests one case, case AI is suspended, then case completed, case AI is completed
    */   
   @Test
   public void testCaseActive8() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      ActivityInstance rootCaseProcessActivityInstance = findFirstActivityInstance(rootCaseProcess.getOID());
      
      ActivityInstanceStateBarrier.instance().await(rootCaseProcessActivityInstance.getOID(), ActivityInstanceState.Suspended);
      
      completeNext(caseProcess1);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
      ActivityInstanceStateBarrier.instance().await(rootCaseProcessActivityInstance.getOID(), ActivityInstanceState.Completed);
   }
   
   /**
    * Tests 1, but 2 times after
    */
   @Test
   public void testCaseActive9() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case_TestCase", null, members);
      assertNotNull(rootCaseProcess);

      abortNext(caseProcess1);

      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Completed);
      
      ProcessInstanceQuery query = ProcessInstanceQuery.findCaseByName("Case_TestCase");
      ProcessInstances allProcessInstances = queryService.getAllProcessInstances(query);
      assertEquals(1, allProcessInstances.size());

      ProcessInstance caseProcessInstance = allProcessInstances.get(0);
      if (caseProcessInstance.getState() == ProcessInstanceState.Completed
            || caseProcessInstance.getState() == ProcessInstanceState.Aborted) 
      {
         caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);
         long[] members2 = {caseProcess1.getOID()};         
         caseProcessInstance = wfService.createCase("Case_TestCase", null, members2);         
      }
      else
      {
         caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);
         long[] members2 = {caseProcess1.getOID()};                  
         wfService.joinCase(caseProcessInstance.getOID(), members2);
      }
      
      abortNext(caseProcess1);
      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      ProcessInstanceStateBarrier.instance().await(caseProcessInstance.getOID(), ProcessInstanceState.Completed);      
   }

   /**
    * Tests case is created by application, case PI is completed
    */
   @Test
   public void testCaseActive10() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS4, null, true);
      completeNext(caseProcess1);
      completeNext(caseProcess1);

      ProcessInstance caseProcessInstance = null;      
      ProcessInstanceQuery query = ProcessInstanceQuery.findCaseByName("CaseProcess");      
      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(query);
      if(allProcessInstances != null && allProcessInstances.size() != 0)
      {
         caseProcessInstance = allProcessInstances.get(0);
      }
      assertNotNull(caseProcessInstance);
      
      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Completed);
      ProcessInstanceStateBarrier.instance().await(caseProcessInstance.getOID(), ProcessInstanceState.Completed);
   }   

   /**
    * Tests case is created by application, and will be removed after from root case PI
    */
   @Test
   @Ignore
   public void testCaseActive11() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS3, null, true);
      completeNext(caseProcess1);
      
      ProcessInstance caseProcessInstance = null;      
      ProcessInstanceQuery query = ProcessInstanceQuery.findCaseByName("CaseProcess");      
      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(query);
      if(allProcessInstances != null && allProcessInstances.size() != 0)
      {
         caseProcessInstance = allProcessInstances.get(0);
      }
      assertNotNull(caseProcessInstance);
            
      ProcessInstanceStateBarrier.instance().await(caseProcessInstance.getOID(), ProcessInstanceState.Aborted);
   }   
   
   /**
    * Tests 1, and detach -> aborted state
    */
   @Test
   public void testCaseActive12() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess(CASE_PROCESS1, null, true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);

      wfService.leaveCase(rootCaseProcess.getOID(), members);
      
      ProcessInstanceStateBarrier.instance().await(rootCaseProcess.getOID(), ProcessInstanceState.Aborted);
   }
      
   private ActivityInstance findFirstActivityInstance(long processInstanceOID)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findForProcessInstance(processInstanceOID);
      return queryService.findFirstActivityInstance(aiQuery);
   }
      
   private void abortNext(ProcessInstance pi)
   {
      ActivityInstance ai = wfService.activateNextActivityInstanceForProcessInstance(pi.getOID());
      wfService.abortActivityInstance(ai.getOID());
   }   
   
   private void completeNext(ProcessInstance pi)
   {
      ActivityInstance ai = wfService.activateNextActivityInstanceForProcessInstance(pi.getOID());
      wfService.activateAndComplete(ai.getOID(), null, null);
   }   
}