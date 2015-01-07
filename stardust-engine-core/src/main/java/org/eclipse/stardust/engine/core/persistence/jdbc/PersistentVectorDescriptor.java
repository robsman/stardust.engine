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

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class PersistentVectorDescriptor
{
   private Field field;
   private Class type;
   private String otherRole;

   public PersistentVectorDescriptor(Field field,
         Class type, String otherRole)
   {
      this.field = field;
      this.type = type;
      this.otherRole = otherRole;
   }

   public Class getType()
   {
      return type;
   }

   public String getOtherRole()
   {
      return otherRole;
   }

   public Field getField()
   {
      return field;
   }
}
