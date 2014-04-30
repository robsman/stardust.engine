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
package org.eclipse.stardust.engine.api.spring;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.security.Principal;
import java.util.HashMap;

import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;


/**
 * @author fherinean
 * @version $Revision$
 */
public class SpringBeanLoginInterceptor extends AbstractLoginInterceptor
      implements PrincipalProvider
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(SpringBeanLoginInterceptor.class);

   private final AbstractSpringServiceBean serviceBean;

   public SpringBeanLoginInterceptor(AbstractSpringServiceBean serviceBean)
   {
      this.serviceBean = serviceBean;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      PrincipalProvider principalProvider;

      if (null != serviceBean.getPrincipalProvider())
      {
         principalProvider = serviceBean.getPrincipalProvider();
      }
      else
      {
         InvokerPrincipal principal = InvokerPrincipalUtils.getCurrent();

         boolean usePrincipal = (null != principal)
               && !isLoginCall(invocation.getMethod())
               && !isLogoutCall(invocation.getMethod());

         if (usePrincipal)
         {
            principalProvider = this;
            if (SecurityProperties.isInternalAuthentication())
            {
               boolean ok = InvokerPrincipalUtils.checkPrincipalSignature(principal);
               if ( !ok)
               {
                  trace.warn("The signature for principal '" + principal + "' is corrupt.");
                  throw new AccessForbiddenException(BpmRuntimeError.AUTHx_NOT_LOGGED_IN.raise());
               }
            }
         }
         else
         {
            principalProvider = null;
         }
      }

      final PropertyLayer props = PropertyLayerProviderInterceptor.getCurrent();
      final Object backup = props.get(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY);

      try
      {
         props.setProperty(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY,
               principalProvider);

         return super.invoke(invocation);
      }
      finally
      {
         props.setProperty(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY,
               backup);
      }
   }

   public Principal getPrincipal()
   {
      return InvokerPrincipalUtils.getCurrent();
   }

   protected LoggedInUser performLoginCall(MethodInvocation invocation)
   {
      LoggedInUser loggedInUser = super.performLoginCall(invocation);

      if (null != loggedInUser)
      {
         InvokerPrincipal signedPrincipal = InvokerPrincipalUtils.generateSignedPrincipal(
               loggedInUser.getUserId(), loggedInUser.getProperties());

         InvokerPrincipalUtils.setCurrent(signedPrincipal);

         HashMap<Object, Object> enrichedProperties = newHashMap();
         enrichedProperties.putAll(loggedInUser.getProperties());
         enrichedProperties.put(InvokerPrincipal.PRP_SIGNED_PRINCIPAL, signedPrincipal);
         loggedInUser = new LoggedInUser(loggedInUser.getUserId(), enrichedProperties);
      }

      return loggedInUser;
   }

   protected void performLogoutCall()
   {
      super.performLogoutCall();

      InvokerPrincipalUtils.removeCurrent();
   }

}
