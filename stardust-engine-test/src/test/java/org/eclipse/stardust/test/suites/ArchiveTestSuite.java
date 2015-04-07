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

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.archive.ArchiveModelConstants;
import org.eclipse.stardust.test.archive.ArchiveTest;
import org.eclipse.stardust.test.archive.AutoArchiveTest;

/**
 * <p>
 * This test suite bundles tests for the <i>Archive</i> functionality.
 * </p>
 *
 * @author jsaayman
 * @version $Revision$
 */
@RunWith(Suite.class)
@SuiteClasses({
   ArchiveTest.class, AutoArchiveTest.class
              })
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/audit-trail-queue.app-ctx.xml")
public class ArchiveTestSuite
{
   /* test suite */

   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.JMS, ArchiveModelConstants.MODEL_ID, ArchiveModelConstants.MODEL_ID_OTHER);

}
