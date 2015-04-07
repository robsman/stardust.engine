package org.eclipse.stardust.test.archive;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import org.junit.*;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.jms.core.JmsTemplate;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.TestTimestampProvider;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.transientpi.AbstractTransientProcessInstanceTest;

@ApplicationContextConfiguration(locations = "classpath:app-ctxs/audit-trail-queue.app-ctx.xml")
public class AutoArchiveTest extends AbstractTransientProcessInstanceTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, ArchiveModelConstants.MODEL_ID, ArchiveModelConstants.MODEL_ID_OTHER);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);
   
   private static TestTimestampProvider testTimestampProvider = new TestTimestampProvider();

   @BeforeClass
   public static void setUpOnce()
   {
      System.setProperty(HAZELCAST_LOGGING_TYPE_KEY, HAZELCAST_LOGGING_TYPE_VALUE);
   }

   @AfterClass
   public static void tearDownOnce()
   {
      System.clearProperty(HAZELCAST_LOGGING_TYPE_KEY);
   }
   
   public AutoArchiveTest()
   {
      super(testClassSetup);
   }
   
   @Before
   public void setUp() throws Exception
   { 
      testTimestampProvider = new TestTimestampProvider();
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE,
            testTimestampProvider);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            3);

      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM);
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveManager");
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_ID,
            "testid");
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "false");
      ((MemoryArchiveManager) ArchiveManagerFactory.getCurrent()).clear();
      
      final GlobalParameters params = GlobalParameters.globals();
      params.set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 0);
      params.set(JmsProperties.RESPONSE_HANDLER_RETRY_COUNT_PROPERTY, 0);
      params.set(KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER, SpringAppContextHazelcastJcaConnectionFactoryProvider.class.getName());
      params.set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, true);

      appMayComplete = false;

      dropTransientProcessInstanceStorage();
      dropSerialActivityThreadQueues();
   }

   @After
   public void tearDown()
   {
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE, null);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            null);

      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER, null);
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_CUSTOM,
            null);
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_ARCHIVE_MANAGER_ID,
            null);
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            null);
   }


   @Test
   @SuppressWarnings("unchecked")
   public void autoExportTransientAll() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);


      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      ArchiveTest.startAllProcesses(workflowService, queryService, aQuery);
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      sf.getAdministrationService().abortProcessInstance(pi.getOID());
      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Aborted);
      
      final ProcessInstance pi2 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi2.getOID(),
            ProcessInstanceState.Completed);
      
      FilterOrTerm orTerm = (FilterOrTerm)aQuery.getFilter().getParts().iterator().next();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(9, oldInstances.size());
      assertEquals(29, oldActivities.size());
      archiveQueue();
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);

      //they are already auto exported so should not be exported again
      HashMap<String, Object> descriptors = null;
      ExportResult result = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      assertNotNull(result);
      assertEquals(9, result.getPurgeProcessIds().size());
      assertEquals(0, result.getDates().size());
      
      // double check they are not purged
      oldInstances = queryService.getAllProcessInstances(pQuery);
      oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(9, oldInstances.size());
      assertEquals(29, oldActivities.size());

      archiveQueue();
      // check that they are archived
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      int deleteCount = (Integer) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.PURGE, result, false));
      assertEquals(9, deleteCount);
      ProcessInstances delInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances delActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(delInstances);
      assertNotNull(delActivities);
      assertEquals(0, delInstances.size());
      assertEquals(0, delActivities.size());
      
      // import the backups
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
      assertEquals(9, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances, newInstances, true, true);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportSubProcessesInModelGlobalOverrideAlwaysTransient() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);

      final ActivityInstance writeActivityOuter = ArchiveTest.completeNextActivity(pi,
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
      ArchiveTest.completeNextActivity(subSimple, null, null, queryService, workflowService);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      ActivityInstance writeActivitySub = ArchiveTest.completeNextActivity(subSimpleManual,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService,
            workflowService);
      ArchiveTest.completeNextActivity(subSimpleManual, null, null, queryService, workflowService);

      ArchiveTest.completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ArchiveTest.assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
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
     
      archiveQueue();
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      List<Long> oids = Arrays.asList(pi.getOID());
      HashMap<String, Object> descriptors = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, descriptors,
                  false));
      assertNotNull(rawData);
      assertEquals(3, rawData.getPurgeProcessIds().size());
      assertEquals(0, rawData.getDates().size());

      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, rawData, false);
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);
      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, rawData, false));
      assertEquals(3, deleteCount);

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
      ArchiveTest.assertDataNotExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataNotExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
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

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances);
      ArchiveTest.assertProcessInstancesEquals(oldInstancesSubSimple, newInstancesSubSimple);
      ArchiveTest.assertProcessInstancesEquals(oldInstancesSubManual, newInstancesSubManual);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
      ArchiveTest.assertActivityInstancesEquals(oldActivitiesSubSimple, newActivitiesSubSimple);
      ArchiveTest.assertActivityInstancesEquals(oldActivitiesSubSimpleManual,
            newActivitiesSubSimpleManual);
      ArchiveTest.assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportSubProcessesInModelGlobalOverrideAfterExportAlwaysTransient() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
      
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);

      final ActivityInstance writeActivityOuter = ArchiveTest.completeNextActivity(pi,
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
      ArchiveTest.completeNextActivity(subSimple, null, null, queryService, workflowService);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      ActivityInstance writeActivitySub = ArchiveTest.completeNextActivity(subSimpleManual,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService,
            workflowService);
      ArchiveTest.completeNextActivity(subSimpleManual, null, null, queryService, workflowService);

      ArchiveTest.completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ArchiveTest.assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
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
      archiveQueue();
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      List<Long> oids = Arrays.asList(pi.getOID());
      HashMap<String, Object> descriptors = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, descriptors,
                  false));
      assertNotNull(rawData);
      assertEquals(3, rawData.getPurgeProcessIds().size());
      assertEquals(0, rawData.getDates().size());

      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, rawData, false);
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);
      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, rawData, false));
      assertEquals(3, deleteCount);

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
      ArchiveTest.assertDataNotExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataNotExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
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

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances);
      ArchiveTest.assertProcessInstancesEquals(oldInstancesSubSimple, newInstancesSubSimple);
      ArchiveTest.assertProcessInstancesEquals(oldInstancesSubManual, newInstancesSubManual);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
      ArchiveTest.assertActivityInstancesEquals(oldActivitiesSubSimple, newActivitiesSubSimple);
      ArchiveTest.assertActivityInstancesEquals(oldActivitiesSubSimpleManual,
            newActivitiesSubSimpleManual);
      ArchiveTest.assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportSubProcessesInModelGlobalOverrideAlwaysDeferred() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED);

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);

      final ActivityInstance writeActivityOuter = ArchiveTest.completeNextActivity(pi,
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
      ArchiveTest.completeNextActivity(subSimple, null, null, queryService, workflowService);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      ActivityInstance writeActivitySub = ArchiveTest.completeNextActivity(subSimpleManual,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService,
            workflowService);
      ArchiveTest.completeNextActivity(subSimpleManual, null, null, queryService, workflowService);

      ArchiveTest.completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ArchiveTest.assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
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
      archiveQueue();
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      List<Long> oids = Arrays.asList(pi.getOID());
      HashMap<String, Object> descriptors = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, descriptors,
                  false));
      assertNotNull(rawData);
      assertEquals(3, rawData.getPurgeProcessIds().size());
      assertEquals(0, rawData.getDates().size());

      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, rawData, false);
      Boolean success = (Boolean) workflowService.execute(command);
      assertTrue(success);
      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, rawData, false));
      assertEquals(3, deleteCount);

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
      ArchiveTest.assertDataNotExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataNotExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
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

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances);
      ArchiveTest.assertProcessInstancesEquals(oldInstancesSubSimple, newInstancesSubSimple);
      ArchiveTest.assertProcessInstancesEquals(oldInstancesSubManual, newInstancesSubManual);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
      ArchiveTest.assertActivityInstancesEquals(oldActivitiesSubSimple, newActivitiesSubSimple);
      ArchiveTest.assertActivityInstancesEquals(oldActivitiesSubSimpleManual,
            newActivitiesSubSimpleManual);
      ArchiveTest.assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1, queryService);

      ArchiveTest.assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2, queryService);

   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportTransient() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
