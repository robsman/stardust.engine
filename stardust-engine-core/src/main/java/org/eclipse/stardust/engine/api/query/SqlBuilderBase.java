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
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.ChangeLogDigester;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.extensions.dms.data.AuditTrailUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.vfs.impl.utils.StringUtils;

/**
 * Filter evaluator generating SQL from a process or activity instance query.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see ProcessHierarchyPreprocessor
 */
public abstract class SqlBuilderBase implements SqlBuilder, FilterEvaluationVisitor,
      OrderEvaluationVisitor
{
   private static final Logger trace = LogManager.getLogger(SqlBuilderBase.class);

   public static final PredicateTerm NOTHING = null;

   private static final String DATA_ORDER_PI_JOIN_TAG = "DV_PI";

   private static final String DATA_ORDER_PIS_JOIN_TAG = "DV_PIS";

   private static final String CASE_JOIN_TAG = "CASE_PI";

   protected static final int SQL_IN_CHUNK_SIZE = 1000;

   protected final Map<Object, Join> dataJoinMapping = new HashMap<Object, Join>();
   protected Join glueJoin = null;
   protected int subProcModeCounter = 0;
   protected int allFromHierModeCounter = 0;

   private int appliedProcDefFilterJoinCounter = 0;

   public ParsedQuery buildSql(Query query, Class type, EvaluationContext evaluationContext)
   {
      DataFilterExtensionContext dataFilterExtensionContext = new DataFilterExtensionContext(query.getFilter());

      final VisitationContext context = new VisitationContext(query, type,
            evaluationContext, dataFilterExtensionContext);

      return buildSql(context);
   }

   protected ParsedQuery buildSql(final VisitationContext context)
   {
      PreprocessedQuery preprocessedQuery = preprocessQuery(context);

      processCasePolicy(context);

      // traverse preprocessed filter tree to build SQL fragments

      PredicateTerm resultTerm = null;
      final PredicateTerm filterTerm = (PredicateTerm) preprocessedQuery.getFilter()
            .accept(this, context);

      // apply model version policy only once to simplify statement structure
      final PredicateTerm modelVersioningFilter = buildModelVersionPredicate(context);

      if ((null != filterTerm) && (null != modelVersioningFilter))
      {
         resultTerm = Predicates.andTerm(filterTerm, modelVersioningFilter);
      }
      else
      {
         resultTerm = (null != filterTerm) ? filterTerm : modelVersioningFilter;
      }


      final org.eclipse.stardust.engine.core.persistence.OrderCriteria orderCriteria = evaluateOrder(context);
      final List<Join> orderByJoins = getOrderByJoins(context);

      return new ParsedQuery(context.getSelectExtension(), resultTerm, getJoins(context),
            orderCriteria, orderByJoins, preprocessedQuery.getFetchPredicate(),
            context.useDistinct(), context.getSelectAlias());
   }

   private void processCasePolicy(VisitationContext context)
   {
      CasePolicy casePolicy = (CasePolicy) context.getQuery().getPolicy(CasePolicy.class);

      if (casePolicy != null)
      {
         long caseDefinitionOid = -1;
         List<IModel> models = ModelManagerFactory.getCurrent().getModelsForId(PredefinedConstants.PREDEFINED_MODEL_ID);
         if (models != null && !models.isEmpty())
         {
            IModel iModel = models.get(0);
            IProcessDefinition caseProcessDefinition = iModel.findProcessDefinition(PredefinedConstants.CASE_PROCESS_ID);
            if (caseProcessDefinition != null)
            {
               caseDefinitionOid = ModelManagerFactory.getCurrent().getRuntimeOid(caseProcessDefinition);
            }
         }

         if (caseDefinitionOid == -1)
         {
            trace.warn("Could not find PredefinedModel while processing CasePolicy.");
         }

         Class<?> joinKey = CasePolicy.class;
         Join caseJoin = (Join) context.getPredicateJoins().get(joinKey);
         if (caseJoin == null)
         {
            caseJoin = new Join(ProcessInstanceBean.class, CASE_JOIN_TAG).on(
                  ProcessInstanceBean.FR__OID, ProcessInstanceBean.FIELD__OID);
            caseJoin.orOn(ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE,
                  ProcessInstanceBean.FIELD__OID);
            caseJoin.andOnConstant(
                  caseJoin.fieldRef(ProcessInstanceBean.FIELD__PROCESS_DEFINITION),
                  Long.valueOf(caseDefinitionOid).toString());
            context.useDistinct(true);
            context.getPredicateJoins().put(joinKey, caseJoin);

            context.setSelectAlias(CASE_JOIN_TAG);
         }
      }
   }

   protected PreprocessedQuery preprocessQuery(VisitationContext context)
   {
      Query query = context.getQuery();
      QueryUtils.addCurrentPartitionFilter(query, context.getType());

      ProcessHierarchyPreprocessor preprocessor = createQueryPreprocessor();
      ProcessHierarchyPreprocessor.Node preprocessingResult = preprocessor.preprocessQuery(
            context.getQuery(), context.getEvaluationContext());

      Assert.isNull(preprocessingResult.getRootProcessOIDs(),
            "Root process OID handling has to be done during FilterTerm preprocessing");

      Set processOIDs;
      FilterCriterion preprocessedFilter;
      if ((null != preprocessingResult.getProcessOIDs())
            && (preprocessingResult.getProcessOIDs().size() <= Parameters.instance()
                  .getLong(KernelTweakingProperties.INLINE_PROCESS_OID_THRESHOLD, 100)))
      {
         // optimize queries involving very selective preprocessing results
         BlacklistFilterVerifyer LENIENT_VERIFYER = new BlacklistFilterVerifyer(new Class[] {});
         preprocessedFilter = new FilterAndTerm(LENIENT_VERIFYER)
               .and(new ProcessInstanceFilter(preprocessingResult.getProcessOIDs(), false))
               .and(preprocessingResult.getFilter());

         processOIDs = null;
      }
      else
      {
         preprocessedFilter = preprocessingResult.getFilter();
         processOIDs = preprocessingResult.getProcessOIDs();
      }

      // fetch predicates are used to realize not (efficently) SQL-executable predicates
      // while fetching the candidate result set
      FetchPredicate fetchPredicate = null;
      if (null != processOIDs)
      {
         if (ProcessInstanceBean.class.equals(context.getType()))
         {
            fetchPredicate = new ProcessInstanceFetchPredicate(processOIDs);
         }
         else if (ActivityInstanceBean.class.equals(context.getType()))
         {
            fetchPredicate = new ActivityInstancePiFetchPredicate(processOIDs);
         }
         else if (WorkItemBean.class.equals(context.getType()))
         {
            fetchPredicate = new WorkItemPiFetchPredicate(processOIDs);
         }
         else
         {
            trace.warn("Don't know how to apply OID fetch predicate for type '"
                  + context.getType().getName() + "'");

            fetchPredicate = null;
         }
      }

      return new PreprocessedQuery(preprocessedFilter, fetchPredicate);
   }

   /**
    * Factory method allowing for varying query preprocessing strategies.
    * @return The query preprocessing strategy to be used.
    */
   protected ProcessHierarchyPreprocessor createQueryPreprocessor()
   {
      return new ProcessHierarchyAndDataPreprocessor();
   }

   protected PredicateTerm buildModelVersionPredicate(final VisitationContext context)
   {
      ModelVersionPolicy modelVersioning = (ModelVersionPolicy) context.getQuery()
            .getPolicy(ModelVersionPolicy.class);

      PredicateTerm resultTerm;
      if ((null != modelVersioning) && modelVersioning.isRestrictedToActiveModel())
      {
         IModel activeModel = context.getEvaluationContext()
               .getModelManager()
               .findActiveModel();
         if (null != activeModel)
         {
            if (ProcessInstanceBean.class.isAssignableFrom(context.getType()))
            {
               resultTerm = Predicates.isEqual(ProcessInstanceBean.FR__MODEL, activeModel
                     .getModelOID());
            }
            else if (ActivityInstanceBean.class.isAssignableFrom(context.getType()))
            {
               resultTerm = Predicates.isEqual(ActivityInstanceBean.FR__MODEL,
                     activeModel.getModelOID());
            }
            else if (WorkItemBean.class.isAssignableFrom(context.getType()))
            {
               resultTerm = Predicates.isEqual(WorkItemBean.FR__MODEL, activeModel
                     .getModelOID());
            }
            else
            {
               trace
                     .warn("Ignoring model version policy while querying for instances of "
                           + context.getType() + ".");
               resultTerm = NOTHING;
            }
         }
         else
         {
            trace.warn("ModelVersionPolicy[restricted to active model] is used, but "
                  + "no model is active.");
            resultTerm = NOTHING;
         }
      }
      else
      {
         resultTerm = NOTHING;
      }

      return resultTerm;
   }

   protected List<Join> getJoins(VisitationContext context)
   {
      List<Join> joins = new ArrayList<Join>(context.getPredicateJoins().size()
            + dataJoinMapping.size());

      for (Iterator itr = context.getPredicateJoins().entrySet().iterator(); itr
            .hasNext();)
      {
         Map.Entry join = (Map.Entry) itr.next();

         Object rawKey = join.getKey();
         if (rawKey instanceof Class)
         {
            if (!context.getType().isAssignableFrom((Class) rawKey))
            {
               joins.add((Join)join.getValue());
            }
         }
         else if (rawKey instanceof Pair)
         {
            joins.add((Join)join.getValue());
         }
         else if (rawKey instanceof ProcessDefinitionFilterJoinKey) {
            joins.add((Join)join.getValue());
         }
         else
         {
            throw new InternalException(MessageFormat.format(
                  "Cannot handle key of type {0}.", new Object[] {rawKey.getClass()
                        .getName()}));
         }
      }
      joins.addAll(dataJoinMapping.values());

      // additional joins must keep the order from DataFilterExtensionContext
      // therefore they are first removed from joins
      joins.removeAll(context.getDataFilterExtensionContext().getJoins());
      // and then added in the correct order
      joins.addAll(context.getDataFilterExtensionContext().getJoins());

      return joins;
   }

   protected List<Join> getOrderByJoins(VisitationContext context)
   {
      int size = context.getCustomOrderJoins().size();
      size += context.getDataOrderJoins().size();
      List<Join> orderByJoins = new ArrayList(size);
      // TODO sort by alias?
      orderByJoins.addAll(context.getDataOrderJoins().values());
      orderByJoins.addAll(context.getCustomOrderJoins().values());
      return orderByJoins;
   }

   public Object visit(FilterTerm filter, Object rawContext)
   {
      MultiPartPredicateTerm resultTerm = null;
      VisitationContext context = (VisitationContext) rawContext;
      
      if (0 < filter.getParts().size())
      {
         try
         {
            context.pushFilterKind(filter.getKind());
            if (FilterTerm.AND == filter.getKind())
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
            context.popFilterKind();
         }
      }

      return resultTerm;
   }

   private FieldRef processAttributeJoinDescriptor(
         IAttributeJoinDescriptor joinDescriptor, VisitationContext context)
   {
      return processAttributeJoinDescriptor(joinDescriptor, context, TypeDescriptor
            .get(context.getType()), true);
   }

   private FieldRef processAttributeJoinDescriptor(
         IAttributeJoinDescriptor joinDescriptor, VisitationContext context,
         boolean predicate)
   {
      return processAttributeJoinDescriptor(joinDescriptor, context, TypeDescriptor
            .get(context.getType()), predicate);
   }

   private FieldRef processAttributeJoinDescriptor(IAttributeJoinDescriptor joinDescriptor,
         VisitationContext context, ITableDescriptor tableDescriptor, boolean predicate)
   {
      final Class joinRhsType = joinDescriptor.getJoinRhsType();
      final Map joinCollector = predicate ? context.getPredicateJoins() : context.getCustomOrderJoins();

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

            join.andOn(tableDescriptor.fieldRef(lhsField), rhsField);
         }
         joinCollector.put(joinRhsType, join);
      }

      return join.fieldRef(joinDescriptor.getJoinAttributeName());
   }

   private FieldRef processAttributedScopedFilter(AttributedScopedFilter filter,
         VisitationContext context)
   {
      final boolean isAiQuery = ActivityInstanceBean.class.equals(context.getType());
      final boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());
      
      FieldRef fieldRef;
      
      
      if (filter instanceof IAttributeJoinDescriptor)
      {
         final IAttributeJoinDescriptor joinDescriptor = (IAttributeJoinDescriptor) filter;

         fieldRef = processAttributeJoinDescriptor(joinDescriptor, context);
      }
      else
      {
         TypeDescriptor typeDescriptor = TypeDescriptor.get(context.getType());
         fieldRef = typeDescriptor.fieldRef(filter.getAttribute());
         if (isAiQuery || isAiQueryOnWorkItem)
         {
            if (isAiQueryOnWorkItem)
            {
               if (WorkitemKeyMap.isMapped(filter.getAttribute()))
               {
                  fieldRef = WorkitemKeyMap.getFieldRef(filter.getAttribute());
                  
               }
            }
         }
      }
      

      return fieldRef;
   }

   private void normalizeParticipants(VisitationContext context,
         Set<IParticipant> participants,
         Map<ScopedParticipantInfo, Set<Long>> versionedRtOids,
         Set<ScopedParticipantInfo> unversionedRtOids)
   {
      final ModelManager modelManager = context.getEvaluationContext().getModelManager();

      for (IParticipant contributor : participants)
      {
         if (contributor instanceof IScopedModelParticipant)
         {
            IScopedModelParticipant scopedModelParticipant = (IScopedModelParticipant) contributor;
            IDepartment department = scopedModelParticipant.getDepartment();

            final long departmentOid = department == null ? 0 : department.getOID();
            final long rtOid = new Long(modelManager.getRuntimeOid(scopedModelParticipant));

            ScopedParticipantInfo scopedKey = new ScopedParticipantInfo(rtOid,
                  departmentOid);

            unversionedRtOids.add(scopedKey);
         }
         else if (contributor instanceof IUserGroup)
         {
            IUserGroup userGroup = (IUserGroup) contributor;
            unversionedRtOids.add(new ScopedParticipantInfo( -userGroup.getOID(), 0L));
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Unsupported worklist contributor: " + contributor);
            }
         }
      }

      // find versioned participants, that are enabled for all model versions, thus
      // effectively being unversioned
      if ( !versionedRtOids.isEmpty())
      {
         Set<Long> allModelOids = new TreeSet();
         for (Iterator modelItr = modelManager.getAllModels(); modelItr.hasNext();)
         {
            allModelOids.add(Long.valueOf((((IModel) modelItr.next()).getModelOID())));
         }

         for (Iterator<Entry<ScopedParticipantInfo, Set<Long>>> i = versionedRtOids.entrySet()
               .iterator(); i.hasNext();)
         {
            Entry<ScopedParticipantInfo, Set<Long>> entry = i.next();
            if (allModelOids.equals(entry.getValue()))
            {
               unversionedRtOids.add(entry.getKey());
               i.remove();
            }
         }
      }
   }

   public Object visit(UnaryOperatorFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;
      FieldRef fieldRef = processAttributedScopedFilter(filter, context);
      return new ComparisonTerm(fieldRef, filter.getOperator());
   }

   public Object visit(BinaryOperatorFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;
      
      
      
      FieldRef fieldRef = processAttributedScopedFilter(filter, context);
      return new ComparisonTerm(fieldRef, filter.getOperator(), filter.getValue());
   }

   public Object visit(TernaryOperatorFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;
      FieldRef fieldRef = processAttributedScopedFilter(filter, context);
      return new ComparisonTerm(fieldRef, filter.getOperator(), filter.getValue());
   }

   public Object visit(ProcessDefinitionFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      FieldRef piProcDefFieldRef = ProcessInstanceBean.FR__PROCESS_DEFINITION;
      int joinCount = context.getPredicateJoins().size() + 1;

      final boolean isAiQuery = ActivityInstanceBean.class.equals(context.getType());
      final boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());

         ++appliedProcDefFilterJoinCounter;
      if (appliedProcDefFilterJoinCounter > 1)
         {
            context.useDistinct(true);
         }

      ProcessDefinitionFilterJoinKey piJoinKey;
      ProcessDefinitionFilterJoinKey pihJoinKey;
      // for and terms and subprocesses use separate joins
      if (filter.isIncludingSubProcesses() && isAndTerm(context))
      {
         piJoinKey = new ProcessDefinitionFilterJoinKey(filter,
               ProcessInstanceBean.class, appliedProcDefFilterJoinCounter);
         pihJoinKey = new ProcessDefinitionFilterJoinKey(filter,
               ProcessInstanceHierarchyBean.class, appliedProcDefFilterJoinCounter);
      }
      // reuse join for better performance
      else
      {
         piJoinKey = new ProcessDefinitionFilterJoinKey(filter, ProcessInstanceBean.class);
         pihJoinKey = new ProcessDefinitionFilterJoinKey(filter,
               ProcessInstanceHierarchyBean.class);
      }

      if (isAiQuery || isAiQueryOnWorkItem)
      {
         Join piJoin;

         FieldRef frProcessInstance = ActivityInstanceBean.FR__PROCESS_INSTANCE;
         if (isAiQueryOnWorkItem)
         {
            frProcessInstance = WorkItemBean.FR__PROCESS_INSTANCE;
         }

         if (filter.isIncludingSubProcesses())
         {
            Join pihJoin = (Join) context.getPredicateJoins().get(pihJoinKey);
            if (pihJoin == null)
            {
               pihJoin = new Join(ProcessInstanceHierarchyBean.class, "PDF_PIH"
                     + joinCount).on(frProcessInstance,
                     ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE);

               context.getPredicateJoins().put(pihJoinKey, pihJoin);
            }

            piJoin = (Join) context.getPredicateJoins().get(piJoinKey);
            if (piJoin == null)
            {
            piJoin = new Join(ProcessInstanceBean.class, "PDF_PI" + joinCount)
                     .on(pihJoin
                           .fieldRef(ProcessInstanceHierarchyBean.FIELD__PROCESS_INSTANCE),
                        ProcessInstanceBean.FIELD__OID);
            piJoin.setDependency(pihJoin);

               context.getPredicateJoins().put(piJoinKey, piJoin);
         }
         }
         else
         {
            piJoin = (Join) context.getPredicateJoins().get(piJoinKey);
            if (null == piJoin)
            {
               piJoin = new Join(ProcessInstanceBean.class, "PDF_PI" + joinCount) //
                     .on(frProcessInstance, ProcessInstanceBean.FIELD__OID);
               context.getPredicateJoins().put(piJoinKey, piJoin);
            }
         }

         piProcDefFieldRef = piJoin
               .fieldRef(ProcessInstanceBean.FIELD__PROCESS_DEFINITION);
      }
      else
      {
         if (filter.isIncludingSubProcesses())
         {
            Join pihJoin = (Join) context.getPredicateJoins().get(pihJoinKey);
            if (pihJoin == null)
            {
               pihJoin = new Join(ProcessInstanceHierarchyBean.class, "PDF_PIH"
                     + joinCount).on(ProcessInstanceBean.FR__OID,
                     ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE);
               context.getPredicateJoins().put(pihJoinKey, pihJoin);
            }

            Join piJoin = (Join) context.getPredicateJoins().get(piJoinKey);
            if (null == piJoin)
            {
               piJoin = new Join(ProcessInstanceBean.class, "PDF_PI" + joinCount)
                     .on(pihJoin
                           .fieldRef(ProcessInstanceHierarchyBean.FIELD__PROCESS_INSTANCE),
                        ProcessInstanceBean.FIELD__OID);
            piJoin.setDependency(pihJoin);
               context.getPredicateJoins().put(piJoinKey, piJoin);
            }

            piProcDefFieldRef = piJoin
                  .fieldRef(ProcessInstanceBean.FIELD__PROCESS_DEFINITION);
         }
      }

      ModelManager modelManager = context.getEvaluationContext().getModelManager();

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
         modelItr = modelManager.getAllModelsForId(namespace);
      }
      else
      {
         modelItr = modelManager.getAllModels();
      }

      Set processRtOids = new HashSet();
      while (modelItr.hasNext())
      {
         IModel model = (IModel) modelItr.next();
         IProcessDefinition process = model.findProcessDefinition(processID);
         if (null != process)
         {
            processRtOids.add(new Long(modelManager.getRuntimeOid(process)));
         }
      }

      ComparisonTerm predicate;
      if (processRtOids.isEmpty())
      {
         predicate = Predicates.isNull(piProcDefFieldRef);
      }
      else
      {
         // model version policy will be enforced centrally
         predicate = Predicates.inList(piProcDefFieldRef, processRtOids.iterator());
      }
      return predicate;
   }

   public Object visit(ProcessStateFilter filter, Object rawContext)
   {
      PredicateTerm resultTerm = null;

      VisitationContext context = (VisitationContext) rawContext;

      if ( !(ProcessInstanceBean.class.isAssignableFrom(context.getType())))
      {
         if (!context.getPredicateJoins().containsKey(ProcessInstanceBean.class))
         {
            final boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());
            FieldRef frProcessInstance = ActivityInstanceBean.FR__PROCESS_INSTANCE;
            if (isAiQueryOnWorkItem)
            {
               frProcessInstance = WorkItemBean.FR__PROCESS_INSTANCE;
            }
            context.getPredicateJoins().put(ProcessInstanceBean.class,
                  new Join(ProcessInstanceBean.class)
                  .on(frProcessInstance, ProcessInstanceBean.FIELD__OID));
         }
      }

      if ((null != filter.getStates()) && (0 < filter.getStates().length))
      {
         if (0 == filter.getStates().length)
         {
            if (!filter.isInclusive())
            {
               resultTerm = Predicates.isNotNull(ProcessInstanceBean.FR__STATE);
            }
            else
            {
               resultTerm = Predicates.isNull(ProcessInstanceBean.FR__STATE);
            }
         }
         else
         {
            List piStateList = new ArrayList(filter.getStates().length);
            for (int i = 0; i < filter.getStates().length; i++)
            {
               ProcessInstanceState stateKey = filter.getStates()[i];
               piStateList.add(new Long(stateKey.getValue()));
            }

            if (filter.isInclusive())
            {
               resultTerm = Predicates.inList(ProcessInstanceBean.FR__STATE, piStateList);
            }
            else
            {
               resultTerm = Predicates.notInList(ProcessInstanceBean.FR__STATE, piStateList);
            }
         }
      }
      else
      {
         trace.warn("Ignoring filter for undefined process states.");
      }

      return resultTerm;
   }

   public Object visit(ProcessInstanceFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      FieldRef piOidFieldRef;
      if (ProcessInstanceBean.class.isAssignableFrom(context.getType()))
      {
         piOidFieldRef = ProcessInstanceBean.FR__OID;
      }
      else if (ActivityInstanceBean.class.isAssignableFrom(context.getType()))
      {
         piOidFieldRef = ActivityInstanceBean.FR__PROCESS_INSTANCE;
      }
      else if (WorkItemBean.class.isAssignableFrom(context.getType()))
      {
         piOidFieldRef = WorkItemBean.FR__PROCESS_INSTANCE;
      }
      else
      {
         piOidFieldRef = null;
      }

      PredicateTerm resultTerm;
      if (null != piOidFieldRef)
      {
         if (filter.getOids().isEmpty())
         {
            resultTerm = Predicates.isNull(piOidFieldRef);
         }
         else
         {
            resultTerm = Predicates.inList(piOidFieldRef, filter.getOids().iterator());
         }
      }
      else
      {
         trace.warn("Ignoring process instance filter while querying for instances of "
               + context.getType() + ".");
         resultTerm = NOTHING;
      }

      return resultTerm;
   }

   public Object visit(StartingUserFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      PredicateTerm resultTerm;
      if (ProcessInstanceBean.class.isAssignableFrom(context.getType()))
      {
         long userOID;
         if (StartingUserFilter.CURRENT_USER.equals(filter)
               && (null != context.getEvaluationContext().getUser()))
         {
            userOID = context.getEvaluationContext().getUser().getOID();
         }
         else
         {
            userOID = filter.getUserOID();
         }

         resultTerm = Predicates.isEqual(ProcessInstanceBean.FR__STARTING_USER, userOID);
      }
      else
      {
         trace.warn("Ignoring starting user filter while querying for instances of "
               + context.getType() + ".");
         resultTerm = NOTHING;
      }

      return resultTerm;
   }

   public Object visit(ActivityFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      PredicateTerm resultTerm = NOTHING;
      boolean isAiQuery = ActivityInstanceBean.class.isAssignableFrom(context.getType());
      boolean isAiQueryOnWorkItem = WorkItemBean.class.isAssignableFrom(context.getType());
      if (isAiQuery || isAiQueryOnWorkItem)
      {
         final Collection modelOids = filter.getModelOids();
         final boolean isModelOidsEmpty = modelOids.isEmpty();
         final ModelManager modelManager = context.getEvaluationContext().getModelManager();
         Iterator i = limitIteratorToRequiredModels(modelOids, modelManager);
         Set activityRtOids = new HashSet();

         String pdModelId = null;
         String processID = filter.getProcessID();
         if (processID != null && processID.startsWith("{"))
         {
            QName qname = QName.valueOf(processID);
            pdModelId = qname.getNamespaceURI();
            processID = qname.getLocalPart();
         }

         String aModelId = null;
         String activityID = filter.getActivityID();
         if (activityID != null && activityID.startsWith("{"))
         {
            QName qname = QName.valueOf(activityID);
            aModelId = qname.getNamespaceURI();
            activityID = qname.getLocalPart();
         }

         // TODO leverage model version policy to restrict number of models traversed
         for (; i.hasNext();)
         {
            IModel model = (IModel) i.next();
            Iterator procDefIterator;
            if (null != processID)
            {
               if (pdModelId != null && !pdModelId.equals(model.getId()))
               {
                  continue;
               }

               IProcessDefinition process = model.findProcessDefinition(processID);
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
               if (aModelId != null && !aModelId.equals(process.getModel().getId()))
               {
                  continue;
               }

               IActivity activity = process.findActivity(activityID);
               if (null != activity)
               {
                  activityRtOids.add(new Long(modelManager.getRuntimeOid(activity)));
               }
            }
         }

         FieldRef frActivity = ActivityInstanceBean.FR__ACTIVITY;
         if (isAiQueryOnWorkItem)
         {
            frActivity = WorkItemBean.FR__ACTIVITY;
         }

         if (activityRtOids.isEmpty())
         {
            resultTerm = Predicates.isNull(frActivity);
         }
         else
         {
            // model version policy will be enforced centrally
            resultTerm = Predicates.inList(frActivity, activityRtOids.iterator());
            if ( !isModelOidsEmpty)
            {
               FieldRef frModel = ActivityInstanceBean.FR__MODEL;
               if (isAiQueryOnWorkItem)
               {
                  frModel = WorkItemBean.FR__MODEL;
               }

               PredicateTerm modelTerm = Predicates.inList(frModel, modelOids.iterator());
               resultTerm = Predicates.andTerm(resultTerm, modelTerm);
            }
         }
      }
      else
      {
         trace.warn("Ignoring activity filter while querying for instances of "
               + context.getType() + ".");
      }

      return resultTerm;
   }

   /**
    * Limits the collection of all the available models to the ones defined in <code>modelOids</code>.
    *
    * If <code>modelOids</code> is empty it just returns the entire list of models.
    *
    * @param modelOids collection of model oids
    * @param modelManager model manager
    * @return Iterator<IModel>
    * @throws IllegalArgumentException if <code>modelOids</code> and/or <code>modelManager</code> is null
    */
   static Iterator limitIteratorToRequiredModels(final Collection/*<IModel>*/ modelOids, final ModelManager modelManager)
   {
      if (modelOids == null || modelManager == null)
      {
         throw new IllegalArgumentException("The arguments can't be null");
      }
      Iterator/*<IModel>*/ iterator = Collections.EMPTY_LIST.iterator();
      if (!modelOids.isEmpty())
      {
         List/*<IModel>*/ listOfModels = new ArrayList/*<IModel>*/();
         for (Iterator modelOidsIterator/* <IModel> */= modelOids.iterator(); modelOidsIterator
               .hasNext();)
         {
            Long modelOid = (Long) modelOidsIterator.next();
            IModel modelFound = modelManager.findModel(modelOid.longValue());
            if (modelFound != null)
            {
               listOfModels.add(modelFound);
            }
         }
         iterator = listOfModels.iterator();
      }
      else
      {
         iterator = modelManager.getAllModels();
      }
      return iterator;
   }

   public Object visit(ActivityInstanceFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      PredicateTerm resultTerm;
      boolean isAiQuery = ActivityInstanceBean.class.isAssignableFrom(context.getType());
      boolean isAiQueryOnWorkItem = WorkItemBean.class.isAssignableFrom(context.getType());
      if (isAiQuery || isAiQueryOnWorkItem)
      {
         FieldRef frAiOid = ActivityInstanceBean.FR__OID;
         if (isAiQueryOnWorkItem)
         {
            frAiOid = WorkItemBean.FR__ACTIVITY_INSTANCE;
         }
         resultTerm = Predicates.isEqual(frAiOid, filter.getOID());
      }
      else
      {
         trace.warn("Ignoring activity instance filter while querying for instances of "
               + context.getType() + ".");
         resultTerm = NOTHING;
      }

      return resultTerm;
   }

   public Object visit(ActivityStateFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      PredicateTerm resultTerm;
      boolean isAiQuery = ActivityInstanceBean.class.isAssignableFrom(context.getType());
      boolean isAiQueryOnWorkItem = WorkItemBean.class.isAssignableFrom(context.getType());
      if ((isAiQuery || isAiQueryOnWorkItem) && (null != filter.getStates())
            && (0 < filter.getStates().length))
      {
         FieldRef frState = ActivityInstanceBean.FR__STATE;
         if (isAiQueryOnWorkItem)
         {
            frState = WorkItemBean.FR__STATE;
         }

         if (0 == filter.getStates().length)
         {
            if ( !filter.isInclusive())
            {
               resultTerm = Predicates.isNotNull(frState);
            }
            else
            {
               resultTerm = Predicates.isNull(frState);
            }
         }
         else
         {
            List aiStateList = new ArrayList(filter.getStates().length);
            for (int i = 0; i < filter.getStates().length; i++)
            {
               ActivityInstanceState stateKey = filter.getStates()[i];
               aiStateList.add(new Long(stateKey.getValue()));
            }

            if (filter.isInclusive())
            {
               resultTerm = Predicates.inList(frState, aiStateList);
            }
            else
            {
               resultTerm = Predicates.notInList(frState, aiStateList);
            }
         }
      }
      else
      {
         trace.warn("Ignoring filter for undefined activity states.");
         resultTerm = NOTHING;
      }

      return resultTerm;
   }

   public Object visit(PerformingUserFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      List<Long> userOidList = CollectionUtils.newArrayList();
      if (PerformingUserFilter.CURRENT_USER.equals(filter)
            && (null != context.getEvaluationContext().getUser()))
      {
         // add current user
         userOidList.add(context.getEvaluationContext().getUser().getOID());

         // if current user is deputy for other user add them as well
         Date now = new Date();
         IUser user = context.getEvaluationContext().getUser();
         List<DeputyBean> deputies = UserUtils.getDeputies(user);
         for (DeputyBean deputy : deputies)
         {
            if (deputy.isActive(now))
            {
               userOidList.add(deputy.user);
            }
         }
      }
      else
      {
         userOidList.add(filter.getUserOID());
      }

      PredicateTerm term;
      if (WorkItemBean.class.equals(context.getType()))
      {
         term = Predicates.andTerm( //
               Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND, PerformerType.USER), //
               Predicates.inList(WorkItemBean.FR__PERFORMER, userOidList));
      }
      else
      {
         term = Predicates.inList(ActivityInstanceBean.FR__CURRENT_USER_PERFORMER,
               userOidList);
      }

      return term;
   }

   public Object visit(PerformingParticipantFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      Set<IParticipant> participants;
      if (null != filter.getParticipant()
            || (PerformingParticipantFilter.ANY_FOR_USER.equals(filter)))
      {
         participants = QueryUtils.findContributingParticipants(filter,
               context.getEvaluationContext());
      }
      else
      {
         participants = filter.getContributors();
      }

      // normalize participants according to any restriction to model version
      // ScopedParticipantInfo holds Pair(participantRtOid, departmentOid) instances
      Map<ScopedParticipantInfo, Set<Long>> versionedRtOids = new TreeMap();
      Set<ScopedParticipantInfo> unversionedRtOids = new TreeSet();
      normalizeParticipants(context, participants, versionedRtOids, unversionedRtOids);

      //final boolean isAiQuery = ActivityInstanceBean.class.equals(context.getType());
      final boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());

      PredicateTerm resultTerm;
      if (unversionedRtOids.isEmpty() && versionedRtOids.isEmpty())
      {
         if (isAiQueryOnWorkItem)
         {
            // Workitems do not exists for completed AIs, therefore testing for being on user worklist it enough.
            resultTerm = Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND,
                  PerformerType.USER);
         }
         else
         // if (isAiQuery) should always evaluate to true here.
         {
            resultTerm = Predicates.isNull(ActivityInstanceBean.FR__CURRENT_PERFORMER);
         }
      }
      else
      {
         if (versionedRtOids.isEmpty())
         {
            if (isAiQueryOnWorkItem)
            {
               resultTerm = createWiUnversionedRtOidsPredicate(context, unversionedRtOids);
            }
            else // if (isAiQuery) should always evaluate to true here.
            {
               resultTerm = createAiUnversionedRtOidsPredicate(context, unversionedRtOids);
            }
         }
         else
         {
            Map<Set<Long>, Set<ScopedParticipantInfo>> mvRtGroups = consolidateRtOidsRestrictedToSameModelVersion(versionedRtOids);

            OrTerm versionCorrectTerm = new OrTerm();

            if ( !unversionedRtOids.isEmpty())
            {
               if (isAiQueryOnWorkItem)
               {
                  versionCorrectTerm.add(createWiUnversionedRtOidsPredicate(context,
                        unversionedRtOids));
               }
               else
               // if (isAiQuery) should always evaluate to true here.
               {
                  versionCorrectTerm.add(createAiUnversionedRtOidsPredicate(context,
                        unversionedRtOids));
               }
            }

            for (Iterator<Entry<Set<Long>, Set<ScopedParticipantInfo>>> i = mvRtGroups
                  .entrySet().iterator(); i.hasNext();)
            {
               Entry<Set<Long>, Set<ScopedParticipantInfo>> entry = i.next();
               Iterator<Long> keyIterator = entry.getKey().iterator();
               Iterator<ScopedParticipantInfo> valueIterator = entry.getValue().iterator();

               if (isAiQueryOnWorkItem)
               {
                  OrTerm scopedPredicate = createWiDepartmentScopedModelParticipantPredicate(
                        context, valueIterator);
                  if ( !scopedPredicate.getParts().isEmpty())
                  {
                     // only model participants are versionized. Restrict it to those performers.
                     versionCorrectTerm.add(Predicates.andTerm(
                           Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND,PerformerType.MODEL_PARTICIPANT),
                           Predicates.inList(WorkItemBean.FR__MODEL, keyIterator),
                           scopedPredicate));
                  }
               }
               else // if (isAiQuery) should always evaluate to true here.
               {
                  OrTerm scopedPredicate = createAiDepartmentScopedModelParticipantPredicate(
                        context, valueIterator);
                  if ( !scopedPredicate.getParts().isEmpty())
                  {
                     versionCorrectTerm.add(Predicates.andTerm(
                           Predicates.inList(ActivityInstanceBean.FR__MODEL, keyIterator),
                           scopedPredicate));
                  }
               }
            }

            resultTerm = versionCorrectTerm;
         }
      }

      return resultTerm;
   }

   /**
    * Create predicate term for unversioned participants. This set can contain
    * oid for model participant (>0) and user groups (<0). These participants need to
    * be reflected by different performer kinds.
    *
    * @param unversionedRtOids
    * @return
    */
   private PredicateTerm createAiUnversionedRtOidsPredicate(VisitationContext context,
         Set<ScopedParticipantInfo> unversionedRtOids)
   {
      final TransformingIterator<ScopedParticipantInfo, Long> ugIter = new TransformingIterator<ScopedParticipantInfo, Long>(
            unversionedRtOids.iterator(), new Functor<ScopedParticipantInfo, Long>()
            {
               public Long execute(ScopedParticipantInfo participantDepartment)
               {
                  return Long.valueOf( participantDepartment.getParticipantOid());
               }
            }, new Predicate<ScopedParticipantInfo>()
            {
               public boolean accept(ScopedParticipantInfo participantDepartment)
               {
                  return participantDepartment.getParticipantOid() < 0;
               }
            });

      OrTerm orTerm = new OrTerm();
      if (ugIter.hasNext())
      {
         orTerm
               .add(Predicates.inList(ActivityInstanceBean.FR__CURRENT_PERFORMER, ugIter));
      }

      final FilteringIterator<ScopedParticipantInfo> mpIter = new FilteringIterator<ScopedParticipantInfo>(
            unversionedRtOids.iterator(), new Predicate<ScopedParticipantInfo>()
            {
               public boolean accept(ScopedParticipantInfo participantDepartment)
               {
                  return participantDepartment.getParticipantOid() > 0;
               }
            });

      if (mpIter.hasNext())
      {
         OrTerm mpOrTerm = createAiDepartmentScopedModelParticipantPredicate(context,
               mpIter);

         if ( !mpOrTerm.getParts().isEmpty())
         {
            orTerm.add(mpOrTerm);
         }
      }

      return orTerm;
   }

   /**
    * Create predicate term for unversioned participants. This set can contain
    * oid for model participant (>0) and user groups (<0). These participants need to
    * be reflected by different performer kinds.
    *
    * @param unversionedRtOids
    * @return
    */
   private PredicateTerm createWiUnversionedRtOidsPredicate(VisitationContext context,
         Set<ScopedParticipantInfo> unversionedRtOids)
   {
      final TransformingIterator<ScopedParticipantInfo, Long> ugIter = new TransformingIterator<ScopedParticipantInfo, Long>(
            unversionedRtOids.iterator(), new Functor<ScopedParticipantInfo, Long>()
            {
               public Long execute(ScopedParticipantInfo participantDepartment)
               {
                  return Long.valueOf( -participantDepartment.getParticipantOid());
               }
            }, new Predicate<ScopedParticipantInfo>()
            {
               public boolean accept(ScopedParticipantInfo participantDepartment)
               {
                  return participantDepartment.getParticipantOid() < 0;
               }
            });

      OrTerm orTerm = new OrTerm();
      if (ugIter.hasNext())
      {
         orTerm.add(Predicates.andTerm( //
               Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND,PerformerType.USER_GROUP), //
               Predicates.inList(WorkItemBean.FR__PERFORMER, ugIter)));
      }

      final FilteringIterator<ScopedParticipantInfo> mpIter = new FilteringIterator<ScopedParticipantInfo>(
            unversionedRtOids.iterator(), new Predicate<ScopedParticipantInfo>()
            {
               public boolean accept(ScopedParticipantInfo participantDepartment)
               {
                  return participantDepartment.getParticipantOid() > 0;
               }
            });

      if (mpIter.hasNext())
      {
         OrTerm mpOrTerm = createWiDepartmentScopedModelParticipantPredicate(context, mpIter);

         if ( !mpOrTerm.getParts().isEmpty())
         {
            orTerm.add(Predicates.andTerm( //
                  Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND,
                        PerformerType.MODEL_PARTICIPANT), //
                  mpOrTerm));
         }
      }

      return orTerm;
   }

   private OrTerm createAiDepartmentScopedModelParticipantPredicate(
         VisitationContext context, final Iterator<ScopedParticipantInfo> mpIter)
   {
      OrTerm mpOrTerm = createDepartmentScopedModelParticipantPredicate(context, mpIter,
            ActivityInstanceBean.FR__CURRENT_PERFORMER,
            ActivityInstanceBean.FR__CURRENT_DEPARTMENT);
      return mpOrTerm;
   }

   private static OrTerm createWiDepartmentScopedModelParticipantPredicate(
         VisitationContext context, final Iterator<ScopedParticipantInfo> mpIter)
   {
      OrTerm mpOrTerm = createDepartmentScopedModelParticipantPredicate(context, mpIter,
            WorkItemBean.FR__PERFORMER, WorkItemBean.FR__DEPARTMENT);
      return mpOrTerm;
   }

   private static OrTerm createAihDepartmentScopedModelParticipantPredicate(
         VisitationContext context, final Iterator<ScopedParticipantInfo> mpIter)
   {
      OrTerm mpOrTerm = createDepartmentScopedModelParticipantPredicate(context, mpIter,
            ActivityInstanceHistoryBean.FR__ON_BEHALF_OF,
            ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_DEPARTMENT);
      return mpOrTerm;
   }

   private static OrTerm createDepartmentScopedModelParticipantPredicate(
         VisitationContext context, final Iterator<ScopedParticipantInfo> mpIter,
         final FieldRef frPerformer, final FieldRef frDepartment)
   {
      OrTerm mpOrTerm = new OrTerm();
      while (mpIter.hasNext())
      {
         ScopedParticipantInfo participantDepartment = mpIter.next();
         // TODO: optimize by grouping department oids by participant
         mpOrTerm.add(Predicates.andTerm( //
               Predicates.isEqual(frPerformer, participantDepartment.getParticipantOid()),
               Predicates.isEqual(frDepartment, participantDepartment.getDepartmentOid())));
      }
      return mpOrTerm;
   }

   public Object visit(PerformingOnBehalfOfFilter filter, Object rawContext)
   {
      if ( !Parameters.instance().getBoolean(ChangeLogDigester.PRP_AIH_ENABLED, true))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.QUERY_FILTER_IS_NOT_AVAILABLE_WITH_DISABLED_AI_HISTORY.raise(PerformingOnBehalfOfFilter.class.getSimpleName()));
      }
      
      VisitationContext context = (VisitationContext) rawContext;

      Set<IParticipant> participants = QueryUtils.findContributingParticipants(
            filter, context.getEvaluationContext());

      // add join to activity history table
      final boolean isAiQuery = ActivityInstanceBean.class.equals(context.getType());
      final boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());
      if (isAiQuery || isAiQueryOnWorkItem)
      {
         Join aihJoin = (Join) context.getPredicateJoins().get(ActivityInstanceHistoryBean.class);
         if (aihJoin == null)
         {
            FieldRef frAiOid = ActivityInstanceBean.FR__OID;
            FieldRef frAiLastModTime = ActivityInstanceBean.FR__LAST_MODIFICATION_TIME;
            if (isAiQueryOnWorkItem)
            {
               frAiOid = WorkItemBean.FR__ACTIVITY_INSTANCE;
               frAiLastModTime = WorkItemBean.FR__LAST_MODIFICATION_TIME;
            }

            aihJoin = new Join(ActivityInstanceHistoryBean.class) //
                  .on(frAiOid, ActivityInstanceHistoryBean.FIELD__ACTIVITY_INSTANCE) //
                  .andOn(frAiLastModTime, ActivityInstanceHistoryBean.FIELD__FROM);

            // Because of backward compatibility we have to set an outer join for activity instances
            // which have no historical activity instance entries
            // TODO We should set the parameter to true if we can make sure that all historical entries are present
            aihJoin.setRequired(false);
            context.getPredicateJoins().put(ActivityInstanceHistoryBean.class, aihJoin);
         }

         // normalize participants according to any restriction to model version
         // ScopedParticipantInfo holds Pair(participantRtOid, departmentOid) instances
         Map<ScopedParticipantInfo, Set<Long>> versionedRtOids = new TreeMap();
         Set<ScopedParticipantInfo> unversionedRtOids = new TreeSet();
         normalizeParticipants(context, participants, versionedRtOids, unversionedRtOids);

         PredicateTerm resultTerm = null;
         if (unversionedRtOids.isEmpty() && versionedRtOids.isEmpty())
         {
            // TODO: What is the correct semantic of that predicate. Reducing to
            // AIs on user worklist AND completed AIs? That is how it is currently implemented.

            if (isAiQueryOnWorkItem)
            {
               // Workitems do not exists for completed AIs, therefore testing for being on user worklist it enough.
               resultTerm = Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND,
                     PerformerType.USER);
            }
            else
            // if (isAiQuery) should always evaluate to true here.
            {
               resultTerm = Predicates.isNull(ActivityInstanceBean.FR__CURRENT_PERFORMER);
            }
         }
         else
         {
            if (versionedRtOids.isEmpty())
            {
               resultTerm = createAihUnversionedRtOidsPredicate(context, unversionedRtOids);
            }
            else
            {
               Map<Set<Long>, Set<ScopedParticipantInfo>> mvRtGroups = consolidateRtOidsRestrictedToSameModelVersion(versionedRtOids);

               OrTerm versionCorrectTerm = new OrTerm();

               if ( !unversionedRtOids.isEmpty())
               {
                  versionCorrectTerm.add(createAihUnversionedRtOidsPredicate(context,
                        unversionedRtOids));
               }

               for (Iterator<Entry<Set<Long>, Set<ScopedParticipantInfo>>> i = mvRtGroups
                     .entrySet().iterator(); i.hasNext();)
               {
                  Entry<Set<Long>, Set<ScopedParticipantInfo>> entry = i.next();
                  Iterator<Long> keyIterator = entry.getKey().iterator();
                  Iterator<ScopedParticipantInfo> valueIterator = entry.getValue().iterator();

                  FieldRef frModel = ActivityInstanceBean.FR__MODEL;
                  if (isAiQueryOnWorkItem)
                  {
                     frModel = WorkItemBean.FR__MODEL;
                  }

                  OrTerm scopedPredicate = createAihDepartmentScopedModelParticipantPredicate(
                        context, valueIterator);
                  if ( !scopedPredicate.getParts().isEmpty())
                  {
                     // only model participants are versionized. Restrict it to those performers.
                     versionCorrectTerm.add(Predicates.andTerm(
                           Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND,PerformerType.MODEL_PARTICIPANT),
                           Predicates.inList(frModel, keyIterator),
                           scopedPredicate));
                  }
               }

               resultTerm = versionCorrectTerm;
            }
         }
         return resultTerm;
      }

      return NOTHING;
   }

   /**
    * Create predicate term for unversioned participants. This set can contain
    * oid for model participant (>0) and user groups (<0). These participants need to
    * be reflected by different performer kinds.
    *
    * @param unversionedRtOids
    * @return
    */
   private PredicateTerm createAihUnversionedRtOidsPredicate(VisitationContext context,
         Set<ScopedParticipantInfo> unversionedRtOids)
   {
      final TransformingIterator<ScopedParticipantInfo, Long> ugIter = new TransformingIterator<ScopedParticipantInfo, Long>(
            unversionedRtOids.iterator(), new Functor<ScopedParticipantInfo, Long>()
            {
               public Long execute(ScopedParticipantInfo participantDepartment)
               {
                  return Long.valueOf( -participantDepartment.getParticipantOid());
               }
            }, new Predicate<ScopedParticipantInfo>()
            {
               public boolean accept(ScopedParticipantInfo participantDepartment)
               {
                  return participantDepartment.getParticipantOid() < 0;
               }
            });

      OrTerm orTerm = new OrTerm();
      if (ugIter.hasNext())
      {
         orTerm.add(Predicates.andTerm( //
               Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND, PerformerType.USER_GROUP), //
               Predicates.inList(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF, ugIter)));
      }

      final FilteringIterator<ScopedParticipantInfo> mpIter = new FilteringIterator<ScopedParticipantInfo>(
            unversionedRtOids.iterator(), new Predicate<ScopedParticipantInfo>()
            {
               public boolean accept(ScopedParticipantInfo participantDepartment)
               {
                  return participantDepartment.getParticipantOid() > 0;
               }
            });

      if (mpIter.hasNext())
      {
         OrTerm mpOrTerm = createAihDepartmentScopedModelParticipantPredicate(context, mpIter);

         if ( !mpOrTerm.getParts().isEmpty())
         {
            orTerm.add(Predicates.andTerm( //
                  Predicates.isEqual(ActivityInstanceHistoryBean.FR__ON_BEHALF_OF_KIND,
                        PerformerType.MODEL_PARTICIPANT), //
                  mpOrTerm));
         }
      }

      return orTerm;
   }

   private static Map<Set<Long>, Set<ScopedParticipantInfo>> consolidateRtOidsRestrictedToSameModelVersion(
         Map<ScopedParticipantInfo, Set<Long>> versionedRtOids)
   {
      Map<Set<Long>, Set<ScopedParticipantInfo>> mvRtGroups = new HashMap(versionedRtOids
            .size());
      for (Iterator<Entry<ScopedParticipantInfo, Set<Long>>> i = versionedRtOids
            .entrySet().iterator(); i.hasNext();)
      {
         Entry<ScopedParticipantInfo, Set<Long>> entry = i.next();
         Set<ScopedParticipantInfo> rtOids = mvRtGroups.get(entry.getValue());
         if (null == rtOids)
         {
            rtOids = new TreeSet();
            mvRtGroups.put(entry.getValue(), rtOids);
         }
         rtOids.add(entry.getKey());
      }
      return mvRtGroups;
   }

   public Object visit(PerformedByUserFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      final long userOID;
      if (PerformedByUserFilter.CURRENT_USER.equals(filter)
            && (null != context.getEvaluationContext().getUser()))
      {
         userOID = context.getEvaluationContext().getUser().getOID();
      }
      else
      {
         userOID = filter.getUserOID();
      }

      return Predicates.isEqual(ActivityInstanceBean.FR__PERFORMED_BY, userOID);
   }

   public Object visit(AbstractDataFilter filter, Object rawContext)
   {
      Assert.lineNeverReached("DataFilter should be removed during preprocessing");

      return NOTHING;
   }

   public Object visit(DocumentFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;
      FilterOrTerm dataFiltersOrTerm = null;
      List<AbstractDataFilter> dataFilters = new LinkedList<AbstractDataFilter>();

      if (ProcessInstanceBean.class.isAssignableFrom(context.getType()))
      {
         dataFiltersOrTerm = new FilterOrTerm(ProcessInstanceQuery.FILTER_VERIFYER);

         List<IModel> allModels = null;
         if (StringUtils.isEmpty(filter.getModelId()))
         {
            allModels = CollectionUtils.newListFromIterator(context.getEvaluationContext()
                  .getModelManager()
                  .getAllAliveModels());

         }
         else
         {
            allModels = CollectionUtils.newListFromIterator(context.getEvaluationContext()
                  .getModelManager()
                  .getAllModelsForId(filter.getModelId()));
         }

         for (IModel iModel : allModels)
         {
            ModelElementList<IData> data = iModel.getData();
            for (IData iData : data)
            {
               String dataTypeId = iData.getType().getId();
               if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
               {
                  String xPath = AuditTrailUtils.RES_ID;
                  DataFilter dataFilter = DataFilter.isEqual(getQualifiedId(iData), xPath , filter.getDocumentId());
                  dataFiltersOrTerm.add(dataFilter);
                  dataFilters.add(dataFilter);
               }
               else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
               {
                  String xPath = AuditTrailUtils.DOCS_DOCUMENTS + "/" + AuditTrailUtils.RES_ID;
                  DataFilter dataFilter = DataFilter.isEqual(getQualifiedId(iData), xPath , filter.getDocumentId());
                  dataFiltersOrTerm.add(dataFilter);
                  dataFilters.add(dataFilter);
               }
            };
         }
      }

      // Add new data filters to data filter extension context.
      DataFilterExtensionContext dataFilterExtensionContext = context.getDataFilterExtensionContext();
      Map<String, List<AbstractDataFilter>> dataFiltersByDataId = dataFilterExtensionContext.getDataFiltersByDataId();
      for (AbstractDataFilter df : dataFilters)
      {
         String dataId = df.getDataID();
         if (dataFiltersByDataId.containsKey(dataId) == false)
         {
            dataFiltersByDataId.put(dataId, new LinkedList<AbstractDataFilter>());
         }
         List<AbstractDataFilter> l = (List<AbstractDataFilter>) dataFiltersByDataId.get(dataId);
         l.add(df);
      }

      // translate using FilterOrTerm of DataFilters
      if (dataFiltersOrTerm != null)
      {
         return visit(dataFiltersOrTerm, context);
      }
      else
      {
         return NOTHING;
      }
   }

   private String getQualifiedId(IData iData)
   {
      IModel model = (IModel) iData.getModel();
      return new QName(model.getId(), iData.getId()).toString();
   }

   public Object visit(ParticipantAssociationFilter filter, Object context)
   {
      // todo/france consider merging this with PerformingParticipantFilter

      Assert.lineNeverReached("ParticipantGrantFilter is not valid for this kind of "
            + "query.");

      return NOTHING;
   }

   public org.eclipse.stardust.engine.core.persistence.OrderCriteria evaluateOrder(VisitationContext context)
   {
      return (org.eclipse.stardust.engine.core.persistence.OrderCriteria) context.getQuery().evaluateOrder(this, context);
   }

   public Object visit(OrderCriteria order, Object context)
   {
      org.eclipse.stardust.engine.core.persistence.OrderCriteria result = new org.eclipse.stardust.engine.core.persistence.OrderCriteria();

      if (0 < order.getCriteria().size())
      {
         for (Iterator itr = order.getCriteria().iterator(); itr.hasNext();)
         {
            OrderCriterion part = (OrderCriterion) itr.next();

            org.eclipse.stardust.engine.core.persistence.OrderCriteria innerResult = (org.eclipse.stardust.engine.core.persistence.OrderCriteria) part
                  .accept(this, context);
            if (null != innerResult)
            {
               result.add(innerResult);
            }
         }
      }

      return result;
   }

   public Object visit(AttributeOrder criterion, Object rawContext)
   {
      final VisitationContext context = (VisitationContext) rawContext;
      Join useJoin = null;

      final boolean isAiQuery = ActivityInstanceBean.class.equals(context.getType());
      final boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());      
      
      if(context.getQuery().getClass().equals(ProcessInstanceQuery.class))
      {
         CasePolicy casePolicy = (CasePolicy) context.getQuery().getPolicy(CasePolicy.class);
         if (casePolicy != null)
         {
            Join caseJoin = (Join) context.getPredicateJoins().get(CasePolicy.class);
            if(caseJoin != null)
            {
               if(caseJoin.getTableName().equals(ProcessInstanceBean.TABLE_NAME))
               {
                  useJoin = caseJoin;
               }
            }
         }
      }

      FieldRef fieldRef;

      FilterableAttribute filterableAttribute = criterion.getFilterableAttribute();
      if (filterableAttribute instanceof IAttributeJoinDescriptor)
      {
         IAttributeJoinDescriptor joinDescr = (IAttributeJoinDescriptor) filterableAttribute;

         fieldRef = processAttributeJoinDescriptor(joinDescr, context);
      }
      else
      {
         if(useJoin != null)
         {
            fieldRef = useJoin.fieldRef(
                  criterion.getAttributeName());
         }
         else
         {
            fieldRef = TypeDescriptor.get(context.type).fieldRef(
                  criterion.getAttributeName());
            
            if (isAiQuery || isAiQueryOnWorkItem)
            {
               if (isAiQueryOnWorkItem)
               {
                  if (WorkitemKeyMap.isMapped(criterion.getAttributeName()))
                  {
                     fieldRef = WorkitemKeyMap.getFieldRef(criterion.getAttributeName());
                     
                  }
               }
            }
         }
      }

      return new org.eclipse.stardust.engine.core.persistence.OrderCriteria(fieldRef, criterion.isAscending());
   }

   public Object visit(DataOrder order, Object rawContext)
   {
      final VisitationContext context = (VisitationContext) rawContext;

      Map <Long,IData> dataMap = this.findAllDataRtOids(order.getDataID(), context.getEvaluationContext().getModelManager());

      org.eclipse.stardust.engine.core.persistence.OrderCriteria orderCriteria = new org.eclipse.stardust.engine.core.persistence.OrderCriteria();

      if (!dataMap.isEmpty())
      {
         // TODO investigate skipping join if data already joined for predicates

         // if needed and available, reuse any process instance join introduced during
         // previous filter evaluation
         Join piJoin = (Join) context.getPredicateJoins().get(ProcessInstanceBean.class);
         if (null == piJoin)
         {
            piJoin = (Join) context.getDataOrderJoins().get(DATA_ORDER_PI_JOIN_TAG);
         }
         // if PI did not get joined, fall back to cheaper and less deadlock prone join of
         // PI scope table
         Join pisJoin = (Join) dataJoinMapping.get(ProcessInstanceScopeBean.class);
         if (null == pisJoin)
         {
            pisJoin = (Join) context.getDataOrderJoins().get(DATA_ORDER_PIS_JOIN_TAG);
         }

         // todo use declared links or any other existing meta-information?

         final boolean isAiQuery = ActivityInstanceBean.class.equals(context.getType());
         final boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());
         if (isAiQuery || isAiQueryOnWorkItem)
         {
            if ((null == piJoin) && (null == pisJoin))
            {
               FieldRef frProcessInstance = ActivityInstanceBean.FR__PROCESS_INSTANCE;
               if (isAiQueryOnWorkItem)
               {
                  frProcessInstance = WorkItemBean.FR__PROCESS_INSTANCE;
               }
               pisJoin = new Join(ProcessInstanceScopeBean.class)
                     .on(frProcessInstance, ProcessInstanceScopeBean.FIELD__PROCESS_INSTANCE);
               context.getDataOrderJoins().put(DATA_ORDER_PIS_JOIN_TAG, pisJoin);
            }
         }
         else if (context.getType().equals(LogEntryBean.class))
         {
            if ((null == piJoin) && (null == pisJoin))
            {
               pisJoin = new Join(ProcessInstanceScopeBean.class)
                     .on(LogEntryBean.FR__PROCESS_INSTANCE, ProcessInstanceScopeBean.FIELD__PROCESS_INSTANCE);
               context.getDataOrderJoins().put(DATA_ORDER_PIS_JOIN_TAG, pisJoin);
            }
         }
         else if ( !context.getType().equals(ProcessInstanceBean.class))
         {
            Assert.lineNeverReached("Unsupported base type for data order: "
                  + context.getType());
         }

         DataFilterExtension dataFilterExtension = SpiUtils.createDataFilterExtension(dataMap);
         dataFilterExtension.extendOrderCriteria(piJoin, pisJoin, orderCriteria, order, dataMap, context.getDataOrderJoins());
      }
      else
      {
         trace.debug("Ignoring request to order by invalid data '" + order.getDataID()
               + "'");
      }

      return orderCriteria;
   }

   public Object visit(CustomOrderCriterion criterion, Object rawContext)
   {
      final VisitationContext context = (VisitationContext) rawContext;
      final Class criterionType = criterion.getType();
      final String criterionFieldName = criterion.getFieldName();
      final Class queryResultType = context.getType();
      FieldRef fieldRef = null;
      
      Join piJoin = (Join) context.getPredicateJoins().get(ProcessInstanceBean.class);
      
      boolean isAiQuery = ActivityInstanceBean.class.equals(queryResultType);
      boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(queryResultType);
      if (isAiQuery || isAiQueryOnWorkItem)
      {
         if (AuditTrailActivityBean.class.equals(criterionType))
         {
            String fieldActivity = ActivityInstanceBean.FIELD__ACTIVITY;
            String fieldModel = ActivityInstanceBean.FIELD__MODEL;
            if (isAiQueryOnWorkItem)
            {
               fieldActivity = WorkItemBean.FIELD__ACTIVITY;
               fieldModel = WorkItemBean.FIELD__MODEL;
            }

            AttributeJoinDescriptor joinDescr = new AttributeJoinDescriptor(
                  AuditTrailActivityBean.class, //
                  new Pair(fieldActivity, AuditTrailActivityBean.FIELD__OID), //
                  new Pair(fieldModel, AuditTrailActivityBean.FIELD__MODEL), //
                  criterionFieldName);
            fieldRef = processAttributeJoinDescriptor(joinDescr, context, false);
         }
         else if (AuditTrailProcessDefinitionBean.class.equals(criterionType))
         {
            String fieldProcessInstance = ActivityInstanceBean.FIELD__PROCESS_INSTANCE;
            if (isAiQueryOnWorkItem)
            {
               fieldProcessInstance = WorkItemBean.FIELD__PROCESS_INSTANCE;
            }

               AttributeJoinDescriptor joinDescr = new AttributeJoinDescriptor(
                     ProcessInstanceBean.class, //
                     fieldProcessInstance, ProcessInstanceBean.FIELD__OID,
                     ProcessInstanceBean.FIELD__OID);
               
               if (piJoin == null)
               {
                  piJoin = (Join) processAttributeJoinDescriptor(joinDescr, context, false).getType();
               }

               joinDescr = new AttributeJoinDescriptor(
                     AuditTrailProcessDefinitionBean.class, //
                     new Pair(ProcessInstanceBean.FIELD__PROCESS_DEFINITION,
                           AuditTrailProcessDefinitionBean.FIELD__OID), //
                     new Pair(ProcessInstanceBean.FIELD__MODEL,
                           AuditTrailProcessDefinitionBean.FIELD__MODEL),
                     criterionFieldName);
               fieldRef = processAttributeJoinDescriptor(joinDescr, context, piJoin,
                     false);

               Join pdJoin = (Join) fieldRef.getType();
               pdJoin.setDependency(piJoin);            
         }
         else if (UserBean.class.equals(criterionType))
         {
            String fieldCurrentUserPerformer = ActivityInstanceBean.FIELD__CURRENT_USER_PERFORMER;
            if (isAiQueryOnWorkItem)
            {
               fieldCurrentUserPerformer = WorkItemBean.FIELD__PERFORMER;
            }

            AttributeJoinDescriptor joinDescr = new AttributeJoinDescriptor(
                  UserBean.class, fieldCurrentUserPerformer,
                  UserBean.FIELD__OID, criterionFieldName);
            fieldRef = processAttributeJoinDescriptor(joinDescr, context, false);
            Join uJoin = (Join) fieldRef.getType();
            uJoin.setRequired(false);

            if (isAiQueryOnWorkItem)
            {
               uJoin.where(Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND,
                     PerformerType.USER));
            }
         }
         /*else if (Participants)
         {
            // How to join? Participants could be modelparticipants as role, orgs, cond.performer or usergroups.
            // Different joins would be necessary.

         }*/
      }
      else if (ProcessInstanceBean.class.equals(queryResultType))
      {
         if (AuditTrailProcessDefinitionBean.class.equals(criterionType))
         {
            AttributeJoinDescriptor joinDescr = new AttributeJoinDescriptor(
                  AuditTrailProcessDefinitionBean.class, //
                  new Pair(ProcessInstanceBean.FIELD__PROCESS_DEFINITION,
                        AuditTrailProcessDefinitionBean.FIELD__OID), //
                  new Pair(ProcessInstanceBean.FIELD__MODEL,
                        AuditTrailProcessDefinitionBean.FIELD__MODEL), criterionFieldName);
            fieldRef = processAttributeJoinDescriptor(joinDescr, context, false);
         }
         else if (UserBean.class.equals(criterionType))
         {
            AttributeJoinDescriptor joinDescr = new AttributeJoinDescriptor(
                  UserBean.class, ProcessInstanceBean.FIELD__STARTING_USER,
                  UserBean.FIELD__OID, criterionFieldName);
            fieldRef = processAttributeJoinDescriptor(joinDescr, context, false);
            Join uJoin = (Join) fieldRef.getType();
            uJoin.setRequired(false);
         }
      }

      org.eclipse.stardust.engine.core.persistence.OrderCriteria orderCriteria = new org.eclipse.stardust.engine.core.persistence.OrderCriteria();
      if (null != fieldRef)
      {
         orderCriteria.add(fieldRef, criterion.isAscending());
      }

      return orderCriteria;
   }

   protected Map<Long, IData> findAllDataRtOids(String dataID, ModelManager modelManager)
   {
      Map<Long, IData> dataRtOids = new HashMap();
      String namespace = null;
      if (dataID.startsWith("{"))
      {
         QName qname = QName.valueOf(dataID);
         namespace = qname.getNamespaceURI();
         dataID = qname.getLocalPart();
      }

      Iterator modelItr = null;
      if (namespace != null)
      {
         modelItr = modelManager.getAllModelsForId(namespace);
      }
      else
      {
         modelItr = modelManager.getAllModels();
      }

      while (modelItr.hasNext())
      {
         IModel model = (IModel) modelItr.next();
         IData data = model.findData(dataID);
         if (null != data)
         {
            dataRtOids.put(new Long(modelManager.getRuntimeOid(data)), data);
         }
      }
      if (dataRtOids.isEmpty())
      {
         trace.warn("Invalid data ID used for data filter predicate: " + dataID + ".");
      }
      return dataRtOids;
   }

   public Object visit(UserStateFilter filter, Object context)
   {
      return NOTHING;
   }

   public Object visit(CurrentPartitionFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;
      PredicateTerm resultTerm = NOTHING;

      boolean isAiQuery = ActivityInstanceBean.class.equals(context.getType());
      boolean isAiQueryOnWorkItem = WorkItemBean.class.equals(context.getType());
      if (isAiQuery || isAiQueryOnWorkItem )
      {
         if ( !context.getPredicateJoins().containsKey(ModelPersistorBean.class))
         {
            FieldRef frModel = ActivityInstanceBean.FR__MODEL;
            if(isAiQueryOnWorkItem)
            {
               frModel = WorkItemBean.FR__MODEL;
            }
            context.getPredicateJoins().put(
                  ModelPersistorBean.class,//
                  new Join(ModelPersistorBean.class)//
                        .andOn(frModel, ModelPersistorBean.FIELD__OID));
         }

         resultTerm = Predicates.isEqual(ModelPersistorBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (ProcessInstanceBean.class.isAssignableFrom(context.getType()) )
      {
         if ( !context.getPredicateJoins().containsKey(ModelPersistorBean.class))
         {
            context.getPredicateJoins().put(
                  ModelPersistorBean.class,//
                  new Join(ModelPersistorBean.class)//
                        .andOn(ProcessInstanceBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID));
         }

         resultTerm = Predicates.isEqual(ModelPersistorBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (UserBean.class.isAssignableFrom(context.getType()) )
      {
         if ( !context.getPredicateJoins().containsKey(UserRealmBean.class))
         {
            context.getPredicateJoins().put(
                  UserRealmBean.class,//
                  new Join(UserRealmBean.class)//
                        .andOn(UserBean.FR__REALM,
                              UserRealmBean.FIELD__OID));
         }

         resultTerm = Predicates.isEqual(UserRealmBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (UserGroupBean.class.isAssignableFrom(context.getType()) )
      {
         resultTerm = Predicates.isEqual(UserGroupBean.FR__PARTITION, filter
               .getPartitionOid());
      }
      else if (LogEntryBean.class.isAssignableFrom(context.getType()) )
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

   public Object visit(ProcessInstanceLinkFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;
      LinkDirection direction = filter.getDirection();
      String[] linkTypes = filter.getLinkType();

      int joinCount = context.getPredicateJoins().size() + 1;

      Class<?> pilJoinKey = ProcessInstanceLinkBean.class;
      Join pilJoin = (Join) context.getPredicateJoins().get(pilJoinKey);
      if (pilJoin == null)
      {
         if (LinkDirection.FROM.equals(direction))
         {
            pilJoin = new Join(ProcessInstanceLinkBean.class, "PIL_PI" + joinCount).on(
                  ProcessInstanceBean.FR__OID,
                  ProcessInstanceLinkBean.FIELD__LINKED_PROCESS_INSTANCE);
         }
         else if (LinkDirection.TO.equals(direction))
         {
            pilJoin = new Join(ProcessInstanceLinkBean.class, "PIL_PI" + joinCount).on(
                  ProcessInstanceBean.FR__OID,
                  ProcessInstanceLinkBean.FIELD__PROCESS_INSTANCE);
         }
         else if (LinkDirection.TO_FROM.equals(direction))
         {
            pilJoin = new Join(ProcessInstanceLinkBean.class, "PIL_PI" + joinCount).on(
                  ProcessInstanceBean.FR__OID,
                  ProcessInstanceLinkBean.FIELD__PROCESS_INSTANCE);
            pilJoin.orOn(ProcessInstanceBean.FR__OID,
                  ProcessInstanceLinkBean.FIELD__LINKED_PROCESS_INSTANCE);
         }

         context.getPredicateJoins().put(pilJoinKey, pilJoin);
      }

      PredicateTerm resultTerm = NOTHING;

      if (LinkDirection.FROM.equals(direction))
      {
         PredicateTerm processInstanceTerm = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__PROCESS_INSTANCE), filter.getProcessInstanceOid());
         PredicateTerm processInstanceTerm2 = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__LINKED_PROCESS_INSTANCE), ProcessInstanceBean.FR__OID);
         resultTerm = Predicates.andTerm(processInstanceTerm, processInstanceTerm2);

      }
      else if (LinkDirection.TO.equals(direction))
      {
         PredicateTerm processInstanceTerm = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__LINKED_PROCESS_INSTANCE), filter.getProcessInstanceOid());
         PredicateTerm processInstanceTerm2 = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__PROCESS_INSTANCE), ProcessInstanceBean.FR__OID);
         resultTerm = Predicates.andTerm(processInstanceTerm, processInstanceTerm2);
      }
      else if (LinkDirection.TO_FROM.equals(direction))
      {
         PredicateTerm processInstanceTerm = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__PROCESS_INSTANCE), filter.getProcessInstanceOid());
         PredicateTerm processInstanceTerm2 = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__LINKED_PROCESS_INSTANCE), ProcessInstanceBean.FR__OID);
         PredicateTerm from = Predicates.andTerm(processInstanceTerm, processInstanceTerm2);

         PredicateTerm processInstanceTerm3 = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__LINKED_PROCESS_INSTANCE), filter.getProcessInstanceOid());
         PredicateTerm processInstanceTerm4 = Predicates.isEqual(pilJoin.fieldRef(ProcessInstanceLinkBean.FIELD__PROCESS_INSTANCE), ProcessInstanceBean.FR__OID);
         PredicateTerm to = Predicates.andTerm(processInstanceTerm3, processInstanceTerm4);

         resultTerm = Predicates.orTerm(from, to);
      }

      if (linkTypes != null)
      {
         Class<?> piltJoinKey = ProcessInstanceLinkTypeBean.class;
         Join piltJoin = (Join) context.getPredicateJoins().get(piltJoinKey);
         if (piltJoin == null)
         {
            piltJoin = new Join(ProcessInstanceLinkTypeBean.class, "PILT_PIL" + joinCount).on(
                  ProcessInstanceLinkBean.FR__LINK_TYPE,
                  ProcessInstanceLinkTypeBean.FIELD__OID);

            context.getPredicateJoins().put(piltJoinKey, piltJoin);
            piltJoin.setDependency(pilJoin);
         }

         PredicateTerm linkTypeTerm = Predicates.inList(
               piltJoin.fieldRef(ProcessInstanceLinkTypeBean.FIELD__ID),
               filter.getLinkType());
         resultTerm = Predicates.andTerm(resultTerm, linkTypeTerm);
      }

      return resultTerm;
   }

   public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
   {
      PredicateTerm resultTerm = NOTHING;

      switch (filter.getMode())
      {
      case ROOT_PROCESS:
         resultTerm = Predicates.isEqual(ProcessInstanceBean.FR__OID,
               ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE);
         break;
      case SUB_PROCESS:
         resultTerm = Predicates.notEqual(ProcessInstanceBean.FR__OID,
               ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE);
         break;
      }

      return resultTerm;
   }

   protected static boolean isAndTerm(VisitationContext context)
   {
      return FilterTerm.AND.equals(context.peekLastFilterKind());
   }

   protected static boolean isIsNullFilter(AbstractDataFilter filter)
   {
      return Operator.IS_EQUAL.equals(filter.getOperator())
            && filter.getOperand() == null;
   }

   public static MultiPartPredicateTerm getTopLevelCollectorForNotAnyOf(Join dvJoin,
         boolean filterUsedInAndTerm)
   {
      MultiPartPredicateTerm result = null;

      AndTerm restriction = dvJoin.getRestriction();
      for (PredicateTerm term : restriction.getParts())
      {
         if (Operator.NOT_ANY_OF.getId().equals(term.getTag()))
         {
            if (term instanceof MultiPartPredicateTerm)
            {
               if (term instanceof OrTerm && filterUsedInAndTerm)
               {
                  // TODO: I18N
                  new PublicException(
                        "Mixed usage of NotAnyOf data filter not supported.");
               }

               result = (MultiPartPredicateTerm) term;
               break;
            }
            else
            {
               new InternalException(
                     "NotAnyOf data filter needs to be collected in MultiPartPredicateTerm.");
            }
         }
      }

      return result;
   }

   protected static class VisitationContext
   {
      private final Query query;
      private final Class type;
      private final EvaluationContext evaluationContext;
      private final DataFilterExtensionContext dataFilterExtensionContext;

      private final Map predicateJoins = new LinkedHashMap();
      private final Map dataOrderJoins = new LinkedHashMap();
      private final Map customOrderJoins = new LinkedHashMap();

      private final List<FieldRef> selectExtension = CollectionUtils.newArrayList();

      private final LinkedList<FilterTerm.Kind> filterKinds = CollectionUtils.newLinkedList();

      private boolean useDistinct = false;

      private String selectAlias = null;

      public VisitationContext(Query query, Class type,
            EvaluationContext evaluationContext, DataFilterExtensionContext dataFilterExtensionContext)
      {
         this.query = query;
         this.type = type;
         this.evaluationContext = evaluationContext;
         this.dataFilterExtensionContext = dataFilterExtensionContext;
      }

      public void setSelectAlias(String selectAlias)
      {
         this.selectAlias = selectAlias;
      }

      public String getSelectAlias()
      {
         return selectAlias;
      }

      public List<FieldRef> getSelectExtension()
      {
         return selectExtension;
      }

      public Query getQuery()
      {
         return query;
      }

      public Class getType()
      {
         return type;
      }

      public EvaluationContext getEvaluationContext()
      {
         return evaluationContext;
      }

      public Map getPredicateJoins()
      {
         return predicateJoins;
      }

      public Map getDataOrderJoins()
      {
         return dataOrderJoins;
      }

      public Map getCustomOrderJoins()
      {
         return customOrderJoins;
      }

      public boolean useDistinct()
      {
         return useDistinct;
      }

      public void useDistinct(boolean useDistinct)
      {
         this.useDistinct = useDistinct;
      }

      public DataFilterExtensionContext getDataFilterExtensionContext()
      {
         return this.dataFilterExtensionContext;
      }

      public List<FilterTerm.Kind> getFilterKinds()
      {
         return Collections.unmodifiableList(filterKinds);
      }

      public void pushFilterKind(FilterTerm.Kind filterKind)
      {
         filterKinds.addLast(filterKind);
         if (dataFilterExtensionContext != null)
         {
            dataFilterExtensionContext.setFilterUsedInAndTerm(FilterTerm.AND
                  .equals(filterKind));
         }
      }

      public void popFilterKind()
      {
         filterKinds.removeLast();
         if (dataFilterExtensionContext != null)
         {
            dataFilterExtensionContext.setFilterUsedInAndTerm(FilterTerm.AND
                  .equals(peekLastFilterKind()));
         }
      }

      public FilterTerm.Kind peekLastFilterKind()
      {
         if(filterKinds.size() > 0)
         {
            return filterKinds.getLast();
         }
         return null;
      }
   }

   protected static class PreprocessedQuery
   {
      private final FilterCriterion filter;
      private final FetchPredicate fetchPredicate;

      public PreprocessedQuery(FilterCriterion filter, FetchPredicate fetchPredicate)
      {
         this.filter = filter;
         this.fetchPredicate = fetchPredicate;
      }

      public FilterCriterion getFilter()
      {
         return filter;
      }

      public FetchPredicate getFetchPredicate()
      {
         return fetchPredicate;
      }
   }

   private static final class ScopedParticipantInfo extends Pair<Long, Long> implements Comparable<ScopedParticipantInfo>
   {
      private static final long serialVersionUID = 1L;

      public ScopedParticipantInfo(Long participantOid, Long departmentOid)
      {
         super(participantOid, departmentOid);
      }

      public Long getParticipantOid()
      {
         return getFirst();
      }

      public Long getDepartmentOid()
      {
         return getSecond();
      }

      public int compareTo(ScopedParticipantInfo o)
      {
         int result = getParticipantOid().compareTo(o.getParticipantOid());

         if (result != 0)
         {
            return result;
         }
         else
         {
            return CompareHelper.compare(getDepartmentOid(), o.getDepartmentOid());
         }
      }
   }

   /**
       * This class is meant as typed key for use in maps or set. It contains the
       * dataId of process data and an attribute name if it specifies an element of
       * a struct data.
       *
       * @author Stephan.Born
       */
   public static class DataAttributeKey
   {
      private final String dataId;
      private final String attributeName;
      
      private final boolean notAnyOfFilter;

      public DataAttributeKey(DataOrder order)
      {
         this(order.getDataID(), order.getAttributeName(), null, false);
      }

      public DataAttributeKey(AbstractDataFilter filter)
      {
         this(filter.getDataID(), filter.getAttributeName(), filter.getOperand(), Operator.NOT_ANY_OF
               .equals(filter.getOperator()));
      }

      public DataAttributeKey(AbstractDataFilter filter, boolean notAnyOfFilter)
      {
         this(filter.getDataID(), filter.getAttributeName(), null, notAnyOfFilter);
      }

      public DataAttributeKey(IData data, String attributeName)
      {
         this(ModelUtils.getQualifiedId(data), attributeName, null, false);
      }

      public DataAttributeKey(String dataId, String attributeName)
      {
         this(dataId, attributeName, null, false);
      }

      private DataAttributeKey(String dataId, String attributeName,
            Serializable operand, boolean notAnyOfFilter)
      {
         this.dataId = dataId;
         this.attributeName = (operand != null)
               && PredefinedConstants.QUALIFIED_CASE_DATA_ID.equals(dataId)
               && PredefinedConstants.CASE_DESCRIPTOR_VALUE_XPATH.equals(attributeName)
               ? attributeName + '/' + operand : attributeName;
         this.notAnyOfFilter = notAnyOfFilter;
      }

      public String getDataId()
      {
         return dataId;
      }

      public String getAttributeName()
      {
         return attributeName;
      }

      public boolean isNotAnyOfFilter()
      {
         return notAnyOfFilter;
      }

      public boolean equals(Object other)
      {
         if (this == other)
         {
            return true;
         }
         if (other instanceof DataAttributeKey)
         {
            final DataAttributeKey that = (DataAttributeKey) other;
            return (this.dataId != null ? this.dataId.equals(that.dataId)
                  : that.dataId == null)
                  && (this.attributeName != null ? this.attributeName
                        .equals(that.attributeName) : that.attributeName == null)
                  && this.notAnyOfFilter == that.notAnyOfFilter;
         }

         return false;
      }

      public int hashCode()
      {
         final int PRIME = 31;
         int result = 1;
         result = PRIME * result + ((dataId == null) ? 0 : dataId.hashCode());
         result = PRIME * result + ((attributeName == null) ? 0 : attributeName.hashCode());
         return result;
      }

      public String toString()
      {
         return "(" + dataId + "," + attributeName + ")";
      }
   }

   private class ProcessDefinitionFilterJoinKey
   {
      private Class<? extends PersistentBean> persistentBeanClass;
      private boolean includeProcesses;
      private int additionalCriteria = -1;


      public ProcessDefinitionFilterJoinKey(ProcessDefinitionFilter filter,
            Class<? extends PersistentBean> persistentBeanClass)
      {
         this.includeProcesses = filter.isIncludingSubProcesses();
         this.persistentBeanClass = persistentBeanClass;
      }

      public ProcessDefinitionFilterJoinKey(ProcessDefinitionFilter filter,
            Class< ? extends PersistentBean> persistentBeanClass, int additionalCriteria)
      {
         this(filter, persistentBeanClass);
         this.additionalCriteria = additionalCriteria;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + getOuterType().hashCode();
         result = prime * result + additionalCriteria;
         result = prime * result + (includeProcesses ? 1231 : 1237);
         result = prime * result
               + ((persistentBeanClass == null) ? 0 : persistentBeanClass.hashCode());
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
         ProcessDefinitionFilterJoinKey other = (ProcessDefinitionFilterJoinKey) obj;
         if (!getOuterType().equals(other.getOuterType()))
            return false;
         if (additionalCriteria != other.additionalCriteria)
            return false;
         if (includeProcesses != other.includeProcesses)
            return false;
         if (persistentBeanClass == null)
         {
            if (other.persistentBeanClass != null)
               return false;
         }
         else if (!persistentBeanClass.equals(other.persistentBeanClass))
            return false;
         return true;
      }

      private SqlBuilderBase getOuterType()
      {
         return SqlBuilderBase.this;
      }
   }
   
   private static class WorkitemKeyMap 
   {
      private static final Map<String, FieldRef> keyMap = CollectionUtils.newMap();
      private static final String ERROR_NO_VALID_ORDER_CRITERION = "no valid order criterion";
      
      static
      {
         keyMap.put(ActivityInstanceQuery.OID.getAttributeName(),
               WorkItemBean.FR__ACTIVITY_INSTANCE);
         
         // Empty mappings will result in IllegalOperationExceptions as they are not supported
         keyMap.put(ActivityInstanceQuery.PERFORMED_BY_OID.getAttributeName(), null);
         keyMap.put(ActivityInstanceQuery.CURRENT_PERFORMER_OID.getAttributeName(), null);
         keyMap.put(ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID.getAttributeName(), null);
      }
      
      public static FieldRef getFieldRef(String attributeName)
      {         
         if (keyMap.get(attributeName) != null)
         {
            return keyMap.get(attributeName);
         }
         else
         {
            throw new IllegalOperationException(
                  BpmRuntimeError.QUERY_FILTER_IS_XXX_FOR_QUERY.raise(attributeName, ERROR_NO_VALID_ORDER_CRITERION));
         }
      }
      
      public static boolean isMapped(String attributeName)
      {
         if (keyMap.containsKey(attributeName))
         {
            return true;
         }
         return false;
      }
      
   }
}
