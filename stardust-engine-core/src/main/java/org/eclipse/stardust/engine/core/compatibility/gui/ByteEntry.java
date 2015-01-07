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

import java.text.ParseException;

import javax.swing.text.Document;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Class for handling and displaying numeric values in the range of a byte
 * Valid values are -127 to +127
 */
public class ByteEntry extends NumberEntry
{
   private static final Logger trace = LogManager.getLogger(ByteEntry.class);

   /**
    * Default constructor which sets the visible size to 5 characters
    */
   public ByteEntry()
   {
      super(5);
   }

   /**
    * Constructor that sets the visual size of this entry field
    * @param size The number of visible characters to be displayed
    */
   public ByteEntry(int size)
   {
      super(size);
   }

   /**
    * Constructor that sets the visual size and the mandatory flag of this entry
    * field
    * @param size The number of visible characters to be displayed
    * @param mandatory <code>true</true> if the field is supposed to be mandatory
    */
   public ByteEntry(int size, boolean mandatory)
   {
      super(size);

      this.mandatory = mandatory;

      initialize();
   }

   /**
    * Creates a default model for the type <code>byte</code>
    */
   protected Document createDefaultModel()
   {
      return new NumberDocument(Byte.class);
   }

   /**
    * Returns the value of the ByteEntry
    */
   public byte getValue()
   {
      try
      {
         if (isEmpty())
         {
            return Unknown.BYTE;
         }

         return format.parse(getText()).byteValue();
      }
      catch (ParseException e)
      {
         trace.warn("", e);
      }

      Assert.lineNeverReached();

      return Unknown.BYTE;
   }

   /**
    *  Set the value of the ByteEntry to be displayed
    *  @param value The new value
    */
   public void setValue(byte value)
   {
      if (!isEnabled())
      {
         return;
      }

      if (value == Unknown.BYTE)
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
    * Generic setter method
    * @param value The value as an Object of type Byte. If <code>null</code>
    Unkown.BYTE will be used
    * @throws IllegalArgumentException
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         setValue(Unknown.BYTE);
         performFlags();
      }
      else if (value instanceof String)
      {
         value = new Byte((String) value);
      }
      else if (value instanceof Byte)
      {
         setValue(((Byte) value).byteValue());
         performFlags();
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + value.getClass() + "\" of the argument is not compatible with java.lang.Byte.");
      }
   }

   /**
    * Generic getter Method
    * Note: returns valid Byte or Byte.byteValue == Unknown.BYTE
    */
   public Object getObjectValue()
   {
      if (getValue() == Unknown.BYTE)
      {
         return null;
      }

      return new Byte(getValue());
   }
}
