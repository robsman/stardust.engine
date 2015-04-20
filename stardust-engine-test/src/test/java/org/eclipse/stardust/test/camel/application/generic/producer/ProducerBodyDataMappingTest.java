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

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
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
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/camel-producer-application.app-ctx.xml")
public class ProducerBodyDataMappingTest extends AbstractCamelIntegrationTest
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

   private static MockEndpoint resultEndpoint;

   @BeforeClass
   public static void beforeClass()
   {
      CamelContext camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
      resultEndpoint = camelContext.getEndpoint("mock:result", MockEndpoint.class);
   }

   @Test
   public void testBodyInDataMappingPrimitive() throws Exception
   {
      String expectedBody = "ResultString";
      resultEndpoint.expectedBodiesReceived(expectedBody);
      sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testBodyInDataMappingPrimitive", null, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }

   @Test
   public void testBodyInDataMappingSDT() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> expectedBody = new HashMap<String, Object>();
      expectedBody.put("firstName", "Manali");
      expectedBody.put("lastName", "Mungikar");
      dataMap.put("Person", expectedBody);
      resultEndpoint.expectedBodiesReceived(expectedBody);
      sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testBodyInDataMappingSDT", dataMap, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }
}
