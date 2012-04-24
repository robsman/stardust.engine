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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.junit.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether setting and retrieving primitive process data
 * via in and out data paths works correctly.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class PrimitiveDataInOutDataPathsTest extends LocalJcrH2Test
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
    * Tests whether setting and retrieving a <i>Calendar</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForCalendar()
   {
      testFor(Calendar.getInstance(), MY_CALENDAR_IN_DATA_PATH, MY_CALENDAR_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>String</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForString()
   {
      testFor("This is a test.", MY_STRING_IN_DATA_PATH, MY_STRING_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>Timestamp</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForTimestamp()
   {
      testFor(new Timestamp(System.currentTimeMillis()), MY_TIMESTAMP_IN_DATA_PATH, MY_TIMESTAMP_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>boolean</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForBoolean()
   {
      testFor(Boolean.TRUE, MY_BOOLEAN_IN_DATA_PATH, MY_BOOLEAN_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>byte</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForByte()
   {
      final byte b = 8;
      testFor(Byte.valueOf(b), MY_BYTE_IN_DATA_PATH, MY_BYTE_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>char</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForChar()
   {
      testFor(Character.valueOf('x'), MY_CHAR_IN_DATA_PATH, MY_CHAR_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>double</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForDouble()
   {
      testFor(Double.valueOf(81.18), MY_DOUBLE_IN_DATA_PATH, MY_DOUBLE_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>float</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForFloat()
   {
      testFor(Float.valueOf(18.81F), MY_FLOAT_IN_DATA_PATH, MY_FLOAT_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>int</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForInt()
   {
      testFor(81, MY_INT_IN_DATA_PATH, MY_INT_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>long</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForLong()
   {
      testFor(Long.valueOf(818L), MY_LONG_IN_DATA_PATH, MY_LONG_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>short</i> process data
    * via an in and out data path works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForShort()
   {
      final short s = 18;
      testFor(Short.valueOf(s), MY_SHORT_IN_DATA_PATH, MY_SHORT_OUT_DATA_PATH);
   }
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the in data path does not exist.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testInDataPathFailDataPathNotFound()
   {
      sf.getWorkflowService().getInDataPath(piOid, "N/A");
   }
   
   /**
    * <p>
    * Tests whether the correct exception is thrown when the out data path does not exist.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testOutDataPathFailDataPathNotFound()
   {
      sf.getWorkflowService().setOutDataPath(piOid, "N/A", "<Value>");
   }
   
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_1, null, true);
      return pi.getOID();
   }
   
   private <T extends Serializable> void testFor(final T value, final String inDataPath, final String outDataPath)
   {
      sf.getWorkflowService().setOutDataPath(piOid, outDataPath, value);
      @SuppressWarnings("unchecked")
      final T retrievedValue = (T) sf.getWorkflowService().getInDataPath(piOid, inDataPath);
      
      assertThat(retrievedValue, notNullValue());
      assertThat(retrievedValue, equalTo(value));
   }
}
