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

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.eclipse.stardust.common.Unknown;


/**
 * DateTextField is generic number field used by AbstractDateEntry.
 */
public class DateTextField extends JTextField
      implements FocusListener
{
   AbstractDateEntry entry;

   /**
    * Constructor with implizit minimum 1.
    *
    * @deprecated ... use the constructor with minimum and maximum value
    */
   public DateTextField(int columns, int maximum)

   {
      this(columns, 1, maximum);
   }

   /**
    *
    */
   public DateTextField(int columns, int minimum, int maximum)
   {
      super(new ShortDocument(minimum, maximum), " ", columns);

      initialize();
   }

   /**
    * Invoked when a component gains the keyboard focus.
    */
   public void focusGained(FocusEvent e)
   {
      if (isEnabled() && isEditable())
      {
         selectAll();
      }
   }

   /**
    * Invoked when a component loses the keyboard focus.
    */
   public void focusLost(FocusEvent e)
   {
   }

   /**
    *
    */
   public int getValue()
   {
      try
      {
         return Integer.parseInt(getText());
      }
      catch (NumberFormatException x)
      {
         return Unknown.INT;
      }
   }

   /**
    *
    */
   public long getValueAsLong()
   {
      try
      {
         return Long.parseLong(getText());
      }
      catch (NumberFormatException x)
      {
         return Unknown.LONG;
      }
   }

   /**
    *
    */
   public void setValue(int value)
   {
      if (value != Unknown.INT)
      {
         setText(Integer.toString(value));
      }
      else
      {
         setText(null);
      }
   }

   /**
    *
    */
   public void setValue(long value)
   {
      if (value != Unknown.LONG)
      {
         setText(Long.toString(value));
      }
      else
      {
         setText(null);
      }
   }

   /**
    *
    */
   private void initialize()
   {
      setMargin(new Insets(0, 2, 0, 2));
      setHorizontalAlignment(JTextField.RIGHT);

      addFocusListener(this);
   }

   /**
    *
    */
   protected void processComponentKeyEvent(KeyEvent e)
   {
      // Only perform tab pressed for focus change

      if ((e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t')
            && e.getID() == KeyEvent.KEY_PRESSED && entry != null)
      {
         try
         {
            if (!e.isShiftDown())
            {
               entry.transferFocus();
            }
            else
            {
               javax.swing.FocusManager.getCurrentManager().focusPreviousComponent(entry);
            }
         }
         catch (Exception x)
         {
         }

         e.consume();

         return;
      }

      super.processComponentKeyEvent(e);
   }

   /**
    *
    */
   public void registerOwner(AbstractDateEntry entry)
   {
      this.entry = entry;
   }
}
