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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceHierarchyBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.DataSlot;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;


public class ClusterAwareInlinedDataFilterSqlBuilder extends SqlBuilderBase
{
   private static final Set<DataAttributeKey> NO_DATA_ATTIBUTE_KEYS = CollectionUtils.newHashSet();
   /**
    * Has to be the same value as  {@link ProcessInstanceBean#FIELD__ROOT_PROCESS_INSTANCE}
    * and {@link ProcessInstanceScopeBean#FIELD__ROOT_PROCESS_INSTANCE}
    */
   public static final String FIELD_GLUE_ROOT_PROCESS_INSTANCE = ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE;

   /**
    * Has to be the same value as  {@link ProcessInstanceBean#FIELD__SCOPE_PROCESS_INSTANCE}
    * and {@link ProcessInstanceScopeBean#FIELD__SCOPE_PROCESS_INSTANCE}
    */
   public static final String FIELD_GLUE_SCOPE_PROCESS_INSTANCE = ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE;

   private final DataCluster[] clusterSetup;

   public ClusterAwareInlinedDataFilterSqlBuilder()
   {
      this.clusterSetup = RuntimeSetup.instance().getDataClusterSetup();
   }

   public ParsedQuery buildSql(Query query, Class type,
         EvaluationContext evaluationContext)
   {
      // advise data cluster candidates
      final Map<DataCluster, Set<DataAttributeKey>> clusterCandidates = CollectionUtils.newHashMap();
      final ClusterAdvisor clusterAdvisor = new ClusterAdvisor(clusterSetup);
      final ClusterAdvisor.Context clusterAdvisorContext = new ClusterAdvisor.Context(
            clusterCandidates, NO_DATA_ATTIBUTE_KEYS, evaluationContext.getModelManager());
      query.evaluateFilter(clusterAdvisor, clusterAdvisorContext);
      query.evaluateOrder(clusterAdvisor, clusterAdvisorContext);

      // erase all clusters fully contained in bigger clusters

      Iterator<Map.Entry<DataCluster, Set<DataAttributeKey>>> i;
      for (i = clusterCandidates.entrySet().iterator(); i.hasNext();)
      {
         final Entry<DataCluster, Set<DataAttributeKey>> entry = i.next();
         final DataCluster cluster = entry.getKey();
         final Set<DataAttributeKey> referencedSlots = entry.getValue();

         boolean eraseCluster = false;
         for (Entry<DataCluster, Set<DataAttributeKey>> superEntry : clusterCandidates.entrySet())
         {
            if (superEntry.getKey() != cluster)
            {
               Set<DataAttributeKey> slotDifference = new HashSet<DataAttributeKey>(referencedSlots);
               slotDifference.removeAll(superEntry.getValue());
               eraseCluster |= slotDifference.isEmpty();
            }
         }
         if (eraseCluster)
         {
            // cluster can be fully replaced by a super cluster
            i.remove();
         }
      }

      // reverse binding information for easier SQL building

      Map<DataAttributeKey, Set<DataCluster>> clusterBindings = CollectionUtils
            .newHashMap();
      for (Map.Entry<DataCluster, Set<DataAttributeKey>> entry : clusterCandidates
            .entrySet())
      {
         for (DataAttributeKey key : entry.getValue())
         {
            Set<DataCluster> boundClusters = clusterBindings.get(key);
            if (null == boundClusters)
            {
               boundClusters = CollectionUtils.newHashSet();
               clusterBindings.put(key, boundClusters);
            }
            boundClusters.add(entry.getKey());
         }
      }

      DataFilterExtensionContext dataFilterExtensionContext = new DataFilterExtensionContext(query.getFilter());

      return super.buildSql(new ClusteredDataVisitationContext(query, type,
            evaluationContext, clusterBindings, dataFilterExtensionContext));
   }

   protected ProcessHierarchyPreprocessor createQueryPreprocessor()
   {
      return new ProcessHierarchyPreprocessor();
   }

   protected List getJoins(VisitationContext rawContext)
   {
      ClusteredDataVisitationContext cntxt = (ClusteredDataVisitationContext) rawContext;

      final List predicateJoins = super.getJoins(cntxt);

      final List joins;
      if (!cntxt.clusterJoins.isEmpty())
      {
         joins = new ArrayList(predicateJoins.size()
               + cntxt.clusterJoins.size());
         joins.addAll(predicateJoins);
         joins.addAll(cntxt.clusterJoins.values());
      }
      else
      {
         joins = predicateJoins;
      }

      return joins;
   }

