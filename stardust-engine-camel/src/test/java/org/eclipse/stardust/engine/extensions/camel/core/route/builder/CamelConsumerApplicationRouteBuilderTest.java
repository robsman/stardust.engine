package org.eclipse.stardust.engine.extensions.camel.core.route.builder;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.ApplicationBean;
import org.eclipse.stardust.engine.core.model.beans.ApplicationTypeBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder;
import org.eclipse.stardust.engine.extensions.camel.core.app.ConsumerApplicationRouteContext;

public class CamelConsumerApplicationRouteBuilderTest
{
   private static final Logger logger = LogManager.getLogger(CamelConsumerApplicationRouteBuilderTest.class.getCanonicalName());

   private static IApplication createConsumerApplication(String id, String providedRoute, boolean includeAttributes,
         boolean includeProcessContextHeaders, Boolean markTransacted)
   {

      IApplication application = new ApplicationBean(id, "Dummy ConsumerApp", "some description");
      application.setParent(new ModelBean("dummyModel", "Dymmy Model", ""));
      application.setApplicationType(new ApplicationTypeBean("camelConsumerApplication", "camelConsumerApplication",
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
      if (includeProcessContextHeaders)
         attributes.put("carnot:engine:camel::processContextHeaders", "true");
      if (providedRoute != null && !providedRoute.isEmpty())
         // StringBuilder providedRoute = new
         // StringBuilder("<to uri=\"jms:queue:out.queue\"/>;");
         attributes.put(CamelConstants.CONSUMER_ROUTE_ATT, providedRoute);
      if (includeAttributes)
         attributes.put("carnot:engine:camel::includeAttributesAsHeaders", "true");

      application.setAllAttributes(attributes);
      return application;
   }

   /**
    * Check that the created route is valid when no user config is provided
    */
   @Test
   public void testConsumerAppEmptyProvidedConfiguration()
   {
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Consumer1997610788\" autoStartup=\"true\"><description>This route is related to dummyApplication defined in dummyModel. The partition is :dummyPartitionId</description></route></routes>";
      String actual = RouteDefinitionBuilder.createConsumerXmlConfiguration(new ConsumerApplicationRouteContext(
            createConsumerApplication("dummyApplication", null, false, false,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   /**
    * Check that the created route is valid when user config is provided
    */
   @Test
   public void testConsumerAppWhenUserConfigIsProvided()
   {
      String userConfig = "<from uri=\"jms:input.queue\"/><to uri=\"ipp:direct\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Consumer1997610788\" autoStartup=\"true\"><description>This route is related to dummyApplication defined in dummyModel. The partition is :dummyPartitionId</description><from uri=\"jms:input.queue\"/><to uri=\"ipp:activity:find?expectedResultSize=1&amp;dataFiltersMap=$simple{header.ippDataFilterMap}\" /><to uri=\"ipp:activity:complete\" /></route></routes>";
      String actual = RouteDefinitionBuilder
            .createConsumerXmlConfiguration(new ConsumerApplicationRouteContext(createConsumerApplication("dummyApplication",
                  userConfig, false, false,null), "dummyPartitionId", "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   /**
    * Check that the created route is valid when No partitionId is provided
    */
   @Test
   public void testConsumerAppWhenEmptyPartitionIdIsProvided()
   {
      String userConfig = "<from uri=\"jms:input.queue\"/><to uri=\"ipp:direct\"/>";
      String expectedRoute = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Consumer669216206\" autoStartup=\"true\"><description>This route is related to dummyApplication defined in dummyModel. The partition is :null</description><from uri=\"jms:input.queue\"/><to uri=\"ipp:activity:find?expectedResultSize=1&amp;dataFiltersMap=$simple{header.ippDataFilterMap}\" /><to uri=\"ipp:activity:complete\" /></route></routes>";
      String actual = RouteDefinitionBuilder.createConsumerXmlConfiguration(new ConsumerApplicationRouteContext(
            createConsumerApplication("dummyApplication", userConfig, false, false,null), null, "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute, actual);
   }

   @Test
   public void testConsumerAppTransactedEASettoTrue(){
      String userConfig = "<from uri=\"jms:input.queue\"/><to uri=\"ipp:authenticate:setCurrent?username=motu&amp;password=motu\"/><to uri=\"ipp:activity:find?expectedResultSize=1&amp;dataFiltersMap=$simple{header.ippDataFilterMap}\" /><to uri=\"ipp:activity:complete\" />";
      String expectedRoute="<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Consumer669216206\" autoStartup=\"true\"><description>This route is related to dummyApplication defined in dummyModel. The partition is :null</description><from uri=\"jms:input.queue\"/><setHeader headerName=\"ippOrigin\"><constant>applicationConsumer</constant></setHeader><setHeader headerName=\"ippModelId\"><constant>dummyModel</constant></setHeader><setHeader headerName=\"ippRouteId\"><constant>Consumer669216206</constant></setHeader><transacted ref=\"required\" /><to uri=\"ipp:authenticate:setCurrent?username=motu&amp;amp;password=motu\"/><to uri=\"ipp:activity:find?expectedResultSize=1&amp;amp;dataFiltersMap=$simple{header.ippDataFilterMap}\" /><to uri=\"ipp:activity:complete\" /></route></routes>";
      String actual = RouteDefinitionBuilder.createConsumerXmlConfiguration(new ConsumerApplicationRouteContext(
            createConsumerApplication("dummyApplication", userConfig, false, false,true), null, "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute,actual);
   }
   @Test
   public void testConsumerAppTransactedEASettoFalse(){
      String userConfig = "<from uri=\"jms:input.queue\"/><to uri=\"ipp:authenticate:setCurrent?username=motu&amp;password=motu\"/><to uri=\"ipp:activity:find?expectedResultSize=1&amp;dataFiltersMap=$simple{header.ippDataFilterMap}\" /><to uri=\"ipp:activity:complete\" />";
      String expectedRoute="<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><route id=\"Consumer669216206\" autoStartup=\"true\"><description>This route is related to dummyApplication defined in dummyModel. The partition is :null</description><from uri=\"jms:input.queue\"/><setHeader headerName=\"ippOrigin\"><constant>applicationConsumer</constant></setHeader><setHeader headerName=\"ippModelId\"><constant>dummyModel</constant></setHeader><setHeader headerName=\"ippRouteId\"><constant>Consumer669216206</constant></setHeader><to uri=\"ipp:authenticate:setCurrent?username=motu&amp;amp;password=motu\"/><to uri=\"ipp:activity:find?expectedResultSize=1&amp;amp;dataFiltersMap=$simple{header.ippDataFilterMap}\" /><to uri=\"ipp:activity:complete\" /></route></routes>";
      String actual = RouteDefinitionBuilder.createConsumerXmlConfiguration(new ConsumerApplicationRouteContext(
            createConsumerApplication("dummyApplication", userConfig, false, false,false), null, "dummyContext"));
      logger.debug("Actual Execution returned :" + actual);
      assertEquals(expectedRoute,actual);
   }
}
