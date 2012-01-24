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
package org.eclipse.stardust.engine.core.spi.security;

import java.util.Map;

/**
 * @author rsauer
 * @version $Revision$
 */
public class LoginProviderAdapter implements ExternalLoginProvider
{
   private final LoginProvider loginProvider;

   public LoginProviderAdapter(LoginProvider loginProvider)
   {
      this.loginProvider = loginProvider;
   }

   public ExternalLoginResult login(String id, String password, Map properties)
   {
      LoginResult result = loginProvider.login(id, password);

      return result.wasSuccessful()
            ? ExternalLoginResult.testifySuccess()
            : ExternalLoginResult.testifyFailure(result.getLoginFailedReason());
   }
}
