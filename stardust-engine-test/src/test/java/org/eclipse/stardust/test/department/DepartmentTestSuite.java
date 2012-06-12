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

import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSuiteSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
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
               ActivateDepartmentActivityTest.class,
               AdminServiceCrudTest.class,
               DeclarativeSecurityDelegationTest.class,
               DepartmentCleanupTest.class,
               DepartmentQueryTest.class,
               HistoricalStatesPolicyTest.class,
               MethodExecutionAuthorizationTest.class,
               NullScopeFallbackTest.class,
               ParticipantAssociationFilterTest.class,
               PerformingParticipantFilterTest.class,
               ScopedWorklistTest.class,
               SubDepartmentDelegationTest.class,
               UserGrantsTest.class,
               WorkitemsAssignmentCreationTest.class
             })
public class DepartmentTestSuite
{
   /* test suite */
   
   @ClassRule
   public static final LocalJcrH2TestSuiteSetup testSuiteSetup = new LocalJcrH2TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
}
