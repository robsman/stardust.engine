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
package org.eclipse.stardust.engine.core.runtime.beans.removethis;

import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_INTERNAL;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_JAAS;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_PROPERTY;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_SERVICE_PROPERTY;

import java.util.Map;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.LoginServiceFactory;
import org.eclipse.stardust.engine.core.security.audittrail.AuditTrailLoginService;
import org.eclipse.stardust.engine.core.security.jaas.JaasLoginService;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginProvider;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginResult;
import org.eclipse.stardust.engine.core.spi.security.LoginProviderAdapter;
import org.eclipse.stardust.engine.core.spi.security.LoginResult;

import junit.framework.TestCase;


public class LoginServiceFactoryTest extends TestCase
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

   public void testForInternalModeByDefaultTheAuditTrailLoginServiceWillBeUsed()
   {
      globals.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);
      ExternalLoginProvider loginProvider = LoginServiceFactory.getService();

      assertNotNull(loginProvider);
      assertSame(AuditTrailLoginService.class, loginProvider.getClass());
   }

   public void testForInternalModeAStaticallyConfiguredNonDefaultProviderWillBeUsed()
   {
      globals.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);
      globals.set(AUTHENTICATION_SERVICE_PROPERTY, TestLoginProvider.class.getName());

      ExternalLoginProvider loginProvider = LoginServiceFactory.getService();

      assertNotNull(loginProvider);
      assertSame(TestLoginProvider.class, loginProvider.getClass());
   }

   public void testForInternalModeANonDefaultServiceProviderWillBeUsed()
   {
      globals.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);
      globals.set(ExternalLoginProvider.class.getName() + ".Providers",
            TestLoginProvider.class.getName());

      ExternalLoginProvider loginProvider = LoginServiceFactory.getService();

      assertNotNull(loginProvider);
      assertSame(TestLoginProvider.class, loginProvider.getClass());
   }

   public void testForInternalModeAStaticallyConfiguredOldStyleNonDefaultProviderWillBeWrappedAndUsed()
   {
      globals.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);
      globals.set(AUTHENTICATION_SERVICE_PROPERTY,
            TestOldStyleLoginProvider.class.getName());

      ExternalLoginProvider loginProvider = LoginServiceFactory.getService();

      assertNotNull(loginProvider);
      assertSame(LoginProviderAdapter.class, loginProvider.getClass());
      assertSame(TestOldStyleLoginProvider.class,
            Reflect.getFieldValue(loginProvider, "loginProvider").getClass());
   }

   public void testForJaasModeByDefaultTheJaasLoginServiceWillBeUsed()
   {
      globals.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_JAAS);
      ExternalLoginProvider loginProvider = LoginServiceFactory.getService();

      assertNotNull(loginProvider);
      assertSame(JaasLoginService.class, loginProvider.getClass());
   }

   public void testForJaasModeOnlyTheJaasLoginServiceWillBeUsed()
   {
      globals.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_JAAS);
      globals.set(AUTHENTICATION_SERVICE_PROPERTY, TestLoginProvider.class);

      ExternalLoginProvider loginProvider = LoginServiceFactory.getService();

      assertNotNull(loginProvider);
      assertSame(JaasLoginService.class, loginProvider.getClass());
   }

   public static class TestLoginProvider implements ExternalLoginProvider
   {
      public ExternalLoginResult login(String id, String password,
            @SuppressWarnings("rawtypes") Map properties)
      {
         return null;
      }
   }

   @SuppressWarnings("deprecation")
   public static class TestOldStyleLoginProvider implements org.eclipse.stardust.engine.core.spi.security.LoginProvider
   {
      public LoginResult login(String id, String password)
      {
         return null;
      }
   }
}
