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
import static org.eclipse.stardust.test.department.DepartmentModelConstants.MODEL_NAME;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.ORG1_ID;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
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
 * Tests the user query filter
 * {@link org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter}
 * wrt. the <i>Department</i> functionality.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ParticipantAssociationFilterTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private static final String ORG1_USERNAME_1 = "org1_1";
   private static final String ORG1_USERNAME_2 = "org1_2";
   
   private static final String DEP_ID_U = "u";
   private static final String DEP_ID_V = "v";
   
   private User org1User1;
   private User org1User2;
   
   private Organization org1;
   
   private Department uDep;
   private Department vDep;
   
   private ModelParticipantInfo org1uGrant;
   private ModelParticipantInfo org1vGrant;

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   @Before
   public void setUp()
   {
      initOrg1();
      createOrg1Users();
      createAllDepartments();
      createScopedGrants();
   }
   
   /**
    * <p>
    * Assuming there are no users with a particular grant,
    * filtering for that grant will result in an empty list.
    * </p>
    */
   @Test
   public void testForParticipantNoGrants()
   {
      final Users users = getUsersFor(org1uGrant);
      
      assertEquals(0, users.size());
   }
   
   /**
    * <p>
    * Assuming there is exactly on user with a particular grant,
    * filtering for that grant will return this and only this
    * user mentioned above.
    * </p>
    */
   @Test
   public void testForParticipantOrg1uGrantOneUser()
   {
      UserHome.addGrants(sf, org1User1, org1uGrant);
      
      final Users users = getUsersFor(org1uGrant);
      
      assertEquals(1, users.size());
      assertEquals(org1User1, users.get(0));
   }
   
   /**
    * <p>
    * Assuming there are exactly two users with a particular grant,
    * filtering for that grant will return these and only these
    * users mentioned above.
    * </p>
    */
   @Test
   public void testForParticipantOrg1uGrantTwoUsers()
   {
      UserHome.addGrants(sf, org1User1, org1uGrant);
      UserHome.addGrants(sf, org1User2, org1uGrant);
      
      final Users users = getUsersFor(org1uGrant);
      
      assertEquals(2, users.size());
      assertTrue(users.contains(org1User1));
      assertTrue(users.contains(org1User2));
   }   
   
   /**
    * <p>
    * Assuming there is exactly one user (user1) with two particular
    * grants on departments, filtering via an AND term for both of
    * these grants will return this and only this user mentioned above.
    * </p>
    */
   @Test
   public void testForParticipantOrg1uAndOrg1vGrant()
   {
      UserHome.addGrants(sf, org1User1, org1uGrant, org1vGrant);
      UserHome.addGrants(sf, org1User2, org1uGrant);
      
      final Users users = getUsersForAndTerm(org1uGrant, org1vGrant);
      
      assertEquals(1, users.size());
      assertEquals(org1User1, users.get(0));
   }
   
   /**
    * <p>
    * Assuming there are exactly two user (user1 and user2) with one
    * particular grant on a department per user, filtering via an OR
    * term for both of these grants will return these and only these
    * users mentioned above.
    * </p>
    */
   @Test
   public void testForParticipantOrg1uOrOrg1vGrant()
   {
      UserHome.addGrants(sf, org1User1, org1uGrant);
      UserHome.addGrants(sf, org1User2, org1vGrant);
      
      final Users users = getUsersForOrTerm(org1uGrant, org1vGrant);
      
      assertEquals(2, users.size());
      assertTrue(users.contains(org1User1));
      assertTrue(users.contains(org1User2));
   }

   private void initOrg1()
   {
      final Model model = sf.getQueryService().getActiveModel();
      org1 = model.getOrganization(ORG1_ID);
   }
   
   private void createOrg1Users()
   {
      org1User1 = UserHome.create(sf, ORG1_USERNAME_1, org1);
      org1User2 = UserHome.create(sf, ORG1_USERNAME_2, org1);
   }
   
   private void createAllDepartments()
   {
      uDep = sf.getAdministrationService().createDepartment(DEP_ID_U, DEP_ID_U, null, null, org1);
      vDep = sf.getAdministrationService().createDepartment(DEP_ID_V, DEP_ID_V, null, null, org1);
   }
   
   private void createScopedGrants()
   {
      org1uGrant = uDep.getScopedParticipant(org1);
      org1vGrant = vDep.getScopedParticipant(org1);
   }
   
   private Users getUsersFor(final ModelParticipantInfo p1)
   {
      final ParticipantAssociationFilter filter = ParticipantAssociationFilter.forParticipant(p1);
      final UserQuery userQuery = new UserQuery();
      userQuery.where(filter);
      return sf.getQueryService().getAllUsers(userQuery);
   }
   
   private Users getUsersForAndTerm(final ModelParticipantInfo p1, final ModelParticipantInfo p2)
   {
      final ParticipantAssociationFilter f1 = ParticipantAssociationFilter.forParticipant(p1);
      final ParticipantAssociationFilter f2 = ParticipantAssociationFilter.forParticipant(p2);
      final UserQuery userQuery = new UserQuery();
      userQuery.getFilter().addAndTerm().and(f1).and(f2);
      return sf.getQueryService().getAllUsers(userQuery);
   }
   
   private Users getUsersForOrTerm(final ModelParticipantInfo p1, final ModelParticipantInfo p2)
   {
      final ParticipantAssociationFilter f1 = ParticipantAssociationFilter.forParticipant(p1);
      final ParticipantAssociationFilter f2 = ParticipantAssociationFilter.forParticipant(p2);
      final UserQuery userQuery = new UserQuery();
      userQuery.getFilter().addOrTerm().or(f1).or(f2);
      return sf.getQueryService().getAllUsers(userQuery);
   }
}
