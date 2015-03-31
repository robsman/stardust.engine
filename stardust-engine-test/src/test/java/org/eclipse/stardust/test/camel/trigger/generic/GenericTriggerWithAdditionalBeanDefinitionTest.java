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
package org.eclipse.stardust.test.camel.trigger.generic;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
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
public class GenericTriggerWithAdditionalBeanDefinitionTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(GenericTriggerWithAdditionalBeanDefinitionTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   public static final String[] MODEL_IDS = { "GenericTriggerTestModel", "GenericTriggerConverterTestModel" };

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static CamelContext camelContext;

   @BeforeClass
   public static void beforeClass() {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
      ProducerTemplate producer = camelContext.createProducerTemplate();
      // body is the 2nd parameter, it's empty in this case.
      producer.sendBody("direct:startGenericTriggerWithAdditionalBean", "message");
   }

   @Test
   public void genericTriggerWithAdditionalBean() throws Exception
   {
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerWithAdditionalBean"));
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(), "AdditionalBeanData");
      trace.info("AdditionalBeanData = " + response);
      assertNotNull(response); // updated in the additional bean
      assertTrue(response instanceof String);
      assertEquals("message updated in the additional bean", response.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerWithAdditionalBean");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }
}
