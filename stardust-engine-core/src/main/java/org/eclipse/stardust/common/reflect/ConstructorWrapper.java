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
package org.eclipse.stardust.common.reflect;

import java.lang.reflect.Constructor;

public class ConstructorWrapper
{
   Constructor constructor;
   String constructorName = "not available";
   /**
    *
    */
   public ConstructorWrapper(Constructor constructor)
   {
      this.constructor = constructor;
      if (constructor != null)
      {
         constructorName = Reflect.getSortableConstructorName(constructor);
      }
   }

   /**
    *
    */
   public String toString()
   {
      return constructorName;
   }

   /**
    *
    */
   public Constructor getConstructor()
   {
      return constructor;
   }

   /**
    *
    */
   public String getName()
   {
      return toString();
   }
   /**
    *
    */
   public int hashCode()
   {
      return constructor.hashCode();
   }

   /**
    *
    */
   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof ConstructorWrapper)
      {
         return ((ConstructorWrapper)obj).getConstructor().equals(getConstructor());
      }
      else if (obj instanceof String)
      {
         return obj.equals(toString());
      }
      return false;
   }
}
