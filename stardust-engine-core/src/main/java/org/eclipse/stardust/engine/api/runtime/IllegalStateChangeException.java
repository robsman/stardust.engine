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
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.common.error.PublicException;


/**
 * Thrown when an activity instance is required to perform an illegal state change (i.e.
 * from completed to application).
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class IllegalStateChangeException extends PublicException
{
   /**
    * Creates a new exception with the original and the requested state of the activity instance.
    *
    * @param target the original state.
    * @param source the requested state.
    */
   public IllegalStateChangeException(
         ActivityInstanceState target, ActivityInstanceState source)
   {
      super(BpmRuntimeError.BPMRT_ILLEGAL_AI_STATE_CHANGE.raise(source, target));
   }

   public IllegalStateChangeException(String aiStringRepresentation,
         ActivityInstanceState target, ActivityInstanceState source)
   {
      super(BpmRuntimeError.BPMRT_ILLEGAL_AI_STATE_CHANGE_FOR_AI.raise(
            source, target, aiStringRepresentation));
   }
   
   public IllegalStateChangeException(String aiStringRepresentation,
         ActivityInstanceState target, ActivityInstanceState source, ProcessInstanceState piState)
   {
      super(BpmRuntimeError.BPMRT_ILLEGAL_AI_STATE_CHANGE_FOR_AI_WITH_PI_STATE.raise(
            source, target, aiStringRepresentation, piState));
   }
}
