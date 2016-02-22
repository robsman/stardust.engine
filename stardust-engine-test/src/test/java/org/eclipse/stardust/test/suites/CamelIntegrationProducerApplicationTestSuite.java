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
import static org.eclipse.stardust.test.camel.application.generic.producer.ProducerBodyDataMappingTest.MODEL_IDS;

import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.camel.application.generic.producer.IncludeHeadersStrategyTest;
import org.eclipse.stardust.test.camel.application.generic.producer.ProducerBodyDataMappingTest;
import org.eclipse.stardust.test.camel.application.generic.producer.ProducerGeneralConfigurationTest;
import org.eclipse.stardust.test.camel.application.generic.producer.ProducerHeaderDataMappingTest;
import org.eclipse.stardust.test.camel.application.generic.producer.ProducerIncludeConverterTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests that deal with <i>IPP</i>'s <i>Camel</i> integration,
 * especially with <i>Camel</i> producer applications.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({
                IncludeHeadersStrategyTest.class,
                ProducerBodyDataMappingTest.class,
                ProducerGeneralConfigurationTest.class,
                ProducerHeaderDataMappingTest.class,
                ProducerIncludeConverterTest.class
              })
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/camel-producer-application.app-ctx.xml")
public class CamelIntegrationProducerApplicationTestSuite extends AbstractCamelIntegrationTest
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);
}
