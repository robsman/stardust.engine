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
import org.eclipse.stardust.engine.core.query.statistics.api.ParticipantDepartmentPair;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatisticsQuery;


/**
 * @author rsauer
 * @version $Revision$
 */
public class WorklistStatisticsResult extends WorklistStatistics
{
   static final long serialVersionUID = -3387683277996236157L;

   public WorklistStatisticsResult(WorklistStatisticsQuery query, Users users,
         Map<Long, UserStatistics> userStatictics,
         Map<ParticipantDepartmentPair, ParticipantStatistics> participantStatistics)
   {
      super(query, users);

      this.userStatistics.putAll(userStatictics);
      this.modelParticipantStatistics.putAll(participantStatistics);
   }
}
