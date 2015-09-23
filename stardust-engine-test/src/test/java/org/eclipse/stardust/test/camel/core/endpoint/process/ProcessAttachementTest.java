/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.core.endpoint.process;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FILE_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FILE_NAME;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FOLDER_NAME;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class ProcessAttachementTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String[] MODEL_IDS = { "GetPropertiesInvokerModel", "PropertiesProducerModel", "ExternalXSDModel", "ProcessContinueModel", "CamelTestModel" };

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

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

   @BeforeClass
   public static void setUpOnce() throws Exception
   {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);

      attachTextFileProducerTemplate = camelContext.createProducerTemplate();
      attachTextFileProducerTemplate.setDefaultEndpointUri(ATTACHMENT_ROUTE_BEGIN);

      attachTextFileRouteUsingParametersFromUriProducerTemplate = camelContext.createProducerTemplate();
      attachTextFileRouteUsingParametersFromUriProducerTemplate.setDefaultEndpointUri(ATTACHMENT_PARAM_ROUTE_BEGIN);

      attachProducerRouteResult = camelContext.getEndpoint(ATTACHMENT_ROUTE_END, MockEndpoint.class);
      attachTextFileRouteUsingParametersFromUriRouteResult = camelContext.getEndpoint(ATTACHMENT_PARAM_ROUTE_END, MockEndpoint.class);
      attachBinaryFileRouteResult= camelContext.getEndpoint(ATTACHMENT_BINARY_ROUTE_END, MockEndpoint.class);

      camelContext.addRoutes(testAttachTextFileRoute());
      camelContext.addRoutes(testAttachTextFileRouteUsingParametersFromUriRoute());
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testAttachTextFolderNameWithoutLeadingSlash() throws Exception
   {
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setProperty(ATTACHMENT_FILE_NAME, "test.txt");
      exchange.setProperty(ATTACHMENT_FOLDER_NAME, "testAttachTextFolderNameWithoutLeadingSlash");
      exchange.getIn().setBody("hello world");

      attachTextFileProducerTemplate.send(exchange);
      attachProducerRouteResult.setExpectedMessageCount(1);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid1);

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
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.setProperty(ATTACHMENT_FILE_NAME, "test.txt");
      exchange.setProperty(ATTACHMENT_FOLDER_NAME, "/testAttachTextFile");
      exchange.getIn().setBody("hello world");

      attachTextFileProducerTemplate.send(exchange);
      attachProducerRouteResult.setExpectedMessageCount(1);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid1);

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
      Exchange exchange = new DefaultExchange(camelContext);
      exchange.getIn().setBody("hello world");
      exchange.getIn().setHeader("CamelFileNameOnly", "testFile.txt");

      attachTextFileRouteUsingParametersFromUriProducerTemplate.send(exchange);
      attachTextFileRouteUsingParametersFromUriRouteResult.setExpectedMessageCount(1);
      Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid1);

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
      NotifyBuilder notify = new NotifyBuilder(camelContext)
         .wereSentTo(ATTACHMENT_BINARY_ROUTE_END).whenDone(1)
         .create();

      camelContext.addRoutes(testAttachBinaryFileRouteDef());

      boolean success = notify.matchesMockWaitTime();
      if ( !success)
      {
         throw new TimeoutException();
      }

      Exchange receivedMessage=attachBinaryFileRouteResult.getReceivedExchanges().get(0);
      Long piOid = receivedMessage.getIn().getHeader(MessageProperty.PROCESS_INSTANCE_OID, Long.class);
      assertNotNull(piOid);

      ProcessInstanceStateBarrier.instance().await(piOid, ProcessInstanceState.Active);
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance pi = wfService.getProcessInstance(piOid);

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
            from("file:target/test-classes/binaryFiles?noop=true&fileName=test-excel.xlsx").id("AttachBinaryFileRoute")
                  .to("ipp:authenticate:" + COMMAND_SET_CURRENT + "?user=motu&password=motu")
                  .to("ipp:process:start?processId=StartProcessAndAttachFile")
                  .to("ipp:process:continue")
                  .to("ipp:process:attach?" + ATTACHMENT_FILE_NAME + "=${header.CamelFileNameOnly}&"+ ATTACHMENT_FILE_CONTENT + "=${body}").to(ATTACHMENT_BINARY_ROUTE_END);
         }
      };
   }
}
