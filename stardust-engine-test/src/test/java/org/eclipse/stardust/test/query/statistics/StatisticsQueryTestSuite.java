/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.query.statistics;

import static org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode.NATIVE_THREADING;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.query.statistics.StatisticsQueryModelConstants.MODEL_ID;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This test suite bundles tests for statistics query functionality.
 * </p>
 *
 * @author Roland.Stamm
 */
@RunWith(Suite.class)
@SuiteClasses({
                ActivityInstanceStatisticsQueryTest.class,
                UserStatisticsQueryTest.class
              })
public class StatisticsQueryTestSuite
{
   /* test suite */

   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), NATIVE_THREADING, MODEL_ID);
}
