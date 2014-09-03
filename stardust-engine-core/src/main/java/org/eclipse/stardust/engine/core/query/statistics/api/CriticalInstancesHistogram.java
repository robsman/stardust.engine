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
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CriticalInstancesHistogram
      implements ICriticalInstancesHistogram, Serializable
{
   private static final long serialVersionUID = 1l;

   private InstancesHistogram instances = new InstancesHistogram();

   private SortedSet<Long> lowPriorityCriticalInstances;

   private SortedSet<Long> normalPriorityCriticalInstances;

   private SortedSet<Long> highPriorityCriticalInstances;
   
   private SortedSet<Long> interupptedInstances;

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

   public long getCriticalInstancesCount(ProcessInstancePriority priority)
   {
      return getCriticalInstancesCount(priority.getValue());
   }

   public long getCriticalInstancesCount(int priority)
   {
      return getCriticalInstancesSet(priority, false).size();
   }

   public Set<Long> getCriticalInstances(ProcessInstancePriority priority)
   {
      return getCriticalInstances(priority.getValue());
   }

   public Set<Long> getCriticalInstances(int priority)
   {
      return Collections.unmodifiableSet(getCriticalInstancesSet(priority, false));
   }

   public long getTotalCriticalInstancesCount()
   {
      return getCriticalInstancesCount(ProcessInstancePriority.Low)
            + getCriticalInstancesCount(ProcessInstancePriority.Normal)
            + getCriticalInstancesCount(ProcessInstancePriority.High);
   }
   
   public long getInterruptedInstancesCount()
   {
      return getInterruptedInstancesSet().size();
   }

   public Set<Long> getTotalCriticalInstances()
   {
      Set<Long> low = getCriticalInstancesSet(ProcessInstancePriority.LOW, false);
      Set<Long> normal = getCriticalInstancesSet(ProcessInstancePriority.NORMAL, false);
      Set<Long> high = getCriticalInstancesSet(ProcessInstancePriority.HIGH, false);

      // TODO create copy only when necessary (more than one set is not empty)
      SortedSet<Long> result = CollectionUtils.newTreeSet();
      result.addAll(low);
      result.addAll(normal);
      result.addAll(high);

      return Collections.unmodifiableSet(result);
   }

   public void registerInstance(int priority, long instanceOid, boolean critical, boolean isInterrupted)
   {
      instances.registerInstance(priority);

      if (critical)
      {
         getCriticalInstancesSet(priority, true).add(instanceOid);
      }
      
      if (isInterrupted)
      {
         getInterruptedInstancesSet().add(instanceOid);
      }
   }

   private Set<Long> getCriticalInstancesSet(int priority, boolean initializeInstance)
   {
      SortedSet<Long> result;

      switch (priority)
      {
      case ProcessInstancePriority.LOW:
         if (initializeInstance && null == lowPriorityCriticalInstances)
         {
            this.lowPriorityCriticalInstances = CollectionUtils.newTreeSet();
         }
         result = lowPriorityCriticalInstances;
         break;

      case ProcessInstancePriority.HIGH:
         if (initializeInstance && null == highPriorityCriticalInstances)
         {
            this.highPriorityCriticalInstances = CollectionUtils.newTreeSet();
         }
         result = highPriorityCriticalInstances;
         break;

      default:
         if (initializeInstance && null == normalPriorityCriticalInstances)
         {
            this.normalPriorityCriticalInstances = CollectionUtils.newTreeSet();
         }
         result = normalPriorityCriticalInstances;
      }

      return (null != result) ? result : Collections.<Long>emptySet();
   }
   
   private Set<Long> getInterruptedInstancesSet()
   {
      if (this.interupptedInstances == null)
      {
         this.interupptedInstances = CollectionUtils.newTreeSet();
      }      
      return this.interupptedInstances;
      
   }

}
