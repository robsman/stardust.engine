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
package org.eclipse.stardust.common.config.extensions;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.stardust.common.config.extensions.ApplicationConfigResolver.getStaticallyConfiguredProvider;
import static org.eclipse.stardust.common.config.extensions.ServiceProviderResolver.resolveServiceProvidersFromClasspath;
import static org.eclipse.stardust.common.config.extensions.TestExtensions.PRP_SPI1;
import static org.eclipse.stardust.common.config.extensions.TestExtensions.PRP_SPI2;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.extensions.ExtensibleExtensionsManager;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplB;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplD;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplB;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplC;
import org.eclipse.stardust.common.config.extensions.spi.ExtensionsResolver;

import junit.framework.TestCase;


public class ExtensibleExtensionsManagerTest extends TestCase
{
   static ThreadLocal<Object> SPI_IMPL = new ThreadLocal<Object>();

   private ExtensibleExtensionsManager extensionsManager;

   @Override
   protected void setUp() throws Exception
   {
      GlobalParameters.globals().set(ExtensionsResolver.class.getName() + ".Providers",
            TestExtensionsResolver.class.getName());

      this.extensionsManager = new ExtensibleExtensionsManager();
   }

   @Override
   protected void tearDown() throws Exception
   {
      SPI_IMPL.remove();

      Parameters.instance().flush();
   }

   public void testSpi1HasNoStaticallyConfiguredProvider()
   {
      assertNull(getStaticallyConfiguredProvider(Spi1.class, PRP_SPI1));
   }

   public void testSpi2HasNoStaticallyConfiguredProvider()
   {
      assertNull(getStaticallyConfiguredProvider(Spi2.class, PRP_SPI1));
   }

   public void testSpi1HasNoDeclaredServiceProvider()
   {
      assertEquals(0, resolveServiceProvidersFromClasspath(Spi1.class).size());
   }

   public void testSpi2HasADeclaredServiceProviderSpi2ImplB()
   {
      assertEquals(1, resolveServiceProvidersFromClasspath(Spi2.class).size());
      assertSame(Spi2ImplB.class, resolveServiceProvidersFromClasspath(Spi2.class).get(0)
            .getClass());
   }

   public void testStaticallyConfiguredExtensionsAreFound()
   {
      GlobalParameters.globals().set(PRP_SPI1, Spi1ImplB.class.getName());

      List<Spi1> providers = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(1, providers.size());
      assertSame(Spi1ImplB.class, providers.get(0).getClass());
   }

   public void testServiceProvidersAreFound()
   {
      List<Spi2> providers = extensionsManager.getExtensionProviders(Spi2.class, PRP_SPI1);
      assertEquals(1, providers.size());
      assertSame(Spi2ImplB.class, providers.get(0).getClass());
   }

   public void testStaticallyConfiguredExtensionsAndServiceProvidersAreFound()
   {
      GlobalParameters.globals().set(PRP_SPI1, Spi2ImplA.class.getName());

      List<Spi2> providers = extensionsManager.getExtensionProviders(Spi2.class, PRP_SPI1);
      assertEquals(2, providers.size());
      assertSame(Spi2ImplA.class, providers.get(0).getClass());
      assertSame(Spi2ImplB.class, providers.get(1).getClass());
   }

   public void testExtensionsFromNonDefaultResolversAreFound()
   {
      List<Spi1> providers = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(0, providers.size());

      SPI_IMPL.set(new Spi1ImplD());

      providers = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(1, providers.size());
      assertSame(Spi1ImplD.class, providers.get(0).getClass());
   }

   public void testStaticallyConfiguredExtensionsAndExtensionsFromNonDefaultResolversAreFound()
   {
      GlobalParameters.globals().set(PRP_SPI1, Spi1ImplA.class.getName());

      List<Spi1> providers = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(1, providers.size());
      assertSame(Spi1ImplA.class, providers.get(0).getClass());

      SPI_IMPL.set(new Spi1ImplD());

      providers = extensionsManager.getExtensionProviders(Spi1.class, PRP_SPI1);
      assertEquals(2, providers.size());
      assertSame(Spi1ImplA.class, providers.get(0).getClass());
      assertSame(Spi1ImplD.class, providers.get(1).getClass());
   }

   public void testExtensionsFromNonDefaultResolversAndServiceProvidersAreFound()
   {
      List<Spi2> providers = extensionsManager.getExtensionProviders(Spi2.class, PRP_SPI2);
      assertEquals(1, providers.size());
      assertSame(Spi2ImplB.class, providers.get(0).getClass());

      SPI_IMPL.set(new Spi2ImplC());

      providers = extensionsManager.getExtensionProviders(Spi2.class, PRP_SPI2);
      assertEquals(2, providers.size());
      assertSame(Spi2ImplC.class, providers.get(0).getClass());
      assertSame(Spi2ImplB.class, providers.get(1).getClass());
   }

   public void testStaticallyConfiguredExtensionsAndExtensionsFromNonDefaultResolversAndServiceProvidersAreFound()
   {
      GlobalParameters.globals().set(PRP_SPI2, Spi2ImplA.class.getName());

      List<Spi2> providers = extensionsManager.getExtensionProviders(Spi2.class, PRP_SPI2);
      assertEquals(2, providers.size());
      assertSame(Spi2ImplA.class, providers.get(0).getClass());
      assertSame(Spi2ImplB.class, providers.get(1).getClass());

      SPI_IMPL.set(new Spi2ImplC());

      providers = extensionsManager.getExtensionProviders(Spi2.class, PRP_SPI2);
      assertEquals(3, providers.size());
      assertSame(Spi2ImplA.class, providers.get(0).getClass());
      assertSame(Spi2ImplC.class, providers.get(1).getClass());
      assertSame(Spi2ImplB.class, providers.get(2).getClass());
   }

   public static class TestExtensionsResolver implements ExtensionsResolver
   {
      public <T> List<T> resolveExtensionProviders(Class<T> providerIntfc, Map<String, ?> resolutionConfig)
      {
         if (providerIntfc.isInstance(SPI_IMPL.get()))
         {
            return singletonList(providerIntfc.cast(SPI_IMPL.get()));
         }
         else
         {
            return emptyList();
         }
      }
   }
}
