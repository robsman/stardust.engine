/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    barry.grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.data.DataModelConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
 * Tests whether CONSTANTS via in data mappings works correctly.
 * </p>
 * 
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class ConstantsDataMappingsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String PROCESS_ID_1 = "{" + CONSTANT_MODEL_NAME + "}" + "ProcessDefinition1";
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, CONSTANT_MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   private long aiOid;
   private long piOid;
   
   @Before
   public void setUp()
   {
      piOid = startProcess();
      aiOid = findFirstAliveActivityInstanceFor();
   }
      
   @Test
   public void testInDataMappingReturnsConstantForCalendarData()
   {
      testInDataMapping(DEFAULT_CALENDAR_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_CALENDAR);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForStringData()
   {
      testInDataMapping(DEFAULT_STRING_IN_DATA_MAPPING, "Hansdampf");
   }
   
   @Test
   public void testInDataMappingReturnsConstantForTimestampData()
   {
      testInDataMapping(DEFAULT_TIMESTAMP_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_TIMESTAMP);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForBooleanData()
   {
      testInDataMapping(DEFAULT_BOOLEAN_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_BOOLEAN);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForByteData()
   {
      testInDataMapping(DEFAULT_BYTE_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_BYTE);
   }   

   @Test
   public void testInDataMappingReturnsConstantForCharData()
   {
      testInDataMapping(DEFAULT_CHAR_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_CHAR);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForDoubleData()
   {
      testInDataMapping(DEFAULT_DOUBLE_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_DOUBLE);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForFloatData()
   {
      testInDataMapping(DEFAULT_FLOAT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_FLOAT);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForIntData()
   {
      testInDataMapping(DEFAULT_INT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_INT);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForLongData()
   {
      testInDataMapping(DEFAULT_LONG_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_LONG);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForShortData()
   {
      testInDataMapping(DEFAULT_SHORT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_SHORT);
   }
   
   private void testInDataMapping(final String inDataMapping, final Object expectedValue)
   {
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      final Object result = sf.getWorkflowService().getInDataValue(ai.getOID(), null, inDataMapping);
      
      assertThat(result, notNullValue());
      assertThat(result, equalTo(expectedValue));
   }
   
   
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the out data mapping does not exist.
    * </p>
    */
   /*
   public void testOutDataMappingFailDataPathNotFound()
   {
      final String outDataMapping = "Hansdamp";
      
      final String inDataMapping = "Hansdamp"; 
      
      sf.getWorkflowService().activate(aiOid);
      //sf.getWorkflowService().suspend(aiOid, data);
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(piOid);
      

      Serializable retrievedValue = sf.getWorkflowService().getInDataValue(aiOid, null, inDataMapping);
      
      
      System.err.println("++++++ " + retrievedValue);
   }
   */
   
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_1, null, true);
      return pi.getOID();
   }
   
   private long findFirstAliveActivityInstanceFor()
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(PROCESS_ID_1);
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }  
}