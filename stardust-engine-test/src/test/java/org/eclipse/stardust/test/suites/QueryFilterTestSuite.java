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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.test.query.filter.QueryFilterTest;

/**
 * <p>
 * This test suite bundles Query Filter related tests.
 * </p>
 *
 * @author Barry.Grotjahn
 */
@RunWith(Suite.class)
@SuiteClasses({
   QueryFilterTest.class
             })
public class QueryFilterTestSuite
{
}