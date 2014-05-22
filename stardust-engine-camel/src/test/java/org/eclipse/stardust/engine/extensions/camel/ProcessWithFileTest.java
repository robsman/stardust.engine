package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class ProcessWithFileTest {
	private static final Logger trace = LogManager
			.getLogger(ProcessWithFileTest.class.getName());
	private static ClassPathXmlApplicationContext ctx;
	{
		ctx = new ClassPathXmlApplicationContext(
				new String[] {
						"org/eclipse/stardust/engine/extensions/camel/ProcessWithFileTest-context.xml",
						"classpath:carnot-spring-context.xml",
						"classpath:jackrabbit-jcr-context.xml",
						"classpath:default-camel-context.xml" });
		camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
		testUtils = (SpringTestUtils) ctx.getBean("ippTestUtils");
		serviceFactoryAccess = (ServiceFactoryAccess) ctx
				.getBean("ippServiceFactoryAccess");
		initialize();
	}

	private static void initialize() {
		ClassPathResource resource = new ClassPathResource(
				"models/FileModelTest.xpdl");
		testUtils.setModelFile(resource);
		File dir = new File("./target/FileDirectory");
		dir.mkdirs();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		try {
			testUtils.deployModel();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Resource
	private static CamelContext camelContext;

	@Resource
	private static SpringTestUtils testUtils;

	@Resource
	private static ServiceFactoryAccess serviceFactoryAccess;

	@Test
	public void testPrimitiveDataWithFileTrigger() throws Exception {
		createFile("./target/FileDirectory/PD","primitiveDataFile.txt",	"primitiveData content from test class");
		Thread.sleep(5000);
		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
		ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
				ProcessInstanceQuery
						.findAlive("{FileModelTest}PrimitiveDataProcess"));
		if(pis.size()>1)
         throw new RuntimeException("Please clean the audit Trial");
		ProcessInstance pi = pis.get(0);
		Object response = sf.getWorkflowService().getInDataPath(pi.getOID(),"FileContent");
		trace.info("FileContent = " + response);
		assertNotNull(response);
		assertTrue(response instanceof String);
		assertEquals("primitiveData content from test class", response.toString());
		ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findAlive("{FileModelTest}PrimitiveDataProcess");
		ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
		sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
	}

	
	@Test
	public void testStructuredDataWithFileTrigger() throws Exception {
		createFile("./target/FileDirectory/SDT", "Person.xml","<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?><person><FirstName>FN</FirstName><LastName>LN</LastName></person>");
		Thread.sleep(5000);
		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
		ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
				ProcessInstanceQuery
						.findAlive("{FileModelTest}StructuredDataProcess"));
		if(pis.size()>1)
		   throw new RuntimeException("Please clean the audit Trial");
		ProcessInstance pi = pis.get(0);
		Object firstName = sf.getWorkflowService().getInDataPath(pi.getOID(),"FirstName");
		trace.info("FirstName = " + firstName);
		assertNotNull(firstName);
		assertTrue(firstName instanceof String);
		assertEquals("FN", firstName.toString());
		Object lastName = sf.getWorkflowService().getInDataPath(pi.getOID(),"LastName");
		trace.info("LastName = " + lastName);
		assertNotNull(lastName);
		assertTrue(lastName instanceof String);
		assertEquals("LN", lastName.toString());
		ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findAlive("{FileModelTest}StructuredDataProcess");
		ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
		sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
	}
	
	@Test
	public void testDocumentDataWithFileTrigger() throws Exception {
		createFile("./target/FileDirectory/Document","DocumentFile.txt","Document File Content");
		Thread.sleep(5000);
		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
		ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
				ProcessInstanceQuery
						.findAlive("{FileModelTest}DocumentProcess"));
		if(pis.size()>1)
         throw new RuntimeException("Please clean the audit Trial");
		ProcessInstance pi = pis.get(0);
		Object documentFileContent = sf.getWorkflowService().getInDataPath(pi.getOID(),"DocumentFile");
		Document document = (Document) sf.getWorkflowService().getInDataPath(pi.getOID(),"DocumentFile");
	    byte[] byteDocumentContent =  sf.getDocumentManagementService().retrieveDocumentContent(document.getId());
	    String documentContent = new String(byteDocumentContent, "UTF-8");
	    trace.debug("documentContent = "+documentContent);
		assertNotNull(document);
		assertTrue(documentFileContent instanceof Document);
		assertTrue("DocumentFile.txt".equals(document.getName()));
		assertTrue("Document File Content".equals(documentContent));		
		ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findAlive("{FileModelTest}DocumentProcess");
		ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
		sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
	}

	@Test
	public void testSplitWithFileTrigger() throws Exception {
		createFile("./target/FileDirectory/SplitTest","SplitFile.txt",	"line1\nline2");
		Thread.sleep(5000);
		ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
		ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
				ProcessInstanceQuery
						.findAlive("{FileModelTest}SplitProcess"));
		ProcessInstance pi1 = pis.get(0);
		Object responseLine1 = sf.getWorkflowService().getInDataPath(pi1.getOID(),"SplitFile");
		trace.info("Split File Content = " + responseLine1);
		assertNotNull(responseLine1);
		assertTrue(responseLine1 instanceof String);
		assertEquals("line1", responseLine1.toString());
		
		ProcessInstance pi2 = pis.get(1);
		Object responseLine2 = sf.getWorkflowService().getInDataPath(pi2.getOID(),"SplitFile");
		trace.info("Split File Content = " + responseLine2);
		assertNotNull(responseLine2);
		assertTrue(responseLine2 instanceof String);
		assertEquals("line2", responseLine2.toString());
		
		ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findAlive("{FileModelTest}SplitProcess");
		ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
		
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
		sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);
		trace.info("activityInstance state = "+activityInstances.get(0).getState());
		
		trace.info("activityInstance state = "+activityInstances.get(1).getState());
		sf.getWorkflowService().activateAndComplete(activityInstances.get(1).getOID(), null, null);
		trace.info("activityInstance state = "+activityInstances.get(1).getState());
	}
	
	private static void createFile(String path, String fileName, String content)
	throws IOException {
		File dir = new File(path);
		String loc = dir.getCanonicalPath() + File.separator + fileName;
		FileWriter fstream = new FileWriter(loc, true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(content);
		out.close();
		}
}
