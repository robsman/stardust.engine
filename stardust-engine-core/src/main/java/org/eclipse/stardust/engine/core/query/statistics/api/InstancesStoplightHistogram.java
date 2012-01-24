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


/**
 * @author rsauer
 * @version $Revision$
 */
public class InstancesStoplightHistogram
      implements IInstancesStoplightHistogram, Serializable
{
   private static final long serialVersionUID = 1l;

   public static final AbstractStoplightPolicy.Status GREEN = AbstractStoplightPolicy.GREEN;

   public static final AbstractStoplightPolicy.Status YELLOW = AbstractStoplightPolicy.YELLOW;

   public static final AbstractStoplightPolicy.Status RED = AbstractStoplightPolicy.RED;

   private SortedSet<Long> yellowInstances;

   private SortedSet<Long> redInstances;

   public long getYellowInstancesCount()
   {
      return getYellowInstancesSet(false).size();
   }

   public long getRedInstancesCount()
   {
      return getRedInstancesSet(false).size();
   }

   public long getInstancesCount(AbstractStoplightPolicy.Status status)
   {
      if (YELLOW.equals(status))
      {
         return getYellowInstancesCount();
      }
      else if (RED.equals(status))
      {
         return getRedInstancesCount();
      }

      return 0;
   }

   public Set<Long> getInstances(AbstractStoplightPolicy.Status status)
   {
      if (YELLOW.equals(status))
      {
         return getYellowInstances();
      }
      else if (RED.equals(status))
      {
         return getRedInstances();
      }

      return Collections.<Long>emptySet();
   }

   public Set<Long> getYellowInstances()
   {
      return Collections.unmodifiableSet(getYellowInstancesSet(false));
   }

   public Set<Long> getRedInstances()
   {
      return Collections.unmodifiableSet(getRedInstancesSet(false));
   }

   public void registerInstance(AbstractStoplightPolicy.Status status, long instanceOid)
   {
      if (YELLOW.equals(status))
      {
         registerYellowInstance(instanceOid);
      }
      else if (RED.equals(status))
      {
         registerRedInstance(instanceOid);
      }
   }

   public void registerYellowInstance(long instanceOid)
   {
      getYellowInstancesSet(true).add(instanceOid);
   }

   public void registerRedInstance(long instanceOid)
   {
      getRedInstancesSet(true).add(instanceOid);
   }

   public void registerInstance(AbstractStoplightPolicy.Status status, Set<Long> instanceOids)
   {
      if (YELLOW.equals(status))
      {
         registerYellowInstances(instanceOids);
      }
      else if (RED.equals(status))
      {
         registerRedInstances(instanceOids);
      }
   }

   public void registerYellowInstances(Set<Long> instanceOids)
   {
      getYellowInstancesSet(true).addAll(instanceOids);
   }

   public void registerRedInstances(Set<Long> instanceOids)
   {
      getRedInstancesSet(true).addAll(instanceOids);
   }

   private Set<Long> getYellowInstancesSet(boolean initializeInstance)
   {
      if (initializeInstance && null == yellowInstances)
      {
         this.yellowInstances = CollectionUtils.newTreeSet();
      }

      return (null != yellowInstances) ? yellowInstances : Collections.<Long>emptySet();
   }

   private Set<Long> getRedInstancesSet(boolean initializeInstance)
   {
      if (initializeInstance && null == redInstances)
      {
         this.redInstances = CollectionUtils.newTreeSet();
      }

      return (null != redInstances) ? redInstances : Collections.<Long>emptySet();
   }
}
