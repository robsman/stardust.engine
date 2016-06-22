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
package org.eclipse.stardust.engine;

import org.eclipse.stardust.engine.api.query.QueryTestSuite;
import org.eclipse.stardust.engine.api.runtime.EngineSetupTestSuite;
import org.eclipse.stardust.engine.core.compatibility.el.ElTestSuite;
import org.eclipse.stardust.engine.core.compatibility.spi.SpiTestSuite;
import org.eclipse.stardust.engine.core.model.ModelTestSuite;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceTestSuite;
import org.eclipse.stardust.engine.core.preferences.PreferencesTestSuite;
import org.eclipse.stardust.engine.core.struct.StructuredDataTestSuite;
import org.eclipse.stardust.engine.core.struct.sxml.SxmlTestSuite;
import org.eclipse.stardust.engine.core.upgrade.UpgradeTestSuite;
import org.eclipse.stardust.engine.extensions.mail.utils.MailValidationUtilsTest;
import org.eclipse.stardust.engine.extensions.xml.XmlUtilsTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( {  EngineSetupTestSuite.class,
                  XmlUtilsTestSuite.class,
                  SxmlTestSuite.class,
                  StructuredDataTestSuite.class,
                  QueryTestSuite.class,
                  MailValidationUtilsTest.class,
                  PreferencesTestSuite.class,
                  SpiTestSuite.class,
                  TransientProcessInstanceTestSuite.class,
                  UpgradeTestSuite.class,
                  ElTestSuite.class,
                  ModelTestSuite.class } )
public class SanityTestSuite
{
   /* test suite */
}