//      disableInMemStorageExposal();
//      enableTransientProcessesSupport();
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_TRANSIENT, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
    
      // transient should never be found
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(0, oldInstances.size());
      assertEquals(0, oldActivities.size());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());
   
      // transient should not be archived
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
   }
   
   //test with multiple subprocesses
   //test with multiple rootpis
   //test with write behind
   //test with model changed
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportDeferredWithSubs() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
      enableTransientProcessesSupport();

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED_WITH_SUBS, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      
      for (ProcessInstance p : oldInstances)
      {
         orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(p.getOID()));
      }
      
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(10, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());
      
      ArchiveTest.assertDataExists(pi.getOID(), oldActivities.get(0).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED_WITH_SUBS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 288, queryService);

      archiveQueue();
      
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      IArchive archive = archives.get(0);
      assertEquals(1, archive.getExportIndex().getRootProcessToSubProcesses().size());
      ExportProcess rootProcess = archive.getExportIndex().getRootProcessToSubProcesses().keySet().iterator().next();
      assertEquals(2, archive.getExportIndex().getRootProcessToSubProcesses().get(rootProcess).size());
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      List<Long> oids = Arrays.asList(pi.getOID());
      HashMap<String, Object> descriptors = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, descriptors,
                  false));
      assertNotNull(rawData);
      assertEquals(3, rawData.getPurgeProcessIds().size());
      assertEquals(0, rawData.getDates().size());

      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, rawData, false));
      assertEquals(3, deleteCount);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      
      ArchiveTest.assertDataNotExists(pi.getOID(), oldActivities.get(1).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 288, queryService);

      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, null, null));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
            
      ArchiveTest.assertDataExists(pi.getOID(), oldActivities.get(0).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED_WITH_SUBS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 288, queryService);
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportDeferredUniqueRootPiTransientProcessInstanceSupport() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
      enableTransientProcessesSupport();

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      
      assertEquals(1, oldInstances.size());
      
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(3, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());
      
      ArchiveTest.assertDataExists(pi.getOID(), oldActivities.get(1).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 36, queryService);
      archiveQueue();
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      List<Long> oids = Arrays.asList(pi.getOID());
      HashMap<String, Object> descriptors = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, descriptors,
                  false));
      assertNotNull(rawData);
      assertEquals(1, rawData.getPurgeProcessIds().size());
      assertEquals(0, rawData.getDates().size());

      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, rawData, false));
      assertEquals(1, deleteCount);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      
      ArchiveTest.assertDataNotExists(pi.getOID(), oldActivities.get(1).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 36, queryService);
      
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
            
      ArchiveTest.assertDataExists(pi.getOID(), newActivities.get(1).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 36, queryService);
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportDeferredGlobalOverrideToTransienAfterExport() throws Exception
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
      enableTransientProcessesSupport();

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      
      assertEquals(1, oldInstances.size());
      
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(3, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());
      
      ArchiveTest.assertDataExists(pi.getOID(), oldActivities.get(1).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 36, queryService);
      archiveQueue();
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      List<Long> oids = Arrays.asList(pi.getOID());
      HashMap<String, Object> descriptors = null;
      ExportResult rawData = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, null, oids, descriptors,
                  false));
      assertNotNull(rawData);
      assertEquals(1, rawData.getPurgeProcessIds().size());
      assertEquals(0, rawData.getDates().size());

      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, rawData, false));
      assertEquals(1, deleteCount);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      
      ArchiveTest.assertDataNotExists(pi.getOID(), oldActivities.get(1).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 36, queryService);
      
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQueryRoot);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
            
      ArchiveTest.assertDataExists(pi.getOID(), newActivities.get(1).getOID(),
            ArchiveModelConstants.PROCESS_DEF_DEFERRED,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 36, queryService);
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void autoExportMultipleRootPisTransientProcessInstanceSupport() throws Exception
   {
      enableAutoArchive();
      enableTransientProcessesSupport();
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      ArchiveTest.startAllProcesses(workflowService, queryService, aQuery);
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      sf.getAdministrationService().abortProcessInstance(pi.getOID());
      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Aborted);
      
      final ProcessInstance pi2 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(pi2.getOID(),
            ProcessInstanceState.Completed);
      
      FilterOrTerm orTerm = (FilterOrTerm)aQuery.getFilter().getParts().iterator().next();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(10, oldInstances.size());
      assertEquals(32, oldActivities.size());
      
      archiveQueue();
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);

      //they are already auto exported so should not be exported again
      HashMap<String, Object> descriptors = null;
      ExportResult result = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      assertNotNull(result);
      assertEquals(10, result.getPurgeProcessIds().size());
      assertEquals(0, result.getDates().size());
      
      // double check they are not purged
      oldInstances = queryService.getAllProcessInstances(pQuery);
      oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(10, oldInstances.size());
      assertEquals(32, oldActivities.size());

      // check that they are archived
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      int deleteCount = (Integer) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.PURGE, result, false));
      assertEquals(10, deleteCount);
      ProcessInstances delInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances delActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(delInstances);
      assertNotNull(delActivities);
      assertEquals(0, delInstances.size());
      assertEquals(0, delActivities.size());
      
      // import the backups
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
      assertEquals(10, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances, newInstances, true, true);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
   }
   @Test
   @SuppressWarnings("unchecked")
   public void autoExportConcurrent() throws Exception
   {
      int concurrentThreads = 5;
      
      enableAutoArchive();
      final WorkflowService workflowService = sf.getWorkflowService();
      final QueryService queryService = sf.getQueryService();
      final ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      final FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});

      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      
      ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
      List<Callable<Integer>> callables = new ArrayList<Callable<Integer>>();
      for (int i = 0; i < concurrentThreads; i++)
      {
         Callable<Integer> exportCallable = new Callable<Integer>()
         {
            @Override
            public Integer call() throws Exception
            {
               final ProcessInstance pi = workflowService.startProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
               ArchiveTest.completeNextActivity(pi, ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data", queryService, workflowService);
               ArchiveTest.completeNextActivity(pi, null, null, queryService, workflowService);
               orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
               ProcessInstanceStateBarrier.instance().await(pi.getOID(),
                     ProcessInstanceState.Completed);
               return 1;
            }

         };
         callables.add(exportCallable);
      }
      List<Future<Integer>> results = executor.invokeAll(callables);
      
      int count = 0;
      for (Future<Integer> result : results)
      {
         try
         {
            count += result.get();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      assertEquals(concurrentThreads, count);

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1*concurrentThreads, oldInstances.size());
      assertEquals(3*concurrentThreads, oldActivities.size());
      archiveQueue();
      // check that they are archived
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);

      //they are already auto exported so should not be exported again
      HashMap<String, Object> descriptors = null;
      ExportResult result = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      assertNotNull(result);
      assertEquals(1*concurrentThreads, result.getPurgeProcessIds().size());
      assertEquals(0, result.getDates().size());
      
      // double check they are not purged
      oldInstances = queryService.getAllProcessInstances(pQuery);
      oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1*concurrentThreads, oldInstances.size());
      assertEquals(3*concurrentThreads, oldActivities.size());

      // check that they are archived
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      int deleteCount = (Integer) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.PURGE, result, false));
      assertEquals(1*concurrentThreads, deleteCount);
      ProcessInstances delInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances delActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(delInstances);
      assertNotNull(delActivities);
      assertEquals(0, delInstances.size());
      assertEquals(0, delActivities.size());
      
      // import the backups
      count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
      assertEquals(concurrentThreads, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances, newInstances, true, true);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   private void enableAutoArchive()
   {
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
   }
   
   private void archiveQueue() throws JMSException
   {
      final Queue queue = testClassSetup.queue(JmsProperties.ARCHIVE_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setReceiveTimeout(5000L);

      Message message = null;
      List<ObjectMessage> messages = new ArrayList<ObjectMessage>();
      do {
         message = jmsTemplate.receive(queue);
         if (message != null && !(message instanceof ObjectMessage))
         {
            throw new UnsupportedOperationException("Can only read from Object message.");
         }
         if (message != null)
         {
            ObjectMessage objMessage = (ObjectMessage) message;
            messages.add(objMessage);
         }
      }
      while (message != null);
     
      final ExportProcessesCommand command = new ExportProcessesCommand(messages);
      sf.getWorkflowService().execute(command);
   }
}
