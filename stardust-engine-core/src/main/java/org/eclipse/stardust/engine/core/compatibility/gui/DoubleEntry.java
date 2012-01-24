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
 * Class for editing double entries / not completed now need other Model
 */
public class DoubleEntry extends NumberEntry
{
   /** Default constructor */
   public DoubleEntry()
   {
      super();
   }

   /** Constructor that sets the visual size of this entry field
    *  @param size The visual size of the entry field
    */
   public DoubleEntry(int size)
   {
      super(size);
   }

   /** Constructor that sets the visual size and the mandatory flag of this entry
    *  field
    *  @param size The visual size of the entry field
    *  @param mandatory Is the field mandatory?
    */
   public DoubleEntry(int size, boolean mandatory)
   {
      super(size, mandatory);
   }

   /** Create a default model that allows only numbers
    */
   protected Document createDefaultModel()
   {
      return new NumberDocument(Double.class);
   }

   /**
    * Gets the field's value as a double
    */
   public double getValue()
   {
      try
      {
         if (isEmpty())
         {
            return Unknown.DOUBLE;
         }
         return format.parse(getText()).doubleValue();
      }
      catch (ParseException e)
      {
      }

      Assert.lineNeverReached();

      return 0.0;
   }

   /**
    * Sets the field's value and accepts only double as a type
    * @param value The double value
    */
   public void setValue(double value)
   {
      if (value == Unknown.DOUBLE)
      {
         setText("");
      }
      else
      {
         setText(format.format(value));
      }

      performFlags();
   }

   /**
    * Generic setter
    * @param value An object of type Double. If <code>null</code> the value will be
    *        Unknown.DOUBLE
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         setValue(Unknown.DOUBLE);
         performFlags();
      }
      else if (value instanceof String)
      {
         setValue(Double.parseDouble((String) value));
         performFlags();
      }
      else if (value instanceof Double)
      {
         setValue(((Double) value).doubleValue());
         performFlags();
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + value.getClass() + "\" of the argument is not compatible with java.lang.Double.");
      }
   }

   /**
    * Generic getter which returns a Double object
    */
   public Object getObjectValue()
   {
      if (getValue() == Unknown.DOUBLE)
      {
         return null;
      }

      return new Double(getValue());
   }
}
