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
package org.eclipse.stardust.test.camel.application.generic.producer;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Ignore;
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
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/camel-producer-application.app-ctx.xml")
public class ProducerIncludeConverterTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(ProducerIncludeConverterTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String[] MODEL_IDS = { "CamelApplicationType", "GenericApplicationProducerTestModel", "CsvConverterTestModel" };

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test
   @Ignore("Engine doesn't recognize the given timezone but uses the system default one.")
   public void testCsvConverterFromSdtListToCSVwithDollarDelimiter() throws Exception
   {
      String expectedCsvOutput =constructCsvEntry(); //"FirstName$LastName$Address$PostalCode$DOB\n\"Mr, \"\"Paul\"\"\"$Gomez$\"Paris, Main Street N$2\"$10$Thu Sep 24 00:00:00 WAT 1987\nABC$CDE$$252$Mon May 11 00:00:00 WAT 1981";

      Map<String, Object> persons = new HashMap<String, Object>();

      List<Map< ? , ? >> personList = new ArrayList<Map< ? , ? >>();
      Map<String, Object> person = new HashMap<String, Object>();
      person.put("FirstName", "Mr, \"Paul\"");
      person.put("LastName", "Gomez");
      person.put("Address", "Paris, Main Street N$2");
      person.put("PostalCode", 10);
      Date myDate;
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      Calendar cal = Calendar.getInstance();//TimeZone.getTimeZone("WAT")
      cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
      cal.set(Calendar.DATE, 24);
      cal.set(Calendar.YEAR, 1987);
      myDate = cal.getTime();
      person.put("DOB", formatter.format(myDate));
      personList.add(person);
      cal = Calendar.getInstance();
      cal.set(Calendar.MONTH, Calendar.MAY);
      cal.set(Calendar.DATE, 11);
      cal.set(Calendar.YEAR, 1981);
      myDate = cal.getTime();
      person = new HashMap<String, Object>();
      person.put("FirstName", "ABC");
      person.put("LastName", "CDE");
      person.put("PostalCode", 252);
      person.put("DOB", formatter.format(myDate));
      personList.add(person);
      persons.put("Persons", personList);
      trace.debug("set Persons : " + persons);
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("Persons", persons);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CsvConverterModelTest}FromSdtToCsv", dataMap, true);

      assertNotNull(pInstance);
      String csvOutput = (String) sf.getWorkflowService().getInDataPath(
            pInstance.getOID(), "csvOutput");
      assertNotNull(csvOutput);
      assertEquals(expectedCsvOutput, csvOutput);
   }

   private String constructCSVString(String delimiter)
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      StringBuilder response = new StringBuilder();
      response.append("FirstName" + delimiter + "LastName" + delimiter + "Address"
            + delimiter + "PostalCode" + delimiter + "DOB" + delimiter + "ExtraField\n");
      response.append("John" + delimiter + "Smith" + delimiter + "Paris" + delimiter
            + "75002" + delimiter + "" + formatter.format(getDob(1, Calendar.MAY, 2014))
            + "" + delimiter + "ExtraField1\n");
      response.append("James" + delimiter + "Johnson" + delimiter + "NY" + delimiter
            + "10173" + delimiter + "" + formatter.format(getDob(7, Calendar.MAY, 2014))
            + "" + delimiter + "ExtraField2");
      return response.toString();
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testCsvConverterFromCsvToSdtListwithCommaDelimiter() throws Exception
   {
      String csv = constructCSVString(",");
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("csvInput", csv);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CsvConverterModelTest}FromCsvToSdtList", dataMap, true);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(
            pInstance.getOID(), "Persons");
      List persons = (List) response.get("Persons");
      Map< ? , ? > person = (Map< ? , ? >) persons.get(0);
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertEquals("John", person.get("FirstName"));
      assertEquals("Smith", person.get("LastName"));
      assertEquals(75002, person.get("PostalCode"));
      assertEquals(formatDate(getDob(1, Calendar.MAY, 2014)),
            formatDate((Date) person.get("DOB")));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{CsvConverterModelTest}FromCsvToSdtList");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testCsvConverterFromCsvToSdtListwithHashTagDelimiter() throws Exception
   {
      String csv = constructCSVString("#");
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("csvInput", csv);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CsvConverterModelTest}FromCsvWithDiezDElimiterToSdtList", dataMap, true);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(
            pInstance.getOID(), "Persons");
      List persons = (List) response.get("Persons");
      Map< ? , ? > person = (Map< ? , ? >) persons.get(0);
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertEquals("John", person.get("FirstName"));
      assertEquals("Smith", person.get("LastName"));
      assertEquals(75002, person.get("PostalCode"));
      assertEquals("Paris", person.get("Address"));
      assertEquals(formatDate(getDob(1, Calendar.MAY, 2014)),
            formatDate((Date) person.get("DOB")));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{CsvConverterModelTest}FromCsvWithDiezDElimiterToSdtList");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
   }

   private Date getDob(int day, int month, int year)
   {
      Calendar dob1 = Calendar.getInstance();
      dob1.set(Calendar.MONTH, month);
      dob1.set(Calendar.DATE, day);
      dob1.set(Calendar.YEAR, year);
      return dob1.getTime();
   }

   private String constructCsvEntry()
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      StringBuilder csv = new StringBuilder();
      csv.append("FirstName$LastName$Address$PostalCode$DOB\n");
      csv.append("\"Mr, \"\"Paul\"\"\"$Gomez$\"Paris, Main Street N$2\"$10$"+formatter.format(getDob(24, Calendar.SEPTEMBER, 1987))+"\n");
      csv.append("ABC$CDE$$252$"+formatter.format(getDob(11, Calendar.MAY, 1981)));
      return csv.toString();
   }

   private static String formatDate(Date input)
   {
      return new SimpleDateFormat("dd-MM-yy").format(input);
   }
}
