/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.test.archiver;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.cli.sysconsole.Archiver;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains functional tests for archiving audittrails containing data cluster.
 * </p>
 * 
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class ArchiveDataClusterTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   public static final String MODEL_NAME = "DataClusterModel";

   private static final String STRING_DATA_VAL = "TestValue";

   private static final String SYSOP = "sysop";

   private static final String DC_TABLE_1 = "dv_mqt01";

   private static final String DC_TABLE_2 = "dv_mqt02";

   private static final String DC_TABLE_3 = "dv_mqt03";

   private static final String DB_SCHEMA = "PUBLIC";

   private static final String LONG_DATA = "aLong";

   private static final String INT_DATA = "anInt";

   private static final String STRING_DATA = "aString";

   private static final String DOUBLE_DATA = "aDouble";

   private static final String PROCESS_DEF_ID_1 = "ProcessDefinition1";

   private static final String PROCESS_DEF_ID_2 = "ProcessDefinition2";

   private static final String PROCESS_DEF_ID_3 = "ProcessDefinition3";

   private static final String PROCESS_DEF_ID_4 = "ProcessDefinition4";

   @Rule
   public ExpectedException exception = ExpectedException.none();

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   private static final String DATA_CLUSTER_CONFIG = "data-cluster.xml";

   @ClassRule
   public static final DataClusterTestClassSetup testClassSetup = new DataClusterTestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private WorkflowService wfService;

   private QueryService queryService;

   private Connection archiveConnection;

   private Connection connection;

   private AdministrationService adminService;

   @Before
   public void setup() throws Exception
   {
      wfService = serviceFactory.getWorkflowService();
      queryService = serviceFactory.getQueryService();
      adminService = serviceFactory.getAdministrationService();
      archiveConnection = ArchiveTestUtils
            .createArchiveAuditTrailWithDataCluster(DATA_CLUSTER_CONFIG);
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      connection = session.getConnection();
   }

   @After
   public void tearDown() throws SQLException
   {
      ArchiveTestUtils.dropArchiveAuditTrail(archiveConnection);
      archiveConnection.close();
      connection.close();
   }

   @Test
   public void testArchiveDeadProcessWithDC() throws Exception
   {
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      Archiver archiver = new Archiver(true, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      ArrayList<Long> piOids = new ArrayList<Long>();
      piOids.add(process.getOID());
      archiver.archiveDeadProcesses(piOids);
      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process.getOID();
      ResultSet result = srcStmt.executeQuery(srcString);
      assertFalse(result.next());
      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process.getOID();
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());
      exception.expect(ObjectNotFoundException.class);
      findCompletedProcessInstance(PROCESS_DEF_ID_1);
   }

   @Test
   public void testArchiveDeadProcessWithDC2() throws Exception
   {
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_4, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_4);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(DOUBLE_DATA, new Double(4.5));
      wfService.activateAndComplete(oid, null, datas);
      Archiver archiver = new Archiver(true, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      ArrayList<Long> piOids = new ArrayList<Long>();
      piOids.add(process.getOID());
      archiver.archiveDeadProcesses(piOids);
      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_2)
            + " WHERE processinstance=" + process.getOID();
      ResultSet result = srcStmt.executeQuery(srcString);
      assertFalse(result.next());
      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_2)
            + " WHERE processinstance=" + process.getOID();
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());
      exception.expect(ObjectNotFoundException.class);
      findCompletedProcessInstance(PROCESS_DEF_ID_4);
   }

   @Test
   public void testArchiveProcessesSynchronizeDataClusterNoInconsistencies()
         throws Exception
   {
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process2 = wfService.startProcess(PROCESS_DEF_ID_4, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_4);
      datas = new HashMap<String, Object>();
      datas.put(DOUBLE_DATA, new Double(4.5));
      wfService.activateAndComplete(oid, null, datas);
      Archiver archiver = new Archiver(true, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      ArrayList<Long> piOids = new ArrayList<Long>();
      piOids.add(process.getOID());
      piOids.add(process2.getOID());
      archiver.archiveDeadProcesses(piOids);
      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process.getOID();
      ResultSet result = srcStmt.executeQuery(srcString);
      assertFalse(result.next());
      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process.getOID();
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());
      srcStmt = connection.createStatement();
      srcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_2)
            + " WHERE processinstance=" + process.getOID();
      result = srcStmt.executeQuery(srcString);
      assertFalse(result.next());
      arcStmt = archiveConnection.createStatement();
      arcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_2)
            + " WHERE processinstance=" + process.getOID();
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());

      ArchiveTestUtils.setArchiveAudittrailProperties();
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies.\r\n"));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Synchronized data cluster table: "
            + ArchiveTestUtils.ARC_SCHEMA + "." + DC_TABLE_1
            + ". There were no inconsistencies to be resolved.\r\n"));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies.\r\n"));
      ArchiveTestUtils.resetProperties();
   }

   @Test
   public void testArchiveDeadDataWithBothDC() throws Exception
   {
      AuditTrailPartitionBean defaultPartitionBean = AuditTrailPartitionBean
            .findById(PredefinedConstants.DEFAULT_PARTITION_ID);
      Parameters.instance().set(SecurityProperties.CURRENT_PARTITION,
            defaultPartitionBean);
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process2 = wfService.startProcess(PROCESS_DEF_ID_4, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_4);
      datas = new HashMap<String, Object>();
      datas.put(DOUBLE_DATA, new Double(4.5));
      wfService.activateAndComplete(oid, null, datas);
      Archiver archiver = new Archiver(false, ArchiveTestUtils.ARC_SCHEMA, 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      ArrayList<Long> piOids = new ArrayList<Long>();
      piOids.add(process.getOID());
      piOids.add(process2.getOID());
      archiver.archiveDeadData(new String[] {
            "{DataClusterModel}" + STRING_DATA, "{DataClusterModel}" + INT_DATA,
            "{DataClusterModel}" + LONG_DATA, "{DataClusterModel}" + DOUBLE_DATA}, null,
            0);

      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_1)
            + " WHERE nval_anint IS NULL AND sval_astring IS NULL AND nval_along IS NULL";
      ResultSet result = srcStmt.executeQuery(srcString);
      assertTrue(result.next());
      assertEquals(2, result.getInt(1));

      srcStmt = connection.createStatement();
      srcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_2)
            + " WHERE dval_adouble IS NULL";
      result = srcStmt.executeQuery(srcString);
      assertTrue(result.next());
      assertEquals(2, result.getInt(1));
   }

   @Test
   public void testArchiveDeadProcessWithDCNoBackup() throws Exception
   {
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      Archiver archiver = new Archiver(false, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      ArrayList<Long> piOids = new ArrayList<Long>();
      piOids.add(process.getOID());
      archiver.archiveDeadProcesses(piOids);
      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process.getOID();
      ResultSet result = srcStmt.executeQuery(srcString);
      assertFalse(result.next());
      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process.getOID();
      result = arcStmt.executeQuery(arcString);
      assertFalse(result.next());
      exception.expect(ObjectNotFoundException.class);
      findCompletedProcessInstance(PROCESS_DEF_ID_1);
   }

   @Test
   public void testArchiveDeadProcessesWithDC() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);

      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 234);
      datas.put(LONG_DATA, 67845);
      wfService.activateAndComplete(oid, null, datas);

      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 345);
      datas.put(LONG_DATA, 87654);
      wfService.activateAndComplete(oid, null, datas);

      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);

      Archiver archiver = new Archiver(true, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      archiver.archiveDeadProcesses(null, 0);
      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_1);
      ResultSet result = srcStmt.executeQuery(srcString);
      assertTrue(result.next());
      assertEquals(2, result.getInt(1));
      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_1);
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());
      assertEquals(3, result.getInt(1));
      exception.expect(ObjectNotFoundException.class);
      findCompletedProcessInstance(PROCESS_DEF_ID_1);
   }

   @Test
   public void testArchiveDeadModelsWithDC() throws Exception
   {
      AuditTrailPartitionBean defaultPartitionBean = AuditTrailPartitionBean
            .findById(PredefinedConstants.DEFAULT_PARTITION_ID);
      Parameters.instance().set(SecurityProperties.CURRENT_PARTITION,
            defaultPartitionBean);
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);

      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 234);
      datas.put(LONG_DATA, 67845);
      wfService.activateAndComplete(oid, null, datas);

      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 345);
      datas.put(LONG_DATA, 87654);
      wfService.activateAndComplete(oid, null, datas);

      RtEnvHome.deploy(adminService, null, MODEL_NAME);
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 12);
      datas.put(LONG_DATA, 76);
      wfService.activateAndComplete(oid, null, datas);

      RtEnvHome.deploy(adminService, null, MODEL_NAME);

      Archiver archiver = new Archiver(true, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      archiver.archiveDeadModels(0);
      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_1);
      ResultSet result = srcStmt.executeQuery(srcString);
      assertTrue(result.next());
      assertEquals(0, result.getInt(1));
      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_1);
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());
      assertEquals(4, result.getInt(1));
      exception.expect(ObjectNotFoundException.class);
      findCompletedProcessInstance(PROCESS_DEF_ID_1);
   }

   @Test
   public void testArchiveDeadModelsWithDCNoBackup() throws Exception
   {
      AuditTrailPartitionBean defaultPartitionBean = AuditTrailPartitionBean
            .findById(PredefinedConstants.DEFAULT_PARTITION_ID);
      Parameters.instance().set(SecurityProperties.CURRENT_PARTITION,
            defaultPartitionBean);
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);

      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 234);
      datas.put(LONG_DATA, 67845);
      wfService.activateAndComplete(oid, null, datas);

      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 345);
      datas.put(LONG_DATA, 87654);
      wfService.activateAndComplete(oid, null, datas);

      RtEnvHome.deploy(adminService, null, MODEL_NAME);
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 12);
      datas.put(LONG_DATA, 76);
      wfService.activateAndComplete(oid, null, datas);

      RtEnvHome.deploy(adminService, null, MODEL_NAME);

      Archiver archiver = new Archiver(false, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      archiver.archiveDeadModels(0);
      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA, DC_TABLE_1);
      ResultSet result = srcStmt.executeQuery(srcString);
      assertTrue(result.next());
      assertEquals(0, result.getInt(1));
      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA, DC_TABLE_1);
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());
      assertEquals(0, result.getInt(1));
      exception.expect(ObjectNotFoundException.class);
      findCompletedProcessInstance(PROCESS_DEF_ID_1);
   }

   @Test
   public void testArchiveProcessesSynchronizeDataClusterDifferentInconsistencies()
         throws Exception
   {
      ProcessInstance process1 = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process2 = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process3 = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      String updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " SET oid_anint=NULL WHERE processinstance=" + process1.getOID();
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      String selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " WHERE oid_anint IS NULL";
      Statement selectStmt = connection.createStatement();
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      String deletePiStmt = "DELETE FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process2.getOID();
      DDLManager.executeOrSpoolStatement(deletePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process2.getOID();
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
      updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " SET oid_anInt=1234,oid_aString=5678,oid_aLong=123445 WHERE processinstance="
            + process3.getOID();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process3.getOID() + " AND oid_anInt=1234";
      result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());

      Archiver archiver = new Archiver(true, ArchiveTestUtils.ARC_SCHEMA, 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      ArrayList<Long> piOids = new ArrayList<Long>();
      piOids.add(process1.getOID());
      piOids.add(process2.getOID());
      piOids.add(process3.getOID());
      archiver.archiveDeadProcesses(piOids);

      ArchiveTestUtils.setArchiveAudittrailProperties();
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry
            .contains("Cluster table "
                  + ArchiveTestUtils.ARC_SCHEMA
                  + "."
                  + DC_TABLE_1
                  + " is not consistent: existing process instances are not referenced by cluster entries."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There are 5 inconsistencies.\r\n"));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Inconsistent cluster table "
            + ArchiveTestUtils.ARC_SCHEMA + "." + DC_TABLE_1
            + ": Inserted missing existing process instances into cluster table."));
      assertTrue(logEntry
            .contains("Synchronized data cluster table: "
                  + ArchiveTestUtils.ARC_SCHEMA
                  + "."
                  + DC_TABLE_1
                  + ". There were 8 inconsistencies. All inconsistencies have been resolved now.\r\n"));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies.\r\n"));
      ArchiveTestUtils.resetProperties();
   }

   @Test
   public void testArchiveProcessesSynchronizeDataClusterDifferentInconsistenciesScopePI()
         throws Exception
   {
      ProcessInstance process1 = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_2);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      long completedSubPiOid = findFirstCompletedProcessInstanceOid(PROCESS_DEF_ID_3);
      ProcessInstance process2 = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_2);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process3 = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_2);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      String insertPiStmt = "INSERT INTO "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " (processInstance) VALUES (" + completedSubPiOid + ")";
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(insertPiStmt, connection, null);
      connection.commit();
      Statement selectStmt = connection.createStatement();
      String selectString = "SELECT processinstance FROM " + DC_TABLE_1
            + " WHERE processinstance=" + completedSubPiOid;
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      String updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " SET oid_anint=NULL WHERE processinstance=" + process1.getOID();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " WHERE oid_anint IS NULL";
      selectStmt = connection.createStatement();
      result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      String deletePiStmt = "DELETE FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process2.getOID();
      DDLManager.executeOrSpoolStatement(deletePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process2.getOID();
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
      updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " SET oid_anInt=1234,oid_aString=5678,oid_aLong=123445 WHERE processinstance="
            + process3.getOID();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE_1)
            + " WHERE processinstance=" + process3.getOID() + " AND oid_anInt=1234";
      result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());

      Archiver archiver = new Archiver(true, ArchiveTestUtils.ARC_SCHEMA, 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      ArrayList<Long> piOids = new ArrayList<Long>();
      piOids.add(process1.getOID());
      piOids.add(process2.getOID());
      piOids.add(process3.getOID());
      archiver.archiveDeadProcesses(piOids);

      ArchiveTestUtils.setArchiveAudittrailProperties();
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry
            .startsWith("Cluster table "
                  + ArchiveTestUtils.ARC_SCHEMA
                  + "."
                  + DC_TABLE_1
                  + " is not consistent: non existing process instances are referenced by cluster entry."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There are 6 inconsistencies.\r\n"));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry
            .startsWith("Inconsistent cluster table "
                  + ArchiveTestUtils.ARC_SCHEMA
                  + "."
                  + DC_TABLE_1
                  + ": deleted cluster entries that referenced non existing process instances."));
      assertTrue(logEntry
            .contains("Synchronized data cluster table: "
                  + ArchiveTestUtils.ARC_SCHEMA
                  + "."
                  + DC_TABLE_1
                  + ". There were 9 inconsistencies. All inconsistencies have been resolved now.\r\n"));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies.\r\n"));
      ArchiveTestUtils.resetProperties();
   }

   @Test
   public void testArchiveBusinessObjectDataCluster() throws Exception
   {
      String keyName = "name";
      String keyStreet = "street";
      String keyPrice = "price";
      Map<String, Object> boValue = CollectionUtils.newMap();
      boValue.put(keyName, "Company A"); // PK
      boValue.put(keyStreet, "street1");
      boValue.put(keyPrice, 300);

      String qualifiedBusinessObjectId = "{DataClusterModel}BOData";
      BusinessObject businessObjectInstance = wfService.createBusinessObjectInstance(
            qualifiedBusinessObjectId, boValue);
      assertNotNull(businessObjectInstance);

      Archiver archiver = new Archiver(true, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      archiver.archiveDeadProcesses(null, 0);

      Map<String, Object> updatedValue = CollectionUtils.newMap();
      updatedValue.put(keyName, "Company A"); // PK
      updatedValue.put(keyStreet, "street2");
      updatedValue.put(keyPrice, 500);

      wfService.updateBusinessObjectInstance(qualifiedBusinessObjectId, updatedValue);
      archiver = new Archiver(true, "archive", 1000, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      archiver.archiveDeadProcesses(null, 0);

      String selectString = "SELECT sval_street, nval_price FROM "
            + ArchiveTestUtils.ARC_SCHEMA + "." + DC_TABLE_3;
      Statement selectStmt = connection.createStatement();
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      assertEquals(updatedValue.get(keyStreet), result.getString(1));
      assertEquals(updatedValue.get(keyPrice), result.getInt(2));
      assertFalse(result.next());
   }

   @Test
   public void testDropDataCluster() throws Exception
   {
      ArchiveTestUtils.dropDCTables();
      DatabaseMetaData metaData = archiveConnection.getMetaData();
      ResultSet resultSet = metaData.getTables(null, ArchiveTestUtils.ARC_SCHEMA,
            DC_TABLE_1.toUpperCase(), new String[] {"TABLE", "SYNONYM", "ALIAS"});
      assertFalse(resultSet.next());
   }

   private long findFirstAliveActivityInstanceOid(String processID)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(processID);
      final ActivityInstance ai = queryService.findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }

   private ProcessInstance findCompletedProcessInstance(String processID)
   {
      final ProcessInstanceQuery query = ProcessInstanceQuery.findCompleted(processID);
      return queryService.findFirstProcessInstance(query);
   }

   private long findFirstCompletedProcessInstanceOid(String processID)
   {
      final ProcessInstanceQuery query = ProcessInstanceQuery.findCompleted(processID);
      final ProcessInstance pi = queryService.findFirstProcessInstance(query);
      return pi.getOID();
   }

}
