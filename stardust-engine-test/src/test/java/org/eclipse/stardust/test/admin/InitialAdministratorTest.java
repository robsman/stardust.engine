/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.admin;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PREDEFINED_MODEL_ID;
import static org.eclipse.stardust.engine.api.query.DeployedModelQuery.findForId;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.examples.MyConstants;
import org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class focuses on testing around the initial Administrator user.
 * </p>
 *
 * @author Roland.Stamm
 */
public class InitialAdministratorTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);


   @Test
   public void testCreateMotuCaseInsensitive()
   {
      boolean failed = false;
      String account = "motu";
      try
      {
         sf.getUserService().createUser(account, account, account, null, account, null, null,
               null);
         failed = true;
      }
      catch (UserExistsException e)
      {
      }

      account = "Motu";
      try
      {
         sf.getUserService().createUser(account, account, account, null, account, null, null,
               null);
         failed = true;
      }
      catch (UserExistsException e)
      {
      }

      account = "MOTU";
      try
      {
         sf.getUserService().createUser(account, account, account, null, account, null, null,
               null);
         failed = true;
      }
      catch (UserExistsException e)
      {
      }

      account = "MotU";
      try
      {
         sf.getUserService().createUser(account, account, account, null, account, null, null,
               null);
         failed = true;
      }
      catch (UserExistsException e)
      {
      }
      Assert.assertFalse(failed);
   }

   @Test
   public void testCleanupRuntimeKeepsMotu()
   {
      sf.getAdministrationService().cleanupRuntime(false, false);
      User user = sf.getUserService().getUser(MOTU);
      Assert.assertNotNull(user);
   }

   @Test
   public void testCleanupRuntimeAndModelsKeepsMotu()
   {
      sf.getAdministrationService().cleanupRuntimeAndModels();
      User user = sf.getUserService().getUser(MOTU);
      Assert.assertNotNull(user);
   }
}
