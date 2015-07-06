/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.test.benchmarks;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Map;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkResult;
import org.eclipse.stardust.engine.core.monitoring.ActivityInstanceStateChangeMonitor;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.engine.core.runtime.utils.ParticipantInfoUtil;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;

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

   private static final String BENCHMARK_PROCESS = "BenchmarkedProcess";

   private static final String BENCHMARK_PROCESS_W_SUB = "BenchmarkedParentProcess";

   private static final String BENCHMARK_REF = "benchmarksTest.benchmark";

   @Before
   public void setup()
   {
      BenchmarkTestUtils.deployBenchmark("benchmarksTest.benchmark", serviceFactory);

      startOptions_withBenchmark = new StartOptions(null, true, BENCHMARK_REF);
      startOptions_withoutBenchmark = new StartOptions(null, true);
   }

   @Test
   public void startProcessInstanceWithBenchmarkTest()
   {
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);

      assertTrue(0 < pi.getBenchmark());
      assertTrue(0 == pi.getBenchmarkResult().getCategory());

   }

   @Test
   public void startProcessInstanceWithDefaultBenchmarkTest()
   {
      // Setting default benchmark in preferences
      
      Map<String, Serializable> pref = CollectionUtils.newMap();
      pref.put("{BenchmarksModel}BenchmarkedProcess", BENCHMARK_REF);
      
      Preferences prefs = new Preferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_DEFAULT_BENCHMARKS, pref);

      serviceFactory.getAdministrationService().savePreferences(prefs);

      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withoutBenchmark);
      
      assertTrue(0 < pi.getBenchmark());
      assertTrue(0 == pi.getBenchmarkResult().getCategory());      
   }

   @Test
   public void recalculateBenchmarkOnActivityStateChangeTest()
   {

      // Test for default
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);

      ActivityInstance instance = serviceFactory.getQueryService()
            .findFirstActivityInstance(ActivityInstanceQuery.findAlive());

      assertNotEquals(instance.getBenchmarkResult().getCategory(), 0);

      // Check if properties are available
      assertEquals("Late", instance.getBenchmarkResult().getProperties().get("name"));
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

      assertEquals(instance.getBenchmarkResult().getCategory(), 0);

      // Suspend AI
      serviceFactory.getWorkflowService().suspendToDefaultPerformer(instance.getOID());

      serviceFactory.getWorkflowService().activate(instance.getOID());
      instance = serviceFactory.getWorkflowService().getActivityInstance(
            instance.getOID());
      assertEquals(instance.getBenchmarkResult().getCategory(), 0);

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

      assertNotEquals(instance.getBenchmarkResult().getCategory(), 0);

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
      ProcessInstance pi1 = serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);
      ProcessInstance pi2 = serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withoutBenchmark);
      ProcessInstance pi3 = serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      serviceFactory.getAdministrationService().startDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);

      DaemonExecutionState state = serviceFactory.getAdministrationService()
            .getDaemon(AdministrationService.BENCHMARK_DAEMON, false)
            .getDaemonExecutionState();

      assertEquals(DaemonExecutionState.OK, state);

      serviceFactory.getAdministrationService().stopDaemon(
            AdministrationService.BENCHMARK_DAEMON, true);
      
      BenchmarkResult res1 = serviceFactory.getWorkflowService()
            .getProcessInstance(pi1.getOID())
            .getBenchmarkResult();
      
      BenchmarkResult res2 = serviceFactory.getWorkflowService()
            .getProcessInstance(pi2.getOID())
            .getBenchmarkResult();      


      assertTrue(res1.getCategory() > 0);
      assertTrue(res2 == null);

   }
   
   @Test(expected = IllegalOperationException.class)
   public void disallowDeleteOfBenchmarkDefinitionForActiveInstancesTest()
   {
      ProcessInstance pi = serviceFactory.getWorkflowService().startProcess(
            BENCHMARK_PROCESS, startOptions_withBenchmark);
      
      long benchmarkOid = pi.getBenchmark();
      
      serviceFactory.getAdministrationService().deleteRuntimeArtifact(benchmarkOid);
      
      Assert.fail("Should throw exception");
   }

   @Test
   public void queryBenchmarkResult()
   {
      BenchmarkTestUtils.deployBenchmark("example.benchmark", serviceFactory);

      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            new StartOptions(null, true, "example.benchmark"));

      serviceFactory.getWorkflowService().startProcess(BENCHMARK_PROCESS,
            startOptions_withBenchmark);

      ActivityInstanceQuery findAlive = ActivityInstanceQuery.findAlive();

      findAlive.where(ActivityInstanceQuery.BENCHMARK_OID.greaterThan(0));
      findAlive.where(ActivityInstanceQuery.BENCHMARK_VALUE.greaterOrEqual(0));

      findAlive.orderBy(ActivityInstanceQuery.BENCHMARK_OID, false);
      findAlive.orderBy(ActivityInstanceQuery.BENCHMARK_VALUE);

      ActivityInstances instances = serviceFactory.getQueryService()
            .getAllActivityInstances(findAlive);

      assertEquals(2, instances.size());

      ActivityInstance ai1 = instances.get(0);
      ActivityInstance ai2 = instances.get(1);
      assertTrue(ai1.getProcessInstance().getBenchmark() > ai2.getProcessInstance()
            .getBenchmark());
   }

}
