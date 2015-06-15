package org.eclipse.stardust.test.bo;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessDefinitions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class BusinessObjectsModelExtenderTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BusinessObjects",
         "BusinessObjectsProcess");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private Map<String, Object> processData;

   @Before
   public void setup()
   {
      // generate BO data
      Map<String, Object> businessObject = CollectionUtils.newHashMap();

      businessObject.put("ID", "BO_ID");
      businessObject.put("Name", "BO_NAME");
      businessObject.put("Description", "BO_DESCRIPTION");
      businessObject.put("INTEGER", 1000);
      businessObject.put("BOOLEAN", true);

      this.processData = CollectionUtils.newHashMap();

      this.processData.put("BOStructured", businessObject);
   }

   @Test
   public void testExtendDescriptors()
   {

      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            "{NewSimpleModel3}DarkerChecklist", this.processData, true);

      ProcessDefinition pd = serviceFactory.getQueryService().getProcessDefinition(
            "{NewSimpleModel3}DarkerChecklist");

      List<DataPath> paths = pd.getAllDataPaths();

      String value = null;
      DataPath path = null;

      System.out.println("Display all");
      for (DataPath p : paths)
      {
         System.out.println("######## "
               + p.getQualifiedId()
               + " with value "
               +  serviceFactory.getWorkflowService().getInDataPath(pi.getOID(),
                     p.getId()));

      }

      for (DataPath p : paths)
      {
         if (p.getId().equals("ID"))
         {
            value = (String) serviceFactory.getWorkflowService().getInDataPath(pi.getOID(),
                  p.getId());
            path = p;
            break;
         }
      }

      System.out.println("----> Descriptor: " + path.getName());
      System.out.println("----> Value: " + value);

      if (value != null && path != null)
      {
         Assert.assertTrue(path.isDescriptor());
         assertEquals(value, "BO_ID");
      }
      else
      {
         Assert.fail("No Descriptor has been found at all");
      }

   }
}
