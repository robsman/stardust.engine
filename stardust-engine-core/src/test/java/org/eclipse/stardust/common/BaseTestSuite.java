/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * DefaultCallbackHandler class for Package Test Classes Each TestClass existing in This
 * Package has to be entered in the method suite()
 */

@RunWith(Suite.class)
@SuiteClasses({
      Base64Test.class, CompareHelperTest.class, DateUtilsTest.class, KeysTest.class,
      MoneyTest.class, PairTest.class, ScopedCollectionsTest.class, UtilsTest.class})
public class BaseTestSuite
{

}
