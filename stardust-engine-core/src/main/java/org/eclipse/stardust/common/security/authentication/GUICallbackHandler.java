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
package org.eclipse.stardust.common.security.authentication;

import java.io.IOException;

import javax.security.auth.callback.*;

import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;


/**
 * A simple callback handler which shows the swing user/password {@link LoginDialog}.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class GUICallbackHandler implements CallbackHandler
{
   private String userName = "";
   private String password = "";
   private String title;

   public GUICallbackHandler(String title)
   {
      this.title = title;
   }

   public void handle(Callback callbacks[])
         throws IOException, UnsupportedCallbackException
   {
      if (LoginDialog.showDialog(title))
      {
         userName = LoginDialog.getId();
         password = LoginDialog.getPassword();
      }
      else
      {
         userName = "";
         password = "";
         throw new LoginFailedException(
               BpmRuntimeError.AUTHx_AUTH_CANCEL_BY_USER.raise(),
               LoginFailedException.LOGIN_CANCELLED);
      }

      for (int i = 0; i < callbacks.length; i++)
      {
         Callback callback = callbacks[i];
         if (callback instanceof NameCallback)
         {
            ((NameCallback) callback).setName(userName);
         }
         else if (callback instanceof PasswordCallback)
         {
            ((PasswordCallback) callback).setPassword(password.toCharArray());
         }
      }

   }
}
