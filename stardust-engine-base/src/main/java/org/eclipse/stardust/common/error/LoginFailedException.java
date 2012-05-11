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
package org.eclipse.stardust.common.error;


/**
 * This exception is thrown by the authentication framework due to various exceptions.
 * Always carries a reason code as an integer
 * 
 * @author swoelk
 * @version $Revision$
 */
public class LoginFailedException extends PublicException
{
   public static final int MAXIMUM_NUMBER_OF_RETRIES_EXCEEDED = 0;
   public static final int INVALID_PASSWORD = 1;
   public static final int INVALID_USER = 2;
   public static final int UNKNOWN_REALM = 3;
   public static final int UNKNOWN_DOMAIN = 4;
   public static final int UNKNOWN_PARTITION = 7;
   public static final int PASSWORD_EXPIRED = 8;
   public static final int DISABLED_USER = 9;

   /**
    * Thrown, e.g. if the login expects the login user to be administrator and
    * the login user is not assigned to this role.
    */
   public static final int AUTHORIZATION_FAILURE = 5;
   public static final int ACCOUNT_EXPIRED = 6;

   public static final int LOGIN_CANCELLED = -1;

   public static final int SYSTEM_ERROR = 100;
   public static final int UNKNOWN_REASON = 200;

   private int reason;

   /**
   * @deprecated
   */
   public LoginFailedException(String message, int reason)
   {
      super(message);

      this.reason = reason;
   }

  /**
  *
  */
 public LoginFailedException(ErrorCase errorCase, int reason)
 {
    super(errorCase);

    this.reason = reason;
 }

   /**
    *
    */
   public int getReason()
   {
      return reason;
   }
}
