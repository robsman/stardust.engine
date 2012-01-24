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

import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.OneElementIterator;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.config.TimestampProviderUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.SqlBuilderBase.VisitationContext;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;


/**
 * @author rsauer
 * @version $Revision$
 */
public final class GenericQueryEvaluator implements FilterEvaluationVisitor
{
   private static final PredicateTerm NOTHING = null;

   private final Query query;

   private final TypeDescriptor typeDescriptor;
   private final EvaluationContext evaluationContext;

   private boolean distinct = false;

   private static Query preprocessQuery(Query query, Class type)
   {
      QueryUtils.addCurrentPartitionFilter(query, type);
      return query;
   }

   /**
    * @param interfaceClass
    * @param detailsClass
    * @param iterator
    * @return
    */
   private static <I, T extends I> List<I> extractDetailsCollection(Query query, Class<?> interfaceClass,
         Class<T> detailsClass, final ResultIterator iterator)
   {
      List<I> result;

      UserDetailsLevel level = UserDetailsLevel.Full;
      UserDetailsPolicy policy = (UserDetailsPolicy) query
            .getPolicy(UserDetailsPolicy.class);
      if (null != policy)
      {
         level = policy.getLevel();
      }

      PropertyLayer layer = null;
      try
      {
         Map<String, Object> props = new HashMap<String, Object>();
         props.put(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, level);

         layer = ParametersFacade.pushLayer(props);
         /* Without <I,T> antit would result in an incompatible types error
          * The strange thing is that it is compiling in eclipse without
          * any explicit declerations */
         result = DetailsFactory.<I,T>createCollection(iterator, interfaceClass, detailsClass);
      }
      finally
      {
         if (null != layer)
         {
            ParametersFacade.popLayer();
         }
      }

      return result;
   }

   public static long count(Query query, Class beanClass, EvaluationContext context)
   {
      query = preprocessQuery(query, beanClass);

      GenericQueryEvaluator evaluator = new GenericQueryEvaluator(query, beanClass,
            context);
      Map predicateJoins = new HashMap();
      PredicateTerm resultTerm = evaluator.buildPredicate(predicateJoins);

      QueryExtension queryExtension = new QueryExtension();
      queryExtension.setDistinct(evaluator.distinct);
      queryExtension.setWhere(resultTerm);

      for (Iterator i = predicateJoins.values().iterator(); i.hasNext();)
      {
         queryExtension.addJoin((Join) i.next());
      }

      SubsetPolicy subset = QueryUtils.getSubset(query);

      long count = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getCount(
            beanClass, queryExtension, QueryUtils.getTimeOut(query));

      long countAfterSkip = Math.max(0, count - subset.getSkippedEntries());
      return Math.min(countAfterSkip, subset.getMaxSize());
   }

