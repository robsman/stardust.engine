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
package org.eclipse.stardust.common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class IntKey implements Serializable, Comparable
{
   private static final long serialVersionUID = 5627417127347728410L;

   private int id;
   private String name;

   public IntKey(int id, String defaultName)
   {
      this.id = id;
      this.name = defaultName;
      // @todo/i18n (ub)
      // name = getResource(getClass().getName() + "." + id)
   }

   public int getValue()
   {
      return id;
   }

   public String toString()
   {
      return getName();
   }

   public String getName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
   {
         return false;
      }
      if (obj.getClass().equals(getClass()))
      {
         return id == (((IntKey) obj).id);
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return 17 * 37 + id;
   }
   
   public static Collection getKeys(Class type)
   {
      Collection result = getKeys(type, type);
      Class outer = type.getDeclaringClass();
      if (outer != null)
      {
         result.addAll(getKeys(outer, type));
      }
      return result;
   }

   private static Collection getKeys(Class type, Class target)
   {
      Field[] fields = type.getFields();
      LinkedList result = new LinkedList();
      for (int i = 0; i < fields.length; i++)
      {
         Field field = fields[i];
         int modifiers = field.getModifiers();
         if (field.getType().equals(target)
               && Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
         {
            try
            {
               result.add(field.get(null));
            }
            catch (Exception e)
            {
               throw new InternalException(e);
            }
         }
      }
      return result;
   }

   public static IntKey getKey(Class type, int id)
   {
      IntKey result = getKey(type, type, id);
      if (result == null)
      {
         Class outer = type.getDeclaringClass();
         if (outer != null)
         {
            result = getKey(outer, type, id);
         }
      }
      return result;
   }

   private static IntKey getKey(Class type, Class target,  int id)
   {
      Field[] fields = type.getFields();
      for (int i = 0; i < fields.length; i++)
      {
         Field field = fields[i];
         int modifiers = field.getModifiers();
         if (field.getType().equals(target)
               && Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
         {
            try
            {
               IntKey value = (IntKey) field.get(null);
               if (value.id == id)
               {
                  return value;
               }
            }
            catch (Exception e)
            {
               throw new InternalException(e);
            }
         }
      }
      return null;
   }

   protected Object readResolve()
   {
      return getKey(getClass(), id);
   }
   
   public int compareTo(Object rhs)
   {
      int result;
      if (rhs instanceof IntKey)
      {
         IntKey rhsKey = (IntKey) rhs;
         if (id == rhsKey.id)
         {
            result = 0;
         }
         else
         {
            result = (id < rhsKey.id) ? -1 : 1;
         }
      }
      else
      {
         throw new ClassCastException("Unable to compare "
               + Reflect.getHumanReadableClassName(getClass()) + " instances to "
               + rhs.getClass());
      }

      return result;
   }
}
