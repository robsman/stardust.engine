package org.eclipse.stardust.test.suites;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.spi.FastCachingSequenceGeneratorTest;
import org.eclipse.stardust.test.spi.PartitionMonitorTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests for <i>SPI</i> functionality, which allows for
 * integrating custom behaviour via the SPI mechanism (refer to the Stardust documentation
 * for details about available SPIs).
 * </p>
 *
 * @author Nicolas.Werlein
 */
@RunWith(Suite.class)
@SuiteClasses({FastCachingSequenceGeneratorTest.class,
               PartitionMonitorTest.class})
public class SpiTestSuite
{
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(
         new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, "BasicWorkflowModel");
}
