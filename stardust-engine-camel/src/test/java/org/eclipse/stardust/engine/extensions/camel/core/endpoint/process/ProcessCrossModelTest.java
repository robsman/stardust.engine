package org.eclipse.stardust.engine.extensions.camel.core.endpoint.process;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class ProcessCrossModelTest
{

   private static ClassPathXmlApplicationContext ctx;
   @Resource
   private static ServiceFactoryAccess serviceFactoryAccess;
   @EndpointInject(uri = "mock:result", context = "defaultCamelContext")
   protected MockEndpoint resultEndpoint;
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml",
                  "classpath:META-INF/spring/default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
   }


   @Test
   public void testStartProcessCrossModel() throws Exception
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
