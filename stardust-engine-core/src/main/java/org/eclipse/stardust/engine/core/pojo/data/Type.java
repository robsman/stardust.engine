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
package org.eclipse.stardust.engine.core.pojo.data;

import org.eclipse.stardust.common.StringKey;

/**
 * @author mgille
 */
public class Type extends StringKey
{
   public static final Type Boolean = new Type("boolean");
   public static final Type Char = new Type("char");
   public static final Type Byte = new Type("byte");
   public static final Type Short = new Type("short");
   public static final Type Integer = new Type("int");
   public static final Type Long = new Type("long");
   public static final Type Float = new Type("float");
   public static final Type Double = new Type("double");
   public static final Type String = new Type("String");
   // @todo (france, ub): leave this out but instead maybe offer period?
   public static final Type Calendar = new Type("Calendar");
   public static final Type Money = new Type("Money");
   public static final Type Timestamp = new Type("Timestamp");

   private Type(String id)
   {
      super(id, id);
   }
}
