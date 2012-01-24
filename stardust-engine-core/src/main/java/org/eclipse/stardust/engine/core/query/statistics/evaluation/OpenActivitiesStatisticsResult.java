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

import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatisticsQuery;

/**
 * @author rsauer
 * @version $Revision$
 */
public class OpenActivitiesStatisticsResult extends OpenActivitiesStatistics
{
   static final long serialVersionUID = -3512161095498482971L;
   
   public OpenActivitiesStatisticsResult(OpenActivitiesStatisticsQuery query, int nDaysHistory)
   {
      super(query, nDaysHistory);
   }
}
