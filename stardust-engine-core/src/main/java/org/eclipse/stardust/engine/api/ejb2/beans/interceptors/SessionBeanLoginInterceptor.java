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
package org.eclipse.stardust.engine.api.ejb2.beans.interceptors;

import java.rmi.RemoteException;
import java.security.Principal;

import javax.ejb.EJBContext;

import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.J2eeSecurityLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalProvider;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;



/**
 * @author fherinean
 * @version $Revision$
 */
public class SessionBeanLoginInterceptor extends J2eeSecurityLoginInterceptor
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger trace = LogManager.getLogger(SessionBeanLoginInterceptor.class);

   private final EJBContext context;
   
   private final boolean stateless;

   public SessionBeanLoginInterceptor(EJBContext context)
   {
      super(null);

      this.context = context;

      boolean isStateless = false;
      if (null != context)
      {
         try
         {
            isStateless = context.getEJBHome().getEJBMetaData().isStatelessSession();
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
      
      this.stateless = isStateless;
   }

   protected boolean isStatefulService()
   {
      return !stateless;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      final boolean useInvokerPrincipal = (null != InvokerPrincipalUtils.getCurrent());

      final PropertyLayer props = useInvokerPrincipal //
            ? PropertyLayerProviderInterceptor.getCurrent()
            : null;

      Object backup = null;
      try
      {
         if (useInvokerPrincipal)
         {
            // make sure the thread local invoker principal is available during duration of call
            backup = props.get(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY);
            props.setProperty(
                  SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY,
                  InvokerPrincipalProvider.INTANCE);
         }

         // proceed with default handling
         return super.invoke(invocation);
      }
      finally
      {
         if (useInvokerPrincipal)
         {
            props.setProperty(
                  SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY, backup);
         }
      }
   }

   public Principal getPrincipal()
   {
      Principal result = null;

      // NOTE the try..catch is to allow uniform handling of nonconfigured principal based
      // login, i.e. JBoss is throwing it's own exception instead of returning null.
      try
      {
         result = context.getCallerPrincipal();
      }
      catch (Exception ex)
      {
         result = null;
      }

      return result;
   }

}