   public static <I, T extends I> RawQueryResult<I> evaluate(Query query, Class beanClass,
         Class interfaceClass, Class<T> detailsClass, EvaluationContext context)
   {
      query = preprocessQuery(query, beanClass);
      TypeDescriptor type = TypeDescriptor.get(beanClass);

      GenericQueryEvaluator evaluator = new GenericQueryEvaluator(query, beanClass,
            context);

      Map predicateJoins = new HashMap();
      VisitationContext visitationContext = new VisitationContext(
            query, beanClass, context, null);
      predicateJoins.put(VisitationContext.class, visitationContext);
      PredicateTerm resultTerm = evaluator.buildPredicate(predicateJoins);
      predicateJoins.remove(VisitationContext.class);

      QueryExtension queryExtension = new QueryExtension();
      queryExtension.setDistinct(evaluator.distinct);
      queryExtension.setWhere(resultTerm);

      for (Iterator i = predicateJoins.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry join = (Map.Entry) i.next();
         if (!join.getKey().equals(beanClass))
         {
            queryExtension.addJoin((Join) join.getValue());
         }
      }

      OrderByClauseBuilder orderEvaluator = new OrderByClauseBuilder(type.getType(),
            context);
      orderEvaluator.evaluateOrder(query);
      queryExtension.setOrderCriteria(orderEvaluator.getOrderCriteria());

      if (orderEvaluator.isNeedingProcessInstanceJoin()
            && !predicateJoins.containsKey(ProcessInstanceBean.class))
      {
         String piFkFieldRef;
         if (LogEntryBean.class.isAssignableFrom(beanClass))
         {
            piFkFieldRef = LogEntryBean.FIELD__PROCESS_INSTANCE;
         }
         else
         {
            // TODO what other cases are possible?
            piFkFieldRef = "processInstance";
         }
         queryExtension.addJoin(new Join(ProcessInstanceBean.class)
               .on(type.fieldRef(piFkFieldRef), ProcessInstanceBean.FIELD__OID));
      }

      Joins existingJoins = queryExtension.getJoins();
      for (Iterator i = orderEvaluator.getJoins().iterator(); i.hasNext();)
      {
         final Join join = (Join) i.next();
         if ( !existingJoins.contains(join))
         {
            queryExtension.addJoin(join);
         }
      }

      SubsetPolicy subset = QueryUtils.getSubset(query);

      final boolean countAll = subset.isEvaluatingTotalCount();

      final boolean countImplicitly = countAll
            && SubsetPolicy.UNRESTRICTED.getMaxSize() == subset.getMaxSize();

      final ResultIterator result = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .getIterator(beanClass, queryExtension, subset.getSkippedEntries(),
                  subset.getMaxSize(), null, countImplicitly,
                  QueryUtils.getTimeOut(query));

      /* Without <I,T> antit would result in an incompatible types error
       * The strange thing is that it is compiling in eclipse without
       * any explicit declerations */
      List<I> details = GenericQueryEvaluator.<I,T>extractDetailsCollection(
            query, interfaceClass, detailsClass, result);

      queryExtension.setOrderCriteria(new org.eclipse.stardust.engine.core.persistence.OrderCriteria());

      // optionally issue explicit count call to avoid fetching whole record set
      final long totalCount = countImplicitly
            ? result.getTotalCount()
            : SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getCount(beanClass,
                  queryExtension, null, QueryUtils.getTimeOut(query));

      return new RawQueryResult<I>(details, subset, result.hasMore(),
            countAll ? totalCount : null);
   }

   private GenericQueryEvaluator(Query query, Class type, EvaluationContext context)
   {
      this.query = query;

      this.typeDescriptor = TypeDescriptor.get(type);
      this.evaluationContext = context;
   }

   PredicateTerm buildPredicate(Map joins)
   {
      return (PredicateTerm) query.evaluateFilter(this, joins);
   }

   public Object visit(FilterTerm filter, Object context)
   {
      MultiPartPredicateTerm resultTerm = null;
      VisitationContext visitationContext = null;
      if(context instanceof Map)
      {
         visitationContext = (VisitationContext) ((Map)context).get(VisitationContext.class);
      }
      if (0 < filter.getParts().size())
      {
         try
         {
            if(null != visitationContext)
            {
               visitationContext.pushFilterKind(filter.getKind());
            }
            if (filter.getKind() == FilterTerm.AND)
            {
               resultTerm = new AndTerm();
            }
            else
            {
               resultTerm = new OrTerm();
            }

            for (Iterator itr = filter.getParts().iterator(); itr.hasNext();)
            {
               final FilterCriterion part = (FilterCriterion) itr.next();

               final PredicateTerm term = (PredicateTerm) part.accept(this, context);
               if (null != term)
               {
                  resultTerm.add(term);
               }
            }
         }
         finally
         {
            if(visitationContext != null)
            {
               visitationContext.popFilterKind();
            }
         }
      }

      return resultTerm;
   }

