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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Document to restrict entry content to string input compatible to numbers.
 */
class ShortDocument extends PlainDocument
{
   private int minimum;
   private int maximum;

   /**
    * Constructor with maximum value. The minimum value is set to 1.
    * @deprecated ... use the constructor with minimum and maximum
    */
   public ShortDocument(int maximum)
   {
      this(1, maximum);
   }

   /**
    */
   public ShortDocument(int minimum, int maximum)
   {
      this.minimum = minimum;
      this.maximum = maximum;
   }

   /**
    *	Handles string insertion
    */
   public void insertString(int offs, String str, AttributeSet a)
         throws BadLocationException
   {
      if (str == null)
      {
         return;
      }

      String oldString = getText(0, getLength());
      String newString = oldString.substring(0, offs);

      newString += str;
      newString += oldString.substring(offs, getLength());

      if (newString.length() == 0)
      {
         return;
      }

      try
      {
         int value = Short.parseShort(newString);

         if ((value >= minimum) && (value <= maximum))
         {
            super.insertString(offs, str, a);
         }
      }
      catch (NumberFormatException x)
      {
      }
   }
}
