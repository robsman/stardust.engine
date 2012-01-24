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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ForkingDebugInterceptor implements MethodInterceptor
{

   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ForkingDebugInterceptor.class);

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      if (trace.isInfoEnabled())
      {
         String description;
         try
         {
            description = invocation.getArguments()[0].toString();
         }
         catch(Exception e)
         {
            trace.warn("", e);
            description = "unknown";
         }
         
         trace.info("--> forked: " + description);
      }

      Object result =  invocation.proceed();

      if (trace.isInfoEnabled())
      {
         trace.info("<-- forked.");
      }

      return result;
   }
}
