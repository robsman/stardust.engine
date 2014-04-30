/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.security;

import static java.util.Collections.emptyMap;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.DEFAULT_PARTITION_ID;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_INTERNAL;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_PRINCIPAL;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_PROPERTY;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.PRINCIPAL_VALIDATOR_DEFAULT_VALUE;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.PRINCIPAL_VALIDATOR_PROPERTY;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.runtime.beans.PartitionAwareExtensionsManager.FlushPartitionPredicate;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.spi.security.AlwaysValidPrincipalValidator;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationProvider;
import org.eclipse.stardust.engine.core.spi.security.ExternalUserConfiguration;
import org.eclipse.stardust.engine.core.spi.security.PrincipalValidator;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
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
 * <p>
 * This class tests functionality regarding Principal authentication
 * in a <i>Spring</i> environment, i.e. when {@link SecurityProperties#AUTHENTICATION_MODE_PROPERTY}
 * is set to {@link SecurityProperties#AUTHENTICATION_MODE_PRINCIPAL}.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class SpringPrincipalAuthenticationTest extends AbstractSpringAuthenticationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   /**
    * <p>
    * By default, an instance of {@link AlwaysValidPrincipalValidator} is used for principal evaluation,
    * i.e. all principals are accepted.
    * </p>
    */
   @Test
   public void testInjectingPrincipalEvaluationSucceeds()
   {
      UserHome.create(sf, REGULAR_USER_ID);

      /* do an ordinary login with the regular user ... */
      final UserService userService = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getUserService();

      /* (the user service needs to be decorated to simulate HTTP invocation) */
      addInBetweenInvocationHandler(userService);

      /* ... now explicitly set the principal to an admin user ... */
      currentInvokerPrincipal = new InvokerPrincipal(MOTU, emptyMap());

      setAuthModeToPrincipal();

      /* ... and execute a service call, which should return the user 'motu' */
      final User user = userService.getUser();
      assertThat(user.getAccount(), equalTo(MOTU));

      setAuthModeToInternal();
   }

   /**
    * <p>
    * When cofiguring an instance of {@link AlwaysInvalidPrincipalValidator} all principals are denied.
    * </p>
    */
   @Test
   public void testInjectingPrincipalEvaluationFails()
   {
      UserHome.create(sf, REGULAR_USER_ID);

      /* do an ordinary login with the regular user ... */
      final UserService userService = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getUserService();

      /* (the user service needs to be decorated to simulate HTTP invocation) */
      addInBetweenInvocationHandler(userService);

      /* ... now explicitly set the principal to an admin user ... */
      currentInvokerPrincipal = new InvokerPrincipal(MOTU, emptyMap());

      setAuthModeToPrincipal();
      setAlwaysInvalidPrincipalEvaluator();

      try
      {
         /* ... and execute a service call, which should result in an AccessForbiddenException */
         userService.getUser();
         fail();
      }
      catch (final AccessForbiddenException ignored)
      {
         /* expected */
      }
      finally
      {
         setAuthModeToInternal();
         setAlwaysValidPrincipalEvaluator();
      }
   }

   private void setAuthModeToPrincipal()
   {
      /* we need to flush the partition since the extension provider for DynamicParticipantSynchronizationProvider is cached */
      final FlushPartitionPredicate flushPartition = new FlushPartitionPredicate(DEFAULT_PARTITION_ID);
      ExtensionProviderUtils.forEachExtensionsManager(flushPartition);

      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_PRINCIPAL);
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, DummySyncProvider.class.getName());
   }

   private void setAuthModeToInternal()
   {
      final FlushPartitionPredicate flushPartition = new FlushPartitionPredicate(DEFAULT_PARTITION_ID);
      ExtensionProviderUtils.forEachExtensionsManager(flushPartition);

      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, "None");
   }

   private void setAlwaysInvalidPrincipalEvaluator()
   {
      final GlobalParameters params = GlobalParameters.globals();
      params.set(PRINCIPAL_VALIDATOR_PROPERTY, AlwaysInvalidPrincipalValidator.class.getName());
   }

   private void setAlwaysValidPrincipalEvaluator()
   {
      final GlobalParameters params = GlobalParameters.globals();
      params.set(PRINCIPAL_VALIDATOR_PROPERTY, PRINCIPAL_VALIDATOR_DEFAULT_VALUE);
   }

   public static final class AlwaysInvalidPrincipalValidator implements PrincipalValidator
   {
      @Override
      public boolean isValid(final Principal ignored)
      {
         return false;
      }
   }

   public static final class DummySyncProvider extends DynamicParticipantSynchronizationProvider
   {
      @Override
      public ExternalUserConfiguration provideUserConfiguration(String account)
      {
         return new MotuUserConfiguration();
      }
   }

   private static final class MotuUserConfiguration extends ExternalUserConfiguration
   {
      @Override
      public String getDescription()
      {
         return MOTU;
      }

      @Override
      public String getEMail()
      {
         return MOTU;
      }

      @Override
      public String getFirstName()
      {
         return MOTU;
      }

      @Override
      public String getLastName()
      {
         return MOTU;
      }

      @Override
      public Map<?, ?> getProperties()
      {
         return Collections.emptyMap();
      }
   }
}
