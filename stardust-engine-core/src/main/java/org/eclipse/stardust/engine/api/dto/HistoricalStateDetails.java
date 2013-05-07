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
package org.eclipse.stardust.engine.api.dto;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;


/**
 * A client side view of an activity instance historic state.
 * <p/>
 * Client side views of CARNOT model and runtime objects are exposed to a client as
 * read-only detail objects which contain a copy of the state of the corresponding server
 * object.
 * <p/>
 * 
 * @author born
 * @version $Revision$
 */
public class HistoricalStateDetails implements HistoricalState
{
   private static final long serialVersionUID = 1L;
   
   private final String activityId;
   private final long activityInstanceOid;
   private final String processDefinitionId;
   private final long processInstanceOid;
   private ActivityInstanceState state;
   private final Date from;
   private final Date until;
   private final Participant perfomer;
   private final Participant onBehalfOf;
   private final ParticipantInfo participant;
   private final ParticipantInfo onBehalfOfParticipant;
   private final UserInfo onBehalfOfUser;   
   private final User user;
   
   public String toString()
   {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
      StringBuilder builder = new StringBuilder();
      builder.append('[');
      builder.append(activityInstanceOid);
      builder.append(']');
      builder.append(state);
      builder.append('(');
      builder.append(format.format(from));
      builder.append('-');
      builder.append(until.getTime() == 0 ? "now" : format.format(until));
      builder.append(')');
      builder.append(participant == null ? "none" : participant);
      builder.append('(');
      builder.append(user == null ? "none" : user.getAccount());
      if (onBehalfOfUser != null)
      {
         builder.append(", on behalf of ");
         builder.append(onBehalfOfUser.getId());
      }      
      builder.append(')');
      return builder.toString();
   }

   public HistoricalStateDetails(ActivityInstanceHistoryBean historicalState)
   {
      super();
      this.activityId = historicalState.getActivityInstance().getActivity().getId();
      this.activityInstanceOid = historicalState.getActivityInstance().getOID();
      this.processDefinitionId = historicalState.getProcessInstance()
            .getProcessDefinition().getId();
      this.processInstanceOid = historicalState.getProcessInstance().getOID();
      this.state = historicalState.getState();
      this.from = historicalState.getFrom();
      this.until = historicalState.getUntil();

      PropertyLayer layer = null;
      try
      {
         Map props = new HashMap();
         props.put(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core);
         props.put(UserGroupDetailsLevel.PRP_DETAILS_LEVEL, UserGroupDetailsLevel.Core);

         layer = ParametersFacade.pushLayer(props);

         this.perfomer = getParticipant(historicalState.getPerformer(), historicalState
               .getDepartment());
         this.onBehalfOf = getParticipant(historicalState.getOnBehalfOf(),
               historicalState.getOnBehalfOfDepartment());
         
         this.participant = getParticipantInfo(historicalState.getPerformer(),
               historicalState.getDepartment());
         this.onBehalfOfParticipant = getParticipantInfo(historicalState.getOnBehalfOf(),
               historicalState.getOnBehalfOfDepartment());
         
         this.onBehalfOfUser = DetailsFactory.create(historicalState.getOnBehalfOfUser());

         this.user = DetailsFactory.createUser(historicalState.getUser());
      }
      finally
      {
         if (null != layer)
         {
            ParametersFacade.popLayer();
         }
      }
   }
   
   public String getActivityId()
   {
      return activityId;
   }

   public long getActivityInstanceOID()
   {
      return activityInstanceOid;
   }

   public long getProcessInstanceOID()
   {
      return processInstanceOid;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

   public Date getFrom()
   {
      return from;
   }

   public Date getUntil()
   {
      return until;
   }
   
   public Participant getPerfomer()
   {
      return perfomer;
   }
   
   public ParticipantInfo getParticipant()
   {
      return participant;
   }
   
   public Participant getOnBehalfOf()
   {
      return onBehalfOf;
   }
   
   public ParticipantInfo getOnBehalfOfParticipant()
   {
      return onBehalfOfParticipant;
   }
   
   public ActivityInstanceState getState()
   {
      return state;
   }

   public User getUser()
   {
      return user;
   }
   
   public UserInfo getOnBehalfOfUser()
   {
      return onBehalfOfUser;
   }      

   private static Participant getParticipant(IParticipant participant,
         IDepartment department)
   {
      if (department != null && participant instanceof IModelParticipant)
      {
         return DetailsFactory.createModelDetails((IModelParticipant) participant,
               department);
      }
      else
      {
         return DetailsFactory.createParticipantDetails(participant);
      }
   }

   private static ParticipantInfo getParticipantInfo(IParticipant participant,
         IDepartment department)
   {
      if (department != null && participant instanceof IModelParticipant)
      {
         return DetailsFactory.createModelInfoDetails((IModelParticipant) participant,
               department);
      }
      else
      {
         return DetailsFactory.createParticipantInfoDetails(participant);
      }
   }
}
