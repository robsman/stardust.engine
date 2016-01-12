/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.application.generic.producer;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.MODEL_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PARTITION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * when "carnot:engine:camel::includeAttributesAsHeaders" is true include Application's
 * attributes to the ecxchange headers the attribute are not added by default when
 * "carnot:engine:camel::processContextHeaders" is true include partitionId, ModelId,
 * ProcessId, PROCESS_INSTANCE_OID, ACTIVITY_ID, ACTIVITY_INSTANCE_OID to the ecxchange
 * headers the attribute are not added by default
 * </p>
 *
 * @author Sabri.Bousselmi
 */
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/camel-producer-application.app-ctx.xml")
public class IncludeHeadersStrategyTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String[] MODEL_IDS = { "CamelApplicationType", "GenericApplicationProducerTestModel", "CsvConverterTestModel" };

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static CamelContext defaultCamelContext;

   @BeforeClass
   public static void beforeClass()
   {
      defaultCamelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
   }

   @Test
   public void testIncludeProcessContextOnly()
   {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      sf.getWorkflowService().startProcess("{Camel_Application_type}TestIncludeProcesscontextOnly", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:includeProcessContextOnly",
            MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertTrue(exchange.getIn().getHeaders().size() == 7);
      assertNotNull(exchange.getIn().getHeader(PARTITION));
      assertNotNull(exchange.getIn().getHeader(MODEL_ID));
      assertEquals("Camel_Application_type", exchange.getIn().getHeader(MODEL_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_ID));
      assertEquals("TestIncludeProcesscontextOnly", exchange.getIn().getHeader(PROCESS_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_INSTANCE_OID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_ID));
      assertEquals("includeProcessContextOnly1", exchange.getIn().getHeader(ACTIVITY_ID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_INSTANCE_OID));
   }

   @Test
   public void testIncludeAttributesOnly()
   {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      sf.getWorkflowService().startProcess("{Camel_Application_type}TestIncludeAttributesOnly", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:includeAttributesOnly", MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertEquals("defaultCamelContext", exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertEquals("synchronous", exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertEquals("send", exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
      assertEquals(true, exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
   }

   @Test
   public void testTestIncludeBoth()
   {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      sf.getWorkflowService().startProcess("{Camel_Application_type}TestIncludeBoth", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:includeBoth", MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertEquals("defaultCamelContext", exchange.getIn().getHeader("carnot:engine:camel::camelContextId"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertEquals("synchronous", exchange.getIn().getHeader("carnot:engine:camel::invocationType"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertEquals("send", exchange.getIn().getHeader("carnot:engine:camel::invocationPattern"));
      assertNotNull(exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
      assertEquals(true, exchange.getIn().getHeader("carnot:engine:camel::supportsMultipleAccessPoints"));
      assertNotNull(exchange.getIn().getHeader(PARTITION));
      assertNotNull(exchange.getIn().getHeader(MODEL_ID));
      assertEquals("Camel_Application_type", exchange.getIn().getHeader(MODEL_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_ID));
      assertEquals("TestIncludeBoth", exchange.getIn().getHeader(PROCESS_ID));
      assertNotNull(exchange.getIn().getHeader(PROCESS_INSTANCE_OID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_ID));
      assertEquals("includeBoth1", exchange.getIn().getHeader(ACTIVITY_ID));
      assertNotNull(exchange.getIn().getHeader(ACTIVITY_INSTANCE_OID));
   }

   @Test
   public void testTestdefaultbehavior()
   {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Content", "abc");
      sf.getWorkflowService().startProcess("{Camel_Application_type}Testdefaultbehavior", data, true);
      MockEndpoint resultEndpoint = defaultCamelContext.getEndpoint("mock:defaultBehavior", MockEndpoint.class);
      Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
      assertNotNull(exchange);
      assertTrue(exchange.getIn().getHeaders().size() == 1);
   }
}
