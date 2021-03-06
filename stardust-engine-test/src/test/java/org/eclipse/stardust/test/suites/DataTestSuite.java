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
import static org.eclipse.stardust.test.data.DataModelConstants.*;

import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.data.*;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests for functionality regarding
 * process data.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({
               DefaultValuePrimitiveDataTest.class,
               FormalParameterValidityTest.class,
               PrimitiveDataInOutDataMappingsTest.class,
               PrimitiveDataInOutDataPathsTest.class,
               InitialValuePrimitiveDataTest.class,
               StructuredDataSanityTest.class,
               StructuredTypeDefinitionTest.class,
               VolatileDataTest.class,
               ConstantsDataMappingsTest.class,
               DataDescriptorInjectionModelExtenderTest.class,
               DataHistoryTest.class,
               EnumDataInOutDataMappingsTest.class,
               MandatoryDataMappingTest.class
             })
public class DataTestSuite
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, true, MODEL_NAME, VOLATILE_MODEL_NAME, CONSTANT_MODEL_NAME, SIMPLE_MODEL_NAME, COMPOSITE_DESCRIPTOR_MODEL_NAME, ENUM_MODEL_NAME, MANDATORY_MODEL_NAME);
}
