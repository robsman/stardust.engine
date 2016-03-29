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

import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.camel.application.script.javascript.IncludeJavaClassTest;
import org.eclipse.stardust.test.camel.application.script.javascript.JavaScriptModelListTest;
import org.eclipse.stardust.test.camel.application.script.javascript.JavaScriptModelTest;
import org.eclipse.stardust.test.camel.application.script.javascript.TestJavaScriptCrossModel;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests that deal with <i>IPP</i>'s <i>Camel</i> integration,
 * especially with <i>IPP</i>'s <i>Camel</i> activity endpoint.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({
      JavaScriptModelTest.class, JavaScriptModelListTest.class,
      IncludeJavaClassTest.class, TestJavaScriptCrossModel.class})
public class CamelIntegrationScriptingApplicationTestSuite
      extends AbstractCamelIntegrationTest
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(
         new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING,
         JavaScriptModelTest.MODEL_1_ID, JavaScriptModelTest.MODEL_2_ID,
         JavaScriptModelTest.MODEL_3_ID, JavaScriptModelListTest.MODEL_ID,
         IncludeJavaClassTest.MODEL_ID, TestJavaScriptCrossModel.MODEL_1_ID,
         TestJavaScriptCrossModel.MODEL_2_ID);
}
