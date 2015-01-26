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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.spinner.DefaultSpinRenderer;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.spinner.JSpinnerTime;


/**
 * Class for time entries using Calendar.
 */
public class TimeEntry extends JSpinnerTime implements ObservedEntry
{
   public static final int UNKNOWN_HOUR = Unknown.INT;
   public static final int UNKNOWN_MINUTE = Unknown.INT;
   public static final int UNKNOWN_SECOND = Unknown.INT;

   private int hour;
   private int minute;
   private int second;

   /**
    * TimeEntry default constructor (uses actual date)
    */
   public TimeEntry()
   {
      super(new DefaultSpinRenderer(
                  DateFormat.getTimeInstance(DateFormat.MEDIUM).format(
                        new Date()).length()),
            DateFormat.getTimeInstance(DateFormat.MEDIUM));

      initialize();
      clearTime();
   }

   /**
    * @return <code>true</code> if the content of the field is empty;
    *         <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return (UNKNOWN_HOUR == hour)
            || (UNKNOWN_MINUTE == minute)
            || (UNKNOWN_SECOND == second);
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
      ChangeEvent event = new ChangeEvent(this);

      EventListener[] listeners = listenerList.getListeners(ChangeListener.class);
      for (int i = 0; i < listeners.length; i++)
      {
         ChangeListener listener = (ChangeListener) listeners[i];
         listener.stateChanged(event);
      }
   }

   /**
    * DateEntry init (used by constructor)
    */
   private void initialize()
   {
      ChangeListener changeListener = new ChangeListener()
      {
         private boolean recursive = false;

         public void stateChanged(ChangeEvent e)
         {
            final boolean inRecursion = recursive;

            try
            {
               if (!recursive)
               {
                  recursive = true;
                  setTime(getTimeModel().getTime());
               }
            }
            finally
            {
               this.recursive = inRecursion;
            }
         }
      };
      getTimeModel().addChangeListener(changeListener);
      addChangeListener(changeListener);
   }

   /**
    * Clear time (the date stays unchanged).
    */
   public void clearTime()
   {
      setTime(null);
   }

   /**
    * Sets the hour using int.
    */
   public void setHour(int hour)
   {
      this.hour = hour;
   }

   /**
    * Returns the hour as an integer.
    */
   public int getHour()
   {
      return this.hour;
   }

   /**
    * Sets the minute.
    */
   public void setMinute(int minute)
   {
      this.minute = minute;
   }

   /**
    * returns minute as int
    */
   public int getMinute()
   {
      return minute;
   }

   /**
    * set the second using int
    */
   public void setSecond(int second)
   {
      this.second = second;
   }

   /**
    * returns second as int
    */
   public int getSecond()
   {
      return second;
   }

   public Calendar getCalendar()
   {
      Calendar calendar = Calendar.getInstance();
      calendar.clear();

      if (!isEmpty())
      {
         calendar.set(Calendar.HOUR_OF_DAY, getHour());
         calendar.set(Calendar.MINUTE, getMinute());
         calendar.set(Calendar.SECOND, getSecond());
      }

      return calendar;
   }

   public void setTime(Calendar calendar)
   {
      if (null != calendar)
      {
         setHour(calendar.get(Calendar.HOUR_OF_DAY));
         setMinute(calendar.get(Calendar.MINUTE));
         setSecond(calendar.get(Calendar.SECOND));

         getTimeModel().setTime(calendar);
      }
      else
      {
         setHour(UNKNOWN_HOUR);
         setMinute(UNKNOWN_MINUTE);
         setSecond(UNKNOWN_SECOND);

         Calendar time = Calendar.getInstance();
         time.clear();
         getTimeModel().setTime(time);
      }

      fireChangeEvent();
   }
}
