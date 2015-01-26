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

import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Unknown;


/**
 *	Class for Date Entries.
 */
public abstract class AbstractDateEntry extends JPanel
      implements Entry, KeyListener, MouseListener, FocusListener
{
   public static final int UNKNOWN_DAY = Unknown.BYTE;
   public static final int UNKNOWN_MONTH = Unknown.BYTE;
   public static final int UNKNOWN_YEAR = Unknown.SHORT;

   private JLabel flagLabel;
   private JPanel dateFieldPanel;
   /**
    * String separator between month and day
    */
   private JLabel sepDayMonthLabel;
   /**
    * String separator between month and year
    */
   private JLabel sepMonthYearLabel;
   /**
    * Visible data fields.
    */
   protected DateTextField monthField;
   protected DateTextField yearField;
   protected DateTextField dayField;

   private boolean mandatory;
   boolean readonly = false;

   /**
    *	AbstractDateEntry default constructor (uses current date).
    */
   public AbstractDateEntry()
   {
      this(false);
   }

   /**
    *	DateEntry constructor.
    */
   public AbstractDateEntry(boolean mandatory)
   {
      this.mandatory = mandatory;

      initialize();
   }

   /**
    *	DateEntry constructor (use day, month, year).
    */
   public AbstractDateEntry(int day, int month, int year, boolean mandatory)
   {
      this(mandatory);

      setDate(day, month, year);
   }

   /**
    * Adds focus listener.
    */
   public void addFocusListener(FocusListener listener)
   {
      final FocusListener thisListener = listener;

      if (yearField != null)
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

         yearField.addFocusListener(listen);
         monthField.addFocusListener(listen);
         dayField.addFocusListener(listen);
      }

      super.addFocusListener(listener);
   }

   /**
    * Clears the date field and sets the field state to uninitialized.
    */
   public void clearDate()
   {
      setDate(UNKNOWN_DAY, UNKNOWN_MONTH, UNKNOWN_YEAR);
//      dayField.requestFocus();
      performFlags();
   }

   /**
    *
    */
   public void focusGained(FocusEvent e)
   {
   }

   /**
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
    * Gets the internal date object.
    */
   public Calendar getCalendar()
   {
      Calendar calendar = null;
      if (!isEmpty())
      {
         calendar = Calendar.getInstance();
      }
      if (null != calendar)
      {
         calendar.set(Calendar.DATE, getDay());
         calendar.set(Calendar.MONTH, getMonth() - 1);
         calendar.set(Calendar.YEAR, getYear());
      }
      return calendar;
   }

   /**
    * @return Day as int. If the date entry is uninitialized <code>Unknown.BYTE</code>
    *         is returned.
    */
   public int getDay()
   {
      int currentDay = UNKNOWN_DAY;
      if ((null != dayField) && (Unknown.INT != dayField.getValue()))
      {
         currentDay = dayField.getValue();
      }
      return currentDay;
   }

   /**
    * @return Month as int. If the date entry is uninitialized <code>Unknown.BYTE</code>
    *         is returned.
    */
   public int getMonth()
   {
      int currentMonth = UNKNOWN_MONTH;
      if ((null != monthField) && (Unknown.INT != monthField.getValue()))
      {
         currentMonth = monthField.getValue();
      }
      return currentMonth;
   }

   /**
    * @return Year as int. If the date entry is uninitialized <code>Unknown.SHORT</code>
    *         is returned.
    */
   public int getYear()
   {
      int currentYear = UNKNOWN_YEAR;
      if ((null != yearField) && (Unknown.INT != yearField.getValue()))
      {
         currentYear = yearField.getValue();
      }
      return currentYear;
   }

   /**
    * If the entry is wrapping a "native" component like JEntryField or JComboBox,
    * this component is returned. Otherwise, the method returns this.
    * <p>
    * The method is thought to be used for table cell editors or the like.
    */
   public JComponent getWrappedComponent()
   {
      return this;
   }

   /**
    * Overloads has focus.
    */
   public boolean hasFocus()
   {
      return yearField.hasFocus()
            || dayField.hasFocus()
            || monthField.hasFocus()
            || super.hasFocus();
   }

   /**
    *	DateEntry init (used by constructor)
    */
   private void initialize()
   {
      // Init labels

      sepMonthYearLabel = new JLabel(".");

      sepDayMonthLabel = new JLabel(".");

      // Add fields and size them

      yearField = new DateTextField(4, 1, 9999);
      yearField.setOpaque(false);
      yearField.setBorder(null);

      monthField = new DateTextField(2, 1, 12);
      monthField.setOpaque(false);
      monthField.setBorder(null);

      dayField = new DateTextField(2, 1, 31);
      dayField.setOpaque(false);
      dayField.setBorder(null);

      // Allow recall from fields to adjust date

      yearField.registerOwner(this);
      monthField.registerOwner(this);
      dayField.registerOwner(this);

      yearField.addFocusListener(this);
      monthField.addFocusListener(this);
      dayField.addFocusListener(this);

      // Add key listener for '.' instead of tab

      yearField.addKeyListener(this);
      monthField.addKeyListener(this);
      dayField.addKeyListener(this);

      // AddMouseListener everywhere in this panel

      addMouseListener(this);
      yearField.addMouseListener(this);
      monthField.addMouseListener(this);
      dayField.addMouseListener(this);

      // Set cursor

      setCursor(GUI.ENTRY_CURSOR);
      yearField.setCursor(GUI.ENTRY_CURSOR);
      monthField.setCursor(GUI.ENTRY_CURSOR);
      dayField.setCursor(GUI.ENTRY_CURSOR);

      // Add layout and fields

      setBorder(null);
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      dateFieldPanel = new JPanel();

      dateFieldPanel.setBorder(
            BorderFactory.createLineBorder(Color.gray));
            //BorderFactory.createBevelBorder(BevelBorder.LOWERED,
            //Color.white, Color.lightGray, Color.black, Color.gray));
      dateFieldPanel.setBackground(Color.white);
      dateFieldPanel.setLayout(new BoxLayout(dateFieldPanel, BoxLayout.X_AXIS));
      dateFieldPanel.add(dayField);
      dateFieldPanel.add(sepDayMonthLabel);
      dateFieldPanel.add(monthField);
      dateFieldPanel.add(sepMonthYearLabel);
      dateFieldPanel.add(yearField);

      flagLabel = new JLabel(GUI.getMandatoryIcon());

      add(flagLabel);
      add(Box.createHorizontalStrut(3));
      add(dateFieldPanel);

      // Check mandatory and isEditable

      performFlags();
   }

   /**
    * Gets input status.
    */
   public boolean isEditable()
   {
      return yearField.isEditable();
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

   /**
    * Checks wether input is enabled.
    */
   public boolean isEnabled()
   {
      return super.isEnabled();
   }

   /**
    * Gets mandatory status.
    */
   public final boolean isMandatory()
   {
      return mandatory;
   }

   /**
    * Gets the opaqueness of this entry.
    */
   public boolean isOpaque()
   {
      return yearField.isOpaque();
   }

   /**
    *
    */
   public boolean isReadonly()
   {
      return readonly;
   }

   /**
    *
    */
   public void keyPressed(KeyEvent e)
   {
      Object source = e.getSource();
      int code = e.getKeyCode();

      if (code == KeyEvent.VK_RIGHT)
      {
         if (source == dayField
               && dayField.getCaretPosition() ==
               dayField.getDocument().getLength())
         {
            monthField.requestFocus();
         }
         else if (source == monthField
               && monthField.getCaretPosition() ==
               monthField.getDocument().getLength())
         {
            yearField.requestFocus();
         }
      }
      else if (code == KeyEvent.VK_LEFT)
      {
         if (source == monthField && monthField.getCaretPosition() == 0)
         {
            dayField.requestFocus();
         }
         else if (source == yearField && yearField.getCaretPosition() == 0)
         {
            monthField.requestFocus();
         }
      }
   }

   /**
    *
    */
   public void keyReleased(KeyEvent e)
   {
      Object source = e.getSource();
      int code = e.getKeyCode();

      if (code == KeyEvent.VK_PERIOD
            || code == KeyEvent.VK_COMMA
            || code == KeyEvent.VK_DECIMAL)
      {
         if (source == monthField)
         {
            yearField.requestFocus();
         }
         else if (source == dayField)
         {
            monthField.requestFocus();
         }
      }

      // Finally consume event

      e.consume();
   }

   /**
    *
    */
   public void keyTyped(KeyEvent e)
   {
   }

   /**
    *
    */
   public void mouseClicked(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseEntered(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseExited(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mousePressed(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseReleased(MouseEvent e)
   {
      if (e.isPopupTrigger() && isEditable())
      {
         VisualCalendar.showCalendar(this, e);
      }
   }

   /**
    * Remove focus listener.
    */
   public void removeFocusListener(FocusListener listener)
   {
      if (yearField != null)
      {
         yearField.removeFocusListener(listener);
         monthField.removeFocusListener(listener);
         dayField.removeFocusListener(listener);
      }

      super.removeFocusListener(listener);
   }

   /**
    *
    */
   public void requestFocus()
   {
      dayField.requestFocus();
   }

   /**
    * Set content to a valid date.
    */
   public void setCalendar(Calendar calendar)
   {
      if (null != calendar)
      {
         setDate(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1,
               calendar.get(Calendar.YEAR));
      }
      else
      {
         setDate(UNKNOWN_DAY, UNKNOWN_MONTH, UNKNOWN_YEAR);
      }
      performFlags();
   }

   /**
    * Set content to a valid date.
    */
   public void setDate(int day, int month, int year)
   {
      Assert.condition((UNKNOWN_DAY == day) || (day > 0 && day < 32));
      Assert.condition((UNKNOWN_MONTH == month) || (month > 0 && month < 13));
      Assert.condition((UNKNOWN_YEAR == year) || (year >= 0 && year < 10000));

      setDay(day);
      setMonth(month);
      setYear(year);

      performFlags();
   }

   /**
    * Sets the day using int.
    */
   public void setDay(int day)
   {
      if (UNKNOWN_DAY == day)
      {
         dayField.setValue(Unknown.INT);
      }
      else
      {
         Assert.condition((1 <= day) && (31 >= day));
         dayField.setValue(day);
      }
      performFlags();
   }

   /**
    *	Sets the month using int.
    */
   public void setMonth(int month)
   {
      if (UNKNOWN_MONTH == month)
      {
         monthField.setValue(Unknown.INT);
      }
      else
      {
         Assert.condition((1 <= month) && (12 >= month));
         monthField.setValue(month);
      }
      performFlags();
   }

   /**
    * Set the year using int.
    */
   public void setYear(int year)
   {
      if (UNKNOWN_YEAR == year)
      {
         yearField.setValue(Unknown.INT);
      }
      else
      {
         Assert.condition((0 <= year) && (10000 > year));
         yearField.setValue(year);
      }
      performFlags();
   }

   /**
    * Sets all areas editable.
    */
   public void setEditable(boolean isEditable)
   {
      yearField.setEditable(isEditable);
      monthField.setEditable(isEditable);
      dayField.setEditable(isEditable);
   }

   /**
    * En/disables input.
    */
   public void setEnabled(boolean isEnabled)
   {
      super.setEnabled(isEnabled);

      GUI.setEnableState(dateFieldPanel, isEnabled);

      performFlags();
   }

   /**
    * En/disables mandatory input.
    */
   public final void setMandatory(boolean mandatory)
   {
      this.mandatory = mandatory;
      performFlags();
   }

   /**
    *
    */
   public void setReadonly(boolean readonly)
   {
      this.readonly = readonly;

      setEditable(!readonly);
      performFlags();
   }

   /**
    * Sets the background color of this entry.
    */
   public void setBackground(Color color)
   {
      super.setBackground(color);

      if (dateFieldPanel != null)
      {
         dateFieldPanel.setBackground(color);
      }
   }

   /**
    * Sets the foreground color of this entry.
    */
   public void setForeground(Color color)
   {
      super.setForeground(color);
      if (yearField != null)
      {
         yearField.setForeground(color);
         monthField.setForeground(color);
         dayField.setForeground(color);
         sepMonthYearLabel.setForeground(color);
         sepDayMonthLabel.setForeground(color);
         dateFieldPanel.setForeground(color);
      }
   }

   /**
    * Marks, wether this date entry is used as a table cell.
    */
   public void setUsedAsTableCell(boolean isCell)
   {
      if (isCell)
      {
         removeAll();
         dateFieldPanel.setBorder(null);
         setLayout(new BorderLayout());
         add(BorderLayout.WEST, dateFieldPanel);
      }
   }

   /**
    *	Checks mandatory and isEditable flags to set color settings.
    */
   protected void performFlags()
   {
      Assert.isNotNull(yearField);
      Assert.isNotNull(monthField);
      Assert.isNotNull(dayField);
      Assert.isNotNull(flagLabel);

      if (!isEnabled())
      {
         dateFieldPanel.setBackground(GUI.DisabledColor);
         setForeground(GUI.DisabledTextColor);
      }
      else if (isReadonly())
      {
         dateFieldPanel.setBackground(GUI.ReadOnlyColor);
         setForeground(GUI.ReadOnlyTextColor);

         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else
      {
         dateFieldPanel.setBackground(GUI.DefaultColor);
         setForeground(GUI.DefaultTextColor);

         if (isMandatory() && isEmpty())
         {
            flagLabel.setIcon(GUI.getMandatoryIcon());
         }
         else
         {
            flagLabel.setIcon(GUI.getOptionalIcon());
         }
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
