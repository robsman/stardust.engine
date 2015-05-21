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
package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * /**
 * <p>
 * Tests if new structured validation via model validation at deployment time works.
 * </p>
 *
 * @author Barry.Grotjahn
 *
 */
public class DeployValidationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Test
   public void testDeploymentValidation()
   {
      boolean errorOccured = false;
      try
      {
         deploy("Structured");
      }
      catch (DeploymentException e)
      {
         System.err.println(e);
         assertDeploymentError(e);
         errorOccured = true;
      }
      Assert.assertTrue(errorOccured);
   }

   private void deploy(String modelId)
   {
      RtEnvHome.deployModel(sf.getAdministrationService(), DeploymentOptions.DEFAULT,
            new String[] {modelId});
   }

   private void assertDeploymentError(DeploymentException e)
   {
      int DATA01027 = 0;
      int DATA01017 = 0;
      int DATA01028 = 0;
      
      List<DeploymentInfo> infos = e.getInfos();
      for (DeploymentInfo deploymentInfo : infos)
      {
         List<Inconsistency> errors = deploymentInfo.getErrors();
         for (Inconsistency inconsistency : errors)
         {
            String errorID = inconsistency.getErrorID();
            if(!StringUtils.isEmpty(errorID))
            {
               if(errorID.equals("DATA01027"))
               {
                  DATA01027++;
               }
               else if(errorID.equals("DATA01017"))
               {
                  DATA01017++;
               }
               else if(errorID.equals("DATA01028"))
               {
                  DATA01028++;
               }
            }
         }
      }

      if(DATA01027 != 2)
      {
         Assert.fail("2 ErrorCase 'DATA01027' expected but not found.");         
      }
      if(DATA01017 != 1)
      {
         Assert.fail("ErrorCase 'DATA01017' expected but not found.");                  
      }
      if(DATA01028 != 1)
      {
         Assert.fail("ErrorCase 'DATA01028' expected but not found.");                  
      }
   }
}