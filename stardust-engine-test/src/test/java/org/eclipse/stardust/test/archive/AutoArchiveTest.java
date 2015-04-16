package org.eclipse.stardust.test.archive;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.engine.spring.integration.jms.archiving.ArchiveQueueAggregator;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.TestTimeZoneProvider;
import org.eclipse.stardust.test.api.util.TestTimestampProvider;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.transientpi.AbstractTransientProcessInstanceTest;


@ApplicationContextConfiguration(locations = "classpath:app-ctxs/archive.app-ctx.xml")
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
   
   ArchiveQueueAggregator aggregator;
   
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
   public void init() throws Exception
   {
      setUp();
      // clear any messages from queue
      clearQueue();
      ((MemoryArchiveManager) ArchiveManagerFactory.getCurrent()).clear();
   }
   
   
   public void setUp() throws Exception
   { 
      aggregator = SpringUtils.getApplicationContext().getBean("ArchiveQueueAggregator", ArchiveQueueAggregator.class);
      GlobalParameters.globals().set(ArchiveManagerFactory.CARNOT_AUTO_ARCHIVE,
            "true");
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
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, null);
      GlobalParameters.globals().set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, null);

      GlobalParameters.globals().set(KernelTweakingProperties.ASYNC_WRITE, null);
   }


   @Test
   @SuppressWarnings("unchecked")
   public void autoExportTransientAll() throws Exception
   {
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
      
      // deferred is forced to be transient due to global, so it must not be saved/exported/imported
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

      archiveQueue();
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

      archiveQueue();
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
   
   @Test
   public void testExportAllTwoModels() throws Exception
   {
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT);

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      ArchiveTest.completeSimpleManual(simpleManualA, queryService, workflowService);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      ArchiveTest.completeSimpleManual(simpleManualB, queryService, workflowService);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      ArchiveTest.completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      ArchiveTest.completeSimple(simpleB, queryService, workflowService);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      ArchiveTest.completeSubProcessesInModel(subProcessesInModel, queryService, workflowService,
            false, testTimestampProvider);
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      ActivityInstance writeActivity = ArchiveTest.completeScriptProcess(scriptProcess, 10, "aaa",
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

      HashMap<String, Object> descriptors = null;
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      ArchiveTest.assertNotNullExportResult(exportResult);
     
      archiveQueue();
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      
      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, exportResult, false));
      assertEquals(8, deleteCount);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      
      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      adminService.deleteModel(modelOID);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER2);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER);
      setUp();
      
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), null, null));
      models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER2));
      model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      setUp();
      assertEquals(8, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances, newInstances, false);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities, false);

      ArchiveTest.assertDataExists(scriptProcess.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20, queryService);

      assertTrue(ArchiveTest.hasStructuredDateField(scriptProcess.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      assertTrue(ArchiveTest.hasStructuredDateField(scriptProcess.getOID(),
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDA, "aaa"));
   }
   
   @Test
   public void testModelRedeployBeforeArchive() throws Exception
   {
      enableTransientProcessesSupport();
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      ArchiveTest.completeSimpleManual(simpleManualA, queryService, workflowService);
      int modelOID1 = simpleManualA.getModelOID();

      final ProcessInstance piOtherModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      ArchiveTest.completeOther(piOtherModel, 5, queryService, workflowService);
      int modelOID2 = piOtherModel.getModelOID();

      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);
      
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
            
      final ProcessInstance piDeferred = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(piDeferred.getOID(),
            ProcessInstanceState.Completed);
      
    
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOtherModel.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piDeferred.getOID()));

      // we have completed three processes from two different models
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(10, oldActivities.size());

      // clear the completed processes without archive queue cleared yet
      HashMap<String, Object> descriptors = null;
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      ArchiveTest.assertNotNullExportResult(exportResult);

      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, exportResult, false));
      assertEquals(3, deleteCount);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      
      // delete all the models, then redeploy them so they have different ids
      adminService.deleteModel(modelOID2);
      adminService.deleteModel(modelOID1);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER2);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER);
      setUp();
      
      // start all the processes again, now with different model oids
      final ProcessInstance simpleManualA2 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      ArchiveTest.completeSimpleManual(simpleManualA2, queryService, workflowService);
      int modelOID12 = simpleManualA2.getModelOID();
      
      final ProcessInstance piOtherModel2 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      ArchiveTest.completeOther(piOtherModel2, 5, queryService, workflowService);
      int modelOID22 = piOtherModel2.getModelOID();

      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);
            
      final ProcessInstance piDeferred2 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(piDeferred2.getOID(),
            ProcessInstanceState.Completed);
      
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOtherModel2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piDeferred2.getOID()));
      
      // clear the completed processes without archive queue cleared yet
      exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      ArchiveTest.assertNotNullExportResult(exportResult);

      deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, exportResult, false));
      assertEquals(3, deleteCount);

      clearedInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(clearedInstances);
      assertEquals(0, clearedInstances.size());
      
      archiveQueue();
      
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(2, archives.size());

      IArchive archive1 = null;
      IArchive archive2 = null;
      for (IArchive archive : archives)
      {
         if (modelOID1 == archive.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID))
         {
            archive1 = archive;
         }
         if (modelOID12 == archive.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID))
         {
            archive2 = archive;
         }
      }
      assertNotNull(archive1);
      assertNotNull(archive2);
      assertEquals(2, archive1.getExportModel().getModelIdToOid().size());
      assertEquals(2, archive2.getExportModel().getModelIdToOid().size());
      assertTrue(archive1.getExportModel().getModelIdToOid().containsKey(ArchiveModelConstants.MODEL_ID));
      assertTrue(archive1.getExportModel().getModelIdToOid().containsKey(ArchiveModelConstants.MODEL_ID_OTHER));
      assertTrue(modelOID1 == archive1.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID));
      assertTrue(modelOID2 == archive1.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID_OTHER));
      assertTrue(archive2.getExportModel().getModelIdToOid().containsKey(ArchiveModelConstants.MODEL_ID));
      assertTrue(archive2.getExportModel().getModelIdToOid().containsKey(ArchiveModelConstants.MODEL_ID_OTHER));
      assertTrue(modelOID12 == archive2.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID));
      assertTrue(modelOID22 == archive2.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID_OTHER));
      
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive1, null, null));
      assertEquals(3, count);
         count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive2, null, null));
      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER2));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      setUp();
      assertEquals(6, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertEquals(6, newInstances.size());
      assertEquals(20, newActivities.size());
   }
   
   @Test
   public void testMultiModelDeleteBeforeArchive() throws Exception
   {
      enableTransientProcessesSupport();
      
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      ArchiveTest.completeSimpleManual(simpleManualA, queryService, workflowService);
      int modelOID1 = simpleManualA.getModelOID();

      final ProcessInstance piOtherModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_OTHER, null, true);

      ArchiveTest.completeOther(piOtherModel, 5, queryService, workflowService);
      int modelOID2 = piOtherModel.getModelOID();


      ProcessInstanceStateBarrier.instance().await(piOtherModel.getOID(),
            ProcessInstanceState.Completed);
      
      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
            
      final ProcessInstance piDeferred = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED, null, true);

      ProcessInstanceStateBarrier.instance().await(piDeferred.getOID(),
            ProcessInstanceState.Completed);
      
    
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piOtherModel.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(piDeferred.getOID()));

      // we have completed three processes from two different models
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(10, oldActivities.size());

      // clear the completed processes without archive queue cleared yet
      HashMap<String, Object> descriptors = null;
      ExportResult exportResult = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      ArchiveTest.assertNotNullExportResult(exportResult);

      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, exportResult, false));
      assertEquals(3, deleteCount);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      
      // delete all the models, then redeploy them so they have different ids
      adminService.deleteModel(modelOID2);
      adminService.deleteModel(modelOID1);

      setUp();
      // archive whilst no models active
      archiveQueue();
      
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER2);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID);
      RtEnvHome.deploy(adminService, null, ArchiveModelConstants.MODEL_ID_OTHER);
      
      
      @SuppressWarnings("unchecked")
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
            
      IArchive archive = archives.get(0);
      assertEquals(2, archive.getExportModel().getModelIdToOid().size());
      assertTrue(archive.getExportModel().getModelIdToOid().containsKey(ArchiveModelConstants.MODEL_ID));
      assertTrue(archive.getExportModel().getModelIdToOid().containsKey(ArchiveModelConstants.MODEL_ID_OTHER));
      assertTrue(modelOID1 == archive.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID));
      assertTrue(modelOID2 == archive.getExportModel().getModelIdToOid().get(ArchiveModelConstants.MODEL_ID_OTHER));
      
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, null, null));
      Models models = queryService.getModels(DeployedModelQuery
            .findForId(ArchiveModelConstants.MODEL_ID_OTHER2));
      DeployedModelDescription model = models.get(0);
      adminService.deleteModel(model.getModelOID());
      setUp();
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances, newInstances, false);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities, false);

   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testExportImportDeferredWithSubs() throws Exception
   {
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
   
   @SuppressWarnings("unchecked")
   @Test
   public void testDatesAndHours() throws Exception
   {
      enableTransientProcessesSupport();
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(0, archives.size());
      Date simpleManualADate = testTimestampProvider.getTimestamp(); //1.1.2080 00:00
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      testTimestampProvider.nextDay();
      ArchiveTest.completeSimpleManual(simpleManualA, queryService, workflowService);
      Date simpleManualBDate = testTimestampProvider.getTimestamp(); //2.1.2080 00:00
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      ArchiveTest.completeSimpleManual(simpleManualB, queryService, workflowService);
      testTimestampProvider.nextHour();
      Date simpleDate = testTimestampProvider.getTimestamp(); //2.1.2080 01:00
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      ArchiveTest.completeSimple(simpleA, queryService, workflowService);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      testTimestampProvider.nextHour();
      ArchiveTest.completeSimple(simpleB, queryService, workflowService);
      Date subProcessesInModelDate = testTimestampProvider.getTimestamp(); //2.1.2080 02:00
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      Date subProcessesDate = ArchiveTest.completeSubProcessesInModel(subProcessesInModel,
            queryService, workflowService, true, testTimestampProvider);
      testTimestampProvider.nextHour();
      Date deferredDate = testTimestampProvider.getTimestamp(); //2.1.2080 04:00
      final ProcessInstance piDeferred = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED_WITH_SUBS, null, true);

      ProcessInstanceStateBarrier.instance().await(piDeferred.getOID(),
            ProcessInstanceState.Completed);
      testTimestampProvider.nextDay();
      Date scriptProcessDate = testTimestampProvider.getTimestamp(); //3.1.2080 04:00
      final ProcessInstance scriptProcess = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);
      ArchiveTest.completeScriptProcess(scriptProcess, 10, "aaa", queryService, workflowService);

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
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      
      for (ProcessInstance p : oldInstances)
      {
         orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(p.getOID()));
      }

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
         if (process.getOID() == piDeferred.getOID())
         {
            assertEquals(dateFormat.parse("2.1.2080 04:00"), process.getStartTime());
            assertEquals(deferredDate, process.getStartTime());
         }
         if (process.getOID() == scriptProcess.getOID())
         {
            assertEquals(dateFormat.parse("3.1.2080 04:00"), process.getStartTime());
            assertEquals(scriptProcessDate, process.getStartTime());
         }
      }
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(11, oldInstances.size());
      assertEquals(38, oldActivities.size());


      archiveQueue();
      HashMap<String, Object> descriptors = null;
      ExportResult result = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      assertEquals(11, result.getPurgeProcessIds().size());
      assertEquals(0, result.getDates().size());

      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);
      int deleteCount = (Integer) workflowService.execute(new ExportProcessesCommand(
            ExportProcessesCommand.Operation.PURGE, result, false));
      assertEquals(11, deleteCount);


      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(6, archives.size());
      ImportMetaData meta = (ImportMetaData) workflowService
            .execute(new ImportProcessesCommand(
                  ImportProcessesCommand.Operation.VALIDATE, archives.get(0),null,  null));
      assertNotNull(meta);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, ArchiveTest.getArchive(scriptProcessDate,
                  archives), null, meta));
      assertEquals(1, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, ArchiveTest.getArchive(simpleManualADate,
                  archives), null, meta));
      assertEquals(2, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, ArchiveTest.getArchive(simpleManualBDate,
                  archives), null, meta));
      assertEquals(3, count);
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, ArchiveTest.getArchive(simpleDate, archives),
            null, meta));
      assertEquals(5, count);
      // assert that sub processes are archived with their root process
      assertNull(ArchiveTest.getArchive(subProcessesDate, archives));
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, ArchiveTest.getArchive(deferredDate,
                  archives), null, meta));
      count += (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, ArchiveTest.getArchive(subProcessesInModelDate,
                  archives), null, meta));
      assertEquals(11, count);

      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testDeferredGlobalOverrideToTransienAfterExport() throws Exception
   {
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
   public void autoExportWriteBehind() throws Exception
   {
      enableTransientProcessesSupport();
      enableWriteBehind();
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
      
      final ProcessInstance pi3 = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DEFERRED_WITH_SUBS, null, true);

      ProcessInstanceStateBarrier.instance().await(pi3.getOID(),
            ProcessInstanceState.Completed);
      
      FilterOrTerm orTerm = (FilterOrTerm)aQuery.getFilter().getParts().iterator().next();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));

      writeFromQueueToAuditTrail(sf, JmsProperties.AUDIT_TRAIL_QUEUE_NAME_PROPERTY);
      writeFromQueueToAuditTrail(sf, JmsProperties.AUDIT_TRAIL_QUEUE_NAME_PROPERTY);
      
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(13, oldInstances.size());
      assertEquals(36, oldActivities.size());
      
      archiveQueue();
      ArchiveTest.assertExportIds(oldInstances, oldInstances, true);

      //they are already auto exported so should not be exported again
      HashMap<String, Object> descriptors = null;
      ExportResult result = (ExportResult) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, descriptors, false));
      assertNotNull(result);
      assertEquals(13, result.getPurgeProcessIds().size());
      assertEquals(0, result.getDates().size());
      
      // double check they are not purged
      oldInstances = queryService.getAllProcessInstances(pQuery);
      oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(13, oldInstances.size());
      assertEquals(36, oldActivities.size());

      // check that they are archived
      archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(null));
      assertEquals(1, archives.size());
      IArchive archive = archives.get(0);
     // assertEquals(11, archive.getExportIndex().getRootProcessToSubProcesses().keySet().size());
      int deleteCount = (Integer) workflowService
            .execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.PURGE, result, false));
      assertEquals(13, deleteCount);
      ProcessInstances delInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances delActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(delInstances);
      assertNotNull(delActivities);
      assertEquals(0, delInstances.size());
      assertEquals(0, delActivities.size());

      // import the backups
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, null, null));
      assertEquals(13, count);

      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      ArchiveTest.assertProcessInstancesEquals(oldInstances, newInstances, newInstances, true, true);
      ArchiveTest.assertActivityInstancesEquals(oldActivities, newActivities);
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void autoExportMultipleRootPisTransientProcessInstanceSupport() throws Exception
   {
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
     
   private void archiveQueue() throws JMSException
   {
      aggregator.doAggregate(testClassSetup.queueConnectionFactory(), testClassSetup.queue(JmsProperties.EXPORT_QUEUE_NAME_PROPERTY),
            testClassSetup.queue(JmsProperties.ARCHIVE_QUEUE_NAME_PROPERTY));
      
      final Queue queue = testClassSetup.queue(JmsProperties.ARCHIVE_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setReceiveTimeout(2000L);

      Message message = null;
     
      message = jmsTemplate.receive(queue);
      if (message != null && !(message instanceof ObjectMessage))
      {
         throw new UnsupportedOperationException("Can only read from Object message.");
      }
         
      if (message != null)
      {
         final ExportProcessesCommand command = new ExportProcessesCommand((ObjectMessage) message);
         sf.getWorkflowService().execute(command);
      }
   }
   
   private void clearQueue() throws JMSException
   {
      aggregator.doAggregate(testClassSetup.queueConnectionFactory(), testClassSetup.queue(JmsProperties.EXPORT_QUEUE_NAME_PROPERTY),
            testClassSetup.queue(JmsProperties.ARCHIVE_QUEUE_NAME_PROPERTY));
      
      final Queue queue = testClassSetup.queue(JmsProperties.ARCHIVE_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setReceiveTimeout(2000L);
      jmsTemplate.receive(queue);
   }
   
   private void enableWriteBehind()
   {
      GlobalParameters.globals().set(KernelTweakingProperties.ASYNC_WRITE, true);
   }
}
