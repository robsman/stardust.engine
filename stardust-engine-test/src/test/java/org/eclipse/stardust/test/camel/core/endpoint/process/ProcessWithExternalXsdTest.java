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
package org.eclipse.stardust.test.camel.core.endpoint.process;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
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
public class ProcessWithExternalXsdTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String[] MODEL_IDS = { "GetPropertiesInvokerModel", "PropertiesProducerModel", "ExternalXSDModel", "ProcessContinueModel", "CamelTestModel" };

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static CamelContext camelContext;

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
      initialize();
   }

   @Test(timeout = 10000)
   public void testStartPi() throws Exception
   {
      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findCompleted("{ExternalXSDModel}FileTriggerProc")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findCompleted("{ExternalXSDModel}FileTriggerProc"));
      assertTrue(pis.size() == 1);
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(),
            "EmployeeOutput");
      assertNotNull(response);
      assertTrue(response instanceof Map);
   }

   private static void initialize() throws Exception
   {
      RouteDefinition routeDefinition = new RouteDefinition();
      routeDefinition.startupOrder(1).autoStartup(true).from("direct:/createFile").to("file://target/incoming/customer?fileName=Person.xml");
      ((ModelCamelContext) camelContext).addRouteDefinition(routeDefinition);

      ProducerTemplate fileProducer = camelContext.createProducerTemplate();
      fileProducer
            .sendBody(
                  "direct:/createFile",
                  "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><person><firstname>SG</firstname><lastname>SG</lastname></person>");
   }
}
