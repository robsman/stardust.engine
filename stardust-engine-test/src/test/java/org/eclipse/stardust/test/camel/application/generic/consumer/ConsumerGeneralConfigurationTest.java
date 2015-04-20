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
package org.eclipse.stardust.test.camel.application.generic.consumer;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
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
public class ConsumerGeneralConfigurationTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_ID = "GenericApplicationConsumerTestModel";

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static CamelContext camelContext;

   @BeforeClass
   public static void beforeClass()
   {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
   }

   @Test
   public void testConsumerRouteProcessWithAdditionaBean() throws Exception
   {
      String message = "{\"creditCard\":{\"creditCardNumber\":411152,\"creditCardType\":\"MasterCard\"},\"lastName\":\"Last Name\",\"firstName\":\"Fist Name\"}";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithAdditionalBean",
            null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeDataWithAdditionalbean", message);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(),
            "ConsumedDataInABP"); // Additional Bean Process
      assertNotNull(result);
      assertTrue(result instanceof Map);

      assertTrue("First Name Updated In Addtional Bean".equals(result.get("firstName")));
      assertTrue("Last Name Updated In Addtional Bean".equals(result.get("lastName")));
      assertTrue((((Map< ? , ? >) (result.get("creditCard"))).get("creditCardNumber")) instanceof Long);
      assertTrue(411152 == (Long) (((Map< ? , ? >) (result.get("creditCard"))).get("creditCardNumber")));
      assertTrue("MasterCard".equals(((Map< ? , ? >) (result.get("creditCard"))).get("creditCardType")));

      ActivityInstanceQuery activityInstance = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerRouteProcessWithAdditionalBean");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstance);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }

   @Test
   public void testConsumerRouteProcessNonTransacted() throws Exception
   {
      String message = "Message content consumed by non transacted Route";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerWithNonTransactedRoute",
            null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeDataInNonTransactedRoute", message);
      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "messageInNonTransactedRoute");
      assertNotNull(result);
      assertTrue(result instanceof String);
      assertTrue(message.equals(result));
      ActivityInstanceQuery activityInstanceQueryConsumerRoute = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerWithNonTransactedRoute");
      ActivityInstances activityInstancesConsumerRoute = sf.getQueryService().getAllActivityInstances(
            activityInstanceQueryConsumerRoute);
      sf.getWorkflowService().activateAndComplete(activityInstancesConsumerRoute.get(0).getOID(), null, null);
   }
}
