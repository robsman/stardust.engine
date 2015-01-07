package org.eclipse.stardust.engine.extensions.camel.application.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;

public class SqlApplicationTest
{
   private static final Logger trace = LogManager.getLogger(SqlApplicationTest.class.getName());
   private static ClassPathXmlApplicationContext ctx;

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
      createProjectTable();
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testSelectAll() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectAllProcess", null, true);
      Thread.sleep(5000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "projects");
      trace.debug("projects result = " + result);
      assertNotNull(result);
      Map< ? , ? > project1 = (Map< ? , ? >) ((List) result.get("projects")).get(0);
      Map< ? , ? > project2 = (Map< ? , ? >) ((List) result.get("projects")).get(1);
      Map< ? , ? > project3 = (Map< ? , ? >) ((List) result.get("projects")).get(2);
      assertTrue(result.get("projects") instanceof List);
      assertEquals("1", project1.get("ID"));
      assertEquals("Camel", project1.get("PROJECT"));
      assertEquals("ASF", project1.get("LICENSE"));
      assertEquals("2", project2.get("ID"));
      assertEquals("AMQ", project2.get("PROJECT"));
      assertEquals("ASF", project2.get("LICENSE"));
      assertEquals("3", project3.get("ID"));
      assertEquals("Linux", project3.get("PROJECT"));
      assertEquals("XXX", project3.get("LICENSE"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{SqlApplicationTestModel}SelectAllProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testSelectWithSdtParam() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> projectParams = new HashMap<String, Object>();
      projectParams.put("LICENSE", "ASF");
      dataMap.put("projectParam", projectParams);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectWithSdtParamProcess", dataMap, true);
      Thread.sleep(3000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "projects");
      trace.debug("projects result = " + result);
      trace.debug("projects result list size = " + ((List) result.get("projects")).size());
      assertNotNull(result);
      assertEquals(2, ((List) result.get("projects")).size());
      Map< ? , ? > project1 = (Map< ? , ? >) ((List) result.get("projects")).get(0);
      Map< ? , ? > project2 = (Map< ? , ? >) ((List) result.get("projects")).get(1);
      trace.debug("project1 : " + project1);
      trace.debug("project2 : " + project2);
      assertTrue(result.get("projects") instanceof List);
      assertEquals("1", project1.get("ID"));
      assertEquals("Camel", project1.get("PROJECT"));
      assertEquals("ASF", project1.get("LICENSE"));
      assertEquals("2", project2.get("ID"));
      assertEquals("AMQ", project2.get("PROJECT"));
      assertEquals("ASF", project2.get("LICENSE"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{SqlApplicationTestModel}SelectWithSdtParamProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testSelectWithPrimitiveParam() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      String licenseParam = "ASF";
      dataMap.put("licenseParam", licenseParam);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectWithPrimitiveParamProcess", dataMap, true);
      Thread.sleep(3000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(),
            "projectsWithPrimitiveParam");
      trace.debug("projects result = " + result);
      trace.debug("projects result list size = " + ((List) result.get("projects")).size());
      assertNotNull(result.get("projects"));
      assertEquals(2, ((List) result.get("projects")).size());
      Map< ? , ? > project1 = (Map< ? , ? >) ((List) result.get("projects")).get(0);
      Map< ? , ? > project2 = (Map< ? , ? >) ((List) result.get("projects")).get(1);
      trace.debug("project1 : " + project1);
      trace.debug("project2 : " + project2);
      assertTrue(result.get("projects") instanceof List);
      assertEquals("1", project1.get("ID"));
      assertEquals("Camel", project1.get("PROJECT"));
      assertEquals("ASF", project1.get("LICENSE"));
      assertEquals("2", project2.get("ID"));
      assertEquals("AMQ", project2.get("PROJECT"));
      assertEquals("ASF", project2.get("LICENSE"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{SqlApplicationTestModel}SelectWithPrimitiveParamProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

   }

   @Test
   public void testSelectAndReturnPrimitiveProcess() throws Exception
   {
      String idParam = "1";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("idParam", idParam);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectAndReturnPrimitiveProcess", dataMap, true);
      Thread.sleep(3000);
      String result = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "licesne");
      trace.info("get license" + result);
      assertNotNull(result);
      assertEquals("ASF", result);
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{SqlApplicationTestModel}SelectAndReturnPrimitiveProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

   }

   @Test
   public void testSelectAndReturnSdtProcess() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      String idParam = "1";
      dataMap.put("idParam", idParam);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectAndReturnSdtProcess", dataMap, true);
      Thread.sleep(3000);
      Map< ? , ? > result = (Map< ? , ? >) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "project");
      trace.info("get project" + result);
      assertNotNull(result);
      assertEquals("1", result.get("ID"));
      assertEquals("Camel", result.get("PROJECT"));
      assertEquals("ASF", result.get("LICENSE"));
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{SqlApplicationTestModel}SelectAndReturnSdtProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

   }

   @Test
   public void testInsertElementsProcess() throws Exception
   {
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}InsertElementsProcess", null, true);
      Thread.sleep(3000);
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
      Connection con = DriverManager.getConnection("jdbc:derby:target/ipp-test-DB", "carnot", "ag");
      Statement stmt = con.createStatement();
      String selectAllQuery = "select * from projects";
      ResultSet rs = stmt.executeQuery(selectAllQuery);
      int numberOfRows = 0;
      con.setAutoCommit(true);
      rs = stmt.executeQuery(selectAllQuery);
      numberOfRows = 0;
      String id = null;
      String project = null;
      String license = null;

      while (rs.next())
      {
         numberOfRows++;
         if ("4".equalsIgnoreCase(rs.getString("ID")))
         {
            id = rs.getString("ID");
            project = rs.getString("PROJECT");
            license = rs.getString("LICENSE");
         }
      }
      assertEquals("4", id);
      assertEquals("Felix", project);
      assertEquals("ASF", license);
      assertEquals(4, numberOfRows);

      String deleteQuery = "delete FROM projects where ID=4";
      stmt.executeUpdate(deleteQuery);
   }

   @Test
   public void testUpdateElementsProcess() throws Exception
   {

      Thread.sleep(3000);
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
      Connection con = DriverManager.getConnection("jdbc:derby:target/ipp-test-DB", "carnot", "ag");

      Statement stmt = con.createStatement();
      String selectLinuxProjectQuery = "select * from projects Where ID=3";
      ResultSet rs = stmt.executeQuery(selectLinuxProjectQuery);
      String licenseValueBeforeUpdate = null;
      while (rs.next())
      {
         trace.debug("project = " + rs.getString("PROJECT"));
         trace.debug("license intital value= " + rs.getString("LICENSE"));
         licenseValueBeforeUpdate = rs.getString("LICENSE");
      }
      assertNotNull(licenseValueBeforeUpdate);
      assertEquals("XXX", licenseValueBeforeUpdate);
      Thread.sleep(1000);
      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}UpdateElementsProcess", null, true);
      con.setAutoCommit(true);
      rs = stmt.executeQuery(selectLinuxProjectQuery);
      while (rs.next())
      {
         trace.debug("project = " + rs.getString("PROJECT"));
         trace.debug("license updated value= " + rs.getString("LICENSE"));
         licenseValueBeforeUpdate = rs.getString("LICENSE");
      }
      assertNotNull(licenseValueBeforeUpdate);
      assertEquals("test", licenseValueBeforeUpdate);
      String revertToInitialLicenseValueQuery = "Update PROJECTS set LICENSE='XXX' where ID=3";
      stmt.executeUpdate(revertToInitialLicenseValueQuery);

   }

   static void createProjectTable()
   {
      try
      {

         Connection connection = DriverManager.getConnection("jdbc:derby:target/ipp-test-DB", "carnot", "ag");
         Statement sta = connection.createStatement();

         int count = sta
               .executeUpdate("create table projects (id integer primary key, project varchar(10), license varchar(5))");

         String query1 = "insert into projects values (1, 'Camel', 'ASF')";
         String query2 = "insert into projects values (2, 'AMQ', 'ASF')";
         String query3 = "insert into projects values (3, 'Linux', 'XXX')";
         sta.execute(query1);
         sta.execute(query2);
         sta.execute(query3);
         trace.info("Table created.");
         sta.close();
         connection.close();
      }
      catch (Exception e)
      {
         System.err.println("Exception: " + e.getMessage());
      }
   }
}
