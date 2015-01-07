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

import javax.swing.JPasswordField;

/** Use this entry for entering passwords. For any given char an asterisk is
 displayed*/
public class PasswordEntry extends AbstractEntry
{
   private static final int DEFAULT_CHAR_SIZE = 20;

   /** set a string to the entry */
   public PasswordEntry()
   {
      this(DEFAULT_CHAR_SIZE, false);
   }

   /** set a string to the entry */
   public PasswordEntry(String string)
   {
      super(new JPasswordField(string), false, false);

      if (string != null)
      {
         setMaximumSize(getPreferredSize());
      }
      setCursor(GUI.ENTRY_CURSOR);
   }

   /** construct a password field as big as <code>size</code> chars */
   public PasswordEntry(int size)
   {
      this(size, false);
   }

   /** construct a password field and set the mandatory flag */
   public PasswordEntry(int size, boolean isMandatory)
   {
      super(new JPasswordField(size), isMandatory, false);

      setMaximumSize(getPreferredSize());
      setCursor(GUI.ENTRY_CURSOR);
   }

   /** setter Method */
   public void setValue(String value)
   {
      setText(value);
   }

   /** getter Method */
   public String getValue()
   {
      return getText();
   }

   /** generic setter Method */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (null == value)
      {
         value = "";
      }
      else if (!(value instanceof String))
      {
         throw new IllegalArgumentException("'" + value + "' is not a String");
      }

      setValue((String) value);
   }

   /** generic getter Method */
   public Object getObjectValue()
   {
      return getValue();
   }

   /** toStringMethod */
   public String toString()
   {
      return getValue();
   }

}