/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sven.Rottstock (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.engine.core.model.beans.ModelParticipantBeanTest;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBeanTest;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBeanTest;

@RunWith(Suite.class)
@SuiteClasses( {
   ModelElementBeanTest.class,
   IdentifiableElementBeanTest.class,
   ModelParticipantBeanTest.class
} )
public class ModelTestSuite
{

}
