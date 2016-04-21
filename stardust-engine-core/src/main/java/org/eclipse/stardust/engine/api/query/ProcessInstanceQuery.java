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
package org.eclipse.stardust.engine.api.query;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailProcessDefinitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;

/**
 * Query container for building complex queries for process instances.
 * <p/>
 * <p>Valid filter criteria are:
 * <ul>
 *    <li>{@link FilterTerm} for building complex criteria.</li>
 *    <li>{@link ProcessDefinitionFilter} for finding instances of specific process definitions.</li>
 *    <li>{@link ProcessInstanceFilter} for finding instances of specific process instance hierarchies.</li>
 *    <li>{@link ProcessStateFilter} for finding process instances currently being in specific states.</li>
 *    <li>{@link StartingUserFilter} for finding process instances being started by a specific user.</li>
 *    <li>{@link DataFilter} for finding process instances with same scope process instance containing specific workflow data.</li>
 *    <li>{@link SubProcessDataFilter} for finding process instances and its subprocess instances containing specific workflow data.</li>
 *    <li>{@link HierarchyDataFilter} for finding the complete hierarchy of process instances containing specific workflow data.</li>
 * </ul>
 * </p>
 *
 * @author rsauer
 * @version $Revision$
 */
public class ProcessInstanceQuery extends Query
{
   private static final String CASE_PROCESS_ID = "{" + PredefinedConstants.PREDEFINED_MODEL_ID + "}" + PredefinedConstants.CASE_PROCESS_ID;
   public static final Attribute OID = new Attribute(ProcessInstanceBean.FIELD__OID);
   public static final Attribute START_TIME = new Attribute(ProcessInstanceBean.FIELD__START_TIME);
   public static final Attribute TERMINATION_TIME = new Attribute(ProcessInstanceBean.FIELD__TERMINATION_TIME);
   public static final Attribute STATE = new Attribute(ProcessInstanceBean.FIELD__STATE);
   public static final Attribute PROCESS_DEFINITION_OID = new Attribute(ProcessInstanceBean.FIELD__PROCESS_DEFINITION);
   public static final Attribute ROOT_PROCESS_INSTANCE_OID = new Attribute(ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE);
   public static final Attribute STARTING_USER_OID = new Attribute(ProcessInstanceBean.FIELD__STARTING_USER);
   public static final Attribute STARTING_ACTIVITY_INSTANCE_OID = new Attribute(ProcessInstanceBean.FIELD__STARTING_ACTIVITY_INSTANCE);
   public static final Attribute PRIORITY = new Attribute(ProcessInstanceBean.FIELD__PRIORITY);
   public static final Attribute BENCHMARK_OID = new Attribute(ProcessInstanceBean.FIELD__BENCHMARK_OID);
   public static final Attribute BENCHMARK_VALUE = new Attribute(ProcessInstanceBean.FIELD__BENCHMARK_VALUE);

   /**
    * @deprecated This attribute existed in AuditTrail.
    */
   public static final Attribute STARTING_DOMAIN_OID = new Attribute("startingDomain");

   /**
    * @deprecated This attribute existed in AuditTrail.
    */
   public static final Attribute CURRENT_DOMAIN_OID = new Attribute("currentDomain");

   /**
    * Orders the resulting process instances by their process definition id.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion PROC_DEF_ID = new CustomOrderCriterion(
         AuditTrailProcessDefinitionBean.class,
         AuditTrailProcessDefinitionBean.FIELD__ID);

   /**
    * Orders the resulting process instances by their process definition name.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion PROC_DEF_NAME = new CustomOrderCriterion(
         AuditTrailProcessDefinitionBean.class,
         AuditTrailProcessDefinitionBean.FIELD__NAME);

   /**
    * Orders the resulting process instances by their starting user account.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion USER_ACCOUNT = new CustomOrderCriterion(
         UserBean.class, UserBean.FIELD__ACCOUNT);

   /**
    * Orders the resulting process instances by their starting user first name.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion USER_FIRST_NAME = new CustomOrderCriterion(
         UserBean.class, UserBean.FIELD__FIRST_NAME);

   /**
    * Orders the resulting process instances by their starting user last name.
    * <br/><br/>
    * For internal use only!
    */
   public static final CustomOrderCriterion USER_LAST_NAME = new CustomOrderCriterion(
         UserBean.class, UserBean.FIELD__LAST_NAME);

