/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.*;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.DeployedModelDescriptionDetails;
import org.eclipse.stardust.engine.api.dto.EventHandlerBindingDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.PrefetchConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.compatibility.el.EvaluationError;
import org.eclipse.stardust.engine.core.compatibility.el.Interpreter;
import org.eclipse.stardust.engine.core.compatibility.el.Result;
import org.eclipse.stardust.engine.core.compatibility.el.SyntaxError;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.DefaultPersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.pojo.data.JavaAccessPoint;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ExecutionPlan;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterHelper;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterInstance;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Represents an instance of a process definition, instantiated to be
 * executed on the carnot workflow engine.
 *
 * @author mgille
 * @version $Revision$
 */
public class ProcessInstanceBean extends AttributedIdentifiablePersistentBean
      implements IProcessInstance
{
   private static final JavaAccessPoint JAVA_ENUM_ACCESS_POINT = new JavaAccessPoint("enum", "enum", Direction.IN_OUT);

   private static final long serialVersionUID = -9116897207696130951L;

   private static final Logger trace = LogManager.getLogger(ProcessInstanceBean.class);

   private static final int PREFETCH_BATCH_SIZE = 400;

   // TODO: property name "NOTE" is too simple.
   private static final String PI_NOTE = "NOTE";
   protected static final String ABORTING_PI_OID = "Infinity.RootProcessInstance.AbortingPiOid";
   public static final String ABORTING_USER_OID = "Infinity.RootProcessInstance.AbortingUserOid";
   public static final String PI_NOTE_CONTEXT_PREFIX_PATTERN = "<context kind=\"{0}\" oid=\"{1}\" />";

   private static final int PI_PROPERTY_FLAG_ANY = 1;          // first bit
   public static final int PI_PROPERTY_FLAG_NOTE = 2;          // second bit
   // PI_ABORTING useful for root process instances only
   public static final int PI_PROPERTY_FLAG_PI_ABORTING = 4;   // third bit
   private static final int PI_PROPERTY_FLAG_ALL = ~0;         // all bits

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__START_TIME = "startTime";
   public static final String FIELD__TERMINATION_TIME = "terminationTime";
   public static final String FIELD__STATE = "state";
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__PROCESS_DEFINITION = "processDefinition";
   public static final String FIELD__PRIORITY = "priority";
   public static final String FIELD__DEPLOYMENT = "deployment";
   public static final String FIELD__ROOT_PROCESS_INSTANCE = "rootProcessInstance";
   public static final String FIELD__SCOPE_PROCESS_INSTANCE = "scopeProcessInstance";
   public static final String FIELD__STARTING_USER = "startingUser";
   public static final String FIELD__STARTING_ACTIVITY_INSTANCE = "startingActivityInstance";
   /**
    * @deprecated This attribute will not be maintained starting with version 3.2.1.
    */
   public static final String FIELD__TOKEN_COUNT = "tokenCount";
   public static final String FIELD__PROPERTIES_AVAILABLE = "propertiesAvailable";

   public static final FieldRef FR__OID = new FieldRef(ProcessInstanceBean.class, FIELD__OID);
   public static final FieldRef FR__START_TIME = new FieldRef(ProcessInstanceBean.class, FIELD__START_TIME);
   public static final FieldRef FR__TERMINATION_TIME = new FieldRef(ProcessInstanceBean.class, FIELD__TERMINATION_TIME);
   public static final FieldRef FR__STATE = new FieldRef(ProcessInstanceBean.class, FIELD__STATE);
   public static final FieldRef FR__MODEL = new FieldRef(ProcessInstanceBean.class, FIELD__MODEL);
   public static final FieldRef FR__PROCESS_DEFINITION = new FieldRef(ProcessInstanceBean.class, FIELD__PROCESS_DEFINITION);
   public static final FieldRef FR__PRIORITY = new FieldRef(ProcessInstanceBean.class, FIELD__PRIORITY);
   public static final FieldRef FR__DEPLOYMENT = new FieldRef(ProcessInstanceBean.class, FIELD__DEPLOYMENT);
   public static final FieldRef FR__ROOT_PROCESS_INSTANCE = new FieldRef(ProcessInstanceBean.class, FIELD__ROOT_PROCESS_INSTANCE);
   public static final FieldRef FR__SCOPE_PROCESS_INSTANCE = new FieldRef(ProcessInstanceBean.class, FIELD__SCOPE_PROCESS_INSTANCE);
   public static final FieldRef FR__STARTING_USER = new FieldRef(ProcessInstanceBean.class, FIELD__STARTING_USER);
   public static final FieldRef FR__STARTING_ACTIVITY_INSTANCE = new FieldRef(ProcessInstanceBean.class, FIELD__STARTING_ACTIVITY_INSTANCE);
   /**
    * @deprecated This attribute will not be maintained starting with version 3.2.1.
    */
   public static final FieldRef FR__TOKEN_COUNT = new FieldRef(ProcessInstanceBean.class, FIELD__TOKEN_COUNT);
   public static final FieldRef FR__PROPERTIES_AVAILABLE = new FieldRef(ProcessInstanceBean.class, FIELD__PROPERTIES_AVAILABLE);

   public static final String TABLE_NAME = "process_instance";
   public static final String DEFAULT_ALIAS = "pi";
   public static final String LOCK_TABLE_NAME = "process_instance_lck";
   public static final String LOCK_INDEX_NAME = "proc_inst_lck_idx";
   private static final String PK_FIELD = FIELD__OID;
   private static final String PK_SEQUENCE = "process_instance_seq";
   public static final boolean TRY_DEFERRED_INSERT = true;
   public static final String[] proc_inst_idx1_UNIQUE_INDEX = new String[]{FIELD__OID};
   public static final String[] proc_inst_idx2_INDEX =
         new String[]{FIELD__STATE, FIELD__TERMINATION_TIME, FIELD__PROCESS_DEFINITION};
   public static final String[] proc_inst_idx3_INDEX =
         new String[]{FIELD__STATE, FIELD__START_TIME, FIELD__TERMINATION_TIME};
   public static final String[] proc_inst_idx4_INDEX =
         new String[]{FIELD__STARTING_ACTIVITY_INSTANCE};
   public static final String[] proc_inst_idx5_INDEX =
         new String[]{FIELD__ROOT_PROCESS_INSTANCE, FIELD__STATE, FIELD__OID};
   public static final String[] proc_inst_idx6_INDEX =
      new String[]{FIELD__SCOPE_PROCESS_INSTANCE, FIELD__STATE, FIELD__OID};

   static final boolean state_USE_LITERALS = true;
   static final boolean priority_USE_LITERALS = true;

   private long startTime;
   private long terminationTime;
   private int state = ProcessInstanceState.CREATED;
   private long model;
   private long processDefinition;
   private int priority;
   private long deployment;

   private ProcessInstanceBean rootProcessInstance;
   private static final String rootProcessInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String rootProcessInstance_MANDATORY = Boolean.TRUE.toString();

   private ProcessInstanceBean scopeProcessInstance;
   private static final String scopeProcessInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String scopeProcessInstance_MANDATORY = Boolean.TRUE.toString();

   private UserBean startingUser;
   private static final String startingUser_EAGER_FETCH = Boolean.TRUE.toString();
   private static final String startingUser_MANDATORY = Boolean.FALSE.toString();

   private ActivityInstanceBean startingActivityInstance;
   private static final String startingActivityInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String startingActivityInstance_MANDATORY = Boolean.FALSE.toString();

   private static final String AUDIT_TRAIL_PERSISTENCE_PROPERTY_KEY = "Infinity.Engine.AuditTrailPersistence";

   /**
    * @deprecated This attribute will not be maintained starting with version 3.2.1.
    */
   private long tokenCount;

   /**
    * Reflects the existence of any {@link ProcessInstanceProperty} for this process instance.
    */
   private int propertiesAvailable;

   private transient Map<String,IDataValue> dataValueCache;

   private transient Map<Long, IStructuredDataValue> structuredDataValueCache;

   private transient PropertyIndexHandler propIndexHandler = new PropertyIndexHandler();

   private transient AuditTrailPersistence previousAuditTrailPersistence = null;

   /**
    * Returns the process instance with the OID <tt>oid</tt>.
    */
   public static ProcessInstanceBean findByOID(long oid)
      throws ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PROCESS_INSTANCE_OID.raise(0), 0);
      }
      ProcessInstanceBean result = (ProcessInstanceBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findByOID(ProcessInstanceBean.class, oid);
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PROCESS_INSTANCE_OID.raise(oid), oid);
      }
      return result;
   }

   /**
    * Returns the process instance with the OID <tt>oid</tt>.
    */
   public static IProcessInstance findForStartingActivityInstance(long oid)
   {
      return (IProcessInstance) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(ProcessInstanceBean.class,
                  QueryExtension.where(Predicates.isEqual(FR__STARTING_ACTIVITY_INSTANCE, oid)));
   }

   public static ProcessInstanceBean createInstance(IProcessDefinition processDefinition,
         IUser user, Map<String, ? > data, boolean isSubprocess)
   {
      return createInstance(processDefinition, null, null, user, data, isSubprocess);
   }

   public static ProcessInstanceBean createInstance(IProcessDefinition processDefinition,
         IUser user, Map<String, ? > data)
   {
      return createInstance(processDefinition, null, null, user, data, false);
   }

   public static ProcessInstanceBean createInstance(IProcessDefinition processDefinition,
         IProcessInstance parentProcessInstance, IUser user, Map<String, ?> data)
   {
      return createInstance(processDefinition, null, parentProcessInstance, user, data, false);
   }

   public static ProcessInstanceBean createInstance(IProcessDefinition processDefinition,
         ActivityInstanceBean parentActivityInstance, IUser user, Map<String, ? > data, boolean isSubprocess)
   {
      return createInstance(processDefinition, parentActivityInstance, null, user, data, isSubprocess);
   }

   public static ProcessInstanceBean createUnboundInstance(IModel model)
   {
      ProcessInstanceBean processInstance = new ProcessInstanceBean(model);
      createHierarchyEntries(processInstance, null);
      IProcessInstanceScope processInstanceScope = createScopeEntry(processInstance, null, null);
      processInstance.setScopeProcessInstance((ProcessInstanceBean) processInstanceScope.getScopeProcessInstance());
      if (processInstance.equals(processInstanceScope.getScopeProcessInstance()))
      {
         createClusterInstance(processInstance);
      }
      return processInstance;
   }

   private static ProcessInstanceBean createInstance(IProcessDefinition processDefinition,
         ActivityInstanceBean parentActivityInstance, IProcessInstance spawnParentProcessInstance, IUser user, Map<String, ? > data, boolean isSubProcess)
   {
      IProcessInstance parentProcessInstance = spawnParentProcessInstance;
      if (parentActivityInstance != null)
      {
         parentProcessInstance = parentActivityInstance.getProcessInstance();
      }

      // lock to ensure consistent information from the parent.
      if (parentProcessInstance != null)
      {
         parentProcessInstance.lock();
      }

      ProcessInstanceBean processInstance = new ProcessInstanceBean(processDefinition);

      // if subprocess then inherit actual priority from the parent process
      // else set priority to the default value.
      int priority = processDefinition.getDefaultPriority();
      if (parentProcessInstance != null)
      {
         priority = parentProcessInstance.getPriority();
      }

      // at the moment the valid value range is -1, 0, and 1.
      // doing a integer signum to be sure that the range is OK.
      if (priority > 0)
      {
         priority = 1;
      }
      else if (priority < 0)
      {
         priority = -1;
      }
      processInstance.setPriority(priority);

      createHierarchyEntries(processInstance, parentProcessInstance);

      if (parentProcessInstance != null)
      {
         processInstance.setRootProcessInstance((ProcessInstanceBean) parentProcessInstance.getRootProcessInstance());
      }
      else
      {
         processInstance.setDeployment(ModelManagerFactory.getCurrent()
               .getLastDeployment());
      }
      if (parentActivityInstance != null)
      {
         processInstance.setStartingActivityInstance(parentActivityInstance);
      }

      IProcessInstanceScope processInstanceScope = createScopeEntry(processInstance,
            parentActivityInstance, parentProcessInstance);

      processInstance.setScopeProcessInstance((ProcessInstanceBean) processInstanceScope
            .getScopeProcessInstance());

      if (processInstance.equals(processInstanceScope.getScopeProcessInstance()))
      {
         createClusterInstance(processInstance);
      }

      if (null != user)
      {
         if (trace.isInfoEnabled())
         {
         trace.info("Setting starting user to '" + user.getOID()
               + "'  for process instance " + processInstance.getOID() + ".");
         }
         processInstance.setStartingUser(user);
      }

      if ((null != data) && !data.isEmpty())
      {
         for (Iterator< ? > iterator = data.entrySet().iterator(); iterator.hasNext(); )
         {
            Map.Entry<String, ? > entry = (Entry<String, ? >) iterator.next();

            String dataId = entry.getKey();
            IData idata = ((IModel) processDefinition.getModel()).findData(dataId);
            if (idata != null)
            {
               String path = "";
               Object value = entry.getValue();
               if (value instanceof DataFragmentValue)
               {
                  path = ((DataFragmentValue) value).path;
                  value = ((DataFragmentValue) value).value;
               }
               processInstance.setOutDataValue(idata, path, value);
            }
            else
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(dataId), dataId);
            }

            if (trace.isDebugEnabled())
            {
               trace.debug("Set data value '" + dataId
                     + "' / process instance " + processInstance.getOID() + ".");
            }
         }
      }

      processInstance.setState(ProcessInstanceState.ACTIVE);
      if(!isSubProcess)
      {
         processInstance.doBindAutomaticlyBoundEvents();
      }

      MonitoringUtils.processExecutionMonitors().processStarted(processInstance);

      return processInstance;
   }

   /**
    * Internal constructor for persistence layer.
    */
   public ProcessInstanceBean()
   {
   }

   private ProcessInstanceBean(IModel model)
   {
      Assert.isNotNull(model);

      this.model = model.getModelOID();
      this.processDefinition = -1;
      this.rootProcessInstance = this;
      this.startTime = TimestampProviderUtils.getTimeStamp().getTime();
      this.terminationTime = startTime;
      this.tokenCount = 0;
      this.state = ProcessInstanceState.COMPLETED;

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      session.cluster(this);
      setAuditTrailPersistencePropertyValue(AuditTrailPersistence.ENGINE_DEFAULT);
   }

   private ProcessInstanceBean(IProcessDefinition processDefinition)
   {
      Assert.isNotNull(processDefinition);

      this.model = processDefinition.getModel().getModelOID();
      this.processDefinition = ModelManagerFactory.getCurrent().getRuntimeOid(
            processDefinition);

      this.rootProcessInstance = this;

      this.startTime = TimestampProviderUtils.getTimeStamp().getTime();
      this.terminationTime = 0;
      this.tokenCount = 1;

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      session.cluster(this);

      setAuditTrailPersistencePropertyValue(determineAuditTrailPersistence(processDefinition));

      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      ExecutionPlan plan = rtEnv.getExecutionPlan();
      if (plan == null || plan.isTerminated() || !plan.hasStartActivity() || plan.hasMoreSteps2())
      {
         TransitionTokenBean.createStartToken(this);
      }
   }

   public String toString()
   {
      ModelBean model = (ModelBean) getProcessDefinition().getModel();

      return "Process instance = " + getOID() + " (" + getProcessDefinition() + ") "
            + ModelUtils.getExtendedVersionString(model);
   }

   public Date getStartTime()
   {
      fetch();

      return new Date(startTime);
   }

   public Date getTerminationTime()
   {
      fetch();

      return terminationTime == 0 ? null : new Date(terminationTime);
   }

   protected void setTerminationTime(Date terminationTime)
   {
      fetch();

      if (terminationTime.getTime() != this.terminationTime)
      {
         markModified(FIELD__TERMINATION_TIME);
         this.terminationTime = terminationTime.getTime();
      }
   }

   public ProcessInstanceState getState()
   {
      fetch();
      return ProcessInstanceState.getState(state);
   }

   public long getReferenceDeployment()
   {
      fetch();
      return deployment;
   }

   public void setState(int state)
   {
      fetch();
      if (state != this.state)
      {
         markModified(FIELD__STATE);

         ProcessInstanceState sourceState = ProcessInstanceState.getState(this.state);
         ProcessInstanceState targetState = ProcessInstanceState.getState(state);

         if (getProcessDefinition().hasEventHandlers(
               PredefinedConstants.PROCESS_STATECHANGE_CONDITION))
         {
            Event event = new Event(Event.PROCESS_INSTANCE, getOID(), Event.OID_UNDEFINED, Event.OID_UNDEFINED,
                  Event.ENGINE_EVENT);
            event.setAttribute(PredefinedConstants.SOURCE_STATE_ATT, sourceState);
            event.setAttribute(PredefinedConstants.TARGET_STATE_ATT, targetState);

            EventUtils.processAutomaticEvent(getProcessDefinition(),
                  PredefinedConstants.PROCESS_STATECHANGE_CONDITION, event);
         }

         if (trace.isInfoEnabled())
         {
            trace.info("State change for " + this + ": " + sourceState + "-->"
                  + targetState + ".");
         }

         this.state = state;

         if (state == ProcessInstanceState.ABORTED)
         {
            MonitoringUtils.processExecutionMonitors().processAborted(this);
         }
         if (state == ProcessInstanceState.COMPLETED)
         {
            MonitoringUtils.processExecutionMonitors().processCompleted(this);
         }
      }

      IProcessInstance scopePi = getScopeProcessInstance();
      if (this == scopePi)
      {
         DataClusterHelper.synchronizeDataCluster(scopePi);
      }
   }

   /**
    * This method is intended to restore the state after a reloadAttribute(FIELD__STATE).
    * Most not be used in any other way and not be made public.
    *
    * @param state the original state.
    */
   void restoreState(ProcessInstanceState state)
   {
      this.state = state.getValue();
   }

   public void bind(IEventHandler handler, EventHandlerBinding aspect)
   {
      EventUtils.bind(this, handler, aspect);
   }

   public void unbind(IEventHandler handler, EventHandlerBinding aspect)
   {
      EventUtils.unbind(this, handler, aspect);
   }

   private void detachHandlers()
   {
      EventUtils.detachAll(this);
   }


   public boolean isTerminated()
   {
      fetch();
      return (ProcessInstanceState.ABORTED == state)
            || (ProcessInstanceState.COMPLETED == state);
   }

   public boolean isCompleted()
   {
      fetch();
      return ProcessInstanceState.COMPLETED == state;
   }

   void complete()
   {
      markModified(FIELD__TERMINATION_TIME);
      this.terminationTime = TimestampProviderUtils.getTimeStamp().getTime();

      markModified(FIELD__TOKEN_COUNT);
      this.tokenCount = 0;

      processSubProcessOutDataMappings();

      setState(ProcessInstanceState.COMPLETED);

      detachHandlers();
   }

   public long getModelOID()
   {
      fetch();

      return model;
   }

   /**
    * Returns the process definition this instance is instantiated from.
    */
   public IProcessDefinition getProcessDefinition()
   {
      fetch();

      IProcessDefinition process = ModelManagerFactory.getCurrent()
            .findProcessDefinition(model, processDefinition);
      if (null == process)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PROCESS_FOR_PI.raise(processDefinition,
                     getOID()), processDefinition);
      }

      return process;
   }

   public boolean isCaseProcessInstance()
   {
      IProcessDefinition process = ModelManagerFactory.getCurrent()
            .findProcessDefinition(model, processDefinition);
      return process != null
         && PredefinedConstants.CASE_PROCESS_ID.equals(process.getId())
         && PredefinedConstants.PREDEFINED_MODEL_ID.equals(process.getModel().getId());
   }

   public int getPriority()
   {
      fetch();
      return priority;
   }

   public void setPriority(int priority)
   {
      markModified(FIELD__PRIORITY);
      this.priority = priority;
   }

   /**
    * Sets the human who has started the process, if the process is
    * started with a manual trigger.
    */
   public void setStartingUser(IUser startingUser)
   {
      fetchLink(FIELD__STARTING_USER);

      markModified(FIELD__STARTING_USER);
      this.startingUser = (UserBean) startingUser;
   }

   /**
    * Returns the OID of the human who has started the process. If the process is not
    * started with a manual trigger, <tt>0</tt> is returned.
    */
   public long getStartingUserOID()
   {
      fetch();

      // set default value if following conditions do not match
      long startingUserOid = 0;

      if (null != startingUser)
      {
         startingUserOid =  startingUser.getOID();
      }
      else if (isPersistent())
      {
         DefaultPersistenceController controller = (DefaultPersistenceController) getPersistenceController();

         final Long rawStartingUserOid = (Long) controller.getLinkFk(FIELD__STARTING_USER);
         if (rawStartingUserOid != null)
         {
            startingUserOid = rawStartingUserOid.longValue();
         }
      }

      return startingUserOid;
   }

   /**
    * Returns the human who has started the process. If the process is not
    * started with a manual trigger, <tt>null</tt> is returned.
    */
   public IUser getStartingUser()
   {
      fetchLink(FIELD__STARTING_USER);

      return startingUser;
   }

   /**
    * Returns the human who performed the last activity in the process instance.
    */
   public IUser getLastActivityPerformer()
   {
      fetch();

      if (AuditTrailPersistence.isTransientExecution(getAuditTrailPersistence()))
      {
         /* transient process instances do not contain any manual activities */
         return null;
      }

      return ActivityInstanceBean.getLastActivityPerformer(getOID());
   }

   /**
    * Retrieves the value of the process data named <code>dataID</code>.<p>
    */
   public IDataValue getDataValue(String dataID)
   {
      return getDataValue(dataID, null);
   }

   /**
    * Retrieves the value of the process data named <code>dataID</code>.<p>
    */
   public IDataValue getDataValue(String dataID,
         AbstractInitialDataValueProvider dataValueProvider)
   {
      Assert.isNotNull(dataID, "Data ID cannot be null.");

      fetch();

      IData data = ((IModel) getProcessDefinition().getModel()).findData(dataID);

      if (null == data)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(dataID), dataID);
      }

      return getDataValue(data, dataValueProvider);
   }

   private IDataValue findDataValue(IData data)
   {
      long dataOid = ModelManagerFactory.getCurrent().getRuntimeOid(data);

      // don't need to filter for model OID, as for any given process instance the
      // model OID is fixed
      DataValueBean dataValue = (DataValueBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(DataValueBean.class,
            QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(DataValueBean.FR__PROCESS_INSTANCE, getScopeProcessInstanceOID()),//
                  Predicates.isEqual(DataValueBean.FR__DATA, dataOid))));
      if (dataValue != null)
      {
         addDataValue(dataValue);
      }
      return dataValue;
   }

   public IDataValue getDataValue(IData data)
   {
      return getDataValue(data, null);
   }

   public IDataValue getDataValue(IData data,
         AbstractInitialDataValueProvider dataValueProvider)
   {
      if ( !isMetaData(data.getId()) && (getOID() != getScopeProcessInstanceOID()))
      {
         return getScopeProcessInstance().getDataValue(data, dataValueProvider);
      }

      String dataID = data.getId();
      // Check, whether this process instance already manages an instance of
      // the data definition named dataID

      IDataValue dataValue = (null != dataValueCache)
            ? (IDataValue) dataValueCache.get(dataID)
            : null;

      if (dataValue == null)
      {
         if ( !isMetaData(dataID))
         {
            // (ellipsis) if PI was just created, it is not necessary to load the data value
            // from the DB as this thread is the sole owner
            if ( !getPersistenceController().isCreated())
            {
               dataValue = findDataValue(data);
            }

            int triesLeft = 3;
            while (null == dataValue && triesLeft > 0)
            {
               --triesLeft;

               try
               {
                  dataValue = new DataValueBean(this, data, dataValueProvider);
               }
               catch(UniqueConstraintViolatedException x)
               {
                  trace.info("Since data value seems already to exist, now trying to load it from audit trail.");
                  dataValue = findDataValue(data);
               }
            }

            if (null == dataValue)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.BPMRT_FAILED_CREATING_DATA_VALUE.raise(dataID));
            }
         }
      }
      else if (trace.isDebugEnabled())
      {
         trace.debug("Found " + dataValue + " in cache.");
      }

      if (dataValue == null)
      {
         // Retrieve the data definition from the processed definition an instantiate it.

         // meta data that should not be written to the audit trail
         if (isMetaData(dataID))
         {
            dataValue = new MetaDataValueBean(this, data);
         }
         else
         {
            throw new InternalException("DataValue has to be of type "
                  + MetaDataValueBean.class.getName());
         }

         // Check, wether predefined data are requested and assign data

         if (dataID.equals(PredefinedConstants.STARTING_USER))
         {
            if (getStartingUser() != null)
            {
               IUser startingUser = new UserBeanClientExtension(getStartingUser());
               startingUser.setRealm(getStartingUser().getRealm());
               dataValue.setValue(startingUser.getPrimaryKey(), false);
            }
            else
            {
               dataValue.setValue(null, false);
            }

         }
         else if (dataID.equals(PredefinedConstants.ROOT_PROCESS_ID))
         {
            IProcessInstance rootProcessInstance = getRootProcessInstance();

            dataValue.setValue(new Long(rootProcessInstance.getOID()), false);
         }
      }

      if (dataID.equals(PredefinedConstants.CURRENT_USER))
      {
         IUser currentUser = SecurityProperties.getUser();
         dataValue.setValue(
               (null != currentUser && 0 != currentUser.getOID())
               ? currentUser.getPrimaryKey() : null, false);
      }
      else if (dataID.equals(PredefinedConstants.LAST_ACTIVITY_PERFORMER))
      {
         if (getLastActivityPerformer() != null)
         {
            IUser lastActivityPerformer = new UserBeanClientExtension(getLastActivityPerformer());
            lastActivityPerformer.setRealm(getStartingUser().getRealm());
            dataValue.setValue(lastActivityPerformer.getPrimaryKey(), false);
         }
         else
         {
            dataValue.setValue(null, false);
         }

      }
      else if (dataID.equals(PredefinedConstants.CURRENT_DATE))
      {
         dataValue.setValue(Calendar.getInstance(), false);
      }
      else if (dataID.equals(PredefinedConstants.PROCESS_ID))
      {
         // @@todo analyze!
         dataValue.setValue(new Long(getOID()), false);
      }
      else if (dataID.equals(PredefinedConstants.PROCESS_PRIORITY))
      {
         // process priority is no longer read only
         if (null != dataValueProvider)
         {
            final Object rawValue = (Integer) dataValueProvider.getEvaluatedValue()
                  .getValue();
            if (rawValue instanceof Integer)
            {
               int value = ((Integer) rawValue).intValue();

               // at the moment the valid value range is -1, 0, and 1.
               // doing a integer signum to be sure that the range is OK.
               if (value > 0)
               {
                  value = 1;
               }
               else if (value < 0)
               {
                  value = -1;
               }
               setPriority(value);
            }
         }

         dataValue.setValue(new Integer(getPriority()), false);
      }
      else if (dataID.equals(PredefinedConstants.CURRENT_LOCALE))
      {
         dataValue.setValue(Locale.getDefault().getCountry(), false);
      }
      else if (dataID.equals(PredefinedConstants.CURRENT_MODEL))
      {
         IProcessDefinition pd = getProcessDefinition();
         IModel model = (IModel) pd.getModel();
         DeployedModelDescription deployed = (DeployedModelDescription) DetailsFactory.create(
               model, IModel.class, DeployedModelDescriptionDetails.class);
         dataValue.setValue(deployed, false);
      }

      return dataValue;
   }

   public static boolean isMetaData(String dataID)
   {
      return PredefinedConstants.META_DATA_IDS.contains(dataID);
   }

   /**
    * Sets the value of the process data named <code>dataID</code> to the value object.
    */
   public void setDataValue(String dataID, Object object)
   {
      Assert.isNotNull(dataID, "Data ID cannot be null");

      DefaultInitialDataValueProvider dvProvider = new DefaultInitialDataValueProvider(
            object);
      IDataValue dataValue = getDataValue(dataID, dvProvider);

      if ( !dvProvider.isUsedForInitialization())
      {
         dataValue.setValue(object, false);
      }
      addDataValue(dataValue);

      onDataValueChanged(dataValue);
   }

   public Iterator getAllDataValues()
   {
      if (getOID() != getScopeProcessInstanceOID())
      {
         return getScopeProcessInstance().getAllDataValues();
      }
      this.loadDataValuesIntoCache(Collections.EMPTY_LIST);

      return (null != dataValueCache)
            ? this.dataValueCache.values().iterator()
            : Collections.EMPTY_LIST.iterator();
   }

   public IDataValue getCachedDataValue(String dataId)
   {
      if (getOID() != getScopeProcessInstanceOID())
      {
         return ((ProcessInstanceBean)getScopeProcessInstance()).getCachedDataValue(dataId);
      }
      if (null == dataValueCache)
      {
         return null;
      }
      return (IDataValue) dataValueCache.get(dataId);
   }

   public IStructuredDataValue getCachedStructuredDataValue(long xPathOid)
   {
      if (getOID() != getScopeProcessInstanceOID())
      {
         return ((ProcessInstanceBean)getScopeProcessInstance()).getCachedStructuredDataValue(xPathOid);
      }
      if(structuredDataValueCache == null)
      {
         return null;
      }

      return structuredDataValueCache.get(xPathOid);
   }

   public Map getExistingDataValues(boolean includePredefined)
   {
      HashMap result = new HashMap();

      for (Iterator i = getAllDataValues(); i.hasNext();)
      {
         IDataValue value = (IDataValue) i.next();

         IData data = value.getData();
         if (includePredefined || !data.isPredefined())
         {
            result.put(data.getId(), value.getSerializedValue());
         }
      }
      return result;
   }

   public boolean isAborted()
   {
      fetch();
      return  ProcessInstanceState.ABORTED == state;
   }

   public boolean isAborting()
   {
      fetch();
      return  ProcessInstanceState.ABORTING == state;
   }

   /**
    * Returns all activity instances already performed on behalf of this process.
    * This is the audit trail of the process.
    */
   public Iterator getAllPerformedActivityInstances()
   {
      // Collect process instance hierarchy

      List processInstances = new ArrayList();

      IProcessInstance rootProcessInstance = this;

      processInstances.add(rootProcessInstance);

      while (rootProcessInstance.getStartingActivityInstance() != null)
      {
         rootProcessInstance = rootProcessInstance.getStartingActivityInstance()
               .getProcessInstance();

         processInstances.add(rootProcessInstance);
      }

      return ActivityInstanceBean.getAllCompletedForProcessInstanceHierarchy(
            processInstances);
   }

   public long getRootProcessInstanceOID()
   {
      final long oid;
      if ((null == rootProcessInstance)
            && (getPersistenceController() instanceof DefaultPersistenceController))
      {
         oid = ((Number) ((DefaultPersistenceController) getPersistenceController())
               .getLinkFk(FIELD__ROOT_PROCESS_INSTANCE)).longValue();
      }
      else
      {
         oid = getRootProcessInstance().getOID();
      }

      return oid;
   }

   public IProcessInstance getRootProcessInstance()
   {
      fetchLink(FIELD__ROOT_PROCESS_INSTANCE);

      if (trace.isDebugEnabled())
      {
         trace.debug("Fetching root process instance for  process instance " + getOID()
               + ": " + rootProcessInstance);
      }

      return rootProcessInstance;
   }

   public IProcessInstance getScopeProcessInstance()
   {
      fetchLink(FIELD__SCOPE_PROCESS_INSTANCE);

      if (trace.isDebugEnabled())
      {
         trace.debug("Fetching scope process instance for process instance " + getOID()
               + ": " + scopeProcessInstance);
      }

      return scopeProcessInstance;
   }

   public long getScopeProcessInstanceOID()
   {
      final long oid;
      if ((null == scopeProcessInstance)
            && (getPersistenceController() instanceof DefaultPersistenceController))
      {
         oid = ((Number) ((DefaultPersistenceController) getPersistenceController())
               .getLinkFk(FIELD__SCOPE_PROCESS_INSTANCE)).longValue();
      }
      else
      {
         oid = getScopeProcessInstance().getOID();
      }

      return oid;
   }

   public void setRootProcessInstance(ProcessInstanceBean rootProcessInstance)
   {
      fetchLink(FIELD__ROOT_PROCESS_INSTANCE);

      if (trace.isDebugEnabled())
      {
         trace.debug("Setting root process instance for  process instance " + getOID()
               + ": " + rootProcessInstance);
      }

      markModified(FIELD__ROOT_PROCESS_INSTANCE);
      this.rootProcessInstance = rootProcessInstance;

      // (fh) is this required ?
      setDeployment(rootProcessInstance.getReferenceDeployment());
   }

   public void setDeployment(long deployment)
   {
      markModified(FIELD__DEPLOYMENT);
      this.deployment = deployment;
   }

   public void setScopeProcessInstance(ProcessInstanceBean scopeProcessInstance)
   {
      fetchLink(FIELD__SCOPE_PROCESS_INSTANCE);

      if (trace.isDebugEnabled())
      {
         trace.debug("Setting scope process instance for process instance " + getOID()
               + ": " + scopeProcessInstance);
      }

      markModified(FIELD__SCOPE_PROCESS_INSTANCE);
      this.scopeProcessInstance = scopeProcessInstance;
   }

   public long getStartingActivityInstanceOID()
   {
      final long oid;
      if ((null == startingActivityInstance)
            && (getPersistenceController() instanceof DefaultPersistenceController))
      {
         Number startingAiOid = (Number) ((DefaultPersistenceController) getPersistenceController()).getLinkFk(FIELD__STARTING_ACTIVITY_INSTANCE);
         oid = (null != startingAiOid) ? startingAiOid.longValue() : 0;
      }
      else
      {
         IActivityInstance ai = getStartingActivityInstance();
         oid = (null != ai) ? ai.getOID() : 0;
      }

      return oid;
   }

   /**
    * Returns the calling activity instance if this process is started as a
    * subprocess on behalf of this activity instance.
    */
   public IActivityInstance getStartingActivityInstance()
   {
      fetchLink(FIELD__STARTING_ACTIVITY_INSTANCE);

      if (trace.isDebugEnabled())
      {
         trace.debug("Fetching starting activity instance for  process instance "
               + getOID() + ": " + startingActivityInstance);
      }

      return startingActivityInstance;
   }

   public void setStartingActivityInstance(ActivityInstanceBean startingActivityInstance)
   {
      fetchLink(FIELD__STARTING_ACTIVITY_INSTANCE);

      if (trace.isDebugEnabled())
      {
         trace.debug("Setting starting activity instance for  process instance "
               + getOID() + ": " + startingActivityInstance);
      }

      markModified(FIELD__STARTING_ACTIVITY_INSTANCE);
      this.startingActivityInstance = startingActivityInstance;
   }

   /**
    * Interrupts the process due to exception in non-interactive application.
    */
   public void interrupt()
   {
      setState(ProcessInstanceState.INTERRUPTED);

      AuditTrailLogger.getInstance(LogCode.ENGINE, this).info(
            "Process instance interrupted.");

      MonitoringUtils.processExecutionMonitors().processInterrupted(this);
   }

   public void resetInterrupted()
   {
      setState(ProcessInstanceState.ACTIVE);
   }

   /**
    * <code>ag.carnot.utils.predicate.SymbolTable</code> protocol.
    */
   public AccessPoint lookupSymbolType(String name)
   {
      return ((IModel) getProcessDefinition().getModel()).findData(name);
   }

   /**
    * <code>ag.carnot.utils.predicate.SymbolTable</code> protocol.
    */
   public Object lookupSymbol(String name)
   {
      Assert.isNotNull(name, "Symbol name may not be null.");

      IDataValue dataValue = getDataValue(name);

      if (dataValue != null)
      {
         Object value = dataValue.getValue();

         if (trace.isDebugEnabled())
         {
            trace.debug("Symbol '" + name + "' retrieved. Value is '" + value + "'.");
         }

         return value;
      }

      return null;
   }

   public void setOutDataValue(IData data, String path, Object value)
         throws InvalidValueException
   {
      /*BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
      if (authorizationPredicate instanceof DataAuthorization2Predicate)
      {
         ((DataAuthorization2Predicate) authorizationPredicate).setProcessInstance(this);
         if (!authorizationPredicate.accept(data))
         {
            return;
         }
      }*/

      String validatorClass = data.getType().getStringAttribute(
            PredefinedConstants.VALIDATOR_CLASS_ATT);
      ExtendedDataValidator validator = SpiUtils.createExtendedDataValidator(validatorClass);
      // TODO fix fragility of test in case of non-Java data
      AccessPathEvaluationContext context = new AccessPathEvaluationContext(this,
            value instanceof Enum && JavaDataTypeUtils.isJavaEnumeration(data) ? JAVA_ENUM_ACCESS_POINT : null);
      BridgeObject dataBridge = validator.getBridgeObject(data, path, Direction.IN, context);
      // (fh) we perform the check only if we're using the default BridgeObject and not a
      // specialized subclass
      if ((null != value) && dataBridge.getClass().equals(BridgeObject.class)
            && !dataBridge.acceptAssignmentFrom(new BridgeObject(value.getClass(),
                  Direction.OUT)))
      {
         // try casting rhs value
         Object castedValue = Reflect.castValue(value, dataBridge.getEndClass());
         if ((null != castedValue)
               && !dataBridge.acceptAssignmentFrom(new BridgeObject(castedValue.getClass(),
                     Direction.OUT)))
         {
            ErrorCase errorCase;
            if (null == path)
            {
               errorCase = BpmRuntimeError.BPMRT_INCOMPATIBLE_TYPE_FOR_DATA.raise(data
                     .getId());
            }
            else
            {
               errorCase = BpmRuntimeError.BPMRT_INCOMPATIBLE_TYPE_FOR_DATA_WITH_PATH
                     .raise(data.getId(), path);
            }
            throw new InvalidValueException(errorCase);
         }
         else
         {
            value = castedValue;
         }
      }

      OutDataMappingValueProvider dvProvider = new OutDataMappingValueProvider(data, path, value, context);
      IDataValue dataValue = getDataValue(data, dvProvider);

      Assert.isNotNull(dataValue);

      if ( !dvProvider.isUsedForInitialization())
      {
         dvProvider.setCurrentValue(dataValue.getValue());
         AbstractInitialDataValueProvider.EvaluatedValue newRef = dvProvider
               .getEvaluatedValue();

         if ( newRef.isModifiedHandle())
         {
               dataValue.setValue(newRef.getValue(), false);
         }
         // NOTE enforcing early persisting of the data. Maybe not feasible if we switch
         // to a data SPI
         if (dataValue instanceof DataValueBean)
         {
            ((DataValueBean) dataValue).lock();
         }
         dataValue.refresh();
         addDataValue(dataValue);
      }

      onDataValueChanged(dataValue);
   }

   @SPI(status = Status.Experimental, useRestriction = UseRestriction.Internal)
   public static interface DataValueChangeListener
   {
      void onDataValueChanged(IDataValue dataValue);
   }

   private void onDataValueChanged(IDataValue dataValue)
   {
      List<DataValueChangeListener> changeListeners = ExtensionProviderUtils
            .getExtensionProviders(DataValueChangeListener.class);
      if (null != changeListeners)
      {
         for (int i = 0; i < changeListeners.size(); i++)
         {
            DataValueChangeListener listener = changeListeners.get(i);
            listener.onDataValueChanged(dataValue);
         }
      }
   }

   public Object getInDataValue(IData data, String path)
   {
      return getInDataValue(data, path, null, null, null);
   }

   public Object getInDataValue(IData data, String path,
         AccessPoint targetActivityAccessPoint, String targetPath, IActivity activity)
   {
      /*BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
      if (authorizationPredicate instanceof DataAuthorization2Predicate)
      {
         ((DataAuthorization2Predicate) authorizationPredicate).setProcessInstance(this);
         if (!authorizationPredicate.accept(data))
         {
            return null;
         }
      }*/

      IDataValue dataValue = getDataValue(data);
      ExtendedAccessPathEvaluator evaluator = SpiUtils
            .createExtendedAccessPathEvaluator(data, path);
      AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(this,
            targetActivityAccessPoint == null && JavaDataTypeUtils.isJavaEnumeration(data)
               ? JAVA_ENUM_ACCESS_POINT : targetActivityAccessPoint,
            targetPath, activity);
      return evaluator.evaluate(data, dataValue.getValue(), path, evaluationContext);
   }

   void lockDataValue(IData data) throws PhantomException
   {
      // TODO (fh) hardcoded 1 min, should be configurable
      long timeout = System.currentTimeMillis() + 60000;
      IDataValue dataValue = getDataValue(data);
      if (dataValue instanceof DataValueBean)
      {
         while (true)
         {
            try
            {
               ((DataValueBean) dataValue).lock();
               ((DataValueBean) dataValue).reload();
               if (dataValueCache != null)
               {
                  dataValueCache.put(data.getId(), dataValue);
               }
               return;
            }
            catch (ConcurrencyException ex)
            {
               if (System.currentTimeMillis() > timeout)
               {
                  throw ex;
               }
               try
               {
                  // TODO (fh) hardcoded random delay between 50 and 100 milliseconds, should be configurable
                  Thread.sleep((long) (Math.random() * 50 + 50));
               }
               catch (InterruptedException e)
               {
                  // (fh) do nothing
               }
            }
            catch (PhantomException ex)
            {
               throw ex;
            }
         }
      }
   }

   /**
    * Evaluates a condition against the data instances of this process instance.
    */
   public boolean validateLoopCondition(String condition)
   {
      try
      {
         trace.debug("Validating loop condition '" + condition + "'");

         return Result.TRUE.equals(Interpreter.evaluate(condition, this));
      }
      catch (SyntaxError x)
      {
         throw new InternalException(x);
      }
      catch (EvaluationError x)
      {
         throw new InternalException(x);
      }
   }

   public void addDataValue(IDataValue value)
   {
      if (null == dataValueCache)
      {
         this.dataValueCache = CollectionUtils.newMap();
      }

      dataValueCache.put(value.getData().getId(), value);
   }

   public void addStructuredDataValue(IStructuredDataValue value)
   {
      if (getOID() != getScopeProcessInstanceOID())
      {
         ProcessInstanceBean scopeProcessInstance
            = (ProcessInstanceBean) getScopeProcessInstance();
         scopeProcessInstance.addStructuredDataValue(value);
      }

      if(structuredDataValueCache == null)
      {
         this.structuredDataValueCache = CollectionUtils.newHashMap();
      }

      structuredDataValueCache.put(value.getXPathOID(), value);

   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      return new ProcessInstanceProperty(getOID(), name, value);
   }

   public Class getPropertyImplementationClass()
   {
      return ProcessInstanceProperty.class;
   }

   public void doBindAutomaticlyBoundEvents()
   {
      final IProcessDefinition processDefinition = getProcessDefinition();

      for (int i = 0; i < processDefinition.getEventHandlers().size(); ++i)
      {
         IEventHandler handler = (IEventHandler) processDefinition.getEventHandlers().get(i);

         IEventConditionType conditionType = (IEventConditionType) handler.getType();
         if (EventType.Pull.equals(conditionType.getImplementation())
               && handler.isAutoBind())
         {
            bind(handler, new EventHandlerBindingDetails(handler));
         }
      }
   }

   public void preloadDataValues(List dataItems)
   {
      if(getOID() != getScopeProcessInstanceOID())
      {
         getScopeProcessInstance().preloadDataValues(dataItems);
         return;
      }

      final int maxBatchSize = Parameters.instance().getInteger(
            KernelTweakingProperties.DESCRIPTOR_PREFETCH_BATCH_SIZE,
            PREFETCH_BATCH_SIZE);

      List dataOidList = new ArrayList(maxBatchSize);

      for (Iterator i = dataItems.iterator(); i.hasNext();)
      {
         IData data = (IData) i.next();

         // Check, wether this process instance already manages an instance of
         // the data definition
         IDataValue dataValue = (null != dataValueCache)
               ? (IDataValue) dataValueCache.get(data.getId())
               : null;

         if (dataValue == null)
         {
            dataOidList.add(new Long(ModelManagerFactory.getCurrent().getRuntimeOid(data)));
         }

         if (dataOidList.size() > maxBatchSize || !i.hasNext())
         {
            // should not call loadDataValuesIntoCache with empty list (the case when all is cached),
            // since this will load all data values (redundant)
            if (!dataOidList.isEmpty())
            {
               loadDataValuesIntoCache(dataOidList);
            }
         }
      }
   }

   public void loadDataValuesIntoCache(List dataOidList)
   {
      if (getPersistenceController().isCreated()
            && (getPersistenceController().getSession() instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session))
      {
         // PI was not yet INSERTed into the Audit Trail DB, so all existing data values
         // will already be in the session cache
         return;
      }

      PredicateTerm predicate;
      if (dataOidList == null || dataOidList.isEmpty())
      {
         predicate = Predicates.isEqual(DataValueBean.FR__PROCESS_INSTANCE,
               getScopeProcessInstanceOID());
      }
      else
      {
         predicate = Predicates.andTerm(Predicates.isEqual(
               DataValueBean.FR__PROCESS_INSTANCE, getScopeProcessInstanceOID()),
               Predicates.inList(DataValueBean.FR__DATA, dataOidList));
      }

      ResultIterator resultIter = SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).getIterator(
                  DataValueBean.class,
                  QueryExtension.where(predicate));

      // iterate over complete result to ensure all items are put into cache
      try
      {
         while (resultIter.hasNext())
         {
            resultIter.next();
         }
      }
      finally
      {
         resultIter.close();
      }
   }


   public boolean isPropertyAvailable()
   {
      fetch();
      return propertiesAvailable != 0 ? true : false;
   }

   public boolean isPropertyAvailable(int pattern)
   {
      fetch();
      return (propertiesAvailable & pattern) == pattern ? true : false;
   }

   public void addNote(String note)
   {
      addNote(note, ContextKind.ProcessInstance, getOID());
   }

   public void addNote(String note, ContextKind contextKind, long contextOid)
   {
      if ( !StringUtils.isEmpty(note))
      {
         // encode the context into the note text
         StringBuffer buffer = new StringBuffer();

         if (ContextKind.ProcessInstance != contextKind)
         {
            MessageFormat msgFormat = new MessageFormat(PI_NOTE_CONTEXT_PREFIX_PATTERN);
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(false);
            msgFormat.setFormatByArgumentIndex(1, numberFormat);
            buffer.append(msgFormat.format(
                  new Object[] { new Integer(contextKind.getValue()),
                        new Long(contextOid) }));
         }

         buffer.append(note);

         setPropertyValue(PI_NOTE, buffer.toString());
      }
   }

   public void addExistingNote(ProcessInstanceProperty srcNote)
   {
      super.addProperty(srcNote.clone(this.getOID()));

      propIndexHandler.handleIndexForGeneralProperties();
      propIndexHandler.handleIndexForNoteProperty(noteExists());
      propIndexHandler.handleIndexForPiAbortingProperty(abortingPiExists());
   }

   public List/* <Attribute> */getNotes()
   {
      // Lookup prefetch cache.
      BpmRuntimeEnvironment bpmRuntimeEnv = PropertyLayerProviderInterceptor.getCurrent();
      Map<Long, List> notesCache = (Map) bpmRuntimeEnv.get(PrefetchConstants.NOTES_PI_CACHE);
      List noteAttributes = notesCache == null ? null : notesCache.get(this.getOID());

      if (noteAttributes == null)
      {
         noteAttributes = (List) getPropertyValue(PI_NOTE);
      }

      if (null == noteAttributes)
      {
         return Collections.EMPTY_LIST;
      }

      return noteAttributes;
   }

   public void addAbortingPiOid(long oid)
   {
      setPropertyValue(ABORTING_PI_OID, new Long(oid));
   }

   public void removeAbortingPiOid(long oid)
   {
      removeProperty(ABORTING_PI_OID, new Long(oid));
   }

   public List/*<Attribute>*/ getAbortingPiOids()
   {
      List attributes = (List) getPropertyValue(ABORTING_PI_OID);
      if(null == attributes)
      {
         return Collections.EMPTY_LIST;
      }

      return attributes;
   }

   public void addAbortingUserOid(long oid)
   {
      setPropertyValue(ABORTING_USER_OID, new Long(oid));
   }

   public void addPropertyValues(Map attributes)
   {
      super.addPropertyValues(attributes);

      propIndexHandler.handleIndexForGeneralProperties();
      propIndexHandler.handleIndexForNoteProperty(noteExists());
      propIndexHandler.handleIndexForPiAbortingProperty(abortingPiExists());
   }

   public void setPropertyValue(String name, Serializable value)
   {
      super.setPropertyValue(name, value);

      propIndexHandler.handleIndexForGeneralProperties();
      propIndexHandler.handleIndexForNoteProperty(noteExists());
      propIndexHandler.handleIndexForPiAbortingProperty(abortingPiExists());
   }

   public void removeProperty(String name)
   {
      super.removeProperty(name);

      propIndexHandler.handleIndexForGeneralProperties();
      propIndexHandler.handleIndexForNoteProperty(noteExists());
      propIndexHandler.handleIndexForPiAbortingProperty(abortingPiExists());
   }

   public void removeProperty(String name, Serializable value)
   {
      super.removeProperty(name, value);

      propIndexHandler.handleIndexForGeneralProperties();
      propIndexHandler.handleIndexForNoteProperty(noteExists());
      propIndexHandler.handleIndexForPiAbortingProperty(abortingPiExists());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistentBean#supportedMultiAttributes()
    */
   protected String[] supportedMultiAttributes()
   {
      return new String[] { PI_NOTE, ABORTING_PI_OID };
   }

   public AuditTrailPersistence getAuditTrailPersistence()
   {
      if (isGlobalAuditTrailPersistenceOverride())
      {
         return determineGlobalOverride();
      }
      else
      {
         return getAuditTrailPersistencePropertyValue();
      }
   }

   private AuditTrailPersistence determineGlobalOverride()
   {
      final Parameters params = Parameters.instance();
      final String globalSetting = params.getString(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF);

      if (KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF.equals(globalSetting))
      {
         return AuditTrailPersistence.IMMEDIATE;
      }
      else if (KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT.equals(globalSetting))
      {
         return AuditTrailPersistence.TRANSIENT;
      }
      else if (KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED.equals(globalSetting))
      {
         return AuditTrailPersistence.DEFERRED;
      }
      else
      {
         throw new IllegalStateException("Value '" + globalSetting + "' is not an override for property '" + KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES + "'.");
      }
   }

   public void setAuditTrailPersistence(final AuditTrailPersistence newValue)
   {
      if (newValue == null)
      {
         throw new NullPointerException("Audit Trail Persistence must not be null.");
      }

      final AuditTrailPersistence oldValue = getAuditTrailPersistencePropertyValue();
      oldValue.assertThatStateChangeIsAllowedTo(newValue, getPersistenceController().isCreated());

      final boolean isGlobalOverride = isGlobalAuditTrailPersistenceOverride();
      if (isGlobalOverride)
      {
         trace.warn("Changing process instance bound Audit Trail Persistence to '" + newValue + "' although a global override is set. (OID: " + oid + ").");
      }

      if (oldValue != newValue && !isGlobalOverride)
      {
         trace.info("Changing Audit Trail Persistence from '" + oldValue + "' to '" + newValue + "' (OID: " + oid + ").");
      }

      if (previousAuditTrailPersistence == null)
      {
         previousAuditTrailPersistence = oldValue;
      }

      setAuditTrailPersistencePropertyValue(newValue);
   }

   public AuditTrailPersistence getPreviousAuditTrailPersistence()
   {
      return previousAuditTrailPersistence;
   }

   private boolean isGlobalAuditTrailPersistenceOverride()
   {
      final Parameters params = Parameters.instance();
      final String globalSetting = params.getString(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF);

      final boolean isOff = KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF.equals(globalSetting);
      final boolean isAlwaysTransient = KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT.equals(globalSetting);
      final boolean isAlwaysDeferred = KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED.equals(globalSetting);
      if (isOff || isAlwaysTransient || isAlwaysDeferred)
      {
         return true;
      }

      return false;
   }

   private void setAuditTrailPersistencePropertyValue(final AuditTrailPersistence value)
   {
      setPropertyValue(AUDIT_TRAIL_PERSISTENCE_PROPERTY_KEY, value.name());
   }

   private AuditTrailPersistence getAuditTrailPersistencePropertyValue()
   {
      final String auditTrailPersistenceName = (String) getPropertyValue(AUDIT_TRAIL_PERSISTENCE_PROPERTY_KEY);
      if (auditTrailPersistenceName == null)
      {
         return AuditTrailPersistence.ENGINE_DEFAULT;
      }

      return AuditTrailPersistence.valueOf(auditTrailPersistenceName);
   }

   private boolean noteExists()
   {
      Attribute property = (Attribute) getAllProperties().get(PI_NOTE);
      return propertyExists(property);
   }

   private boolean abortingPiExists()
   {
      Attribute property = (Attribute) getAllProperties().get(ABORTING_PI_OID);
      return propertyExists(property);
   }

   private AuditTrailPersistence determineAuditTrailPersistence(final IProcessDefinition processDef)
   {
      final String auditTrailPersistenceName = (String) processDef.getAttribute(PredefinedConstants.TRANSIENT_PROCESS_AUDIT_TRAIL_PERSISTENCE);
      if (auditTrailPersistenceName == null)
      {
         return AuditTrailPersistence.ENGINE_DEFAULT;
      }

      return AuditTrailPersistence.valueOf(auditTrailPersistenceName);
   }

   /**
    * @throws ObjectNotFoundException
    * @throws InvalidValueException
    */
   private void processSubProcessOutDataMappings() throws ObjectNotFoundException,
         InvalidValueException
   {
      IActivityInstance startingActivityInstance = getStartingActivityInstance();
      if (null != startingActivityInstance)
      {
         IActivity startingActivity = startingActivityInstance.getActivity();
         if (ImplementationType.SubProcess == startingActivity.getImplementationType())
         {
            SubProcessModeKey subProcessMode = startingActivity.getSubProcessMode();
            if (subProcessMode != null && !SubProcessModeKey.ASYNC_SEPARATE.equals(subProcessMode))
            {
               String outputParameterId = null;
               String parameterContext = null;
               ILoopCharacteristics loop = startingActivity.getLoopCharacteristics();
               if (loop instanceof IMultiInstanceLoopCharacteristics)
               {
                  outputParameterId = ((IMultiInstanceLoopCharacteristics) loop).getOutputParameterId();
                  if (outputParameterId != null)
                  {
                     parameterContext = outputParameterId.substring(0, outputParameterId.indexOf(':'));
                     outputParameterId = outputParameterId.substring(parameterContext.length() + 1);
                  }
               }

               ModelElementList outDataMappings = startingActivity.getOutDataMappings();
               for (int i = 0; i < outDataMappings.size(); ++i)
               {
                  IDataMapping mapping = (IDataMapping) outDataMappings.get(i);

                  String context = mapping.getContext();
                  if (PredefinedConstants.ENGINE_CONTEXT.equals(context) ||
                      PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(context))
                  {
                     // copy data value
                     String accessPointId = mapping.getActivityAccessPointId();
                     IData subProcessData = PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(context)
                        ? ModelUtils.getMappedData(getProcessDefinition(), accessPointId)
                        : ModelUtils.getData(getProcessDefinition(), accessPointId);

                     String subProcessDataPath = mapping.getActivityPath();
                     Object subProcessDataValue = getInDataValue(subProcessData, subProcessDataPath);

                     IProcessInstance parentProcessInstance = startingActivityInstance.getProcessInstance();
                     IData data = mapping.getData();
                     String dataPath = mapping.getDataPath();
                     if (outputParameterId != null
                           && outputParameterId.equals(accessPointId)
                           && parameterContext.equals(context))
                     {
                        int index = TransitionTokenBean.getMultiInstanceIndex(startingActivityInstance.getOID());
                        if (index >= 0)
                        {
                           try
                           {
                              lockDataValue(data);
                           }
                           catch (PhantomException e)
                           {
                              // (fh) do nothing
                              continue;
                           }
                           Object list = parentProcessInstance.getInDataValue(data, dataPath);
                           subProcessDataValue = ActivityInstanceBean.setValueInList(index, subProcessDataValue, list);
                        }
                     }
                     parentProcessInstance.setOutDataValue(data, dataPath, subProcessDataValue);
                  }
               }
            }
         }
      }
   }

   private static void createHierarchyEntries(ProcessInstanceBean processInstance,
         IProcessInstance parentProcessInstance)
   {
      new ProcessInstanceHierarchyBean(processInstance, processInstance);

      if (parentProcessInstance != null)
      {
         IProcessInstance innerParentProcessInstance = parentProcessInstance;

         IActivityInstance parentActivityInstance = null;

         boolean iterateParents = true;
         while (iterateParents)
         {
            new ProcessInstanceHierarchyBean(innerParentProcessInstance, processInstance);

            parentActivityInstance = (ActivityInstanceBean) innerParentProcessInstance.getStartingActivityInstance();
            if (parentActivityInstance != null)
            {
               innerParentProcessInstance = parentActivityInstance.getProcessInstance();
               if (innerParentProcessInstance == null)
               {
                  iterateParents = false;
               }
            }
            else
            {
               innerParentProcessInstance = ProcessInstanceHierarchyBean.findParentForSubProcessInstanceOid(innerParentProcessInstance.getOID());
               if (innerParentProcessInstance == null)
               {
                  iterateParents = false;
               }
            }
         }
      }
   }

   private static IProcessInstanceScope createScopeEntry(ProcessInstanceBean processInstance,
         ActivityInstanceBean parentActivityInstance, IProcessInstance parentProcessInstance)
   {
      SubProcessModeKey mode = null;
      if (null != parentActivityInstance)
      {
         mode = parentActivityInstance.getActivity().getSubProcessMode();
         if (null == mode)
         {
            mode = SubProcessModeKey.SYNC_SHARED;
         }
      }
      else if (null != parentProcessInstance)
      {
         // spawn process scope
         mode = SubProcessModeKey.SYNC_SEPARATE;
      }

      final IProcessInstance rootProcessInstance = processInstance
            .getRootProcessInstance();
      IProcessInstance scopeProcessInstance = null;

      if (null == parentProcessInstance || SubProcessModeKey.ASYNC_SEPARATE == mode)
      {
         scopeProcessInstance = rootProcessInstance;
      }
      else if (SubProcessModeKey.SYNC_SEPARATE == mode)
      {
         scopeProcessInstance = processInstance;
      }
      else if (SubProcessModeKey.SYNC_SHARED == mode)
      {
         scopeProcessInstance = parentActivityInstance.getProcessInstance()
               .getScopeProcessInstance();
      }
      else
      {
         Assert.lineNeverReached();
      }

      return new ProcessInstanceScopeBean(processInstance, scopeProcessInstance,
            rootProcessInstance);
   }

   private static void createClusterInstance(IProcessInstance scopeProcessInstance)
   {
      DataCluster[] clusterSetup = RuntimeSetup.instance().getDataClusterSetup();
      if (null != clusterSetup)
      {
         for (int i = 0; i < clusterSetup.length; ++i)
         {
            new DataClusterInstance(clusterSetup[i], scopeProcessInstance.getOID());
         }
      }
   }

   private class PropertyIndexHandler
   {
      public void handleIndexForGeneralProperties()
      {
         if (getAllProperties().isEmpty())
         {
            unmarkPropertyAsAvailable(PI_PROPERTY_FLAG_ALL);
         }
         else
         {
            markPropertyAsAvailable(PI_PROPERTY_FLAG_ANY);
         }
      }

      public void handleIndexForNoteProperty(boolean noteAvailable)
      {
         if (noteAvailable)
         {
            markPropertyAsAvailable(PI_PROPERTY_FLAG_NOTE);
         }
         else
         {
            unmarkPropertyAsAvailable(PI_PROPERTY_FLAG_NOTE);
         }
      }

      public void handleIndexForPiAbortingProperty(boolean piAborting)
      {
         if (piAborting)
         {
            markPropertyAsAvailable(PI_PROPERTY_FLAG_PI_ABORTING);
         }
         else
         {
            unmarkPropertyAsAvailable(PI_PROPERTY_FLAG_PI_ABORTING);
         }
      }

      private void markPropertyAsAvailable(int pattern)
      {
         fetch();

         int tempPropsAvailable = propertiesAvailable | (pattern | PI_PROPERTY_FLAG_ANY);
         if (propertiesAvailable != tempPropsAvailable)
         {
            propertiesAvailable = tempPropsAvailable;
            markModified(FIELD__PROPERTIES_AVAILABLE);
         }
      }

      private void unmarkPropertyAsAvailable(int pattern)
      {
         fetch();

         int tempPropsAvailable = propertiesAvailable & ~pattern;
         if (propertiesAvailable != tempPropsAvailable)
         {
            propertiesAvailable = tempPropsAvailable;
            markModified(FIELD__PROPERTIES_AVAILABLE);
         }
      }

   }

   private class OutDataMappingValueProvider extends AbstractInitialDataValueProvider
   {
      private final IData data;
      private final String path;
      private final Object value;

      private Object currentValue;

      private final ExtendedAccessPathEvaluator evaluator;
      private AccessPathEvaluationContext context;

      public OutDataMappingValueProvider(IData data, String path, Object value, AccessPathEvaluationContext context)
      {
         this.data = data;
         this.path = path;
         this.value = value;
         this.context = context;

         currentValue = DataValueUtils.createNewValueInstance(data, ProcessInstanceBean.this);

         evaluator = SpiUtils.createExtendedAccessPathEvaluator(data, path);
      }

      public EvaluatedValue getEvaluatedValue()
      {
         final Object evaluationResult = evaluator.evaluate(data,
               currentValue, path, context, value);

         return AccessPathEvaluator.UNMODIFIED_HANDLE == evaluationResult
               ? new EvaluatedValue(currentValue, false)
               : new EvaluatedValue(evaluationResult, true);
      }

      public void setCurrentValue(Object defaultValue)
      {
         this.currentValue = defaultValue;
      }
   }

   public class IStructuredDataValueCacheKey
   {
      private final long xpathOid;
      private final long processInstanceOid;

      public IStructuredDataValueCacheKey(long processInstanceOid, long xpathOid)
      {
         this.processInstanceOid = processInstanceOid;
         this.xpathOid = xpathOid;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + getOuterType().hashCode();
         result = prime * result
               + (int) (processInstanceOid ^ (processInstanceOid >>> 32));
         result = prime * result + (int) (xpathOid ^ (xpathOid >>> 32));
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         IStructuredDataValueCacheKey other = (IStructuredDataValueCacheKey) obj;
         if (!getOuterType().equals(other.getOuterType()))
            return false;
         if (processInstanceOid != other.processInstanceOid)
            return false;
         if (xpathOid != other.xpathOid)
            return false;
         return true;
      }

      private ProcessInstanceBean getOuterType()
      {
         return ProcessInstanceBean.this;
      }
   }
}
