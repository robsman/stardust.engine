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
package org.eclipse.stardust.test.preferences;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class focuses on testing the preferences API
 * exposed by {@link AdministrationService}.
 * </p>
 *
 * @author Roland.Stamm
 * @version $Revision$
 */
public class PreferencesTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   /**
    * Tests if UTF-8 strings containing "han" (CJK) characters are saved and loaded correctly.
    * This needs to be supported by java code, jdbc driver and database by setting the encoding to UTF-8.
    * <p>
    * See <br>
    * http://unicode.org/faq/han_cjk.html <br>
    * http://balusc.blogspot.de/2009/05/unicode-how-to-get-characters-right.html <br>
    */
   @Test
   public void testSaveLoadUTF8CJK() {
      Preferences preferences = sf.getAdministrationService().getPreferences(PreferenceScope.PARTITION, "stardust-test", "stardust-test");

      preferences.getPreferences().put("tsurugi", "ML劍");

      sf.getAdministrationService().savePreferences(preferences);

      Preferences preferences2 = sf.getAdministrationService().getPreferences(PreferenceScope.PARTITION, "stardust-test", "stardust-test");

      Assert.assertEquals(preferences.getPreferences(), preferences2.getPreferences());

   }
}
