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
package org.eclipse.stardust.engine.core.spi.security;

import java.io.Serializable;

import org.eclipse.stardust.common.error.LoginFailedException;


/**
 * Data value object wrapping the result of an authentication request.
 * 
 * @author rsauer
 * @version $Revision$
 */
public final class LoginResult implements Serializable
{
   private final boolean succeeded;
   private final LoginFailedException loginFailedReason;

   private static final LoginResult LOGIN_SUCCESS = new LoginResult(true, null);
   //private long oid;

   /**
    * Testifies a successful authentication.
    * 
    * @return The sucess ticket.
    */
   public static final LoginResult testifySuccess()
   {
      return LOGIN_SUCCESS;
   }

   /**
    * Testifies a failed authentification.
    * 
    * @param reason The reason the authentication went fail.
    * @return The failure ticket.
    */
   public static final LoginResult testifyFailure(LoginFailedException reason)
   {
      return new LoginResult(false, reason);
   }

   /**
    * Constructs a new instance.
    * 
    * @param succeeded         The success indicator.
    * @param loginFailedReason The reason the authentication went fail. May be
    *                          <code>null</code> if the authentication succeeded.
    * @see #testifySuccess
    * @see #testifyFailure
    */
   private LoginResult(boolean succeeded, LoginFailedException loginFailedReason)
   {
      this.succeeded = succeeded;
      this.loginFailedReason = loginFailedReason;
   }

   /**
    * Queries for success.
    * 
    * @return The success indicator.
    */
   public boolean wasSuccessful()
   {
      return succeeded;
   }

   /**
    * Queries for the reason the authentification failed.
    * 
    * @return The reason for failure.
    */
   public LoginFailedException getLoginFailedReason()
   {
      return loginFailedReason;
   }
}
