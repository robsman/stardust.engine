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
package org.eclipse.stardust.test.daemon;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MODEL_NAME;
import static org.junit.Assert.assertNotNull;

import java.util.Timer;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.core.runtime.beans.BenchmarkDaemon;
import org.eclipse.stardust.engine.core.runtime.beans.EventDaemon;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * /**
 * <p>
 * This class tests the monitoring functionality of the DaemonExecutionMonitor
 * </p>
 *
 * @author Thomas.Wolfram
 *
 */
public class DaemonExecutionMonitorTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

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

   @Test
   public void testMonitorUserOperations()
   {

      TestDaemonExecutionMonitorLog executionLog = TestDaemonExecutionMonitorLog.getInstance();

      serviceFactory.getAdministrationService().startDaemon(BenchmarkDaemon.ID, true);

      try
      {
         Thread.sleep(10000);
      }
      catch (Exception e)
      {
         org.junit.Assert.fail(e.getMessage());
      }

      serviceFactory.getAdministrationService().stopDaemon(BenchmarkDaemon.ID, true);

      // Test if log entry has been written after creation of user
      assertNotNull(executionLog.findLogEntryForMethod("beforeExecute"));

      // Test if log entry has been written after creation of user
      assertNotNull(executionLog.findLogEntryForMethod("afterExecute"));

   }

}
