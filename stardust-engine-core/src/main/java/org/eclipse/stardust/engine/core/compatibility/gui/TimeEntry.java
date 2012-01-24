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


import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.InternalException;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.Calendar;
import java.util.Date;

/**
 * Class for time entries using Calendar.
 */
public class TimeEntry extends AbstractDateEntry
{
   public static final int UNKNOWN_HOUR = Unknown.INT;
   public static final int UNKNOWN_MINUTE = Unknown.INT;
   public static final int UNKNOWN_SECOND = Unknown.INT;

   static protected final String HOUR_LABEL = ":";
   static protected final String MINUTE_LABEL = ":";

   static protected final String LEADING_NULL_CHAR = "0";

   /**
    * label for hours
    */
   JLabel hoursLabel;
   /**
    * label for minutes
    */
   JLabel minutesLabel;

   // visible data fields
   DateTextField hourField;
   DateTextField minuteField;
   DateTextField secondField;

   JPanel timePanel;

   /** boolean that indicates status to allow overloading of setOpaque */
   boolean guiInitialized = false;

   /**
    * TimeEntry default constructor (uses actual date)
    */
   public TimeEntry()
   {
      this(false);
   }

   /**
    * TimeEntry constructor
    */
   public TimeEntry(boolean mandatory)
   {
      super(mandatory);

      initialize();
   }

   /**
    * Completes a uncomplete entered date to the first of january of the
    * current year (0:00 hour)
    *
    */
   public void focusLost(FocusEvent e)
   {
      if (isEditable() && !isReadonly())
      {
         if (!isEmpty())
         {
            validateDate();
         }
      }
   }

   /**
    * DateEntry init (used by constructor)
    */
   void initialize()
   {
      // Init labels

      hoursLabel = new JLabel(HOUR_LABEL);

      minutesLabel = new JLabel(MINUTE_LABEL);

      // Add layout and fields

      hourField = new DateTextField(2, 0, 23);
      hourField.setBorder(null);
      hourField.setOpaque(false);

      minuteField = new DateTextField(2, 0, 59);
      minuteField.setBorder(null);
      minuteField.setOpaque(false);

      secondField = new DateTextField(2, 0, 59);
      secondField.setBorder(null);
      secondField.setOpaque(false);

      // panel for right alignment (BorderPanel.East)
      timePanel = new JPanel();
      timePanel.setBackground(Color.white);
      timePanel.setBorder(
            BorderFactory.createLineBorder(Color.gray));
      //timePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      //      Color.white, Color.lightGray, Color.black, Color.gray));

      timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
      timePanel.add(hourField);
      timePanel.add(hoursLabel);
      timePanel.add(minuteField);
      timePanel.add(minutesLabel);
      timePanel.add(secondField);

      add(timePanel);

      // Check mandatory & isEditable

      performFlags();

      // Set cursor

      hourField.setCursor(GUI.ENTRY_CURSOR);
      minuteField.setCursor(GUI.ENTRY_CURSOR);
      secondField.setCursor(GUI.ENTRY_CURSOR);
      setCursor(GUI.ENTRY_CURSOR);

      // At laest set the max. size

      setMaximumSize(getPreferredSize());
   }

