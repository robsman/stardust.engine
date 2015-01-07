package org.eclipse.stardust.engine.extensions.camel.core.endpoint.process;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class ProcessWithExternalXsdTest
{
   private static ClassPathXmlApplicationContext ctx;
   @Resource
   private static CamelContext camelContext;


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
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
      initialize();
   }

   private static void initialize()
   {
      try
      {
         RouteDefinition routeDefinition = new RouteDefinition();
         routeDefinition.startupOrder(1).autoStartup(true).from("direct:/createFile")
               .to("file://./target/incoming/customer?fileName=Person.xml");
         ((ModelCamelContext) camelContext).addRouteDefinition(routeDefinition);
         ProducerTemplate fileProducer = camelContext.createProducerTemplate();
         fileProducer.sendBody("direct:/createFile",
               "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><person><firstname>SG</firstname><lastname>SG</lastname></person>");

      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

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
