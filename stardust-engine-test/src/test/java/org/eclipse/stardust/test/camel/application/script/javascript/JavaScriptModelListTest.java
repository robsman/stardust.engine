package org.eclipse.stardust.test.camel.application.script.javascript;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.camel.application.sql.SqlApplicationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class JavaScriptModelListTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager
         .getLogger(SqlApplicationTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   public static final String MODEL_ID = "JavaScriptModelList";

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

   @SuppressWarnings("unchecked")
   @Test
   public void testJsSdtListProcess() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      Map<String, Object> persons = new HashMap<String, Object>();
      List<Map< ? , ? >> personsList = new LinkedList<Map< ? , ? >>();
      Map<String, Object> person = new HashMap<String, Object>();
      person.put("FirstName", "Paul");
      person.put("LastName", "Gomez");
      person.put("Age", 10);
      personsList.add(person);

      person = new HashMap<String, Object>();
      person.put("FirstName", "ABC");
      person.put("LastName", "CDE");
      person.put("Age", 22);
      personsList.add(person);

      persons.put("Persons", personsList);
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("PersonsSdtInput", persons);
      ProcessInstance pInstance = workflowService
            .startProcess("{JavaScriptModelList}JavascriptListSdtProcess", dataMap, true);
      ActivityInstanceStateBarrier.instance().awaitAlive(pInstance.getOID());
      assertNotNull(pInstance);
      Map< ? , ? > response = (Map< ? , ? >) workflowService
            .getInDataPath(pInstance.getOID(), "PersonsSdtOutput");
      trace.info("get PersonsSdtOutput = " + response);
      List<Map< ? , ? >> personsOutput = (List<Map< ? , ? >>) response.get("Persons");
      assertEquals(3, personsOutput.size());
      assertEquals("Paul", ((Map< ? , ? >) personsOutput.get(0)).get("FirstName"));
      assertEquals("Gomez", ((Map< ? , ? >) personsOutput.get(0)).get("LastName"));
      assertEquals(12, ((Map< ? , ? >) personsOutput.get(0)).get("Age"));
      assertEquals("ABC", ((Map< ? , ? >) personsOutput.get(1)).get("FirstName"));
      assertEquals("CDE", ((Map< ? , ? >) personsOutput.get(1)).get("LastName"));
      assertEquals(26, ((Map< ? , ? >) personsOutput.get(1)).get("Age"));
      assertEquals("FN added in JS",
            ((Map< ? , ? >) personsOutput.get(2)).get("FirstName"));
      assertEquals("LN added in JS",
            ((Map< ? , ? >) personsOutput.get(2)).get("LastName"));
      assertEquals(10, ((Map< ? , ? >) personsOutput.get(2)).get("Age"));

   }

   @SuppressWarnings("unchecked")
   @Test
   public void testJsStringListProcess() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      Map<String, Object> cities = new HashMap<String, Object>();
      List<String> citiesList = new ArrayList<String>();
      citiesList.add("Rome");
      citiesList.add("Madrid");
      cities.put("Cities", citiesList);
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("CitiesInput", cities);
      ProcessInstance pInstance = workflowService.startProcess(
            "{JavaScriptModelList}JavascriptListStringProcess", dataMap, true);
      ActivityInstanceStateBarrier.instance().awaitAlive(pInstance.getOID());
      assertNotNull(pInstance);
      Map< ? , ? > response = (Map< ? , ? >) workflowService
            .getInDataPath(pInstance.getOID(), "CitiesOutput");
      List<String> citiesOutput = (List<String>) response.get("Cities");
      assertEquals(4, citiesOutput.size());
      assertEquals("Rome", citiesOutput.get(0));
      assertEquals("Madrid", citiesOutput.get(1));
      assertEquals("Paris", citiesOutput.get(2));
      assertEquals("New York", citiesOutput.get(3));
   }
}
