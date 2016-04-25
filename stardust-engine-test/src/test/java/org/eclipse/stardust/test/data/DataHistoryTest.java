package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.data.DataModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.data.DataModelConstants.PROCESS_ID_1;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.api.dto.HistoricalData;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DataQueryResult;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class DataHistoryTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private long piOid;

   @Before
   public void setUp()
   {
      ParametersFacade.instance().flush();
      GlobalParameters.globals().set(KernelTweakingProperties.WRITE_HISTORICAL_DATA_TO_DB, true);
      
      piOid = startProcess();

      ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(
            ActivityInstanceQuery.findForProcessInstance(piOid));

      Map initialOutData = CollectionUtils.newMap();
      initialOutData.put("MyString", "initial Value");
      initialOutData.put("MyInt", 1);

      sf.getWorkflowService().activate(ai.getOID());

      sf.getWorkflowService()
            .suspendToDefaultPerformer(ai.getOID(), null, initialOutData);

      Map finalOutData = CollectionUtils.newMap();
      finalOutData.put("MyString", "final Value");
      finalOutData.put("MyInt", 2);

      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, finalOutData);
      sf.close();
      
   }

   @Test
   public void testHistoricalDataRetrievalNoHistoricalData()
   {
      
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      
      piQuery.setPolicy(HistoricalDataPolicy.NO_HISTORICAL_DATA);
      
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(piQuery);
      
      List<Serializable> historicValues = CollectionUtils.newList();

      for (ProcessInstance pi : pis)
      {
         if (pi.getOID() == this.piOid)
         {
            List<HistoricalData> histDataList = ((ProcessInstanceDetails) pi).getHistoricalData();
                        
            
            for (HistoricalData histData : histDataList)
            {
               historicValues.add(histData.getHistoricalDataValue());
            }
         }
                  
      }
      Assert.assertFalse(historicValues.contains(1));
      Assert.assertFalse(historicValues.contains(2));
      Assert.assertFalse(historicValues.contains("initial Value"));
      Assert.assertFalse(historicValues.contains("final Value"));

   }

   @Test
   public void testHistoricalDataExists()
   {
      
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      
      piQuery.setPolicy(HistoricalDataPolicy.INCLUDE_HISTORICAL_DATA);
      
      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(piQuery);
      
      List<Serializable> historicValues = CollectionUtils.newList();

      for (ProcessInstance pi : pis)
      {
         
         if (pi.getOID() == this.piOid)
         {
            
            List<HistoricalData> histDataList = ((ProcessInstanceDetails) pi).getHistoricalData();
                        
            
            for (HistoricalData histData : histDataList)
            {
               System.out.println("#### Type " + histData.getData().getModelOID());
               System.out.println("#### Type " + histData.getData().getId());
               historicValues.add(histData.getHistoricalDataValue());
            }
         }
                  
      }
      Assert.assertTrue(historicValues.contains(1));
      Assert.assertTrue(historicValues.contains(2));
      Assert.assertTrue(historicValues.contains("initial Value"));
      Assert.assertTrue(historicValues.contains("final Value"));

   }   
   
   
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_1, null,
            true);
      return pi.getOID();
   }
}
