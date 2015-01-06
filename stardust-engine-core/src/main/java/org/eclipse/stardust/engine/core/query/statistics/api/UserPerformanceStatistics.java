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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class UserPerformanceStatistics extends CustomUserQueryResult
{
   private static final long serialVersionUID = 1l;

   protected final Map<Long, Map<String, PerformanceStatistics>> statistics;

   protected UserPerformanceStatistics(UserPerformanceStatisticsQuery query, Users users)
   {
      super(query, users);

      this.statistics = CollectionUtils.newMap();
   }

   public Map<String, PerformanceStatistics> getStatisticsForUser(long userOid)
   {
      return statistics.get(userOid);
   }

   public PerformanceStatistics getStatisticsForUserAndProcess(long userOid, String processId)
   {
      Map<String, PerformanceStatistics> perProcessStatistics = getStatisticsForUser(userOid);

      return (null != perProcessStatistics)
            ? (PerformanceStatistics) perProcessStatistics.get(processId)
            : null;
   }

   public static class PerformanceStatistics implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final long userOid;

      /**
       * For any performer the user worked on behalf of, a {@link Contribution} instance
       * is available for details.
       */
      public List<Contribution> contributions;

      public PerformanceStatistics(long userOid)
      {
         this.userOid = userOid;

         this.contributions = CollectionUtils.newList();
      }

      public int getContributionIndex(ParticipantInfo onBehalfOfParticipant)
      {
         for (int i = 0; i < contributions.size(); ++i)
         {
            Contribution contrib = (Contribution) contributions.get(i);
            if (ParticipantInfoHelper.areEqual(contrib.onBehalfOfParticipant, onBehalfOfParticipant))
            {
               return i;
            }
         }
         return -1;
      }

      public Contribution findContribution(ParticipantInfo onBehalfOfParticipant)
      {
         Contribution contribution = null;
         int index = getContributionIndex(onBehalfOfParticipant);
         if(index == -1)
         {
            contribution = new Contribution(onBehalfOfParticipant);
            this.contributions.add(contribution);
         }
         else
         {
            contribution = this.contributions.get(index);
         }
         return contribution;
      }
   }

   public static class Contribution implements Serializable
   {
      private static final long serialVersionUID = 1l;

      private final ParticipantInfo onBehalfOfParticipant;

      private final Map<DateRange, PerformanceInInterval> performancesInIntervals = new LinkedHashMap();


      public Contribution(ParticipantInfo onBehalfOfParticipant)
      {
         this.onBehalfOfParticipant = onBehalfOfParticipant;
      }

      public PerformanceInInterval getOrCreatePerformanceInInterval(DateRange dateRange)
      {
         PerformanceInInterval performanceInInterval = performancesInIntervals.get(dateRange);
         if (performanceInInterval == null)
         {
            performanceInInterval = new PerformanceInInterval();
            performancesInIntervals.put(dateRange, performanceInInterval);
         }
         return performanceInInterval;
      }

      public PerformanceInInterval getPerformanceInInterval(DateRange dateRange)
      {
         return performancesInIntervals.get(dateRange);
      }

      public Map<DateRange, PerformanceInInterval> getAllPerformancesInIntervals()
      {
         return Collections.unmodifiableMap(performancesInIntervals);
      }

      public ParticipantInfo getOnBehalfOfParticipant()
      {
         return onBehalfOfParticipant;
      }

   }

   public static class PerformanceInInterval implements Serializable
   {
      private static final long serialVersionUID = 1l;

      /**
       * The number of PIs the user completed AIs in on behalf of a specific role.
       */
      private int nPisAffected;

      /**
       * The number of AIs the user completed on behalf of a specific role.
       */
      private int nAisCompleted;

      private InstancesStoplightHistogram yellowByProcessingTime = new InstancesStoplightHistogram();

      private InstancesStoplightHistogram redByProcessingTime = new InstancesStoplightHistogram();

      private InstancesStoplightHistogram yellowByInstanceCostTime = new InstancesStoplightHistogram();

      private InstancesStoplightHistogram redByInstanceCostTime = new InstancesStoplightHistogram();

      public int getnPisAffected()
      {
         return nPisAffected;
      }

      public void addnPisAffected(int increment)
      {
         this.nPisAffected += increment;
      }

      public int getnAisCompleted()
      {
         return nAisCompleted;
      }

      public void addnAisCompleted(int increment)
      {
         this.nAisCompleted += increment;
      }

      public InstancesStoplightHistogram getYellowByProcessingTime()
      {
         return yellowByProcessingTime;
      }

      public InstancesStoplightHistogram getRedByProcessingTime()
      {
         return redByProcessingTime;
      }

      public InstancesStoplightHistogram getYellowByInstanceCostTime()
      {
         return yellowByInstanceCostTime;
      }

      public InstancesStoplightHistogram getRedByInstanceCostTime()
      {
         return redByInstanceCostTime;
      }

   }

}
