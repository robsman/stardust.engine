/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailActivityBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailProcessDefinitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.extensions.dms.data.AuditTrailUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;



/**
 * Query container for building complex queries for activity instances.
 * <p/>
 * <p>Valid filter criteria are:
 * <ul>
 *    <li>{@link FilterTerm} for building complex criteria.</li>
 *    <li>{@link UnaryOperatorFilter}, {@link BinaryOperatorFilter} or {@link TernaryOperatorFilter} for applying filters to activity instance attributes.</li>
 *    <li>{@link ProcessDefinitionFilter} for finding instances of activities belonging to specific process definitions.</li>
 *    <li>{@link ProcessInstanceFilter} for finding activities belonging to specific process instances.</li>
 *    <li>{@link ActivityFilter} for finding instances of specific activities.</li>
 *    <li>{@link ActivityInstanceFilter} for finding specific activity instances.</li>
 *    <li>{@link ActivityStateFilter} for finding activity instances currently being in specific states.</li>
 *    <li>{@link PerformingUserFilter} for finding activities currently being performed by a specific user.</li>
 *    <li>{@link PerformingParticipantFilter} for finding activities currently being performed by a specific participant.</li>
 *    <li>{@link PerformingOnBehalfOfFilter} for finding activities currently being performed on behalf of a specific participant.</li>
 *    <li>{@link PerformedByUserFilter} for finding activities that were performed and completed by a specific user.</li>
 *    <li>{@link DataFilter} for finding activity instances belonging to process instances with same scope process instance containing specific workflow data.</li>
 *    <li>{@link SubProcessDataFilter} for finding activity instances belonging to process instances and its subprocess instances containing specific workflow data.</li>
 *    <li>{@link HierarchyDataFilter} for finding activity instances belonging to the complete hierarchy of process instances containing specific workflow data.</li>
 * </ul>
 * </p>
 *
 * @author rsauer
 * @version $Revision$
 */
public class ActivityInstanceQuery extends Query
{
   private static final long serialVersionUID = -9079245444850854101L;

   private static final String FIELD__PROCESS_INSTANCE_PRIORITY = "process_instance_"
         + ProcessInstanceBean.FIELD__PRIORITY;

   private static final String FIELD__PROCESS_INSTANCE_BENCHMARK_OID = "process_instance_"
         + ProcessInstanceBean.FIELD__BENCHMARK_OID;

   /**
    * The OID of the activity instance.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getOID()
    */
   public static final Attribute OID =
         new Attribute(ActivityInstanceBean.FIELD__OID);
   /**
    * The state of the activity instance.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getState()
    */
   public static final Attribute STATE =
         new Attribute(ActivityInstanceBean.FIELD__STATE);
   /**
    * The {@link java.lang.Long} representation of the start time of the activity
    * instance.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getStartTime()
    * @see java.util.Calendar#getTime()
    */
   public static final Attribute START_TIME =
         new Attribute(ActivityInstanceBean.FIELD__START_TIME);
   /**
    * The {@link java.lang.Long} representation of the last modification time of the
    * activity instance.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getLastModificationTime()
    * @see java.util.Calendar#getTime()
    */
   public static final Attribute LAST_MODIFICATION_TIME =
         new Attribute(ActivityInstanceBean.FIELD__LAST_MODIFICATION_TIME);
   /**
    * The OID of the activity instance's definition.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getActivity()
    */
   public static final Attribute ACTIVITY_OID =
         new Attribute(ActivityInstanceBean.FIELD__ACTIVITY);

