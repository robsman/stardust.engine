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
import static org.eclipse.stardust.test.data.DataModelConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.ContextData;
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
 * Tests whether setting and retrieving primitive process data
 * via in and out data mappings works correctly.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class PrimitiveDataInOutDataMappingsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   private long aiOid;
   
   @Before
   public void setUp()
   {
      startProcess();
      aiOid = findFirstAliveActivityInstanceFor();
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>Calendar</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForCalendar()
   {
      testFor(Calendar.getInstance(), MY_CALENDAR_IN_DATA_MAPPING, MY_CALENDAR_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>String</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForString()
   {
      testFor("This is a test.", MY_STRING_IN_DATA_MAPPING, MY_STRING_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>Timestamp</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForTimestamp()
   {
      testFor(new Date(System.currentTimeMillis()), MY_TIMESTAMP_IN_DATA_MAPPING, MY_TIMESTAMP_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>boolean</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForBoolean()
   {
      testFor(Boolean.TRUE, MY_BOOLEAN_IN_DATA_MAPPING, MY_BOOLEAN_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>byte</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForByte()
   {
      final byte b = 8;
      testFor(Byte.valueOf(b), MY_BYTE_IN_DATA_MAPPING, MY_BYTE_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>char</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForChar()
   {
      testFor(Character.valueOf('x'), MY_CHAR_IN_DATA_MAPPING, MY_CHAR_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>double</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForDouble()
   {
      testFor(Double.valueOf(81.18), MY_DOUBLE_IN_DATA_MAPPING, MY_DOUBLE_OUT_DATA_MAPPING);
   }

   /**
    * <p>
    * Tests whether setting and retrieving a <i>float</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForFloat()
   {
      testFor(Float.valueOf(18.81F), MY_FLOAT_IN_DATA_MAPPING, MY_FLOAT_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>int</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForInt()
   {
      testFor(81, MY_INT_IN_DATA_MAPPING, MY_INT_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>long</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForLong()
   {
      testFor(Long.valueOf(818), MY_LONG_IN_DATA_MAPPING, MY_LONG_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>short</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForShort()
   {
      final short s = 18;
      testFor(Short.valueOf(s), MY_SHORT_IN_DATA_MAPPING, MY_SHORT_OUT_DATA_MAPPING);
   }
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the in data mapping does not exist.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testInDataMappingFailDataPathNotFound()
   {
      sf.getWorkflowService().getInDataValue(aiOid, null, "N/A");
   }
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the out data mapping does not exist.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testOutDataMappingFailDataPathNotFound()
   {
      sf.getWorkflowService().activate(aiOid);
      final ContextData data = new ContextData(null, Collections.singletonMap("N/A", "<Value>"));
      sf.getWorkflowService().suspend(aiOid, data);
   }
   
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
   
   private <T extends Serializable> void testFor(final T value, final String inDataMapping, final String outDataMapping)
   {
      sf.getWorkflowService().activate(aiOid);
      final ContextData data = new ContextData(null, Collections.singletonMap(outDataMapping, value));
      sf.getWorkflowService().suspend(aiOid, data);
      @SuppressWarnings("unchecked")
      final T retrievedValue = (T) sf.getWorkflowService().getInDataValue(aiOid, null, inDataMapping);
      
      assertThat(retrievedValue, notNullValue());
      assertThat(retrievedValue, equalTo(value));
   }
}
