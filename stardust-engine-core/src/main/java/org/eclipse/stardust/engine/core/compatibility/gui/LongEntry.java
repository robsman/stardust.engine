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
 * Class for editing long values.
 */
public class LongEntry extends NumberEntry
{
   /**
    * Default constructor
    */
   public LongEntry()
   {
      super();
   }

   /**
    * Constructor that sets the visual size of this entry field
    */
   public LongEntry(int size)
   {
      super(size);
   }

   /**
    * constructor that sets the visual size and the mandatory flag of this entry
    * field
    */
   public LongEntry(int size, boolean mandatory)
   {
      super(size, mandatory);
   }

   /**
    * Create a default model that allows only numbers
    */
   protected Document createDefaultModel()
   {
      return new NumberDocument(Long.class);
   }

   /**
    *
    */
   public long getValue()
   {
      if (isEmpty())
      {
         return Unknown.LONG;
      }

      try
      {
         return format.parse(getText()).longValue();
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
   public void setValue(long value)
   {
      // @ todo something is going wrong
      /**
       if (!isEnabled())
       {
       return;
       }
       ***/

      if (value == Unknown.LONG)
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
         setValue(Unknown.LONG);
         performFlags();
      }
      else if (value instanceof String)
      {
         setValue(Long.parseLong((String) value));
         performFlags();
      }
      else if (value instanceof Long)
      {
         setValue(((Long) value).longValue());
         performFlags();
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + value.getClass() + "\" of the argument is not compatible with java.lang.Long.");
      }
   }

   /**
    *
    */
   public Object getObjectValue()
   {
      if (getValue() == Unknown.LONG)
      {
         return null;
      }

      return new Long(getValue());
   }
}
