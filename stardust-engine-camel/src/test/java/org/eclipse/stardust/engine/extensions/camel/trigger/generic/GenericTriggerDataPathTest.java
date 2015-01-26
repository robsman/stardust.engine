package org.eclipse.stardust.engine.extensions.camel.trigger.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.component.mock.MockEndpoint;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class GenericTriggerDataPathTest
{
   protected static MockEndpoint resultEndpoint;
   private static final Logger trace = LogManager.getLogger(GenericTriggerDataPathTest.class.getName());
   private static ClassPathXmlApplicationContext ctx;

   @Resource
   private static ServiceFactoryAccess serviceFactoryAccess;
   
   @BeforeClass
   public static void beforeClass() {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "org/eclipse/stardust/engine/extensions/camel/common/SharedTestContext.xml",
            "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml",
            "classpath:default-camel-context.xml"});
      serviceFactoryAccess = (ServiceFactoryAccess) ctx.getBean("ippServiceFactoryAccess");
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testDataPath() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      String firstName = "Marcelo";
      String lastName = "Tom";

      String street = "1234 Main Street";
      String city = "London";

      String firstFieldOfListSdts = "Email 1 : marcelo.tom@test.com";
      String secondFieldOfListSdts = "Email 2 : marcelo@work.com";

      String anotherFieldOfListSdts = "anotherFieldOfListSdts1InC";

      String firstElementOflistOfText = "Phone 1: +245142145147";
      String secondElementOfListOfString = "Phone 2: +95554564564";

      String content = "Software Developer";

      dataMap.put("firstName", firstName);
      dataMap.put("lastName", lastName);

      dataMap.put("street", street);
      dataMap.put("city", city);

      dataMap.put("FirstFieldOfListSdts", firstFieldOfListSdts);
      dataMap.put("SecondFieldOfListSdts", secondFieldOfListSdts);

      dataMap.put("anotherFieldOfListSdts", anotherFieldOfListSdts);

      dataMap.put("firstElementOflistOfText", firstElementOflistOfText);
      dataMap.put("secondElementOfListOfString", secondElementOfListOfString);
      dataMap.put("Content", content);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      sf.getWorkflowService().startProcess("{GenericTriggerDataPathTestModel}TestGenericTriggerDataPath", dataMap, true);

      ProcessInstances pis = sf.getQueryService().getAllProcessInstances(
            ProcessInstanceQuery.findAlive("{GenericTriggerDataPathTestModel}ConsumerProcess"));
      ProcessInstance pi = pis.get(0);
      Map< ? , ? > response = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pi.getOID(), "Person");

      trace.info("get Person : " + response);
      Object response1 = sf.getWorkflowService().getInDataPath(pi.getOID(), "MessageBody");
      trace.info("get MessageBody : " + response1);
      assertNotNull(response1);
      assertTrue(response1 instanceof String);
      assertEquals("Software Developer", response1);

      assertNotNull(response);
      assertTrue(response instanceof Map);
      assertEquals(firstName, response.get("firstName"));
      assertEquals(lastName, response.get("lastName"));

      assertTrue(response.get("address") instanceof Map);
      trace.debug("address =" + response.get("address"));
      Map< ? , ? > address = (Map< ? , ? >) response.get("address");
      assertNotNull(address);
      assertTrue(address instanceof Map);
      assertEquals(city, address.get("city"));
      assertEquals(street, address.get("street"));

      List listOfSdt = (List) response.get("simpleListOfSdts");
      assertTrue(listOfSdt instanceof List);
      assertEquals(firstFieldOfListSdts, ((Map< ? , ? >) listOfSdt.get(0)).get("A"));
      assertEquals(secondFieldOfListSdts, ((Map< ? , ? >) listOfSdt.get(1)).get("A"));

      List listOfString = (List) response.get("listOfString");
      assertTrue(listOfString instanceof List);
      assertEquals(firstElementOflistOfText, listOfString.get(0));
      assertEquals(secondElementOfListOfString, listOfString.get(1));
      
      sf.getWorkflowService().activateNextActivityInstanceForProcessInstance(pi.getOID());

   }
}
