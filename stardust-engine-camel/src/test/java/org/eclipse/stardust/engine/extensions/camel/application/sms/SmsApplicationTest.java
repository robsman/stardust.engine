package org.eclipse.stardust.engine.extensions.camel.application.sms;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class SmsApplicationTest
{
   private static ClassPathXmlApplicationContext ctx;
   private String smppAccountName = "smppclient1";
   private String smppPassword = "testPW";
   private String srcAddress = "[IPP]";
   private String destAddress = "0021652010755";
   private int smppPort = 8056;
   private String expectedMsg= "Hi Sabri Bousselmi Your subscription is about to expire. " +
   		              "Could you please contact our sales department.   Thanks -- Technical Support";
   private SmppServer smppServer;

   @Resource
   private static ServiceFactoryAccess serviceFactoryAccess;

   @BeforeClass
   public static void beforeClass() throws IOException
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
   }
   
   
   @Before
   public void setup()
   {
      // SMPP Server should be running in a separate Threrad.
      new Thread(new Runnable()
      {
         public void run()
         {
            smppServer = SmppServer.getInstance();
            smppServer.setPort(smppPort);
            smppServer.start();
         }
      }).start();
      
   }

   @Test
   public void testSendSms() throws InterruptedException
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      sf.getWorkflowService().startProcess("{SmsApplicationTestModel}SendSMSProcess", null, true);
      
      // prepare output data for manual activity
      // customer Details: firstName & lastName
      Map<String, Object> data = new HashMap<String, Object>();
      Map<String, Object> sdt = new HashMap<String, Object>();
      sdt.put("firstName", "Sabri");
      sdt.put("lastName", "Bousselmi");
      data.put("Person", sdt);
       
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findAlive("{SmsApplicationTestModel}SendSMSProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
         
      // complete activity set info
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, data);
      // SMS has been sent via SMS Application
      Thread.sleep(3000);
      
      // No need to Assert Server Host Name & Port 
      // They should have a right value otherwise connection with SMPP server will not attempt
      
      assertEquals(smppAccountName,smppServer.getCamelRequest().getSystemId());
      assertEquals(smppPassword,smppServer.getCamelRequest().getPassword());
      assertEquals(srcAddress,smppServer.getCamelSubmitSm().getSourceAddr());
      assertEquals(destAddress,smppServer.getCamelSubmitSm().getDestAddress());
      String msg = new String(smppServer.getCamelSubmitSm().getShortMessage());
      assertEquals(expectedMsg,msg);
      
   }
}
