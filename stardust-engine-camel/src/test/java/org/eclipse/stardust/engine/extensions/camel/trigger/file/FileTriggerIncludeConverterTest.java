package org.eclipse.stardust.engine.extensions.camel.trigger.file;

import static org.eclipse.stardust.engine.extensions.camel.common.Util.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class FileTriggerIncludeConverterTest
{
   private static final Logger trace = LogManager.getLogger(FileTriggerIncludeConverterTest.class.getName());
   private static ClassPathXmlApplicationContext ctx;
   @Resource
   private static ServiceFactoryAccess serviceFactoryAccess;
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml",
                  "classpath:default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
   }

   @Test
   public void testXmlConverterWithFileTrigger() throws Exception
   {

      String xmlFileContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>   <Structured>    <stringField>text in xml file</stringField>     <intField>565</intField>   <longField>126954</longField>    <dateField>2014-09-24-06:00</dateField>   </Structured>";
      createFile("./target/FileDirectory/XML", "messageFile.xml", xmlFileContent);
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
      assertEquals("Wed Sep 24 00:00:00 WAT 2014", response.get("dateField").toString());
   }

   @Test
   public void testJsonConverterWithFileTrigger() throws Exception
   {

      String jsonFileContent = "{\"intField\":456,\"longField\":789444,\"stringField\":\"text in Json file\",\"dateField\":\"2014-05-28T00:00:00.000\"}";
      createFile("./target/FileDirectory/Json", "messageFile.json", jsonFileContent);
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
      assertEquals("Wed May 28 00:00:00 WAT 2014", response.get("dateField").toString());
   }

   @Test
   public void testCsvConverterWithFileTrigger() throws Exception
   {
      String csvFileContent = "stringField,intField,longField,dateField\ntext in csv file,123,789,Wed May 28 11:30:00 WAT 2014";
      createFile("./target/FileDirectory/CSV", "messageFile.csv", csvFileContent);
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
      assertEquals("Wed May 28 11:30:00 WAT 2014", response.get("dateField").toString());
   }
   
   @Test
   public void testListCsvConverterWithFileTrigger() throws Exception
   {

      String csvFileContent = "stringField#intField#longField#dateField\nFirst Line#12#45454#Tue May 27 00:00:00 WAT 2014\nSecondLine#41#95854#Sun May 18 00:00:00 WAT 2014";
      createFile("./target/FileDirectory/ListCSV", "messageFile.csv", csvFileContent);
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
      assertEquals("Tue May 27 00:00:00 WAT 2014", firstElement.get("dateField").toString());
      assertTrue(secondElement.get("intField") instanceof Integer);
      assertEquals(41, secondElement.get("intField"));
      assertTrue(secondElement.get("stringField") instanceof String);
      assertEquals("SecondLine", secondElement.get("stringField"));
      assertTrue(secondElement.get("longField") instanceof Long);
      assertEquals(95854L, secondElement.get("longField"));
      assertTrue(secondElement.get("dateField") instanceof Date);
      assertEquals("Sun May 18 00:00:00 WAT 2014", secondElement.get("dateField").toString());
   }

}
