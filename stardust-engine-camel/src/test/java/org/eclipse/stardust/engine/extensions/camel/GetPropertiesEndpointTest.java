package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;

import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class GetPropertiesEndpointTest
{

   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/GetPropertiesEndpointTest-context.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml",
                  "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
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

   // @Before
   // public void setUp()
   // {
   // if (!initialized)
   // initialize();
   // }
   //
   // @After
   // public void tearDown()
   // {
   //
   // try
   // {
   // this.serviceFactoryAccess.getDefaultServiceFactory().getAdministrationService().cleanupRuntimeAndModels();
   // }
   // catch (Exception e)
   // {
   // throw new RuntimeException(e);
   // }
   //
   // }

   private static void initialize()
   {
      ClassPathResource firstModelResource = new ClassPathResource(
            "models/GetPropertiesInvokerModel.xpdl");
      testUtils.setModelFile(firstModelResource);

      try
      {
         testUtils.deployModel();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      ClassPathResource secondModelResource = new ClassPathResource(
            "models/PropertiesProducerModel.xpdl");
      testUtils.setModelFile(secondModelResource);
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
      WorkflowService wfService = sf.getWorkflowService();
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put("Data", "NEW_VALUE");
      ProcessInstance pi = wfService.startProcess(
            "{PropertiesProducerModel}PropertiesProducer", datas, true);
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("TargetPI", pi.getOID());
      ProcessInstance invokerPi = wfService.startProcess(
            "{GetPropertiesInvokerModel}GetPropertiesInvoker", params, true);
      assertEquals("NEW_VALUE",
            (String) wfService.getInDataPath(invokerPi.getOID(), "Response"));
   }
}
