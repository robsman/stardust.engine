package org.eclipse.stardust.engine.extensions.camel.core.endpoint.activity;

import static org.junit.Assert.assertEquals;

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

public class ActivityEndpointTestCompletion
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
   public void testManualActivityCompletionWithoutData() throws InterruptedException
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CompleteActivityModel}TestManualActivityCompletionWithoutData", null, true);
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);
      
      ActivityInstance manualActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "ManualActivity")).get(0);
      
      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, manualActivity.getState());
      
      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);
      Thread.sleep(1000);
      
      manualActivity = sf.getWorkflowService().getActivityInstance(manualActivity.getOID());
      // check Activity Status
      assertEquals(ActivityInstanceState.Completed, manualActivity.getState());

   }

   @Test
   public void testManualActivityCompletionWithData() throws InterruptedException
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CompleteActivityModel}TestManualActivityCompletionWithData", null, true);
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);
      
      ActivityInstance manualActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "ManualActivity")).get(0);
      
      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, manualActivity.getState());
      
      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);
      Thread.sleep(1000);
      
      manualActivity = sf.getWorkflowService().getActivityInstance(manualActivity.getOID());
      // check Activity Status
      assertEquals(ActivityInstanceState.Completed, manualActivity.getState());
      
      // check data value
      String data = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "data");
      assertEquals("some value", data);
      
      // complete process
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pInstance.getOID());
   }
   
   @Test
   public void testRootActivityCompletion() throws InterruptedException
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CompleteActivityModel}TestRootActivityCompletion", null, true);
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);
      
      ActivityInstance rootActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "RootActivity")).get(0);
      
      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Hibernated, rootActivity.getState());
      
      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);
      Thread.sleep(1000);
      
      rootActivity = sf.getWorkflowService().getActivityInstance(rootActivity.getOID());
      // check Activity Status
      assertEquals(ActivityInstanceState.Completed, rootActivity.getState());
   }

   @Test
   public void testUIMashupCompletion() throws InterruptedException
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      // start process
      ProcessInstance pInstance = sf.getWorkflowService().startProcess(
            "{CompleteActivityModel}TestUIMashupCompletion", null, true);
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "DoSomething"));
      ActivityInstance doSomethingActivityInstance = activityInstances.get(0);
      
      ActivityInstance uIMashupActivity = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive(
            pInstance.getProcessID(), "UIMashupActivity")).get(0);
      
      // before running activity:complete command
      // check Activity status
      assertEquals(ActivityInstanceState.Suspended, uIMashupActivity.getState());
      
      // run activity:complete command
      sf.getWorkflowService().activateAndComplete(doSomethingActivityInstance.getOID(), null, null);
      Thread.sleep(1000);
      
      uIMashupActivity = sf.getWorkflowService().getActivityInstance(uIMashupActivity.getOID());
      // check Activity Status
      assertEquals(ActivityInstanceState.Completed, uIMashupActivity.getState());
      
      // check data value
      String data = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "data");
      assertEquals("some value", data);
      
      // complete process
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pInstance.getOID());
   }

}
