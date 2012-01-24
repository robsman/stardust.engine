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
package org.eclipse.stardust.engine.core.extensions.actions.delegate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DelegateEventAction implements EventActionInstance
{
   private Map attributes;

   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      this.attributes = actionAttributes;
   }

   public Event execute(Event event)
   {
      if (Event.ACTIVITY_INSTANCE == event.getType())
      {
         ActivityInstanceBean ai = (ActivityInstanceBean) EventUtils
               .getEventSourceInstance(event);

         if (!ai.isTerminated() && !ai.isAborting())
         {
            Object targetWorklist = attributes
                  .get(PredefinedConstants.TARGET_WORKLIST_ATT);
            if (targetWorklist instanceof TargetWorklist)
            {
               performDelegation(ai, (TargetWorklist) targetWorklist);
            }
            else
            {
               throw new UnrecoverableExecutionException(
                     "Invalid target worklist for delegate activity action: "
                           + targetWorklist);
            }
         }
         else
         {
            AuditTrailLogger.getInstance(LogCode.EVENT, ai).warn(
                  ai.isTerminated() ? 
                        "Skipping delegation of already terminated activity." :
                           "Skipping delegation of an aborting activity.");
         }
      }
      else
      {
         AuditTrailLogger.getInstance(LogCode.EVENT).error(
               "Skipping activity delegation as the event " + event
                     + " is not triggered by an activity.");
      }

      return event;
   }

   private void performDelegation(ActivityInstanceBean ai, TargetWorklist targetWorklist)
         throws UnrecoverableExecutionException
   {
      if (TargetWorklist.DefaultPerformer.equals(targetWorklist))
      {
         try
         {
            ai.delegateToDefaultPerformer();
         }
         catch (AccessForbiddenException e)
         {
            throw new UnrecoverableExecutionException(
                  "Activity delegation is not allowed.");
         }
         catch (PublicException e)
         {
            throw new UnrecoverableExecutionException(
                  "Error during activity delegation");
         }
      }
      else if (TargetWorklist.CurrentUser.equals(targetWorklist))
      {
         IUser user = SecurityProperties.getUser();
         if (user != null && 0 != user.getOID())
         {
            try
            {
               ai.delegateToUser(user);
            }
            catch (AccessForbiddenException e)
            {
               throw new UnrecoverableExecutionException(
                     "Access for current user is forbidden.");
            }
            catch (PublicException e)
            {
               throw new UnrecoverableExecutionException(
                     "Error during activity delegation.");
            }
         }
         else
         {
            throw new UnrecoverableExecutionException(
                  "Couldn't schedule activity instance to current user "
                        + "- running in noninteractive context.");
         }
      }
      else if (TargetWorklist.Participant.equals(targetWorklist))
      {
         Object targetParticipant = attributes
               .get(PredefinedConstants.TARGET_PARTICIPANT_ATT);
         if (targetParticipant instanceof String)
         {
            IModelParticipant participant = ((IModel) ai.getActivity().getModel())
                  .findParticipant((String) targetParticipant);
            try
            {
               ai.delegateToParticipant(participant);
            }
            catch (AccessForbiddenException e)
            {
               throw new UnrecoverableExecutionException("Access for participant '"
                     + participant.getId() + "' is forbidden.");
            }
            catch (PublicException e)
            {
               throw new UnrecoverableExecutionException(
                     "Error during activity delegation.");
            }
         }
      }
      else if (TargetWorklist.RandomUser.equals(targetWorklist))
      {
         assignToRandomUser(ai);
      }
      else
      {
         throw new UnrecoverableExecutionException(
               "Invalid target worklist for delegate activity action: "
                     + targetWorklist);
      }
   }

   private void assignToRandomUser(ActivityInstanceBean ai)
         throws UnrecoverableExecutionException
   {
      List users = UserBean.findAllForParticipant(ai.getActivity().getPerformer());

      while (true)
      {
         if (0 < users.size())
         {
            int chosenUser = (int) Math.round(Math.floor(Math.random() * (users.size())));

            try
            {
               IUser user = (IUser) users.get(chosenUser);
               ai.delegateToUser(user);
               if (ai.getCurrentUserPerformerOID() == user.getOID())
               {
                  break;
               }
            }
            catch (PublicException e)
            {
               // catches both access forbidden and general errors during assignment
               users.remove(chosenUser);
            }
         }
         else
         {
            throw new UnrecoverableExecutionException("No users found to assign '" + ai
                  + "'.");
         }
      }
   }
}
