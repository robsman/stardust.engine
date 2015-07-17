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
import static org.eclipse.stardust.test.spawn.SpawnPeerProcessCrossModelTest.MODEL_NAME;
import static org.eclipse.stardust.test.spawn.SpawnPeerProcessCrossModelTest.MODEL_NAME2;

import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.spawn.SpawnPeerProcessCrossModelTest;
import org.eclipse.stardust.test.spawn.SpawnProcessCrossModelTest;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests for the <i>Spawn Process</i> functionality,
 * which allows for ad hoc spawning of process instances (refer to the Stardust documentation
 * for details about <i>Spawn Process</i>).<br>
 * This suite targets spawning processes across different models.
 * </p>
 *
 * @author Roland.Stamm
 */
@RunWith(Suite.class)
@SuiteClasses({ SpawnPeerProcessCrossModelTest.class, SpawnProcessCrossModelTest.class })
public class SpawnProcessMultiModelTestSuite
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_NAME, MODEL_NAME2);
}
