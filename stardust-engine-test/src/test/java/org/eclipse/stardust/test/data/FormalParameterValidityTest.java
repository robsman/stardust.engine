/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

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
 * Tests if FormalParameters use valid data, especially when external process
 * invocation via SOAP and REST is enabled. This tests validation at deployment time.
 * </p>
 *
 * @author Roland.Stamm
 *
 */
public class FormalParameterValidityTest
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
   public void testFormalParameterValid()
   {
      deploy("ProviderModel");
      deploy("ConsumerModel");
   }

   @Test
   public void testExternalInvocationFormalParameterInvalid()
   {
      boolean errorOccured = false;
      try
      {
         deploy("FormalParameterInvalid");
      }
      catch (DeploymentException e)
      {
         // No data assigned error
         assertDeploymentError("PD01004", "InNoData", e);
         // Incompatible type assigned warning
         assertDeploymentWarning("PD01010", "InDocument", e);

         errorOccured = true;
      }
      Assert.assertTrue(errorOccured);
   }

   private void deploy(String modelId)
   {
      RtEnvHome.deploy(sf.getAdministrationService(), DeploymentOptions.DEFAULT,
            new String[] {modelId});
   }

   private void assertDeploymentError(String errorId, String messageContains,
         DeploymentException e)
   {
      boolean found = false;
      List<DeploymentInfo> infos = e.getInfos();

      for (DeploymentInfo deploymentInfo : infos)
      {
         List<Inconsistency> errors = deploymentInfo.getErrors();
         for (Inconsistency inconsistency : errors)
         {
            found |= errorId.equals(inconsistency.getErrorID())
                  && inconsistency.getMessage().contains(messageContains);
         }
      }
      if (!found)
      {
         Assert.fail("ErrorCase '" + errorId + "' with contained String '"
               + messageContains + "' expected but not found. Instead: " + e.getMessage());
      }
   }

   private void assertDeploymentWarning(String errorId, String messageContains,
         DeploymentException e)
   {
      boolean found = false;
      List<DeploymentInfo> infos = e.getInfos();

      for (DeploymentInfo deploymentInfo : infos)
      {
         List<Inconsistency> warnings = deploymentInfo.getWarnings();
         for (Inconsistency inconsistency : warnings)
         {
            found |= errorId.equals(inconsistency.getErrorID())
                  && inconsistency.getMessage().contains(messageContains);
         }
      }
      if (!found)
      {
         Assert.fail("ErrorCase '" + errorId + "' with contained String '"
               + messageContains + "' expected but not found. Instead: " + e.getMessage());
      }
   }

}

