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
package org.eclipse.stardust.test.camel.splitter;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.camel.common.TestModelConstants.PROCESS_ID_STRAIGHT_THROUGH;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.splitter.InstancesSplitter;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
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
public class InstancesSplitterTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_ID = "CamelTestModel";

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   // URI constants
   private static final String SPLIT_PROCESS_ROUTE_BEGIN = "direct:startProcessSplitterTestRoute";

   private static final String SPLIT_PROCESS_ROUTE_END = "mock:endProcessSplitterTestRoute";

   private static ProducerTemplate splitProcessProducerTemplate;

   private static MockEndpoint fullRouteResult;

   @BeforeClass
   public static void setUpOnce() throws Exception
   {
      CamelContext camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);

      camelContext.addRoutes(createFullRoute());

      splitProcessProducerTemplate = camelContext.createProducerTemplate();
      splitProcessProducerTemplate.setDefaultEndpointUri(SPLIT_PROCESS_ROUTE_BEGIN);
      fullRouteResult = camelContext.getEndpoint(SPLIT_PROCESS_ROUTE_END, MockEndpoint.class);
   }

   @Test
   public void testSplitProcesses() throws Exception
   {
      Set<Long> piOids = new HashSet<Long>();
      ProcessInstances pis = null;

      // establish authentication for Camel endpoints
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      try
      {
         WorkflowService wfService = sf.getWorkflowService();
         QueryService qService = sf.getQueryService();
         // start a couple of processes
         piOids.add(wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, null, true)
               .getOID());
         piOids.add(wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, null, true)
               .getOID());
         piOids.add(wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, null, true)
               .getOID());

         ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
         piQuery.where(ProcessInstanceFilter.in(piOids));
         pis = qService.getAllProcessInstances(piQuery);
         assertEquals(3, pis.size());
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      splitProcessProducerTemplate.sendBodyAndHeader(SPLIT_PROCESS_ROUTE_BEGIN,
            CamelConstants.MessageProperty.PROCESS_INSTANCES, pis);

      fullRouteResult.setExpectedMessageCount(3);
      fullRouteResult.assertIsSatisfied();

      // examine exchanges
      Iterator<Exchange> exchangeIter;
      oidLoop: for (Long oid : piOids)
      {
         exchangeIter = fullRouteResult.getReceivedExchanges().iterator();
         while (exchangeIter.hasNext())
         {
            if (oid == exchangeIter
                  .next()
                  .getIn()
                  .getHeader(CamelConstants.MessageProperty.PROCESS_INSTANCE_OID,
                        Long.class))
            {
               continue oidLoop;
            }
         }
         Assert.fail("OID <" + oid + "> not found in received messages!");
      }
      fullRouteResult.reset();
   }

   @Test
   public void testSplitSingleprocess() throws Exception
   {

      Set<Long> piOids = new HashSet<Long>();
     Long piOid ;
      ProcessInstances pis = null;

      // establish authentication for Camel endpoints
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      try
      {
         WorkflowService wfService = sf.getWorkflowService();
         QueryService qService = sf.getQueryService();
         // start one process
         piOid = wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, null, true)
               .getOID();
         piOids.add(piOid);
         ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
         piQuery.where(ProcessInstanceFilter.in(piOids));
         pis = qService.getAllProcessInstances(piQuery);
         assertEquals(1, pis.size());
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      splitProcessProducerTemplate.sendBodyAndHeader(SPLIT_PROCESS_ROUTE_BEGIN,
            CamelConstants.MessageProperty.PROCESS_INSTANCES, pis);

      fullRouteResult.setExpectedMessageCount(1);
      fullRouteResult.assertIsSatisfied();

      // examine exchanges
      Iterator<Exchange> exchangeIter;
      oidLoop: for (Long oid : piOids)
      {
         exchangeIter = fullRouteResult.getReceivedExchanges().iterator();
         while (exchangeIter.hasNext())
         {
            if (oid == exchangeIter
                  .next()
                  .getIn()
                  .getHeader(CamelConstants.MessageProperty.PROCESS_INSTANCE_OID,
                        Long.class))
            {
               continue oidLoop;
            }
         }
         Assert.fail("OID <" + oid + "> not found in received messages!");
      }
      fullRouteResult.reset();
   }

   public static RouteBuilder createFullRoute()
   {
      return new RouteBuilder()
      {
         InstancesSplitter splitter = new InstancesSplitter();

         @Override
         public void configure() throws Exception
         {
            from(SPLIT_PROCESS_ROUTE_BEGIN).split()
                  .method(splitter, "splitProcessInstances").to(SPLIT_PROCESS_ROUTE_END);
         }
      };
   }
}
