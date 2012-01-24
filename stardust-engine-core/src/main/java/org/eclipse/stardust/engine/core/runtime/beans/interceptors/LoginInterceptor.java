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

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LoginInterceptor extends AbstractLoginInterceptor
{
   private LoggedInUser loggedInUser;
   
   protected boolean isStatefulService()
   {
      return true;
   }

   protected Object performCall(MethodInvocation invocation, LoggedInUser loggedInUser)
         throws Throwable
   {
      return super.performCall(invocation, (null != loggedInUser)
            ? loggedInUser
            : this.loggedInUser);
   }

   protected LoggedInUser performLoginCall(MethodInvocation invocation)
   {
      LoggedInUser loggedInUser = super.performLoginCall(invocation);
      
      if (isStatefulService())
      {
         this.loggedInUser = loggedInUser;
      }
      else
      {
         Assert.isNull(this.loggedInUser,
               "loggedInUser field must be null for stateless deployments.");
      }

      return loggedInUser;
   }

   protected void performLogoutCall()
   {
      super.performLogoutCall();

      if (isStatefulService())
      {
         this.loggedInUser = null;
      }
      else
      {
         Assert.isNull(this.loggedInUser,
               "loggedInUser field must be null for stateless deployments.");
      }
   }
}