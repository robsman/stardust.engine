/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.api.runtime.MultiPartitionTestSupport.withinMockPartition;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationProvider;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationStrategy;
import org.eclipse.stardust.engine.core.spi.security.ExternalUserConfiguration;

import junit.framework.TestCase;


public class MultiplePartitionsSynchronizationServiceTest extends TestCase
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

   public void testPartitionAUsesSyncProviderA()
   {
      globals.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, TestSyncProviderA.class.getName());

      withinMockPartition("A", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

            assertNotNull(provider);
            assertSame(TestSyncProviderA.class, provider.getClass());
         }
      });
   }

   public void testPartitionBUsesSyncProviderB()
   {
      globals.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, TestSyncProviderB.class.getName());

      withinMockPartition("B", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

            assertNotNull(provider);
            assertSame(TestSyncProviderB.class, provider.getClass());
         }
      });
   }

   public void testPartitionAUsesSyncStrategyA()
   {
      globals.set(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY, TestSyncStrategyA.class.getName());

      withinMockPartition("A", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.initializeStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyA.class, strategy.getClass());
         }
      });
   }

   public void testPartitionAUsesSyncStrategyB()
   {
      globals.set(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY, TestSyncStrategyB.class.getName());

      withinMockPartition("B", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.initializeStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyB.class, strategy.getClass());
         }
      });
   }

   public void testSyncProviderIsResolvedPerPartition()
   {
      globals.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, TestSyncProviderA.class.getName());

      withinMockPartition("A", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

            assertNotNull(provider);
            assertSame(TestSyncProviderA.class, provider.getClass());
         }
      });

      globals.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, TestSyncProviderB.class.getName());

      withinMockPartition("B", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationProvider provider = SynchronizationService.getSynchronizationProvider();

            assertNotNull(provider);
            assertSame(TestSyncProviderB.class, provider.getClass());
         }
      });
   }

   public void testSyncStrategyIsResolvedPerPartition()
   {
      globals.set(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY, TestSyncStrategyA.class.getName());

      withinMockPartition("A", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyA.class, strategy.getClass());
         }
      });

      globals.set(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY, TestSyncStrategyB.class.getName());

      withinMockPartition("B", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyB.class, strategy.getClass());
         }
      });
   }

   public void testSyncStrategyIsCachedPerPartition()
   {
      globals.set(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY, TestSyncStrategyA.class.getName());

      final AtomicReference<DynamicParticipantSynchronizationStrategy> partitionAStrategy = new AtomicReference<DynamicParticipantSynchronizationStrategy>();
      withinMockPartition("A", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyA.class, strategy.getClass());

            partitionAStrategy.set(strategy);
         }
      });

      globals.set(AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY, TestSyncStrategyB.class.getName());

      final AtomicReference<DynamicParticipantSynchronizationStrategy> partitionBStrategy = new AtomicReference<DynamicParticipantSynchronizationStrategy>();
      withinMockPartition("B", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyB.class, strategy.getClass());

            partitionBStrategy.set(strategy);
         }
      });

      withinMockPartition("A", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyA.class, strategy.getClass());

            assertSame(partitionAStrategy.get(), strategy);
         }
      });

      withinMockPartition("B", new Runnable()
      {
         public void run()
         {
            DynamicParticipantSynchronizationStrategy strategy = SynchronizationService.getSynchronizationStrategy();

            assertNotNull(strategy);
            assertSame(TestSyncStrategyB.class, strategy.getClass());

            assertSame(partitionBStrategy.get(), strategy);
         }
      });
   }

   public static class TestSyncProviderA extends DynamicParticipantSynchronizationProvider
   {
      @Override
      public ExternalUserConfiguration provideUserConfiguration(String account)
      {
         return null;
      }
   }

   public static class TestSyncProviderB extends DynamicParticipantSynchronizationProvider
   {
      @Override
      public ExternalUserConfiguration provideUserConfiguration(String account)
      {
         return null;
      }
   }

   public static class TestSyncStrategyA extends DynamicParticipantSynchronizationStrategy
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

   public static class TestSyncStrategyB extends DynamicParticipantSynchronizationStrategy
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
