package org.eclipse.stardust.engine.extensions.camel.trigger.generic;

import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

public class GenericTriggerTest
{

   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/trigger/generic/CamelTriggerTest-context.xml", "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      initialize();
   }
   
   @Resource
   private static CamelContext camelContext;

   @Resource
   private static SpringTestUtils testUtils;

   @Resource
   private static ServiceFactoryAccess serviceFactoryAccess;

   @EndpointInject(uri = "mock:result", context = "defaultCamelContext")
   protected MockEndpoint resultEndpoint;


   private static void initialize()
   {
      ClassPathResource resource = new ClassPathResource("models/CamelTriggerTestModel.xpdl");
      testUtils.setModelFile(resource);

      try
      {
         testUtils.deployModel();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
     
   }

   @Test
   public void testRouteUpdateOnCVChange() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      ConfigurationVariables cvs = sf.getAdministrationService().getConfigurationVariables("CamelTriggerTestModel");

      List<ConfigurationVariable> listOfCvs = cvs.getConfigurationVariables();

      for (Iterator<ConfigurationVariable> i = listOfCvs.iterator(); i.hasNext();)
      {
         ConfigurationVariable cv = i.next();
         if (cv.getName().equals("myConfVar"))
         {
            cv.setValue("myNewValue");
         }
      }

      cvs.setConfigurationVariables(listOfCvs);
      sf.getAdministrationService().saveConfigurationVariables(cvs, false);
   }

   @Test
   public void testStartProcessWithoutData() throws Exception
   {

      ProducerTemplate template = new DefaultProducerTemplate(camelContext);
      template.start();

      Exchange exchange = new DefaultExchange(camelContext);

      exchange = template.send("direct:testStartProcessWithoutData", exchange);
      long oid = (Long) exchange.getIn().getHeader(CamelConstants.MessageProperty.PROCESS_INSTANCE_OID);

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      ProcessInstance pInstance = sf.getWorkflowService().getProcessInstance(oid);
      assertNotNull(pInstance);

   }

}
