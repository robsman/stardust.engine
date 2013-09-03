package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.TestModelConstants.PROCESS_ID_STRAIGHT_THROUGH;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.splitter.InstancesSplitter;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration(locations = {
      "InstancesSplitterTest-context.xml", "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml"})
public class InstancesSplitterTest extends AbstractJUnit4SpringContextTests
{

   private static final transient Logger LOG = LoggerFactory.getLogger(InstancesSplitterTest.class);
   // URI constants
   private static final String SPLIT_PROCESS_ROUTE_BEGIN = "direct:startProcessSplitterTestRoute";
   private static final String SPLIT_PROCESS_ROUTE_END = "mock:endProcessSplitterTestRoute";

   @Resource
   CamelContext camelContext;
   @Resource
   private SpringTestUtils testUtils;
   /**
    * Use this service factory access for testing assumptions!
    */
   @Resource
   private ServiceFactoryAccess serviceFactoryAccess;

   @Produce(uri = "direct:in")
   protected ProducerTemplate defaultProducerTemplate;
   @Produce(uri = SPLIT_PROCESS_ROUTE_BEGIN)
   protected ProducerTemplate splitProcessProducerTemplate;
   @EndpointInject(uri = SPLIT_PROCESS_ROUTE_END)
   protected MockEndpoint fullRouteResult;

   private boolean initiated = false;

   @Test
   public void testSplitProcesses() throws Exception
   {
      Set<Long> piOids = new HashSet<Long>();
      ProcessInstances pis = null;

      // establish authentication for Camel endpoints
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         WorkflowService wfService = sf.getWorkflowService();
         QueryService qService = sf.getQueryService();
         // start a couple of processes
         piOids.add(wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, null, true).getOID());
         piOids.add(wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, null, true).getOID());
         piOids.add(wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, null, true).getOID());
         
         ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
         piQuery.where( ProcessInstanceFilter.in(piOids));
         pis = qService.getAllProcessInstances(piQuery);
         assertEquals(3, pis.size());
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      splitProcessProducerTemplate.sendBodyAndHeader(null, CamelConstants.MessageProperty.PROCESS_INSTANCES, pis);

      fullRouteResult.setExpectedMessageCount(3);
      fullRouteResult.assertIsSatisfied();

      // examine exchanges
      Iterator<Exchange> exchangeIter;
      oidLoop:
      for( Long oid : piOids )
      {
         exchangeIter = fullRouteResult.getReceivedExchanges().iterator();
         while( exchangeIter.hasNext() )
         {
            if( oid == exchangeIter.next().getIn().getHeader(CamelConstants.MessageProperty.PROCESS_INSTANCE_OID, Long.class))
            {
               continue oidLoop;
            }
         }
         Assert.fail("OID <"+oid+"> not found in received messages!");
      }
    }

   @Before
   public void setUp() throws Exception
   {
      if (!initiated)
         setUpGlobal();
   }

   public void setUpGlobal() throws Exception
   {
      // initiate environment
      testUtils.setUpGlobal();
      initiated = true;
   }
   
   @After
   public void tearDown() throws Exception {
      testUtils.tearDown();
   }

   public static RouteBuilder createFullRoute()
   {
      return new RouteBuilder()
      {
         InstancesSplitter splitter = new InstancesSplitter();
         @Override
         public void configure() throws Exception
         {
            from(SPLIT_PROCESS_ROUTE_BEGIN)
            .split().method(splitter, "splitProcessInstances")
            .to(SPLIT_PROCESS_ROUTE_END);
         }
      };
   }

}
