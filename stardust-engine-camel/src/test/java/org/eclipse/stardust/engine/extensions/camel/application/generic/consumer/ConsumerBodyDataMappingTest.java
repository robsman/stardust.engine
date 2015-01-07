package org.eclipse.stardust.engine.extensions.camel.application.generic.consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class ConsumerBodyDataMappingTest
{
   private static ClassPathXmlApplicationContext ctx;
   private static CamelContext camelContext;
   private static ServiceFactoryAccess serviceFactoryAccess;

   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
   }
   
   @Test
   public void testConsumerRouteProcessWithPrimitiveData() throws Exception
   {
      Thread.sleep(1000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String message = "Message content consumed by a Create Generic Camel Endpoint saved in primitive data";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithPrimitiveData",
            null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumePrimitiveData", message);
      Thread.sleep(1000);
      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "ConsumedPrimitiveData");
      assertNotNull(result);
      assertTrue(result instanceof String);
      assertTrue(message.equals(result));
      ActivityInstanceQuery activityInstanceQueryConsumerRoute = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithPrimitiveData");
      ActivityInstances activityInstancesConsumerRoute = sf.getQueryService().getAllActivityInstances(
            activityInstanceQueryConsumerRoute);
      sf.getWorkflowService().activateAndComplete(activityInstancesConsumerRoute.get(0).getOID(), null, null);

   }

   @Test
   public void testConsumerRouteProcessWithSDT() throws Exception
   {
      Thread.sleep(2000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String message = "{\"creditCard\":{\"creditCardNumber\":451542,\"creditCardType\":\"VISA\"},\"lastName\":\"clientLN\",\"firstName\":\"clientFN\"}";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithSDT", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeSDT", message);
      Thread.sleep(1000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "ConsumedSdt");
      assertNotNull(result);
      assertTrue(result instanceof Map);
      assertTrue("clientFN".equals(result.get("firstName")));
      assertTrue("clientLN".equals(result.get("lastName")));
      assertTrue((((Map< ? , ? >) ((result).get("creditCard"))).get("creditCardNumber")) instanceof Long);
      assertTrue(451542 == (Long) (((Map< ? , ? >) ((result).get("creditCard"))).get("creditCardNumber")));
      assertTrue("VISA".equals(((Map< ? , ? >) ((result).get("creditCard"))).get("creditCardType")));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithSDT");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }
   
}
