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
package org.eclipse.stardust.test.department;

import org.eclipse.stardust.test.api.LocalJcrH2TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * This test suite bundles tests for the <i>Department</i> functionality,
 * which allows for scoping organization (refer to the Stardust documentation 
 * for details about <i>Departments</i>).
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
@RunWith(Suite.class)
@SuiteClasses({
               AdminServiceCrudTest.class,
               HistoricalStatesPolicyTest.class,
               MethodExecutionAuthorizationTest.class, 
               ParticipantAssociationFilterTest.class,
               PerformingParticipantFilterTest.class
             })
public class DepartmentTestSuite extends LocalJcrH2TestSuite
{
   /* nothing to do */
}
