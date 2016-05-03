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
package org.eclipse.stardust.engine.core.extensions.actions.excludeuser;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ExcludeUserAction implements EventActionInstance
{
   private Map attributes;

   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      attributes = actionAttributes;
   }

   public Event execute(Event event)
   {
      if (Event.ACTIVITY_INSTANCE == event.getType())
      {
         ActivityInstanceBean ai = (ActivityInstanceBean) EventUtils
               .getEventSourceInstance(event);
         if (null != ai)
         {
            IProcessInstance pi = ai.getProcessInstance();
            IUser currentUserPerformer = ai.getCurrentUserPerformer();

            if (null != currentUserPerformer)
            {
               Object dataID = attributes.get(PredefinedConstants.EXCLUDED_PERFORMER_DATA);
               Object dataPath = attributes
                     .get(PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH);
               if ((dataID instanceof String)
                     && ((null == dataPath) || (dataPath instanceof String)))
               {
                  Object dataValue = pi.getInDataValue(ModelUtils.getData(pi
                        .getProcessDefinition(), (String) dataID), (String) dataPath);
                  if (dataValue instanceof IUser)
                  {
                     if (((IUser) dataValue).getOID() == currentUserPerformer.getOID())
                     {
                        throw new AccessForbiddenException(
                              BpmRuntimeError.BPMRT_USER_IS_EXLUDED_TO_PERFORM_AI.raise(
                                    currentUserPerformer.getId(),
                                    currentUserPerformer.getOID(),
                                    ai.getActivity().getId(), ai.getOID()));
                     }
                  }
                  else if (dataValue instanceof Long)
                  {
                     if (((Long) dataValue).longValue() == currentUserPerformer.getOID())
                     {
                        throw new AccessForbiddenException(
                              BpmRuntimeError.BPMRT_USER_IS_EXLUDED_TO_PERFORM_AI.raise(
                                    currentUserPerformer.getId(),
                                    currentUserPerformer.getOID(),
                                    ai.getActivity().getId(), ai.getOID()));
                     }
                  }
               }
               else
               {
                  throw new UnrecoverableExecutionException(
                        "Skipping user exclusion as of"
                              + " wrong configured data binding.");
               }
            }
         }
      }
      else
      {
         throw new UnrecoverableExecutionException("Skipping user exclusion as the event "
               + event + "is not triggered by an activity instance.");
      }

      return event;
   }
}
