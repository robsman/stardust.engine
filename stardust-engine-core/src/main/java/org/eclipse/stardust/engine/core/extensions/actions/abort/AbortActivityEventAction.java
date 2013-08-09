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
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;


/**
 * @author fherinean
 * @version $Revision: 12382 $
 */
public class AbortActivityEventAction implements EventActionInstance
{
   private static final Logger trace = LogManager.getLogger(AbortActivityEventAction.class);

   private AbortScope scope;
   
   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      Object scopeId = actionAttributes.get(PredefinedConstants.ABORT_ACTION_SCOPE_ATT);
      if (scopeId instanceof String)
      {
         scope = (AbortScope) AbortScope.getKey(AbortScope.class, (String) scopeId);
      }
      if (scope == null)
      {
         scope = AbortScope.RootHierarchy;
      }
   }

   public Event execute(Event event)
   {
      ActivityInstanceBean activityInstance = ActivityInstanceBean.findByOID(event
            .getObjectOID());
      if ( !activityInstance.isTerminated() && !activityInstance.isAborting())
      {
         ActivityInstanceUtils.abortActivityInstance(activityInstance, scope);

         if (AbortScope.RootHierarchy == scope)
         {
            // create and return a copy of event ...
            Event alteredEvent = new Event(event.getType(), event.getObjectOID(), event
                  .getHandlerOID(), event.getHandlerModelElementOID(), event.getEmitterType());
            alteredEvent.setAttributes(event.getAttributes());

            // ... but with different intended state.
            alteredEvent.setIntendedState(ActivityInstanceState.Aborted);

            event = alteredEvent;
         }
      }
      else
      {
         trace.warn("Skipping event based abortion of terminated or an aborting activity instance "
               + activityInstance + ".");
      }
      return event;
   }
}
