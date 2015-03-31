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

import static org.eclipse.stardust.test.camel.common.Util.createFile;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

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
public class GenericTriggerHeaderDataMappingTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(GenericTriggerHeaderDataMappingTest.class.getName());

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
   public static void beforeClass()
   {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);

      ProducerTemplate producer = camelContext.createProducerTemplate();
      // body is the 2nd parameter, it's empty in this case.
      producer.sendBodyAndHeader("direct:startGenericTriggerToPrimitiveInHeader", "", "GenericTriggerToPrimitiveInHeader",
            "primitive Data content set in Header from test class Generic Trigger");
      Map<String, String> projectMap = new HashMap<String, String>();
      projectMap.put("id", "22");
      projectMap.put("name", "Camel");
      projectMap.put("license", "ASF");
      producer.sendBodyAndHeader("direct:startGenericTriggerToSdtInHeader", "", "project", projectMap);
   }

   @Test(timeout = 10000)
   public void genericTriggerToPrimitiveInHeader() throws Exception
   {
      ProducerTemplate producer = camelContext.createProducerTemplate();
      // body is the 2nd parameter, it's empty in this case.
      producer.sendBodyAndHeader("direct:startGenericTriggerToPrimitiveInHeader", "", "GenericTriggerToPrimitiveInHeader",
            "primitive Data content set in Header from test class Generic Trigger");

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToPrimitiveInHeader")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToPrimitiveInHeader"));
      ProcessInstance pi = pis.get(0);
      Object response = sf.getWorkflowService().getInDataPath(pi.getOID(), "PrimitiveDataInHeader");
      trace.info("PrimitiveDataInHeader = " + response);
      assertNotNull(response);
      assertTrue(response instanceof String);
      assertEquals("primitive Data content set in Header from test class Generic Trigger", response.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerToPrimitiveInHeader");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());

   }

   @Test(timeout = 10000)
   public void genericTriggerToSdtInHeader() throws Exception
   {
      ProducerTemplate producer = camelContext.createProducerTemplate();
      Map<String, String> projectMap = new HashMap<String, String>();
      projectMap.put("id", "22");
      projectMap.put("name", "Camel");
      projectMap.put("license", "ASF");
      producer.sendBodyAndHeader("direct:startGenericTriggerToSdtInHeader", "", "project", projectMap);

      createFile("target/FileDirectory/DocumentGT", "DocumentFileGT.txt", "Document File Content GT");

      // TODO find a reliable criterion of waiting for the process to be triggered
      while (sf.getQueryService().getAllProcessInstances(ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToSdtInHeader")).size() == 0)
      {
         Thread.sleep(1000);
      }

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerTestModel}GenericTriggerToSdtInHeader"));
      ProcessInstance pi = pis.get(0);
      Object id = sf.getWorkflowService().getInDataPath(pi.getOID(), "id");
      trace.info("id = " + id);
      assertNotNull(id);
      assertTrue(id instanceof String);
      assertEquals("22", id.toString());
      Object name = sf.getWorkflowService().getInDataPath(pi.getOID(), "name");
      trace.info("name = " + name);
      assertNotNull(name);
      assertTrue(name instanceof String);
      assertEquals("Camel", name.toString());
      Object license = sf.getWorkflowService().getInDataPath(pi.getOID(), "license");
      trace.info("license = " + license);
      assertNotNull(license);
      assertTrue(license instanceof String);
      assertEquals("ASF", license.toString());
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{GenericTriggerTestModel}GenericTriggerToSdtInHeader");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
      trace.info("activityInstance state = " + activityInstances.get(0).getState());
   }
}
