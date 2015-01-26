/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws.configurer;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import javax.xml.ws.WebServiceException;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.validate.UsernameTokenValidator;
import org.eclipse.stardust.engine.ws.WebServiceEnv;


/**
 * <p>
 * This class only validates that
 * <ul>
 *   <li>the password format is correct (only plain text is supported), and that</li>
 *   <li>a username is provided.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The actual authentication and authorization will be done later by means of the
 * common Stardust security mechanisms. Therefore the credentials are stored in the
 * web service environment represented by {@link org.eclipse.stardust.engine.ws.WebServiceEnv}.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class UsernameTokenValidatingConfigurer extends UsernameTokenValidator
{
   @Override
   protected void verifyPlaintextPassword(final UsernameToken usernameToken, final RequestData ignored)
   {
      final String pwdType = usernameToken.getPasswordType();
      if (pwdType == null || pwdType.isEmpty() || !pwdType.endsWith("#" + WSConstants.PW_TEXT))
      {
         throw new WebServiceException("WS-Security: Only plain text passwords are supported.");
      }
      
      final String username = usernameToken.getName();
      final String pwd = usernameToken.getPassword();
      if (isEmpty(username))
      {
         throw new WebServiceException("WS-Security: No username provided.");
      }
      
      WebServiceEnv.setCurrentCredentials(username, pwd);
   }   
}
