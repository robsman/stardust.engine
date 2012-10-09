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
package org.eclipse.stardust.engine.extensions.mail.action.sendmail;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IDataValue;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.MailHelper;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SendmailAction implements EventActionInstance
{
   private static final Logger trace = LogManager.getLogger(SendmailAction.class);

   private Map attributes = Collections.EMPTY_MAP;

   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      attributes = new HashMap(actionAttributes);
   }

   public Event execute(Event event)
   {
      final long timestamp = TimestampProviderUtils.getTimeStamp().getTime();

      if (Event.ACTIVITY_INSTANCE == event.getType())
      {
         IActivityInstance ai = (IActivityInstance) EventUtils
               .getEventSourceInstance(event);
         String mailBody = prepareMailBody(event, ai.getProcessInstance(), ai, timestamp);

         try
         {
            sendMail(event, mailBody, ai);
         }
         catch (PublicException e)
         {
            throw new UnrecoverableExecutionException("Failed sending mail.", e);
         }
      }
      else if (Event.PROCESS_INSTANCE == event.getType())
      {
         IProcessInstance pi = (IProcessInstance) EventUtils
               .getEventSourceInstance(event);
         String mailBody = prepareMailBody(event, pi, null, timestamp);

         try
         {
            sendMail(event, mailBody, pi);
         }
         catch (PublicException e)
         {
            throw new UnrecoverableExecutionException("Failed sending mail.", e);
         }
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Skipping 'Send Mail' action for event of unknown type "
                  + event.getType());
         }
      }

      return event;
   }

   private String prepareMailBody(Event event, IProcessInstance processInstance,
         IActivityInstance activityInstance, long currentTimeStamp)
   {
      String body = null;

      if (null != attributes.get(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT))
      {
         final String dataID = (String)attributes.get(PredefinedConstants.MAIL_ACTION_BODY_DATA_ATT);
         final String dataPath = (String)attributes.get(PredefinedConstants.MAIL_ACTION_BODY_DATA_PATH_ATT);
         // @todo (france, ub): use data path here!
         final IDataValue dataValue = processInstance.getDataValue(ModelUtils.getData(
               processInstance.getProcessDefinition(), dataID));

         if (null != dataValue)
         {
            final IData data = dataValue.getData();
            ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(data, dataPath);
            AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(processInstance, null);
            body = String.valueOf(evaluator.evaluate(data, dataValue.getValue(), dataPath, evaluationContext));
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Failed to retrieve data to compose notification message ("
                           + processInstance + ", dataID='" + dataID + "')");
            }
         }
      }
      else
      {
         // todo/france perform template expansion
         body = (String)attributes.get(PredefinedConstants.MAIL_ACTION_BODY_TEMPLATE_ATT);
      }

      if (StringUtils.isEmpty(body))
      {
         body = new StringBuffer()
               .append("Dear CARNOT User,\n\n")
               .append("The activity instance ").append(activityInstance)
               .append(" of process ").append(processInstance).append(" was notified at ")
               .append(DateFormat.getInstance().format(new Date(currentTimeStamp)))
               .append(" of the event ").append(event).append(".\n\n")
               .append("You receive this message as of no custom notification message ")
               .append("could be retrieved. Please contact your CARNOT Administrator.\n")
               .toString();
      }

      return body;
   }

   private void sendMail(Event event, String message, IProcessInstance processInstance)
   {
      String[] addresses = null;
      ReceiverType type = (ReceiverType)attributes.get(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT);
      if (type == ReceiverType.EMail)
      {
         addresses = prepareDirectAddress();
      }
      else if(type == ReceiverType.Participant)
      {
         addresses = prepareParticipantAddresses(processInstance);
      }
      else
      {
         trace.warn("Skipping 'Send Mail' action  for event '" + event
               + "' Unknown receiver type " + type +".");
         return;
      }

      sendMail(addresses, message, event);
   }

   private void sendMail(Event event, String message, IActivityInstance activityInstance)
   {
      String[] addresses = null;

      ReceiverType type = (ReceiverType)attributes.get(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT);

      if (type == ReceiverType.EMail)
      {
         addresses = prepareDirectAddress();
      }
      else if(type == ReceiverType.Participant)
      {
         addresses = prepareParticipantAddresses(activityInstance.getProcessInstance());
      }
      else if (type == ReceiverType.CurrentUserPerformer)
      {
         addresses = prepareCurrentPerformerAddress(activityInstance);
      }
      else
      {
         trace.warn("Skipping 'Send Mail' action  for event '" + event
               + "' Unknown receiver type " + type +".");
         return;
      }

      sendMail(addresses, message, event);
   }

   private String[] prepareDirectAddress()
   {
      return new String[] {(String)attributes.get(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT)};
   }

   private void sendMail(String[] addresses, String message, Event event)
   {
      if ((null != addresses) && (0 < addresses.length))
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Sending mail with message: '" + message + "'");
         }
         Object attSubject = attributes.get(PredefinedConstants.MAIL_ACTION_SUBJECT);
         String subject = attSubject == null
               ? "Infinity Process Platform Notification Mail"
               : (String) attSubject;
         MailHelper.sendSimpleMessage(addresses, subject, message);
      }
      else
      {
         trace.warn("Skipping 'Send Mail' action as no qualifying receivers can be "
               + "found (event=" + event + ").");
      }
   }

   private String[] prepareCurrentPerformerAddress(IActivityInstance activityInstance)
   {
      IUser user = activityInstance.getCurrentUserPerformer();
      if (user != null)
      {
         return new String[] { user.getEMail() };
      }
      return new String[0];
   }

   private String[] prepareParticipantAddresses(IProcessInstance processInstance)
   {
      IModel model = (IModel) processInstance.getProcessDefinition().getModel();

      if (null != model)
      {
         String participantId = (String)attributes.get(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT);
         IModelParticipant participant = model.findParticipant(participantId);
         if (participant != null)
         {
            Collection users = UserBean.findAllForParticipant(participant);
            if (participant instanceof IConditionalPerformer)
            {
               IParticipant performer = ((IConditionalPerformer) participant)
                     .retrievePerformer(processInstance);
               users = new ArrayList();
               if (performer instanceof IUserGroup)
               {
                  for (Iterator i = ((IUserGroup) performer).findAllUsers(); i.hasNext();)
                  {
                     IUser user = (IUser) i.next();
                     users.add(user);
                  }
               }
               else if (performer instanceof IUser)
               {
                  users.add(performer);
               }
               else if (performer instanceof IModelParticipant)
               {
                  users = UserBean.findAllForParticipant((IModelParticipant) performer);
               }
            }

            String[] addresses = new String[users.size()];

            int n = 0;
            for (Iterator i = users.iterator(); i.hasNext();)
            {
               addresses[n] = ((IUser) i.next()).getEMail();
               ++n;
            }
            return addresses;
         }
         else
         {
            trace.warn("Participant '" + participantId
                  + "' not found in model " + model.getModelOID());
         }
      }
      return null;
   }
}