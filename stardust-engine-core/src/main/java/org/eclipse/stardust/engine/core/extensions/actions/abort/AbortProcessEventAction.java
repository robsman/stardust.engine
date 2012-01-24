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
package org.eclipse.stardust.engine.core.extensions.actions.abort;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AbortProcessEventAction implements EventActionInstance
{
   private static final Logger trace = LogManager.getLogger(AbortProcessEventAction.class);
   
   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      // nothing to do here
   }

   public Event execute(Event event)
   {
      IProcessInstance processInstance = EventUtils.getProcessInstance(event);
      if ((null != processInstance) && !processInstance.isTerminated() && !processInstance.isAborting())
      {
         // Abort the complete process hierarchy (starting from the root)
         IProcessInstance rootProcessInstance = processInstance.getRootProcessInstance();
         ProcessInstanceUtils.abortProcessInstance(rootProcessInstance.getOID());
         // create and return a copy of event ...
         Event alteredEvent = new Event(event.getType(), event.getObjectOID(), event
               .getHandlerOID(), event.getEmitterType());
         alteredEvent.setAttributes(event.getAttributes());

         // ... but with different intended state.
         alteredEvent.setIntendedState(processInstance.isAborted()
               ? ActivityInstanceState.Aborted
               : ActivityInstanceState.Aborting);
         event = alteredEvent;

      }
      else
      {
         trace.warn("Skipping event based abortion of terminated or an aborting process instance "
               + processInstance + ".");
      }
      
      return event;
   }
}
