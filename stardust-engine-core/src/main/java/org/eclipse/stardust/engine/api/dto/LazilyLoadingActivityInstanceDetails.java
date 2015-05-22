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
package org.eclipse.stardust.engine.api.dto;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.PermissionState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;

public class LazilyLoadingActivityInstanceDetails extends RuntimeObjectDetails implements ActivityInstance
{
   private static final long serialVersionUID = -7276968269040239704L;

   private static final String AI_DETAILS_PROPERTIES_KEY = LazilyLoadingActivityInstanceDetails.class.getName();

   /** hold an instance of {@link IActivityInstance} for deferred calculation */
   private IActivityInstance activityInstance;

   /** hold the parameters relevant for deferred {@link ActivityInstanceDetails} creation */
   private final Map<String, Object> aiDetailsParameters;


   /** lazily created {@link IActivity} object */
   private IActivity activity;

   /** lazily created {@link Activity} object */
   private Activity activityDetails;

   /** lazily created {@link ActivityInstance} object to delegate to for complex requests */
   private ActivityInstance activityInstanceDetails;

   /** lazily created {@link ProcessInstance} object */
   private ProcessInstance processInstanceDetails;

   private boolean userPerformerInitialized = false;
   /** lazily created object */
   private User userPerformer;

   private boolean performerInitialized = false;
   /** lazily created object */
   private ParticipantInfo performer;

   private boolean performedByUserInitialized = false;
   /** lazily created object */
   private UserInfo performedByUser;


   /** lazily created object */
   private String toStringInfo;

   private boolean useFullBlownActivityDetailsObjects = false;
   private boolean useFullBlownAiDetailsObjects = false;

   public LazilyLoadingActivityInstanceDetails(final IActivityInstance activityInstance)
   {
      super(activityInstance, activityInstance.getActivity());

      this.activityInstance = activityInstance;

      Map<String, Object> tmpAiDetailsParameters = (Map<String, Object>) PropertyLayerProviderInterceptor.getCurrent().get(AI_DETAILS_PROPERTIES_KEY);
      if (tmpAiDetailsParameters == null)
      {
         tmpAiDetailsParameters = initAiDetailsParameters(Parameters.instance());
         PropertyLayerProviderInterceptor.getCurrent().setProperty(AI_DETAILS_PROPERTIES_KEY, tmpAiDetailsParameters);
      }
      aiDetailsParameters = tmpAiDetailsParameters;

      /* do not create anything eagerly, but only as soon as it's requested */
   }

   @Override
   public Object getDescriptorValue(final String id)
   {
      return getProcessInstanceDetails().getDescriptorValue(id);
   }

   @Override
   public List<DataPath> getDescriptorDefinitions()
   {
      return getProcessInstanceDetails().getDescriptorDefinitions();
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
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getState();
      }

