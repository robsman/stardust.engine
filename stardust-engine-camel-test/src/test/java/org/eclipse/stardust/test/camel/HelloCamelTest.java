package org.eclipse.stardust.test.camel;

import static org.apache.camel.builder.script.ScriptBuilder.javaScript;
import static org.apache.camel.component.jms.JmsComponent.jmsComponent;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.jms.Queue;

import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsMessageType;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class HelloCamelTest
{
   /* package-private */static final String START_EVENTS_MODEL_NAME = "MultipleStartEventsTest";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, START_EVENTS_MODEL_NAME);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private SpringCamelContext camelContext;

   @Before
   public void initCamelRoute() throws Exception
   {
      this.camelContext = new SpringCamelContext(SpringUtils.getApplicationContext());
      camelContext.setTracing(true);

      camelContext.addComponent("jms",
            jmsComponent(testClassSetup.queueConnectionFactory()));

      new RouteBuilder(camelContext)
      {
         @Override
         public void configure() throws Exception
         {

            from("direct:js-test")
                  //
                  .setBody(
                        javaScript("var a = 1; var b = a + 1; result = 'Echo ' + body;"))
                  .to("log:output");

            from("direct:test").id("triggerStartEvent1")
                  //
                  .setHeader("StartEventName").constant("bla")
                  .to("jms:queue:jmsApplicationQueue?jmsMessageType=Text&transactionManager=jtaTxManager")
                  .transacted();
         }
      }.addRoutesToCamelContext(camelContext);

      camelContext.start();
   }

   @After
   public void shutdownCamel() throws Exception
   {
      if (null != camelContext)
      {
         camelContext.shutdown();
         this.camelContext = null;
      }
   }

   @Test
   public void test() throws Exception
   {
      assertThat(camelContext, is(notNullValue()));

      Endpoint jsTest = camelContext.getEndpoint("direct:test");
      assertThat(jsTest, is(notNullValue()));

      ProducerTemplate pt = camelContext.createProducerTemplate();

      pt.sendBody(jsTest, "Bla");
   }

}
