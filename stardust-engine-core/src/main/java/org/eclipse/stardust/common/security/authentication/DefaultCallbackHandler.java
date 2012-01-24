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
/*

 * @(#)DefaultCallbackHandler.java	1.19 00/01/11
 *
 */

package org.eclipse.stardust.common.security.authentication;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Implement callback handling without user interaction.
 * <p>
 * User name and password are provided in the cosntructor.
 */
public class DefaultCallbackHandler implements CallbackHandler
{
   private String name;
   private char[] password;

   //TODO what are the preconditions? shouldn't it rule out nulls
   public DefaultCallbackHandler(final String name, final char[] aPassword)
   {
      this.name = name;
      if(aPassword != null) {
         int length = aPassword.length;
         this.password = new char[aPassword.length];
         System.arraycopy(aPassword, 0, this.password, 0, length);         
      }
   }

   public void handle(Callback[] callbacks)
         throws IOException, UnsupportedCallbackException
   {
      for (int i = 0; i < callbacks.length; i++)
      {
         if (callbacks[i] instanceof NameCallback)
         {
            ((NameCallback) callbacks[i]).setName(name);

         }
         else if (callbacks[i] instanceof PasswordCallback)
         {
            ((PasswordCallback) callbacks[i]).setPassword(password);
         }
      }
   }
}
