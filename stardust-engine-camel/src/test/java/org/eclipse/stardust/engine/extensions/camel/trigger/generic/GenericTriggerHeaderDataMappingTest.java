package org.eclipse.stardust.engine.extensions.camel.trigger.generic;

import static org.eclipse.stardust.engine.extensions.camel.common.Util.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class GenericTriggerHeaderDataMappingTest
{
   private static final Logger trace = LogManager.getLogger(GenericTriggerHeaderDataMappingTest.class.getName());
   
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
            "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      try
      {
         ProducerTemplate producer = camelContext.createProducerTemplate();
         // body is the 2nd parameter, it's empty in this case.
         producer.sendBodyAndHeader("direct:startGenericTriggerToPrimitiveInHeader", "", "GenericTriggerToPrimitiveInHeader",
               "primitive Data content set in Header from test class Generic Trigger");
         Map<String, String> projectMap = new HashMap<String, String>();
         projectMap.put("id", "22");
         projectMap.put("name", "Camel");
         projectMap.put("license", "ASF");
         producer.sendBodyAndHeader("direct:startGenericTriggerToSdtInHeader", "", "project", projectMap);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Test
   public void genericTriggerToPrimitiveInHeader() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToPrimitiveInHeader"));
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(), "PrimitiveDataInHeader");
      trace.info("PrimitiveDataInHeader = " + response);
      assertNotNull(response);
      assertTrue(response instanceof String);
      assertEquals("primitive Data content set in Header from test class Generic Trigger", response.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerToPrimitiveInHeader");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

   }

   @Test
   public void genericTriggerToSdtInHeader() throws Exception
   {
      createFile("./target/FileDirectory/DocumentGT", "DocumentFileGT.txt", "Document File Content GT");
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToSdtInHeader"));
      ProcessInstance pi = pis.get(0);
      Object id = sf.getWorkflowService().getInDataPath(pi.getOID(), "id");
      trace.info("id = " + id);
      assertNotNull(id);
      assertTrue(id instanceof String);
      assertEquals("22", id.toString());
      Object name = sf.getWorkflowService().getInDataPath(pi.getOID(), "name");
      trace.info("name = " + name);
      assertNotNull(name);
      assertTrue(name instanceof String);
      assertEquals("Camel", name.toString());
      Object license = sf.getWorkflowService().getInDataPath(pi.getOID(), "license");
      trace.info("license = " + license);
      assertNotNull(license);
      assertTrue(license instanceof String);
      assertEquals("ASF", license.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerToSdtInHeader");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

   }

}
