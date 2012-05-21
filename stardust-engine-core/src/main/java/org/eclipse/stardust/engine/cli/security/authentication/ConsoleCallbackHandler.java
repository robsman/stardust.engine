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
package org.eclipse.stardust.engine.cli.security.authentication;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ConsoleCallbackHandler implements CallbackHandler
{

   private String username="";
   private String password="";

   public void handle(Callback callbacks[])
         throws IOException, UnsupportedCallbackException
   {
      ConsolePrompt.show();
      username = ConsolePrompt.getUsername();
      password = ConsolePrompt.getPassword();
      for (int i = 0; i < callbacks.length; i++)
      {
         Callback callback = callbacks[i];
         if (callback instanceof NameCallback)
         {
            ((NameCallback) callback).setName(username);
         }
         else if (callback instanceof PasswordCallback)
         {
            ((PasswordCallback) callback).setPassword(password != null ? password.toCharArray() : new char[0]);
         }
      }
   }
}