   private FieldRef processAttributeJoinDescriptor(IAttributeJoinDescriptor joinDescriptor,
         Map joinCollector)
   {
      final Class joinRhsType = joinDescriptor.getJoinRhsType();

      Join join = (Join) joinCollector.get(joinRhsType);
      if (null == join)
      {
         join = new Join(joinRhsType);
         for (Iterator iterator = joinDescriptor.getJoinFields().iterator(); iterator
               .hasNext();)
         {
            final Pair joinFields = (Pair) iterator.next();
            final String lhsField = (String) joinFields.getFirst();
            final String rhsField = (String) joinFields.getSecond();

            join.andOn(typeDescriptor.fieldRef(lhsField), rhsField);
         }

         joinCollector.put(joinRhsType, join);
      }

      return join.fieldRef(joinDescriptor.getJoinAttributeName());
   }

   private FieldRef processAttributedScopedFilter(AttributedScopedFilter filter,
         Object context)
   {
      final FieldRef fieldRef;
      if (filter instanceof IAttributeJoinDescriptor)
      {
         final IAttributeJoinDescriptor joinDescriptor = (IAttributeJoinDescriptor) filter;
         final Map joinCollector = (Map) context;

         fieldRef = processAttributeJoinDescriptor(joinDescriptor, joinCollector);
      }
      else
      {
         fieldRef = typeDescriptor.fieldRef(filter.getAttribute());
      }

      return fieldRef;
   }

   /**
    * This method creates a predicate for a query fetching users by a given set of participants.
    * If an additional join is necessary this will be created and added to joinCollector map.
    *
    * @param joinCollector
    * @param participants
    * @return
    */
   private PredicateTerm createPredicateForUsersByParticipants(
         Map joinCollector,
         Collection<? extends IParticipant> participants)
   {
      final PredicateTerm resultTerm;

      VisitationContext visitationContext = (VisitationContext)
         joinCollector.get(VisitationContext.class);
      FilterTerm.Kind filterKind = visitationContext != null ?
         visitationContext.peekLastFilterKind() : null;

      String tableName = TypeDescriptor.get(UserParticipantLink.class).getTableName();
      Join grantTableJoin = (Join) joinCollector.get(tableName);
      if (null == grantTableJoin)
      {
         grantTableJoin = new Join(UserParticipantLink.class).on(UserBean.FR__OID,
               UserParticipantLink.FIELD__USER);
         joinCollector.put(tableName, grantTableJoin);
      }
      else if(FilterTerm.AND.equals(filterKind))
      {
         // if it is an AndTerm then we have to join an additional user_participant table
         int lastJoinIdx = 0;
         for(int i = 1; i<=joinCollector.size(); ++i)
         {
            if(null != joinCollector.get(tableName + i))
            {
               lastJoinIdx = i;
            }
         }
         String tableAlias = grantTableJoin.getTableAlias() + ++lastJoinIdx;
         Join tableJoin = new Join(UserParticipantLink.class, tableAlias).on(UserBean.FR__OID,
               UserParticipantLink.FIELD__USER);
         joinCollector.put(tableName + lastJoinIdx, tableJoin);
         tableJoin.andOn(grantTableJoin.fieldRef(UserParticipantLink.FIELD__USER),
               UserParticipantLink.FIELD__USER);
         tableJoin.setDependency(grantTableJoin);
         grantTableJoin = tableJoin;
      }

      if (participants.isEmpty())
      {
         resultTerm = Predicates.isNull(grantTableJoin
               .fieldRef(UserParticipantLink.FIELD__PARTICIPANT));
      }
      else
      {
         Map<Long, Set<Long>> participantRtOids = new HashMap(participants.size());
         for (IParticipant rawParticipant : participants)
         {
            IDepartment department = null;

            if (rawParticipant instanceof IScopedModelParticipant)
            {
               IScopedModelParticipant scopedModelParticipant = (IScopedModelParticipant) rawParticipant;
               rawParticipant = scopedModelParticipant.getModelParticipant();
               department = scopedModelParticipant.getDepartment();
            }

            if (rawParticipant instanceof IModelParticipant)
            {
               IModelParticipant participant = (IModelParticipant) rawParticipant;
               final Long participantRtOid = Long.valueOf(evaluationContext
                     .getModelManager().getRuntimeOid(participant));
               Set<Long> departments = participantRtOids.get(participantRtOid);
               if (null == departments)
               {
                  departments = new HashSet<Long>();
                  participantRtOids.put(participantRtOid, departments);
               }

               departments
                     .add(Long.valueOf(department == null ? 0 : department.getOID()));
            }
         }

         OrTerm spOrTerm = new OrTerm();
         for (Entry<Long, Set<Long>> entry : participantRtOids.entrySet())
         {
            Long participant = entry.getKey();
            Set<Long> departments = entry.getValue();

            spOrTerm.add(Predicates.andTerm(
                  Predicates.isEqual(
                        grantTableJoin.fieldRef(UserParticipantLink.FIELD__PARTICIPANT),
                        participant),
                  Predicates.inList(
                        grantTableJoin.fieldRef(UserParticipantLink.FIELD__DEPARTMENT),
                        departments.iterator())));
         }

         resultTerm = spOrTerm;
      }

      return resultTerm;
   }

