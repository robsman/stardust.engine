package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.ApplicationBean;
import org.eclipse.stardust.engine.core.model.beans.ApplicationTypeBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;
import org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder;

import org.junit.Test;

public class ProducerAppRouteBuilderTest
{
   private static final Logger logger = LogManager.getLogger(ProducerAppRouteBuilderTest.class.getCanonicalName());

   private static IApplication createProducerApplication(String id, String providedRoute, boolean includeAttributes, boolean includeProcessContextHeaders, Boolean markTransacted)
   {

      IApplication application = new ApplicationBean(id, "Dummy ConsumerApp", "some description");
      application.setParent(new ModelBean("dummyModel", "Dymmy Model", ""));
      application.setApplicationType(new ApplicationTypeBean("camelSpringProducerApplication", "camelSpringProducerApplication",
            true, false));
      Map<String, Object> attributes = new HashMap<String, Object>();
      attributes.put("carnot:engine:camel::applicationIntegrationOverlay", "genericEndpointOverlay");
      attributes.put("carnot:engine:camel::supportsMultipleAccessPoints", "true");
      attributes.put("carnot:engine:camel::camelContextId", "defaultCamelContext");
      attributes.put("carnot:engine:camel::invocationPattern", "send");
      attributes.put("carnot:engine:camel::invocationType", "synchronous");
      attributes.put("carnot:engine:camel::inBodyAccessPoint", "content");
      if(markTransacted!=null){
         if(markTransacted)
            attributes.put(CamelConstants.TRANSACTED_ROUTE_EXT_ATT, true);
         else
            attributes.put(CamelConstants.TRANSACTED_ROUTE_EXT_ATT, false);
      }
      
      if(includeProcessContextHeaders)
         attributes.put("carnot:engine:camel::processContextHeaders", "true");
      if(providedRoute!=null && !providedRoute.isEmpty())
      //StringBuilder providedRoute = new StringBuilder("<to uri=\"jms:queue:out.queue\"/>;");
      attributes.put("carnot:engine:camel::routeEntries", providedRoute);
      if(includeAttributes)
      attributes.put("carnot:engine:camel::includeAttributesAsHeaders", "true");

      application.setAllAttributes(attributes);
      return application;
   }


   /**
    * Check that the created route is valid when no user config is provided
    */
   @Test
   public void testProducerAppEmptyProvidedConfiguration()
   {
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1458931696\" autoStartup=\"true\"></route></routes>";

      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",null,false,false,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   /**
    * Check that the created route is valid when no user config is provided
    */
   @Test
   public void testProducerAppWhenUserConfigurationIsProvided()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1458931696\" autoStartup=\"true\"><from uri=\"direct://dummyPartitionId_dummyModel_dummyAppId\" /><transacted ref=\"required\" /><to uri=\"file:c:/temp\"/></route></routes>";
      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",userConfiguration,false,false,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   /**
    * Check that the created route  when no ApplicationID is provided
    * the acctual behavior is that when ApplicationId is missing an empty route is created.
    */
   @Test
   public void testProducerAppWhenEmptyAppIdIsProvided()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer-862273757\" autoStartup=\"true\"></route></routes>";
      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication(null,userConfiguration,false,false,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }
   /**
    * Check that the created route is valid when no partition ID is provided
    */
   @Test
   public void testProducerAppWhenPartitionIdIsMissing()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1823120922\" autoStartup=\"true\"><from uri=\"direct://dummyModel_dummyAppId\" /><transacted ref=\"required\" /><to uri=\"file:c:/temp\"/></route></routes>";
      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",userConfiguration,false,false,null), null, "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   /**
    * Check that when carnot:engine:camel::includeAttributesAsHeaders is false the generated route doesn't contain mapAppenderProcessor bean definition
    * this is the default behavior
    */
   @Test
   public void testProducerAppWhenIncludeAttributesAsHeadersIsFalse()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1458931696\" autoStartup=\"true\"><from uri=\"direct://dummyPartitionId_dummyModel_dummyAppId\" /><transacted ref=\"required\" /><to uri=\"file:c:/temp\"/></route></routes>";

      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",userConfiguration,false,false,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   /**
    * Check that when carnot:engine:camel::includeAttributesAsHeaders is false the generated route doesn't contain mapAppenderProcessor bean definition
    * this is the default behavior
    */
   @Test
   public void testProducerAppWhenIncludeAttributesAsHeadersIsTrue()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1458931696\" autoStartup=\"true\"><from uri=\"direct://dummyPartitionId_dummyModel_dummyAppId\" /><transacted ref=\"required\" /><process ref=\"mapAppenderProcessor\" /><to uri=\"file:c:/temp\"/></route></routes>";

      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",userConfiguration,true,false,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   /**
    * Check that when carnot:engine:camel::includeAttributesAsHeaders is false the generated route doesn't contain mapAppenderProcessor bean definition
    * this is the default behavior
    */
   @Test
   public void testProducerAppWhenIncludeProcessContextAsHeadersIsTrue()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1458931696\" autoStartup=\"true\"><from uri=\"direct://dummyPartitionId_dummyModel_dummyAppId\" /><transacted ref=\"required\" /><process ref=\"mapAppenderProcessor\" /><to uri=\"file:c:/temp\"/></route></routes>";

      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",userConfiguration,false,true,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }
   
   /**
    * 
    */
   @Test
   public void testProducerAppWhenTransactedEAIsTrue()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1458931696\" autoStartup=\"true\"><from uri=\"direct://dummyPartitionId_dummyModel_dummyAppId\" /><transacted ref=\"required\" /><process ref=\"mapAppenderProcessor\" /><to uri=\"file:c:/temp\"/></route></routes>";
      
      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",userConfiguration,false,true,true), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }
   /**
    * 
    */
   @Test
   public void testProducerAppWhenTransactedEAIsFalse()
   {
      String userConfiguration="<to uri=\"file:c:/temp\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Producer1458931696\" autoStartup=\"true\"><from uri=\"direct://dummyPartitionId_dummyModel_dummyAppId\" /><process ref=\"mapAppenderProcessor\" /><to uri=\"file:c:/temp\"/></route></routes>";
      
      String actual = RouteDefinitionBuilder.createProducerXmlConfiguration(new ProducerRouteContext(createProducerApplication("dummyAppId",userConfiguration,false,true,false), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }
}
