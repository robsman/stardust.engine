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

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.DEFAULT_PARTITION_ID;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.*;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.PartitionAwareExtensionsManager.FlushPartitionPredicate;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationProvider;
import org.eclipse.stardust.engine.core.spi.security.ExternalUserConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class tests QA probability settings.
 * </p>
 *
 * @author barry.Grotjahn
 * @version $Revision: 74449 $
 */
public class ChangeQAProbabilityPercentageTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   private static final String REGULAR_USER_ID = "test";
   private static final String MODEL_NAME = "BasicWorkflowModel";
      
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
      
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   /**
    * <p>
    * Tests change QA probability settings, if internal authentication is false.
    * The user to be changed must have Administration Role or must be Team Leader. 
    * </p>
    */   
   @Test
   public void changeQualityAssuranceProbability()
   {      
      Role adminRole = (Role) sf.getQueryService().getParticipant(PredefinedConstants.ADMINISTRATOR_ROLE);
      User user = UserHome.create(sf, REGULAR_USER_ID, adminRole);
      
      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_PRINCIPAL);
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, DummySyncProvider.class.getName());
            
      user.setQualityAssuranceProbability(88);      
      User modifyUser = sf.getUserService().modifyUser(user);

      setAuthModeToInternal();
      
      Integer qualityAssuranceProbability = modifyUser.getQualityAssuranceProbability();
      assertThat(qualityAssuranceProbability, is(88));
   }
   
   public static final class DummySyncProvider extends DynamicParticipantSynchronizationProvider
   {
      @Override
      public ExternalUserConfiguration provideUserConfiguration(String account)
      {
         return new MotuUserConfiguration();
      }
   }

   private void setAuthModeToInternal()
   {
      final FlushPartitionPredicate flushPartition = new FlushPartitionPredicate(DEFAULT_PARTITION_ID);
      ExtensionProviderUtils.forEachExtensionsManager(flushPartition);

      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, "None");
   }
      
   private static final class MotuUserConfiguration extends ExternalUserConfiguration
   {
      @Override
      public String getDescription()
      {
         return MOTU;
      }

      @Override
      public String getEMail()
      {
         return MOTU;
      }

      @Override
      public String getFirstName()
      {
         return MOTU;
      }

      @Override
      public String getLastName()
      {
         return MOTU;
      }

      @Override
      public Map<?, ?> getProperties()
      {
         return Collections.emptyMap();
      }
   }
}