   public Object visit(AbstractDataFilter filter, Object rawContext)
   {
      ClusteredDataVisitationContext cntxt = (ClusteredDataVisitationContext) rawContext;

      if (DataValueBean.isLargeValue(filter.getOperand()))
      {
         throw new InternalException(
               "Inlined data filter evaluation is not supported for big data values.");
      }

      PredicateTerm resultTerm = null;

      final boolean filterUsedInAndTerm = isAndTerm(cntxt);
      final boolean isPrefetchHint = filter instanceof DataPrefetchHint;
      final boolean isIsNullFilter = isIsNullFilter(filter);
      final Integer dataFilterMode = Integer.valueOf(filter.getFilterMode());

      IData data = findCorrespondingData(filter.getDataID(), cntxt.getEvaluationContext().getModelManager());
      final DataAttributeKey filterKey = new DataAttributeKey(data, filter.getAttributeName());

      Set<DataCluster> boundClusters = cntxt.getClusterBindings().get(filterKey);
      if (null != boundClusters
            // Clusters can only be used for this data scope mode (default mode).
            && AbstractDataFilter.MODE_ALL_FROM_SCOPE == dataFilterMode.intValue())
      {
         resultTerm = new AndTerm();

         JoinFactory joinFactory = new JoinFactory(cntxt);

         for (DataCluster cluster : boundClusters)
         {
            DataSlot slot = cluster.getSlot(filterKey.getDataId(), filterKey.getAttributeName());
            if (null == slot)
            {
               throw new InternalException("Invalid cluster binding for data ID "
                     + filterKey.getDataId() + " and cluster " + cluster.getTableName());
            }

            Pair joinKey = new Pair(dataFilterMode, cluster);
            Join clusterJoin = (Join) cntxt.clusterJoins.get(joinKey);
            if (null == clusterJoin)
            {
               // first use of this specific cluster, setup join
               final int idx = cntxt.clusterJoins.size() + 1;
               final String clusterAlias = "PR_DVCL" + idx;
               if (joinFactory.isProcInstQuery)
               {
                  clusterJoin = new Join(cluster, clusterAlias) //
                        .on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE, cluster
                              .getProcessInstanceColumn());
               }
               else
               {
                  Join glue = joinFactory.getGlueJoin();
                  clusterJoin = new Join(cluster, clusterAlias) //
                        .on(glue.fieldRef(FIELD_GLUE_SCOPE_PROCESS_INSTANCE), cluster
                              .getProcessInstanceColumn());

                  clusterJoin.setDependency(glue);
               }

               cntxt.clusterJoins.put(joinKey, clusterJoin);
            }

            if (isPrefetchHint)
            {
               final List<FieldRef> selectExtension = cntxt.getSelectExtension();

               selectExtension.add(clusterJoin.fieldRef(slot.getTypeColumn()));
               selectExtension.add(clusterJoin.fieldRef(slot.getSValueColumn()));

               return NOTHING;
            }
            else
            {
               IEvaluationOptionProvider evalProvider = filter;
               ((AndTerm) resultTerm).add(matchDataInstancesPredicate(filter
                     .getOperator(), filter.getOperand(), clusterJoin, slot
                     .getTypeColumn(), slot.getNValueColumn(), slot.getSValueColumn(),
                     evalProvider));
            }
         }
      }
      else
      {
         // join data_value table at most once for every dataID involved with the query, this
         // join will eventually be reused by successive DataFilters targeting the same
         // dataID (especially needed for ORed predicate to prevent combinatorical explosion)

         // TODO (peekaboo): refactor this code block (join generation) to reusable factory class
         Pair joinKey = new Pair(dataFilterMode, new DataAttributeKey(filterKey.getDataId(), filterKey.getAttributeName()));
         Join dvJoin = (Join) dataJoinMapping.get(joinKey);
         final Map<Long, IData> dataMap = this.findAllDataRtOids(filterKey.getDataId(),
               cntxt.getEvaluationContext().getModelManager());
         DataFilterExtension dataFilterExtension = SpiUtils.createDataFilterExtension(
               dataMap);
         final DataFilterExtensionContext dataFilterExtensionContext = cntxt.getDataFilterExtensionContext();
         if (null == dvJoin)
         {
            // first use of this specific dataID, setup join
            // a dummy queryDescriptor needed here
            QueryDescriptor queryDescriptor = QueryDescriptor.from(ProcessInstanceBean.class)
                  .select(ProcessInstanceBean.FIELD__OID,
                        ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE);

            JoinFactory joinFactory = new JoinFactory(cntxt);
            dvJoin = dataFilterExtension.createDvJoin(queryDescriptor, filter,
                  dataJoinMapping.size() + 1, dataFilterExtensionContext,
                  filterUsedInAndTerm, joinFactory);

            // use INNER JOIN if both is valid
            // * filter is used in an AND term
            // * filter is NOT prefetch hint
            // otherwise use LEFT OUTER JOIN
            dvJoin.setRequired(filterUsedInAndTerm && !isPrefetchHint && !isIsNullFilter);

            if (dataFilterExtensionContext.useDistinct())
            {
               cntxt.useDistinct(dataFilterExtensionContext.useDistinct());
            }
            dataJoinMapping.put(joinKey, dvJoin);

            AndTerm andTerm = new AndTerm();
            dataFilterExtension.appendDataIdTerm(andTerm, dataMap, dvJoin, filter);
            if (andTerm.getParts().size() != 0)
            {
               dvJoin.where(andTerm);
            }
         }
      else
      {
         // if join already exists
         // * and filter is used in an AND term
         // * and filter is NOT prefetch hint
         // then force join to be an INNER JOIN
         // otherwise leave it as it is
            if (filterUsedInAndTerm && !isPrefetchHint && !isIsNullFilter)
         {
            dvJoin.setRequired(true);
         }
      }

         if (isPrefetchHint)
         {
            final List<FieldRef> selectExtension = cntxt.getSelectExtension();
            selectExtension.addAll(dataFilterExtension.getPrefetchSelectExtension(dvJoin));

            return NOTHING;
         }
         else
         {
            resultTerm = dataFilterExtension.createPredicateTerm(dvJoin, filter, dataMap,
                  cntxt.getDataFilterExtensionContext());
         }
      }

