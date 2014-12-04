package org.eclipse.stardust.engine.extensions.camel.core.endpoint.activity;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

@RunWith(Suite.class)
@SuiteClasses({ ActivityEndpointTest.class })
public class ActivityEndpointTestSuite
{
   private static ClassPathXmlApplicationContext ctx;
   private static ServiceFactoryAccess serviceFactoryAccess;
   private static String[] deployedModels = {};
   
   @AfterClass
   public static void afterClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:META-INF/spring/default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      sf.getAdministrationService().cleanupRuntime(true);
      for (String deployedModelId : deployedModels)
      {
         DeployedModelQuery deployedModelQuery  = DeployedModelQuery.findActiveForId(deployedModelId);
         Models models = sf.getQueryService().getModels(deployedModelQuery);
         if(!models.isEmpty())
         {
            sf.getAdministrationService().deleteModel(models.get(0).getModelOID());
         }
      }
      sf.getAdministrationService().cleanupRuntimeAndModels();
   }
}
