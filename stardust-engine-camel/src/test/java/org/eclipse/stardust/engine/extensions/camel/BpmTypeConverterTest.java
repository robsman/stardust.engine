package org.eclipse.stardust.engine.extensions.camel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
//
//@ContextConfiguration(locations = {
//      "BpmTypeConverterTest-context.xml", "classpath:carnot-spring-context.xml",
//      "classpath:jackrabbit-jcr-context.xml", "classpath:default-camel-context.xml"})

public class BpmTypeConverterTest // extends AbstractJUnit4SpringContextTests
{

   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/BpmTypeConverterTest-context.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");

      ClassPathResource resource = new ClassPathResource("models/BpmTypeConverterTestModel.xpdl");
      testUtils.setModelFile(resource);
      // this.serviceFactoryAccess.getDefaultServiceFactory().getAdministrationService().cleanupRuntimeAndModels();
      try
      {
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

  // @EndpointInject(uri = "mock:result", context = "defaultCamelContext")
   protected static MockEndpoint resultEndpoint;

   @SuppressWarnings("rawtypes")
   @Test
   public void testBpmTypeConversionSDTtoJSONDataTypes() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

      String dateString1 = "2013-05-26T23:59:27.000";
      String dateString2 = "2013-05-26T00:00:00.000";

      String expectedBody = "{\"short\":5,\"dateTime\":\"" + dateString1
            + "\",\"byte\":1,\"int\":10,\"string\":\"aString\",\"boolean\":false,\"date\":\"" + dateString2
            + "\",\"double\":2.5,\"decimal\":\"500.00\",\"float\":3.5}";

      resultEndpoint.expectedBodiesReceived(expectedBody);

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> dataTypesMap = new HashMap<String, Object>();
      dataTypesMap.put("string", "aString");
      dataTypesMap.put("boolean", false);
      dataTypesMap.put("int", 10);
      dataTypesMap.put("short", new Short("5"));
      dataTypesMap.put("byte", new Byte("1"));
      dataTypesMap.put("double", new Double("2.50"));
      dataTypesMap.put("float", new Float("3.50"));
      dataTypesMap.put("decimal", new BigDecimal("500.00"));
      dataTypesMap.put("date", sdf.parse(dateString2));
      dataTypesMap.put("dateTime", sdf.parse(dateString1));

      dataMap.put("DataTypes", dataTypesMap);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("SDTtoJSONDataTypes", dataMap, true);

      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();

      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "DataTypes");

      assertNotNull(result);

      assertTrue(result instanceof Map< ? , ? >);

      assertThat(((Map) result).get("string"), equalTo(dataTypesMap.get("string")));
      assertThat(((Map) result).get("boolean"), equalTo(dataTypesMap.get("boolean")));
      assertThat(((Map) result).get("int"), equalTo(dataTypesMap.get("int")));
      assertThat(((Map) result).get("short"), equalTo(dataTypesMap.get("short")));
      assertThat(((Map) result).get("byte"), equalTo(dataTypesMap.get("byte")));
      assertThat(((Map) result).get("double"), equalTo(dataTypesMap.get("double")));
      assertThat(((Map) result).get("float"), equalTo(dataTypesMap.get("float")));

      // TODO: decimal is handled as String in IPP
      // assertThat(((Map) result).get("decimal"),
      // equalTo(dataTypesMap.get("decimal")));
      assertThat(((Map) result).get("date"), equalTo(dataTypesMap.get("date")));
      assertThat(((Map) result).get("dateTime"), equalTo(dataTypesMap.get("dateTime")));
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testBpmTypeConversionSDTtoJSONDataTypesList() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

      String dateString1 = "2013-05-26T23:59:27.000";
      String dateString2 = "2013-05-26T00:00:00.000";