   /**
    * This method creates a predicate for a query fetching users by a given department.
    * Is department is null it will be interpreted as default department with oid 0.
    * If an additional join is necessary this will be created and added to joinCollector map.
    *
    * @param joinCollector
    * @param department, may be null.
    * @return
    */
   private PredicateTerm createPredicateForUsersByDepartment(Map joinCollector,
         DepartmentInfo department)
   {
      final PredicateTerm resultTerm;

      Join grantTableJoin = (Join) joinCollector.get(TypeDescriptor
            .get(UserParticipantLink.class));
      if (null == grantTableJoin)
      {
         grantTableJoin = new Join(UserParticipantLink.class).on(UserBean.FR__OID,
               UserParticipantLink.FIELD__USER);
         joinCollector.put(TypeDescriptor.get(UserParticipantLink.class), grantTableJoin);
      }

      long depOid = department == null ? 0 : department.getOID();

      resultTerm = Predicates.isEqual(grantTableJoin
            .fieldRef(UserParticipantLink.FIELD__DEPARTMENT), depOid);

      return resultTerm;
   }

   public Object visit(UnaryOperatorFilter filter, Object context)
   {
      FieldRef fieldRef = processAttributedScopedFilter(filter, context);
      return new ComparisonTerm(fieldRef, filter.getOperator());
   }

   public Object visit(BinaryOperatorFilter filter, Object context)
   {
      FieldRef fieldRef = processAttributedScopedFilter(filter, context);
      return new ComparisonTerm(fieldRef, filter.getOperator(), filter.getValue());
   }

   public Object visit(TernaryOperatorFilter filter, Object context)
   {
      FieldRef fieldRef = processAttributedScopedFilter(filter, context);
      return new ComparisonTerm(fieldRef, filter.getOperator(), filter.getValue());
   }

   public Object visit(ProcessDefinitionFilter filter, Object context)
   {
      Map joinCollector = (Map) context;

      if (!joinCollector.containsKey(ProcessInstanceBean.class))
      {
         joinCollector.put(ProcessInstanceBean.class, new Join(ProcessInstanceBean.class)
               .on(typeDescriptor.fieldRef("processInstance"), ProcessInstanceBean.FIELD__OID));
      }

      Set processRtOids = new HashSet();
      String namespace = null;
      String processID = filter.getProcessID();
      if (processID.startsWith("{"))
      {
         QName qname = QName.valueOf(processID);
         namespace = qname.getNamespaceURI();
         processID = qname.getLocalPart();
      }

      Iterator modelItr = null;
      if (namespace != null)
      {
         modelItr = evaluationContext.getModelManager().getAllModelsForId(namespace);
      }
      else
      {
         modelItr = evaluationContext.getModelManager().getAllModels();
      }

      while (modelItr.hasNext())
      {
         IModel model = (IModel) modelItr.next();
         IProcessDefinition process = model.findProcessDefinition(processID);
         if (null != process)
         {
            processRtOids.add(new Long(evaluationContext.getModelManager().getRuntimeOid(
                  process)));
         }
      }

      AndTerm resultTerm = new AndTerm();
      resultTerm.add(Predicates.inList(ProcessInstanceBean.FR__PROCESS_DEFINITION,
            processRtOids.iterator()));

      QueryUtils.addModelVersionPredicate(resultTerm, ProcessInstanceBean.FR__MODEL,
            query, evaluationContext.getModelManager());

      return resultTerm;
   }

