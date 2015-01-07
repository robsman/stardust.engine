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

public class ConsumerGeneralConfigurationTest
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
   public void testConsumerRouteProcessWithAdditionaBean() throws Exception
   {
      Thread.sleep(3000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String message = "{\"creditCard\":{\"creditCardNumber\":411152,\"creditCardType\":\"MasterCard\"},\"lastName\":\"Last Name\",\"firstName\":\"Fist Name\"}";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithAdditionalBean",
            null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeDataWithAdditionalbean", message);
      Thread.sleep(1000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(),
            "ConsumedDataInABP"); // Additional Bean Process
      assertNotNull(result);
      assertTrue(result instanceof Map);

      assertTrue("First Name Updated In Addtional Bean".equals(result.get("firstName")));
      assertTrue("Last Name Updated In Addtional Bean".equals(result.get("lastName")));
      assertTrue((((Map< ? , ? >) (result.get("creditCard"))).get("creditCardNumber")) instanceof Long);
      assertTrue(411152 == (Long) (((Map< ? , ? >) (result.get("creditCard"))).get("creditCardNumber")));
      assertTrue("MasterCard".equals(((Map< ? , ? >) (result.get("creditCard"))).get("creditCardType")));

      ActivityInstanceQuery activityInstance = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithAdditionalBean");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstance);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

   }
   
   @Test
   public void testConsumerRouteProcessNonTransacted() throws Exception
   {
      Thread.sleep(1000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String message = "Message content consumed by non transacted Route";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerWithNonTransactedRoute",
            null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeDataInNonTransactedRoute", message);
      Thread.sleep(1000);
      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "messageInNonTransactedRoute");
      assertNotNull(result);
      assertTrue(result instanceof String);
      assertTrue(message.equals(result));
      ActivityInstanceQuery activityInstanceQueryConsumerRoute = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerWithNonTransactedRoute");
      ActivityInstances activityInstancesConsumerRoute = sf.getQueryService().getAllActivityInstances(
            activityInstanceQueryConsumerRoute);
      sf.getWorkflowService().activateAndComplete(activityInstancesConsumerRoute.get(0).getOID(), null, null);

   }

}
