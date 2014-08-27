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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;

/**
 * A client side view of an activity instance.
 * <p/>
 * Client side views of CARNOT model and runtime objects are exposed to a client as
 * read-only detail objects which contain a copy of the state of the corresponding server
 * object.
 * <p/>
 * ActivityInstanceDetails contains information on an activityInstance and it's
 * corresponding activity and application.
 *
 * @author mgille
 * @version $Revision$
 * @see org.eclipse.stardust.engine.api.dto.UserDetails
 * @see org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails
 * @see org.eclipse.stardust.engine.api.dto.LogEntryDetails
 * @see org.eclipse.stardust.engine.api.dto.DataMappingDetails
 */
public class ActivityInstanceDetails extends RuntimeObjectDetails
      implements ActivityInstance
{
   private static final long serialVersionUID = 2L;

   static final String DATE_FORMAT = "yy/MM/dd HH:mm:ss";

   // this pattern does define a group which will contain a long value: the handler oid
   private static final Pattern handlerOidPattern = Pattern.compile("handlerOID *= *([0-9]+)");
   private static final Pattern handlerModelElementOidPattern = Pattern.compile("handlerModelElementOID *= *([0-9]+)");

   private ActivityInstanceState state;
   private Date startTime;
   private Date lastModificationTime;
   private String processDefinitionName;
   private String processDefinitionId;
   private long processInstanceOID;
   private ProcessInstance processInstance;
   private boolean scopeProcessInstanceNoteAvailable;
   private double criticality;

   private Activity activityDetails;

   private Map<String, PermissionState> permissions;

   private ParticipantInfo performer;
   private UserInfo performedBy;
   private UserInfo performedOnBehalfOf;
   private User userPerformer;

   private List<HistoricalState> historicalStates = Collections.emptyList();

   private final List<HistoricalEvent> historicalEvents = CollectionUtils.newList();

   private ActivityInstanceAttributes attributes;
   private QualityAssuranceState qualityAssuranceState = QualityAssuranceState.NO_QUALITY_ASSURANCE;
   private QualityAssuranceInfo qcInfo;

   public ActivityInstanceDetails(final IWorkItem workItem)
   {
      this(new WorkItemAdapter(workItem));
   }

   public ActivityInstanceDetails(IActivityInstance activityInstance)
   {
      super(activityInstance, activityInstance.getActivity());

      Parameters parameters = Parameters.instance();

      state = activityInstance.getState();

      startTime = activityInstance.getStartTime();
      lastModificationTime = activityInstance.getLastModificationTime();

      IActivity activity = activityInstance.getActivity();

      Assert.isNotNull(activity);

      // if the activity's default performer is conditional, details creation needs to be
      // done in the context of the activity instance to provide access to the data
      // identifying the default performer
      if (activity.getPerformer() instanceof IConditionalPerformer)
      {
         activityDetails = new ActivityDetails(activity, activityInstance);
      }
      else
      {
         activityDetails = (Activity) DetailsFactory.create(activity, IActivity.class, ActivityDetails.class);
      }

      IProcessDefinition processDefinition = activity.getProcessDefinition();

      processDefinitionName = processDefinition.getName();
      processDefinitionId = processDefinition.getId();
      IProcessInstance processInstance = activityInstance.getProcessInstance();
      processInstanceOID = processInstance.getOID();
      scopeProcessInstanceNoteAvailable = processInstance.getScopeProcessInstance()
            .isPropertyAvailable(ProcessInstanceBean.PI_PROPERTY_FLAG_NOTE);
      criticality = activityInstance.getCriticality();

      // participant means the performer as defined in the model
      performer = DetailsFactory.create(activityInstance.getCurrentUserPerformer());
      if (performer == null)
      {
         IParticipant currentPerformer = activityInstance.getCurrentPerformer();
         if (currentPerformer instanceof IModelParticipant)
         {
            long runtimeOid = ModelManagerFactory.getCurrent().getRuntimeOid((IModelParticipant) currentPerformer);
            String qualifiedId = ((IModelParticipant) currentPerformer).getQualifiedId();
            String name = currentPerformer.getName();
            boolean isDepartmentScoped = DepartmentUtils.getFirstScopedOrganization(
                  (IModelParticipant) currentPerformer) != null;
            boolean definesDepartmentScope = ((IModelParticipant) currentPerformer).getBooleanAttribute(
                  PredefinedConstants.BINDING_ATT);
            DepartmentInfo department = DetailsFactory.create(activityInstance.getCurrentDepartment());
            if (currentPerformer instanceof IOrganization)
            {
               performer = new OrganizationInfoDetails(runtimeOid, qualifiedId, name, isDepartmentScoped,
                     definesDepartmentScope, department);
            }
            else if (currentPerformer instanceof IRole)
            {
               performer = new RoleInfoDetails(runtimeOid, qualifiedId, name, isDepartmentScoped,
                     definesDepartmentScope, department);
            }
            else if (currentPerformer instanceof IConditionalPerformer)
            {
               performer = new ConditionalPerformerInfoDetails(runtimeOid, qualifiedId, name, department);
            }
         }
         else if (currentPerformer instanceof IUserGroup)
         {
            performer = DetailsFactory.create((IUserGroup) currentPerformer);
         }
      }

      performedBy = DetailsFactory.create(activityInstance.getPerformedBy());

      PropertyLayer layer = null;
      if (activityInstance.getCurrentUserPerformer() != null)
         try
         {
            // Do not overwrite level if explicitly set (not null!).
            if (parameters.get(UserDetailsLevel.PRP_USER_DETAILS_LEVEL) == null)
            {
               Map<String, ?> props = Collections.singletonMap(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core);
            layer = ParametersFacade.pushLayer(props);
            }

            userPerformer = DetailsFactory.createUser(activityInstance.getCurrentUserPerformer());
         }
         finally
         {
            if (null != layer)
            {
               ParametersFacade.popLayer();
            }
         }
      else
      {
         userPerformer = null;
      }

      HistoricalStatesPolicy historicalStatesPolicy = parameters.getObject(
            HistoricalStatesPolicy.PRP_PROPVIDE_HIST_STATES,
            HistoricalStatesPolicy.NO_HIST_STATES);
      switch (historicalStatesPolicy)
      {
      case WITH_HIST_STATES:
         Iterator<ActivityInstanceHistoryBean> histStatesIterator = ActivityInstanceHistoryBean
               .getAllForActivityInstance(activityInstance, false);
         if (histStatesIterator.hasNext())
         {
            List<HistoricalStateDetails> states = DetailsFactory.createCollection(
                  histStatesIterator, ActivityInstanceHistoryBean.class,
                  HistoricalStateDetails.class);
            if (activityInstance.isTerminated())
            {
               HistoricalStateDetails first = states.get(0);
               performedOnBehalfOf = first.getOnBehalfOfUser();
               if (performedOnBehalfOf != null)
               {
                  states.remove(0);
               }
            }
            historicalStates = Collections.<HistoricalState> unmodifiableList(states);
         }
         break;
      case WITH_LAST_HIST_STATE:
         Pair<ActivityInstanceHistoryBean, IUser> pair = ActivityInstanceHistoryBean
               .getLastForActivityInstance(activityInstance);
         performedOnBehalfOf = DetailsFactory.create(pair.getSecond());
         ActivityInstanceHistoryBean last = pair.getFirst();
         if (last != null)
         {
            historicalStates = Collections.<HistoricalState>singletonList(DetailsFactory.create(
                  pair, ActivityInstanceHistoryBean.class, HistoricalStateDetails.class));
         }
         break;
      case WITH_LAST_USER_PERFORMER:
         pair = ActivityInstanceHistoryBean
               .getLastUserPerformerForActivityInstance(activityInstance);
         performedOnBehalfOf = DetailsFactory.create(pair.getSecond());
         ActivityInstanceHistoryBean lastUserPerformer = pair.getFirst();
         if (lastUserPerformer != null)
         {
            historicalStates = Collections.<HistoricalState> singletonList(DetailsFactory.create(
                  lastUserPerformer, ActivityInstanceHistoryBean.class,
                  HistoricalStateDetails.class));
         }
         break;
      }

      permissions = CollectionUtils.newHashMap();
      PermissionState ps = PermissionState.Denied;
      AuthorizationContext ctx = AuthorizationContext.create(WorkflowService.class, "abortActivityInstance", long.class);
      if (!isTerminated())
      {
         ctx.setActivityInstance(activityInstance);
         if(Authorization2.hasPermission(ctx))
         {
            ps = PermissionState.Granted;
         }
      }
      permissions.put(ctx.getPermissionId(), ps);

      ps = PermissionState.Denied;
      ctx = AuthorizationContext.create(WorkflowService.class, "delegateToUser", long.class, long.class);
      if (activity.isInteractive())
      {
         ctx.setActivityInstance(activityInstance);
         if (!isTerminated() && Authorization2.hasPermission(ctx))
         {
            ps = PermissionState.Granted;
         }
      }
      permissions.put(ctx.getPermissionId(), ps);

      try
      {
         // Skip process authorization check here, since it was already checked for the activity instance
         // Do overwrite level explicitly. Otherwise we would end in an infinity loop AI / PI / AI / ...
         Map<String, Object> props = CollectionUtils.newMap();
         props.put(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL, ProcessInstanceDetailsLevel.Core);
         if (parameters.get(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS) == null)
         {
            props.put(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, true);
         }
         layer = ParametersFacade.pushLayer(props);
         this.processInstance = DetailsFactory.create(processInstance);
      }
      finally
      {
         if (null != layer)
         {
            ParametersFacade.popLayer();
         }
      }

      layer = null;
      try
      {
         // Do not overwrite level if explicitly set (not null!).
         if (parameters.get(UserDetailsLevel.PRP_USER_DETAILS_LEVEL) == null)
         {
            Map<String, ?> props = Collections.singletonMap(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core);
            layer = ParametersFacade.pushLayer(props);
         }

         initHistoricalEvents(parameters, activityInstance);
      }
      finally
      {
         if (null != layer)
         {
            ParametersFacade.popLayer();
         }
      }

      if (QualityAssuranceUtils.isQualityAssuranceEnabled(activityInstance))
      {
         qualityAssuranceState = activityInstance.getQualityAssuranceState();
         attributes = QualityAssuranceUtils.getActivityInstanceAttributes(activityInstance);
         //build info object regarding qa workflow
         if(qualityAssuranceState == QualityAssuranceState.IS_QUALITY_ASSURANCE
               || qualityAssuranceState == QualityAssuranceState.IS_REVISED)
         {
            qcInfo = QualityAssuranceUtils.getQualityAssuranceInfo(activityInstance);
         }
      }

      //if note for the process instance exists, - potentially notes for this activity instance exists
      //load and set these note
      final IProcessInstance scopeProcessInstance
         = activityInstance.getProcessInstance().getScopeProcessInstance();
      if(ProcessInstanceUtils.isLoadNotesEnabled()
            && ProcessInstanceUtils.hasNotes(scopeProcessInstance))
      {
         List<Note> aiNotes = ProcessInstanceUtils.getNotes(scopeProcessInstance, this);
         if(!aiNotes.isEmpty())
         {
            //create attributes object to present notes
            if(attributes == null)
            {
               attributes = new ActivityInstanceAttributesImpl(activityInstance.getOID());
            }

            attributes.setNotes(aiNotes);
         }
      }
   }

   private boolean isTerminated()
   {
      return (state == ActivityInstanceState.Aborted)
            || (state == ActivityInstanceState.Aborting)
            || (state == ActivityInstanceState.Completed);
   }

   /**
    * A human readable representation of basic aspects of this activity instance.
    * <p/>
    * It contains:
    * <ul>
    * <li>The process definition name
    * <li>The activity name
    * <li>The start time.
    * </ul>
    * <p/>
    * e.g. <code>Support Case Management: Create Case (02/09/20 10:10:10)</code>
    *
    * @return The stringified representation of this activity instance
    */
   public String toString()
   {
   StringBuffer sb = new StringBuffer();
   sb.append(processDefinitionName);
   sb.append(": ");
   sb.append(getActivity().getName());
   sb.append(" (");
   sb.append(new SimpleDateFormat(DATE_FORMAT).format(getStartTime()));
   sb.append(") ");

      return sb.toString();
   }

   public ActivityInstanceState getState()
   {
      return state;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public Date getLastModificationTime()
   {
      return lastModificationTime;
   }

   public Activity getActivity()
   {
      return activityDetails;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

   public long getProcessInstanceOID()
   {
      return processInstanceOID;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public long getUserPerformerOID()
   {
      return performer instanceof UserInfo ? ((UserInfo) performer).getOID() : 0;
   }

   public String getParticipantPerformerID()
   {
      return performer == null || performer instanceof UserInfo ? null : performer.getId();
   }

   public String getUserPerformerName()
   {
      return performer instanceof UserInfo ? performer.getName() : null;
   }

   public User getUserPerformer()
   {
      return userPerformer;
   }

   public long getParticipantPerformerOID()
   {
      if (performer instanceof UserGroupInfo)
      {
         return ((UserGroupInfo) performer).getOID();
      }
      if (performer instanceof ModelParticipantInfo)
      {
         return ((ModelParticipantInfo) performer).getRuntimeElementOID();
      }
      return 0;
   }

   public String getParticipantPerformerName()
   {
      return performer == null || performer instanceof UserInfo ? null : performer.getName();
   }

   public ParticipantInfo getCurrentPerformer()
   {
      return performer;
   }

   public boolean isAssignedToUser()
   {
      return performer instanceof UserInfo;
   }

   public boolean isAssignedToModelParticipant()
   {
      return performer instanceof ModelParticipantInfo;
   }

   public boolean isAssignedToUserGroup()
   {
      return performer instanceof UserGroupInfo;
   }

   public long getPerformedByOID()
   {
      return performedBy == null ? 0 : performedBy.getOID();
   }

   public String getPerformedByName()
   {
      return performedBy == null ? null : performedBy.getName();
   }

   public UserInfo getPerformedBy()
   {
      return performedBy;
   }

   public UserInfo getPerformedOnBehalfOf()
   {
      return performedOnBehalfOf;
   }

   public Object getDescriptorValue(String id)
   {
      return processInstance instanceof IDescriptorProvider ? ((IDescriptorProvider) processInstance).getDescriptorValue(id) : null;
   }

   public List<DataPath> getDescriptorDefinitions()
   {
      return processInstance instanceof IDescriptorProvider ? ((IDescriptorProvider) processInstance).getDescriptorDefinitions(): Collections.EMPTY_LIST;
   }

   public boolean equals(Object foreign)
   {
      if ((null == foreign) || !(foreign instanceof ActivityInstance))
      {
         return false;
      }

      if (getOID() == ((ActivityInstance) foreign).getOID())
      {
         return true;
      }
      return false;
   }

   public boolean isScopeProcessInstanceNoteAvailable()
   {
      return scopeProcessInstanceNoteAvailable;
   }

   public List<HistoricalState> getHistoricalStates()
   {
      return historicalStates;
   }

   public List<HistoricalEvent> getHistoricalEvents()
   {
      return historicalEvents;
   }

   private void initHistoricalEvents(Parameters parameters, IActivityInstance activityInstance)
   {
      int eventTypes = parameters.getInteger(
            HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES, 0);

      if (0 == eventTypes)
      {
         return;
      }

      if (isEventTypeSet(eventTypes, HistoricalEventType.STATE_CHANGE)
            || isEventTypeSet(eventTypes, HistoricalEventType.DELEGATION))
      {
         HistoricalStateDetails prevHistState = null;

         // Here the iteration is done in reverse order (oldest to newest historical state).
         for (ListIterator<HistoricalState> iter = historicalStates.listIterator(historicalStates.size());
               iter.hasPrevious();)
         {
            HistoricalStateDetails histState = (HistoricalStateDetails) iter.previous();

            // exists previous state? Only then a state change has occurred.
            if (null != prevHistState)
            {
               if (histState.getState() == prevHistState.getState())
               {
                  if (isEventTypeSet(eventTypes, HistoricalEventType.DELEGATION)
                        && isDelegationCandidate(prevHistState, histState))
                  {
                     historicalEvents.add(new HistoricalEventDetails(
                           HistoricalEventType.Delegation, histState, prevHistState));
                  }
               }
               else
               {
                  if (isEventTypeSet(eventTypes, HistoricalEventType.STATE_CHANGE))
                  {
                     if (activityInstance.isTerminated() && histState.getState() == state)
                     {
                        // handle termination elements differently - if available at all
                        // (there was change in behavior).
                        UserDetails performedByDetails = null;
                        IUser performedBy = activityInstance.getPerformedBy();
                        if (performedBy != null)
                        {
                           performedByDetails = (UserDetails) DetailsFactory.create(
                                 performedBy, IUser.class, UserDetails.class);
                        }

                        final Serializable descriptionDetails = new HistoricalEventDescriptionStateChangeDetails(
                              prevHistState.getState(), state, performedByDetails);
                        historicalEvents.add(new HistoricalEventDetails(
                              HistoricalEventType.StateChange, activityInstance
                                    .getLastModificationTime(), HistoricalEventDetails
                                    .getUser(histState), descriptionDetails));
                     }
                     else
                     {
                        historicalEvents.add(new HistoricalEventDetails(
                              HistoricalEventType.StateChange, histState, prevHistState));
                     }
                  }
               }
            }

            prevHistState = histState;
         }

         // add state change event for states not covered by history table,
         // e.g completed, terminated.
         if (null != prevHistState
               && prevHistState.getState() != state
               && activityInstance.isTerminated()
               && isEventTypeSet(eventTypes, HistoricalEventType.STATE_CHANGE))
         {
            UserDetails performedByDetails = null;
            IUser performedBy = activityInstance.getPerformedBy();
            if(performedBy != null)
            {
               performedByDetails = (UserDetails) DetailsFactory.create(performedBy,
                     IUser.class, UserDetails.class);
            }

            historicalEvents.add(new HistoricalEventDetails( //
                  HistoricalEventType.StateChange, //
                  activityInstance.getLastModificationTime(), //
                  performedByDetails, //
                  new HistoricalEventDescriptionStateChangeDetails(prevHistState
                        .getState(), state, performedByDetails)));
         }
      }

      if (isEventTypeSet(eventTypes, HistoricalEventType.EXCEPTION)
            || isEventTypeSet(eventTypes, HistoricalEventType.EVENT_EXECUTION))
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         Iterator lIter = null;

         // LogEntry has been already fetched in prefetch phase of query execution - take it from cache
         if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
         {
            lIter = ((org.eclipse.stardust.engine.core.persistence.jdbc.Session) session)
                  .getCache(LogEntryBean.class).iterator();
         }

         ModelElementList<IEventHandler> eventHandlers = null;
         while (lIter != null && lIter.hasNext())
         {
            PersistenceController pc = (PersistenceController) lIter.next();
            ILogEntry logEntry = (ILogEntry) pc.getPersistent();

            if (logEntry.getActivityInstanceOID() == getOID())
            {
               LogType logtype = LogType.getKey(logEntry.getType());
               LogCode logCode = LogCode.getKey(logEntry.getCode());

               if (isEventTypeSet(eventTypes, HistoricalEventType.EXCEPTION)
                     // exceptions are represented by log entries logged with WARN, ERROR or even higher level
                     && !LogType.Debug.equals(logtype) && !LogType.Info.equals(logtype))
               {
                  historicalEvents.add(new HistoricalEventDetails(logEntry));
               }

               if (isEventTypeSet(eventTypes, HistoricalEventType.EVENT_EXECUTION)
                     // event execution is currently logged at INFO level
                     && LogType.Info.equals(logtype) && LogCode.EVENT.equals(logCode))
               {
                  eventHandlers = addHistoricalEventExecution(activityInstance,
                        eventHandlers, logEntry);
               }
            }
         }
      }

      if (scopeProcessInstanceNoteAvailable
            && isEventTypeSet(eventTypes, HistoricalEventType.NOTE))
      {
         final IProcessInstance scopeProcessInstance = activityInstance
               .getProcessInstance().getScopeProcessInstance();
         List<Note> aiNotes = ProcessInstanceUtils.getNotes(scopeProcessInstance, this);
         for (Note note: aiNotes)
         {
            historicalEvents.add(new HistoricalEventDetails(note));
         }
      }

      if (historicalEvents.size() > 1)
      {
         Collections.sort(historicalEvents, new Comparator<HistoricalEvent>()
         {
            public int compare(HistoricalEvent event1, HistoricalEvent event2)
            {
               return event1.getEventTime().compareTo(event2.getEventTime());
            }
         });
      }
   }

   /**
    * @param activityInstance the AI for which event execution will be added
    * @param eventHandlers this will contain a list of event handlers for the current activity.
    *          If null it will be initialized in the method call, otherwise its content will be used.
    * @param logEntry the LogEntry which currently is inspected
    * @return
    */
   private ModelElementList<IEventHandler> addHistoricalEventExecution(
         IActivityInstance activityInstance, ModelElementList<IEventHandler> eventHandlers,
         ILogEntry logEntry)
   {
      // retrieve event handlers for current AI once in this loop
      if (eventHandlers == null)
      {
         eventHandlers = activityInstance.getActivity().getEventHandlers();
      }

      // try to fetch event handler by event handler runtime oid
      String subject = logEntry.getSubject();
      Matcher matcher = handlerOidPattern.matcher(subject);
      IEventHandler handler = null;
      if (matcher.find())
      {
         long eventHandlerRuntimeOid = Long.valueOf(matcher.group(1));
         if(eventHandlerRuntimeOid != Event.OID_UNDEFINED)
         {
            long modelOid = activityInstance.getActivity().getModel().getModelOID();
            handler = EventUtils.getEventHandler(modelOid, eventHandlerRuntimeOid);
         }
      }

      //try to fetch event handler by event handler model element oid
      if (handler == null)
      {
         matcher = handlerModelElementOidPattern.matcher(subject);
         if (matcher.find())
         {
            long eventHandlerModelElementOid = Long.valueOf(matcher.group(1));
            if (eventHandlerModelElementOid != Event.OID_UNDEFINED)
            {
               handler = EventUtils.getEventHandler(eventHandlers, eventHandlerModelElementOid);
            }
         }
      }

      if (handler != null)
      {
         EventHandler eventHandlerDetails = new EventHandlerDetails(handler);
         HistoricalEvent histEvent = new HistoricalEventDetails(
               HistoricalEventType.EventExecution, logEntry.getTimeStamp(),
               HistoricalEventDetails.getUser(logEntry.getUserOID()),
               eventHandlerDetails);
         historicalEvents.add(histEvent);
      }

      return eventHandlers;
   }

   private static boolean isEventTypeSet(int eventTypes, int eventType)
   {
      return (eventTypes & eventType) == eventType;
   }

   private static boolean isDelegationCandidate(HistoricalState prevHistState,
         HistoricalState histState)
   {
      if (histState.getState() == ActivityInstanceState.Suspended
            && histState.getParticipant() != null
            && histState.getParticipant() != null && prevHistState.getParticipant() != null)
      {

         if ( !(histState.getParticipant()).equals(prevHistState.getParticipant())
               || (((ModelParticipantInfo) histState.getParticipant()).getDepartment() != null)
               && (((ModelParticipantInfo) histState.getParticipant()).getDepartment() != null)
               && ( !((ModelParticipantInfo) histState.getParticipant()).getDepartment()
                     .equals(
                           ((ModelParticipantInfo) prevHistState.getParticipant()).getDepartment())))
         {
            return true;
         }
      }
      return false;
   }



   public PermissionState getPermission(String permissionId)
   {
      PermissionState ps = (PermissionState) permissions.get(permissionId);
      return ps == null ? PermissionState.Unknown : ps;
   }

   public double getCriticality()
   {
      return criticality;
   }

   public QualityAssuranceState getQualityAssuranceState()
   {
      return qualityAssuranceState;
   }

   public ActivityInstanceAttributes getAttributes()
   {
      return attributes;
   }

   public QualityAssuranceInfo getQualityAssuranceInfo()
   {
      return qcInfo;
   }
}