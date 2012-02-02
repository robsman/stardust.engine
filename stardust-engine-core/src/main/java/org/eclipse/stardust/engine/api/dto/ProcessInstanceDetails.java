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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataPath;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.LogType;
import org.eclipse.stardust.engine.api.runtime.PermissionState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLink;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ILogEntry;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstanceLink;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.LogEntryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceGroupUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceHierarchyBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceLinkBean;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.engine.core.runtime.utils.PerformerUtils;


/**
 * <p/>
 * Many methods of the CARNOT EJBs return detail objects. Detail objects are
 * serializable helper objects passed by value to the client. They can, for
 * instance, pass the necessary information from the audit trail to the
 * embedding application in a dynamic way to guarantee an optimum of
 * performance.
 * </p>
 * <p/>
 * Instances of the class ProcessInstanceDetails are always referring to
 * runtime objects - process instances - and contain data of these runtime
 * objects (e.g. their OID, ID or start timestamp).
 * </p>
 *
 * @version $Revision$
 */
public class ProcessInstanceDetails extends RuntimeObjectDetails
      implements ProcessInstance, IDescriptorProvider
{
   private static final long serialVersionUID = 2L;

   public static final String PRP_PI_DETAILS_OPTIONS = "PROCESS_INSTANCE_DETAILS_OPTIONS";
   private final static String DATE_FORMAT = "yy/MM/dd HH:mm:ss";

   private static final Logger trace = LogManager.getLogger(ProcessInstanceDetails.class);
   private ProcessInstanceDetailsLevel detailsLevel;
   private EnumSet<ProcessInstanceDetailsOptions> detailsOptions;

   private final String processName;
   private final long rootProcessOID;
   private final long scopeProcessOID;
   private final int priority;
   private final User startingUserDetails;
   private final ProcessInstanceState state;
   private final Date startingTime;
   private final Date terminationTime;

   private ProcessInstance scopeprocessInstance = null;

   private long startingActivityInstanceOid;
   private long parentProcessInstanceOid;

   private Map<String, Object> descriptors = null;
   private Map<String, PermissionState> permissions;

   private ProcessInstanceAttributes attributes;

   private final List<HistoricalEvent> historicalEvents = CollectionUtils.newList();

   private List<ProcessInstanceLink> linkedProcessInstances = Collections.emptyList();

   private List<DataPath> descriptorDefinitions = CollectionUtils.newList();

   private boolean caseProcessInstance;

   ProcessInstanceDetails(IProcessInstance processInstance)
   {
      super(processInstance, processInstance.getProcessDefinition());

      Parameters parameters = Parameters.instance();
      detailsLevel = parameters.getObject(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL, ProcessInstanceDetailsLevel.Default);
      detailsOptions = parameters.getObject(PRP_PI_DETAILS_OPTIONS, EnumSet.noneOf(ProcessInstanceDetailsOptions.class));

      this.processName = processInstance.getProcessDefinition().getName();

      //<fix>
      long rootPIOID = UNKNOWN_OID;
      try
      {
         rootPIOID = processInstance.getRootProcessInstanceOID();
      }
      catch (Exception e)
      {
      }
      this.rootProcessOID = rootPIOID;
      //</fix>

      //<fix>
      long scopePIOID = UNKNOWN_OID;
      try
      {
         scopePIOID = processInstance.getScopeProcessInstanceOID();
      }
      catch (Exception e)
      {
      }
      this.scopeProcessOID = scopePIOID;
      //</fix>

      initializeScopePi(processInstance, parameters);

      this.priority = processInstance.getPriority();
      this.caseProcessInstance = processInstance.isCaseProcessInstance();
      this.startingTime = processInstance.getStartTime();
      this.terminationTime = processInstance.getTerminationTime();
      this.state = processInstance.getState();

      IUser startingUser = processInstance.getStartingUser();

      if (null != startingUser)
      {
         startingUserDetails = (UserDetails) DetailsFactory.create(startingUser,
               IUser.class, UserDetails.class);
      }
      else
      {
         this.startingUserDetails = null;
      }

      // get the starting AI oid
      startingActivityInstanceOid = UNKNOWN_OID;
      try
      {
         startingActivityInstanceOid = processInstance.getStartingActivityInstanceOID();
      }
      catch (Exception e)
      {
      }

      // get the PI of the starting AI
      parentProcessInstanceOid = UNKNOWN_OID;
      if (startingActivityInstanceOid > 0)
      {
         if (detailsOptions.contains(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO))
         {
            try
            {
               IActivityInstance ai = processInstance.getStartingActivityInstance();
               if (ai != null)
               {
                  this.parentProcessInstanceOid = ai.getProcessInstanceOID();
               }
            }
            catch (Exception x)
            {
               trace.warn("Could not load process instance for starting AI with oid "
                     + startingActivityInstanceOid, x);
            }
         }
      }
      else
      {
         if (detailsOptions.contains(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO))
         {
            try
            {
               IProcessInstance parentProcessInstance = ProcessInstanceHierarchyBean.findParentForSubProcessInstanceOid(getOID());
               if (parentProcessInstance != null)
               {
                  this.parentProcessInstanceOid = parentProcessInstance.getOID();
               }
            }
            catch (Exception x)
            {
               trace.warn("Could not load parent process instance.", x);
            }
         }
      }

      if (detailsOptions.contains(ProcessInstanceDetailsOptions.WITH_LINK_INFO))
      {
         ResultIterator<IProcessInstanceLink> beans = ProcessInstanceLinkBean.findAllForProcessInstance(processInstance);
         try
         {
            if (beans.hasNext())
            {
               linkedProcessInstances = CollectionUtils.newList();
               while (beans.hasNext())
               {
                  linkedProcessInstances.add(DetailsFactory.create(beans.next()));
               }
            }
         }
         finally
         {
            beans.close();
         }
      }

      boolean isCase = processInstance.isCaseProcessInstance();
      if (isCase || parameters.getBoolean(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, false))
      {
         loadDescriptors(processInstance);
         // add dynamic descriptors for process instance groups
         if (isCase)
         {
            addGroupDescriptors(processInstance);
         }
      }

      permissions = CollectionUtils.newHashMap();
      AuthorizationContext ctx = AuthorizationContext.create(AdministrationService.class, "abortProcessInstance", long.class);
      ctx.setProcessInstance(processInstance);
      PermissionState ps = Authorization2.hasPermission(ctx) ? PermissionState.Granted : PermissionState.Denied;
      permissions.put(ctx.getPermissionId(), ps);

      ctx = AuthorizationContext.create(AdministrationService.class, "setProcessInstancePriority", long.class, int.class);
      ctx.setProcessInstance(processInstance);
      ps = Authorization2.hasPermission(ctx) ? PermissionState.Granted : PermissionState.Denied;
      permissions.put(ctx.getPermissionId(), ps);
      
      if (isCase)
      {
         ctx = AuthorizationContext.create(WorkflowService.class, "joinCase", long.class, long[].class);
         ctx.setProcessInstance(processInstance);
         ps = Authorization2.hasPermission(ctx) ? PermissionState.Granted : PermissionState.Denied;
         permissions.put(ctx.getPermissionId(), ps);
      }

      PropertyLayer layer = null;
      try
      {
         // Do not overwrite level if explicitly set (not null!).
         if (parameters.get(UserDetailsLevel.PRP_USER_DETAILS_LEVEL) == null)
         {
            Map<String, ?> props = Collections.singletonMap(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core);
            layer = ParametersFacade.pushLayer(props);
         }

         initHistoricalEvents(parameters, processInstance);
      }
      finally
      {
         if (null != layer)
         {
            ParametersFacade.popLayer();
         }
      }
   }

   public String getProcessID()
   {
      return getModelElementID();
   }

   public String getProcessName()
   {
      return processName;
   }

   public long getRootProcessInstanceOID()
   {
      return rootProcessOID;
   }

   public long getScopeProcessInstanceOID()
   {
      return scopeProcessOID;
   }

   public ProcessInstance getScopeProcessInstance()
   {
      return scopeprocessInstance;
   }

   public int getPriority()
   {
      return priority;
   }

   public Date getStartTime()
   {
      return startingTime;
   }

   public Date getTerminationTime()
   {
      return terminationTime;
   }

   public User getStartingUser()
   {
      return startingUserDetails;
   }

   public ProcessInstanceState getState()
   {
      return state;
   }

   public long getStartingActivityInstanceOID()
   {
      return startingActivityInstanceOid;
   }

   public long getParentProcessInstanceOid()
   {
      return parentProcessInstanceOid;
   }

   public Map<String, Object> getDescriptors()
   {
      return (null != descriptors) ? descriptors : Collections.<String, Object>emptyMap();
   }

   public Object getDescriptorValue(String id)
   {
      return (null != descriptors) ? descriptors.get(id) : null;
   }

   public List<DataPath> getDescriptorDefinitions()
   {
      return descriptorDefinitions;
   }

   private void addGroupDescriptors(IProcessInstance processInstance)
   {
      // load from struct data.
      Map<String, Object> primitiveDescriptors = ProcessInstanceGroupUtils.getPrimitiveDescriptors(processInstance, null);
      this.descriptors.putAll(primitiveDescriptors);

      // create descriptor definitions
      this.descriptorDefinitions.addAll(ProcessInstanceGroupUtils.getDescriptorDefinitions(processInstance));
   }
   
   public void loadDescriptors(IProcessInstance processInstance)
   {
      final IProcessDefinition processDefinition = processInstance.getProcessDefinition();
      ModelElementList dataPaths = processDefinition.getDataPaths();

      // prefetch data values in batch to improve performance
      List<IData> dataItems = new ArrayList<IData>(dataPaths.size());

      Parameters parameters = Parameters.instance();
      Set<String> descriptorIds = (Set<String>) parameters.get(IDescriptorProvider.PRP_DESCRIPTOR_IDS);
      boolean limitDescriptors = descriptorIds != null && !descriptorIds.isEmpty();

      for (int i = 0; i < dataPaths.size(); ++i)
      {
         IDataPath dataPath = (IDataPath) dataPaths.get(i);
         boolean predefined = dataPath.getData().isPredefined();
         if (dataPath.isDescriptor() && !predefined)
         {
            if ( !limitDescriptors || descriptorIds.contains(dataPath.getId()))
            {
               dataItems.add(dataPath.getData());
            }
         }
      }
      processInstance.preloadDataValues(dataItems);

      descriptorDefinitions.clear();
      for (int i = 0; i < dataPaths.size(); ++i)
      {
         IDataPath dataPath = (IDataPath) dataPaths.get(i);

         if (dataPath.isDescriptor())
         {
            if (null == descriptors)
            {
               descriptors = CollectionUtils.newMap();
            }

            try
            {
               if ( !limitDescriptors || descriptorIds.contains(dataPath.getId()))
               {
                     this.descriptors.put(
                           dataPath.getId(),
                           processInstance.getInDataValue(dataPath.getData(),
                                 dataPath.getAccessPath()));

                     descriptorDefinitions.add(new DataPathDetails(dataPath));
               }
            }
            catch (Exception x)
            {
               trace.warn("Couldn't evaluate descriptor '" + dataPath.getId() + "'.", x);
               this.descriptors.put(dataPath.getId(), null);
            }
         }
      }
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
      sb.append(processName);
      sb.append(" (");
      sb.append(new SimpleDateFormat(DATE_FORMAT).format(getStartTime()));
      sb.append(")");
      return sb.toString();
   }

   public ProcessInstanceDetailsLevel getDetailsLevel()
   {
      return detailsLevel;
   }

   public EnumSet<ProcessInstanceDetailsOptions> getDetailsOptions()
   {
      return detailsOptions.clone();
   }

   public ProcessInstanceAttributes getAttributes()
   {
      return attributes;
   }

   public List<HistoricalEvent> getHistoricalEvents()
   {
      return historicalEvents;
   }

   private void initializeScopePi(IProcessInstance processInstance, Parameters parameters)
   {
      if (getOID() == this.scopeProcessOID)
      {
         // self reference
         this.scopeprocessInstance = this;
      }
      else if (this.scopeProcessOID != UNKNOWN_OID)
      {
         PropertyLayer layer = null;
         try
         {
            Map<String, Object> props = CollectionUtils.newHashMap();

            // notes shall be retrieved for scopePI if requested
            props.put(
                  PRP_PI_DETAILS_OPTIONS,
                  requestedNotes(parameters)
                     ? EnumSet.of(ProcessInstanceDetailsOptions.WITH_NOTES)
                     : EnumSet.noneOf(ProcessInstanceDetailsOptions.class));
            // but the rest shall be initialized with Core level
            props.put(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL,
                  ProcessInstanceDetailsLevel.Core);
            props.put(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES, 0);
            props.put(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, false);
            props.put(HistoricalStatesPolicy.PRP_PROPVIDE_HIST_STATES,
                  HistoricalStatesPolicy.NO_HIST_STATES);
            layer = ParametersFacade.pushLayer(props);

            this.scopeprocessInstance = DetailsFactory.create(processInstance
                  .getScopeProcessInstance());
         }
         finally
         {
            if (null != layer)
            {
               ParametersFacade.popLayer();
            }
         }
      }
   }

   public List<ProcessInstanceLink> getLinkedProcessInstances()
   {
      return linkedProcessInstances;
   }

   public boolean isCaseProcessInstance()
   {
      return caseProcessInstance;
   }

   private boolean requestedNotes(Parameters parameters)
   {
      int eventTypes = parameters.getInteger(
            HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES, 0);

      boolean addEventNotes = isEventTypeSet(eventTypes, HistoricalEventType.NOTE);

      return ProcessInstanceDetailsLevel.Full == detailsLevel
            || ProcessInstanceDetailsLevel.WithProperties == detailsLevel
            || ProcessInstanceDetailsLevel.WithResolvedProperties == detailsLevel
            || detailsOptions.contains(ProcessInstanceDetailsOptions.WITH_NOTES)
            || addEventNotes;
   }

   private void initHistoricalEvents(Parameters parameters, IProcessInstance processInstance)
   {
      int eventTypes = parameters.getInteger(
            HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES, 0);

      if (requestedNotes(parameters))
      {
         ProcessInstanceAttributesDetails details = new ProcessInstanceAttributesDetails(
               this);
         if (processInstance
               .isPropertyAvailable(ProcessInstanceBean.PI_PROPERTY_FLAG_NOTE))
         {
            List<Note> piNotes = ProcessInstanceUtils.getNotes(processInstance, this);
            details.initNotes(piNotes);
         }

         attributes = details;
         if (isEventTypeSet(eventTypes, HistoricalEventType.NOTE))
         {
            List<Note> notes = details.getNotes();
            for (Note note : notes)
            {
               if (ContextKind.ProcessInstance.equals(note.getContextKind())
                     && note.getContextOid() == getOID())
               {
                  historicalEvents.add(new HistoricalEventDetails(note));
               }
            }
         }
      }

      if (isEventTypeSet(eventTypes, HistoricalEventType.EXCEPTION))
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         Iterator lIter = null;
         if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
         {
            lIter = ((org.eclipse.stardust.engine.core.persistence.jdbc.Session) session).getCache(LogEntryBean.class)
                  .iterator();
         }
         while (lIter != null && lIter.hasNext())
         {
            PersistenceController pc = (PersistenceController) lIter.next();
            ILogEntry logEntry = (ILogEntry) pc.getPersistent();
            LogType logtype = LogType.getKey(logEntry.getType());
            if (logEntry.getProcessInstanceOID() == getOID()
                  && !LogType.Debug.equals(logtype) && !LogType.Info.equals(logtype))
            {
               historicalEvents.add(new HistoricalEventDetails(logEntry));
            }
         }
      }
   }

   private static boolean isEventTypeSet(int eventTypes, int eventType)
   {
      return (eventTypes & eventType) == eventType;
   }

   public PermissionState getPermission(String permissionId)
   {
      PermissionState ps = (PermissionState) permissions.get(permissionId);
      return ps == null ? PermissionState.Unknown : ps;
   }
}
