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
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ProcessStatechangeCondition implements EventHandlerInstance
{
   private static final Logger trace = LogManager
         .getLogger(ProcessStatechangeCondition.class);
   
   private static final String PRP_ABORTING_PREDECESSOR_STATE = "Infinity.Engine.EventHandling.AbortingPredecessorState";
   
   private Map attributes;

   public void bootstrap(Map attributes)
   {
      this.attributes = attributes;
   }

   public boolean accept(Event event)
   {
      boolean accepted = false;

      if (Event.PROCESS_INSTANCE == event.getType())
      {
         try
         {
            final ProcessInstanceState targetState = (ProcessInstanceState) attributes
                  .get(PredefinedConstants.TARGET_STATE_ATT);
            
            ProcessInstanceState eventSourceState = (ProcessInstanceState) event
                  .getAttribute(PredefinedConstants.SOURCE_STATE_ATT);
            final ProcessInstanceState eventTargetState = (ProcessInstanceState) event
                  .getAttribute(PredefinedConstants.TARGET_STATE_ATT);
            
            if (ProcessInstanceState.Aborting == eventTargetState
                  && ProcessInstanceState.Aborted == targetState)
            {
               ProcessInstanceBean pi = ProcessInstanceBean.findByOID(event
                     .getObjectOID());
               pi.setPropertyValue(PRP_ABORTING_PREDECESSOR_STATE, new Integer(
                     eventSourceState.getValue()));
            }
            else
            {
               final ProcessInstanceState sourceState = (ProcessInstanceState) attributes
                     .get(PredefinedConstants.SOURCE_STATE_ATT);

               if (ProcessInstanceState.Aborting == eventSourceState
                     && ProcessInstanceState.Aborted == targetState)
               {
                  ProcessInstanceBean pi = ProcessInstanceBean.findByOID(event
                        .getObjectOID());
                  Integer predecessorState = (Integer) pi
                        .getPropertyValue(PRP_ABORTING_PREDECESSOR_STATE);
                  if (null != predecessorState)
                  {
                     eventSourceState = ProcessInstanceState.getState(predecessorState
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
