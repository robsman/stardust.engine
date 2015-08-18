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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.eclipse.stardust.common.Unknown;


/**
 * Class for editing character entries.
 */
public class CharEntry extends AbstractEntry
{
   /**
    * Default constructor with default size of 1 character
    */
   public CharEntry()
   {
      super(1);

      setHorizontalAlignment(JTextField.RIGHT);
   }

   /**
    * Constructor that sets the visual size of this entry field
    * @param size The witdh in characters of the field
    */
   public CharEntry(int size)
   {
      super(size);

      setHorizontalAlignment(JTextField.RIGHT);
   }

   /**
    * Constructor that sets the visual size and the mandatory flag of this entry
    * field
    * @param size The width in characters of the field
    * @param mandatory Is the field mandatory?
    */
   public CharEntry(int size, boolean mandatory)
   {
      super(size, mandatory);

      setHorizontalAlignment(JTextField.RIGHT);
   }

   /**
    * Create a default model that allows only single characters.
    */
   protected Document createDefaultModel()
   {
      return new CharacterDocument();
   }

   /**
    * Returns the value of this field as a primitive character
    * Note: Returns Unknown.CHAR if the field is empty
    */
   public char getValue()
   {
      if (isEmpty())
      {
         return Unknown.CHAR;
      }

      return getText().charAt(0);
   }

   /**
    * Set the value of the field
    * @param value The character value
    */
   public void setValue(char value)
   {
      if (!isEnabled())
      {
         return;
      }

      if (value == Unknown.CHAR)
      {
         setText("");
      }
      else
      {
         setText("" + value);
      }

      performFlags();
   }

   /**
    * Generic setter
    * @param value the value of type Character. If <code>null</code> then value will be Unknown.CHAR
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         setValue(Unknown.CHAR);
         performFlags();
      }
      else if (value instanceof Character)
      {
         setValue(((Character) value).charValue());
         performFlags();
      }
      else if (value instanceof String)
      {
         setValue(((String) value).charAt(0));
         performFlags();
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + value.getClass() + "\" of the argument is not compatible with java.lang.Character.");
      }
   }

   /**
    * Generic getter. Returns the Character object
    */
   public Object getObjectValue()
   {
      if (Unknown.CHAR != getValue())
      {
         return new Character(getValue());
      }
      else
      {
         return null;
      }
   }
}

/**
 * Document to restrict entry content to single character.
 */
class CharacterDocument extends PlainDocument
{
   /**
    * Handles string insertion
    */
   public void insertString(int offs, String str, AttributeSet a)
         throws BadLocationException
   {
      if (str == null ||
            str.length() == 0)
      {
         super.remove(0, 1);
      }
      else if (getLength() == 0)
      {
         super.insertString(0, "" + str.charAt(str.length() - 1), null);
      }
   }
}
