package org.eclipse.stardust.test.benchmarks;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.StartOptions;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.model.utils.test.beans.Participant;
import org.eclipse.stardust.engine.core.monitoring.ActivityInstanceStateChangeMonitor;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.engine.core.runtime.utils.ParticipantInfoUtil;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * Class to test benchmark functionality
 *
 * @author Thomas.Wolfram
 *
 */
public class BenchmarksTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BenchmarksModel");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   private StartOptions startOptions_withBenchmark;

   private StartOptions startOptions_withoutBenchmark;

   private static final String BENCHMARK_TEST_REF = "bench1.benchmark";

   private static final String BENCHMARK_PROCESS = "BenchmarkedProcess";

   private static final String BENCHMARK_PROCESS_W_SUB = "BenchmarkedParentProcess";

   private static final String BENCHMARK_ARTIFACT_TYPE_ID = BenchmarkDefinitionArtifactType.TYPE_ID;

   private static final String ARTIFACT_ID = "bench1.benchmark";

   private static final String ARTIFACT_NAME = "Benchmark One";

   private static final String ARTIFACT_CONTENT = "true;";

   @Before
   public void setup()
   {
      deployCalendar("timeOffCalendar-d76edddf-361f-4423-8f70-de8d72b1d277.json");
      serviceFactory.getAdministrationService().deployRuntimeArtifact(
            getRuntimeArtifact(ARTIFACT_ID));

      startOptions_withBenchmark = new StartOptions(null, true, BENCHMARK_TEST_REF);
      startOptions_withoutBenchmark = new StartOptions(null, true);
   }

   private void deployCalendar(String calendarName)
   {
      DocumentManagementService dms = serviceFactory.getDocumentManagementService();

      final String parentFolder = "/business-calendars/timeOffCalendar";

      if (dms.getFolder(parentFolder) != null)
      {
         return;
      }

      final String calendarFilePath = "binaryFiles/" + calendarName;
      final InputStream is = RtEnvHome.class.getClassLoader().getResourceAsStream(
            calendarFilePath);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String read;
      try
      {
         read = br.readLine();
         while (read != null)
         {
            sb.append(read);
            read = br.readLine();
         }
      }
      catch (IOException e)
      {
         Assert.fail(e.getMessage());
      }

      DocumentInfo document = DmsUtils.createDocumentInfo(calendarName);
      byte[] content = sb.toString().getBytes();

      DmsUtils.ensureFolderHierarchyExists(parentFolder, dms);

      Document calDoc = dms.createDocument(parentFolder, document, content, null);

      byte[] checkContent = dms.retrieveDocumentContent(calDoc.getId());
      Assert.assertNotNull(checkContent);
      Assert.assertNotEquals(0, checkContent.length);
   }

   @Test
   public void startProcessInstanceWithBenchmarkTest()
   {

      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);

      assertTrue(0 < pi.getBenchmark());
      assertTrue(0 == pi.getBenchmarkValue());

   }

   @Test
   public void recalculateBenchmarkOnActivityStateChangeTest()
   {

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(instance.getBenchmarkValue(), 0);
   }

   @Test
   public void recaculateBenchmarkOnActivityStateChangeSwitchedOffTest()
   {
      User user = serviceFactory.getUserService().getUser();
      user.addGrant(ParticipantInfoUtil.newModelParticipantInfo("{BenchmarksModel}Rolle1"));
      serviceFactory.getUserService().modifyUser(user);
      
      Map<String, Serializable> pref = CollectionUtils.newMap();
      pref.put(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONCREATE, false);
      pref.put(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONSUSPEND, false);

      Preferences prefs = new Preferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_BENCHMARKS, pref);

      serviceFactory.getAdministrationService().savePreferences(prefs);

      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertEquals(instance.getBenchmarkValue(), 0);

      // Suspend AI
      serviceFactory.getWorkflowService().suspendToDefaultPerformer(instance.getOID());

      serviceFactory.getWorkflowService().activate(instance.getOID());
      instance = serviceFactory.getWorkflowService().getActivityInstance(
            instance.getOID());
      assertEquals(instance.getBenchmarkValue(), 0);

      // Switch Preference to recalulate on suspend
      pref.put(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONSUSPEND, true);

      Preferences changedPrefs = new Preferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_BENCHMARKS, pref);

      serviceFactory.getAdministrationService().savePreferences(changedPrefs);
      
      // Suspend AI again
      serviceFactory.getWorkflowService().activate(instance.getOID());
      serviceFactory.getWorkflowService().suspendToDefaultPerformer(instance.getOID());
      
      instance = serviceFactory.getWorkflowService().getActivityInstance(
            instance.getOID());
      assertNotEquals(instance.getBenchmarkValue(), 0);

   }

   @Test
   public void startSubProcessInstanceWithBenchmarkTest()
   {
      ProcessInstance parentPi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS_W_SUB, startOptions_withBenchmark);

      ActivityInstances instances = serviceFactory.getQueryService()
            .getAllActivityInstances(
                  ActivityInstanceQuery.findAlive("BenchmarkedSubProcess",
                        "BenchmarkedActivity"));

      if (instances.size() > 0)
      {
         ActivityInstance ai = instances.get(0);
         assertEquals(ai.getProcessInstance().getBenchmark(), parentPi.getBenchmark());
      }
      else
      {
         fail("Expected AI in Subprocess not found");
      }

   }

   @Test
   public void runBenchmarkDaemonTest()
   {

      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withoutBenchmark);
      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      serviceFactory.getAdministrationService().startDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);

      DaemonExecutionState state = serviceFactory.getAdministrationService()
            .getDaemon(AdministrationService.BENCHMARK_DAEMON, false)
            .getDaemonExecutionState();

      assertEquals(DaemonExecutionState.OK, state);

      serviceFactory.getAdministrationService().stopDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);

   }

   private RuntimeArtifact getRuntimeArtifact(String artifactId)
   {
      return new RuntimeArtifact(BENCHMARK_ARTIFACT_TYPE_ID, artifactId, ARTIFACT_NAME,
            ARTIFACT_CONTENT.getBytes(), new Date(1));
   }

}
