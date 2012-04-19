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

import static org.eclipse.stardust.test.data.DataModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether the modeled default value for primitive process data
 * can be retrieved correctly - via in data paths as well as in data mappings.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class DefaultValuePrimitiveDataTest extends LocalJcrH2Test
{
   private final ClientServiceFactory sf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(sf, MODEL_NAME);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(sf)
                                    .around(rtConfigurer);
   
   private long piOid;
   
   @Before
   public void setUp()
   {
      piOid = startProcess();
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>Calendar</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForCalendarData()
   {
      testInDataMapping(DEFAULT_CALENDAR_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_CALENDAR);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>String</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForStringData()
   {
      testInDataMapping(DEFAULT_STRING_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_STRING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>Timestamp</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForTimestampData()
   {
      testInDataMapping(DEFAULT_TIMESTAMP_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_TIMESTAMP);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>boolean</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForBooleanData()
   {
      testInDataMapping(DEFAULT_BOOLEAN_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_BOOLEAN);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>byte</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForByteData()
   {
      testInDataMapping(DEFAULT_BYTE_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_BYTE);
   }   

   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>char</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForCharData()
   {
      testInDataMapping(DEFAULT_CHAR_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_CHAR);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>double</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForDoubleData()
   {
      testInDataMapping(DEFAULT_DOUBLE_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_DOUBLE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>float</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForFloatData()
   {
      testInDataMapping(DEFAULT_FLOAT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_FLOAT);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>int</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForIntData()
   {
      testInDataMapping(DEFAULT_INT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_INT);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>long</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForLongData()
   {
      testInDataMapping(DEFAULT_LONG_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_LONG);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>short</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsDefaultValueForShortData()
   {
      testInDataMapping(DEFAULT_SHORT_IN_DATA_MAPPING, DEFAULT_VALUE_DEFAULT_SHORT);
   }

   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>Calendar</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForCalendarData()
   {
      testInDataPath(DEFAULT_CALENDAR_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_CALENDAR);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>String</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForStringData()
   {
      testInDataPath(DEFAULT_STRING_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_STRING);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>Timestamp</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForTimestampData()
   {
      testInDataPath(DEFAULT_TIMESTAMP_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_TIMESTAMP);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>boolean</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForBooleanData()
   {
      testInDataPath(DEFAULT_BOOLEAN_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_BOOLEAN);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>byte</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForByteData()
   {
      testInDataPath(DEFAULT_BYTE_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_BYTE);
   }   

   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>char</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForCharData()
   {
      testInDataPath(DEFAULT_CHAR_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_CHAR);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>double</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForDoubleData()
   {
      testInDataPath(DEFAULT_DOUBLE_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_DOUBLE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>float</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForFloatData()
   {
      testInDataPath(DEFAULT_FLOAT_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_FLOAT);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>int</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForIntData()
   {
      testInDataPath(DEFAULT_INT_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_INT);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>long</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForLongData()
   {
      testInDataPath(DEFAULT_LONG_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_LONG);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>short</i> returns the modeled default value.
    * </p>
    */
   @Test
   public void testInDataPathReturnsDefaultValueForShortData()
   {
      testInDataPath(DEFAULT_SHORT_IN_DATA_PATH, DEFAULT_VALUE_DEFAULT_SHORT);
   }
   
   private void testInDataMapping(final String inDataMapping, final Object expectedValue)
   {
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      final Object result = sf.getWorkflowService().getInDataValue(ai.getOID(), null, inDataMapping);
      
      assertThat(result, notNullValue());
      assertThat(result, equalTo(expectedValue));
   }

   private void testInDataPath(final String inDataPath, final Object expectedValue)
   {
      final Object result = sf.getWorkflowService().getInDataPath(piOid, inDataPath);
      
      assertThat(result, notNullValue());
      assertThat(result, equalTo(expectedValue));
   }
   
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_2, null, true);
      return pi.getOID();
   }
}
