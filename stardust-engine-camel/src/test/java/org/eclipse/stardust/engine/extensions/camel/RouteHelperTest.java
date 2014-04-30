package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.ApplicationBean;
import org.eclipse.stardust.engine.core.model.beans.ApplicationTypeBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.jdom.JDOMException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class RouteHelperTest
{

   @Test
   public void testStopRunningRoute() throws Exception
   {
      ModelCamelContext camelContext = new DefaultCamelContext();
      RouteDefinition routeDefinition = new RouteDefinition();
      routeDefinition.id("dummyRoute").from("direct:testEndpoint").to("log:received ${body}");
      camelContext.addRouteDefinition(routeDefinition);
      camelContext.start();
      assertTrue(camelContext.getRoutes().size() == 1);
      assertEquals(camelContext.getRouteStatus("dummyRoute"), ServiceStatus.Started);
      RouteHelper.stopRunningRoute(camelContext, "dummyRoute");
      assertEquals(camelContext.getRouteStatus("dummyRoute"), ServiceStatus.Stopped);
   }

   @Test
   public void testStopAndRemoveRunningRoute() throws Exception
   {
      ModelCamelContext camelContext = new DefaultCamelContext();
      RouteDefinition routeDefinition = new RouteDefinition();
      routeDefinition.id("dummyRoute").from("direct:testEndpoint").to("log:received ${body}");
      camelContext.addRouteDefinition(routeDefinition);
      camelContext.start();
      assertTrue(camelContext.getRoutes().size() == 1);
      assertEquals(camelContext.getRouteStatus("dummyRoute"), ServiceStatus.Started);
      RouteHelper.stopAndRemoveRunningRoute(camelContext, "dummyRoute");
      assertTrue(camelContext.getRoutes().size() == 0);
      assertNull(camelContext.getRoute("dummyRoute"));
   }

   @Test
   public void testLoadBeanDefinition() throws JDOMException, IOException
   {
      ApplicationContext applicationContext = new GenericXmlApplicationContext();
      StringBuilder beanDefinition = new StringBuilder();
      beanDefinition.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      beanDefinition.append("<beans xmlns=\"http://www.springframework.org/schema/beans\" ");
      beanDefinition.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      beanDefinition
            .append(" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd");
      beanDefinition
            .append(" http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd\">");
      beanDefinition.append("<bean name=\"user\" class=\"org.eclipse.stardust.engine.extensions.camel.DateUtils\"/>");
      beanDefinition.append("</beans>");
      RouteHelper.loadBeanDefinition(beanDefinition, (AbstractApplicationContext) applicationContext);
      assertNotNull(applicationContext.getBean("user"));
   }

   @Test
   public void testCreateAndStartProducerRoute() throws Exception
   {
      IApplication application = new ApplicationBean("dummyApp", "Dummy App", "some description");
      application.setParent(new ModelBean("dummyModel", "Dymmy Model", ""));
      application.setApplicationType(new ApplicationTypeBean("camelSpringProducerApplication", "camelSpringProducerApplication",
            true, false));

      Map<String, String> attributes = new HashMap<String, String>();
      attributes.put("carnot:engine:camel::applicationIntegrationOverlay", "genericEndpointOverlay");
      attributes.put("carnot:engine:camel::supportsMultipleAccessPoints", "true");
      attributes.put("carnot:engine:camel::camelContextId", "defaultCamelContext");
      attributes.put("carnot:engine:camel::invocationPattern", "sendReceive");
      attributes.put("carnot:engine:camel::invocationType", "synchronous");
      attributes.put("carnot:engine:camel::processContextHeaders", "true");
      attributes.put(CamelConstants.PRODUCER_ROUTE_ATT, "&lt;to uri=&quot;seda:outboundQueue&quot;/&gt;");
      application.setAllAttributes(attributes);
      ModelCamelContext camelContext=new DefaultCamelContext();
      RouteHelper.createAndStartProducerRoute(application,camelContext , "defaultPartition");
      assertTrue(camelContext.getRouteDefinitions().size()==1);

   }

   @Test
   public void testCreateAndStartConsumerRoute() throws Exception
   {
      IApplication application = new ApplicationBean("dummyConsumerApp", "Dummy ConsumerApp", "some description");
      application.setParent(new ModelBean("dummyModel", "Dymmy Model", ""));
      application.setApplicationType(new ApplicationTypeBean("camelConsumerApplication", "camelConsumerApplication",
            true, false));
      Map<String, String> attributes = new HashMap<String, String>();
      attributes.put("carnot:engine:camel::applicationIntegrationOverlay", "genericEndpointOverlay");
      attributes.put("carnot:engine:camel::supportsMultipleAccessPoints", "true");
      attributes.put("carnot:engine:camel::camelContextId", "defaultCamelContext");
      attributes.put("carnot:engine:camel::invocationPattern", "sendReceive");
      attributes.put("carnot:engine:camel::invocationType", "synchronous");
      attributes.put("carnot:engine:camel::processContextHeaders", "true");
      StringBuilder consumerRoute = new StringBuilder("<from uri=\"direct:inbound\" />");
      consumerRoute.append("<to uri=\"log:-- $simple{body}\"/>");
      consumerRoute.append("<to uri=\"ipp:authenticate:setCurrent?user=motu&amp;password=motu\" />");
      consumerRoute.append("<to uri=\"ipp:activity:find?dataFiltersMap=$simple{header.ippDataFiltersMap}\" />");
      consumerRoute.append("<to uri=\"bean:bpmTypeConverter?method=fromXML\" />");
      consumerRoute.append("<to uri=\"ipp:activity:complete\" />");
      attributes.put("carnot:engine:camel::consumerRoute", consumerRoute.toString());
      application.setAllAttributes(attributes);

      RouteHelper.createAndStartConsumerRoute(application, new DefaultCamelContext(), "default");
   }

   @Test
   @Ignore
   public void testLoadRouteDefinition()
   {
      fail("Not yet implemented");
   }

   @Test
   public void testParseInValidSimpleExpression()
   {
      String expression = "$sile{in.header.TEST_HEADER}";
      Expression simpleExpression = RouteHelper.parseSimpleExpression(expression);
      assertNotNull(simpleExpression);
      assertTrue(simpleExpression instanceof Expression);
      assertNotEquals("dummy", simpleExpression.evaluate(createExchange(), String.class));
   }

   @Test
   public void testParseValidSimpleExpression()
   {
      String expression = "$simple{body}";
      Expression simpleExpression = RouteHelper.parseSimpleExpression(expression);
      assertNotNull(simpleExpression);
      assertTrue(simpleExpression instanceof Expression);
      assertEquals("dummy body", simpleExpression.evaluate(createExchange(), String.class));
   }

   @Test
   @Ignore
   public void testCreateRouteForAllApplications()
   {

   }

   @Test
   @Ignore
   public void testCreateRouteForApplication()
   {

   }

   private Exchange createExchange()
   {
      Exchange exchange = new DefaultExchange(new DefaultCamelContext());
      exchange.getIn().setBody("dummy body");
      exchange.getIn().setHeader("TEST_HEADER", "dummy");
      return exchange;
   }
}
