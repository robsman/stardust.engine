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

import org.eclipse.stardust.engine.core.query.statistics.api.*;
import org.eclipse.stardust.engine.core.spi.query.IActivityInstanceQueryEvaluator;
import org.eclipse.stardust.engine.core.spi.query.IProcessInstanceQueryEvaluator;
import org.eclipse.stardust.engine.core.spi.query.IQueryEvaluatorFactory;
import org.eclipse.stardust.engine.core.spi.query.IUserQueryEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class StatisticsQueryEvaluatorFactory implements IQueryEvaluatorFactory
{

   public IUserQueryEvaluator getUserQueryEvaluator(String queryId)
   {
      IUserQueryEvaluator result = null;

      if (WorklistStatisticsQuery.ID.equals(queryId))
      {
         result = new WorklistStatisticsRetriever();
      }
      else if (UserLoginStatisticsQuery.ID.equals(queryId))
      {
         result = new UserLoginStatisticsRetriever();
      }
      else if (UserWorktimeStatisticsQuery.ID.equals(queryId))
      {
         result = new UserWorktimeStatisticsRetriever();
      }
      else if (UserPerformanceStatisticsQuery.ID.equals(queryId))
      {
         result = new UserPerformanceStatisticsRetriever();
      }
      else if (PostponedActivitiesStatisticsQuery.ID.equals(queryId))
      {
         result = new PostponedActivitiesStatisticsRetriever();
      }
      else if (PerformanceStatisticsQuery.ID.equals(queryId))
      {
         result = new PerformanceStatisticsRetriever();
      }

      return result;
   }

   public IProcessInstanceQueryEvaluator getProcessInstanceQueryEvaluator(String queryId)
   {
      IProcessInstanceQueryEvaluator result = null;

      if (ProcessStatisticsQuery.ID.equals(queryId))
      {
         result = new ProcessStatisticsRetriever();
      }

      return result;
   }

   public IActivityInstanceQueryEvaluator getActivityInstanceQueryEvaluator(String queryId)
   {
      IActivityInstanceQueryEvaluator result = null;

      if (ActivityStatisticsQuery.ID.equals(queryId))
      {
         result = new ActivityStatisticsRetriever();
      }
      else if (OpenActivitiesStatisticsQuery.ID.equals(queryId))
      {
         result = new OpenActivitiesStatisticsRetriever();
      }
      else if (WorklistProcessFiltersQuery.ID.equals(queryId))
      {
         result = new WorklistProcessFiltersRetriever();
      }
      else if (CriticalityStatisticsQuery.ID.equals(queryId))
      {
         result = new CriticalityStatisticsRetriever();
      }

      return result;
   }

}
