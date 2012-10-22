/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.OneElementIterator;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataPath;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.DataClusterPrefetchUtil.StructuredDataEvaluaterInfo;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.query.DataClusterPrefetchUtil.StructuredDataPrefetchInfo;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.LogType;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.ComparisonTerm;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Functions;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Joins;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.AliasProjectionResultSet;
import org.eclipse.stardust.engine.core.persistence.jdbc.AliasProjectionResultSet.ValueType;
import org.eclipse.stardust.engine.core.persistence.jdbc.FieldDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.MultiplePersistentResultSet;
import org.eclipse.stardust.engine.core.persistence.jdbc.ResultSetIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.TableAliasDecorator;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler;
import org.eclipse.stardust.engine.core.runtime.beans.LogEntryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.GetDaemonLogAction;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.DataSlot;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ClusterAwareXPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluatorRegistry;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;


/**
 * Filter evaluator performing an exact match for any specified {@link DataFilter}, thus
 * possibly reducing the set of data retrieved by a previous data-base search.
 *
 * <p>Additionally it evaluates a possibly set {@link SubsetPolicy}.</p>
 *
 * @author rsauer
 * @version $Revision$
 */
public class ProcessQueryPostprocessor
{
   private static final Logger trace = LogManager.getLogger(ProcessQueryPostprocessor.class);

   private static final int PREFETCH_N_PARALLEL_DATA = 4;
   private static final int PREFETCH_BATCH_SIZE = 400;
   
   /**
    * Columns in data cluster per slot
    */
   private static final int DATA_CLUSTER_SLOT_COLOUMNS = 3;
   
   private static final SubsetPolicy FIRST_ELEMENT_SUBSET_POLICY = new SubsetPolicy(1);

   /**
    * Extracts the first process instance satisfying the query's filter condition from
    * the raw list of results.
    *
    * @param query The query the result is based on.
    * @param rawResult The source of process instances to postprocess.
    *
    * @return An implementation of {@link IProcessInstance} representing the found process
    *         instance, or <code>null</code> if no such can be found.
    *
    * @see #findMatchingProcessInstances
    * @see #findFirstMatchingProcessInstanceDetails
    */
   public static IProcessInstance findFirstMatchingProcessInstance(
         ProcessInstanceQuery query, ResultIterator rawResult)
         throws ObjectNotFoundException
   {
      SubsetPolicy subset = getOneElementSubset(query);

      RawQueryResult collector = packageProcessInstances(rawResult, subset, false, null);

      if (!collector.getItemList().isEmpty())
      {
         return (IProcessInstance) collector.getItemList().get(0);
      }
      throw new ObjectNotFoundException(
            BpmRuntimeError.ATDB_NO_MATCHING_PROCESS_INSTANCE.raise());
   }

   /**
    * Finds all process instances satisfying the query's filter criteria from the raw list
    * of results.
    *
    * @param query The query the result is based on.
    * @param rawResult The source of process instances to postprocess.
    *
    * @return A list of implementations of interface {@link IProcessInstance} describing
    *         the found process instances.
    *
    * @see #findFirstMatchingProcessInstance
    * @see #findMatchingProcessInstancesDetails
    */
   public static RawQueryResult findMatchingProcessInstances(
         ProcessInstanceQuery query, ResultIterator rawResult)
   {
      return packageProcessInstances(rawResult, QueryUtils.getSubset(query), false, null);
   }