   protected static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[]
         {
            FilterTerm.class,
            UnaryOperatorFilter.class,
            BinaryOperatorFilter.class,
            TernaryOperatorFilter.class,
            ProcessDefinitionFilter.class,
            ProcessInstanceFilter.class,
            ProcessStateFilter.class,
            StartingUserFilter.class,
            DataFilter.class,
            SubProcessDataFilter.class,
            HierarchyDataFilter.class,
            DataPrefetchHint.class,
            CurrentPartitionFilter.class,
            ProcessInstanceLinkFilter.class,
            ProcessInstanceHierarchyFilter.class,
            DocumentFilter.class,
            RootProcessInstanceFilter.class            
         }),
         ProcessInstanceQuery.class
   );

   /**
    * Creates a query for finding all process instances currently existing.
    *
    * @return The readily configured query.
    */
   public static ProcessInstanceQuery findAll()
   {
      return new ProcessInstanceQuery();
   }

   /**
    * Creates a query for finding instances of the process definition identified by
    * <code>processID</code>. Also retrieves instances of subprocesses.
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @return The readily configured query.
    *
    * @see #findForProcess(String, boolean)
    * @see ProcessDefinitionFilter#ProcessDefinitionFilter(String)
    * @see #findInState(String, ProcessInstanceState)
    * @see #findAlive(String)
    */
   public static ProcessInstanceQuery findForProcess(String processID)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(new ProcessDefinitionFilter(processID));

      return query;
   }

   /**
    * Creates a query for finding instances of the process definition identified by
    * <code>processID</code>. Optionally retrieves subprocesses.
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @param includingSubprocesses Flag indicating if subprocesses should be retrieved,
    *       too.
    * @return The readily configured query.
    *
    * @see #findForProcess(String)
    * @see ProcessDefinitionFilter#ProcessDefinitionFilter(String, boolean)
    */
   public static ProcessInstanceQuery findForProcess(String processID,
         boolean includingSubprocesses)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter()
            .add(new ProcessDefinitionFilter(processID, includingSubprocesses));

      return query;
   }

   /**
    * Creates a query for finding case instances.
    *
    * @return The configured query.
    */
   public static ProcessInstanceQuery findCases()
   {
      return findForProcess(CASE_PROCESS_ID, false);
   }

   /**
    * Creates a query for finding case instances with the specified name.
    *
    * @param caseName The name of the case instance to search for.
    * @return The configured query.
    */
   public static ProcessInstanceQuery findCaseByName(String caseName)
   {
      ProcessInstanceQuery query = findCases();

      query.where(DataFilter.isEqual(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
            PredefinedConstants.CASE_NAME_ELEMENT, caseName));

      return query;
   }

   /**
    * Creates a query for finding instances belonging to the case instance with the specified oid.
    *
    * @param caseOid The oid of the case instance to search members for.
    * @return The configured query.
    */
   public static ProcessInstanceQuery findCaseMembers(long caseOid)
   {
      ProcessInstanceQuery query = findForProcess(CASE_PROCESS_ID);

      query.where(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(caseOid))
           .and(ProcessInstanceQuery.OID.notEqual(caseOid));

      return query;
   }

   /**
    * Creates a query for finding process instances currently being in the specified
    * state.
    *
    * @param state The state the process instance should be in.
    * @return The readily configured query.
    *
    * @see #findInState(String, ProcessInstanceState)
    * @see #findAlive()
    * @see ProcessStateFilter
    */
   public static ProcessInstanceQuery findInState(ProcessInstanceState state)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(new ProcessStateFilter(state));

      return query;
   }

   /**
    * Creates a query for finding process instances currently being in one of
    * the specified states.
    *
    * @param states The list of states the process instance should be in one of.
    * @return The readily configured query.
    *
    * @see #findInState(String, ProcessInstanceState[])
    * @see #findAlive()
    * @see ProcessStateFilter
    */
   public static ProcessInstanceQuery findInState(ProcessInstanceState[] states)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(new ProcessStateFilter(states));

      return query;
   }

   /**
    * Creates a query for finding instances of the process definition identified by
    * <code>processID</code> currently being in the specified state. Also retrieves instances of subprocesses.
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @param state The state the process instance should be in.
    * @return The readily configured query.
    *
    * @see #findInState(String, ProcessInstanceState[])
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see ProcessStateFilter
    */
   public static ProcessInstanceQuery findInState(String processID,
         ProcessInstanceState state)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(new ProcessDefinitionFilter(processID))
            .add(new ProcessStateFilter(state));

      return query;
   }

   /**
    * Creates a query for finding instances of the process definition identified by
    * <code>processID</code> currently being in one of the specified states. Also retrieves instances of subprocesses.
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @param states    The list of states the process instance should be in one of.
    * @return The readily configured query.
    *
    * @see #findInState(String, ProcessInstanceState)
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see ProcessStateFilter
    */
   public static ProcessInstanceQuery findInState(String processID,
         ProcessInstanceState[] states)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(new ProcessDefinitionFilter(processID))
            .add(new ProcessStateFilter(states));

      return query;
   }

   /**
    * Creates a query for finding alive process instances.
    * <p/>
    * <p>Alive means not being in states {@link ProcessInstanceState#ABORTED} or
    * {@link ProcessInstanceState#Completed}</p>
    *
    * @return The readily configured query.
    *
    * @see #findAlive(String)
    * @see #findInState(ProcessInstanceState[])
    * @see ProcessStateFilter
    * @see ProcessStateFilter#ALIVE
    */
   public static ProcessInstanceQuery findAlive()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(ProcessStateFilter.ALIVE);

      return query;
   }

   /**
    * Creates a query for finding active process instances.
    *
    * @return The readily configured query.
    * @see #findInterrupted()
    * @see #findCompleted()
    * @see #findAlive()
    * @see #findInState(ProcessInstanceState)
    * @see ProcessStateFilter
    * @see ProcessStateFilter#ALIVE
    */
   public static ProcessInstanceQuery findActive()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(ProcessStateFilter.ACTIVE);

      return query;
   }

   /**
    * Creates a query for finding pending process instances.
    *
    * @return The readily configured query.
    *
    * @see #findActive()
    * @see #findCompleted()
    * @see #findAlive()
    * @see #findInState(ProcessInstanceState)
    * @see ProcessStateFilter
    * @see ProcessStateFilter#ALIVE
    */
   public static ProcessInstanceQuery findInterrupted()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(ProcessStateFilter.INTERRUPTED);

      return query;
   }

   /**
    * Creates a query for finding completed process instances.
    * <p/>
    * <p>Completed means being in state {@link ProcessInstanceState#Completed}</p>
    *
    * @return The readily configured query.
    *
    * @see #findCompleted(String)
    * @see #findAlive()
    * @see #findInState(ProcessInstanceState)
    * @see ProcessStateFilter
    * @see ProcessStateFilter#COMPLETED
    */
   public static ProcessInstanceQuery findCompleted()
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(ProcessStateFilter.COMPLETED);

      return query;
   }

   /**
    * Creates a query for finding alive instances of the process definition identified by
    * <code>processID</code>. Also retrieves instances of subprocesses.
    * <p/>
    * <p>Alive means not being in states {@link ProcessInstanceState#ABORTED} or
    * {@link ProcessInstanceState#Completed}</p>
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @return The readily configured query.
    *
    * @see #findInState(String, ProcessInstanceState)
    * @see ProcessDefinitionFilter
    * @see ProcessStateFilter
    * @see ProcessStateFilter#ALIVE
    */
   public static ProcessInstanceQuery findAlive(String processID)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.where(new ProcessDefinitionFilter(processID))
            .and(ProcessStateFilter.ALIVE);

      return query;
   }

   /**
    * Creates a query for finding completed instances of the process definition identified
    * by <code>processID</code>. Also retrieves instances of subprocesses.
    * <p/>
    * <p>Completed means being in state {@link ProcessInstanceState#Completed}</p>
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @return The readily configured query.
    *
    * @see #findCompleted()
    * @see #findAlive(String)
    * @see #findInState(String, ProcessInstanceState)
    * @see ProcessDefinitionFilter
    * @see ProcessStateFilter
    * @see ProcessStateFilter#COMPLETED
    */
   public static ProcessInstanceQuery findCompleted(String processID)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.getFilter().add(new ProcessDefinitionFilter(processID))
            .add(ProcessStateFilter.COMPLETED);

      return query;
   }

   /**
    * Creates a query for finding instances of the process definition identified by
    * <code>processID</code> containing workflow data <code>dataID</code> having a value
    * of <code>dataValue</code> and currently being in the specified state. Also retrieves instances of subprocesses.
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @param dataID    The ID of the workflow data to match with.
    * @param dataValue The value to match the workflow data with.
    * @param state     The state the process instance should be in.
    * @return The readily configured query.
    *
    * @see #findInStateHavingData(String, String, Serializable, ProcessInstanceState[])
    * @see #findAliveHavingData
    * @see #findInState(String, ProcessInstanceState)
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see DataFilter
    * @see ProcessStateFilter
    */
   public static ProcessInstanceQuery findInStateHavingData(String processID,
         String dataID, Serializable dataValue, ProcessInstanceState state)
   {
      ProcessInstanceQuery query = findInState(processID, state);

      query.getFilter().add(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding instances of the process definition identified by
    * <code>processID</code> containing workflow data <code>dataID</code> having a value
    * of <code>dataValue</code> and currently being in one of the specified states.
    * Also retrieves instances of subprocesses.
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @param dataID    The ID of the workflow data to match with.
    * @param dataValue The value to match the workflow data with.
    * @param states    The list of states the process instance should be in one of.
    * @return The readily configured query.
    *
    * @see #findInStateHavingData(String, String, Serializable, ProcessInstanceState)
    * @see #findAliveHavingData
    * @see #findInState(ProcessInstanceState[])
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see DataFilter
    * @see ProcessStateFilter
    */
   public static ProcessInstanceQuery findInStateHavingData(String processID,
         String dataID, Serializable dataValue, ProcessInstanceState[] states)
   {
      ProcessInstanceQuery query = findInState(processID, states);

      query.getFilter().add(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding alive instances of the process definition identified by
    * <code>processID</code> containing workflow data <code>dataID</code> having a value
    * of <code>dataValue</code>. Also retrieves instances of subprocesses.
    * <p/>
    * <p>Alive means not being in states {@link ProcessInstanceState#ABORTED} or
    * {@link ProcessInstanceState#Completed}</p>
    *
    * @param processID The ID of the process definition the activity should belong to.
    * @param dataID    The ID of the workflow data to match with.
    * @param dataValue The value to match the workflow data with.
    * @return The readily configured query.
    *
    * @see #findInStateHavingData(String, String, Serializable, ProcessInstanceState[])
    * @see #findInState(String, ProcessInstanceState[])
    * @see #findAlive(String)
    * @see ProcessDefinitionFilter
    * @see DataFilter
    * @see ProcessStateFilter
    * @see ProcessStateFilter#ALIVE
    */
   public static ProcessInstanceQuery findAliveHavingData(String processID,
         String dataID, Serializable dataValue)
   {
      ProcessInstanceQuery query = findAlive(processID);

      query.getFilter().add(DataFilter.isEqual(dataID, dataValue));

      return query;
   }

   /**
    * Creates a query for finding process instances started by the user identified by the
    * given OID.
    *
    * @param userOID The OID of the user having started the process instances to find.
    * @return The readily configured query.
    *
    * @see StartingUserFilter
    * @see StartingUserFilter#CURRENT_USER
    */
   public static ProcessInstanceQuery findStartedByUser(long userOID)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.where(new StartingUserFilter(userOID));

      return query;
   }

   /**
    * Creates a query for finding process instances the given process instance is linked to via the given link type(s) and direction.
    *
    * @param piOid The OID of the process instance the query should be executed for
    * @param direction The direction of the links that should be taken into account when determining the linked process instances
    * @param linkType The link types that should be taken into account when determining the linked process instances
    * @return The readily configured query.
    */
   public static ProcessInstanceQuery findLinked(long processInstanceOid, LinkDirection direction, String ... linkType)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.where(new ProcessInstanceLinkFilter(processInstanceOid, direction, linkType));

      return query;
   }

   /**
    * Creates a query for finding process instances the given process instance is linked to via the given link type(s) and direction.
    *
    * @param piOid The OID of the process instance the query should be executed for
    * @param direction The direction of the links that should be taken into account when determining the linked process instances
    * @param linkType The link types that should be taken into account when determining the linked process instances
    * @return The readily configured query.
    */
   public static ProcessInstanceQuery findLinked(long processInstanceOid, LinkDirection direction, PredefinedProcessInstanceLinkTypes ... linkType)
   {
      String[] linkTypeIds = null;
      if (linkType != null)
      {
         linkTypeIds = new String[linkType.length];
         for (int i = 0; i < linkType.length; i++)
         {
            linkTypeIds[i] = linkType[i].getId();
         }
      }
      return findLinked(processInstanceOid, direction, linkTypeIds);
   }

   /**
    * Creates a query for finding process instances which have
    * the given Document as a process attachment, document data or document list data.
    *
    * @param document The Document to find process instances having a reference to.
    * @param modelId The model id (<code>null</code> searches all models).
    *
    * @return The readily configured query.
    */
   public static ProcessInstanceQuery findHavingDocument(Document document, String modelId)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.where(new DocumentFilter(document.getId(), modelId));

      return query;
   }

   /**
    * Creates a query for finding process instances which have
    * the given Document as a process attachment, document data or document list data.
    *
    * @param document The Document to find process instances having a reference to.
    *
    * @return The readily configured query.
    */
   public static ProcessInstanceQuery findHavingDocument(Document document)
   {
      return findHavingDocument(document, null);
   }

   /**
    * Creates a query for finding process instances which have
    * the given Document as a process attachment, document data or document list data.
    *
    * @param documentId The id of the Document to find process instances having a reference to.
    *
    * @return The readily configured query.
    */
   public static ProcessInstanceQuery findHavingDocument(String documentId)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query.where(new DocumentFilter(documentId, null));

      return query;
   }

   public ProcessInstanceQuery()
   {
      super(FILTER_VERIFYER);
      setPolicy(new ModelVersionPolicy(false));
      setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
   }

   /**
    * Process instance attribute supporting filter operations.
    * <p />
    * Not for direct use.
    *
    */
   public static final class Attribute extends FilterableAttributeImpl
   {
      private Attribute(String name)
      {
         super(ProcessInstanceQuery.class, name);
      }
   }

   @Override
   public String toString()
   {
      return "ProcessInstanceQuery: " + super.toString();
   }
}