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
package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventDescriptionStateChange;


public class HistoricalEventDescriptionStateChangeDetails implements
      HistoricalEventDescriptionStateChange
{
   private final ActivityInstanceState fromState;
   private final ActivityInstanceState toState;
   private final Participant toPerformer;

   public HistoricalEventDescriptionStateChangeDetails(ActivityInstanceState fromState,
         ActivityInstanceState toState, Participant toPerformer)
   {
      super();
      this.fromState = fromState;
      this.toState = toState;
      this.toPerformer = toPerformer;
   }

   private static final long serialVersionUID = 1L;

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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.HistoricalEventDescriptionStateChange#getFromState()
    */
   public ActivityInstanceState getFromState()
   {
      return fromState;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.dto.HistoricalEventDescriptionStateChange#getToState()
    */
   public ActivityInstanceState getToState()
   {
      return toState;
   }

   /* (non-Javadoc)
    * @see ag.carnot.workflow.runtime.HistoricalEventDescriptionStateChange#getToPerformer()
    */
   public Participant getToPerformer()
   {
      return toPerformer;
   }
}
