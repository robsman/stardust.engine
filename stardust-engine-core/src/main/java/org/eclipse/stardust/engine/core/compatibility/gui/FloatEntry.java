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
/**
 * @author Mark Gille, j.talk() GmbH
 * @version 	%I%, %G%
 */

package org.eclipse.stardust.engine.core.compatibility.gui;

import java.text.ParseException;

import javax.swing.text.Document;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Unknown;


/**
 * Entry for float values.
 */
public class FloatEntry extends NumberEntry
{
   /**
    * Default constructor.
    */
   public FloatEntry()
   {
      super();
   }

   /**
    * Constructor that sets the visual size of this entry field.
    * @param size The visual size of the field
    */
   public FloatEntry(int size)
   {
      super(size);
   }

   /**
    * Constructor that sets the visual size and the mandatory flag of
    * this entry field.
    * @param size The visual size of the field
    * @param mandatory Is this field mandatory?
    */
   public FloatEntry(int size, boolean mandatory)
   {
      super(size, mandatory);
   }

   /** Create a default model that allows only numbers. */
   protected Document createDefaultModel()
   {
      return new NumberDocument(Float.class);
   }

   /**
    * Gets the value of the Field of type float. If <code>null</code> returns Unknown.FLOAT
    */
   public float getValue()
   {
      try
      {
         if (isEmpty())
         {
            return Unknown.FLOAT;
         }
         return (format.parse(getText())).floatValue();
      }
      catch (ParseException e)
      {
      }

      Assert.lineNeverReached();

      return 0.0f;
   }

   /**
    * Sets the value of type float for this field.
    * @param value The float value for this field
    */
   public void setValue(float value)
   {
      if (value == Unknown.FLOAT)
      {
         setText("");
      }
      else
      {
         // Hint: We must use the instance of BigDecimal to get the correct
         //       formatted text.
         //       Otherwise special values like "0.2" are formatted "0,200064922"
         setText(format.format(new java.math.BigDecimal(Float.toString(value))));

      }

      performFlags();
   }

   /**
    * Generic setter method. Expects an object of type
    * <code>java.lang.Float</code>.
    * @param value The value of Type Float. If <code>null</code> value will be Unknown.FLOAT
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         setValue(Unknown.FLOAT);
         performFlags();
      }
      else if (value instanceof String)
      {
         setValue(Float.parseFloat((String) value));
         performFlags();
      }
      else if (value instanceof Float)
      {
         setValue(((Float) value).floatValue());
         performFlags();
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + value.getClass() + "\" of the argument is not compatible with java.lang.Float.");
      }
   }

   /**
    * Generic getter method.
    * @return Returns valid <code>Float</code> object. If the entry is empty,
    *         <code>Float.floatValue()</code> returns <code>Unknown.FLOAT</code>
    */
   public Object getObjectValue()
   {
      if (getValue() == Unknown.FLOAT)
      {
         return null;
      }

      return new Float(getValue());
   }
}
