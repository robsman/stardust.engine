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
package org.eclipse.stardust.test.camel.trigger.file;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.camel.common.Util.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
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
public class FileTriggerIncludeConverterTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(FileTriggerIncludeConverterTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   public static final String MODEL_ID = "FileTriggerTestModel";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test(timeout = 10000)
   public void testXmlConverterWithFileTrigger() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
      String xmlFileContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>   <Structured>    <stringField>text in xml file</stringField>     <intField>565</intField>   <longField>126954</longField>    <dateField>"+formatter.format(getDate(24, Calendar.SEPTEMBER, 2002,6,0))+"</dateField>   </Structured>";
      createFile("target/FileDirectory/XML", "messageFile.xml", xmlFileContent);

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromXML")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromXML"));
      ProcessInstance pi = pis.get(0);
      ActivityInstanceQuery activityInstanceQueryGetXmlConvertedToSdt = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileTriggerFromXML");
      ActivityInstances activityInstancesGetXmlConvertedToSdt = sf.getQueryService().getAllActivityInstances(
            activityInstanceQueryGetXmlConvertedToSdt);
      sf.getWorkflowService().activateAndComplete(activityInstancesGetXmlConvertedToSdt.get(0).getOID(), null, null);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pi.getOID(), "Structured");
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertTrue(response.get("intField") instanceof Integer);
      assertEquals(565, response.get("intField"));
      assertTrue(response.get("stringField") instanceof String);
      assertEquals("text in xml file", response.get("stringField"));
      assertTrue(response.get("longField") instanceof Long);
      assertEquals(126954L, response.get("longField"));
      assertTrue(response.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(24, Calendar.SEPTEMBER, 2002)), formatDate((Date) response.get("dateField")));
   }

   @Test(timeout = 10000)
   public void testJsonConverterWithFileTrigger() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      String jsonFileContent = "{\"intField\":456,\"longField\":789444,\"stringField\":\"text in Json file\",\"dateField\":\""+formatter.format(getDate(28,Calendar.MAY,2014))+"\"}";
      createFile("target/FileDirectory/Json", "messageFile.json", jsonFileContent);

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromJSON")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromJSON"));
      ProcessInstance pi = pis.get(0);
      ActivityInstanceQuery activityInstanceQueryGetJsonConvertedToSdt = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileTriggerFromJSON");
      ActivityInstances activityInstancesGetXmlConvertedToSdt = sf.getQueryService().getAllActivityInstances(
            activityInstanceQueryGetJsonConvertedToSdt);
      sf.getWorkflowService().activateAndComplete(activityInstancesGetXmlConvertedToSdt.get(0).getOID(), null, null);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pi.getOID(), "Structured");
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertTrue(response.get("intField") instanceof Integer);
      assertEquals(456, response.get("intField"));
      assertTrue(response.get("stringField") instanceof String);
      assertEquals("text in Json file", response.get("stringField"));
      assertTrue(response.get("longField") instanceof Long);
      assertEquals(789444L, response.get("longField"));
      assertTrue(response.get("dateField") instanceof Date);
      assertEquals(formatDate(getDate(28, Calendar.MAY, 2014)), formatDate((Date) response.get("dateField")));
   }

   @Test(timeout = 10000)
   public void testCsvConverterWithFileTrigger() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy",Locale.US);

      String csvFileContent = "stringField,intField,longField,dateField\ntext in csv file,123,789,"+formatter.format(getDate(28,Calendar.MAY,2014,11,30));
      createFile("target/FileDirectory/CSV", "messageFile.csv", csvFileContent);

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromCSV")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromCSV"));
      ProcessInstance pi = pis.get(0);
      ActivityInstanceQuery activityInstanceQueryGetCsvConvertedToSdt = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileTriggerFromCSV");
      ActivityInstances activityInstancesGetCsvConvertedToSdt = sf.getQueryService().getAllActivityInstances(
            activityInstanceQueryGetCsvConvertedToSdt);
      sf.getWorkflowService().activateAndComplete(activityInstancesGetCsvConvertedToSdt.get(0).getOID(), null, null);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pi.getOID(), "Structured");
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertTrue(response.get("intField") instanceof Integer);
      assertEquals(123, response.get("intField"));
      assertTrue(response.get("stringField") instanceof String);
      assertEquals("text in csv file", response.get("stringField"));
      assertTrue(response.get("longField") instanceof Long);
      assertEquals(789L, response.get("longField"));
      assertTrue(response.get("dateField") instanceof Date);
      //assertEquals("Wed May 28 11:30:00 WAT 2014", response.get("dateField").toString());
      assertEquals(formatDate(getDate(28, Calendar.MAY, 2014)), formatDate((Date) response.get("dateField")));
   }

   @Test(timeout = 10000)
   public void testListCsvConverterWithFileTrigger() throws Exception
   {
      SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy",Locale.US);
      String csvFileContent = "stringField#intField#longField#dateField\nFirst Line#12#45454#"+formatter.format(getDate(27,Calendar.MAY,2014))+"\nSecondLine#41#95854#"+formatter.format(getDate(18,Calendar.MAY,2014));
      createFile("target/FileDirectory/ListCSV", "messageFile.csv", csvFileContent);

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromListCSV")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerFromListCSV"));
      ProcessInstance pi = pis.get(0);
      ActivityInstanceQuery activityInstancesGetListCsvConvertedToSdtQuery = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileTriggerFromListCSV");
      ActivityInstances activityInstancesGetListCsvConvertedToSdt = sf.getQueryService().getAllActivityInstances(
            activityInstancesGetListCsvConvertedToSdtQuery);
      sf.getWorkflowService().activateAndComplete(activityInstancesGetListCsvConvertedToSdt.get(0).getOID(), null, null);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pi.getOID(), "ListStructured");
      assertTrue(response instanceof Map);
      assertNotNull(response);
      List<?> elements = (List<?>) response.get("elmts");
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
      assertEquals(formatDate(getDate(18, Calendar.MAY, 2014)), formatDate((Date) secondElement.get("dateField")));
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
