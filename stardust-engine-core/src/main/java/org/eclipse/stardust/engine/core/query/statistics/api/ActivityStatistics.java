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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class ActivityStatistics extends CustomActivityInstanceQueryResult
{
   private static final long serialVersionUID = 1l;

   protected Map<String, ProcessEntry> processEntries;

   protected ActivityStatistics(ActivityStatisticsQuery query)
   {
      super(query);

      this.processEntries = CollectionUtils.newMap();
   }

   public IActivityStatistics getStatisticsForActivity(String processId, String activityId)
   {
      ProcessEntry processEntry = (ProcessEntry) processEntries.get(processId);

      return (null != processEntry) ? processEntry.getForActivity(activityId) : null;
   }

   public long getInstancesWithPriority(String processId, String activityId, int priority)
   {
      ProcessEntry processEntry = (ProcessEntry) processEntries.get(processId);
      ActivityEntry aiHistogram = (null != processEntry)
            ? processEntry.getForActivity(activityId)
            : null;

      long nInstances = (null != aiHistogram)
            ? aiHistogram.getInstancesWithPriority(priority)
            : 0;

      return nInstances;
   }

   public long getInstancesWithPriority(int priority)
   {
      long result = 0;

      for (Iterator<ProcessEntry> i = processEntries.values().iterator(); i.hasNext();)
      {
         ProcessEntry processEntry = i.next();
         for (Iterator<ActivityEntry> j = processEntry.activityEntries.values().iterator(); j.hasNext();)
         {
            ActivityEntry aiHistogram = j.next();

            long nInstances = (null != aiHistogram)
                  ? aiHistogram.getInstancesWithPriority(priority)
                  : 0;

            result += nInstances;
         }
      }

      return result;
   }

   public interface IActivityStatistics extends ICriticalInstancesHistogram
   {
      String getProcessId();

      String getActivityId();
   }

   protected static class ProcessEntry implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final String processId;

      public final Map<String, ActivityEntry> activityEntries = CollectionUtils.newMap();

      public ProcessEntry(String processId)
      {
         this.processId = processId;
      }

      public ActivityEntry getForActivity(String activityId)
      {
         return (ActivityEntry) activityEntries.get(activityId);
      }
   }

   protected static class ActivityEntry extends CriticalInstancesHistogram
         implements IActivityStatistics
   {
      private static final long serialVersionUID = 1l;

      public final String processId;

      public final String activityId;

      public ActivityEntry(String processId, String activityId)
      {
         this.processId = processId;
         this.activityId = activityId;
      }

      public String getProcessId()
      {
         return processId;
      }

      public String getActivityId()
      {
         return activityId;
      }
         

      /**
       * @deprecated Please directly use {@link #getInstances(int)}
       */
      public long getInstancesWithPriority(int priority)
      {
         return getInstancesCount(priority);
      }
   }
}
