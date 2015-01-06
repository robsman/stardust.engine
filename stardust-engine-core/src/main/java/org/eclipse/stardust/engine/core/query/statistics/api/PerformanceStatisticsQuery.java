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

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;


/**
 * @author rsauer
 * @version $Revision$
 */
public class PerformanceStatisticsQuery extends CustomUserQuery
{
   static final long serialVersionUID = -9002055623426995094L;
   
   public static final String ID = PerformanceStatisticsQuery.class.getName();

   public static PerformanceStatisticsQuery forAllUsers()
   {
      return new PerformanceStatisticsQuery();
   }

   public static PerformanceStatisticsQuery forModelParticipant(ModelParticipantInfo
         modelParticipantInfo)
   {
      PerformanceStatisticsQuery query = forAllUsers();

      query.where(ParticipantAssociationFilter.forParticipant(modelParticipantInfo));

      return query;
   }
   
   @Deprecated
   public static PerformanceStatisticsQuery forModelParticipant(String participantId)
   {
      PerformanceStatisticsQuery query = forAllUsers();

      query.where(ParticipantAssociationFilter.forModelParticipant(participantId));

      return query;
   }

   protected PerformanceStatisticsQuery()
   {
      super(ID);
   }
}
