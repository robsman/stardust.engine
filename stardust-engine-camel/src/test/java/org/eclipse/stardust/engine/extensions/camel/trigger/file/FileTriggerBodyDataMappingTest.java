package org.eclipse.stardust.engine.extensions.camel.trigger.file;

import static org.eclipse.stardust.engine.extensions.camel.common.Util.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

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
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class FileTriggerBodyDataMappingTest
{
   private static final Logger trace = LogManager.getLogger(FileTriggerBodyDataMappingTest.class
         .getName());

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
      File dir = new File("./target/FileDirectory");
      dir.mkdirs();
      try
      {
         Thread.sleep(2000);
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
   }

   @Test
   public void fileTriggerToPrimitive() throws Exception
   {
      createFile("./target/FileDirectory/PD", "primitiveDataFile.txt",
            "primitiveData content from test class");
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerToPrimitiveBDM"));
      if (pis.size() > 1)
         throw new RuntimeException("Please clean the audit Trial");
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(), "FileContent");
      trace.info("FileContent = " + response);
      assertNotNull(response);
      assertTrue(response instanceof String);
      assertEquals("primitiveData content from test class", response.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileTriggerToPrimitiveBDM");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }

   @Test
   public void fileTriggerToSDT() throws Exception
   {
      createFile(
            "./target/FileDirectory/SDT",
            "Person.xml",
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><person><FirstName>FN</FirstName><LastName>LN</LastName></person>");
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerToSdtBDM"));
      if (pis.size() > 1)
         throw new RuntimeException("Please clean the audit Trial");
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
            .findAlive("{FileTriggerTestModel}FileTriggerToSdtBDM");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }

   @Test
   public void fileTriggerToDocument() throws Exception
   {
      createFile("./target/FileDirectory/Document", "DocumentFile.txt",
            "Document File Content");
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileTriggerToDocumentBDM"));
      if (pis.size() > 1)
         throw new RuntimeException("Please clean the audit Trial");
      ProcessInstance pi = pis.get(0);
      Object documentFileContent = sf.getWorkflowService().getInDataPath(pi.getOID(),
            "DocumentFile");
      Document document = (Document) sf.getWorkflowService().getInDataPath(pi.getOID(),
            "DocumentFile");
      byte[] byteDocumentContent = sf.getDocumentManagementService()
            .retrieveDocumentContent(document.getId());
      String documentContent = new String(byteDocumentContent, "UTF-8");
      trace.debug("documentContent = " + documentContent);
      assertNotNull(document);
      assertTrue(documentFileContent instanceof Document);
      assertTrue("DocumentFile.txt".equals(document.getName()));
      assertTrue("Document File Content".equals(documentContent));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileTriggerToDocumentBDM");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }

}