   public Object visit(ProcessStateFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(ProcessInstanceFilter filter, Object context)
   {
      PredicateTerm resultTerm;
      if (filter.getOids().isEmpty())
      {
         // insert tautology
         resultTerm = Predicates.isNull(typeDescriptor.fieldRef("processInstance"));
      }
      else
      {
         resultTerm = Predicates.inList(typeDescriptor.fieldRef("processInstance"),
               filter.getOids().iterator());
      }

      return resultTerm;
   }

   public Object visit(StartingUserFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(ActivityFilter filter, Object context)
   {
      Map joinCollector = (Map) context;
      if (!joinCollector.containsKey(ActivityInstanceBean.class))
      {
         joinCollector.put(ActivityInstanceBean.class, new Join(
               ActivityInstanceBean.class).on(
               typeDescriptor.fieldRef("activityInstance"),
               ActivityInstanceBean.FIELD__OID));
      }
      final Collection modelOids = filter.getModelOids();
      final boolean isModelOidsEmpty = modelOids.isEmpty();
      final ModelManager modelManager = evaluationContext.getModelManager();
      Iterator/*<IModel>*/ i = SqlBuilderBase.limitIteratorToRequiredModels(modelOids, modelManager);
      Set activityRtOids = new HashSet();
      // TODO leverage model version policy to restrict number of models traversed
      for (; i.hasNext();)
      {
         IModel model = (IModel) i.next();

         Iterator procDefIterator;
         if (null != filter.getProcessID())
         {
            IProcessDefinition process = model.findProcessDefinition(filter
                  .getProcessID());
            if (null != process)
            {
               if (filter.isIncludingSubProcesses())
               {
                  Set hierarchy = QueryUtils.findProcessHierarchyClosure(process);
                  procDefIterator = hierarchy.iterator();
               }
               else
               {
                  procDefIterator = new OneElementIterator(process);
               }
            }
            else
            {
               procDefIterator = Collections.EMPTY_LIST.iterator();
            }
         }
         else
         {
            procDefIterator = model.getAllProcessDefinitions();
         }

         while (procDefIterator.hasNext())
         {
            IProcessDefinition process = (IProcessDefinition) procDefIterator.next();
            IActivity activity = process.findActivity(filter.getActivityID());
            if (null != activity)
            {
               activityRtOids.add(new Long(modelManager.getRuntimeOid(activity)));
            }
         }
      }
      PredicateTerm resultTerm = null;
      if (activityRtOids.isEmpty())
      {
         resultTerm = Predicates.isNull(ActivityInstanceBean.FR__ACTIVITY);
      }
      else
      {
         AndTerm andTerm = new AndTerm();
         andTerm.add(Predicates.inList(ActivityInstanceBean.FR__ACTIVITY, activityRtOids
               .iterator()));

         if (!isModelOidsEmpty)
         {
            PredicateTerm modelTerm = Predicates.inList(ActivityInstanceBean.FR__MODEL,
                  modelOids.iterator());
            andTerm.add(modelTerm);
         }
         QueryUtils.addModelVersionPredicate(andTerm, ActivityInstanceBean.FR__MODEL,
               query, modelManager);
         resultTerm = andTerm;

      }
      return resultTerm;
   }

   public Object visit(ActivityInstanceFilter filter, Object context)
   {
      return Predicates.isEqual(typeDescriptor.fieldRef("activityInstance"),
            filter.getOID());
   }

   public Object visit(ActivityStateFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(PerformingUserFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(PerformingParticipantFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(PerformingOnBehalfOfFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(PerformedByUserFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(AbstractDataFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(CurrentPartitionFilter filter, Object context)
   {
      Map /*<TableDescriptor, Join> */ joinCollector = (Map) context;
      PredicateTerm resultTerm = NOTHING;

      if (ActivityInstanceBean.class.isAssignableFrom(filter.getType()) )
      {
         if ( !joinCollector.containsKey(ModelPersistorBean.class))
         {
            joinCollector.put(
                  ModelPersistorBean.class,//
                  new Join(ModelPersistorBean.class)//
                        .andOn(ActivityInstanceBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID));
         }

         resultTerm = Predicates.isEqual(ModelPersistorBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (WorkItemBean.class.isAssignableFrom(filter.getType()) )
      {
         if ( !joinCollector.containsKey(ModelPersistorBean.class))
         {
            joinCollector.put(
                  ModelPersistorBean.class,//
                  new Join(ModelPersistorBean.class)//
                        .andOn(WorkItemBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID));
         }

         resultTerm = Predicates.isEqual(ModelPersistorBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (ProcessInstanceBean.class.isAssignableFrom(filter.getType()) )
      {
         if ( !joinCollector.containsKey(ModelPersistorBean.class))
         {
            joinCollector.put(
                  ModelPersistorBean.class,//
                  new Join(ModelPersistorBean.class)//
                        .andOn(ProcessInstanceBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID));
         }

         resultTerm = Predicates.isEqual(ModelPersistorBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (UserBean.class.isAssignableFrom(filter.getType()) )
      {
         if ( !joinCollector.containsKey(UserRealmBean.class))
         {
            joinCollector.put(
                  UserRealmBean.class,//
                  new Join(UserRealmBean.class)//
                        .andOn(UserBean.FR__REALM,
                              UserRealmBean.FIELD__OID));
         }

         resultTerm = Predicates.isEqual(UserRealmBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (UserGroupBean.class.isAssignableFrom(filter.getType()) )
      {
         resultTerm = Predicates.isEqual(UserGroupBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (LogEntryBean.class.isAssignableFrom(filter.getType()) )
      {
         resultTerm = Predicates.isEqual(LogEntryBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else
      {
         Assert.lineNeverReached(//
               "CurrentPartitionFilter is not valid for this kind of query.");
      }

      return resultTerm;
   }

   public Object visit(ParticipantAssociationFilter filter, Object context)
   {
      Map /*<TableDescriptor, Join> */ joinCollector = (Map) context;
      ParticipantAssociationFilter.Kind filterKind = filter.getFilterKind();

      final PredicateTerm resultTerm;
      if (ParticipantAssociationFilter.FILTER_KIND_MODEL_PARTICIPANT.equals(filterKind))
      {
         this.distinct = true;
         // original comment by
         // rsauer: A join was chosen as a replacement for a sub-select. This join of the
         // USER_PARTICIPANT table will create duplicate user instances in the result as of
         // possible the 1-N grant relationships. Preparation of the final result requires
         // removal of duplicate rows.

         Set<IParticipant> participants = QueryUtils.findParticipants(filter,
               evaluationContext);

         resultTerm = createPredicateForUsersByParticipants(joinCollector, participants);
      }
      else if (ParticipantAssociationFilter.FILTER_KIND_USER_GROUP.equals(filterKind))
      {
         Join ugLinkJoin = (Join) joinCollector.get(TypeDescriptor.get(UserUserGroupLink.class));
         if (null == ugLinkJoin)
         {
            ugLinkJoin = new Join(UserUserGroupLink.class)
                  .on(UserBean.FR__OID, UserUserGroupLink.FIELD__USER);
            joinCollector.put(ugLinkJoin.getRhsTableDescriptor(), ugLinkJoin);
         }

         Join groupJoin = (Join) joinCollector.get(TypeDescriptor.get(UserGroupBean.class));
         if (null == groupJoin)
         {
            groupJoin = new Join(UserGroupBean.class)
                  .on(UserUserGroupLink.FR__USER_GROUP, UserGroupBean.FIELD__OID);
            groupJoin.setDependency(ugLinkJoin);
            joinCollector.put(groupJoin.getRhsTableDescriptor(), groupJoin);
         }

         if (null == filter.getParticipant())
         {
            resultTerm = Predicates.isNull(groupJoin.fieldRef(UserGroupBean.FIELD__ID));
         }
         else
         {
            resultTerm = Predicates.isEqual(groupJoin.fieldRef(UserGroupBean.FIELD__ID),
                  filter.getParticipant().getId());
         }
      }
      else if (ParticipantAssociationFilter.FILTER_KIND_USER.equals(filterKind))
      {
         Join ugLinkJoin = (Join) joinCollector.get(TypeDescriptor.get(UserUserGroupLink.class));
         if (null == ugLinkJoin)
         {
            ugLinkJoin = new Join(UserUserGroupLink.class)
                  .on(UserGroupBean.FR__OID, UserUserGroupLink.FIELD__USER_GROUP);
            joinCollector.put(ugLinkJoin.getRhsTableDescriptor(), ugLinkJoin);
         }

         Join userJoin = (Join) joinCollector.get(TypeDescriptor.get(UserBean.class));
         if (null == userJoin)
         {
            userJoin = new Join(UserBean.class)
                  .on(UserUserGroupLink.FR__USER, UserBean.FIELD__OID);
            userJoin.setDependency(ugLinkJoin);
            joinCollector.put(userJoin.getRhsTableDescriptor(), userJoin);
         }

         resultTerm = Predicates.isEqual(userJoin.fieldRef(UserBean.FIELD__ACCOUNT),
               filter.getParticipant().getId());
      }
      else if (ParticipantAssociationFilter.FILTER_KIND_TEAM_LEADER.equals(filterKind))
      {
         // users can be member of many organizations. This will prevent duplicate users.
         this.distinct = true;

         Set<IModelParticipant> allOrgs = QueryUtils.findOrganizationsAndRolesByTeamLeaderRole(filter,
               evaluationContext);

         resultTerm = createPredicateForUsersByParticipants(joinCollector, allOrgs);
      }
      else if (ParticipantAssociationFilter.FILTER_KIND_DEPARTMENT.equals(filterKind))
      {
         // users can be member of many participants with same department. This will prevent duplicate users.
         this.distinct = true;

         ModelParticipantInfo participant = (ModelParticipantInfo) filter
               .getParticipant();
         resultTerm = createPredicateForUsersByDepartment(joinCollector, participant
               .getDepartment());
      }
      else
      {
         Assert.lineNeverReached("Unknown filter kind in ParticipantAssociationFilter.");
         resultTerm = NOTHING;
      }

      return resultTerm;
   }

   public Object visit(UserStateFilter filter, Object context)
   {
      Map /*<TableDescriptor, Join> */joinCollector = (Map) context;
      PredicateTerm resultTerm = NOTHING;

      if (filter.isLoggedInOnly())
      {
         // users can have many sessions. This will prevent duplicate users.
         this.distinct = true;

         if ( !joinCollector.containsKey(UserSessionBean.class))
         {
            joinCollector.put(UserSessionBean.class,//
                  new Join(UserSessionBean.class)//
                        .andOn(UserBean.FR__OID, UserSessionBean.FIELD__USER));
         }

         long now = TimestampProviderUtils.getTimeStamp().getTime();

         resultTerm = Predicates.andTerm( //
               Predicates.lessOrEqual(UserSessionBean.FR__START_TIME, now), //
               Predicates.greaterOrEqual(UserSessionBean.FR__EXPIRATION_TIME, now));
      }

      return resultTerm;
   }

   public Object visit(ProcessInstanceLinkFilter filter, Object context)
   {
     return NOTHING;
   }

   public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
   {
      return NOTHING;
   }
}
