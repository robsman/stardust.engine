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
package org.eclipse.stardust.engine.core.compatibility.gui.utils;

import java.util.Calendar;
import java.util.Date;

import javax.swing.InputVerifier;

import org.eclipse.stardust.engine.core.compatibility.gui.AbstractDateEntry;


/**
 * Entry representing java.util.Calendar values.
 */
public class CalendarEntry extends AbstractDateEntry
{
   /**
    * DateEntry default constructor (uses actual date)
    */
   public CalendarEntry()
   {
      this(false);
   }

   /**
    * DateEntry constructor.
    */
   public CalendarEntry(boolean mandatory)
   {
      super(mandatory);
   }

   /**
    * DateEntry constructor (use day, month, year & mandatory).
    */
   public CalendarEntry(int day, int month, int year, boolean mandatory)
   {
      super(day, month, year, mandatory);
   }

   /**
    * Sets date as calendar.
    */
   public void setValue(Calendar calendar)
   {
      if (calendar != null)
      {
         setCalendar(calendar);
      }
      else
      {
         clearDate();
      }

      performFlags();
   }

   public void setValue(Date date)
   {
      if (date != null)
      {
         Calendar value = Calendar.getInstance();
         value.setTime(date);
         setValue(value);
      }
      else
      {
         clearDate();
         performFlags();
      }
   }

   /**
    * Gets date as Calendar.
    *
    *	@return Returns null for an uninitialized entry.
    */
   public Calendar getValue()
   {
      return getCalendar();
   }

   /**
    *
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null || value instanceof Calendar)
      {
         setValue((Calendar) value);
      }
      else
      {
         throw new IllegalArgumentException("" + value.getClass() + " is not compatible with java.util.Calendar.");
      }
   }

   /**
    * Gets date as Calendar.
    *
    *	@return Returns null for an uninitialized entry.
    */
   public Object getObjectValue()
   {
      return getValue();
   }

   public Date getDate()
   {
      Calendar value = getCalendar();
      if (value == null)
      {
         return null;
      }
      return value.getTime();
   }

   public void setInputVerifier(InputVerifier verifier)
   {
      super.setInputVerifier(verifier);
      dayField.setInputVerifier(verifier);
      monthField.setInputVerifier(verifier);
      yearField.setInputVerifier(verifier);
   }
}
