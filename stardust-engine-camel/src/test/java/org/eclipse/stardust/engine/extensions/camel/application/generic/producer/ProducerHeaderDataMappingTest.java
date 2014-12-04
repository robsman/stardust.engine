package org.eclipse.stardust.engine.extensions.camel.application.generic.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class ProducerHeaderDataMappingTest
{
   private static ClassPathXmlApplicationContext ctx;
   private static CamelContext camelContext;
   private static ServiceFactoryAccess serviceFactoryAccess;
   protected static MockEndpoint resultEndpoint;
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/application/generic/producer/ProducerApplicationTest-context.xml", "classpath:carnot-spring-context.xml",
      "classpath:jackrabbit-jcr-context.xml","classpath:META-INF/spring/default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      resultEndpoint =camelContext.getEndpoint("mock:result", MockEndpoint.class);
   }

   @Test
   public void testHeaderInDataMappingsPrimitive() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      resultEndpoint.expectedHeaderReceived("input1", "header1");
      resultEndpoint.expectedHeaderReceived("input2", "header2");
      resultEndpoint.expectedHeaderReceived("input3", "header3");

      sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testHeaderInDataMappingsPrimitive", null, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }
   
   @Test
   public void testHeaderInBodyInMappingsMixed() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      resultEndpoint.expectedHeaderReceived("input1", "header1");
      resultEndpoint.expectedHeaderReceived("input2", "header2");

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> expectedBody = new HashMap<String, Object>();
      expectedBody.put("firstName", "Manali");
      expectedBody.put("lastName", "Mungikar");
      dataMap.put("Person", expectedBody);

      resultEndpoint.expectedBodiesReceived(expectedBody);

      sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testHeaderInBodyInMappingsMixed", dataMap, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();

   }
}
