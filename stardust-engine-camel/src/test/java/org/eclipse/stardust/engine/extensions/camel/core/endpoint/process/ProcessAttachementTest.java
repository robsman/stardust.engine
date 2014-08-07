package org.eclipse.stardust.engine.extensions.camel.core.endpoint.process;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.*;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class ProcessAttachementTest
{
   private static final String ATTACHMENT_ROUTE_BEGIN = "direct:attachTextFile";
   private static final String ATTACHMENT_ROUTE_END = "mock:endAttachementRoute";

   private static final String ATTACHMENT_PARAM_ROUTE_BEGIN = "direct:attachTextFileRouteUsingParametersFromUri";
   private static final String ATTACHMENT_PARAM_ROUTE_END = "mock:endAttachTextFileRouteUsingParametersFromUri";

   private static final String ATTACHMENT_BINARY_ROUTE_END = "mock:endAttachBinaryFileRoute";

   private static CamelContext camelContext;

   private static ProducerTemplate attachTextFileProducerTemplate;
   private static MockEndpoint attachProducerRouteResult;

   private static ProducerTemplate attachTextFileRouteUsingParametersFromUriProducerTemplate;
   private static MockEndpoint attachTextFileRouteUsingParametersFromUriRouteResult;

   private static MockEndpoint attachBinaryFileRouteResult;

   private static ServiceFactoryAccess serviceFactoryAccess;
   private static ClassPathXmlApplicationContext ctx;
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/core/endpoint/process/ProcessAttachementTest-context.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");

      attachTextFileProducerTemplate = camelContext.createProducerTemplate();
      attachTextFileProducerTemplate.setDefaultEndpointUri(ATTACHMENT_ROUTE_BEGIN);

      attachTextFileRouteUsingParametersFromUriProducerTemplate = camelContext.createProducerTemplate();
      attachTextFileRouteUsingParametersFromUriProducerTemplate.setDefaultEndpointUri(ATTACHMENT_PARAM_ROUTE_BEGIN);

      attachProducerRouteResult = camelContext.getEndpoint(ATTACHMENT_ROUTE_END, MockEndpoint.class);
      attachTextFileRouteUsingParametersFromUriRouteResult = camelContext.getEndpoint(ATTACHMENT_PARAM_ROUTE_END, MockEndpoint.class);
      attachBinaryFileRouteResult= camelContext.getEndpoint(ATTACHMENT_BINARY_ROUTE_END, MockEndpoint.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testAttachTextFolderNameWithoutLeadingSlash() throws Exception
   {
      camelContext.addRoutes(testAttachTextFileRoute());
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setProperty(ATTACHMENT_FILE_NAME, "test.txt");
      exchange.setProperty(ATTACHMENT_FOLDER_NAME, "testAttachTextFolderNameWithoutLeadingSlash");
      exchange.getIn().setBody("hello world");

      attachTextFileProducerTemplate.send(exchange);
      attachProducerRouteResult.setExpectedMessageCount(1);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid1);

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance pi = wfService.getProcessInstance(piOid1);
      assertEquals(ProcessInstanceState.Active, pi.getState());
      List<Document> attachments = (List<Document>) wfService.getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);
      assertNotNull(attachments);
      assertTrue(attachments.size() == 1);
      Document ach = (Document) attachments.get(0);
      assertTrue(ach.getName().equals("test.txt"));
      assertTrue(ach.getPath().equals("/testAttachTextFolderNameWithoutLeadingSlash/test.txt"));

   }
   @SuppressWarnings("unchecked")
   @Test
   public void testAttachTextByProvidingTheFullPath() throws Exception
   {
      camelContext.addRoutes(testAttachTextFileRoute());
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setProperty(ATTACHMENT_FILE_NAME, "test.txt");
      exchange.setProperty(ATTACHMENT_FOLDER_NAME, "/testAttachTextFile");
      exchange.getIn().setBody("hello world");

      attachTextFileProducerTemplate.send(exchange);
      attachProducerRouteResult.setExpectedMessageCount(1);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid1);

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance pi = wfService.getProcessInstance(piOid1);
      assertEquals(ProcessInstanceState.Active, pi.getState());
      List<Document> attachments = (List<Document>) wfService.getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);
      assertNotNull(attachments);
      assertTrue(attachments.size() == 1);
      Document ach = (Document) attachments.get(0);
      assertTrue(ach.getName().equals("test.txt"));
      assertTrue(ach.getPath().equals("/testAttachTextFile/test.txt"));

   }

   @SuppressWarnings("unchecked")
   @Test
   public void testAttachTextFileRouteUsingParametersFromUri() throws Exception
   {
      camelContext.addRoutes(testAttachTextFileRouteUsingParametersFromUriRoute());
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.getIn().setBody("hello world");
      exchange.getIn().setHeader("CamelFileNameOnly", "testFile.txt");

      attachTextFileRouteUsingParametersFromUriProducerTemplate.send(exchange);
      attachTextFileRouteUsingParametersFromUriRouteResult.setExpectedMessageCount(1);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid1);

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance pi = wfService.getProcessInstance(piOid1);
      assertEquals(ProcessInstanceState.Active, pi.getState());
      List<Document> attachments = (List<Document>) wfService.getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);
      assertNotNull(attachments);
      assertTrue(attachments.size() == 1);
      Document ach = (Document) attachments.get(0);
      assertTrue(ach.getName().equals("testFile.txt"));
      assertTrue(ach.getPath().equals("/myFolder/testFile.txt"));

   }

   @SuppressWarnings("unchecked")
   @Test
   public void testAttachBinaryFileRoute() throws Exception
   {
      camelContext.addRoutes(testAttachBinaryFileRouteDef());
   //   NotifyBuilder notify = new NotifyBuilder(camelContext).from("file:target/test-classes/binaryFiles").whenDone(1).create();

      NotifyBuilder notify = new NotifyBuilder(camelContext)
      .wereSentTo(ATTACHMENT_BINARY_ROUTE_END).whenDone(1)
      .create();
      notify.matchesMockWaitTime();
    //  attachBinaryFileRouteResult.setExpectedMessageCount(1);
      Exchange receivedMessage=attachBinaryFileRouteResult.getReceivedExchanges().get(0);
      Long piOid = receivedMessage.getIn().getHeader(MessageProperty.PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid);

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance pi = wfService.getProcessInstance(piOid);
      assertEquals(ProcessInstanceState.Active, pi.getState());
      List<Document> attachments = (List<Document>) wfService.getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);
      assertNotNull(attachments);
      assertTrue(attachments.size() == 1);
      Document ach = (Document) attachments.get(0);
      assertTrue(ach.getName().equals("test-excel.xlsx"));
     String folderName = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
      assertTrue(ach.getPath().equals(folderName+"/test-excel.xlsx"));
     // attachBinaryFileRouteResult.assertIsSatisfied();
   }


   public static RouteBuilder testAttachTextFileRoute()
   {

      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from(ATTACHMENT_ROUTE_BEGIN).to("ipp:authenticate:" + COMMAND_SET_CURRENT + "?user=motu&password=motu")
                  .to("ipp:process:start?processId=StartProcessAndAttachFile").to("ipp:process:continue")
                  .to("ipp:process:attach?" + ATTACHMENT_FILE_CONTENT + "=${body}").to(ATTACHMENT_ROUTE_END);
         }
      };
   }

   public static RouteBuilder testAttachTextFileRouteUsingParametersFromUriRoute()
   {

      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from(ATTACHMENT_PARAM_ROUTE_BEGIN)
                  .to("ipp:authenticate:" + COMMAND_SET_CURRENT + "?user=motu&password=motu")
                  .to("ipp:process:start?processId=StartProcessAndAttachFile")
                  .to("ipp:process:continue")
                  .to("ipp:process:attach?" + ATTACHMENT_FILE_NAME + "=testFile.txt&" + ATTACHMENT_FOLDER_NAME
                        + "=/myFolder&" + ATTACHMENT_FILE_CONTENT + "=${body}").to(ATTACHMENT_PARAM_ROUTE_END);
         }
      };
   }

   public static RouteBuilder testAttachBinaryFileRouteDef()
   {

      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from("file:target/test-classes/binaryFiles").id("AttachBinaryFileRoute")
                  .to("ipp:authenticate:" + COMMAND_SET_CURRENT + "?user=motu&password=motu")
                  .to("ipp:process:start?processId=StartProcessAndAttachFile")
                  .to("ipp:process:continue")
                  .to("ipp:process:attach?" + ATTACHMENT_FILE_NAME + "=${header.CamelFileNameOnly}&"+ ATTACHMENT_FILE_CONTENT + "=${body}").to(ATTACHMENT_BINARY_ROUTE_END);
         }
      };
   }
}
