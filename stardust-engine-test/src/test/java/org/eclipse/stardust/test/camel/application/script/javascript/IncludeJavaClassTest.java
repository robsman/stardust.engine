package org.eclipse.stardust.test.camel.application.script.javascript;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;
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

public class IncludeJavaClassTest  extends AbstractCamelIntegrationTest
{
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_ID = "JavaScriptOverlayTypeTestModel";

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @BeforeClass
   public static void setUpOnce()
   {
   }

   @AfterClass
   public static void tearDownOnce()
   {}
   @Test
   public void testCallJavaMethod() throws Exception
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
      
      WorkflowService workflowService=sf.getWorkflowService();
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("S1",sdt);
      ProcessInstance pInstance = workflowService.startProcess("{Model12}ScriptingExample", dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object response = workflowService.getInDataPath(pInstance.getOID(), "S1");
      assertNotNull(response);
      assertTrue("The response received is not a Map",response instanceof Map);
      Map<String, Object> sdtOut = (Map<String, Object>)response;
      assertEquals(sdtOut.get("aText"), "Hello JS OverlayType");
      assertNotNull(sdtOut.get("aDate"));
      assertTrue(sdtOut.get("aDate") instanceof Date);
      assertNotNull(sdtOut.get("aDateTime"));
      assertTrue(sdtOut.get("aDateTime") instanceof Date);
      assertNotNull(sdtOut.get("aDateList"));
      assertNotNull(sdtOut.get("aDateList") instanceof List);
      for (Object elt : (List<Date>) sdtOut.get("aDateList"))
         assertTrue(elt instanceof Date);

      assertNotNull(((Map<String, Object>) sdtOut.get("S2")).get("aDate"));
      assertTrue(((Map<String, Object>) sdtOut.get("S2")).get("aDate") instanceof Date);
      assertNotNull(((Map<String, Object>) sdtOut.get("S2")).get("aDateTime"));
      assertTrue(((Map<String, Object>) sdtOut.get("S2")).get("aDateTime") instanceof Date);
      // S2s
      assertNotNull(sdtOut.get("S2s"));
      assertTrue(sdtOut.get("S2s") instanceof List);
      for (Map<String, Object> elt : (List<Map<String, Object>>) sdtOut.get("S2s"))
      {
         assertNotNull(elt.get("aDate"));
         assertTrue(elt.get("aDate") instanceof Date);
         assertNotNull(elt.get("aDateTime"));
         assertTrue(elt.get("aDateTime") instanceof Date);
      }
   }
}
