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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether the correct value is returned for primitive data
 * that do not have a default value set.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
@Ignore("CRNT-22830")
public class NoDefaultValueDataTest extends LocalJcrH2Test
{
   private final ClientServiceFactory sf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(sf, MODEL_NAME);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(sf)
                                    .around(rtConfigurer);
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>Calendar</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForCalendarData()
   {
      testForInDataMapping(MY_CALENDAR_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>String</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForStringData()
   {
      testForInDataMapping(MY_STRING_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>Timestamp</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForTimestampData()
   {
      testForInDataMapping(MY_TIMESTAMP_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>boolean</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForBooleanData()
   {
      testForInDataMapping(MY_BOOLEAN_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>byte</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForByteData()
   {
      testForInDataMapping(MY_BYTE_IN_DATA_MAPPING);
   }   

   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>char</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForCharData()
   {
      testForInDataMapping(MY_CHAR_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>double</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForDoubleData()
   {
      testForInDataMapping(MY_DOUBLE_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>float</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForFloatData()
   {
      testForInDataMapping(MY_FLOAT_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>int</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForIntData()
   {
      testForInDataMapping(MY_INT_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>long</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForLongData()
   {
      testForInDataMapping(MY_LONG_IN_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>short</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForShortData()
   {
      testForInDataMapping(MY_SHORT_IN_DATA_MAPPING);
   }

   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>Calendar</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForCalendarData()
   {
      testForInDataPath(MY_CALENDAR_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>String</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForStringData()
   {
      testForInDataPath(MY_STRING_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>Timestamp</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForTimestampData()
   {
      testForInDataPath(MY_TIMESTAMP_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>boolean</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForBooleanData()
   {
      testForInDataPath(MY_BOOLEAN_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>byte</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForByteData()
   {
      testForInDataPath(MY_BYTE_IN_DATA_PATH);
   }   

   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>char</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForCharData()
   {
      testForInDataPath(MY_CHAR_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>double</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForDoubleData()
   {
      testForInDataPath(MY_DOUBLE_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>float</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForFloatData()
   {
      testForInDataPath(MY_FLOAT_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>int</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForIntData()
   {
      testForInDataPath(MY_INT_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>long</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForLongData()
   {
      testForInDataPath(MY_LONG_IN_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>short</i> returns <code>null</code>, if there's no default
    * value set for the data.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForShortData()
   {
      testForInDataPath(MY_SHORT_IN_DATA_PATH);
   }
   
   private void testForInDataMapping(final String inDataMapping)
   {
      startProcess();
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      final Object result = sf.getWorkflowService().getInDataValue(ai.getOID(), null, inDataMapping);
      
      assertThat("CRNT-22830", result, nullValue());
   }

   private void testForInDataPath(final String inDataPath)
   {
      final long piOid = startProcess();
      final Object result = sf.getWorkflowService().getInDataPath(piOid, inDataPath);
      
      assertThat("CRNT-22830", result, nullValue());
   }
   
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_1, null, true);
      return pi.getOID();
   }
}
