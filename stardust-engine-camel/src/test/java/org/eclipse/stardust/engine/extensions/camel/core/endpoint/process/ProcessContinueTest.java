package org.eclipse.stardust.engine.extensions.camel.core.endpoint.process;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class ProcessContinueTest
{
   private static ClassPathXmlApplicationContext ctx;

   private static ServiceFactoryAccess serviceFactoryAccess;

   @BeforeClass
   public static void beforeClass()
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {
                  "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
                  "classpath:carnot-spring-context.xml",
                  "classpath:jackrabbit-jcr-context.xml",
                  "classpath:default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx
            .getBean("ippServiceFactoryAccess");
   }
   
  
   @Test
   public void testContinueProcessManualActivity() throws InterruptedException
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}TestContinueProcessManualActivity", null, true);
      // start process which will be completed by the CMD
      ProcessInstance pInstanceToBeCompleted = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}ManualActivityInProcessToBeContinued", null, true);
      
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);
      
      ActivityInstance manualActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstanceToBeCompleted.getProcessID(), "WaitActivity")).get(0);
      
      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, manualActivity.getState());
      
      // run activity:complete command 
      // ippProcessInstanceOid
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("ippProcessInstanceOid", String.valueOf(pInstanceToBeCompleted.getOID()));
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, data);
      Thread.sleep(1000);
      
      manualActivity = sf.getWorkflowService().getActivityInstance(manualActivity.getOID());
      // check Activity Status
      assertEquals(ActivityInstanceState.Completed, manualActivity.getState());
   }
  
   @Test
   public void testContinueProcessUIMashup() throws InterruptedException
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}TestContinueProcessUIMashup", null, true);
     // start process which will be completed by the CMD
      ProcessInstance pInstanceToBeCompleted = sf.getWorkflowService().startProcess(
            "{ProcessContinueModel}UIMasupActivityInProcessToBeContinued", null, true);
      
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);
      
      ActivityInstance uIMashupActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstanceToBeCompleted.getProcessID(), "UIMashup")).get(0);
      
      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, uIMashupActivity.getState());
      
      // run activity:complete command
      // ippProcessInstanceOid
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("ippProcessInstanceOid", String.valueOf(pInstanceToBeCompleted.getOID()));
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, dataMap);
      Thread.sleep(1000);
      
      uIMashupActivity = sf.getWorkflowService().getActivityInstance(uIMashupActivity.getOID());
      // check Activity Status
      assertEquals(ActivityInstanceState.Completed, uIMashupActivity.getState());
      
      // check data value
      String data = (String) sf.getWorkflowService().getInDataPath(pInstanceToBeCompleted.getOID(), "data");
      assertEquals("some value", data);
      
      // complete process
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pInstanceToBeCompleted.getOID());
   }
}
