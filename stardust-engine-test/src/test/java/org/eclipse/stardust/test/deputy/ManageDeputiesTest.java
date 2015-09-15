/**********************************************************************************
 * Copyright (c) 2012, 2015 SunGard CSA LLC and others.
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

import java.sql.Date;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.preferences.permissions.GlobalPermissionConstants;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.*;
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

   /**
    * <p>
    *
    * </p>
    */
   @Test(expected=InvalidArgumentException.class)
   public void addDeputyAdminTest2ForTest3ToInPast()
   {
      DeputyOptions pastOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.TO, DEPUTY_VALIDITY_DATE.PAST);

      @SuppressWarnings("unused")
      Deputy addedDeputy = adminUserService.addDeputy(test3, test2, pastOptions);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void addDeputyAdminTest2ForTest3FromInPast()
   {
      DeputyOptions pastOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.FROM, DEPUTY_VALIDITY_DATE.PAST);

      Deputy addedDeputy = adminUserService.addDeputy(test3, test2, pastOptions);

      Assert.assertTrue(addedDeputy.getFromDate().compareTo(pastOptions.getFromDate()) > 0);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void addDeputyAdminTest2ForTest3ToInFuture()
   {
      DeputyOptions futureOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.TO, DEPUTY_VALIDITY_DATE.FUTURE);

      Deputy addedDeputy = adminUserService.addDeputy(test3, test2, futureOptions);

      Assert.assertTrue(addedDeputy.getUntilDate().compareTo(futureOptions.getToDate()) == 0);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void addDeputyAdminTest2ForTest3FromInFuture()
   {
      DeputyOptions futureOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.FROM, DEPUTY_VALIDITY_DATE.FUTURE);

      Deputy addedDeputy = adminUserService.addDeputy(test3, test2, futureOptions);

      Assert.assertTrue(addedDeputy.getFromDate().compareTo(futureOptions.getFromDate()) == 0);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test(expected=InvalidArgumentException.class)
   public void modifyDeputyAdminTest2ForTest3ToInPast()
   {
      DeputyOptions pastOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.TO, DEPUTY_VALIDITY_DATE.PAST);

      adminUserService.addDeputy(test3, test2, options);
      @SuppressWarnings("unused")
      Deputy modifiedDeputy = adminUserService.modifyDeputy(test3, test2, pastOptions);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void modifyDeputyAdminTest2ForTest3FromInPast()
   {
      DeputyOptions pastOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.FROM, DEPUTY_VALIDITY_DATE.PAST);

      adminUserService.addDeputy(test3, test2, options);
      Deputy modifiedDeputy = adminUserService.modifyDeputy(test3, test2, pastOptions);

      Assert.assertTrue(modifiedDeputy.getFromDate().compareTo(pastOptions.getFromDate()) > 0);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void modifyDeputyAdminTest2ForTest3ToInFuture()
   {
      DeputyOptions futureOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.TO, DEPUTY_VALIDITY_DATE.FUTURE);

      adminUserService.addDeputy(test3, test2, options);
      Deputy modifiedDeputy = adminUserService.modifyDeputy(test3, test2, futureOptions);

      Assert.assertTrue(modifiedDeputy.getUntilDate().compareTo(futureOptions.getToDate()) == 0);
   }

   /**
    * <p>
    *
    * </p>
    */
   @Test
   public void modifyDeputyAdminTest2ForTest3FromInFuture()
   {
      DeputyOptions futureOptions = createDeputyOptions(DEPUTY_VALIDITY_TARGET.FROM, DEPUTY_VALIDITY_DATE.FUTURE);

      adminUserService.addDeputy(test3, test2, options);
      Deputy modifiedDeputy = adminUserService.modifyDeputy(test3, test2, futureOptions);

      Assert.assertTrue(modifiedDeputy.getFromDate().compareTo(futureOptions.getFromDate()) == 0);
   }

   @Test
   public void createInvalidDeputyOptions()
   {
      // fromDate must not be null
      try
      {
         new DeputyOptions(null, TimestampProviderUtils.getTimeStamp());
         Assert.fail("fromDate must not be null!");
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         DeputyOptions depOpt = new DeputyOptions(TimestampProviderUtils.getTimeStamp(), TimestampProviderUtils.getTimeStamp());
         depOpt.setFromDate(null);

         Assert.fail("fromDate must not be null!");
      }
      catch (IllegalArgumentException e)
      {
      }

      // participants must not be null
      try
      {
         new DeputyOptions(TimestampProviderUtils.getTimeStamp(), TimestampProviderUtils.getTimeStamp(), null);

         Assert.fail("participants must not be null!");
      }
      catch (IllegalArgumentException e)
      {
      }

      // participants must not be null
      try
      {
         DeputyOptions depOpt = new DeputyOptions(TimestampProviderUtils.getTimeStamp(), TimestampProviderUtils.getTimeStamp(), Collections.<ModelParticipantInfo> emptySet());
         depOpt.setParticipants(null);

         Assert.fail("participants must not be null!");
      }
      catch (IllegalArgumentException e)
      {
      }
}

   private static DeputyOptions createDeputyOptions(DEPUTY_VALIDITY_TARGET target, DEPUTY_VALIDITY_DATE dateSelector)
   {
      long nowInMs = TimestampProviderUtils.getTimeStampValue();
      Date date;
      switch (dateSelector)
      {
         case PAST:
            date = new Date(nowInMs - 1000 * 60 * 60 * 24);
            break;
         case FUTURE:
            date = new Date(nowInMs + 1000 * 60 * 60 * 24);
            break;

         default:
            date = new Date(nowInMs);
            break;
      }

      DeputyOptions options = null;
      switch (target)
      {
         case FROM:
            options = new DeputyOptions(date, null);
            break;

         case TO:
            options = new DeputyOptions(new Date(nowInMs), date);
            break;

         default:
            Assert.fail("Value not supported: " + target);
            break;
      }

      return options;
   }

   private static enum DEPUTY_VALIDITY_DATE
   {
      PAST, NOW, FUTURE
   }

   private static enum DEPUTY_VALIDITY_TARGET
   {
      FROM, TO
   }
}