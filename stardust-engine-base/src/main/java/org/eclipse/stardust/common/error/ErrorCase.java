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
package org.eclipse.stardust.common.error;

import java.io.Serializable;

import org.eclipse.stardust.common.reflect.Reflect;


/**
 * Object to define an error.
 * 
 * @author sauer
 * @version $Revision: $
 */
public abstract class ErrorCase implements Serializable, Comparable
{
   private static final long serialVersionUID = 1L;

   private final String id;

   /**
    * Constructor using an id to identify this error.
    * 
    * @param id the id.
    */
   protected ErrorCase(String id)
   {
      this.id = id;
   }

   /**
    * Returns the id of this error.
    * 
    * @return the id.
    */
   public String getId()
   {
      return id;
   }

   public String toString()
   {
      return id;
   }

   public int compareTo(Object rhs)
   {
      int result;

      if (rhs instanceof ErrorCase)
      {
         result = id.compareTo(((ErrorCase) rhs).id);
      }
      else
      {
         throw new ClassCastException("Unable to compare "
               + Reflect.getHumanReadableClassName(ErrorCase.class) + " instances to "
               + rhs.getClass());
      }

      return result;
   }

   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

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
      ErrorCase other = (ErrorCase) obj;
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if ( !id.equals(other.id))
      {
         return false;
      }
      return true;
   }
}