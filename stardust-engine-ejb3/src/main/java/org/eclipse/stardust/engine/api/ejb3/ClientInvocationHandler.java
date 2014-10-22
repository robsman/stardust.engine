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
package org.eclipse.stardust.engine.api.ejb3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.Ejb3ManagedService;
import org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;



/**
 * Default client invocation handler responsible for unwrapping WorkflowExceptions.
 *
 * @author ubirkemeyer
 * @version $Revision: 60059 $
 */
public class ClientInvocationHandler implements InvocationHandler, Serializable
{
   private static final long serialVersionUID = 2L;

   private static final Logger trace = LogManager.getLogger(ClientInvocationHandler.class);

   private transient Object inner;

   private transient TunneledContext tunneledContext;

   public ClientInvocationHandler(Object inner, TunneledContext tunneledContext)
   {
      this.inner = inner;
      this.tunneledContext = tunneledContext;

      if ((null != tunneledContext) && !(inner instanceof Ejb3ManagedService))
      {
         trace.warn("Found tunneling context but EJB facade does not seem to support tunneling.");
      }
   }

   /**
    * Serialization method to restore the inner state.
    */
   private void readObject(ObjectInputStream stream) throws IOException,
         ClassNotFoundException
   {
      Serializable serializable = (Serializable) stream.readObject();
      if (serializable instanceof Handle)
      {
         inner = ((Handle) serializable).getEJBObject();
      }
      else
      {
         inner = serializable;
      }
/*      if (inner instanceof Stub)
      {
         ((Stub) inner).connect(ORB.init());
      }*/

      // restore tunneled context
      this.tunneledContext = (TunneledContext) stream.readObject();
   }

   /**
    * Serialization method to save the inner state.
    *
    * @serialData The Handle if inner is an EJBObject,
    *             the inner itself if it is serializable or null.
    */
   private void writeObject(java.io.ObjectOutputStream stream) throws IOException
   {
      Serializable serializable = null;
      if (inner instanceof EJBObject)
      {
         serializable = ((EJBObject) inner).getHandle();
      }
      else if (inner instanceof Serializable)
      {
         serializable = (Serializable) inner;
      }
      stream.writeObject(serializable);

      // save tunneled context
      stream.writeObject(tunneledContext);
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      final Class<?>[] paramTypes;
      final Object[] paramValues;

      if (null != tunneledContext
            && !("remove".equals(method.getName()) && (null == args || args.length == 0)))
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Tunneling invocation of method " + method);
         }

         // extend the argument type list by TunneledContext
         paramTypes = new Class[method.getParameterTypes().length + 1];
         System.arraycopy(method.getParameterTypes(), 0, paramTypes, 0, method
               .getParameterTypes().length);
         paramTypes[paramTypes.length - 1] = TunneledContext.class;

         // add the tunneled context to argument list
         Object[] tmpArgs = args;
         if (null == tmpArgs)
         {
            tmpArgs = new Object[0];
         }
         paramValues = new Object[tmpArgs.length + 1];
         System.arraycopy(tmpArgs, 0, paramValues, 0, tmpArgs.length);
         paramValues[paramValues.length - 1] = tunneledContext;
      }
      else
      {
         paramTypes = method.getParameterTypes();
         paramValues = args;
      }

      // Update the tunnelingContext's principal if needed.
      updateTunnelingContext();

      // work for florin
      // bug #3137: check what exception is coming and why it's incorrectly unwrapped
      Method innerMethod = inner.getClass().getMethod(method.getName(), paramTypes);
      try
      {
         return innerMethod.invoke(inner, paramValues);
      }
      catch (Throwable t)
      {
         throw unwrapException(t);
      }
   }

   private void updateTunnelingContext()
   {
      if (tunneledContext != null)
      {
         InvokerPrincipal outer = InvokerPrincipalUtils.getCurrent();
         InvokerPrincipal inner = tunneledContext.getInvokerPrincipal();

         InvokerPrincipal principal = LoginUtils.getReauthenticationPrincipal(outer,
               inner);
         if (principal != null
               && principal.getProperties().containsKey(
                     AbstractLoginInterceptor.REAUTH_USER_ID))
         {
            tunneledContext = new TunneledContext(principal);
         }
      }
   }

   public static ApplicationException unwrapException(Throwable source)
   {
      while (!(source instanceof ApplicationException))
      {
         Throwable cause = source.getCause();
         if (source instanceof UndeclaredThrowableException)
         {
            trace.warn("Undeclared throwable: ", cause);
         }
         if (cause == null)
         {
            throw new InternalException(source.getMessage(), source);
         }
         source = cause;
      }
      return (ApplicationException) source;
   }
}
