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

import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;


/**
 * @author florin.herinean
 * @version $Revision: $
 */
public class WorklistProcessFiltersQuery extends CustomActivityInstanceQuery
{
   private static final long serialVersionUID = 1L;
   
   public static final String ID = WorklistProcessFiltersQuery.class.getName();

   private ParticipantInfo participant;

   public static WorklistProcessFiltersQuery forParticipant(ParticipantInfo participant)
   {
      return new WorklistProcessFiltersQuery(participant);
   }

   public ParticipantInfo getParticipant()
   {
      return participant;
   }

   private WorklistProcessFiltersQuery(ParticipantInfo info)
   {
      super(ID);
      participant = info;
   }
}
