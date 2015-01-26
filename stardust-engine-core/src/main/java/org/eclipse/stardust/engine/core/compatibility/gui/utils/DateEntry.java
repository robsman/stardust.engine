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

import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.calendar.CalendarMonth;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.calendar.JCalendar;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.calendar.SimpleCalendarRenderer;


/**
 *	Class for Date Entries.
 */
public class DateEntry extends JComponent implements ObservedEntry
{
   public static final int UNKNOWN_DAY = Unknown.BYTE;
   public static final int UNKNOWN_MONTH = Unknown.BYTE;
   public static final int UNKNOWN_YEAR = Unknown.SHORT;

   private final BasicArrowButton calendarButton;
   private final JTextField field;

   private JPopupMenu popup;

   private int year = UNKNOWN_YEAR;
   private int month = UNKNOWN_MONTH;
   private int day = UNKNOWN_DAY;

   private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

   ActionListener action = new ActionListener()
   {
      public void actionPerformed(ActionEvent event)
      {
         if (event.getSource() == calendarButton)
         {
            field.requestFocus();
            getRootPane().setDefaultButton(null);
            if ((null == popup) || !popup.isVisible())
            {
               popup = new JPopupMenu();
               popup.add(configureCalendarFlyout(getCalendar()));

               Dimension dim = calendarButton.getSize();
               popup.show(calendarButton, dim.width -
                     popup.getPreferredSize().width,
                     dim.height);
            }
            else
            {
               popup.setVisible(false);
               popup = null;
               field.requestFocus();
            }
         }
         else if (event.getSource() instanceof JCalendar)
         {
            final JCalendar calendar = (JCalendar) event.getSource();
            setCalendar(calendar.getDate());
            popup.setVisible(false);
            field.requestFocus();
         }
      }
   };

   /**
    *	Default constructor (initializes to current date).
    */
   public DateEntry()
   {
      field = new JTextField(dateFormat.format(new Date()).length());

      setBorder(field.getBorder());
      field.setBorder(null);

      calendarButton = new BasicArrowButton(BasicArrowButton.SOUTH);

      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, field);
      add(BorderLayout.EAST, calendarButton);

      configureCalendarFlyout(getCalendar());

      setCalendar(getCalendar());

      setEditable(true);

