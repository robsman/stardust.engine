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
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessStatisticsQuery;

/**
 * @author rsauer
 * @version $Revision$
 */
public class ProcessStatisticsResult extends ProcessStatistics
{
   static final long serialVersionUID = -5825543169280211775L;

   public ProcessStatisticsResult(ProcessStatisticsQuery query)
   {
      super(query);
   }

   public void addPriorizedInstances(String processId, int priority, long instanceOid,
         boolean isCritical)
   {
      ProcessEntry processHistogram = (ProcessEntry) priorizedInstancesHistogram.get(processId);
      if (null == processHistogram)
      {
         processHistogram = new ProcessEntry(processId);
         priorizedInstancesHistogram.put(processId, processHistogram);
      }

      processHistogram.registerInstance(priority, instanceOid, isCritical);
   }
}
