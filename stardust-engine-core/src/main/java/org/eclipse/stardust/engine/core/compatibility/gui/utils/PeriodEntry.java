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
import java.util.EventListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.compatibility.gui.DateTextField;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;


/**
 * Class for Period Entries
 *
 */
public class PeriodEntry extends JComponent implements ObservedEntry
{
   private static final String YEAR_SEPERATOR = " years ";
   private static final String MONTH_SEPERATOR = " months ";
   private static final String DAY_SEPERATOR = " days ";
   private static final String HOUR_SEPERATOR = " hours ";
   private static final String MINUTE_SEPERATOR = " mins ";
   private static final String SECOND_SEPERATOR = " secs ";

   private boolean showMonthYear = true;

   private JLabel yearLabel;
   private JLabel monthLabel;
   private JLabel dayLabel;
   private JLabel hourLabel;
   private JLabel minuteLabel;
   private JLabel secondLabel;

   private JPanel fieldPanel;

   // visible data fields
   private DateTextField yearEntry;
   private DateTextField monthEntry;
   private DateTextField dayEntry;
   private DateTextField hourEntry;
   private DateTextField minuteEntry;
   private DateTextField secondEntry;

   /** PeriodEntry default constructor  */
   public PeriodEntry()
   {
      initialize();
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

   /** get input status like Textfield */
   public boolean isEditable()
   {
      return hourEntry.isEditable();
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

   /** PeriodEntry init (used by constructor) */
   private void initialize()
   {
      DocumentListener listener = new DocumentListener()
      {
         public void changedUpdate(DocumentEvent e)
         {
            fireChangeEvent();
         }

         public void insertUpdate(DocumentEvent e)
         {
            fireChangeEvent();
         }

         public void removeUpdate(DocumentEvent e)
         {
            fireChangeEvent();
         }
      };

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setBorder(null);

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
         yearEntry.getDocument().addDocumentListener(listener);

         monthEntry = new DateTextField(2, 0, 11);
         monthEntry.setBorder(null);
         monthEntry.getDocument().addDocumentListener(listener);
      }

      dayEntry = new DateTextField(3, 0, 999);

      dayEntry.setBorder(null);
      dayEntry.getDocument().addDocumentListener(listener);

      hourEntry = new DateTextField(2, 0, 23);
      hourEntry.setBorder(null);
      hourEntry.getDocument().addDocumentListener(listener);

      minuteEntry = new DateTextField(2, 0, 59);
      minuteEntry.setBorder(null);
      minuteEntry.getDocument().addDocumentListener(listener);

      secondEntry = new DateTextField(2, 0, 59);
      secondEntry.setBorder(null);
      secondEntry.getDocument().addDocumentListener(listener);

      // add field panel
      fieldPanel = new JPanel();
      fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
      fieldPanel.setBorder(UIManager.getBorder("TextField.border"));

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
   }

   /** en/disable input like JComponent */
   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      GuiUtils.setEnabled(fieldPanel, enabled);
      fieldPanel.setBackground(dayEntry.getBackground());
   }

   public void setVisible(boolean aFlag)
   {
      super.setVisible(aFlag);
      fieldPanel.setBackground(dayEntry.getBackground());
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