      String expectedBody = "{\"dataTypes\":[{\"short\":5,\"dateTime\":\"" + dateString1
            + "\",\"byte\":1,\"int\":10,\"string\":\"aString\",\"boolean\":false,\"date\":\"" + dateString2
            + "\",\"double\":2.5,\"decimal\":\"500.00\",\"float\":3.5},{\"short\":5,\"dateTime\":\"" + dateString1
            + "\",\"byte\":1,\"int\":10,\"string\":\"aString\",\"boolean\":false,\"date\":\"" + dateString2
            + "\",\"double\":2.5,\"decimal\":\"500.00\",\"float\":3.5}]}";

      resultEndpoint.expectedBodiesReceived(expectedBody);

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> dataTypeListMap = new HashMap<String, Object>();
      List<Map<String, Object>> dataTypeList = new ArrayList<Map<String, Object>>();

      Map<String, Object> dataTypesMap = new HashMap<String, Object>();
      dataTypesMap.put("string", "aString");
      dataTypesMap.put("boolean", false);
      dataTypesMap.put("int", 10);
      dataTypesMap.put("short", new Short("5"));
      dataTypesMap.put("byte", new Byte("1"));
      dataTypesMap.put("double", new Double("2.50"));
      dataTypesMap.put("float", new Float("3.50"));
      dataTypesMap.put("decimal", new BigDecimal("500.00"));
      dataTypesMap.put("date", sdf.parse(dateString2));
      dataTypesMap.put("dateTime", sdf.parse(dateString1));

      dataTypeList.add(dataTypesMap);
      dataTypeList.add(dataTypesMap);

      dataTypeListMap.put("dataTypes", dataTypeList);