   // @todo (france, ub): currently not there
   /**
    * The OID of the activity instance's current performing participant.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getParticipantPerformerID()
    */
   public static final Attribute CURRENT_PERFORMER_OID =
         new Attribute(ActivityInstanceBean.FIELD__CURRENT_PERFORMER);
   /**
    * The OID of the activity instance's current performing user.
    *
    * @see org.eclipse.stardust.engine.api.runtime.User#getOID()
    */
   public static final Attribute CURRENT_USER_PERFORMER_OID =
         new Attribute(ActivityInstanceBean.FIELD__CURRENT_USER_PERFORMER);
   /**
    * The OID of the activity instance's completing user.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getPerformedByOID()
    * @see org.eclipse.stardust.engine.api.runtime.User#getOID()
    */
   public static final Attribute PERFORMED_BY_OID =
         new Attribute(ActivityInstanceBean.FIELD__PERFORMED_BY);
   /**
    * The OID of the process instance the activity instance belongs to.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getProcessInstanceOID()
    */
   public static final Attribute PROCESS_INSTANCE_OID =
         new Attribute(ActivityInstanceBean.FIELD__PROCESS_INSTANCE);
   /**
    * The Criticality of the activity instance.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getCriticality()
    */
   public static final Attribute CRITICALITY =
         new Attribute(ActivityInstanceBean.FIELD__CRITICALITY);
   /**
    * The benchmark result category of the activity instance.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getBenchmarkResult()
    */
   public static final Attribute BENCHMARK_VALUE =
         new Attribute(ActivityInstanceBean.FIELD__BENCHMAKRK_VALUE);
   
   /**
    * The benchmark definition oid of the process instance the activity instance belongs to.
    *
    * @see org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean#getBenchmark()
    */
   public static final FilterableAttribute BENCHMARK_OID = new ReferenceAttribute(
         new Attribute(FIELD__PROCESS_INSTANCE_BENCHMARK_OID), ProcessInstanceBean.class,
         ActivityInstanceBean.FIELD__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID,
         ProcessInstanceBean.FIELD__BENCHMARK_OID);

   /**
    * The priority of the process instance the activity instance belongs to.
    *
    * @see org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean#getPriority()
    */
   public static final FilterableAttribute PROCESS_INSTANCE_PRIORITY = new ReferenceAttribute(
         new Attribute(FIELD__PROCESS_INSTANCE_PRIORITY), ProcessInstanceBean.class,
         ActivityInstanceBean.FIELD__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID,
         ProcessInstanceBean.FIELD__PRIORITY);

   /**
    * Orders the resulting activity instances by their activity id.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion ACTIVITY_ID = new CustomOrderCriterion(
         AuditTrailActivityBean.class, AuditTrailActivityBean.FIELD__ID);

   /**
    * Orders the resulting activity instances by their activity name.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion ACTIVITY_NAME = new CustomOrderCriterion(
         AuditTrailActivityBean.class, AuditTrailActivityBean.FIELD__NAME);

   /**
    * Orders the resulting activity instances by their starting process instances definition id.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion PROC_DEF_ID = new CustomOrderCriterion(
         AuditTrailProcessDefinitionBean.class,
         AuditTrailProcessDefinitionBean.FIELD__ID);

   /**
    * Orders the resulting activity instances by their starting process instances definition name.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion PROC_DEF_NAME = new CustomOrderCriterion(
         AuditTrailProcessDefinitionBean.class,
         AuditTrailProcessDefinitionBean.FIELD__NAME);

   /**
    * Orders the resulting activity instances by their current user performer account.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion USER_ACCOUNT = new CustomOrderCriterion(
         UserBean.class, UserBean.FIELD__ACCOUNT);

   /**
    * Orders the resulting activity instances by their current user performer first name.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion USER_FIRST_NAME = new CustomOrderCriterion(
         UserBean.class, UserBean.FIELD__FIRST_NAME);

   /**
    * Orders the resulting activity instances by their current user performer last name.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion USER_LAST_NAME = new CustomOrderCriterion(
         UserBean.class, UserBean.FIELD__LAST_NAME);

   private static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[]
         {
            FilterTerm.class,
            UnaryOperatorFilter.class,
            BinaryOperatorFilter.class,
            TernaryOperatorFilter.class,
            ProcessDefinitionFilter.class,
            ProcessInstanceFilter.class,
            ActivityFilter.class,
            ActivityInstanceFilter.class,
            ActivityStateFilter.class,
            PerformingUserFilter.class,
            PerformingParticipantFilter.class,
            PerformingOnBehalfOfFilter.class,
            PerformedByUserFilter.class,
            DataFilter.class,
            SubProcessDataFilter.class,
            HierarchyDataFilter.class,
            DataPrefetchHint.class,
            CurrentPartitionFilter.class,
            RootProcessInstanceFilter.class,
            DescriptorFilter.class
         }),
         ActivityInstanceQuery.class
   );

   /**
    * Creates a query for finding all activity instances currently existing.
    *
    * @return The readily configured query.
    */
   public static ActivityInstanceQuery findAll()
   {
      return new ActivityInstanceQuery();
   }

