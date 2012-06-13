/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationProvider;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationStrategy;
import org.eclipse.stardust.engine.core.spi.security.ExternalUserConfiguration;
import org.eclipse.stardust.engine.core.spi.security.TimebasedSynchronizationStrategy;

import junit.framework.TestCase;


public class SynchronizationServiceTest extends TestCase
{
   GlobalParameters globals;

   @Override
   protected void setUp() throws Exception
   {
      this.globals = GlobalParameters.globals();
   }

   @Override
   protected void tearDown() throws Exception
   {
      Parameters.instance().flush();
   }

   public void testByDefaultNoSyncProviderWillBeUsed()
   {
      DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

      assertNull(provider);
   }

   public void testNoSyncProviderIndicatesInternalAuthentication()
   {
      testByDefaultNoSyncProviderWillBeUsed();

      assertTrue(SecurityProperties.isInternalAuthentication());
   }

   public void testAStaticallyConfiguredNonDefaultProviderWillBeUsed()
   {
      globals.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, TestSyncProvider.class.getName());

      DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

      assertNotNull(provider);
      assertSame(TestSyncProvider.class, provider.getClass());
   }

   public void testAStaticallyConfiguredNonDefaultProviderIndicatesExternalAuthentication()
   {
      testAStaticallyConfiguredNonDefaultProviderWillBeUsed();

      assertFalse(SecurityProperties.isInternalAuthentication());
   }

   public void testAStaticallyConfiguredThreadLocalNonDefaultProviderWillBeUsed()
   {
      ParametersFacade.pushLayer(singletonMap( //
            AUTHORIZATION_SYNC_CLASS_PROPERTY, TestSyncProvider.class.getName()));

      try
      {
         DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

         assertNotNull(provider);
         assertSame(TestSyncProvider.class, provider.getClass());
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public void testAStaticallyConfiguredThreadLocalNonDefaultProviderIndicatesExternalAuthentication()
   {
      testAStaticallyConfiguredThreadLocalNonDefaultProviderWillBeUsed();

      assertFalse(SecurityProperties.isInternalAuthentication());
   }

   public void testANonDefaultServiceProviderWillBeUsed()
   {
      globals.set(DynamicParticipantSynchronizationProvider.class.getName()
            + ".Providers", TestSyncProvider.class.getName());

      DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

      assertNotNull(provider);
      assertSame(TestSyncProvider.class, provider.getClass());
   }

   public void testANonDefaultServiceProviderIndicatesExternalAuthentication()
   {
      testANonDefaultServiceProviderWillBeUsed();

      assertFalse(SecurityProperties.isInternalAuthentication());
   }

   public void testByDefaultTimeBasedStrategyWillBeUsed()
   {
      DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

      assertNotNull(strategy);
      assertSame(TimebasedSynchronizationStrategy.class, strategy.getClass());
   }

   public void testAStaticallyConfiguredNonDefaultStrategyWillBeUsed()
   {
      globals.set(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY, TestSyncStrategy.class.getName());

      DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

      assertNotNull(strategy);
      assertSame(TestSyncStrategy.class, strategy.getClass());
   }

   public void testAStaticallyConfiguredThreadLocalNonDefaultStrategyWillBeUsed()
   {
      ParametersFacade.pushLayer(singletonMap(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY,
            TestSyncStrategy.class.getName()));

      try
      {
         DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

         assertNotNull(strategy);
         assertSame(TestSyncStrategy.class, strategy.getClass());
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public void testANonDefaultServiceStrategyWillBeUsed()
   {
      globals.set(DynamicParticipantSynchronizationStrategy.class.getName()
            + ".Providers", TestSyncStrategy.class.getName());

      DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

      assertNotNull(strategy);
      assertSame(TestSyncStrategy.class, strategy.getClass());
   }

   public static class TestSyncProvider extends DynamicParticipantSynchronizationProvider
   {
      @Override
      public ExternalUserConfiguration provideUserConfiguration(String account)
      {
         return null;
      }
   }

   public static class TestSyncStrategy extends DynamicParticipantSynchronizationStrategy
   {
      @Override
      public boolean isDirty(IUser user)
      {
         return false;
      }

      @Override
      public void setSynchronized(IUser user)
      {
      }
   }
}
