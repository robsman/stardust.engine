package org.eclipse.stardust.engine.extensions.camel.core.endpoint.activity;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ACTIVITY_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_COMPLETE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Activity.COMMAND_FIND;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_REMOVE_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.camel.common.CamelTestUtils;
import org.eclipse.stardust.engine.extensions.camel.common.SpecialTestException;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.BpmAssert;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

public class ActivityEndpointTest
{
   private static ClassPathXmlApplicationContext ctx;
   // URI constants
   private static final String FULL_ROUTE_BEGIN = "direct:startActivityEndpointTestRoute";
   private static final String FULL_ROUTE_END = "mock:endActivityEndpointTestRoute";
   private static CamelContext defaultCamelContext;
   private static SpringTestUtils testUtils;
   private static ServiceFactoryAccess serviceFactoryAccess;

   @Produce(uri = "direct:in")
   protected ProducerTemplate defaultProducerTemplate;
   
   @BeforeClass
   public static void beforeClass() throws IOException
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml",
                  "classpath:default-camel-context.xml"});
      defaultCamelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
      try
      {
         defaultCamelContext.addRoutes(createFullRoute());
         testUtils.deployModel();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }

   @Test
   public void testFindActivities() throws Exception
   {
      String uri;
      Map<String, Object> headerMap = new HashMap<String, Object>();
      Exchange exchange = new DefaultExchange(defaultCamelContext);

      // establish authentication for Camel endpoints
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      // start two processes with different data and different waiting activities
      long piOid1, piOid2;
      long kvId = 838742l;
      String kvName = "Stevens";
      Map<String, Object> startData = new HashMap<String, Object>();
      startData.put(DATA_ID_TEST_DATA, createTestDataSDT(kvId, kvName));

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         WorkflowService wfService = sf.getWorkflowService();
         QueryService qService = sf.getQueryService();

         // The first process will be hibernated in activity 2 with no data
         ProcessInstance pi = wfService.startProcess(PROCESS_ID_WAITING_ACTIVITIES, null,
               true);
         piOid1 = pi.getOID();
         ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(piOid1,
               ACTIVITY_ID_WAITING_ACTIVITY_1);
         ActivityInstance ai = qService.findFirstActivityInstance(aiQuery);
         wfService.activateAndComplete(ai.getOID(), null, null);

         // The second process will be hibernated in activity 1 with data
         pi = wfService.startProcess(PROCESS_ID_WAITING_ACTIVITIES, startData, true);
         piOid2 = pi.getOID();
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      // Test 1 searches for hibernated activities in the whole process
      uri = "ipp:activity:find?processId=" + PROCESS_ID_WAITING_ACTIVITIES
            + "&state=hibernated";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      ActivityInstances result = exchange.getIn().getHeader(ACTIVITY_INSTANCES,
            ActivityInstances.class);
      assertTrue(result.size() > 0);

      // Test 2 searches for hibernated activities by activity ID
      uri = "ipp:activity:find?processId=" + PROCESS_ID_WAITING_ACTIVITIES
            + "&activityId=" + ACTIVITY_ID_WAITING_ACTIVITY_2 + "&state=hibernated";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      result = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class);
      assertTrue(result.size() > 0);
      // assertEquals(piOid1, result.get(0).getProcessInstanceOID());

      // Test 3 searches for activities with a data filter
      uri = "ipp:activity:find?processId=" + PROCESS_ID_WAITING_ACTIVITIES
            + "&activityId=" + ACTIVITY_ID_WAITING_ACTIVITY_1 + "&dataFilters="
            + DATA_ID_TEST_DATA + ".id::" + kvId + "::long," + DATA_ID_TEST_DATA
            + ".name::" + kvName;
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      result = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class);
      assertTrue(result.size() > 0);
      // assertEquals(piOid2, result.get(0).getProcessInstanceOID());

      // Test 4 repeats test 3, but using a map from the message header
      headerMap = new HashMap<String, Object>();
      Map<String, Serializable> filters = new HashMap<String, Serializable>();
      filters.put(DATA_ID_TEST_DATA + ".id", kvId);
      filters.put(DATA_ID_TEST_DATA + ".name", kvName);
      headerMap.put("activityFilters", filters);
      uri = "ipp:activity:find?processId=" + PROCESS_ID_WAITING_ACTIVITIES
            + "&activityId=" + ACTIVITY_ID_WAITING_ACTIVITY_1
            + "&dataFiltersMap=$simple{header.activityFilters}";
     
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
		
      result = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class);
      assertTrue(result.size() > 0);
      int resultSize= result.size();
      
      // Test 5 repeats test 4, but expectedResultSize is throwing an exception
      String expectedExceptionMessage=resultSize+" activity instances found - 1000 activity instances expected.";	
      headerMap = new HashMap<String, Object>();
      Map<String, Serializable> filtersMap = new HashMap<String, Serializable>();
      filtersMap.put(DATA_ID_TEST_DATA + ".id", kvId);
      filtersMap.put(DATA_ID_TEST_DATA + ".name", kvName);
      headerMap.put("activityFilters", filtersMap);
      uri = "ipp:activity:find?processId=" + PROCESS_ID_WAITING_ACTIVITIES
            + "&activityId=" + ACTIVITY_ID_WAITING_ACTIVITY_1
            + "&dataFiltersMap=$simple{header.activityFilters}"
            + "&expectedResultSize=1000";
     
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      String exceptionMessage= exchange.getException().getMessage();
      
      result = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class);
      assertEquals(expectedExceptionMessage, exceptionMessage);
      
      // Test 6 repeats test 5, but expectedResultSize has a good value 
      headerMap = new HashMap<String, Object>();
      headerMap.put("activityFilters", filtersMap);
      uri = "ipp:activity:find?processId=" + PROCESS_ID_WAITING_ACTIVITIES
            + "&activityId=" + ACTIVITY_ID_WAITING_ACTIVITY_1
            + "&dataFiltersMap=$simple{header.activityFilters}"
            + "&expectedResultSize="+resultSize;
      exchange = new DefaultExchange(defaultCamelContext);
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      
      // expectedResultSize == 1
      if (exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstanceDetails.class) != null){
    	  ActivityInstanceDetails activityInstanceDetails = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstanceDetails.class);
    	  long activityInstanceOID = exchange.getIn().getHeader(ACTIVITY_INSTANCE_OID, Long.class);
    	  assertTrue(activityInstanceDetails != null);
    	  assertTrue(activityInstanceOID>0);
      }
      
      // Test 7 searches for activities with expectedResultSize > 1
      uri = "ipp:activity:find?processId=" + PROCESS_ID_WAITING_ACTIVITIES
      + "&state=hibernated&expectedResultSize=2";
      exchange = new DefaultExchange(defaultCamelContext);
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      if (exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class) != null){
    	  result = exchange.getIn().getHeader(ACTIVITY_INSTANCES, ActivityInstances.class);
    	  assertTrue(result.size() > 0);
      }

      ClientEnvironment.removeCurrent();
   }

   @Test
   public void testCompleteActivities() throws Exception
   {
      String uri;
      Map<String, Object> headerMap = new HashMap<String, Object>();
      Exchange exchange = new DefaultExchange(defaultCamelContext);

      // establish authentication for Camel endpoints
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      long piOid1, aiOid1, aiOid2;

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      QueryService qService = sf.getQueryService();

      // start a process instance to test with
      ProcessInstance pi = wfService.startProcess(PROCESS_ID_WAITING_ACTIVITIES, null,
            true);
      piOid1 = pi.getOID();

      // Test 1 completes the first activity of process 1 by OID
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery
            .findForProcessInstance(piOid1);
      aiQuery.where(ActivityFilter.forAnyProcess(ACTIVITY_ID_WAITING_ACTIVITY_1));
      aiOid1 = qService.findFirstActivityInstance(aiQuery).getOID();

      uri = "ipp:activity:complete?activityInstanceOid=" + aiOid1;
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      assertTrue(BpmAssert.activityCompleted(piOid1, ACTIVITY_ID_WAITING_ACTIVITY_1));

      // Test 2 completes the second activity of process 1 by OID in the header and sets
      // out data from header
      aiQuery = ActivityInstanceQuery.findForProcessInstance(piOid1);
      aiQuery.where(ActivityFilter.forAnyProcess(ACTIVITY_ID_WAITING_ACTIVITY_2));
      aiOid2 = qService.findFirstActivityInstance(aiQuery).getOID();
      long kvId = 6375883;
      String kvName = "Peterson";
      Map<String, Serializable> testData = createTestDataSDT(kvId, kvName);
      headerMap.clear();
      headerMap.put("TestData", testData);
      headerMap.put(ACTIVITY_INSTANCE_OID, aiOid2);

      uri = "ipp:activity:complete?dataOutput=$simple{header.TestData}";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      assertTrue(BpmAssert.activityCompleted(piOid1, ACTIVITY_ID_WAITING_ACTIVITY_2));
      Long valueId = (Long) wfService.getInDataPath(piOid1, DATA_PATH_ID);
      String valueName = (String) wfService.getInDataPath(piOid1, DATA_PATH_NAME);
      assertEquals(kvId, valueId.longValue());
      assertEquals(kvName, valueName);

      // Test 3 completes the third activity of process 1 by activity name and
      // data filter and sets out data in URI
      int outInteger = 735;
      headerMap.clear();
      uri = "ipp:activity:complete?activityId=" + ACTIVITY_ID_WAITING_ACTIVITY_3
            + "&processId=" + PROCESS_ID_WAITING_ACTIVITIES
            + "&dataFilters=TestData.id::" + kvId + "::long"
            + "&dataOutput=SimpleInteger::" + outInteger + "::int";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      assertTrue(BpmAssert.activityCompleted(piOid1, ACTIVITY_ID_WAITING_ACTIVITY_2));
      Integer processInteger = (Integer) wfService.getInDataPath(piOid1,
            DATA_PATH_INTEGER);
      // assertEquals( outInteger, processInteger.intValue() );
   }

   @Test
   public void testFullRoute() throws Exception
   {
      Integer id = new Integer(2322244);
      String firstname = "Hank";
      String lastname = "Hawthorne";

      // start a process as target for the route
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance pi = wfService.startProcess(PROCESS_ID_MESSAGE_TRANSFORMATION,
            null, true);

      // send corrupt input
      ProducerTemplate fullRouteProducerTemplate = defaultCamelContext
            .createProducerTemplate();
      fullRouteProducerTemplate.sendBodyAndHeader(FULL_ROUTE_BEGIN, id + "," + firstname
            + "," + lastname, "myPiOid", pi.getOID());
      MockEndpoint fullRouteResult = defaultCamelContext.getEndpoint(FULL_ROUTE_END,
            MockEndpoint.class);

      fullRouteResult.setExpectedMessageCount(1);
      fullRouteResult.assertIsSatisfied();

      // examine exchange
      Exchange exchange1 = fullRouteResult.getReceivedExchanges().get(0);
      ActivityInstances processedActivities = exchange1.getIn().getHeader(
            ACTIVITY_INSTANCES, ActivityInstances.class);
      assertTrue(processedActivities.size() > 0);

      // examine IPP process and activity instances
      QueryService qService = sf.getQueryService();
      assertNotNull(qService);
      ProcessInstance pi1 = wfService.getProcessInstance(pi.getOID());
      assertEquals(ProcessInstanceState.Completed, pi1.getState());

      for (ActivityInstance ai : processedActivities)
      {
         assertEquals(ActivityInstanceState.Completed, ai.getState());
         // make sure the found AIs belong to the process instance
         assertEquals(ai.getProcessInstanceOID(), pi1.getOID());
      }
   }

   public static RouteBuilder createFullRoute()
   {
      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from(FULL_ROUTE_BEGIN)
                  .to("ipp:authenticate:" + COMMAND_SET_CURRENT
                        + "?user=motu&password=motu")
                  .process(new Processor()
                  {
                     public void process(Exchange exchange) throws Exception
                     {
                        String body = null;
                        try
                        {
                           body = exchange.getIn().getBody(String.class);
                           String[] elements = body.split(",");
                           StringBuilder xml = new StringBuilder("<TestData>");
                           xml.append("<id>" + elements[0] + "</id>");
                           xml.append("<name>" + elements[1] + " " + elements[2]
                                 + "</name>");
                           xml.append("</TestData>");
                           exchange.getIn().setBody(xml.toString());
                        }
                        catch (Exception e)
                        {
                           throw new SpecialTestException("Invalid content: " + body, e);
                        }
                     }
                  })
                  .to("ipp:activity:" + COMMAND_COMPLETE
                        + "?state=hibernated&activityId=" + ACTIVITY_ID_WAIT_FOR_DATA
                        + "&dataOutput=TestXML::$simple{body}")
                  .process(new Processor()
                  {
                     public void process(Exchange exchange) throws Exception
                     {
                        System.out.println(exchange);
                     }
                  })
                  .delay(1000)
                  // delay a little bit to make sure the process completes and we can
                  // collect all activities
                  .to("ipp:activity:" + COMMAND_FIND
                        + "?processInstanceOid=$simple{header.myPiOid}"
                        + "&state=completed").process(new Processor()
                  {
                     public void process(Exchange exchange) throws Exception
                     {
                        System.out.println(exchange);
                     }
                  }).to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT).to(FULL_ROUTE_END);
         }
      };
   }

   private static Map<String, Serializable> createTestDataSDT(long id, String name)
   {
      Map<String, Serializable> testDataObject = new HashMap<String, Serializable>(2);
      testDataObject.put("id", id);
      testDataObject.put("name", name);
      return testDataObject;
   }

}
