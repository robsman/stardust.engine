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

import org.eclipse.stardust.test.authorization.AuditorRoleAuthorizationTest;
import org.eclipse.stardust.test.authorization.DataAuthorizationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This test suite bundles tests for the <i>authorization</i> functionality.
 *
 * @author Florin.Herinean
 */
@RunWith(Suite.class)
@SuiteClasses({
      DataAuthorizationTest.class,
      AuditorRoleAuthorizationTest.class
})
public class AuthorizationTestSuite
{
}
