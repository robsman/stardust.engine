package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.*;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.*;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

public class ProcessWithExternalXsdTest
{
   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/ProcessWithExternalXsdTest-context.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml",
                  "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
      initialize();
   }

   private static void initialize()
   {
      ClassPathResource resource = new ClassPathResource("models/ExternalXSDModel.xpdl");
      testUtils.setModelFile(resource);

      try
      {
         RouteDefinition routeDefinition = new RouteDefinition();
         routeDefinition.startupOrder(1).autoStartup(true).from("direct:/createFile")
               .to("file://./target/incoming/customer?fileName=Person.xml");
         ((ModelCamelContext) camelContext).addRouteDefinition(routeDefinition);
         ProducerTemplate fileProducer = camelContext.createProducerTemplate();
         fileProducer.sendBody("direct:/createFile",
               "<person><firstname>SG</firstname><lastname>SG</lastname></person>");
         testUtils.deployModel();

      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   @Resource
   private static CamelContext camelContext;

   @Resource
   private static SpringTestUtils testUtils;

   @Resource
   private static ServiceFactoryAccess serviceFactoryAccess;

   @Test
   public void testStartPi() throws Exception
   {
      Thread.sleep(5000);
      while (camelContext.getInflightRepository().size("Consumer200230975") > 0)
      {
         System.out.println("waiting for PI");
      }
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findCompleted("{ExternalXSDModel}FileTriggerProc"));
      assertTrue(pis.size() == 1);
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(),
            "EmployeeOutput");
      assertNotNull(response);
      assertTrue(response instanceof Map);

   }
}
