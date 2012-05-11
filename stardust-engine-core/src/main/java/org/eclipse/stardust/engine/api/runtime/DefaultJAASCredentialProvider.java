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
package org.eclipse.stardust.engine.api.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.security.authentication.ConsoleCallbackHandler;
import org.eclipse.stardust.common.security.authentication.DefaultCallbackHandler;
import org.eclipse.stardust.common.security.authentication.GUICallbackHandler;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DefaultJAASCredentialProvider extends CredentialProvider
{
   private static final Logger trace = LogManager.getLogger(DefaultJAASCredentialProvider.class);

   protected Subject lastSubject;

   // @todo (france): remove synchronization
   public synchronized Map getCredentials(int loginType) throws LoginFailedException
   {
      switch (loginType)
      {
         case SWING_LOGIN:
            return login(new GUICallbackHandler("Logging in:"));
         case CONSOLE_LOGIN:
            return login(new ConsoleCallbackHandler());
         default:
            throw new InternalException("Unknown login type '" + loginType + "'.");
      }
   }

   // @todo (france): remove synchronization
   public synchronized Map getCredentials(Map credentials) throws LoginFailedException
   {
      String password = (String) credentials.get("password");
      if (password == null)
      {
         password = "";
      }
      return login(new DefaultCallbackHandler(
            (String) credentials.get("user"), password.toCharArray()));
   }

   private Map login(CallbackHandler handler) throws LoginFailedException
   {
      try
      {
         LoginContext lc = new LoginContext(Parameters.instance().getString(
               SecurityProperties.AUTHENTICATION_CONFIGURATION_NAME_PROPERTY,
               SecurityProperties.DEFAULT_AUTHENTICATION_CONFIGURATION_NAME), handler);
         lc.login();
         Map credentials = new HashMap();
         lastSubject = lc.getSubject();
         credentials.put(SUBJECT, lastSubject);
         return credentials;
      }
      catch (LoginException e)
      {
         trace.warn("", e);
         throw new LoginFailedException(e.getMessage(), LoginFailedException.UNKNOWN_REASON);
      }
   }
}
