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
package org.eclipse.stardust.engine.core.extensions.conditions.statechange;

import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class StatechangeCondition implements EventHandlerInstance
{
   private static final Logger trace = LogManager.getLogger(StatechangeCondition.class);
   

   private static final String PRP_ABORTING_PREDECESSOR_STATE = "Infinity.Engine.EventHandling.AbortingPredecessorState";

   private Map attributes;

   public void bootstrap(Map attributes)
   {
      this.attributes = attributes;
   }

   public boolean accept(Event event)
   {
      boolean accepted = false;

      if (Event.ACTIVITY_INSTANCE == event.getType())
      {
         try
         {
            final ActivityInstanceState targetState = (ActivityInstanceState) attributes
                  .get(PredefinedConstants.TARGET_STATE_ATT);

            ActivityInstanceState eventSourceState = (ActivityInstanceState) event
                  .getAttribute(PredefinedConstants.SOURCE_STATE_ATT);
            final ActivityInstanceState eventTargetState = (ActivityInstanceState) event
                  .getAttribute(PredefinedConstants.TARGET_STATE_ATT);

            if (ActivityInstanceState.Aborting == eventTargetState
                  && ActivityInstanceState.Aborted == targetState)
            {
               ActivityInstanceBean ai = ActivityInstanceBean.findByOID(event
                     .getObjectOID());
               ai.setPropertyValue(PRP_ABORTING_PREDECESSOR_STATE, new Integer(
                     eventSourceState.getValue()));
            }
            else
            {
               final ActivityInstanceState sourceState = (ActivityInstanceState) attributes
                     .get(PredefinedConstants.SOURCE_STATE_ATT);

               if (ActivityInstanceState.Aborting == eventSourceState
                     && ActivityInstanceState.Aborted == targetState)
               {
                  ActivityInstanceBean ai = ActivityInstanceBean.findByOID(event
                        .getObjectOID());
                  Integer predecessorState = (Integer) ai
                        .getPropertyValue(PRP_ABORTING_PREDECESSOR_STATE);
                  if (null != predecessorState)
                  {
                     eventSourceState = ActivityInstanceState.getState(predecessorState
                           .intValue());
                  }
               }

               if (null == sourceState || sourceState.equals(eventSourceState))
               {
                  accepted = null == targetState || targetState.equals(eventTargetState);
               }
            }
         }
         catch (ClassCastException e)
         {
            trace.warn("Invalid state attribute for event " + event + ".", e);
         }
      }

      return accepted;
   }
}
