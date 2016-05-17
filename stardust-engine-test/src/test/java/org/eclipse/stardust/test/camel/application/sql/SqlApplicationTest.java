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
package org.eclipse.stardust.test.camel.application.sql;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestRtEnvException;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.AfterClass;
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
public class SqlApplicationTest extends AbstractCamelIntegrationTest
{
   private static final Logger trace = LogManager.getLogger(SqlApplicationTest.class.getName());

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String MODEL_ID = "SqlApplicationTestModel";

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @BeforeClass
   public static void setUpOnce()
   {
      createProjectTable();
   }

   @AfterClass
   public static void tearDownOnce()
   {
      dropProjectTable();
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testSelectAll() throws Exception
   {
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectAllProcess", null, true);
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

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testSelectWithSdtParam() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      Map<String, Object> projectParams = new HashMap<String, Object>();
      projectParams.put("LICENSE", "ASF");
      dataMap.put("projectParam", projectParams);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectWithSdtParamProcess", dataMap, true);
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

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @SuppressWarnings("rawtypes")
   @Test
   public void testSelectWithPrimitiveParam() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      String licenseParam = "ASF";
      dataMap.put("licenseParam", licenseParam);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectWithPrimitiveParamProcess", dataMap, true);
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

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testSelectAndReturnPrimitiveProcess() throws Exception
   {
      String idParam = "1";
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("idParam", idParam);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectAndReturnPrimitiveProcess", dataMap, true);
      String result = (String) sf.getWorkflowService().getInDataPath(pInstance.getOID(), "licesne");
      trace.info("get license" + result);
      assertNotNull(result);
      assertEquals("ASF", result);
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery
            .findAlive("{SqlApplicationTestModel}SelectAndReturnPrimitiveProcess");
      ActivityInstances activityInstances = sf.getQueryService().getAllActivityInstances(activityInstanceQuery);
      sf.getWorkflowService().activateAndComplete(activityInstances.get(0).getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testSelectAndReturnSdtProcess() throws Exception
   {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      String idParam = "1";
      dataMap.put("idParam", idParam);
      ProcessInstance pInstance = sf.getWorkflowService().startProcess("{SqlApplicationTestModel}SelectAndReturnSdtProcess", dataMap, true);
      ActivityInstanceStateBarrier.instance().awaitForId(pInstance.getOID(), "show");

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

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   @Test
   public void testInsertElementsProcess() throws Exception
   {
      sf.getWorkflowService().startProcess("{SqlApplicationTestModel}InsertElementsProcess", null, true);

      Connection con = testClassSetup.dataSource().getConnection();
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
      Connection con = testClassSetup.dataSource().getConnection();
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
      assertEquals("Ipp", licenseValueBeforeUpdate);
      String revertToInitialLicenseValueQuery = "Update PROJECTS set LICENSE='XXX' where ID=3";
      stmt.executeUpdate(revertToInitialLicenseValueQuery);

      ProcessInstanceStateBarrier.instance().await(pInstance.getOID(), ProcessInstanceState.Completed);
   }

   private static void createProjectTable()
   {
      try
      {
         Connection connection = testClassSetup.dataSource().getConnection();
         Statement stmt = connection.createStatement();

         stmt.executeUpdate("create table projects (id integer primary key, project varchar(10), license varchar(5))");

         String query1 = "insert into projects values (1, 'Camel', 'ASF')";
         String query2 = "insert into projects values (2, 'AMQ', 'ASF')";
         String query3 = "insert into projects values (3, 'Linux', 'XXX')";
         stmt.execute(query1);
         stmt.execute(query2);
         stmt.execute(query3);
         trace.info("Table created.");
         stmt.close();
         connection.close();
      }
      catch (Exception e)
      {
         throw new TestRtEnvException("Unable to create table 'projects'.", TestRtEnvAction.DB_SETUP);
      }
   }

   private static void dropProjectTable()
   {
      try
      {
         Connection connection = testClassSetup.dataSource().getConnection();
         Statement stmt = connection.createStatement();

         stmt.executeUpdate("drop table projects");
      }
      catch (Exception e)
      {
         throw new TestRtEnvException("Unable to drop table 'projects'.", TestRtEnvAction.DB_TEARDOWN);
      }
   }
}
