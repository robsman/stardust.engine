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

import java.io.Serializable;

/**
 * The Scope for a Permission.
 * 
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public abstract class Scope implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private Scope parent;
   private String id;

   public Scope(Scope parent, String id)
   {
      this.parent = parent;
      this.id = id;
   }

   /**
    * Get the parent scope of this scope.
    * 
    * @return the parent scope or null.
    */
   public Scope getParent()
   {
      return parent;
   }

   public String getId()
   {
      return id;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((parent == null) ? 0 : parent.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      Scope other = (Scope) obj;
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if (!id.equals(other.id))
      {
         return false;
      }
      if (parent == null)
      {
         if (other.parent != null)
         {
            return false;
         }
      }
      else if (!parent.equals(other.parent))
      {
         return false;
      }
      return true;
   }
}
