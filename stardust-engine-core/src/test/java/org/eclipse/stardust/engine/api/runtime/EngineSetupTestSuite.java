/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.engine.core.runtime.audittrail.management.DeleteScriptGeneratorTest;
import org.eclipse.stardust.engine.core.runtime.beans.MultiplePartitionsSynchronizationServiceTest;
import org.eclipse.stardust.engine.core.runtime.beans.PartitionAwareExtensionsManagerTest;
import org.eclipse.stardust.engine.core.runtime.beans.PiRtClassesArePiOrAiAwareTest;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationServiceTest;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.LoginServiceFactoryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses( {PartitionAwareExtensionsManagerTest.class,
                LoginServiceFactoryTest.class,
                SynchronizationServiceTest.class,
                MultiplePartitionsSynchronizationServiceTest.class,
                PiRtClassesArePiOrAiAwareTest.class,
                DeleteScriptGeneratorTest.class} )
public class EngineSetupTestSuite
{
   /* test suite */
}
