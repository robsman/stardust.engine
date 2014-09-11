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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.ConcatenatedList;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;

/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class OpenActivitiesStatistics extends CustomActivityInstanceQueryResult
{
   private static final long serialVersionUID = 1l;

   protected final Map<String, List<OpenActivities>> openActivities;

   protected final int nDaysHistory;

   protected OpenActivitiesStatistics(OpenActivitiesStatisticsQuery query,
         int nDaysHistory)
   {
      super(query);

      this.openActivities = CollectionUtils.newMap();

      this.nDaysHistory = nDaysHistory;
   }

   public List<OpenActivities> getOpenActivities()
   {
      ConcatenatedList result = null;
      List prev = null;
      for (Iterator i = openActivities.values().iterator(); i.hasNext();)
      {
         List/* <OpenActivities> */openActList = (List) i.next();
         if (prev == null)
         {
            prev = openActList;
         }
         else
         {
            if (result == null)
            {
               result = new ConcatenatedList(prev, openActList);
            }
            else
            {
               result = new ConcatenatedList(result, new ConcatenatedList(prev,
                     openActList));
            }
            prev = null;
         }
      }
      if (result == null)
      {
         return prev == null
               ? Collections.EMPTY_LIST
               : Collections.unmodifiableList(prev);
      }
      if (prev != null)
      {
         result = new ConcatenatedList(result, prev);
      }
      return Collections.unmodifiableList(result);
   }

   public int getNumberOfDaysHistory()
   {
      return nDaysHistory;
   }

   public int getOpenActivitiesIndex(String processId,
         ParticipantInfo onBehalfOfParticipant)
   {
      List openActList = (List) openActivities.get(processId);

      if (openActList == null)
      {
         openActList = CollectionUtils.newList();
         openActivities.put(processId, openActList);
      }

      for (int i = 0; i < openActList.size(); ++i)
      {
         OpenActivities contrib = (OpenActivities) openActList.get(i);
         if (ParticipantInfoHelper.areEqual(contrib.performerInfo, onBehalfOfParticipant))
         {
            return i;
         }
      }
      return -1;
   }

   public OpenActivities findOpenActivities(String processId,
         ParticipantInfo onBehalfOfParticipant)
   {
      OpenActivities contribution = null;
      int index = getOpenActivitiesIndex(processId, onBehalfOfParticipant);
      List<OpenActivities> openActList = openActivities.get(processId);
      if (index == -1)
      {
         contribution = new OpenActivities(processId, onBehalfOfParticipant, null, 0,
               nDaysHistory);
         openActList.add(contribution);
      }
      else
      {
         contribution = openActList.get(index);
      }

      return contribution;
   }

   @Deprecated
   public OpenActivities findOpenActivities(String processId,
         PerformerType onBehalfOfKind, long onBehalfOfOid)
   {
      ParticipantInfo performer = ParticipantInfoHelper.getLegacyParticipantInfo(
            onBehalfOfKind, onBehalfOfOid);
      return findOpenActivities(processId, performer);
   }

   public static class OpenActivities implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final String processId;

      @Deprecated
      public final PerformerType performerKind;

      @Deprecated
      public final long performerOid;

      public final ParticipantInfo performerInfo;

      public final OpenActivitiesDetails lowPriority;

      public final OpenActivitiesDetails normalPriority;

      public final OpenActivitiesDetails highPriority;

      public OpenActivities(String processId, ParticipantInfo performerInfo,
            PerformerType performerKind, long performerOid, int nDaysHistory)
      {
         this.processId = processId;
         this.performerKind = performerKind;
         this.performerOid = performerOid;
         this.performerInfo = performerInfo;

         this.lowPriority = new OpenActivitiesDetails(ProcessInstancePriority.LOW,
               nDaysHistory);
         this.normalPriority = new OpenActivitiesDetails(ProcessInstancePriority.NORMAL,
               nDaysHistory);
         this.highPriority = new OpenActivitiesDetails(ProcessInstancePriority.HIGH,
               nDaysHistory);
      }

      public OpenActivitiesDetails getDetailsForPriority(int priority)
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
   }

   public static class OpenActivitiesDetails implements Serializable
   {

      private static final long serialVersionUID = 1l;

      public final int priority;

      /**
       * The number of currently pending AIs.
       */
      public long pendingAis;

      /**
       * List of pending AI instances;
       */
      public SortedSet<Long> pendingAiInstances;

      /**
       * The number of currently hibernated AI instances
       */
      public long hibernatedAis;

      /**
       * The number of currently critical hibernated AI instances       
       */
      public long hibernatedCriticalAis;
      
      /**
       * List of hibernated AIs
       */
      public SortedSet<Long> hibernatedAiInstances;

      /**
       * List of hibernated critical AIs
       */
      public SortedSet<Long> hibernatedCriticalAiInstances;
      
      
      /**
       * The number of distinct PIs wrt. to currently pending AIs.
       */
      public long pendingPis;

      /**
       * The number of currently pending AIs exceeding the critical duration.
       */
      public long pendingCriticalAis;

      /**
       * List of pending critical AI instances
       */
      public SortedSet<Long> pendingCriticalAiInstances;

      /**
       * The number of distinct PIs wrt. to currently pending critical AIs.
       */
      public long pendingCriticalPis;

      /**
       * Holds the history of pending AIs. First entry is for yesterday, second entry the
       * day before yesterday etc.
       * 
       * Pending means the AI was not terminated at midnight.
       */
      public final long[] pendingAisHistory;

      /**
       * Holds the history of pending AI Oids;
       */
      private SortedSet<Long>[] pendingAiInstancesHistory;

      /**
       * Holds the history of pending PIs wrt. to history of pending AIs.
       */
      public final long[] pendingPisHistory;

      public final long[] pendingCriticalAisHistory;

      /**
       * Holds the hisotry of pending critical AI Oids;
       */
      private SortedSet<Long>[] pendingCriticalAiInstancesHistory;

      public final long[] pendingCriticalPisHistory;

      protected OpenActivitiesDetails(int priority, int nDayHistory)
      {
         this.priority = priority;

         this.pendingAiInstances = CollectionUtils.newTreeSet();
         this.pendingCriticalAiInstances = CollectionUtils.newTreeSet();
         this.hibernatedAiInstances = CollectionUtils.newTreeSet();
         this.hibernatedCriticalAiInstances = CollectionUtils.newTreeSet();

         this.pendingAisHistory = new long[nDayHistory];
         this.pendingPisHistory = new long[nDayHistory];
         this.pendingAiInstancesHistory = new TreeSet[nDayHistory];

         this.pendingCriticalAisHistory = new long[nDayHistory];
         this.pendingCriticalPisHistory = new long[nDayHistory];
         this.pendingCriticalAiInstancesHistory = new TreeSet[nDayHistory];
      }

      public SortedSet<Long> getPendingAiInstancesHistory(int index)
      {
         if (index < this.pendingAiInstancesHistory.length)
         {
            if (this.pendingAiInstancesHistory[index] == null)
            {
               this.pendingAiInstancesHistory[index] = CollectionUtils.newTreeSet();
            }
            return this.pendingAiInstancesHistory[index];
         }
         return null;
      }

      public SortedSet<Long> getPendingCriticalAiInstancesHistory(int index)
      {
         if (index < this.pendingCriticalAiInstancesHistory.length)
         {
            if (this.pendingCriticalAiInstancesHistory[index] == null)
            {
               this.pendingCriticalAiInstancesHistory[index] = CollectionUtils.newTreeSet();
            }
            return this.pendingCriticalAiInstancesHistory[index];
         }
         return null;
      }
   }

}
