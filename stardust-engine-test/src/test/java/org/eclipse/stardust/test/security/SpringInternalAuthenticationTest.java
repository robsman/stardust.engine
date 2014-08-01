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

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
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
 * This class tests functionality regarding internal authentication
 * in a <i>Spring</i> environment, i.e. when {@link SecurityProperties#AUTHENTICATION_MODE_PROPERTY}
 * is set to {@link SecurityProperties#AUTHENTICATION_MODE_INTERNAL}.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class SpringInternalAuthenticationTest extends AbstractSpringAuthenticationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   @Test
   public void testInternalLoginProvidesASignedPrincipal()
   {
      UserHome.create(sf, REGULAR_USER_ID);

      /* do an ordinary login with the regular user ... */
      final UserService userService = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getUserService();

      /* ... and check whether the principal has been signed */
      final LoggedInUser loggedInUser = retrieveLoggedInUser(userService);
      final InvokerPrincipal invokerPrincipal = retrieveInvokerPrincipal(loggedInUser);
      assertThat(invokerPrincipal, notNullValue());
      assertThat(invokerPrincipal.getSignature(), notNullValue());
   }

   /**
    * assumes that {@link #testInternalLoginProvidesASignedPrincipal()} is passing
    */
   @Test
   public void testInjectingValidPrincipalIsPossible()
   {
      UserHome.create(sf, REGULAR_USER_ID);

      /* do an ordinary login with the regular user ... */
      final UserService userService = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getUserService();
      final User userFromRegularCall = userService.getUser();

      /* (the user service needs to be decorated to simulate HTTP invocation) */
      addInBetweenInvocationHandler(userService);

      /* ... now explicitly set the valid principal ... */
      final LoggedInUser loggedInUser = retrieveLoggedInUser(userService);
      final InvokerPrincipal invokerPrincipal = retrieveInvokerPrincipal(loggedInUser);
      currentInvokerPrincipal = new InvokerPrincipal(REGULAR_USER_ID, invokerPrincipal.getProperties(), invokerPrincipal.getSignature());

      /* ... and execute a service call, which should return the same user as returned above */
      final User userFromInjectedPrincipalCall = userService.getUser();

      assertThat(userFromInjectedPrincipalCall, equalTo(userFromRegularCall));
   }

   @Test(expected = AccessForbiddenException.class)
   public void testInjectingForgedPrincipalIsNotPossibleWithoutSignature()
   {
      UserHome.create(sf, REGULAR_USER_ID);

      /* do an ordinary login with the regular user ... */
      final UserService userService = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getUserService();

      /* (the user service needs to be decorated to simulate HTTP invocation) */
      addInBetweenInvocationHandler(userService);

      /* ... now explicitly set the principal to an admin user ... */
      currentInvokerPrincipal = new InvokerPrincipal(MOTU, Collections.emptyMap());

      /* ... and execute a service call, which should result in an AccessForbiddenException */
      userService.getUser();
   }

   private LoggedInUser retrieveLoggedInUser(final UserService userService)
   {
      final InvocationHandler authHandler = Proxy.getInvocationHandler(userService);
      final Object user = Reflect.getFieldValue(authHandler, "user");
      assertThat(user, is(instanceOf(LoggedInUser.class)));

      return (LoggedInUser) user;
   }

   private InvokerPrincipal retrieveInvokerPrincipal(final LoggedInUser loggedInUser)
   {
      final Map<?, ?> userProperties = loggedInUser.getProperties();

      InvokerPrincipal invokerPrincipal = null;
      for (final Object obj : userProperties.values())
      {
         if (obj instanceof InvokerPrincipal)
         {
            invokerPrincipal = (InvokerPrincipal) obj;
            break;
         }
      }

      return invokerPrincipal;
   }
}
