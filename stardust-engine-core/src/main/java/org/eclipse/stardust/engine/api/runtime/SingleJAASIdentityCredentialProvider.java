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

import java.util.Map;

import org.eclipse.stardust.common.error.LoginFailedException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SingleJAASIdentityCredentialProvider extends DefaultJAASCredentialProvider
{
   public Map getCredentials(int loginType) throws LoginFailedException
   {
      if (lastSubject != null)
      {
         throw new LoginFailedException("Configuration doesn't allow multiple logins "
               + "during one JVM session.", LoginFailedException.SYSTEM_ERROR);
      }
      return super.getCredentials(loginType);
   }

   public Map getCredentials(Map credentials) throws LoginFailedException
   {
      if (lastSubject != null)
      {
         throw new LoginFailedException("Configuration doesn't allow multiple logins "
               + "during one JVM session.", LoginFailedException.SYSTEM_ERROR);
      }
      return super.getCredentials(credentials);
   }

   public boolean hasMultipleIdenties()
   {
      return false;
   }
}
