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
public class FieldDescriptor
{
   private final Field field;
   private final String wrapFunction;
   private final String unwrapFunction;
   private final int length;
   
   private final boolean secret;

   public FieldDescriptor(Field field, int length, String wrapFunction,
         String unwrapFunction, boolean secret)
   {
      this.field = field;
      this.wrapFunction = wrapFunction;
      this.unwrapFunction = unwrapFunction;
      this.length = length;
      this.secret = secret;
   }

   public Field getField()
   {
      return field;
   }

   public String getFieldEncryptFunction()
   {
      return wrapFunction;
   }

   public String getFieldDecryptFunction()
   {
      return unwrapFunction;
   }

   public int getLength()
   {
      return length;
   }
   
   public boolean isSecret()
   {
      return secret;
   }
}

