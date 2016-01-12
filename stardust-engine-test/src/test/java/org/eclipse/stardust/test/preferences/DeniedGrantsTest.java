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

package org.eclipse.stardust.test.preferences;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.HashSet;
import java.util.Set;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.RuntimePermissions;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.dms.DmsModelConstants;

/**
 * <p>
 * This class tests participant validation in PermissionUtils (fixed wrong validation).
 * </p>
 *
 * @author Barry.Grotjahn
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeniedGrantsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   private final TestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BasicWorkflowModel", DmsModelConstants.DMS_MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   private AdministrationService adminService;

   @Before
   public void setUp()
   {
      adminService = sf.getAdministrationService();
   }

   @Test
   public void testSetDeniedGrants()
   {      
      Role role = (Role) sf.getQueryService().getParticipant("{DmsModel}MyRole");      
      Set<ModelParticipantInfo> grants = new HashSet<ModelParticipantInfo>();
      grants.add(role);
      
      RuntimePermissions globalPermissions = adminService.getGlobalPermissions();
      globalPermissions.setDeniedGrants("spawnPeerProcessInstance", grants);
      adminService.setGlobalPermissions(globalPermissions);

      globalPermissions = adminService.getGlobalPermissions();
      Set<ModelParticipantInfo> deniedGrants = globalPermissions.getDeniedGrants("spawnPeerProcessInstance");

      Assert.assertEquals(deniedGrants.size(), 1);            
      for (ModelParticipantInfo modelParticipantInfo : deniedGrants)
      {
         Assert.assertEquals(modelParticipantInfo.getQualifiedId(), ("{DmsModel}MyRole"));      
      }      
   }
}