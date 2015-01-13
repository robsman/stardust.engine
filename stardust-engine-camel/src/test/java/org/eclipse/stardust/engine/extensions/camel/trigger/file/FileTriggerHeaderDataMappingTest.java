package org.eclipse.stardust.engine.extensions.camel.trigger.file;

import static org.eclipse.stardust.engine.extensions.camel.common.Util.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

public class FileTriggerHeaderDataMappingTest
{
   
   private static final Logger trace = LogManager.getLogger(FileTriggerHeaderDataMappingTest.class.getName());
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
   public void fileTriggerToPrimitiveInHeader() throws Exception
   {
      String fileContent = "primitiveData content from test class";
      createFile("target/FileDirectory/PrimitiveInHeader", "messageFile.xml", fileContent);
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}FileToPrimitiveInHeader"));
      if (pis.size() > 1)
         throw new RuntimeException("Please clean the audit Trial");
      ProcessInstance pi = pis.get(0);
      Object CamelFileNameOnly = sf.getWorkflowService().getInDataPath(pi.getOID(), "FileContent");
      trace.info("FileContent = " + CamelFileNameOnly);
      assertNotNull(CamelFileNameOnly);
      assertTrue(CamelFileNameOnly instanceof String);
      assertEquals("messageFile.xml", CamelFileNameOnly.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}FileToPrimitiveInHeader");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }

}
