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
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.query.EvaluationPolicy;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.integration.calendar.IWorktimeCalendar;
import org.eclipse.stardust.engine.core.integration.calendar.WorktimeCalendarUtils;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.StatisticsDateUtils;
import org.eclipse.stardust.engine.core.query.statistics.evaluation.StatisticsModelUtils;


/**
 * Policy that determines if process instances are considered critical if their duration
 * exceeds a certain limit.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class PerformanceCriticalityPolicy implements EvaluationPolicy
{
   static final long serialVersionUID = 5399623483324308429L;
   
   public static final PerformanceCriticalityPolicy EXCEEDING_TARGET_PROCESSING_TIME = new PerformanceCriticalityPolicy(
         1.0f, 1.0f, 1.0f);

   private final float lowPriorityCriticalPct;

   private final float normalPriorityCriticalPct;

   private final float highPriorityCriticalPct;

   private final Calendar comparisionCalendar = Calendar.getInstance();

   /**
    * PIs are considered critical if their duration exceeds a certain limit. Limits can be
    * defined per priority.
    *
    * @param lowPriorityCriticalPct The percentage of the "target execution time" parameter a process with priority LOW must exceed to be considered critical.
    * @param normalPriorityCriticalPct The percentage of the "target execution time" parameter a process with priority NORMAL must exceed to be considered critical.
    * @param highPriorityCriticalPct The percentage of the "target execution time" parameter a process with priority HIGH must exceed to be considered critical.
    *
    * @return
    */
   public static PerformanceCriticalityPolicy criticalityByDuration(
         float lowPriorityCriticalPct, float normalPriorityCriticalPct,
         float highPriorityCriticalPct)
   {
      return new PerformanceCriticalityPolicy(lowPriorityCriticalPct,
            normalPriorityCriticalPct, highPriorityCriticalPct);
   }

   public PerformanceCriticalityPolicy(float lowPriorityCriticalPct,
         float normalPriorityCriticalPct, float highPriorityCriticalPct)
   {
      this.lowPriorityCriticalPct = lowPriorityCriticalPct;
      this.normalPriorityCriticalPct = normalPriorityCriticalPct;
      this.highPriorityCriticalPct = highPriorityCriticalPct;
   }

   public float getCriticalDurationFactor(ProcessInstancePriority priority)
   {
      switch (priority.getValue())
      {
      case ProcessInstancePriority.LOW:
         return lowPriorityCriticalPct;

      case ProcessInstancePriority.HIGH:
         return highPriorityCriticalPct;

      default:
         return normalPriorityCriticalPct;
      }
   }

   public boolean isCriticalDuration(int priorityValue, Date tsStart, Date tsEnd,
         IProcessDefinition processDefinition)
   {
      ProcessInstancePriority priority = ProcessInstancePriority.getPriority(priorityValue);

      return isCriticalDuration(priority, tsStart, tsEnd, processDefinition);
   }

   public boolean isCriticalDuration(ProcessInstancePriority priority, Date tsStart,
         Date tsEnd, IProcessDefinition processDefinition)
   {
      boolean isCritical = false;

      Period targetTime = StatisticsModelUtils.getTargetExecutionTime(processDefinition);

      if (null != targetTime)
      {
         Date targetExecutionTime = StatisticsDateUtils.periodToDate(targetTime, comparisionCalendar);

         long criticalDuration = (long) (getCriticalDurationFactor(priority) * targetExecutionTime.getTime());

         IWorktimeCalendar worktimeCalendar = WorktimeCalendarUtils.getWorktimeCalendar();
         // TODO provide performer to calendar
         long worktime = worktimeCalendar.calculateWorktime(tsStart, tsEnd, null);

         isCritical = (worktime >= criticalDuration);
      }

      return isCritical;
   }

   public boolean isCriticalDuration(int priorityValue, Date tsStart, Date tsEnd,
         IActivity activity)
   {
      ProcessInstancePriority priority = ProcessInstancePriority.getPriority(priorityValue);

      return isCriticalDuration(priority, tsStart, tsEnd, activity);
   }

   public boolean isCriticalDuration(ProcessInstancePriority priority, Date tsStart,
         Date tsEnd, IActivity activity)
   {
      boolean isCritical = false;

      Period targetTime = StatisticsModelUtils.getTargetExecutionTime(activity);

      if (null != targetTime)
      {
         Date targetExecutionTime = StatisticsDateUtils.periodToDate(targetTime, comparisionCalendar);

         long criticalDuration = (long) (getCriticalDurationFactor(priority) * targetExecutionTime.getTime());

         IWorktimeCalendar worktimeCalendar = WorktimeCalendarUtils.getWorktimeCalendar();
         // TODO provide performer to calendar
         long worktime = worktimeCalendar.calculateWorktime(tsStart, tsEnd, null);

         isCritical = (worktime >= criticalDuration);
      }

      return isCritical;
   }

}
