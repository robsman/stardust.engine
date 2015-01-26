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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.InternalException;


/**
 * Class for Duration Entries
 *
 * Currently not supported!
 */
public class PeriodEntry extends JPanel
      implements Entry, DocumentListener
{
   static protected final String YEAR_SEPERATOR = " years ";
   static protected final String MONTH_SEPERATOR = " months ";
   static protected final String DAY_SEPERATOR = " days ";
   static protected final String HOUR_SEPERATOR = " hours ";
   static protected final String MINUTE_SEPERATOR = " mins ";
   static protected final String SECOND_SEPERATOR = " secs ";

   boolean showMonthYear = true;

   /**
    * Label for the mandatory icon
    */
   private JLabel flagLabel;
   /**
    * Label for the days
    */
   private JLabel yearLabel;
   /**
    * Label for the days
    */
   private JLabel monthLabel;
   /**
    * Label for the days
    */
   private JLabel dayLabel;
   /**
    * Label for the hours
    */
   private JLabel hourLabel;
   /**
    * Label for the minutes
    */
   private JLabel minuteLabel;

   /**
    * Label for the seconds
    */
   private JLabel secondLabel;

   private JPanel fieldPanel;

   // visible data fields
   private DateTextField yearEntry;
   private DateTextField monthEntry;
   private DateTextField dayEntry;
   private DateTextField hourEntry;
   private DateTextField minuteEntry;
   private DateTextField secondEntry;

   // mandatory //currently only boolean
   private boolean mandatory;

   // readonly flag
   private boolean isReadonly = false;

   /** PeriodEntry default constructor  */
   public PeriodEntry()
   {
      this(false);
   }

   /** PeriodEntry constructor */
   public PeriodEntry(boolean mandatory)
   {
      this.mandatory = mandatory;
      initialize();
   }

   /**
    * Gives notification that an attribute or set of attributes changed.
    */
   public void changedUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    * Gives notification that there was an insert into the document.
    */
   public void insertUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    * Gives notification that a portion of the document has been removed.
    */
   public void removeUpdate(DocumentEvent e)
   {
      performFlags();
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

   /** PeriodEntry init (used by constructor) */
   void initialize()
   {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      // init mandatory sign
      flagLabel = new JLabel(GUI.getMandatoryIcon());

      // init labels
      if (showMonthYear)
      {
         yearLabel = new JLabel(YEAR_SEPERATOR);
         yearLabel.setOpaque(false);

         monthLabel = new JLabel(MONTH_SEPERATOR);
         monthLabel.setOpaque(false);
      }

      dayLabel = new JLabel(DAY_SEPERATOR);
      dayLabel.setOpaque(false);

      hourLabel = new JLabel(HOUR_SEPERATOR);
      hourLabel.setOpaque(false);

      minuteLabel = new JLabel(MINUTE_SEPERATOR);
      minuteLabel.setOpaque(false);

      secondLabel = new JLabel(SECOND_SEPERATOR);
      secondLabel.setOpaque(false);

      // init fields
      if (showMonthYear)
      {
         yearEntry = new DateTextField(3, 0, 999);
         yearEntry.setBorder(null);
         yearEntry.setOpaque(false);
         yearEntry.getDocument().addDocumentListener(this);

         monthEntry = new DateTextField(2, 0, 11);
         monthEntry.setBorder(null);
         monthEntry.setOpaque(false);
         monthEntry.getDocument().addDocumentListener(this);

         dayEntry = new DateTextField(2, 0, 30);
      }
      else
      {
         dayEntry = new DateTextField(3, 0, 999);
      }

      dayEntry.setBorder(null);
      dayEntry.setOpaque(false);
      dayEntry.getDocument().addDocumentListener(this);

      hourEntry = new DateTextField(2, 0, 23);
      hourEntry.setBorder(null);
      hourEntry.setOpaque(false);
      hourEntry.getDocument().addDocumentListener(this);

      minuteEntry = new DateTextField(2, 0, 59);
      minuteEntry.setBorder(null);
      minuteEntry.setOpaque(false);
      minuteEntry.getDocument().addDocumentListener(this);

      secondEntry = new DateTextField(2, 0, 59);
      secondEntry.setBorder(null);
      secondEntry.setOpaque(false);
      secondEntry.getDocument().addDocumentListener(this);

      // add field panel
      fieldPanel = new JPanel();
      fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
      fieldPanel.setBorder(new javax.swing.plaf.basic.BasicBorders.FieldBorder(Color.gray, Color.black, Color.lightGray, Color.white));

      add(flagLabel);
      add(Box.createHorizontalStrut(3));
      if (showMonthYear)
      {
         fieldPanel.add(yearEntry);
         fieldPanel.add(yearLabel);
         fieldPanel.add(monthEntry);
         fieldPanel.add(monthLabel);
      }
      fieldPanel.add(dayEntry);
      fieldPanel.add(dayLabel);
      fieldPanel.add(hourEntry);
      fieldPanel.add(hourLabel);
      fieldPanel.add(minuteEntry);
      fieldPanel.add(minuteLabel);
      fieldPanel.add(secondEntry);
      fieldPanel.add(secondLabel);
      add(fieldPanel);

      // check mandatory & isEditable
      performFlags();

      // set cursor
      if (showMonthYear)
      {
         yearEntry.setCursor(GUI.ENTRY_CURSOR);
         monthEntry.setCursor(GUI.ENTRY_CURSOR);
      }
      dayEntry.setCursor(GUI.ENTRY_CURSOR);
      hourEntry.setCursor(GUI.ENTRY_CURSOR);
      minuteEntry.setCursor(GUI.ENTRY_CURSOR);
      secondEntry.setCursor(GUI.ENTRY_CURSOR);
      setCursor(GUI.ENTRY_CURSOR);

      //at laest set the max. size
      setMaximumSize(getPreferredSize());
   }

   /**
    * Marks, wether this time entry is used as a table cell.
    */
   public void setUsedAsTableCell(boolean isCell)
   {
      if (isCell)
      {
         setBorder(null);
      }
   }

   /** generic setter Method / implements abstract super method <p>
    Note: object == null <=> uninitialized state
    The method expected an Long with the Value as Seconds
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         setPeriod(null);
      }
      else if (value instanceof String)
      {
         setPeriod(new Period((String) value));
      }
      else if (value instanceof Period)
      {
         setPeriod((Period) value);
      }
      else
      {
         throw new IllegalArgumentException("not a " + Period.class.getName());
      }
   }

   /** generic getter Method / implements abstract super method
    * The returned Object is a Long with the Value as Seconds
    */
   public Object getObjectValue()
   {
      return getPeriod();
   }

   /** setReadonly */
   public void setReadonly(boolean isReadonly)
   {
      this.isReadonly = isReadonly;
      setEditable(!isReadonly);
      performFlags();
   }

   /** isReadonly */
   public boolean isReadonly()
   {
      return isReadonly;
   }

   /** en/disable mandatory input */
   public void setMandatory(boolean mandatory)
   {
      this.mandatory = mandatory;
      performFlags();
   }

   /** get mandatory status */
   public boolean isMandatory()
   {
      return mandatory;
   }

   /** en/disable input like Textfield */
   public void setEditable(boolean isEditable)
   {
      if (showMonthYear)
      {
         yearEntry.setEditable(isEditable);
         monthEntry.setEditable(isEditable);
      }
      dayEntry.setEditable(isEditable);
      hourEntry.setEditable(isEditable);
      minuteEntry.setEditable(isEditable);
      secondEntry.setEditable(isEditable);

      performFlags();
   }

   /** get input status like Textfield */
   public boolean isEditable()
   {
      return hourEntry.isEditable();
   }

   /**
    * @return  <code>true</code> if the content of the field is empty;
    *          <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return secondEntry.getValue() == Unknown.INT
            && minuteEntry.getValue() == Unknown.INT
            && hourEntry.getValue() == Unknown.INT
            && dayEntry.getValue() == Unknown.INT
            && (showMonthYear ? monthEntry.getValue() == Unknown.INT
            && yearEntry.getValue() == Unknown.INT : true);
   }

   /** en/disable input like JComponent */
   public void setEnabled(boolean isEnabled)
   {
      super.setEnabled(isEnabled);
      performFlags();
   }

   /** get input status like Textfield */
   public boolean isEnabled()
   {
      return super.isEnabled();
   }

   /** clear time */
   public void clearTime()
   {
      if (showMonthYear)
      {
         yearEntry.setText(null);
         monthEntry.setText(null);
      }
      dayEntry.setText(null);
      hourEntry.setText(null);
      minuteEntry.setText(null);
      secondEntry.setText(null);
   }

   /** simple date to string conversion */
   public String toString()
   {
      return isEmpty() ? "" : (showMonthYear ? yearEntry.getText() + YEAR_SEPERATOR
            + monthEntry.getText() + MONTH_SEPERATOR : "")
            + dayEntry.getText() + DAY_SEPERATOR
            + hourEntry.getText() + HOUR_SEPERATOR
            + minuteEntry.getText() + MINUTE_SEPERATOR
            + secondEntry.getText() + SECOND_SEPERATOR;
   }

   /** performFlags */
   public void performFlags()
   {
      if (!isEnabled())
      {
         fieldPanel.setBackground(GUI.DisabledColor);
         setForeground(GUI.DisabledTextColor);
         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else if (isReadonly())
      {
         fieldPanel.setBackground(GUI.ReadOnlyColor);
         setForeground(GUI.ReadOnlyTextColor);
         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else // regular state
      {
         fieldPanel.setBackground(GUI.DefaultColor);
         setForeground(GUI.DefaultTextColor);

         if (mandatory && isEmpty())
         {
            flagLabel.setIcon(GUI.getMandatoryIcon());
         }
         else
         {
            flagLabel.setIcon(GUI.getOptionalIcon());
         }
      }
      repaint();
   }

   /**
    * Sets the foreground color of this entry.
    */
   public void setForeground(Color color)
   {
      if (fieldPanel != null)
      {
         try
         {
            if (showMonthYear)
            {
               yearEntry.setForeground(color);
               yearLabel.setForeground(color);
               monthEntry.setForeground(color);
               monthLabel.setForeground(color);
            }
            dayEntry.setForeground(color);
            dayLabel.setForeground(color);
            hourEntry.setForeground(color);
            hourLabel.setForeground(color);
            minuteEntry.setForeground(color);
            minuteLabel.setForeground(color);
            secondEntry.setForeground(color);
            secondLabel.setForeground(color);
         }
         catch (Exception _ex)
         {
            throw new InternalException(_ex);
         }
      }
   }

   public void setPeriod(Period period)
   {
      if (period == null)
      {
         clearTime();
      }
      else
      {
         yearEntry.setText(Short.toString(period.get(Period.YEARS)));
         monthEntry.setText(Short.toString(period.get(Period.MONTHS)));
         dayEntry.setText(Short.toString(period.get(Period.DAYS)));
         hourEntry.setText(Short.toString(period.get(Period.HOURS)));
         minuteEntry.setText(Short.toString(period.get(Period.MINUTES)));
         secondEntry.setText(Short.toString(period.get(Period.SECONDS)));
      }
   }

   public Period getPeriod()
   {
      if (isEmpty())
      {
         return null;
      }
      int years = showMonthYear ? yearEntry.getValue() : Unknown.INT;
      int months = showMonthYear ? monthEntry.getValue() : Unknown.INT;
      int days = dayEntry.getValue();
      int hours = hourEntry.getValue();
      int minutes = minuteEntry.getValue();
      int seconds = secondEntry.getValue();
      return new Period(years == Unknown.INT ? 0 : (short) years,
            months == Unknown.INT ? 0 : (short) months,
            days == Unknown.INT ? 0 : (short) days,
            hours == Unknown.INT ? 0 : (short) hours,
            minutes == Unknown.INT ? 0 : (short) minutes,
            seconds == Unknown.INT ? 0 : (short) seconds);
   }
}
