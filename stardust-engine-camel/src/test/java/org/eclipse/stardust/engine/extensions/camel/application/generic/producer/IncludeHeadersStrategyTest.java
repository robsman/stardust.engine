package org.eclipse.stardust.engine.extensions.camel.application.generic.producer;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.MODEL_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PARTITION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * when "carnot:engine:camel::includeAttributesAsHeaders" is true include Application's
 * attributes to the ecxchange headers the attribute are not added by default when
 * "carnot:engine:camel::processContextHeaders" is true include partitionId, ModelId,
 * ProcessId, PROCESS_INSTANCE_OID, ACTIVITY_ID, ACTIVITY_INSTANCE_OID to the ecxchange
 * headers the attribute are not added by default
 *
 */
public class IncludeHeadersStrategyTest
{

   private static CamelContext defaultCamelContext;
   /**
    * Use this service factory access for testing assumptions!
    */
   private static ServiceFactoryAccess serviceFactoryAccess;

   @Produce(uri = "direct:in")
   protected ProducerTemplate defaultProducerTemplate;
   private static ClassPathXmlApplicationContext ctx;
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      defaultCamelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
   }

   @Test
   public void testIncludeProcessContextOnly()
   {
      WorkflowService workflowService = serviceFactoryAccess.getDefaultServiceFactory().getWorkflowService();
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      workflowService.startProcess("{Camel_Application_type}TestIncludeProcesscontextOnly", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:includeProcessContextOnly",
            MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertTrue(exchange.getIn().getHeaders().size() == 7);
      assertNotNull(exchange.getIn().getHeader(PARTITION));
      assertNotNull(exchange.getIn().getHeader(MODEL_ID));
      assertEquals("Camel_Application_type", exchange.getIn().getHeader(MODEL_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_ID));
      assertEquals("TestIncludeProcesscontextOnly", exchange.getIn().getHeader(PROCESS_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_INSTANCE_OID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_ID));
      assertEquals("includeProcessContextOnly1", exchange.getIn().getHeader(ACTIVITY_ID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_INSTANCE_OID));
   }

   @Test
   public void testIncludeAttributesOnly()
   {
      WorkflowService workflowService = serviceFactoryAccess.getDefaultServiceFactory().getWorkflowService();
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      workflowService.startProcess("{Camel_Application_type}TestIncludeAttributesOnly", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:includeAttributesOnly", MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertEquals("defaultCamelContext", exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertEquals("synchronous", exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertEquals("send", exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
      assertEquals(true, exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
   }

   @Test
   public void testTestIncludeBoth()
   {
      WorkflowService workflowService = serviceFactoryAccess.getDefaultServiceFactory().getWorkflowService();
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      workflowService.startProcess("{Camel_Application_type}TestIncludeBoth", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:includeBoth", MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertEquals("defaultCamelContext", exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertEquals("synchronous", exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertEquals("send", exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
      assertEquals(true, exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
      assertNotNull(exchange.getIn().getHeader(PARTITION));
      assertNotNull(exchange.getIn().getHeader(MODEL_ID));
      assertEquals("Camel_Application_type", exchange.getIn().getHeader(MODEL_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_ID));
      assertEquals("TestIncludeBoth", exchange.getIn().getHeader(PROCESS_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_INSTANCE_OID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_ID));
      assertEquals("includeBoth1", exchange.getIn().getHeader(ACTIVITY_ID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_INSTANCE_OID));
   }

   @Test
   public void testTestdefaultbehavior()
   {
      WorkflowService workflowService = serviceFactoryAccess.getDefaultServiceFactory().getWorkflowService();
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      workflowService.startProcess("{Camel_Application_type}Testdefaultbehavior", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:defaultBehavior", MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertTrue(exchange.getIn().getHeaders().size() == 1);
   }
}
