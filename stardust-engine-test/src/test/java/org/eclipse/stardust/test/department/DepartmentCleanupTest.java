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

import static org.junit.Assert.assertEquals;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.DEPT_ID_DE;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG_ID_1;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.OrganizationInfoDetails;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests cleanup functionality regarding <i>Departments</i>.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DepartmentCleanupTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   /**
    * <p>
    * A cleanup of the runtime with <code>keepUsers</code> set to <code>true</code> 
    * should leave existing departments untouched.
    * </p>
    */
   @Test
   public void testCleanupRuntimeKeepUsers()
   {
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      sf.getAdministrationService().cleanupRuntime(true);
      ensureDepartmentExists();
   }
   
   /**
    * <p>
    * A cleanup of the runtime with <code>keepUsers</code> set to <code>false</code> 
    * should remove existing departments.
    * </p>
    */
   @Test
   public void testCleanupRuntimeDoNotKeepUsers() throws InterruptedException
   {
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      sf.getAdministrationService().cleanupRuntime(false);
      ensureDepartmentCleanup();
   }
   
   /**
    * <p>
    * A cleanup of the runtime including model elements 
    * should remove existing departments.
    * </p>
    */
   @Test
   public void testCleanupRuntimeAndModels()
   {
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      sf.getAdministrationService().cleanupRuntimeAndModels();
      RtEnvHome.deploy(sf.getAdministrationService(), null, MODEL_NAME);
      ensureDepartmentCleanup();
   }
   
   private void ensureDepartmentExists()
   {
      final Department dept = sf.getQueryService().findDepartment(null, DEPT_ID_DE, new OrganizationInfoDetails(ORG_ID_1));
      assertEquals(DEPT_ID_DE, dept.getId());
   }
   
   private void ensureDepartmentCleanup()
   {
      try
      {
         sf.getQueryService().findDepartment(null, DEPT_ID_DE, new OrganizationInfoDetails(ORG_ID_1));
         Assert.fail("Department must not exist.");
      } 
      catch (ObjectNotFoundException e)
      {
         /* expected */
      }
   }
}
