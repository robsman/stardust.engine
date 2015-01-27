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
package org.eclipse.stardust.test.suites;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.examples.MyConstants.MODEL_NAME;

import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.examples.MyAppCtxTest;
import org.eclipse.stardust.test.examples.MyDmsTest;
import org.eclipse.stardust.test.examples.MyWorkflowServiceTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This is an example for a functional test suite running in a
 * local Spring environment, using a H2 DB and providing JCR support.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({ MyWorkflowServiceTest.class, MyDmsTest.class, MyAppCtxTest.class })
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/my-app-ctx-test.app-ctx.xml")
public class MyTestSuite
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
}
