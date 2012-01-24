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

import java.lang.reflect.Field;

public class AttributeStep extends DereferenceStep
{
   protected Field field;

   //@ needed for Hawk only
   public Field getField()
   {
      return field;
   }

   /** */
   public AttributeStep(Field newField)
   {
      field = newField;
   }

   /** Returns the field value for non-array fields and a cursor initialized with
    the array for array fields. */
   public Object getValue(Object startObject)
   {
      try
      {
         return field.get(startObject);
      }
      catch (IllegalAccessException E)
      {
         return null;
      }
   }

   /** */
   public void setValue(Object startObject,
         Object newValue)
   {
      try
      {
         field.set(startObject,
               newValue);
      }
      catch (IllegalAccessException E)
      {
      }
   }

   /** */
   public Object createInstance(Object startObject)
   {
      Class type = field.getType();
      Object newValue = null;

      try
      {
         newValue = type.newInstance();
         field.set(startObject,
               newValue);
      }
      catch (java.lang.InstantiationException Ex)
      {
      }
      catch (IllegalAccessException Ex)
      {
      }
      return newValue;
   }

   /** */
   public String getName()
   {
      return field.getName();
   }
}