   /**
    * Creates a query for finding activity instances currently being in the specified
    * state.
    *
    * @param activityState The state the activity instance should be in.
    * @return The readily configured query.
    *
    * @see #findInState(ActivityInstanceState[])
    * @see #findInState(String, ActivityInstanceState)
    * @see #findInState(String, ActivityInstanceState[])
    * @see #findInState(String, String, ActivityInstanceState)
    * @see #findInState(String, String, ActivityInstanceState[])
    * @see #findAlive()
    * @see ProcessDefinitionFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInState(
         ActivityInstanceState activityState)
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query.where(new ActivityStateFilter(activityState));

      return query;
   }

   /**
    * Creates a query for finding activity instances currently being in one of the
    * specified states.
    *
    * @param activityStates The list of states the activity instance should be in one of.
    * @return The readily configured query.
    *
    * @see #findInState(ActivityInstanceState)
    * @see #findInState(String, ActivityInstanceState)
    * @see #findInState(String, ActivityInstanceState[])
    * @see #findInState(String, String, ActivityInstanceState)
    * @see #findInState(String, String, ActivityInstanceState[])
    * @see #findAlive()
    * @see ProcessDefinitionFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInState(
         ActivityInstanceState[] activityStates)
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query.where(new ActivityStateFilter(activityStates));

      return query;
   }

   /**
    * Creates a query for finding instances of activities belonging to the process
    * definition identified by <code>processID</code> currently being in the specified
    * state.
    *
    * @param processID     The ID of the process definition the activity should belong to.
    * @param activityState The state the activity instance should be in.
    * @return The readily configured query.
    *
    * @see #findInState(ActivityInstanceState)
    * @see #findInState(ActivityInstanceState[])
    * @see #findInState(String, ActivityInstanceState[])
    * @see #findInState(String, String, ActivityInstanceState)
    * @see #findInState(String, String, ActivityInstanceState[])
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInState(String processID,
         ActivityInstanceState activityState)
   {
      ActivityInstanceQuery query = findInState(activityState);

      query.where(new ProcessDefinitionFilter(processID));

      return query;
   }

   /**
    * Creates a query for finding instances of activities belonging to the process
    * definition identified by <code>processID</code> currently being in one of the
    * specified states.
    *
    * @param processID      The ID of the process definition the activity should belong to.
    * @param activityStates The list of states the activity instance should be in one of.
    * @return The readily configured query.
    *
    * @see #findInState(ActivityInstanceState)
    * @see #findInState(ActivityInstanceState[])
    * @see #findInState(String, ActivityInstanceState)
    * @see #findInState(String, String, ActivityInstanceState)
    * @see #findInState(String, String, ActivityInstanceState[])
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInState(String processID,
         ActivityInstanceState[] activityStates)
   {
      ActivityInstanceQuery query = findInState(activityStates);

      query.where(new ProcessDefinitionFilter(processID));

      return query;
   }

   /**
    * Creates a query for finding instances of the activity identified by
    * <code>activityID</code> and belonging to the process definition identified by
    * <code>processID</code> currently being in the specified state.
    *
    * @param processID     The ID of the process definition the activity should belong to.
    * @param activityID    The ID of the activity to find instances of.
    * @param activityState The state the activity instance should be in.
    * @return The readily configured query.
    *
    * @see #findInState(ActivityInstanceState)
    * @see #findInState(ActivityInstanceState[])
    * @see #findInState(String, String, ActivityInstanceState[])
    * @see #findInState(String, ActivityInstanceState)
    * @see #findInState(String, ActivityInstanceState[])
    * @see #findAlive(String, String)
    * @see ActivityFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInState(String processID,
         String activityID, ActivityInstanceState activityState)
   {
      ActivityInstanceQuery query = findInState(activityState);

      query.where(ActivityFilter.forProcess(activityID, processID));

      return query;
   }

   /**
    * Creates a query for finding instances of the activity identified by
    * <code>activityID</code> belonging to the process definition identified by
    * <code>processID</code> currently being in one of the specified states.
    *
    * @param processID      The ID of the process definition the activity should belong to.
    * @param activityID     The ID of the activity to find instances of.
    * @param activityStates The list of states the activity instance should be in one of.
    * @return The readily configured query.
    *
    * @see #findInState(ActivityInstanceState)
    * @see #findInState(ActivityInstanceState[])
    * @see #findInState(String, String, ActivityInstanceState)
    * @see #findInState(String, ActivityInstanceState)
    * @see #findInState(String, ActivityInstanceState[])
    * @see #findAlive(String, String)
    * @see ActivityFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInState(String processID,
         String activityID, ActivityInstanceState[] activityStates)
   {
      ActivityInstanceQuery query = findInState(activityStates);

      query.where(ActivityFilter.forProcess(activityID, processID));

      return query;
   }

   /**
    * Creates a query for finding alive activity instances.
    * <p/>
    * <p>Alive means not being in states {@link ActivityInstanceState#ABORTED}
    * or {@link ActivityInstanceState#COMPLETED}</p>
    *
    * @return The readily configured query.
    *
    * @see #findCompleted()
    * @see #findAlive(String, String)
    * @see #findAlive(long, String)
    * @see #findInState(ActivityInstanceState)
    * @see ActivityStateFilter
    * @see ActivityStateFilter#ALIVE
    */
   public static ActivityInstanceQuery findAlive()
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query.where(ActivityStateFilter.ALIVE);

