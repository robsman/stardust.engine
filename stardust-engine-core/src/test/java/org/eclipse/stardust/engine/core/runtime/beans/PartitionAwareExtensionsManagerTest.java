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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.stardust.engine.api.runtime.MultiPartitionTestSupport.newMockPartition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.annotations.ConfigurationProperty;
import org.eclipse.stardust.common.annotations.PropertyValueType;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.extensions.TestExtensions;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplB;
import org.eclipse.stardust.common.config.extensions.spi.ExtensionsResolver;
import org.eclipse.stardust.engine.core.runtime.beans.PartitionAwareExtensionsManager;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PartitionAwareExtensionsManagerTest
{
   @ConfigurationProperty(status = Status.Internal, useRestriction = UseRestriction.Internal)
   @PropertyValueType(Spi1.class)
   private static final String PRP_SPI1 = "TestConfig.Extensions.SPI1";

   static ThreadLocal<Spi1> SPI1_TENANT_A = new ThreadLocal<TestExtensions.Spi1>();

   static ThreadLocal<Spi1> SPI1_TENANT_B = new ThreadLocal<TestExtensions.Spi1>();

   private PartitionAwareExtensionsManager extensionsManager;

   public void setPartitionId(final String partitionId)
   {
      GlobalParameters.globals().set(SecurityProperties.CURRENT_PARTITION,
            newMockPartition(partitionId));
   }

   @Before
   public void setUp() throws Exception
   {
      GlobalParameters globals = GlobalParameters.globals();

      this.extensionsManager = new PartitionAwareExtensionsManager();

      // initialize tenant A specific extensions manager

      globals.set(ExtensionsResolver.class.getName() + ".Providers",
            TenantAResolver.class.getName());

      setPartitionId("a");
      extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);

      // initialize tenant B specific extensions manager

      globals.set(ExtensionsResolver.class.getName() + ".Providers",
            TenantBResolver.class.getName());

      setPartitionId("b");
      extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
   }

   @After
   public void tearDown() throws Exception
   {
      SPI1_TENANT_A.remove();
      SPI1_TENANT_B.remove();

      Parameters.instance().flush();
   }

   @Test
   public void testExtensionAreResolvedWithPartitionScope()
   {
      // for tenant A requests, only the A resolver should be invoked
      // for tenant B requests, only the B resolver should be invoked

      setPartitionId("a");
      List<Spi1> tenantAProviders = extensionsManager.getExtensionProviders(Spi1.class,
            PRP_SPI1);
      assertEquals(0, tenantAProviders.size());

      setPartitionId("b");
      List<Spi1> tenantBProviders = extensionsManager.getExtensionProviders(Spi1.class,
            PRP_SPI1);
      assertEquals(0, tenantBProviders.size());

      SPI1_TENANT_A.set(new Spi1ImplA());

      setPartitionId("a");
      tenantAProviders = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(1, tenantAProviders.size());
      assertSame(Spi1ImplA.class, tenantAProviders.get(0).getClass());

      setPartitionId("b");
      tenantBProviders = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(0, tenantBProviders.size());

      SPI1_TENANT_B.set(new Spi1ImplB());

      setPartitionId("a");
      tenantAProviders = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(1, tenantAProviders.size());
      assertSame(Spi1ImplA.class, tenantAProviders.get(0).getClass());

      setPartitionId("b");
      tenantBProviders = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(1, tenantBProviders.size());
      assertSame(Spi1ImplB.class, tenantBProviders.get(0).getClass());

      SPI1_TENANT_A.remove();

      setPartitionId("a");
      tenantAProviders = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(0, tenantAProviders.size());

      setPartitionId("b");
      tenantBProviders = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(1, tenantBProviders.size());
      assertSame(Spi1ImplB.class, tenantBProviders.get(0).getClass());
   }

   public static class TenantAResolver implements ExtensionsResolver
   {
      public <T> List<T> resolveExtensionProviders(Class<T> providerIntfc,
            Map<String, ? > resolutionConfig)
      {
         if (providerIntfc.isInstance(SPI1_TENANT_A.get()))
         {
            return singletonList(providerIntfc.cast(SPI1_TENANT_A.get()));
         }
         else
         {
            return emptyList();
         }
      }
   }

   public static class TenantBResolver implements ExtensionsResolver
   {
      public <T> List<T> resolveExtensionProviders(Class<T> providerIntfc,
            Map<String, ? > resolutionConfig)
      {
         if (providerIntfc.isInstance(SPI1_TENANT_B.get()))
         {
            return singletonList(providerIntfc.cast(SPI1_TENANT_B.get()));
         }
         else
         {
            return emptyList();
         }
      }
   }
}
