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
package org.eclipse.stardust.test.multimodel;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.multimodel.MultiModelConstants.PROVIDER_MODEL_ID;
import static org.eclipse.stardust.test.multimodel.MultiModelConstants.CONSUMER_MODEL_ID;

import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSuiteSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests that verify multiple, cross-referencing models.
 * </p>
 *
 * @author Robert Sauer
 * @version $Revision$
 */
@RunWith(Suite.class)
@SuiteClasses({
               RetrieveModelDetailsTest.class
             })
public class MultiModelTestSuite
{
   /* test suite */

   @ClassRule
   public static final LocalJcrH2TestSuiteSetup testSuiteSetup = new LocalJcrH2TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, PROVIDER_MODEL_ID, CONSUMER_MODEL_ID);
}