      return query;
   }

   /**
    * Creates a query for finding pending activity instances.
    * <p/>
    * <p>Pending means being in states {@link ActivityInstanceState#APPLICATION}
    * or {@link ActivityInstanceState#INTERRUPTED}
    * or {@link ActivityInstanceState#SUSPENDED}
    * or {@link ActivityInstanceState#HIBERNATED}.</p>
    *
    * @return The readily configured query.
    * @see #findCompleted
    * @see #findAlive()
    * @see #findInState(ActivityInstanceState)
    * @see ActivityStateFilter
    * @see ActivityStateFilter#PENDING
    */
   public static ActivityInstanceQuery findPending()
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query.where(ActivityStateFilter.PENDING);

      return query;
   }

   /**
    * Creates a query for finding completed activity instances.
    * <p/>
    * <p>Completed means being in state {@link ActivityInstanceState#COMPLETED}.
    * </p>
    *
    * @return The readily configured query.
    *
    * @see #findAlive()
    * @see #findPending()
    * @see #findInState(ActivityInstanceState)
    * @see ActivityStateFilter
    * @see ActivityStateFilter#COMPLETED
    */
   public static ActivityInstanceQuery findCompleted()
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query.where(ActivityStateFilter.COMPLETED);

      return query;
   }

   /**
    * Creates a query for finding alive instances of activities belonging to the process
    * definition identified by <code>processID</code>.
    * <p/>
    * <p>Alive means not being in states {@link ActivityInstanceState#ABORTED}
    * or {@link ActivityInstanceState#COMPLETED}</p>
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @return The readily configured query.
    *
    * @see #findAlive()
    * @see #findAlive(String, String)
    * @see #findAlive(long, String)
    * @see #findInState(ActivityInstanceState)
    * @see ProcessDefinitionFilter
    * @see ActivityStateFilter
    * @see ActivityStateFilter#ALIVE
    */
   public static ActivityInstanceQuery findAlive(String processID)
   {
      ActivityInstanceQuery query = findAlive();

      query.where(new ProcessDefinitionFilter(processID));

      return query;
   }

   /**
    * Creates a query for finding alive instances of the activity identified by
    * <code>activityID</code> belonging to the process definition identified by
    * <code>processID</code>.
    * <p/>
    * <p>Alive means not being in states {@link ActivityInstanceState#ABORTED}
    * or {@link ActivityInstanceState#COMPLETED}</p>
    *
    * @param processID  The ID of the process definition the activity should belong to.
    * @param activityID The ID of the activity to find instances of.
    * @return The readily configured query.
    * @see #findAlive()
    * @see #findAlive(String)
    * @see #findAlive(long, String)
    * @see #findInState(String, String, ActivityInstanceState)
    * @see ProcessDefinitionFilter
    * @see ActivityStateFilter
    * @see ActivityStateFilter#ALIVE
    */
   public static ActivityInstanceQuery findAlive(String processID,
         String activityID)
   {
      ActivityInstanceQuery query = findAlive();

      query.where(ActivityFilter.forProcess(activityID, processID));

      return query;
   }

   /**
    * Creates a query for finding alive instances of the activity identified by
    * <code>activityID</code> belonging to the process instance identified by
    * <code>processInstanceOID</code>.
    * <p/>
    * <p>Alive means not being in states {@link ActivityInstanceState#ABORTED}
    * or {@link ActivityInstanceState#COMPLETED}</p>
    *
    * @param processInstanceOID The OID of the process instance the activity should belong
    *                           to.
    * @param activityID         The ID of the activity to find instances of.
    * @return The readily configured query.
    * @see #findAlive()
    * @see #findAlive(String)
    * @see #findAlive(String, String)
    * @see #findInState(String, String, ActivityInstanceState)
    * @see ProcessDefinitionFilter
    * @see ActivityStateFilter
    * @see ActivityStateFilter#ALIVE
    */
   public static ActivityInstanceQuery findAlive(long processInstanceOID,
         String activityID)
   {
      ActivityInstanceQuery query = findAlive();

      query.where(new ProcessInstanceFilter(processInstanceOID))
            .and(ActivityFilter.forAnyProcess(activityID));

      return query;
   }

   /**
    * Creates a query for finding instances of activities currently being in the specified
    * state and belonging to instances of the process definition identified by
    * <code>processID</code> containing workflow data <code>dataID</code> having a value
    * of <code>dataValue</code>.
    *
    * @param processID     The ID of the process definition the activity should belong to.
    * @param dataID        The ID of the workflow data to match with.
    * @param dataValue     The value to match the workflow data with.
    * @param activityState The state the activity instance should be in.
    * @return The readily configured query.
    * @see #findInStateHavingData(String, String, Serializable, ActivityInstanceState[])
    * @see #findInStateHavingData(String, String, String, Serializable, ActivityInstanceState)
    * @see #findInStateHavingData(String, String, String, Serializable, ActivityInstanceState[])
    * @see #findInState(String, ActivityInstanceState)
    * @see #findAliveHavingData(String, String, Serializable)
    * @see ProcessDefinitionFilter
    * @see DataFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInStateHavingData(String processID,
         String dataID, Serializable dataValue, ActivityInstanceState activityState)
   {
      ActivityInstanceQuery query = findInState(processID, activityState);

      query.where(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding instances of activities currently being in one of the
    * specified states and belonging to instances of the process definition identified by
    * <code>processID</code> containing workflow data <code>dataID</code> having a value
    * of <code>dataValue</code>.
    *
    * @param processID      The ID of the process definition the activity should belong to.
    * @param dataID         The ID of the workflow data to match with.
    * @param dataValue      The value to match the workflow data with.
    * @param activityStates The list of states the activity instance should be in one of.
    * @return The readily configured query.
    * @see #findInStateHavingData(String, String, Serializable, ActivityInstanceState)
    * @see #findInStateHavingData(String, String, String, Serializable, ActivityInstanceState)
    * @see #findInStateHavingData(String, String, String, Serializable, ActivityInstanceState[])
    * @see #findInState(String, ActivityInstanceState[])
    * @see #findAliveHavingData(String, String, Serializable)
    * @see ProcessDefinitionFilter
    * @see DataFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInStateHavingData(String processID,
         String dataID, Serializable dataValue, ActivityInstanceState[] activityStates)
   {
      ActivityInstanceQuery query = findInState(processID, activityStates);

      query.where(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding instances of the activity identified by
    * <code>activityID</code> currently being in the specified state and belonging to
    * instances of the process definition identified by <code>processID</code> containing
    * workflow data <code>dataID</code> having a value of <code>dataValue</code>.
    *
    * @param processID     The ID of the process definition the activity should belong to.
    * @param activityID    The ID of the activity to find instances of.
    * @param dataID        The ID of the workflow data to match with.
    * @param dataValue     The value to match the workflow data with.
    * @param activityState The state the activity instance should be in.
    * @return The readily configured query.
    * @see #findInStateHavingData(String, String, String, Serializable, ActivityInstanceState[])
    * @see #findInStateHavingData(String, String, Serializable, ActivityInstanceState)
    * @see #findInStateHavingData(String, String, Serializable, ActivityInstanceState[])
    * @see #findInState(String, String, ActivityInstanceState)
    * @see #findAliveHavingData(String, String, String, Serializable)
    * @see ActivityFilter
    * @see DataFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInStateHavingData(String processID,
         String activityID, String dataID, Serializable dataValue,
         ActivityInstanceState activityState)
   {
      ActivityInstanceQuery query = findInState(processID, activityID, activityState);

      query.where(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding instances of the activity identified by
    * <code>activityID</code> currently being in one of the specified states and belonging
    * to instances of the process definition identified by <code>processID</code>
    * containing workflow data <code>dataID</code> having a value of
    * <code>dataValue</code>.
    *
    * @param processID      The ID of the process definition the activity should belong to.
    * @param activityID     The ID of the activity to find instances of.
    * @param dataID         The ID of the workflow data to match with.
    * @param dataValue      The value to match the workflow data with.
    * @param activityStates The list of states the activity instance should be in one of.
    * @return The readily configured query.
    * @see #findInStateHavingData(String, String, String, Serializable, ActivityInstanceState[])
    * @see #findInStateHavingData(String, String, Serializable, ActivityInstanceState)
    * @see #findInStateHavingData(String, String, Serializable, ActivityInstanceState[])
    * @see #findInState(String, String, ActivityInstanceState[])
    * @see #findAliveHavingData(String, String, String, Serializable)
    * @see ActivityFilter
    * @see DataFilter
    * @see ActivityStateFilter
    */
   public static ActivityInstanceQuery findInStateHavingData(String processID,
         String activityID, String dataID, Serializable dataValue,
         ActivityInstanceState[] activityStates)
   {
      ActivityInstanceQuery query = findInState(processID, activityID, activityStates);

      query.where(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding alive instances of activities belonging to instances of
    * the process definition identified by <code>processID</code> containing workflow data
    * <code>dataID</code> having a value of <code>dataValue</code>.
    * <p/>
    * <p>Alive means not being in states {@link ActivityInstanceState#ABORTED}
    * or {@link ActivityInstanceState#COMPLETED}</p>
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @param dataID    The ID of the workflow data to match with.
    * @param dataValue The value to match the workflow data with.
    * @return The readily configured query.
    * @see #findAliveHavingData(String, String, String, Serializable)
    * @see #findInStateHavingData(String, String, Serializable, ActivityInstanceState)
    * @see #findInState(String, ActivityInstanceState)
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see DataFilter
    * @see ActivityStateFilter
    * @see ActivityStateFilter#ALIVE
    */
   public static ActivityInstanceQuery findAliveHavingData(String processID,
         String dataID, Serializable dataValue)
   {
      ActivityInstanceQuery query = findAlive(processID);

      query.where(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding alive instances of the activity identified by
    * <code>activityID</code> belonging to instances of the process definition identified
    * by <code>processID</code> containing workflow data <code>dataID</code> having a
    * value of <code>dataValue</code>.
    * <p/>
    * <p>Alive means not being in states {@link ActivityInstanceState#ABORTED}
    * or {@link ActivityInstanceState#COMPLETED}</p>
    *
    * @param processID  The ID of the process definition the activity should belong to.
    * @param activityID The ID of the activity to find instances of.
    * @param dataID     The ID of the workflow data to match with.
    * @param dataValue  The value to match the workflow data with.
    * @return The readily configured query.
    * @see #findAliveHavingData(String, String, Serializable)
    * @see #findInStateHavingData(String, String, String, Serializable, ActivityInstanceState)
    * @see #findInState(String, String, ActivityInstanceState)
    * @see #findAlive(String, String)
    * @see ActivityFilter
    * @see DataFilter
    * @see ActivityStateFilter
    * @see ActivityStateFilter#ALIVE
    */
   public static ActivityInstanceQuery findAliveHavingData(String processID,
         String activityID, String dataID, Serializable dataValue)
   {
      ActivityInstanceQuery query = findAlive(processID, activityID);

      query.where(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding activity instances that were performed and completed by
    * the user identified by the given OID.
    *
    * @param userOID The OID of the user having performed the to be found activity
    *                instances.
    * @return The readily configured query.
    *
    * @see PerformedByUserFilter
    * @see PerformedByUserFilter#CURRENT_USER
    */
   public static ActivityInstanceQuery findPerformedByUser(long userOID)
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query.where(new PerformedByUserFilter(userOID));

      return query;
   }

   /**
    * Creates a query for finding activity instances belonging to the process instance
    * identified by the given OID.
    *
    * @param processInstanceOID The OID of the process instance to find activity instances
    *                           belonging to.
    * @return The readily configured query.
    */
   public static ActivityInstanceQuery findForProcessInstance(
         long processInstanceOID)
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query.where(new ProcessInstanceFilter(processInstanceOID));

      return query;
   }

   /**
    * Creates a query for finding activity instances which have
    * the given Document as a process attachment
    *
    * @param document The Document to find activity instances having a reference to.
    * @return The readily configured query.
    */
   public static ActivityInstanceQuery findHavingDocument(Document document)
   {
      return findHavingDocument(document.getId());
   }

   /**
    * Creates a query for finding activity instances which have
    * the given Document as a process attachment
    *
    * @param documentId The Id of the Document to find activity instances having a reference to.
    * @return The readily configured query.
    */
   public static ActivityInstanceQuery findHavingDocument(String documentId)
   {
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      String xPath = AuditTrailUtils.DOCS_DOCUMENTS + "/" + AuditTrailUtils.RES_ID;
      query.where(DataFilter.isEqual(DmsConstants.DATA_ID_ATTACHMENTS, xPath, documentId));

      return query;
   }

   /**
    * Initializes a query matching all activity instances.
    *
    * @see #findAll()
    */
   public ActivityInstanceQuery()
   {
      super(FILTER_VERIFYER);
      setPolicy(new ModelVersionPolicy(false));
      setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      setPolicy(HistoricalStatesPolicy.NO_HIST_STATES);
   }

   protected ActivityInstanceQuery(WorklistQuery query)
   {
      super(query, new WorklistFilterCopier(FILTER_VERIFYER));

      getFilter().and(new ActivityStateFilter(
            new ActivityInstanceState[] {ActivityInstanceState.Suspended,
                                         ActivityInstanceState.Application}));
   }

   protected ActivityInstanceQuery(WorklistQuery query,
         PerformingUserFilter userPerformer)
   {
      super(query, new WorklistFilterCopier(FILTER_VERIFYER));

      getFilter().and(userPerformer).and(new ActivityStateFilter(
            new ActivityInstanceState[] {ActivityInstanceState.Suspended,
                                         ActivityInstanceState.Application}));
   }

   protected ActivityInstanceQuery(WorklistQuery query,
         PerformingParticipantFilter performer)
   {
      super(query, new WorklistFilterCopier(FILTER_VERIFYER));

      getFilter().and(performer).and(
            new ActivityStateFilter(ActivityInstanceState.Suspended));
   }

   /**
    * Activity instance attribute supporting filter operations.
    * <p />
    * Not for direct use.
    *
    */
   public static final class Attribute extends FilterableAttributeImpl
   {
      private Attribute(String name)
      {
         super(ActivityInstanceQuery.class, name);
      }
   }
}