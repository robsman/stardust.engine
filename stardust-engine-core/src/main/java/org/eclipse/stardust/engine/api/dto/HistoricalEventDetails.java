/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventDescription;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ILogEntry;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;


public class HistoricalEventDetails implements HistoricalEvent
{
   private static final long serialVersionUID = 1L;

   final private Serializable details;
   final private Date eventTime;
   final private HistoricalEventType eventType;
   final private User user;

   public HistoricalEventDetails(HistoricalEventType eventType, Date eventTime,
         User user, Serializable details)
   {
      super();
      this.details = details;
      this.eventTime = eventTime;
      this.eventType = eventType;
      this.user = user;
   }

   public HistoricalEventDetails(ILogEntry logEntry)
   {
      this(HistoricalEventType.Exception, logEntry.getTimeStamp(), getUser(logEntry
            .getUserOID()), logEntry.getSubject());
   }

   public HistoricalEventDetails(Note note)
   {
      this(HistoricalEventType.Note, note.getTimestamp(), note.getUser(),
            note.getText());
   }

   public HistoricalEventDetails(HistoricalEventType eventType,
         HistoricalState histState, HistoricalState prevHistState)
   {

      this(eventType, histState.getFrom(), getUser(histState),
            createHistoricalEventDescription(eventType, prevHistState, histState));
   }

   public Serializable getDetails()
   {
      return details;
   }

   public Date getEventTime()
   {
      return eventTime;
   }

   public HistoricalEventType getEventType()
   {
      return eventType;
   }

   public User getUser()
   {
      return user;
   }

   public static User getUser(long userOid)
   {
      User userDetails = null;
      if (0 != userOid)
      {
         try
         {
            UserBean user = UserBean.findByOid(userOid);
            userDetails = (User) DetailsFactory.createParticipantDetails(user);
         }
         catch (ObjectNotFoundException x)
         {
            // left empty intentionally.
         }
      }

      return userDetails;
   }

   public static User getUser(HistoricalState histState)
   {
      final long userOid = (null == histState.getUser())
            ? 0
            : histState.getUser().getOID();

      return getUser(userOid);
   }

   private static HistoricalEventDescription createHistoricalEventDescription(
         HistoricalEventType eventType, HistoricalState prevHistState,
         HistoricalState histState)
   {
      if (HistoricalEventType.StateChange == eventType)
      {
         return new HistoricalEventDescriptionStateChangeDetails(
               prevHistState.getState(), histState.getState(), histState.getPerfomer());
      }
      else if (HistoricalEventType.Delegation == eventType)
      {
         return new HistoricalEventDescriptionDelegationDetails(prevHistState
               .getPerfomer(), histState.getPerfomer());
      }
      else
      {
         throw new InternalException(MessageFormat.format(
               "Event type {0} not allowed here.", new Object[] { eventType }));
      }
   }

}
