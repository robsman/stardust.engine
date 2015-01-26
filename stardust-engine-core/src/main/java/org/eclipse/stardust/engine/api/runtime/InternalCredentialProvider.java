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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.cli.security.authentication.ConsolePrompt;
import org.eclipse.stardust.engine.core.compatibility.gui.security.LoginDialog;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class InternalCredentialProvider extends CredentialProvider
{
   public Map getCredentials(int loginType) throws LoginFailedException
   {
      Map credentials = new HashMap();
      switch (loginType)
      {
         case SWING_LOGIN:
            if (LoginDialog.showDialog("Login"))
            {
               credentials.put(SecurityProperties.CRED_USER, LoginDialog.getId());
               credentials.put(SecurityProperties.CRED_PASSWORD, LoginDialog
                     .getPassword());
               setOptionalCredential(credentials, SecurityProperties.CRED_PARTITION,
                     LoginDialog.getPartitionId());
               setOptionalCredential(credentials, SecurityProperties.CRED_DOMAIN,
                     LoginDialog.getDomainId());
               setOptionalCredential(credentials, SecurityProperties.CRED_REALM,
                     LoginDialog.getRealmId());
            }
            else
            {
               throw new LoginFailedException(
                     BpmRuntimeError.AUTHx_AUTH_CANCEL_BY_USER.raise(),
                     LoginFailedException.LOGIN_CANCELLED);
            }

            break;
         case CONSOLE_LOGIN:
            ConsolePrompt.show();
            credentials.put(SecurityProperties.CRED_USER, ConsolePrompt.getUsername());
            credentials
                  .put(SecurityProperties.CRED_PASSWORD, ConsolePrompt.getPassword());
            setOptionalCredential(credentials, SecurityProperties.CRED_PARTITION,
                  ConsolePrompt.getPartition());
            setOptionalCredential(credentials, SecurityProperties.CRED_DOMAIN,
                  ConsolePrompt.getDomain());
            setOptionalCredential(credentials, SecurityProperties.CRED_REALM,
                  ConsolePrompt.getRealm());

            break;
         default:
            throw new InternalException("Unknown login type: " + loginType);
      }
      return credentials;
   }

   public Map getCredentials(Map credentials)
   {
      return credentials;
   }
   
   private void setOptionalCredential(Map credentials, String key, String value)
   {
      if ( !StringUtils.isEmpty(value))
      {
         credentials.put(key, value);
      }
   }
}
