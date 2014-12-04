package org.eclipse.stardust.engine.extensions.camel.trigger.generic;

import static org.eclipse.stardust.engine.extensions.camel.common.Util.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class GenericTriggerBodyDataMappingTest
{
   private static final Logger trace = LogManager.getLogger(GenericTriggerBodyDataMappingTest.class.getName());
   private static ClassPathXmlApplicationContext ctx;
   @Resource
   private static CamelContext camelContext;

   @Resource
   private static ServiceFactoryAccess serviceFactoryAccess;

   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:META-INF/spring/default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");

      try
      {
         ProducerTemplate producer = camelContext.createProducerTemplate();
         Thread.sleep(1000);
         producer.sendBody("direct:startGenericTriggerToPrimitive", "primitiveData content from test class Generic Trigger");
         Thread.sleep(1000);
         producer.sendBody("direct:startGenericTriggerToSdt",
               "<person><FirstName>FN</FirstName><LastName>LN</LastName></person>");
         Thread.sleep(2000);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Test
   public void genericTriggerToPrimitive() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToPrimitive"));
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(), "FileContent");
      trace.info("FileContent = " + response);
      assertNotNull(response);
      assertTrue(response instanceof String);
      assertEquals("primitiveData content from test class Generic Trigger", response.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerToPrimitive");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }

   @Test
   public void genericTriggerToSdt() throws Exception
   {
      Thread.sleep(2000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToSdt"));
      ProcessInstance pi = pis.get(0);

      Object firstName = sf.getWorkflowService().getInDataPath(pi.getOID(), "FirstName");
      trace.info("FirstName = " + firstName);
      assertNotNull(firstName);
      assertTrue(firstName instanceof String);
      assertEquals("FN", firstName.toString());
      Object lastName = sf.getWorkflowService().getInDataPath(pi.getOID(), "LastName");
      trace.info("LastName = " + lastName);
      assertNotNull(lastName);
      assertTrue(lastName instanceof String);
      assertEquals("LN", lastName.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerToSdt");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

   }

   @Test
   public void genericTriggerToDocument() throws Exception
   {
      createFile("./target/FileDirectory/DocumentGT", "DocumentFileGT.txt", "Document File Content GT");
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToDocument"));
      ProcessInstance pi = pis.get(0);
      Object documentFileContent = sf.getWorkflowService().getInDataPath(pi.getOID(), "DocumentFile");
      Document document = (Document) sf.getWorkflowService().getInDataPath(pi.getOID(), "DocumentFile");
      byte[] byteDocumentContent = sf.getDocumentManagementService().retrieveDocumentContent(document.getId());
      String documentContent = new String(byteDocumentContent, "UTF-8");
      trace.debug("documentContent = " + documentContent);
      assertNotNull(document);
      assertTrue(documentFileContent instanceof Document);
      assertTrue("DocumentFileGT.txt".equals(document.getName()));
      System.out.println("DocumentFile.txt - document.getName() - " + document.getName());
      System.out.println("documentContent - " + documentContent);
      assertTrue("Document File Content GT".equals(documentContent));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerToDocument");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }
   
}
