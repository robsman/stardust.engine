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

import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.query.SqlBuilderBase.VisitationContext;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterHelper;
import org.eclipse.stardust.engine.core.runtime.setup.DataSlot;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;


public class ClusterAwareInlinedDataFilterSqlBuilder extends InlinedDataFilterSqlBuilder
{
   private static final Set<DataAttributeKey> NO_DATA_ATTIBUTE_KEYS = CollectionUtils.newHashSet();

   private final DataCluster[] clusterSetup;

   /**
    * The set of {@link ProcessInstanceState} the DataCluster must support
    * for fetching data values - see {@link DataCluster#getEnableStates()}
    */
   private final Set<ProcessInstanceState> requiredClusterPiStates;

   public ClusterAwareInlinedDataFilterSqlBuilder()
   {
      super();

      this.clusterSetup = RuntimeSetup.instance().getDataClusterSetup();
      this.requiredClusterPiStates = DataClusterHelper.getRequiredClusterPiStates();
   }

   public ParsedQuery buildSql(Query query, Class type,
         EvaluationContext evaluationContext)
   {
      // advise data cluster candidates
      final Map<DataCluster, Set<DataAttributeKey>> clusterCandidates = CollectionUtils.newHashMap();
      final ClusterAdvisor clusterAdvisor = new ClusterAdvisor(clusterSetup, requiredClusterPiStates);
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
      Set<DataAttributeKey> clusteredFilters = new HashSet<DataAttributeKey>();
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
               clusteredFilters.add(key);
            }
            boundClusters.add(entry.getKey());
         }
      }

      DataFilterExtensionContext dataFilterExtensionContext = new DataFilterExtensionContext(query.getFilter());
      dataFilterExtensionContext.setClusteredFilter(clusteredFilters);

      return super.buildSql(new ClusteredDataVisitationContext(query, type,
            evaluationContext, clusterBindings, dataFilterExtensionContext));
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

      final boolean isPrefetchHint = filter instanceof DataPrefetchHint;
      final Integer dataFilterMode = Integer.valueOf(filter.getFilterMode());

      final DataAttributeKey filterKey;
      IData data = findCorrespondingData(filter.getDataID(), cntxt.getEvaluationContext().getModelManager());
      if(data != null)
      {
         filterKey = new DataAttributeKey(data, filter.getAttributeName());
      }
      else
      {
         filterKey = new DataAttributeKey(filter.getDataID(), filter.getAttributeName());
      }

      Set<DataCluster> boundClusters = cntxt.getClusterBindings().get(filterKey);
      if (null != boundClusters
            // Clusters can only be used for this data scope mode (default mode).
            && AbstractDataFilter.MODE_ALL_FROM_SCOPE == dataFilterMode.intValue())
      {
         resultTerm = new AndTerm();

         JoinFactory joinFactory = new JoinFactory(cntxt);

         for (DataCluster cluster : boundClusters)
         {
            DataSlot slot = cluster.getDataSlot(filterKey.getDataId(), filterKey.getAttributeName());
            if (null == slot)
            {
               throw new InternalException("Invalid cluster binding for data ID "
                     + filterKey.getDataId() + " and cluster " + cluster.getTableName());
            }

            boolean ignorePreparedStatements = slot.isIgnorePreparedStatements();
            Pair joinKey = new Pair(dataFilterMode, cluster);
            Join clusterJoin = (Join) cntxt.clusterJoins.get(joinKey);
            if (null == clusterJoin)
            {
               // first use of this specific cluster, setup join
               final int idx = cntxt.clusterJoins.size() + 1;
               final String clusterAlias = "PR_DVCL" + idx;

               clusterJoin = new Join(cluster, clusterAlias) //
                     .on(joinFactory.getScopePiFieldRef(), cluster.getProcessInstanceColumn());

               Join scopePiGlueJoin = joinFactory.getGlueJoin();
               if (null != scopePiGlueJoin)
               {
                  clusterJoin.setDependency(scopePiGlueJoin);
               }

               cntxt.clusterJoins.put(joinKey, clusterJoin);
            }

            if (isPrefetchHint)
            {
               final List<FieldRef> selectExtension = cntxt.getSelectExtension();

               selectExtension.add(clusterJoin.fieldRef(slot.getTypeColumn(), ignorePreparedStatements));
               selectExtension.add(clusterJoin.fieldRef(slot.getSValueColumn(), ignorePreparedStatements));
               // Workaround: cluster column count needs to be dividable by 3,
               // third column can be any number as nValueColumns are never prefetched
               selectExtension.add(clusterJoin.fieldRef(slot.getTypeColumn(), ignorePreparedStatements));

               return NOTHING;
            }
            else
            {
               IEvaluationOptionProvider evalProvider = filter;
               ((AndTerm) resultTerm).add(matchDataInstancesPredicate(ignorePreparedStatements, filter
                     .getOperator(), filter.getOperand(), clusterJoin, slot
                     .getTypeColumn(), slot.getNValueColumn(), slot.getSValueColumn(),
                     evalProvider));
            }
         }
      }
      else
      {
         resultTerm = (PredicateTerm) super.visit(filter, rawContext);
      }

      return resultTerm;
   }
   
   public Object visit(DescriptorFilter filter, Object rawContext)
   {
      ClusteredDataVisitationContext context = (ClusteredDataVisitationContext) rawContext;
      if (DataValueBean.isLargeValue(filter.getOperand()))
      {
         throw new InternalException(
               "Inlined data filter evaluation is not supported for big data values.");
      }
      
      String descriptorID = filter.getDescriptorID();
      PredicateTerm resultTerm = null;
      final Integer dataFilterMode = Integer.valueOf(filter.getFilterMode());
      Map<String, String> dataAccessPath = getDescriptorDataAccessPathMap(descriptorID,
            context.getEvaluationContext().getModelManager());
      for (Map.Entry<String, String> entry : dataAccessPath.entrySet())
      {
         String dataID = entry.getKey();
         String attributeName = entry.getValue();
         final DataAttributeKey filterKey;
         IData data = findCorrespondingData(dataID, context.getEvaluationContext().getModelManager());
         if(data != null)
         {
            filterKey = new DataAttributeKey(data, attributeName);
         }
         else
         {
            filterKey = new DataAttributeKey(dataID, attributeName);
         }
         
//         Set<DataCluster> boundClusters = context.getClusterBindings().get(filterKey);
//         if (null != boundClusters
//               // Clusters can only be used for this data scope mode (default mode).
//               && AbstractDataFilter.MODE_ALL_FROM_SCOPE == dataFilterMode.intValue())
//         {
//            resultTerm = new AndTerm();
//
//            JoinFactory joinFactory = new JoinFactory(cntxt);
//
//            for (DataCluster cluster : boundClusters)
//            {
//               DescriptorSlot slot = cluster.getSlot(filterKey.getDataId(), filterKey.getAttributeName());
//               if (null == slot)
//               {
//                  throw new InternalException("Invalid cluster binding for data ID "
//                        + filterKey.getDataId() + " and cluster " + cluster.getTableName());
//               }
//
//               boolean ignorePreparedStatements = slot.isIgnorePreparedStatements();
//               Pair joinKey = new Pair(dataFilterMode, cluster);
//               Join clusterJoin = (Join) cntxt.clusterJoins.get(joinKey);
//               if (null == clusterJoin)
//               {
//                  // first use of this specific cluster, setup join
//                  final int idx = cntxt.clusterJoins.size() + 1;
//                  final String clusterAlias = "PR_DVCL" + idx;
//
//                  clusterJoin = new Join(cluster, clusterAlias) //
//                        .on(joinFactory.getScopePiFieldRef(), cluster.getProcessInstanceColumn());
//
//                  Join scopePiGlueJoin = joinFactory.getGlueJoin();
//                  if (null != scopePiGlueJoin)
//                  {
//                     clusterJoin.setDependency(scopePiGlueJoin);
//                  }
//
//                  cntxt.clusterJoins.put(joinKey, clusterJoin);
//               }
//
//               if (isPrefetchHint)
//               {
//                  final List<FieldRef> selectExtension = cntxt.getSelectExtension();
//
//                  selectExtension.add(clusterJoin.fieldRef(slot.getTypeColumn(), ignorePreparedStatements));
//                  selectExtension.add(clusterJoin.fieldRef(slot.getSValueColumn(), ignorePreparedStatements));
//                  // Workaround: cluster column count needs to be dividable by 3,
//                  // third column can be any number as nValueColumns are never prefetched
//                  selectExtension.add(clusterJoin.fieldRef(slot.getTypeColumn(), ignorePreparedStatements));
//
//                  return NOTHING;
//               }
//               else
//               {
//                  IEvaluationOptionProvider evalProvider = filter;
//                  ((AndTerm) resultTerm).add(matchDataInstancesPredicate(ignorePreparedStatements, filter
//                        .getOperator(), filter.getOperand(), clusterJoin, slot
//                        .getTypeColumn(), slot.getNValueColumn(), slot.getSValueColumn(),
//                        evalProvider));
//               }
//            }
//         }
//         else
//         {
//            resultTerm = (PredicateTerm) super.visit(filter, rawContext);
//         }

      }
      return resultTerm;
   }

   public Object visit(DataOrder order, Object rawContext)
   {
      final ClusteredDataVisitationContext cntxt = (ClusteredDataVisitationContext) rawContext;

      // Check for existing cluster joins. If no match exists then create new join.
      Map clusterJoins = new UnionMap(cntxt.clusterJoins, cntxt.clusterOrderByJoins, false);

      IData data = findCorrespondingData(order.getDataID(), cntxt.getEvaluationContext().getModelManager());

      DataAttributeKey orderKey = new DataAttributeKey(data, order.getAttributeName());
      Set<DataCluster> boundClusters = cntxt.getClusterBindings().get(orderKey);
      if(null != boundClusters)
      {
         JoinFactory joinFactory = new JoinFactory(cntxt);
         for (DataCluster cluster : boundClusters)
         {
            DataSlot slot = (DataSlot) cluster.getDataSlot(orderKey.getDataId(), orderKey.getAttributeName());
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

               clusterJoin = new Join(cluster, clusterAlias) //
                     .on(joinFactory.getScopePiFieldRef(), cluster.getProcessInstanceColumn());

               Join scopePiGlueJoin = joinFactory.getGlueJoin();
               if (null != scopePiGlueJoin)
               {
                  clusterJoin.setDependency(scopePiGlueJoin);
               }

               clusterJoin.setRequired(false);

               cntxt.clusterOrderByJoins.put(joinKey, clusterJoin);
            }
         }
      }

      boolean useNumericColumn = false;
      boolean useStringColumn = false;
      boolean useDoubleColumn = false;

      if (null != data)
      {
         final int typeClassification = LargeStringHolderBigDataHandler.classifyTypeForSorting(
               data, order.getAttributeName());
         useNumericColumn |= (BigData.NUMERIC_VALUE == typeClassification);
         useStringColumn |= (BigData.STRING_VALUE == typeClassification);
         useDoubleColumn |= (BigData.DOUBLE_VALUE == typeClassification);
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

            DataSlot slot = (DataSlot) cluster.getDataSlot(orderKey.getDataId(), orderKey.getAttributeName());

            if ((null != join)
                  && (null != slot)
                  && !(useNumericColumn && StringUtils.isEmpty(slot.getNValueColumn()))
                  && !((useStringColumn | useDoubleColumn) && StringUtils.isEmpty(slot
                        .getSValueColumn())))
            {
               if (useNumericColumn)
               {
                  result.add(join.fieldRef(slot.getNValueColumn()), order.isAscending());
               }

               if (useStringColumn)
               {
                  result.add(join.fieldRef(slot.getSValueColumn()), order.isAscending());
               }

               if (useDoubleColumn)
               {
                  String orderByCol = slot.getDValueColumn();
                  if ( !useStringColumn && StringUtils.isEmpty(orderByCol))
                  {
                     // Fall back to order by on string column if order by double is requested
                     // but slot does not define this column.
                     orderByCol = slot.getSValueColumn();
                  }
                  result.add(join.fieldRef(orderByCol), order.isAscending());
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
   private static PredicateTerm matchDataInstancesPredicate(boolean ignorePreparedStatements, Operator operator, Object value,
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
         resultTerm.add(new ComparisonTerm(clusterTable.fieldRef(typeColumn, ignorePreparedStatements),
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
                  orTerm.add(new ComparisonTerm(clusterTable.fieldRef(typeColumn, ignorePreparedStatements),
                        Operator.IS_NULL));
               }
               orTerm.add(new ComparisonTerm(clusterTable.fieldRef(typeColumn, ignorePreparedStatements),
                     (Operator.Binary) operator, new Integer(BigData.NULL)));
               resultTerm.add(orTerm);
            }
            else
            {
               throw new PublicException(
                     BpmRuntimeError.QUERY_NULL_VALUES_NOT_SUPPORTED_WITH_OPERATOR
                           .raise(operator));
            }
         }
         else
         {
            FieldRef lhsOperand = clusterTable.fieldRef(valueColumn, ignorePreparedStatements);

            if (!EvaluationOptions.isCaseSensitive(evaluationOptions))
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
               resultTerm.add(Predicates.inList(clusterTable.fieldRef(typeColumn, ignorePreparedStatements),
                        new int[] {BigData.STRING, BigData.BIG_STRING}));
               }
               else
               {
                  resultTerm.add(Predicates.isEqual(clusterTable.fieldRef(typeColumn, ignorePreparedStatements),
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

                     if (operator.equals(Operator.NOT_IN))
                     {
                        mpTerm.add(Predicates.notInList(lhsOperand, valuesIter));
                     }
                     else
                     {
                        mpTerm.add(Predicates.inList(lhsOperand, valuesIter));
                     }
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
                  throw new PublicException(
                        BpmRuntimeError.QUERY_INCONSISTENT_OPERATOR_USE.raise(operator,
                              matchValue));
               }

               Pair pair = (Pair) matchValue;
               Pair valuePair = new Pair(
                     getDataPredicateArgumentValue(pair.getFirst(), evaluationOptions),
                     getDataPredicateArgumentValue(pair.getSecond(), evaluationOptions));

               resultTerm.add(Predicates.isEqual(clusterTable.fieldRef(typeColumn, ignorePreparedStatements),
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
      private final Set<ProcessInstanceState> piFilterStates;

      public ClusterAdvisor(DataCluster[] clusters, Set<ProcessInstanceState> piFilterStates)
      {
         this.clusters = clusters;
         this.piFilterStates = piFilterStates;
      }

      public Object visit(FilterTerm filter, Object rawContext)
      {
         Context context = (Context) rawContext;

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
            if(!cluster.isEnabledFor(piFilterStates))
            {
               continue;
            }

            Set<DataAttributeKey> referencedSlots = context.clusterCandidates.get(cluster);
            for (DataAttributeKey slotCandidate : context.slotCandidates)
            {
               DataSlot slot = (DataSlot) cluster.getDataSlot(slotCandidate.getDataId(),
                     slotCandidate.getAttributeName());

               if (null != slot)
               {
                  // prefetch hints can only be strings (solve workaround line 217 if number values should be prefetched too)
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
            if(!cluster.isEnabledFor(piFilterStates))
            {
               continue;
            }

            Set<DataAttributeKey> referencedSlots = context.clusterCandidates.get(cluster);
            for (DataAttributeKey key : context.slotCandidates)
            {
               DataSlot slot = cluster.getDataSlot(key.getDataId(), key.getAttributeName());

               if (null != slot)
               {
                  boolean useNumericColumn = false;
                  boolean useStringColumn = false;

                  if (null != data)
                  {
                     // type classification in this case still decides between string and numeric
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

      public Object visit(DocumentFilter filter, Object context)
      {
         return null;
      }

      public Object visit(DescriptorFilter filter, Object context)
      {
         return null;
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
