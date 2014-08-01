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
import static org.eclipse.stardust.test.data.DataModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_BOOLEAN_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_BOOLEAN_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_BYTE_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_BYTE_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_CALENDAR_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_CALENDAR_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_CHAR_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_CHAR_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_DOUBLE_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_DOUBLE_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_FLOAT_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_FLOAT_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_INT_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_INT_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_LONG_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_LONG_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_SHORT_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_SHORT_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_STRING_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_STRING_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_TIMESTAMP_IN_DATA_MAPPING;
import static org.eclipse.stardust.test.data.DataModelConstants.MY_TIMESTAMP_IN_DATA_PATH;
import static org.eclipse.stardust.test.data.DataModelConstants.PROCESS_ID_1;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether the correct initial value is returned for primitive data
 * that do not have a default value set.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class InitialValuePrimitiveDataTest
{
   private static final Calendar CALENDAR_CURRENT_TIME_VALUE = Calendar.getInstance();
   private static final String STRING_INIT_VALUE = "";
   private static final Timestamp TIMESTAMP_CURRENT_TIME_VALUE = new Timestamp(System.currentTimeMillis());
   private static final boolean BOOLEAN_INIT_VALUE = false;
   private static final byte BYTE_INIT_VALUE = 0;
   private static final char CHAR_INIT_VALUE = 0;
   private static final double DOUBLE_INIT_VALUE = 0.0;
   private static final float FLOAT_INIT_VALUE = 0.0F;
   private static final int INT_INIT_VALUE = 0;
   private static final long LONG_INIT_VALUE = 0L;
   private static final short SHORT_INIT_VALUE = 0;
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   private long piOid;
   
   @Before
   public void setUp()
   {
      piOid = startProcess();
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>Calendar</i> returns the correct initial value: <current time>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForCalendarData()
   {
      testInDataMapping(MY_CALENDAR_IN_DATA_MAPPING, CALENDAR_CURRENT_TIME_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>String</i> returns the correct initial value: <code>""</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForStringData()
   {
      testInDataMapping(MY_STRING_IN_DATA_MAPPING, STRING_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>Timestamp</i> returns the correct initial value: <current time>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForTimestampData()
   {
      testInDataMapping(MY_TIMESTAMP_IN_DATA_MAPPING, TIMESTAMP_CURRENT_TIME_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>boolean</i> returns the correct initial value: <code>false</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForBooleanData()
   {
      testInDataMapping(MY_BOOLEAN_IN_DATA_MAPPING, BOOLEAN_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>byte</i> returns the correct initial value: <code>0</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForByteData()
   {
      testInDataMapping(MY_BYTE_IN_DATA_MAPPING, BYTE_INIT_VALUE);
   }   

   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>char</i> returns the correct initial value: <code>''</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForCharData()
   {
      testInDataMapping(MY_CHAR_IN_DATA_MAPPING, CHAR_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>double</i> returns the correct initial value: <code>0.0</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForDoubleData()
   {
      testInDataMapping(MY_DOUBLE_IN_DATA_MAPPING, DOUBLE_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>float</i> returns the correct initial value: <code>0.0F</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForFloatData()
   {
      testInDataMapping(MY_FLOAT_IN_DATA_MAPPING, FLOAT_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>int</i> returns the correct initial value: <code>0</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForIntData()
   {
      testInDataMapping(MY_INT_IN_DATA_MAPPING, INT_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>long</i> returns the correct initial value: <code>0L</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForLongData()
   {
      testInDataMapping(MY_LONG_IN_DATA_MAPPING, LONG_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data mapping for a primitive data of type
    * <i>short</i> returns the correct initial value: <code>0</code>.
    * </p>
    */
   @Test
   public void testInDataMappingReturnsNullForShortData()
   {
      testInDataMapping(MY_SHORT_IN_DATA_MAPPING, SHORT_INIT_VALUE);
   }

   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>Calendar</i> returns the correct initial value: <current time>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForCalendarData()
   {
      testInDataPath(MY_CALENDAR_IN_DATA_PATH, CALENDAR_CURRENT_TIME_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>String</i> returns the correct initial value: <code>""</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForStringData()
   {
      testInDataPath(MY_STRING_IN_DATA_PATH, STRING_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>Timestamp</i> returns the correct initial value: <current time>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForTimestampData()
   {
      testInDataPath(MY_TIMESTAMP_IN_DATA_PATH, TIMESTAMP_CURRENT_TIME_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>boolean</i> returns the correct initial value: <code>false</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForBooleanData()
   {
      testInDataPath(MY_BOOLEAN_IN_DATA_PATH, BOOLEAN_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>byte</i> returns the correct initial value: <code>0</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForByteData()
   {
      testInDataPath(MY_BYTE_IN_DATA_PATH, BYTE_INIT_VALUE);
   }   

   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>char</i> returns the correct initial value: <code>''</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForCharData()
   {
      testInDataPath(MY_CHAR_IN_DATA_PATH, CHAR_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>double</i> returns the correct initial value: <code>0.0</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForDoubleData()
   {
      testInDataPath(MY_DOUBLE_IN_DATA_PATH, DOUBLE_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>float</i> returns the correct initial value: <code>0.0F</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForFloatData()
   {
      testInDataPath(MY_FLOAT_IN_DATA_PATH, FLOAT_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>int</i> returns the correct initial value: <code>0</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForIntData()
   {
      testInDataPath(MY_INT_IN_DATA_PATH, INT_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>long</i> returns the correct initial value: <code>0L</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForLongData()
   {
      testInDataPath(MY_LONG_IN_DATA_PATH, LONG_INIT_VALUE);
   }
   
   /**
    * <p>
    * Tests whether the in data path for a primitive data of type
    * <i>short</i> returns the correct initial value: <code>0</code>.
    * </p>
    */
   @Test
   public void testInDataPathReturnsNullForShortData()
   {
      testInDataPath(MY_SHORT_IN_DATA_PATH, SHORT_INIT_VALUE);
   }
   
   private void testInDataMapping(final String inDataMapping, final Object expected)
   {
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      final Object result = sf.getWorkflowService().getInDataValue(ai.getOID(), null, inDataMapping);
      
      assertThat(result, notNullValue());
      assertThat(result, equalTo(expected));
   }

   private void testInDataMapping(final String inDataMapping, final Calendar expected)
   {
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      final Object result = sf.getWorkflowService().getInDataValue(ai.getOID(), null, inDataMapping);
      
      assertThat(result, notNullValue());
      assertThat(result, instanceOf(Calendar.class));
      assertThat(expected.before(result), is(true));
   }
   
   private void testInDataMapping(final String inDataMapping, final Timestamp expected)
   {
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
      final Object result = sf.getWorkflowService().getInDataValue(ai.getOID(), null, inDataMapping);
      
      assertThat(result, notNullValue());
      assertThat(result, instanceOf(Date.class));
      assertThat(expected.before((Date) result), is(true));
   }
   
   private void testInDataPath(final String inDataPath, final Object expected)
   {
      final Object result = sf.getWorkflowService().getInDataPath(piOid, inDataPath);
      
      assertThat(result, notNullValue());
      assertThat(result, equalTo(expected));
   }

   private void testInDataPath(final String inDataPath, final Calendar expected)
   {
      final Object result = sf.getWorkflowService().getInDataPath(piOid, inDataPath);
      
      assertThat(result, notNullValue());
      assertThat(result, instanceOf(Calendar.class));
      assertThat(expected.before(result), is(true));
   }

   private void testInDataPath(final String inDataPath, final Timestamp expected)
   {
      final Object result = sf.getWorkflowService().getInDataPath(piOid, inDataPath);
      
      assertThat(result, notNullValue());
      assertThat(result, instanceOf(Date.class));
      assertThat(expected.before((Date) result), is(true));
   }
   
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_1, null, true);
      return pi.getOID();
   }
}
