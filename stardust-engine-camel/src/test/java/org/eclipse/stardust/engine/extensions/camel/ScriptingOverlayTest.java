package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class ScriptingOverlayTest
{
   // private static final transient Logger LOG =
   // LoggerFactory.getLogger(ProcessEndpointTest.class);
   // URI constants
   private static final String FULL_ROUTE_BEGIN = "direct:startProcessEndpointTestRoute";
   private static final String FULL_ROUTE_END = "mock:endProcessEndpointTestRoute";

   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/ScriptingOverlayTest-context.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      try
      {
         ClassPathResource resource = new ClassPathResource("models/JavaScriptOverlayTypeTestModel.xpdl");
         testUtils.setModelFile(resource);
         testUtils.deployModel();
         camelContext.addRoutes(createFullRoute());
         testUtils.deployModel();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      defaultProducerTemplate = camelContext.createProducerTemplate();
      fullRouteProducerTemplate = camelContext.createProducerTemplate();
      fullRouteResult = camelContext.getEndpoint(FULL_ROUTE_END, MockEndpoint.class);
   }

   private static CamelContext camelContext;
   private static SpringTestUtils testUtils;
   private static ServiceFactoryAccess serviceFactoryAccess;

   protected static ProducerTemplate defaultProducerTemplate;
   protected ProducerTemplate fullRouteProducerTemplate;
   protected static MockEndpoint fullRouteResult;

   @SuppressWarnings("unchecked")
   @Test
   public void testFullRoute() throws Exception
   {
      Map<String, Object> sdt = new HashMap<String, Object>();
      String base = "created";
      Date createdDt = KeyValueList.getDateFormat().parse("07.11.2013");
      sdt.put(base, createdDt);
      sdt.put("aText", "Hello JS OverlayType");
      Date aDate = KeyValueList.getDateFormat().parse("07.11.2013");
      sdt.put("aDate", aDate);
      sdt.put("aDateTime", aDate.getTime());
      sdt.put("aBoolean", true);
      sdt.put("aLong", new Long("123456"));
      sdt.put("anInteger", new Integer("1212"));
      sdt.put("aDouble", new Double("12.23"));
      List<Date> aDateList = new ArrayList<Date>();
      Date aDate1 = KeyValueList.getDateFormat().parse("05.11.2013");
      Date aDate2 = KeyValueList.getDateFormat().parse("06.11.2013");
      aDateList.add(aDate1);
      aDateList.add(aDate2);
      sdt.put("aDateList", aDateList);
      List<Long> aDateTimeList = new ArrayList<Long>();
      Date aDateTime1 = KeyValueList.getDateFormat().parse("05.11.2013");
      Date aDateTime2 = KeyValueList.getDateFormat().parse("06.11.2013");
      aDateTimeList.add(aDateTime1.getTime());
      aDateTimeList.add(aDateTime2.getTime());
      sdt.put("aDateTimeList", aDateTimeList);

      Map<String, Object> s2 = new HashMap<String, Object>();
      s2.put("aString", "hello s2");
      s2.put("aBoolean", true);
      s2.put("aLong", new Long("123456"));
      s2.put("anInteger", new Integer("1212"));
      s2.put("aDouble", new Double("12.23"));
      s2.put("aDate", KeyValueList.getDateFormat().parse("07.11.2013"));
      s2.put("aDateTime", KeyValueList.getDateFormat().parse("07.11.2013").getTime());

      Map<String, Object> s3 = new HashMap<String, Object>();
      s3.put("aString", "hello s3");
      s3.put("aBoolean", true);
      s3.put("aLong", new Long("123456"));
      s3.put("anInteger", new Integer("1212"));
      s3.put("aDouble", new Double("12.23"));
      s3.put("aDate", KeyValueList.getDateFormat().parse("07.11.2013"));
      s3.put("aDateTime", KeyValueList.getDateFormat().parse("07.11.2013").getTime());

      // add s2, s3 to SDT
      sdt.put("S2", s2);
      sdt.put("S3", s3);
      List<Map<String, Object>> s2s = new ArrayList<Map<String, Object>>();
      s2s.add(s2);
      s2s.add(s2);
      s2s.add(s2);
      s2s.add(s2);
      s2s.add(s2);
      sdt.put("S2s", s2s);

      // Map<String,Object> s1 = new HashMap<String,Object>();
      // s1.put("S1",sdt);

      // send valid input
      fullRouteProducerTemplate.sendBody(FULL_ROUTE_BEGIN, sdt);

      Exchange exchange1 = fullRouteResult.getReceivedExchanges().get(0);
      assertNotNull(exchange1);
      Long piOid = exchange1.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance pi = wfService.getProcessInstance(piOid);
      assertEquals(ProcessInstanceState.Completed, pi.getState());

      Map<String, Object> reponse = (Map<String, Object>) wfService.getInDataPath(pi.getOID(), "S1");
      assertNotNull(reponse);
      assertEquals(reponse.get("aText"), "Hello JS OverlayType");
      assertNotNull(reponse.get("aDate"));
      assertTrue(reponse.get("aDate") instanceof Date);
      assertNotNull(reponse.get("aDateTime"));
      assertTrue(reponse.get("aDateTime") instanceof Date);
      assertNotNull(reponse.get("aDateList"));
      assertNotNull(reponse.get("aDateList") instanceof List);
      for (Object elt : (List<Map<String,Object>>) reponse.get("aDateList"))
         assertTrue(elt instanceof Date);

      assertNotNull(((Map<String, Object>) reponse.get("S2")).get("aDate"));
      assertTrue(((Map<String, Object>) reponse.get("S2")).get("aDate") instanceof Date);
      assertNotNull(((Map<String, Object>) reponse.get("S2")).get("aDateTime"));
      assertTrue(((Map<String, Object>) reponse.get("S2")).get("aDateTime") instanceof Date);
      // S2s
      assertNotNull(reponse.get("S2s"));
      assertTrue(reponse.get("S2s") instanceof List);
      for (Map<String, Object> elt : (List<Map<String, Object>>) reponse.get("S2s"))
      {
         assertNotNull(elt.get("aDate"));
         assertTrue(elt.get("aDate") instanceof Date);
         assertNotNull(elt.get("aDateTime"));
         assertTrue(elt.get("aDateTime") instanceof Date);
      }
   }

   public static RouteBuilder createFullRoute()
   {
      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from(FULL_ROUTE_BEGIN).to("ipp:authenticate:setCurrent?user=motu&password=motu")
                  .to("ipp:process:start?modelId=Model12&processId=ScriptingExample&data=S1::$simple{body}")
                  .to(FULL_ROUTE_END);
         }
      };
   }
}
