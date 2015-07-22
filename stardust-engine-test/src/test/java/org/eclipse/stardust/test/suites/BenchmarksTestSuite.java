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
package org.eclipse.stardust.test.suites;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.benchmarks.BenchmarkCalendarTest;
import org.eclipse.stardust.test.benchmarks.BenchmarkStatisticsTest;
import org.eclipse.stardust.test.benchmarks.BenchmarksTest;


@RunWith(Suite.class)
@SuiteClasses({
                BenchmarksTest.class,
                BenchmarkCalendarTest.class,
                BenchmarkStatisticsTest.class
              })
public class BenchmarksTestSuite
{
   private static final String[] MODEL_NAME_ARRAY = new String[]{"BenchmarksModel"};

   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_NAME_ARRAY);
}
