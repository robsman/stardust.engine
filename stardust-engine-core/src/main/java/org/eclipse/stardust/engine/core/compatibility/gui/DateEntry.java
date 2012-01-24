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

import java.util.Calendar;
import java.util.Date;

/**
 * Entry representing java.util.Calendar values.
 */
public class DateEntry extends AbstractDateEntry
{
   /**
    * DateEntry default constructor (uses actual date)
    */
   public DateEntry()
   {
      this(false);
   }

   /**
    * DateEntry constructor.
    */
   public DateEntry(boolean mandatory)
   {
      super(mandatory);
   }

   /**
    * DateEntry constructor (use day, month, year & mandatory).
    */
   public DateEntry(int day, int month, int year, boolean mandatory)
   {
      super(day, month, year, mandatory);
   }

   /**
    * Sets date as calendar.
    */
   public void setValue(Date date)
   {
      if (date != null)
      {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
         setCalendar(calendar);
      }
      else
      {
         clearDate();
      }

      performFlags();
   }

   /**
    * Gets date as Calendar.
    *
    *	@return Returns null for an uninitialized entry.
    */
   public Date getValue()
   {
      Calendar calendar =  getCalendar();
      if (calendar != null)
      {
         return calendar.getTime();
      }
      else
      {
         return null;
      }
   }

   /**
    *
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null || value instanceof Date)
      {
         setValue((Date) value);
      }
      else if (value instanceof String)
      {
         setValue(new Date((String) value));
      }
      else
      {
         throw new IllegalArgumentException("" + value.getClass() + " is not compatible with java.util.Date.");
      }
   }

   public Object getObjectValue()
   {
      return getValue();
   }
}
