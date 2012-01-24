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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class StringKey implements Serializable, Comparable
{
   private static final long serialVersionUID = 1L;
   
   private String id;
   private String name;

   public StringKey(String id, String defaultName)
   {
      if(id == null) {
         throw new IllegalArgumentException("Parameter id can never be null");
      }
      this.id = id;
      this.name = defaultName;
      // @todo/i18n (ub)
      // name = getResource(getClass().getName() + "." + id)
   }

   public String toString()
   {
      return id;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(final Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      
      if (obj == null)
      {
         return false;
      }
      if (obj.getClass().equals(getClass()))
      {
         return id.equals(((StringKey) obj).id);
      }
      //TODO this breaks the contract for equals
      return id.equals(obj);
   }

   public int hashCode()
   {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   public static List getKeys(Class type)
   {
      List result = getKeys(type, type);
      Class outer = type.getDeclaringClass();
      if (outer != null)
      {
         result.addAll(getKeys(outer, type));
      }
      return result;
   }

   private static List getKeys(Class type, Class target)
   {
      Field[] fields = type.getFields();
      List result = new ArrayList();
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

   /**
    * Translates the stringified ID into the appropriate key instance.
    *  
    * @param id The stringified ID to be resolved.
    * @return The resolved key, <code>null </code> if no key could be resolved.
    */
   public static StringKey getKey(Class type, String id, List valueCache)
   {
      StringKey result = null;

      for (int i = 0; i < valueCache.size(); ++i)
      {
         StringKey key = (StringKey) valueCache.get(i);
         
         if (type.isAssignableFrom(key.getClass()) && key.id.equals(id))
         {
            result = key;
            break;
         }
      }
      
      return result;
   }

   /**
    * Translates the stringified ID into the appropriate key instance.
    *  
    * @param id The stringified ID to be resolved.
    * @return The resolved key, <code>null </code> if no key could be resolved.
    */
   public static StringKey getKey(Class type, String id)
   {
      StringKey result = getKey(type, type, id);
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

   private static StringKey getKey(Class type, Class target, String id)
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
               StringKey value = (StringKey) field.get(null);
               if (value.id.equals(id))
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
      return getKey(getClass(), toString());
   }

   public int compareTo(Object rhs)
   {
      int result;
      if (rhs instanceof StringKey)
      {
         result = id.compareTo(((StringKey) rhs).id);
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
