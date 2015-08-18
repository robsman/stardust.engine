/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

/**
 * <p>
 * Represents a username password pair.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class UsernamePasswordPair
{
   private final String username;
   private final String password;
   
   /**
    * <p>
    * Initializes the object with the given username and password.
    * </p>
    * 
    * @param username the username to use; must not be null or empty
    * @param password the password to use; must not be null, but may be empty
    */
   public UsernamePasswordPair(final String username, final String password)         
   {
      if (username == null)
      {
         throw new NullPointerException("Username must not be null.");
      }
      if (username.isEmpty())
      {
         throw new IllegalArgumentException("Username must not be empty.");
      }
      if (password == null)
      {
         throw new NullPointerException("Password must not be null.");
      }
      /* password may be empty */
      
      this.username = username;
      this.password = password;
   }
   
   /**
    * @return the username this object has been initialized with
    */
   public String username()
   {
      return username;
   }
   
   /**
    * @return the password this object has been initialized with
    */
   public String password()
   {
      return password;
   }
}
