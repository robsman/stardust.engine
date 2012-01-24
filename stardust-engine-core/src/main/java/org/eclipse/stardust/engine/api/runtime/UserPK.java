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

import org.eclipse.stardust.engine.core.runtime.beans.IUser;

/**
 *
 */
public class UserPK implements java.io.Serializable
{
   /**
    *
    */
   public long oid;

   /**
    *
    */
   public UserPK()
   {
   }

   /**
    *
    */
   public UserPK(long oid)
   {
      this.oid = oid;
   }

   /**
    *
    */
   public UserPK(IUser user)
   {
      this.oid = user.getOID();
   }

   /**
    *
    */
   public String toString()
   {
      return "" + oid;
   }
}
