/**********************************************************************************
 * Copyright (c) 2014, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.security;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.spring.CarnotHttpInvokerRequestExecutor;
import org.eclipse.stardust.engine.api.spring.CarnotRemoteInvocationExecutor;
import org.eclipse.stardust.engine.api.spring.SpringServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;

/**
 * <p>
 * Base class for security tests holding some
 * common methods and classes.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class AbstractSpringAuthenticationTest
{
   protected static final String REGULAR_USER_ID = "hans";
   protected static final String REGULAR_USER2_ID = "dampf";

   protected static InvokerPrincipal currentInvokerPrincipal;

   protected void addInBetweenInvocationHandler(final UserService userService)
   {
      final String userServiceFieldName = "bean";

      final InvocationHandler invocationHandler = Proxy.getInvocationHandler(userService);
      final UserService plainUserService = (UserService) Reflect.getFieldValue(invocationHandler, userServiceFieldName);
      final UserService decoratedUserService = createInBetweenInvocationHandlerProxy(plainUserService);
      Reflect.setFieldValue(invocationHandler, userServiceFieldName, decoratedUserService);
   }

   protected UserService createInBetweenInvocationHandlerProxy(final UserService plainUserService)
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
   protected static final class InBetweenInvocationHandler implements InvocationHandler
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
               InvokerPrincipalUtils.setCurrent(currentInvokerPrincipal);
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
