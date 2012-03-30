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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.DEPT_ID_DE;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.DEPT_ID_EN;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG_ID_1;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG_ID_2;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.List;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.DepartmentHome;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.eclipse.stardust.test.api.UserHome;
import org.junit.Before;
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
public class UserGrantsTest extends LocalJcrH2Test
{
   private static final String USER_ID = "User";
   
   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(adminSf, MODEL_NAME);
   private final ClientServiceFactory userSf = new ClientServiceFactory(USER_ID, USER_ID);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(userSf);
   
   private User user;
   
   @Before
   public void setUp()
   {
      UserHome.create(adminSf, USER_ID, new String[0]);
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
      final Department dep1 = DepartmentHome.create(DEPT_ID_DE, ORG_ID_1, null, adminSf);
      final Department dep2 = DepartmentHome.create(DEPT_ID_EN, ORG_ID_2, null, adminSf);
      final ModelParticipantInfo mpi1 = dep1.getScopedParticipant(orgs[0]);
      final ModelParticipantInfo mpi2 = dep2.getScopedParticipant(orgs[1]);
      
      user.removeAllGrants();
      user.addGrant(mpi1);
      user.addGrant(mpi2);
      
      adminSf.getUserService().modifyUser(user);
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
      final Department dep1 = DepartmentHome.create(DEPT_ID_DE, ORG_ID_1, null, adminSf);
      final Department dep2 = DepartmentHome.create(DEPT_ID_EN, ORG_ID_2, null, adminSf);
      final ModelParticipantInfo mpi1 = dep1.getScopedParticipant(orgs[0]);
      final ModelParticipantInfo mpi2 = dep2.getScopedParticipant(orgs[1]);
      
      user.removeAllGrants();
      user.addGrant(mpi1);
      user.addGrant(mpi2);
      
      adminSf.getUserService().modifyUser(user);
      user = userSf.getWorkflowService().getUser();
      ensureGrantsExist(dep1, dep2, user);
      
      user.removeGrant(mpi1);
      
      adminSf.getUserService().modifyUser(user);
      user = userSf.getWorkflowService().getUser();
      ensureGrantIsRemoved(dep1, dep2, user);
   }
   
   private Organization[] getOrgs()
   {
      final Model model = adminSf.getQueryService().getActiveModel();
      final Organization org1 = model.getOrganization(ORG_ID_1);
      final Organization org2 = model.getOrganization(ORG_ID_2);
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
