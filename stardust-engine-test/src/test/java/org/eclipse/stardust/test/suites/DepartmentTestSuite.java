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
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;

import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.department.ActivateDepartmentActivityTest;
import org.eclipse.stardust.test.department.AdminServiceCrudTest;
import org.eclipse.stardust.test.department.DeclarativeSecurityDelegationTest;
import org.eclipse.stardust.test.department.DepartmentCleanupTest;
import org.eclipse.stardust.test.department.DepartmentQueryTest;
import org.eclipse.stardust.test.department.HistoricalStatesPolicyTest;
import org.eclipse.stardust.test.department.MethodExecutionAuthorizationTest;
import org.eclipse.stardust.test.department.NullScopeFallbackTest;
import org.eclipse.stardust.test.department.ParticipantAssociationFilterTest;
import org.eclipse.stardust.test.department.PerformingParticipantFilterTest;
import org.eclipse.stardust.test.department.ScopedWorklistTest;
import org.eclipse.stardust.test.department.SubDepartmentDelegationTest;
import org.eclipse.stardust.test.department.UserGrantsTest;
import org.eclipse.stardust.test.department.WorkitemsAssignmentCreationTest;
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
   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
}
