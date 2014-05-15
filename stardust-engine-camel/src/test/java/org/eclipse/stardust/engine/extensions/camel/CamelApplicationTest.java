package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
@Ignore
public class CamelApplicationTest 
{
   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/CamelApplicationTest-context.xml", "classpath:carnot-spring-context.xml",
      "classpath:jackrabbit-jcr-context.xml","classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      

      try
      {
         ClassPathResource resource = new ClassPathResource("models/CamelApplicationTestModel.xpdl");
         testUtils.setModelFile(resource);
         testUtils.deployModel();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      resultEndpoint =camelContext.getEndpoint("mock:result", MockEndpoint.class);
   }
   private static CamelContext camelContext;
   private static SpringTestUtils testUtils;
   private static ServiceFactoryAccess serviceFactoryAccess;
   protected static MockEndpoint resultEndpoint;


   @SuppressWarnings("unchecked")
   @Test
   public void testLegacySpringInvocation() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

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

      ProcessInstance pInstance = sf.getWorkflowService().startProcess("testLegacySpringInvocation", dataMap, true);

      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "Address");

      assertNotNull(result);
      assertTrue(result instanceof Map< ? , ? >);
      assertTrue(((Map<String,Object>) result).get("addrLine1").equals(addressMap.get("addrLine1")));
      assertTrue(((Map<String,Object>) result).get("addrLine2").equals(addressMap.get("addrLine2")));
      assertTrue(((Map<String,Object>) result).get("zipCode").equals(addressMap.get("zipCode")));
      assertTrue(((Map<String,Object>) result).get("city").equals(addressMap.get("city")));

   }

   @Test
   public void testLegacyJmsRequest() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      
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

      sf.getWorkflowService().startProcess("testLegacyJmsRequest", dataMap, true);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.getFilter().add(new ProcessDefinitionFilter("testLegacyJmsResponseTrigger"));
      ProcessInstance pInstance = sf.getQueryService().findFirstProcessInstance(query);

      assertNotNull(pInstance);

      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "PersonJSON");

      assertNotNull(result);

      Object expectedJSON = "{\"lastName\":\"Mungikar\",\"address\":{\"addrLine1\":\"test1\",\"addrLine2\":\"test1\",\"zipCode\":\"test1\",\"city\":\"test1\"},\"firstName\":\"Manali\"}";

      assertTrue(result.equals(expectedJSON));

   }

   @Test
   public void testBodyInDataMappingPrimitive() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      String expectedBody = "ResultString";

      resultEndpoint.expectedBodiesReceived(expectedBody);

      sf.getWorkflowService().startProcess("testBodyInDataMappingPrimitive", null, true);

      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }
   
   @Test
   public void testBodyInDataMappingPrimitiveNonTransacted() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      String expectedBody = "Non Transacted Data";
      resultEndpoint.expectedBodiesReceived(expectedBody);
      sf.getWorkflowService().startProcess("testBodyInDataMappingPrimitiveNonTransacted", null, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }
   
   @Test
   public void testBodyInDataMappingSDT() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> expectedBody = new HashMap<String, Object>();
      expectedBody.put("firstName", "Manali");
      expectedBody.put("lastName", "Mungikar");
      dataMap.put("Person", expectedBody);

      resultEndpoint.expectedBodiesReceived(expectedBody);
      sf.getWorkflowService().startProcess("testBodyInDataMappingSDT", dataMap, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }

   @Test
   public void testHeaderInDataMappingsPrimitive() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      resultEndpoint.expectedHeaderReceived("input1", "header1");
      resultEndpoint.expectedHeaderReceived("input2", "header2");
      resultEndpoint.expectedHeaderReceived("input3", "header3");

      sf.getWorkflowService().startProcess("testHeaderInDataMappingsPrimitive", null, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }

   @Test
   public void testHeaderInBodyInMappingsMixed() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      resultEndpoint.expectedHeaderReceived("input1", "header1");
      resultEndpoint.expectedHeaderReceived("input2", "header2");

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> expectedBody = new HashMap<String, Object>();
      expectedBody.put("firstName", "Manali");
      expectedBody.put("lastName", "Mungikar");
      dataMap.put("Person", expectedBody);

      resultEndpoint.expectedBodiesReceived(expectedBody);

      sf.getWorkflowService().startProcess("testHeaderInBodyInMappingsMixed", dataMap, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();

   }

   @Test
   public void testLegacyInMapping() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> expectedBody = new HashMap<String, Object>();
      expectedBody.put("firstName", "Manali");
      expectedBody.put("lastName", "Mungikar");
      dataMap.put("Person", expectedBody);

      resultEndpoint.expectedBodiesReceived(expectedBody);

      ProcessInstance pInstance = sf.getWorkflowService().startProcess("testLegacyInMapping", dataMap, true);
      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
