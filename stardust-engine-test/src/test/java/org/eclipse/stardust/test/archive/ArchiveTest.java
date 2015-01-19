/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.archive;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class ArchiveTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private static final String ASSERTION_MSG_HAS_ENTRY_IN_DB = " - process instance entry in database";

   private static final String NL = "\n";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         ArchiveModelConstants.MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Test
   public void testExportImport() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      final ActivityInstance ai1 = sf.getQueryService().findFirstActivityInstance(
            ActivityInstanceQuery.findAlive(pi.getProcessID()));
      Map<String, String> outData = new HashMap<String, String>();
      outData.put(ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");
      final ActivityInstance writeActivity = sf.getWorkflowService()
            .activateAndComplete(ai1.getOID(), null, outData);

      final ActivityInstance ai2 = sf.getQueryService().findFirstActivityInstance(
            ActivityInstanceQuery.findAlive(pi.getProcessID()));
      final ActivityInstance readActivity = sf.getWorkflowService()
            .activateAndComplete(ai2.getOID(), null, null);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activities);
      assertEquals(1, instances.size());
      assertEquals(3, activities.size());
      assertEquals(pi.getOID(), instances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      instances = queryService.getAllProcessInstances(pQuery);
      activities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activities);
      assertEquals(0, instances.size());
      assertEquals(0, activities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");


      workflowService.execute(new ImportProcessesCommand(rawData));
      instances = queryService.getAllProcessInstances(pQuery);
      activities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activities);
      assertEquals(1, instances.size());
      assertEquals(3, activities.size());
      ProcessInstance restoredProcess = (ProcessInstance) instances.get(0);
      assertEquals(pi.getOID(), restoredProcess.getOID());
      assertEquals(pi.getScopeProcessInstanceOID(),
            restoredProcess.getScopeProcessInstanceOID());
      assertEquals(pi.getRootProcessInstanceOID(),
            restoredProcess.getRootProcessInstanceOID());
      assertThat(NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
            hasEntryInDbForObject("PROCINST_SCOPE", "SCOPEPROCESSINSTANCE", pi.getOID()),
            is(true));
      assertThat(NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
            hasEntryInDbForObject("PROCINST_SCOPE", "ROOTPROCESSINSTANCE", pi.getOID()),
            is(true));
      for (ActivityInstance activity : activities)
      {
         assertThat(
               NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("ACTIVITY_INSTANCE", "OID", activity.getOID()),
               is(true));
         if (activity.getOID() == readActivity.getOID()) {
            assertEquals(readActivity.getState(), activity.getState());
            assertEquals(readActivity.getStartTime(), activity.getStartTime());
            assertEquals(readActivity.getCurrentPerformer(), activity.getCurrentPerformer());
            assertEquals(readActivity.getPerformedBy(), activity.getPerformedBy());
            assertEquals(readActivity.getLastModificationTime(), activity.getLastModificationTime());
            assertEquals(readActivity.getModelOID(), activity.getModelOID());
         }
         if (activity.getOID() == writeActivity.getOID()) {
            assertEquals(writeActivity.getState(), activity.getState());
            assertEquals(writeActivity.getStartTime(), activity.getStartTime());
            assertEquals(writeActivity.getCurrentPerformer(), activity.getCurrentPerformer());
            assertEquals(writeActivity.getPerformedBy(), activity.getPerformedBy());
            assertEquals(writeActivity.getLastModificationTime(), activity.getLastModificationTime());
            assertEquals(writeActivity.getModelOID(), activity.getModelOID());
         }
      }
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

   }
   
   private boolean hasEntryInDbForObject(final String tableName, String fieldName,
         final long id) throws SQLException
   {
      final DataSource ds = testClassSetup.dataSource();
      final boolean result;

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT * FROM PUBLIC." + tableName
               + " WHERE " + fieldName + " = " + id);
         result = rs.first();
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }

      return result;
   }

   private void assertDataExists(long processInstanceOid, long activityOid,
         String processName, String dataId, String expectedValue)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            true);
   }

   private void assertDataNotExists(long processInstanceOid, long activityOid,
         String processName, String dataId, String expectedValue)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            false);
   }

   private void checkDataValue(long processInstanceOid, long activityOid,
         String processName, String dataId, String expectedValue, boolean shouldExists)
   {
      QueryService queryService = sf.getQueryService();

      ActivityInstances ais = queryService.getAllActivityInstances(ActivityInstanceQuery
            .findInStateHavingData(processName, dataId, expectedValue,
                  ActivityInstanceState.Completed));

      boolean found = false;
      for (int i = 0; i < ais.getSize(); i++)
      {
         ActivityInstance ai = ais.get(i);
         if (ai.getOID() == activityOid)
         {
            found = true;
         }
      }

      if (shouldExists)
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("No matching data found for process isntance oid '");
         errorMsg.append(processInstanceOid);
         errorMsg.append("' and value '");
         errorMsg.append(expectedValue);
         errorMsg.append("'");

         assertTrue(errorMsg.toString(), found);
      }
      else
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Data found for process isntance oid '");
         errorMsg.append(processInstanceOid);
         errorMsg.append("' and value '");
         errorMsg.append(expectedValue);
         errorMsg.append("' but should not exist");

         assertFalse(errorMsg.toString(), found);
      }
   }
}