      initialize();
   }

   /**
    *	DateEntry constructor (use day, month, year).
    */
   public DateEntry(int day, int month, int year)
   {
      this();
      setDate(day, month, year);
   }

   /**
    * @return  <code>true</code> if the content of the field is empty;
    *          <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return (UNKNOWN_DAY == getDay())
            || (UNKNOWN_MONTH == getMonth()
            || (UNKNOWN_YEAR == getYear()));
   }

   public boolean isEditable()
   {
      return field.isEditable();
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

   public void setInputVerifier(final InputVerifier inputVerifier)
   {
      InputVerifier inner = new InputVerifier()
      {
         public boolean verify(JComponent input)
         {
            parseDate();
            return inputVerifier.verify(input);
         }

      };
      field.setInputVerifier(inner);
   }

   /**
    * Clears the date field and sets the field state to uninitialized.
    */
   public void clearDate()
   {
      setDate(UNKNOWN_DAY, UNKNOWN_MONTH, UNKNOWN_YEAR);
   }

   /**
    * Gets the internal date object.
    */
   public final Calendar getCalendar()
   {
      Calendar calendar = null;
      if (!isEmpty())
      {
         calendar = Calendar.getInstance();
         calendar.clear();

         calendar.set(Calendar.YEAR, getYear());
         calendar.set(Calendar.MONTH, getMonth() - 1);
         calendar.set(Calendar.DAY_OF_MONTH, getDay());
      }

      return calendar;
   }

   /**
    * @return Day as int. If the date entry is uninitialized <code>Unknown.BYTE</code>
    *         is returned.
    */
   public int getDay()
   {
      return day;
   }

   /**
    * @return Month as int. If the date entry is uninitialized <code>Unknown.BYTE</code>
    *         is returned.
    */
   public int getMonth()
   {
      return month;
   }

   /**
    * @return Year as int. If the date entry is uninitialized <code>Unknown.SHORT</code>
    *         is returned.
    */
   public int getYear()
   {
      return year;
   }

   /**
    *	DateEntry init (used by constructor)
    */
   private void initialize()
   {
      FocusListener focus = new FocusAdapter()
      {
         public void focusLost(FocusEvent e)
         {
            if (isEditable() && isEnabled())
            {
               parseDate();
            }
         }
      };

      addFocusListener(focus);
      field.addFocusListener(focus);

      KeyListener key = new KeyAdapter()
      {
         public void keyPressed(KeyEvent event)
         {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE)
            {
               popup.setVisible(false);
            }
         }
      };
      field.addKeyListener(key);

      calendarButton.addActionListener(action);
   }

   /**
    * Set content to a valid date.
    */
   public final void setCalendar(Calendar calendar)
   {
      String formattedDate;
      if (null != calendar)
      {
         setDay(calendar.get(Calendar.DATE));
         setMonth(calendar.get(Calendar.MONTH) + 1);
         setYear(calendar.get(Calendar.YEAR));

         formattedDate = dateFormat.format(calendar.getTime());
      }
      else
      {
         setDay(UNKNOWN_DAY);
         setMonth(UNKNOWN_MONTH);
         setYear(UNKNOWN_YEAR);

         formattedDate = "";
      }

      field.setText(formattedDate);

      fireChangeEvent();
   }

   /**
    * Set content to a valid date.
    */
   public final void setDate(int day, int month, int year)
   {
      Assert.condition((UNKNOWN_DAY == day) || (day > 0 && day < 32));
      Assert.condition((UNKNOWN_MONTH == month) || (month > 0 && month < 13));
      Assert.condition((UNKNOWN_YEAR == year) || (year >= 0 && year < 10000));

      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(Calendar.YEAR, year);
      calendar.set(Calendar.MONTH, month - 1);
      calendar.set(Calendar.DAY_OF_MONTH, day);

      setCalendar(calendar);
   }

   public void setDate(Date date)
   {
      if (date == null)
      {
         setCalendar(null);
      }
      else
      {
         Calendar calendar = Calendar.getInstance();
         calendar.clear();
         calendar.setTime(date);
         setCalendar(calendar);
      }
   }

   /**
    * Sets the day using int.
    */
   public void setDay(int day)
   {
      if (UNKNOWN_DAY == day)
      {
         this.day = UNKNOWN_DAY;
      }
      else
      {
         Assert.condition((1 <= day) && (31 >= day));
         this.day = day;
      }

      fireChangeEvent();
   }

   /**
    *	Sets the month using int.
    */
   public void setMonth(int month)
   {
      if (UNKNOWN_MONTH == month)
      {
         this.month = UNKNOWN_MONTH;
      }
      else
      {
         Assert.condition((1 <= month) && (12 >= month));
         this.month = month;
      }

      fireChangeEvent();
   }

   /**
    * Set the year using int.
    */
   public void setYear(int year)
   {
      if (UNKNOWN_YEAR == year)
      {
         this.year = UNKNOWN_YEAR;
      }
      else
      {
         Assert.condition((0 <= year) && (10000 > year));
         this.year = year;
      }

      fireChangeEvent();
   }

   /**
    * Sets all areas editable.
    */
   public final void setEditable(boolean isEditable)
   {
      field.setEditable(isEditable);

//      field.setEnabled(isEditable);
//      calendarButton.setEnabled(isEditable);

      fireChangeEvent();
   }

   /**
    * En/disables input.
    */
   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);

      GuiUtils.setEnabled(field, enabled);
      calendarButton.setEnabled(enabled);

      fireChangeEvent();
   }

   private void parseDate()
   {
      try
      {
         if (!StringUtils.isEmpty(field.getText()))
         {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(field.getText()));
            setCalendar(calendar);
         }
         else
         {
            setCalendar(null);
         }
      }
      catch (ParseException e)
      {
         setCalendar(null);
      }
   }

   private JCalendar configureCalendarFlyout(Calendar date)
   {
      if (null == date)
      {
         date = Calendar.getInstance();
      }

      JCalendar calendar = new JCalendar(date, 1, 1,
            new DefaultListSelectionModel(),
            new SimpleCalendarRenderer(true),
            new SimpleCalendarRenderer(false));
      calendar.addActionListener(action);

      CalendarMonth month = calendar.getGroup().getActiveMonth();
      field.addKeyListener(month);

      return calendar;
   }

   public Date getDate()
   {
      Calendar calendar = getCalendar();
      return calendar == null ? null : calendar.getTime();
   }
}
