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
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class PerformanceStatistics extends CustomUserQueryResult
{
   private static final long serialVersionUID = 1l;

   /**
    * Performance for all model participants requested.
    */
   protected final Map<ParticipantDepartmentPair, ModelParticipantPerformance> mpPerformance;

   protected PerformanceStatistics(PerformanceStatisticsQuery query)
   {
      super(query);

      this.mpPerformance = CollectionUtils.newMap();
   }

   public Collection<ModelParticipantPerformance> getModelParticipantPerformances()
   {
      return Collections.unmodifiableCollection(mpPerformance.values());
   }

   @Deprecated
   public ModelParticipantPerformance getModelParticipantPerformance(String participantId)
   {
      ParticipantDepartmentPair id = new ParticipantDepartmentPair(participantId, 0);
      return mpPerformance.get(id);
   }
   
   public ModelParticipantPerformance getModelParticipantPerformance(
         ModelParticipantInfo modelParticipant)
   {
      return mpPerformance.get(
            ParticipantDepartmentPair.getParticipantDepartmentPair(modelParticipant));
   }

   public static class ModelParticipantPerformance extends PerformanceDetails
   {
      private static final long serialVersionUID = 1l;

      @Deprecated
      public final String participantId;
      
      public final ModelParticipantInfo modelParticipantInfo;

      public long nUsers;

      public long nLoggedInUsers;

      public ModelParticipantPerformance(ModelParticipantInfo modelParticipantInfo)
      {
         this.participantId = modelParticipantInfo.getId();
         this.modelParticipantInfo = modelParticipantInfo;
      }
      
   }

   public static class PerformanceDetails implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final OpenInstancesDetails openActivities = new OpenInstancesDetails();

      public final InstancesHistogram openProcesses = new InstancesCount();

      public final OpenInstancesDetails openCriticalActivities = new OpenInstancesDetails();

      public final InstancesHistogram openCriticalProcesses = new InstancesCount();

      public final InstancesHistogram completedActivities = new InstancesCount();

      public final InstancesHistogram completedProcesses = new InstancesCount();
   }

   /**
    * @deprecated Please directly use {@link InstancesHistogram}
    */
   public static class InstancesCount extends InstancesHistogram
   {

      private static final long serialVersionUID = 1l;

      public long getTotalInstances()
      {
         return super.getTotalInstancesCount();
      }

   }

   public static class OpenInstancesDetails implements IInstancesHistogram, Serializable
   {

      private static final long serialVersionUID = 1l;

      private final InstancesHistogram instances = new InstancesHistogram();

      public long tLowPriorityWorktime;

      public long tNormalPriorityWorktime;

      public long tHighPriorityWorktime;

      public long getInstancesCount(ProcessInstancePriority priority)
      {
         return instances.getInstancesCount(priority);
      }

      public long getInstancesCount(int priority)
      {
         return instances.getInstancesCount(priority);
      }

      public long getTotalInstancesCount()
      {
         return instances.getTotalInstancesCount();
      }

      public long getTotalWorktime()
      {
         return tLowPriorityWorktime + tNormalPriorityWorktime + tHighPriorityWorktime;
      }

      public long registerInstance(int priority, long targetExecutionTime)
      {
         instances.registerInstance(priority);

         switch (priority)
         {
         case -1:
            return tLowPriorityWorktime += targetExecutionTime;

         case 1:
            return tHighPriorityWorktime += targetExecutionTime;

         default:
            return tNormalPriorityWorktime += targetExecutionTime;
         }
      }

   }

}
