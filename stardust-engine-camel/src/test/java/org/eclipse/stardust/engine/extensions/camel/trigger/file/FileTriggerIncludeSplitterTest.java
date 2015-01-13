package org.eclipse.stardust.engine.extensions.camel.trigger.file;

import javax.annotation.Resource;
import static org.eclipse.stardust.engine.extensions.camel.common.Util.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

public class FileTriggerIncludeSplitterTest
{
   private static final Logger trace = LogManager.getLogger(FileTriggerIncludeSplitterTest.class
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
   }
   
   @Test
   public void testSplitWithFileTrigger() throws Exception
   {
      createFile("target/FileDirectory/SplitTest", "SplitFile.txt", "line1\nline2");
      Thread.sleep(5000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{FileTriggerTestModel}SplitProcess"));
      ProcessInstance pi1 = pis.get(0);
      Object responseLine1 = sf.getWorkflowService().getInDataPath(pi1.getOID(),
            "SplitFile");
      trace.info("Split File Content = " + responseLine1);
      assertNotNull(responseLine1);
      assertTrue(responseLine1 instanceof String);
      assertEquals("line1", responseLine1.toString());

      ProcessInstance pi2 = pis.get(1);
      Object responseLine2 = sf.getWorkflowService().getInDataPath(pi2.getOID(),
            "SplitFile");
      trace.info("Split File Content = " + responseLine2);
      assertNotNull(responseLine2);
      assertTrue(responseLine2 instanceof String);
      assertEquals("line2", responseLine2.toString());

      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{FileTriggerTestModel}SplitProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(
            activityInstanceQuery);

      trace.info("activityInstance state = " + activityInstances.get(0).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

      trace.info("activityInstance state = " + activityInstances.get(1).getState());
      sf.getWorkflowService().activateAndComplete(activityInstances.get(1).getOID(),
            null, null);
      trace.info("activityInstance state = " + activityInstances.get(1).getState());
   }

}
