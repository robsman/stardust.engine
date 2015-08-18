package org.eclipse.stardust.engine.extensions.camel.trigger.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

public class GenericTriggerWithAdditionalBeanDefinitionTest
{
private static final Logger trace = LogManager.getLogger(GenericTriggerWithAdditionalBeanDefinitionTest.class.getName());
   
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
         producer.sendBody("direct:startGenericTriggerWithAdditionalBean", "message");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Test
   public void genericTriggerWithAdditionalBean() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerWithAdditionalBean"));
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(), "AdditionalBeanData");
      trace.info("AdditionalBeanData = " + response);
      assertNotNull(response); // updated in the additional bean
      assertTrue(response instanceof String);
      assertEquals("message updated in the additional bean", response.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerWithAdditionalBean");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

   }
   
}
