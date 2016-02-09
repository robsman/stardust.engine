package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests whether in simple modeler descriptors for BUSINESS_DATE will be injected correctly.
 * </p>
 * 
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class DataDescriptorInjectionModelExtenderTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory serviceFactory = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "SimpleModeler");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(serviceFactory);

   private Map<String, Object> processData;

   @Before
   public void setup()
   {
      processData = CollectionUtils.newHashMap();

      Date date = new Date();
      date.setYear(2000);      
      processData.put(PredefinedConstants.BUSINESS_DATE, date);
   }

   @Test
   public void testExtendDescriptorsProcessDefinition_1()
   {
      Date value = null;
      DataPath path = null;
      
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            "ProcessDefinition_1", this.processData, true);
      
      List<ProcessDefinition> allProcessDefinitions = serviceFactory.getQueryService().getAllProcessDefinitions();
      for(ProcessDefinition pd : allProcessDefinitions)
      {
         List<DataPath> paths = pd.getAllDataPaths();   
         for (DataPath p : paths)
         {
            if (p.getId().equals("BusinessDate_2") && pd.getId().equals("ProcessDefinition_1"))
            {
               value = (Date) serviceFactory.getWorkflowService().getInDataPath(pi.getOID(), p.getId());
               path = p;
               break;
            }
         }
      }
         
      if (value != null && path != null)
      {
         Assert.assertTrue(path.isDescriptor());
         assertEquals(path.getData(), PredefinedConstants.BUSINESS_DATE);            
         assertEquals(path.getName(), "Business Date");                     
         assertEquals(value.getYear(), 2000);
      }
      else
      {
         Assert.fail("No Descriptor has been found at all!");
      }
   }
      
   @Test
   public void testExtendDescriptorsProcessDefinition_2()
   {
      Date value = null;
      DataPath path = null;
      
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            "ProcessDefinition_2", this.processData, true);
            
      List<ProcessDefinition> allProcessDefinitions = serviceFactory.getQueryService().getAllProcessDefinitions();
      for(ProcessDefinition pd : allProcessDefinitions)
      {
         List<DataPath> paths = pd.getAllDataPaths();   
         for (DataPath p : paths)
         {
            if (p.getId().equals("BusinessDate_1") && pd.getId().equals("ProcessDefinition_2"))
            {
               value = (Date) serviceFactory.getWorkflowService().getInDataPath(pi.getOID(), p.getId());
               path = p;
               break;
            }
         }
      }
         
      if (value != null && path != null)
      {
         Assert.assertTrue(path.isDescriptor());
         assertEquals(path.getData(), PredefinedConstants.BUSINESS_DATE);       
         assertEquals(path.getName(), "Business Date");                              
         assertEquals(value.getYear(), 2000);
      }
      else
      {
         Assert.fail("No Descriptor has been found at all!");
      }
   }   
}