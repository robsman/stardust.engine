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
package org.eclipse.stardust.engine.core.cache;

/**
 * @author Florin.Herinean
 */
public abstract class PrimaryKey implements CacheKey
{
   private static final long serialVersionUID = 1L;
   
   private long oid;

   public PrimaryKey(long oid)
   {
      this.oid = oid;
   }

   public long getOid()
   {
      return oid;
   }

   @Override
   public String toString()
   {
      return Long.toString(oid);
   }

   @Override
   public int hashCode()
   {
      return (int)(oid ^ (oid >>> 32));
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj != null && getClass().equals(obj.getClass()))
      {
         return oid == ((PrimaryKey) obj).oid;
      }
      return false;
   }
}
