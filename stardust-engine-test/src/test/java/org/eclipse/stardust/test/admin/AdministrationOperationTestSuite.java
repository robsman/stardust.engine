package org.eclipse.stardust.test.admin;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
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
 * @version $Revision$
 */
@RunWith(Suite.class)
@SuiteClasses({
               AdministrationOperationTest.class
             })
public class AdministrationOperationTestSuite
{
   /* test suite */

   @ClassRule
   public static final LocalJcrH2TestSuiteSetup testSuiteSetup = new LocalJcrH2TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING);
}
