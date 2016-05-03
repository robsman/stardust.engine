package org.eclipse.stardust.test.camel.application.script.javascript;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

public class TestJavaScriptCrossModel extends AbstractCamelIntegrationTest
{
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_1_ID = "JavascriptOverlayInCrossModel1";
   public static final String MODEL_2_ID = "JavascriptOverlayInCrossModel2";

   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_1_ID,MODEL_2_ID);

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
   public void testJsSdtCrossModelProcess() throws Exception
   {
      WorkflowService workflowService=sf.getWorkflowService();
      ProcessInstance pInstance = workflowService.startProcess("{JavascriptOverlayInCrossModel2}JavascriptOverlayinCrossProcess", null, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      Object personContent = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "personContent");     
      Object detailContent = sf.getWorkflowService().getInDataPath(pInstance.getOID(), "detailContent");
      assertTrue("The response received is not a Map",personContent instanceof Map);
      assertTrue("The response received is not a Map",detailContent instanceof Map);
      Map<String,Object> personContentOut=(Map<String, Object>) personContent;
      Map<String,Object> detailContentOut=(Map<String, Object>) detailContent;
      assertEquals("John",  personContentOut.get("firstName"));
      assertEquals("Jack", personContentOut.get("lastName"));
      assertEquals("street 123", ((Map< ? , ? >) detailContentOut.get("address")).get("street"));
      assertEquals("4512", ((Map< ? , ? >) detailContentOut.get("address")).get("postalCode"));      
      assertEquals("12124545", ((Map< ? , ? >) detailContentOut.get("contact")).get("phone"));
      assertEquals("john.jack@mail.com", ((Map< ? , ? >) detailContentOut.get("contact")).get("email"));
   }
}
