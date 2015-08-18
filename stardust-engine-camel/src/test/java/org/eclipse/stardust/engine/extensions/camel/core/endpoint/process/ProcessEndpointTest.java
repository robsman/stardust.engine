package org.eclipse.stardust.engine.extensions.camel.core.endpoint.process;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_PROPERTIES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_REMOVE_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Process.COMMAND_CONTINUE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Process.COMMAND_GET_PROPERTIES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Process.COMMAND_SET_PROPERTIES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Process.COMMAND_START;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.ACTIVITY_ID_ERROR_HANDLING;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_ID_BOOLEAN;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_ID_INTEGER;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_ID_TEST_DATA;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_MAPPING_INVALID_INPUT;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_MAPPING_TEST_XML;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_PATH_BOOLEAN;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_PATH_ID;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_PATH_INTEGER;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_PATH_NAME;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_PATH_PERSON;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.DATA_PATH_TEST_DATA;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.PROCESS_ID_MESSAGE_TRANSFORMATION;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.PROCESS_ID_STRAIGHT_THROUGH;
import static org.eclipse.stardust.engine.extensions.camel.common.TestModelConstants.PROCESS_ID_WAITING_ACTIVITIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.common.CamelTestUtils;
import org.eclipse.stardust.engine.extensions.camel.common.SpecialTestException;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProcessEndpointTest
{
   private static final transient Logger LOG = LoggerFactory.getLogger(ProcessEndpointTest.class);
   // URI constants
   private static final String FULL_ROUTE_BEGIN = "direct:startProcessEndpointTestRoute";
   private static final String FULL_ROUTE_END = "mock:endProcessEndpointTestRoute";
   
   private static ClassPathXmlApplicationContext ctx;
   private static CamelContext camelContext;
   private static SpringTestUtils testUtils;
   private static ServiceFactoryAccess serviceFactoryAccess;

   //@Produce(uri = "direct:in")
   protected static ProducerTemplate defaultProducerTemplate;
   //@Produce(uri = FULL_ROUTE_BEGIN)
   protected static ProducerTemplate fullRouteProducerTemplate;
   //@EndpointInject(uri = FULL_ROUTE_END)
   protected static MockEndpoint fullRouteResult;

   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml", "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml","classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      try
      {
         camelContext.addRoutes(createFullRoute());
         testUtils.deployModel();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      defaultProducerTemplate=camelContext.createProducerTemplate();
      fullRouteProducerTemplate=camelContext.createProducerTemplate();
      fullRouteResult=camelContext.getEndpoint(FULL_ROUTE_END, MockEndpoint.class);
   }

   @Test
   public void testStartProcess() throws Exception
   {
      String uri;
      Map<String, Object> headerMap;
      Exchange exchange = new DefaultExchange(camelContext);

      // establish authentication for Camel endpoints
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      // Test 1 uses ippProcessId in header and keyValue data parameters
      headerMap = new HashMap<String, Object>();
      long kvId = 7783834l;
      String kvName = "Mueller";
      headerMap.put(PROCESS_ID, PROCESS_ID_STRAIGHT_THROUGH);

      uri = "ipp:process:start?data=" + DATA_ID_TEST_DATA + ".id::" + kvId + "::long," + DATA_ID_TEST_DATA + ".name::"
            + kvName;
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);

      // Test 2 uses multiple simple data refs in header and parameter process ID
      headerMap = new HashMap<String, Object>();
      Integer simpleInt = new Integer(7342);
      headerMap.put(DATA_ID_BOOLEAN, Boolean.TRUE);
      headerMap.put(DATA_ID_INTEGER, simpleInt);

//      uri = "ipp:process:start?processId=" + PROCESS_ID_WAITING_ACTIVITIES + "&data=${header." + DATA_ID_BOOLEAN + "},"
//            + "${header." + DATA_ID_INTEGER + "}";
//      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
//      Long piOid2 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);

      // Test 3 uses single complex data ref in header and parameter process ID
      headerMap = new HashMap<String, Object>();
      long sdtId = 83483l;
      String sdtName = "Smith";
      Map<String, Object> startData = new HashMap<String, Object>();
      startData.put(DATA_ID_TEST_DATA, createTestDataSDT(sdtId, sdtName));
      headerMap.put("testStartData", startData);

      uri = "ipp:process:start?processId=" + PROCESS_ID_STRAIGHT_THROUGH + "&dataMap=${header.testStartData}";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      Long piOid3 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);

      ClientEnvironment.removeCurrent();

      // Assertions
      assertNotNull(piOid1);