      return activityInstance.getState();
   }

   @Override
   public Date getStartTime()
   {
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getStartTime();
      }

      return activityInstance.getStartTime();
   }

   @Override
   public Date getLastModificationTime()
   {
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getLastModificationTime();
      }

      return activityInstance.getLastModificationTime();
   }

   @Override
   public Activity getActivity()
   {
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getActivity();
      }

      return getActivityDetails();
   }

   @Override
   public String getProcessDefinitionId()
   {
      if (useFullBlownActivityDetailsObjects)
      {
         return getActivityDetails().getProcessDefinitionId();
      }
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getActivity().getProcessDefinitionId();
      }

      return getIActivity().getProcessDefinition().getId();
   }

   @Override
   public long getProcessInstanceOID()
   {
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getProcessInstanceOID();
      }

      return activityInstance.getProcessInstanceOID();
   }

   @Override
   public ProcessInstance getProcessInstance()
   {
      return getProcessInstanceDetails();
   }

   @Override
   public long getUserPerformerOID()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return (performer instanceof UserInfo) ? ((UserInfo) performer).getOID() : 0;
   }

   @Override
   public String getUserPerformerName()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return (performer instanceof UserInfo) ? performer.getName() : null;
   }

   @Override
   public User getUserPerformer()
   {
      if ( !userPerformerInitialized)
      {
         userPerformer = initUserPerformer();
      }

      return userPerformer;
   }

   @Override
   public String getParticipantPerformerID()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return (performer == null || performer instanceof UserInfo) ? null : performer.getId();
   }

   @Override
   public String getParticipantPerformerName()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return (performer == null || performer instanceof UserInfo) ? null : performer.getName();
   }

   @Override
   public boolean isAssignedToUser()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return performer instanceof UserInfo;
   }

   @Override
   public boolean isAssignedToModelParticipant()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return performer instanceof ModelParticipantInfo;
   }

   @Override
   public boolean isAssignedToUserGroup()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return performer instanceof UserGroupInfo;
   }

   @Override
   public ParticipantInfo getCurrentPerformer()
   {
      if ( !performerInitialized)
      {
         performer = initPerformer();
      }

      return performer;
   }

   @Override
   public long getPerformedByOID()
   {
      if ( !performedByUserInitialized)
      {
         performedByUser = initPerformedByUser();
      }

      return (performedByUser == null) ? 0 : performedByUser.getOID();
   }

   @Override
   public String getPerformedByName()
   {
      if ( !performedByUserInitialized)
      {
         performedByUser = initPerformedByUser();
      }

      return (performedByUser == null) ? null : performedByUser.getName();
   }

   @Override
   public UserInfo getPerformedBy()
   {
      if ( !performedByUserInitialized)
      {
         performedByUser = initPerformedByUser();
      }

      return performedByUser;
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
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getCriticality();
      }

      return activityInstance.getCriticality();
   }

   @Override
   public String toString()
   {
      return getToStringInfo();
   }

   private Map<String, Object> initAiDetailsParameters(final Parameters params)
   {
      final Map<String, Object> result = newHashMap();

      final Object userDetailsLevel = params.get(UserDetailsLevel.PRP_USER_DETAILS_LEVEL);
      if (userDetailsLevel != null)
      {
         result.put(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, userDetailsLevel);
      }

      final Object provideHistStates = params.get(HistoricalStatesPolicy.PRP_PROPVIDE_HIST_STATES);
      if (provideHistStates != null)
      {
         result.put(HistoricalStatesPolicy.PRP_PROPVIDE_HIST_STATES, provideHistStates);
      }

      final Object provideDescriptors = params.get(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS);
      if (provideDescriptors != null)
      {
         result.put(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, provideDescriptors);
      }

      final Object piDetailsLevel = params.get(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL);
      if (piDetailsLevel != null)
      {
         result.put(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL, piDetailsLevel);
      }

      final Object piDetailsOptions = params.get(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS);
      if (piDetailsOptions != null)
      {
         result.put(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS, piDetailsOptions);
      }

      final Object provideEventTypes = params.get(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES);
      if (provideEventTypes != null)
      {
         result.put(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES, provideEventTypes);
      }

      return result;
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

         /* initialize stuff that depends on the fields to be nulled out */
         getToStringInfo();

         activity = null;
         useFullBlownActivityDetailsObjects = true;
      }
      return activityDetails;
   }

   private ActivityInstance getActivityInstanceDetails()
   {
      if (activityInstanceDetails == null)
      {
         ParametersFacade.pushLayer(aiDetailsParameters);
         try
         {
            activityInstanceDetails = new ActivityInstanceDetails(activityInstance);
         }
         finally
         {
            ParametersFacade.popLayer();
         }

         /* initialize stuff that depends on the fields to be nulled out */
         getProcessInstanceDetails();
         getToStringInfo();

         activity = null;
         activityInstance = null;
         useFullBlownAiDetailsObjects = true;
      }
      return activityInstanceDetails;
   }

   private ProcessInstance getProcessInstanceDetails()
   {
      if (processInstanceDetails == null)
      {
         ParametersFacade.pushLayer(aiDetailsParameters);
         try
         {
            processInstanceDetails = new LazilyLoadingProcessInstanceDetails(activityInstance.getProcessInstance());
         }
         finally
         {
            ParametersFacade.popLayer();
         }
      }
      return processInstanceDetails;
   }

   private User initUserPerformer()
   {
      final User result;

      ParametersFacade.pushLayer(aiDetailsParameters);
      try
      {
         result = ActivityInstanceDetails.initUserPerformer(activityInstance, Parameters.instance());
         userPerformerInitialized = true;
      }
      finally
      {
         ParametersFacade.popLayer();
      }

      return result;
   }

   private ParticipantInfo initPerformer()
   {
      final ParticipantInfo result;

      ParametersFacade.pushLayer(aiDetailsParameters);
      try
      {
         result = ActivityInstanceDetails.initPerformer(activityInstance);
         performerInitialized = true;
      }
      finally
      {
         ParametersFacade.popLayer();
      }

      return result;
   }

   private UserInfo initPerformedByUser()
   {
      final UserInfo result;

      ParametersFacade.pushLayer(aiDetailsParameters);
      try
      {
         result = ActivityInstanceDetails.initPerformedByUser(activityInstance);
         performedByUserInitialized = true;
      }
      finally
      {
         ParametersFacade.popLayer();
      }

      return result;
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
         sb.append(new SimpleDateFormat(ActivityInstanceDetails.DATE_FORMAT).format(getStartTime()));
         sb.append(") ");
         toStringInfo = sb.toString();
      }
      return toStringInfo;
   }

   /* prevent objects of this class from being serialized: they can only used on the server-side */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      throw new UnsupportedOperationException();
   }
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getBenchmarkValue()
   {
      if (useFullBlownAiDetailsObjects)
      {
         return getActivityInstanceDetails().getBenchmarkValue();
      }
      return activityInstance.getBenchmarkValue();
   }
}
