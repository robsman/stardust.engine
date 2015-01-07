package org.eclipse.stardust.engine.extensions.camel.application.generic.consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

public class ConsumerHeaderDataMappingTest
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
   public void testConsumerRouteProcessMessageInHeader() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String message = "Message send to consumer route in header";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerDataInHeaderProcess", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBodyAndHeader("direct:startConsumeConsumeMessageInHeader", "", "MessageInHeader", message);
      Thread.sleep(1000);
      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "MessageInHeader");
      assertNotNull(result);
      assertTrue(result instanceof String);
      assertTrue(message.equals(result));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerDataInHeaderProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

   }
}
