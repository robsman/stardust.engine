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
public class ConsumerIncludeConverterTest extends AbstractCamelIntegrationTest
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
   public void testConsumerWithFromXmlConverter() throws InterruptedException
   {
      String xmlMessage = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><customer><customerId>1</customerId><firstName>FN</firstName><lastName>LN</lastName><salary>5000</salary></customer>";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromXmlConverter", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeFromXmlConverter", xmlMessage);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "customer");
      assertNotNull(result);
      assertTrue(result instanceof Map);
      assertTrue("1".equals(result.get("customerId")));
      assertTrue("FN".equals(result.get("firstName")));
      assertTrue("LN".equals(result.get("lastName")));
      assertTrue(result.get("salary") instanceof Integer);
      assertTrue(5000 == (Integer)result.get("salary"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromXmlConverter");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }

   @Test
   public void testConsumerWithFromJSONConverter() throws InterruptedException
   {
      String jsonMessage = "{\"customerId\":\"1\",\"lastName\":\"LN\",\"firstName\":\"FN\",\"salary\":\"5000\"}";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromJsonConverter", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeFromJsonConverter", jsonMessage);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "customer");
      assertNotNull(result);
      assertTrue(result instanceof Map);
      assertTrue("1".equals(result.get("customerId")));
      assertTrue("FN".equals(result.get("firstName")));
      assertTrue("LN".equals(result.get("lastName")));
      assertTrue(result.get("salary") instanceof Integer);
      assertTrue(5000 == (Integer)result.get("salary"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromJsonConverter");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }

   @Test
   public void testConsumerWithFromCsvConverter() throws InterruptedException
   {
      String csvMessage = "customerId,firstName,lastName,salary\n1,FN,LN,5000";
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromCsvConverter", null, true);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      producer.sendBody("direct:startConsumeFromCsvConverter", csvMessage);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "customer");
      assertNotNull(result);
      assertTrue(result instanceof Map);
      assertTrue("1".equals(result.get("customerId")));
      assertTrue("FN".equals(result.get("firstName")));
      assertTrue("LN".equals(result.get("lastName")));
      assertTrue(result.get("salary") instanceof Integer);
      assertTrue(5000 == (Integer)result.get("salary"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericApplicationConsumerTestModel}TestConsumerIncludeFromCsvConverter");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
   }
}
