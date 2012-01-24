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


/**
 * Entry field for editing short values.
 */
public class ShortEntry extends NumberEntry
{
   /**
    * Default constructor
    */
   public ShortEntry()
   {
      super();
   }

   /**
    * Constructor that sets the visual size of this entry field.
    */
   public ShortEntry(int size)
   {
      super(size);
   }

   /**
    * Constructor that sets the visual size and the mandatory flag of this entry
    * field
    */
   public ShortEntry(int size, boolean mandatory)
   {
      super(size, mandatory);
   }

   /**
    * Create a default model that allows only numbers
    */
   protected Document createDefaultModel()
   {
      return new NumberDocument(Short.class);
   }

   /**
    *
    */
   public short getValue()
   {
      try
      {
         if (isEmpty())
         {
            return Unknown.SHORT;
         }

         return format.parse(getText()).shortValue();
      }
      catch (ParseException e)
      {
      }

      Assert.lineNeverReached();

      return 0;
   }

   /**
    *
    */
   public void setValue(short value)
   {
      if (!isEnabled())
      {
         return;
      }

      if (value == Unknown.SHORT)
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
    *
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         setValue(Unknown.SHORT);
         performFlags();
      }
      else if (value instanceof String)
      {
         setValue(Short.parseShort((String) value));
         performFlags();
      }
      else if (value instanceof Short)
      {
         setValue(((Short) value).shortValue());
         performFlags();
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + value.getClass() + "\" of the argument is not compatible with java.lang.Short.");
      }
   }

   /**
    *
    */
   public Object getObjectValue()
   {
      if (getValue() == Unknown.SHORT)
      {
         return null;
      }

      return new Short(getValue());
   }
}
