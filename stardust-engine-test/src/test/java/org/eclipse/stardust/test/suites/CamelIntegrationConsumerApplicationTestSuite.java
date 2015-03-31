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
import static org.eclipse.stardust.test.camel.application.generic.consumer.ConsumerBodyDataMappingTest.MODEL_ID;

import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.camel.application.generic.consumer.ConsumerBodyDataMappingTest;
import org.eclipse.stardust.test.camel.application.generic.consumer.ConsumerGeneralConfigurationTest;
import org.eclipse.stardust.test.camel.application.generic.consumer.ConsumerHeaderDataMappingTest;
import org.eclipse.stardust.test.camel.application.generic.consumer.ConsumerIncludeConverterTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests that deal with <i>IPP</i>'s <i>Camel</i> integration,
 * especially with <i>Camel</i> consumer applications.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({
                ConsumerBodyDataMappingTest.class,
                ConsumerGeneralConfigurationTest.class,
                ConsumerHeaderDataMappingTest.class,
                ConsumerIncludeConverterTest.class
              })
public class CamelIntegrationConsumerApplicationTestSuite extends AbstractCamelIntegrationTest
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_ID);
}
