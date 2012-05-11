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
package org.eclipse.stardust.engine.core.security.audittrail;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.common.security.utils.SecurityUtils;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginProvider;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginResult;



/**
 * This login module authenticates users directly against
 * the users managed in the CARNOT audit trail database.
 * <p>
 * If a user successfully authenticates itself,
 * a <code>DefaultPrincipal</code> with the login user's username
 * is added to the subject.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AuditTrailLoginService implements ExternalLoginProvider
{
   public static final Logger trace = LogManager
         .getLogger(AuditTrailLoginService.class);

   private String account;
   private String password;
   private UserBean user;

   /**
    * default value for the number of login retries
    */
   private static final int DEFAULT_MAX_LOGIN_RETRIES = 3;

   /**
    * default value for the login invalidation time in minutes
    */
   private static final int DEFAULT_LOGIN_INVALIDATION_TIME = 1;

   /**
    *
    */
   private int invalidationTime = 0;

   /**
    *
    */
   private int maximumNumberLoginRetries;

   public ExternalLoginResult login(String id, String password, Map properties)
   {
      this.account= id;
      this.password = password;

      maximumNumberLoginRetries = Parameters.instance().getInteger(
            SecurityProperties.MAXIMUM_LOGIN_RETRIES_PROPERTY, DEFAULT_MAX_LOGIN_RETRIES);

      invalidationTime = Parameters.instance().getInteger(
            SecurityProperties.INVALIDATION_TIME_PROPERTY, DEFAULT_LOGIN_INVALIDATION_TIME);
      try
      {
         if ((this.password == null) || (this.password.length() == 0))
         {
            throw new LoginFailedException(
                  BpmRuntimeError.AUTHx_USER_PASSWORD_NOT_VALID.raise(getRealmQualifiedUserId(
                        account, properties)), LoginFailedException.INVALID_PASSWORD);
         }

         try
         {
            Parameters params = Parameters.instance();
            try
            {
               PropertyLayer props = ParametersFacade.pushLayer(params,
                     Collections.EMPTY_MAP);
               props.setProperty(SynchronizationService.PRP_DISABLE_SYNCHRONIZATION,
                     Boolean.TRUE.toString());
               
               user = (UserBean) LoginUtils.findLoginUser(account, properties);
            }
            finally
            {
               ParametersFacade.popLayer(params);
            }
         }
         catch (ObjectNotFoundException e)
         {
            throw new LoginFailedException(
                  BpmRuntimeError.AUTHx_USER_PASSWORD_NOT_VALID.raise(getRealmQualifiedUserId(
                        account, properties)), LoginFailedException.INVALID_USER);
         }

         long failedLoginCount = user.getFailedLoginCount();

         if ( LoginUtils.isUserExpired(user))
         {
            throw LoginUtils.createAccountExpiredException(user);
         }

         if (!user.checkPassword(this.password))
         {
            if (!Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
            {
               user.setFailedLoginCount(++failedLoginCount);

               if (maximumNumberLoginRetries != 0 &&
                     failedLoginCount >= maximumNumberLoginRetries)
               {
                  if (invalidationTime == 0)
                  {
                     user.setValidTo(new Date());
                  }
                  else
                  {
                     Date newValidFrom=new Date(
                           System.currentTimeMillis() + (invalidationTime * 1000 * 60));

                     if (user.getValidTo() != null && user.getValidTo().before(newValidFrom))
                     {
                        user.setValidFrom(new Date());
                     }
                     else
                     {
                        user.setValidFrom(newValidFrom);
                     }
                  }

                  user.setFailedLoginCount(0);

                  throw new LoginFailedException(
                        BpmRuntimeError.AUTHx_USER_TEMPORARILY_INVALIDATED.raise(getRealmQualifiedUserId(
                              account, properties)),
                        LoginFailedException.MAXIMUM_NUMBER_OF_RETRIES_EXCEEDED);
               }
            }

            throw new LoginFailedException(
                  BpmRuntimeError.AUTHx_USER_PASSWORD_NOT_VALID.raise(getRealmQualifiedUserId(
                        account, properties)), LoginFailedException.INVALID_PASSWORD);
         }
         
         
         if (!Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
         {
            if ( !LoginUtils.isLoginUserWithoutTimestamp(user))
            {
               user.setLastLoginTime(Calendar.getInstance().getTime());
               SecurityUtils.updatePasswordHistory(user, password);
               if(SecurityUtils.isPasswordExpired(user))
               {
                  if(SecurityUtils.isUserDisabled(user))
                  {
                     throw new LoginFailedException(
                           BpmRuntimeError.AUTHx_USER_DISABLED_BY_PW_RULES.raise(getRealmQualifiedUserId(
                                 account, properties)),
                           LoginFailedException.DISABLED_USER);
                  }
                  
                  throw new LoginFailedException(
                        BpmRuntimeError.AUTHx_USER_ID_PASSWORD_EXPIRED.raise(getRealmQualifiedUserId(
                              account, properties)),
                        LoginFailedException.PASSWORD_EXPIRED);
               }               
            }
            user.setFailedLoginCount(0);
         }
         
         trace.debug("logged in successfully.");

         return ExternalLoginResult.testifySuccess();
      }
      catch (LoginFailedException e)
      {         
         return ExternalLoginResult.testifyFailure(e);
      }
   }
   
   private static String getRealmQualifiedUserId(String account, Map properties)
   {
      String realmId = (String) properties.get(SecurityProperties.REALM);
      if (StringUtils.isEmpty(realmId))
      {
         return MessageFormat.format("''{0}''", new Object[] {account});
      }
      else
      {
         return MessageFormat.format("''{0}'' (Realm: ''{1}'')", new Object[] {account, realmId});
      }
   }
}