/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.api.util;

import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.spi.security.CredentialDeliveryStrategy;

/**
 *
 * @author Barry.Grotjahn
 *
 */
public class TestCredentialDeliveryStrategy implements CredentialDeliveryStrategy 
{
   protected static final TestCredentialDeliveryStrategy INSTANCE = new TestCredentialDeliveryStrategy();
   
   private String token;
   public void setToken(String token)
   {
      this.token = token;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   private String password;
   
	public String getPassword()
   {
      return password;
   }

   public String getToken()
   {
      return token;
   }

	@Override
	public void deliverPasswordResetToken(IUser user, String token) 
	{
	   getInstance().setToken(token);
	}

	@Override
	public void deliverNewPassword(IUser user, String password) 
	{
      getInstance().setPassword(password);
	}
	
   public static TestCredentialDeliveryStrategy getInstance()
   {
      return INSTANCE;
   }	
}