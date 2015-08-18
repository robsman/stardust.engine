package org.eclipse.stardust.engine.extensions.camel.core.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;

import org.eclipse.stardust.engine.extensions.camel.component.CamelHelper;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration
public class CamelHelperTest extends AbstractJUnit4SpringContextTests
{

   @Resource
   CamelContext camelContext;

   private static String dateString = "17.04.1833";

   public String getDateString()
   {
      return dateString;
   }

   public static Date getDate() throws ParseException
   {

      return KeyValueList.getDateFormat().parse(dateString);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testTypedMapCreation() throws Exception
   {
      String birthday = "24.04.1975";
      String testDatePattern = "yyyyMMdd";
      // DateUtilsBean dateUtils = new DateUtilsBean();

      String bookNameKey = "bookName";
      String bookName = "camel-ipp";

      // String representing the data to be parsed
      String testDataInput =

      "SimpleBoolean::true::boolean," + // primitive boolean with fixed ID
            "${header.SimpleInteger}," + // primitive Integer header with automatic ID
            "prefix-${header.ReferenceId}-suffix," + // primitive String with automatic ID
                                                     // from header and implicit
                                                     // long-to-string conversion
            "ID::${header.PersonId}," + // primitive header with fixed ID
            "Account::${header.AccountNumber}::int," + // primitive header with fixed ID
                                                       // and integer conversion
            "Person.name::${body}," + // struct field with Camel variable
            "Person.birthday::" + birthday + "::date," + // struct field with date
                                                         // conversion
            "BusinessData.arrivalDate::$simple{bean:camelHelperTest.dateString}::date," + // date
                                                                                          // conversion
                                                                                          // from
                                                                                          // String
                                                                                          // with
                                                                                          // bean
                                                                                          // reference
            "Filename::file-${date:header.TestDate:" + testDatePattern + "}.txt," + // date
                                                                                    // formatting
                                                                                    // with
                                                                                    // prefix

            "$simple{header.BookNameKey}::" + bookName + "," + // dynamic ID with fix
                                                               // value
            "$simple{header.PersonNameKey}::$simple{header.PersonNameValue}," + // dynamic
                                                                                // ID and
                                                                                // value
                                                                                // from
                                                                                // header
            "DataField$simple{header.FieldIndex}::$simple{header.DataValue}," + // dynamic
                                                                                // DataFieldIndex
                                                                                // and
                                                                                // value
                                                                                // from
                                                                                // header
            "DataFieldNumber$simple{header.FieldIndex}::$simple{header.DataNumber}::int"; // dynamic
                                                                                          // DataFieldIndex
                                                                                          // and
                                                                                          // value
                                                                                          // from
                                                                                          // header
                                                                                          // with
                                                                                          // integer
                                                                                          // conversion

      String errorInput1 = "NameKey::";

      Integer simpleInteger = 63544;
      Long referenceId = 834792398347432L;
      String accountNumber = "74643221";
      String personName = "Mackeroy";
      String personNameKey = "personName";

      Date personBirthday = KeyValueList.getDateFormat().parse(birthday);
      Long personId = new Long(88373422);
      String dataNumber = "123456";
      String dataValue = "testData";
      String fieldIndex = "111";

      Map<String, Object> headerMap = new HashMap<String, Object>();
      headerMap.put("SimpleInteger", simpleInteger);
      headerMap.put("ReferenceId", referenceId);
      headerMap.put("PersonId", personId);
      headerMap.put("AccountNumber", accountNumber);
      headerMap.put("TestDate", getDate());

      headerMap.put("PersonNameKey", personNameKey);
      headerMap.put("PersonNameValue", personName);
      headerMap.put("FieldIndex", fieldIndex);
      headerMap.put("DataValue", dataValue);
      headerMap.put("DataNumber", dataNumber);

      headerMap.put("BookNameKey", bookNameKey);

      Message message = new DefaultMessage();
      message.setHeaders(headerMap);
      message.setBody(personName);
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setIn(message);

      // First test exception behavior
      try
      {
         CamelHelper.createStructuredDataMap(errorInput1, exchange);
         fail("The input " + errorInput1 + " should've raised an exception!");
      }
      catch (Exception e)
      {
         assertTrue(e instanceof IllegalArgumentException);
      }

      // Then test regular input as structured data
      Map<String, Object> result = CamelHelper.createStructuredDataMap(testDataInput,
            exchange);

      // verify
      assertEquals(Boolean.TRUE, (Boolean) result.get("SimpleBoolean"));
      assertEquals(simpleInteger, (Integer) result.get("SimpleInteger"));
      assertEquals("prefix-" + referenceId + "-suffix",
            (String) result.get("ReferenceId"));
      assertEquals(personName,
            (String) ((Map<String, Object>) result.get("Person")).get("name"));
      assertEquals(personBirthday,
            (Date) ((Map<String, Object>) result.get("Person")).get("birthday"));
      assertEquals(personId, (Long) result.get("ID")); // this one has a forced nameKey
      assertEquals(new Integer(accountNumber), (Integer) result.get("Account"));
      assertEquals(getDate(),
            (Date) ((Map<String, Object>) result.get("BusinessData")).get("arrivalDate"));
      assertEquals("file-" + new SimpleDateFormat("yyyyMMdd").format(getDate()) + ".txt",
            result.get("Filename"));
      assertEquals(bookName, (String) result.get(bookNameKey));
      assertEquals(personName, (String) result.get(personNameKey));
      assertEquals(dataValue, (String) result.get("DataField" + fieldIndex));
      assertEquals(new Integer(dataNumber),
            (Integer) result.get("DataFieldNumber" + fieldIndex));

      // Then test regular input as flat data
      result = CamelHelper.createFlatDataMap(testDataInput, exchange);

      // verify
      assertEquals(personName, (String) result.get("Person.name"));
      assertEquals(personBirthday, (Date) result.get("Person.birthday"));

   }

   @Test
   public void testNestedTypedMap() throws Exception
   {
      String body = "some content from body";
      String headerValue = "city Name";
      String uri = "Person.firstName::$simple{body},Person.address/city::$simple{header.city}";
      Exchange exchange = new DefaultExchange(camelContext);
      Message message = new DefaultMessage();
      Map<String, Object> headerMap = new HashMap<String, Object>();
      headerMap.put("city", headerValue);
      message.setHeaders(headerMap);
      message.setBody(body);
      exchange.setIn(message);

      Map<String, Object> result = CamelHelper.createStructuredDataMap(uri, exchange);
      Map<String, Object> sdt = (Map<String, Object>) result.get("Person");
      assertTrue(sdt != null);
      assertEquals(body, sdt.get("firstName"));
      assertEquals(headerValue, ((Map) sdt.get("address")).get("city"));
   }

   @Test
   public void testMultiLevelSupportInDataPath() throws Exception
   {
      String body = "some content from body";
      String headerValue = "city Name";
      String uri = "Person.firstName::$simple{body},Order.customer/address/city::$simple{header.city}";
      Exchange exchange = new DefaultExchange(camelContext);
      Message message = new DefaultMessage();
      Map<String, Object> headerMap = new HashMap<String, Object>();
      headerMap.put("city", headerValue);
      message.setHeaders(headerMap);
      message.setBody(body);
      exchange.setIn(message);

      Map<String, Object> result = CamelHelper.createStructuredDataMap(uri, exchange);
      Map<String, Object> sdt1 = (Map<String, Object>) result.get("Person");
      Map<String, Object> sdt2 = (Map<String, Object>) result.get("Order");
      assertTrue(sdt1 != null);
      assertTrue(sdt2 != null);
      assertEquals(body, sdt1.get("firstName"));
      assertEquals(headerValue,
            ((Map) ((Map) sdt2.get("customer")).get("address")).get("city"));
   }

   @Test
   public void testListSupportInDataPath() throws Exception
   {
      String body = "some content from body";
      String headerValue = "city Name";
      String uri = "Person.firstName::$simple{body},Person.address[0]/city::$simple{header.city},Person.address[1]/city::$simple{header.city}";
      Exchange exchange = new DefaultExchange(camelContext);
      Message message = new DefaultMessage();
      Map<String, Object> headerMap = new HashMap<String, Object>();
      headerMap.put("city", headerValue);
      message.setHeaders(headerMap);
      message.setBody(body);
      exchange.setIn(message);

      Map<String, Object> result = CamelHelper.createStructuredDataMap(uri, exchange);
      Map<String, Object> sdt = (Map<String, Object>) result.get("Person");
      assertTrue(sdt != null);
      assertTrue(sdt.get("address") instanceof List);
      assertEquals(headerValue, ((Map) ((List) sdt.get("address")).get(0)).get("city"));
      assertEquals(headerValue, ((Map) ((List) sdt.get("address")).get(1)).get("city"));
   }

   @Test
   public void testListSupportSecondExample() throws Exception
   {
      String body = "some content from body";
      String headerValue = "city Name";
      String uri = "Person.firstName::$simple{body},Person.address/cities[0]/abc::$simple{header.city},Person.address/street::$simple{header.street},Person.a/b/c/cs[3]/c::$simple{header.c}";
      Exchange exchange = new DefaultExchange(camelContext);
      Message message = new DefaultMessage();
      Map<String, Object> headerMap = new HashMap<String, Object>();
      headerMap.put("city", headerValue);
      headerMap.put("street", "populated from header street");
      headerMap.put("c", "Populated from header c");
      message.setHeaders(headerMap);
      message.setBody(body);
      exchange.setIn(message);

      Map<String, Object> result = CamelHelper.createStructuredDataMap(uri, exchange);
      Map<String, Object> sdt = (Map<String, Object>) result.get("Person");
      assertTrue(sdt != null);
      assertEquals(body, sdt.get("firstName"));
      assertTrue((Map) sdt.get("address") != null);
      assertTrue(((Map) sdt.get("address")).get("cities") != null);
      // assertTrue(((Map)sdt.get("address")).get("cities") instanceof List);
      assertEquals(headerValue,
            ((Map) ((List) ((Map) sdt.get("address")).get("cities")).get(0)).get("abc"));
      assertEquals("populated from header street",
            ((Map) sdt.get("address")).get("street"));
      assertEquals("Populated from header c",
            ((Map) ((List) ((Map) ((Map) ((Map) sdt.get("a")).get("b")).get("c"))
                  .get("cs")).get(2)).get("c"));
   }

   @Test
   public void testListOfString() throws Exception
   {
      String body = "some content from body";
      String headerValue = "city Name";
      String uri = "Person.firstName::$simple{body},Person.address/cities[0]::$simple{header.city},Person.a/b/c/cs[3]::$simple{header.c}";
      Exchange exchange = new DefaultExchange(camelContext);
      Message message = new DefaultMessage();
      Map<String, Object> headerMap = new HashMap<String, Object>();
      headerMap.put("city", headerValue);
      headerMap.put("street", "populated from header street");
      headerMap.put("c", "Populated from header c");
      message.setHeaders(headerMap);
      message.setBody(body);
      exchange.setIn(message);

      Map<String, Object> result = CamelHelper.createStructuredDataMap(uri, exchange);
      Map<String, Object> sdt = (Map<String, Object>) result.get("Person");
      assertTrue(sdt != null);
      assertEquals(body, sdt.get("firstName"));
      // assertTrue((Map)sdt.get("address") != null);
      assertTrue(((Map) sdt.get("address")).get("cities") != null);
      // assertTrue(((Map)sdt.get("address")).get("cities") instanceof List);
      assertEquals(headerValue, ((List) ((Map) sdt.get("address")).get("cities")).get(0));
      // assertEquals("populated from header street",
      // ((Map)sdt.get("address")).get("street") );
      // assertEquals("Populated from header c",
      // ((Map)((List)((Map)((Map)((Map)sdt.get("a")).get("b")).get("c")).get("cs")).get(2)).get("c"));
   }

   @Test
   public void testSecondListOfString() throws Exception
   {
      String body = "some content from body";
      String headerValue = "city Name";
      String uri = "Person.address/city::$simple{header.city},Person.firstName::$simple{header.firstName},Person.listOfString[0]::$simple{header.firstElementOflistOfStringHeader},MessageBody::$simple{bodyAs(java.lang.String)},Person.listOfString[2]::$simple{header.secondElementOflistOfTextHeader}";
      // String uri =
      // "Person.listOfString[0]::$simple{header.firstElementOflistOfStringHeader},Person.listOfString[2]::$simple{header.secondElementOflistOfTextHeader}";
      Exchange exchange = new DefaultExchange(camelContext);
      Message message = new DefaultMessage();
      Map<String, Object> headerMap = new HashMap<String, Object>();
      headerMap.put("city", "city");
      headerMap.put("firstName", "firstName");
      headerMap.put("firstElementOflistOfStringHeader",
            "firstElementOflistOfStringHeader");
      headerMap.put("secondElementOflistOfTextHeader", "secondElementOflistOfTextHeader");
      headerMap.put("street", "populated from header street");
      headerMap.put("c", "Populated from header c");
      message.setHeaders(headerMap);
      message.setBody(body);
      exchange.setIn(message);

      Map<String, Object> result = CamelHelper.createStructuredDataMap(uri, exchange);
      Map<String, Object> sdt = (Map<String, Object>) result.get("Person");
      assertTrue(sdt != null);
      assertEquals(body, result.get("MessageBody"));
      // assertTrue((Map)sdt.get("address") != null);
      // assertTrue(((Map)sdt.get("address")).get("cities") != null);
      // assertTrue(((Map)sdt.get("address")).get("cities") instanceof List);
      // assertEquals(headerValue, ((List)((Map)sdt.get("address")).get("cities")).get(0)
      // );
      // assertEquals("populated from header street",
      // ((Map)sdt.get("address")).get("street") );
      // assertEquals("Populated from header c",
      // ((Map)((List)((Map)((Map)((Map)sdt.get("a")).get("b")).get("c")).get("cs")).get(2)).get("c"));
   }

}
