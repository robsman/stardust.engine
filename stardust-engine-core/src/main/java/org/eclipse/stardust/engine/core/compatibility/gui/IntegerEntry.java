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
 *	Entry for displaying and editing integer entries.
 */
public class IntegerEntry extends NumberEntry
{
   /**
    *	Default constructor.
    */
   public IntegerEntry()
   {
      super();
   }

   /**
    * Constructor that sets the visual size of this entry field.
    * @param size The visual size of the field
    */
   public IntegerEntry(int size)
   {
      super(size);
   }

   /**
    * Constructor that sets the visual size and the mandatory flag of this entry
    * field.
    * @param size The visual size of the field
    * @param mandatory Indicates if this field has to be mandatory
    */
   public IntegerEntry(int size, boolean mandatory)
   {
      super(size, mandatory);
   }

   /**
    * Create a default model that allows only numbers.
    * @return Returns the model of the field of type Document
    */
   protected Document createDefaultModel()
   {
      return new NumberDocument(Integer.class);
   }

   /**
    * Getter method.
    */
   public int getValue()
   {
      try
      {
         if (isEmpty())
         {
            return Unknown.INT;
         }

         return format.parse(getText()).intValue();
      }
      catch (ParseException e)
      {
      }

      Assert.lineNeverReached();

      return 0;
   }

   /**
    * Setter method.
    */
   public void setValue(int value)
   {
      if (value == Unknown.INT)
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
    * Generic setter method. Expects an object of type
    * <code>java.lang.Integer</code>.
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         setValue(Unknown.INT);
         performFlags();
      }
      else if (value instanceof String)
      {
         setValue(Integer.parseInt((String) value));
         performFlags();
      }
      else if (value instanceof Integer)
      {
         setValue(((Integer) value).intValue());
         performFlags();
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + value.getClass() + "\" of the argument is not compatible with java.lang.Integer.");
      }
   }

   /**
    *	Generic getter method.
    */
   public Object getObjectValue()
   {
      if (getValue() == Unknown.INT)
      {
         return null;
      }

      return new Integer(getValue());
   }
}
