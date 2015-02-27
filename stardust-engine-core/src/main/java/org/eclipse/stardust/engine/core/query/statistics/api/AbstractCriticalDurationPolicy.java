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
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.integration.calendar.IWorktimeCalendar;
import org.eclipse.stardust.engine.core.integration.calendar.WorktimeCalendarUtils;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.StatisticsDateUtils;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractCriticalDurationPolicy extends AbstractCriticalityPolicy
{

   private final Calendar comparisionCalendar = TimestampProviderUtils.getCalendar(0L);

   protected abstract Period getTargetDuration(ModelElement modelElement);

   public AbstractCriticalDurationPolicy(float lowPriorityCriticalPct,
         float normalPriorityCriticalPct, float highPriorityCriticalPct)
   {
      super(lowPriorityCriticalPct, normalPriorityCriticalPct, highPriorityCriticalPct);
   }

   public boolean isCriticalDuration(int priorityValue, Date tsStart, Date tsEnd,
         ModelElement modelElement)
   {
      ProcessInstancePriority priority = ProcessInstancePriority.getPriority(priorityValue);

      return isCriticalDuration(priority, tsStart, tsEnd, modelElement);
   }

   public boolean isCriticalDuration(ProcessInstancePriority priority, Date tsStart,
         Date tsEnd, ModelElement modelElement)
   {
      boolean isCritical = false;

      Period targetDuration = getTargetDuration(modelElement);

      if (null != targetDuration)
      {
         long criticalDuration = (long) (getCriticalityFactor(priority)
               * StatisticsDateUtils.periodToDate(targetDuration, comparisionCalendar).getTime());

         IWorktimeCalendar worktimeCalendar = WorktimeCalendarUtils.getWorktimeCalendar();
         // TODO provide performer to calendar
         long worktime = worktimeCalendar.calculateWorktime(tsStart, tsEnd, null);

         isCritical = (worktime >= criticalDuration);
      }

      return isCritical;
   }

}
