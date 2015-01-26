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
package org.eclipse.stardust.test.transientpi;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.ALTERNATIVE_IMPL_MODEL_ID;

import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests for the <i>Transient Process Instance</i> functionality,
 * which allows for executing process instances without persisting them (refer to the Stardust
 * documentation for details about <i>Transient Process Instances</i>).
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
@RunWith(Suite.class)
@SuiteClasses({
                TransientProcessInstanceTest.class,
                TransientProcessInstanceWriteBehindTest.class,
                TransientProcessInstanceMonitoringTest.class
              })
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/audit-trail-queue.app-ctx.xml")
public class TransientProcessInstanceTestSuite
{
   /* test suite */

   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.JMS, MODEL_ID, ALTERNATIVE_IMPL_MODEL_ID);
}
