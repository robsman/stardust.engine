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
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils.isSerialExecutionScenario;

import java.io.Serializable;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.*;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.dto.EventHandlerBindingDetails;
import org.eclipse.stardust.engine.api.dto.LazilyLoadingActivityInstanceDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkUtils;
import org.eclipse.stardust.engine.core.model.beans.ActivityBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.QualityAssuranceActivityBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.Session.FilterOperation;
import org.eclipse.stardust.engine.core.persistence.jdbc.DefaultPersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.ChangeLogDigester;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.utils.*;
import org.eclipse.stardust.engine.core.runtime.utils.PerformerUtils.EncodedPerformer;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.*;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * @author mgille
 */
public class ActivityInstanceBean extends AttributedIdentifiablePersistentBean
      implements IActivityInstance, IProcessInstanceAware
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ActivityInstanceBean.class);

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;

   public static final String FIELD__STATE = "state";

   public static final String FIELD__START_TIME = "startTime";

   public static final String FIELD__LAST_MODIFICATION_TIME = "lastModificationTime";

   public static final String FIELD__MODEL = "model";

   public static final String FIELD__ACTIVITY = "activity";

   public static final String FIELD__PROCESS_INSTANCE = "processInstance";

   public static final String FIELD__CURRENT_PERFORMER = "currentPerformer";

   public static final String FIELD__CURRENT_USER_PERFORMER = "currentUserPerformer";

   public static final String FIELD__PERFORMED_BY = "performedBy";

   public static final String FIELD__CURRENT_DEPARTMENT = "currentDepartment";

   // Adding crticality field
   public static final String FIELD__CRITICALITY = "criticality";

   public static final String FIELD__PROPERTIES_AVAILABLE = "propertiesAvailable";

   public static final String FIELD__BENCHMAKRK_VALUE = "benchmarkValue";


   public static final FieldRef FR__OID = new FieldRef(ActivityInstanceBean.class,
         FIELD__OID);

   public static final FieldRef FR__STATE = new FieldRef(ActivityInstanceBean.class,
         FIELD__STATE);

   public static final FieldRef FR__START_TIME = new FieldRef(ActivityInstanceBean.class,
         FIELD__START_TIME);

   public static final FieldRef FR__LAST_MODIFICATION_TIME = new FieldRef(
         ActivityInstanceBean.class, FIELD__LAST_MODIFICATION_TIME);

   public static final FieldRef FR__MODEL = new FieldRef(ActivityInstanceBean.class,
         FIELD__MODEL);

   public static final FieldRef FR__ACTIVITY = new FieldRef(ActivityInstanceBean.class,
         FIELD__ACTIVITY);

   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(
         ActivityInstanceBean.class, FIELD__PROCESS_INSTANCE);

   public static final FieldRef FR__CURRENT_PERFORMER = new FieldRef(
         ActivityInstanceBean.class, FIELD__CURRENT_PERFORMER);

   public static final FieldRef FR__CURRENT_USER_PERFORMER = new FieldRef(
         ActivityInstanceBean.class, FIELD__CURRENT_USER_PERFORMER);

   public static final FieldRef FR__PERFORMED_BY = new FieldRef(
         ActivityInstanceBean.class, FIELD__PERFORMED_BY);

   public static final FieldRef FR__CURRENT_DEPARTMENT = new FieldRef(
         ActivityInstanceBean.class, FIELD__CURRENT_DEPARTMENT);

   // Adding criticality field ref
   public static final FieldRef FR__CRITICALITY = new FieldRef(
         ActivityInstanceBean.class, FIELD__CRITICALITY);

   public static final FieldRef FR__PROPERTIES_AVAILABLE = new FieldRef(
         ActivityInstanceBean.class, FIELD__PROPERTIES_AVAILABLE);

   public static final FieldRef FR__BENCHMARK_VALUE = new FieldRef(
         ActivityInstanceBean.class, FIELD__BENCHMAKRK_VALUE);

   public static final String TABLE_NAME = "activity_instance";

   public static final String DEFAULT_ALIAS = "ai";

   public static final String LOCK_TABLE_NAME = "activity_instance_lck";

   public static final String LOCK_INDEX_NAME = "act_inst_lck_idx";

   public static final String PK_FIELD = FIELD__OID;

   public static final String PK_SEQUENCE = "activity_instance_seq";

   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] activity_inst_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};

   public static final String[] activity_inst_idx2_INDEX = new String[] {
         FIELD__CURRENT_PERFORMER, FIELD__CURRENT_DEPARTMENT};

   public static final String[] activity_inst_idx3_INDEX = new String[] {
         FIELD__CURRENT_USER_PERFORMER, FIELD__CURRENT_PERFORMER,
         FIELD__CURRENT_DEPARTMENT};

   public static final String[] activity_inst_idx4_INDEX = new String[] {FIELD__PERFORMED_BY};

   public static final String[] activity_inst_idx5_INDEX = new String[] {FIELD__PROCESS_INSTANCE};

   public static final String[] activity_inst_idx6_INDEX = new String[] {
         FIELD__START_TIME, FIELD__ACTIVITY, FIELD__STATE};

   public static final String[] activity_inst_idx7_INDEX = new String[] {FIELD__STATE};

   public static final String[] activity_inst_idx8_INDEX = new String[] {
         FIELD__ACTIVITY, FIELD__PROCESS_INSTANCE};

   public static final String[] activity_inst_idx9_INDEX = new String[] {
         FIELD__CRITICALITY, FIELD__PROCESS_INSTANCE};

   public static final String BOUNDARY_EVENT_HANDLER_ACTIVATED_PROPERTY_KEY = "Infinity.Engine.BoundaryEventHandler.Activated";

   static final boolean state_USE_LITERALS = true;

   static final boolean currentUserPerformer_USE_LITERALS = true;

   private transient ActivityInstanceState originalState;

   private transient EncodedPerformer originalPerformer;

   private transient List<ChangeLogDigester.HistoricState> historicStates;

   private transient Long lastModifyingUser;

   private transient int index = -1;

   private int state;

   private long startTime;

   private long lastModificationTime;

   /**
    * Contains the OID of the activity.
    */
   @ForeignKey (modelElement=ModelBean.class)
   protected long model;

   @ForeignKey (modelElement=ActivityBean.class)
   protected long activity;

   ProcessInstanceBean processInstance;

   static final String processInstance_EAGER_FETCH = Boolean.TRUE.toString();

   static final String processInstance_MANDATORY = Boolean.TRUE.toString();

   /**
    * Contains the OID of the current performer, if it is an organization or role or user
    * group. An user group will be identified by a negative OID;
    */
   private long currentPerformer;

   /**
    * Contains the OID of the current performer, if it is a user.
    */
   private long currentUserPerformer;

   /**
    * Contains the OID of the user, who performed this instance.
    */
   private long performedBy;

   private long currentDepartment;

   /**
    * Contains the Double for the criticality of this instance
    */
   private double criticality;

   /**
    * Contains the integer value for the benchmark of this instance
    */
   private int benchmarkValue;

   /**
    * the user to which should be delegate when this instance is new created
    */
   private transient IUser initialWorklistUser;

   /**
    * Reflects the existence of any {@link ActivityInstanceProperty} for this activity
    * instance.
    */
   private int propertiesAvailable;

   /**
    * Returns all activity instances, whose current performer equals
    * <tt>currentPerformer</tt>.
    */
   public static ActivityInstanceBean findByOID(long oid) throws ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_ACTIVITY_INSTANCE_OID.raise(0), 0);
      }
      ActivityInstanceBean result = (ActivityInstanceBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findByOID(ActivityInstanceBean.class, oid);
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_ACTIVITY_INSTANCE_OID.raise(oid), oid);
      }
      return result;
   }

   /**
    * Gets all activity instances instantiated on behalf of the process instance
    * <tt>processInstance</tt>.
    */
   public static Iterator<IActivityInstance> getAllForProcessInstance(
         IProcessInstance processInstance)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .getVector(
                  ActivityInstanceBean.class,
                  QueryExtension.where(Predicates.andTerm(
                        Predicates.isEqual(FR__PROCESS_INSTANCE, processInstance.getOID()),
                        Predicates.notEqual(FR__STATE, ActivityInstanceState.COMPLETED),
                        Predicates.notEqual(FR__STATE, ActivityInstanceState.ABORTED))))
            .iterator();
   }

   /**
    * Gets all activity instances instantiated on behalf of the process instance
    * <tt>processInstance</tt>.
    */
   public static IActivityInstance getDefaultGroupActivityInstance(IProcessInstance group)
   {
      if (group.getPersistenceController().isCreated())
      {
         BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
         return runtimeEnvironment.getCurrentActivityInstance();
      }
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      IActivity rootActivity = group.getProcessDefinition().getRootActivity();
      return session.findFirst(ActivityInstanceBean.class,
            QueryExtension.where(Predicates.andTerm(Predicates.isEqual(
                  FR__PROCESS_INSTANCE, group.getOID()), Predicates.isEqual(FR__ACTIVITY,
                  ModelManagerFactory.getCurrent().getRuntimeOid(rootActivity)))));
   }

   /**
    * Gets all completed activity instances instantiated on behalf of the process instance
    * <tt>processInstance</tt>.
    */
   public static Iterator getAllCompletedForProcessInstanceHierarchy(
         Collection processInstances)
   {
      Assert.condition( !processInstances.isEmpty(),
            "At least one process instance provided.");

      OrTerm processInstancePredicate = new OrTerm();
      for (Iterator i = processInstances.iterator(); i.hasNext();)
      {
         processInstancePredicate.add(Predicates.isEqual(FR__PROCESS_INSTANCE,
               ((IProcessInstance) i.next()).getOID()));
      }
      ComparisonTerm statePredicate = Predicates.isEqual(FR__STATE,
            ActivityInstanceState.COMPLETED);
      QueryExtension queryExtension = QueryExtension.where(Predicates.andTerm(
            processInstancePredicate, statePredicate));
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      return session.getVector(ActivityInstanceBean.class, queryExtension).iterator();
   }

   public static boolean existsForDepartment(long departmentOid)
   {
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ComparisonTerm predicate = Predicates.isEqual(FR__CURRENT_DEPARTMENT, departmentOid);
      QueryExtension queryExtension = QueryExtension.where(predicate);
      return session.exists(ActivityInstanceBean.class, queryExtension);
   }

   public static IUser getLastActivityPerformer(final long processInstanceOid)
   {
      IUser user = null;

      // we look up first in the session cache, since it's obvious that if we
      // completed any interactive activity in the current transaction, then we're the
      // "last activity performer".
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      Iterator<ActivityInstanceBean> cachedAIs = session.getSessionCacheIterator(ActivityInstanceBean.class, new FilterOperation<ActivityInstanceBean>()
      {
         @Override
         public org.eclipse.stardust.engine.core.persistence.Session.FilterOperation.FilterResult filter(
               ActivityInstanceBean ai)
         {
            return ai.isCompleted() && (ai.getProcessInstanceOID() == processInstanceOid)
                  ? Session.FilterOperation.FilterResult.ADD
                  : Session.FilterOperation.FilterResult.OMIT;
         }
      });
      Date stamp = null;
      while (cachedAIs.hasNext())
      {
         IActivityInstance ai = cachedAIs.next();
         Date last = ai.getLastModificationTime();
         if (stamp == null || !last.before(stamp))
         {
            IUser performed = ai.getPerformedBy();
            if (performed != null)
            {
               user = performed;
            }
         }
      }

      if (user == null)
      {
         // we haven't completed any interactive activity in the current transaction, so
         // start finding performers at last completed AI committed (CRNT-5888)
         QueryExtension query = QueryExtension.where(Predicates.andTerm(
               Predicates.isEqual(FR__PROCESS_INSTANCE, processInstanceOid),
               Predicates.isEqual(FR__STATE, ActivityInstanceState.COMPLETED)));
         query.addOrderBy(FR__LAST_MODIFICATION_TIME, false);
         ClosableIterator ais = session.getIterator(ActivityInstanceBean.class, query);
         try
         {
            while (user == null && ais.hasNext())
            {
               IActivityInstance ai = (IActivityInstance) ais.next();
               user = ai.getPerformedBy();
            }
         }
         finally
         {
            ais.close();
         }
      }
      return user;
   }

   /**
    * Constructor for persistence framework.
    */
   public ActivityInstanceBean()
   {
   }

   public ActivityInstanceBean(IActivity activity, IProcessInstance processInstance)
   {
      this.model = activity.getModel().getModelOID();
      this.activity = ModelManagerFactory.getCurrent().getRuntimeOid(activity);
      this.processInstance = (ProcessInstanceBean) processInstance;

      this.startTime = TimestampProviderUtils.getTimeStamp().getTime();
      this.lastModificationTime = startTime;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);

      setState(ActivityInstanceState.CREATED);
   }

   public ActivityInstanceBean(IActivity activity, IProcessInstance processInstance,
         IUser initialWorklistUser)
   {
      this(activity, processInstance);
      this.initialWorklistUser = initialWorklistUser;

   }

   /**
    * @return The text representation of this activity.
    */
   public String toString()
   {
      IActivity activity = getActivity();
      Assert.isNotNull(activity);

      ModelBean model = (ModelBean) activity.getModel();

      return "Activity instance '" + activity.getId() + "',  oid: " + getOID()
            + " (process instance = " + getProcessInstanceOID() + ") "
            + ModelUtils.getExtendedVersionString(model);
   }

   void setIndex(int index)
   {
      this.index = index;
   }

   int getIndex()
   {
      return index;
   }

   public ActivityInstanceState getOriginalState()
   {
      return originalState;
   }

   public EncodedPerformer getOriginalPerformer()
   {
      return originalPerformer;
   }

   /**
    * @return The state of the activity instance.
    */
   public ActivityInstanceState getState()
   {
      fetch();
      return ActivityInstanceState.getState(state);
   }

   public final void setState(int state)
   {
      if (getActivity().isInteractive())
      {
         setState(state, SecurityProperties.getUserOID());
      }
      else
      {
         setState(state, 0);
      }
   }

   /**
    * Sets the state of the activity instance.
    */
   public final void setState(int state, long workflowUserOid) throws IllegalStateChangeException
   {
      fetch();

      MonitoringUtils.activityInstanceMonitors()
            .activityInstanceStateChanged(this, state);

      if ((this.state == state) && (ActivityInstanceState.CREATED != state))
      {
         return;
      }

      if ((this.state == ActivityInstanceState.SUSPENDED || this.state == ActivityInstanceState.HIBERNATED)
            && state == ActivityInstanceState.COMPLETED)
      {
         throw new IllegalStateChangeException(this.toString(),
               ActivityInstanceState.Completed, this.getState());
      }
      if (isTerminated())
      {
         throw new IllegalStateChangeException(this.toString(),
               ActivityInstanceState.getState(state), this.getState());
      }
      if (this.state == ActivityInstanceState.ABORTING
            && state != ActivityInstanceState.ABORTED)
      {
         throw new IllegalStateChangeException(this.toString(),
               ActivityInstanceState.getState(state), this.getState());
      }
      ProcessInstanceState piState = getProcessInstance().getState();

      if (ProcessInstanceUtils.isInAbortingPiHierarchy(getProcessInstance())
            && !(state == ActivityInstanceState.ABORTED || state == ActivityInstanceState.ABORTING))
      {
         if (piState.equals(ProcessInstanceState.Aborted))
         {
            StringBuilder msg = new StringBuilder();
            msg.append("The process of activity instance (oid '")
                  .append(oid)
                  .append(
                        "') is in state 'Aborted'. Hence the state of the activity had to be changed to 'Aborted'.");

            trace.warn(msg.toString());
            state = ActivityInstanceState.ABORTED;
         }
         else
         {
            // reschedule aborting
            ProcessAbortionJanitor.scheduleJanitor(new AbortionJanitorCarrier(
                  getProcessInstanceOID(), workflowUserOid), false);

            ActivityInstanceState newState = ActivityInstanceState.getState(state);
            StringBuffer msg = new StringBuffer("Invalid state change from ");
            msg.append(ActivityInstanceState.getState(this.state)).append(" to ")
                  .append(newState);
            msg.append(" because the process instance is ");
            msg.append("in process of aborting.");

            trace.error(msg.toString());
            throw new IllegalStateChangeException(this.toString(),
                  ActivityInstanceState.getState(state), this.getState(), piState);
         }
      }

      /*boolean inHaltingPiHierarchy = ProcessInstanceUtils.isInHaltingPiHierarchy(getProcessInstance());
      if (inHaltingPiHierarchy
            && ActivityInstanceUtils.isHaltable(this)
            && !(state == ActivityInstanceState.HALTED))
      {
         // Acquire lock on root to avoid conflict with halt or resume.
         try
         {
            getProcessInstance().getRootProcessInstance().lock();

            // reload state after lock was obtained
            ProcessInstanceState originalPiState = piState;
            try
            {
               if (!processInstance.getPersistenceController().isLocked())
               {
                  processInstance.reloadAttribute(ProcessInstanceBean.FIELD__STATE);
                  piState = processInstance.getState();
               }
            }
            catch (PhantomException e)
            {
               // (fh) don't know what else to do
               throw new InternalException(e);
            }
            finally
            {
               if (piState != originalPiState)
               {
                  processInstance.restoreState(originalPiState);
                  trace.debug("Using current process instance state '"
                        + piState + "' instead of the cached state '"
                        + originalPiState + "'.");
               }
            }
         }
         catch (ConcurrencyException e)
         {
            trace.warn("Could not obtain lock on root process instance (oid '"
                  + this.processInstance.getRootProcessInstanceOID()
                  + "'). This might be due to currently running Halt or Resume action.");
            throw e;
         }

         if (piState.equals(ProcessInstanceState.Halted) || piState.equals(ProcessInstanceState.Halting))
         {
            StringBuilder msg = new StringBuilder();
            msg.append("The process of activity instance (oid '")
                  .append(oid)
                  .append("') is in state '")
                  .append(piState)
                  .append("'. Hence the state of the activity had to be changed to 'Halted'.");

            trace.warn(msg.toString());
            state = ActivityInstanceState.HALTED;
         }
      }*/

      recordHistoricState(workflowUserOid);

      int oldState = this.state;
      markModified(FIELD__STATE);
      if (null == this.originalState)
      {
         // original state is needed to keep workitem table in sync
         this.originalState = ActivityInstanceState.getState(oldState);
      }
      this.lastModifyingUser = workflowUserOid;
      this.state = state;

      if (trace.isInfoEnabled())
      {
         trace.info("State change for " + this + ": "
               + ActivityInstanceState.getString(oldState) + "-->"
               + ActivityInstanceState.getString(state) + ".");
      }

      if (getActivity().hasEventHandlers(
            PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION))
      {
         Event event = new Event(Event.ACTIVITY_INSTANCE, getOID(), Event.OID_UNDEFINED,
               Event.OID_UNDEFINED, Event.ENGINE_EVENT);
         event.setAttribute(PredefinedConstants.SOURCE_STATE_ATT,
               ActivityInstanceState.getState(oldState));
         event.setAttribute(PredefinedConstants.TARGET_STATE_ATT,
               ActivityInstanceState.getState(state));

         EventUtils.processAutomaticEvent(getActivity(),
               PredefinedConstants.ACTIVITY_STATECHANGE_CONDITION, event);
      }

      if ((ActivityInstanceState.HIBERNATED == state)
            && getActivity().hasEventHandlers(PredefinedConstants.SIGNAL_CONDITION))
      {
         // TODO cleanup event
         Event event = new Event(Event.ACTIVITY_INSTANCE, getOID(), Event.OID_UNDEFINED,
               Event.OID_UNDEFINED, Event.ENGINE_EVENT);
         event.setAttribute(PredefinedConstants.SOURCE_STATE_ATT,
               ActivityInstanceState.getState(oldState));
         event.setAttribute(PredefinedConstants.TARGET_STATE_ATT,
               ActivityInstanceState.getState(state));

         EventUtils.processAutomaticEvent(getActivity(),
               PredefinedConstants.SIGNAL_CONDITION, event);
      }

      /*
      // TODO: (fh) move to ActivityThread
      if (inHaltingPiHierarchy && ActivityInstanceUtils.isHaltable(this))
      {
         // is haltable after state change of this ActivityInstance.
         if (state == ActivityInstanceState.SUSPENDED)
         {
            // halt activity instance directly after suspend.
            this.setState(ActivityInstanceState.HALTED, workflowUserOid);
         }
         // is haltable after state change of this ActivityInstance.
         if (state == ActivityInstanceState.INTERRUPTED)
         {
            // halt activity instance directly after interruption.
            this.setState(ActivityInstanceState.HALTED, workflowUserOid);
         }

         if (piState.equals(ProcessInstanceState.Halting))
         {
            // reshedule halting for process
            ProcessHaltJanitor.schedule(getProcessInstanceOID(), workflowUserOid);
         }
      }*/
   }

   public Date getStartTime()
   {
      fetch();
      return new Date(startTime);
   }

   public Date getLastModificationTime()
   {
      fetch();
      return new Date(lastModificationTime);
   }

   private void updateModificationTime(long modificationTime)
   {
      markModified(FIELD__LAST_MODIFICATION_TIME);
      this.lastModificationTime = modificationTime;
   }

   // Add for criticality
   public double getCriticality()
   {
      fetch();
      return criticality;
   }

   public void updateCriticality(double criticality)
   {
      markModified(FIELD__CRITICALITY);
      this.criticality = criticality;
   }

   public int getBenchmarkValue()
   {
      fetch();
      return benchmarkValue;
   }

   public void setBenchmarkValue(int benchmarkValue)
   {
      markModified(FIELD__BENCHMAKRK_VALUE);
      this.benchmarkValue = benchmarkValue;
   }
   public boolean isDefaultCaseActivityInstance()
   {
      fetch();

      IActivity result = ModelManagerFactory.getCurrent().findActivity(model, activity);
      if (null == result)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_ACTIVITY_FOR_AI.raise(activity, getOID()),
               activity);
      }
      return PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID.equals(result.getId())
            && getProcessInstance().isCaseProcessInstance();
   }

   public IActivity getActivity()
   {
      fetch();
      IActivity result = ModelManagerFactory.getCurrent().findActivity(model, activity);
      if (null == result)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_ACTIVITY_FOR_AI.raise(activity, getOID()),
               activity);
      }

      // if qc instance return a decorated activity that will change the default performer
      if (QualityAssuranceUtils.isQualityAssuranceInstance(result,
            getQualityAssuranceState()))
      {
         return new QualityAssuranceActivityBean(result);
      }

      return getProcessInstance().isCaseProcessInstance()
            ? groupActivity(result)
            : result;
   }

   private IActivity groupActivity(final IActivity groupActivity)
   {
      if (groupActivity == null)
      {
         return null;
      }
      return (IActivity) Proxy.newProxyInstance(Thread.currentThread()
            .getContextClassLoader(), new Class< ? >[] {IActivity.class},
            new InvocationHandler()
            {
               public Object invoke(Object proxy, Method method, Object[] args)
                     throws Throwable
               {
                  if (method.getName().equals("getPerformer"))
                  {
                     return groupPerformer(groupActivity.getPerformer());
                  }
                  return method.invoke(groupActivity, args);
               }
            });
   }

   protected IModelParticipant groupPerformer(final IModelParticipant groupPerformer)
   {
      if (groupPerformer == null
            || !PredefinedConstants.CASE_PERFORMER_ID.equals(groupPerformer.getId()))
      {
         return null;
      }
      return (IModelParticipant) Proxy.newProxyInstance(Thread.currentThread()
            .getContextClassLoader(), new Class< ? >[] {IModelParticipant.class},
            new InvocationHandler()
            {
               public Object invoke(Object proxy, Method method, Object[] args)
                     throws Throwable
               {
                  if (method.getName().equals("isAuthorized"))
                  {
                     return true;
                  }
                  return method.invoke(groupPerformer, args);
               }
            });
   }

   public IProcessInstance getProcessInstance()
   {
      fetchLink(FIELD__PROCESS_INSTANCE);
      return processInstance;
   }

   public IParticipant getPerformer()
   {
      IParticipant performer = getCurrentUserPerformer();

      if (null == performer)
      {
         performer = getCurrentPerformer();
      }

      return performer;
   }

   public IUser getCurrentUserPerformer()
   {
      fetch();

      if (currentUserPerformer == 0)
      {
         return null;
      }

      return UserBean.findByOid(currentUserPerformer);
   }

   public long getCurrentUserPerformerOID()
   {
      fetch();
      return currentUserPerformer;
   }

   public IParticipant getCurrentPerformer()
   {
      fetch();

      if (currentPerformer > 0)
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         IModelParticipant participant = modelManager.findModelParticipant(model,
               currentPerformer);
         if (participant == null)
         {
            participant = modelManager.findModelParticipant(
                  PredefinedConstants.ANY_MODEL, currentPerformer);
         }
         return participant;
      }
      else if (currentPerformer < 0)
      {
         return UserGroupBean.findByOid( -currentPerformer);
      }

      return null;
   }

   public long getCurrentPerformerOID()
   {
      fetch();
      return currentPerformer;
   }

   /**
    * Retrieves the user, the activity instance is performed by.
    */
   public IUser getPerformedBy()
   {
      fetch();

      if (performedBy == 0)
      {
         return null;
      }

      IUser user = (IUser) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findByOID(UserBean.class, performedBy);
      if (user == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_OID.raise(performedBy), getOID());
      }

      return user;
   }

   public IDepartment getCurrentDepartment()
   {
      fetch();
      if (0 != currentDepartment)
      {
         return DepartmentBean.findByOID(currentDepartment);
      }

      return null;
   }

   public long getCurrentDepartmentOid()
   {
      fetch();
      return currentDepartment;
   }

   public boolean isCompleted()
   {
      return ActivityInstanceState.Completed == getState();

   }

   public boolean isTerminated()
   {
      ActivityInstanceState state = getState();
      return (ActivityInstanceState.Completed == state)
            || (ActivityInstanceState.Aborted == state);
   }

   public boolean isHalted()
   {
      return ActivityInstanceState.Halted == getState();
   }

   public boolean isHibernated()
   {
      return ActivityInstanceState.Hibernated == getState();
   }

   public boolean isAborting()
   {
      return ActivityInstanceState.Aborting == getState();
   }

   private void putToUserWorklist(IUser user)
   {
      if ( !user.isValid()
            && !Parameters.instance().getBoolean(
                  KernelTweakingProperties.ASSIGN_TO_INVALID_USER, false))
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.AUTHx_USER_NOT_VALID.raise(user.getId(), user.getOID()));
      }

      fetch();

      if (user.getOID() != this.currentUserPerformer)
      {
         final long oldUserPerformer = currentUserPerformer;
         final long oldPerformer = currentPerformer;
         final long oldDepartment = currentDepartment;

         recordHistoricState();
         recordInitialPerformer();

         try
         {
            // marking fields as modified is deferred to finally block
            this.currentUserPerformer = user.getOID();
            this.currentPerformer = 0;
            this.currentDepartment = 0;

            if (getActivity().hasEventHandlers(
                  PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
            {
               Event event = new Event(Event.ACTIVITY_INSTANCE, getOID(), Event.OID_UNDEFINED, Event.OID_UNDEFINED,
                     Event.ENGINE_EVENT);
               event.setAttribute(PredefinedConstants.SOURCE_USER_ATT,
                     Long.valueOf(oldUserPerformer));
               event.setAttribute(PredefinedConstants.TARGET_USER_ATT,
                     Long.valueOf(user.getOID()));

               Event handledEvent = EventUtils.processAutomaticEvent(getActivity(),
                     PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, event);

               if (null != handledEvent.getIntendedState())
               {
                  try
                  {
                     setState(handledEvent.getIntendedState().getValue());
                  }
                  catch (IllegalStateChangeException e)
                  {
                     AuditTrailLogger.getInstance(LogCode.EVENT, this).error(
                           "Skipping " + "illegal state change requested during event "
                                 + "action processing", e);
                  }
               }
            }
         }
         catch (PublicException e)
         {
            // rollback to pre-assignment state, i.e. in case of a 4eyes assertion

            recordHistoricState();

            this.currentUserPerformer = oldUserPerformer;
            this.currentPerformer = oldPerformer;
            this.currentDepartment = oldDepartment;
            throw e;
         }
         finally
         {
            // now flag the actual modifications so they will be persisted
            if (currentUserPerformer != oldUserPerformer)
            {
               markModified(FIELD__CURRENT_USER_PERFORMER);
            }
            if (currentPerformer != oldPerformer)
            {
               markModified(FIELD__CURRENT_PERFORMER);
            }
            if (currentDepartment != oldDepartment)
            {
               markModified(FIELD__CURRENT_DEPARTMENT);
            }
         }

         trace.info("Performer of " + this + " set to " + user + ".");
      }
   }

   private void putToUserGroupWorklist(IUserGroup userGroup)
   {
      fetch();

      recordHistoricState();
      recordInitialPerformer();

      if (0 != currentUserPerformer)
      {
         markModified(FIELD__CURRENT_USER_PERFORMER);
         this.currentUserPerformer = 0;
      }

      if (0 != currentDepartment)
      {
         markModified(FIELD__CURRENT_DEPARTMENT);
         this.currentDepartment = 0;
      }

      markModified(FIELD__CURRENT_PERFORMER);
      this.currentPerformer = -1 * userGroup.getOID();

      trace.info("Performer of " + this + " set to " + userGroup + ".");
   }

   private void putToParticipantWorklist(IModelParticipant participant, long departmentOid)
   {
      fetch();

      if ((ModelManagerFactory.getCurrent().getRuntimeOid(participant) != this.currentPerformer)
            || (departmentOid != this.currentDepartment))
      {

         final long oldUserPerformer = currentUserPerformer;
         final long oldPerformer = currentPerformer;
         final long oldDepartment = currentDepartment;

         recordHistoricState();
         recordInitialPerformer();

         try
         {
            // marking fields as modified is deferred to finally block
            this.currentUserPerformer = 0;
            this.currentPerformer = ModelManagerFactory.getCurrent().getRuntimeOid(
                  participant);
            this.currentDepartment = departmentOid;

            if (state != ActivityInstanceState.CREATED
                  && getActivity().hasEventHandlers(
                        PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
            {
               Event event = new Event(Event.ACTIVITY_INSTANCE, getOID(),
                     Event.OID_UNDEFINED, Event.OID_UNDEFINED, Event.ENGINE_EVENT);
               event.setAttribute(PredefinedConstants.SOURCE_USER_ATT,
                     Long.valueOf(oldUserPerformer));
               event.setAttribute(PredefinedConstants.TARGET_USER_ATT,
                     Long.valueOf(participant.getOID()));

               Event handledEvent = EventUtils.processAutomaticEvent(getActivity(),
                     PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, event);

               if (null != handledEvent.getIntendedState())
               {
                  try
                  {
                     setState(handledEvent.getIntendedState().getValue());
                  }
                  catch (IllegalStateChangeException e)
                  {
                     AuditTrailLogger.getInstance(LogCode.EVENT, this).error(
                           "Skipping " + "illegal state change requested during event "
                                 + "action processing", e);
                  }
               }
            }
         }
         catch (PublicException e)
         {
            // rollback to pre-assignment state, i.e. in case of a 4eyes assertion

            recordHistoricState();

            this.currentUserPerformer = oldUserPerformer;
            this.currentPerformer = oldPerformer;
            this.currentDepartment = oldDepartment;
            throw e;
         }
         finally
         {
            // now flag the actual modifications so they will be persisted
            if (currentUserPerformer != oldUserPerformer)
            {
               markModified(FIELD__CURRENT_USER_PERFORMER);
            }
            if (currentPerformer != oldPerformer)
            {
               markModified(FIELD__CURRENT_PERFORMER);
            }
            if (currentDepartment != oldDepartment)
            {
               markModified(FIELD__CURRENT_DEPARTMENT);
            }
         }
         if (departmentOid > 0)
         {
            trace.info("Performer of " + this + " set to " + participant + "["
                  + departmentOid + "].");
         }
         else
         {
            trace.info("Performer of " + this + " set to " + participant + ".");
         }
      }
   }

   public long getProcessInstanceOID()
   {
      fetch();

      if (null != processInstance)
      {
         return getProcessInstance().getOID();
      }
      else if (isPersistent())
      {
         DefaultPersistenceController controller = (DefaultPersistenceController) getPersistenceController();

         return ((Long) controller.getLinkFk(FIELD__PROCESS_INSTANCE)).longValue();
      }
      else
      {
         return 0;
      }
   }

   public void removeFromWorklists()
   {
      fetch();

      if ((0 != currentUserPerformer) || (0 != currentPerformer))
      {
         trace.info("Remove from worklists: " + this);

         recordHistoricState();
         recordInitialPerformer();

         // Cases need to keep case owner.
         if ( !isDefaultCaseActivityInstance())
         {
            if (0 != currentUserPerformer)
            {
               markModified(FIELD__CURRENT_USER_PERFORMER);
               this.currentUserPerformer = 0;
            }
            if (0 != currentPerformer)
            {
               markModified(FIELD__CURRENT_PERFORMER);
               this.currentPerformer = 0;
            }
            if (0 != currentDepartment)
            {
               markModified(FIELD__CURRENT_DEPARTMENT);
               this.currentDepartment = 0;
            }
         }
      }
   }

   public boolean isIntrinsicOutAccessPoint(String id)
   {
      return PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT.equals(id);
   }

   public Map getIntrinsicOutAccessPointValues()
   {
      return Collections.singletonMap(PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT,
            new ActivityInstanceDetails(this));
   }

   public void start()
   {
      IActivity activity = getActivity();

      doBindAutomaticlyBoundEvents(activity);

      // binding events may cause any state change, watch out
      if ((ActivityInstanceState.Created.equals(getState()))
            || ActivityInstanceState.Interrupted.equals(getState()))
      {
         // support polymorphism wrt. activity definition
         IActivityExecutionStrategy aeStrategy = null;
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         if (rtEnv.getExecutionPlan() == null) // force default if an execution plan is
                                               // present
         {
            aeStrategy = ActivityExecutionUtils.getExecutionStrategy(activity);
         }
         if (aeStrategy == null)
         {
            doStartActivity(activity);
         }
         else
         {
            aeStrategy.startActivityInstance(this);
         }
      }
   }

   public void doStartActivity(IActivity activity) throws IllegalStateChangeException
   {
      fetch();
      if (activity.isInteractive())
      {
         if ((0 == currentPerformer) && (0 == currentUserPerformer))
         {
            if (initialWorklistUser == null)
            {
               putToDefaultWorklist();
            }
            else
            {
               putToUserWorklist(initialWorklistUser);
            }

            ActivityThread.getCurrentActivityThreadContext().handleWorklistItem(this);
         }
      }

      if (activity.isHibernateOnCreation())
      {
         setState(ActivityInstanceState.HIBERNATED);
      }
      else if (activity.isInteractive())
      {
         setState(ActivityInstanceState.SUSPENDED);
      }
      else
      {
         invoke(activity);
      }
   }

   private void invoke(IActivity activity)
   {
      ImplementationType type = activity.getImplementationType();

      if (type == ImplementationType.Application)
      {
         invokeApplication(activity);
      }
      else if (type == ImplementationType.SubProcess)
      {
         invokeSubprocess(activity);
      }
      else if (type == ImplementationType.Route)
      {
         invokeRoute(activity);
      }
   }

   private void invokeRoute(IActivity activity)
   {
      setState(ActivityInstanceState.APPLICATION);
      try
      {
         try
         {
            Map apValues;
            try
            {
               apValues = processRouteInDataMappings(activity);
            }
            catch (PublicException e)
            {
               throw new InvocationTargetException(e,
                     "Failed processing IN data mappings.");
            }

            try
            {
               processRouteOutDataMappings(activity, apValues);
            }
            catch (PublicException e)
            {
               throw new InvocationTargetException(e,
                     "Failed processing OUT data mappings.");
            }
         }
         catch (InvocationTargetException e)
         {
            processException(e.getTargetException());
         }
      }
      catch (Throwable e)
      {
         LogUtils.traceException(e, false);
         AuditTrailLogger.getInstance(LogCode.ENGINE, this).warn(e);

         throw new NonInteractiveApplicationException(
               "Exception occured for route activity " + getOID() + ". Message was: "
                     + e.getMessage(), e);
      }
   }

   private void invokeSubprocess(IActivity activity)
   {
      final SubProcessModeKey subProcessMode = activity.getSubProcessMode();
      final boolean synchronous = !SubProcessModeKey.ASYNC_SEPARATE.equals(subProcessMode);
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      ExecutionPlan plan = rtEnv.getExecutionPlan();
      if (plan != null && !synchronous)
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_ADHOC_ASYNC_START_ACTIVITY_THREAD.raise());
      }

      setState(ActivityInstanceState.SUSPENDED);

      final boolean copyAllData = ActivityBean.getCopyAllDataAttribute(activity);
      final boolean separateData = SubProcessModeKey.ASYNC_SEPARATE.equals(subProcessMode)
            || SubProcessModeKey.SYNC_SEPARATE.equals(subProcessMode);

      IProcessInstance subProcess;
      try
      {
         if (synchronous)
         {
            subProcess = doWithRetry(10, 500, new Callable<IProcessInstance>()
            {
               public IProcessInstance call() throws Exception
               {
                  ProcessInstanceBean instance = ProcessInstanceBean.createInstance(
                        getActivity().getImplementationProcessDefinition(),
                        ActivityInstanceBean.this, SecurityProperties.getUser(),
                        Collections.EMPTY_MAP, true);

                  if (BenchmarkUtils.isBenchmarkedPI(getProcessInstance()))
                  {
                     instance.setBenchmark(getProcessInstance().getBenchmark());
                  }

                  return instance;
               }
            });
         }
         else
         {
            subProcess = ProcessInstanceBean.createInstance(
                  getActivity().getImplementationProcessDefinition(),
                  SecurityProperties.getUser(), Collections.EMPTY_MAP, true);

            if (BenchmarkUtils.isBenchmarkedPI(getProcessInstance()))
            {
               subProcess.setBenchmark(getProcessInstance().getBenchmark());
            }

            if (ActivityInstanceUtils.isTransientExecutionScenario(this))
            {
               if (subProcess.getAuditTrailPersistence() == AuditTrailPersistence.ENGINE_DEFAULT)
               {
                  subProcess.setAuditTrailPersistence(getProcessInstance().getAuditTrailPersistence());
               }
            }
         }

         if (separateData && copyAllData)
         {
            for (Iterator<?> iterator = getProcessInstance().getAllDataValues(); iterator.hasNext();)
            {
               IDataValue srcValue = (IDataValue) iterator.next();

               IData srcData = srcValue.getData();

               if (!RootPIUtils.isRootProcessAttachmentAttributeEnabled(srcData.getId(), getProcessInstance()))
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Data value '" + srcData.getId() + "' retrieved.");
                  }

                  // DataValueBean.copyDataValue(subProcess, srcValue);

                  IModel targetModel = (IModel) subProcess.getProcessDefinition().getModel();
                  // we copy only data that exists in the target model
                  if (srcData == targetModel.findData(srcData.getId()))
                  {
                     subProcess.setOutDataValue(srcData, "", srcValue.getSerializedValue());
                  }
                  else
                  {
                     trace.warn(BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(srcData.getId()).toString());
                  }
               }
            }
            if (!synchronous && RootPIUtils.isRootProcessAttachmentAttributeEnabled(DmsConstants.DATA_ID_ATTACHMENTS, getProcessInstance()))
            {
               // copy process attachments from root for async subprocess
               IProcessInstance rootProcessInstance = getProcessInstance().getRootProcessInstance();
               IModel rootModel = (IModel) rootProcessInstance.getProcessDefinition().getModel();
               IData rootAttachments = rootModel.findData(DmsConstants.DATA_ID_ATTACHMENTS);
               if (rootAttachments != null)
               {
                  IDataValue rootDataValue = rootProcessInstance.getDataValue(rootAttachments);
                  IModel targetModel = (IModel) subProcess.getProcessDefinition().getModel();
                  // we copy only data that exists in the target model
                  IData targetAttachments = targetModel.findData(DmsConstants.DATA_ID_ATTACHMENTS);
                  if (targetAttachments != null)
                  {
                     subProcess.setOutDataValue(targetAttachments, "",
                           rootDataValue.getSerializedValue());
                  }

                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Data value '" + targetAttachments.getId()
                           + "' retrieved from root.");
                  }
               }
            }
         }
         processSubProcessInDataMappings(activity, subProcess);
      }
      catch (RuntimeException e)
      {
         // probably no process created, enable recovery
         setState(ActivityInstanceState.INTERRUPTED);
         throw e;
      }

      ((ProcessInstanceBean) subProcess).doBindAutomaticlyBoundEvents();

      if (plan != null && plan.hasNextActivity())
      {
         if (plan.nextStep())
         {
            ActivityThread.schedule(subProcess, plan.getCurrentStep(), null, true, null,
                  Collections.EMPTY_MAP, false);
         }
         else
         {
            ActivityThread.schedule(subProcess, plan.getTargetActivity(), null, true,
                  null, Collections.EMPTY_MAP, false);
         }
      }
      else
      {
         ActivityThread.schedule(subProcess, subProcess.getProcessDefinition()
               .getRootActivity(), null, synchronous, null, Collections.EMPTY_MAP,
               synchronous);
      }
      if ( !synchronous || subProcess.isCompleted())
      {
         setState(ActivityInstanceState.APPLICATION);
      }
      if ( !synchronous && isSerialExecutionScenario(subProcess))
      {
         ProcessInstanceUtils.scheduleSerialActivityThreadWorkerIfNecessary(subProcess);
      }
   }

   private IProcessInstance doWithRetry(int retries, int wait,
         Callable<IProcessInstance> callable)
   {
      int trys = 0;

      // will try to execute the callable and re-throw the exception after x retries fail.
      while (true)
      {
         try
         {
            return callable.call();
         }
         catch (Exception e)
         {
            if (trys >= retries)
            {
               throw (RuntimeException) e;
            }
            else
            {
               trys++ ;
               trace.warn("Subprocess creation failed. Try " + trys + " of " + retries
                     + " Exception was " + e.getMessage());
               // wait and retry
               try
               {
                  Thread.sleep(wait);
               }
               catch (InterruptedException e1)
               {
               }
            }
         }
      }
   }

   private void invokeApplication(IActivity activity)
   {
      IApplication application = activity.getApplication();
      IApplicationType applicationType = (IApplicationType) application.getType();

      if (application.isSynchronous())
      {
         setState(ActivityInstanceState.APPLICATION);
         SynchronousApplicationInstance applicationInstance = null;
         try
         {
            applicationInstance = (SynchronousApplicationInstance) createApplicationInstance(applicationType);
            invokeApplication(activity, applicationInstance);
         }
         catch (TransactionFreezedException e)
         {
            throw e;
         }
         catch (Throwable e)
         {
            LogUtils.traceException(e, false);

            if ( !TransactionUtils.isCurrentTxRollbackOnly())
            {
               // do not try to log to audit trail if TX is marked for rollback
               // already to prevent SQLExceptions being raised
               AuditTrailLogger.getInstance(LogCode.ENGINE, this).warn(e);
            }

            throw new NonInteractiveApplicationException(
                  "Exception occured for noninteractive activity " + getOID()
                        + ". Message was: " + e.getMessage(), e);
         }
         finally
         {
            if (applicationInstance != null)
            {
               applicationInstance.cleanup();
            }
         }
      }
      else
      {
         AsynchronousApplicationInstance applicationInstance = null;
         try
         {
            applicationInstance = (AsynchronousApplicationInstance) createApplicationInstance(applicationType);
            setState(ActivityInstanceState.APPLICATION);
            if (applicationInstance.isSending())
            {
               invokeAsynchronously(activity, applicationInstance);
            }
            if (applicationInstance.isReceiving()
                  && ActivityInstanceState.Application.equals(getState()))
            {
               setState(ActivityInstanceState.HIBERNATED);
            }
         }
         catch (TransactionFreezedException e)
         {
            throw e;
         }
         catch (Throwable e)
         {

            LogUtils.traceException(e, false);
            AuditTrailLogger.getInstance(LogCode.ENGINE, this).warn(e);

            throw new NonInteractiveApplicationException(
                  "Exception occured for noninteractive activity " + getOID()
                        + ". Message was: " + e.getMessage(), e);
         }
         finally
         {
            if (applicationInstance != null)
            {
               applicationInstance.cleanup();
            }
         }
      }
   }

   private void doBindAutomaticlyBoundEvents(IActivity activity)
   {
      for (int i = 0; i < activity.getEventHandlers().size(); ++i)
      {
         IEventHandler handler = (IEventHandler) activity.getEventHandlers().get(i);

         if (handler.isAutoBind()
               && EventType.Pull.equals(((IEventConditionType) handler.getType()).getImplementation()))
         {
            bind(handler, new EventHandlerBindingDetails(handler));
         }
      }
   }

   private ApplicationInstance createApplicationInstance(IApplicationType applicationType)
   {
      try
      {
         String instanceType = applicationType.getStringAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT);
         ApplicationInstance result = SpiUtils.createApplicationInstance(instanceType);
         result.bootstrap(new LazilyLoadingActivityInstanceDetails(this));
         return result;
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   private void putToDefaultWorklist() throws PublicException
   {
      IActivity activity = getActivity();

      IModelParticipant performer = activity.getPerformer();
      if (performer == null)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_NON_INTERACTIVE_AI_CAN_NOT_BE_DELEGATED
                     .raise(getOID(), getActivity().getId()));
      }

      if (performer instanceof IConditionalPerformer)
      {
         IConditionalPerformer conditionalPerformer = (IConditionalPerformer) performer;
         IParticipant actualPerformer = conditionalPerformer.retrievePerformer(getProcessInstance());

         if (actualPerformer instanceof IUser)
         {
            putToUserWorklist((IUser) actualPerformer);
         }
         else if (actualPerformer instanceof IUserGroup)
         {
            putToUserGroupWorklist((IUserGroup) actualPerformer);
         }
         else if (actualPerformer instanceof IModelParticipant)
         {
            IModelParticipant participant = (IModelParticipant) actualPerformer;
            IDepartment targetDepartment = getTargetDepartment(participant, null);
            putToParticipantWorklist(participant, targetDepartment == null
                  ? 0
                  : targetDepartment.getOID());
         }
      }
      else
      {
         IDepartment targetDepartment = getTargetDepartment(performer, null);
         putToParticipantWorklist(performer, targetDepartment == null
               ? 0
               : targetDepartment.getOID());
      }
   }

   private IDepartment getTargetDepartment(IModelParticipant participant,
         IDepartment department)
   {
      List<IOrganization> restrictions = Authorization2.findRestricted(participant);
      if (department != IDepartment.NULL
            && (department == null || department.getOID() == 0)
            && !restrictions.isEmpty())
      {
         AuthorizationContext context = AuthorizationContext.create((ClientPermission) null);
         context.setActivityInstance(this);
         try
         {
            department = DepartmentBean.findByOID(Authorization2.getTargetDepartmentOid(
                  context, restrictions, true));
         }
         catch (Exception ex)
         {
            // defaults to 0
         }
      }
      return department;
   }

   /**
    * Call a synchronous application instance
    *
    * @param activity
    * @param applicationInstance
    */
   public void invokeApplication(IActivity activity,
         SynchronousApplicationInstance applicationInstance) throws Throwable
   {
      try
      {
         try
         {
            processInDataMappings(activity, applicationInstance);
         }
         catch (PublicException e)
         {
            throw new InvocationTargetException(e, "Failed processing IN data mappings.");
         }

         Map outAccessPointValues = retrySynchronousApplication(activity,
               applicationInstance);

         processOutDataMappings(outAccessPointValues);

      }
      catch (InvocationTargetException e)
      {
         processException(e.getTargetException());
      }
   }

   private Map retrySynchronousApplication(IActivity activity,
         SynchronousApplicationInstance applicationInstance)
         throws InvocationTargetException
   {
      int number = 0;
      int time = 5;
      boolean retry = false;

      IApplication application = activity.getApplication();
      if (application != null)
      {
         Boolean retryAttribute = (Boolean) application.getAttribute(PredefinedConstants.SYNCHRONOUS_APPLICATION_RETRY_ENABLE);
         if (retryAttribute != null)
         {
            try
            {
               retry = retryAttribute.booleanValue();
            }
            catch (Exception e)
            {
            }
         }

         String reponsibilityAttribute = (String) application.getAttribute(PredefinedConstants.SYNCHRONOUS_APPLICATION_RETRY_RESPONSIBILITY);
         if(reponsibilityAttribute != null && reponsibilityAttribute.equals(PredefinedConstants.APPLICATION_CONTEXT))
         {
            // retry will be handled by application
            retry = false;
         }

         // if retry is enabled, check the other values
         if (retry)
         {
            String numberAttribute = (String) application.getAttribute(PredefinedConstants.SYNCHRONOUS_APPLICATION_RETRY_NUMBER);
            if (numberAttribute != null)
            {
               try
               {
                  number = Integer.parseInt(numberAttribute);
               }
               catch (NumberFormatException e)
               {
               }
            }
            String timeAttribute = (String) application.getAttribute(PredefinedConstants.SYNCHRONOUS_APPLICATION_RETRY_TIME);
            if (timeAttribute != null)
            {
               try
               {
                  time = Integer.parseInt(timeAttribute);
               }
               catch (NumberFormatException e)
               {
               }
            }
         }
      }

      // in this case we will not retry
      if (TransactionUtils.isCurrentTxRollbackOnly())
      {
         number = 0;
      }

      while (number > -1)
      {
         number-- ;
         try
         {
            return applicationInstance.invoke(activity.getApplicationOutDataMappingAccessPoints());
         }
         catch (InvocationTargetException e)
         {
            if (number > -1)
            {
               trace.warn("Unexpected exception : " + e.getMessage() + ".");
               trace.warn("Retrying " + (number + 1) + ((0 < number) ? " times." : " time."));

               try
               {
                  Thread.sleep(time * 1000);
               }
               catch (InterruptedException e1)
               {
               }
            }
            else
            {
               throw e;
            }
         }
      }

      // never reached
      return null;
   }

   /**
    * Execute the sending part of an asynchronous application instance.
    *
    * @throws Throwable
    */
   private void invokeAsynchronously(IActivity activity,
         AsynchronousApplicationInstance applicationInstance) throws Throwable
   {
      try
      {
         try
         {
            processInDataMappings(activity, applicationInstance);
         }
         catch (PublicException e)
         {
            throw new InvocationTargetException(e, "Failed processing IN data mappings.");
         }

         applicationInstance.send();
      }
      catch (InvocationTargetException e)
      {
         processException(e.getTargetException());
      }
   }

   private void processInDataMappings(IActivity activity,
         ApplicationInstance applicationInstance)
   {
      // setting default values stored in the access points itself

      IApplication application = activity.getApplication();
      Iterator inAccessPoints = application.getAllInAccessPoints();
      while (inAccessPoints.hasNext())
      {
         AccessPoint accessPoint = (AccessPoint) inAccessPoints.next();
         ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(
               accessPoint, null);
         AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
               processInstance, null);
         Object defaultValue = evaluator.createDefaultValue(accessPoint,
               evaluationContext);

         if (defaultValue != null)
         {
            applicationInstance.setInAccessPointValue(accessPoint.getId(), defaultValue);
         }
      }

      String inputParameterId = null;
      String counterParameterId = null;
      ILoopCharacteristics loop = activity.getLoopCharacteristics();
      if (loop instanceof IMultiInstanceLoopCharacteristics)
      {
         counterParameterId = ((IMultiInstanceLoopCharacteristics) loop).getCounterParameterId();
         if (counterParameterId != null)
         {
            int indexOf = counterParameterId.indexOf(':');
            String counterParameterContext = counterParameterId.substring(0, indexOf);
            if (counterParameterContext.equals(PredefinedConstants.APPLICATION_CONTEXT))
            {
               counterParameterId = counterParameterId.substring(indexOf + 1);
               applicationInstance.setInAccessPointValue(counterParameterId, index);
            }
         }
         inputParameterId = ((IMultiInstanceLoopCharacteristics) loop).getInputParameterId();
         if (inputParameterId != null)
         {
            int indexOf = inputParameterId.indexOf(':');
            String inputParameterContext = inputParameterId.substring(0, indexOf);
            if (inputParameterContext.equals(PredefinedConstants.APPLICATION_CONTEXT))
            {
               inputParameterId = inputParameterId.substring(indexOf + 1);
            }
            else
            {
               inputParameterId = null;
            }
         }
      }

      // processing all in data mappings
      ModelElementList inDataMappings = activity.getInDataMappings();
      for (int i = 0; i < inDataMappings.size(); ++i)
      {
         IDataMapping mapping = (IDataMapping) inDataMappings.get(i);
         Object bridgeObject = processInstance.getInDataValue(mapping.getData(),
               mapping.getDataPath(), mapping.getActivityAccessPoint(),
               mapping.getActivityPath(), mapping.getActivity());

         if (bridgeObject != null && inputParameterId != null && inputParameterId.equals(mapping.getActivityAccessPointId()))
         {
            bridgeObject = getMIBridgeObject(mapping, bridgeObject);
         }

         // @todo (france, ub): plethora: same style as out mappings?
         if (StringUtils.isEmpty(mapping.getActivityPath()))
         {
            applicationInstance.setInAccessPointValue(mapping.getActivityAccessPointId(), bridgeObject);
         }
         else
         {
            Object accessPointValue = applicationInstance.getOutAccessPointValue(mapping.getActivityAccessPointId());
            AccessPoint activityAccessPoint = mapping.getActivityAccessPoint();
            ExtendedAccessPathEvaluator appPathEvaluator = SpiUtils.createExtendedAccessPathEvaluator(
                  activityAccessPoint, mapping.getActivityPath());
            AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
                  processInstance, null);
            appPathEvaluator.evaluate(activityAccessPoint,
                  accessPointValue, mapping.getActivityPath(), evaluationContext,
                  bridgeObject);
         }
      }
   }

   protected Object getMIBridgeObject(IDataMapping mapping, Object bridgeObject)
   {
      boolean isItem = false;
      if (bridgeObject instanceof List)
      {
         isItem = true;
         bridgeObject = index >= 0 && index < ((List) bridgeObject).size()
               ? ((List) bridgeObject).get(index) : null;
      }
      else if (bridgeObject.getClass().isArray())
      {
         isItem = true;
         bridgeObject = index >= 0 && index < Array.getLength(bridgeObject)
               ? Array.get(bridgeObject, index) : null;
      }
      if (isItem)
      {
         IData data = mapping.getData();
         if (BusinessObjectUtils.hasBusinessObject(data))
         {
            Map<String, BusinessObjectRelationship> relationships = BusinessObjectUtils.getBusinessObjectRelationships(data);
            BusinessObjectRelationship relationship = relationships.get(mapping.getDataPath());
            if (relationship != null && relationship.otherBusinessObject != null)
            {
               BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(data.getModel().getModelOID(),
                     relationship.otherBusinessObject.id, bridgeObject);
               query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
               BusinessObjects objects = BusinessObjectUtils.getBusinessObjects(query);
               for (BusinessObject bo : objects)
               {
                  List<Value> values = bo.getValues();
                  bridgeObject = values == null || values.isEmpty() ? null : values.get(0).getValue();
                  break;
               }
            }
         }
      }
      return bridgeObject;
   }

   private Map processRouteInDataMappings(IActivity activity)
   {
      // TODO: (fh) handle multi instance activity

      Map apValues = null;

      // processing all in data mappings
      ModelElementList inDataMappings = activity.getInDataMappings();
      for (int i = 0; i < inDataMappings.size(); ++i)
      {
         IDataMapping mapping = (IDataMapping) inDataMappings.get(i);

         if (PredefinedConstants.DEFAULT_CONTEXT.equals(mapping.getContext()))
         {
            Object bridgeObject = processInstance.getInDataValue(mapping.getData(),
                  mapping.getDataPath());

            if (StringUtils.isEmpty(mapping.getActivityPath()))
            {
               if (null == apValues)
               {
                  apValues = CollectionUtils.newMap();
               }

               apValues.put(mapping.getActivityAccessPointId(), bridgeObject);
            }
         }
      }
      return (null != apValues) ? apValues : Collections.EMPTY_MAP;
   }

   private void processSubProcessInDataMappings(IActivity activity,
         IProcessInstance subProcessInstance)
   {
      IProcessDefinition processDefinition = subProcessInstance.getProcessDefinition();
      String inputParameterId = null;
      String counterParameterId = null;
      ILoopCharacteristics loop = activity.getLoopCharacteristics();
      String parameterContext = null;
      if (loop instanceof IMultiInstanceLoopCharacteristics)
      {
         counterParameterId = ((IMultiInstanceLoopCharacteristics) loop).getCounterParameterId();
         if (counterParameterId != null)
         {
            String counterParameterContext = counterParameterId.substring(0, counterParameterId.indexOf(':'));
            if (PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(counterParameterContext))
            {
               counterParameterId = counterParameterId.substring(counterParameterContext.length() + 1);
               IData subProcessData = ModelUtils.getMappedData(processDefinition, counterParameterId);
               subProcessInstance.setOutDataValue(subProcessData, null, index);
            }
         }
         inputParameterId = ((IMultiInstanceLoopCharacteristics) loop).getInputParameterId();
         if (inputParameterId != null)
         {
            // must be set last
            parameterContext = inputParameterId.substring(0, inputParameterId.indexOf(':'));
            inputParameterId = inputParameterId.substring(parameterContext.length() + 1);
         }
      }
      ModelElementList inDataMappings = activity.getInDataMappings();
      for (int i = 0; i < inDataMappings.size(); ++i)
      {
         IDataMapping mapping = (IDataMapping) inDataMappings.get(i);

         String context = mapping.getContext();
         if (PredefinedConstants.ENGINE_CONTEXT.equals(context)
               || PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(context))
         {
            // copy data value
            Object bridgeObject = getProcessInstance().getInDataValue(
                  mapping.getData(), mapping.getDataPath());

            String accessPointId = mapping.getActivityAccessPointId();
            if (bridgeObject != null && inputParameterId != null
                  && inputParameterId.equals(accessPointId) && parameterContext.equals(context))
            {
               bridgeObject = getMIBridgeObject(mapping, bridgeObject);
            }
            IData subProcessData = null;
            if(accessPointId == null && mapping.getData() == null && mapping.getDataPath().startsWith("("))
            {
               // Use data of the referenced model if constant has no data
               subProcessData = ModelUtils.getMappedData(processDefinition, mapping.getId());
            }
            else
            {
               subProcessData = PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(context)
                  ? ModelUtils.getMappedData(processDefinition, accessPointId)
                  : ModelUtils.getData(processDefinition, accessPointId);
            }

            String subProcessDataPath = mapping.getActivityPath();
            subProcessInstance.setOutDataValue(subProcessData, subProcessDataPath, bridgeObject);
         }
      }
   }

   /**
    * @param applicationOutAccessPointValues
    *           a map of (accesspoint, value) pairs
    */
   public void processOutDataMappings(Map applicationOutAccessPointValues)
   {
      // lazily evaluate engine access points as most of the time they will not be
      // needed
      Map activityOutAccessPointValues = null;

      ModelElementList outDataMappings = getActivity().getOutDataMappings();
      for (int i = 0; i < outDataMappings.size(); ++i)
      {
         IDataMapping mapping = (IDataMapping) outDataMappings.get(i);
         if (PredefinedConstants.APPLICATION_CONTEXT.equals(mapping.getContext()))
         {
            if (isIntrinsicOutAccessPoint(mapping.getActivityAccessPointId()))
            {
               if (null == activityOutAccessPointValues)
               {
                  activityOutAccessPointValues = getIntrinsicOutAccessPointValues();
               }

               setOutMappingValue(activityOutAccessPointValues, mapping);
            }
            else
            {
               setOutMappingValue(applicationOutAccessPointValues, mapping);
            }
         }
         else if (PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(mapping.getContext()))
         {
            setOutMappingValue(applicationOutAccessPointValues, mapping);
         }
      }
   }

   private void setOutMappingValue(Map values, IDataMapping mapping)
   {
      String activityAccessPointId = mapping.getActivityAccessPointId();
      if (values != null && values.containsKey(activityAccessPointId))
      {
         IData data = mapping.getData();
         String dataPath = mapping.getDataPath();
         AccessPoint activityAP = mapping.getActivityAccessPoint();
         String activityPath = mapping.getActivityPath();
         IActivity activity = mapping.getActivity();

         String outputParameterId = null;
         String parameterContext = null;
         ILoopCharacteristics loop = activity.getLoopCharacteristics();
         if (loop instanceof IMultiInstanceLoopCharacteristics)
         {
            outputParameterId = ((IMultiInstanceLoopCharacteristics) loop).getOutputParameterId();
            if (outputParameterId != null)
            {
               parameterContext = outputParameterId.substring(0, outputParameterId.indexOf(':'));
               outputParameterId = outputParameterId.substring(parameterContext.length() + 1);
            }
         }

         Object apValue = values.get(activityAccessPointId);

         ExtendedAccessPathEvaluator apEvaluator = SpiUtils.createExtendedAccessPathEvaluator(
               activityAP, activityPath);
         AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
               processInstance, data, dataPath, activity);
         Object bridgeObject = apEvaluator.evaluate(activityAP, apValue,
               activityPath, evaluationContext);

         if (outputParameterId != null && outputParameterId.equals(activityAccessPointId) && parameterContext.equals(mapping.getContext()))
         {
            if (index >= 0)
            {
               try
               {
                  processInstance.lockDataValue(data);
               }
               catch (PhantomException e)
               {
                  // (fh) do nothing
                  return;
               }
               Object list = processInstance.getInDataValue(data, dataPath);
               bridgeObject = setValueInList(index, bridgeObject, list);
            }
         }

         processInstance.setOutDataValue(data, dataPath, bridgeObject, new DataMappingContext(this));
      }
   }

   static Object setValueInList(int index, Object item, Object list)
   {
      if (!(list instanceof List))
      {
         list = new ArrayList();
      }
      // ensure size
      for (int li = ((List) list).size(); li <= index; li++)
      {
         ((List) list).add(null);
      }
      ((List) list).set(index, item);
      return list;
   }

   /**
    * @param apValues
    *           a map of (activityAccessPointId, value) pairs
    */
   public void processRouteOutDataMappings(IActivity activity, Map apValues)
   {
      ModelElementList outDataMappings = getActivity().getOutDataMappings();
      for (int i = 0; i < outDataMappings.size(); ++i)
      {
         IDataMapping mapping = (IDataMapping) outDataMappings.get(i);
         String context = mapping.getContext();
         if (PredefinedConstants.DEFAULT_CONTEXT.equals(context))
         {
            Object bridgeObject = apValues.get(mapping.getActivityAccessPointId());
            processInstance.setOutDataValue(mapping.getData(), mapping.getDataPath(),
                  bridgeObject);
         }
      }
   }

   /**
    * Engine out data mappings are always performed
    *
    */
   public void processEngineOutDataMappings()
   {
      // sub process engine out data mappings are processed on completion of the process
      if ( !(ImplementationType.SubProcess.equals(getActivity().getImplementationType()) && SubProcessModeKey.SYNC_SEPARATE.equals(getActivity().getSubProcessMode())))
      {
         // lazily evaluate engine access points as most of the time they will not be
         // needed
         Map apValues = null;

         ModelElementList outDataMappings = getActivity().getOutDataMappings();
         for (int i = 0; i < outDataMappings.size(); ++i)
         {
            IDataMapping mapping = (IDataMapping) outDataMappings.get(i);
            String context = mapping.getContext();
            if (PredefinedConstants.ENGINE_CONTEXT.equals(context))
            {
               if (null == apValues)
               {
                  apValues = getIntrinsicOutAccessPointValues();
               }

               setOutMappingValue(apValues, mapping);
            }
         }
      }
   }

   public void processException(Throwable exception) throws Throwable
   {
      Event event = new Event(Event.ACTIVITY_INSTANCE, getOID(), Event.OID_UNDEFINED, Event.OID_UNDEFINED, Event.ENGINE_EVENT);
      event.setAttribute(PredefinedConstants.EXCEPTION_ATT, exception);

      event = EventUtils.processAutomaticEvent(getActivity(),
            PredefinedConstants.EXCEPTION_CONDITION, event);

      if (null == event.getIntendedState())
      {
         throw exception;
      }
      else
      {
         try
         {
            if (ActivityInstanceState.Application == getState())
            {
               // finalizing activity gracefully as requested
               setState(event.getIntendedState().getValue());

               if (isTerminated())
               {
                  EventUtils.detachAll(this);
               }

               RuntimeLog.WF_EVENT.info(MessageFormat.format(
                     "Processed expected exception as modeled for: {0}:",
                     new Object[] {this}), exception);
            }
         }
         catch (IllegalStateChangeException e)
         {
            trace.warn("Failed finalizing activity after caught exception, interrupting "
                  + "activity thread.", e);
            throw exception;
         }
      }
   }

   public void accept(Map receiverData)
   {
      IProcessDefinition subProcessDefinition = getActivity().getImplementationProcessDefinition();
      if (null == subProcessDefinition)
      {
         // Only processing of data mappings for non-subprocess-activities
         // is done here. See {@link ProcessInstanceBean#complete} for more.
         processOutDataMappings(receiverData);
      }
   }

   // @todo (france, ub): this has strange semantics because it is contrary to it's
   // sister methods not allowed to call this in vitro
   public void complete()
   {
      fetch();

      // support polymorphism wrt. activity definition
      IActivityExecutionStrategy aeStrategy = ActivityExecutionUtils.getExecutionStrategy(getActivity());
      if (null != aeStrategy)
      {
         aeStrategy.completeActivityInstance(this);
      }
      else
      {
         doCompleteActivity();
      }

      EventUtils.detachAll(this);

      ActivityThread.getCurrentActivityThreadContext().completingActivity(this);
   }

   public void doCompleteActivity() throws IllegalStateChangeException
   {
      setState(ActivityInstanceState.COMPLETED);

      removeFromWorklists();

      if (null != getActivity().getPerformer())
      {
         long userOID = SecurityProperties.getUserOID();
         if (userOID != 0)
         {
            markModified(FIELD__PERFORMED_BY);
            this.performedBy = userOID;
         }
      }

      processEngineOutDataMappings();
   }

   public void activate() throws IllegalStateChangeException, IllegalOperationException
   {
      QualityAssuranceUtils.assertActivationIsAllowed(this);
      IActivity activity = getActivity();
      if (activity.isHibernateOnCreation()
            && getState().equals(ActivityInstanceState.Hibernated)
            && !ImplementationType.Manual.equals(activity.getImplementationType()))
      {
         invoke(activity);
      }
      else
      {
         setState(ActivityInstanceState.APPLICATION);
      }
   }

   public void hibernate() throws IllegalStateChangeException
   {
      setState(ActivityInstanceState.HIBERNATED);
   }

   public void suspend() throws IllegalStateChangeException
   {
      setState(ActivityInstanceState.SUSPENDED);
   }

   public void halt() throws IllegalStateChangeException
   {
      setState(ActivityInstanceState.HALTED);
   }

   public void interrupt() throws IllegalStateChangeException
   {
      setState(ActivityInstanceState.INTERRUPTED);
   }

   public void delegateToDefaultPerformer() throws AccessForbiddenException
   {
      assertDelegationGranted();
      putToDefaultWorklist();
   }

   public void delegateToUser(IUser user) throws AccessForbiddenException
   {
      internalDelegateToUser(user, true);
   }

   void internalDelegateToUser(IUser user, boolean checkPermissionToDelegate)
   {
      assertDelegationGranted();

      IModelParticipant performer = getActivity().getPerformer();

      if (null == performer)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_NON_INTERACTIVE_AI_CAN_NOT_BE_DELEGATED
                     .raise(getOID(), getActivity().getId()));
      }

      // check for conditional performer of type UserGroup if user is part of the group
      if (performer instanceof IConditionalPerformer)
      {
         IParticipant participant = ((IConditionalPerformer) performer).retrievePerformer(getProcessInstance());
         {
            if (participant instanceof IUserGroup)
            {
               boolean inGroup = false;
               Iterator<IUserGroup> groups = user.getAllUserGroups(true);
               while (groups.hasNext())
               {
                  IUserGroup group = groups.next();
                  if (group.getId().equals(participant.getId()))
                  {
                     inGroup = true;
                     break;
                  }
               }
               if ( !inGroup)
               {
                  throw new AccessForbiddenException(
                        BpmRuntimeError.BPMRT_USER_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
                              user.getId(), user.getOID(), getOID(), getActivity().getId()));
               }
            }
         }
      }

      if (performer.isAuthorized(user))
      {
         if (checkPermissionToDelegate
               && DepartmentUtils.getFirstScopedOrganization(performer) != null)
         {
            AccessForbiddenException exception = null;
            Iterator<UserParticipantLink> links = user.getAllParticipantLinks();
            while (links.hasNext())
            {
               UserParticipantLink link = links.next();
               IModelParticipant participant = link.getParticipant();
               if (DepartmentUtils.getFirstScopedOrganization(participant) != null)
               {
                  try
                  {
                     checkDepartmentChange(participant, link.getDepartment(), null);
                     exception = null;
                     break;
                  }
                  catch (AccessForbiddenException ex)
                  {
                     if (exception == null)
                     {
                        exception = ex;
                     }
                  }
               }
            }
            if (exception != null)
            {
               throw exception;
            }
         }
         putToUserWorklist(user);
      }
      else
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_USER_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
                     user.getId(), user.getOID(), getOID(), getActivity().getId()));
      }
   }

   public void delegateToUserGroup(IUserGroup userGroup) throws AccessForbiddenException
   {
      assertDelegationGranted();

      IModelParticipant performer = getActivity().getPerformer();

      if (null == performer)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_NON_INTERACTIVE_AI_CAN_NOT_BE_DELEGATED
                     .raise(getOID(), getActivity().getId()));
      }

      if (performer.isAuthorized(userGroup))
      {
         putToUserGroupWorklist(userGroup);
      }
      else
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_USER_GROUP_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
                     userGroup.getId(), userGroup.getOID(), getOID(),
                     getActivity().getId()));
      }

   }

   public void delegateToParticipant(IModelParticipant participant)
         throws AccessForbiddenException
   {
      delegateToParticipant(participant, null, null);
   }

   @ExecutionPermission(id = ExecutionPermission.Id.delegateToDepartment,
         scope = ExecutionPermission.Scope.activity,
         defaults = {ExecutionPermission.Default.ADMINISTRATOR})
   public void delegateToParticipant(IModelParticipant participant,
         IDepartment newDepartment, IDepartment lastDepartment)
         throws AccessForbiddenException
   {
      assertDelegationGranted();

      IModelParticipant performer = getActivity().getPerformer();

      if (null == performer)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_NON_INTERACTIVE_AI_CAN_NOT_BE_DELEGATED
                     .raise(getOID(), getActivity().getId()));
      }

      if (performer.isAuthorized(participant))
      {
         IDepartment targetDepartment = checkDepartmentChange(participant, newDepartment,
               lastDepartment);
         putToParticipantWorklist(participant, targetDepartment == null
               ? 0
               : targetDepartment.getOID());
      }
      else
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_MODEL_PARTICIPANT_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
                     participant.getId(), Long.valueOf(getOID()), getActivity().getId()));
      }
   }

   private IDepartment checkDepartmentChange(IModelParticipant participant,
         IDepartment newDepartment, IDepartment lastDepartment)
   {
      if (newDepartment != null && newDepartment != IDepartment.NULL)
      {
         long modelOid = participant.getModel().getModelOID();
         IOrganization refOrg = DepartmentUtils.getOrganization(newDepartment, modelOid);
         if ( !refOrg.equals(participant)
               && !DepartmentUtils.isChild(participant, refOrg))
         {
            throw new AccessForbiddenException(
                  BpmRuntimeError.BPMRT_MODEL_PARTICIPANT_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
                        participant.getId(), Long.valueOf(getOID()), getActivity().getId()));
         }
      }

      IDepartment oldDepartment = getCurrentDepartment();
      if (oldDepartment == null)
      {
         if (lastDepartment == null)
         {
            Iterator<ActivityInstanceHistoryBean> history = ActivityInstanceHistoryBean.getAllForActivityInstance(
                  this, false);
            while (history.hasNext())
            {
               ActivityInstanceHistoryBean aih = history.next();
               if (ActivityInstanceState.Application == aih.getState())
               {
                  continue;
               }
               if (ActivityInstanceState.Suspended == aih.getState())
               {
                  lastDepartment = aih.getDepartment();
                  if (lastDepartment == null)
                  {
                     lastDepartment = IDepartment.NULL;
                  }
               }
               break;
            }
         }
         if (lastDepartment != null)
         {
            oldDepartment = lastDepartment;
         }
      }

      IDepartment targetDepartment = getTargetDepartment(participant, newDepartment);
      if (!isCompatible(participant, targetDepartment, oldDepartment))
      {
         Method method;
         try
         {
            method = ActivityInstanceBean.class.getMethod("delegateToParticipant",
                  IModelParticipant.class, IDepartment.class, IDepartment.class);
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
         Authorization2.checkPermission(method, new Long[] {getOID()});
      }
      return targetDepartment;
   }

   private boolean isCompatible(IModelParticipant newParticipant,
         IDepartment newDepartment, IDepartment oldDepartment)
   {
      IModelParticipant root = getActivity().getPerformer();
      IOrganization org = DepartmentUtils.getFirstScopedOrganization(root);
      if (org != null)
      {
         IDepartment oldRoot = getRoot(org, oldDepartment);
         IDepartment newRoot = getRoot(org, newDepartment);
         return newRoot == oldRoot;
      }
      return true;
   }

   private IDepartment getRoot(IOrganization org, IDepartment department)
   {
      if (department == null || department == IDepartment.NULL)
      {
         return null;
      }
      IOrganization refOrg = DepartmentUtils.getOrganization(department, model);
      if (refOrg == org)
      {
         return department;
      }
      if (DepartmentUtils.isChild(org, refOrg))
      {
         return department;
      }
      if (DepartmentUtils.isChild(refOrg, org))
      {
         while (department != null)
         {
            department = department.getParentDepartment();
            refOrg = DepartmentUtils.getOrganization(department, model);
            if (refOrg == org)
            {
               return department;
            }
         }
      }
      return null;
   }

   public void bind(IEventHandler handler, EventHandlerBinding aspect)
   {
      EventUtils.bind(this, handler, aspect);
   }

   public void unbind(IEventHandler handler, EventHandlerBinding aspect)
   {
      EventUtils.unbind(this, handler, aspect);
   }

   private void assertDelegationGranted() throws AccessForbiddenException
   {
      fetch();
      switch (state)
      {
      case ActivityInstanceState.SUSPENDED:
      case ActivityInstanceState.HIBERNATED:
      case ActivityInstanceState.APPLICATION:
      case ActivityInstanceState.HALTED:
         // do nothing
         break;
      default:
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_AI_CAN_NOT_BE_DELEGATED_IN_CURRENT_STATE.raise(
                     Long.valueOf(getOID()), getActivity().getId(), getState()));
      }
   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      return new ActivityInstanceProperty(getOID(), name, value);

   }

   public Class getPropertyImplementationClass()
   {
      return ActivityInstanceProperty.class;
   }

   public List getHistoricStates()
   {
      return (null != historicStates) ? historicStates : Collections.EMPTY_LIST;
   }

   private void recordInitialPerformer()
   {
      if (null == originalPerformer)
      {
         if (0 != currentUserPerformer)
         {
            this.originalPerformer = new EncodedPerformer(PerformerType.User,
                  currentUserPerformer);
         }
         else if (0 < currentPerformer)
         {
            this.originalPerformer = new EncodedPerformer(PerformerType.ModelParticipant,
                  currentPerformer, currentDepartment);
         }
         else if (0 > currentPerformer)
         {
            this.originalPerformer = new EncodedPerformer(PerformerType.UserGroup,
                  -currentPerformer);
         }
         else
         {
            this.originalPerformer = new EncodedPerformer(PerformerType.None, 0);
         }
      }
   }

   private ChangeLogDigester.HistoricState recordHistoricState()
   {
      return recordHistoricState(SecurityProperties.getUserOID());
   }

   private ChangeLogDigester.HistoricState recordHistoricState(long workflowUserOid)
   {
      ChangeLogDigester.HistoricState state = null;

      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      long now = TimestampProviderUtils.getTimeStampValue();
      if (rtEnv.getChangeLogDigester().isAiChangeLogEnabled(this))
      {
         final Date tsFrom = getLastModificationTime();

         final Date tsUntil = new Date(now);
         if ( !tsUntil.after(tsFrom))
         {
            trace.debug("Patching modification time by 1ms");

            tsUntil.setTime(tsFrom.getTime() + 1);
         }

         // interval must be synchronized with last modification time to allow association
         // of
         // current/terminal state of AI with history table
         updateModificationTime(tsUntil.getTime());

         state = new ChangeLogDigester.HistoricState( //
               tsFrom, tsUntil, //
               getState(), getPerformer(), getCurrentDepartment(), workflowUserOid);

         if (null == historicStates)
         {
            this.historicStates = CollectionUtils.newList();
         }
         historicStates.add(state);
      }
      else
      {
         updateModificationTime(now);
      }

      return state;
   }

   public void lockAndCheck()
   {
      if (getPersistenceController() != null && !getPersistenceController().isLocked())
      {
         lock();
         try
         {
            reloadAttribute("state");
         }
         catch (PhantomException e)
         {
            throw new InternalException(e);
         }
      }
   }

   /**
    * This method is called when importing an activity instance from an archive.
    * This is the only time this should be called, it initialized the initial performer
    * attribute which is necessary upon session flushing
    */
   public void prepareForImportFromArchive() {
      recordInitialPerformer();
   }

   public QualityAssuranceState getQualityAssuranceState()
   {
      if (isPropertyAvailable())
      {
         String value = (String) getPropertyValue(QualityAssuranceState.PROPERTY_KEY);
         if (StringUtils.isNotEmpty(value))
         {
            return QualityAssuranceState.valueOf(value);
         }
      }

      return QualityAssuranceState.NO_QUALITY_ASSURANCE;
   }

   public void setQualityAssuranceState(QualityAssuranceState s)
   {
      String stateValue = s.toString();
      setPropertyValue(QualityAssuranceState.PROPERTY_KEY, stateValue);

      propertiesAvailable = 1;
      markModified(FIELD__PROPERTIES_AVAILABLE);
   }

   public boolean isPropertyAvailable()
   {
      fetch();
      return propertiesAvailable != 0 ? true : false;
   }

   public long getLastModifyingUser()
   {
      if (this.lastModifyingUser != null)
      {
         return this.lastModifyingUser;
      }
      else
      {
         return SecurityProperties.getUserOID();
      }
   }
}
