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
package org.eclipse.stardust.engine.core.runtime.beans.interceptors;

import java.security.Principal;
import java.util.Map;

import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;


/**
 * @author rsauer
 * @version $Revision$
 */
public class J2eeSecurityLoginInterceptor extends LoginInterceptor
      implements PrincipalProvider
{
   private static final long serialVersionUID = 1L;

   private transient Principal principal;

   public J2eeSecurityLoginInterceptor(Principal principal)
   {
      super();

      this.principal = principal;
   }

   public Principal getPrincipal()
   {
      return principal;
   }

   public Map getAuthenticationProperties()
   {
      // TODO support custom properties
      return null;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      final boolean usePrincipal = SecurityProperties.isPrincipalBasedLogin(invocation.getParameters());

      final PropertyLayer props = usePrincipal //
            ? PropertyLayerProviderInterceptor.getCurrent()
            : null;

      Object backup = null;
      try
      {
         if (usePrincipal)
         {
            backup = props.get(SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY);
            props.setProperty(
                  SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY, this);
         }

         return super.invoke(invocation);
      }
      finally
      {
         if (usePrincipal)
         {
            props.setProperty(
                  SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY, backup);
         }
      }
   }

}
