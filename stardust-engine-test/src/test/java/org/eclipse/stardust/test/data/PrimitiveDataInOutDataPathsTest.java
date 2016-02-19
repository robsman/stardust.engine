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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
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
 * Tests whether setting and retrieving primitive process data
 * via in and out data paths works correctly.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class PrimitiveDataInOutDataPathsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME, "Model11");

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
      testFor(new Date(System.currentTimeMillis()), MY_TIMESTAMP_IN_DATA_PATH, MY_TIMESTAMP_OUT_DATA_PATH);
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

   /**
    * <p>
    * Tests whether the correct exception is thrown when the in data path does not exist.
    * </p>
    */
   @Test
   public void testCompositeInDataPath()
   {
      final String retrievedValue = (String) sf.getWorkflowService().getInDataPath(piOid, "FullName");

      assertThat(retrievedValue, equalTo("Frank Underwood"));
   }

   @Test
   public void testSimpleCompositeDescriptor()
   {
      testDescriptor("Frank Underwood", "FullName");
   }

   @Test
   public void testLinkDescriptor()
   {
      ProcessInstance pi = testDescriptor("http://fisglobal.com?id=666", "Invoice");
      List<DataPath> defs = pi.getDescriptorDefinitions();
      for (DataPath def : defs)
      {
         if ("Invoice".equals(def.getId()))
         {
            String retrievedValue = (String) def.getAttribute("text");
            assertThat(retrievedValue , equalTo("See invoice '666' details"));
         }
      }
   }

   @Test
   public void testTwoLevelCompositeDescriptor()
   {
      testDescriptor("Name: Frank Underwood, URL: http://fisglobal.com?id=666", "Composite2Levels");
   }

   @Test
   public void testMultiLevelCircularCompositeDescriptor()
   {
      testDescriptor("Composition [Name: Frank Underwood, URL: http://fisglobal.com?id=666]", "Composite3Levels");
   }

   @Test
   public void malfunctioningLinkText()
   {
      Map<String, String> data = CollectionUtils.newMap();
      data.put("Ticker", "3XK");
      data.put("CompanyName", "Triple Killer Networks");
      ProcessInstance pi = sf.getWorkflowService().startProcess("{Model11}StockQuote", data, true);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      pi = sf.getQueryService().findFirstProcessInstance(query);

      String accessPathValue = (String) pi.getDescriptorValue("StockChart");
      assertThat(accessPathValue , equalTo("https://finance/yahoo/com/q?s=3XK"));

      boolean found = false;
      List<DataPath> defs = pi.getDescriptorDefinitions();
      for (DataPath def : defs)
      {
         if ("StockChart".equals(def.getId()))
         {
            accessPathValue = (String) def.getAttribute("text");
            assertThat(accessPathValue , equalTo("Stock Chart for Triple Killer Networks (3XK)"));
            found = true;
         }
      }
      assertThat(found, is(true));

      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(pi.getOID(), "GetCompanyInfo");
      aiQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(aiQuery);

      pi = ai.getProcessInstance();
      accessPathValue = (String) pi.getDescriptorValue("StockChart");
      assertThat(accessPathValue , equalTo("https://finance/yahoo/com/q?s=3XK"));

      found = false;
      defs = pi.getDescriptorDefinitions();
      for (DataPath def : defs)
      {
         if ("StockChart".equals(def.getId()))
         {
            accessPathValue = (String) def.getAttribute("text");
            assertThat(accessPathValue , equalTo("Stock Chart for Triple Killer Networks (3XK)"));
            found = true;
         }
      }
      assertThat(found, is(true));


   }

   @Test
   public void malfunctioningLinkText2()
   {
      checkLink("goog", "Google");
      checkLink("yah", "Yahoo");
   }

   protected Map<String, String> checkLink(String ticker, String companyName)
   {
      Map<String, String> data = CollectionUtils.newMap();
      data.put("Ticker", ticker);
      data.put("CompanyName", companyName);
      ProcessInstance pi = sf.getWorkflowService().startProcess("{Model11}StockQuote", data, true);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      pi = sf.getQueryService().findFirstProcessInstance(query);

      String accessPathValue = (String) pi.getDescriptorValue("StockChart");
      assertThat(accessPathValue , equalTo("https://finance/yahoo/com/q?s=" + ticker));

      boolean found = false;
      List<DataPath> defs = pi.getDescriptorDefinitions();
      for (DataPath def : defs)
      {
         if ("StockChart".equals(def.getId()))
         {
            accessPathValue = (String) def.getAttribute("text");
            assertThat(accessPathValue , equalTo("Stock Chart for " + companyName + " (" + ticker + ")"));
            found = true;
         }
      }
      assertThat(found, is(true));
      return data;
   }

   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_1,
            Collections.singletonMap("invoiceId", "666"), true);
      return pi.getOID();
   }

   private <T extends Serializable> ProcessInstance testDescriptor(final T value, final String descriptorId)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(piOid));
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      ProcessInstance pi = sf.getQueryService().findFirstProcessInstance(query);

      @SuppressWarnings("unchecked")
      final T descriptorValue = (T) pi.getDescriptorValue(descriptorId);
      assertThat(descriptorValue, equalTo(value));

      @SuppressWarnings("unchecked")
      final T dataPathValue = (T) sf.getWorkflowService().getInDataPath(piOid, descriptorId);
      assertThat(dataPathValue, equalTo(value));

      List<DataPath> defs = pi.getDescriptorDefinitions();
      for (DataPath def : defs)
      {
         if (descriptorId.equals(def.getId()))
         {
            @SuppressWarnings("unchecked")
            T accessPathValue = (T) def.getAccessPath();
            assertThat(accessPathValue , equalTo(value));
         }
      }

      return pi;
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
