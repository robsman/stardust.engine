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
package org.eclipse.stardust.engine.core.security.jaas;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.security.authentication.DefaultCallbackHandler;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginProvider;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginResult;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class JaasLoginService implements ExternalLoginProvider
{
   /**
    * The default value for the configuration name property
    */
   private static final String DEFAULT_JAAS_CONFIG_NAME = "CARNOT";

   private LoginContext loginContext;
   private CallbackHandler callbackHandler;
   private Subject subject;

   private Principal replicatedPrincipal;
   private String password;
   private String account;

   public ExternalLoginResult login(String id, String password, Map properties)
   {
      // TODO (kafka) use properties
      
      this.account = id;
      this.password = password;

      callbackHandler = new DefaultCallbackHandler(account, this.password.toCharArray());

      String configName = Parameters.instance().getString(SecurityProperties
            .AUTHENTICATION_CONFIGURATION_NAME_PROPERTY, DEFAULT_JAAS_CONFIG_NAME);

      try
      {
         loginContext = new LoginContext(configName, callbackHandler);
      }
      catch (LoginException e)
      {
         return ExternalLoginResult.testifyFailure(new LoginFailedException(
               e.getMessage(), LoginFailedException.SYSTEM_ERROR));
      }
      try
      {
         loginContext.login();

         subject = loginContext.getSubject();

         String principalClassName = Parameters.instance().getString(SecurityProperties
               .AUTHENTICATION_PRINCIPAL_CLASS_PROPERTY);

         Set principals = null;
         if (principalClassName != null)
         {
            Class principalClass = Reflect.getClassFromClassName(principalClassName);

            principals = subject.getPrincipals(principalClass);
         }
         else
         {
            principals = subject.getPrincipals();
         }
         for (Iterator i = principals.iterator(); i.hasNext();)
         {
            replicatedPrincipal = (Principal) i.next();
            break;
         }

         if (replicatedPrincipal == null)
         {
            throw new InternalException(
                  "No principal can be obtained for filter class.");
         }

         return ExternalLoginResult.testifySuccess();
      }
      catch (AccountExpiredException e)
      {
         return ExternalLoginResult.testifyFailure(new LoginFailedException(
               e.getMessage(), LoginFailedException.ACCOUNT_EXPIRED));
      }
      catch (CredentialExpiredException e)
      {
         return ExternalLoginResult.testifyFailure(new LoginFailedException(
               e.getMessage(), LoginFailedException.ACCOUNT_EXPIRED));
      }
      catch (FailedLoginException e)
      {
         return ExternalLoginResult.testifyFailure(new LoginFailedException(
               e.getMessage(), LoginFailedException.INVALID_PASSWORD));
      }
      catch (LoginException e)
      {
         return ExternalLoginResult.testifyFailure(new LoginFailedException(
               e.getMessage(), LoginFailedException.UNKNOWN_REASON));
      }
   }
}
