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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class PostponedActivitiesStatistics extends CustomUserQueryResult
{
   private static final long serialVersionUID = 1l;

   protected final Map<Long, PostponedActivities> postponedActivities;
   
   protected PostponedActivitiesStatistics(PostponedActivitiesStatisticsQuery query,
         Users users)
   {
      super(query, users);
      
      this.postponedActivities = CollectionUtils.newMap();
   }
   
   public Collection<PostponedActivities> getPostponedActivities()
   {
      return Collections.unmodifiableCollection(postponedActivities.values());
   }

   public PostponedActivities getPostponedActivities(long userOid)
   {
      return (PostponedActivities) postponedActivities.get(userOid);
   }

   public static class PostponedActivities implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final long userOid;

      /**
       * For any performer the user worked on behalf of, a {@link Participation} instance
       * is available for details.
       */
      public final Map<String, List<Participation>> participationsPerProcess;
      
      public PostponedActivities(long userOid)
      {
         this.userOid = userOid;
         
         this.participationsPerProcess = CollectionUtils.newMap();
      }
      
      public int getParticipationIndex(String processId, ParticipantInfo onBehalfOfParticipant)
      {
         List<Participation> participations = participationsPerProcess.get(processId);
         if(participations == null)
         {
            participations = CollectionUtils.newList();
            participationsPerProcess.put(processId, participations);
         }
         
         for (int i = 0; i < participations.size(); ++i)
         {
            Participation contrib = (Participation) participations.get(i);
            if (CompareHelper.areEqual(contrib.processId, processId)
                  && ParticipantInfoHelper.areEqual(contrib.performerInfo, onBehalfOfParticipant))
            {
               return i;
            }
         }
         return -1;
      }
      
      public Participation findParticipation(String processId, 
            ParticipantInfo onBehalfOfParticipant)
      {
         Participation contribution = null;
         int index = getParticipationIndex(processId, onBehalfOfParticipant);
         List<Participation> participations = participationsPerProcess.get(processId);
         if(index == -1)
         {
            contribution = new Participation(processId, onBehalfOfParticipant, null, 0);
            participations.add(contribution);
         }
         else
         {
            contribution = participations.get(index);            
         }
         return contribution;
      }
      
      @Deprecated
      public Participation findParticipation(String processId,
            PerformerType onBehalfOfKind, long onBehalfOfOid)
      {
         ParticipantInfo performer = ParticipantInfoHelper.getLegacyParticipantInfo(
               onBehalfOfKind, onBehalfOfOid);
         return findParticipation(processId, performer);
      }

   }
   
   public static class Participation implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final String processId;
      
      @Deprecated
      public final PerformerType performerKind;
      
      @Deprecated
      public final long performerOid;
      
      public final ParticipantInfo performerInfo;
      
      public final List<PostponedActivityDetails> lowPriority;
      
      public final List<PostponedActivityDetails> normalPriority;
      
      public final List<PostponedActivityDetails> highPriority;
      
      public final List<PostponedActivityDetails> lowPriorityCritical;
      
      public final List<PostponedActivityDetails> normalPriorityCritical;
      
      public final List<PostponedActivityDetails> highPriorityCritical;
      
      public Participation(String processId, ParticipantInfo performerInfo,
            PerformerType performerKind, long performerOid)
      {
         this.processId = processId;
         this.performerKind = performerKind;
         this.performerOid = performerOid;
         this.performerInfo = performerInfo;

         this.lowPriority = CollectionUtils.newList();
         this.normalPriority = CollectionUtils.newList();
         this.highPriority = CollectionUtils.newList();

         this.lowPriorityCritical = CollectionUtils.newList();
         this.normalPriorityCritical = CollectionUtils.newList();
         this.highPriorityCritical = CollectionUtils.newList();
      }
      
      public List<PostponedActivityDetails> getDetailsForPriority(int priority)
      {
         switch (priority)
         {
         case ProcessInstancePriority.LOW:
            return lowPriority;

         case ProcessInstancePriority.HIGH:
            return highPriority;

         default:
            return normalPriority;
         }
      }

      public List<PostponedActivityDetails> getCriticalDetailsForPriority(int priority)
      {
         switch (priority)
         {
         case ProcessInstancePriority.LOW:
            return lowPriorityCritical;

         case ProcessInstancePriority.HIGH:
            return highPriorityCritical;

         default:
            return normalPriorityCritical;
         }
      }
   }

   public static class PostponedActivityDetails implements Serializable
   {
      private static final long serialVersionUID = 1l;
      
      /**
       * The PK of this postponed AI.
       */
      public final long aiOid;

      /**
       * The PIs wrt. to this postponed AI.
       * 
       * @see ProcessCumulationPolicy
       */
      public final long piOid;

      public final int priority;

      /**
       * The start time of this AI.
       */
      public final Date aiStart;

      /**
       * The start time of this AI.
       * 
       * @see ProcessCumulationPolicy
       */
      public final Date piStart;

      public PostponedActivityDetails(int priority, long aiOid, long piOid, Date aiStart,
            Date piStart)
      {
         this.aiOid = aiOid;
         this.piOid = piOid;
         
         this.priority = priority;

         this.aiStart = new Date(aiStart.getTime());
         this.piStart = new Date(piStart.getTime());
      }
   }
   
}