//      assertNotNull(piOid2);
      assertNotNull(piOid3);

      // check the created process instances
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         WorkflowService wfService = sf.getWorkflowService();

         // Verify test 1 result
         ProcessInstance pi = wfService.getProcessInstance(piOid1);
         assertNotNull(pi);
         assertEquals(kvId, ((Long) wfService.getInDataPath(piOid1, DATA_PATH_ID)).longValue());
         assertEquals(kvName, wfService.getInDataPath(piOid1, DATA_PATH_NAME));

         // Verify test 2 result
//         pi = wfService.getProcessInstance(piOid2);
//         assertNotNull(pi);
//         assertEquals(Boolean.TRUE, ((Boolean) wfService.getInDataPath(piOid2, DATA_PATH_BOOLEAN)));
//         assertEquals(simpleInt, ((Integer) wfService.getInDataPath(piOid2, DATA_PATH_INTEGER)));

         // Verify test 3 result
         pi = wfService.getProcessInstance(piOid3);
         assertNotNull(pi);
         assertEquals(sdtId, ((Long) wfService.getInDataPath(piOid3, DATA_PATH_ID)).longValue());
         assertEquals(sdtName, wfService.getInDataPath(piOid3, DATA_PATH_NAME));
      }
      finally
      {
         if (null != sf)
            sf.close();
      }
   }

   @Test
   public void testContinueProcess() throws Exception
   {
      String uri;
      Exchange exchange = new DefaultExchange(camelContext);
      Map<String, Object> headerMap;

      long resultId = 9999l;
      int resultSimpleId = 12345;
      String resultName = "NewName";
      Long piOid;

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         WorkflowService wfService = sf.getWorkflowService();
         ProcessInstance pi = wfService.startProcess(PROCESS_ID_WAITING_ACTIVITIES, null, true);
         piOid = pi.getOID();
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      // complete three activities with varying data
      headerMap = new HashMap<String, Object>();
      headerMap.put(PROCESS_INSTANCE_OID, piOid);
      uri = "ipp:process:" + COMMAND_CONTINUE;
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      uri = "ipp:process:" + COMMAND_CONTINUE + "?dataOutput=" + DATA_ID_TEST_DATA + ".id::" + resultId + "::long,"
            + DATA_ID_TEST_DATA + ".name::" + resultName + "&processInstanceOid=" + piOid;
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, null, null);
      uri = "ipp:process:" + COMMAND_CONTINUE + "?dataOutput=" + DATA_ID_INTEGER + "::" + resultSimpleId + "::int"
            + "&processInstanceOid=${body}";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, null, piOid);

      ClientEnvironment.removeCurrent();

      // check the states of the activities in the PI
      sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         // the process should have 3 completed activities
         ActivityInstanceQuery query = ActivityInstanceQuery.findCompleted();
         query.where(new ProcessInstanceFilter(piOid));
         assertEquals(3, sf.getQueryService().getActivityInstancesCount(query));
         // check updated data
         WorkflowService wfService = sf.getWorkflowService();
         assertEquals(resultId, ((Long) wfService.getInDataPath(piOid, DATA_PATH_ID)).longValue());
         assertEquals(resultName, wfService.getInDataPath(piOid, DATA_PATH_NAME));
         assertEquals(resultSimpleId, wfService.getInDataPath(piOid, DATA_PATH_INTEGER));
      }
      finally
      {
         if (null != sf)
            sf.close();
      }
   }

   /**
    * Test functionality of setProperties and setProperties sub commands.
    *
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   @Test
   public void testGetSetProperties() throws Exception
   {

      String uri;
      Message message;
      Exchange exchange = new DefaultExchange(camelContext);
      Map<String, Object> headerMap;

      // test data
      Integer newInt1 = new Integer(39959);
      Integer newInt2 = new Integer(998833);
      Integer newInt3 = new Integer(22332);
      Long testId1 = 3884322l;
      Long testId2 = 555553l;
      Long testId3 = 883322l;
      String testName1 = "Anderson";
      String testName2 = "Williams";
      Map<String, Serializable> testDataMap1 = createTestDataSDT(testId1, testName1);
      Map<String, Serializable> testDataMap2 = createTestDataSDT(testId2, testName2);

      // assemble initial message
      message = new DefaultMessage();
      headerMap = new HashMap<String, Object>();
      headerMap.put("newInteger", newInt1);
      message.setBody(testDataMap1);
      message.setHeaders(headerMap);
      exchange.setIn(message);

      // create a process
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);
      uri = "ipp:process:" + COMMAND_START + "?processId=" + PROCESS_ID_STRAIGHT_THROUGH;
      LOG.info("Testing endpoint URI: " + uri);
      exchange = defaultProducerTemplate.send(uri, exchange);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);

      // set data using different options drawing on existent data in message
      uri = "ipp:process:" + COMMAND_SET_PROPERTIES + "?" + "properties=" + DATA_PATH_INTEGER
            + "::${header.newInteger}," + "" + DATA_PATH_TEST_DATA + "::${body}," + DATA_PATH_BOOLEAN
            + "::true::boolean";
      LOG.info("Testing endpoint URI: " + uri);
      exchange = defaultProducerTemplate.send(uri, exchange);

      // verify intermediate results using workflow service directly
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      assertEquals(newInt1, wfService.getInDataPath(piOid1, DATA_PATH_INTEGER));
      assertEquals(Boolean.TRUE, wfService.getInDataPath(piOid1, DATA_PATH_BOOLEAN));
      assertEquals(testDataMap1, wfService.getInDataPath(piOid1, DATA_PATH_TEST_DATA));

      // change data again
      Map<String, Object> propertiesMap = new HashMap<String, Object>();
      propertiesMap.put(DATA_PATH_INTEGER, newInt2);
      propertiesMap.put(DATA_PATH_TEST_DATA, testDataMap2);
      exchange.getIn().setHeader("propertiesMap", propertiesMap);
      uri = "ipp:process:" + COMMAND_SET_PROPERTIES + "?propertiesMap=${header.propertiesMap}";
      LOG.info("Testing endpoint URI: " + uri);
      exchange = defaultProducerTemplate.send(uri, exchange);

      // verify results again using "get" command
      assertNull(exchange.getIn().getHeader(PROCESS_INSTANCE_PROPERTIES));
      uri = "ipp:process:" + COMMAND_GET_PROPERTIES + "?properties=" + DATA_PATH_TEST_DATA + "," + DATA_PATH_INTEGER;
      LOG.info("Testing endpoint URI: " + uri);
      exchange = defaultProducerTemplate.send(uri, exchange);
      Map<String, Serializable> propertiesResult = (Map<String, Serializable>) exchange.getIn().getHeader(
            PROCESS_INSTANCE_PROPERTIES);
      assertNotNull(propertiesResult);
      assertEquals(testDataMap2, propertiesResult.get(DATA_PATH_TEST_DATA));
      assertEquals(newInt2, propertiesResult.get(DATA_PATH_INTEGER));

      // change again using default header map
      Map<String, Object> newPropertiesMap = new HashMap<String, Object>();
      newPropertiesMap.put(DATA_PATH_INTEGER, newInt3);
      newPropertiesMap.put(DATA_PATH_ID, testId3);
      newPropertiesMap.put(DATA_PATH_BOOLEAN, Boolean.FALSE);
      exchange.getIn().setHeader(PROCESS_INSTANCE_PROPERTIES, newPropertiesMap);
      uri = "ipp:process:" + COMMAND_SET_PROPERTIES;
      LOG.info("Testing endpoint URI: " + uri);
      exchange = defaultProducerTemplate.send(uri, exchange);
      uri = "ipp:process:" + COMMAND_GET_PROPERTIES;
      LOG.info("Testing endpoint URI: " + uri);
      exchange = defaultProducerTemplate.send(uri, exchange);
      propertiesResult = (Map<String, Serializable>) exchange.getIn().getHeader(PROCESS_INSTANCE_PROPERTIES);
      // now the header should contain a union of all the changed properties of the
      // process
      assertEquals(testId3, ((Map) propertiesResult.get(DATA_PATH_TEST_DATA)).get("id")); // was
      // overwritten
      // with
      // ID
      // data
      // path
      assertEquals(testDataMap2.get("name"), ((Map) propertiesResult.get(DATA_PATH_TEST_DATA)).get("name"));
      assertEquals(testId3, propertiesResult.get(DATA_PATH_ID));
      assertEquals(testDataMap2.get("name"), propertiesResult.get(DATA_PATH_NAME));
      assertEquals(newInt3, propertiesResult.get(DATA_PATH_INTEGER));
      assertEquals(Boolean.FALSE, propertiesResult.get(DATA_PATH_BOOLEAN));

      ClientEnvironment.removeCurrent();
   }

   @Test
   public void testFindProcesses() throws Exception
   {
      String uri;
      Map<String, Object> headerMap = new HashMap<String,Object>();
      Exchange exchange = new DefaultExchange(camelContext);

      // establish authentication for Camel endpoints
      ClientEnvironment.setCurrent("motu", "motu", null, null, null);

      // start two processes with different data
      long piOid1, piOid2;
      long kvId = 45363l;
      String kvName = "Patterson";
      Map<String,Object> startData = new HashMap<String, Object>();
      startData.put(DATA_ID_TEST_DATA, createTestDataSDT(kvId, kvName));

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      QueryService qService = sf.getQueryService();
      try
      {
         // First process ends up hibernated
         ProcessInstance pi = wfService.startProcess(PROCESS_ID_WAITING_ACTIVITIES, null, true);
         piOid1 = pi.getOID();

         // The second process is straight-through and completed
         pi = wfService.startProcess(PROCESS_ID_STRAIGHT_THROUGH, startData, true);
         piOid2 = pi.getOID();
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      // Test 1 is a search to confirm the expected state of a known OID
      uri = "ipp:process:find?processInstanceOid="+piOid2 + "&state=completed";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      ProcessInstances pi = exchange.getIn().getHeader(PROCESS_INSTANCES, ProcessInstances.class);
      assertNotNull(pi);
      assertTrue(pi.size()==1);

      // Test 2 searches for hibernated processes by ID
      uri = "ipp:process:find?processId="+PROCESS_ID_WAITING_ACTIVITIES + "&state=active&expectedResultSize=1";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      ProcessInstance singleProcessInstance =exchange.getIn().getHeader(PROCESS_INSTANCES, ProcessInstance.class);
      assertNotNull(singleProcessInstance);

       wfService.startProcess(PROCESS_ID_WAITING_ACTIVITIES, null, true);
      // Test 2 searches for hibernated processes by ID
      uri = "ipp:process:find?processId="+PROCESS_ID_WAITING_ACTIVITIES + "&state=active&expectedResultSize=2";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      ProcessInstances pis = exchange.getIn().getHeader(PROCESS_INSTANCES, ProcessInstances.class);
      assertTrue(pis.size()==2);

      // Test 3 searches for processes with a data filter
      uri = "ipp:process:find?dataFilters=" + DATA_ID_TEST_DATA + ".id::" + kvId + "::long," + DATA_ID_TEST_DATA + ".name::" + kvName;
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      ProcessInstances result = exchange.getIn().getHeader(PROCESS_INSTANCES, ProcessInstances.class);
      assertNotNull(result);
      assertTrue(!result.isEmpty());
      if(result.size()==1)
      assertEquals(piOid2, ((ProcessInstance)result.get(0)).getOID());

      // Test 4 repeats test 3, but using a map from the message header
      headerMap = new HashMap<String,Object>();
      Map<String,Serializable> filters = new HashMap<String,Serializable>();
      filters.put(DATA_ID_TEST_DATA+".id", kvId);
      filters.put(DATA_ID_TEST_DATA+".name", kvName);
      headerMap.put("processDataFilters", filters);
      uri = "ipp:process:find?dataFiltersMap=$simple{header.processDataFilters}";
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      result = exchange.getIn().getHeader(PROCESS_INSTANCES, ProcessInstances.class);
      assertNotNull(result);
      assertTrue(!result.isEmpty());
      if(result.size()==1)
         assertEquals(piOid2, ((ProcessInstance)result.get(0)).getOID());

      ClientEnvironment.removeCurrent();
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testFullRoute() throws Exception
   {
      Integer id = new Integer(234234);
      String firstname = "Arnold";
      String lastname = "Schwarzenegger";

      // send valid input
      fullRouteProducerTemplate.sendBody(FULL_ROUTE_BEGIN,id + "," + firstname + "," + lastname);
      // send corrupt input
      fullRouteProducerTemplate.sendBody(FULL_ROUTE_BEGIN,id + "ttt" + firstname + ":" + lastname);

      fullRouteResult.setExpectedMessageCount(2);
      fullRouteResult.assertIsSatisfied();

      // examine successful exchange
      Exchange exchange1 = fullRouteResult.getReceivedExchanges().get(0);
      Map<String, Serializable> person = (Map<String, Serializable>) exchange1.getIn().getHeader(
            PROCESS_INSTANCE_PROPERTIES, Map.class).get(DATA_PATH_PERSON);
      assertNotNull(person);
      assertEquals(id, person.get("ID"));
      assertEquals(firstname, person.get("Firstname"));
      assertEquals(lastname, person.get("Lastname"));

      // examine invalid exchange
      Exchange exchange2 = fullRouteResult.getReceivedExchanges().get(1);
      Object ex = exchange2.getProperty("CamelExceptionCaught");
      assertNotNull(ex);
      assertTrue(ex instanceof SpecialTestException);

      // examine IPP process instances
      Long piOid1 = exchange1.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      Long piOid2 = exchange2.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      try
      {
         WorkflowService wfService = sf.getWorkflowService();
         ProcessInstance pi1 = wfService.getProcessInstance(piOid1);
         ProcessInstance pi2 = wfService.getProcessInstance(piOid2);
         assertEquals(ProcessInstanceState.Completed, pi1.getState());
         assertEquals(ProcessInstanceState.Active, pi2.getState());

         QueryService qService = sf.getQueryService();
         ActivityInstanceQuery query = ActivityInstanceQuery.findPending();
         query.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOid2));
         ActivityInstances result = qService.getAllActivityInstances(query);
         assertEquals(1, result.size());
         assertEquals(ActivityInstanceState.Suspended, result.get(0).getState());
         assertEquals(ACTIVITY_ID_ERROR_HANDLING, result.get(0).getActivity().getId());
      }
      finally
      {
         if (null != sf)
            sf.close();
      }
   }

//   @Before
//   public void setUp() throws Exception
//   {
//      if (!initiated)
//         setUpGlobal();
//   }
//
//   public void setUpGlobal() throws Exception
//   {
//      // initiate environment
//      testUtils.setUpGlobal();
//      initiated = true;
//   }

   public static RouteBuilder createFullRoute()
   {
      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from(FULL_ROUTE_BEGIN).onException(SpecialTestException.class).to(
                  "ipp:process:" + COMMAND_CONTINUE + "?dataOutput=" + DATA_MAPPING_INVALID_INPUT + "::${body}")
                  .handled(true).log("Handled invalid input: ${body}").to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT)
                  .to(FULL_ROUTE_END).end().to(
                        "ipp:authenticate:" + COMMAND_SET_CURRENT + "?user=motu&password=motu").to(
                        "ipp:process:" + COMMAND_START + "?processId=" + PROCESS_ID_MESSAGE_TRANSFORMATION).log(
                        "Created process instance OID: ${header." + PROCESS_INSTANCE_OID + "}").process(new Processor()
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
                           xml.append("<name>" + elements[1] + " " + elements[2] + "</name>");
                           xml.append("</TestData>");
                           exchange.getIn().setBody(xml.toString());
                        }
                        catch (Exception e)
                        {
                           throw new SpecialTestException("Invalid content: " + body, e);
                        }
                     }
                  }).to("ipp:process:" + COMMAND_CONTINUE + "?dataOutput=" + DATA_MAPPING_TEST_XML + "::${body}")
                  .delay(5000).to("ipp:process:" + COMMAND_GET_PROPERTIES + "?properties=" + DATA_PATH_PERSON).log(
                        "IPP processing result: ${header." + PROCESS_INSTANCE_PROPERTIES + "[Person]}").to(
                        "ipp:authenticate:" + COMMAND_REMOVE_CURRENT).to(FULL_ROUTE_END);
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
