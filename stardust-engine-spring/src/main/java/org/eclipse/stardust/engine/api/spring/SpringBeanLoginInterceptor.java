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
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;
import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;


/**
 * @author fherinean
 * @version $Revision$
 */
public class SpringBeanLoginInterceptor extends AbstractLoginInterceptor
      implements PrincipalProvider
{
   private static final long serialVersionUID = 1L;

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
      return loggedInUser;
   }

   protected void performLogoutCall()
   {
      super.performLogoutCall();

      InvokerPrincipalUtils.removeCurrent();
   }
}
