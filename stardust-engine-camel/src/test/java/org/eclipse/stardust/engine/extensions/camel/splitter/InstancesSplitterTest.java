package org.eclipse.stardust.engine.extensions.camel.splitter;

import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.PROCESS_ID_STRAIGHT_THROUGH;
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
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.splitter.InstancesSplitter;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InstancesSplitterTest
{

   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/splitter/InstancesSplitterTest-context.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
      try
      {
         camelContext.addRoutes(createFullRoute());
         testUtils.deployModel();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      splitProcessProducerTemplate = camelContext.createProducerTemplate();
      splitProcessProducerTemplate.setDefaultEndpointUri(SPLIT_PROCESS_ROUTE_BEGIN);
      fullRouteResult = camelContext.getEndpoint(SPLIT_PROCESS_ROUTE_END,
            MockEndpoint.class);
   }

   // URI constants
   private static final String SPLIT_PROCESS_ROUTE_BEGIN = "direct:startProcessSplitterTestRoute";

   private static final String SPLIT_PROCESS_ROUTE_END = "mock:endProcessSplitterTestRoute";

   private static CamelContext camelContext;

   private static SpringTestUtils testUtils;

   private static ServiceFactoryAccess serviceFactoryAccess;

   private static ProducerTemplate splitProcessProducerTemplate;

   private static MockEndpoint fullRouteResult;

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

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
