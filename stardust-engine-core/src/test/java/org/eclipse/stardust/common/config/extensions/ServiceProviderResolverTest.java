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

import static org.eclipse.stardust.common.config.extensions.ServiceProviderResolver.resolveFirstServiceProviderFromClasspath;
import static org.eclipse.stardust.common.config.extensions.ServiceProviderResolver.resolveServiceProvidersFromClasspath;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplB;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi1ImplD;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplA;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplB;
import org.eclipse.stardust.common.config.extensions.TestExtensions.Spi2ImplC;

import junit.framework.TestCase;


public class ServiceProviderResolverTest extends TestCase
{
   @Override
   protected void setUp() throws Exception
   {
      Enumeration<URL> spi1Descriptors = getClass().getClassLoader().getResources(
            "META-INF/services/" + Spi1.class.getName());
      assertNotNull(spi1Descriptors);

      assertFalse("No provider descriptor must be available for TestSpi2",
            spi1Descriptors.hasMoreElements());

      Enumeration<URL> spi2Descriptors = getClass().getClassLoader().getResources(
            "META-INF/services/" + Spi2.class.getName());
      assertNotNull(spi2Descriptors);

      assertTrue("Test provider descripter must be available",
            spi2Descriptors.hasMoreElements());

      spi2Descriptors.nextElement();
      assertFalse("Only one provider descriptor must be available for TestSpi1",
            spi2Descriptors.hasMoreElements());
   }

   @Override
   protected void tearDown() throws Exception
   {
      Parameters.instance().flush();
   }

   public void testWithoutDeclaredProviderAndNoConfiguredDefaultNoProvidersWillBeFound()
   {
      List<Spi1> providers = resolveServiceProvidersFromClasspath(Spi1.class);

      assertEquals(0, providers.size());
   }

   public void testWithoutDeclaredProviderAndNoConfiguredDefaultNoFirstProviderWillBeFound()
   {
      Spi1 provider = resolveFirstServiceProviderFromClasspath(Spi1.class);

      assertNull(provider);
   }

   public void testWithoutDeclaredProviderAndNoConfiguredDefaultAGivenFallbackTypeWillBeUsed()
   {
      Spi1 provider = resolveFirstServiceProviderFromClasspath(Spi1.class, Spi1ImplD.class);

      assertNotNull(provider);
      assertSame(Spi1ImplD.class, provider.getClass());
   }

   public void testWithoutDeclaredProviderTheConfiguredDefaultWillBeUsed()
   {
      GlobalParameters.globals().set(Spi1.class.getName() + ".Providers",
            Spi1ImplA.class.getName());

      List<Spi1> providers = resolveServiceProvidersFromClasspath(Spi1.class);

      assertEquals(1, providers.size());
      assertSame(Spi1ImplA.class, providers.get(0).getClass());
   }

   public void testWithoutDeclaredProviderAllConfiguredDefaultsWillBeUsed()
   {
      GlobalParameters.globals().set(Spi1.class.getName() + ".Providers",
            Spi1ImplA.class.getName() + ", " + Spi1ImplB.class.getName());

      List<Spi1> providers = resolveServiceProvidersFromClasspath(Spi1.class);

      assertEquals(2, providers.size());
      assertSame(Spi1ImplA.class, providers.get(0).getClass());
      assertSame(Spi1ImplB.class, providers.get(1).getClass());
   }

   public void testADeclaredProviderWillBeFound()
   {
      List<Spi2> providers = resolveServiceProvidersFromClasspath(Spi2.class);

      assertEquals(1, providers.size());
      assertSame(Spi2ImplB.class, providers.get(0).getClass());
   }

   public void testADeclaredProviderWillBeFoundAfterTheConfiguredDefault()
   {
      GlobalParameters.globals().set(Spi2.class.getName() + ".Providers",
            Spi2ImplA.class.getName());

      List<Spi2> providers = resolveServiceProvidersFromClasspath(Spi2.class);

      assertEquals(2, providers.size());
      assertSame(Spi2ImplA.class, providers.get(0).getClass());
      assertSame(Spi2ImplB.class, providers.get(1).getClass());
   }

   public void testADeclaredProviderWillBeFoundAfterAllConfiguredDefaults()
   {
      GlobalParameters.globals().set(Spi2.class.getName() + ".Providers",
            Spi2ImplA.class.getName() + ", " + Spi2ImplC.class.getName());

      List<Spi2> providers = resolveServiceProvidersFromClasspath(Spi2.class);

      assertEquals(3, providers.size());
      assertSame(Spi2ImplA.class, providers.get(0).getClass());
      assertSame(Spi2ImplC.class, providers.get(1).getClass());
      assertSame(Spi2ImplB.class, providers.get(2).getClass());
   }

   public void testADeclaredProviderWillNotBeFoundIfBlacklisted()
   {
      GlobalParameters.globals().set(Spi2.class.getName() + ".BlacklistedProviders",
            Spi2ImplB.class.getName());

      List<Spi2> providers = resolveServiceProvidersFromClasspath(Spi2.class);

      assertEquals(0, providers.size());
   }
}