   /**
    * Set date as Calendar.
    */
   public void setValue(Calendar calendar)
   {
      setCalendar(calendar);

      if (calendar != null)
      {
         setHour(calendar.get(Calendar.HOUR_OF_DAY));
         setMinute(calendar.get(Calendar.MINUTE));
         setSecond(calendar.get(Calendar.SECOND));
      }
      else
      {
         clearTime();
      }

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
    * get time as Calendar <P>
    * Note: can return null <=> not initialized
    */
   public Calendar getValue()
   {
      boolean isValidTime = true;

      if (getHour() == UNKNOWN_HOUR
            || getMinute() == UNKNOWN_MINUTE
            || getSecond() == UNKNOWN_SECOND)
      {
         isValidTime = false;
      }

      Calendar calendar = super.getCalendar();

      if (calendar != null)
      {
         if (isValidTime)
         {
            calendar.set(Calendar.HOUR_OF_DAY, getHour());
            calendar.set(Calendar.MINUTE, getMinute());
            calendar.set(Calendar.SECOND, getSecond());
         }
         else
         {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
         }
      }

      return calendar;
   }

   /**
    * generic setter Method / implements abstract super method <p>
    * Note: object == null <=> uninitialized state
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null || value instanceof Calendar)
      {
         setValue((Calendar) value);
      }
      else
      {
         throw new IllegalArgumentException("not a java.util.Calendar");
      }
   }

   /** generic getter Method / implements abstract super method */
   public Object getObjectValue()
   {
      return getValue();
   }

   /** set BackgroundColor of this entry */
   public void setBackground(Color color)
   {
      super.setBackground(color);

      if (guiInitialized)
      {
         timePanel.setBackground(color);
      }
   }

   /**
    * Clear time and date.
    */
   public void clearDate()
   {
      super.clearDate();
      clearTime();
   }

   /**
    * Clear time (the date stays unchanged).
    */
   public void clearTime()
   {
      if (hourField != null)
      {
         hourField.setValue(Unknown.INT);
      }
      if (minuteField != null)
      {
         minuteField.setValue(Unknown.INT);
      }
      if (secondField != null)
      {
         secondField.setValue(Unknown.INT);
      }
   }

   /**
    * @return  <code>true</code> if the content of the field is empty;
    *          <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return super.isEmpty();
   }

   public boolean isEmptyTime()
   {
      return (UNKNOWN_HOUR == getHour())
            || (UNKNOWN_MINUTE == getMinute())
            || (UNKNOWN_SECOND == getSecond());
   }

   /**
    * Sets the hour using int.
    */
   public void setHour(int hour)
   {
      Assert.isNotNull(hourField);

      if (hour == UNKNOWN_HOUR)
      {
         hourField.setValue(Unknown.INT);
      }
      else
      {
         hourField.setValue(hour);
      }
   }

   /**
    * Returns the hour as an integer.
    */
   public int getHour()
   {
      int currentHour = UNKNOWN_HOUR;

      if ((hourField != null) && (Unknown.INT != hourField.getValue()))
      {
         currentHour = hourField.getValue();
      }
      return currentHour;
   }

   /**
    * Sets the minute.
    */
   public void setMinute(int minute)
   {
      Assert.isNotNull(minuteField);

      String minString = null;

      if (minute != UNKNOWN_MINUTE)
      {
         if (minute < 10)
         {
            minString = LEADING_NULL_CHAR + Integer.toString(minute);
         }
         else
         {
            minString = Integer.toString(minute);
         }
      }
      minuteField.setText(minString);
   }

   /**
    * returns minute as int
    */
   public int getMinute()
   {
      int currentMin = UNKNOWN_MINUTE;

      if ((minuteField != null) && (Unknown.INT != minuteField.getValue()))
      {
         currentMin = minuteField.getValue();
      }
      return currentMin;
   }

   /**
    * set the second using int
    */
   public void setSecond(int second)
   {
      Assert.isNotNull(secondField);

      String secString = null;
      if (second != UNKNOWN_SECOND)
      {
         if (second < 10)
         {
            secString = LEADING_NULL_CHAR + Integer.toString(second);
         }
         else
         {
            secString = Integer.toString(second);
         }
      }
      secondField.setText(secString);
   }

   /**
    * returns second as int
    */
   public int getSecond()
   {
      int currentSec = UNKNOWN_SECOND;

      if ((secondField != null) && (Unknown.INT != secondField.getValue()))
      {
         currentSec = secondField.getValue();
      }
      return currentSec;
   }

   /**
    * performFlags
    */
   public void performFlags()
   {
      if (!isEnabled())
      {
         if (timePanel != null)
         {
            timePanel.setBackground(GUI.DisabledColor);
         }
         setForeground(GUI.DisabledTextColor);
      }
      else if (isReadonly())
      {
         if (timePanel != null)
         {
            timePanel.setBackground(GUI.ReadOnlyColor);
         }
         setForeground(GUI.ReadOnlyTextColor);
      }
      else // regular state
      {
         if (timePanel != null)
         {
            timePanel.setBackground(GUI.DefaultColor);
         }
         setForeground(GUI.DefaultTextColor);
      }

      super.performFlags();

      repaint();
   }

   /**
    * Sets the foreground color of this entry.
    */
   public void setForeground(Color color)
   {
      if (timePanel != null)
      {
         try
         {
            hourField.setForeground(color);
            hoursLabel.setForeground(color);
            minuteField.setForeground(color);
            minutesLabel.setForeground(color);
            secondField.setForeground(color);
         }
         catch (Exception _ex)
         {
            throw new InternalException(_ex);
         }
      }
   }

   public Calendar getCalendar()
   {
      Calendar calendar = super.getCalendar();

      if (null != calendar)
      {
         Assert.condition(!isEmpty());

         if (!isEmptyTime())
         {
            calendar.set(Calendar.HOUR_OF_DAY, getHour());
            calendar.set(Calendar.MINUTE, getMinute());
            calendar.set(Calendar.SECOND, getSecond());
         }
         else
         {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
         }
         calendar.set(Calendar.MILLISECOND, 0);
      }
      return calendar;
   }

   public void setCalendar(Calendar calendar)
   {
      super.setCalendar(calendar);
      if (null != calendar)
      {
         setHour(calendar.get(Calendar.HOUR_OF_DAY));
         setMinute(calendar.get(Calendar.MINUTE));
         setSecond(calendar.get(Calendar.SECOND));
      }
      else
      {
         setHour(UNKNOWN_HOUR);
         setMinute(UNKNOWN_MINUTE);
         setSecond(UNKNOWN_SECOND);
      }
      performFlags();
   }

   /**
    * Set the date as described with the parameter and set the time to 0:00.00
    */
   public void setDate(int day, int month, int year)
   {
      super.setDate(day, month, year);

      setHour(0);
      setMinute(0);
      setSecond(0);
   }

   /**
    * Sets all areas editable.
    */
   public void setEditable(boolean isEditable)
   {
      super.setEditable(isEditable);

      hourField.setEditable(isEditable);
      minuteField.setEditable(isEditable);
      secondField.setEditable(isEditable);
   }

   /**
    * En/disables input.
    */
   public void setEnabled(boolean isEnabled)
   {
      super.setEnabled(isEnabled);

      GUI.setEnableState(timePanel, isEnabled);
   }

   /**
    *
    */
   public void setReadonly(boolean isReadonly)
   {
      super.setReadonly(isReadonly);

      hourField.setEditable(!isReadonly);
      minuteField.setEditable(!isReadonly);
      secondField.setEditable(!isReadonly);
   }
}
