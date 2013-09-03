package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
public class CamelHelperTest extends AbstractJUnit4SpringContextTests {

	@Resource
	CamelContext camelContext;

	@SuppressWarnings("unchecked")
   @Test
	public void testTypedMapCreation() throws Exception
	{
	   String birthday = "24.04.1975";
	   String testDatePattern = "yyyyMMdd";
	   DateUtilsBean dateUtils = new DateUtilsBean();
	   
	   String bookNameKey = "bookName";
	   String bookName = "camel-ipp";
	   
	   // String representing the data to be parsed
		String testDataInput =
			
			"SimpleBoolean::true::boolean," + // primitive boolean with fixed ID
			"${header.SimpleInteger}," + // primitive Integer header with automatic ID
			"prefix-${header.ReferenceId}-suffix," + // primitive String with automatic ID from header and implicit long-to-string conversion
			"ID::${header.PersonId}," + // primitive header with fixed ID
            "Account::${header.AccountNumber}::int," + // primitive header with fixed ID and integer conversion
			"Person.name::${body}," + // struct field with Camel variable
			"Person.birthday::"+birthday+"::date," + // struct field with date conversion
			"BusinessData.arrivalDate::$simple{bean:dateUtils.dateString}::date," + // date conversion from String with bean reference
			"Filename::file-${date:header.TestDate:"+testDatePattern+"}.txt,"+ // date formatting with prefix
		
			"$simple{header.BookNameKey}::"+bookName+","+ //dynamic ID with fix value
			"$simple{header.PersonNameKey}::$simple{header.PersonNameValue},"+ // dynamic ID and value from header
			"DataField$simple{header.FieldIndex}::$simple{header.DataValue},"+  // dynamic DataFieldIndex and value from header
			"DataFieldNumber$simple{header.FieldIndex}::$simple{header.DataNumber}::int";  //dynamic DataFieldIndex and value from header with integer conversion

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

		Map<String,Object> headerMap = new HashMap<String,Object>();
		headerMap.put("SimpleInteger", simpleInteger);
		headerMap.put("ReferenceId", referenceId);
		headerMap.put("PersonId", personId);
		headerMap.put("AccountNumber", accountNumber);
		headerMap.put("TestDate", dateUtils.getDate());
		
		headerMap.put("PersonNameKey", personNameKey);
		headerMap.put("PersonNameValue", personName);
		headerMap.put("FieldIndex", fieldIndex);
		headerMap.put("DataValue", dataValue);
		headerMap.put("DataNumber", dataNumber);
		
		headerMap.put("BookNameKey", bookNameKey);
		
		Message message = new DefaultMessage();
		message.setHeaders( headerMap );
		message.setBody( personName );
		Exchange exchange = new DefaultExchange(camelContext);
		exchange.setIn( message );

		// First test exception behavior
		try {
			CamelHelper.createStructuredDataMap(errorInput1, exchange);
			fail("The input "+errorInput1+" should've raised an exception!");
		} catch(Exception e) {
			assertTrue( e instanceof IllegalArgumentException );
		}

		// Then test regular input as structured data
		Map<String,Object> result =
		CamelHelper.createStructuredDataMap(testDataInput, exchange);

		//verify
		assertEquals( Boolean.TRUE, (Boolean)result.get("SimpleBoolean") );
		assertEquals( simpleInteger, (Integer)result.get("SimpleInteger") );
		assertEquals( "prefix-"+referenceId+"-suffix", (String)result.get("ReferenceId"));
		assertEquals( personName, (String)((Map<String,Object>)result.get("Person")).get("name") );
		assertEquals( personBirthday, (Date)((Map<String,Object>)result.get("Person")).get("birthday") );
		assertEquals( personId, (Long)result.get("ID") ); // this one has a forced nameKey
		assertEquals( new Integer(accountNumber), (Integer)result.get("Account") );
        assertEquals( new DateUtilsBean().getDate(), (Date)((Map<String,Object>)result.get("BusinessData")).get("arrivalDate") );
        assertEquals( "file-"+new SimpleDateFormat("yyyyMMdd").format(dateUtils.getDate())+".txt", result.get("Filename") );
        assertEquals( bookName, (String)result.get(bookNameKey) );
        assertEquals( personName, (String)result.get(personNameKey) );
        assertEquals( dataValue, (String)result.get("DataField"+fieldIndex) );
        assertEquals( new Integer(dataNumber), (Integer)result.get("DataFieldNumber"+fieldIndex) );

		// Then test regular input as flat data
		result = CamelHelper.createFlatDataMap(testDataInput, exchange);

		//verify
		assertEquals( personName, (String)result.get("Person.name") );
		assertEquals( personBirthday, (Date)result.get("Person.birthday") );


	}

}
