package org.eclipse.stardust.engine.extensions.camel.core.endpoint.data;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class BeanBpmTypeConverterTest
{

   private static ClassPathXmlApplicationContext ctx;
   private static CamelContext camelContext;
   private static ServiceFactoryAccess serviceFactoryAccess;
   protected static MockEndpoint resultEndpoint;
   
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
      resultEndpoint =camelContext.getEndpoint("mock:result", MockEndpoint.class);
   }



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
            + "\",\"double\":2.5,\"decimal\":500.00,\"float\":3.5}";

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

      assertTrue(((Map) result).get("string").equals(dataTypesMap.get("string")));
      assertTrue(((Map) result).get("boolean").equals(dataTypesMap.get("boolean")));
      assertTrue(((Map) result).get("int").equals(dataTypesMap.get("int")));
      assertTrue(((Map) result).get("short").equals(dataTypesMap.get("short")));
      assertTrue(((Map) result).get("byte").equals(dataTypesMap.get("byte")));
      assertTrue(((Map) result).get("double").equals(dataTypesMap.get("double")));
      assertTrue(((Map) result).get("float").equals(dataTypesMap.get("float")));

      // TODO: decimal is handled as String in IPP
      // assertThat(((Map) result).get("decimal"),
      // equalTo(dataTypesMap.get("decimal")));
      assertTrue(((Map) result).get("date").equals(dataTypesMap.get("date")));
      assertTrue(((Map) result).get("dateTime").equals(dataTypesMap.get("dateTime")));
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
            + "\",\"double\":2.5,\"decimal\":500.00,\"float\":3.5},{\"short\":5,\"dateTime\":\"" + dateString1
            + "\",\"byte\":1,\"int\":10,\"string\":\"aString\",\"boolean\":false,\"date\":\"" + dateString2
            + "\",\"double\":2.5,\"decimal\":500.00,\"float\":3.5}]}";

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

         assertTrue(((Map) map).get("string").equals(dataTypesMap.get("string")));
         assertTrue(((Map) map).get("boolean").equals(dataTypesMap.get("boolean")));
         assertTrue(((Map) map).get("int").equals(dataTypesMap.get("int")));
         assertTrue(((Map) map).get("short").equals(dataTypesMap.get("short")));
         assertTrue(((Map) map).get("byte").equals(dataTypesMap.get("byte")));
         assertTrue(((Map) map).get("double").equals(dataTypesMap.get("double")));
         assertTrue(((Map) map).get("float").equals(dataTypesMap.get("float")));

         // TODO: decimal is handled as String in IPP
         // assertThat(((Map) map).get("decimal"),
         // equalTo(dataTypesMap.get("decimal")));
         assertTrue(((Map) map).get("date").equals(dataTypesMap.get("date")));
         assertTrue(((Map) map).get("dateTime").equals(dataTypesMap.get("dateTime")));
      }

   }

   @Test
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
      assertTrue(((Map) result).get("addrLine1").equals(addressMap.get("addrLine1")));
      assertTrue(((Map) result).get("addrLine2").equals(addressMap.get("addrLine2")));
      assertTrue(((Map) result).get("zipCode").equals(addressMap.get("zipCode")));
      assertTrue(((Map) result).get("city").equals(addressMap.get("city")));

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
   
   @Test
   public void testOutDataPath() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{BPMTypeConverterTestModel}FromJsontooutdatapath", new HashMap<String, Object>(), true);
      Object result = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "PersonAddresses");
      assertNotNull(result);
      assertTrue(result instanceof Map< ? , ? >);
      assertTrue("France".equals(((Map)((Map) result).get("PAddress")).get("State")));
      assertTrue("Paris".equals(((Map)((Map) result).get("PAddress")).get("City")));
   }

}
