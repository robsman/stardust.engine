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
package org.eclipse.stardust.test.suites;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MODEL_NAME;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.artifact.ArtifactQueryTest;
import org.eclipse.stardust.test.artifact.ArtifactSanityTest;
import org.eclipse.stardust.test.artifact.CleanupRuntimeTest;

/**
 * <p>
 * This test suite bundles artifact related tests.
 * </p>
 *
 * @author Barry.Grotjahn
 */
@RunWith(Suite.class)
@SuiteClasses({
   CleanupRuntimeTest.class,      
   ArtifactQueryTest.class,
   ArtifactSanityTest.class
             })
public class ArtifactTestSuite
{
}
