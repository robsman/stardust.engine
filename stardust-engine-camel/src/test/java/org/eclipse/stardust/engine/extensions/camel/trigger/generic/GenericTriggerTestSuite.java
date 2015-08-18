package org.eclipse.stardust.engine.extensions.camel.trigger.generic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

@RunWith(Suite.class)
@SuiteClasses({
      GenericTriggerBodyDataMappingTest.class, GenericTriggerHeaderDataMappingTest.class,
      GenericTriggerWithAdditionalBeanDefinitionTest.class,
      GenericTriggerIncludeConverterTest.class, GenericTriggerDataPathTest.class})
public class GenericTriggerTestSuite
{
   private static ClassPathXmlApplicationContext ctx;
   private static ServiceFactoryAccess serviceFactoryAccess;
   private static ServiceFactory sf;
   private static SpringTestUtils testUtils;
   private static String[] deployedModels = { "GenericTriggerTestModel", "GenericTriggerDataPathTestModel", "GenericTriggerConverterTestModel" };
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      sf = serviceFactoryAccess.getDefaultServiceFactory();
      
      try
      {
         for (String deployedModelId : deployedModels)
         {
            ClassPathResource resource = new ClassPathResource("models/" + deployedModelId + ".xpdl");
            testUtils.setModelFile(resource);
            testUtils.deployModel();
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   
   @AfterClass
   public static void afterClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      sf = serviceFactoryAccess.getDefaultServiceFactory();
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
