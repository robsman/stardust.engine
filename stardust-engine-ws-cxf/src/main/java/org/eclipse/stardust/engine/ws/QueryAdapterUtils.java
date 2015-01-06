/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.OperatorFilterAdapterUtils.unmarshalBinaryOperatorFilter;
import static org.eclipse.stardust.engine.ws.OperatorFilterAdapterUtils.unmarshalUnaryOperatorFilter;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.ws.ProcessInstanceDetailsOptionXto;
import org.eclipse.stardust.engine.api.ws.ProcessInstanceDetailsOptionsXto;
import org.eclipse.stardust.engine.api.ws.query.*;
import org.eclipse.stardust.engine.api.ws.query.ProcessInstanceLinkFilterXto.LinkTypesXto;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalCostPerExecutionPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalProcessingTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.PerformanceCriticalityPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessCumulationPolicy;



/**
 * @author roland.stamm
 *
 */
public class QueryAdapterUtils
{
   public static final String CUSTOM_ORDER_LAST_NAME = "lastName";

   public static final String CUSTOM_ORDER_FIST_NAME = "fistName";

   public static final String CUSTOM_ORDER_ACCOUNT = "account";

   public static final String CUSTOM_ORDER_NAME = "name";

   public static final String CUSTOM_ORDER_ID = "id";

   public static final String CUSTOM_ORDER_USER = "user";

   public static final String CUSTOM_ORDER_PROC_DEF = "processDefinition";

   public static final String CUSTOM_ORDER_ACTIVITY = "activityDefinition";

   private static final Logger trace = LogManager.getLogger(QueryAdapterUtils.class);

