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
package org.eclipse.stardust.test.security;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.DEFAULT_PARTITION_ID;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MODEL_NAME;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.*;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.PartitionAwareExtensionsManager.FlushPartitionPredicate;
import org.eclipse.stardust.engine.core.runtime.beans.UserGroupBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationProvider;
import org.eclipse.stardust.engine.core.spi.security.ExternalUserConfiguration;
import org.eclipse.stardust.engine.core.spi.security.ExternalUserGroupConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
/**
 * @author stephan.born
 * @version $Revision$
 */
public class SynchronithationProviderTest extends AbstractSpringAuthenticationTest
{
   private static final String RETURN_GROUP_CONFIG = "RETURN_GROUP_CONFIG";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   @Test
   public void testSynchUserWithRemovedUserGroup()
   {
      UserHome.create(sf, REGULAR_USER_ID);
      UserHome.create(sf, REGULAR_USER2_ID);

      WorkflowService wfs_u1 = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getWorkflowService();
      WorkflowService wfs_u2 = ServiceFactoryLocator.get(REGULAR_USER2_ID, REGULAR_USER2_ID).getWorkflowService();

      setSynchProvider(RegulareUserWithConfigurableGroupsSyncProvider.class);

      // call will synch with user group link creation
      final GlobalParameters params = GlobalParameters.globals();
      params.set(RETURN_GROUP_CONFIG, Boolean.TRUE.toString());

      wfs_u1.execute(new ServiceCommand()
      {
         private static final long serialVersionUID = 1L;

         @Override
         public Serializable execute(ServiceFactory sf)
         {
            // do nothing
            return null;
         }
      });

      try
      {
         wfs_u2.execute(new ServiceCommand()
         {
            private static final long serialVersionUID = 1L;

            @Override
            public Serializable execute(ServiceFactory sf)
            {
               // next possible synch should remove the user group link for user
               final GlobalParameters params = GlobalParameters.globals();
               params.set(RETURN_GROUP_CONFIG, Boolean.FALSE.toString());
               params.set(
                     SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_USER_SYNC_TIMEOUT, 0);

               short partitionOid = SecurityProperties.getPartitionOid();
               UserGroupBean userGroup = UserGroupBean.findById("theUserGroup",
                     partitionOid);

               for (Iterator i = userGroup.findAllUsers(); i.hasNext();)
               {
                  // just iterate - this should synch again
                  i.next();
               }

               return null;
            }
         });
      }
      catch (Exception e)
      {
         removeSynchProvider();
         fail(e.getMessage());
      }
      finally
      {
         params.set(RETURN_GROUP_CONFIG, Boolean.FALSE.toString());
         removeSynchProvider();
      }
   }

   @Test
   public void testCreateModifyUserExternalAuthenticationInternalAuthrizationSuccess()
   {
      final GlobalParameters params = GlobalParameters.globals();
      try
      {
         // set any synch provider
         setSynchProvider(RegulareUserWithConfigurableGroupsSyncProvider.class);
         params.set(SecurityProperties.AUTHORIZATION_MODE_PROPERTY, SecurityProperties.AUTHORIZATION_MODE_INTERNAL);

         // create user should be allowed - but no password being required to be set
         UserHome.UserCredentials userWithoutPW = new UserHome.UserCredentials(REGULAR_USER_ID, null);
         Role adminRole = (Role) sf.getQueryService().getParticipant(PredefinedConstants.ADMINISTRATOR_ROLE);

         User user = UserHome.create(sf, userWithoutPW, adminRole);

         // TODO: check that password is always empty, even if provided.
         // cannot be done with black box testing as API does not provide password data in user object

         // need to access returned password - can only be done with implementation details
         List<Grant> allGrants = user.getAllGrants();
         Assert.assertTrue(allGrants.size() == 1);
         Assert.assertTrue(PredefinedConstants.ADMINISTRATOR_ROLE.equals(allGrants.get(0).getQualifiedId()));

         user.setAccount(REGULAR_USER2_ID);
         user.setFirstName(REGULAR_USER2_ID);
         user = UserHome.modify(sf, user);

         // Account needs to be changed ...
         Assert.assertTrue(REGULAR_USER2_ID.equals(user.getAccount()));
         // ... other setting still need to be the same
         Assert.assertTrue(REGULAR_USER_ID.equals(user.getFirstName()));

      }
      finally
      {
         params.set(SecurityProperties.AUTHORIZATION_MODE_PROPERTY, null);
         removeSynchProvider();
      }
   }

   private void setSynchProvider(Class<? extends DynamicParticipantSynchronizationProvider> class1)
   {
      /* we need to flush the partition since the extension provider for DynamicParticipantSynchronizationProvider is cached */
      final FlushPartitionPredicate flushPartition = new FlushPartitionPredicate(DEFAULT_PARTITION_ID);
      ExtensionProviderUtils.forEachExtensionsManager(flushPartition);

      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, class1.getName());
   }

   private void removeSynchProvider()
   {
      /* we need to flush the partition since the extension provider for DynamicParticipantSynchronizationProvider is cached */
      final FlushPartitionPredicate flushPartition = new FlushPartitionPredicate(DEFAULT_PARTITION_ID);
      ExtensionProviderUtils.forEachExtensionsManager(flushPartition);

      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, "None");
   }

   public static final class RegulareUserWithConfigurableGroupsSyncProvider extends DynamicParticipantSynchronizationProvider
   {
      private static final class RegularUserUserGroupConfiguration extends
            ExternalUserGroupConfiguration
      {
         @Override
         public Map getProperties()
         {
            return Collections.emptyMap();
         }

         @Override
         public String getName()
         {
            return "theUserGroup";
         }

         @Override
         public String getDescription()
         {
            return "theUserGroup";
         }
      }

      @Override
      public ExternalUserConfiguration provideUserConfiguration(final String account)
      {
         return new RegularUserConfigurationWithConfigurableGroups(account);
      }

      @Override
      public ExternalUserGroupConfiguration provideUserGroupConfiguration(String groupId)
      {
         return new RegularUserUserGroupConfiguration();
      }
   }

   private static class RegularUserConfigurationWithConfigurableGroups extends ExternalUserConfiguration
   {
      private String account;

      public RegularUserConfigurationWithConfigurableGroups(String account)
      {
         this.account = account;
      }

      @Override
      public String getDescription()
      {
         return account;
      }

      @Override
      public String getEMail()
      {
         return account;
      }

      @Override
      public String getFirstName()
      {
         return account;
      }

      @Override
      public String getLastName()
      {
         return account;
      }

      @Override
      public Set<GrantInfo> getModelParticipantsGrants()
      {
         if (PredefinedConstants.MOTU.equals(account))
         {
            return Collections.singleton(new GrantInfo(
                  PredefinedConstants.ADMINISTRATOR_ROLE, Collections
                        .<String> emptyList()));
         }

         return super.getModelParticipantsGrants();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Collection<String> getUserGroupMemberships()
      {
         final GlobalParameters params = GlobalParameters.globals();
         Object returnGroupConfig = params.get(RETURN_GROUP_CONFIG);

         if (Boolean.TRUE.toString().equals(returnGroupConfig))
         {
            // TODO Auto-generated method stub
            return Collections.singletonList("theUserGroup");
         }
         else
         {
            return super.getUserGroupMemberships();
         }
      }

      @Override
      public Map<?, ?> getProperties()
      {
         return Collections.emptyMap();
      }
   }
}
