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

public class ConsumerIncludeConverterTest
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
   public void testConsumerWithFromXmlConverter() throws InterruptedException
   {
      Thread.sleep(1000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String xmlMessage = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><customer><customerId>1</customerId><firstName>FN</firstName><lastName>LN</lastName><salary>5000</salary></customer>";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromXmlConverter", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeFromXmlConverter", xmlMessage);
      Thread.sleep(1000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "customer");
      assertNotNull(result);
      assertTrue(result instanceof Map);
      assertTrue("1".equals(result.get("customerId")));
      assertTrue("FN".equals(result.get("firstName")));
      assertTrue("LN".equals(result.get("lastName")));
      assertTrue(result.get("salary") instanceof Integer);
      assertTrue(5000 == (Integer)result.get("salary"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromXmlConverter");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }
   @Test
   public void testConsumerWithFromJSONConverter() throws InterruptedException
   {
      Thread.sleep(1000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String jsonMessage = "{\"customerId\":\"1\",\"lastName\":\"LN\",\"firstName\":\"FN\",\"salary\":\"5000\"}";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromJsonConverter", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeFromJsonConverter", jsonMessage);
      Thread.sleep(1000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "customer");
      assertNotNull(result);
      assertTrue(result instanceof Map);
      assertTrue("1".equals(result.get("customerId")));
      assertTrue("FN".equals(result.get("firstName")));
      assertTrue("LN".equals(result.get("lastName")));
      assertTrue(result.get("salary") instanceof Integer);
      assertTrue(5000 == (Integer)result.get("salary"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromJsonConverter");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }
   @Test
   public void testConsumerWithFromCsvConverter() throws InterruptedException
   {
      Thread.sleep(1000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String csvMessage = "customerId,firstName,lastName,salary\n1,FN,LN,5000";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromCsvConverter", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeFromCsvConverter", csvMessage);
      Thread.sleep(1000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "customer");
      assertNotNull(result);
      assertTrue(result instanceof Map);
      assertTrue("1".equals(result.get("customerId")));
      assertTrue("FN".equals(result.get("firstName")));
      assertTrue("LN".equals(result.get("lastName")));
      assertTrue(result.get("salary") instanceof Integer);
      assertTrue(5000 == (Integer)result.get("salary"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromCsvConverter");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }
}
