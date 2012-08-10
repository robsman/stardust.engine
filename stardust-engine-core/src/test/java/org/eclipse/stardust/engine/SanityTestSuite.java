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

import org.eclipse.stardust.common.BaseTestSuite;
import org.eclipse.stardust.common.config.ConfigTestSuite;
import org.eclipse.stardust.common.config.extensions.ExtensionsTestSuite;
import org.eclipse.stardust.common.reflect.ReflectTestSuite;
import org.eclipse.stardust.common.utils.xml.XmlUtilsTestSuite;
import org.eclipse.stardust.engine.api.query.QueryTestSuite;
import org.eclipse.stardust.engine.api.runtime.EngineSetupTestSuite;
import org.eclipse.stardust.engine.core.compatibility.el.ElTestSuite;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceTestSuite;
import org.eclipse.stardust.engine.core.preferences.PreferencesTestSuite;
import org.eclipse.stardust.engine.core.struct.StructuredDataTestSuite;
import org.eclipse.stardust.engine.core.struct.sxml.SxmlTestSuite;
import org.eclipse.stardust.engine.core.upgrade.UpgradeTestSuite;
import org.eclipse.stardust.engine.extensions.mail.utils.MailValidationUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( {  BaseTestSuite.class,
                  ConfigTestSuite.class,
                  EngineSetupTestSuite.class,
                  ExtensionsTestSuite.class,
                  XmlUtilsTestSuite.class,
                  SxmlTestSuite.class,
                  StructuredDataTestSuite.class,
                  QueryTestSuite.class,
                  MailValidationUtilsTest.class,
                  PreferencesTestSuite.class,
                  ReflectTestSuite.class,
                  TransientProcessInstanceTestSuite.class,
                  UpgradeTestSuite.class,
                  ElTestSuite.class} )
public class SanityTestSuite
{
   /* test suite */
}
