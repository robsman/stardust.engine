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
import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.pojo.AuditTrailPartitionManager;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand.ExportMetaData;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.*;

/*
 * add test for:
 * actinst_properties
 * 
 */
public class ArchiveTest
{
   private static final String PARTION_B = "PARTION_B";

   private static final String PARTION_A = "PARTION_A";

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
         ArchiveModelConstants.MODEL_ID, ArchiveModelConstants.MODEL_ID_OTHER);

   private final TestTimestampProvider testTimestampProvider = new TestTimestampProvider();

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Before
   public void setUp() throws Exception
   {
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE,
            testTimestampProvider);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            3);
   }

   @After
   public void tearDown()
   {
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE, null);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            null);
   }

   @Test
   public void testMultiPartition() throws Exception
   {
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_B, "sysop");

      Map<String, String> propertiesA = new HashMap<String, String>();
      Map<String, String> propertiesB = new HashMap<String, String>();
      propertiesA.put(SecurityProperties.PARTITION, PARTION_A);
      propertiesB.put(SecurityProperties.PARTITION, PARTION_B);

      ServiceFactory factoryA = ServiceFactoryLocator.get(ADMIN_USER_PWD_PAIR.username(),
            ADMIN_USER_PWD_PAIR.password(), propertiesA);
      ServiceFactory factoryB = ServiceFactoryLocator.get(ADMIN_USER_PWD_PAIR.username(),
            ADMIN_USER_PWD_PAIR.password(), propertiesB);

      WorkflowService wsA = factoryA.getWorkflowService();
      QueryService qsA = factoryA.getQueryService();
      AdministrationService asA = factoryA.getAdministrationService();

      WorkflowService wsB = factoryB.getWorkflowService();
      QueryService qsB = factoryB.getQueryService();
      AdministrationService asB = factoryB.getAdministrationService();

      RtEnvHome.deploy(asA, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(asB, null, ArchiveModelConstants.MODEL_ID);

      final ProcessInstance piA = wsA.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      final ActivityInstance writeActivityA = completeSimpleManual(piA, qsA, wsA);

      final ProcessInstance piB = wsB.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      final ActivityInstance writeActivityB = completeSimpleManual(piB, qsB, wsB);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piB.getOID()));

      ProcessInstances oldInstancesA = qsA.getAllProcessInstances(pQuery);
      ActivityInstances oldActivitiesA = qsA.getAllActivityInstances(aQuery);
      ProcessInstances oldInstancesB = qsB.getAllProcessInstances(pQuery);
      ActivityInstances oldActivitiesB = qsB.getAllActivityInstances(aQuery);
      assertNotNull(oldInstancesA);
      assertNotNull(oldActivitiesA);
      assertNotNull(oldInstancesB);
      assertNotNull(oldActivitiesB);
      assertEquals(1, oldInstancesA.size());
      assertEquals(3, oldActivitiesA.size());
      assertEquals(1, oldInstancesB.size());
      assertEquals(3, oldActivitiesB.size());

      assertDataExists(piA.getOID(), writeActivityA.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", qsA);
      assertDataExists(piB.getOID(), writeActivityB.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", qsB);

      ExportResult rawDataA = (ExportResult) wsA.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawDataA);
      ExportResult rawDataB = (ExportResult) wsB.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawDataB);

      ProcessInstances instancesA = qsA.getAllProcessInstances(pQuery);
      ActivityInstances activitiesClearedA = qsA.getAllActivityInstances(aQuery);
      ProcessInstances instancesB = qsB.getAllProcessInstances(pQuery);
      ActivityInstances activitiesClearedB = qsB.getAllActivityInstances(aQuery);
      assertNotNull(instancesA);
      assertNotNull(activitiesClearedA);
      assertNotNull(instancesB);
      assertNotNull(activitiesClearedB);
      assertEquals(0, instancesA.size());
      assertEquals(0, activitiesClearedA.size());
      assertEquals(0, instancesB.size());
      assertEquals(0, activitiesClearedB.size());

      int count = (Integer) wsA.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawDataA),
            null));
      assertEquals(1, count);
      count = (Integer) wsB.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawDataB),
            null));
      assertEquals(1, count);
      ProcessInstances newInstancesA = qsA.getAllProcessInstances(pQuery);
      ActivityInstances newActivitiesA = qsA.getAllActivityInstances(aQuery);
      ProcessInstances newInstancesB = qsB.getAllProcessInstances(pQuery);
      ActivityInstances newActivitiesB = qsB.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(oldInstancesA, newInstancesA);
      assertActivityInstancesEquals(oldActivitiesA, newActivitiesA);
      assertProcessInstancesEquals(oldInstancesB, newInstancesB);
      assertActivityInstancesEquals(oldActivitiesB, newActivitiesB);
      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_B, "sysop");
   }

   @Test
   public void testPartitionValidation() throws Exception
   {
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_B, "sysop");

      Map<String, String> propertiesA = new HashMap<String, String>();
      Map<String, String> propertiesB = new HashMap<String, String>();
      propertiesA.put(SecurityProperties.PARTITION, PARTION_A);
      propertiesB.put(SecurityProperties.PARTITION, PARTION_B);

      ServiceFactory factoryA = ServiceFactoryLocator.get(ADMIN_USER_PWD_PAIR.username(),
            ADMIN_USER_PWD_PAIR.password(), propertiesA);
      ServiceFactory factoryB = ServiceFactoryLocator.get(ADMIN_USER_PWD_PAIR.username(),
            ADMIN_USER_PWD_PAIR.password(), propertiesB);

      WorkflowService wsA = factoryA.getWorkflowService();
      QueryService qsA = factoryA.getQueryService();
      AdministrationService asA = factoryA.getAdministrationService();

      WorkflowService wsB = factoryB.getWorkflowService();
      QueryService qsB = factoryB.getQueryService();
      AdministrationService asB = factoryB.getAdministrationService();

      RtEnvHome.deploy(asA, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(asB, null, ArchiveModelConstants.MODEL_ID);

      final ProcessInstance piA = wsA.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      final ActivityInstance writeActivityA = completeSimpleManual(piA, qsA, wsA);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piA.getOID()));

      ProcessInstances oldInstancesA = qsA.getAllProcessInstances(pQuery);
      ActivityInstances oldActivitiesA = qsA.getAllActivityInstances(aQuery);
      ProcessInstances oldInstancesB = qsB.getAllProcessInstances(pQuery);
      ActivityInstances oldActivitiesB = qsB.getAllActivityInstances(aQuery);
      assertNotNull(oldInstancesA);
      assertNotNull(oldActivitiesA);
      assertNotNull(oldInstancesB);
      assertNotNull(oldActivitiesB);
      assertEquals(1, oldInstancesA.size());
      assertEquals(3, oldActivitiesA.size());
      assertEquals(0, oldInstancesB.size());
      assertEquals(0, oldActivitiesB.size());

      assertDataExists(piA.getOID(), writeActivityA.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", qsA);

      ExportResult rawData = (ExportResult) wsA.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances instancesA = qsA.getAllProcessInstances(pQuery);
      ActivityInstances activitiesClearedA = qsA.getAllActivityInstances(aQuery);
      ProcessInstances instancesB = qsB.getAllProcessInstances(pQuery);
      ActivityInstances activitiesClearedB = qsB.getAllActivityInstances(aQuery);
      assertNotNull(instancesA);
      assertNotNull(activitiesClearedA);
      assertNotNull(instancesB);
      assertNotNull(activitiesClearedB);
      assertEquals(0, instancesA.size());
      assertEquals(0, activitiesClearedA.size());
      assertEquals(0, instancesB.size());
      assertEquals(0, activitiesClearedB.size());

      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.ERROR);
      barrier.registerWithLog4j();

      int count = (Integer) wsB
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(0, count);

      ProcessInstances newInstancesA = qsA.getAllProcessInstances(pQuery);
      ActivityInstances newActivitiesA = qsA.getAllActivityInstances(aQuery);
      ProcessInstances newInstancesB = qsB.getAllProcessInstances(pQuery);
      ActivityInstances newActivitiesB = qsB.getAllActivityInstances(aQuery);
      assertEquals(0, newInstancesA.size());
      assertEquals(0, newActivitiesA.size());
      assertEquals(0, newInstancesB.size());
      assertEquals(0, newActivitiesB.size());

      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_B, "sysop");

      barrier
            .waitForLogMessage(
                  "Invalid environment to import into.*Export partition PARTION_A does not match current partition PARTION_B.*",
                  new WaitTimeout(5, TimeUnit.SECONDS));
   }

   @Test
   public void testMultiModel() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance piOtherModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      final ActivityInstance otherActivity = completeOther(piOtherModel, 5, queryService,
            workflowService);

      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);

      final ProcessInstance piModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      final ActivityInstance writeActivity = completeSimpleManual(piModel, queryService,
            workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      assertDataExists(piModel.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      assertDataExists(piOtherModel.getOID(), otherActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_OTHER,
            ArchiveModelConstants.DATA_ID_OTHER_NUMBER, 5, queryService);

      assertTrue(hasStructuredDateField(piOtherModel.getOID(),
            ArchiveModelConstants.DATA_ID_OTHER_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 5));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOtherModel.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piModel.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(2, oldInstances.size());
      assertEquals(7, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(2, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testMultiModel1MissingModel() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance piOtherModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      final ActivityInstance otherActivity = completeOther(piOtherModel, 5, queryService,
            workflowService);

      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);

      final ProcessInstance piModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      final ActivityInstance writeActivity = completeSimpleManual(piModel, queryService,
            workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      assertDataExists(piModel.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      assertDataExists(piOtherModel.getOID(), otherActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_OTHER,
            ArchiveModelConstants.DATA_ID_OTHER_NUMBER, 5, queryService);

      assertTrue(hasStructuredDateField(piOtherModel.getOID(),
            ArchiveModelConstants.DATA_ID_OTHER_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 5));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOtherModel.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piModel.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(2, oldInstances.size());
      assertEquals(7, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      adminService.deleteModel(piOtherModel.getModelOID());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER);
      assertEquals(0, count);
   }

   @Test
   public void testMultiModelExportFilterByModelWithDep() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance piOtherModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      final ActivityInstance otherActivity = completeOther(piOtherModel, 5, queryService,
            workflowService);

      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);

      final ProcessInstance piModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      final ActivityInstance writeActivity = completeSimpleManual(piModel, queryService,
            workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      assertDataExists(piModel.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      assertDataExists(piOtherModel.getOID(), otherActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_OTHER,
            ArchiveModelConstants.DATA_ID_OTHER_NUMBER, 5, queryService);

      assertTrue(hasStructuredDateField(piOtherModel.getOID(),
            ArchiveModelConstants.DATA_ID_OTHER_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 5));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOtherModel.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piModel.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(2, oldInstances.size());
      assertEquals(7, oldActivities.size());

      List<Integer> modelOids = Arrays.asList(piOtherModel.getModelOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, modelOids, null,
                  true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, instances.size());
      assertEquals(3, activitiesCleared.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testMultiModelExportFilterByModelWithNoDep() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance piOtherModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      final ActivityInstance otherActivity = completeOther(piOtherModel, 5, queryService,
            workflowService);

      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);

      final ProcessInstance piModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      final ActivityInstance writeActivity = completeSimpleManual(piModel, queryService,
            workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      assertDataExists(piModel.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      assertDataExists(piOtherModel.getOID(), otherActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_OTHER,
            ArchiveModelConstants.DATA_ID_OTHER_NUMBER, 5, queryService);

      assertTrue(hasStructuredDateField(piOtherModel.getOID(),
            ArchiveModelConstants.DATA_ID_OTHER_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 5));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOtherModel.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piModel.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(2, oldInstances.size());
      assertEquals(7, oldActivities.size());

      List<Integer> modelOids = Arrays.asList(piModel.getModelOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, modelOids, null,
                  true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, instances.size());
      assertEquals(4, activitiesCleared.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportImportRedeployBeforeExport() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      final ActivityInstance writeActivity = completeSimpleManual(pi, queryService,
            workflowService);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(3, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID);

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);

      assertProcessInstancesEquals(oldInstances, clearedInstances, false);
      assertActivityInstancesEquals(oldActivities, clearedActivities, false);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

   }

   @Test
   public void testExportImportOldCompatibleModel() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      final ActivityInstance writeActivity = completeSimpleManual(pi, queryService,
            workflowService);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(3, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID);

      List<Integer> modelOids = Arrays.asList(pi.getModelOID());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, modelOids, null,
                  true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

   }

   @Test
   public void importNull()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, null, null));
      assertEquals(0, count);
   }

   @Test
   public void invalidProcessInstanceOid()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      List<Long> oids = Arrays.asList(-1L);
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(0, count);
      assertNullRawData(rawData);
   }

   @Test
   public void testExportNoData() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNullRawData(rawData);
   }

   @Test
   public void invalidProcessInstanceOidNull()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      Long oid = null;
      List<Long> oids = Arrays.asList(oid);
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNullRawData(rawData);
   }

   @Test
   public void invalidProcessInstanceOidBlankList()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      List<Long> oids = new ArrayList<Long>();
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNullRawData(rawData);
   }

   @Test
   public void testExportNotCompletedOrAborted() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNullRawData(rawData);

      // ensure it was not purged
      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(1, oldInstances.size());
   }

   @Test
   public void testExportAllNullDates() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      List<Long> oids = null;
      List<Integer> modelOids = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, modelOids, oids,
                  true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(7, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFromAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      testTimestampProvider.nextDay();
      Date toDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, fromDate, toDate,
                  true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(4, clearedInstances.size());
      assertEquals(13, clearedActivities.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testDatesAndHours() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date simpleManualADate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      testTimestampProvider.nextDay();
      completeSimpleManual(simpleManualA, queryService, workflowService);
      Date simpleManualBDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      testTimestampProvider.nextHour();
      Date simpleDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      testTimestampProvider.nextHour();
      completeSimple(simpleB, queryService, workflowService);
      Date subProcessesInModelDate = testTimestampProvider.getTimestamp();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      Date subProcessesDate = completeSubProcessesInModel(subProcessesInModel,
            queryService, workflowService, true);
      testTimestampProvider.nextDay();
      Date scriptProcessDate = testTimestampProvider.getTimestamp();
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(scriptProcess.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      final DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy HH:mm",
            Locale.GERMAN);
      dateFormat.setTimeZone(TestTimeZoneProvider.FIXED_TEST_TIME_ZONE);
      for (ProcessInstance process : oldInstances)
      {
         if (process.getOID() == simpleA.getOID())
         {
            assertEquals(dateFormat.parse("2.1.2080 01:00"), process.getStartTime());
            assertEquals(simpleDate, process.getStartTime());
         }
         if (process.getOID() == simpleB.getOID())
         {
            assertEquals(dateFormat.parse("2.1.2080 01:00"), process.getStartTime());
            assertEquals(simpleDate, process.getStartTime());
         }
         if (process.getOID() == simpleManualA.getOID())
         {
            assertEquals(dateFormat.parse("1.1.2080 00:00"), process.getStartTime());
            assertEquals(simpleManualADate, process.getStartTime());
         }
         if (process.getOID() == simpleManualB.getOID())
         {
            assertEquals(dateFormat.parse("2.1.2080 00:00"), process.getStartTime());
            assertEquals(simpleManualBDate, process.getStartTime());
         }
         if (process.getOID() == subSimple.getOID())
         {
            assertEquals(dateFormat.parse("2.1.2080 03:00"), process.getStartTime());
            assertEquals(subProcessesDate, process.getStartTime());
         }
         if (process.getOID() == subManual.getOID())
         {
            assertEquals(dateFormat.parse("2.1.2080 03:00"), process.getStartTime());
            assertEquals(subProcessesDate, process.getStartTime());
         }
         if (process.getOID() == subProcessesInModel.getOID())
         {
            assertEquals(dateFormat.parse("2.1.2080 02:00"), process.getStartTime());
            assertEquals(subProcessesInModelDate, process.getStartTime());
         }
         if (process.getOID() == scriptProcess.getOID())
         {
            assertEquals(dateFormat.parse("3.1.2080 03:00"), process.getStartTime());
            assertEquals(scriptProcessDate, process.getStartTime());
         }
      }
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());

      ExportResult result = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(result);
      assertEquals(6, result.getDates().size());
      assertNotNull(result.getResults(scriptProcessDate));
      assertNotNull(result.getResults(simpleManualADate));
      assertNotNull(result.getResults(simpleManualBDate));
      assertNotNull(result.getResults(simpleDate));
      assertNotNull(result.getResults(subProcessesDate));
      assertNotNull(result.getResults(subProcessesInModelDate));

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      ImportMetaData meta = (ImportMetaData) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE, result.getModelData(), null));
      assertNotNull(meta);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT,
            result.getResults(scriptProcessDate), null, meta));
      assertEquals(1, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT,
            result.getResults(simpleManualADate), null, meta));
      assertEquals(2, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT,
            result.getResults(simpleManualBDate), null, meta));
      assertEquals(3, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, result.getResults(simpleDate), null,
            meta));
      assertEquals(5, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, result
                  .getResults(subProcessesInModelDate), null, meta));
      assertEquals(6, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, result.getResults(subProcessesDate),
            null, meta));
      assertEquals(8, count);

      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportImportOperations() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(scriptProcess.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());

      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY, true);
      ExportMetaData exportMetaData = (ExportMetaData) workflowService.execute(command);
      List<ExportMetaData> batches = ExportImportSupport.partition(exportMetaData, 5);
      List<ExportResult> datas = new ArrayList<ExportResult>();
      assertEquals(8, exportMetaData.getAllProcessInstanceOids().size());
      assertEquals(6, exportMetaData.getRootProcessInstanceOids().size());

      command = new ExportProcessesCommand(ExportProcessesCommand.Operation.EXPORT_MODEL,
            exportMetaData, true);
      ExportResult modelData = (ExportResult) workflowService.execute(command);
      assertNotNullModel(modelData);

      for (ExportMetaData batch : batches)
      {
         command = new ExportProcessesCommand(
               ExportProcessesCommand.Operation.EXPORT_BATCH, batch, true);
         ExportResult rawData = (ExportResult) workflowService.execute(command);
         assertNotNullBatches(rawData);
         datas.add(rawData);
      }
      assertEquals(2, batches.size());
      assertEquals(2, datas.size());

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      ImportProcessesCommand importCommand = new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, getBytes(modelData), null);
      ImportMetaData importMetaData = (ImportMetaData) workflowService
            .execute(importCommand);
      assertNotNull(importMetaData);
      assertNotNull(importMetaData.getImportId(ModelBean.class,
            new Long(simpleA.getModelOID())));

      int countAll = 0;
      for (ExportResult data : datas)
      {
         importCommand = new ImportProcessesCommand(
               ImportProcessesCommand.Operation.IMPORT, getBytes(data), importMetaData);
         int count = (Integer) workflowService.execute(importCommand);
         countAll += count;
      }
      assertEquals(8, countAll);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportImportModel() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY, true);
      ExportMetaData exportMetaData = (ExportMetaData) workflowService.execute(command);

      command = new ExportProcessesCommand(ExportProcessesCommand.Operation.EXPORT_MODEL,
            exportMetaData, true);
      ExportResult rawData = (ExportResult) workflowService.execute(command);
      assertNotNullModel(rawData);
      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.INFO);
      barrier.registerWithLog4j();
      ImportMetaData importMetaData = (ImportMetaData) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE, getBytes(rawData), null));

      barrier.waitForLogMessage("No Process Data in this file", new WaitTimeout(5,
            TimeUnit.SECONDS));

      assertNotNull(importMetaData);
      assertNotNull(importMetaData.getImportId(ModelBean.class,
            new Long(pi.getModelOID())));

   }

   @Test(expected = ServiceCommandException.class)
   public void testExportInvalidDateRange() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      Date fromDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      Date toDate = testTimestampProvider.getTimestamp();

      workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT, toDate, fromDate, true));
      fail("Invalid date ranges. Code should not get here");
   }

   @Test(expected = ServiceCommandException.class)
   public void testImportInvalidDateRange() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date fromDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      Date toDate = testTimestampProvider.getTimestamp();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            toDate, fromDate, null));
      fail("Invalid date ranges. Test should not get here");
   }

   @Test
   public void testImportInvalidBadData() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, new byte[] {5}, null));
      assertEquals(0, count);
   }

   @Test
   public void testImportInvalidDataEOF() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
            new byte[] {BlobBuilder.SECTION_MARKER_EOF}, null));
      assertEquals(0, count);
   }

   @Test
   public void testImportInvalidDataInstances() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
            new byte[] {BlobBuilder.SECTION_MARKER_INSTANCES}, null));
      assertEquals(0, count);
   }

   @Test
   public void testImportInvalidDataInstancesBadData() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, new byte[] {
                  BlobBuilder.SECTION_MARKER_INSTANCES, 5}, null));
      assertEquals(0, count);
   }

   @Test
   public void testExportAllFilterImportFromAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date firstDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);// 1jan
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualB.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(7, expectedActivities.size());
      assertEquals(3, expectedInstances.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData,
                  firstDate), fromDate, toDate, null));
      assertEquals(0, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData,
                  lastDate), fromDate, toDate, null));
      assertEquals(0, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData,
                  fromDate), fromDate, toDate, null));
      assertEquals(2, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData,
                  toDate), fromDate, toDate, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportFromAndToDateBothNull() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      Date date = null;
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            date, date, null));
      assertEquals(7, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportFromNullAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date date1 = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      testTimestampProvider.nextDay();
      Date date2 = testTimestampProvider.getTimestamp();
      Date fromDate = null;
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      Date toDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualA.getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualB.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualA.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(10, expectedActivities.size());
      assertEquals(4, expectedInstances.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
            getBytes(rawData, date1), fromDate, toDate, null));
      assertEquals(1, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
            getBytes(rawData, date2), fromDate, toDate, null));
      assertEquals(4, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportFromAndToDateNull() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      testTimestampProvider.nextDay();
      Date date1 = testTimestampProvider.getTimestamp();
      Date fromDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      testTimestampProvider.nextDay();
      Date date2 = testTimestampProvider.getTimestamp();
      Date toDate = null;
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualB.getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subProcessesInModel.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subSimple.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subManual.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(17, expectedActivities.size());
      assertEquals(6, expectedInstances.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
            getBytes(rawData, date1), fromDate, toDate, null));
      assertEquals(3, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
            getBytes(rawData, date2), fromDate, toDate, null));
      assertEquals(6, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllNullFromAndToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      testTimestampProvider.nextDay();
      Date toDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      Date fromDate = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, fromDate, toDate,
                  true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(3, clearedInstances.size());
      assertEquals(10, clearedActivities.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(4, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFromAndNullToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      Date toDate = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, fromDate, toDate,
                  true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(1, clearedInstances.size());
      assertEquals(3, clearedActivities.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(6, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllTwoModels() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      ActivityInstance writeActivity = completeScriptProcess(scriptProcess, 10, "aaa",
            queryService, workflowService);
      int modelOID = subProcessesInModel.getModelOID();

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(scriptProcess.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      adminService.deleteModel(modelOID);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER2);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER);

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER2));
      model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      assertEquals(8, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);

      assertDataExists(scriptProcess.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20, queryService);

      assertTrue(hasStructuredDateField(scriptProcess.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      assertTrue(hasStructuredDateField(scriptProcess.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, "aaa"));
   }

   @Test
   public void testExportAllImportAll() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(scriptProcess.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(8, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllImportAllIgnoreExistingInstances() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(scriptProcess.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<Long> oids = Arrays.asList(simpleManualA.getOID());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            oids, null));
      assertEquals(1, count);
      oids = Arrays.asList(simpleManualA.getOID(), simpleManualB.getOID());
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            oids, null));
      assertEquals(1, count);
      oids = Arrays.asList(simpleManualA.getOID(), simpleManualB.getOID(),
            subProcessesInModel.getOID());
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            oids, null));
      assertEquals(3, count);
      oids = Arrays.asList(simpleManualA.getOID(), simpleManualB.getOID(),
            subProcessesInModel.getOID(), simpleA.getOID(), simpleB.getOID(),
            scriptProcess.getOID());
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            oids, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportProcessIds() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subProcessesInModel.getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual
            .getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subProcessesInModel
            .getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subSimple.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subManual.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(12, expectedActivities.size());
      assertEquals(4, expectedInstances.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<Long> oids = Arrays.asList(simpleA.getOID(), subProcessesInModel.getOID());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            oids, null));
      assertEquals(4, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportProcessIdsInvalid() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<Long> oids = Arrays.asList(-1L, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            oids, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      assertEquals(0, newInstances.size());
   }

   @Test
   public void testExportAllFilterImportProcessIdsBlank() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<Long> oids = new ArrayList<Long>();
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, getBytes(rawData),
            oids, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      assertEquals(0, newInstances.size());
   }

   @Test
   public void testExport2OfMany() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      List<Long> oids = Arrays.asList(simpleA.getOID(), simpleManualA.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(15, clearedActivities.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(2, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportImportSimple() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportSimpleOidNoPurge() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, false));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
   }

   @Test
   public void testExportSimpleNoPurge() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, false));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
   }

   @Test
   public void testExportSimpleDatesNoPurge() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      Date startDate = testTimestampProvider.getTimestamp();
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);
      Date endDate = testTimestampProvider.getTimestamp();

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, startDate, endDate,
                  false));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
   }

   private ProcessInstance completeSimple(ProcessInstance pi, QueryService qs,
         WorkflowService ws) throws TimeoutException, InterruptedException
   {
      completeNextActivity(pi, null, null, qs, ws);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return pi;
   }

   @Test
   public void testExportImportSimpleManualAborted() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService,
            workflowService);

      adminService.abortProcessInstance(pi.getOID());

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Aborted);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

   }

   @Test
   public void testExportImportSimpleManual() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      final ActivityInstance writeActivity = completeSimpleManual(pi, queryService,
            workflowService);

      
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(3, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

   }

   @Test
   public void testExportImportScriptProcess() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);

      //test largeStringHolder
      char[] chars = new char[5000];
      Arrays.fill(chars, 'a');
      String textValue = new String(chars);
      final ActivityInstance writeActivity = completeScriptProcess(pi, 10, textValue,
            queryService, workflowService);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20, queryService);

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue));

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(8, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20, queryService);

      assertFalse(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      assertFalse(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue));

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20, queryService);

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue));

   }

   private ActivityInstance completeOther(final ProcessInstance pi, int numberValue,
         QueryService qs, WorkflowService ws) throws TimeoutException,
         InterruptedException
   {
      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_OTHER_NUMBER, numberValue, ArchiveModelConstants.DATA_ID_TEXTDATA, "aaa", qs, ws);

      completeNextActivity(pi, null, null, qs, ws);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return writeActivity;
   }

   private ActivityInstance completeScriptProcess(final ProcessInstance pi,
         int numberValue, String textValue, QueryService qs, WorkflowService ws)
         throws TimeoutException, InterruptedException
   {
      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, numberValue,
            ArchiveModelConstants.DATA_ID_TEXTDATA, textValue, qs, ws);

      completeNextActivity(pi, null, null, qs, ws);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return writeActivity;
   }

   private ActivityInstance completeSimpleManual(final ProcessInstance pi,
         QueryService qs, WorkflowService ws) throws TimeoutException,
         InterruptedException
   {
      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", qs, ws);

      completeNextActivity(pi, null, null, qs, ws);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return writeActivity;
   }

   private Date completeSubProcessesInModel(final ProcessInstance pi, QueryService qs,
         WorkflowService ws, boolean changeSubProcessDate) throws Exception
   {

      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      if (changeSubProcessDate)
      {
         testTimestampProvider.nextHour();
      }
      Date subDate = testTimestampProvider.getTimestamp();
      completeNextActivity(pi, ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, qs,
            ws);

      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      FilterAndTerm term = querySubSimple.getFilter().addAndTerm();
      term.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      ProcessInstances subProcessInstancesSimple = qs
            .getAllProcessInstances(querySubSimple);
      assertNotNull(subProcessInstancesSimple);
      assertEquals(1, subProcessInstancesSimple.size());
      ProcessInstance subSimple = subProcessInstancesSimple.iterator().next();
      completeNextActivity(subSimple, null, null, qs, ws);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      FilterAndTerm term1 = querySubManual.getFilter().addAndTerm();
      term1.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      ProcessInstances subProcessInstancesManual = qs
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      completeNextActivity(subSimpleManual, ArchiveModelConstants.DATA_ID_TEXTDATA,
            dataInput1, qs, ws);
      completeNextActivity(subSimpleManual, null, null, qs, ws);

      completeNextActivity(pi, null, null, qs, ws);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return subDate;
   }

   private ActivityInstance completeNextActivity(final ProcessInstance pi, String dataId,
         Object data, QueryService qs, WorkflowService ws)
   {
      final ActivityInstance ai1 = qs.findFirstActivityInstance(ActivityInstanceQuery
            .findAlive(pi.getProcessID()));
      Map<String, Object> outData;
      if (dataId != null)
      {
         outData = new HashMap<String, Object>();
         outData.put(dataId, data);
      }
      else
      {
         outData = null;
      }
      final ActivityInstance writeActivity = ws.activateAndComplete(ai1.getOID(), null,
            outData);
      return writeActivity;
   }

   private ActivityInstance completeNextActivity(final ProcessInstance pi, String dataId,
         Object data, String dataId2, Object data2, QueryService qs, WorkflowService ws)
   {
      final ActivityInstance ai1 = qs.findFirstActivityInstance(ActivityInstanceQuery
            .findAlive(pi.getProcessID()));
      Map<String, Object> outData;
      if (dataId != null)
      {
         outData = new HashMap<String, Object>();
         outData.put(dataId, data);
         outData.put(dataId2, data2);
      }
      else
      {
         outData = null;
      }
      final ActivityInstance writeActivity = ws.activateAndComplete(ai1.getOID(), null,
            outData);
      return writeActivity;

   }

   @Test
   public void testExportImportSubProcessesInModel() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);

      final ActivityInstance writeActivityOuter = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService,
            workflowService);

      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      assertNotNull(subProcessInstancesSimple);
      assertEquals(1, subProcessInstancesSimple.size());
      ProcessInstance subSimple = subProcessInstancesSimple.iterator().next();
      completeNextActivity(subSimple, null, null, queryService, workflowService);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      ActivityInstance writeActivitySub = completeNextActivity(subSimpleManual,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService,
            workflowService);
      completeNextActivity(subSimpleManual, null, null, queryService, workflowService);

      completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuerySubSimple = new ActivityInstanceQuery();
      aQuerySubSimple.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      ActivityInstanceQuery aQuerySubSimpleManual = new ActivityInstanceQuery();
      aQuerySubSimpleManual.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subSimpleManual.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances oldInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances oldActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances oldActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(oldInstances);
      assertNotNull(oldInstancesSubSimple);
      assertNotNull(oldInstancesSubManual);
      assertNotNull(oldActivities);
      assertNotNull(oldActivitiesSubSimple);
      assertNotNull(oldActivitiesSubSimpleManual);
      assertEquals(1, oldInstances.size());
      assertEquals(5, oldActivities.size());
      assertEquals(2, oldActivitiesSubSimple.size());
      assertEquals(3, oldActivitiesSubSimpleManual.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances clearedInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances clearedInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances clearedActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances clearedActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(clearedInstances);
      assertNotNull(clearedInstancesSubSimple);
      assertNotNull(clearedInstancesSubManual);
      assertNotNull(clearedActivities);
      assertNotNull(clearedActivitiesSubSimple);
      assertNotNull(clearedActivitiesSubSimpleManual);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedInstancesSubSimple.size());
      assertEquals(0, clearedInstancesSubManual.size());
      assertEquals(0, clearedActivities.size());
      assertEquals(0, clearedActivitiesSubSimple.size());
      assertEquals(0, clearedActivitiesSubSimpleManual.size());
      assertDataNotExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      assertDataNotExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances newInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances newInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances newActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances newActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertProcessInstancesEquals(oldInstancesSubSimple, newInstancesSubSimple);
      assertProcessInstancesEquals(oldInstancesSubManual, newInstancesSubManual);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertActivityInstancesEquals(oldActivitiesSubSimple, newActivitiesSubSimple);
      assertActivityInstancesEquals(oldActivitiesSubSimpleManual,
            newActivitiesSubSimpleManual);
      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

   }

   @Test
   public void testExportImportSubProcessesInModelExplicitRequestForSubProcesses()
         throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);

      final ActivityInstance writeActivityOuter = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService,
            workflowService);

      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      assertNotNull(subProcessInstancesSimple);
      assertEquals(1, subProcessInstancesSimple.size());
      ProcessInstance subSimple = subProcessInstancesSimple.iterator().next();
      completeNextActivity(subSimple, null, null, queryService, workflowService);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      ActivityInstance writeActivitySub = completeNextActivity(subSimpleManual,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService,
            workflowService);
      completeNextActivity(subSimpleManual, null, null, queryService, workflowService);

      completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuerySubSimple = new ActivityInstanceQuery();
      aQuerySubSimple.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      ActivityInstanceQuery aQuerySubSimpleManual = new ActivityInstanceQuery();
      aQuerySubSimpleManual.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subSimpleManual.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances oldInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances oldActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances oldActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(oldInstances);
      assertNotNull(oldInstancesSubSimple);
      assertNotNull(oldInstancesSubManual);
      assertNotNull(oldActivities);
      assertNotNull(oldActivitiesSubSimple);
      assertNotNull(oldActivitiesSubSimpleManual);
      assertEquals(1, oldInstances.size());
      assertEquals(5, oldActivities.size());
      assertEquals(2, oldActivitiesSubSimple.size());
      assertEquals(3, oldActivitiesSubSimpleManual.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID(), subSimple.getOID(),
            subSimpleManual.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances clearedInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances clearedInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances clearedActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances clearedActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(clearedInstances);
      assertNotNull(clearedInstancesSubSimple);
      assertNotNull(clearedInstancesSubManual);
      assertNotNull(clearedActivities);
      assertNotNull(clearedActivitiesSubSimple);
      assertNotNull(clearedActivitiesSubSimpleManual);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedInstancesSubSimple.size());
      assertEquals(0, clearedInstancesSubManual.size());
      assertEquals(0, clearedActivities.size());
      assertEquals(0, clearedActivitiesSubSimple.size());
      assertEquals(0, clearedActivitiesSubSimpleManual.size());
      assertDataNotExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      assertDataNotExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances newInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances newInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances newActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances newActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertProcessInstancesEquals(oldInstancesSubSimple, newInstancesSubSimple);
      assertProcessInstancesEquals(oldInstancesSubManual, newInstancesSubManual);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertActivityInstancesEquals(oldActivitiesSubSimple, newActivitiesSubSimple);
      assertActivityInstancesEquals(oldActivitiesSubSimpleManual,
            newActivitiesSubSimpleManual);
      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

   }

   @Test
   public void testExportImportSimpleNoActiveModel() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);
      int modelOID = pi.getModelOID();

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      adminService.deleteModel(modelOID);

      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.ERROR);
      barrier.registerWithLog4j();

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));

      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER);
      assertEquals(0, count);
      barrier
            .waitForLogMessage(
                  "Invalid environment to import into.* Current environment does not have an active model.*",
                  new WaitTimeout(5, TimeUnit.SECONDS));

   }

   @Test
   public void testExportImportSimpleDifferentModelDeployed() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      Models models = queryService.getModels(DeployedModelQuery.findAll());
      for (DeployedModelDescription model : models)
      {
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            adminService.deleteModel(model.getModelOID());
         }
      }
      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER2);

      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.ERROR);
      barrier.registerWithLog4j();

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));

      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER);
      barrier
            .waitForLogMessage(
                  "Invalid environment to import into.* Current environment does not have an active model with id.*",
                  new WaitTimeout(5, TimeUnit.SECONDS));

      assertEquals(0, count);
   }

   @Test
   public void testExportImportSimpleSameModelRedeployed() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);
      int modelOID = pi.getModelOID();

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, true));
      assertNotNullRawData(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      adminService.deleteModel(modelOID);
      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER);

      int count = (Integer) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT,
                  getBytes(rawData), null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(oldInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);
   }

   private boolean hasStructuredDateField(long processInstanceOid, String dataId,
         String fieldName, Object value) throws Exception
   {
      String sql = "select cd.stringvalue from data d inner join data_value dv on d.oid = dv.data"
            + " inner join clob_data cd on dv.number_value = cd.oid"
            + " where d.id = ?"
            + " and dv.processinstance = ?";
      String sql2 = "select sdv.string_value, number_value from data d inner join structured_data sd on d.oid = sd.data"
            + " inner join structured_data_value sdv on sd.oid = sdv.xpath "
            + " where sd.xpath = ?" + " and sdv.processinstance = ?";

      String sql3 ="select str.data from data d inner join structured_data sd on d.oid = sd.data"
            + " inner join structured_data_value sdv on sd.oid = sdv.xpath "
            + " inner join string_data str on str.objectid = sdv .oid"
            + " where sd.xpath = ?" + " and sdv.processinstance = ?";


      final DataSource ds = testClassSetup.dataSource();
      boolean result;

      Connection connection = null;
      PreparedStatement stmt = null;
      PreparedStatement stmt2 = null;
      PreparedStatement stmt3 = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.prepareStatement(sql);
         stmt.setString(1, dataId);
         stmt.setLong(2, processInstanceOid);

         stmt2 = connection.prepareStatement(sql2);
         stmt2.setString(1, fieldName);
         stmt2.setLong(2, processInstanceOid);
         final ResultSet rs = stmt.executeQuery();
         final ResultSet rs2 = stmt2.executeQuery();
         if (rs.next())
         {
            result = rs.getString(1).contains(">" + value + "</" + fieldName + ">");
         }
         else
         {
            result = false;
         }

         if (result)
         {
            if (rs2.next())
            {
               if (value instanceof String)
               {
                  if (((String) value).length() > 128)
                  {
                     result = rs2.getString(1).equals(((String) value).substring(0, 128));
                     
                     stmt3 = connection.prepareStatement(sql3);
                     stmt3.setString(1, fieldName);
                     stmt3.setLong(2, processInstanceOid);
                     final ResultSet rs3 = stmt3.executeQuery();
                     result = rs3.next();
                  }
                  else
                  {
                     result = rs2.getString(1).equals(((String) value));
                  }
               }
               else
               {
                  result = rs2.getBigDecimal(2).intValue() == (Integer)value;
               }
            }
            else
            {
               result = false;
            }
         }
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
            stmt2.close();
         }
         if (stmt3 != null)
         {
            stmt3.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }

      return result;
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
         String processName, String dataId, Serializable expectedValue,
         QueryService queryService)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            true, queryService);
   }

   private void assertDataNotExists(long processInstanceOid, long activityOid,
         String processName, String dataId, Serializable expectedValue,
         QueryService queryService)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            false, queryService);
   }

   private void checkDataValue(long processInstanceOid, long activityOid,
         String processName, String dataId, Serializable expectedValue,
         boolean shouldExists, QueryService queryService)
   {
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

   private void assertProcessInstancesEquals(ProcessInstances oldInstances,
         ProcessInstances newInstances) throws Exception
   {
      assertProcessInstancesEquals(oldInstances, newInstances, true);
   }

   private void assertProcessInstancesEquals(ProcessInstances oldInstances,
         ProcessInstances newInstances, boolean compareRTOids) throws Exception
   {
      int countCompared = 0;
      assertNotNull(newInstances);
      assertEquals(oldInstances.size(), newInstances.size());
      for (ProcessInstance process : oldInstances)
      {
         assertThat(
               NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("PROCINST_SCOPE", "SCOPEPROCESSINSTANCE",
                     process.getScopeProcessInstanceOID()), is(true));
         assertThat(
               NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("PROCINST_SCOPE", "ROOTPROCESSINSTANCE",
                     process.getRootProcessInstanceOID()), is(true));
         for (ProcessInstance newProcess : newInstances)
         {
            if (process.getOID() == newProcess.getOID())
            {
               assertObjectEquals(process, newProcess, process, compareRTOids);
               countCompared++;
               break;
            }
         }
      }
      assertEquals(oldInstances.size(), countCompared);
   }

   public static byte[] addAll(final byte[] array1, final byte[] array2)
   {
      final byte[] joinedArray = new byte[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   private byte[] getBytes(ExportResult result)
   {
      byte[] resultData;
      if (result == null)
      {
         resultData = null;
      }
      else
      {
         if (result.hasModelData() && !result.hasExportData())
         {
            resultData = result.getModelData();
         }
         else
         {
            if (result.hasModelData())
            {
               resultData = result.getModelData().clone();
            }
            else
            {
               resultData = new byte[] {};
            }
            if (result.getDates().size() == 1)
            {
               resultData = addAll(resultData,
                     result.getResults(result.getDates().iterator().next()));
            }
         }
      }

      return resultData;
   }

   private byte[] getBytes(ExportResult result, Date date)
   {
      byte[] resultData;
      if (result == null)
      {
         resultData = null;
      }
      else
      {
         if (result.hasModelData() && !result.hasExportData())
         {
            resultData = result.getModelData();
         }
         else
         {
            if (result.hasModelData())
            {
               resultData = result.getModelData().clone();
            }
            else
            {
               resultData = new byte[] {};
            }
            resultData = addAll(resultData, result.getResults(date));
         }
      }

      return resultData;
   }

   private void assertNotNullBatches(ExportResult result)
   {
      assertNotNull(result);
      assertFalse(result.hasModelData());
      assertTrue(result.hasExportData());
   }

   private void assertNotNullModel(ExportResult result)
   {
      assertNotNull(result);
      assertTrue(result.hasModelData());
      assertFalse(result.hasExportData());
   }

   private void assertNotNullRawData(ExportResult result)
   {
      assertNotNull(result);
      assertTrue(result.hasModelData());
      assertTrue(result.hasExportData());
   }

   private void assertNullRawData(ExportResult result)
   {
      assertFalse(result.hasModelData());
      assertFalse(result.hasExportData());
   }

   private void assertActivityInstancesEquals(ActivityInstances oldActivities,
         ActivityInstances newActivities) throws Exception
   {
      assertActivityInstancesEquals(oldActivities, newActivities, true);
   }

   private void assertActivityInstancesEquals(ActivityInstances oldActivities,
         ActivityInstances newActivities, boolean compareRTOids) throws Exception
   {
      int countCompared = 0;
      assertNotNull(newActivities);
      assertEquals(oldActivities.size(), newActivities.size());
      for (ActivityInstance activity : oldActivities)
      {
         assertThat(
               NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("ACTIVITY_INSTANCE", "OID", activity.getOID()),
               is(true));
         for (ActivityInstance newActivity : newActivities)
         {
            if (activity.getOID() == newActivity.getOID())
            {
               assertObjectEquals(activity, newActivity, activity, compareRTOids);
               countCompared++;
               break;
            }
         }
      }
      assertEquals(oldActivities.size(), countCompared);
   }

   private void assertObjectEquals(Object a, Object b, Object from, boolean compareRTOids)
         throws Exception
   {
      if (a == null && b == null)
      {
         return;
      }
      if (a == null && b != null)
      {
         fail("Original object was null but imported object is not: " + b);
         return;
      }
      if (a != null && b == null)
      {
         fail("Original object was not null but imported object is null: " + a);
         return;
      }
      BeanInfo beanInfo = Introspector.getBeanInfo(a.getClass());

      for (PropertyDescriptor property : beanInfo.getPropertyDescriptors())
      {
         Object valueA = property.getReadMethod().invoke(a);
         Object valueB = property.getReadMethod().invoke(b);
         if (property.getPropertyType().getPackage() != null
               && property.getPropertyType().getPackage().getName()
                     .contains("org.eclipse.stardust.engine"))
         {
            if (from.equals(valueA))
            {
               return;
            }
            assertObjectEquals(valueA, valueB, valueA, compareRTOids);
         }
         else
         {
            if (property.getPropertyType().isArray())
            {
               assertArrayEquals(a.getClass().getSimpleName() + " " + property.getName()
                     + " not equals. Expected " + valueA + " but got " + valueB,
                     (Object[]) valueA, (Object[]) valueB);
            }
            else if (Collection.class.isAssignableFrom(property.getPropertyType()))
            {

            }
            else
            {

               boolean mustTest = false;

               if ("modeloid".equalsIgnoreCase(property.getName()) && !compareRTOids)
               {
                  mustTest = false;
               }
               else if ("runtimeElementOID".equals(property.getName()) && !compareRTOids)
               {
                  mustTest = false;
               }
               else
               {
                  mustTest = true;
               }

               if (mustTest)
               {
                  assertEquals(a.getClass().getSimpleName() + " " + property.getName()
                        + " not equals. Expected " + valueA + " but got " + valueB,
                        valueA, valueB);
               }
            }
         }
      }
   }

   @SuppressWarnings("unused")
   private void assertObjectEqualsAlt(Object a, Object b, Object from) throws Exception
   {
      if (a == null && b == null)
      {
         return;
      }
      if (a == null && b != null)
      {
         fail("Original object was null but imported object is not: " + b);
         return;
      }
      if (a != null && b == null)
      {
         fail("Original object was not null but imported object is null: " + a);
         return;
      }

      Method[] methods = a.getClass().getMethods();
      for (Method method : methods)
      {
         if (method.getParameterTypes().length == 0 && method.getName().startsWith("get"))
         {
            if (method.getReturnType() != null)
            {
               Object valueA = method.invoke(a);
               Object valueB = method.invoke(b);

               if (method.getReturnType().getPackage() != null
                     && method.getReturnType().getPackage().getName()
                           .contains("org.eclipse.stardust.engine"))
               {
                  if (from.equals(valueA))
                  {
                     return;
                  }
                  assertObjectEqualsAlt(valueA, valueB, valueA);
               }
               else
               {
                  if (method.getReturnType().isArray())
                  {
                     assertArrayEquals(method.getName() + " not equals. Expected "
                           + valueA + " but got " + valueB, (Object[]) valueA,
                           (Object[]) valueB);
                  }
                  else
                  {
                     assertEquals(method.getName() + " not equals. Expected " + valueA
                           + " but got " + valueB, valueA, valueB);
                  }
               }
            }
         }

      }

   }
}
