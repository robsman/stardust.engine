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
package org.eclipse.stardust.engine.core.extensions.triggers.timer;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.extensions.triggers.timer.TimerTriggerMatch;
import org.eclipse.stardust.engine.core.runtime.beans.TimerLog;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.BatchedPullTriggerEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class TimerTriggerEvaluator implements BatchedPullTriggerEvaluator
{
   private static final Logger trace = LogManager.getLogger(TimerTriggerEvaluator.class);

   public Iterator getMatches(ITrigger trigger, long batchSize)
   {      
      Period periodicity = (Period) trigger.getAttribute(PredefinedConstants
            .TIMER_TRIGGER_PERIODICITY_ATT);
      Long startTimestamp = (Long) trigger.getAttribute(PredefinedConstants
            .TIMER_TRIGGER_START_TIMESTAMP_ATT);
      Long stopTimestamp = (Long) trigger.getAttribute(PredefinedConstants
            .TIMER_TRIGGER_STOP_TIMESTAMP_ATT);

      if (null == startTimestamp)
      {
         trace.warn("Missing start timestamp for timer trigger " + trigger);
      }

      TimerLog timerLog = TimerLog.findOrCreate(trigger);
      long lastExecutionTimestamp = timerLog.getTimeStamp();
      if (trace.isDebugEnabled())
      {
         trace.debug("Obtaining timer log with timestamp " + timerLog.getTimeStamp() + ".");
      }

      final List triggerMatches;

      if (isNewExecutionRequired(periodicity, startTimestamp, stopTimestamp,
            lastExecutionTimestamp))
      {
         triggerMatches = Collections.singletonList(new TimerTriggerMatch());

         long now = System.currentTimeMillis();

         trace.debug("Write timer log with timestamp " + now + ".");

         timerLog.setTimeStamp(now);
      }
      else
      {
         triggerMatches = Collections.EMPTY_LIST;
      }

      return triggerMatches.iterator();
   }

   private boolean isNewExecutionRequired(Period periodicity,
         Long startTimestamp, Long stopTimestamp, long lastExecutionTimestamp)
   {
      final Calendar now = Calendar.getInstance();
      final Calendar start = toCalendar(startTimestamp);      
      boolean executeTrigger;

      if ((null != start) && now.before(start))
      {
         // trigger will be started in future
         executeTrigger = false;
      }
      else if (Unknown.LONG == lastExecutionTimestamp)
      {
         // trigger did never run before, but should already be started, so do it right
         // now
         executeTrigger = true;
      }
      else
      {
         final Calendar lastRun = toCalendar(lastExecutionTimestamp);

         if (null != periodicity)
         {
            // calculate the next time the trigger should run after now
            Calendar nextScheduled = null;
            if (null != start)
            {
               // caclulate latest scheduled time between last execution and now, if any
               nextScheduled = toCalendar(start.getTimeInMillis());
               
               // TODO optimize for cases when only a single field is used in period,
               // or when period is smaller than a day/hour as this loop may take quiet
               // long for small periods
               if ( !nextScheduled.after(now))
               {
                  if(periodicity.get(Period.YEARS) == 0
                     && periodicity.get(Period.MONTHS) == 0)
                  {
                     int years = now.get(Calendar.YEAR) - nextScheduled.get(Calendar.YEAR);
                     if(years > 0)
                     {
                        nextScheduled.add(Calendar.YEAR, years);                        
                        if ( nextScheduled.after(now))
                        {
                           nextScheduled.add(Calendar.YEAR, - 1);                                                   
                        }
                     }
                     while ( !nextScheduled.after(now))
                     {
                        nextScheduled.add(Calendar.MONTH, 1);                        
                     }
                     if ( nextScheduled.after(now))
                     {
                        nextScheduled.add(Calendar.MONTH, - 1);
                     }
                  }
               }
               
               while ( !nextScheduled.after(now))
               {
                  // TODO use method which does'n produce new Calendar instance
                  nextScheduled = periodicity.add(nextScheduled);
               }
            }
            else
            {
               nextScheduled = periodicity.add(lastRun);
            }
            
            // go back from next trigger run scheduled after now to find if its
            // predecessor run is still due
            final Calendar lastScheduled = periodicity.subtract(nextScheduled);

            // watch interval begin, lastRun may be equal lastScheduled 
            if (lastRun.before(lastScheduled))
            {
               // last execution was before last scheduled time, so probably trigger
               // should run
               
               final Calendar stop = toCalendar(stopTimestamp);
               if ((null != stop) && stop.before(lastScheduled))
               {
                  // don't run stale timer trigger
                  executeTrigger = false;
               }
               else
               {
                  // trigger is scheduled to run between [last scheduled time, now) but
                  // didn't, so we should do so now
                  executeTrigger = true;
               }
            }
            else
            {
               // last execution was in [last scheduled time, now), so nothing
               // to be done
               executeTrigger = false;
            }
         }
         else
         {
            // no periodicity means only run once
            executeTrigger = false;
         }
      }

      return executeTrigger;
   }

   private static Calendar toCalendar(Long timestamp)
   {
      Calendar result = null;

      if (null != timestamp)
      {
         result = toCalendar(timestamp.longValue());
      }
      
      return result;
   }
   
   private static Calendar toCalendar(long timestamp)
   {
      Calendar result = Calendar.getInstance();
      result.setTimeInMillis(timestamp);
      
      return result;
   }  
}