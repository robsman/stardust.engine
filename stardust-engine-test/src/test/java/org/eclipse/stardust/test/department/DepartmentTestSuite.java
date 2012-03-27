package org.eclipse.stardust.test.department;

import org.eclipse.stardust.test.api.LocalJcrH2TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * TODO javadoc
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
@RunWith(Suite.class)
@SuiteClasses({ MethodExecutionAuthorizationTest.class })
public class DepartmentTestSuite extends LocalJcrH2TestSuite
{
   /* nothing to do */
}
