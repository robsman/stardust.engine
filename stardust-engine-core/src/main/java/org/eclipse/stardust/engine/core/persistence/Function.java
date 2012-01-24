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
package org.eclipse.stardust.engine.core.persistence;

import org.eclipse.stardust.common.StringKey;

public abstract class Function extends StringKey
{
   /**
    * Yields the number of rows.
    */
   public static final ParamLess ROW_COUNT = new ParamLess("COUNT(*)");
   
   /**
    * Yields the number of values.
    */
   public static final Unary COUNT = new Unary("COUNT");
   
   /**
    * Yields the number of distinct values.
    */
   public static final Nary COUNT_DISTINCT = new Nary("COUNT(DISTINCT");
   
   /**
    * Yields the minimum value.
    */
   public static final Unary MIN = new Unary("MIN");

   /**
    * Yields the maximum value.
    */
   public static final Unary MAX = new Unary("MAX");

   /**
    * Converts a string to upper case.
    */
   public static final Unary STR_UPPER = new Unary("UPPER");
   
   /**
    * Converts a string to lower case.
    */
   public static final Unary STR_LOWER = new Unary("LOWER");
   
   private Function(String id)
   {
      super(id, id);
   }

   /**
    * @author rsauer
    * @version $Revision$
    */
   public static class ParamLess extends Function
   {
      private ParamLess(String id)
      {
         super(id);
      }
   }

   /**
    * @author rsauer
    * @version $Revision$
    */
   public static class Constant extends ParamLess
   {
      public Constant(String expression)
      {
         super(expression);
      }
   }

   /**
    * @author rsauer
    * @version $Revision$
    */
   public static class Unary extends Function
   {
      private Unary(String id)
      {
         super(id);
      }
   }

   /**
    * @author rsauer
    * @version $Revision$
    */
   public static class Nary extends Function
   {
      private Nary(String id)
      {
         super(id);
      }
   }
}
