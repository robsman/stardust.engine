package org.eclipse.stardust.engine.extensions.camel.application.generic.producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.camel.component.mock.MockEndpoint;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class ProducerIncludeConverterTest
{
   private static final Logger trace = LogManager
         .getLogger(ProducerIncludeConverterTest.class.getName());

   private static ClassPathXmlApplicationContext ctx;

   private static ServiceFactoryAccess serviceFactoryAccess;

   protected static MockEndpoint resultEndpoint;

   public static String formatDate(Date input)
   {
      return new SimpleDateFormat("dd-MM-yy").format(input);
   }

   @BeforeClass
   public static void beforeClass()
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml",
            "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");

      File dir = new File("./target/FileDirectory");
      dir.mkdirs();
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

   @Test
   @Ignore("Engine doesn't recognize the given timezone but uses the system default one.")
   public void testCsvConverterFromSdtListToCSVwithDollarDelimiter() throws Exception
   {
      String expectedCsvOutput =constructCsvEntry(); //"FirstName$LastName$Address$PostalCode$DOB\n\"Mr, \"\"Paul\"\"\"$Gomez$\"Paris, Main Street N$2\"$10$Thu Sep 24 00:00:00 WAT 1987\nABC$CDE$$252$Mon May 11 00:00:00 WAT 1981";
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

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
      // cal.set(Calendar.HOUR, 13);
      // cal.set(Calendar.MINUTE, 45);
      // cal.set(Calendar.SECOND, 52);
      myDate = cal.getTime();
      person.put("DOB", formatter.format(myDate));
      personList.add(person);
      cal = Calendar.getInstance();
      cal.set(Calendar.MONTH, Calendar.MAY);
      cal.set(Calendar.DATE, 11);
      cal.set(Calendar.YEAR, 1981);
      // cal.set(Calendar.HOUR, 03);
      // cal.set(Calendar.MINUTE, 25);
      // cal.set(Calendar.SECOND, 12);
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
      Thread.sleep(2000);
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
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
}
