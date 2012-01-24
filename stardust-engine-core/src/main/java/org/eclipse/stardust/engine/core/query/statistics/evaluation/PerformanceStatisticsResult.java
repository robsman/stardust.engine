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

import org.eclipse.stardust.engine.core.query.statistics.api.ParticipantDepartmentPair;
import org.eclipse.stardust.engine.core.query.statistics.api.PerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.PerformanceStatisticsQuery;


/**
 * @author rsauer
 * @version $Revision$
 */
public class PerformanceStatisticsResult extends PerformanceStatistics
{
   static final long serialVersionUID = -6832532527126773382L;
   
   public PerformanceStatisticsResult(PerformanceStatisticsQuery query,
         Map<ParticipantDepartmentPair, ModelParticipantPerformance> mpPerformance)
   {
      super(query);

      this.mpPerformance.putAll(mpPerformance);
   }
}
