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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
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
public class GenericTriggerDataPathTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(GenericTriggerDataPathTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   public static final String[] MODEL_IDS = { "GenericTriggerTestModel", "GenericTriggerConverterTestModel" };

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @BeforeClass
   public static void setUpOnce()
   {
      DeploymentOptions options = DeploymentOptions.DEFAULT;
      options.setIgnoreWarnings(true);
      RtEnvHome.deployModel(adminSf.getAdministrationService(), options, "GenericTriggerDataPathTestModel");
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testDataPath() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      String firstName = "Marcelo";
      String lastName = "Tom";

      String street = "1234 Main Street";
      String city = "London";

      String firstFieldOfListSdts = "Email 1 : marcelo.tom@test.com";
      String secondFieldOfListSdts = "Email 2 : marcelo@work.com";

      String anotherFieldOfListSdts = "anotherFieldOfListSdts1InC";

      String firstElementOflistOfText = "Phone 1: +245142145147";
      String secondElementOfListOfString = "Phone 2: +95554564564";

      String content = "Software Developer";

      dataMap.put("firstName", firstName);
      dataMap.put("lastName", lastName);

      dataMap.put("street", street);
      dataMap.put("city", city);

      dataMap.put("FirstFieldOfListSdts", firstFieldOfListSdts);
      dataMap.put("SecondFieldOfListSdts", secondFieldOfListSdts);

      dataMap.put("anotherFieldOfListSdts", anotherFieldOfListSdts);

      dataMap.put("firstElementOflistOfText", firstElementOflistOfText);
      dataMap.put("secondElementOfListOfString", secondElementOfListOfString);
      dataMap.put("Content", content);
      sf.getWorkflowService().startProcess("{GenericTriggerDataPathTestModel}TestGenericTriggerDataPath", dataMap, true);

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerDataPathTestModel}ConsumerProcess"));
      ProcessInstance pi = pis.get(0);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pi.getOID(), "Person");

      trace.info("get Person : " + response);
      Object response1 = sf.getWorkflowService().getInDataPath(pi.getOID(), "MessageBody");
      trace.info("get MessageBody : " + response1);
      assertNotNull(response1);
      assertTrue(response1 instanceof String);
      assertEquals("Software Developer", response1);

      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertEquals(firstName, response.get("firstName"));
      assertEquals(lastName, response.get("lastName"));

      assertTrue(response.get("address") instanceof Map);
      trace.debug("address =" + response.get("address"));
      Map< ? , ? > address = (Map< ? , ? >) response.get("address");
      assertNotNull(address);
      assertTrue(address instanceof Map);
      assertEquals(city, address.get("city"));
      assertEquals(street, address.get("street"));

      List listOfSdt = (List) response.get("simpleListOfSdts");
      assertTrue(listOfSdt instanceof List);
      assertEquals(firstFieldOfListSdts, ((Map< ? , ? >) listOfSdt.get(0)).get("A"));
      assertEquals(secondFieldOfListSdts, ((Map< ? , ? >) listOfSdt.get(1)).get("A"));

      List listOfString = (List) response.get("listOfString");
      assertTrue(listOfString instanceof List);
      assertEquals(firstElementOflistOfText, listOfString.get(0));
      assertEquals(secondElementOfListOfString, listOfString.get(1));

      ActivityInstance ai = sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());
      sf.getWorkflowService().complete(ai.getOID(), null, null);
   }
}
