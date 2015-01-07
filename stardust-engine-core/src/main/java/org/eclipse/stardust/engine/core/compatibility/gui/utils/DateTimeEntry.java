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

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Class for Date Entries.
 */
public class DateTimeEntry extends JComponent implements ObservedEntry
{
   /**
    * Visible data fields.
    */
   private DateEntry dateEntry;
   private TimeEntry timeEntry;

   /**
    * Default constructor (initializes to current date).
    */
   public DateTimeEntry()
   {
      initialize();
   }

   /**
    * DateEntry constructor (use day, month, year).
    */
   public DateTimeEntry(int day, int month, int year)
   {
      this();

      setDate(day, month, year);
   }

   /**
    * @return <code>true</code> if the content of the field is empty;
    *         <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return dateEntry.isEmpty() || timeEntry.isEmpty();
   }

   /**
    * Gets input status.
    */
   public boolean isEditable()
   {
      return dateEntry.isEditable();
   }

   public void addChangeListener(ChangeListener listener)
   {
      listenerList.add(ChangeListener.class, listener);
   }

   public void removeChangeListener(ChangeListener listener)
   {
      listenerList.remove(ChangeListener.class, listener);
   }

   private void fireChangeEvent()
   {
      final ChangeEvent event = new ChangeEvent(this);

      EventListener[] listeners = listenerList.getListeners(ChangeListener.class);
      for (int i = 0; i < listeners.length; i++)
      {
         ChangeListener listener = (ChangeListener) listeners[i];
         listener.stateChanged(event);
      }
   }

   /**
    * Adds focus listener.
    */
   public void addFocusListener(FocusListener listener)
   {
      final FocusListener thisListener = listener;

      if (null != dateEntry)
      {
         final FocusListener listen = new FocusListener()
         {
            public void focusGained(FocusEvent e)
            {
            }

            public void focusLost(FocusEvent e)
            {
               thisListener.focusLost(e);
            }
         };

         dateEntry.addFocusListener(listen);
         timeEntry.addFocusListener(listen);
      }

      super.addFocusListener(listener);
   }

   /**
    * Clears the date field and sets the field state to uninitialized.
    */
   public void clearDate()
   {
      dateEntry.setCalendar((Calendar) null);
      timeEntry.setTime(null);

      fireChangeEvent();
   }

   /**
    * Gets the internal date object.
    */
   public Calendar getCalendar()
   {
      Calendar calendar = null;
      if (!isEmpty())
      {
         calendar = dateEntry.getCalendar();

         calendar.set(Calendar.HOUR_OF_DAY, timeEntry.getHour());
         calendar.set(Calendar.MINUTE, timeEntry.getMinute());
         calendar.set(Calendar.SECOND, timeEntry.getSecond());
      }

      return calendar;
   }

   /**
    * @return Day as int. If the date entry is uninitialized <code>Unknown.BYTE</code>
    *         is returned.
    */
   public int getDay()
   {
      return dateEntry.getDay();
   }

   /**
    * @return Month as int. If the date entry is uninitialized <code>Unknown.BYTE</code>
    *         is returned.
    */
   public int getMonth()
   {
      return dateEntry.getMonth();
   }

   /**
    * @return Year as int. If the date entry is uninitialized <code>Unknown.SHORT</code>
    *         is returned.
    */
   public int getYear()
   {
      return dateEntry.getYear();
   }

   /**
    * Overloads has focus.
    */
   public boolean hasFocus()
   {
      return dateEntry.hasFocus()
            || timeEntry.hasFocus()
            || super.hasFocus();
   }

   /**
    * DateEntry init (used by constructor)
    */
   private void initialize()
   {
      // Add fields and size them

      ChangeListener changeListener = new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            fireChangeEvent();
         }
      };

      dateEntry = new DateEntry();
      dateEntry.addChangeListener(changeListener);

      timeEntry = new TimeEntry();
      timeEntry.clearTime();
      timeEntry.addChangeListener(changeListener);

      // Add layout and fields
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      add(dateEntry);
      add(timeEntry);
      setBorder(null);
      setOpaque(false);

      // Check mandatory and isEditable

      fireChangeEvent();
   }

   /**
    * Remove focus listener.
    */
   public void removeFocusListener(FocusListener listener)
   {
      if (dateEntry != null)
      {
         dateEntry.removeFocusListener(listener);
         timeEntry.removeFocusListener(listener);
      }

      super.removeFocusListener(listener);
   }

   /**
    *
    */
   public void requestFocus()
   {
      dateEntry.requestFocus();
   }

   /**
    * Set content to a valid date.
    */
   public void setCalendar(Calendar calendar)
   {
      if (null != dateEntry)
      {
         dateEntry.setCalendar(calendar);
         timeEntry.setTime(calendar);
      }

      fireChangeEvent();
   }

   /**
    * Set content to a valid date.
    */
   public final void setDate(int day, int month, int year)
   {
      if (null != dateEntry)
      {
         dateEntry.setDay(day);
         dateEntry.setMonth(month);
         dateEntry.setYear(year);

         timeEntry.clearTime();
      }

      fireChangeEvent();
   }

   /**
    * Sets the day using int.
    */
   public void setDay(int day)
   {
      if (null != dateEntry)
      {
         dateEntry.setDay(day);
      }

      fireChangeEvent();
   }

   /**
    * Sets the month using int.
    */
   public void setMonth(int month)
   {
      if (null != dateEntry)
      {
         dateEntry.setMonth(month);
      }
   }

   /**
    * Set the year using int.
    */
   public void setYear(int year)
   {
      if (null != dateEntry)
      {
         dateEntry.setYear(year);
      }
   }

   /**
    * Sets all areas editable.
    */
   public void setEditable(boolean isEditable)
   {
      dateEntry.setEditable(isEditable);

      fireChangeEvent();
   }

   /**
    * En/disables input.
    */
   public void setEnabled(boolean isEnabled)
   {
      super.setEnabled(isEnabled);

      dateEntry.setEnabled(isEnabled);
      timeEntry.setEnabled(isEnabled);

      fireChangeEvent();
   }

   /**
    * Sets the background color of this entry.
    */
   public void setBackground(Color color)
   {
      super.setBackground(color);

      if (null != dateEntry)
      {
         dateEntry.setBackground(color);
         timeEntry.setBackground(color);
      }
   }

   /**
    * Sets the foreground color of this entry.
    */
   public void setForeground(Color color)
   {
      super.setForeground(color);

      if (null != dateEntry)
      {
         dateEntry.setForeground(color);
         timeEntry.setForeground(color);
      }
   }

   /**
    * Gets the entry from day, month and year entry, checks and corrects the
    * date and refreshes the entry content accordingly.
    */
   protected void validateDate()
   {
      setCalendar(getCalendar());
   }
}
