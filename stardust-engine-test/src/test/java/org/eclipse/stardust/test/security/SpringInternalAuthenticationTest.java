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

import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.spring.CarnotHttpInvokerRequestExecutor;
import org.eclipse.stardust.engine.api.spring.CarnotRemoteInvocationExecutor;
import org.eclipse.stardust.engine.api.spring.InvokerPrincipal;
import org.eclipse.stardust.engine.api.spring.InvokerPrincipalUtils;
import org.eclipse.stardust.engine.api.spring.SpringServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

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
public class SpringInternalAuthenticationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String REGULAR_USER_ID = "hans";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private static InvokerPrincipal CURRENT_INVOKER_PRINCIPAL;

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   @Test
   public void testInternalLoginProvidesASignedPrincipal()
   {
      UserHome.create(sf, REGULAR_USER_ID);

      UserService userService = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getUserService();

      InvocationHandler authHandler = Proxy.getInvocationHandler(userService);
      Object user = Reflect.getFieldValue(authHandler, "user");

      assertThat(user, is(instanceOf(LoggedInUser.class)));

      LoggedInUser loggedInUser = (LoggedInUser) user;

      Map< ? , ? > userProperties = (Map< ? , ? >) loggedInUser.getProperties();
      assertThat(userProperties, hasValue( //
            both(instanceOf(InvokerPrincipal.class)) //
                  .and(hasProperty("signature", notNullValue()))));
   }

   @Test(expected = AccessForbiddenException.class)
   public void testInjectingForgedPrincipalIsNotPossibleWithoutSecretSet()
   {
      doTestInjectingForgedPrincipalIsNotPossible();
   }

   @Test(expected = AccessForbiddenException.class)
   public void testInjectingForgedPrincipalIsNotPossibleWithSecretSet()
   {
      Parameters.instance().set(SecurityProperties.PRINCIPAL_SECRET, "fnweufnweufnwafuwenfuwenmfwenmfuwenf");

      doTestInjectingForgedPrincipalIsNotPossible();
   }

   private void doTestInjectingForgedPrincipalIsNotPossible()
   {
      UserHome.create(sf, REGULAR_USER_ID);

      /* do an ordinary login with the regular user ... */
      final UserService userService = ServiceFactoryLocator.get(REGULAR_USER_ID, REGULAR_USER_ID).getUserService();
      /* (the user service needs to be decorated to simulate HTTP invocation) */
      addInBetweenInvocationHandler(userService);
      /* ... now explicitly set the principal to an admin user ... */
      CURRENT_INVOKER_PRINCIPAL = new InvokerPrincipal(MOTU, Collections.emptyMap(), null);
      /* ... and execute a service call, which should result in an AccessForbiddenException */
      userService.getUser();
   }

   private void addInBetweenInvocationHandler(final UserService userService)
   {
      final String userServiceFieldName = "bean";

      final InvocationHandler invocationHandler = Proxy.getInvocationHandler(userService);
      final UserService plainUserService = (UserService) Reflect.getFieldValue(invocationHandler, userServiceFieldName);
      final UserService decoratedUserService = createInBetweenInvocationHandlerProxy(plainUserService);
      Reflect.setFieldValue(invocationHandler, userServiceFieldName, decoratedUserService);
   }

   private UserService createInBetweenInvocationHandlerProxy(final UserService plainUserService)
   {
      final ClassLoader cl = getClass().getClassLoader();
      final Class<?>[] interfaces = new Class[] { UserService.class, ManagedService.class};
      final InvocationHandler invocationHandler = new InBetweenInvocationHandler(plainUserService);
      return (UserService) Proxy.newProxyInstance(cl, interfaces, invocationHandler);
   }

   /**
    * <p>
    * This invocation handler runs between the client-side invocation handler {@link SpringServiceFactory#SpringServiceInvocationHandler}
    * and the actual service call.
    * </p>
    *
    * <p>
    * The handler sets the {@link InvokerPrincipalUtils}'s thread local to the principal set in the client code
    * to simulate the behavior during an HTTP call:
    * <ol>
    *    <li>client-side: {@link CarnotHttpInvokerRequestExecutor} augments the request with the {@link InvokerPrincipal}
    *        set in the thread local in {@link InvokerPrincipalUtils}</li>
    *    <li>server-side: {@link CarnotRemoteInvocationExecutor} obtains the {@link InvokerPrincipal} from the request and
    *        puts it back in the thread local in {@link InvokerPrincipalUtils}</li>
    * </ol>
    * By doing so, the client-side invocation handler in {@link SpringServiceFactory}, which removes the set principal
    * from the thread local {@link InvokerPrincipalUtils}, can be circumvented, since the server-side handler sets the
    * {@link InvokerPrincipalUtils}'s thread local back to the value set on the client-side.
    * </p>
    */
   private static final class InBetweenInvocationHandler implements InvocationHandler
   {
      private final UserService userService;

      public InBetweenInvocationHandler(final UserService userService)
      {
         this.userService = userService;
      }

      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
      {
         final InvokerPrincipal current = InvokerPrincipalUtils.getCurrent();
         try
         {
            if (current != null)
            {
               InvokerPrincipalUtils.setCurrent(CURRENT_INVOKER_PRINCIPAL);
            }

            return method.invoke(userService, args);
         }
         catch (InvocationTargetException e)
         {
            final Throwable t = e.getTargetException();
            throw t;
         }
         finally
         {
            if (current != null)
            {
               InvokerPrincipalUtils.setCurrent(current);
            }
         }
      }
   }
}
