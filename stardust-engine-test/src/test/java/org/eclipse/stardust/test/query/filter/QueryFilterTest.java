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

package org.eclipse.stardust.test.query.filter;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
 * Tests RootProcessInstanceFilter.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class QueryFilterTest
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

   @Test
   public void testWorklistQueryRootProcessInstanceFilter()
   {
      ProcessInstance startProcess = adminSf.getWorkflowService().startProcess("Process1", null, true);    
      ActivityInstance activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
            
      WorklistQuery query = new WorklistQuery();
      query.where(new RootProcessInstanceFilter("{QueryFilterModel}Process1", null));
      Worklist wl = adminSf.getWorkflowService().getWorklist(query);
      assertThat(wl.size(), is(1));      
      
      query = new WorklistQuery();
      query.where(new RootProcessInstanceFilter("Process1", null));
      query.where(new RootProcessInstanceFilter(null, "Process"));      
      wl = adminSf.getWorkflowService().getWorklist(query);
      assertThat(wl.size(), is(1));      
      
      query = new WorklistQuery();
      query.where(new RootProcessInstanceFilter(null, "Sub"));
      wl = adminSf.getWorkflowService().getWorklist(query);
      assertThat(wl.size(), is(0));      
      
      query = new WorklistQuery();
      query.where(new RootProcessInstanceFilter(null, "Process"));      
      wl = adminSf.getWorkflowService().getWorklist(query);
      assertThat(wl.size(), is(1));            
   }   
  
   @Test
   public void testActivityInstanceQueryRootProcessInstanceFilter()
   {
      ProcessInstance startProcess = adminSf.getWorkflowService().startProcess("Process1", null, true);    
      ActivityInstance activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();      
      aiQuery.where(new RootProcessInstanceFilter(null, null));      
      ActivityInstances allActivityInstances = adminSf.getQueryService().getAllActivityInstances(aiQuery);
      assertThat(allActivityInstances.size(), is(2));            
      
      aiQuery = ActivityInstanceQuery.findAll();
      aiQuery.where(new RootProcessInstanceFilter("{QueryFilterModel}Process1", "Process"));
      allActivityInstances = adminSf.getQueryService().getAllActivityInstances(aiQuery);
      assertThat(allActivityInstances.size(), is(2));            
      
      aiQuery = ActivityInstanceQuery.findAll();
      aiQuery.where(new RootProcessInstanceFilter(null, "Process"));
      aiQuery.where(new RootProcessInstanceFilter("{QueryFilterModel}Process1", null));      
      allActivityInstances = adminSf.getQueryService().getAllActivityInstances(aiQuery);
      assertThat(allActivityInstances.size(), is(2));            
      
      aiQuery = ActivityInstanceQuery.findAll();
      aiQuery.where(new RootProcessInstanceFilter(null, "Sub"));      
      allActivityInstances = adminSf.getQueryService().getAllActivityInstances(aiQuery);
      assertThat(allActivityInstances.size(), is(0));            

      aiQuery = ActivityInstanceQuery.findAll();
      aiQuery.where(new RootProcessInstanceFilter(null, "Process"));
      allActivityInstances = adminSf.getQueryService().getAllActivityInstances(aiQuery);
      assertThat(allActivityInstances.size(), is(2));            
   } 
   
   @Test
   public void testProcessInstanceQueryRootProcessInstanceFilter()
   {
      ProcessInstance startProcess = adminSf.getWorkflowService().startProcess("ProcessDefinition2", null, true);    
      
      startProcess = adminSf.getWorkflowService().startProcess("Process1", null, true);    
      ActivityInstance activateNextActivityInstanceForProcessInstance = adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      adminSf.getWorkflowService().complete(activateNextActivityInstanceForProcessInstance.getOID(), null, null);
      adminSf.getWorkflowService().activateNextActivityInstanceForProcessInstance(startProcess.getOID());
      
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();      
      piQuery.where(new RootProcessInstanceFilter(null, null));      
      ProcessInstances allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(3));            
      
      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.where(new RootProcessInstanceFilter("{QueryFilterModel}Process1", "Process"));
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(2));            
      
      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.where(new RootProcessInstanceFilter("{QueryFilterModel}Process1", null));      
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(2));            

      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.where(new RootProcessInstanceFilter(null, "Sub"));      
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(0));            
            
      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.where(new RootProcessInstanceFilter(null, "Process"));
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(2));                  
            
      // and + or
      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.where(new RootProcessInstanceFilter(null, "Process")).and(new RootProcessInstanceFilter("{QueryFilterModel}Process1", null));
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(2));                  
      
      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.where(new RootProcessInstanceFilter(null, "Process")).and(new RootProcessInstanceFilter(null, "Sub"));
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(0));                  
      
      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.getFilter().addOrTerm()
         .or(new RootProcessInstanceFilter(null, "Process"))
         .or(new RootProcessInstanceFilter(null, "Sub"));
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(2));                        
      
      piQuery = ProcessInstanceQuery.findAll();      
      piQuery.getFilter().addAndTerm()
         .and(new RootProcessInstanceFilter(null, "Process"))
         .and(new RootProcessInstanceFilter(null, "ABC"));
      allProcessInstances = adminSf.getQueryService().getAllProcessInstances(piQuery);
      assertThat(allProcessInstances.size(), is(0));                              
  }    
}