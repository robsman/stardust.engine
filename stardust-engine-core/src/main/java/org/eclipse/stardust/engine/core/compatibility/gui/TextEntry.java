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

/**
 *	An entry field for arbitrary text.
 */
public class TextEntry extends AbstractEntry
{
   /**
    *	Default constructor. The created text entry will require the maximum space
    * in a container.
    */
   public TextEntry()
   {
      super();
      performFlags();

   }

   /**
    * Creates an entry field with a length of <code>value</code>.
    */
   public TextEntry(String value)
   {
      super(value.length());
      performFlags();
   }

   /**
    * Creates an entry field with a length of a string containing
    *	<code>size</code> times 'M'.
    */
   public TextEntry(int size)
   {
      super(size);
      performFlags();
   }

   /**
    * Creates an entry field with a length of a string containing
    *	<code>size</code> times 'M'. The entry is set mandatory.
    */
   public TextEntry(int size, boolean mandatory)
   {
      super(size, mandatory);
      performFlags();
   }

   /**
    *	@return String content of the entry. <code>null</code> if the content is
    *         uninitialized.
    */
   public String getValue()
   {
      String text = getText();

      if (text.length() == 0)
      {
         return null;
      }
      else
      {
         return getText();
      }
   }

   /**
    * Sets the content of the entry to <code>text</code>. If <code>text</code> is
    * <code>null</code>, the entry will be empty.
    */
   public void setValue(String text)
   {
      if (!isEnabled())
      {
         return;
      }

      if (text == null)
      {
         setText("");
      }
      else
      {
         setText(text);
      }
   }

   /**
    * Generic setter method.
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null || value instanceof String)
      {
         setValue((String) value);
      }
      else
      {
         setValue(value.toString());
      }
   }

   /**
    *	Generic getter Method.
    *
    *	@return Always returns a <code>String</code> object, even if the entry is
    *	               empty.
    */
   public Object getObjectValue()
   {
      return getValue();
   }
}
