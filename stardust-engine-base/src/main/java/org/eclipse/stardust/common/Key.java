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

import java.io.Serializable;

/**
 *	Base class for enumeration types.
 */
public abstract class Key implements Serializable, Comparable
{
   private static final long serialVersionUID = 7329189583654765759L;

   /**
    *	String that will be returned if <code>value == Unknown.KEY_VALUE</code>.
    */
   public static String UNKNOWN_STRING = "";

   protected int value;

   /**
    *
    */
   public Key()
   {
      this.value = Unknown.KEY_VALUE;
   }

   /**
    *
    */
   public Key(int value)
   {
      this.value = value;

      if (this.value < 0)
      {
         this.value = Unknown.KEY_VALUE;
      }
   }

   /**
    *	creates an Key-Instance for an String-Representation
    *  if the string is unknown for the key it returns the "unknown"-Key
    */
//   public Key(String keyRepresentation)
//   {
//      this(getValue(keyRepresentation, getKeyList()));
//   }

   /**
    * Checks whether the key value equals a given integer.
    */
   public boolean equals(int value)
   {
      return (this.value == value);
   }

   /**
    * Checks whether the key value equals a given object.
    */
   public boolean equals(Object obj)
   {
      return ((this == obj)
            || ((obj != null)
            && (obj instanceof Key)
            && (value == ((Key) obj).getValue())
            )
            );
   }

   public int hashCode()
   {
      int result = value;
      result = 29 * result + getClass().hashCode();
      return result;
   }

//   /**
//    *	returns the complete list of keys
//    */
//   public abstract String[] getKeyList();

   /**
    * Returns the string representation of the key.
    */
   public abstract String getString();

   /**
    *
    */
   public int getValue()
   {
      return value;
   }
   /**
    * Returns the int value of a string representation of the key.
    * If string is not found returns Unknown.KEY_VALUE <=> Integer.MIN_VALUE.
    */
   // @todo review method!
   public static int getValue(String stringToFind, String[] stringList)
   {
      int position = Unknown.KEY_VALUE;

      if (stringToFind == null || stringList == null)
      {
         return position;
      }

      for (int i = 0; i < stringList.length; i++)
      {
         if (stringToFind.equals(stringList[i]))
         {
            position = i;
            break;
         }
      }

      return position;
   }

   /**
    * Returns by default the result of getString().
    */
   public String toString()
   {
      return getString();
   }
   
   public int compareTo(Object rhs)
   {
      int result;
      if (rhs instanceof Key)
      {
         Key rhsKey = (Key) rhs;
         if (value == rhsKey.value)
         {
            result = 0;
         }
         else
         {
            result = (value < rhsKey.value) ? -1 : 1;
         }
      }
      else
      {
         throw new ClassCastException("Unable to compare Key instances to "
               + rhs.getClass());
      }

      return result;
   }
}
