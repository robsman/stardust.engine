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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.common.Key;

/** */
public class CardinalityKey extends Key
{
   public static final CardinalityKey UNKNOWN = new CardinalityKey(-1);
   public static final CardinalityKey TO_ONE = new CardinalityKey(0);
   public static final CardinalityKey TO_MANY = new CardinalityKey(1);

   static String[] keyList = {"*:1", "*:N"};

   /** */
   public CardinalityKey()
   {
      super();
   }

   /** */
   public CardinalityKey(int value)
   {
      super(value);
   }

   /**
    * Creates an key instance from its string representation.
    *
    * @param keyRepresentation java.lang.String
    */
   public CardinalityKey(String keyRepresentation)
   {
      this(getValue(keyRepresentation, getKeyList()));
   }

   /** */
   public static String[] getKeyList()
   {
      return keyList;
   }

   /** */
   public String getString()
   {
      if (value < 0)
      {
         return UNKNOWN_STRING;
      }

      return keyList[value];
   }
}