      dataMap.put("DataTypeList", dataTypeListMap);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("SDTtoJSONDataTypesList", dataMap, true);

      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();

      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "DataTypeList");

      assertNotNull(result);

      assertTrue(result instanceof Map< ? , ? >);

      List< ? > dataTypeListResult = (List< ? >) ((Map< ? , ? >) result).get("dataTypes");

      for (Iterator< ? > i = dataTypeListResult.iterator(); i.hasNext();)
      {
         Object map = i.next();

         assertTrue(map instanceof Map< ? , ? >);

         assertThat(((Map) map).get("string"), equalTo(dataTypesMap.get("string")));
         assertThat(((Map) map).get("boolean"), equalTo(dataTypesMap.get("boolean")));
         assertThat(((Map) map).get("int"), equalTo(dataTypesMap.get("int")));
         assertThat(((Map) map).get("short"), equalTo(dataTypesMap.get("short")));
         assertThat(((Map) map).get("byte"), equalTo(dataTypesMap.get("byte")));
         assertThat(((Map) map).get("double"), equalTo(dataTypesMap.get("double")));
         assertThat(((Map) map).get("float"), equalTo(dataTypesMap.get("float")));

         // TODO: decimal is handled as String in IPP
         // assertThat(((Map) map).get("decimal"),
         // equalTo(dataTypesMap.get("decimal")));
         assertThat(((Map) map).get("date"), equalTo(dataTypesMap.get("date")));
         assertThat(((Map) map).get("dateTime"), equalTo(dataTypesMap.get("dateTime")));
      }

   }

   @Test
   // @Ignore
   public void testBpmTypeConversionSDTtoJSONNestedDataTypes()
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> dataTypesNestedMap = new HashMap<String, Object>();
      Map<String, Object> dataTypesMap = new HashMap<String, Object>();
      dataTypesMap.put("string", "aString");
      dataTypesMap.put("boolean", false);
      dataTypesMap.put("int", 10);
      dataTypesMap.put("short", 5);
      dataTypesMap.put("byte", 1);
      dataTypesMap.put("double", 2.50);
      dataTypesMap.put("float", 3.50);
      dataTypesMap.put("decimal", 500.00);
      dataTypesMap.put("date", new Date());
      dataTypesMap.put("dateTime", new Timestamp(new Date().getTime()));

      dataTypesNestedMap.put("dataTypes", dataTypesMap);

      dataMap.put("NestedDataTypes", dataTypesNestedMap);
      sf.getWorkflowService().startProcess("SDTtoJSONNestedDataTypes", dataMap, true);

   }

   @Test
   // @Ignore
   public void testBpmTypeConversionSDTToJSONHeader()
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> personMap = new HashMap<String, Object>();

      Map<String, Object> addressMap = new HashMap<String, Object>();
      addressMap.put("addrLine1", "test1");
      addressMap.put("addrLine2", "test1");
      addressMap.put("zipCode", "test1");
      addressMap.put("city", "test1");

      personMap.put("firstname", "Hans");
      personMap.put("firstname", "Wurst");
      personMap.put("address", addressMap);

      dataMap.put("Person", personMap);
      sf.getWorkflowService().startProcess("SDTtoJSONHeader", dataMap, true);

   }

   @Test
   // @Ignore
   public void testBpmTypeConversionXMLNested()
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> personMap = new HashMap<String, Object>();

      Map<String, Object> addressMap = new HashMap<String, Object>();
      addressMap.put("addrLine1", "test1");
      addressMap.put("addrLine2", "test1");
      addressMap.put("zipCode", "test1");
      addressMap.put("city", "test1");
      addressMap.put("short", 5);

      personMap.put("firstname", "Hans");
      personMap.put("firstname", "Wurst");
      personMap.put("age", 36);
      personMap.put("address", addressMap);

      dataMap.put("Person", personMap);
      sf.getWorkflowService().startProcess("SDTtoXMLNested", dataMap, true);
   }

   @Test
   // @Ignore
   public void testBpmTypeConversionJSONNested()
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> personMap = new HashMap<String, Object>();

      Map<String, Object> addressMap = new HashMap<String, Object>();
      addressMap.put("addrLine1", "test1");
      addressMap.put("addrLine2", "test1");
      addressMap.put("zipCode", "test1");
      addressMap.put("city", "test1");
      addressMap.put("short", 5);

      personMap.put("firstname", "Hans");
      personMap.put("firstname", "Wurst");
      personMap.put("int", 36);
      personMap.put("double", new Double(2.50));
      personMap.put("address", addressMap);

      dataMap.put("Person", personMap);
      sf.getWorkflowService().startProcess("SDTtoJSONNested", dataMap, true);
   }

   @Test
   // @Ignore
   public void testBpmTypeConversionXMLSimple() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> addressMap = new HashMap<String, Object>();
      addressMap.put("addrLine1", "test1");
      addressMap.put("addrLine2", "test1");
      addressMap.put("zipCode", "test1");
      addressMap.put("city", "test1");
      dataMap.put("Address", addressMap);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("SDTtoXMLSimple", dataMap, true);
      List<Exchange> exchanges = resultEndpoint.getReceivedExchanges();
      assertTrue(exchanges != null && !exchanges.isEmpty());
      assertTrue(((String) exchanges.get(0).getIn().getBody()) != null);

       resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();

      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "Address");

      assertNotNull(result);
      assertTrue(result instanceof Map< ? , ? >);
      assertThat(((Map) result).get("addrLine1"), equalTo(addressMap.get("addrLine1")));
      assertThat(((Map) result).get("addrLine2"), equalTo(addressMap.get("addrLine2")));
      assertThat(((Map) result).get("zipCode"), equalTo(addressMap.get("zipCode")));
      assertThat(((Map) result).get("city"), equalTo(addressMap.get("city")));

   }

   @Test
   public void testBpmTypeConversionJSONSimple() throws Exception
   {

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

      String expectedBody = "{\"addrLine1\":\"test1\",\"addrLine2\":\"test1\",\"zipCode\":\"test1\",\"city\":\"test1\"}";

      resultEndpoint.expectedBodiesReceived(expectedBody);

      Map<String, Object> dataMap = new HashMap<String, Object>();

      Map<String, Object> addressMap = new HashMap<String, Object>();
      addressMap.put("addrLine1", "test1");
      addressMap.put("addrLine2", "test1");
      addressMap.put("zipCode", "test1");
      addressMap.put("city", "test1");

      dataMap.put("Address", addressMap);
      sf.getWorkflowService().startProcess("SDTtoJSONSimple", dataMap, true);

      resultEndpoint.assertIsSatisfied();
      resultEndpoint.reset();
   }

}
