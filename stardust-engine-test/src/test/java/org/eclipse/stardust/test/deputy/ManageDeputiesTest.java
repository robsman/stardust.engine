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
package org.eclipse.stardust.test.deputy;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.deputy.DeputyModelConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeputyOptions;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.preferences.permissions.GlobalPermissionConstants;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

/**
 * <p>
 * This class tests manage <i>Deputy</i> permissions.
 * </p>
 *
 * @author barry.Grotjahn
 * @version $Revision: 74449 $
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManageDeputiesTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory userSf1 = new TestServiceFactory(new UsernamePasswordPair(USER1_ID, USER1_ID));
   private final TestServiceFactory userSf2 = new TestServiceFactory(new UsernamePasswordPair(USER2_ID, USER2_ID));
   private final TestServiceFactory userSf3 = new TestServiceFactory(new UsernamePasswordPair(USER3_ID, USER3_ID));
   private final TestServiceFactory userSf4 = new TestServiceFactory(new UsernamePasswordPair(USER4_ID, USER4_ID));

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf)
                                          .around(userSf1)
                                          .around(userSf2)
                                          .around(userSf3)
                                          .around(userSf4);

   private AdministrationService adminService;
   private QueryService adminQueryService;
   private UserService adminUserService;

   private DeputyOptions options;

   private Role role1;
   private Role role2;
   private Role role3;
   private Role role4;

   private User test1;
   private User test2;
   private User test3;
   private User test4;

   @Before
   public void setUp()
   {
      adminService = sf.getAdministrationService();
      adminQueryService = sf.getQueryService();
      adminUserService = sf.getUserService();

      role1 = (Role) adminQueryService.getParticipant(ROLE1_ID);
      role2 = (Role) adminQueryService.getParticipant(ROLE2_ID);
      role3 = (Role) adminQueryService.getParticipant(ROLE3_ID);
      role4 = (Role) adminQueryService.getParticipant(ROLE4_ID);

      Set<ModelParticipantInfo> grants = new HashSet<ModelParticipantInfo>();
      grants.add(role1);
      grants.add(role2);

      RuntimePermissionsDetails runtimePermissionsDetails = (RuntimePermissionsDetails) adminService.getGlobalPermissions();
      runtimePermissionsDetails.setGrants(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES, grants);
      adminService.setGlobalPermissions(runtimePermissionsDetails);

      test1 = UserHome.create(sf, USER1_ID, role1);
      test2 = UserHome.create(sf, USER2_ID, role2);
      test3 = UserHome.create(sf, USER3_ID, role3);
      test4 = UserHome.create(sf, USER4_ID, role4);

      options = new DeputyOptions();
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void addDeputyU1Test3ForTest2()
   {
      UserService userService = userSf1.getUserService();
      userService.addDeputy(test2, test3, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void addDeputyU1Test2ForTest3()
   {
      UserService userService = userSf1.getUserService();
      userService.addDeputy(test3, test2, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void modifyDeputyU1Test2ForTest3()
   {
      adminUserService.addDeputy(test3, test2, options);

      UserService userService = userSf1.getUserService();
      userService.modifyDeputy(test3, test2, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void removeDeputyU1Test2ForTest3()
   {
      adminUserService.addDeputy(test3, test2, options);

      UserService userService = userSf1.getUserService();
      userService.removeDeputy(test3, test2);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void addDeputyU3Test1ForTest3()
   {
      UserService userService = userSf3.getUserService();
      userService.addDeputy(test3, test1, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void modifyDeputyU3Test1ForTest3()
   {
      adminUserService.addDeputy(test3, test1, options);

      UserService userService = userSf3.getUserService();
      userService.modifyDeputy(test3, test1, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void removeDeputyU3Test1ForTest3()
   {
      adminUserService.addDeputy(test3, test1, options);

      UserService userService = userSf3.getUserService();
      userService.removeDeputy(test3, test1);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void modifyDeputyU2Test2ForTest3()
   {
      adminUserService.addDeputy(test3, test2, options);

      UserService userService = userSf2.getUserService();
      userService.modifyDeputy(test3, test2, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void addDeputyU4Test1ForTest3()
   {
      UserService userService = userSf4.getUserService();
      userService.addDeputy(test3, test1, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void modifyDeputyU4Test1ForTest3()
   {
      adminUserService.addDeputy(test3, test1, options);

      UserService userService = userSf4.getUserService();
      userService.modifyDeputy(test3, test1, options);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test(expected = AccessForbiddenException.class)
   public void removeDeputyU4Test1ForTest3()
   {
      adminUserService.addDeputy(test3, test1, options);

      UserService userService = userSf4.getUserService();
      userService.removeDeputy(test3, test1);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void addDeputyU4Test1ForTest4()
   {
      UserService userService = userSf4.getUserService();
      userService.addDeputy(test4, test1, options);
   }
}