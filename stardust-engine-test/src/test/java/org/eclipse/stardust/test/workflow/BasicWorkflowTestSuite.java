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
package org.eclipse.stardust.test.workflow;

import org.eclipse.stardust.test.api.LocalJcrH2TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests that deal with the basic workflow
 * functionality exposed by <i>Stardust</i>'s <i>Workflow Service</i>.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
@RunWith(Suite.class)
@SuiteClasses({ 
               ActivityInstanceWorkflowTest.class,
               ProcessInstanceWorkflowTest.class
             })
public class BasicWorkflowTestSuite extends LocalJcrH2TestSuite
{
   /* test suite */
}
