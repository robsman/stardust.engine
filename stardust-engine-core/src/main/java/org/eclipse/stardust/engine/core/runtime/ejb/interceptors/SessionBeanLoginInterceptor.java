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
package org.eclipse.stardust.engine.core.runtime.ejb.interceptors;

import java.rmi.RemoteException;
import java.security.Principal;

import javax.ejb.SessionContext;

import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.J2eeSecurityLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalProvider;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;

/**
 * @author fherinean
 * @version $Revision: 52592 $
 */
public class SessionBeanLoginInterceptor extends J2eeSecurityLoginInterceptor
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(SessionBeanLoginInterceptor.class);

   private SessionContext context;
   private boolean stateless;

   public SessionBeanLoginInterceptor(SessionContext context, boolean checkStateless)
   {
      super(null);
      this.context = context;

      if (checkStateless)
      {
         stateless = false;
         if (null != context)
         {
            try
            {
               stateless = context.getEJBHome().getEJBMetaData().isStatelessSession();
            }
            catch (IllegalStateException ise)
            {
               trace.warn("Failed to determine if EJB is stateful or stateless, assuming stateful.", ise);
            }
            catch (RemoteException re)
            {
               trace.warn("Failed to determine if EJB is stateful or stateless, assuming stateful.", re);
            }
         }
         else
         {
            trace.warn("Failed to determine if EJB is stateful or stateless due to an unavailable EJB context, assuming stateful.");
         }
      }
      else
      {
         stateless = true;
      }
   }

   protected boolean isStatefulService()
   {
      return !stateless;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      InvokerPrincipal invokerPrincipal = InvokerPrincipalUtils.getCurrent();
      final boolean useInvokerPrincipal = (null != invokerPrincipal
            && invokerPrincipal.getProperties() != null && !invokerPrincipal.getProperties()
            .containsKey(AbstractLoginInterceptor.REAUTH_OUTER_PRINCIPAL));


      PropertyLayer props = useInvokerPrincipal
            ? PropertyLayerProviderInterceptor.getCurrent()
            : null;

      Object backup = null;
      try
      {
         if (useInvokerPrincipal)
         {
            // make sure the thread local invoker principal is available during duration of call
            backup = props.get(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY);
            props.setProperty(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY,
                  InvokerPrincipalProvider.INSTANCE);
         }

         // proceed with default handling
         return super.invoke(invocation);
      }
      finally
      {
         if (useInvokerPrincipal)
         {
            props.setProperty(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY, backup);
         }
      }
   }

   protected boolean useInvokerPrincipal(MethodInvocation invocation)
   {
      return InvokerPrincipalUtils.getCurrent() != null
            && !isLoginCall(invocation.getMethod())
            && !isLogoutCall(invocation.getMethod());
   }

   public Principal getPrincipal()
   {
      // NOTE the try..catch is to allow uniform handling of non configured principal based
      // login, i.e. JBoss is throwing it's own exception instead of returning null.
      try
      {
         return context.getCallerPrincipal();
      }
      catch (Exception ex)
      {
      }
      return null;
   }
}
