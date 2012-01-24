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

import java.io.IOException;
import java.security.Principal;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CarnotHttpInvokerRequestExecutor implements HttpInvokerRequestExecutor
{
   private static final Logger trace = LogManager.getLogger(CarnotHttpInvokerRequestExecutor.class);

   private HttpInvokerRequestExecutor requestExecutor = new SimpleHttpInvokerRequestExecutor();
   
   private PrincipalProvider principalProvider;

   public HttpInvokerRequestExecutor getRequestExecutor()
   {
      return requestExecutor;
   }

   public void setRequestExecutor(HttpInvokerRequestExecutor requestExecutor)
   {
      this.requestExecutor = requestExecutor;
   }

   public PrincipalProvider getPrincipalProvider()
   {
      return principalProvider;
   }

   public void setPrincipalProvider(PrincipalProvider principalProvider)
   {
      this.principalProvider = principalProvider;
   }

   public RemoteInvocationResult executeRequest(HttpInvokerClientConfiguration config,
         RemoteInvocation invocation) throws IOException, ClassNotFoundException,
         Exception
   {
      Principal principal = (null != principalProvider)
            ? principalProvider.getPrincipal()
            : InvokerPrincipalUtils.getCurrent();
      if (principal instanceof InvokerPrincipal)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Setting principal '" + principal.getName()
                  + "' for invocation of method " + invocation.getMethodName());
         }

         invocation.addAttribute(SpringConstants.ATTR_CARNOT_PRINCIPAL,
               (InvokerPrincipal) principal);
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Not setting principal for invocation of method "
                  + invocation.getMethodName());
         }
      }

      RemoteInvocationResult invocationResult = requestExecutor.executeRequest(config,
            invocation);

      return invocationResult;
   }
}
