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

import java.util.Calendar;

public class CompareHelper
{
   /**
    * Extracts the int value of a key.
    *
    * @param key the key to get the value
    * @return the value of the key if not null, or Unknown.INT
    */
   public static int getValue(Key key)
   {
      if (key == null)
      {
         return Unknown.INT;
      }
      return key.getValue();
   }

   /**
    * Checks if a key has a specific value.
    *
    * @param keyValue the value to test against
    * @param key the key to test (may be null)
    * @return true if the value is Unknown.INT and the key is null or if the key is not
    * null and the key value equals the provided value
    */
   public static boolean areEqual(int keyValue, Key key)
   {
      if (keyValue == Unknown.INT)
      {
         return key == null;
      }
      else
      {
         return key != null && key.getValue() == keyValue;
      }
   }

   /**
    * Checks if two object references are equals.
    *
    * @param object1 the first object (may be null)
    * @param object2 the second object (may be null)
    * @return true if both arguments are null or if both arguments are not null and equal
    */
   public static boolean areEqual(Object object1, Object object2)
   {
      if (object1 == null)
      {
         return object2 == null;
      }
      else
      {
         return object2 != null && object1.equals(object2);
      }
   }

   /**
    * Compares two objects by sligthly extending the {@link Comparable} contract.
    *
    * <ul>
    *    <li><code>null</code> compared to <code>null</code> will yield <code>0</code></li>
    *    <li><code>null</code> compared to something non-<code>null</code> will yield 1</li>
    *    <li>something non-<code>null</code> compared to <code>null</code> will yield -1</li>
    *    <li>something non-<code>null</code> compared to something non-<code>null</code> will try to leverage {@link Comparable#compareTo(Object)}</li>
    * </ul>
    *
    * @param lhs the value to be compared
    * @param rhs the value <code>lhs</code> has to be compared to
    * @return a negative integer, zero, or a positive integer as <code>lhs</code> is less than, equal to, or greater than <code>rhs</code>
    * @throws ClassCastException if none of both arguments is of type {@link Comparable}
    *
    * @see Comparable#compareTo(Object)
    */
   public static int compare(Object lhs, Object rhs)
   {
      int result;

      if ((null == lhs) && (null == rhs))
      {
         result = 0;
      }
      else if (lhs instanceof Comparable)
      {
         if (null != rhs)
         {
            result = ((Comparable) lhs).compareTo(rhs);
         }
         else
         {
            result = -1;
         }
      }
      else if (rhs instanceof Comparable)
      {
         if (null != lhs)
         {
            result = -((Comparable) rhs).compareTo(lhs);
         }
         else
         {
            result = 1;
         }
      }
      else if ((lhs instanceof Boolean) && (rhs instanceof Boolean))
      {
         if (lhs.equals(rhs))
         {
            result = 0;
         }
         else
         {
            result = (Boolean.FALSE.equals(lhs) ? -1 : 1);
         }
      }
      else if ((lhs instanceof Calendar) && (rhs instanceof Calendar))
      {
         result = compare(((Calendar) lhs).getTime(), ((Calendar) rhs).getTime());
      }
      else
      {
         throw new ClassCastException("Incomparable objects (" + lhs + ", " + rhs + ")");
      }

      return result;
   }

   private CompareHelper()
   {
      // no instances allowed
   }
}