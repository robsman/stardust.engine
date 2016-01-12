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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
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
public class ProducerGeneralConfigurationTest extends AbstractCamelIntegrationTest
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

   private static CamelContext camelContext;
   private static MockEndpoint resultEndpoint;

   @BeforeClass
   public static void beforeClass()
   {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
      resultEndpoint = camelContext.getEndpoint("mock:result", MockEndpoint.class);
   }

   @Test
   public void testBodyInDataMappingPrimitiveNonTransacted() throws Exception
   {
      String expectedBody = "Non Transacted Data";
      resultEndpoint.expectedBodiesReceived(expectedBody);
      sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testBodyInDataMappingPrimitiveNonTransacted", null, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }

   @Test
   public void testLegacyInMapping() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> expectedBody = new HashMap<String, Object>();
      expectedBody.put("firstName", "Manali");
      expectedBody.put("lastName", "Mungikar");
      dataMap.put("Person", expectedBody);

      resultEndpoint.expectedBodiesReceived(expectedBody);

      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testLegacyInMapping", dataMap, true);
      resultEndpoint.assertIsSatisfied();
      @SuppressWarnings("unchecked")
      Map<String, Object> resultDataMap = (Map<String, Object>) sf.getWorkflowService().getInDataPath(
            pInstance.getOID(), "Person");

      assertNotNull(resultDataMap);
      assertTrue(resultDataMap.get("firstName").equals(expectedBody.get("lastName")));
      assertTrue(resultDataMap.get("lastName").equals(expectedBody.get("firstName")));
      resultEndpoint.reset();
   }

   @Test
   public void testLegacyJmsRequest() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> addressMap = new HashMap<String, Object>();
      addressMap.put("addrLine1", "test1");
      addressMap.put("addrLine2", "test1");
      addressMap.put("zipCode", "test1");
      addressMap.put("city", "test1");
      Map<String, Object> personMap = new HashMap<String, Object>();
      personMap.put("firstName", "Manali");
      personMap.put("lastName", "Mungikar");
      personMap.put("address", addressMap);
      dataMap.put("Person", personMap);
      ProcessInstance pi=sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testLegacyJmsRequest", dataMap, true);
      assertNotNull(pi);
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.getFilter().add(new ProcessDefinitionFilter("testLegacyJmsResponseTrigger"));
      ProcessInstance pInstance = sf.getQueryService().findFirstProcessInstance(query);
      assertNotNull(pInstance);
      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "PersonJSON");
      assertNotNull(result);
      Object expectedJSON = "{\"lastName\":\"Mungikar\",\"address\":{\"addrLine1\":\"test1\",\"addrLine2\":\"test1\",\"zipCode\":\"test1\",\"city\":\"test1\"},\"firstName\":\"Manali\"}";
      assertTrue(result.equals(expectedJSON));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testLegacySpringInvocation() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> addressMap = new HashMap<String, Object>();
      addressMap.put("addrLine1", "test1");
      addressMap.put("addrLine2", "test1");
      addressMap.put("zipCode", "test1");
      addressMap.put("city", "test1");
      Map<String, Object> personMap = new HashMap<String, Object>();
      personMap.put("firstName", "Manali");
      personMap.put("lastName", "Mungikar");
      personMap.put("address", addressMap);
      dataMap.put("Person", personMap);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testLegacySpringInvocation", dataMap, true);
      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "Address");
      assertNotNull(result);
      assertTrue(result instanceof Map< ? , ? >);
      assertTrue(((Map<String,Object>) result).get("addrLine1").equals(addressMap.get("addrLine1")));
      assertTrue(((Map<String,Object>) result).get("addrLine2").equals(addressMap.get("addrLine2")));
      assertTrue(((Map<String,Object>) result).get("zipCode").equals(addressMap.get("zipCode")));
      assertTrue(((Map<String,Object>) result).get("city").equals(addressMap.get("city")));
   }

   @Test
   public void testRequestReponseMessage()
   {

      final long id = System.currentTimeMillis();
      RouteBuilder builder = new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from("direct:outbound").process(new Processor()
            {

               public void process(Exchange exchange) throws Exception
               {
                  StringBuffer address = new StringBuffer();
                  address.append("<Address>");
                  address.append("<addrLine1>addrLine1</addrLine1>");
                  address.append("<addrLine2>addrLine2</addrLine2>");
                  address.append("<zipCode>zipCode</zipCode>");
                  address.append("<city>city</city>");
                  address.append("</Address>");
                  exchange.getOut().setBody(address.toString());
                  Map<String, Object> dataMap = new HashMap<String, Object>();
                  dataMap.put("ID", new Long(id).toString());
                  exchange.getOut().setHeader("ippDataFiltersMap", dataMap);
               }
            }).setHeader(CamelConstants.MessageProperty.PROCESS_ID, constant("testRequestReponseMessage"))
                  .setHeader(CamelConstants.MessageProperty.ACTIVITY_ID, constant("testRequestReponseMessage")).to("direct:inbound");

         }
      };
      ProducerTemplate producer = new DefaultProducerTemplate(camelContext);
      try
      {
         builder.addRoutesToCamelContext(camelContext);
         producer.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      Map<String, Object> personMap = new HashMap<String, Object>();
      personMap.put("firstName", "Manali");
      personMap.put("lastName", "Mungikar");

      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("ID", new Long(id).toString());
      dataMap.put("Person", personMap);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testRequestReponseMessage", dataMap, true);
      assertNotNull(pInstance);
   }

   @Test
   public void testProcessCorrelationMatch()
   {
      final ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testProcessCorrelationMatch", null, true);
      assertNotNull(pInstance);
      RouteBuilder builder = new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from("seda:testProcessCorrelationMatch")
               .setHeader(CamelConstants.MessageProperty.PROCESS_INSTANCE_OID, constant(pInstance.getOID()))
               .to("direct:testProcessCorrelationMatch");
         }
      };
      ProducerTemplate producer = new DefaultProducerTemplate(camelContext);
      try
      {
         builder.addRoutesToCamelContext(camelContext);
         producer.start();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      producer.sendBody("seda:testProcessCorrelationMatch", "Hello World!");

   }

   @Test
   public void testSpringInvocationWithBpmTypeConverter()
   {
      Map<String, Object> personMap = new HashMap<String, Object>();
      personMap.put("firstName", "Manali");
      personMap.put("lastName", "Mungikar");
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("Person", personMap);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{GenericApplicationProducerTestModel}testSpringInvocationWithBpmTypeConverter",
            dataMap, true);
      assertNotNull(pInstance);
   }
}
