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

package org.eclipse.stardust.test.bo;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.StartOptions;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests BUSINESS_DATE StartOptions.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class BusinessDateTest
{
   private static String PROCESS = "{BDModel}ProcessDefinition1";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BDModel");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);
   
   @Test
   public void testBusinessDate() throws Exception
   {
      Calendar workday = Calendar.getInstance();
      workday.set(2016, Calendar.SEPTEMBER, 12);
      StartOptions options = new StartOptions(Collections.singletonMap(PredefinedConstants.BUSINESS_DATE, workday), true);

      serviceFactory.getWorkflowService().startProcess(PROCESS, options);
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(PROCESS);
      ActivityInstance ai = serviceFactory.getQueryService().findFirstActivityInstance(aiQuery);
      serviceFactory.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);      
      ProcessInstances processInstances = serviceFactory.getQueryService().getAllProcessInstances(query);
      ProcessInstance p1 = processInstances.get(0);
      ProcessInstance p2 = processInstances.get(1);
      
      Calendar descriptorValue1 = (Calendar) p1.getDescriptorValue("DataPath1");
      Calendar descriptorValue2 = (Calendar) p2.getDescriptorValue("DataPath1");
      
      assertEquals(descriptorValue1.get(Calendar.YEAR), 2016);
      assertEquals(descriptorValue1.get(Calendar.MONTH), Calendar.SEPTEMBER);
      assertEquals(descriptorValue1.get(Calendar.DAY_OF_MONTH), 12);
      assertEquals(descriptorValue2.get(Calendar.YEAR), 2016);
      assertEquals(descriptorValue2.get(Calendar.MONTH), Calendar.SEPTEMBER);
      assertEquals(descriptorValue2.get(Calendar.DAY_OF_MONTH), 12);
            
      assertEquals(isValid(workday), false);
   }
 
   public boolean isValid(Calendar workday)
   {
      ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery.findForProcess(PROCESS);
      FilterAndTerm filter = processInstanceQuery.getFilter().addAndTerm();
      filter.add(DataFilter.isEqual(PredefinedConstants.BUSINESS_DATE, "", workday));
      long count = serviceFactory.getQueryService().getProcessInstancesCount(processInstanceQuery);
            
      if (count > 0)
      {
         return false;
      }
      return true;
   }
}