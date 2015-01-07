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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.DEPT_ID_DE;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.DEPT_ID_EN;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG_ID_1;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG_ID_2;

import java.util.List;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests adding and removing of grants functionality regarding <i>Departments</i>.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class UserGrantsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private static final String USER_ID = "User";
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory userSf = new TestServiceFactory(new UsernamePasswordPair(USER_ID, USER_ID));
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf)
                                          .around(userSf);
   
   private User user;
   
   @Before
   public void setUp()
   {
      UserHome.create(adminSf, USER_ID);
      user = userSf.getWorkflowService().getUser();
   }
   
   /**
    * <p>
    * Model participant info must not be null.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testAddGrantNullWithoutModelOID()
   {
      user.addGrant((ModelParticipantInfo) null);
      fail("Model participant info must not be null.");
   }
   
   /**
    * <p>
    * This method tests whether two grants really exist after adding them.
    * </p>
    */
   @Test
   public void testAddGrants()
   {
      final Organization[] orgs = getOrgs();
      final Department dep1 = DepartmentHome.create(adminSf, DEPT_ID_DE, ORG_ID_1, null);
      final Department dep2 = DepartmentHome.create(adminSf, DEPT_ID_EN, ORG_ID_2, null);
      final ModelParticipantInfo mpi1 = dep1.getScopedParticipant(orgs[0]);
      final ModelParticipantInfo mpi2 = dep2.getScopedParticipant(orgs[1]);
      
      UserHome.removeAllGrants(adminSf, user);
      UserHome.addGrants(adminSf, user, mpi1, mpi2);
      
      final User retrievedUser = userSf.getWorkflowService().getUser();
      ensureGrantsExist(dep1, dep2, retrievedUser);
   }

   /**
    * <p>
    * This method tests whether one grant really does not exist any longer
    * after removing it.
    * </p>
    */
   @Test
   public void testRemoveGrants()
   {
      final Organization[] orgs = getOrgs();
      final Department dep1 = DepartmentHome.create(adminSf, DEPT_ID_DE, ORG_ID_1, null);
      final Department dep2 = DepartmentHome.create(adminSf, DEPT_ID_EN, ORG_ID_2, null);
      final ModelParticipantInfo mpi1 = dep1.getScopedParticipant(orgs[0]);
      final ModelParticipantInfo mpi2 = dep2.getScopedParticipant(orgs[1]);
      
      UserHome.removeAllGrants(adminSf, user);
      UserHome.addGrants(adminSf, user, mpi1, mpi2);
      
      user = userSf.getWorkflowService().getUser();
      ensureGrantsExist(dep1, dep2, user);
      
      UserHome.removeGrants(adminSf, user, mpi1);

      user = userSf.getWorkflowService().getUser();
      ensureGrantIsRemoved(dep1, dep2, user);
   }
   
   private Organization[] getOrgs()
   {
      final Organization org1 = (Organization) adminSf.getQueryService().getParticipant(ORG_ID_1);
      final Organization org2 = (Organization) adminSf.getQueryService().getParticipant(ORG_ID_2);
      return new Organization[] { org1, org2 };
   }
   
   private void ensureGrantsExist(final Department dep1, final Department dep2, final User user)
   {
      boolean mpi1Found = false;
      boolean mpi2Found = false;
      final List<Grant> grants = user.getAllGrants();
      assertEquals(2, grants.size());
      
      for (final Grant grant : grants)
      {
         if (dep1.equals(grant.getDepartment()))
         {
            mpi1Found = true;
         }
         else if (dep2.equals(grant.getDepartment()))
         {
            mpi2Found = true;
         }
      }
      
      assertTrue(mpi1Found && mpi2Found);
   }
   
   private void ensureGrantIsRemoved(final Department dep1, final Department dep2, final User user)
   {
      boolean mpi2Found = false;
      final List<Grant> grants = user.getAllGrants();
      assertEquals(1, grants.size());
      
      for (final Grant grant : grants)
      {
         if (dep1.equals(grant.getDepartment()))
         {
            fail("Grant should have been removed.");
         }
         else if (dep2.equals(grant.getDepartment()))
         {
            mpi2Found = true;
         }
      }
      
      assertTrue("Grant should not have been removed.", mpi2Found);
   }
}
