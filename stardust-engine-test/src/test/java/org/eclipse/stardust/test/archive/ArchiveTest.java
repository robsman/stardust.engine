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
import java.math.BigDecimal;
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
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import com.google.gson.Gson;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.pojo.AuditTrailPartitionManager;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand.ExportMetaData;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceProperty;
import org.eclipse.stardust.engine.core.runtime.beans.PreferencesBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceProperty;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.*;

/*
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
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS,
         ArchiveModelConstants.MODEL_ID, ArchiveModelConstants.MODEL_ID_OTHER);

   private static TestTimestampProvider testTimestampProvider = new TestTimestampProvider();

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @BeforeClass
   public static void clearManagers()
   {
      ArchiveManagerFactory.resetArchiveManagers();
   }
   
   @Before 
   public void init() throws Exception
   {
      setUp();
      ArchiveTest.deletePreferences();
      int id = ((BigDecimal)ArchiveTest.getEntryInDbForObject("PARTITION", "id", "default", "oid")).intValue();
      createPreference(id, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      createPreference(id,  ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");
      createPreference(id, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_ID,
            "testid");
      createPreference(id, ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "false");
   }
   
   public void setUp() throws Exception
   { 
      testTimestampProvider = new TestTimestampProvider();
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE,
            testTimestampProvider);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            3);

   }

   @After
   public void tearDown() throws Exception
   {
      clearArchiveManager("default");
      clearArchiveManager(PARTION_A);
      clearArchiveManager(PARTION_B);
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE, null);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            null);
   }

   protected static void clearArchiveManager(String partition)
   {
      try
      {
         IArchiveManager archiveManager = ArchiveManagerFactory.getArchiveManager(partition);
         ((MemoryArchiveManager) archiveManager).clear();
      }
      catch (Exception e)
      {
         //ignoring incase default archivemanager doesnt exist
      }
   }
       
   @Test
   public void testArchiveManagerId() throws Exception
   {
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_B, "sysop");

      Map<String, Object> propertiesA = new HashMap<String, Object>();
      Map<String, Object> propertiesB = new HashMap<String, Object>();
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

      RtEnvHome.deployModel(asA, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deployModel(asB, null, ArchiveModelConstants.MODEL_ID);
      setUp();
      
      int partitionIdA = ((BigDecimal)getEntryInDbForObject("PARTITION", "id", PARTION_A, "oid")).intValue();
      int partitionIdB = ((BigDecimal)getEntryInDbForObject("PARTITION", "id", PARTION_B, "oid")).intValue();
      createPreference(partitionIdA, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      createPreference(partitionIdA,  ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");
      createPreference(partitionIdB, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      createPreference(partitionIdB,  ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");
     
      startAndCompleteSimple(wsA, qsA);
      startAndCompleteSimple(wsB, qsB);
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(wsA, filter);      
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(wsB, filter);      
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = findArchives(wsA, filter, 1);
      IArchive archive = archives.get(0);
      assertEquals(PARTION_A, archive.getArchiveManagerId());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      archives = findArchives(wsB, filter, 1);
      archive = archives.get(0);
      assertEquals(PARTION_B, archive.getArchiveManagerId());

      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_B, "sysop");
   }
   
   @Test
   public void testMultiPartition() throws Exception
   {
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_B, "sysop");

      Map<String, Object> propertiesA = new HashMap<String, Object>();
      Map<String, Object> propertiesB = new HashMap<String, Object>();
      propertiesA.put(SecurityProperties.PARTITION, PARTION_A);
      propertiesB.put(SecurityProperties.PARTITION, PARTION_B);
      
      int partitionIdA = ((BigDecimal)getEntryInDbForObject("PARTITION", "id", PARTION_A, "oid")).intValue();
      int partitionIdB = ((BigDecimal)getEntryInDbForObject("PARTITION", "id", PARTION_B, "oid")).intValue();
      createPreference(partitionIdA, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      createPreference(partitionIdA,  ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");
      createPreference(partitionIdB, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      createPreference(partitionIdB,  ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");

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

      RtEnvHome.deployModel(asA, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deployModel(asB, null, ArchiveModelConstants.MODEL_ID);
      setUp();
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
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);

      ExportResult exportResultA = (ExportResult) wsA.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNotNullExportResult(exportResultA);
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResultB = (ExportResult) wsB.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNotNullExportResult(exportResultB);
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResultA, null);
      Boolean success = (Boolean) wsA.execute(command);
      command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResultB, null);
      success = (Boolean) wsB.execute(command);
      assertTrue(success);
  
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
      @SuppressWarnings("unchecked")
      List<IArchive> archivesA = (List<IArchive>) wsA
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archivesA.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archivesB = (List<IArchive>) wsB
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archivesB.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) wsA
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archivesA.get(0),
                  filter, null, null));
      assertEquals(1, count);
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      count = (Integer) wsB
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archivesB.get(0),
                  filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstancesA = qsA.getAllProcessInstances(pQuery);
      ActivityInstances newActivitiesA = qsA.getAllActivityInstances(aQuery);
      ProcessInstances newInstancesB = qsB.getAllProcessInstances(pQuery);
      ActivityInstances newActivitiesB = qsB.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(qsA, oldInstancesA, newInstancesA);
      assertActivityInstancesEquals(oldActivitiesA, newActivitiesA);
      assertProcessInstancesEquals(qsB, oldInstancesB, newInstancesB);
      assertActivityInstancesEquals(oldActivitiesB, newActivitiesB);
      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.dropAuditTrailPartition(PARTION_B, "sysop");
   }

   @Test
   public void testPartitionValidation() throws Exception
   {
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_A, "sysop");
      AuditTrailPartitionManager.createAuditTrailPartition(PARTION_B, "sysop");

      Map<String, Object> propertiesA = new HashMap<String, Object>();
      Map<String, Object> propertiesB = new HashMap<String, Object>();
      propertiesA.put(SecurityProperties.PARTITION, PARTION_A);
      propertiesB.put(SecurityProperties.PARTITION, PARTION_B);

      int partitionIdA = ((BigDecimal)getEntryInDbForObject("PARTITION", "id", PARTION_A, "oid")).intValue();
      int partitionIdB = ((BigDecimal)getEntryInDbForObject("PARTITION", "id", PARTION_B, "oid")).intValue();
      createPreference(partitionIdA, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      createPreference(partitionIdA,  ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");
      createPreference(partitionIdB, ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      createPreference(partitionIdB,  ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");
      
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

      RtEnvHome.deployModel(asA, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deployModel(asB, null, ArchiveModelConstants.MODEL_ID);
      setUp();
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


      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(wsA, filter);

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
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) wsA
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.ERROR);
      barrier.registerWithLog4j();
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) wsB.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(2, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      adminService.deleteModel(piOtherModel.getModelOID());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
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
      ArchiveFilter filter = new ArchiveFilter(null, null,null, modelOids, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, instances.size());
      assertEquals(3, activitiesCleared.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(piOtherModel));
      assertActivityInstancesEquals(oldActivities, newActivities);

   }
   
   @Test
   public void testMultiModelImportFilterByModelIDWithDep() throws Exception
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      List<String> modelids = Arrays.asList(ArchiveModelConstants.MODEL_ID_OTHER);
      filter = new ArchiveFilter(modelids, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(newInstances);
      assertNotNull(newActivities);
      assertEquals(1, newInstances.size());
      assertEquals(4, newActivities.size());
      assertExportIds(queryService, newInstances, newInstances, true);
   }

   @Test
   public void testMultiModelImportFilterByModelIDInvalid() throws Exception
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      List<String> modelids = Arrays.asList("x");
      filter = new ArchiveFilter(modelids, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(newInstances);
      assertNotNull(newActivities);
      assertEquals(0, newInstances.size());
      assertEquals(0, newActivities.size());
   }
   
   @Test
   public void testMultiModelExportFilterByModelIDWithDep() throws Exception
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

      List<String> modelids = Arrays.asList(ArchiveModelConstants.MODEL_ID_OTHER);
      ArchiveFilter filter = new ArchiveFilter(modelids, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, instances.size());
      assertEquals(3, activitiesCleared.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(piOtherModel));
      assertActivityInstancesEquals(oldActivities, newActivities);

   }

   @Test
   public void testMultiModelExportFilterByModelIDInvalid() throws Exception
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

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(2, oldInstances.size());
      assertEquals(7, oldActivities.size());

      List<String> modelids = Arrays.asList("x");
      ArchiveFilter filter = new ArchiveFilter(modelids, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNotNull(exportResult);
      assertEquals(0, exportResult.getPurgeProcessIds().size());
      assertEquals(0, exportResult.getDates().size());

      archive(workflowService, exportResult);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(2, oldInstances.size());
      assertEquals(7, oldActivities.size());
   }

   private void archive(WorkflowService workflowService, ExportResult exportResult)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResult, null);
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);
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
      ArchiveFilter filter = new ArchiveFilter(null, null,null, modelOids, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, instances.size());
      assertEquals(4, activitiesCleared.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(queryService,oldInstances, newInstances, Arrays.asList(piModel));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testRedeployBeforeArchive() throws Exception
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

      RtEnvHome.deployModel(adminService, null, ArchiveModelConstants.MODEL_ID);
      setUp();
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);
      
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      IArchive archive = archives.get(0);
      
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);
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

      RtEnvHome.deployModel(adminService, null, ArchiveModelConstants.MODEL_ID);
      setUp();
      List<Integer> modelOids = Arrays.asList(pi.getModelOID());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, modelOids, null, null, null);
      ExportMetaData exportMetaData = (ExportMetaData) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY, filter, null));
      assertNotNull(exportMetaData);
      ExportResult exportResultModel = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.EXPORT_MODEL, exportMetaData, null));
      ExportResult data = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.EXPORT_BATCH, exportMetaData, null));
      assertNotNull(data);
      ExportResult exportResult = ExportImportSupport.merge(Arrays.asList(data),
            exportResultModel.getExportModelsByDate());
      assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      IArchive archive = archives.get(0);
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);
   }

   @Test
   public void importNull()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, null, filter, null, null));
      assertEquals(0, count);
   }

   @Test
   public void invalidProcessInstanceOid()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      List<Long> oids = Arrays.asList(-1L);
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      archive(workflowService, exportResult);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(0, archives.size());
      assertNullRawData(exportResult);
   }

   @Test
   public void testExportNoData() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNullRawData(exportResult);
   }

   @Test
   public void invalidProcessInstanceOidNull()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      Long oid = null;
      List<Long> oids = Arrays.asList(oid);
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNullRawData(exportResult);
   }

   @Test
   public void invalidProcessInstanceOidBlankList()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      List<Long> oids = new ArrayList<Long>();
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNullRawData(exportResult);
   }

   @Test
   public void testArchiveNotCompletedOrAborted() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNullRawData(exportResult);
      archive(workflowService, exportResult);
      // ensure it was not purged
      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(1, oldInstances.size());
   }
   
   @Test
   public void testArchiveSubProcessCompleted() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      
      // start subprocesses process and do everything except completing it
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
      assertEquals(4, oldActivities.size());
      assertEquals(2, oldActivitiesSubSimple.size());
      assertEquals(3, oldActivitiesSubSimpleManual.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());
      for (ActivityInstance activity : oldActivities)
      {
         createActivityInstanceProperty(activity);
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         createActivityInstanceProperty(activity);
      }
      for (ActivityInstance activity : oldActivities)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }

      // dump the process that is still active
      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNullRawData(exportResult);
   }
   
   @Test
   public void testDumpNotCompletedOrAborted() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      
      // start subprocesses process and do everything except completing it
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
      assertEquals(4, oldActivities.size());
      assertEquals(2, oldActivitiesSubSimple.size());
      assertEquals(3, oldActivitiesSubSimpleManual.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());
      for (ActivityInstance activity : oldActivities)
      {
         createActivityInstanceProperty(activity);
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         createActivityInstanceProperty(activity);
      }
      for (ActivityInstance activity : oldActivities)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }

      // dump the process that is still active
      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  "c"));
      assertNotNullExportResult(exportResult);

      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResult, "c");
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);

      // check active process is not deleted
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
      assertEquals(1, clearedInstances.size());
      assertEquals(1, clearedInstancesSubSimple.size());
      assertEquals(1, clearedInstancesSubManual.size());
      assertEquals(4, clearedActivities.size());
      assertEquals(2, clearedActivitiesSubSimple.size());
      assertEquals(3, clearedActivitiesSubSimpleManual.size());
      
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      IArchive dump = archives.get(0);
      assertEquals("c", dump.getExportIndex().getDumpLocation());
      
      // now complete process and export it
      completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNotNullExportResult(exportResult);

      command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResult, null);
      success = (Boolean) workflowService.execute(command);
      assertTrue(success);

      // check process now removed from db
      clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      clearedInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      clearedInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      clearedActivities = queryService.getAllActivityInstances(aQuery);
      clearedActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      clearedActivitiesSubSimpleManual = queryService
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
      
      //import the dump
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, dump, filter, null, null));
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

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, oldInstances, true, false);
      assertProcessInstancesEquals(queryService,oldInstancesSubSimple, newInstancesSubSimple,
            oldInstancesSubSimple, true, false);
      assertProcessInstancesEquals(queryService,oldInstancesSubManual, newInstancesSubManual,
            oldInstancesSubManual, true, false);
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

      for (ActivityInstance activity : oldActivities)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }

   }

   @Test
   public void testExportAllNullDates() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, modelOids, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(7, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }

   @Test
   public void testExportAllFilterFromAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date toDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(4, clearedInstances.size());
      assertEquals(13, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(simpleManualB, simpleA, simpleB));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   @Test
   public void testFilterDateArchive() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date startP1 = testTimestampProvider.getTimestamp();
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      testTimestampProvider.nextDay();
      Date midP1 = testTimestampProvider.getTimestamp();
      completeNextActivity(pi, ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data",
            queryService, workflowService);

      testTimestampProvider.nextDay();
      Date endP1 = testTimestampProvider.getTimestamp();
      completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      testTimestampProvider.nextDay();
            
      final ProcessInstance pi2 = startAndCompleteSimpleManual(workflowService,
            queryService);
      
      ProcessInstanceStateBarrier.instance().await(pi2.getOID(),
            ProcessInstanceState.Completed);
     
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
     
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
     
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(2, oldInstances.size());
      assertEquals(6, oldActivities.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, startP1, midP1, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNullRawData(exportResult);

      filter = new ArchiveFilter(null, null,null, null, midP1, endP1, null);
      exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNotNullExportResult(exportResult);
      
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(1, clearedInstances.size());
      assertEquals(3, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      IArchive archive = archives.get(0);
      assertNotNull(archive.getExportIndex().getOidsToUuids().get(pi.getOID()));
      assertNull(archive.getExportIndex().getOidsToUuids().get(pi2.getOID()));
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, Arrays.asList(pi), false);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testFilterDateDump() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date startP1 = testTimestampProvider.getTimestamp();
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      testTimestampProvider.nextDay();
      Date midP1 = testTimestampProvider.getTimestamp();
      completeNextActivity(pi, ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data",
            queryService, workflowService);

      testTimestampProvider.nextDay();
      completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      testTimestampProvider.nextDay();
            
      final ProcessInstance pi2 = startAndCompleteSimpleManual(workflowService,
            queryService);
      
      ProcessInstanceStateBarrier.instance().await(pi2.getOID(),
            ProcessInstanceState.Completed);
     
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
     
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
     
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(2, oldInstances.size());
      assertEquals(6, oldActivities.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, startP1, midP1, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  "c"));

      assertNotNullExportResult(exportResult);
      
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResult, "c");
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(2, clearedInstances.size());
      assertEquals(6, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      IArchive archive = archives.get(0);
      assertNotNull(archive.getExportIndex().getOidsToUuids().get(pi.getOID()));
      assertNull(archive.getExportIndex().getOidsToUuids().get(pi2.getOID()));
   }
   
   @Test
   public void testImportFilterCombinedCriteriaWithResult() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB
            .getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));
      ProcessInstances expectedInstances = queryService.getAllProcessInstances(pExpectedQuery);
      ActivityInstances expectedActivities = queryService.getAllActivityInstances(aExpectedQuery);
      
      assertNotNull(expectedInstances);
      assertNotNull(expectedActivities);
      assertEquals(2, expectedInstances.size());
      assertEquals(5, expectedActivities.size());
      
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); assertNotNull(exportResult);
     
                  assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = findArchives(workflowService, filter, 1);

      List<String> procDefIds = Arrays.asList("Simple","SimpleManual");
      List<Long> processInstanceOids = Arrays.asList(simpleA.getOID(), simpleManualB.getOID());
     
      filter = new ArchiveFilter(null, procDefIds, processInstanceOids, null, null, null, null);
    
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(2, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,expectedInstances, newInstances,
            Arrays.asList(simpleA, simpleManualB));
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testImportFilterCombinedCriteriaWithNoResult() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB
            .getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));
      ProcessInstances expectedInstances = queryService.getAllProcessInstances(pExpectedQuery);
      ActivityInstances expectedActivities = queryService.getAllActivityInstances(aExpectedQuery);
      
      assertNotNull(expectedInstances);
      assertNotNull(expectedActivities);
      assertEquals(2, expectedInstances.size());
      assertEquals(5, expectedActivities.size());
      
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); assertNotNull(exportResult);
     
                  assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = findArchives(workflowService, filter, 1);

      List<String> procDefIds = Arrays.asList("Simple","SimpleManual");
      List<Long> processInstanceOids = Arrays.asList(subProcessesInModel.getOID());
     
      filter = new ArchiveFilter(null, procDefIds, processInstanceOids, null, null, null, null);
    
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertEquals(0, newInstances.size());
      assertEquals(0, newActivities.size());
   }


   @Test
   public void testImportFilterProcessDefinitionIds() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(scriptProcess.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(scriptProcess.getOID()));
      ProcessInstances expectedInstances = queryService.getAllProcessInstances(pExpectedQuery);
      ActivityInstances expectedActivities = queryService.getAllActivityInstances(aExpectedQuery);
      
      assertNotNull(expectedInstances);
      assertNotNull(expectedActivities);
      assertEquals(3, expectedInstances.size());
      assertEquals(12, expectedActivities.size());
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = findArchives(workflowService, filter, 1);

      List<String> procDefIds = Arrays.asList("Simple", "ScriptProcess");
      filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
     
      assertProcessInstancesEquals(queryService,expectedInstances, newInstances,
            Arrays.asList(scriptProcess, simpleA, simpleB));
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @SuppressWarnings("unchecked")
   private List<IArchive> findArchives(WorkflowService workflowService,
         ArchiveFilter filter, int count)
   {
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(count, archives.size());
      return archives;
   }
   
   @Test
   public void testImportFilterProcessDefinitionIdsWithSubs() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subProcessesInModel.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subSimple.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subManual.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subProcessesInModel.getOID()));
      ProcessInstances expectedInstances = queryService.getAllProcessInstances(pExpectedQuery);
      ActivityInstances expectedActivities = queryService.getAllActivityInstances(aExpectedQuery);
      
      assertNotNull(expectedInstances);
      assertNotNull(expectedActivities);
      assertEquals(3, expectedInstances.size());
      assertEquals(10, expectedActivities.size());
      
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); 
      assertNotNull(exportResult);
     
                  assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      List<String> procDefIds = Arrays.asList("CallSubProcessesInModel");
      filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,expectedInstances, newInstances,
            Arrays.asList(subProcessesInModel, subManual, subSimple));
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }
   
   @Test
   public void testImportFilterProcessDefinitionIdsNotFindSub() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
     
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

      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
     
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
     
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(10, oldActivities.size());
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      List<String> procDefIds = Arrays.asList("Simple");
      filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertEquals(0, newInstances.size());
      assertEquals(0, newActivities.size());
   }
   
   @Test
   public void testImportFilterProcessDefinitionIdsInvalid() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null, null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      List<String> ids = Arrays.asList("x");
      filter = new ArchiveFilter(null, ids,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertEquals(0, newInstances.size());
      assertEquals(0, newActivities.size());
   }
   
   @Test
   public void testExportAllFilterProcessDefinitionIds() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      List<String> procDefIds = Arrays.asList("Simple", "ScriptProcess");
      
      ArchiveFilter filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(16, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(scriptProcess, simpleA, simpleB));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   @Test
   public void testExportAllFilterProcessDefinitionIdsWithSubs() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      List<String> procDefIds = Arrays.asList("CallSubProcessesInModel");
      
      ArchiveFilter filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); assertNotNull(exportResult);
      assertEquals(3, exportResult.getPurgeProcessIds().size());
      assertEquals(1, exportResult.getDates().size());
      assertEquals(3, exportResult.getExportIndex(exportResult.getDates().iterator().next()).getOidsToUuids().size());

      assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(18, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(subProcessesInModel, subManual, subSimple));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   @Test
   public void testExportAllFilterProcessDefinitionIdsNotFindSub() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
     
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

      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
     
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
     
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(10, oldActivities.size());
      
      List<String> procDefIds = Arrays.asList("Simple");
      
      ArchiveFilter filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));

      assertNullRawData(exportResult);
      
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(3, clearedInstances.size());
      assertEquals(10, clearedActivities.size());
   }
   
   @Test
   public void testExportAllFilterProcessDefinitionIdsInvalid() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

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

      List<String> ids = Arrays.asList("x");
      ArchiveFilter filter = new ArchiveFilter(null, ids, null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNullRawData(exportResult);
      
      archive(workflowService, exportResult);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
   }
   
   @SuppressWarnings("unchecked")
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
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextHour();
      Date simpleDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
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
      final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm",
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, simpleManualADate, scriptProcessDate, null);
      ExportResult result = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));

      archive(workflowService, result);
      assertEquals(5, result.getDates().size());
      assertNotNull(result.getResults(scriptProcessDate));
      assertNotNull(result.getResults(simpleManualADate));
      assertNotNull(result.getResults(simpleManualBDate));
      assertNotNull(result.getResults(simpleDate));
      // assert that sub processes are archived with their root process
      assertNull(result.getResults(subProcessesDate));
      assertNotNull(result.getResults(subProcessesInModelDate));

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(5, archives.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ImportMetaData meta = (ImportMetaData) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE, archives.get(0), filter,  null, null));
      assertNotNull(meta);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, getArchive(scriptProcessDate,
                  archives), filter, meta, null));
      assertEquals(1, count);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, getArchive(simpleManualADate,
                  archives), filter, meta, null));
      assertEquals(2, count);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, getArchive(simpleManualBDate,
                  archives), filter, meta, null));
      assertEquals(3, count);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, getArchive(simpleDate, archives),
            filter, meta, null));
      assertEquals(5, count);
      // assert that sub processes are archived with their root process
      assertNull(getArchive(subProcessesDate, archives));

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, getArchive(subProcessesInModelDate,
                  archives), filter, meta, null));
      assertEquals(8, count);

      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }

   public static IArchive getArchive(Date date, List<IArchive> archives)
   {
      for (IArchive archive : archives)
      {
         if (date.equals(((MemoryArchive)archive).getDate()))
         {
            return archive;
         }
      }
      return null;
   }

   @Test
   public void testExportImportOperations() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      testTimestampProvider.nextDay();
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY, filter, null);
      ExportMetaData exportMetaData = (ExportMetaData) workflowService.execute(command);
      List<ExportMetaData> batches = ExportImportSupport.partition(exportMetaData, 5);
      List<ExportResult> datas = new ArrayList<ExportResult>();
      assertEquals(8, exportMetaData.getAllProcessesForExport(false).size());
      assertEquals(6, exportMetaData.getRootToSubProcesses().keySet().size());

      command = new ExportProcessesCommand(ExportProcessesCommand.Operation.EXPORT_MODEL,
            exportMetaData, null);
      ExportResult modelData = (ExportResult) workflowService.execute(command);
      assertNotNullModel(modelData);

      for (ExportMetaData batch : batches)
      {
         command = new ExportProcessesCommand(
               ExportProcessesCommand.Operation.EXPORT_BATCH, batch, null);
         ExportResult exportResult = (ExportResult) workflowService.execute(command);
         assertNotNullBatches(exportResult);
         datas.add(exportResult);
      }

      ExportResult exportResult = ExportImportSupport.merge(datas,
            modelData.getExportModelsByDate());
      command = new ExportProcessesCommand(ExportProcessesCommand.Operation.ARCHIVE,
            exportResult, null);
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);

      assertEquals(2, batches.size());
      assertEquals(2, datas.size());

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(2, archives.size());
      ImportProcessesCommand importCommand = new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, archives.get(0),null, null, null);
      ImportMetaData importMetaData1 = (ImportMetaData) workflowService
            .execute(importCommand);
      assertNotNull(importMetaData1);
      assertNotNull(importMetaData1.getImportId(ModelBean.class,
            new Long(simpleA.getModelOID())));
      importCommand = new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, archives.get(1), null, null, null);
      ImportMetaData importMetaData2 = (ImportMetaData) workflowService
            .execute(importCommand);
      assertNotNull(importMetaData2);
      assertNotNull(importMetaData2.getImportId(ModelBean.class,
            new Long(simpleA.getModelOID())));


      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      importCommand = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
            archives.get(0), filter, importMetaData1, null);
      int count = (Integer) workflowService.execute(importCommand);
      assertTrue(count > 0);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      importCommand = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
            archives.get(1), filter, importMetaData2, null);
      count += (Integer) workflowService.execute(importCommand);
      assertEquals(8, count);

      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }

   private void assertProcessAndActivities(QueryService queryService,
         ProcessInstanceQuery pQuery, ActivityInstanceQuery aQuery,
         ProcessInstances oldInstances, ActivityInstances oldActivities) throws Exception
   {
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportManyMinutes() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date startDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextMinute();
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextMinute();
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextMinute();
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextMinute();
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      testTimestampProvider.nextMinute();
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);
      Date endDate = testTimestampProvider.getTimestamp();

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY, filter, null);
      ExportMetaData exportMetaData = (ExportMetaData) workflowService.execute(command);
      List<ExportMetaData> batches = ExportImportSupport.partition(exportMetaData, 5);
      List<ExportResult> datas = new ArrayList<ExportResult>();
      assertEquals(8, exportMetaData.getAllProcessesForExport(false).size());
      assertEquals(6, exportMetaData.getRootToSubProcesses().keySet().size());

      command = new ExportProcessesCommand(ExportProcessesCommand.Operation.EXPORT_MODEL,
            exportMetaData, null);
      ExportResult modelData = (ExportResult) workflowService.execute(command);
      assertNotNullModel(modelData);

      Date indexDate = ExportImportSupport.getIndexDateTime(startDate);
      for (ExportMetaData batch : batches)
      {
         command = new ExportProcessesCommand(
               ExportProcessesCommand.Operation.EXPORT_BATCH, batch, null);
         ExportResult exportResult = (ExportResult) workflowService.execute(command);
         assertNotNullBatches(exportResult);
         datas.add(exportResult);
      }

      
      ExportResult exportResult = ExportImportSupport.merge(datas,
            modelData.getExportModelsByDate());
      assertEquals(6, exportResult.getExportIndex(indexDate).getFields().get(ExportIndex.FIELD_START_DATE).size());
      command = new ExportProcessesCommand(ExportProcessesCommand.Operation.ARCHIVE,
            exportResult, null);
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);

      assertEquals(2, batches.size());
      assertEquals(2, datas.size());

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, startDate, endDate, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      ImportProcessesCommand importCommand = new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, archives.get(0),null,  null, null);
      ImportMetaData importMetaData1 = (ImportMetaData) workflowService
            .execute(importCommand);
      assertNotNull(importMetaData1);
      assertNotNull(importMetaData1.getImportId(ModelBean.class,
            new Long(simpleA.getModelOID())));
     
      importCommand = new ImportProcessesCommand(ImportProcessesCommand.Operation.IMPORT,
            archives.get(0), filter, importMetaData1, null);
      int count = (Integer) workflowService.execute(importCommand);
      assertTrue(count > 0);
      assertEquals(8, count);

      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }
   
   @Test
   public void testExportImportModel() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY, filter, null);
      ExportMetaData exportMetaData = (ExportMetaData) workflowService.execute(command);

      command = new ExportProcessesCommand(ExportProcessesCommand.Operation.EXPORT_MODEL,
            exportMetaData, null);
      ExportResult exportResult = (ExportResult) workflowService.execute(command);
      assertNotNullModel(exportResult);
      HashMap<Long, byte[]> data = new HashMap<Long, byte[]>();
      data.put(1L, new byte[] {1});
      String json = getExportIndexJSON(workflowService);

      MemoryArchive archive = new MemoryArchive("key", testTimestampProvider.getTimestamp(),
            data, getJSON(exportResult.getExportModel(testTimestampProvider.getTimestamp())), json);

      ImportMetaData importMetaData = (ImportMetaData) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE, archive, null, null, null));

      assertNotNull(importMetaData);
      assertNotNull(importMetaData.getImportId(ModelBean.class,
            new Long(pi.getModelOID())));

   }

   private String getExportIndexJSON(WorkflowService workflowService)
   {
      ArchiveFilter filter = new ArchiveFilter(null, null, null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      Map<Long, List<Long>> oids = new HashMap<Long, List<Long>>();
      String uuid = getArchiveManagerId("default") + "_" + 1 + "_"
            + testTimestampProvider.getTimestamp().getTime();
      String start = "2015/03/05 00:00:00:000";
      String end = "2015/03/05 13:00:00:000";
      oids.put(1L, new ArrayList<Long>());
      ExportIndex exportIndex = new ExportIndex(getArchiveManagerId("default"), getDateFormat("default"), "c");
      List<Long> subProcesses = new ArrayList<Long>();
      exportIndex.getRootProcessToSubProcesses().put(1L, subProcesses);
      exportIndex.setUuid(1L, uuid);
      exportIndex.addField(1L, ExportIndex.FIELD_START_DATE, start);
      exportIndex.addField(1L, ExportIndex.FIELD_END_DATE, end);
      String json = getJSON(exportIndex);
      return json;
   }

   private String getDateFormat(String partition)
   {
      return ArchiveManagerFactory.getDateFormat(partition);
   }

   private String getArchiveManagerId(String partition)
   {
      return ArchiveManagerFactory.getCurrentId(partition);
   }

   private String getJSON(ExportIndex index)
   {
      Gson gson = ExportImportSupport.getGson();
      String json = gson.toJson(index);
      return json;
   }
   
   private String getJSON(ExportModel exportModel)
   {
      Gson gson = ExportImportSupport.getGson();
      String json = gson.toJson(exportModel);
      return json;
   }

   @Test(expected = ServiceCommandException.class)
   public void testExportInvalidDateRange() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      Date fromDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      Date toDate = testTimestampProvider.getTimestamp();

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, toDate, fromDate, null);
      workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT,filter,
            null));
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

      startAndCompleteSimple(workflowService, queryService);

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, toDate, fromDate, null);
      workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0),
            filter, null, null));
      fail("Invalid date ranges. Test should not get here");
   }

   @Test
   public void testImportInvalidBadData() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      HashMap<Long, byte[]> dataByProcess = new HashMap<Long, byte[]>();
      dataByProcess.put(1L, new byte[] {5});
      String json = getExportIndexJSON(workflowService);
      ExportModel exportModel = new ExportModel(new HashMap<String, Long>(), 
            new HashMap<Integer, String>(), new HashMap<String, String>(), "");
      MemoryArchive archive = new MemoryArchive("key",testTimestampProvider.getTimestamp(),
            dataByProcess, getJSON(exportModel), json);
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(0, count);
   }

   @Test
   public void testImportInvalidDataEOF() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      HashMap<Long, byte[]> dataByProcess = new HashMap<Long, byte[]>();
      dataByProcess.put(1L, new byte[] {BlobBuilder.SECTION_MARKER_EOF});
      String json = getExportIndexJSON(workflowService);
      ExportModel exportModel = new ExportModel(new HashMap<String, Long>(), 
           new HashMap<Integer, String>(), new HashMap<String, String>(), "");
      
      MemoryArchive archive = new MemoryArchive("key",testTimestampProvider.getTimestamp(),
            dataByProcess, getJSON(exportModel), json);
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(0, count);
   }

   @Test
   public void testImportInvalidDataInstances() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      HashMap<Long, byte[]> dataByProcess = new HashMap<Long, byte[]>();
      dataByProcess.put(1L, new byte[] {BlobBuilder.SECTION_MARKER_INSTANCES});
      String json = getExportIndexJSON(workflowService);
      ExportModel exportModel = new ExportModel(new HashMap<String, Long>(), 
            new HashMap<Integer, String>(), new HashMap<String, String>(), "");
      MemoryArchive archive = new MemoryArchive("key",testTimestampProvider.getTimestamp(),
            dataByProcess, getJSON(exportModel), json);
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(0, count);
   }

   @Test
   public void testImportInvalidDataInstancesBadData() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      HashMap<Long, byte[]> dataByProcess = new HashMap<Long, byte[]>();
      dataByProcess.put(1L, new byte[] {BlobBuilder.SECTION_MARKER_INSTANCES, 5});
      String json = getExportIndexJSON(workflowService);
      ExportModel exportModel = new ExportModel(new HashMap<String, Long>(), 
            new HashMap<Integer, String>(), new HashMap<String, String>(), "");
      MemoryArchive archive = new MemoryArchive("key",testTimestampProvider.getTimestamp(),
            dataByProcess, getJSON(exportModel), json);
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(0, count);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testExportAllFilterImportFromAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();// 4jan
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(4, archives.size());
      int count = 0;
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      for (IArchive archive: archives)
      {
         count += (Integer) workflowService.execute(new ImportProcessesCommand(
               ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive,
               filter, null, null));
      }
      assertEquals(3, count);
    
      assertProcessAndActivities(queryService, pQuery, aExpectedQuery, expectedInstances,
            expectedActivities);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testExportAllFilterImportArchiveFromAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date firstDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, firstDate, firstDate, null);
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0),
            filter, null, null));
      assertEquals(0, count);
      filter = new ArchiveFilter(null, null,null, null, lastDate, lastDate, null);
      archives = (List<IArchive>) workflowService.execute(new ImportProcessesCommand(
            filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0),
            filter, null, null));
      assertEquals(0, count);
      filter = new ArchiveFilter(null, null,null, null, fromDate, fromDate, null);
      archives = (List<IArchive>) workflowService.execute(new ImportProcessesCommand(
            filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0),
            filter, null, null));
      assertEquals(2, count);
      filter = new ArchiveFilter(null, null,null, null, toDate, toDate, null);
      archives = (List<IArchive>) workflowService.execute(new ImportProcessesCommand(
            filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0),
            filter, null, null));
      assertEquals(3, count);
      assertProcessAndActivities(queryService, pQuery, aExpectedQuery, expectedInstances,
            expectedActivities);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testFindArchivesBlankFilter() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;

      Date firstDate = testTimestampProvider.getTimestamp();// 2jan
      startAndCompleteSimpleManual(workflowService, queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      startAndCompleteScriptProcess(workflowService, queryService, textValue1,
            numberValue1);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      startAndCompleteScriptProcess(workflowService, queryService, textValue2,
            numberValue2);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      startAndCompleteSubprocessInModel(workflowService, queryService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(6, oldInstances.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(4, archives.size());
      boolean foundFrom = false;
      boolean foundTo = false;
      boolean foundLast = false;
      boolean foundFirst = false;
      for (IArchive archive : archives)
      {
         MemoryArchive memeoryArchive = (MemoryArchive)archive;
         if (fromDate.equals(memeoryArchive.getDate()))
         {
            foundFrom = true;
         }
         else if (toDate.equals(memeoryArchive.getDate()))
         {
            foundTo = true;
         }
         else if (lastDate.equals(memeoryArchive.getDate()))
         {
            foundLast = true;
         }
         else if (firstDate.equals(memeoryArchive.getDate()))
         {
            foundFirst = true;
         }

      }
      assertTrue(foundFrom);
      assertTrue(foundTo);
      assertTrue(foundLast);
      assertTrue(foundFirst);
   }
   
   @Test
   public void testFindArchivesCombinedFilter() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;

      startAndCompleteSimpleManual(workflowService, queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      final ProcessInstance pi1 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue1, numberValue1);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      final ProcessInstance pi2 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue2, numberValue2);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      startAndCompleteSubprocessInModel(workflowService, queryService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(6, oldInstances.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      List<Long> processInstanceOids = Arrays.asList(pi1.getOID(), pi2.getOID());
      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERID, Integer.toString((numberValue2*2)));
      //key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERNAME, "bbb");
      filter = new ArchiveFilter(null, null,processInstanceOids, null, fromDate, lastDate, descriptors);
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      boolean foundFrom = false;
      boolean foundTo = false;
      boolean foundLast = false;
      for (IArchive archive : archives)
      {
         MemoryArchive memeoryArchive = (MemoryArchive)archive;
         if (fromDate.equals(memeoryArchive.getDate()))
         {
            foundFrom = true;
         }
         else if (toDate.equals(memeoryArchive.getDate()))
         {
            foundTo = true;
         }
         else if (lastDate.equals(memeoryArchive.getDate()))
         {
            foundLast = true;
         }

      }
      assertFalse(foundFrom);
      assertTrue(foundTo);
      assertFalse(foundLast);
   }
   
   @Test
   public void testFindArchivesDescriptors() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;
     
      startAndCompleteSimpleManual(workflowService, queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      startAndCompleteScriptProcess(workflowService, queryService, textValue1,
            numberValue1);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      startAndCompleteScriptProcess(workflowService, queryService, textValue2,
            numberValue2);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      startAndCompleteSubprocessInModel(workflowService, queryService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(6, oldInstances.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      List<Long> processInstanceOids = null;
      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERID, Integer.toString((numberValue2*2)));
      //key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERNAME, "bbb");
      filter = new ArchiveFilter(null, null,processInstanceOids, null, null, null, descriptors);
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      boolean foundFrom = false;
      boolean foundTo = false;
      boolean foundLast = false;
      for (IArchive archive : archives)
      {
         MemoryArchive memeoryArchive = (MemoryArchive)archive;
         if (fromDate.equals(memeoryArchive.getDate()))
         {
            foundFrom = true;
         }
         else if (toDate.equals(memeoryArchive.getDate()))
         {
            foundTo = true;
         }
         else if (lastDate.equals(memeoryArchive.getDate()))
         {
            foundLast = true;
         }

      }
      assertFalse(foundFrom);
      assertTrue(foundTo);
      assertFalse(foundLast);
   }
   
   @Test
   public void testFindArchivesModelID() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance piOtherModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      final ActivityInstance otherActivity = completeOther(piOtherModel, 5, queryService,
            workflowService);

      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);
      testTimestampProvider.nextDay();
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      List<String> modelids = Arrays.asList(ArchiveModelConstants.MODEL_ID_OTHER);
      filter = new ArchiveFilter(modelids, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      IArchive archive = archives.get(0);
      assertEquals(1, archive.getExportIndex().getOidsToUuids().size());
      assertTrue(archive.getExportIndex().getOidsToUuids().containsKey(piOtherModel.getOID()));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testFindArchivesProcessDefinitionIds() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);

      testTimestampProvider.nextDay();
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

      testTimestampProvider.nextDay();
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
           
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(3, archives.size());
      List<String> procDefIds = Arrays.asList("Simple", "ScriptProcess");
      filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      archives = (List<IArchive>) workflowService.execute(new ImportProcessesCommand(filter, null));
      assertEquals(2, archives.size());
   }

   private ProcessInstance startAndCompleteSimpleManual(WorkflowService workflowService,
         QueryService queryService) throws TimeoutException, InterruptedException
   {
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      return simpleManualA;
   }
   
   @Test
   public void testFindArchivesProcessDefinitionIdsNotFindSub() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
     
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

      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
     
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));
     
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(10, oldActivities.size());
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<String> procDefIds = Arrays.asList("Simple");
      filter = new ArchiveFilter(null, procDefIds,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(0, archives.size());
   }
   
   
   @SuppressWarnings("unchecked")
   @Test
   public void testFindArchivesProcessInstanceOids() throws Exception
   {
      Date firstDate = testTimestampProvider.getTimestamp();// 1jan
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      startAndCompleteSimpleManual(workflowService, queryService);
      startAndCompleteSimple(workflowService, queryService);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      startAndCompleteSubprocessInModel(workflowService, queryService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(7, oldInstances.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(4, archives.size());

      List<Long> processInstanceOids = Arrays.asList(simpleB.getOID());
      filter = new ArchiveFilter(null, null,processInstanceOids, null, null, null, null);
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      IArchive archive = archives.get(0);
      assertEquals(1, archive.getExportIndex().getRootProcessToSubProcesses().keySet().size());
      Long process = archive.getExportIndex().getRootProcessToSubProcesses().keySet().iterator().next();
      assertTrue(process == simpleB.getOID());
      processInstanceOids = Arrays.asList(simpleB.getOID(), simpleManualA.getOID());
      filter = new ArchiveFilter(null, null,processInstanceOids, null, null, null, null);
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(2, archives.size());

      boolean foundFirst = false;
      boolean foundFrom = false;
      boolean foundTo = false;
      boolean foundLast = false;
      for (IArchive a : archives)
      {
         MemoryArchive memeoryArchive = (MemoryArchive)a;
         if (fromDate.equals(memeoryArchive.getDate()))
         {
            foundFrom = true;
         }
         else if (toDate.equals(memeoryArchive.getDate()))
         {
            foundTo = true;
         }
         else if (lastDate.equals(memeoryArchive.getDate()))
         {
            foundLast = true;
         }
         else if (firstDate.equals(memeoryArchive.getDate()))
         {
            foundFirst = true;
         }
      }
      assertTrue(foundFirst);
      assertFalse(foundFrom);
      assertTrue(foundTo);
      assertFalse(foundLast);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testFindArchivesFromAndToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      startAndCompleteSimpleManual(workflowService, queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      startAndCompleteSimpleManual(workflowService, queryService);
      startAndCompleteSimple(workflowService, queryService);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      startAndCompleteSimple(workflowService, queryService);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      startAndCompleteSubprocessInModel( workflowService, queryService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(7, oldInstances.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      filter = new ArchiveFilter(null, null,null, null, fromDate, lastDate, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(3, archives.size());
      boolean foundFrom = false;
      boolean foundTo = false;
      boolean foundLast = false;
      for (IArchive archive : archives)
      {
         MemoryArchive memeoryArchive = (MemoryArchive)archive;
         if (fromDate.equals(memeoryArchive.getDate()))
         {
            foundFrom = true;
         }
         else if (toDate.equals(memeoryArchive.getDate()))
         {
            foundTo = true;
         }
         else if (lastDate.equals(memeoryArchive.getDate()))
         {
            foundLast = true;
         }

      }
      assertTrue(foundFrom);
      assertTrue(foundTo);
      assertTrue(foundLast);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testFindArchivesFromNullAndToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date firstDate = testTimestampProvider.getTimestamp();
      startAndCompleteSimpleManual(workflowService, queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();// 2jan
      startAndCompleteSimpleManual(workflowService, queryService);
      startAndCompleteSimple(workflowService, queryService);
      testTimestampProvider.nextDay();// 3jan
      startAndCompleteSimple(workflowService, queryService);
      testTimestampProvider.nextDay();// 4jan
      startAndCompleteSubprocessInModel(workflowService, queryService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(7, oldInstances.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      filter = new ArchiveFilter(null, null,null, null, null, fromDate, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(2, archives.size());
      boolean foundFrom = false;
      boolean foundFirst = false;
      for (IArchive archive : archives)
      {
         MemoryArchive memeoryArchive = (MemoryArchive)archive;
         if (fromDate.equals(memeoryArchive.getDate()))
         {
            foundFrom = true;
         }
         else if (firstDate.equals(memeoryArchive.getDate()))
         {
            foundFirst = true;
         }

      }
      assertTrue(foundFrom);
      assertTrue(foundFirst);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testFindArchivesFromAndToDateNull() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      startAndCompleteSimpleManual(workflowService, queryService);
      testTimestampProvider.nextDay();
      startAndCompleteSimpleManual(workflowService, queryService);
      startAndCompleteSimple(workflowService, queryService);
      testTimestampProvider.nextDay();// 3jan
      Date toDate = testTimestampProvider.getTimestamp();
      startAndCompleteSimple(workflowService, queryService);
      testTimestampProvider.nextDay();// 4jan
      Date lastDate = testTimestampProvider.getTimestamp();
      startAndCompleteSubprocessInModel(workflowService, queryService);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(7, oldInstances.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      filter = new ArchiveFilter(null, null,null, null, toDate, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(2, archives.size());
      boolean foundTo = false;
      boolean foundLast = false;
      for (IArchive archive : archives)
      {
         MemoryArchive memeoryArchive = (MemoryArchive)archive;
         if (toDate.equals(memeoryArchive.getDate()))
         {
            foundTo = true;
         }
         else if (lastDate.equals(memeoryArchive.getDate()))
         {
            foundLast = true;
         }

      }
      assertTrue(foundTo);
      assertTrue(foundLast);
   }

   @Test
   public void testExportAllFilterImportFromAndToDateBothNull() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      Date date = null;
      filter = new ArchiveFilter(null, null,null, null, date, date, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(7, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testExportAllFilterImportFromNullAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      Date date1 = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date date2 = testTimestampProvider.getTimestamp();
      Date fromDate = null;
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      Date toDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, date1, date1, null);
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0),
            filter, null, null));
      assertEquals(1, count);
      filter = new ArchiveFilter(null, null,null, null, date2, date2, null);
      archives = (List<IArchive>) workflowService.execute(new ImportProcessesCommand(
            filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0),
            filter, null, null));
      assertEquals(4, count);

      assertProcessAndActivities(queryService, pQuery, aExpectedQuery, expectedInstances,
            expectedActivities);
   }

   @Test
   public void testExportAllFilterImportFromAndToDateNull() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date date1 = testTimestampProvider.getTimestamp();
      Date fromDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date date2 = testTimestampProvider.getTimestamp();
      Date toDate = null;
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, date1, date1, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archivesDate1 = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archivesDate1.size());
      filter = new ArchiveFilter(null, null,null, null, date2, date2, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archivesDate2 = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archivesDate2.size());
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archivesDate1.get(0),
            filter, null, null));
      assertEquals(3, count);
      filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archivesDate2.get(0),
            filter, null, null));
      assertEquals(6, count);
      assertProcessAndActivities(queryService, pQuery, aExpectedQuery, expectedInstances,
            expectedActivities);

   }

   @Test
   public void testExportAllFilterNullFromAndToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date toDate = testTimestampProvider.getTimestamp();
      testTimestampProvider.nextDay();
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(3, clearedInstances.size());
      assertEquals(10, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(4, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(simpleA, simpleB, simpleManualB, simpleManualA));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFromAndNullToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      testTimestampProvider.nextDay();
      Date fromDate = testTimestampProvider.getTimestamp();
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
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
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, fromDate, toDate, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(1, clearedInstances.size());
      assertEquals(3, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(6, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, Arrays.asList(simpleA,
            simpleB, simpleManualB, subSimple, subManual, subProcessesInModel));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllTwoModels() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      
      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      
      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      adminService.deleteModel(modelOID);
      RtEnvHome.deployModel(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER2);
      RtEnvHome.deployModel(adminService, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deployModel(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER);
      setUp();

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER2));
      model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      setUp();
      assertEquals(8, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertExportIds(queryService, newInstances, newInstances, true);
      assertProcessInstancesEquals(queryService,oldInstances, newInstances, newInstances, false);
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
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      startAllProcesses(workflowService, queryService, aQuery);

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(8, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }

   @Test
   public void dumpDBNotBackedUp() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      startAllProcesses(workflowService, queryService, aQuery);

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, "c"));
      assertNotNullExportResult(exportResult);
      assertEquals("c", exportResult.getExportIndex(exportResult.getDates().iterator().next()).getDumpLocation());

      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResult, "c");
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(8, clearedInstances.size());
      assertEquals(28, clearedActivities.size());

      assertExportIds(queryService, oldInstances, oldInstances, false);
   }
  
   @SuppressWarnings("unchecked")
   @Test
   public void archiveImportArchive() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      startAllProcesses(workflowService, queryService, aQuery);

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(8, oldInstances.size());
      assertEquals(28, oldActivities.size());
      List<ProcessInstance> exported = new ArrayList<ProcessInstance>();
      List<Long> exportedIds = new ArrayList<Long>();
      for (ProcessInstance pi : oldInstances)
      {
         if (pi.getProcessName().equals("CallSubProcessesInModel"))
         {
            exported.add(pi);
            exportedIds.add(pi.getOID());
         }
         else if (pi.getRootProcessInstanceOID() != pi.getOID())
         {
            exported.add(pi);
            exportedIds.add(pi.getOID());
         }
      }
      assertEquals(3, exported.size());
      // backup some processes
      ArchiveFilter filter = new ArchiveFilter(null, null,exportedIds, null, null, null, null);
      ExportResult exportResultBackUp = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT,filter,
                  null));
      assertNotNullExportResult(exportResultBackUp);
      assertEquals(3, exportResultBackUp.getPurgeProcessIds().size());
      assertEquals(1, exportResultBackUp.getDates().size());
      assertNull(exportResultBackUp
            .getExportIndex(exportResultBackUp.getDates().iterator().next()).getDumpLocation());
      archive(workflowService, exportResultBackUp);
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      assertNull(archives.get(0).getDumpLocation());

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(18, clearedActivities.size());
           
      archives = (List<IArchive>) workflowService.execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

      // import the backup
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, exported, true, true);
      assertActivityInstancesEquals(oldActivities, newActivities);
      
      // archive the same processes again

      filter = new ArchiveFilter(null, null,exportedIds, null, null, null, null);
      exportResultBackUp = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNotNull(exportResultBackUp);
      assertEquals(3, exportResultBackUp.getPurgeProcessIds().size());
      assertEquals(0, exportResultBackUp.getDates().size());
      Date startDate = testTimestampProvider.getTimestamp();
      ExportIndex exportIndex = exportResultBackUp.getExportIndex(startDate);
      assertNull(exportIndex);
      
      archive(workflowService, exportResultBackUp);
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      // check no new archives added
      assertEquals(1, archives.size());
      assertNull(archives.get(0).getDumpLocation());
      
      // check processes deleted again
      clearedInstances = queryService.getAllProcessInstances(pQuery);
      clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(18, clearedActivities.size());
   }
   
   protected static void startAllProcesses(WorkflowService workflowService,
         QueryService queryService, ActivityInstanceQuery aQuery, Map<String, String> options)
         throws TimeoutException, InterruptedException, Exception
   {
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, options, true);
      completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, options, true);
      completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, options, true);
      completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, options, true);
      completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, options, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, options, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
   }
   
   protected static void startAllProcesses(WorkflowService workflowService,
         QueryService queryService, ActivityInstanceQuery aQuery)
         throws TimeoutException, InterruptedException, Exception
   {
      startAllProcesses(workflowService, queryService, aQuery, null);
   }

   @Test
   public void testExportAllImportAllIgnoreExistingInstances() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      List<Long> oids = Arrays.asList(simpleManualA.getOID());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      oids = Arrays.asList(simpleManualA.getOID(), simpleManualB.getOID());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      oids = Arrays.asList(simpleManualA.getOID(), simpleManualB.getOID(),
            subProcessesInModel.getOID());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      oids = Arrays.asList(simpleManualA.getOID(), simpleManualB.getOID(),
            subProcessesInModel.getOID(), simpleA.getOID(), simpleB.getOID(),
            scriptProcess.getOID());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }

   @Test
   public void testExportAllFilterImportProcessIds() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      List<Long> oids = Arrays.asList(simpleA.getOID(), subProcessesInModel.getOID());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(4, count);

      assertProcessAndActivities(queryService, pQuery, aExpectedQuery, expectedInstances,
            expectedActivities);
   }
   
   @Test
   public void testExportAllFilterImportSubProcessIds() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      List<Long> oids = Arrays.asList(simpleA.getOID(), subSimple.getOID());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(4, count);

      assertProcessAndActivities(queryService, pQuery, aExpectedQuery, expectedInstances,
            expectedActivities);
   }

   private ProcessInstance startAndCompleteSubprocessInModel(
         WorkflowService workflowService, QueryService queryService) throws Exception
   {
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false);
      return subProcessesInModel;
   }

   @Test
   public void testExportAllFilterImportProcessIdsInvalid() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      List<Long> oids = Arrays.asList(-1L, null);
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      assertEquals(0, newInstances.size());
   }

   @Test
   public void testExportAllFilterImportProcessIdsBlank() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      List<Long> oids = new ArrayList<Long>();
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      assertEquals(0, newInstances.size());
   }

   @Test
   public void testExport2OfMany() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<Long> oids = Arrays.asList(simpleA.getOID(), simpleManualA.getOID());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(15, clearedActivities.size());
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(2, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(simpleA, simpleManualA));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportImportSimple() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);

      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
   }

   @Test
   public void testExportSimpleOid() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
   }

   @Test
   public void testExportSimple() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
   }
   
   @Test
   public void testExportSimpleDates() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      Date startDate = testTimestampProvider.getTimestamp();
      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);
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

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, startDate, endDate, null);
      exportAndArchive(workflowService, filter);
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
   }

   public static ProcessInstance completeSimple(ProcessInstance pi, QueryService qs,
         WorkflowService ws) throws TimeoutException, InterruptedException
   {
      completeNextActivity(pi, null, null, qs, ws);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return pi;
   }
   
   public static ProcessInstance completeTest(ProcessInstance pi, QueryService qs,
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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService);

   }

   @Test
   public void testDescriptorInvalid() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi1 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
    
      String textValue1 = "aaa";
      int numberValue1 = 20;
      completeScriptProcess(pi1, numberValue1, textValue1, queryService, workflowService);
    
      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      FilterOrTerm pOrTerm = pQuery.getFilter().addOrTerm();
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi1.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(8, oldActivities.size());
      assertNotNull(pi1.getScopeProcessInstanceOID());
      assertNotNull(pi1.getRootProcessInstanceOID());

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put("invalid", "invalid");
      //key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERNAME, "bbb");
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, null));
      assertNullRawData(exportResult);
   }

   
   //
   //testDescriptorInvalidImport
   @Test
   public void testDescriptorExportByPrimitive() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;
      String textValue3 = "bbb";
      int numberValue3 = 20;

      final ProcessInstance pi1 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue1, numberValue1);
      final ProcessInstance pi2 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue2, numberValue2);
      final ProcessInstance pi3 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue3, numberValue3);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      FilterOrTerm pOrTerm = pQuery.getFilter().addOrTerm();
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi1.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi2.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi3.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(24, oldActivities.size());
      assertNotNull(pi1.getScopeProcessInstanceOID());
      assertNotNull(pi1.getRootProcessInstanceOID());

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERID, Integer.toString((numberValue2*2)));
      //key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERNAME, "bbb");
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(1, clearedInstances.size());
      assertEquals(8, clearedActivities.size());

      assertTrue(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue1 * 2));
      assertFalse(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue2 * 2));
      assertFalse(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue3 * 2));

      List<IArchive> archives = findArchives(workflowService, filter, 1);
      IArchive archive = archives.get(0);
      assertFalse(archive.getExportIndex().contains(pi1.getOID()));
      assertTrue(archive.getExportIndex().contains(pi2.getOID()));
      assertTrue(archive.getExportIndex().contains(pi3.getOID()));
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(2, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, Arrays.asList(pi2,pi3), true);
      assertActivityInstancesEquals(oldActivities, newActivities);

      assertTrue(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue1));
      assertTrue(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue2));
      assertTrue(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue3));

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);
   }

   private ProcessInstance startAndCompleteScriptProcess(WorkflowService workflowService,
         QueryService queryService, String textValue1, int numberValue1)
         throws TimeoutException, InterruptedException
   {
      final ProcessInstance pi1 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(pi1, numberValue1, textValue1, queryService, workflowService);
      return pi1;
   }
   
   @Test
   public void testDescriptorImportMatchOnSub() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subProcessesInModel.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subSimple.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subManual.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subProcessesInModel.getOID()));
      ProcessInstances expectedInstances = queryService.getAllProcessInstances(pExpectedQuery);
      ActivityInstances expectedActivities = queryService.getAllActivityInstances(aExpectedQuery);
      
      assertNotNull(expectedInstances);
      assertNotNull(expectedActivities);
      assertEquals(3, expectedInstances.size());
      assertEquals(10, expectedActivities.size());
      
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); assertNotNull(exportResult);
     
                  assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);

      List<IArchive> archives = findArchives(workflowService, filter, 1);

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_SIMPLE, Long.toString(subSimple.getOID()));
      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,expectedInstances, newInstances,
            Arrays.asList(subProcessesInModel, subManual, subSimple));
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }
   
   @Test
   public void testDescriptorImportMatchOnRoot() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subProcessesInModel.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subSimple.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subManual.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subProcessesInModel.getOID()));
      ProcessInstances expectedInstances = queryService.getAllProcessInstances(pExpectedQuery);
      ActivityInstances expectedActivities = queryService.getAllActivityInstances(aExpectedQuery);
      
      assertNotNull(expectedInstances);
      assertNotNull(expectedActivities);
      assertEquals(3, expectedInstances.size());
      assertEquals(10, expectedActivities.size());
      
      
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); assertNotNull(exportResult);
     
                  assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);

      List<IArchive> archives = findArchives(workflowService, filter, 1);

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CALL_SUBPROCESSES_IN_MODEL, Long.toString(subProcessesInModel.getOID()));
      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);;
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,expectedInstances, newInstances,
            Arrays.asList(subProcessesInModel, subManual, subSimple));
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }
   
   @Test
   public void testDescriptorExportMatchOnSub() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_SIMPLE, Long.toString(subSimple.getOID()));
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); assertNotNull(exportResult);
     
                  assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(18, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);

      List<IArchive> archives = findArchives(workflowService, filter, 1);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(subProcessesInModel, subManual, subSimple));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   @Test
   public void testDescriptorExportMatchOnRoot() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      final ProcessInstance simpleManualA = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleManualB = startAndCompleteSimpleManual(workflowService,
            queryService);
      final ProcessInstance simpleA = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance simpleB = startAndCompleteSimple(workflowService,
            queryService);
      final ProcessInstance subProcessesInModel = startAndCompleteSubprocessInModel(
            workflowService, queryService);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      
      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CALL_SUBPROCESSES_IN_MODEL, Long.toString(subProcessesInModel.getOID()));
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null)); assertNotNull(exportResult);
     
                  assertNotNullExportResult(exportResult);
      archive(workflowService, exportResult);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(18, clearedActivities.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);

      List<IArchive> archives = findArchives(workflowService, filter, 1);
      
      filter = new ArchiveFilter(null, null,null, null, null, null, null);;
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances,
            Arrays.asList(subProcessesInModel, subManual, subSimple));
      assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   
   @Test
   public void testDescriptorPrimitiveImport() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;
      String textValue3 = "bbb";
      int numberValue3 = 20;
      
      final ProcessInstance pi1 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue1, numberValue1);
      final ProcessInstance pi2 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue2, numberValue2);
      final ProcessInstance pi3 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue3, numberValue3);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      FilterOrTerm pOrTerm = pQuery.getFilter().addOrTerm();
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi1.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi2.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi3.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(24, oldActivities.size());
      assertNotNull(pi1.getScopeProcessInstanceOID());
      assertNotNull(pi1.getRootProcessInstanceOID());

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      assertFalse(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue1 * 2));
      assertFalse(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue2 * 2));
      assertFalse(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue3 * 2));

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERID, Integer.toString((numberValue2*2)));
      //key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERNAME, "bbb");
      
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      IArchive archive = archives.get(0);
      assertTrue(archive.getExportIndex().contains(pi1.getOID()));
      assertTrue(archive.getExportIndex().contains(pi2.getOID()));
      assertTrue(archive.getExportIndex().contains(pi3.getOID()));
      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(2, count);
      descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERID, Integer.toString((numberValue1*2)));
      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(1, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);

      assertTrue(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue1));
      assertTrue(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue2));
      assertTrue(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue3));

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);
   }
   
   @Test
   public void testDescriptorDateImport() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;
      String textValue3 = "bbb";
      int numberValue3 = 20;
      
      final ProcessInstance pi1 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue1, numberValue1);
      final ProcessInstance pi2 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue2, numberValue2);
      final ProcessInstance pi3 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue3, numberValue3);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      FilterOrTerm pOrTerm = pQuery.getFilter().addOrTerm();
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi1.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi2.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi3.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(24, oldActivities.size());
      assertNotNull(pi1.getScopeProcessInstanceOID());
      assertNotNull(pi1.getRootProcessInstanceOID());

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      assertFalse(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue1 * 2));
      assertFalse(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue2 * 2));
      assertFalse(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue3 * 2));

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
      Date date = dateFormat.parse(ArchiveModelConstants.DEFAULT_DATE);
      descriptors.put(ArchiveModelConstants.DESCR_BUSINESSDATE, date);
      
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      IArchive archive = archives.get(0);
      assertTrue(archive.getExportIndex().contains(pi1.getOID()));
      assertTrue(archive.getExportIndex().contains(pi2.getOID()));
      assertTrue(archive.getExportIndex().contains(pi3.getOID()));
      //change one exportProcess to not fit in descriptor filter
      Long processInstanceOid = archive.getExportIndex().getRootProcessToSubProcesses().keySet().iterator().next();
      updateField(archive, processInstanceOid, ArchiveModelConstants.DESCR_BUSINESSDATE, "2015/03/06 00:00:00:000");

      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(2, count);
   }

   private void updateField(IArchive archive, Long processInstanceOid, String field, String value)
   {
      Map<String, List<Long>> valuesToProcesses = archive.getExportIndex().getFields().get(field);
      for (String str : valuesToProcesses.keySet())
      {
         valuesToProcesses.get(str).remove(processInstanceOid);
      }
      archive.getExportIndex().addField(processInstanceOid, field, value);
   }
   
   @Test
   public void testDescriptorPrimitiveImportOperations() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;
      String textValue3 = "bbb";
      int numberValue3 = 20;
      
      final ProcessInstance pi1 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue1, numberValue1);
      final ProcessInstance pi2 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue2, numberValue2);
      final ProcessInstance pi3 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue3, numberValue3);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      FilterOrTerm pOrTerm = pQuery.getFilter().addOrTerm();
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi1.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi2.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi3.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(24, oldActivities.size());
      assertNotNull(pi1.getScopeProcessInstanceOID());
      assertNotNull(pi1.getRootProcessInstanceOID());

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      assertFalse(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue1 * 2));
      assertFalse(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue2 * 2));
      assertFalse(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue3 * 2));

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERID, Integer.toString((numberValue2*2)));
      //key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERNAME, "bbb");

      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      IArchive archive = archives.get(0);
      assertTrue(archive.getExportIndex().contains(pi1.getOID()));
      assertTrue(archive.getExportIndex().contains(pi2.getOID()));
      assertTrue(archive.getExportIndex().contains(pi3.getOID()));
      
      ImportMetaData importMetaData = (ImportMetaData) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, archive, filter, null, null));
            
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, archive, filter, importMetaData, null));
      assertEquals(2, count);
      descriptors = new HashMap<String, Object>();
      //non key key primitive
      descriptors.put(ArchiveModelConstants.DESCR_CUSTOMERID, Integer.toString((numberValue1*2)));
      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, archive, filter, importMetaData, null));
      assertEquals(1, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);

      assertTrue(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue1));
      assertTrue(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue2));
      assertTrue(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue3));

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);
   }

   @Test
   public void testDescriptorsDifferentDateFormat() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      String textValue1 = "aaa";
      int numberValue1 = 20;
     
      final ProcessInstance pi1 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue1, numberValue1);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      FilterOrTerm pOrTerm = pQuery.getFilter().addOrTerm();
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi1.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(8, oldActivities.size());
      assertNotNull(pi1.getScopeProcessInstanceOID());
      assertNotNull(pi1.getRootProcessInstanceOID());

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      assertFalse(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue1 * 2));

      List<IArchive> archives = findArchives(workflowService, filter, 1);
      MemoryArchive archive = (MemoryArchive)archives.get(0);
      ExportIndex oldIndex = archive.getExportIndex();
      assertTrue(oldIndex.contains(pi1.getOID()));
      
      Long exportProcess = oldIndex.getRootProcessToSubProcesses().keySet().iterator().next();
      oldIndex.addField(exportProcess, ArchiveModelConstants.DESCR_BUSINESSDATE, "2015-05-03 00:00");
  
      ExportIndex exportIndex = new ExportIndex(getArchiveManagerId("default"), "yyyy-dd-MM HH:mm", null);
      
      for (Long oid : oldIndex.getRootProcessToSubProcesses().keySet())
      {
         exportIndex.getRootProcessToSubProcesses().put(oid, oldIndex.getRootProcessToSubProcesses().get(oid));
         exportIndex.setUuid(oid, oldIndex.getUuid(oid));
         for (Long subId : oldIndex.getRootProcessToSubProcesses().get(oid))
         {
            exportIndex.setUuid(subId, oldIndex.getUuid(subId));
         }
      }      
      exportIndex.setFields(oldIndex.getFields());
      
      String json = getJSON(exportIndex);
      MemoryArchive archive1 = new MemoryArchive((String)archive.getArchiveKey(), archive.getDate(), archive.getDataByProcess(), getJSON(archive.getExportModel()), json);
      
      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //key primitive
      final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
      Date date = dateFormat.parse(ArchiveModelConstants.DEFAULT_DATE);
      descriptors.put(ArchiveModelConstants.DESCR_BUSINESSDATE, date);

      filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive1, filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, newInstances, true);
      assertActivityInstancesEquals(oldActivities, newActivities);

      assertTrue(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue1));

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
   }
   
   @Test
   public void testDescriptorExportByDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      String textValue1 = "aaa";
      int numberValue1 = 20;
      String textValue2 = "ccc";
      int numberValue2 = 10;
      String textValue3 = "bbb";
      int numberValue3 = 20;
     
      final ProcessInstance pi1 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue1, numberValue1);
      final ProcessInstance pi2 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue2, numberValue2);
      final ProcessInstance pi3 = startAndCompleteScriptProcess(workflowService,
            queryService, textValue3, numberValue3);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      FilterOrTerm pOrTerm = pQuery.getFilter().addOrTerm();
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi1.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi2.getOID()));
      pOrTerm.or(ProcessInstanceQuery.OID.isEqual(pi3.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(24, oldActivities.size());
      assertNotNull(pi1.getScopeProcessInstanceOID());
      assertNotNull(pi1.getRootProcessInstanceOID());

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);

      HashMap<String, Object> descriptors = new HashMap<String, Object>();
      //key primitive
      final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
      Date date = dateFormat.parse(ArchiveModelConstants.DEFAULT_DATE);
      descriptors.put(ArchiveModelConstants.DESCR_BUSINESSDATE, date);
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, descriptors);
      exportAndArchive(workflowService, filter);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      assertFalse(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue1 * 2));
      assertFalse(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue2 * 2));
      assertFalse(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, numberValue3 * 2));

      List<IArchive> archives = findArchives(workflowService, filter, 1);
      IArchive archive = archives.get(0);
      assertTrue(archive.getExportIndex().contains(pi1.getOID()));
      assertTrue(archive.getExportIndex().contains(pi2.getOID()));
      assertTrue(archive.getExportIndex().contains(pi3.getOID()));
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(queryService,oldInstances, newInstances, newInstances, true);
      assertActivityInstancesEquals(oldActivities, newActivities);

      assertTrue(hasStructuredDateField(pi1.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue1));
      assertTrue(hasStructuredDateField(pi2.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue2));
      assertTrue(hasStructuredDateField(pi3.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue3));

      assertDataPaths(workflowService, pi1, textValue1, numberValue1, dataPathIds);
      assertDataPaths(workflowService, pi2, textValue2, numberValue2, dataPathIds);
      assertDataPaths(workflowService, pi3, textValue3, numberValue3, dataPathIds);
   }
         
   @SuppressWarnings("unchecked")
   private void assertDataPaths(WorkflowService workflowService,
         final ProcessInstance pi1, String textValue1, int numberValue1,
         Set<String> dataPathIds) throws Exception
   {
      Map<String, Serializable> inDataPaths = workflowService.getInDataPaths(
            pi1.getOID(), dataPathIds);

      assertEquals(6, inDataPaths.size());

      HashMap<String, Serializable> customerData = ((HashMap<String, Serializable>) inDataPaths
            .get(ArchiveModelConstants.DESCR_CUSTOMERDATA));
      assertEquals(textValue1,
            customerData.get(ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA));
      assertEquals(numberValue1 * 2,
            customerData.get(ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB));
      assertEquals(numberValue1 * 2,
            inDataPaths.get(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID));
      assertEquals(textValue1,
            inDataPaths.get(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME));
      assertEquals(numberValue1 * 2,
            inDataPaths.get(ArchiveModelConstants.DESCR_CUSTOMERID));
      assertEquals(textValue1, inDataPaths.get(ArchiveModelConstants.DESCR_CUSTOMERNAME));
      //2015/03/05 00:00:00:000
      final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            
      assertEquals(dateFormat.parse(ArchiveModelConstants.DEFAULT_DATE), inDataPaths.get(ArchiveModelConstants.DESCR_BUSINESSDATE));
   }

   @Test
   public void testExportImportScriptProcess() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);

      // test largeStringHolder
      char[] chars = new char[5];
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

      HashMap<Long, Boolean> transitionSourceMap = new HashMap<Long, Boolean>();
      HashMap<Long, Boolean> transitionTargetMap = new HashMap<Long, Boolean>();
      for (ActivityInstance activity : oldActivities)
      {
         boolean source = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__SOURCE, activity.getOID());
         boolean target = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__TARGET, activity.getOID());
         transitionSourceMap.put(activity.getOID(), source);
         transitionTargetMap.put(activity.getOID(), target);
      }

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi, textValue, 10, dataPathIds);

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

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

      List<IArchive> archives = findArchives(workflowService, filter, 1);
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20, queryService);

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue));

      for (ActivityInstance activity : oldActivities)
      {
         boolean source = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__SOURCE, activity.getOID());
         boolean target = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__TARGET, activity.getOID());
         assertEquals(activity.getOID() + " source not equals",
               transitionSourceMap.get(activity.getOID()), source);
         assertEquals(activity.getOID() + " target not equals",
               transitionTargetMap.get(activity.getOID()), target);
      }
      assertDataPaths(workflowService, pi, textValue, 10, dataPathIds);
   }

   @Test
   public void testExportImportScriptProcessLargeData() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);

      // test largeStringHolder
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

      HashMap<Long, Boolean> transitionSourceMap = new HashMap<Long, Boolean>();
      HashMap<Long, Boolean> transitionTargetMap = new HashMap<Long, Boolean>();
      for (ActivityInstance activity : oldActivities)
      {
         boolean source = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__SOURCE, activity.getOID());
         boolean target = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__TARGET, activity.getOID());
         transitionSourceMap.put(activity.getOID(), source);
         transitionTargetMap.put(activity.getOID(), target);
      }

      Set<String> dataPathIds = new HashSet<String>();
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_ID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERDATA_NAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERID);
      dataPathIds.add(ArchiveModelConstants.DESCR_CUSTOMERNAME);
      dataPathIds.add(ArchiveModelConstants.DESCR_BUSINESSDATE);

      assertDataPaths(workflowService, pi, textValue, 10, dataPathIds);

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

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

      List<IArchive> archives = findArchives(workflowService, filter, 1);
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20, queryService);

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      assertTrue(hasStructuredDateField(pi.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, textValue));

      for (ActivityInstance activity : oldActivities)
      {
         boolean source = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__SOURCE, activity.getOID());
         boolean target = hasEntryInDbForObject(TransitionInstanceBean.TABLE_NAME,
               TransitionInstanceBean.FIELD__TARGET, activity.getOID());
         assertEquals(activity.getOID() + " source not equals",
               transitionSourceMap.get(activity.getOID()), source);
         assertEquals(activity.getOID() + " target not equals",
               transitionTargetMap.get(activity.getOID()), target);
      }
      assertDataPaths(workflowService, pi, textValue, 10, dataPathIds);
   }

   public static ActivityInstance completeOther(final ProcessInstance pi, int numberValue,
         QueryService qs, WorkflowService ws) throws TimeoutException,
         InterruptedException
   {
      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_OTHER_NUMBER, numberValue,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "aaa", qs, ws);

      completeNextActivity(pi, null, null, qs, ws);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return writeActivity;
   }

   public static ActivityInstance completeScriptProcess(final ProcessInstance pi,
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

   public static ActivityInstance completeSimpleManual(final ProcessInstance pi,
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

   private static Date completeSubProcessesInModel(final ProcessInstance pi, QueryService qs,
         WorkflowService ws, boolean changeSubProcessDate) throws Exception
   {

      return completeSubProcessesInModel(pi, qs, ws, changeSubProcessDate, testTimestampProvider);
   }
   
   public static Date completeSubProcessesInModel(final ProcessInstance pi, QueryService qs,
         WorkflowService ws, boolean changeSubProcessDate, TestTimestampProvider testTimestampProvider) throws Exception
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

   protected static ActivityInstance completeNextActivity(final ProcessInstance pi, String dataId,
         Object data, QueryService qs, WorkflowService ws)
   {
//      final ActivityInstance ai1 = qs.findFirstActivityInstance(ActivityInstanceQuery
//            .findAlive(pi.getProcessID()));
      
      final ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterAndTerm term = aQuery.getFilter().addAndTerm();
      term.and(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      term.and(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      
      ActivityInstances allActivityInstances = qs.getAllActivityInstances(aQuery);
      Long aiOid = null;
      for (ActivityInstance ai : allActivityInstances)
      {
         System.out.println(ai.getOID());
         aiOid = ai.getOID();
      }
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
      final ActivityInstance writeActivity = ws.activateAndComplete(aiOid, null,
            outData);
      return writeActivity;
   }

   public static ActivityInstance completeNextActivity(final ProcessInstance pi, String dataId,
         Object data, String dataId2, Object data2, QueryService qs, WorkflowService ws)
   {
//      final ActivityInstance ai1 = qs.findFirstActivityInstance(ActivityInstanceQuery
//            .findForProcessInstance(pi.getOID()));
      
      final ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterAndTerm term = aQuery.getFilter().addAndTerm();
      term.and(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      term.and(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.SUSPENDED));
      
      ActivityInstances allActivityInstances = qs.getAllActivityInstances(aQuery);
      Long aiOid = null;
      for (ActivityInstance ai : allActivityInstances)
      {
         System.out.println(ai.getOID());
         aiOid = ai.getOID();
      }
      
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
      final ActivityInstance writeActivity = ws.activateAndComplete(aiOid, null,
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
      for (ActivityInstance activity : oldActivities)
      {
         createActivityInstanceProperty(activity);
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         createActivityInstanceProperty(activity);
      }
      for (ActivityInstance activity : oldActivities)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

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
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
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

      assertProcessInstancesEquals(queryService,oldInstances, newInstances);
      assertProcessInstancesEquals(queryService,oldInstancesSubSimple, newInstancesSubSimple);
      assertProcessInstancesEquals(queryService,oldInstancesSubManual, newInstancesSubManual);
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

      for (ActivityInstance activity : oldActivities)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }
      for (ActivityInstance activity : oldActivitiesSubSimple)
      {
         assertTrue(hasEntryInDbForObject(ActivityInstanceProperty.TABLE_NAME,
               ActivityInstanceProperty.FIELD__OBJECT_OID, activity.getOID()));
      }

   }
  
   protected static void createActivityInstanceProperty(ActivityInstance activity)
         throws Exception
   {
      final DataSource ds = testClassSetup.dataSource();

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         int count = stmt
               .executeUpdate("INSERT INTO PUBLIC."
                     + ActivityInstanceProperty.TABLE_NAME
                     + " (OID, OBJECTOID, NAME, TYPE_KEY, NUMBER_VALUE, STRING_VALUE, LASTMODIFICATIONTIME, WORKFLOWUSER) "
                     + " VALUES ('" + activity.getOID() + "', '" + activity.getOID()
                     + "', 'name', '123', '123', '123', '123', '1')");
         assertEquals(1, count);
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

   }
   
   protected static void createPreference(Integer partitionOid, String name, String value)
         throws Exception
   {
      final DataSource ds = testClassSetup.dataSource();

      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
      "<preferences id=\"" + name + "\" module=\"stardust-archiving\">" +
        "<preference name=\"" + name + "\">" + value + "</preference>" +
      "</preferences>";

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         int count = stmt
               .executeUpdate("INSERT INTO PUBLIC."
                     + PreferencesBean.TABLE_NAME
                     + " (OWNERID, OWNERTYPE, MODULEID, PREFERENCESID, PARTITION, STRINGVALUE) "
                     + " VALUES (" + partitionOid + ", 'PARTITION', '" 
                     + ArchiveManagerFactory.MODULE_ID_STARDUST_ARCHIVING + "', '" + name + "', " + partitionOid + ", '" + xml + "')");
         assertEquals(1, count);
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

   }
   
   protected static void deletePreferences() throws Exception
   {
      final DataSource ds = testClassSetup.dataSource();

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         stmt.executeUpdate("DELETE FROM PUBLIC." + PreferencesBean.TABLE_NAME);
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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);

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
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
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

      assertProcessInstancesEquals(queryService,oldInstances, newInstances);
      assertProcessInstancesEquals(queryService,oldInstancesSubSimple, newInstancesSubSimple);
      assertProcessInstancesEquals(queryService,oldInstancesSubManual, newInstancesSubManual);
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

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);
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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);
      
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      
      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      adminService.deleteModel(modelOID);
      setUp();

      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.ERROR);
      barrier.registerWithLog4j();

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));

      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER);
      assertEquals(0, count);
      barrier
            .waitForLogMessage(
                  "Invalid environment to import into.* Current environment does not have a model with uuid.*",
                  new WaitTimeout(5, TimeUnit.SECONDS));

   }

   @Test
   public void testExportImportSimpleDifferentModelDeployed() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());

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
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER2);
      setUp();
      
      final Log4jLogMessageBarrier barrier = new Log4jLogMessageBarrier(Level.ERROR);
      barrier.registerWithLog4j();
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));

      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER);
      barrier
            .waitForLogMessage(
                  "Invalid environment to import into.* Current environment does not have a model with uuid.*",
                  new WaitTimeout(5, TimeUnit.SECONDS));

      assertEquals(0, count);
   }

   @Test
   public void testExportImportSimpleSameModelRedeployed() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);
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
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      exportAndArchive(workflowService, filter);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      
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
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID_OTHER);
      setUp();
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(queryService,oldInstances, newInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);

   }
   
   @Test
   public void testModelVersions() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = startAndCompleteSimple(workflowService, queryService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      
      // deploy same model with one added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV2_ID);
      
      final ProcessInstance piv2 = startAndCompleteTest(workflowService, queryService);

      // deploy same model with change in added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV3_ID);
      
      final ProcessInstance pi3 = startAndCompleteTest(workflowService, queryService);
      
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piv2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));
      
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(6, oldActivities.size());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(queryService,oldInstances, newInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);
   }
   @Test
   public void testArchiveProcess3ModelVersionsDown() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);

      // deploy same model with one added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV2_ID);
      final ProcessInstance piT1 = startAndCompleteTest(workflowService, queryService);
            
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);

      // redeploy same model
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV2_ID);

      // redeploy same model without test process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV3_ID);
      
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piT1.getOID()));

      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      IArchive archive1 = archives.get(0);
      
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
         ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive1, filter, null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(newInstances);
      assertNotNull(newActivities);
      assertEquals(1, newInstances.size());
      assertEquals(2, newActivities.size());
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void test3ModelVersionsDifferentArchiveTimes() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);

      final ProcessInstance piS1 = startAndCompleteSimple(workflowService, queryService);

      // deploy same model with one added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV2_ID);
      final ProcessInstance piS2 = startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance piT1 = startAndCompleteTest(workflowService, queryService);

      // deploy same model with change in added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV3_ID);

      final ProcessInstance piS3 = startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance piT2 = startAndCompleteTest(workflowService, queryService);
      
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter);
      List<IArchive> archives = findArchives(workflowService, filter, 1);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null));
      assertEquals(5, count);

      // deploy same model with change in added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      final ProcessInstance piS4 = startAndCompleteSimple(workflowService, queryService);

      // deploy same model with one added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV2_ID);
      final ProcessInstance piS5 = startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance piT3 = startAndCompleteTest(workflowService, queryService);

      // deploy same model with change in added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV3_ID);

      final ProcessInstance piS6 = startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance piT4 = startAndCompleteTest(workflowService, queryService);
      
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      exportAndArchive(workflowService, filter,5,10);

      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      testTimestampProvider.nextDay();
      
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS3.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS4.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS5.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS6.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piT1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piT2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piT3.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piT4.getOID()));

      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(2, archives.size());
      IArchive archive1 = null;
      IArchive archive2 = null;
      for (IArchive archive : archives)
      {
         if (archive.getExportIndex().getOidsToUuids().containsKey(piT1.getOID()))
         {
            assertEquals(5, archive.getExportIndex().getOidsToUuids().size());
            archive1 = archive;
         }
         if (archive.getExportIndex().getOidsToUuids().containsKey(piT3.getOID()))
         {
            assertEquals(5, archive.getExportIndex().getOidsToUuids().size());
            archive2 = archive;
         }
      }
      assertNotNull(archive1);
      assertNotNull(archive2);
      assertEquals(3, archive1.getExportModel().getModelOidToUuid().size());
      assertEquals(3, archive2.getExportModel().getModelOidToUuid().size());
      
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      count = 0;
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
         ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive1, filter, null, null));
      assertEquals(5, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive2, filter, null, null));
      assertEquals(10, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(newInstances);
      assertNotNull(newActivities);
      assertEquals(10, newInstances.size());
      assertEquals(20, newActivities.size());
   }

   @SuppressWarnings("unchecked")
   @Test
   public void test3ModelVersionsOneArchiveTime() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      
      Date date1 = testTimestampProvider.getTimestamp();
      final ProcessInstance piS1 = startAndCompleteSimple(workflowService, queryService);

      // deploy same model with one added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV2_ID);
      final ProcessInstance piS2 = startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance piT1 = startAndCompleteTest(workflowService, queryService);

      // deploy same model with change in added process
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODELV3_ID);

      final ProcessInstance piS3 = startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance piT2 = startAndCompleteTest(workflowService, queryService);
      
      RtEnvHome.deployModel(sf.getAdministrationService(), null,
            ArchiveModelConstants.MODEL_ID);
      testTimestampProvider.nextDay();
      Date date2 = testTimestampProvider.getTimestamp();
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      final ProcessInstance piS4 = startAndCompleteSimple(workflowService, queryService);
      
      
     
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS3.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piS4.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piT1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piT2.getOID()));
      
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(6, oldInstances.size());
      assertEquals(12, oldActivities.size());
      
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNotNullExportResult(exportResult);
      assertEquals(2, exportResult.getDates().size());
      assertEquals(5, exportResult.getExportIndex(date1).getOidsToUuids().size());
      assertEquals(1, exportResult.getExportIndex(date2).getOidsToUuids().size());
      assertEquals(6, exportResult.getPurgeProcessIds().size());
      archive(workflowService, exportResult);

      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(2, archives.size());
      IArchive archive1 = null;
      IArchive archive2 = null;
      for (IArchive archive : archives)
      {
         if (archive.getExportIndex().getOidsToUuids().containsKey(piT1.getOID()))
         {
            assertEquals(5, archive.getExportIndex().getOidsToUuids().size());
            archive1 = archive;
         }
         if (archive.getExportIndex().getOidsToUuids().containsKey(piS4.getOID()))
         {
            assertEquals(1, archive.getExportIndex().getOidsToUuids().size());
            archive2 = archive;
         }
      }
      assertNotNull(archive1);
      assertNotNull(archive2);
      assertEquals(3, archive1.getExportModel().getModelOidToUuid().size());
      assertEquals(1, archive2.getExportModel().getModelOidToUuid().size());
      
      ProcessInstances instancesCleared = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instancesCleared);
      assertNotNull(activitiesCleared);
      assertEquals(0, instancesCleared.size());
      assertEquals(0, activitiesCleared.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      int count = 0;
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
         ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive1, filter, null, null));
      assertEquals(5, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive2, filter, null, null));
      assertEquals(6, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(newInstances);
      assertNotNull(newActivities);
      assertProcessInstancesEquals(queryService, oldInstances, newInstances, newInstances, false);
      assertActivityInstancesEquals(oldActivities, newActivities, false);
   }

   private ProcessInstance startAndCompleteTest(WorkflowService workflowService,
         QueryService queryService) throws TimeoutException, InterruptedException
   {
      final ProcessInstance piv2 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_TEST, null, true);
      completeTest(piv2, queryService, workflowService);

      return piv2;
   }

   private ProcessInstance startAndCompleteSimple(WorkflowService workflowService,
         QueryService queryService) throws TimeoutException, InterruptedException
   {
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi, queryService, workflowService);
      return pi;
   }

   private void exportAndArchive(WorkflowService workflowService, ArchiveFilter filter)
         throws Exception
   {
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNotNullExportResult(exportResult);

      archive(workflowService, exportResult);
   }
   
   private void exportAndArchive(WorkflowService workflowService, ArchiveFilter filter,
         int processCount, int purgeCount)
         throws Exception
   {
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null));
      assertNotNullExportResult(exportResult);
      assertEquals(1, exportResult.getDates().size());
      Date date = exportResult.getDates().iterator().next();
      assertEquals(processCount, exportResult.getExportIndex(date).getOidsToUuids().size());
      assertEquals(purgeCount, exportResult.getPurgeProcessIds().size());
      archive(workflowService, exportResult);
   }
   
   public static boolean hasStructuredDateField(long processInstanceOid, String dataId,
         String fieldName, Object value) throws Exception
   {
      String sql = "select cd.stringvalue from data d inner join data_value dv on d.oid = dv.data"
            + " inner join clob_data cd on dv.number_value = cd.oid"
            + " where d.id = ?"
            + " and dv.processinstance = ?";
      String sql2 = "select sdv.string_value, number_value from data d inner join structured_data sd on d.oid = sd.data"
            + " inner join structured_data_value sdv on sd.oid = sdv.xpath "
            + " where sd.xpath = ?" + " and sdv.processinstance = ?";

      String sql3 = "select str.data from data d inner join structured_data sd on d.oid = sd.data"
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
                  result = rs2.getBigDecimal(2).intValue() == (Integer) value;
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

   protected static boolean hasEntryInDbForObject(final String tableName, String fieldName,
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

   private static void assertExportIds(QueryService queryService, List<ProcessInstance> instances,
         List<ProcessInstance> exportedInstances, boolean mustHave) throws Exception
   {
      int count = 0;
      for (ProcessInstance pi : instances)
      {
         Object uuid = getEntryInDbForObject(ProcessInstanceProperty.TABLE_NAME,
               ProcessInstanceProperty.FIELD__OBJECT_OID, pi.getOID(),
               ProcessInstanceProperty.FIELD__STRING_VALUE);
         if (mustHave && exportedInstances.contains(pi))
         {
            count++;
            assertNotNull(uuid);
         }
         else
         {
            assertNull(uuid);
         }
      }
      if (mustHave)
      {
         assertEquals(exportedInstances.size(), count);
      }
   }

   private static Object getEntryInDbForObject(final String tableName, String fieldName,
         final long id, String selectField) throws SQLException
   {
      final DataSource ds = testClassSetup.dataSource();
      final Object result;

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT " + selectField + " FROM PUBLIC."
               + tableName + " WHERE " + fieldName + " = " + id);
         if (rs.next())
         {
            result = rs.getObject(selectField);
         }
         else
         {
            result = null;
         }
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
   
   protected static Object getEntryInDbForObject(final String tableName, String fieldName,
         final String value, String selectField) throws SQLException
   {
      final DataSource ds = testClassSetup.dataSource();
      final Object result;

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT " + selectField + " FROM PUBLIC."
               + tableName + " WHERE " + fieldName + " = '" + value + "'");
         if (rs.next())
         {
            result = rs.getObject(selectField);
         }
         else
         {
            result = null;
         }
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
   
   
   protected static void assertDataExists(long processInstanceOid, long activityOid,
         String processName, String dataId, Serializable expectedValue,
         QueryService queryService)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            true, queryService);
   }

   protected static void assertDataNotExists(long processInstanceOid, long activityOid,
         String processName, String dataId, Serializable expectedValue,
         QueryService queryService)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            false, queryService);
   }

   public static void checkDataValue(long processInstanceOid, long activityOid,
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

   protected static void assertProcessInstancesEquals(QueryService queryService,ProcessInstances oldInstances,
         ProcessInstances newInstances) throws Exception
   {
      assertProcessInstancesEquals(queryService, oldInstances, newInstances, newInstances, true);
   }

   protected static void assertProcessInstancesEquals(QueryService queryService,ProcessInstances oldInstances,
         ProcessInstances newInstances, List<ProcessInstance> exportedInstances)
         throws Exception
   {
      assertProcessInstancesEquals(queryService, oldInstances, newInstances, exportedInstances, true);
   }

   protected static void assertProcessInstancesEquals(QueryService queryService, ProcessInstances oldInstances,
         ProcessInstances newInstances, List<ProcessInstance> exportedInstances,
         boolean compareRTOids) throws Exception
   {
      assertProcessInstancesEquals(queryService,oldInstances, newInstances, exportedInstances,
            compareRTOids, true);
   }

   protected static void assertProcessInstancesEquals(QueryService queryService, ProcessInstances oldInstances,
         ProcessInstances newInstances, List<ProcessInstance> exportedInstances,
         boolean compareRTOids, boolean mustHaveExportIds) throws Exception
   {
      int countCompared = 0;
      assertNotNull(newInstances);
      assertEquals(oldInstances.size(), newInstances.size());
      assertExportIds(queryService, newInstances, exportedInstances, mustHaveExportIds);
      for (ProcessInstance process : oldInstances)
      {
         assertThat(
               NL + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("PROCINST_SCOPE", "SCOPEPROCESSINSTANCE",
                     process.getScopeProcessInstanceOID()), is(true));
         assertThat(
               NL + ASSERTION_MSG_HAS_ENTRY_IN_DB,
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

   private void assertNotNullBatches(ExportResult result)
   {
      assertNotNull(result);
      assertFalse(result.hasExportModel());
      assertTrue(result.hasExportData());
   }

   private void assertNotNullModel(ExportResult result)
   {
      assertNotNull(result);
      assertTrue(result.hasExportModel());
      assertFalse(result.hasExportData());
   }

   protected static void assertNotNullExportResult(ExportResult result) throws Exception
   {
      assertNotNull(result);
      assertTrue(result.hasExportModel());
      assertTrue(result.hasExportData());
      if (result.hasExportData())
      {
         for (Date date : result.getDates())
         {
            assertNotNull(date);
         }
      }
   }

   protected static void assertNullRawData(ExportResult result)
   {
      assertFalse(result.hasExportModel());
      assertFalse(result.hasExportData());
      assertEquals(0, result.getPurgeProcessIds().size());
      assertEquals(0, result.getDates().size());
   }

   protected static void assertActivityInstancesEquals(ActivityInstances oldActivities,
         ActivityInstances newActivities) throws Exception
   {
      assertActivityInstancesEquals(oldActivities, newActivities, true);
   }

   protected static void assertActivityInstancesEquals(ActivityInstances oldActivities,
         ActivityInstances newActivities, boolean compareRTOids) throws Exception
   {
      int countCompared = 0;
      assertNotNull(newActivities);
      assertEquals(oldActivities.size(), newActivities.size());
      for (ActivityInstance activity : oldActivities)
      {
         assertThat(
               NL + ASSERTION_MSG_HAS_ENTRY_IN_DB,
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

   private static void assertObjectEquals(Object a, Object b, Object from, boolean compareRTOids)
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
         if (a.getClass() == UserDetails.class) {
            continue;
         }
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
               else if ("runtimeAttributes".equals(property.getName()))
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
