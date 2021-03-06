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
package org.eclipse.stardust.test.datacluster;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.test.api.setup.DataClusterTestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains functional tests for verification and synchronization of data
 * cluster commands of sysconsole and its console logging action as well.
 * </p>
 *
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class DataClusterTest
{
   public static final String MODEL_NAME = "DataClusterModel";

   private static final String STRING_DATA_VAL = "TestValue";

   private static final String SYSOP = "sysop";

   private static final String DC_TABLE = "dv_mqt01";

   private static final String DB_SCHEMA = "PUBLIC";

   private static final String LONG_DATA = "aLong";

   private static final String INT_DATA = "anInt";

   private static final String STRING_DATA = "aString";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final DataClusterTestClassSetup testClassSetup = new DataClusterTestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private WorkflowService wfService;

   private static final String PROCESS_DEF_ID_1 = "ProcessDefinition1";

   private static final String PROCESS_DEF_ID_2 = "ProcessDefinition2";

   private static final String PROCESS_DEF_ID_3 = "ProcessDefinition3";

   private QueryService queryService;

   @Before
   public void before() throws Exception
   {
      GlobalParameters.globals().set("Carnot.Engine.Tuning.Query.EvaluationProfile",
            "dataClusters");
      wfService = serviceFactory.getWorkflowService();
      queryService = serviceFactory.getQueryService();

   }

   @Test
   public void testSynchronizeDataClusterTableNotExist() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      DBDescriptor dbDescriptor = DBDescriptor.create(SessionFactory.AUDIT_TRAIL);
      DDLManager ddlManager = new DDLManager(dbDescriptor);
      Connection connection = session.getConnection();
      String dropString = ddlManager.getDropTableStatementString(DB_SCHEMA, DC_TABLE);
      DDLManager.executeOrSpoolStatement(dropString, connection, null);
      connection.commit();
      assertFalse(ddlManager.containsTable(DB_SCHEMA, DC_TABLE, session.getConnection()));
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " does not exist."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There is 1 inconsistency."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " does not exist: Created table now."));
      assertTrue(logEntry.contains("Inconsistent cluster table " + DB_SCHEMA + "."
            + DC_TABLE
            + ": Inserted missing existing process instances into cluster table."));
      assertTrue(logEntry.contains("Synchronized data cluster table: " + DB_SCHEMA + "."
            + DC_TABLE + ". There were 5 inconsistencies. "
            + "All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      assertTrue(ddlManager.containsTable(DB_SCHEMA, DC_TABLE, session.getConnection()));
   }

   @Test
   public void testSynchronizeDataClusterWithNonExistingPI() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      int pi2 = 2;
      String insertPiStmt = "INSERT INTO "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " (processInstance) VALUES (" + pi2 + ")";
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(insertPiStmt, connection, null);
      connection.commit();
      Statement selectStmt = connection.createStatement();
      String selectString = "SELECT processinstance FROM " + DC_TABLE
            + " WHERE processinstance=" + pi2;
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " is not consistent: "
            + "non existing process instances are referenced by cluster entry."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There is 1 inconsistency."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry
            .contains("Inconsistent cluster table "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + ": deleted cluster entries that referenced non existing process instances."));
      assertTrue(logEntry.contains("Synchronized data cluster table: " + DB_SCHEMA + "."
            + DC_TABLE
            + ". There was 1 inconsistency. All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
   }

   @Test
   public void testSynchronizeDataClusterWithNoInconsistencies() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Synchronized data cluster table: " + DB_SCHEMA + "."
            + DC_TABLE + ". There were no inconsistencies to be resolved."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
   }

   @Test
   public void testSynchronizeDataClusterWithMissingPIInDataCluster() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      String deletePiStmt = "DELETE FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " WHERE processinstance=" + process.getOID();
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(deletePiStmt, connection, null);
      connection.commit();
      Statement selectStmt = connection.createStatement();
      String selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process.getOID();
      ResultSet result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " is not consistent: "
            + "existing process instances are not referenced by cluster entries."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There is 1 inconsistency."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Inconsistent cluster table " + DB_SCHEMA + "."
            + DC_TABLE
            + ": Inserted missing existing process instances into cluster table."));
      assertTrue(logEntry
            .contains("Synchronized data cluster table: "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + ". There were 4 inconsistencies. All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
   }

   @Test
   public void testSynchronizeDataClusterWithNonExistingDV() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      String updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " SET oid_anInt=1234,oid_aString=5678,oid_aLong=123445 WHERE processinstance="
            + process.getOID();
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      Statement selectStmt = connection.createStatement();
      String selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process.getOID() + " AND oid_anInt=1234";
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry
            .contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
                  + " is not consistent: "
                  + "referenced data values by cluster entry for slot"));
      assertTrue(logEntry.contains("attributeName '' are not matching."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There are 3 inconsistencies."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Inconsistent cluster table " + DB_SCHEMA + "."
            + DC_TABLE + ": Fixed cluster entries for slot"));
      assertTrue(logEntry
            .contains("Synchronized data cluster table: "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + ". There were 3 inconsistencies. All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
   }

   @Test
   public void testSynchronizeDataClusterEmptyDCEntryNotNull() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      String updatePiStmt = "UPDATE " + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " SET oid_anint=NULL WHERE processinstance=" + process.getOID();
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      String selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " WHERE oid_anint IS NULL";
      Statement selectStmt = connection.createStatement();
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " is not consistent: " + "empty cluster entries for slot"));
      assertTrue(logEntry.contains("are not completely set to null."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There is 1 inconsistency."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Inconsistent cluster table " + DB_SCHEMA + "."
            + DC_TABLE + ": Fixed empty cluster entries for slot"));
      assertTrue(logEntry.contains("Synchronized data cluster table: " + DB_SCHEMA + "."
            + DC_TABLE
            + ". There was 1 inconsistency. All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
   }

   @Test
   public void testSynchronizeDataClusterWithDVNotReferencedByDC() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process = wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      String updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " SET oid_astring=NULL,tk_astring=NULL,sval_astring=NULL WHERE processinstance="
            + process.getOID();
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      String selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " WHERE oid_astring IS NULL AND tk_astring IS NULL AND sval_astring IS NULL";
      Statement selectStmt = connection.createStatement();
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " is not consistent: existing data values for slot"));
      assertTrue(logEntry.contains("are not referenced by cluster entry."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There is 1 inconsistency."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry
            .contains("Inconsistent cluster table "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + ": Fixed cluster entries that have not referenced existing data values for slot"));
      assertTrue(logEntry.contains("Synchronized data cluster table: " + DB_SCHEMA + "."
            + DC_TABLE
            + ". There was 1 inconsistency. All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
   }

   @Test
   public void testSynchronizeDataClusterDifferentInconsistencies() throws Exception
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
      String updatePiStmt = "UPDATE " + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " SET oid_anint=NULL WHERE processinstance=" + process1.getOID();
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      String selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " WHERE oid_anint IS NULL";
      Statement selectStmt = connection.createStatement();
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      String deletePiStmt = "DELETE FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " WHERE processinstance=" + process2.getOID();
      DDLManager.executeOrSpoolStatement(deletePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process2.getOID();
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
      updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " SET oid_anInt=1234,oid_aString=5678,oid_aLong=123445 WHERE processinstance="
            + process3.getOID();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process3.getOID() + " AND oid_anInt=1234";
      result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());

      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry
            .contains("Cluster table "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + " is not consistent: existing process instances are not referenced by cluster entries."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There are 5 inconsistencies."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Inconsistent cluster table " + DB_SCHEMA + "."
            + DC_TABLE
            + ": Inserted missing existing process instances into cluster table."));
      assertTrue(logEntry
            .contains("Synchronized data cluster table: "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + ". There were 8 inconsistencies. All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
   }

   @Test
   public void testSynchronizeDataClusterDifferentInconsistenciesScopePI()
         throws Exception
   {
      ProcessInstance process1 = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_2);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstances completedSubPis = findFirstCompletedProcessInstanceOid(PROCESS_DEF_ID_3);
      assertEquals(1, completedSubPis.size());
      ProcessInstance completedSubPi = completedSubPis.get(0);
      long completedSubPiOid = completedSubPi.getOID();
      ProcessInstance process2 = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_2);
      wfService.activateAndComplete(oid, null, datas);
      ProcessInstance process3 = wfService.startProcess(PROCESS_DEF_ID_2, null, true);
      oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_2);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      String insertPiStmt = "INSERT INTO "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " (processInstance) VALUES (" + completedSubPiOid + ")";
      Connection connection = session.getConnection();
      DDLManager.executeOrSpoolStatement(insertPiStmt, connection, null);
      connection.commit();
      Statement selectStmt = connection.createStatement();
      String selectString = "SELECT processinstance FROM " + DC_TABLE
            + " WHERE processinstance=" + completedSubPiOid;
      ResultSet result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      String updatePiStmt = "UPDATE " + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " SET oid_anint=NULL WHERE processinstance=" + process1.getOID();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      selectString = "SELECT processinstance FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " WHERE oid_anint IS NULL";
      selectStmt = connection.createStatement();
      result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());
      String deletePiStmt = "DELETE FROM "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " WHERE processinstance=" + process2.getOID();
      DDLManager.executeOrSpoolStatement(deletePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process2.getOID();
      result = selectStmt.executeQuery(selectString);
      assertFalse(result.next());
      updatePiStmt = "UPDATE "
            + DDLManager.getQualifiedName(DB_SCHEMA, DC_TABLE)
            + " SET oid_anInt=1234,oid_aString=5678,oid_aLong=123445 WHERE processinstance="
            + process3.getOID();
      DDLManager.executeOrSpoolStatement(updatePiStmt, connection, null);
      connection.commit();
      selectStmt = connection.createStatement();
      selectString = "SELECT processinstance FROM dv_mqt01 WHERE processinstance="
            + process3.getOID() + " AND oid_anInt=1234";
      result = selectStmt.executeQuery(selectString);
      assertTrue(result.next());

      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry
            .contains("Cluster table "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + " is not consistent: non existing process instances are referenced by cluster entry."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There are 6 inconsistencies."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry
            .contains("Inconsistent cluster table "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + ": deleted cluster entries that referenced non existing process instances."));
      assertTrue(logEntry
            .contains("Synchronized data cluster table: "
                  + DB_SCHEMA
                  + "."
                  + DC_TABLE
                  + ". There were 9 inconsistencies. All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
   }

   private long findFirstAliveActivityInstanceOid(String processID)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(processID);
      final ActivityInstance ai = queryService.findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }

   private ProcessInstances findFirstCompletedProcessInstanceOid(String processID)
   {
      final ProcessInstanceQuery query = ProcessInstanceQuery.findCompleted(processID);
      final ProcessInstances pis = queryService.getAllProcessInstances(query);
      return pis;
   }

   @Test
   public void testNotInPredicateOnDataCluster() throws Exception
   {
      wfService.startProcess(PROCESS_DEF_ID_1, null, true);
      long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_1);
      Map<String, Object> datas = new HashMap<String, Object>();
      datas.put(STRING_DATA, STRING_DATA_VAL);
      datas.put(INT_DATA, 123);
      datas.put(LONG_DATA, 45678);
      wfService.activateAndComplete(oid, null, datas);
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      DBDescriptor dbDescriptor = DBDescriptor.create(SessionFactory.AUDIT_TRAIL);
      DDLManager ddlManager = new DDLManager(dbDescriptor);
      Connection connection = session.getConnection();
      String dropString = ddlManager.getDropTableStatementString(DB_SCHEMA, DC_TABLE);
      DDLManager.executeOrSpoolStatement(dropString, connection, null);
      connection.commit();
      assertFalse(ddlManager.containsTable(DB_SCHEMA, DC_TABLE, session.getConnection()));
      ByteArrayOutputStream logEntryBeforeSync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntrySync = new ByteArrayOutputStream();
      ByteArrayOutputStream logEntryAfterSync = new ByteArrayOutputStream();
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryBeforeSync));
      String logEntry = logEntryBeforeSync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " does not exist."));
      assertTrue(logEntry
            .contains("The data cluster is invalid. There is 1 inconsistency."));
      SchemaHelper.alterAuditTrailSynchronizeDataClusterTables(SYSOP, new PrintStream(
            logEntrySync), null, null);
      logEntry = logEntrySync.toString();
      assertTrue(logEntry.contains("Cluster table " + DB_SCHEMA + "." + DC_TABLE
            + " does not exist: Created table now."));
      assertTrue(logEntry.contains("Inconsistent cluster table " + DB_SCHEMA + "."
            + DC_TABLE
            + ": Inserted missing existing process instances into cluster table."));
      assertTrue(logEntry.contains("Synchronized data cluster table: " + DB_SCHEMA + "."
            + DC_TABLE + ". There were 5 inconsistencies. "
            + "All inconsistencies have been resolved now."));
      SchemaHelper.alterAuditTrailVerifyDataClusterTables(SYSOP, new PrintStream(
            logEntryAfterSync));
      logEntry = logEntryAfterSync.toString();
      assertTrue(logEntry
            .contains("Verified data cluster. There are no inconsistencies."));
      assertTrue(ddlManager.containsTable(DB_SCHEMA, DC_TABLE, session.getConnection()));

      Collection<String> pds = new ArrayList<String>(3);
      pds.add("aaa");
      pds.add("bbb");
      pds.add("ccc");

      // This failed before fix applied for DC evaluation. NotIn-DataFilter had been evaluated to IN before
      ActivityInstanceQuery aiquery = ActivityInstanceQuery.findAll();
      aiquery.getFilter().add(DataFilter.notIn("aString", pds));
      ActivityInstances ais = queryService.getAllActivityInstances(aiquery);
      assertTrue(ais.getSize() == 1);

      // Evaluateion of In-DataFilter worked before, too.
      aiquery = ActivityInstanceQuery.findAll();
      aiquery.getFilter().add(DataFilter.in("aString", pds));
      ais = queryService.getAllActivityInstances(aiquery);
      assertTrue(ais.getSize() == 0);
   }
}
