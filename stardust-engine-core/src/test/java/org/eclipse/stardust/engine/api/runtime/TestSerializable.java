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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 *
 */
public class TestSerializable implements Serializable
{
   private static final long serialVersionUID = 6486331682547628323L;

   public int integer;
   public String string;

   /**
    *
    */
   public TestSerializable(int integer, String string)
   {
      this.integer = integer;
      this.string = string;
   }

   /**
    *
    */
   public int getInteger()
   {
      return integer;
   }

   /**
    *
    */
   public void setInteger(int integer)
   {
      this.integer = integer;
   }

   /**
    *
    */
   public String getString()
   {
      return string;
   }

   /**
    *
    */
   public void setString(String string)
   {
      this.string = string;
   }
}
