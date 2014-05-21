package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_REMOVE_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Process.COMMAND_START;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class SpawnSubProcessEndpointTest
{
   boolean initiated;

   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/SpawnSubProcessEndpointTest-context.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml",
                  "classpath:default-camel-context.xml"});
      defaultCamelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      if (!initiated)
      {
         try
         {
            defaultCamelContext.addRoutes(createFullRoute());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
   }

   private static CamelContext defaultCamelContext;

   private static SpringTestUtils testUtils;

   private static ServiceFactoryAccess serviceFactoryAccess;

   private static final String FULL_ROUTE_BEGIN = "direct:startSpawnSubProcessEndpointTestRoute";

   @Before
   public void setUp()
   {
      try
      {
         if (!initiated)
            setUpGlobal();
         ClassPathResource resource = new ClassPathResource(
               "models/SpawnSubProcessModel.xpdl");
         testUtils.setModelFile(resource);

         testUtils.deployModel();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @After
   public void tearDown()
   {

      try
      {
         serviceFactoryAccess.getDefaultServiceFactory().getAdministrationService()
               .cleanupRuntimeAndModels();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   @Test
   public void testStartProcess() throws Exception
   {

      Map<String, Object> headerMap = new HashMap<String, Object>();
      Exchange exchange = new DefaultExchange(defaultCamelContext);
      String messageBody = "Message from Unit Test";

      exchange = CamelTestUtils.invokeEndpoint(FULL_ROUTE_BEGIN, exchange, headerMap,
            messageBody);
   }

   public void setUpGlobal() throws Exception
   {
      // initiate environment
      testUtils.setUpGlobal();
      initiated = true;
   }

   public static RouteBuilder createFullRoute()
   {
      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from(FULL_ROUTE_BEGIN)
                  .to("ipp:authenticate:" + COMMAND_SET_CURRENT
                        + "?user=motu&password=motu")
                  .to("ipp:process:"
                        + COMMAND_START
                        + "?processId=MainProcess&modelId=SpawnSubProcessModel&synchronousMode=false&data=MessageBody::${body}")
                  .log("Created process instance OID: ${header." + PROCESS_INSTANCE_OID
                        + "}")
                  .to("ipp:process:spawnSubprocess?parentProcessInstanceOid=${header."
                        + PROCESS_INSTANCE_OID
                        + "}&processId=CustomSubProcess&copyData=false&data=MessageBody::${body}")
                  .to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT);

         }
      };
   }
}
