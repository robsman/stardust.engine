/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.query.order;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests ProcessInstanceQuery.ROOT_PROC_DEF_NAME and ProcessInstanceQuery.ROOT_PROC_DEF_ID query order.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class QueryOrderTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static String MODEL_NAME = "QueryFilterModel";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf);
   
   private int cntPD2 = 8;
   private String process = "Process";   
   private int cntPD1 = 4;
   private String process1 = "Process Definition 1";   
   private int cntPD = 6;
   private String process2 = "Process Definition 2";
      
   @Before
   public void setUp()
   {
      ProcessInstance startProcess = adminSf.getWorkflowService().startProcess("Process1", null, true);    
      ActivityInstance activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      
      startProcess = adminSf.getWorkflowService().startProcess("ProcessDefinition1", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());

      startProcess = adminSf.getWorkflowService().startProcess("ProcessDefinition2", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());

      startProcess = adminSf.getWorkflowService().startProcess("Process1", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      
      startProcess = adminSf.getWorkflowService().startProcess("ProcessDefinition2", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());

      startProcess = adminSf.getWorkflowService().startProcess("ProcessDefinition2", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      
      startProcess = adminSf.getWorkflowService().startProcess("Process1", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      
      startProcess = adminSf.getWorkflowService().startProcess("ProcessDefinition1", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      
      startProcess = adminSf.getWorkflowService().startProcess("ProcessDefinition2", null, true);    
      activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
   }

   @Test
   public void testProcessQueryRootProcessInstanceOrderWithFilter()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery().findAll();
      query.getFilter().addOrTerm()
         .or(new RootProcessInstanceFilter(null, "Process"))
         .or(new RootProcessInstanceFilter(null, "Process Definition 1"))
         .or(new RootProcessInstanceFilter(null, "Process Definition 1"));
      query.getOrderCriteria().and(ProcessInstanceQuery.ROOT_PROC_DEF_NAME);
      
      int cnt = 0;
      int cnt0 = 0;      
      int cnt1 = 0;
      int cnt2 = 0;
      
      ProcessInstances oldInstances = adminSf.getQueryService().getAllProcessInstances(query);
      for(ProcessInstance pi : oldInstances)
      {
         if(cnt < 4)
         {
            if(pi.getRootProcessInstanceName().equals(process1))
            {
               cnt1++;
            }            
         }
         else
         {
            if(pi.getRootProcessInstanceName().equals(process))
            {
               cnt0++;
            }            
         }            
         cnt++;
      }

      assertThat(cnt0, is(cntPD));                  
      assertThat(cnt1, is(cntPD1));                  
      assertThat(cnt2, is(0));                  
   }
      
   @Test
   public void testProcessQueryRootProcessInstanceOrderName()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery().findAll();
      query.getOrderCriteria().and(ProcessInstanceQuery.ROOT_PROC_DEF_NAME);
      
      int cnt = 0;
      int cnt0 = 0;      
      int cnt1 = 0;
      int cnt2 = 0;
      
      ProcessInstances oldInstances = adminSf.getQueryService().getAllProcessInstances(query);
      for(ProcessInstance pi : oldInstances)
      {
         if(cnt < 8)
         {
            if(pi.getRootProcessInstanceName().equals(process2))
            {
               cnt2++;
            }            
         }
         else if(cnt < 12)
         {
            if(pi.getRootProcessInstanceName().equals(process1))
            {
               cnt1++;
            }            
         }
         else
         {
            if(pi.getRootProcessInstanceName().equals(process))
            {
               cnt0++;
            }            
         }            
         cnt++;
      }

      assertThat(cnt0, is(cntPD));                  
      assertThat(cnt1, is(cntPD1));                  
      assertThat(cnt2, is(cntPD2));                  
   }

   @Test
   public void testProcessQueryRootProcessInstanceOrderNameAscending()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery().findAll();
      query.getOrderCriteria().and(ProcessInstanceQuery.ROOT_PROC_DEF_NAME.ascendig(true));
      
      int cnt = 0;
      int cnt0 = 0;      
      int cnt1 = 0;
      int cnt2 = 0;
      
      ProcessInstances oldInstances = adminSf.getQueryService().getAllProcessInstances(query);
      for(ProcessInstance pi : oldInstances)
      {
         if(cnt < 6)
         {
            if(pi.getRootProcessInstanceName().equals(process))
            {
               cnt0++;
            }            
         }
         else if(cnt < 10)
         {
            if(pi.getRootProcessInstanceName().equals(process1))
            {
               cnt1++;
            }            
         }
         else
         {
            if(pi.getRootProcessInstanceName().equals(process2))
            {
               cnt2++;
            }            
         }            
         cnt++;         
      }

      assertThat(cnt0, is(cntPD));                  
      assertThat(cnt1, is(cntPD1));                  
      assertThat(cnt2, is(cntPD2));                  
   }
     
   @Test
   public void testProcessQueryRootProcessInstanceOrderId()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery().findAll();
      query.getOrderCriteria().and(ProcessInstanceQuery.ROOT_PROC_DEF_ID.ascendig(false));     

      int cnt = 0;
      int cnt0 = 0;      
      int cnt1 = 0;
      int cnt2 = 0;
      
      ProcessInstances oldInstances = adminSf.getQueryService().getAllProcessInstances(query);
      for(ProcessInstance pi : oldInstances)
      {
         if(cnt < 8)
         {
            if(pi.getRootProcessInstanceName().equals(process2))
            {
               cnt2++;
            }            
         }
         else if(cnt < 12)
         {
            if(pi.getRootProcessInstanceName().equals(process1))
            {
               cnt1++;
            }            
         }
         else
         {
            if(pi.getRootProcessInstanceName().equals(process))
            {
               cnt0++;
            }            
         }            
         cnt++;
      }

      assertThat(cnt0, is(cntPD));                  
      assertThat(cnt1, is(cntPD1));                  
      assertThat(cnt2, is(cntPD2));                  
   }
      
   @Test
   public void testProcessQueryRootProcessInstanceOrderIdAscending()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery().findAll();
      query.getOrderCriteria().and(ProcessInstanceQuery.ROOT_PROC_DEF_ID.ascendig(true));      

      int cnt = 0;
      int cnt0 = 0;      
      int cnt1 = 0;
      int cnt2 = 0;
      
      ProcessInstances oldInstances = adminSf.getQueryService().getAllProcessInstances(query);
      for(ProcessInstance pi : oldInstances)
      {
         if(cnt < 6)
         {
            if(pi.getRootProcessInstanceName().equals(process))
            {
               cnt0++;
            }            
         }
         else if(cnt < 10)
         {
            if(pi.getRootProcessInstanceName().equals(process1))
            {
               cnt1++;
            }            
         }
         else
         {
            if(pi.getRootProcessInstanceName().equals(process2))
            {
               cnt2++;
            }            
         }            
         cnt++;
      }

      assertThat(cnt0, is(cntPD));                  
      assertThat(cnt1, is(cntPD1));                  
      assertThat(cnt2, is(cntPD2));                  
   }     
}