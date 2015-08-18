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
import javax.swing.border.EmptyBorder;

import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Visual Calendar component.
 */
public class VisualCalendar extends JDialog
      implements ActionListener, KeyListener,
      FocusListener, MouseListener
{

   static protected final String CALENDAR_TITLE = "Calendar";

   static protected final String MONTH_LABEL = "Month:";
   static protected final String YEAR_LABEL = "     Year:";

   static protected final String JANUARY = "January";
   static protected final String FEBRUARY = "Feburary";
   static protected final String MARCH = "March";
   static protected final String APRIL = "April";
   static protected final String MAY = "May";
   static protected final String JUNE = "June";
   static protected final String JULY = "July";
   static protected final String AUGUST = "August";
   static protected final String SEPTEMBER = "September";
   static protected final String OKTOBER = "Oktober";
   static protected final String NOVEMBER = "November";
   static protected final String DECEMBER = "December";

   static protected final String MONDAY_SHORTNAME = " Mon";
   static protected final String TUESDAY_SHORTNAME = " Tue";
   static protected final String WEDNESDAY_SHORTNAME = " Wen";
   static protected final String THURSDAY_SHORTNAME = " Thu";
   static protected final String FRIDAY_SHORTNAME = " Fri";
   static protected final String SATURDAY_SHORTNAME = " Sat";
   static protected final String SUNDAY_SHORTNAME = " Sun";

   // @optimize look&feel of the calendar
   // hint: add some space to name for days of the week for a better look
   private final String days[] = {MONDAY_SHORTNAME
                                  , TUESDAY_SHORTNAME
                                  , WEDNESDAY_SHORTNAME
                                  , THURSDAY_SHORTNAME
                                  , FRIDAY_SHORTNAME
                                  , SATURDAY_SHORTNAME
                                  , SUNDAY_SHORTNAME};

   private static final int BUTTON_COUNT = 42;
   private static final int ADD_2000_BEFORE_YEAR = 30;
   private static VisualCalendar instance;

   private DateTextField yearField;
   private JButton yearUp;
   private JButton yearDown;
   private JComboBox monthBox;
   private JPanel daysPanel;
   private JButton dayButtons[];
   private JButton clearButton;
   private AbstractDateEntry entry;
   private Calendar calendar;

   /** */
   protected VisualCalendar(Frame parent)
   {
      super(parent, CALENDAR_TITLE, true);

      setResizable(false);

      JPanel _calendarPanel = new JPanel();

      // Layout panel

      _calendarPanel.setLayout(new BoxLayout(_calendarPanel, BoxLayout.Y_AXIS));
      _calendarPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

      getRootPane().setContentPane(_calendarPanel);

      dayButtons = new JButton[BUTTON_COUNT];

      // Panel to put month and year in

      JPanel yearMonthPanel = new JPanel();

      yearMonthPanel.setLayout(new BoxLayout(yearMonthPanel, BoxLayout.X_AXIS));

      yearField = new DateTextField(4, 1, 9999);

      yearField.addKeyListener(this);
      yearField.addFocusListener(this);
      yearField.setMaximumSize(new Dimension(yearField.getFontMetrics(yearField.getFont()).stringWidth("8888") + 4,
            yearField.getPreferredSize().height));

      // Spinbuttons

      yearUp = new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.NORTH);
      yearDown = new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.SOUTH);

      // Panel to put spin buttons in

      JPanel yearSpinPanel = new JPanel();

      yearSpinPanel.setLayout(new BoxLayout(yearSpinPanel, BoxLayout.Y_AXIS));
      yearSpinPanel.add(yearUp);
      yearSpinPanel.add(yearDown);

      // Modify size

      Dimension yearSpinDim = yearUp.getPreferredSize();
      yearSpinDim.height = yearField.getPreferredSize().height;

      yearSpinPanel.setPreferredSize(yearSpinDim);
      yearSpinPanel.setMaximumSize(yearSpinDim);

      // Add Mouse listeners for year spin button

      yearUp.addMouseListener(this);
      yearDown.addMouseListener(this);

      // Month combo

      monthBox = new JComboBox();

      monthBox.addItem(JANUARY);
      monthBox.addItem(FEBRUARY);
      monthBox.addItem(MARCH);
      monthBox.addItem(APRIL);
      monthBox.addItem(MAY);
      monthBox.addItem(JUNE);
      monthBox.addItem(JULY);
      monthBox.addItem(AUGUST);
      monthBox.addItem(SEPTEMBER);
      monthBox.addItem(OKTOBER);
      monthBox.addItem(NOVEMBER);
      monthBox.addItem(DECEMBER);
      monthBox.addActionListener(this);
      monthBox.setMaximumRowCount(12);

      // Month

      LabeledComponentsPanel monthComponent = new LabeledComponentsPanel();
      monthComponent.add(monthBox, MONTH_LABEL);
      monthComponent.pack();

      yearMonthPanel.add(monthComponent);

      // Year

      LabeledComponentsPanel yearComponent = new LabeledComponentsPanel();
      yearComponent.add(yearField, YEAR_LABEL);
      yearComponent.pack();

      yearMonthPanel.add(yearComponent);
      yearMonthPanel.add(yearSpinPanel);

      getContentPane().add(yearMonthPanel);

      daysPanel = new JPanel();
      daysPanel.setLayout(new GridLayout(days.length, dayButtons.length / days.length));

      // Set initial text & get max size

      Dimension dim = new Dimension(1, 1);
      JLabel _newLabel = null;

      for (int i = 0; i < days.length; i++)
      {
         _newLabel = new JLabel(days[i]);
         _newLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
         daysPanel.add(_newLabel);
      }

      // Set initial button text & max size

      for (int i = 0; i < dayButtons.length; i++)
      {
         dayButtons[i] = new ToolbarButton(" ");
         dayButtons[i].setBackground(Color.white);
         dayButtons[i].addActionListener(this);
         daysPanel.add(dayButtons[i]);
      }

      getContentPane().add(daysPanel);
      getContentPane().add(Box.createVerticalStrut(10));

      Box box = Box.createHorizontalBox();

      box.add(Box.createHorizontalGlue());
      box.add(clearButton = new JButton("Clear"));
      clearButton.addActionListener(this);
      box.add(Box.createHorizontalGlue());
      getContentPane().add(box);
   }

   /**
    *
    */
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();
      int _currentDay = 0;

      if (source == clearButton)
      {
         entry.clearDate();
         setVisible(false);

         return;
      }
      else if (source == monthBox)
      {
         // Attention! Setting the month in calendar may lead to problems.
         //            For example if the current date is 31.05.2001 and the
         //            month is set to february the calendar trys to change
         //            the date to 31.02.2001 that isn't a valid date.
         //            Thats why the calendar "moves" to 03.03.2001.
         _currentDay = calendar.get(Calendar.DATE);

         calendar.set(Calendar.DATE, 1);
         calendar.set(Calendar.MONTH, monthBox.getSelectedIndex());

         if (_currentDay > calendar.getActualMaximum(Calendar.DATE))
         {
            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
         }

         updateControls();
      }
      else if (source instanceof JButton)
      {
         try
         {
            int day = Integer.parseInt(((JButton) source).getText());

            entry.setDate(day, getMonth(), getYear());
         }
         catch (NumberFormatException x)
         {
         }

         setVisible(false);
      }
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
      if (e.getSource() != yearField)
      {
         return;
      }

      // Year is to be completed (always > 1000)

      if (yearField.getText().length() < 3)
      {
         int year = getInt(yearField.getText());
         year = getCenturyYear(year);

         calendar.set(Calendar.YEAR, year);
         updateControls();
      }
   }

   /** */
   public static int getCenturyYear(int year)
   {
      if (year >= ADD_2000_BEFORE_YEAR)
      {
         year += 1900;
      }
      else
      {
         year += 2000;
      }

      return year;
   }

   /** */
   public int getDay(JButton button)
   {
      return getInt(button.getText());
   }

   /**
    * @deprecated Move to DateTextEntry
    */
   private static int getInt(String text)
   {
      int value = 0;

      try
      {
         value = Integer.parseInt(text);
      }
      catch (NumberFormatException e)
      {
      }

      return value;
   }

   /**
    *
    */
   public int getMonth()
   {
      return calendar.get(Calendar.MONTH) + 1;
   }

   /** */
   public int getYear()
   {
      return calendar.get(Calendar.YEAR);
   }

   /**
    * Implements KeyListener
    */
   public void keyPressed(KeyEvent e)
   {
   }

   /**
    * Implements KeyListener
    */
   public void keyReleased(KeyEvent e)
   {
      if (e.getSource() == yearField &&
            e.getKeyCode() == KeyEvent.VK_ENTER)
      {
         if (yearField.getText().length() < 3)
         {
            int year = getInt(yearField.getText());

            calendar.set(Calendar.YEAR, getCenturyYear(year));
            updateControls();
         }
      }
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
      Object source = e.getSource();
      int currentYear = getInt(yearField.getText());

      // Allow 0 <= year <= 9999

      if (source == yearUp && currentYear < 9999)
      {
         calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
      }
      else if (source == yearDown && currentYear > 0)
      {
         calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
      }
      else
      {
         return;
      }

      updateControls();
   }

   /**
    *
    */
   public void mouseReleased(MouseEvent e)
   {
   }

   /** */
   private void setData(AbstractDateEntry entry)
   {
      this.entry = entry;

      if (entry.getCalendar() == null)
      {
         calendar = TimestampProviderUtils.getCalendar();
      }
      else
      {
         this.calendar = entry.getCalendar();
      }

      updateControls();
   }

   /**
    *
    */
   public static void showCalendar(AbstractDateEntry entry, MouseEvent e)
   {
      if (instance == null)
      {
         instance = new VisualCalendar(JOptionPane.getFrameForComponent(entry));
      }

      instance.setData(entry);

      // Calculate location on screen

      Point point = ((JComponent) e.getSource()).getLocationOnScreen();
      int x = point.x + e.getX();
      int y = point.y + e.getY();

      // Ensure that it does not exceed the screen limits

      Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension dim = instance.getPreferredSize();

      if (x + dim.width >= screenDim.width)
      {
         x = screenDim.width - dim.width - 1;
      }

      if (y + dim.height >= screenDim.height)
      {
         y = screenDim.height - dim.height - 1;
      }

      // Show calendar at desired location

      instance.setLocation(x, y);
      instance.pack();
      instance.show();
   }

   /**
    * Update all controls to reflect the current date of the internal
    * calendar.
    */
   private void updateControls()
   {
      yearField.setText(Integer.toString(calendar.get(Calendar.YEAR)));
      monthBox.setSelectedIndex(calendar.get(Calendar.MONTH));

      int _startButtonIndex = (calendar.get(Calendar.DAY_OF_WEEK)
            - (calendar.get(Calendar.DATE) % 7)
            + 6// that -1 to correct day of the week
            // and  +7 to get always an positiv result
            ) % 7;
      // hint:  Remember the value for DAY_OF_WEEK _startButtonIndexs
      //        with SUNDAY = 1
      //        and MONDAY is 2 and not 1(!!!)

      int i;

      Calendar today = TimestampProviderUtils.getCalendar();
      int todaysDay = today.get(Calendar.DATE);
      int _todaysMonth = today.get(Calendar.MONTH) + 1;
      int todaysYear = today.get(Calendar.YEAR);

      // Clear front buttons

      for (i = 0; i < _startButtonIndex; i++)
      {
         dayButtons[i].setText(" ");
         dayButtons[i].setEnabled(false);
      }

      // Set day buttons

      for (; i < calendar.getActualMaximum(Calendar.DATE) + _startButtonIndex; i++)
      {
         dayButtons[i].setText("" + (i - _startButtonIndex + 1));
         dayButtons[i].setEnabled(true);

         if (i - _startButtonIndex + 1 == todaysDay &&
               _todaysMonth == getMonth() &&
               todaysYear == getYear())
         {
            dayButtons[i].setForeground(Color.red);
         }
         else
         {
            dayButtons[i].setForeground(Color.black);
         }
      }

      // Clear last buttons

      for (; i < dayButtons.length; i++)
      {
         dayButtons[i].setText(" ");
         dayButtons[i].setEnabled(false);
      }
   }
}
