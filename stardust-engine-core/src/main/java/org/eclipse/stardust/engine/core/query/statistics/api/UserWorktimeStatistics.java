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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
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
            contribution = new Contribution(processId, onBehalfOfParticipant, null, 0);
            this.contributions.add(contribution);
         }
         else
         {
            contribution = this.contributions.get(index);
         }
         
         return contribution;

      }

      @Deprecated
      public Contribution findContribution(String processId,
            PerformerType onBehalfOfKind, long onBehalfOfOid)
      {
         ParticipantInfo performer = ParticipantInfoHelper.getLegacyParticipantInfo(
               onBehalfOfKind, onBehalfOfOid);
         return findContribution(processId, performer);
      }
   }

   public static class Contribution implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final String processId;

      @Deprecated
      public final PerformerType onBehalfOfKind;

      @Deprecated
      public final long onBehalfOf;
      
      public final ParticipantInfo onBehalfOfParticipant;

      public final ContributionInInterval contributionToday;

      public final ContributionInInterval contributionThisWeek;

      public final ContributionInInterval contributionLastWeek;

      public final ContributionInInterval contributionThisMonth;

      public final ContributionInInterval contributionLastMonth;

      public Contribution(String processId, ParticipantInfo onBehalfOfParticipant,
            PerformerType onBehalfOfKind, long onBehalfOf)
      {
         this.processId = processId;
         
         this.onBehalfOfKind = onBehalfOfKind;
         this.onBehalfOf = onBehalfOf;
         this.onBehalfOfParticipant = onBehalfOfParticipant;

         this.contributionToday = new ContributionInInterval();
         this.contributionThisWeek = new ContributionInInterval();
         this.contributionLastWeek = new ContributionInInterval();
         this.contributionThisMonth = new ContributionInInterval();
         this.contributionLastMonth = new ContributionInInterval();
      }
      
   }

   public static class ContributionInInterval implements Serializable
   {
      private static final long serialVersionUID = 1l;

      /**
       * The number of PIs the user contributed to on behalf of a specific role.
       */
      public int nPis;

      /**
       * The number of AIs the user contributed to on behalf of a specific role.
       */
      public int nAis;

      /**
       * The amount of time the user spent working on behalf of a specific role.
       */
      public Date timeSpent = new Date(0l);

      /**
       * The cost the user created working on behalf of a specific role.
       */
      public double cost;

      public InstancesStoplightHistogram criticalByProcessingTime = new InstancesStoplightHistogram();

      public InstancesStoplightHistogram criticalByExecutionCost = new InstancesStoplightHistogram();

   }

}
