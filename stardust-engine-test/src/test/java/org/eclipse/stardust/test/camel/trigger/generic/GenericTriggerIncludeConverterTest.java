/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.trigger.generic;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class GenericTriggerIncludeConverterTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(GenericTriggerIncludeConverterTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   public static final String[] MODEL_IDS = { "GenericTriggerTestModel", "GenericTriggerConverterTestModel" };

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test
   public void genericTriggerFromXmlConverter() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

      String xmlMessage = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?> <Structured >   <stringField>some text in Xml</stringField>   <intField>121</intField>   <longField>1222</longField>   <dateField>"+formatter.format(getDate(24, Calendar.SEPTEMBER, 2002,6,0))+"</dateField> </Structured>";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("XmlData", xmlMessage);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{GenericTriggerConverterTestModel}TestFromXml", dataMap, true);
      assertNotNull(pInstance);
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerConverterTestModel}FromXml"));
      ProcessInstance pi = pis.get(0);
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(
            pi.getOID(), "Structured");
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertTrue(response.get("intField") instanceof Integer);
      assertEquals(121, response.get("intField"));
      assertTrue(response.get("stringField") instanceof String);
      assertEquals("some text in Xml", response.get("stringField"));
      assertTrue(response.get("longField") instanceof Long);
      assertEquals(1222L, response.get("longField"));
      assertTrue(response.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(24, Calendar.SEPTEMBER, 2002)),  formatDate((Date) response.get("dateField")));

   }

   @Test
   public void genericTriggerFromCsvConverter() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy",Locale.US);
      String stringMessage = "stringField#intField#longField#dateField\nString Field in Csv#33#2222222#"+formatter.format(getDate(13, Calendar.MAY, 2014))+"";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("CsvData", stringMessage);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{GenericTriggerConverterTestModel}TestFromCSV", dataMap, true);
      assertNotNull(pInstance);
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerConverterTestModel}FromCsv"));
      ProcessInstance pi = pis.get(0);
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(
            pi.getOID(), "Structured");
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertTrue(response.get("intField") instanceof Integer);
      assertEquals(33, response.get("intField"));
      assertTrue(response.get("stringField") instanceof String);
      assertEquals("String Field in Csv", response.get("stringField"));
      assertTrue(response.get("longField") instanceof Long);
      assertEquals(2222222L, response.get("longField"));
      assertTrue(response.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(13, Calendar.MAY, 2014)),  formatDate((Date) response.get("dateField")));
   }

   @Test
   public void genericTriggerFromJsonConverter() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

      String jsonMessage = "{\"intField\":22,\"longField\":11,\"stringField\":\"String Field\",\"dateField\":\""+formatter.format(getDate(12,Calendar.MAY,2014))+"\"}";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("JsonData", jsonMessage);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{GenericTriggerConverterTestModel}TestFromJSON", dataMap, true);
      assertNotNull(pInstance);
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerConverterTestModel}FromJson"));
      ProcessInstance pi = pis.get(0);
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(
            pi.getOID(), "Structured");
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertTrue(response.get("intField") instanceof Integer);
      assertEquals(22, response.get("intField"));
      assertTrue(response.get("stringField") instanceof String);
      assertEquals("String Field", response.get("stringField"));
      assertTrue(response.get("longField") instanceof Long);
      assertEquals(11L, response.get("longField"));
      assertTrue(response.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(12, Calendar.MAY, 2014)), formatDate((Date) response.get("dateField")));
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void genericTriggerFromListCsvConverter() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy",Locale.US);
      String csvListMessage = "stringField#intField#longField#dateField\nFirst Line#12#45454#"+formatter.format(getDate(27,Calendar.MAY,2014))+"\nSecondLine#41#95854#"+formatter.format(getDate(18,Calendar.MAY,2014))+"";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("CsvListData", csvListMessage);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{GenericTriggerConverterTestModel}TestFromListCSV", dataMap, true);
      assertNotNull(pInstance);
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery
                  .findAlive("{GenericTriggerConverterTestModel}FromListCSV"));
      ProcessInstance pi = pis.get(0);
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(
            pi.getOID(), "ListStructured");
      assertTrue(response instanceof Map);
      assertNotNull(response);
      List elements = (List) response.get("elmts");
      assertTrue(response.get("elmts") instanceof List);
      trace.debug("get response List Csv = " + response);
      Map< ? , ? > firstElement = (Map< ? , ? >) elements.get(0);
      Map< ? , ? > secondElement = (Map< ? , ? >) elements.get(1);
      assertTrue(firstElement.get("intField") instanceof Integer);
      assertEquals(12, firstElement.get("intField"));
      assertTrue(firstElement.get("stringField") instanceof String);
      assertEquals("First Line", firstElement.get("stringField"));
      assertTrue(firstElement.get("longField") instanceof Long);
      assertEquals(45454L, firstElement.get("longField"));
      assertTrue(firstElement.get("dateField") instanceof Date);
      assertEquals("27-05-14", formatDate((Date) firstElement.get("dateField")));
      assertTrue(secondElement.get("intField") instanceof Integer);
      assertEquals(41, secondElement.get("intField"));
      assertTrue(secondElement.get("stringField") instanceof String);
      assertEquals("SecondLine", secondElement.get("stringField"));
      assertTrue(secondElement.get("longField") instanceof Long);
      assertEquals(95854L, secondElement.get("longField"));
      assertTrue(secondElement.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(18, Calendar.MAY, 2014)),  formatDate((Date) secondElement.get("dateField")));
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void genericTriggerFromListJsonConverter() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

      String jsonListMessage = "[{\"intField\":123,\"longField\":321414,\"stringField\":\"First Element In Json List\",\"dateField\":\""+formatter.format(getDate(1,Calendar.MAY,2014))+"\"},{\"intField\":847,\"longField\":215245,\"stringField\":\"SecondElement In Json List\",\"dateField\":\""+formatter.format(getDate(21,Calendar.JANUARY,2014))+"\"}]";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("JsonListData", jsonListMessage);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{GenericTriggerConverterTestModel}TestFromListJSON", dataMap, true);
      assertNotNull(pInstance);
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery
                  .findAlive("{GenericTriggerConverterTestModel}FromListJson"));
      ProcessInstance pi = pis.get(0);
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(
            pi.getOID(), "ListStructured");
      assertTrue(response instanceof Map);
      assertNotNull(response);
      List elements = (List) response.get("elmts");
      assertTrue(response.get("elmts") instanceof List);
      assertEquals(2, elements.size());
      trace.debug("get response List Csv = " + response);
      Map< ? , ? > firstElement = (Map< ? , ? >) elements.get(0);
      Map< ? , ? > secondElement = (Map< ? , ? >) elements.get(1);
      assertTrue(firstElement.get("intField") instanceof Integer);
      assertEquals(123, firstElement.get("intField"));
      assertTrue(firstElement.get("stringField") instanceof String);
      assertEquals("First Element In Json List", firstElement.get("stringField"));
      assertTrue(firstElement.get("longField") instanceof Long);
      assertEquals(321414L, firstElement.get("longField"));
      assertTrue(firstElement.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(1, Calendar.MAY, 2014)),  formatDate((Date) firstElement.get("dateField")));
      assertTrue(secondElement.get("intField") instanceof Integer);
      assertEquals(847, secondElement.get("intField"));
      assertTrue(secondElement.get("stringField") instanceof String);
      assertEquals("SecondElement In Json List", secondElement.get("stringField"));
      assertTrue(secondElement.get("longField") instanceof Long);
      assertEquals(215245L, secondElement.get("longField"));
      assertTrue(secondElement.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(21, Calendar.JANUARY, 2014)), formatDate((Date) secondElement.get("dateField")));
   }

   private String formatDate(Date input)
   {
      return new SimpleDateFormat("dd-MM-yy").format(input);
   }

   private Date getDate(int day, int month, int year)
   {
      Calendar dob1 = Calendar.getInstance();
      dob1.set(Calendar.MONTH, month);
      dob1.set(Calendar.DATE, day);
      dob1.set(Calendar.YEAR, year);
      return dob1.getTime();
   }

   private Date getDate(int day, int month, int year, int hours, int minutes)
   {
      Calendar dob1 = Calendar.getInstance();
      dob1.set(Calendar.MONTH, month);
      dob1.set(Calendar.DATE, day);
      dob1.set(Calendar.YEAR, year);
      dob1.set(Calendar.HOUR, hours);
      dob1.set(Calendar.MINUTE, minutes);
      return dob1.getTime();
   }
}
