package org.eclipse.stardust.test.camel.application.decorator;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
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

public class TestApplicationDecorator extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_1_ID = "MessageTransformationCore";
   public static final String MODEL_2_ID = "MessageTransformationDecorator";
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_1_ID,MODEL_2_ID);

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
   public void testApplicationDecorator() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("FirstName", "John");
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{MessageTransformationDecorator}DecoratorProcessForApplication",
           dataMap, true);
      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(),ProcessInstanceState.Completed);
      assertNotNull(pInstance);
      String response = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "Response");
      assertTrue("The output is not a String",response instanceof String);
      assertEquals("Hello John Taylor", response);
     
   }
}
