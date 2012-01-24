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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.security.authentication.LoginFailedException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class CredentialProvider
{
   /**
    * Indicates the usage of a graphical (Swing) login
    */
   public static final int SWING_LOGIN = 1;

   /**
    * Indicates the usage of a console based login.
    */
   public static final int CONSOLE_LOGIN = 2;
   
   /**
    * Indicates the usage of the current transaction.
    * (The login of the current transaction will be reused.) 
    */
   public static final int CURRENT_TX = 3; 

   public static final String SUBJECT = "subject";

   private static CredentialProvider instance;

   public static synchronized CredentialProvider instance() throws PublicException
   {
      if (null == instance)
      {
         try
         {
            instance = (CredentialProvider) Reflect.createInstance(Parameters.instance()
                  .getString(SecurityProperties.CREDENTIAL_PROVIDER,
                        PredefinedConstants.INTERNAL_CREDENTIALPROVIDER_CLASS));
         }
         catch (InternalException e)
         {
            throw new PublicException("Invalid credential provider configuration: "
                  + e.getMessage());
         }
      }
      return instance;

   }

   public abstract Map getCredentials(int loginType) throws LoginFailedException;

   public abstract Map getCredentials(Map credentials) throws LoginFailedException;

   public boolean hasMultipleIdenties()
   {
      return true;
   }
}