//      try
//      {
//         Thread.currentThread().sleep(5000);
//      }
//      catch (InterruptedException e)
//      {
//         // move ahead
//      }
      @SuppressWarnings("unchecked")
      Map<String, Object> resultDataMap = (Map<String, Object>) sf.getWorkflowService().getInDataPath(
            pInstance.getOID(), "Person");

      assertNotNull(resultDataMap);
      assertTrue(resultDataMap.get("firstName").equals(expectedBody.get("lastName")));
      assertTrue(resultDataMap.get("lastName").equals(expectedBody.get("firstName")));
   }

   // @Test
   // public void testHeaderInOutMappingPrimitive() throws Exception
   // {
   // resultEndpoint.expectedHeaderReceived("output1", "header2");
   // resultEndpoint.expectedHeaderReceived("output2", "header1");
   //
   //
   // Exchange exchange = new DefaultExchange(this.context);
   // exchange.getOut().setHeader("output1", "header2");
   // exchange.getOut().setHeader("output2", "header1");
   // this.switchEndpoint.createExchange(exchange);
   //
   // this.wService.startProcess("testLegacyInMapping", null, true);
   //
   // resultEndpoint.assertIsSatisfied();
   //
   // }

   @Test
   public void testBodyOutDataMapping()
   {

   }

   @Test
   public void testHeaderOutDataMappings()
   {

   }

   @Test
   public void testEMailOverlayRuntime()
   {

      RouteBuilder builder = new RouteBuilder()
      {

         @Override
         public void configure() throws Exception
         {

            StringBuffer javeScript = new StringBuffer();
            javeScript.append("function setOutHeader(key, output){");
            javeScript.append("exchange.out.headers.put(key,output);}");

            javeScript.append("String.prototype.hashCode = function() {");
            javeScript.append("var hash = 0;");
            javeScript.append("if (this == 0) return hash;");
            javeScript.append("for (var i = 0; i < this.length; i++) {");
            javeScript.append("var character = this.charCodeAt(i);");
            javeScript.append("hash = ((hash<<5)-hash)+character;");
            javeScript.append("hash = hash & hash;");
            javeScript.append("}");
            javeScript.append("return hash;");
            javeScript.append("};");

            javeScript.append("var processInstanceOid = request.headers.get('ippProcessInstanceOid');\n");
            javeScript.append("var activityInstanceOid = request.headers.get('ippActivityInstanceOid');\n");
            javeScript.append("var partition = request.headers.get('ippPartition');\n");
            // javeScript.append("var investigate = false;\n");
            // javeScript.append("var outputValue = 'Peter';\n");

            // javeScript.append("var hashCode = (");
            // javeScript.append("processInstanceOid + '|' + ");
            // javeScript.append("activityInstanceOid + '|' + ");
            // javeScript.append("partition + '|' + ");
            // javeScript.append("investigate + '|' + ");
            // javeScript.append("outputValue).hashCode();");

            // javeScript.append("var link = 'http://localhost:8080/integration-runtime/mail-confirmation");
            // javeScript.append("?activityInstanceOid=' + activityInstanceOid + '");
            // javeScript.append("&processInstanceOid=' + processInstanceOid + '");
            // javeScript.append("&partition=' + partition + '");
            // javeScript.append("&investigate=false");
            // javeScript.append("&outputValue=' + outputValue + '");
            // javeScript.append("&hashCode=' + hashCode;");
            // javeScript.append("var hashCode = (processInstanceOid + '|' + activityInstanceOid + '|' + partition + '|false|zwei').hashCode();");
            javeScript
                  .append("var response = 'http://localhost:8080/integration-runtime/mail-confirmation?activityInstanceOid=' + activityInstanceOid + '&processInstanceOid=' + processInstanceOid + '&partition=' + partition + '&investigate=false&outputValue=zwei&hashCode=' + (processInstanceOid + '|' + activityInstanceOid + '|' + partition + '|false|zwei').hashCode();");

            javeScript.append("setOutHeader('response', response);");

            from("direct:test").setHeader("CamelLanguageScript", constant(javeScript.toString()))
                  .to("language:rhino-nonjdk").to("mock:result");

         }
      };

      try
      {

         String aiOid = "12345";
         String piOid = "54321";
         String partition = "abc";

         int hashCode = (piOid + "|" + aiOid + "|" + partition + "|false|zwei").hashCode();

         String link = "http://localhost:8080/integration-runtime/mail-confirmation?activityInstanceOid=" + aiOid
               + "&processInstanceOid=" + piOid + "&partition=" + partition
               + "&investigate=false&outputValue=zwei&hashCode=" + hashCode;

         resultEndpoint.expectedHeaderReceived("response", link);

         camelContext.addRoutes(builder);

         ProducerTemplate producer = new DefaultProducerTemplate(camelContext);
         producer.start();

         Exchange exchange = new DefaultExchange(camelContext);
         exchange.getIn().setHeader("ippActivityInstanceOid", aiOid);
         exchange.getIn().setHeader("ippProcessInstanceOid", piOid);
         exchange.getIn().setHeader("ippPartition", partition);

         exchange = producer.send("direct:test", exchange);

         assertNull(exchange.getException());

         resultEndpoint.assertIsSatisfied();
         resultEndpoint.reset();

      }
      catch (Exception e)
      {
         assertNotNull(e);
      }

   }

   @SuppressWarnings("unchecked")
   @Test
   public void testSpringInvocationWithBpmTypeConverter()
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> personMap = new HashMap<String, Object>();
      personMap.put("firstName", "Manali");
      personMap.put("lastName", "Mungikar");

      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("Person", personMap);

      ProcessInstance pInstance = sf.getWorkflowService().startProcess("testSpringInvocationWithBpmTypeConverter",
            dataMap, true);

      assertNotNull(pInstance);

   }

   @Test
   public void testProcessCorrelationMatch()
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      final ProcessInstance pInstance = sf.getWorkflowService().startProcess("testProcessCorrelationMatch", null, true);

      assertNotNull(pInstance);

