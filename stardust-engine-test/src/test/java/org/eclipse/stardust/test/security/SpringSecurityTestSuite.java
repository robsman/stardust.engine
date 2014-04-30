package org.eclipse.stardust.test.security;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests for security functionality.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
@RunWith(Suite.class)
@SuiteClasses({ SpringInternalAuthenticationTest.class,
                SpringPrincipalAuthenticationTest.class
              })
public class SpringSecurityTestSuite
{
   /* test suite */

   @ClassRule
   public static final LocalJcrH2TestSuiteSetup testSuiteSetup = new LocalJcrH2TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING);
}
