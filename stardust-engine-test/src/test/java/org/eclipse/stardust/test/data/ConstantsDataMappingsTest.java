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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.model.*;
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
   
   @Before
   public void setUp()
   {
      startProcess();
   }
      
   @Test
   public void testInDataMappingReturnsConstantForCalendarData()
   {
      testInDataMapping(DEFAULT_CALENDAR_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_CALENDAR, Calendar.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForStringData()
   {
      testInDataMapping(DEFAULT_STRING_IN_DATA_MAPPING, "Hansdampf", String.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForTimestampData()
   {
      testInDataMapping(DEFAULT_TIMESTAMP_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_TIMESTAMP, Date.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForBooleanData()
   {
      testInDataMapping(DEFAULT_BOOLEAN_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_BOOLEAN, Boolean.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForByteData()
   {
      testInDataMapping(DEFAULT_BYTE_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_BYTE, Byte.class);
   }   

   @Test
   public void testInDataMappingReturnsConstantForCharData()
   {
      testInDataMapping(DEFAULT_CHAR_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_CHAR, Character.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForDoubleData()
   {
      testInDataMapping(DEFAULT_DOUBLE_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_DOUBLE, Double.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForFloatData()
   {
      testInDataMapping(DEFAULT_FLOAT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_FLOAT, Float.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForIntData()
   {
      testInDataMapping(DEFAULT_INT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_INT, Integer.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForLongData()
   {
      testInDataMapping(DEFAULT_LONG_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_LONG, Long.class);
   }
   
   @Test
   public void testInDataMappingReturnsConstantForShortData()
   {
      testInDataMapping(DEFAULT_SHORT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_SHORT, Short.class);
   }
   
   private void testInDataMapping(final String inDataMapping, final Object expectedValue, Class classValue)
   {
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      final Object result = sf.getWorkflowService().getInDataValue(ai.getOID(), null, inDataMapping);
      
      assertThat(result, notNullValue());
      assertThat(result, equalTo(expectedValue));
      
      testMappedType(ai, inDataMapping, classValue);      
   }
   
   private void testMappedType(ActivityInstance ai, String inDataMapping, Class classValue)
   {
      Activity activity = ai.getActivity();
      List allInDataMappings = activity.getApplicationContext("default").getAllInDataMappings();
            
      DataMapping dataMapping = null;
      for(Object object : allInDataMappings)
      {
         DataMapping dm = (DataMapping) object;
         if(inDataMapping.equals(dm.getId()))
         {
            dataMapping = dm;
            break;
         }         
      }

      assertThat(dataMapping, notNullValue());
      assertThat(dataMapping.getMappedType(), equalTo(classValue));
   }
   
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_1, null, true);
      return pi.getOID();
   }
}