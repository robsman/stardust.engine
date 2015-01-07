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
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.engine.core.integration.calendar.IWorktimeCalendar;
import org.eclipse.stardust.engine.core.integration.calendar.WorktimeCalendarUtils;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.StatisticsDateUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractStoplightDurationPolicy extends AbstractStoplightPolicy
{

   private final Calendar comparisionCalendar = Calendar.getInstance();

   protected abstract Period getTargetDuration(ModelElement modelElement);

   public AbstractStoplightDurationPolicy(float yellowPct, float redPct)
   {
      super(yellowPct, redPct);
   }

   public Status rateDuration(Date tsStart, Date tsEnd, ModelElement modelElement)
   {
      IWorktimeCalendar worktimeCalendar = WorktimeCalendarUtils.getWorktimeCalendar();
      // TODO provide performer to calendar
      final long worktime = worktimeCalendar.calculateWorktime(tsStart, tsEnd, null);

      return rateDuration(worktime, modelElement);
   }

   public Status rateDuration(long worktime, ModelElement modelElement)
   {
      Status status = GREEN;

      Period targetDuration = getTargetDuration(modelElement);

      if (null != targetDuration)
      {
         long targetDurationInMs = StatisticsDateUtils.periodToDate(targetDuration,
               comparisionCalendar).getTime();

         long redDuration = (long) (redPct * targetDurationInMs);
         long yellowDuration = (long) (yellowPct * targetDurationInMs);

         if (worktime >= redDuration)
         {
            status = RED;
         }
         else if (worktime >= yellowDuration)
         {
            status = YELLOW;
         }
      }

      return status;
   }

}
