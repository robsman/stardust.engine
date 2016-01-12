/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.admin;

import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.dto.PasswordRulesDetails;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.TestCredentialDeliveryStrategy;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

/**
 * <p>
 * This class tests password rules.
 * </p>
 *
 * @author barry.Grotjahn
 * @version $Revision: 74449 $
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChangePasswordTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   private static final String USER1_ID = "test1";
   private static final String USER2_ID = "test2";
      
   private static final String MODEL_NAME = "BasicWorkflowModel";
      
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
      
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private AdministrationService adminService;
   private UserService adminUserService;

   private User test1;
   private User test2;
   
   private PasswordRulesDetails passwordRules;

   @Before
   public void setUp()
   {
      
      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, TestCredentialDeliveryStrategy.class.getName());
            
      adminService = sf.getAdministrationService();
      adminUserService = sf.getUserService();

      test1 = UserHome.create(sf, USER1_ID);
      test2 = UserHome.create(sf, USER2_ID);      
   }


   private void setPasswordRules(boolean reset)
   {
      passwordRules = new PasswordRulesDetails();
      if(!reset)
      {
         passwordRules.setPasswordTracking(10);
         passwordRules.setUniquePassword(true);
      }
      adminService.setPasswordRules(passwordRules);
   }
   
   @Test(expected=InvalidPasswordException.class)   
   public void changePasswordUseOldPassword()
   {
      setPasswordRules(false);
      
      adminUserService.generatePasswordResetToken(USER1_ID, USER1_ID);
      Map<String, String> properties = CollectionUtils.newHashMap();
      
      String token = TestCredentialDeliveryStrategy.getInstance().getToken();
      adminUserService.resetPassword(USER1_ID, properties, token);
      
      test1.setPassword(USER1_ID);      
      adminUserService.modifyUser(test1);
   }

   @Test(expected=InvalidPasswordException.class)      
   public void changePasswordUseGeneratedPassword()
   {
      setPasswordRules(false);
      
      adminUserService.generatePasswordResetToken(USER2_ID, USER2_ID);
      Map<String, String> properties = CollectionUtils.newHashMap();
      
      String token = TestCredentialDeliveryStrategy.getInstance().getToken();
      adminUserService.resetPassword(USER2_ID, properties, token);
      String password = TestCredentialDeliveryStrategy.getInstance().getPassword();
      
      test2.setPassword(password);      
      adminUserService.modifyUser(test2);
   }   
}