      return resultTerm;
   }

   public Object visit(DataOrder order, Object rawContext)
   {
      final ClusteredDataVisitationContext cntxt = (ClusteredDataVisitationContext) rawContext;

      // Check for existing cluster joins. If no match exists then create new join.
      // TODO (sb): Code duplication with method visit(AbstractDataFilter). Refactor.
      Map clusterJoins = new UnionMap(cntxt.clusterJoins, cntxt.clusterOrderByJoins, false);

      IData data = findCorrespondingData(order.getDataID(), cntxt.getEvaluationContext().getModelManager());

      DataAttributeKey orderKey = new DataAttributeKey(data, order.getAttributeName());
      Set<DataCluster> boundClusters = cntxt.getClusterBindings().get(orderKey);
      if(null != boundClusters)
      {
         JoinFactory joinFactory = new JoinFactory(cntxt);
         for (DataCluster cluster : boundClusters)
         {
            DataSlot slot = (DataSlot) cluster.getSlot(orderKey.getDataId(), orderKey.getAttributeName());
            if (null == slot)
            {
               throw new InternalException("Invalid cluster binding for data ID "
                     + orderKey.getDataId() + " and cluster " + cluster.getTableName());
            }


            Pair joinKey = new Pair(Integer.valueOf(AbstractDataFilter.MODE_ALL_FROM_SCOPE),
                  cluster);
            Join clusterJoin = (Join) clusterJoins.get(joinKey);
            if (null == clusterJoin)
            {
               // first use of this specific cluster, setup join
               final int idx = clusterJoins.size() + 1;
               final String clusterAlias = "PR_DVCL" + idx;
               if (joinFactory.isProcInstQuery)
               {
                  clusterJoin = new Join(cluster, clusterAlias) //
                        .on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE, cluster
                              .getProcessInstanceColumn());
               }
               else
               {
                  Join glue = joinFactory.getGlueJoin();
                  clusterJoin = new Join(cluster, clusterAlias) //
                        .on(glue.fieldRef(FIELD_GLUE_SCOPE_PROCESS_INSTANCE), cluster
                              .getProcessInstanceColumn());

                  clusterJoin.setDependency(glue);
               }

               clusterJoin.setRequired(false);

               cntxt.clusterOrderByJoins.put(joinKey, clusterJoin);
            }
         }
      }

      boolean useNumericColumn = false;
      boolean useStringColumn = false;

      if (null != data)
      {
         final int typeClassification = LargeStringHolderBigDataHandler.classifyType(
               data, order.getAttributeName());
         useNumericColumn |= (BigData.NUMERIC_VALUE == typeClassification);
         useStringColumn |= (BigData.STRING_VALUE == typeClassification);
      }

      final org.eclipse.stardust.engine.core.persistence.OrderCriteria result = new org.eclipse.stardust.engine.core.persistence.OrderCriteria();

      if ((null != clusterJoins) && !clusterJoins.isEmpty())
      {
         // reuse join of data cluster

         // TODO (sb): if multiple clusters are joined, use the one covering most
         // order-by-data criteria
         for (Iterator i = clusterJoins.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            Pair joinKey = (Pair) entry.getKey();
            DataCluster cluster = (DataCluster) joinKey.getSecond();
            Join join = (Join) entry.getValue();

            DataSlot slot = (DataSlot) cluster.getSlot(orderKey.getDataId(), orderKey.getAttributeName());

            if ((null != join) && (null != slot)
                  && !(useNumericColumn && StringUtils.isEmpty(slot.getNValueColumn()))
                  && !(useStringColumn && StringUtils.isEmpty(slot.getSValueColumn())))
            {
               if (useNumericColumn)
               {
                  result.add(join.fieldRef(slot.getNValueColumn()), order.isAscending());
               }

               if (useStringColumn)
               {
                  result.add(join.fieldRef(slot.getSValueColumn()), order.isAscending());
               }

               break;
            }
         }
      }

      // fall back to standard strategy
      if (result.isEmpty())
      {
         result.add((org.eclipse.stardust.engine.core.persistence.OrderCriteria) super.visit(order, cntxt));
      }

      return result;
   }

   private static IData findCorrespondingData(final String rawDataId,
         final ModelManager modelManager)
   {
      IData data = null;

      String namespace = null;
      String dataId = rawDataId;
      if (dataId.startsWith("{"))
      {
         QName qname = QName.valueOf(dataId);
         namespace = qname.getNamespaceURI();
         dataId = qname.getLocalPart();
      }

      Iterator<IModel> modelItr = null;
      if (StringUtils.isNotEmpty(namespace))
      {
         modelItr = modelManager.getAllModelsForId(namespace);
      }
      else
      {
         modelItr = modelManager.getAllModels();
      }

      while (modelItr.hasNext())
      {
         IModel model = modelItr.next();
         data = model.findData(dataId);
         if (null != data)
         {
            break;
         }
      }
      return data;
   }

   private static int getClassificationKey(Object value)
   {
      final LargeStringHolderBigDataHandler.Representation canonicalValue =
            LargeStringHolderBigDataHandler.canonicalizeDataValue(
                  DataValueBean.getStringValueMaxLength(), value);

      return canonicalValue.getClassificationKey();
   }

   /**
    * Builds a predicate fragment for matching data instances having the given value.
    * Depending on the data type this predicate may result in an exact match (if the
    * value can be represented inline in the <code>data_value</code> table) or just match
    * a set of candidate instances (if the value's representations has to be sliced for
    * storage).
    * @param value The generic representation of the data value to match with.
    * @param evaluationOptions TODO
    *
    * @return A SQL-compatible predicate for matching data instances possibly having the
    *         given value.
    *
    * @see #isLargeValue
    */
   private static PredicateTerm matchDataInstancesPredicate(Operator operator, Object value,
         ITableDescriptor clusterTable, String typeColumn, String nValueColumn,
         String sValueColumn, final IEvaluationOptionProvider evaluationOptions)
   {
      final LargeStringHolderBigDataHandler.Representation canonicalValue =
            LargeStringHolderBigDataHandler.canonicalizeDataValue(
                  DataValueBean.getStringValueMaxLength(), value);

      String valueColumn;
      Object matchValue = canonicalValue.getRepresentation();

      switch (canonicalValue.getClassificationKey())
      {
         case BigData.NULL_VALUE:
            valueColumn = null;
            break;

         case BigData.NUMERIC_VALUE:
            valueColumn = nValueColumn;
            break;

         case BigData.STRING_VALUE:
            valueColumn = sValueColumn;
            break;

         default:
            throw new InternalException("Unsupported BigData type classification: "
                  + canonicalValue.getClassificationKey());
      }

      final AndTerm resultTerm = new AndTerm();

      if (operator.isUnary())
      {
         resultTerm.add(new ComparisonTerm(clusterTable.fieldRef(typeColumn),
               (Operator.Unary) operator));
      }
      else
      {
         if (BigData.NULL_VALUE == canonicalValue.getClassificationKey())
         {
            if (Operator.IS_EQUAL.equals(operator) || Operator.NOT_EQUAL.equals(operator))
            {
               OrTerm orTerm = new OrTerm();
               if (Operator.IS_EQUAL.equals(operator))
               {
                  orTerm.add(new ComparisonTerm(clusterTable.fieldRef(typeColumn),
                        Operator.IS_NULL));
               }
               orTerm.add(new ComparisonTerm(clusterTable.fieldRef(typeColumn),
                     (Operator.Binary) operator, new Integer(BigData.NULL)));
               resultTerm.add(orTerm);
            }
            else
            {
               throw new PublicException("Null values are not supported with operator "
                     + operator);
            }
         }
         else
         {
         FieldRef lhsOperand = clusterTable.fieldRef(valueColumn);

         if ( !EvaluationOptions.isCaseSensitive(evaluationOptions))
         {
            // ignore case by applying LOWER(..) SQL function
            lhsOperand = Functions.strLower(lhsOperand);
         }

         if (operator.isBinary())
         {
            Assert.isNotNull(valueColumn);

            if (operator.equals(Operator.LIKE)
                  && canonicalValue.getTypeKey() == BigData.STRING)
            {
               resultTerm.add(Predicates.inList(clusterTable.fieldRef(typeColumn),
                     new int[] { BigData.STRING, BigData.BIG_STRING }));
            }
            else
            {
                  resultTerm.add(Predicates.isEqual(clusterTable.fieldRef(typeColumn),
                        canonicalValue.getTypeKey()));
            }

            if (matchValue instanceof Collection)
               {
                  List<List< ? >> subLists = CollectionUtils.split(
                        (Collection) matchValue, SQL_IN_CHUNK_SIZE);

                  MultiPartPredicateTerm mpTerm = new OrTerm();
                  for (List< ? > subList : subLists)
                  {
                     Iterator valuesIter = new TransformingIterator(subList.iterator(),
                           new Functor()
                           {
                              public Object execute(Object source)
                              {
                                 return getDataPredicateArgumentValue(source,
                                       evaluationOptions);
                              }
                           });

                     resultTerm.add(Predicates.inList(lhsOperand, valuesIter));
                  }
                  resultTerm.add(mpTerm);
               }
            else
            {
                  resultTerm.add(new ComparisonTerm(lhsOperand,
                        (Operator.Binary) operator, getDataPredicateArgumentValue(
                              matchValue, evaluationOptions)));
            }
         }
         else if (operator.isTernary())
         {
            Assert.isNotNull(valueColumn);

            if (!(matchValue instanceof Pair))
            {
               throw new PublicException("Inconsistent operator use " + operator + " --> "
                     + matchValue);
            }

            Pair pair = (Pair) matchValue;
            Pair valuePair = new Pair(
                  getDataPredicateArgumentValue(pair.getFirst(), evaluationOptions),
                  getDataPredicateArgumentValue(pair.getSecond(), evaluationOptions));

               resultTerm.add(Predicates.isEqual(clusterTable.fieldRef(typeColumn),
                     canonicalValue.getTypeKey()));
               resultTerm.add(new ComparisonTerm(lhsOperand, (Operator.Ternary) operator,
                     valuePair));
         }
      }
      }

      return resultTerm;
   }

   private static final Object getDataPredicateArgumentValue(Object value,
         IEvaluationOptionProvider options)
   {
      Object argumentValue;

      if (value instanceof String)
      {
         String string = (String) value;

         if (string.length() > DataValueBean.getStringValueMaxLength())
         {
            argumentValue = string.substring(0, DataValueBean.getStringValueMaxLength());
         }
         else
         {
            argumentValue = string;
         }
      }
      else
      {
         argumentValue = value;
      }

      if ( !EvaluationOptions.isCaseSensitive(options)
            && (argumentValue instanceof String))
      {
         argumentValue = ((String) argumentValue).toLowerCase();
      }

      return argumentValue;
   }

   private static class ClusteredDataVisitationContext extends VisitationContext
   {
      private final Map<DataAttributeKey, Set<DataCluster>> clusterBindings;

      private final Map /*<Pair<Integer, DataCluster>, Join>*/clusterJoins = new HashMap();
      private final Map /*<Pair<Integer, DataCluster>, Join>*/clusterOrderByJoins = new HashMap();

      public ClusteredDataVisitationContext(Query query, Class type,
            EvaluationContext evaluationContext,
            Map<DataAttributeKey, Set<DataCluster>> clusterBindings,
            DataFilterExtensionContext dataFilterExtensionContext)
      {
         super(query, type, evaluationContext, dataFilterExtensionContext);

         this.clusterBindings = Collections.unmodifiableMap(clusterBindings);
      }

      public Map<DataAttributeKey, Set<DataCluster>> getClusterBindings()
      {
         return clusterBindings;
      }

      public Map getDataOrderJoins()
      {
         return new UnionMap(super.getDataOrderJoins(), clusterOrderByJoins, true);
      }
   }

   private static class ClusterAdvisor implements FilterEvaluationVisitor,
         OrderEvaluationVisitor
   {
      private final DataCluster[] clusters;

      public ClusterAdvisor(DataCluster[] clusters)
      {
         this.clusters = clusters;
      }

      public Object visit(FilterTerm filter, Object rawContext)
      {
         Context context = (Context) rawContext;

         // if this is an AND term, add directly contained data filters to list of
         // cluster slot candidates

         if (FilterTerm.AND.equals(filter.getKind()))
         {
            context = new Context(context.clusterCandidates,
                  new HashSet<DataAttributeKey>(context.slotCandidates),
                  context.modelManager);
            for (Iterator<?> i = filter.getParts().iterator(); i.hasNext();)
            {
               FilterCriterion part = (FilterCriterion) i.next();
               if (part instanceof AbstractDataFilter)
               {
                  final AbstractDataFilter dataFilter = (AbstractDataFilter) part;
                  IData data = ClusterAwareInlinedDataFilterSqlBuilder.findCorrespondingData(dataFilter.getDataID(), context.modelManager);
                  DataAttributeKey slotCandidate = new DataAttributeKey(data, dataFilter.getAttributeName());
                  context.slotCandidates.add(slotCandidate);
               }
            }
         }

         for (Iterator<?> i = filter.getParts().iterator(); i.hasNext();)
         {
            ((FilterCriterion) i.next()).accept(this, context);
         }

         return null;
      }

      public Object visit(UnaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(BinaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(TernaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessDefinitionFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceFilter filter, Object context)
      {
         return null;
      }

      public Object visit(StartingUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityInstanceFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingParticipantFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingOnBehalfOfFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformedByUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(AbstractDataFilter filter, Object rawContext)
      {
         Context context = (Context) rawContext;
         final boolean isPrefetchHint = filter instanceof DataPrefetchHint;

         // try to select for each a suitable cluster

         for (int i = 0; i < clusters.length; i++ )
         {
            final DataCluster cluster = clusters[i];

            Set<DataAttributeKey> referencedSlots = context.clusterCandidates.get(cluster);
            for (DataAttributeKey slotCandidate : context.slotCandidates)
            {
               DataSlot slot = (DataSlot) cluster.getSlot(slotCandidate.getDataId(),
                     slotCandidate.getAttributeName());

               if (null != slot)
               {
                  // prefetch hints can only be strings
                  final int classificationKey = getClassificationKey(filter.getOperand());
                  boolean dataIsNumericValue = !isPrefetchHint &&
                     classificationKey == BigData.NUMERIC_VALUE;
                  boolean slotIsNumericValue = !StringUtils.isEmpty(slot.getNValueColumn());
                  boolean valuesAreEqualTyped =
                     classificationKey == BigData.NULL_VALUE ||
                     (dataIsNumericValue == true && slotIsNumericValue == true) ||
                     (dataIsNumericValue == false && slotIsNumericValue == false);

                  if (valuesAreEqualTyped)
                  {
                     if (null == referencedSlots)
                     {
                        referencedSlots = CollectionUtils.newHashSet();
                        context.clusterCandidates.put(cluster, referencedSlots);
                     }
                     referencedSlots.add(slotCandidate);
                  }
               }
            }
         }

         return null;
      }

      public Object visit(CurrentPartitionFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ParticipantAssociationFilter filter, Object context)
      {
         Assert.lineNeverReached("ParticipantGrantFilter is not valid for this kind of "
               + "query.");

         return null;
      }

      public Object visit(OrderCriteria order, Object rawContext)
      {
         Context context = (Context) rawContext;
         context = new Context(context.clusterCandidates, new HashSet<DataAttributeKey>(
               context.slotCandidates), context.modelManager);

         for (Iterator< ? > itr = order.getCriteria().iterator(); itr.hasNext();)
         {
            OrderCriterion criterion = (OrderCriterion) itr.next();
            criterion.accept(this, context);
         }

         return null;
      }

      public Object visit(AttributeOrder order, Object context)
      {
         return null;
      }

      public Object visit(DataOrder order, Object rawContext)
      {
         Context context = (Context) rawContext;

         // add data order to list of cluster slot candidates
         IData data = ClusterAwareInlinedDataFilterSqlBuilder.findCorrespondingData(order.getDataID(), context.modelManager);
         final DataAttributeKey slotCandidate = new DataAttributeKey(data, order.getAttributeName());
         context.slotCandidates.add(slotCandidate);

         // try to select for each a suitable cluster

         for (int i = 0; i < clusters.length; i++ )
         {
            final DataCluster cluster = clusters[i];

            Set<DataAttributeKey> referencedSlots = context.clusterCandidates.get(cluster);
            for (DataAttributeKey key : context.slotCandidates)
            {
               DataSlot slot = cluster.getSlot(key.getDataId(), key.getAttributeName());

               if (null != slot)
               {
                  boolean useNumericColumn = false;
                  boolean useStringColumn = false;

                  if (null != data)
                  {
                     final int typeClassification = LargeStringHolderBigDataHandler
                           .classifyType(data, order.getAttributeName());
                     useNumericColumn |= (BigData.NUMERIC_VALUE == typeClassification);
                     useStringColumn |= (BigData.STRING_VALUE == typeClassification);
                  }

                  boolean slotIsNumericValue = !StringUtils.isEmpty(slot.getNValueColumn());
                  boolean valuesAreEqualTyped =
                     (useNumericColumn == true && slotIsNumericValue == true) ||
                     (useStringColumn == true && slotIsNumericValue == false);

                  if (valuesAreEqualTyped)
                  {
                     if (null == referencedSlots)
                     {
                        referencedSlots = CollectionUtils.newHashSet();
                        context.clusterCandidates.put(cluster, referencedSlots);
                     }
                     referencedSlots.add(key);
                  }
               }
            }
         }

         return null;
      }

      public Object visit(CustomOrderCriterion order, Object context)
      {
         return null;
      }

      public Object visit(UserStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceLinkFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
      {
         return null;
      }

      private static class Context
      {
         private final Map<DataCluster, Set<DataAttributeKey>> clusterCandidates;
         private final Set<DataAttributeKey> slotCandidates;
         private final ModelManager modelManager;

         public Context(Map<DataCluster, Set<DataAttributeKey>> clusterCandidates,
               Set<DataAttributeKey> slotCandidates, ModelManager modelManager)
         {
            this.clusterCandidates = clusterCandidates;
            this.slotCandidates = slotCandidates;
            this.modelManager = modelManager;
         }
      }
   }

   // TODO (peekaboo): Refactor this and other classes with same name and semantic into common (base) class
   private class JoinFactory implements IJoinFactory
   {
      private final VisitationContext context;
      private final boolean isProcInstQuery;
      private final boolean isAiQueryUsingWorkItem;

      public JoinFactory(ClusteredDataVisitationContext context)
      {
         this.context = context;
         this.isProcInstQuery = ProcessInstanceBean.class.isAssignableFrom(context
               .getType());
         this.isAiQueryUsingWorkItem = WorkItemBean.class.isAssignableFrom(context
               .getType());
      }

      public Join createDataFilterJoins(int dataFilterMode, int idx, Class dvClass, FieldRef dvProcessInstanceField)
      {
         final Join dvJoin;

         final String pisAlias;
         final String pihAlias = "PR_PIH" + idx;
         final String dvAlias = "PR_"+ dvProcessInstanceField.getType().getTableAlias() + idx;

         if (AbstractDataFilter.MODE_ALL_FROM_SCOPE == dataFilterMode)
         {
            if (isProcInstQuery)
            {
               dvJoin = new Join(dvClass, dvAlias)//
                     .on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                           dvProcessInstanceField.fieldName);
            }
            else
            {
               Join glue = getGlueJoin();

               dvJoin = new Join(dvClass, dvAlias)//
                     .on(glue.fieldRef(FIELD_GLUE_SCOPE_PROCESS_INSTANCE),
                           dvProcessInstanceField.fieldName);

               dvJoin.setDependency(glue);
            }
         }
         else if (AbstractDataFilter.MODE_SUBPROCESSES == dataFilterMode)
         {
            // TODO (peekaboo): Improve detection wether distinct is needed.
            //incSubProcModeCounter();
            context.useDistinct(true);

            FieldRef lhsFieldRef;
            if (isProcInstQuery)
            {
               lhsFieldRef = ProcessInstanceBean.FR__OID;
            }
            else if (isAiQueryUsingWorkItem)
            {
               lhsFieldRef = WorkItemBean.FR__PROCESS_INSTANCE;
            }
            else
            {
               lhsFieldRef = ActivityInstanceBean.FR__PROCESS_INSTANCE;
            }

            Join hierJoin = new Join(ProcessInstanceHierarchyBean.class, pihAlias)//
                  .on(lhsFieldRef,
                        ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE);

            dataJoinMapping.put(new Pair(
                  Integer.valueOf(AbstractDataFilter.MODE_SUBPROCESSES), pihAlias), hierJoin);

            dvJoin = new Join(dvClass, dvAlias)//
                  .on(hierJoin.fieldRef(ProcessInstanceHierarchyBean.FIELD__PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);

            dvJoin.setDependency(hierJoin);
         }
         else if (AbstractDataFilter.MODE_ALL_FROM_HIERARCHY == dataFilterMode)
         {
            // TODO (peekaboo): Improve detection whether distinct is needed.
            //incAllFromHierModeCounter();
            context.useDistinct(true);

            Join pisJoin;
            pisAlias = "PR_PIS" + idx;
            if (isProcInstQuery)
            {
               pisJoin = new Join(ProcessInstanceScopeBean.class, pisAlias)//
                     .on(ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE,
                           ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE);
            }
            else
            {
               Join glue = getGlueJoin();

               pisJoin = new Join(ProcessInstanceScopeBean.class, pisAlias)//
                     .on(glue.fieldRef(FIELD_GLUE_ROOT_PROCESS_INSTANCE),
                           ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE);

               pisJoin.setDependency(glue);
            }

            dataJoinMapping.put(new Pair(Integer.valueOf(
                  AbstractDataFilter.MODE_ALL_FROM_HIERARCHY), pisAlias), pisJoin);

            dvJoin = new Join(dvClass, dvAlias)//
                  .on(pisJoin.fieldRef(ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);

            dvJoin.setDependency(pisJoin);
         }
         else
         {
            throw new InternalException(MessageFormat.format(
                  "Invalid DataFilter mode: {0}.", new Object[] { Integer.valueOf(dataFilterMode) }));
         }

         return dvJoin;
      }

      public Join getGlueJoin()
      {
         if (null == glueJoin)
         {
            glueJoin = (Join) context.getPredicateJoins().get(ProcessInstanceBean.class);

            if (null == glueJoin)
            {
               FieldRef frProcessInstance = ActivityInstanceBean.FR__PROCESS_INSTANCE;
               if (isAiQueryUsingWorkItem)
               {
                  frProcessInstance = WorkItemBean.FR__PROCESS_INSTANCE;
               }

               glueJoin = new Join(ProcessInstanceScopeBean.class, "PR_PIS")//
                     .on(frProcessInstance,
                           ProcessInstanceScopeBean.FIELD__PROCESS_INSTANCE);

               dataJoinMapping.put(ProcessInstanceScopeBean.class, glueJoin);
            }
         }

         return glueJoin;
      }

      public boolean isProcInstQuery()
      {
         return isProcInstQuery;
      }
   }

   /**
    * With this class access to two maps is possible. As it is only a view changes done
    * externally on both child maps are seen in this union map.
    *
    * This implementation is not just a view, but modifying calls like {@link #clear()} or
    * {@link #put()} are delegated to one of the child maps.
    *
    * @author born
    * @version $Revision$
    */
   private static class UnionMap implements Map
   {
      private Map first;
      private Map second;
      private final boolean modifyFirst;

      /**
       * @param first
       * @param second
       * @param modifyFirst   true when the first child map shall be modified on calls like {@link #clear()}.
       *                      Otherwise the second child map will be modified.
       */
      public UnionMap(Map first, Map second, boolean modifyFirst)
      {
         this.first = first;
         this.second = second;
         this.modifyFirst = modifyFirst;
      }

      public void clear()
      {
         if (modifyFirst)
         {
            first.clear();
         }
         else
         {
            second.clear();
         }
      }

      public boolean containsKey(Object key)
      {
         if ( !first.containsKey(key) && !second.containsKey(key))
         {
            return false;
         }

         return true;
      }

      public boolean containsValue(Object value)
      {
         if ( !first.containsValue(value) && !second.containsValue(value))
         {
            return false;
         }

         return true;
      }

      public Set entrySet()
      {
         Set entries = new HashSet(first.entrySet());
         entries.addAll(second.entrySet());
         return entries;
      }

      public Object get(Object key)
      {
         if(first.containsKey(key))
         {
            return first.get(key);
         }

         if(second.containsKey(key))
         {
            return second.get(key);
         }

         return null;
      }

      public boolean isEmpty()
      {
         if (first.isEmpty() && second.isEmpty())
         {
            return true;
         }

         return false;
      }

      public Set keySet()
      {
         Set keys = new HashSet(first.keySet());
         keys.addAll(second.keySet());
         return keys;
      }

      public Object put(Object key, Object value)
      {
         if(modifyFirst)
         {
            return first.put(key, value);
         }
         else
         {
            return second.put(key, value);
         }
      }

      public void putAll(Map t)
      {
         if(modifyFirst)
         {
            first.putAll(t);
         }
         else
         {
            second.putAll(t);
         }
      }

      public Object remove(Object key)
      {
         if(modifyFirst)
         {
            return first.remove(key);
         }
         else
         {
            return second.remove(key);
         }
      }

      public int size()
      {
         return first.size() + second.size();
      }

      public Collection values()
      {
         ArrayList list = new ArrayList(first.values());
         list.addAll(second.values());
         return list;
      }
   }
}
