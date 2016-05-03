package org.eclipse.stardust.test.camel.application.generic.producer;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.BreakpointSupport;
import org.apache.camel.impl.ConditionSupport;
import org.apache.camel.impl.DefaultDebugger;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Breakpoint;
import org.apache.camel.spi.Condition;
import org.apache.camel.spi.Debugger;
import org.apache.camel.spi.InterceptStrategy;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * Test Retry feature for camel producer application
 * 
 * @author Fradj.ZAYEN
 *
 */
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/camel-producer-application.app-ctx.xml")
public class ProducerApplicationWithRetryTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager
         .getLogger(ProducerApplicationWithRetryTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String[] MODEL_IDS = {"ProducerApplicationWithRetry"};

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private static ModelCamelContext camelContext;
   private static List<String> logs = new ArrayList<String>();
   private static Condition exceptionCondition;
   private static Breakpoint breakpoint;
   
   @BeforeClass
   public static void beforeClass()
   {
      camelContext = testClassSetup.getBean("defaultCamelContext",
            ModelCamelContext.class);
      breakpoint = new BreakpointSupport() {
         @Override
         public void afterProcess(Exchange exchange, Processor processor, ProcessorDefinition<?> definition, long timeTaken) {
             Exception e = exchange.getException();
             logs.add("Breakpoint at " + definition.getShortName() + " caused by: " + e.getClass().getSimpleName() + "[" + e.getMessage() + "]");
         }
     };

     exceptionCondition = new ConditionSupport() {
         @Override
         public boolean matchProcess(Exchange exchange, Processor processor, ProcessorDefinition<?> definition) {
             return exchange.getException() != null;
         }
     };
     Debugger debugger=new DefaultDebugger();
     debugger.setCamelContext(camelContext);
     debugger.addBreakpoint(breakpoint, exceptionCondition);
     
     camelContext.setDebugger(debugger);
   }
   
//   @Before
//   public static void setUp() throws Exception {
//
//   }

   @Test
   public void testRetryBehaviorForProducerApplication() throws Exception
   {
      String generatedRouteId = "Producer496420070";
      RouteDefinition routeDefinition = camelContext.getRouteDefinition(generatedRouteId);
      routeDefinition.adviceWith(camelContext, new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            onCompletion().to("mock:end");
         }
      });
      MockEndpoint end = camelContext.getEndpoint("mock:end", MockEndpoint.class);
      
      end.expectedBodiesReceived("some data provided from Junit Test");
      end.expectedHeaderReceived("CamelRedeliveryCounter", 1);
      end.expectedHeaderReceived("CamelRedeliveryMaxCounter", 1);
      end.expectedHeaderReceived("CamelRedelivered", true);
      
      camelContext.addRouteDefinition(routeDefinition);
      camelContext.startRoute("Producer496420070");
      
      
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("Message", "some data provided from Junit Test");
      try
      {
         sf.getWorkflowService().startProcess(
               "{ProducerApplicationWithRetry}ThrowExceptionTestProcess", dataMap, true);
      }
      catch (Exception e)
      {
        //ignore
      }
      end.assertIsSatisfied();

   }

}
