package org.eclipse.stardust.engine.extensions.camel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = { "classpath:default-camel-context.xml",
		"CamelTriggerTest-context.xml", "classpath:carnot-spring-context.xml",
		"classpath:jackrabbit-jcr-context.xml" })
public class CamelTriggerTest extends AbstractJUnit4SpringContextTests {

	@Resource
	CamelContext camelContext;
	@Resource
	private SpringTestUtils testUtils;
	@Resource
	private ServiceFactoryAccess serviceFactoryAccess;

	@EndpointInject(uri = "mock:result", context = "defaultCamelContext")
	protected MockEndpoint resultEndpoint;
	
	@Before
	public void setUp() {

		ClassPathResource resource = new ClassPathResource(
				"models/CamelTriggerTestModel.xpdl");
		this.testUtils.setModelFile(resource);

		try {
			this.testUtils.deployModel();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@After
	public void tearDown() {

		try {
			this.serviceFactoryAccess.getDefaultServiceFactory()
					.getAdministrationService().cleanupRuntimeAndModels();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	@Test
	public void testRouteUpdateOnCVChange() throws Exception {

		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

		ConfigurationVariables cvs = sf.getAdministrationService()
				.getConfigurationVariables("CamelTriggerTestModel");

		List<ConfigurationVariable> listOfCvs = cvs.getConfigurationVariables();

		for (Iterator<ConfigurationVariable> i = listOfCvs.iterator(); i.hasNext();)
		{
			ConfigurationVariable cv = i.next();
			if (cv.getName().equals("myConfVar"))
			{
				cv.setValue("myNewValue");
			}
		}

		cvs.setConfigurationVariables(listOfCvs);
		sf.getAdministrationService().saveConfigurationVariables(cvs, false);
	}

	@Test
	public void testStartProcessWithoutData() throws Exception {

		ProducerTemplate template = new DefaultProducerTemplate(this.camelContext);
		template.start();
		
		Exchange exchange = new DefaultExchange(this.camelContext);
		
		exchange = template.send("direct:testStartProcessWithoutData", exchange);
		long oid = (Long) exchange.getIn().getHeader(CamelConstants.MessageProperty.PROCESS_INSTANCE_OID);
		
		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();

		ProcessInstance pInstance = sf.getWorkflowService().getProcessInstance(oid);
		assertNotNull(pInstance);

	}

}
