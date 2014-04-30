/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.dto.ActivityDetails;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.HistoricalState;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceInfo;
import org.eclipse.stardust.engine.api.dto.RuntimeObjectDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.PermissionState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;

public class LazilyLoadingActivityInstanceDetails extends RuntimeObjectDetails implements ActivityInstance
{
   private static final long serialVersionUID = -7276968269040239704L;

   private static final String DATE_FORMAT = "yy/MM/dd HH:mm:ss";


   /** hold an instance of {@link IActivityInstance} for deferred calculation */
   private final IActivityInstance activityInstance;


   /** lazily created {@link IActivity} object */
   private IActivity activity;

   /** lazily created {@link Activity} object */
   private Activity activityDetails;

   /** lazily created {@link ActivityInstance} object to delegate to for complex requests */
   private ActivityInstance activityInstanceDetails;

   /** lazily created object */
   private String toStringInfo;

   public LazilyLoadingActivityInstanceDetails(final IActivityInstance activityInstance)
   {
      super(activityInstance, activityInstance.getActivity());

      this.activityInstance = activityInstance;

      /* do not create anything eagerly, but only as soon as it's requested */
   }

   @Override
   public Object getDescriptorValue(final String id)
   {
      return getActivityInstanceDetails().getDescriptorValue(id);
   }

   @Override
   public List<DataPath> getDescriptorDefinitions()
   {
      return getActivityInstanceDetails().getDescriptorDefinitions();
   }

   @Override
   public QualityAssuranceInfo getQualityAssuranceInfo()
   {
      return getActivityInstanceDetails().getQualityAssuranceInfo();
   }

   @Override
   public QualityAssuranceState getQualityAssuranceState()
   {
      return getActivityInstanceDetails().getQualityAssuranceState();
   }

   @Override
   public ActivityInstanceAttributes getAttributes()
   {
      return getActivityInstanceDetails().getAttributes();
   }

   @Override
   public ActivityInstanceState getState()
   {
      return activityInstance.getState();
   }

   @Override
   public Date getStartTime()
   {
      return activityInstance.getStartTime();
   }

   @Override
   public Date getLastModificationTime()
   {
      return activityInstance.getLastModificationTime();
   }

   @Override
   public Activity getActivity()
   {
      return getActivityDetails();
   }

   @Override
   public String getProcessDefinitionId()
   {
      return getIActivity().getProcessDefinition().getId();
   }

   @Override
   public long getProcessInstanceOID()
   {
      return activityInstance.getProcessInstanceOID();
   }

   @Override
   public ProcessInstance getProcessInstance()
   {
      return getActivityInstanceDetails().getProcessInstance();
   }

   @Override
   public long getUserPerformerOID()
   {
      return getActivityInstanceDetails().getUserPerformerOID();
   }

   @Override
   public String getUserPerformerName()
   {
      return getActivityInstanceDetails().getUserPerformerName();
   }

   @Override
   public User getUserPerformer()
   {
      return getActivityInstanceDetails().getUserPerformer();
   }

   @Override
   public String getParticipantPerformerID()
   {
      return getActivityInstanceDetails().getParticipantPerformerID();
   }

   @Override
   public String getParticipantPerformerName()
   {
      return getActivityInstanceDetails().getParticipantPerformerName();
   }

   @Override
   public boolean isAssignedToUser()
   {
      return getActivityInstanceDetails().isAssignedToUser();
   }

   @Override
   public boolean isAssignedToModelParticipant()
   {
      return getActivityInstanceDetails().isAssignedToModelParticipant();
   }

   @Override
   public boolean isAssignedToUserGroup()
   {
      return getActivityInstanceDetails().isAssignedToUserGroup();
   }

   @Override
   public ParticipantInfo getCurrentPerformer()
   {
      return getActivityInstanceDetails().getCurrentPerformer();
   }

   @Override
   public long getPerformedByOID()
   {
      return getActivityInstanceDetails().getPerformedByOID();
   }

   @Override
   public String getPerformedByName()
   {
      return getActivityInstanceDetails().getPerformedByName();
   }

   @Override
   public UserInfo getPerformedBy()
   {
      return getActivityInstanceDetails().getPerformedBy();
   }

   @Override
   public UserInfo getPerformedOnBehalfOf()
   {
      return getActivityInstanceDetails().getPerformedOnBehalfOf();
   }

   @Override
   public boolean isScopeProcessInstanceNoteAvailable()
   {
      return getActivityInstanceDetails().isScopeProcessInstanceNoteAvailable();
   }

   @Override
   public List<HistoricalState> getHistoricalStates()
   {
      return getActivityInstanceDetails().getHistoricalStates();
   }

   @Override
   public List<HistoricalEvent> getHistoricalEvents()
   {
      return getActivityInstanceDetails().getHistoricalEvents();
   }

   @Override
   public PermissionState getPermission(final String permissionId)
   {
      return getActivityInstanceDetails().getPermission(permissionId);
   }

   @Override
   public double getCriticality()
   {
      return activityInstance.getCriticality();
   }

   @Override
   public String toString()
   {
      return getToStringInfo();
   }

   private IActivity getIActivity()
   {
      if (activity == null)
      {
         activity = activityInstance.getActivity();
      }
      return activity;
   }

   private Activity getActivityDetails()
   {
      if (activityDetails == null)
      {
         if (getIActivity().getPerformer() instanceof IConditionalPerformer)
         {
            activityDetails = new ActivityDetails(getIActivity(), activityInstance);
         }
         else
         {
            activityDetails = DetailsFactory.create(getIActivity(), IActivity.class, ActivityDetails.class);
         }
      }
      return activityDetails;
   }

   private ActivityInstance getActivityInstanceDetails()
   {
      if (activityInstanceDetails == null)
      {
         activityInstanceDetails = new ActivityInstanceDetails(activityInstance);
      }
      return activityInstanceDetails;
   }

   private String getToStringInfo()
   {
      if (toStringInfo == null)
      {
         final StringBuffer sb = new StringBuffer();
         sb.append(getIActivity().getProcessDefinition().getName());
         sb.append(": ");
         sb.append(getActivity().getName());
         sb.append(" (");
         sb.append(new SimpleDateFormat(DATE_FORMAT).format(getStartTime()));
         sb.append(") ");
         toStringInfo = sb.toString();
      }
      return toStringInfo;
   }
}
