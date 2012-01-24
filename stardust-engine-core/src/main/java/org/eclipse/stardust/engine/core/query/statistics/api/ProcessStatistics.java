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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class ProcessStatistics extends CustomProcessInstanceQueryResult
{
   private static final long serialVersionUID = 1l;

   protected Map<String, IProcessStatistics> priorizedInstancesHistogram;

   protected ProcessStatistics(ProcessStatisticsQuery query)
   {
      super(query);

      this.priorizedInstancesHistogram = CollectionUtils.newMap();
   }

   public IProcessStatistics getStatisticsForProcess(String processId)
   {
      return (IProcessStatistics) priorizedInstancesHistogram.get(processId);
   }

   public long getInstancesWithPriority(String processId, int priority)
   {
      IProcessStatistics processStatistics = getStatisticsForProcess(processId);

      return (null != processStatistics)
            ? processStatistics.getInstancesCount(priority)
            : 0l;
   }

   public long getInstancesWithPriority(int priority)
   {
      long result = 0;

      for (Iterator<IProcessStatistics> i = priorizedInstancesHistogram.values().iterator(); i.hasNext();)
      {
         IProcessStatistics processStatistics = i.next();

         result += ((null != processStatistics)
               ? processStatistics.getInstancesCount(priority)
               : 0l);
      }

      return result;
   }

   public interface IProcessStatistics extends ICriticalInstancesHistogram
   {
      String getProcessId();
   }

   protected static class ProcessEntry extends CriticalInstancesHistogram
         implements IProcessStatistics
   {
      private static final long serialVersionUID = 1l;

      public final String processId;

      public ProcessEntry(String processId)
      {
         this.processId = processId;
      }

      public String getProcessId()
      {
         return processId;
      }

      /**
       * @deprecated Please directly use {@link #getInstances(int)}
       */
      public long getInstancesWithPriority(int priority)
      {
         return getInstancesCount(priority);
      }

   }
}
