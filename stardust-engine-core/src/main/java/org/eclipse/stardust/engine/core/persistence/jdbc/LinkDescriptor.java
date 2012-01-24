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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LinkDescriptor
{
   /**
    * The field being linked.
    */
   private final Field field;

   /**
    * The classes used as references might differ from the classes their primary key
    * attributes are defined in. Hence, we need to store link types in addition to the
    * result of <tt>fkFields[].getDeclaringClass()</tt>.
    */
   private final Class targetType;

   /**
    * The fields making up the foreign key to identify the linked target instance.
    */
   private final Field fkField;
   private int fkFieldLength;
   private Method registrar;
   private boolean isMandatory;
   private boolean isEagerFetchable;

   public LinkDescriptor(Field field, int fkFieldLength, Field fkField, Class targetType,
         Method registrar, boolean isMandatory, boolean isEagerFetchable)
   {
      this.field = field;
      this.fkFieldLength = fkFieldLength;

      this.targetType = targetType;
      this.fkField = fkField;
      this.registrar = registrar;
      if (registrar != null)
      {
         registrar.setAccessible(true);
      }
      this.isMandatory = isMandatory;
      this.isEagerFetchable = isEagerFetchable;
   }

   public Field getField()
   {
      return field;
   }

   public Class getTargetType()
   {
      return targetType;
   }

   public Field getFkField()
   {
      return fkField;
   }

   public int getFKFieldLength()
   {
      return fkFieldLength;
   }

   public Method getRegistrar()
   {
      return registrar;
   }

   public boolean isMandatory()
   {
      return isMandatory;
   }

   public boolean isEagerFetchable()
   {
      return isEagerFetchable;
   }
}
