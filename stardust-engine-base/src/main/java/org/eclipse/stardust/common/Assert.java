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

import java.util.Collection;

/**
 * @version $Revision$
 */
public class Assert extends RuntimeException
{
   private static final long serialVersionUID = 3758189311622160671L;

   private static final String SIMPLE_ASSERT_MESSAGE = "Assertion failed.";
   private static final String ASSERT_MESSAGE = "Assertion failed: ";
   private static final String DEFAULT_MESSAGE_UNEXPECTED_BEHAVIOUR =
         "Internal Error.";

   protected Assert()
   {
      super(SIMPLE_ASSERT_MESSAGE);
   }

   protected Assert(String text)
   {
      super(ASSERT_MESSAGE + text);
   }

   /**
    * Throws an Assert Exception if the provided expression is false.
    * @param condExpr expression to be checked
    */
   public static void condition(boolean condExpr)
   {
      if (!condExpr)
      {
         throw new Assert();
      }
   }

   public static void condition(boolean condExpr, String text)
   {
      if (!condExpr)
      {
         throw new Assert(text);
      }
   }

   public static void isNull(Object isNullObj, String text)
   {
      if (isNullObj != null)
      {
         throw new Assert(text);
      }
   }

   /**
    * Checks whether the provided object is null or not. If it is null, an
    * Assert is thrown
    * @param isNotNullObj the object to be tested
    *
    * @see Assert
    */
   public static void isNotNull(Object isNotNullObj)
   {
      if (isNotNullObj == null)
      {
         throw new Assert();
      }
   }

   public static void isNotNull(Object isNotNullObj, String text)
   {
      if (isNotNullObj == null)
      {
         throw new Assert(text);
      }
   }

   public static void isNotEmpty(Collection<?> collection, String text)
   {
      if (collection.isEmpty())
      {
         throw new Assert(text);
      }
   }

   public static void isNotEmpty(String string, String text)
   {
      if (string== null || string.length() == 0)
      {
         throw new Assert(text);
      }
   }

   public static void lineNeverReached()
   {
      throw new Assert(DEFAULT_MESSAGE_UNEXPECTED_BEHAVIOUR);
   }

   public static void lineNeverReached(String text)
   {
      throw new Assert(text);
   }
}

