package org.eclipse.stardust.test.boundaryevent;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.boundaryevent.BoundaryEventModelConstants.MODEL_ID;

import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * TODO (nw) javadoc
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
@RunWith(Suite.class)
@SuiteClasses({ BoundaryEventTest.class })
public class BoundaryEventTestSuite
{
   /* test suite */
   
   @ClassRule
   public static final LocalJcrH2TestSuiteSetup testSuiteSetup = new LocalJcrH2TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_ID);
}
