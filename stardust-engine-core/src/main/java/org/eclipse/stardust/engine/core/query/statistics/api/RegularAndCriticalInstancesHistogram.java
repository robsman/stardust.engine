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
import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;


/**
 * @author rsauer
 * @version $Revision$
 */
public class RegularAndCriticalInstancesHistogram
      implements IInstancesHistogram, ICriticalInstancesHistogram, Serializable
{
   private static final long serialVersionUID = 1l;

   public final InstancesHistogram instances;

   public final CriticalInstancesHistogram criticalInstances;

   public RegularAndCriticalInstancesHistogram()
   {
      this.instances = new InstancesHistogram();
      this.criticalInstances = new CriticalInstancesHistogram();
   }

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
      return criticalInstances.getCriticalInstancesCount(priority);
   }

   public long getCriticalInstancesCount(int priority)
   {
      return criticalInstances.getCriticalInstancesCount(priority);
   }

   public long getTotalCriticalInstancesCount()
   {
      return criticalInstances.getTotalCriticalInstancesCount();
   }

   public Set<Long> getCriticalInstances(ProcessInstancePriority priority)
   {
      return criticalInstances.getCriticalInstances(priority);
   }

   public Set<Long> getCriticalInstances(int priority)
   {
      return criticalInstances.getCriticalInstances(priority);
   }

   public Set<Long> getTotalCriticalInstances()
   {
      return criticalInstances.getTotalCriticalInstances();
   }

   public long getInterruptedInstancesCount()
   {
      return criticalInstances.getInterruptedInstancesCount();
   }

   @Override
   public Set<Long> getInterruptedInstances()
   {
      return criticalInstances.getInterruptedInstances();
   }

}
