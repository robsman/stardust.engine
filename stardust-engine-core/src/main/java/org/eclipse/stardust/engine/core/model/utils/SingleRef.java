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
package org.eclipse.stardust.engine.core.model.utils;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SingleRef extends SingleHook implements Reference
{
   private static final String PRP_FIELD_CACHE_PREFIX = SingleRef.class.getName() + ".FieldCache.";
   
   private String otherRole;
   private String name;

   public SingleRef(ModelElement owner, String name)
   {
      super(owner);
      this.name = name;
   }

   public SingleRef(ModelElement owner, String name, String otherRole)
   {
      super(owner);
      this.name = name;
      this.otherRole = otherRole;
   }

   public void remove(ModelElement element)
   {
      removeOtherRole(getElement());
      super.remove(element);
   }

   private void removeOtherRole(ModelElement element)
   {
      if (otherRole != null && element != null)
      {
         try
         {
            Field field = resolveField(element.getClass(), otherRole);
            RefEnd otherEnd = (RefEnd) field.get(element);
            otherEnd.__remove__(getOwner());
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }
   }

   void setOtherRole(ModelElement element)
   {
      if (otherRole != null && element != null)
      {
         try
         {
            Field field = resolveField(element.getClass(), otherRole);
            RefEnd otherEnd = (RefEnd) field.get(element);
            otherEnd.__add__(getOwner());
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }
   }

   public void setElement(ModelElement element)
   {
      delete();
      if (element != null)
      {
         super.setElement(element);
         element.addReference(this);
         setOtherRole(element);
         getOwner().markModified();
      }
   }

   public void delete()
   {
      if (getElement() != null)
      {
         remove(getElement());
      }
   }

   private static Field resolveField(Class elementClazz, String otherRole)
   {
      GlobalParameters globals = GlobalParameters.globals();

      String cacheId = PRP_FIELD_CACHE_PREFIX + elementClazz.getName();
      
      ConcurrentHashMap fieldCache = (ConcurrentHashMap) globals.get(cacheId);
      if (null == fieldCache)
      {
         fieldCache = (ConcurrentHashMap) globals.initializeIfAbsent(cacheId,
               new ValueProvider()
               {
                  public Object getValue()
                  {
                     return new ConcurrentHashMap();
                  }
               });
      }
      
      Field field = (Field) fieldCache.get(otherRole);
      
      if (null == field)
      {
         field = Reflect.getField(elementClazz, otherRole);
         if (null == field)
         {
            throw new InternalException("Other role '" + otherRole
                  + "' does not exist in " + elementClazz);
         }

         fieldCache.put(otherRole, field);
      }

      return field;
   }

}
