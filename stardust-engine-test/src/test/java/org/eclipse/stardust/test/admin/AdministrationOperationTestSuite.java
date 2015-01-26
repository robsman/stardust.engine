package org.eclipse.stardust.test.admin;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
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
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING);
}
