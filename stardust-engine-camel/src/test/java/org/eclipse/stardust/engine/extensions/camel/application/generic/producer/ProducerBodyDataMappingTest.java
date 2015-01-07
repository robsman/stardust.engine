package org.eclipse.stardust.engine.extensions.camel.application.generic.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

public class ProducerBodyDataMappingTest
{
   private static ClassPathXmlApplicationContext ctx;
   private static CamelContext camelContext;
//   private static SpringTestUtils testUtils;
   private static ServiceFactoryAccess serviceFactoryAccess;
   protected static MockEndpoint resultEndpoint;
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/application/generic/producer/ProducerApplicationTest-context.xml", "classpath:carnot-spring-context.xml",
      "classpath:jackrabbit-jcr-context.xml","classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
//      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      

//      try
//      {
//         ClassPathResource resource = new ClassPathResource("models/GenericApplicationProducerTestModel.xpdl");
//         testUtils.setModelFile(resource);
//         testUtils.deployModel();
//      }
//      catch (Exception e)
//      {
//         throw new RuntimeException(e);
//      }
      resultEndpoint =camelContext.getEndpoint("mock:result", MockEndpoint.class);
   }

   @Test
   public void testBodyInDataMappingPrimitive() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      String expectedBody = "ResultString";

      resultEndpoint.expectedBodiesReceived(expectedBody);

      sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testBodyInDataMappingPrimitive", null, true);

      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }
   
   @Test
   public void testBodyInDataMappingSDT() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> expectedBody = new HashMap<String, Object>();
      expectedBody.put("firstName", "Manali");
      expectedBody.put("lastName", "Mungikar");
      dataMap.put("Person", expectedBody);

      resultEndpoint.expectedBodiesReceived(expectedBody);
      sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testBodyInDataMappingSDT", dataMap, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }
   
}
