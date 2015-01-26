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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Claude Duguay
 * @author rsauer
 * @version $Revision$
 */
public class CalendarGroup
{
   private Component parent;
   private List group;
   private int active;

   public CalendarGroup()
   {
      this(null);
   }

   public CalendarGroup(Component parent)
   {
      this.parent = parent;
      group = new ArrayList();
      active = 0;
   }

   public void setParent(Component parent)
   {
      this.parent = parent;
   }

   public void setActiveMonth(int index)
   {
      if (index > group.size())
         throw new IndexOutOfBoundsException("Out of range CalendarGroup index");
      active = index;
      for (int i = 0; i < group.size(); i++)
      {
         getCalendarMonth(i).setActive(i == active);
         if (i == active)
            getCalendarMonth(i).requestFocus();
      }
   }

   public void setActiveMonth(CalendarMonth month)
   {
      for (int i = 0; i < group.size(); i++)
      {
         if (getCalendarMonth(i) == month)
         {
            setActiveMonth(i);
            parent.repaint();
            break;
         }
      }
   }

   public void add(CalendarMonth month)
   {
      group.add(month);
      active = group.size() - 1;
   }

   public CalendarMonth getActiveMonth()
   {
      return getCalendarMonth(active);
   }

   public CalendarMonth getCalendarMonth(int index)
   {
      return (CalendarMonth) group.get(index);
   }

   public CalendarMonth nextCalendarMonth()
   {
      active++;
      if (active >= group.size()) active = 0;
      setActiveMonth(active);
      return getCalendarMonth(active);
   }

   public CalendarMonth prevCalendarMonth()
   {
      active--;
      if (active < 0) active = group.size() - 1;
      setActiveMonth(active);
      return getCalendarMonth(active);
   }

   public boolean isFirstCalendarMonth(CalendarMonth month)
   {
      return month == getCalendarMonth(0);
   }

   public boolean isLastCalendarMonth(CalendarMonth month)
   {
      return month == getCalendarMonth(group.size() - 1);
   }

   public void nextMonth(boolean repaint)
   {
      for (int i = 0; i < group.size(); i++)
      {
         getCalendarMonth(i).nextMonth();
         getCalendarMonth(i).invalidate();
         if (repaint && parent != null)
            parent.repaint();
      }
   }

   public void prevMonth(boolean repaint)
   {
      for (int i = 0; i < group.size(); i++)
      {
         getCalendarMonth(i).prevMonth();
         getCalendarMonth(i).invalidate();
         if (repaint && parent != null)
            parent.repaint();
      }
   }
}