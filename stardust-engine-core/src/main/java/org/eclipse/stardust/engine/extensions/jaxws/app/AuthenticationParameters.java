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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import org.eclipse.stardust.engine.core.runtime.beans.IUser;

/**
 * @author fherinean
 * @version $Revision$
 */
public class AuthenticationParameters implements IBasicAuthenticationParameters
{
   private String mechanism;
   private String variant;

   private String username;
   private String password;

   private IUser user;

   public AuthenticationParameters(String mechanism)
   {
      this.mechanism = mechanism;
   }

   public String getMechanism()
   {
      return mechanism;
   }

   public void setMechanism(String mechanism)
   {
      this.mechanism = mechanism;
   }

   public String getVariant()
   {
      return variant;
   }

   public void setVariant(String variant)
   {
      this.variant = variant;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public IUser getUser()
   {
      return user;
   }

   public void setUser(IUser user)
   {
      this.user = user;
   }
   
   @Override
   public boolean equals(final Object obj)
   {
      // TODO override (see CRNT-21251)
      return super.equals(obj);
   }
   
   @Override
   public int hashCode()
   {
      // TODO override (see CRNT-21251)
      return super.hashCode();
   }
}
