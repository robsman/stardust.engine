/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventDescriptionStateChange;


public class HistoricalEventDescriptionStateChangeDetails implements
      HistoricalEventDescriptionStateChange
{
   private final ActivityInstanceState fromState;
   private final ParticipantInfo fromPerformer;
   private final ActivityInstanceState toState;
   private final Participant toPerformer;
   private final ParticipantInfo onBehalfOfPerformer;

   public HistoricalEventDescriptionStateChangeDetails(ActivityInstanceState fromState, ParticipantInfo fromPerformer,
         ActivityInstanceState toState, Participant toPerformer, ParticipantInfo onBehalfOfPerformer)
   {
      this.fromState = fromState;
      this.fromPerformer = fromPerformer;
      this.toState = toState;
      this.toPerformer = toPerformer;
      this.onBehalfOfPerformer = onBehalfOfPerformer;
   }

   private static final long serialVersionUID = 1L;

   @Override
   public Serializable getItem(int idx)
   {
      switch (idx)
      {
         case FROM_STATE_IDX:
            return fromState;
         case TO_STATE_IDX:
            return toState;
         case TO_PERFORMER_IDX:
            return toPerformer;
         default:
            throw new PublicException(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED.raise());
      }
   }

   @Override
   public ActivityInstanceState getFromState()
   {
      return fromState;
   }

   @Override
   public ParticipantInfo getFromPerformer()
   {
      return fromPerformer;
   }

   @Override
   public ActivityInstanceState getToState()
   {
      return toState;
   }

   @Override
   public Participant getToPerformer()
   {
      return toPerformer;
   }

   @Override
   public ParticipantInfo getOnBehalfOfPerformer()
   {
      return onBehalfOfPerformer;
   }
}
