package org.eclipse.stardust.test.camel.application.script.javascript;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class JavaScriptModelTest extends AbstractCamelIntegrationTest
{
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_1_ID = "JavaScriptModel";
   public static final String MODEL_2_ID = "JavaScriptEnumModel";
   public static final String MODEL_3_ID = "JavaScriptXmlAttributes";
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_1_ID,MODEL_2_ID,MODEL_3_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @BeforeClass
   public static void setUpOnce()
   {
      TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
   }

   @AfterClass
   public static void tearDownOnce()
   {}

   @Test
   public void testBooleanWithJs() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("booleanA", false);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestBoolean", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "BooleanB");
      assertTrue("The response received is not a boolean",response instanceof Boolean);
      assertTrue((Boolean)response);
   }
   
   private Date toDate(String input) throws ParseException{
      return toDate(input, "dd-M-yyyy hh:mm:ss");
   }
   private Date toDate(String input, String format) throws ParseException{
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      return sdf.parse(input);
   }
   
   @Test
   public void testStringAndDateConcatenation() throws Exception
   {
      String textA = "Date Of Birth : ";
      Date dateB= toDate("31-08-1980 10:40:26"); 
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("textA", textA);
      dataMap.put("dateB", dateB);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestConcatStringAndDate", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "textC");
      assertTrue(response instanceof String);
      assertEquals("Date Of Birth : Sun Aug 31 10:40:26 GMT+01:00 1980", response.toString());
   }
   
   @Test
   public void testDateProcessingWithJsApplication() throws Exception
   {
      Date date = toDate("31-08-1982 10:20:56"); 
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("dateA", date);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestDate", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "DateB");
      assertTrue("The response received is not a Date",response instanceof Date);
      assertEquals("Tue Aug 31 10:20:56 GMT+01:00 1982", response.toString());
   }
   
   @Test
   public void testFloatAddition() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("floatA", 12.2);
      dataMap.put("floatB", 13.4);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestFloatAddition", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "FloatC");
      assertTrue(response instanceof Double);
      assertEquals(25.6, response);
   }
   
   @Test
   public void testIntAddition() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("IntA", 4);
      dataMap.put("IntB", 5);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestIntAddition", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "IntC");
      assertTrue(response instanceof Integer);
      assertEquals(9, response);
   }
   
   @Test
   public void testIntAndFloatAddition() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("IntA", 12);
      dataMap.put("floatB", 13.4);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestIntAndFloatAddition", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "floatC");
      assertTrue(response instanceof Double);
      assertEquals(25.4, response);
   }
   @Test
   public void testLongAddition() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("longA", 3L);
      dataMap.put("longB", 8L);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestLongAddition", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "longC");
      assertTrue(response instanceof Long);
      assertEquals(11L, response);
   }

   @Test
   public void testLongAndIntAddition() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("IntA", 2);
      dataMap.put("longB", 555555555L);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestLongAndIntAddition", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "longC");
      assertTrue(response instanceof Long);
      assertEquals(555555557L, response);
   }
   @Test
   public void testStringAddition() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("textA", "Addition of ");
      dataMap.put("textB", "textA and textB");
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestStringAddition", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "textC");
      assertTrue("The output is not a String",response instanceof String);
      assertEquals("Addition of textA and textB", response);
   }

   @Test
   public void testStringAndIntAddition() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("textA", "Age : ");
      dataMap.put("IntB", 40);
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptModel}TestStringAndIntAddition", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "textC");
      assertTrue("The output is not a String",response instanceof String);
      assertEquals("Age : 40", response);
   }

   @Test
   public void testEnumDataWithJavaScriptApplication() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("JSEnumDataIn", "b");
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptEnumModel}ProcessJsEnumeration", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "JSEnumDataOut");
      assertEquals("b", response);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testJsSdtXmlAttributesProcess() throws Exception
   {
      
      WorkflowService workflowService=sf.getWorkflowService();
      ProcessInstance pInstance = workflowService.startProcess("{JavaScriptXmlAttributes}JavaScriptXmlAttributesProcess", null, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object out = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "PersonData");
      assertNotNull(out);
      assertTrue("The output object is not a Map",out instanceof Map);
      Map<String,Object> response=(Map<String, Object>) out;
      assertEquals("Jastin", response.get("firstName"));
      assertEquals("Taylor", response.get("lastName"));
      assertEquals("street 123", ((Map< ? , ? >) response.get("address")).get("street"));
      assertEquals("California", ((Map< ? , ? >) response.get("address")).get("city"));
   }
}
