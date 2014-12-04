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
   private static final Logger trace = LogManager.getLogger(ProducerIncludeConverterTest.class.getName());
   private static ClassPathXmlApplicationContext ctx;
   private static ServiceFactoryAccess serviceFactoryAccess;
   protected static MockEndpoint resultEndpoint;
//   private static SpringTestUtils testUtils;
   
   public static String formatDate(Date input)
   {
      return new SimpleDateFormat("dd-MM-yy").format(input);
   }
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:META-INF/spring/default-camel-context.xml"});
//      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
//      ClassPathResource resource = new ClassPathResource("models/CsvConverterModelTest.xpdl");
//      testUtils.setModelFile(resource);
      
      File dir = new File("./target/FileDirectory");
      dir.mkdirs();
//      try
//      {
//         Thread.sleep(3000);
//      }
//      catch (InterruptedException e)
//      {
//         e.printStackTrace();
//      }
//      try
//      {
//         testUtils.deployModel();
//         Thread.sleep(1000);
//      }
//      catch (Exception e)
//      {
//         throw new RuntimeException(e);
//      }
   }

  
   @Test
   @Ignore ("Engine doesn't recognize the given timezone but uses the system default one.")
   public void testCsvConverterFromSdtListToCSVwithDollarDelimiter() throws Exception
   {
      String expectedCsvOutput = "FirstName$LastName$Address$PostalCode$DOB\n\"Mr, \"\"Paul\"\"\"$Gomez$\"Paris, Main Street N$2\"$10$Thu Sep 24 00:00:00 WAT 1987\nABC$CDE$$252$Mon May 11 00:00:00 WAT 1981";
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      
      Map<String, Object> persons = new HashMap<String, Object>();

      List<Map< ? , ? >> personList = new ArrayList<Map< ? , ? >>();
      Map<String, Object> person = new HashMap<String, Object>();
      person.put("FirstName", "Mr, \"Paul\"");
      person.put("LastName", "Gomez");
      person.put("Address", "Paris, Main Street N$2");
      person.put("PostalCode", 10);
      Date myDate;
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("WAT"));
      cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
      cal.set(Calendar.DATE, 24);
      cal.set(Calendar.YEAR, 1987);
//      cal.set(Calendar.HOUR, 13);
//      cal.set(Calendar.MINUTE, 45);
//      cal.set(Calendar.SECOND, 52);
      myDate = cal.getTime();
      person.put("DOB", myDate);
      personList.add(person);
      cal = Calendar.getInstance();
      cal.set(Calendar.MONTH, Calendar.MAY);
      cal.set(Calendar.DATE, 11);
      cal.set(Calendar.YEAR, 1981);
//      cal.set(Calendar.HOUR, 03);
//      cal.set(Calendar.MINUTE, 25);
//      cal.set(Calendar.SECOND, 12);
      myDate = cal.getTime();
      person = new HashMap<String, Object>();
      person.put("FirstName", "ABC");
      person.put("LastName", "CDE");
      person.put("PostalCode", 252);
      person.put("DOB", myDate);
      personList.add(person);
      persons.put("Persons", personList);
      trace.debug("set Persons : " + persons);
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("Persons", persons);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{CsvConverterModelTest}FromSdtToCsv",
            dataMap, true);

      assertNotNull(pInstance);
      Thread.sleep(2000);
      String csvOutput = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "csvOutput");
      assertNotNull(csvOutput);
      assertEquals(expectedCsvOutput, csvOutput);
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testCsvConverterFromCsvToSdtListwithCommaDelimiter() throws Exception
   {
      String csv = "FirstName,LastName,Address,PostalCode,DOB,ExtraField\n\"Mr,John\",,Nabel,2332,Thu May 01 00:00:00 WAT 2014,ExtraField1\nmelek,zribi,Tunis,1254,Wed May 07 00:00:00 WAT 2014,ExtraField2";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("csvInput", csv);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{CsvConverterModelTest}FromCsvToSdtList",
            dataMap, true);
      Thread.sleep(4000);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "Persons");
      List persons = (List) response.get("Persons");
      Map< ? , ? > person = (Map< ? , ? >) persons.get(0);
      trace.debug("get persons = " + persons);
      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertEquals("Mr,John", person.get("FirstName"));
      assertEquals(null, person.get("LastName"));
      assertEquals(2332, person.get("PostalCode"));
      // assertEquals("Thu May 01 00:00:00 WAT 2014", person.get("DOB").toString());
      assertEquals("01-05-14", formatDate((Date) person.get("DOB")));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{CsvConverterModelTest}FromCsvToSdtList");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testCsvConverterFromCsvToSdtListwithHashTagDelimiter() throws Exception
   {
      String csv = "FirstName#LastName#Address#PostalCode#DOB\nPaul#Gomez#Paris#1246#Thu May 01 00:00:00 WAT 2014\nmelek#zribi#Tunis#1254#Wed May 07 00:00:00 WAT 2014";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("csvInput", csv);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{CsvConverterModelTest}FromCsvWithDiezDElimiterToSdtList",
            dataMap, true);
      Thread.sleep(4000);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "Persons");
      List persons = (List) response.get("Persons");

      Map< ? , ? > person = (Map< ? , ? >) persons.get(0);

      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertEquals("Paul", person.get("FirstName"));
      assertEquals("Gomez", person.get("LastName"));
      assertEquals(1246, person.get("PostalCode"));
      assertEquals("Paris", person.get("Address"));

      // assertEquals("Thu May 01 00:00:00 WAT 2014", person.get("DOB").toString());
      assertEquals("01-05-14", formatDate((Date) person.get("DOB")));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{CsvConverterModelTest}FromCsvWithDiezDElimiterToSdtList");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      trace.info("activityInstance size = " + activityInstances.size());
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

   }
}
