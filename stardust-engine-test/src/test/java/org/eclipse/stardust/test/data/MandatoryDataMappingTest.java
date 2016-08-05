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
package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether mandatoryDataMapping flag works correctly.
 * </p>
 * 
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class MandatoryDataMappingTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "TestDM");
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   private long aiOid1;
   private long aiOid2;
   private long startProcess1;
   private long startProcess2;

   private String dataMapping3 = "PrimitiveData3";   
   private String dataMapping2 = "PrimitiveData2";
   private String dataMapping1 = "PrimitiveData1";
      
   @Before
   public void setUp()
   {
      startProcess1 = startProcess1();
      startProcess2 = startProcess2();
      
      aiOid1 = findFirstAliveActivityInstanceFor1();
      aiOid2 = findFirstAliveActivityInstanceFor2();      
   }
   
   /**
    * <p>
    * Tests with process started with data values.
    * </p>
    */
   @Test
   public void test1()
   {
      sf.getWorkflowService().activate(aiOid1);            
      Map<String, Serializable> mapValue = sf.getWorkflowService().getInDataValues(aiOid1, null, null);
      Object retrievedValue1 = mapValue.get(dataMapping1);      
      Object retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2,77);

      Map data = new HashMap();
      data.put(dataMapping2, 5000);      
      sf.getWorkflowService().complete(aiOid1, null, data);
      
      ActivityInstance nextAI = sf.getWorkflowService().activateNextActivityInstance(aiOid1);
      mapValue = sf.getWorkflowService().getInDataValues(nextAI.getOID(), null, null);
      retrievedValue1 = mapValue.get(dataMapping1);
      retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2, 5000);      

      data = new HashMap();
      data.put(dataMapping3, 5000);            
      sf.getWorkflowService().complete(nextAI.getOID(), null, data);
      
      nextAI = sf.getWorkflowService().activateNextActivityInstance(aiOid1);
      mapValue = sf.getWorkflowService().getInDataValues(nextAI.getOID(), null, null);
      retrievedValue1 = mapValue.get(dataMapping1);
      retrievedValue2 = mapValue.get(dataMapping2);
      Object retrievedValue3 = mapValue.get(dataMapping3);      
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2, 5000);      
      assertThat(retrievedValue3, notNullValue());
      assertEquals(retrievedValue3, 5000);            
   }

   /**
    * <p>
    * Tests with process started without data values.
    * </p>
    */   
   @Test
   public void test2()
   {
      sf.getWorkflowService().activate(aiOid2);
      Map<String, Serializable> mapValue = sf.getWorkflowService().getInDataValues(aiOid2, null, null);
      Object retrievedValue1 = mapValue.get(dataMapping1);
      Object retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, is(nullValue()));

      Map data = new HashMap();
      data.put(dataMapping2, 5000);      
      sf.getWorkflowService().complete(aiOid2, null, data);
      
      ActivityInstance nextAI = sf.getWorkflowService().activateNextActivityInstance(aiOid2);
      mapValue = sf.getWorkflowService().getInDataValues(nextAI.getOID(), null, null);
      retrievedValue1 = mapValue.get(dataMapping1);
      retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2, 5000);
   }

   /**
    * <p>
    * Tests with process started with data values and suspended.
    * </p>
    */
   @Test
   public void test3()
   {
      sf.getWorkflowService().activate(aiOid1);     
      sf.getWorkflowService().suspend(aiOid1, null);
      sf.getWorkflowService().activate(aiOid1);
            
      Map<String, Serializable> mapValue = sf.getWorkflowService().getInDataValues(aiOid1, null, null);
      Object retrievedValue1 = mapValue.get(dataMapping1);      
      Object retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2,77);

      Map data = new HashMap();
      data.put(dataMapping2, 5000);      
      sf.getWorkflowService().complete(aiOid1, null, data);
      
      ActivityInstance nextAI = sf.getWorkflowService().activateNextActivityInstance(aiOid1);
      mapValue = sf.getWorkflowService().getInDataValues(nextAI.getOID(), null, null);
      retrievedValue1 = mapValue.get(dataMapping1);
      retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2, 5000);      

      data = new HashMap();
      data.put(dataMapping3, 5000);            
      sf.getWorkflowService().complete(nextAI.getOID(), null, data);
      
      nextAI = sf.getWorkflowService().activateNextActivityInstance(aiOid1);
      mapValue = sf.getWorkflowService().getInDataValues(nextAI.getOID(), null, null);
      retrievedValue1 = mapValue.get(dataMapping1);
      retrievedValue2 = mapValue.get(dataMapping2);
      Object retrievedValue3 = mapValue.get(dataMapping3);      
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2, 5000);      
      assertThat(retrievedValue3, notNullValue());
      assertEquals(retrievedValue3, 5000);            
   }
      
   /**
    * <p>
    * Tests with process started without data values and suspended.
    * </p>
    */   
   @Test
   public void test4()
   {
      sf.getWorkflowService().activate(aiOid2);
      sf.getWorkflowService().suspend(aiOid2, null);
      sf.getWorkflowService().activate(aiOid2);
                  
      Map<String, Serializable> mapValue = sf.getWorkflowService().getInDataValues(aiOid2, null, null);
      Object retrievedValue1 = mapValue.get(dataMapping1);
      Object retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, is(nullValue()));

      Map data = new HashMap();
      data.put(dataMapping2, 5000);      
      sf.getWorkflowService().complete(aiOid2, null, data);
      
      ActivityInstance nextAI = sf.getWorkflowService().activateNextActivityInstance(aiOid2);
      mapValue = sf.getWorkflowService().getInDataValues(nextAI.getOID(), null, null);
      retrievedValue1 = mapValue.get(dataMapping1);
      retrievedValue2 = mapValue.get(dataMapping2);
      assertThat(retrievedValue1, notNullValue());
      assertEquals(retrievedValue1, 0);
      assertThat(retrievedValue2, notNullValue());
      assertEquals(retrievedValue2, 5000);
   }
      
   private long startProcess2()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess("{TestDM}ProcessDefinition1", null, true);
      return pi.getOID();
   }
      
   private long startProcess1()
   {
      Map data = new HashMap();
      data.put(dataMapping2, 77);
            
      final ProcessInstance pi = sf.getWorkflowService().startProcess("{TestDM}ProcessDefinition1", data, true);
      return pi.getOID();
   }
   
   private long findFirstAliveActivityInstanceFor1()
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(startProcess1, "ManualActivity1");
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }
   
   private long findFirstAliveActivityInstanceFor2()
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(startProcess2, "ManualActivity1");
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }   
}