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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class UserWorktimeStatistics extends CustomUserQueryResult
{
   private static final long serialVersionUID = 1l;

   protected final Map<Long, WorktimeStatistics> worktimeStatistics;

   protected UserWorktimeStatistics(UserWorktimeStatisticsQuery query, Users users)
   {
      super(query, users);

      this.worktimeStatistics = CollectionUtils.newMap();
   }

   public WorktimeStatistics getWorktimeStatistics(long userOid)
   {
      return (WorktimeStatistics) worktimeStatistics.get(Long.valueOf(userOid));
   }

   public Set getAvailableUserOids()
   {
      return Collections.unmodifiableSet(worktimeStatistics.keySet());
   }

   public static class WorktimeStatistics implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final long userOid;

      /**
       * For any combination of scope process ID/performer the user contributed to, a
       * {@link Contribution} instance is available for details.
       */
      public List<Contribution> contributions;

      public WorktimeStatistics(long userOid)
      {
         this.userOid = userOid;

         this.contributions = CollectionUtils.newList();
      }

      public int getContributionIndex(String processId,
            ParticipantInfo onBehalfOfParticipant)
      {
         for (int i = 0; i < contributions.size(); ++i)
         {
            Contribution contrib = contributions.get(i);
            if (CompareHelper.areEqual(contrib.processId, processId)
                  && ParticipantInfoHelper.areEqual(contrib.onBehalfOfParticipant,
                        onBehalfOfParticipant))
            {
               return i;
            }
         }
         return -1;
      }

      public Contribution findContribution(String processId,
            ParticipantInfo onBehalfOfParticipant)
      {
         Contribution contribution = null;

         int index = getContributionIndex(processId, onBehalfOfParticipant);
         if(index == -1)
         {
            contribution = new Contribution(processId, onBehalfOfParticipant);
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

      private final String processId;

      private final ParticipantInfo onBehalfOfParticipant;

      private final Map<DateRange, ContributionInInterval> contributionsInIntervals = new LinkedHashMap();

      public Contribution(String processId, ParticipantInfo onBehalfOfParticipant)
      {
         this.processId = processId;
         this.onBehalfOfParticipant = onBehalfOfParticipant;
      }

      public ContributionInInterval getOrCreateContributionInInterval(DateRange dateRange)
      {
         ContributionInInterval contributionInInterval = contributionsInIntervals.get(dateRange);
         if (contributionInInterval == null)
         {
            contributionInInterval = new ContributionInInterval();
            contributionsInIntervals.put(dateRange, contributionInInterval);
         }
         return contributionInInterval;
      }

      public ContributionInInterval getContributionInInterval(DateRange dateRange)
      {
         return contributionsInIntervals.get(dateRange);
      }

      public Map<DateRange, ContributionInInterval> getAllContributionsInIntervals()
      {
         return Collections.unmodifiableMap(contributionsInIntervals);
      }

      public ParticipantInfo getOnBehalfOfParticipant()
      {
         return onBehalfOfParticipant;
      }

      public String getProcessId()
      {
         return processId;
      }

   }

   public static class ContributionInInterval implements Serializable
   {
      private static final long serialVersionUID = 1l;

      /**
       * The number of PIs the user contributed to on behalf of a specific role.
       */
      private int nPis;

      /**
       * The number of AIs the user contributed to on behalf of a specific role.
       */
      private int nAis;

      /**
       * The amount of time the user spent working on behalf of a specific role.
       */
      private Date timeSpent = new Date(0l);

      /**
       * The cost the user created working on behalf of a specific role.
       */
      private double cost;

      private InstancesStoplightHistogram criticalByProcessingTime = new InstancesStoplightHistogram();

      private InstancesStoplightHistogram criticalByExecutionCost = new InstancesStoplightHistogram();

      public int getnPis()
      {
         return nPis;
      }

      public void addnPis(int increment)
      {
         this.nPis += increment;
      }

      public int getnAis()
      {
         return nAis;
      }

      public void addnAis(int increment)
      {
         this.nAis += increment;
      }

      public Date getTimeSpent()
      {
         return timeSpent;
      }

      public void addTimeSpent(long duration)
      {
         this.timeSpent.setTime(this.timeSpent.getTime() + duration);
      }

      public double getCost()
      {
         return cost;
      }

      public void addCost(double toAddCost)
      {
         this.cost += toAddCost;
      }

      public InstancesStoplightHistogram getCriticalByProcessingTime()
      {
         return criticalByProcessingTime;
      }

      public InstancesStoplightHistogram getCriticalByExecutionCost()
      {
         return criticalByExecutionCost;
      }

   }

}
