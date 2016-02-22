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

import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.camel.trigger.generic.GenericTriggerBodyDataMappingTest;
import org.eclipse.stardust.test.camel.trigger.generic.GenericTriggerDataPathTest;
import org.eclipse.stardust.test.camel.trigger.generic.GenericTriggerHeaderDataMappingTest;
import org.eclipse.stardust.test.camel.trigger.generic.GenericTriggerIncludeConverterTest;
import org.eclipse.stardust.test.camel.trigger.generic.GenericTriggerTest;
import org.eclipse.stardust.test.camel.trigger.generic.GenericTriggerWithAdditionalBeanDefinitionTest;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests that deal with <i>IPP</i>'s <i>Camel</i> integration,
 * especially with <i>IPP</i>'s <i>Camel</i> Generic Trigger.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({
                 GenericTriggerTest.class,
                 GenericTriggerBodyDataMappingTest.class,
                 GenericTriggerDataPathTest.class,
                 GenericTriggerHeaderDataMappingTest.class,
                 GenericTriggerIncludeConverterTest.class,
                 GenericTriggerWithAdditionalBeanDefinitionTest.class
              })
public class CamelIntegrationGenericTriggerTestSuite extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String[] MODEL_IDS = { "GenericTriggerTestModel", "GenericTriggerDataPathTestModel", "GenericTriggerConverterTestModel", "CamelTriggerTestModel" };

   @ClassRule
   public static final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @BeforeClass
   public static void setUpOnce()
   {
      DeploymentOptions options = DeploymentOptions.DEFAULT;
      options.setIgnoreWarnings(true);
      RtEnvHome.deployModel(adminSf.getAdministrationService(), options, MODEL_IDS);
   }

   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING);
}
