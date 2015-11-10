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
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.cli.sysconsole.Archiver;
import org.eclipse.stardust.engine.core.persistence.jdbc.DDLManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class contains functional tests for archiving audittrails.
 * </p>
 * 
 * @author Antje.Fuhrmann
 * @version $Revision$
 */
public class ArchiveTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   public static final String MODEL_NAME = "BasicWorkflowModel";

   private static final String PROCESS_DEF_ID_2 = "ProcessDefinition_2";

   @Rule
   public ExpectedException exception = ExpectedException.none();

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private WorkflowService wfService;

   private QueryService queryService;

   private Connection archiveConnection;

   private Connection connection;

   @Before
   public void setup() throws Exception
   {
      wfService = serviceFactory.getWorkflowService();
      queryService = serviceFactory.getQueryService();
      archiveConnection = ArchiveTestUtils.createArchiveAuditTrail();
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

   /**
    * Tests archiving using batch size.
    * 
    * <p> See also <a
    * href="https://www.csa.sungard.com/jira/browse/CRNT-37305">CRNT-38830</a>. </p> 
    */
   @Test
   public void testArchivingUsingBatchSize() throws Exception
   {
      for (int i = 0; i < 100; i++)
      {
         wfService.startProcess(PROCESS_DEF_ID_2, null, true);
         long oid = findFirstAliveActivityInstanceOid(PROCESS_DEF_ID_2);
         wfService.activateAndComplete(oid, null, null);
      }

      Archiver archiver = new Archiver(true, ArchiveTestUtils.ARC_SCHEMA, 10, true,
            PredefinedConstants.DEFAULT_PARTITION_ID);
      archiver.archiveDeadProcesses(null, 60 * 1000);

      Statement srcStmt = connection.createStatement();
      String srcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.SRC_SCHEMA,
                  ProcessInstanceBean.TABLE_NAME);
      ResultSet result = srcStmt.executeQuery(srcString);
      assertTrue(result.next());
      assertEquals(0, result.getInt(1));

      Statement arcStmt = archiveConnection.createStatement();
      String arcString = "SELECT COUNT(*) FROM "
            + DDLManager.getQualifiedName(ArchiveTestUtils.ARC_SCHEMA,
                  ProcessInstanceBean.TABLE_NAME);
      result = arcStmt.executeQuery(arcString);
      assertTrue(result.next());
      assertEquals(100, result.getInt(1));
   }

   private long findFirstAliveActivityInstanceOid(String processID)
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive(processID);
      final ActivityInstance ai = queryService.findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }

}
