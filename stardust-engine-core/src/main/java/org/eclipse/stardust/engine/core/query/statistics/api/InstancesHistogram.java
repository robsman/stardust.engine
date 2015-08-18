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

import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;


/**
 * @author rsauer
 * @version $Revision$
 */
public class InstancesHistogram implements IInstancesHistogram, Serializable
{
   private static final long serialVersionUID = 1l;

   public long nLowPriorityInstances;

   public long nNormalPriorityInstances;

   public long nHighPriorityInstances;

   public long getInstancesCount(ProcessInstancePriority priority)
   {
      return getInstancesCount(priority.getValue());
   }

   public long getInstancesCount(int priority)
   {
      switch (priority)
      {
      case ProcessInstancePriority.LOW:
         return nLowPriorityInstances;

      case ProcessInstancePriority.HIGH:
         return nHighPriorityInstances;

      default:
         return nNormalPriorityInstances;
      }
   }

   public long getTotalInstancesCount()
   {
      return nLowPriorityInstances + nNormalPriorityInstances + nHighPriorityInstances;
   }

   public long registerInstance(int priority)
   {
      switch (priority)
      {
      case ProcessInstancePriority.LOW:
         return ++nLowPriorityInstances;

      case ProcessInstancePriority.HIGH:
         return ++nHighPriorityInstances;

      default:
         return ++nNormalPriorityInstances;
      }
   }
}
