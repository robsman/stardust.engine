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
package org.eclipse.stardust.engine.core.extensions.actions.schedule;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ScheduleEventAction implements EventActionInstance
{
   private static final Logger trace = LogManager.getLogger(ScheduleEventAction.class);

   private Map attributes = Collections.EMPTY_MAP;

   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      this.attributes = actionAttributes;
   }

   public Event execute(Event event)
   {
      if (Event.ACTIVITY_INSTANCE == event.getType())
      {
         IActivityInstance ai = (IActivityInstance) EventUtils
               .getEventSourceInstance(event);

         if ((null != ai) && !ai.isTerminated() && !ai.isAborting())
         {
            Object targetState = attributes.get(PredefinedConstants.TARGET_STATE_ATT);
            if (ActivityInstanceState.Suspended.equals(targetState))
            {
               try
               {
                  ai.suspend();
               }
               catch (IllegalStateChangeException e)
               {
                  throw new UnrecoverableExecutionException(
                        "Unable to suspend activity instance " + ai);
               }
            }
            else if (ActivityInstanceState.Hibernated.equals(targetState))
            {
               try
               {
                  ai.hibernate();
               }
               catch (IllegalStateChangeException e)
               {
                  throw new UnrecoverableExecutionException(
                        "Unable to hibernate activity instance " + ai);
               }
            }
            else
            {
               // @todo (france, ub): handle also other states (active?)
               trace.warn("Skipping activity scheduling due to unsupported target state "
                     + targetState);
            }
         }
         else
         {
            if(ai.isTerminated())
            {
               trace.info("Skipping schedule action for terminated activity " + ai);
            }
            else
            {
               trace.info("Skipping schedule action for an aborting activity " + ai);
               
            }
         }
      }
      else
      {
         trace.error("Skipping activity scheduling due to unsupported event type "
               + event.getType());
      }

      return event;
   }
}
