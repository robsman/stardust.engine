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

import java.util.Map;

import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatisticsQuery;


/**
 * @author rsauer
 * @version $Revision$
 */
public class UserPerformanceStatisticsResult extends UserPerformanceStatistics
{
   static final long serialVersionUID = 1514284637817505521L;

   public UserPerformanceStatisticsResult(UserPerformanceStatisticsQuery query,
         Users users, Map<Long, Map<String, PerformanceStatistics>> statistics)
   {
      super(query, users);

      this.statistics.putAll(statistics);
   }
}
