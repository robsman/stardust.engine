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
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
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
public class GenericTriggerTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   public static final String MODEL_ID = "CamelTriggerTestModel";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static CamelContext camelContext;

   @BeforeClass
   public static void setUpOnce()
   {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
   }

   @Test
   public void testRouteUpdateOnCVChange() throws Exception
   {
      ConfigurationVariables cvs = sf.getAdministrationService().getConfigurationVariables("CamelTriggerTestModel");

      List<ConfigurationVariable> listOfCvs = cvs.getConfigurationVariables();

      for (Iterator<ConfigurationVariable> i = listOfCvs.iterator(); i.hasNext();)
      {
         ConfigurationVariable cv = i.next();
         if (cv.getName().equals("myConfVar"))
         {
            cv.setValue("myNewValue");
         }
      }

      cvs.setConfigurationVariables(listOfCvs);
      sf.getAdministrationService().saveConfigurationVariables(cvs, false);
   }

   @Test
   public void testStartProcessWithoutData() throws Exception
   {
      ProducerTemplate template = new DefaultProducerTemplate(camelContext);
      template.start();

      Exchange exchange = new DefaultExchange(camelContext);

      exchange = template.send("direct:testStartProcessWithoutData", exchange);
      long oid = (Long) exchange.getIn().getHeader(CamelConstants.MessageProperty.PROCESS_INSTANCE_OID);

      ProcessInstance pInstance = sf.getWorkflowService().getProcessInstance(oid);
      assertNotNull(pInstance);
   }
}
