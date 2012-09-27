/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
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
import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;


/**
 * Data value object wrapping the result of an authentication request.
 * 
 * @author rsauer
 * @version $Revision$
 */
public final class ExternalLoginResult implements Serializable
{
   private final boolean succeeded;
   private final LoginFailedException loginFailedReason;
   
   private final boolean overridesProperties;
   private final Map properties;

   private static final ExternalLoginResult LOGIN_SUCCESS = new ExternalLoginResult(true, null);

   /**
    * Testifies a successful authentication.
    * 
    * @return The sucess ticket.
    */
   public static final ExternalLoginResult testifySuccess()
   {
      return LOGIN_SUCCESS;
   }

   /**
    * Testifies a successful authentication.
    * 
    * @param properties        The modified properties map. <code>null</code> results in 
    *                          an empty map.
    *                          
    * @return The sucess ticket.
    */
   public static final ExternalLoginResult testifySuccess(Map properties)
   {
      return new ExternalLoginResult(true, null, properties);
   }

   /**
    * Testifies a failed authentification.
    * 
    * @param reason The reason the authentication went fail.
    * @return The failure ticket.
    */
   public static final ExternalLoginResult testifyFailure(LoginFailedException reason)
   {
      if(reason == null)
      {
         StringBuilder sb = new StringBuilder();
         sb.append("LoginFailedException");
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise(sb.toString()));         
      }      
      
      return new ExternalLoginResult(false, reason);
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
   private ExternalLoginResult(boolean succeeded, LoginFailedException loginFailedReason)
   {
      this.succeeded = succeeded;
      this.loginFailedReason = loginFailedReason;
      
      this.overridesProperties = false;
      this.properties = Collections.EMPTY_MAP;
   }
   
   /**
    * Constructs a new instance.
    * 
    * @param succeeded         The success indicator.
    * @param loginFailedReason The reason the authentication went fail. May be
    *                          <code>null</code> if the authentication succeeded.
    * @param properties        The modified properties map. <code>null</code> results in 
    *                          an empty map.
    *                          
    * @see #testifySuccess
    * @see #testifyFailure
    */
   private ExternalLoginResult(boolean succeeded, LoginFailedException loginFailedReason,
         Map properties)
   {
      this.succeeded = succeeded;
      this.loginFailedReason = loginFailedReason;
      
      this.overridesProperties = true;
      this.properties = null == properties ? Collections.EMPTY_MAP : properties;
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
   
   /**
    * Queries for the existence of overiding properties. 
    * These can be accessed by {@link #getProperties()}.
    * 
    * @return The existence of an overiding properties.
    */
   public boolean isOverridingProperties()
   {
      return overridesProperties;
   }
   
   /**
    * Queries for the overiding properties. The map may be empty.
    * 
    * @return The overiding properties.
    */
   public Map getProperties()
   {
      return Collections.unmodifiableMap(properties);
   }
}
