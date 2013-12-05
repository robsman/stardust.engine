package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FILE_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FILE_NAME;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FOLDER_NAME;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.language.simple.SimpleLanguage;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

//import com.infinity.bpm.util.test.SpringTestUtils;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

@ContextConfiguration(locations = { "ProcessAttachementTest-context.xml",
		"classpath:carnot-spring-context.xml",
		"classpath:jackrabbit-jcr-context.xml"})
public class ProcessAttachementTest extends AbstractJUnit4SpringContextTests {
	private static final String FULL_ROUTE_BEGIN = "direct:fullRoute";
	private static final String FULL_ROUTE_END = "mock:fullRoute";
	private static final String ATTACHMENT_ROUTE_BEGIN = "direct:attachmentRoute";
	private static final String ATTACHMENT_ROUTE_END = "mock:endAttachementRoute";

	private static final String ATTACHMENT_PARAM_ROUTE_BEGIN = "direct:attachmentWithParamRoute";
	private static final String ATTACHMENT_PARAM_ROUTE_END = "mock:endAttachementWithParamRoute";

	private boolean initiated = false;
	@Resource
	CamelContext camelContext;
	@Resource
	private SpringTestUtils testUtils;

	@Produce(uri = FULL_ROUTE_BEGIN)
	protected ProducerTemplate fullRouteProducerTemplate;
	@EndpointInject(uri = FULL_ROUTE_END)
	protected MockEndpoint fullRouteResult;

	@Produce(uri = ATTACHMENT_ROUTE_BEGIN)
	protected ProducerTemplate attachProducerTemplate;
	@EndpointInject(uri = ATTACHMENT_ROUTE_END)
	protected MockEndpoint attachProducerRouteResult;

	@Produce(uri = ATTACHMENT_PARAM_ROUTE_BEGIN)
	protected ProducerTemplate attachWithParamProducerTemplate;
	@EndpointInject(uri = ATTACHMENT_PARAM_ROUTE_END)
	protected MockEndpoint attachWithParamProducerRouteResult;

	@Resource
	private ServiceFactoryAccess serviceFactoryAccess;

	@Before
	public void setUp() throws Exception {
		if (!initiated)
			setUpGlobal();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStartProcessAndAttachFile() throws Exception {
		Exchange exchange = new DefaultExchange(camelContext);
		exchange.setProperty(ATTACHMENT_FILE_CONTENT, "hello world");

		fullRouteProducerTemplate.send(exchange);
		fullRouteResult.setExpectedMessageCount(1);
		Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID,
				Long.class);
		assertNotNull(piOid1);

		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
		WorkflowService wfService = sf.getWorkflowService();
		ProcessInstance pi = wfService.getProcessInstance(piOid1);
		assertEquals(ProcessInstanceState.Active, pi.getState());
		List<Document> attachments = (List<Document>) wfService.getInDataPath(
				pi.getOID(), PROCESS_ATTACHMENTS);
		assertNotNull(attachments);
		assertTrue(attachments.size() == 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAttachFile() throws Exception {
		Exchange exchange = new DefaultExchange(camelContext);
		exchange.setProperty(ATTACHMENT_FILE_NAME, "test.txt");
		exchange.setProperty(ATTACHMENT_FOLDER_NAME, "example");
		exchange.getIn().setBody("hello world");

		attachProducerTemplate.send(exchange);
		attachProducerRouteResult.setExpectedMessageCount(1);
		Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID,
				Long.class);
		assertNotNull(piOid1);

		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
		WorkflowService wfService = sf.getWorkflowService();
		ProcessInstance pi = wfService.getProcessInstance(piOid1);
		assertEquals(ProcessInstanceState.Active, pi.getState());
		List<Document> attachments = (List<Document>) wfService.getInDataPath(
				pi.getOID(), PROCESS_ATTACHMENTS);
		assertNotNull(attachments);
		assertTrue(attachments.size() == 1);
		Document ach = (Document) attachments.get(0);
		assertTrue(ach.getName().equals("test.txt"));
		assertTrue(ach.getPath().equals("/example/test.txt"));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAttachWithParamFile() throws Exception {
		Exchange exchange = new DefaultExchange(camelContext);
		exchange.getIn().setBody("hello world");
		exchange.getIn().setHeader("CamelFileNameOnly", "testFile.txt");

		attachWithParamProducerTemplate.send(exchange);
		attachWithParamProducerRouteResult.setExpectedMessageCount(1);
		Long piOid1 = exchange.getIn().getHeader(PROCESS_INSTANCE_OID,
				Long.class);
		assertNotNull(piOid1);

		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
		WorkflowService wfService = sf.getWorkflowService();
		ProcessInstance pi = wfService.getProcessInstance(piOid1);
		assertEquals(ProcessInstanceState.Active, pi.getState());
		List<Document> attachments = (List<Document>) wfService.getInDataPath(
				pi.getOID(), PROCESS_ATTACHMENTS);
		assertNotNull(attachments);
		assertTrue(attachments.size() == 1);
		Document ach = (Document) attachments.get(0);
		assertTrue(ach.getName().equals("testFile.txt"));
		assertTrue(ach.getPath().equals("/myFolderFromHeader/testFile.txt"));

	}

	@DirtiesContext
	public void setUpGlobal() throws Exception {
		// initiate environment
		testUtils.setUpGlobal();
		initiated = true;
	}

	public static RouteBuilder createFullRoute() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(FULL_ROUTE_BEGIN)
						.to("ipp:authenticate:" + COMMAND_SET_CURRENT
								+ "?user=motu&password=motu")
						.to("ipp:process:start?processId=StartProcessAndAttachFile")
						.to(FULL_ROUTE_END);
				// .to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT );
			}
		};
	}

	public static RouteBuilder createattachementEndpointRoute() {

		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(ATTACHMENT_ROUTE_BEGIN)
						.to("ipp:authenticate:" + COMMAND_SET_CURRENT
								+ "?user=motu&password=motu")
						.to("ipp:process:start?processId=StartProcessAndAttachFile")
						.to("ipp:process:continue")
						.to("ipp:process:attach?" + ATTACHMENT_FILE_CONTENT
								+ "=${body}").to(ATTACHMENT_ROUTE_END);
				// .to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT );
			}
		};
	}