//      try
//      {
//         Thread.currentThread().sleep(5000);
//      }
//      catch (InterruptedException e)
//      {
//         // move ahead
//      }

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
      
      try
      {
         Thread.currentThread().sleep(5000);
      }
      catch (InterruptedException e)
      {
         // move ahead
      }
   }

   @SuppressWarnings("unchecked")
   @Test 
   public void testRequestReponseMessage()
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

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
                  .setHeader(CamelConstants.MessageProperty.ACTIVITY_ID, constant("Test")).to("direct:inbound");

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

      ProcessInstance pInstance = sf.getWorkflowService().startProcess("testRequestReponseMessage", dataMap, true);

      assertNotNull(pInstance);

//      try
//      {
//         Thread.currentThread().sleep(5000);
//      }
//      catch (InterruptedException e)
//      {
//         // move ahead
//      }

//      Map<String, Object> dataFilterMap = new HashMap<String, Object>();
//      dataFilterMap.put("ID", new Long(id).toString());
    //  sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pInstance.getOID());
      // Exchange exchange = new DefaultExchange(this.camelContext);
      // producer.send("direct:inbound", exchange);
//      Map<String, Object> resultDataMap = (Map<String, Object>) sf.getWorkflowService().getInDataPath(
//            pInstance.getOID(), "Address");
//
//      assertNotNull(resultDataMap);
//
//      assertThat((String) resultDataMap.get("addrLine1"), equalTo("addrLine1"));
//      assertThat((String) resultDataMap.get("addrLine2"), equalTo("addrLine2"));
//      assertThat((String) resultDataMap.get("zipCode"), equalTo("zipCode"));
//      assertThat((String) resultDataMap.get("city"), equalTo("city"));

   }
}