   /**
    * Extracts the first process instance satisfying the query's filter condition from
    * the raw list of results.
    *
    * @param query       The query the result is based on.
    * @param rawResult   The source of process instances to postprocess.
    * @param targetClass
    * @return An instance of {@link ProcessInstanceDetails} describing the found process
    *         instance, or <code>null</code> if no such can be found.
    *
    * @see #findMatchingProcessInstancesDetails
    * @see #findFirstMatchingProcessInstance
    */
   public static Object findFirstMatchingProcessInstanceDetails(
         ProcessInstanceQuery query, ResultIterator rawResult, Class targetClass)
   {
      IProcessInstance process = findFirstMatchingProcessInstance(query, rawResult);

      DescriptorPolicy descriptorPolicy = (DescriptorPolicy) query.getPolicy(DescriptorPolicy.class);
      if (null == descriptorPolicy)
      {
         descriptorPolicy = DescriptorPolicy.NO_DESCRIPTORS;
      }

      ProcessInstanceDetailsLevel level = ProcessInstanceDetailsLevel.Default;
      EnumSet<ProcessInstanceDetailsOptions> detailsOptions = EnumSet
            .noneOf(ProcessInstanceDetailsOptions.class);
      ProcessInstanceDetailsPolicy policy = (ProcessInstanceDetailsPolicy) query
            .getPolicy(ProcessInstanceDetailsPolicy.class);
      if (null != policy)
      {
         level = policy.getLevel();
         detailsOptions = policy.getOptions();
      }

      HistoricalEventPolicy eventPolicy = (HistoricalEventPolicy) query
         .getPolicy(HistoricalEventPolicy.class);

      PropertyLayer props = ParametersFacade.pushLayer(null);
      try
      {
         if (eventPolicy != null)
         {
            int eventTypes = eventPolicy.getEventTypes();
            props.setProperty(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES,
                  new Integer(eventTypes));

            if (isEventTypeSet(eventTypes, HistoricalEventType.EXCEPTION))
            {
               prefetchLogEntries(new OneElementIterator(process), QueryUtils
                     .getTimeOut(query));
            }
         }
         props.setProperty(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS,
               Boolean.valueOf(descriptorPolicy.includeDescriptors()));
         props.setProperty(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL, level);
         props.setProperty(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS, detailsOptions);

         prefetchStartingUsers(new OneElementIterator(process),
               QueryUtils.getTimeOut(query));

         if (detailsOptions.contains(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO))
         {
            prefetchStartingActivityInstances(new OneElementIterator(process),
                  QueryUtils.getTimeOut(query));
         }

         if (descriptorPolicy.includeDescriptors())
         {
            // prefetching of scopePI done during descriptor value prefetch
            prefetchDescriptorValues(new OneElementIterator(process),
                  QueryUtils.getTimeOut(query), descriptorPolicy);
         }
         else
         {
            // prefetching done explicitly
            if (process.getOID() != process.getScopeProcessInstanceOID())
            {
               prefetchProcessInstances(
                     Collections.singleton(process.getScopeProcessInstanceOID()),
                     QueryUtils.getTimeOut(query), false);
            }
         }

         return DetailsFactory.create(process, IProcessInstance.class, targetClass);
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   /**
    * Finds all process instances satisfying the query's filter criteria from the raw
    * list of results.
    *
    * @param query The query the result is based on.
    * @param rawResult The source of process instances to postprocess.
    * @param targetClass
    * @return A list of instances of class {@link ProcessInstanceDetails} describing the
    *         found process instances.
    *
    * @see #findFirstMatchingProcessInstanceDetails
    * @see #findMatchingProcessInstances
    */
   public static ProcessInstances findMatchingProcessInstancesDetails(
         ProcessInstanceQuery query, ResultIterator rawResult, Class targetClass)
   {
      RawQueryResult queryResult = findMatchingProcessInstances(query, rawResult);

      DescriptorPolicy descriptorPolicy = (DescriptorPolicy) query.getPolicy(DescriptorPolicy.class);
      if (null == descriptorPolicy)
      {
         descriptorPolicy = DescriptorPolicy.NO_DESCRIPTORS;
      }

      ProcessInstanceDetailsLevel level = ProcessInstanceDetailsLevel.Default;
      EnumSet<ProcessInstanceDetailsOptions> detailsOptions = EnumSet
            .noneOf(ProcessInstanceDetailsOptions.class);
      ProcessInstanceDetailsPolicy policy = (ProcessInstanceDetailsPolicy) query
            .getPolicy(ProcessInstanceDetailsPolicy.class);
      if (null != policy)
      {
         level = policy.getLevel();
         detailsOptions = policy.getOptions();
      }

      HistoricalEventPolicy eventPolicy = (HistoricalEventPolicy) query
         .getPolicy(HistoricalEventPolicy.class);

      PropertyLayer props = ParametersFacade.pushLayer(null);
      try
      {
         if(eventPolicy != null)
         {
            int eventTypes = eventPolicy.getEventTypes();
            props.setProperty(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES, new Integer(eventTypes));
            if(isEventTypeSet(eventTypes, HistoricalEventType.EXCEPTION))
            {
               prefetchLogEntries(queryResult.iterator(), QueryUtils.getTimeOut(query));
            }
         }
         props.setProperty(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS,
               Boolean.valueOf(descriptorPolicy.includeDescriptors()));
         props.setProperty(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL, level);
         props.setProperty(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS, detailsOptions);

         prefetchStartingUsers(queryResult.iterator(), QueryUtils.getTimeOut(query));

         if (detailsOptions.contains(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO))
         {
            prefetchStartingActivityInstances(queryResult.iterator(),
                  QueryUtils.getTimeOut(query));
         }

         if (descriptorPolicy.includeDescriptors())
         {
            // prefetching of scopePI done during descriptor value prefetch
            prefetchDescriptorValues(queryResult.iterator(), QueryUtils.getTimeOut(query), descriptorPolicy);
         }
         else
         {
            // prefetching done explicitly
            Set<Long> scopePIs = CollectionUtils.newHashSet();
            for (Object rawPi : queryResult)
            {
               IProcessInstance pi = (IProcessInstance) rawPi;
               if (pi.getOID() != pi.getScopeProcessInstanceOID())
               {
                  scopePIs.add(pi.getScopeProcessInstanceOID());
               }
            }
            prefetchProcessInstances(scopePIs, QueryUtils.getTimeOut(query), false);
         }

         List processDetails = new ArrayList(queryResult.size());
         for (Iterator i = queryResult.iterator(); i.hasNext();)
         {
            processDetails.add(DetailsFactory.create(i.next(), IProcessInstance.class,
                  targetClass));
         }

         return new ProcessInstances(query, new RawQueryResult(processDetails,
               queryResult.getSubsetPolicy(), queryResult.hasMore(),
               queryResult.hasTotalCount() //
                     ? new Long(queryResult.getTotalCount())
                     : null));
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   /**
    * Extracts the first activity instance satisfying the query's filter condition from
    * the raw list of results.
    *
    * @param query The query the result is based on.
    * @param rawResult The source of activity instances to postprocess.
    *
    * @return An implementation of {@link IActivityInstance} representing the found
    *         activity instance, or <code>null</code> if no such can be found.
    *
    * @see #findMatchingActivityInstances
    * @see #findFirstMatchingActivityInstanceDetails
    */
   public static IActivityInstance findFirstMatchingActivityInstance(
         ActivityInstanceQuery query, ResultIterator rawResult)
   {
      RawQueryResult collector = retrieveActivityInstances(rawResult,
            getOneElementSubset(query));

      if (!collector.getItemList().isEmpty())
      {
         return (IActivityInstance) collector.getItemList().get(0);
      }
      throw new ObjectNotFoundException(
            BpmRuntimeError.ATDB_NO_MATCHING_ACTIVITY_INSTANCE.raise());
   }

   /**
    * Finds all activity instances satisfying the query's filter criteria from the raw
    * list of results.
    *
    * @param query The query the result is based on.
    * @param rawResult The source of activity instances to postprocess.
    *
    * @return A list of implementations of class {@link IActivityInstance} describing the
    *         found activity instances.
    *
    * @see #findFirstMatchingActivityInstance
    * @see #findMatchingActivityInstanceDetails
    */
   public static RawQueryResult findMatchingActivityInstances(
         ActivityInstanceQuery query, ResultIterator rawResult)
   {
      RawQueryResult queryResult = retrieveActivityInstances(rawResult,
            QueryUtils.getSubset(query));

      prefetchProcessInstances(queryResult.iterator(), QueryUtils.getTimeOut(query));

      return queryResult;
   }

   /**
    * Extracts the first activity instance satisfying the query's filter condition from
    * the raw list of results.
    *
    * @param query The query the result is based on.
    * @param rawResult The source of activity instances to postprocess.
    * @param targetClass
    *
    * @return An instance of {@link ActivityInstanceDetails} describing the found activity
    *         instance, or <code>null</code> if no such can be found.
    *
    * @see #findMatchingActivityInstanceDetails
    */
   public static Object findFirstMatchingActivityInstanceDetails(
         ActivityInstanceQuery query, ResultIterator rawResult, Class targetClass)
   {
      IActivityInstance activity = findFirstMatchingActivityInstance(query, rawResult);

      Object result = null;
      if (null != activity)
      {
         DescriptorPolicy descriptorPolicy = (DescriptorPolicy) query
               .getPolicy(DescriptorPolicy.class);
         if (null == descriptorPolicy)
         {
            descriptorPolicy = DescriptorPolicy.WITH_DESCRIPTORS;
         }
         HistoricalStatesPolicy statesPolicy = (HistoricalStatesPolicy) query
               .getPolicy(HistoricalStatesPolicy.class);
         if (null == statesPolicy)
         {
            statesPolicy = HistoricalStatesPolicy.NO_HIST_STATES;
         }
         HistoricalEventPolicy eventPolicy = (HistoricalEventPolicy) query
            .getPolicy(HistoricalEventPolicy.class);

         PropertyLayer props = ParametersFacade.pushLayer(null);
         try
         {
            if (eventPolicy != null)
            {
               int eventTypes = eventPolicy.getEventTypes();
               props.setProperty(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES,
                     new Integer(eventTypes));

               if (isEventTypeSet(eventTypes, HistoricalEventType.DELEGATION)
                     || isEventTypeSet(eventTypes, HistoricalEventType.STATE_CHANGE))
               {
                  statesPolicy = HistoricalStatesPolicy.WITH_HIST_STATES;
               }
               if (isEventTypeSet(eventTypes, HistoricalEventType.EXCEPTION))
               {
                  prefetchLogEntries(new OneElementIterator(activity), QueryUtils
                        .getTimeOut(query));
               }
            }
            props.setProperty(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, Boolean
                  .valueOf(descriptorPolicy.includeDescriptors()));
            props.setProperty(HistoricalStatesPolicy.PRP_PROPVIDE_HIST_STATES, statesPolicy);

            prefetchProcessInstances(new OneElementIterator(activity),
                  QueryUtils.getTimeOut(query));
            prefetchDescriptorValues(new OneElementIterator(activity),
                  QueryUtils.getTimeOut(query), descriptorPolicy);
            result = DetailsFactory.create(activity, IActivityInstance.class, targetClass);
         }
         finally
         {
            ParametersFacade.popLayer();
         }
      }
      return result;
   }

   /**
    * Finds all activity instances satisfying the query's filter criteria from the raw
    * list of results.
    *
    * @param query The query the result is based on.
    * @param rawResult The source of activity instances to postprocess.
    *
    * @return A list of instances of class {@link org.eclipse.stardust.engine.api.runtime.ActivityInstance} describing the
    *         found activity instances.
    *
    * @see #findFirstMatchingActivityInstanceDetails
    */
   public static ActivityInstances findMatchingActivityInstanceDetails(
         ActivityInstanceQuery query, ResultIterator rawResult)
   {
      RawQueryResult queryResult = findMatchingActivityInstances(query, rawResult);

      DescriptorPolicy descriptorPolicy = (DescriptorPolicy) query
            .getPolicy(DescriptorPolicy.class);
      if (null == descriptorPolicy)
      {
         descriptorPolicy = DescriptorPolicy.WITH_DESCRIPTORS;
      }
      HistoricalStatesPolicy statesPolicy = (HistoricalStatesPolicy) query
            .getPolicy(HistoricalStatesPolicy.class);
      if (null == statesPolicy)
      {
         statesPolicy = HistoricalStatesPolicy.NO_HIST_STATES;
      }
      HistoricalEventPolicy eventPolicy = (HistoricalEventPolicy) query
            .getPolicy(HistoricalEventPolicy.class);

      PropertyLayer props = ParametersFacade.pushLayer(null);
      try
      {
         if (eventPolicy != null)
         {
            int eventTypes = eventPolicy.getEventTypes();
            props.setProperty(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES,
                  new Integer(eventTypes));
            if (isEventTypeSet(eventTypes, HistoricalEventType.DELEGATION)
                  || isEventTypeSet(eventTypes, HistoricalEventType.STATE_CHANGE))
            {
               statesPolicy = HistoricalStatesPolicy.WITH_HIST_STATES;
            }
            if (isEventTypeSet(eventTypes, HistoricalEventType.EXCEPTION))
            {
               prefetchLogEntries(queryResult.iterator(), QueryUtils.getTimeOut(query));
            }
         }
         props.setProperty(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, Boolean
               .valueOf(descriptorPolicy.includeDescriptors()));
         props.setProperty(HistoricalStatesPolicy.PRP_PROPVIDE_HIST_STATES, statesPolicy);

         // process instances are prefetched inside findMatchingActivityInstances
         prefetchDescriptorValues(queryResult.iterator(), QueryUtils.getTimeOut(query), descriptorPolicy);

         List activityDetails = DetailsFactory.createCollection(queryResult.iterator(),
               IActivityInstance.class, ActivityInstanceDetails.class);

         return new ActivityInstances(query, new RawQueryResult(activityDetails,
               queryResult.getSubsetPolicy(), queryResult.hasMore(),
               queryResult.hasTotalCount() ? new Long(queryResult.getTotalCount()) : null));
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   private ProcessQueryPostprocessor()
   {
   }

   private static SubsetPolicy getOneElementSubset(Query query)
   {
      SubsetPolicy subset = (SubsetPolicy) query.getPolicy(SubsetPolicy.class);
      if (null != subset)
      {
         subset = new SubsetPolicy(1, subset.getSkippedEntries());
      }
      else
      {
         subset = FIRST_ELEMENT_SUBSET_POLICY;
      }
      return subset;
   }

   private static RawQueryResult packageProcessInstances(ResultIterator rawResult,
         SubsetPolicy subset, boolean deliverDetails, Class targetClass)
   {
      List result = new ArrayList();

      int retrievedEntries = 0;

      boolean hasMore = false;

      while (rawResult.hasNext())
      {
         IProcessInstance processInstance = (IProcessInstance) rawResult.next();
         if (deliverDetails)
         {
            result.add(DetailsFactory.create(processInstance,
                  IProcessInstance.class, targetClass));
         }
         else
         {
            result.add(processInstance);
         }
         ++retrievedEntries;

         if ((null != subset) && (retrievedEntries >= subset.getMaxSize()))
         {
            hasMore = rawResult.hasNext() || rawResult.hasMore();
            break;
         }
      }
      return new RawQueryResult(result, subset, hasMore, rawResult.hasTotalCount()
            ? new Long(rawResult.getTotalCount())
            : null);
   }

   private static RawQueryResult retrieveActivityInstances(ResultIterator rawResult,
         SubsetPolicy subset)
   {
      List result = new ArrayList();

      int retrievedEntries = 0;

      boolean hasMore = false;

      while (rawResult.hasNext())
      {
         IActivityInstance activityInstance = (IActivityInstance) rawResult.next();

         result.add(activityInstance);

         ++retrievedEntries;

         if ((null != subset) && (retrievedEntries >= subset.getMaxSize()))
         {
            hasMore = rawResult.hasNext() || rawResult.hasMore();
            break;
         }
      }

      return new RawQueryResult(result, subset, hasMore, rawResult.hasTotalCount()
            ? new Long(rawResult.getTotalCount())
            : null);
   }

   private static void prefetchStartingUsers(Iterator piItr, int timeout)
   {
      Set piSet = new HashSet();
      while (piItr.hasNext())
      {
         piSet.add(new Long(((ProcessInstanceBean) piItr.next()).getStartingUserOID()));
      }

      // This term reference will be used for later modification of its value expression
      ComparisonTerm termTemplate = Predicates.inList(UserBean.FR__OID, new ArrayList());

      QueryExtension queryExtension = QueryExtension.where(termTemplate);

      if (trace.isDebugEnabled())
      {
         trace.debug("Prefetching " + piSet.size() + " user(s)");
      }

      int instancesBatchSize = Parameters.instance().getInteger(
            KernelTweakingProperties.USER_PREFETCH_N_PARALLEL_INSTANCES,
            Parameters.instance().getInteger(
                  KernelTweakingProperties.DESCRIPTOR_PREFETCH_BATCH_SIZE,
                  PREFETCH_BATCH_SIZE));
      performPrefetch(UserBean.class, queryExtension, termTemplate, piSet, true, timeout,
            instancesBatchSize);
   }

   private static void prefetchStartingActivityInstances(
         Iterator<IProcessInstance> piItr, int timeout)
   {
      Set<Long> aiSet = new HashSet();
      while (piItr.hasNext())
      {
         aiSet.add(new Long(piItr.next().getStartingActivityInstanceOID()));
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Prefetching " + aiSet.size() + " starting activity instance(s)");
      }

      // This term reference will be used for later modification of its value expression
      ComparisonTerm termTemplate = Predicates.inList(ActivityInstanceBean.FR__OID,
            Collections.EMPTY_LIST.iterator());

      QueryExtension queryExtension = QueryExtension.where(termTemplate);

      // TODO: Change property names
      int instancesBatchSize = Parameters.instance().getInteger(
            KernelTweakingProperties.PROCESS_PREFETCH_N_PARALLEL_INSTANCES,
            Parameters.instance().getInteger(
                  KernelTweakingProperties.DESCRIPTOR_PREFETCH_BATCH_SIZE,
                  PREFETCH_BATCH_SIZE));
      performPrefetch(ActivityInstanceBean.class, queryExtension, termTemplate, aiSet,
            true, timeout, instancesBatchSize);
   }

   private static void prefetchProcessInstances(Iterator aiItr, int timeout)
   {
      Set piSet = new HashSet();
      while (aiItr.hasNext())
      {
         piSet.add(new Long(((IActivityInstance) aiItr.next()).getProcessInstanceOID()));
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Prefetching " + piSet.size() + " process instance(s)");
      }

      prefetchProcessInstances(piSet, timeout, true);
   }

   private static void prefetchProcessInstances(Set piSet, int timeout, boolean includeScopeProcesses)
   {
      // This term reference will be used for later modification of its value expression
      ComparisonTerm termTemplate = Predicates.inList(ProcessInstanceBean.FR__OID,
            Collections.EMPTY_LIST.iterator());

      QueryExtension queryExtension = QueryExtension.where(termTemplate);


      int instancesBatchSize = Parameters.instance().getInteger(
            KernelTweakingProperties.PROCESS_PREFETCH_N_PARALLEL_INSTANCES,
            Parameters.instance().getInteger(
                  KernelTweakingProperties.DESCRIPTOR_PREFETCH_BATCH_SIZE,
                  PREFETCH_BATCH_SIZE));
      performPrefetch(ProcessInstanceBean.class, queryExtension, termTemplate, piSet,
            true, timeout, instancesBatchSize);

      if(includeScopeProcesses)
      {
         Set scopePiSet = new HashSet();
         for(Iterator iter = piSet.iterator(); iter.hasNext();)
         {
            Long piOid = (Long) iter.next();
            IProcessInstance pi = ProcessInstanceBean.findByOID(piOid.longValue());
            Long scopePiOid = new Long(pi.getScopeProcessInstanceOID());
            if(!piSet.contains(scopePiOid))
            {
               scopePiSet.add(scopePiOid);
            }
         }
         trace.debug("Prefetching " + scopePiSet.size() + " scope process instance(s)");
         performPrefetch(ProcessInstanceBean.class, queryExtension, termTemplate, scopePiSet,
               true, timeout, instancesBatchSize);
      }
   }

   private static void prefetchLogEntries(Iterator instanceItr, int timeout)
   {
      ComparisonTerm piTermTemplate = Predicates.inList(
            LogEntryBean.FR__PROCESS_INSTANCE, Collections.EMPTY_LIST);
      ComparisonTerm aiTermTemplate = Predicates.inList(
            LogEntryBean.FR__ACTIVITY_INSTANCE, Collections.EMPTY_LIST);

      Set piSet = CollectionUtils.newSet();
      Set aiSet = CollectionUtils.newSet();

      while (instanceItr.hasNext())
      {
         IdentifiablePersistent instance = (IdentifiablePersistent) instanceItr.next();

         IActivityInstance ai;
         IProcessInstance pi;
         if (instance instanceof IActivityInstance)
         {
            ai = (IActivityInstance) instance;
            aiSet.add(new Long(ai.getOID()));
            // calling getProcessInstance() is assumed to be cheap as of a previous
            // process instance prefetch
            pi = ai.getProcessInstance();
            if (pi != null)
            {
               piSet.add(new Long(((IProcessInstance) pi).getOID()));
            }
         }
         else if (instance instanceof IProcessInstance)
         {
            ai = null;
            piSet.add(new Long(((IProcessInstance) instance).getOID()));
         }
         else
         {
            throw new InternalException("Unsupported log entry context " + instance);
         }
      }

      // Reduce log entries to those not DEBUG and not INFO, not necessary for HistoricalEventType.Exception.
      final ComparisonTerm notDebugAndInfo = Predicates.notInList(LogEntryBean.FR__TYPE,
            new int[] { LogType.DEBUG, LogType.INFO });
      performPrefetch(LogEntryBean.class, //
            QueryExtension.where(Predicates.andTerm(piTermTemplate, notDebugAndInfo)), //
            piTermTemplate, piSet, false, timeout, PREFETCH_BATCH_SIZE);
      performPrefetch(LogEntryBean.class, //
            QueryExtension.where(Predicates.andTerm(aiTermTemplate, notDebugAndInfo)), //
            aiTermTemplate, aiSet, false, timeout, PREFETCH_BATCH_SIZE);
   }

   private static void prefetchDescriptorValues(Iterator instanceItr, int timeout, DescriptorPolicy descriptorPolicy)
   {
      final double prefetchDataDiscriminationThreshold = Parameters.instance().getDouble(
            KernelTweakingProperties.DESCRIPTOR_PREFETCH_DATA_DISCRIMINATION_THRESHOLD,
            1.0);

      ModelManager modelManager = ModelManagerFactory.getCurrent();

      Map dataPrefetchFilter = new HashMap();

      //contains which structured data entries can be prefetched from the datacluster
      List<StructuredDataPrefetchInfo> dcStructuredDataInClusterPrefetchInfo
         = new ArrayList<StructuredDataPrefetchInfo>();
      //contains which evaluator to use for given data & xpath
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      ExtendedAccessPathEvaluatorRegistry evaluatorRegistry
         = new ExtendedAccessPathEvaluatorRegistry();
      rtEnv.setEvaluatorRegistry(evaluatorRegistry);
      
      while (instanceItr.hasNext())
      {
         IdentifiablePersistent instance = (IdentifiablePersistent) instanceItr.next();

         IActivityInstance ai;
         IProcessInstance pi;
         if (instance instanceof IActivityInstance)
         {
            ai = (IActivityInstance) instance;
            // calling getProcessInstance() is assumed to be cheap as of a previous
            // process instance prefetch
            pi = ai.getProcessInstance();
         }
         else if (instance instanceof IProcessInstance)
         {
            ai = null;
            pi = (IProcessInstance) instance;
         }
         else
         {
            throw new InternalException("Unsupported descriptor context " + instance);
         }
         
         IProcessDefinition pd = pi.getProcessDefinition();

         Pair pdSlice = (Pair) dataPrefetchFilter.get(pd);
         if (null == pdSlice)
         {
            Set<Long> dataRtOidsStructuredNoCluster = new TreeSet<Long>();
            Map<Long, IData> dataRtOidsNonStructured = new TreeMap<Long, IData>();
            Pair dataRtOids = new Pair(dataRtOidsStructuredNoCluster, dataRtOidsNonStructured);
            pdSlice = new Pair(dataRtOids, new TreeSet());
            dataPrefetchFilter.put(pd, pdSlice);

            boolean prefetchDescriptors = ParametersFacade.instance()
               .getBoolean(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, true);
            if(prefetchDescriptors)
            {
               ParametersFacade.instance().set(IDescriptorProvider.PRP_DESCRIPTOR_IDS, descriptorPolicy.getDescriptorIds());
               Set<String> descriptorIds = descriptorPolicy.getDescriptorIds();
               boolean limitDescriptors = descriptorIds != null && !descriptorIds.isEmpty();
               // evaluate data used for descriptors only once per process definition
               for (Iterator i = pd.getAllDescriptors(); i.hasNext();)
               {
                  IDataPath descriptor = (IDataPath) i.next();
                  if ( !limitDescriptors || descriptorIds.contains(descriptor.getId()))
                  {
                     IData data = descriptor.getData();
                     if ((null != data) && !ProcessInstanceBean.isMetaData(data.getId()))
                     {
                        if (StructuredTypeRtUtils.isDmsType(data.getType().getId())
                              || StructuredTypeRtUtils.isStructuredType(data.getType()
                                    .getId()))
                        {
                           String dataXPath = descriptor.getAccessPath();
                           List<StructuredDataPrefetchInfo> dataPrefetchInfo
                              = DataClusterPrefetchUtil.getPrefetchInfo(data, dataXPath);
                           if(!dataPrefetchInfo.isEmpty())
                           {
                              //this data can be fetched from datacluster
                              dcStructuredDataInClusterPrefetchInfo.addAll(dataPrefetchInfo);
                              //rember the data and the xpath for which the custom evaluator will be used 
                              evaluatorRegistry.register(data, dataXPath, ClusterAwareXPathEvaluator.class);
                           }
                           else
                           {
                              dataRtOidsStructuredNoCluster.add(new Long(
                                    modelManager.getRuntimeOid(data)));
                           }
                        }
                        // always add to non structured, even if it is a structured data
                        // (because the data_value entry
                        // is also needed to fetch structured data value)
                        dataRtOidsNonStructured.put(
                              new Long(modelManager.getRuntimeOid(data)), data);
                     }
                  }
               }
            }
         }

         Set piSet = (Set) pdSlice.getSecond();

         piSet.add(new Long(pi.getScopeProcessInstanceOID()));

         // prefetch data used for conditonal default performers
         if (null != ai)
         {
            IModelParticipant defaultPerformer = ai.getActivity().getPerformer();
            if (defaultPerformer instanceof IConditionalPerformer)
            {
               IConditionalPerformer conditionalDefaultPerformer = (IConditionalPerformer) defaultPerformer;
               IData data = conditionalDefaultPerformer.getData();
               if ((null != data) && !ProcessInstanceBean.isMetaData(data.getId()))
               {
                  if (StructuredTypeRtUtils.isDmsType(data.getType().getId())
                        || StructuredTypeRtUtils.isStructuredType(data.getType().getId()))
                  {
                     // slice -> first -> first contains dataOids of structured data only
                     Set<Long> dataRtOids = (Set<Long>) ((Pair)pdSlice.getFirst()).getFirst();
                     dataRtOids.add(new Long(modelManager.getRuntimeOid(data)));
                  }
                  // always add to non structured, even if it is a structured data (because the data_value entry
                  // is also needed to fetch structured data value)
                  // slice -> first -> second contains dataOids of all data
                  Map<Long, IData> dataRtOids = (Map<Long, IData>) ((Pair)pdSlice.getFirst()).getSecond();
                  dataRtOids.put(new Long(modelManager.getRuntimeOid(data)), data);
               }
            }
         }
      }

      final int prefetchNParallelData = Parameters.instance().getInteger(
            KernelTweakingProperties.DESCRIPTOR_PREFETCH_N_PARALLEL_DATA,
            PREFETCH_N_PARALLEL_DATA);
      final int prefetchNParallelInstances = Parameters.instance().getInteger(
            KernelTweakingProperties.DESCRIPTOR_PREFETCH_N_PARALLEL_INSTANCES,
            Parameters.instance().getInteger(
                  KernelTweakingProperties.DESCRIPTOR_PREFETCH_BATCH_SIZE,
                  PREFETCH_BATCH_SIZE));

      for (Iterator filterItr = dataPrefetchFilter.entrySet().iterator(); filterItr
            .hasNext();)
      {
         final Map.Entry filter = (Map.Entry) filterItr.next();
         final IProcessDefinition pd = (IProcessDefinition) filter.getKey();
         final Pair pdSlice = (Pair) filter.getValue();

         // count total number of data
         // TODO restrict to data used in the process definition closure
         int nData = 0;
         for (Iterator dataItr = ((IModel) pd.getModel()).getAllData(); dataItr.hasNext();)
         {
            dataItr.next();
            ++nData;
         }

         final Pair dataRtOids = (Pair) pdSlice.getFirst();
         final Set<Long> dataRtOidsStructured = (Set<Long> ) dataRtOids.getFirst();
         final Map<Long, IData> dataRtOidsNonStructured = (Map<Long, IData>) dataRtOids.getSecond();
         final Set<Long> piSet = (Set) pdSlice.getSecond();
         if (!dataRtOidsStructured.isEmpty() || !dataRtOidsNonStructured.isEmpty())
         {
            final HashSet<Long> prefetchedPiOids = CollectionUtils.newHashSet();
            final HashSet<Long> prefetchedDataRtOids = CollectionUtils.newHashSet();
                                 
            if( !dcStructuredDataInClusterPrefetchInfo.isEmpty() && !piSet.isEmpty())
            {
               performPrefetchStructuredFromDataCluster(dcStructuredDataInClusterPrefetchInfo,
                     piSet, prefetchNParallelInstances);
            }
            
            if ( !dataRtOidsNonStructured.isEmpty() && !piSet.isEmpty())
            {
               performPrefetchNonStructuredFromDataCluster(dataRtOidsNonStructured,
                     piSet, prefetchNParallelInstances, pd, timeout, prefetchedPiOids,
                     prefetchedDataRtOids);
            }
            
            // prefetching remaining scope PIs to prevent persistence framework from performing an
            // inefficient star fetch during data value load (fixes #3712)
            if (trace.isDebugEnabled())
            {
               trace.debug("Prefetching remaining scope process instance(s) for "
                     + piSet.size() + " instance(s) of process " + pd + ".");
            }
            HashSet<Long> remainingPiSet = new HashSet<Long>(piSet);
            remainingPiSet.removeAll(prefetchedPiOids);
            prefetchProcessInstances(remainingPiSet, timeout, false);

            if (trace.isDebugEnabled())
            {
               trace.debug("Prefetching descriptor value(s) for " + piSet.size()
                     + " instance(s) of process " + pd + ".");
            }
            
            // remove any data cluster prefetched data RT oid which will prevent duplicate prefetching
            CollectionUtils.remove(dataRtOidsNonStructured, prefetchedDataRtOids);
            
            if ( !dataRtOidsNonStructured.isEmpty())
            {
               performPrefetchNonStructured(dataRtOidsNonStructured, nData, piSet,
                     timeout, prefetchNParallelData, prefetchNParallelInstances,
                     prefetchDataDiscriminationThreshold);
            }

            if (!dataRtOidsStructured.isEmpty())
            {
               performPrefetchStructured(dataRtOidsStructured, nData, piSet, timeout,
                     prefetchNParallelData, prefetchNParallelInstances,
                     prefetchDataDiscriminationThreshold);
            }
         }
      }
   }
    
   private static void performPrefetchStructuredFromDataCluster(
         List<StructuredDataPrefetchInfo> prefetchInfo, 
         Collection<Long> piSet,
         int prefetchNParallelInstances)
   {
      boolean useDataClusters = Parameters.instance().getBoolean(
            KernelTweakingProperties.DESCRIPTOR_PREFETCH_USE_DATACLUSTER, false);
      if ( !useDataClusters || RuntimeSetup.instance().getDataClusterSetup().length == 0)
      {
         return;
      }

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      org.eclipse.stardust.engine.core.persistence.jdbc.Session 
         jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;     
      ResultSet baseResultSet = null;
      
      //selected fields
      List<Column> allColumns = new ArrayList<Column>();
      List<FieldRef> defaultFields = SqlUtils.getDefaultSelectFieldList(TypeDescriptor.get(ProcessInstanceBean.class));
      allColumns.addAll(defaultFields);
      //joins to cluster table
      Joins dcJoins = new Joins();
      List<AliasProjectionResultSetInfo> sdvProjectionInfo 
         = new ArrayList<AliasProjectionResultSetInfo>();

      List<List<Long>> piSelections = CollectionUtils.split(new ArrayList<Long>(piSet),
            prefetchNParallelInstances);
      int i = 0;
      for(List<Long> piSelection: piSelections)
      {
         ComparisonTerm piSelectionTerm = Predicates.inList(
               ProcessInstanceBean.FR__OID, piSelection.iterator());
         for(StructuredDataPrefetchInfo info: prefetchInfo)
         {    
            boolean stringValueColumnMapped = false;
            boolean numberValueColumnMapped = false;
            
            i++;
            DataCluster cluster = info.getCluster();
            DataSlot slot = info.getDataslot();
            String joinAlias = "dc"+i;
            
            Join dcJoin = new Join(cluster, joinAlias);
            dcJoin = dcJoin.on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE, cluster
                  .getProcessInstanceColumn());
            dcJoins.add(dcJoin);
            
            List<Column> additionalSelectColumns = new ArrayList<Column>();

            additionalSelectColumns.add(new FieldRef(dcJoin, slot.getTypeColumn()));
            if (StringUtils.isNotEmpty(slot.getSValueColumn()))
            {
               stringValueColumnMapped = true;
               additionalSelectColumns.add(new FieldRef(dcJoin, slot.getSValueColumn()));
            }   
            if (StringUtils.isNotEmpty(slot.getNValueColumn()))
            {
               numberValueColumnMapped = true;
               additionalSelectColumns.add(new FieldRef(dcJoin, slot.getNValueColumn()));
            }

            //build result set index mapping based on the order of the fields 
            //declared in class StructuredDataValueBean, links have to be declared as last
            int localRsIndex = 0;
            List<Pair<ValueType, Object>> sdvRsMapping = CollectionUtils.newArrayList();
            long xpathOid = info.getXpathOid();
                     
            //REGULAR FIELDS
            //StructuredDataValueBean.FIELD__OID
            addCustomIndexEntry(sdvRsMapping, ValueType.OBJECT, new Long(i));
            //StructuredDataValueBean.FIELD__PARENT
            addCustomIndexEntry(sdvRsMapping, ValueType.OBJECT, Long.valueOf(-1));
            //StructuredDataValueBean.FIELD__ENTRY_KEY
            addCustomIndexEntry(sdvRsMapping, ValueType.OBJECT, "-1");
            //StructuredDataValueBean.FIELD__XPATH
            addCustomIndexEntry(sdvRsMapping, ValueType.OBJECT, xpathOid);
            //StructuredDataValueBean.FIELD__TYPE_KEY
            addCustomIndexEntry(sdvRsMapping, ValueType.LOCAL_RS_INDEX, ++localRsIndex);
            //StructuredDataValueBean.FIELD__STRING_VALUE 
            if(stringValueColumnMapped)
            {
               addCustomIndexEntry(sdvRsMapping, ValueType.LOCAL_RS_INDEX, ++localRsIndex);
            }
            else
            {
               addCustomIndexEntry(sdvRsMapping, ValueType.OBJECT, "-1");
            }
            //StructuredDataValueBean.FIELD__NUMBER_VALUE
            if(numberValueColumnMapped)
            {
               addCustomIndexEntry(sdvRsMapping, ValueType.LOCAL_RS_INDEX, ++localRsIndex);
            }
            else
            {
               addCustomIndexEntry(sdvRsMapping, ValueType.OBJECT, -1L);
            }
            //LINKS TO OTHER PERSISTENT
            //StructuredDataValueBean.FIELD__PROCESS_INSTANCE
            addCustomIndexEntry(sdvRsMapping, ValueType.GLOBAL_RS_INDEX, 1); 
            
            allColumns.addAll(additionalSelectColumns);
            AliasProjectionResultSetInfo projectionInfo = new AliasProjectionResultSetInfo(dcJoin, sdvRsMapping);
            sdvProjectionInfo.add(projectionInfo);
         }
         
         //prepare sql query
         Column[] allColumnsAsArray = allColumns.toArray(new Column[allColumns.size()]);
         QueryExtension queryExtension = QueryExtension.where(piSelectionTerm);
         queryExtension.addJoins(dcJoins);
         queryExtension.setSelection(allColumnsAsArray);
         baseResultSet = jdbcSession.executeQuery(ProcessInstanceBean.class, queryExtension);
         
         //create the final result
         MultiplePersistentResultSet extendedResultSet = (MultiplePersistentResultSet) MultiplePersistentResultSet
         .createPersistentProjector(ProcessInstanceBean.class,
               TypeDescriptor.get(ProcessInstanceBean.class), baseResultSet,
               allColumnsAsArray, true /* this will force loading all persistent objects */);
         for(AliasProjectionResultSetInfo info: sdvProjectionInfo)
         {
            AliasProjectionResultSet projector 
               = AliasProjectionResultSet.createAliasProjector(StructuredDataValueBean.class, info.getJoin(), baseResultSet, allColumnsAsArray, info.getMapping());
            extendedResultSet.add(projector);
         }
         
         ResultIterator iterator = new ResultSetIterator(jdbcSession,
               ProcessInstanceBean.class, true, extendedResultSet, 0, -1, null, false);
         while (iterator.hasNext())
         {
            iterator.next();
         }        
      }
   }
   
   private static void performPrefetchNonStructuredFromDataCluster(
         final Map<Long, IData> dataRtOids, Collection<Long> piSet,
         int prefetchNParallelInstances, final IProcessDefinition pd, int timeout,
         final HashSet<Long> prefetchedPiOids, final HashSet<Long> prefetchedDataRtOids)
   {
      boolean useDataClusters = Parameters.instance().getBoolean(
            KernelTweakingProperties.DESCRIPTOR_PREFETCH_USE_DATACLUSTER, false);
      if ( !useDataClusters || RuntimeSetup.instance().getDataClusterSetup().length == 0)
      {
         return;
      }
      
      // get all clusters which contain requested primitive data
      ProcessInstanceQuery dummyQuery = ProcessInstanceQuery.findAll();
      FilterAndTerm andTerm = dummyQuery.getFilter();
      for (IData data : dataRtOids.values())
      {
         int type = LargeStringHolderBigDataHandler.classifyType(data);
         if(type == BigData.NUMERIC_VALUE)
         {
            andTerm.add(DataFilter.isEqual(data.getId(), 0));
         }
         else if (type == BigData.STRING_VALUE)
         {
            andTerm.add(DataFilter.isEqual(data.getId(), ""));
         }
         else
         {
            continue;
         }
      }
      
      final EvaluationContext evaluationContext = QueryServiceUtils
            .getDefaultEvaluationContext();
      final ParsedQuery parsedQuery = new ProcessInstanceQueryEvaluator(dummyQuery,
            evaluationContext).parseQuery();
      
      final QueryExtension queryExtension = new QueryExtension();
      final List<Column> additionalSelection = CollectionUtils.newArrayList(dataRtOids
            .size() * DATA_CLUSTER_SLOT_COLOUMNS);
      final Map<ITableDescriptor, List<Pair<ValueType, Object>>> descrToJoinAssociator = CollectionUtils
            .newHashMap();
      
      // add joins if it does not join the data value table
      boolean clusterMatchFound = false;
      for (Join join : parsedQuery.getPredicateJoins())
      {
         ITableDescriptor tableDescr = join.getRhsTableDescriptor();
         if (tableDescr instanceof DataCluster)
         {
            clusterMatchFound = true;

            DataCluster cluster = (DataCluster) tableDescr;
            int slotCount = 0;
            for (Entry<Long, IData> entry : dataRtOids.entrySet())
            {
               IData data = entry.getValue();
               Long dataRtOid = entry.getKey();
               
               DataSlot slot = cluster.getSlot(data.getId(), "");
               if (slot != null)
               {
                  ITableDescriptor typeDescr = addDataClusterSlot(cluster, slot, slotCount,
                        join, additionalSelection);
                  slotCount++;
                  prefetchedDataRtOids.add(dataRtOid);
                  
                  List<Pair<ValueType, Object>> fieldIndexValueList = buildCustomResultSetIndexMapping(
                        data, dataRtOid, slot);

                  // add cluster join for each slot which will be fetched
                  descrToJoinAssociator.put(typeDescr, fieldIndexValueList);

               }
            }

            queryExtension.addJoin(join);
         }
      }
      
      if ( !clusterMatchFound)
      {
         // prefetching by cluster table not possible
         trace.debug("Prefetching scope process instance(s) for " + piSet.size()
               + " instance(s) of process " + pd + ".");

         return;
      }
      
      List<List<Long>> splittedList = CollectionUtils.split(new ArrayList<Long>(piSet),
            prefetchNParallelInstances);
      
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;
         
         // This term reference will be used for later modification of its value expression
         ComparisonTerm piTermTemplate = Predicates.inList(
               ProcessInstanceBean.FR__OID, Collections.EMPTY_LIST.iterator());
         queryExtension.setWhere(piTermTemplate);
         
         List<Column> newSelection = CollectionUtils.<Column> newArrayList(SqlUtils
               .getDefaultSelectFieldList(TypeDescriptor.get(ProcessInstanceBean.class)));
         newSelection.addAll(additionalSelection);
         queryExtension.setSelection((Column[]) newSelection
               .toArray(new Column[newSelection.size()]));
         
         for (List<Long> piSubList : splittedList)
         {
            List innerOidList = (List) piTermTemplate.getValueExpr();
            innerOidList.addAll(piSubList);

            boolean distinct = queryExtension.isDistinct()
                  || queryExtension.isEngineDistinct();
            ResultSet resultSet = jdbcSession.executeQuery(ProcessInstanceBean.class,
                  queryExtension);
            
            if ( !additionalSelection.isEmpty())
            {
               // Since there exist some additional persistent objects to be fetched eagerly
               // the original result set has to be wrapped by an MultiPersistenResultSet.
               
               Column[] selectionList = queryExtension.getSelection();
               ResultSet baseResultSet = resultSet;
               resultSet = MultiplePersistentResultSet.createPersistentProjector(
                     ProcessInstanceBean.class,
                     TypeDescriptor.get(ProcessInstanceBean.class), baseResultSet,
                     selectionList, true /* this will force loading all persistent objects */);
               
               for (Entry<ITableDescriptor, List<Pair<ValueType, Object>>> projectorDescr : descrToJoinAssociator.entrySet())
               {
                  AliasProjectionResultSet projector = AliasProjectionResultSet
                        .createAliasProjector(DataValueBean.class, projectorDescr.getKey(),
                              baseResultSet, selectionList, projectorDescr.getValue());
                  ((MultiplePersistentResultSet) resultSet).add(projector);
               }
            }            
            
            ResultIterator iterator = new ResultSetIterator(jdbcSession,
                  ProcessInstanceBean.class, distinct, resultSet, 0, -1, null, false);

            // TODO: necessary? ResultIterator already did all fetching....
            while (iterator.hasNext())
            {
               IProcessInstance pi = (IProcessInstance) iterator.next();
               prefetchedPiOids.add(pi.getOID());
            }

            innerOidList.clear();
         }
      }
      
      return;
   }

   private static List<Pair<ValueType, Object>> buildCustomResultSetIndexMapping(IData data, Long dataRtOid,
         DataSlot slot)
   {
      List<Pair<ValueType, Object>> fieldIndexValueList = CollectionUtils.newArrayList();

      // init value mapper in order the persistent mapper will provide it
      
      // fields
      // oid
      addCustomIndexEntry(fieldIndexValueList, ValueType.LOCAL_RS_INDEX, 1);
      // model
      addCustomIndexEntry(fieldIndexValueList, ValueType.OBJECT, Long.valueOf(data.getModel().getModelOID()));
      // data
      addCustomIndexEntry(fieldIndexValueList, ValueType.OBJECT, dataRtOid);
      
      // column double_value does not need to be handled as this is used for sorting only
      if (StringUtils.isNotEmpty(slot.getSValueColumn()))
      {
         // string_value from DB
         addCustomIndexEntry(fieldIndexValueList, ValueType.LOCAL_RS_INDEX, 3);
         // number_value default: 0
         addCustomIndexEntry(fieldIndexValueList, ValueType.OBJECT, Long.valueOf(0));
      }
      else
      {
         // string_value default: null
         addCustomIndexEntry(fieldIndexValueList, ValueType.OBJECT, null);
         // number_value from DB
      }
      addCustomIndexEntry(fieldIndexValueList, ValueType.LOCAL_RS_INDEX, 3);
      // type_key
      addCustomIndexEntry(fieldIndexValueList, ValueType.LOCAL_RS_INDEX, 2);
      
      // links
      // processInstance
      addCustomIndexEntry(fieldIndexValueList, ValueType.GLOBAL_RS_INDEX, 1);
      
      return fieldIndexValueList;
   }

   private static void addCustomIndexEntry(List<Pair<ValueType, Object>> fieldIndexValueList,
         ValueType valueType, Object object)
   {
      fieldIndexValueList.add(new Pair<ValueType, Object>(valueType, object));
   }
   
   private static ITableDescriptor addDataClusterSlot(DataCluster cluster, DataSlot slot,
         int usedSlotCnt, final Join join, 
         List<Column> additionalSelection)
   {
      TableAliasDecorator typeDescr = new TableAliasDecorator(
            join.getTableAlias() + "_S" + usedSlotCnt, null)
      {
         @Override
         public String getTableName()
         {
            return join.getTableName();
         }
      };

      // currently the following order of columns has to be used:
      // dv.oid, dv.type_key, dv.string_value XOR dv.number_value

      // oid
      FieldRef oid = new FieldRef(join, slot.getOidColumn());
      additionalSelection.add(Functions.constantExpression(oid.toString(),
            typeDescr.getTableAlias() + "." + DataValueBean.FIELD__OID));

      // dv.type_key
      FieldRef typeKey = new FieldRef(join, slot.getTypeColumn());
      additionalSelection.add(Functions.constantExpression(
            typeKey.toString(), typeDescr.getTableAlias() + "."
                  + DataValueBean.FIELD__TYPE_KEY));
      
      // xxx_value
      if (StringUtils.isNotEmpty(slot.getSValueColumn()))
      {
         // string_value
         FieldRef stringValue = new FieldRef(join, slot.getSValueColumn());
         additionalSelection.add(Functions.constantExpression(
               stringValue.toString(), typeDescr.getTableAlias() + "."
                     + DataValueBean.FIELD__STRING_VALUE));
      }
      else
      {
         // number_value
         FieldRef numberValue = new FieldRef(join, slot.getNValueColumn());
         additionalSelection.add(Functions.constantExpression(
               numberValue.toString(), typeDescr.getTableAlias() + "."
                     + DataValueBean.FIELD__NUMBER_VALUE));
      }

      return typeDescr;
   }
   
   private static void performPrefetchNonStructured(Map<Long, IData> dataRtOids, int nData,
         Collection<Long> piSet, int timeout, int prefetchNParallelData,
         int prefetchNParallelInstances, double prefetchDataDiscriminationThreshold)
   {
      // This term reference will be used for later modification of its value expression
      ComparisonTerm piTermTemplate = Predicates.inList(
            DataValueBean.FR__PROCESS_INSTANCE, Collections.EMPTY_LIST.iterator());

      Set<Long> prefetchedDataRtOids = null;
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      if (rtEnv != null)
      {
         prefetchedDataRtOids = (Set<Long>) rtEnv
               .get(KernelTweakingProperties.DESCRIPTOR_PREFETCHED_RTOID_SET);
      }

      if (prefetchedDataRtOids != null)
      {
         dataRtOids = CollectionUtils.copyMap(dataRtOids);
         CollectionUtils.remove(dataRtOids, prefetchedDataRtOids);
      }

      if ((((double) dataRtOids.size() / (double) nData) < prefetchDataDiscriminationThreshold))
      {
         List dataRtOidSlice = new ArrayList(prefetchNParallelData);
         for (Iterator dataRtOidItr = dataRtOids.keySet().iterator(); dataRtOidItr.hasNext();)
         {
            dataRtOidSlice.add(dataRtOidItr.next());
            if ((dataRtOidSlice.size() >= prefetchNParallelData)
                  || !dataRtOidItr.hasNext())
            {
               performPrefetch(DataValueBean.class, QueryExtension.where(Predicates
                     .andTerm(Predicates.inList(
                           DataValueBean.FR__DATA, dataRtOidSlice.iterator()),
                     piTermTemplate)),
                     piTermTemplate, piSet, false, timeout,
                     prefetchNParallelInstances);

               dataRtOidSlice.clear();
            }
         }
      }
      else
      {
         performPrefetch(DataValueBean.class, QueryExtension.where(piTermTemplate),
               piTermTemplate, piSet, false, timeout, prefetchNParallelInstances);
      }
   }

   private static void performPrefetchStructured(Set dataRtOids, int nData, Collection piSet, int timeout,  int prefetchNParallelData, int prefetchNParallelInstances, double prefetchDataDiscriminationThreshold)
   {
      // This term reference will be used for later modification of its value expression
      ComparisonTerm piTermTemplate = Predicates.inList(
            StructuredDataValueBean.FR__PROCESS_INSTANCE, Collections.EMPTY_LIST.iterator());
      ComparisonTerm dvPerPiTermTemplate = Predicates.inList(
            DataValueBean.FR__PROCESS_INSTANCE, Collections.EMPTY_LIST.iterator());
      TypeDescriptor tdDv = TypeDescriptor.get(DataValueBean.class);

      if ((((double) dataRtOids.size() / (double) nData) < prefetchDataDiscriminationThreshold))
      {
         List<Long> dataRtOidSlice = new ArrayList(prefetchNParallelData);
         for (Iterator<Long> dataRtOidItr = dataRtOids.iterator(); dataRtOidItr.hasNext();)
         {
            dataRtOidSlice.add(dataRtOidItr.next());
            if ((dataRtOidSlice.size() >= prefetchNParallelData)
                  || !dataRtOidItr.hasNext())
            {
               if (Parameters.instance().getBoolean(
                     KernelTweakingProperties.DESCRIPTOR_PREFETCH_STRUCT_INDEX, false))
               {
                  QueryExtension queryExtension = QueryExtension.where(piTermTemplate);
                  queryExtension.addJoin(new Join(StructuredDataBean.class)
                        .on(StructuredDataValueBean.FR__XPATH, StructuredDataBean.FIELD__OID)
                        .where(Predicates.inList(StructuredDataBean.FR__DATA, dataRtOidSlice.iterator())));

                  performPrefetch(StructuredDataValueBean.class, queryExtension,
                        piTermTemplate, piSet, false, timeout,
                        prefetchNParallelInstances);
               }

               if (Parameters.instance().getBoolean(
                     KernelTweakingProperties.DESCRIPTOR_PREFETCH_STRUCT_XML, true))
               {
                  // prefetch associated clob rows
                  QueryExtension clobQueryExtension = QueryExtension.where(Predicates
                        .andTerm(
                              Predicates.inList(DataValueBean.FR__DATA, dataRtOidSlice),
                              dvPerPiTermTemplate));
                  clobQueryExtension.addJoin(new Join(DataValueBean.class, "dv")
                        .on(ClobDataBean.FR__OID, DataValueBean.FIELD__NUMBER_VALUE)
                        .where(Predicates.isEqual(ClobDataBean.FR__OWNER_TYPE, tdDv.getTableName())));

                  performPrefetch(ClobDataBean.class, clobQueryExtension,
                        dvPerPiTermTemplate, piSet, false, timeout,
                        prefetchNParallelInstances);
               }

               dataRtOidSlice.clear();
            }
         }
      }
      else
      {
         if (Parameters.instance().getBoolean(
               KernelTweakingProperties.DESCRIPTOR_PREFETCH_STRUCT_INDEX, false))
         {
            performPrefetch(StructuredDataValueBean.class, QueryExtension.where(piTermTemplate),
                  piTermTemplate, piSet, false, timeout, prefetchNParallelInstances);
         }

         if (Parameters.instance().getBoolean(
               KernelTweakingProperties.DESCRIPTOR_PREFETCH_STRUCT_XML, true))
         {
            // prefetch associated clob rows
            QueryExtension clobQueryExtension = QueryExtension.where(dvPerPiTermTemplate);
            clobQueryExtension.addJoin(new Join(DataValueBean.class, "dv")
                  .on(ClobDataBean.FR__OID, DataValueBean.FIELD__NUMBER_VALUE)
                  .where(Predicates.isEqual(ClobDataBean.FR__OWNER_TYPE, tdDv.getTableName())));

            performPrefetch(ClobDataBean.class, clobQueryExtension,
                  dvPerPiTermTemplate, piSet, false, timeout,
                  prefetchNParallelInstances);
         }
      }
   }


   private static void performPrefetch(Class type, QueryExtension queryExtension,
         ComparisonTerm oidInListTermReference, Collection oidSet,
         boolean useOidsForCacheHitTest, int timeout, int maxInstanceBatchSize)
   {
      int batchSizeCounter = 0;

      List innerOidList = (List) oidInListTermReference.getValueExpr();

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      innerOidList.clear();

      // all prefetches currently affect types with one PK field
      TypeDescriptor typeDescr = TypeDescriptor.get(type);
      for (Iterator oidItr = oidSet.iterator(); oidItr.hasNext();)
      {
         Long oid = (Long) oidItr.next();

         if ( !useOidsForCacheHitTest
               || !session.existsInCache(type, typeDescr.getIdentityKey(oid)))
         {
            innerOidList.add(oid);
            ++batchSizeCounter;
         }

         if ((maxInstanceBatchSize <= batchSizeCounter) || !oidItr.hasNext())
         {
            // iterate over complete result to ensure all items are put into cache

            if ( !innerOidList.isEmpty())
            {
               ResultIterator i = session.getIterator(type, queryExtension, 0, -1, timeout);
               try
               {
                  while (i.hasNext())
                  {
                     i.next();
                  }
               }
               finally
               {
                  i.close();
               }
            }

            batchSizeCounter = 0;
            innerOidList.clear();
         }
      }
   }

   private static boolean isEventTypeSet(int eventTypes, int eventType)
   {
      return (eventTypes & eventType) == eventType;
   }
   
   private static class AliasProjectionResultSetInfo
   {
      private final Join join;
      private final List<Pair<ValueType, Object>> mapping;

      public AliasProjectionResultSetInfo(Join join, List<Pair<ValueType, Object>> mapping)
      {
         this.join = join;
         this.mapping = mapping;
      }

      public Join getJoin()
      {
         return join;
      }
      
      public List<Pair<ValueType, Object>> getMapping()
      {
         return mapping;
      }
   }
}