//	public static RouteBuilder createattachementEndpointParamRoute() {
//
//		return new RouteBuilder() {
//			@Override
//			public void configure() throws Exception {
//				from(ATTACHMENT_PARAM_ROUTE_BEGIN)
//						.to("ipp:authenticate:" + COMMAND_SET_CURRENT
//								+ "?user=motu&password=motu")
//						.to("ipp:process:start?processId=StartProcessAndAttachFile")
//						.to("ipp:process:continue")
//						.to("ipp:process:attach?" + ATTACHMENT_FILE_NAME
//								+ "=testFile.txt&" + ATTACHMENT_FOLDER_NAME
//								+ "=myFolder&" + ATTACHMENT_FILE_CONTENT
//								+ "=${body}").to(ATTACHMENT_PARAM_ROUTE_END);
//				// .to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT );
//			}
//		};
//	}
	
	public static RouteBuilder createattachementEndpointParamRoute() {

		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(ATTACHMENT_PARAM_ROUTE_BEGIN)
//				from("file:C:/Camel-IPP?move=.done")
						.convertBodyTo(String.class)
						.setHeader("folder", constant("myFolderFromHeader"))
						.to("ipp:authenticate:" + COMMAND_SET_CURRENT
								+ "?user=motu&password=motu")
						.to("ipp:process:start?processId=StartProcessAndAttachFile")
						.to("ipp:process:continue")
						.to("ipp:process:attach?" + ATTACHMENT_FILE_NAME						
								+ "=${header.CamelFileNameOnly}&" + ATTACHMENT_FOLDER_NAME
								+ "=${header.folder}&" + ATTACHMENT_FILE_CONTENT
								+ "=${body}").to(ATTACHMENT_PARAM_ROUTE_END);
				// .to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT );
			}
		};
	}

	
	public static RouteBuilder createattachementEndpointParamRouteDSL() {

		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(ATTACHMENT_PARAM_ROUTE_BEGIN)
						.to("ipp:authenticate:" + COMMAND_SET_CURRENT
								+ "?user=motu&password=motu")
						.to("ipp:process:start?processId=StartProcessAndAttachFile")
						.to("ipp:process:continue")
						.to("ipp:process:attach?" + ATTACHMENT_FILE_NAME
								+ "=testFile.txt&" + ATTACHMENT_FOLDER_NAME
								+ "=myFolder&" + ATTACHMENT_FILE_CONTENT
								+ "=${body}").to(ATTACHMENT_PARAM_ROUTE_END);
				// .to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT );
			}
		};
	}
	

}
