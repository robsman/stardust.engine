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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.test.datacluster.DataClusterTest;
import org.eclipse.stardust.test.datacluster.DescriptorDataClusterTest;
import org.eclipse.stardust.test.datacluster.DescriptorFilterDataClusterTest;
import org.eclipse.stardust.test.datacluster.DescriptorOrderDataClusterTest;

/**
 * <p>
 * This test suite bundles tests for functionality regarding datacluster.
 * </p>
 *
 * @author Antje.Fuhrmann
 */
@RunWith(Suite.class)
@SuiteClasses({
      DataClusterTest.class, DescriptorDataClusterTest.class,
      DescriptorFilterDataClusterTest.class, DescriptorOrderDataClusterTest.class})
public class DataClusterTestSuite
{

//   @ClassRule
//   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(
//         new DataClusterTestClassSetup(new UsernamePasswordPair(MOTU, MOTU),
//               ForkingServiceMode.NATIVE_THREADING, DataClusterTest.MODEL_NAME));

}
