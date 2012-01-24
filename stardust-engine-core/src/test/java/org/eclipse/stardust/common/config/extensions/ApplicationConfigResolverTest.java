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

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.common.config.extensions.ApplicationConfigResolver.PRP_CONFIGURATION_PROPERTY;
import static org.eclipse.stardust.common.config.extensions.ApplicationConfigResolver.getStaticallyConfiguredProvider;
import static org.eclipse.stardust.common.config.extensions.TestExtensions.PRP_SPI1;

import java.util.List;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.extensions.ApplicationConfigResolver;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplC;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplD;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1StatefulImpl;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1StatelessImpl;

import junit.framework.TestCase;


public class ApplicationConfigResolverTest extends TestCase
{
   private ApplicationConfigResolver resolver;

   @Override
   protected void setUp() throws Exception
   {
      assertNull(Parameters.instance().get(Spi1.class.getName()));

      resolver = new ApplicationConfigResolver();
   }

   @Override
   protected void tearDown() throws Exception
   {
      Parameters.instance().flush();
   }

   public void testWithoutConfiguredProviderNoProviderWillBeFound()
   {
      Spi1 provider = getStaticallyConfiguredProvider(Spi1.class, PRP_SPI1);

      assertNull(provider);
   }

   public void testOneConfiguredProviderWillBeFound()
   {
      GlobalParameters.globals().set(PRP_SPI1, Spi1ImplA.class.getName());

      Spi1 provider = getStaticallyConfiguredProvider(Spi1.class, PRP_SPI1);

      assertNotNull(provider);
      assertSame(Spi1ImplA.class, provider.getClass());
   }

   public void testMultipleConfiguredProvidersWillBeFound()
   {
      GlobalParameters.globals().set(PRP_SPI1,
            Spi1ImplC.class.getName() + ", " + Spi1ImplD.class.getName());

      List<Spi1> provider = ApplicationConfigResolver.getStaticallyConfiguredProviders(
            Spi1.class, PRP_SPI1);

      assertEquals(2, provider.size());
      assertSame(Spi1ImplC.class, provider.get(0).getClass());
      assertSame(Spi1ImplD.class, provider.get(1).getClass());
   }

   public void testOneThreadLocallyConfiguredProviderWillBeFound()
   {
      ParametersFacade.pushLayer(singletonMap(PRP_SPI1, Spi1ImplA.class.getName()));
      try
      {
         Spi1 provider = getStaticallyConfiguredProvider(Spi1.class, PRP_SPI1);

         assertNotNull(provider);
         assertSame(Spi1ImplA.class, provider.getClass());
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public void testMultipleThreadLocallyConfiguredProvidersWillBeFound()
   {
      ParametersFacade.pushLayer(singletonMap( //
            PRP_SPI1, //
            Spi1ImplC.class.getName() + ", " + Spi1ImplD.class.getName()));

      try
      {
         List<Spi1> providers = ApplicationConfigResolver.getStaticallyConfiguredProviders(
               Spi1.class, PRP_SPI1);

         assertEquals(2, providers.size());
         assertSame(Spi1ImplC.class, providers.get(0).getClass());
         assertSame(Spi1ImplD.class, providers.get(1).getClass());
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public void testStatefulExtensionsAreInstantiatedPerResolution()
   {
      GlobalParameters.globals().set(PRP_SPI1,
            Spi1StatefulImpl.class.getName());

      List<Spi1> run1 = resolver.resolveExtensionProviders(Spi1.class,
            singletonMap(PRP_CONFIGURATION_PROPERTY, PRP_SPI1));

      assertEquals(1, run1.size());
      assertSame(Spi1StatefulImpl.class, run1.get(0).getClass());

      List<Spi1> run2 = resolver.resolveExtensionProviders(Spi1.class,
            singletonMap(PRP_CONFIGURATION_PROPERTY, PRP_SPI1));

      assertEquals(1, run2.size());
      assertSame(Spi1StatefulImpl.class, run2.get(0).getClass());

      assertNotSame(run1.get(0), run2.get(0));
   }

   public void testStatelessExtensionsAreCachedAfterInitialResolution()
   {
      GlobalParameters.globals().set(PRP_SPI1,
            Spi1StatelessImpl.class.getName());

      List<Spi1> run1 = resolver.resolveExtensionProviders(Spi1.class,
            singletonMap(PRP_CONFIGURATION_PROPERTY, PRP_SPI1));

      assertEquals(1, run1.size());
      assertSame(Spi1StatelessImpl.class, run1.get(0).getClass());

      List<Spi1> run2 = resolver.resolveExtensionProviders(Spi1.class,
            singletonMap(PRP_CONFIGURATION_PROPERTY, PRP_SPI1));

      assertEquals(1, run2.size());
      assertSame(Spi1StatelessImpl.class, run2.get(0).getClass());

      assertSame(run1.get(0), run2.get(0));
   }
}
