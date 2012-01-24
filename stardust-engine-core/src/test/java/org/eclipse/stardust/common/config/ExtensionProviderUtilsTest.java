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
package org.eclipse.stardust.common.config;

import static org.eclipse.stardust.common.config.ExtensionProviderUtils.getExtensionProviders;
import static org.eclipse.stardust.common.config.ExtensionProviderUtils.getFirstExtensionProvider;
import static org.eclipse.stardust.common.config.extensions.TestExtensions.PRP_SPI1;
import static org.eclipse.stardust.common.config.extensions.TestExtensions.PRP_SPI2;
import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplC;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplD;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1StatefulImpl;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1StatelessImpl;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplB;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplC;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ExtensionProviderUtilsTest
{
   @Before
   public void setUp() throws Exception
   {
      assertNull(Parameters.instance().get(PRP_SPI1));
   }

   @After
   public void tearDown() throws Exception
   {
      Parameters.instance().flush();
   }

   @Test
   public void testWithoutConfiguredProviderAndNoDefaultNoProviderWillBeFound()
   {
      Spi1 provider = getFirstExtensionProvider(Spi1.class, PRP_SPI1);

      assertNull(provider);
   }

   @Test
   public void testAConfiguredProviderWillBeFound()
   {
      GlobalParameters.globals().set(PRP_SPI1, Spi1ImplA.class.getName());

      Spi1 provider = getFirstExtensionProvider(Spi1.class, PRP_SPI1);

      assertNotNull(provider);
      assertSame(Spi1ImplA.class, provider.getClass());
   }

   @Test
   public void testAServiceProviderWillBeFound()
   {
      Spi2 provider = getFirstExtensionProvider(Spi2.class, PRP_SPI2);

      assertNotNull(provider);
      assertSame(Spi2ImplB.class, provider.getClass());
   }

   @Test
   public void testAllConfiguredProvidersWillBeFound()
   {
      GlobalParameters.globals().set(PRP_SPI1,
            Spi1ImplC.class.getName() + ", " + Spi1ImplD.class.getName());

      List<Spi1> provider = getExtensionProviders(Spi1.class, PRP_SPI1);

      assertEquals(2, provider.size());
      assertSame(Spi1ImplC.class, provider.get(0).getClass());
      assertSame(Spi1ImplD.class, provider.get(1).getClass());
   }

   @Test
   public void testAllConfiguredServiceProvidersWillBeFound()
   {
      GlobalParameters.globals().set(Spi2.class.getName() + ".Providers",
            Spi2ImplA.class.getName());

      List<Spi2> provider = getExtensionProviders(Spi2.class, PRP_SPI2);

      assertEquals(2, provider.size());
      assertSame(Spi2ImplA.class, provider.get(0).getClass());
      assertSame(Spi2ImplB.class, provider.get(1).getClass());
   }

   @Test
   public void testAConfiguredProviderWillBeFoundBeforeAServiceProvider()
   {
      GlobalParameters.globals().set(PRP_SPI2, Spi2ImplC.class.getName());

      List<Spi2> providers = getExtensionProviders(Spi2.class, PRP_SPI2);

      assertEquals(2, providers.size());
      assertSame(Spi2ImplC.class, providers.get(0).getClass());
      assertSame(Spi2ImplB.class, providers.get(1).getClass());
   }

   @Test
   public void testStatefulExtensionsAreInstantiatedPerResolution()
   {
      GlobalParameters.globals().set(PRP_SPI1, Spi1StatefulImpl.class.getName());

      List<Spi1> run1 = getExtensionProviders(Spi1.class, PRP_SPI1);

      assertEquals(1, run1.size());
      assertSame(Spi1StatefulImpl.class, run1.get(0).getClass());

      List<Spi1> run2 = getExtensionProviders(Spi1.class, PRP_SPI1);

      assertEquals(1, run2.size());
      assertSame(Spi1StatefulImpl.class, run2.get(0).getClass());

      assertNotSame(run1.get(0), run2.get(0));
   }

   @Test
   public void testStatelessExtensionsAreCachedAfterInitialResolution()
   {
      GlobalParameters.globals().set(PRP_SPI1, Spi1StatelessImpl.class.getName());

      List<Spi1> run1 = getExtensionProviders(Spi1.class, PRP_SPI1);

      assertEquals(1, run1.size());
      assertSame(Spi1StatelessImpl.class, run1.get(0).getClass());

      List<Spi1> run2 = getExtensionProviders(Spi1.class, PRP_SPI1);

      assertEquals(1, run2.size());
      assertSame(Spi1StatelessImpl.class, run2.get(0).getClass());

      assertSame(run1.get(0), run2.get(0));
   }
}
