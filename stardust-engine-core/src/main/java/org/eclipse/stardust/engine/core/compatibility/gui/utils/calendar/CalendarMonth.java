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
package org.eclipse.stardust.engine.core.compatibility.gui.utils.calendar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.CellRendererPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

/**
 * @author Claude Duguay
 * @author rsauer
 * @version $Revision$
 */
public class CalendarMonth extends JPanel
      implements MouseListener, MouseMotionListener, KeyListener, FocusListener
{
   public static final String ACTION_ENTER = "Enter";
   public static final String ACTION_CLICK = "Click";
   public static final String ACTION_NEXT = "Next";
   public static final String ACTION_PREV = "Prev";

   private List listeners = new ArrayList();
   private boolean hasFocus = false;
   private CellRendererPane renderPane = new CellRendererPane();

   private CalendarRenderer renderer;
   private ListSelectionModel selector;
   private CalendarGroup group;

   private static final int[] daysInMonth =
         {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

   private Calendar date = Calendar.getInstance();
   private double xunit = 0;
   private double yunit = 0;
   private int first;
   private int days;
   private boolean active = true;

   public CalendarMonth(Calendar date, ListSelectionModel selector,
         CalendarRenderer renderer, CalendarGroup group)
   {
      this.group = group;
      this.selector = selector;
      this.renderer = renderer;
      group.add(this);
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, renderPane);
      addMouseMotionListener(this);
      addMouseListener(this);
      addFocusListener(this);
      addKeyListener(this);
      setDate(date);
   }

   public void setDay(int day)
   {
      selector.setSelectionInterval(day, day);
      date.set(Calendar.DAY_OF_MONTH, day);
   }

   public void setMonth(int month)
   {
      date.set(Calendar.MONTH, month);
      setDate(date);
   }

   public void setYear(int year)
   {
      date.set(Calendar.YEAR, year);
      setDate(date);
   }

   public void setDate(Calendar date)
   {
      this.date = date;
      Calendar temp = (Calendar) date.clone();
      temp.set(Calendar.DAY_OF_MONTH, 1);
      first = temp.get(Calendar.DAY_OF_WEEK) - 1;
      int current = date.get(Calendar.DAY_OF_MONTH);
      selector.setSelectionInterval(current, current);
      days = daysInMonth[date.get(Calendar.MONTH)];
      if (isLeapYear(date.get(Calendar.YEAR)) &&
            date.get(Calendar.MONTH) == 1)
         days = 29;
   }

   public Calendar getDate()
   {
      return date;
   }

   public void nextMonth()
   {
      date.add(Calendar.MONTH, 1);
      setDate(date);
      fireActionEvent(ACTION_NEXT);
   }

   public void prevMonth()
   {
      date.add(Calendar.MONTH, -1);
      setDate(date);
      fireActionEvent(ACTION_PREV);
   }

   public void setFirstDay()
   {
      setDay(1);
   }

   public void setLastDay()
   {
      setDay(days);
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

   public void paintComponent(Graphics g)
   {
      xunit = getSize().width / 7;
      yunit = getSize().height / 6;
      g.setColor(renderer.getBackdrop());
      g.fillRect(0, 0, getSize().width, getSize().height);

      int day = 1;
      for (int y = 0; y < 6; y++)
      {
         for (int x = 0; x < 7; x++)
         {
            if (isValidDay(x, y))
            {
               drawCell(g,
                     (int) (x * xunit), (int) (y * yunit),
                     (int) xunit, (int) yunit,
                     "" + day, isSelected(day));
               day++;
            }
         }
      }
   }

   protected boolean isSelected(int day)
   {
      if (!active) return false;
      return selector.isSelectedIndex(day);
   }

   protected boolean isValidDay(int x, int y)
   {
      int day = (x + y * 7) - first;
      return (y == 0 && x >= first) || (y > 0 && day < days);
   }

   protected void drawCell(Graphics g, int x, int y,
         int w, int h, String text, boolean isSelected)
   {
      Component render =
            renderer.getCalendarRendererComponent(this, text, isSelected, hasFocus);
      renderPane.paintComponent(g, render, this, x, y, w, h);
   }

   protected boolean isLeapYear(int year)
   {
      return ((year % 4 == 0) &&
            ((year % 100 != 0) || (year % 400 == 0)));
   }

   public Dimension getPreferredSize()
   {
      Dimension dimension =
            ((Component) renderer).getPreferredSize();
      int width = dimension.width * 7;
      int height = dimension.height * 6;
      return new Dimension(width, height);
   }

   public Dimension getMinimumSize()
   {
      Dimension dimension =
            ((Component) renderer).getMinimumSize();
      int width = dimension.width * 7;
      int height = dimension.height * 6;
      return new Dimension(width, height);
   }

   public void mouseClicked(MouseEvent event)
   {
   }

   public void mouseReleased(MouseEvent event)
   {
   }

   public void mouseEntered(MouseEvent event)
   {
   }

   public void mouseExited(MouseEvent event)
   {
   }

   public void mousePressed(MouseEvent event)
   {
      if (!hasFocus())
      {
         requestFocus();
         group.setActiveMonth(this);
      }
      int x = (int) (event.getX() / xunit);
      int y = (int) (event.getY() / yunit);
      if (!isValidDay(x, y)) return;
      int day = x + y * 7 - first + 1;
      if (event.isShiftDown() || event.isControlDown())
      {
         selector.setLeadSelectionIndex(day);
         repaint();
      }
      else
      {
         setDay(day);
         repaint();
      }
      fireActionEvent(ACTION_CLICK);
   }

   public void mouseMoved(MouseEvent event)
   {
   }

   public void mouseDragged(MouseEvent event)
   {
      int x = (int) (event.getX() / xunit);
      int y = (int) (event.getY() / yunit);
      if (!isValidDay(x, y)) return;
      int day = x + y * 7 - first + 1;
      selector.setLeadSelectionIndex(day);
      repaint();
   }

   public void keyTyped(KeyEvent event)
   {
   }

   public void keyReleased(KeyEvent event)
   {
   }

   public void keyPressed(KeyEvent event)
   {
      if (!active) return;
      int key = event.getKeyCode();
      if (key == KeyEvent.VK_ENTER)
      {
         fireActionEvent(ACTION_ENTER);
      }
      if (key == KeyEvent.VK_HOME)
      {
         setFirstDay();
         repaint();
      }
      if (key == KeyEvent.VK_END)
      {
         setLastDay();
         repaint();
      }
      if (key == KeyEvent.VK_PAGE_DOWN)
      {
         group.prevMonth(true);
      }
      if (key == KeyEvent.VK_PAGE_UP)
      {
         group.nextMonth(true);
      }
      int anchor = selector.getAnchorSelectionIndex();
      int lead = selector.getLeadSelectionIndex();
      if (key == KeyEvent.VK_RIGHT)
      {
         if (event.isShiftDown() || event.isControlDown())
         {
            if (lead < days)
            {
               selector.setLeadSelectionIndex(lead + 1);
               repaint();
            }
         }
         else if (anchor < days)
         {
            setDay(anchor + 1);
            repaint();
         }
         else if (anchor == days)
         {
            if (group.isLastCalendarMonth(this))
            {
               group.nextMonth(true);
               setLastDay();
               repaint();
            }
            else
            {
               CalendarMonth next =
                     group.nextCalendarMonth();
               next.setFirstDay();
               next.repaint();
               repaint();
            }
         }
      }
      if (key == KeyEvent.VK_LEFT)
      {
         if (event.isShiftDown() || event.isControlDown())
         {
            if (lead > 1)
            {
               selector.setLeadSelectionIndex(lead - 1);
               repaint();
            }
         }
         else if (anchor > 1)
         {
            setDay(anchor - 1);
            repaint();
         }
         else if (anchor == 1)
         {
            if (group.isFirstCalendarMonth(this))
            {
               group.prevMonth(true);
               setFirstDay();
               repaint();
            }
            else
            {
               CalendarMonth prev =
                     group.prevCalendarMonth();
               prev.setLastDay();
               prev.repaint();
               repaint();
            }
         }
      }
      if (key == KeyEvent.VK_UP)
      {
         if (event.isShiftDown() || event.isControlDown())
         {
            if (lead > 7)
            {
               selector.setLeadSelectionIndex(lead - 7);
               repaint();
            }
         }
         else if (anchor > 7)
         {
            setDay(anchor - 7);
            repaint();
         }
      }
      if (key == KeyEvent.VK_DOWN)
      {
         if (event.isShiftDown() || event.isControlDown())
         {
            if (lead <= (days - 7))
            {
               selector.setLeadSelectionIndex(lead + 7);
               repaint();
            }
         }
         else if (anchor <= (days - 7))
         {
            setDay(anchor + 7);
            repaint();
         }
      }
   }

   public void focusGained(FocusEvent event)
   {
      hasFocus = true;
      repaint();
   }

   public void focusLost(FocusEvent event)
   {
      hasFocus = false;
      repaint();
   }

   public boolean isFocusTraversable()
   {
      return active;
   }

   public void addActionListener(ActionListener listener)
   {
      listeners.add(listener);
   }

   public void removeActionListener(ActionListener listener)
   {
      listeners.remove(listener);
   }

   public void fireActionEvent(String command)
   {
      ActionListener listener;
      List list = new ArrayList(listeners);
      ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);
      for (int i = 0; i < list.size(); i++)
      {
         listener = ((ActionListener) list.get(i));
         listener.actionPerformed(event);
      }
   }
}