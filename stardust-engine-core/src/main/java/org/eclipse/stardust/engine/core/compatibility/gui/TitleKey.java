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
package org.eclipse.stardust.engine.core.compatibility.gui;

import org.eclipse.stardust.common.Key;

/**
 * For testing.
 */
public class TitleKey extends Key
{
   public static final int DR = 0;
   public static final int DIPL_ING = 1;
   public static final int DIPL_OEC = 2;
   public static final int MA = 3;
   public static final int PROF_DR = 4;

   private static String[] keyList = {"Dr.", "Dipl-Ing.", "Dipl.Oec.", "MA", "Prof.Dr."};

   /**
    *
    */
   public TitleKey(int value)
   {
      super(value);
   }

   /**
    * creates an key instance from its string representation
    *
    * @param keyRepresentation java.lang.String
    */
   public TitleKey(String keyRepresentation)
   {
      this(getValue(keyRepresentation, getKeyList()));
   }

   /**
    *
    */
   public static String[] getKeyList()
   {
      return keyList;
   }

   /**
    *
    */
   public String getString()
   {
      return keyList[value];
   }
}
