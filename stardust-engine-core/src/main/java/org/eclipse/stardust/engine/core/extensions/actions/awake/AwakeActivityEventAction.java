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
package org.eclipse.stardust.engine.core.extensions.actions.awake;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AwakeActivityEventAction  implements EventActionInstance
{
   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {

   }

   public Event execute(Event event)
   {
      if (Event.ACTIVITY_INSTANCE == event.getType())
      {
         try
         {
            IActivityInstance ai = (IActivityInstance) EventUtils
                  .getEventSourceInstance(event);
            try
            {
               new WorkflowServiceImpl().activateAndComplete(ai.getOID(), null,
                     Collections.EMPTY_MAP, false);
            }
            catch (ConcurrencyException e)
            {
               AuditTrailLogger.getInstance(LogCode.EVENT, ai).error(
                     "Unable to wake up activity due to concurrent operations.", e);
            }
            catch (IllegalStateChangeException e)
            {
               AuditTrailLogger.getInstance(LogCode.EVENT, ai).error(
                     "Unable to wake up activity.", e);
            }
         }
         catch (ObjectNotFoundException e)
         {
            AuditTrailLogger.getInstance(LogCode.EVENT).error(
                  "Unable to wake up activity " + event.getObjectOID(), e);
         }
      }

      return event;
   }
}
