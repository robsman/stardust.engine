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
package org.eclipse.stardust.engine.core.extensions.conditions.timer;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.engine.api.model.EventAware;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.core.compatibility.gui.TimeEntry;
import org.eclipse.stardust.engine.core.compatibility.spi.runtime.gui.RuntimeConditionPanel;


public class TimerbasedRuntimeBindPanel extends RuntimeConditionPanel
{
   // @todo (france, ub): optionally provide a period
   private TimeEntry timeEntry;
   private EventHandlerBinding handler;

   public TimerbasedRuntimeBindPanel()
   {
      setLayout(new BorderLayout());
      add(getTimePanel());
   }

   private JPanel getTimePanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.add(new JLabel("Scheduling Date:"), BorderLayout.NORTH);
      panel.add(timeEntry = new TimeEntry(true));
      return panel;
   }

   public void apply()
   {
      handler.setAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT,
            new Long(timeEntry.getCalendar().getTime().getTime()));
      handler.removeAttribute(PredefinedConstants.TIMER_PERIOD_ATT);
   }

   public void validateSettings()
   {
      // ???
   }

   public void setData(EventAware owner, EventHandlerBinding handler)
   {
      this.handler = handler;
      Calendar current = Calendar.getInstance();
      Long value = (Long) handler.getAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT);
      if (value != null)
      {
         current.setTime(new Date(value.longValue()));
      }
      else
      {
         Period period = (Period) handler.getAttribute(PredefinedConstants.TIMER_PERIOD_ATT);
         if (period != null)
         {
            current = period.add(current);
         }
      }
      timeEntry.setCalendar(current);
   }
}
