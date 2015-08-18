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
package org.eclipse.stardust.engine.core.compatibility.gui.utils;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import org.eclipse.stardust.common.Unknown;


public class IntegerDocument extends PlainDocument
{
   public void insertString(int offs, String str, AttributeSet a)
         throws BadLocationException
   {
      if (str != null)
      {
         StringBuffer sb = new StringBuffer(str.length());
         for (int i = 0; i < str.length(); i++)
         {
            char c = str.charAt(i);
            // allows one leading sign
            if (Character.isDigit(c) || offs == 0 && i == 0 && (c == '-' || c == '+'))
            {
               sb.append(c);
            }
         }
         super.insertString(offs, sb.toString(), a);
      }
   }

   public int getValue()
   {
      try
      {
         if (getLength() > 0)
         {
            String content = getText(0, getLength());
            return Integer.parseInt(content);
         }
      }
      catch (BadLocationException e)
      {
      }
      return Unknown.INT;
   }

   public void setValue(int value)
   {
      try
      {
         if (getLength() > 0)
         {
            remove(0, getLength());
         }
         if (value != Unknown.INT)
         {
            insertString(0, Integer.toString(value), null);
         }
      }
      catch (BadLocationException e)
      {
      }
   }
}

