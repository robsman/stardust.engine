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
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
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
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

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
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

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
