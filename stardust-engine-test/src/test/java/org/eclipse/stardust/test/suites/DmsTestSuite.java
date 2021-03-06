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
import static org.eclipse.stardust.test.dms.DmsModelConstants.DMS_MODEL_NAME;

import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.dms.*;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests for the <i>Document Management</i> functionality,
 * which allows for storing data in a JCR compliant repository (refer to the Stardust
 * documentation for details about <i>Document Management</i>).
 * </p>
 *
 * @author Nicolas.Werlein, Roland.Stamm
 */
@RunWith(Suite.class)
@SuiteClasses({
   DmsSanityTest.class,
   DmsDocumentAnnotationsTest.class,
   DmsDocumentSearchTest.class,
   DmsSecurityTest.class,
   DmsSecurityScopedTest.class,
   DmsMultiRepositoryTest.class,
   DmsFederatedDocumentSearchTest.class,
   DmsCrossRepositoryMoveTest.class,
   DmsAuditTrailMetaDataTest.class,
   DmsMultiRepositoryMultiPartitionTest.class})
public class DmsTestSuite
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, DMS_MODEL_NAME);
}
