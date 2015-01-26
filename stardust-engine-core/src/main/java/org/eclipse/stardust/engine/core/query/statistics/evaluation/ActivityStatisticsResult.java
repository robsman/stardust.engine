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

import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.ActivityStatisticsQuery;

/**
 * @author rsauer
 * @version $Revision$
 */
public class ActivityStatisticsResult extends ActivityStatistics
{
   static final long serialVersionUID = -6036388240512274629L;
   
   public ActivityStatisticsResult(ActivityStatisticsQuery query)
   {
      super(query);
   }

   public void addPriorizedInstances(String processId, String activityId, int priority,
         long aiOid, boolean isCritical, boolean isInterrupted)
   {
      ProcessEntry processEntry = (ProcessEntry) processEntries.get(processId);
      if (null == processEntry)
      {
         processEntry = new ProcessEntry(processId);
         processEntries.put(processId, processEntry);
      }

      ActivityEntry aiHistogram = processEntry.getForActivity(activityId);
      if (null == aiHistogram)
      {
         aiHistogram = new ActivityEntry(processId, activityId);
         processEntry.activityEntries.put(activityId, aiHistogram);
      }

      aiHistogram.registerInstance(priority,aiOid, isCritical, isInterrupted);
   }
}
