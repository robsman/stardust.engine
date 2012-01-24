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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationExecutor;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CarnotRemoteInvocationExecutor implements RemoteInvocationExecutor
{
   private static final Logger trace = LogManager.getLogger(CarnotRemoteInvocationExecutor.class);
   
   private RemoteInvocationExecutor executor = new DefaultRemoteInvocationExecutor();

   public Object invoke(RemoteInvocation invocation, Object targetObject)
         throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
   {
      boolean setUserId = false;

      InvokerPrincipal outerPrincipal = null;
      try
      {
         boolean loggingIn = AbstractLoginInterceptor.METHODNAME_LOGIN.equals(invocation.getMethodName());

         Serializable userId = invocation.getAttribute("carnot:userId");
         
         if ( !loggingIn && ((null == userId) || "".equals(userId)))
         {
            String defaultUserId = Parameters.instance().getString(
                  "Carnot.Spring.Remoting.DefaultUserId");
            if ( !StringUtils.isEmpty(defaultUserId))
            {
               userId = defaultUserId;
               trace.info("Falling back to default User ID '" + userId
                     + "' for invocation of method " + invocation.getMethodName());
            }
         }

         outerPrincipal = InvokerPrincipalUtils.getCurrent();

         Object principal = invocation.getAttribute(SpringConstants.ATTR_CARNOT_PRINCIPAL);
         
         if (principal instanceof InvokerPrincipal)
         {
            // TODO remove
            //SpringRemoteUserIdentityInterceptor.setUserId(((InvokerPrincipal) principal).getName());

            InvokerPrincipalUtils.setCurrent((InvokerPrincipal) principal);
            
            if (trace.isDebugEnabled())
            {
               trace.debug("Setting User ID '" + userId + "' for invocation of method "
                     + invocation.getMethodName());
            }
         }
         else
         {
            InvokerPrincipalUtils.removeCurrent();

            if (trace.isDebugEnabled())
            {
               trace.debug("Not setting User ID for invocation of method "
                     + invocation.getMethodName());
            }
         }
         
         return executor.invoke(invocation, targetObject);
      }
      finally
      {
         if (null != outerPrincipal)
         {
            InvokerPrincipalUtils.setCurrent(outerPrincipal);
         }
         else
         {
            InvokerPrincipalUtils.removeCurrent();
         }
         
         if (setUserId)
         {
            //SpringRemoteUserIdentityInterceptor.resetUserId();
         }
      }
   }
}
