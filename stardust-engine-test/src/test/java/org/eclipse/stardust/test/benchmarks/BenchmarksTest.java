package org.eclipse.stardust.test.benchmarks;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.StartOptions;
import org.eclipse.stardust.engine.core.spi.artifact.ArtifactManagerFactory;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
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
      serviceFactory.getAdministrationService().deployRuntimeArtifact(getRuntimeArtifact(ARTIFACT_ID));

      startOptions_withBenchmark = new StartOptions(null, true, BENCHMARK_TEST_REF);
      startOptions_withoutBenchmark = new StartOptions(null, true);
   }

   @Test
   public void startProcessInstanceWithBenchmarkTest()
   {
      
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);

      assertEquals(BENCHMARK_TEST_REF, pi.getBenchmark());

   }

   @Test
   public void recalculateBenchmarkOnActivityStateChangeTest()
   {
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      System.out.println("#### Benchmark Value " + instance.getBenchmarkValue());
      
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
