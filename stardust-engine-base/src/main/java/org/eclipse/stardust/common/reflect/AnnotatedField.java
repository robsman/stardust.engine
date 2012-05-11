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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.error.InternalException;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AnnotatedField
{
   private Field field;
   private Map annotations = new HashMap();

   AnnotatedField(Field field)
   {
      this.field = field;
   }

   void addAnnotation(String id, Object value)
   {
      annotations.put(id, value);
   }

   public Object getAnnotation(String id)
   {
      return annotations.get(id);
   }

   public Field getField()
   {
      return field;
   }

   public Object get(Object o)
   {
      try
      {
         field.setAccessible(true);
         return field.get(o);
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }
}
