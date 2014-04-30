package org.eclipse.stardust.engine.extensions.camel;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_FOOTER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_HEADER;
import static org.eclipse.stardust.engine.extensions.camel.Util.createSpringFileContent;
import static org.eclipse.stardust.engine.extensions.camel.Util.replaceSymbolicEndpoint;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ApplicationDetails;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.ApplicationBean;
import org.eclipse.stardust.engine.core.model.beans.ApplicationTypeBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.junit.Ignore;
import org.junit.Test;

public class UtilTest
{
   private static IApplication createApplication()
   {

      IApplication application = new ApplicationBean("dummyConsumerApp", "Dummy ConsumerApp", "some description");
      application.setParent(new ModelBean("dummyModel", "Dymmy Model", ""));
      application.setApplicationType(new ApplicationTypeBean("camelConsumerApplication", "camelConsumerApplication",
            true, false));
      Map<String, String> attributes = new HashMap<String, String>();
      attributes.put("carnot:engine:camel::applicationIntegrationOverlay", "genericEndpointOverlay");
      attributes.put("carnot:engine:camel::supportsMultipleAccessPoints", "true");
      attributes.put("carnot:engine:camel::camelContextId", "defaultCamelContext");
      attributes.put("carnot:engine:camel::inBodyAccessPoint", "input");
      attributes.put("carnot:engine:camel::outBodyAccessPoint", "output");

      attributes.put("carnot:engine:camel::invocationPattern", "sendReceive");
      attributes.put("carnot:engine:camel::invocationType", "synchronous");
      StringBuilder beanDefinition = new StringBuilder();
      beanDefinition
            .append("<bean id=\"ippServiceFactoryAccess\" class=\"org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess\">");
      beanDefinition.append("<property name=\"defaultUser\" value=\"motu\" />");
      beanDefinition.append("<property name=\"defaultPassword\" value=\"motu\" />");
      beanDefinition.append("</bean>");
      attributes.put("carnot:engine:camel::additionalSpringBeanDefinitions", beanDefinition.toString());

      attributes.put("carnot:engine:camel::processContextHeaders", "true");
      StringBuilder consumerRoute = new StringBuilder("<from uri=\"direct:inbound\" />");
      consumerRoute.append("<to uri=\"log:-- $simple{body}\"/>");
      consumerRoute.append("<to uri=\"ipp:authenticate:setCurrent?user=motu&amp;password=motu\" />");
      consumerRoute.append("<to uri=\"ipp:activity:find?dataFiltersMap=$simple{header.ippDataFiltersMap}\" />");
      consumerRoute.append("<to uri=\"bean:bpmTypeConverter?method=fromXML\" />");
      consumerRoute.append("<to uri=\"ipp:activity:complete\" />");
      attributes.put("carnot:engine:camel::consumerRoute", consumerRoute.toString());
      application.setAllAttributes(attributes);
      return application;
   }

   private static IApplication application = createApplication();

   @Test
   public void testGetCurrentPartitionWithParam()
   {
      assertEquals("default", Util.getCurrentPartition("default"));
   }

   @Test
   public void testGetCurrentPartitionFromContext()
   {
      assertEquals("default", Util.getCurrentPartition(null));
   }

   @Test
   @Ignore
   public void testGetUserName()
   {
      fail("Not yet implemented");
   }

   @Test
   @Ignore
   public void testGetPassword()
   {
      fail("Not yet implemented");
   }

   @Test
   @Ignore
   public void testGetProcessId()
   {
      fail("Not yet implemented");
   }

   @Test
   @Ignore
   public void testGetModelId()
   {
      fail("Not yet implemented");
   }

   @Test
   @Ignore
   public void testGetProvidedRouteConfigurationITrigger()
   {
      fail("Not yet implemented");
   }

   @Test
   public void testIsConsumerApplication()
   {
      assertTrue(Util.isConsumerApplication(application));
   }

   @Test
   public void testGetProvidedRouteConfigurationIApplication()
   {
      String expectedRoute = "<from uri=\"direct:inbound\" /><to uri=\"log:-- $simple{body}\"/><to uri=\"ipp:authenticate:setCurrent?user=motu&amp;password=motu\" /><to uri=\"ipp:activity:find?dataFiltersMap=$simple{header.ippDataFiltersMap}\" /><to uri=\"bean:bpmTypeConverter?method=fromXML\" /><to uri=\"ipp:activity:complete\" />";
      assertEquals(expectedRoute, Util.getConsumerRouteConfiguration(application));
   }

   @Test
   @Ignore
   public void testExtractBodyMainType()
   {
      fail("Not yet implemented");
   }

   @Test
   public void testGetAdditionalBeansDefinition()
   {
      StringBuilder expectedBeanDefinition = new StringBuilder();
      expectedBeanDefinition
            .append("<bean id=\"ippServiceFactoryAccess\" class=\"org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess\">");
      expectedBeanDefinition.append("<property name=\"defaultUser\" value=\"motu\" />");
      expectedBeanDefinition.append("<property name=\"defaultPassword\" value=\"motu\" />");
      expectedBeanDefinition.append("</bean>");
      assertEquals(expectedBeanDefinition.toString(), Util.getAdditionalBeansDefinition(application));
   }

