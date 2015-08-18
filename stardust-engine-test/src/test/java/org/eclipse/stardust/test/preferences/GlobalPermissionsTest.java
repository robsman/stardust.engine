/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.preferences;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.Collections;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.RuntimePermissions;
import org.eclipse.stardust.engine.core.preferences.permissions.GlobalPermissionConstants;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class tests the default preferences SPI for global permissions.
 * </p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GlobalPermissionsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);


   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private AdministrationService adminService;

   @Before
   public void setUp()
   {
      adminService = sf.getAdministrationService();
   }

   @Test
   public void test01ValueChangedViaSPI()
   {
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();

      Assert.assertFalse(globalPermissions.isDefaultGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES));
      Assert.assertTrue(globalPermissions.hasAllGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES));
   }

   @Test
   public void test02StillModifiableToEngineDefault()
   {
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();

      globalPermissions.setGrants(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES, Collections.singleton(ModelParticipantInfo.ADMINISTRATOR));

      adminService.setGlobalPermissions(globalPermissions);

      globalPermissions = adminService.getGlobalPermissions();

      Assert.assertTrue(globalPermissions.isDefaultGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES));
      Assert.assertFalse(globalPermissions.hasAllGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES));
      Assert.assertEquals(Collections.singleton(ModelParticipantInfo.ADMINISTRATOR), globalPermissions.getGrants(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES));
   }

   @Test
   public void test03ResetsToSpiValue()
   {
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();

      globalPermissions.setGrants(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES, Collections.<ModelParticipantInfo> emptySet());

      adminService.setGlobalPermissions(globalPermissions);

      globalPermissions = adminService.getGlobalPermissions();

      Assert.assertFalse(globalPermissions.isDefaultGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES));
      Assert.assertTrue(globalPermissions.hasAllGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES));
   }


   @Test
   public void test11DefaultValueUnchangedViaSPI()
   {
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();

      Assert.assertTrue(globalPermissions.isDefaultGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS));
      Assert.assertFalse(globalPermissions.hasAllGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS));
   }

   @Test
   public void test12ModifiableToNonDefault()
   {
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();

      globalPermissions.setAllGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS);

      adminService.setGlobalPermissions(globalPermissions);

      globalPermissions = adminService.getGlobalPermissions();

      Assert.assertFalse(globalPermissions.isDefaultGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS));
      Assert.assertTrue(globalPermissions.hasAllGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS));
   }

   @Test
   public void test13ResetsToDefaultValue()
   {
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();

      globalPermissions.setGrants(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS, Collections.<ModelParticipantInfo> emptySet());

      adminService.setGlobalPermissions(globalPermissions);

      globalPermissions = adminService.getGlobalPermissions();

      Assert.assertTrue(globalPermissions.isDefaultGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS));
      Assert.assertFalse(globalPermissions.hasAllGrant(GlobalPermissionConstants.GLOBAL_MANAGE_DAEMONS));
   }


}