   public static ProcessInstanceQuery unmarshalProcessQuery(ProcessQueryXto queryXto)
   {
      final Class<ProcessInstanceQuery> clazz = ProcessInstanceQuery.class;
      ProcessInstanceQuery query = new ProcessInstanceQuery();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   public static ProcessDefinitionQuery unmarshalProcessDefinitionQuery(
         ProcessDefinitionQueryXto queryXto)
   {
      final Class<ProcessDefinitionQuery> clazz = ProcessDefinitionQuery.class;
      ProcessDefinitionQuery query = ProcessDefinitionQuery.findAll();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   public static ActivityInstanceQuery unmarshalActivityQuery(ActivityQueryXto queryXto)
   {
      final Class<ActivityInstanceQuery> clazz = ActivityInstanceQuery.class;
      ActivityInstanceQuery query = new ActivityInstanceQuery();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   public static UserQuery unmarshalUserQuery(UserQueryXto queryXto)
   {
      final Class<UserQuery> clazz = UserQuery.class;
      UserQuery query = new UserQuery();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   public static UserGroupQuery unmarshalUserGroupQuery(UserGroupQueryXto queryXto)
   {
      final Class<UserGroupQuery> clazz = UserGroupQuery.class;
      UserGroupQuery query = new UserGroupQuery();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   public static LogEntryQuery unmarshalLogEntryQuery(LogEntryQueryXto queryXto)
   {
      final Class<LogEntryQuery> clazz = LogEntryQuery.class;
      LogEntryQuery query = new LogEntryQuery();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   public static WorklistQuery unmarshalWorklistQuery(WorklistQueryXto worklistQueryXto)
   {
      final Class<WorklistQuery> clazz = WorklistQuery.class;

      WorklistQuery query;
      if (null == worklistQueryXto)
      {
         query = WorklistQuery.findCompleteWorklist();
      }
      else
      {
         query = new WorklistQuery();

         if (null != worklistQueryXto)
         {
            // WorklistQuery methods
            if (worklistQueryXto == null
                  || (worklistQueryXto.getUserContribution() == null && worklistQueryXto.getParticipantContributions() == null))
            {
               query = WorklistQuery.findCompleteWorklist();
            }
            else
            {
               query = unmarshalUserContribution(worklistQueryXto.getUserContribution(),
                     query);
               query = unmarshalParticipantContributions(
                     worklistQueryXto.getParticipantContributions(), query);
            }

            // Query methods
            query = unmarshalQuery(worklistQueryXto, query, clazz);
         }
      }

      return query;
   }

   public static DocumentQuery unmarshalDocumentQuery(DocumentQueryXto queryXto)
   {
      final Class<DocumentQuery> clazz = DocumentQuery.class;
      DocumentQuery query = DocumentQuery.findAll();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   public static DataQuery unmarshalDataQuery(VariableDefinitionQueryXto queryXto)
   {
      final Class<DataQuery> clazz = DataQuery.class;
      DataQuery query = DataQuery.findAll();

      query = unmarshalQuery(queryXto, query, clazz);

      return query;
   }

   private static WorklistQuery unmarshalParticipantContributions(
         ParticipantContributionsXto participantContributions, WorklistQuery query)
   {
      if (participantContributions != null)
      {
         for (ParticipantContributionXto pc : participantContributions.getContribution())
         {
            unmarshalParticipantContribution(pc, query);
         }
      }
      return query;
   }

   private static <T extends Query> T unmarshalQuery(QueryXto queryXto, T query,
         Class<T> clazz)
   {
      if (null != queryXto)
      {
         if (null != queryXto.getPredicate())
         {
            unmarshalPredicateTerm(queryXto.getPredicate(), query.getFilter(), clazz);
         }
         if (null != queryXto.getOrder())
         {
            query = clazz.cast(unmarshalOrder(queryXto.getOrder()
                  .getAttributeOrderOrDataOrder(), query));
         }
         if (null != queryXto.getPolicy())
         {
            query = clazz.cast(unmarshalPolicy(queryXto.getPolicy()
                  .getSubsetPolicyOrSubFolderPolicyOrModelVersionPolicy(), query));
         }
      }

      return query;
   }

   private static Query unmarshalPolicy(List<EvaluationPolicyXto> policies, Query query)
   {
      for (final EvaluationPolicyXto policy : policies)
      {
         if (policy instanceof SubsetPolicyXto)
         {
            query.setPolicy(unmarshalSubsetPolicy((SubsetPolicyXto) policy));
         }
         else if (policy instanceof SubFolderPolicyXto)
         {
            query.setPolicy(unmarshalSubFolderPolicy((SubFolderPolicyXto) policy));
         }
         else if (policy instanceof ModelVersionPolicyXto)
         {
            query.setPolicy(unmarshalModelVersionPolicy((ModelVersionPolicyXto) policy));
         }
         else if (policy instanceof DescriptorPolicyXto)
         {
            query.setPolicy(unmarshalDescriptorPolicy((DescriptorPolicyXto) policy));
         }
         else if (policy instanceof ProcessDefinitionDetailsPolicyXto)
         {
            query.setPolicy(unmarshalProcessDefinitionDetailsPolicy((ProcessDefinitionDetailsPolicyXto) policy));
         }
         else if (policy instanceof ProcessInstanceDetailsPolicyXto)
         {
            query.setPolicy(unmarshalProcessInstanceDetailsPolicy((ProcessInstanceDetailsPolicyXto) policy));
         }
         else if (policy instanceof HistoricalStatesPolicyXto)
         {
            query.setPolicy(unmarshalHistoricalStatesPolicy((HistoricalStatesPolicyXto) policy));
         }
         else if (policy instanceof HistoricalEventPolicyXto)
         {
            query.setPolicy(unmarshalHistoricalEventPolicy((HistoricalEventPolicyXto) policy));
         }
         else if (policy instanceof CriticalExecutionTimePolicyXto)
         {
            query.setPolicy(unmarshalCriticalExecutionTimePolicy((CriticalExecutionTimePolicyXto) policy));
         }
         else if (policy instanceof CriticalCostPerExecutionPolicyXto)
         {
            query.setPolicy(unmarshalCriticalCostPerExecutionPolicy((CriticalCostPerExecutionPolicyXto) policy));
         }
         else if (policy instanceof CriticalProcessingTimePolicyXto)
         {
            query.setPolicy(unmarshalCriticalProcessingTimePolicy((CriticalProcessingTimePolicyXto) policy));
         }
         else if (policy instanceof PerformanceCriticalityPolicyXto)
         {
            query.setPolicy(unmarshalPerformanceCriticalityPolicy((PerformanceCriticalityPolicyXto) policy));
         }
         else if (policy instanceof ProcessCumulationPolicyXto)
         {
            query.setPolicy(unmarshalProcessCumulationPolicy((ProcessCumulationPolicyXto) policy));
         }
         else if (policy instanceof TimeoutPolicyXto)
         {
            query.setPolicy(unmarshalTimeoutPolicy((TimeoutPolicyXto) policy));
         }
         else if (policy instanceof UserDetailsPolicyXto)
         {
            query.setPolicy(unmarshalUserDetailsPolicy((UserDetailsPolicyXto) policy));
         }
         else if (policy instanceof CasePolicyXto)
         {
            query.setPolicy(unmarshalCasePolicy((CasePolicyXto) policy));
         }
         else if (policy instanceof EvaluateByWorkitemsPolicyXto)
         {
            query.setPolicy(unmarshalEvaluateByWorkitemsPolicy((EvaluateByWorkitemsPolicyXto) policy));
         }
         else if (policy instanceof ExcludeUserPolicyXto)
         {
            query.setPolicy(unmarshalExcludeUserPolicy((ExcludeUserPolicyXto) policy));
         }
         else
         {
            throw new UnsupportedOperationException("Unknown Evaluation Policy: " + policy);
         }
      }

      return query;
   }

   private static EvaluationPolicy unmarshalExcludeUserPolicy(ExcludeUserPolicyXto policy)
   {
      if (policy != null)
      {
         return ExcludeUserPolicy.EXCLUDE_USER;
      }
      return null;
   }

   private static EvaluationPolicy unmarshalEvaluateByWorkitemsPolicy(
         EvaluateByWorkitemsPolicyXto policy)
   {
      if (policy != null)
      {
         return EvaluateByWorkitemsPolicy.WORKITEMS;
      }
      return null;
   }

   private static HistoricalEventPolicy unmarshalHistoricalEventPolicy(
         HistoricalEventPolicyXto historicalEventPolicy)
   {
      if (historicalEventPolicy.getEventTypes().isEmpty())
      {
         return HistoricalEventPolicy.ALL_EVENTS;
      }
      else
      {
         return new HistoricalEventPolicy(
               unmarshalEventTypes(historicalEventPolicy.getEventTypes()));
      }
   }

   private static HistoricalEventType[] unmarshalEventTypes(
         List<HistoricalEventTypeXto> list)
   {
      Set<HistoricalEventTypeXto> xto = new HashSet<HistoricalEventTypeXto>(list);
      HistoricalEventType[] a = new HistoricalEventType[xto.size()];

      int i = 0;
      for (HistoricalEventTypeXto entry : xto)
      {
         a[i++ ] = unmarshalHistoricalEventType(entry);
      }

      return a;
   }

   private static HistoricalEventType unmarshalHistoricalEventType(
         HistoricalEventTypeXto historicalEventTypeXto)
   {
      if (HistoricalEventTypeXto.STATE_CHANGE.equals(historicalEventTypeXto))
      {
         return HistoricalEventType.StateChange;
      }
      else if (HistoricalEventTypeXto.DELEGATION.equals(historicalEventTypeXto))
      {
         return HistoricalEventType.Delegation;
      }
      else if (HistoricalEventTypeXto.NOTE.equals(historicalEventTypeXto))
      {
         return HistoricalEventType.Note;
      }
      else if (HistoricalEventTypeXto.EXCEPTION.equals(historicalEventTypeXto))
      {
         return HistoricalEventType.Exception;
      }
      else
      {
         throw new UnsupportedOperationException(
               "Error unmarshaling HistoricalEventType: unmarshaling not implemented for: "
                     + historicalEventTypeXto);
      }
   }

   private static EvaluationPolicy unmarshalCasePolicy(CasePolicyXto policyXto)
   {
      CasePolicy ret = null;
      if (policyXto != null)
      {
         ret = CasePolicy.INCLUDE_CASES;
      }
      return ret;
   }

   public static SubsetPolicy unmarshalSubsetPolicy(SubsetPolicyXto subsetPolicy)
   {
      SubsetPolicy ret = null;
      if (subsetPolicy != null)
      {
         if (subsetPolicy.getSkippedEntries() != null)
         {
            ret = new SubsetPolicy(subsetPolicy.getMaxSize(),
                  subsetPolicy.getSkippedEntries(), subsetPolicy.isEvaluateTotalCount());
         }
         else
         {
            ret = new SubsetPolicy(subsetPolicy.getMaxSize(),
                  subsetPolicy.isEvaluateTotalCount());
         }
      }
      return ret;
   }

   private static EvaluationPolicy unmarshalSubFolderPolicy(SubFolderPolicyXto policy)
   {
      if (policy != null)
      {
         return new SubFolderPolicy(policy.getLimitSubFolder(), policy.isRecursive());
      }
      return null;
   }

   private static EvaluationPolicy unmarshalModelVersionPolicy(
         ModelVersionPolicyXto modelVersionPolicy)
   {
      if (modelVersionPolicy != null)
      {
         return new ModelVersionPolicy(modelVersionPolicy.isRestrictedToActiveModel());
      }
      throw new NullPointerException(
            "Cannot unmarshal ModelVersionPolicyXto: ModelVersionPolicyXto is null");
   }

   private static EvaluationPolicy unmarshalDescriptorPolicy(
         DescriptorPolicyXto descriptorPolicy)
   {
      if (descriptorPolicy.isIncludeDescriptors())
      {
         return DescriptorPolicy.WITH_DESCRIPTORS;
      }
      else
      {
         return DescriptorPolicy.NO_DESCRIPTORS;
      }
   }

   private static EvaluationPolicy unmarshalProcessDefinitionDetailsPolicy(
         ProcessDefinitionDetailsPolicyXto processDefinitionDetailsPolicy)
   {
      ProcessDefinitionDetailsPolicy policy = null;
      switch (processDefinitionDetailsPolicy.getDetailsLevel())
      {
      case CORE:
         policy = new ProcessDefinitionDetailsPolicy(ProcessDefinitionDetailsLevel.CORE);
         break;
      case WITHOUT_ACTIVITIES:
         policy = new ProcessDefinitionDetailsPolicy(
               ProcessDefinitionDetailsLevel.WITHOUT_ACTIVITIES);
         break;
      case FULL:
         policy = new ProcessDefinitionDetailsPolicy(ProcessDefinitionDetailsLevel.FULL);
         break;
      default:
            throw new UnsupportedOperationException(
                  "ProcessDefinitionDetailsLevel mapping not implemented: "
                  + processDefinitionDetailsPolicy.getDetailsLevel());
      }
      return policy;
   }

   private static EvaluationPolicy unmarshalProcessInstanceDetailsPolicy(
         ProcessInstanceDetailsPolicyXto processInstanceDetailsPolicy)
   {
      ProcessInstanceDetailsPolicy policy = null;
      switch (processInstanceDetailsPolicy.getDetailsLevel())
      {
      case CORE:
         policy = new ProcessInstanceDetailsPolicy(ProcessInstanceDetailsLevel.Core);
         break;
      case WITH_PROPERTIES:
         policy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.WithProperties);
         break;
      case WITH_RESOLVED_PROPERTIES:
         policy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.WithResolvedProperties);
         break;
      case FULL:
         policy = new ProcessInstanceDetailsPolicy(ProcessInstanceDetailsLevel.Full);
         break;
      case DEFAULT:
         policy = new ProcessInstanceDetailsPolicy(ProcessInstanceDetailsLevel.Default);
         break;
      default:
            throw new UnsupportedOperationException(
                  "ProcessInstanceDetailsLevel mapping not implemented: "
                  + processInstanceDetailsPolicy.getDetailsLevel());
      }

      ProcessInstanceDetailsOptionsXto detailsOptions = processInstanceDetailsPolicy.getDetailsOptions();
      if (detailsOptions != null)
      {
         List<ProcessInstanceDetailsOptionXto> pidoList = detailsOptions.getProcessInstanceDetailsOption();
         for (ProcessInstanceDetailsOptionXto pido : pidoList)
         {
            switch (pido)
            {
            case WITH_HIERARCHY_INFO:
               policy.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
               break;
            case WITH_LINK_INFO:
               policy.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);
               break;
            case WITH_NOTES:
               policy.getOptions().add(ProcessInstanceDetailsOptions.WITH_NOTES);
               break;
            default:
               throw new UnsupportedOperationException(
                     "ProcessInstanceDetailsOption mapping not implemented: " + pido);
            }
         }
      }

      return policy;
   }

   private static HistoricalStatesPolicy unmarshalHistoricalStatesPolicy(
         HistoricalStatesPolicyXto historicalStatesPolicy)
   {
      try
      {
         return HistoricalStatesPolicy.valueOf(historicalStatesPolicy.getType());
      }
      catch (Exception ex)
      {
         return HistoricalStatesPolicy.NO_HIST_STATES;
      }
   }

   private static CriticalCostPerExecutionPolicy unmarshalCriticalCostPerExecutionPolicy(CriticalCostPerExecutionPolicyXto xto)
   {
      return new CriticalCostPerExecutionPolicy(xto.getYellowPct(), xto.getRedPct());
   }

   private static CriticalExecutionTimePolicy unmarshalCriticalExecutionTimePolicy(CriticalExecutionTimePolicyXto xto)
   {
      return new CriticalExecutionTimePolicy(xto.getLowPriorityCriticalPct(), xto.getNormalPriorityCriticalPct(), xto.getHighPriorityCriticalPct());
   }

   private static CriticalProcessingTimePolicy unmarshalCriticalProcessingTimePolicy(CriticalProcessingTimePolicyXto xto)
   {
      return new CriticalProcessingTimePolicy(xto.getYellowPct(), xto.getRedPct());
   }

   private static PerformanceCriticalityPolicy unmarshalPerformanceCriticalityPolicy(PerformanceCriticalityPolicyXto xto)
   {
      return new PerformanceCriticalityPolicy(xto.getLowPriorityCriticalPct(), xto.getNormalPriorityCriticalPct(), xto.getHighPriorityCriticalPct());
   }

   private static ProcessCumulationPolicy unmarshalProcessCumulationPolicy(ProcessCumulationPolicyXto xto)
   {
      ProcessCumulationPolicy policy;

      if (xto.isCumulateWithRootPi() && !xto.isCumulateWithScopePi())
      {
         policy = ProcessCumulationPolicy.WITH_ROOT_PI;
      }
      else if ( !xto.isCumulateWithRootPi() && xto.isCumulateWithScopePi())
      {
         policy = ProcessCumulationPolicy.WITH_SCOPE_PI;
      }
      else if ( !xto.isCumulateWithRootPi() && !xto.isCumulateWithScopePi())
      {
         policy = ProcessCumulationPolicy.WITH_PI;
      }
      else
      {
         throw new IllegalArgumentException("Process Cumulation Policy is illegal.");
      }

      return policy;
   }

   private static TimeoutPolicy unmarshalTimeoutPolicy(TimeoutPolicyXto xto)
   {
      return new TimeoutPolicy(xto.getTimeout());
   }

   private static UserDetailsPolicy unmarshalUserDetailsPolicy(UserDetailsPolicyXto xto)
   {
      return new UserDetailsPolicy(unmarshalUserDetailsLevel(xto.getLevel()));
   }

   private static UserDetailsLevel unmarshalUserDetailsLevel(UserDetailsLevelXto xto)
   {
      switch (xto)
      {
      case CORE:
         return UserDetailsLevel.Core;
      case FULL:
         return UserDetailsLevel.Full;
      case WITH_PROPERTIES:
         return UserDetailsLevel.WithProperties;
      default:
         throw new IllegalArgumentException("User Details Level is illegal.");
      }
   }

   private static Query unmarshalOrder(List<OrderCriterionXto> orderCriteria, Query query)
   {

      for (OrderCriterionXto orderCriterionXto : orderCriteria)
      {
         if (orderCriterionXto instanceof AttributeOrderXto)
         {
            AttributeOrderXto ao = (AttributeOrderXto) orderCriterionXto;

            if (ao.getAttribute() != null)
            {
               if (ao.getAttribute().getEntity() == null)
               {
                  query.orderBy(new AttributeOrder(
                        AttributeFilterUtils.unmarshalFilterableAttribute(
                              ao.getAttribute().getValue(), query.getClass()),
                        ao.isAscending()));
               }
               else
               {
                  CustomOrderCriterion customOrder = inferCustomOrderCriterion(
                        ao.getAttribute(), query.getClass());
                  query.orderBy(customOrder.ascendig(ao.isAscending()));
               }
            }

         }
         else if (orderCriterionXto instanceof DataOrderXto)
         {
            DataOrderXto dataOrder = (DataOrderXto) orderCriterionXto;
            if (isEmpty(dataOrder.getAttribute()))
            {
               query.orderBy(new DataOrder(dataOrder.getDataId(), dataOrder.isAscending()));
            }
            else
            {
               query.orderBy(new DataOrder(dataOrder.getDataId(),
                     dataOrder.getAttribute(), dataOrder.isAscending()));
            }
         }
         else
         {
            // TODO not supported
            throw new UnsupportedOperationException(
                  "Error unmarshaling OrderCriteria: CustomOrderCriterion not supported");
         }
      }
      return query;
   }

   private static CustomOrderCriterion inferCustomOrderCriterion(
         AttributeReferenceXto attribute, Class< ? > queryClazz)
   {
      CustomOrderCriterion ret = null;

      String entity = attribute.getEntity();
      String field = attribute.getValue();

      if (ActivityInstanceQuery.class.equals(queryClazz))
      {
         if (CUSTOM_ORDER_ACTIVITY.equals(entity))
         {
            if (CUSTOM_ORDER_ID.equals(field))
            {
               ret = ActivityInstanceQuery.ACTIVITY_ID;
            }
            else if (CUSTOM_ORDER_NAME.equals(field))
            {
               ret = ActivityInstanceQuery.ACTIVITY_NAME;
            }
            else
            {
               throw new UnsupportedOperationException(
                     "Error unmarshaling CustomOrderCriterion: Entity (" + entity
                           + ") does not support field: " + field);
            }
         }
         else if (CUSTOM_ORDER_PROC_DEF.equals(entity))
         {
            if (CUSTOM_ORDER_ID.equals(field))
            {
               ret = ActivityInstanceQuery.PROC_DEF_ID;
            }
            else if (CUSTOM_ORDER_NAME.equals(field))
            {
               ret = ActivityInstanceQuery.PROC_DEF_NAME;
            }
            else
            {
               throw new UnsupportedOperationException(
                     "Error unmarshaling CustomOrderCriterion: Entity (" + entity
                           + ") does not support field: " + field);
            }
         }
         else if (CUSTOM_ORDER_USER.equals(entity))
         {
            if (CUSTOM_ORDER_ACCOUNT.equals(field))
            {
               ret = ActivityInstanceQuery.USER_ACCOUNT;
            }
            else if (CUSTOM_ORDER_FIST_NAME.equals(field))
            {
               ret = ActivityInstanceQuery.USER_FIRST_NAME;
            }
            else if (CUSTOM_ORDER_LAST_NAME.equals(field))
            {
               ret = ActivityInstanceQuery.USER_LAST_NAME;
            }
            else
            {
               throw new UnsupportedOperationException(
                     "Error unmarshaling CustomOrderCriterion: Entity (" + entity
                           + ") does not support field: " + field);
            }
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Error unmarshaling CustomOrderCriterion: Entity not supported: "
                        + entity);
         }
      }
      else if (ProcessInstanceQuery.class.equals(queryClazz))
      {
         if (CUSTOM_ORDER_PROC_DEF.equals(entity))
         {
            if (CUSTOM_ORDER_ID.equals(field))
            {
               ret = ProcessInstanceQuery.PROC_DEF_ID;
            }
            else if (CUSTOM_ORDER_NAME.equals(field))
            {
               ret = ProcessInstanceQuery.PROC_DEF_NAME;
            }
            else
            {
               throw new UnsupportedOperationException(
                     "Error unmarshaling CustomOrderCriterion: Entity (" + entity
                           + ") does not support field: " + field);
            }
         }
         else if (CUSTOM_ORDER_USER.equals(entity))
         {
            if (CUSTOM_ORDER_ACCOUNT.equals(field))
            {
               ret = ProcessInstanceQuery.USER_ACCOUNT;
            }
            else if (CUSTOM_ORDER_FIST_NAME.equals(field))
            {
               ret = ProcessInstanceQuery.USER_FIRST_NAME;
            }
            else if (CUSTOM_ORDER_LAST_NAME.equals(field))
            {
               ret = ProcessInstanceQuery.USER_LAST_NAME;
            }
            else
            {
               throw new UnsupportedOperationException(
                     "Error unmarshaling CustomOrderCriterion: Entity (" + entity
                           + ") does not support field: " + field);
            }
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Error unmarshaling CustomOrderCriterion: Entity not supported: "
                        + entity);
         }
      }
      else
      {
         throw new UnsupportedOperationException(
               "Error unmarshaling CustomOrderCriterion: No supported CustomOrderCriterion for class: "
                     + queryClazz);
      }
      return ret;
   }

   private static void unmarshalPredicateTerm(PredicateTermXto predicateTerm,
         FilterTerm filterTerm, Class< ? extends Query> queryClazz)
   {
      for (PredicateBaseXto filterCriterionXto : predicateTerm.getAndOrOrOrIsNull())
      {
         if (null != filterCriterionXto)
         {
            // add filters
            unmarshalTermElement(filterCriterionXto, filterTerm, queryClazz);
         }
      }
   }

   private static void unmarshalTermElement(PredicateBaseXto filterCriterionXto,
         FilterTerm filterTerm, Class< ? extends Query> clazz)
   {


      if (filterCriterionXto instanceof AndTermXto)
      {
         // *FilterAndTerm.class,
         FilterAndTerm innerAndTerm = filterTerm.addAndTerm();

         unmarshalPredicateTerm((AndTermXto) filterCriterionXto, innerAndTerm, clazz);
      }
      else if (filterCriterionXto instanceof OrTermXto)
      {
         // *FilterOrTerm.class,
         FilterOrTerm innerOrTerm = filterTerm.addOrTerm();

         unmarshalPredicateTerm((OrTermXto) filterCriterionXto, innerOrTerm, clazz);
      }
      else if (filterCriterionXto instanceof UnaryPredicateXto)
      {
         // *UnaryOperatorFilter.class,
         // Data filters, too
         filterTerm.add(unmarshalUnaryOperatorFilter(
               (UnaryPredicateXto) filterCriterionXto, clazz));
      }
      else if (filterCriterionXto instanceof BinaryPredicateBaseXto)
      {
         // *BinaryOperatorFilter.class,
         // *TernaryOperatorFilter.class,
         // Data filters, too
         filterTerm.add(unmarshalBinaryOperatorFilter(
               (BinaryPredicateBaseXto) filterCriterionXto, clazz));
      }
      else if (filterCriterionXto instanceof ProcessDefinitionFilterXto)
      {
         // *ProcessDefinitionFilter.class,
         filterTerm.add(unmarshalProcessDefinitionFilter((ProcessDefinitionFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ProcessInstanceFilterXto)
      {
         // *ProcessInstanceFilter.class,
         filterTerm.add(unmarshalProcessInstanceFilter((ProcessInstanceFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ActivityDefinitionFilterXto)
      {
         // *ActivityFilter.class,
         filterTerm.add(unmarshalActivityFilter((ActivityDefinitionFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ActivityInstanceFilterXto)
      {
         // *ActivityInstanceFilter.class,
         filterTerm.add(unmarshalActivityInstanceFilter((ActivityInstanceFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ActivityStateFilterXto)
      {
         // *ActivityStateFilter.class,
         filterTerm.add(unmarshalActivityStateFilter((ActivityStateFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ProcessStateFilterXto)
      {
         // *ProcessStateFilter.class,
         filterTerm.add(unmarshalProcessStateFilter((ProcessStateFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof PerformingUserFilterXto)
      {
         // *PerformingUserFilter.class,
         filterTerm.add(unmarshalPerformingUserFilter((PerformingUserFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof PerformedByUserFilterXto)
      {
         // *PerformedByUserFilter.class,
         filterTerm.add(unmarshalPerformedByUserFilter((PerformedByUserFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof PerformingParticipantFilterXto)
      {
         // *PerformingParticipantFilter.class,
         filterTerm.add(unmarshalPerformingParticipantFilter((PerformingParticipantFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof PerformingOnBehalfOfFilterXto)
      {
         // *PerformingOnBehalfOfFilter.class,
         filterTerm.add(unmarshalPerformingOnBehalfOfFilter((PerformingOnBehalfOfFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof UserStateFilterXto)
      {
         // *UserStateFilter.class
         filterTerm.add(unmarshalUserStateFilter((UserStateFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof StartingUserFilterXto)
      {
         // *StartingUserFilter.class,
         filterTerm.add(unmarshalStartingUserFilter((StartingUserFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ParticipantAssociationFilterXto)
      {
         // *ParticipantAssociationFilter.class
         filterTerm.add(unmarshalParticipantAssociationFilter((ParticipantAssociationFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ProcessInstanceHierarchyFilterXto)
      {
         // *ProcessInstanceHierarchyFilter.class
         filterTerm.add(unmarshalProcessInstanceHierarchyFilter((ProcessInstanceHierarchyFilterXto) filterCriterionXto));
      }
      else if (filterCriterionXto instanceof ProcessInstanceLinkFilterXto)
      {
         // *ProcessInstanceLinkFilter.class
         filterTerm.add(unmarshalProcessInstanceLinkFilter((ProcessInstanceLinkFilterXto) filterCriterionXto));
      }
      // else if (filterCriterionXto.getDataFilter() != null)
      // {
      // // *DataFilter.class,
      // filterTerm.add(unmarshalDataFilter(filterCriterionXto));
      // }
      // else if (filterCriterionXto.getSubProcessDataFilter() != null)
      // {
      // // *SubProcessDataFilter.class,
      // filterTerm.add(unmarshalSubProcessDataFilter(filterCriterionXto));
      // }
      // else if (filterCriterionXto.getHierarchyDataFilter() != null)
      // {
      // // *HierarchyDataFilter.class,
      // filterTerm.add(unmarshalHierarchyDataFilter(filterCriterionXto));
      // }
      // else if (filterCriterionXto.getFilterAndTerm() != null
      // || filterCriterionXto.getFilterOrTerm() != null)
      // {
      // // do nothing, filter term elements allowed
      // }
      else
      {
         throw new UnsupportedOperationException("Filter not supported for "
               + clazz.getName());
      }
   }

   private static FilterCriterion unmarshalProcessDefinitionFilter(
         ProcessDefinitionFilterXto processDefinitionFilter)
   {
      if ( !isEmpty(processDefinitionFilter.getProcessDefinitionId()))
      {
         return new ProcessDefinitionFilter(
               processDefinitionFilter.getProcessDefinitionId(),
               processDefinitionFilter.isIncludingSubprocesses());
      }

      trace.error("ProcessDefinitionFilterXto could not be unmarshaled");
      throw new UnsupportedOperationException(
            "Error unmarshaling ProcessDefinitionFilterXto");
   }

   private static FilterCriterion unmarshalProcessInstanceFilter(
         ProcessInstanceFilterXto processInstanceFilter)
   {
      return new ProcessInstanceFilter(processInstanceFilter.getProcessOid(),
            processInstanceFilter.isIncludingSubprocesses());
   }

   private static FilterCriterion unmarshalProcessInstanceHierarchyFilter(
         ProcessInstanceHierarchyFilterXto filterXto)
   {
     HierarchyModeXto mode = filterXto.getMode();

     switch (mode)
     {
     case ROOT_PROCESS:
        return ProcessInstanceHierarchyFilter.ROOT_PROCESS;
     case SUB_PROCESS:
        return ProcessInstanceHierarchyFilter.SUB_PROCESS;
     }
     return null;
   }

   private static FilterCriterion unmarshalProcessInstanceLinkFilter(
         ProcessInstanceLinkFilterXto filterCriterionXto)
   {
      LinkDirectionXto directionXto = filterCriterionXto.getDirection();
      LinkTypesXto linkTypesXto = filterCriterionXto.getLinkTypes();

      String[] linkTypes = null;
      if (linkTypesXto != null)
      {
         List<String> typeId = linkTypesXto.getTypeId();
         linkTypes = (String[]) typeId.toArray(new String[typeId.size()]);
      }

      LinkDirection direction = null;
      if (directionXto != null)
      {
         switch (directionXto)
         {
         case FROM:
            direction = LinkDirection.FROM;
            break;
         case TO:
            direction = LinkDirection.TO;
            break;
         case TO_FROM:
            direction = LinkDirection.TO_FROM;
            break;
         }
      }

      return new ProcessInstanceLinkFilter(filterCriterionXto.getProcessOid(), direction, linkTypes);
   }

   private static FilterCriterion unmarshalActivityFilter(
         ActivityDefinitionFilterXto activityFilter)
   {
      if ( !isEmpty(activityFilter.getActivityId()))
      {
         if (isEmpty(activityFilter.getProcessId()))
         {
            if ( !isEmpty(activityFilter.getModelOids()))
            {
               return ActivityFilter.forAnyProcess(activityFilter.getActivityId(),
                     activityFilter.getModelOids());
            }
            else
            {
               return ActivityFilter.forAnyProcess(activityFilter.getActivityId());
            }
         }
         else
         {
            if ( !isEmpty(activityFilter.getModelOids()))
            {
               return ActivityFilter.forProcess(activityFilter.getActivityId(),
                     activityFilter.getProcessId(), activityFilter.getModelOids(),
                     activityFilter.isIncludingSubprocesses());
            }
            else
            {
               return ActivityFilter.forProcess(activityFilter.getActivityId(),
                     activityFilter.getProcessId(),
                     activityFilter.isIncludingSubprocesses());
            }
         }
      }
      trace.error("ActivityFilterXto could not be unmarshaled");
      throw new UnsupportedOperationException("Error unmarshaling ActivityFilterXto");
   }

   private static FilterCriterion unmarshalActivityInstanceFilter(
         ActivityInstanceFilterXto activityInstanceFilter)
   {
      return new ActivityInstanceFilter(activityInstanceFilter.getActivityOid());
   }

   private static FilterCriterion unmarshalActivityStateFilter(
         ActivityStateFilterXto activityStateFilter)
   {
      return new ActivityStateFilter(activityStateFilter.getStates().isInclusive(),
            activityInstanceStatesToArray(activityStateFilter.getStates().getState()));
   }

   private static ActivityInstanceState[] activityInstanceStatesToArray(
         List<ActivityInstanceState> xto)
   {
      ActivityInstanceState[] a = new ActivityInstanceState[xto.size()];
      for (int i = 0; i < xto.size(); i++ )
      {
         a[i] = xto.get(i);
      }
      return a;
   }

   private static StartingUserFilter unmarshalStartingUserFilter(
         StartingUserFilterXto filterXto)
   {
      if (filterXto.getUserOid() == -1)
      {
         return StartingUserFilter.CURRENT_USER;
      }
      else
      {
         return new StartingUserFilter(filterXto.getUserOid());
      }
   }

   private static FilterCriterion unmarshalProcessStateFilter(
         ProcessStateFilterXto processStateFilter)
   {
      return new ProcessStateFilter(processStateFilter.getStates().isInclusive(),
            processInstanceStatesToArray(processStateFilter.getStates().getState()));
   }

   private static ProcessInstanceState[] processInstanceStatesToArray(
         List<ProcessInstanceState> xto)
   {
      ProcessInstanceState[] a = new ProcessInstanceState[xto.size()];
      for (int i = 0; i < xto.size(); i++ )
      {
         a[i] = xto.get(i);
      }
      return a;
   }

   private static PerformingUserFilter unmarshalPerformingUserFilter(
         PerformingUserFilterXto filterXto)
   {
      if (filterXto.getUserOid() == -1)
      {
         return PerformingUserFilter.CURRENT_USER;
      }
      else
      {
         return new PerformingUserFilter(filterXto.getUserOid());
      }
   }

   private static PerformingParticipantFilter unmarshalPerformingParticipantFilter(
         PerformingParticipantFilterXto filterXto)
   {
      if (filterXto.getAnyForUser() != null)
      {
         return PerformingParticipantFilter.ANY_FOR_USER;
      }
      else if (filterXto.getModelParticipant() != null)
      {
         return PerformingParticipantFilter.forParticipant(
               XmlAdapterUtils.unmarshalParticipantInfo(filterXto.getModelParticipant()
                     .getParticipant()), filterXto.getModelParticipant().isRecursively());
      }
      else
      {
         trace.error("PerformingParticipantFilterXto could not be unmarshaled");
         throw new UnsupportedOperationException(
               "PerformingParticipantFilterXto could not be unmashaled");
      }
   }

   private static PerformingOnBehalfOfFilter unmarshalPerformingOnBehalfOfFilter(
         PerformingOnBehalfOfFilterXto filterXto)
   {
      if (filterXto.getModelParticipant() != null)
      {
         return PerformingOnBehalfOfFilter.forParticipant(
               XmlAdapterUtils.unmarshalParticipantInfo(filterXto.getModelParticipant()
                     .getParticipant()), filterXto.getModelParticipant().isRecursively());
      }
      else if (filterXto.getModelParticipants() != null)
      {
         if (filterXto.getModelParticipants().getParticipants() != null)
         {
            Set<ParticipantInfo> participants = new HashSet<ParticipantInfo>(
                  XmlAdapterUtils.unmarshalParticipantInfoList(filterXto.getModelParticipants()
                        .getParticipants()
                        .getParticipant()));

            return PerformingOnBehalfOfFilter.forParticipants(participants);
         }
         else
         {
            throw new IllegalArgumentException("Element 'participants' is not optional");
         }
      }
      else
      {
         trace.error("PerformingOnBehalfOfFilter could not be unmarshaled");
         throw new UnsupportedOperationException(
               "PerformingOnBehalfOfFilter could not be unmashaled");
      }
   }

   private static FilterCriterion unmarshalPerformedByUserFilter(
         PerformedByUserFilterXto filterXto)
   {
      if (filterXto.getUserOid() == -1)
      {
         return PerformedByUserFilter.CURRENT_USER;
      }
      else
      {
         return new PerformedByUserFilter(filterXto.getUserOid());
      }
   }

   private static FilterCriterion unmarshalUserStateFilter(
         UserStateFilterXto userStateFilter)
   {
      if (userStateFilter.isLoggedInOnly())
      {
         return UserStateFilter.forLoggedInUsers();
      }
      else
      {
         throw new UnsupportedOperationException(
               "Unmarshaling UserStateFilter failed: Currently loggedInOnly must be specified as true or not at all");
      }
   }

   private static FilterCriterion unmarshalParticipantAssociationFilter(
         ParticipantAssociationFilterXto participantAssociationFilter)
   {
      if (participantAssociationFilter.getModelParticipant() != null)
      {
         return ParticipantAssociationFilter.forParticipant(
               XmlAdapterUtils.unmarshalParticipantInfo(participantAssociationFilter.getModelParticipant()
                     .getParticipant()),
               participantAssociationFilter.getModelParticipant().isRecursively());
      }
      else if (participantAssociationFilter.getTeamLeader() != null)
      {
         return ParticipantAssociationFilter.forTeamLeader((RoleInfo) XmlAdapterUtils.unmarshalParticipantInfo(participantAssociationFilter.getTeamLeader()));
      }
      else if (participantAssociationFilter.getDepartment() != null)
      {
         return ParticipantAssociationFilter.forDepartment(XmlAdapterUtils.unmarshalDepartmentInfo(participantAssociationFilter.getDepartment()));
      }
      else
      {

         trace.error("ParticipantAssociationFilterXto could not be unmarshaled");
         throw new UnsupportedOperationException(
               "Error unmarshaling ParticipantAssociationFilterXto");
      }
   }

   private static WorklistQuery unmarshalUserContribution(
         UserContributionXto userContribution, WorklistQuery query)
   {
      if (userContribution != null)
      {
         if (userContribution.getSubsetPolicy() != null)
         {
            query.setUserContribution(unmarshalSubsetPolicy(userContribution.getSubsetPolicy()));
         }
         else
         {
            query.setUserContribution(userContribution.isIncluded());
         }
      }

      return query;
   }

   private static WorklistQuery unmarshalParticipantContribution(
         ParticipantContributionXto participantContributionXto, WorklistQuery query)
   {
      if (participantContributionXto != null)
      {
         if (participantContributionXto.getSubsetPolicy() != null)
         {
            SubsetPolicy policy = unmarshalSubsetPolicy(participantContributionXto.getSubsetPolicy());
            query.setParticipantContribution(
                  unmarshalPerformingParticipantFilter(participantContributionXto.getFilter()),
                  policy);
         }
         else
         {
            query.setParticipantContribution(unmarshalPerformingParticipantFilter(participantContributionXto.getFilter()));
         }
      }
      return query;
   }

	public static PreferenceQuery unmarshalPreferenceQuery(PreferenceQueryXto preferenceQuery)
	{
		PreferenceQuery query = null;
		if (preferenceQuery != null)
		{
			final Class<PreferenceQuery> clazz = PreferenceQuery.class;
			query = createInstance(PreferenceQuery.class);

			query = unmarshalQuery(preferenceQuery, query, clazz);
		}
		return query;

	}

   public static DeployedModelQuery unmarshalDeployedModelQuery(
         DeployedModelQueryXto deployedModelQuery)
	{
      final Class<DeployedModelQuery> clazz = DeployedModelQuery.class;
      DeployedModelQuery query = createInstance(DeployedModelQuery.class);

      query = unmarshalQuery(deployedModelQuery, query, clazz);

      return query;
	}


	private static <T> T createInstance(Class<T> clazz)
	{
		try
		{
			 Constructor<T> ctor = clazz.getDeclaredConstructor(new Class[]{});

			if (!ctor.isAccessible())
			{
				ctor.setAccessible(true);
			}
			return ctor.newInstance();
		}
		catch (Exception e)
		{
			throw new InternalException("Cannot instantiate class '" + clazz.getName()
					+ "'.", e);
		}
	}
}