   @Test
   public void testGetCamelContextIdIApplication()
   {
      assertEquals("defaultCamelContext", Util.getCamelContextId(application));
   }

   @Test
   @Ignore
   public void testGetCamelContextIdApplication()
   {
      fail("Not yet implemented");
   }

   @Test
   public void testGetInvocationPatternIApplication()
   {
      assertEquals("defaultCamelContext", Util.getCamelContextId(application));
   }

   @Test
   @Ignore
   public void testGetInvocationPatternApplication()
   {
      fail("Not yet implemented");
   }

   @Test
   public void testGetInvocationType()
   {
      assertEquals("synchronous", Util.getInvocationType(application));
   }

   @Test
   @Ignore
   public void testGetBodyOutAccessPoint()
   {
      assertEquals("output", Util.getBodyOutAccessPoint(new ApplicationDetails(application)));
   }

   @Test
   @Ignore
   public void testGetBodyInAccessPoint()
   {
      assertEquals("input", Util.getBodyInAccessPoint(new ApplicationDetails(application)));
   }

   @Test
   @Ignore
   public void testGetSupportMultipleAccessPointAttribute()
   {

   }

   @Test
   @Ignore
   public void testGetActivityInstanceApplicationContext()
   {
      fail("Not yet implemented");
   }

   @Test
   @Ignore
   public void testGetActivityInstanceDefaultContext()
   {
      fail("Not yet implemented");
   }

   @Test
   public void testIsProducerApplication()
   {
      IApplication producerApplication = new ApplicationBean("dummyConsumerApp", "Dummy ConsumerApp",
            "some description");
      producerApplication.setParent(new ModelBean("dummyModel", "Dymmy Model", ""));
      producerApplication.setApplicationType(new ApplicationTypeBean("camelSpringProducerApplication",
            "camelSpringProducerApplication", true, false));
      assertTrue(Util.isProducerApplication(producerApplication));
      assertFalse(Util.isProducerApplication(application));
   }

   @Test
   public void testGetRouteId()
   {

      assertEquals("Producer-487245793", Util.getRouteId("default", "dumyModel", null, "dummyElement", true));
      assertEquals("Consumer-487245793", Util.getRouteId("default", "dumyModel", null, "dummyElement", false));
   }

   @Test
   public void testCreateSpringFileContent()
   {
      StringBuilder expectedResult = new StringBuilder();
      expectedResult.append(SPRING_XML_HEADER);
      expectedResult.append("<bean id=\"ippServiceFactoryAccess\" class=\"org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess\"><property name=\"defaultUser\" value=\"motu\" /><property name=\"defaultPassword\" value=\"motu\"/></bean>");
      expectedResult.append(SPRING_XML_FOOTER);

      StringBuilder beanDefinition = new StringBuilder();
      beanDefinition.append("<bean id=\"ippServiceFactoryAccess\" class=\"org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess\"><property name=\"defaultUser\" value=\"motu\" /><property name=\"defaultPassword\" value=\"motu\"/></bean>");
      assertEquals(expectedResult.toString(), createSpringFileContent(beanDefinition.toString(), false, null).toString());
   }

   @Test
   public void testReplaceSymbolicEndpoint()
   {
      String expected = "<route id=\"FileTrigger\" autoStartup=\"true\" ><from uri=\"file://C:/tmp/camel/sdt?delay=5000\" /><convertBodyTo type=\"java.lang.String\"/><to uri=\"ipp:authenticate:setCurrent?user=motu&amp;password=motu\" /><to uri=\"ipp:process:start?processId=FileTriggerWithBodyConversion&amp;data=BodyContent::$simple{bodyAs(java.lang.String)}\"/></route>";

      String providedRouteDefinition = "<route id=\"FileTrigger\" autoStartup=\"true\" ><from uri=\"file://C:/tmp/camel/sdt?delay=5000\" />"
            + "<convertBodyTo type=\"java.lang.String\"/>" + "<to uri=\"ipp:direct\" />" + "</route>";
      String replacement = "ipp:authenticate:setCurrent?user=motu&amp;password=motu\" />"
            + "<to uri=\"ipp:process:start?processId=FileTriggerWithBodyConversion&amp;data=BodyContent::$simple{bodyAs(java.lang.String)}\"";
      assertTrue(replaceSymbolicEndpoint(providedRouteDefinition, replacement).equalsIgnoreCase(expected));
   }

   @Test
   public void testReplaceSymbolicEndpointAnotherExample()
   {
      String providedRouteDefinition = "<from uri=\"file://C:/tmp/camel/sdt?delay=5000\" />"
            + "<convertBodyTo type=\"java.lang.String\"/>" + "<to uri=\"ipp:direct\" />";
      String replacement = "ipp:authenticate:setCurrent?user=motu&amp;password=motu\" />"
            + "<to uri=\"ipp:process:start?processId=FileTriggerWithBodyConversion&amp;data=BodyContent::$simple{bodyAs(java.lang.String)}\"";
      replaceSymbolicEndpoint(providedRouteDefinition, replacement);
   }
}
