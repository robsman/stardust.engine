/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.suites;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.test.admin.AdministrationOperationTest;
import org.eclipse.stardust.test.admin.ChangePasswordTest;
import org.eclipse.stardust.test.admin.ChangeQAProbabilityPercentageTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles test classes focussing on administration operations
 * exposed by {@link AdministrationService}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({
               AdministrationOperationTest.class,
               ChangePasswordTest.class,
               ChangeQAProbabilityPercentageTest.class
             })
public class AdministrationOperationTestSuite
{
}
