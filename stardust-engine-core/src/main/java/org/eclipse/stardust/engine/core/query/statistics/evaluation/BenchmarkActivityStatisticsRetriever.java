/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy.BusinessObjectData;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectRelationship;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.BusinessObjectQueryPredicate;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IActivityInstanceQueryEvaluator;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkActivityStatisticsRetriever
      extends AbstractBenchmarkStatisticsRetriever
      implements IActivityInstanceQueryEvaluator
{
   private static final Predicate aiFilterPredicate = new Predicate()
   {
      @Override
      public boolean accept(Object o)
      {
         return true;
      }
   };

   @Override
   public CustomActivityInstanceQueryResult evaluateQuery(
         CustomActivityInstanceQuery query)
   {
      if (!(query instanceof BenchmarkActivityStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + BenchmarkActivityStatisticsQuery.class.getName());
      }

      final BenchmarkActivityStatisticsQuery psq = (BenchmarkActivityStatisticsQuery) query;

      BusinessObjectPolicy boPolicy = (BusinessObjectPolicy)
            psq.getPolicy(BusinessObjectPolicy.class);

      return boPolicy == null ? evaluateActivbityStatisticsQuery(psq) :
         evaluateBusinessObjectStatisticsQuery(psq, boPolicy);
   }

   private CustomActivityInstanceQueryResult evaluateActivbityStatisticsQuery(BenchmarkActivityStatisticsQuery query)
   {
      final BenchmarkActivityStatisticsResult result = new BenchmarkActivityStatisticsResult(
            query);

      ResultIterator rawResult = null;
      try
      {
         rawResult = new ActivityInstanceQueryEvaluator(query,
               QueryServiceUtils.getDefaultEvaluationContext()).executeFetch();

         while (rawResult.hasNext())
         {
            ActivityInstanceBean activity = (ActivityInstanceBean) rawResult.next();
            String qualifiedActivityId = ModelUtils
                  .getQualifiedId(activity.getActivity());
            String qualifiedProcessId = ModelUtils.getQualifiedId(activity.getActivity()
                  .getProcessDefinition());

            if (ActivityInstanceState.Completed.equals(activity.getState()))
            {
               result.addCompletedInstance(qualifiedProcessId, qualifiedActivityId);
            }
            else if (ActivityInstanceState.Aborted.equals(activity.getState()))
            {
               result.addAbortedInstance(qualifiedProcessId, qualifiedActivityId);
            }
            else
            {
               // Count benchmark results only for Alive processes.
               int benchmarkValue = activity.getBenchmarkValue();

               result.registerActivityBenchmarkCategory(qualifiedProcessId,
                     qualifiedActivityId, benchmarkValue);
            }
         }
      }
      finally
      {
         if (rawResult != null)
         {
            rawResult.close();
         }
      }
      return result;
   }

   private CustomActivityInstanceQueryResult evaluateBusinessObjectStatisticsQuery(
         BenchmarkActivityStatisticsQuery query, BusinessObjectPolicy boPolicy)
   {
      final BenchmarkBusinessObjectActivityStatisticsResult result =
            new BenchmarkBusinessObjectActivityStatisticsResult(query);

      BusinessObjectData filter = boPolicy.getFilter();

      QName boName = new QName(filter.getModelId(), filter.getBusinessObjectId());
      BusinessObjectQuery boq = filter.getPrimaryKeyValues() == null
            ? BusinessObjectQuery.findForBusinessObject(boName.toString())
            : BusinessObjectQuery.findWithPrimaryKey(boName.toString(), filter.getPrimaryKeyValues());
      SubsetPolicy subset = (SubsetPolicy) query.getPolicy(SubsetPolicy.class);
      if (subset != null)
      {
         boq.setPolicy(subset);
      }

      BusinessObjectQueryPredicate boqEvaluator = new BusinessObjectQueryPredicate(boq);
      Set<IData> allData = BusinessObjectUtils.collectData(ModelManagerFactory.getCurrent(), boqEvaluator);
      if (allData.isEmpty())
      {
         return result;
      }

      if (!BusinessObjectUtils.isUniqueBusinessObject(allData))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("query","Business Object not uniquely specified."));
      }

      final BusinessObjectData groupBy = boPolicy.getGroupBy();

      final Map<Object, String> otherBOs = BenchmarkProcessStatisticsRetriever.fetchReferencedBOs(groupBy);

      for (IData data : allData)
      {
         ActivityInstanceQuery pi = ActivityInstanceQuery.findAll();
         BusinessObjectUtils.copyFilters(query.getFilter(), pi.getFilter(), aiFilterPredicate);
         ActivityInstanceQueryEvaluator eval = new ActivityInstanceQueryEvaluator(pi, QueryServiceUtils.getDefaultEvaluationContext());
         ParsedQuery parsedQuery = eval.parseQuery();
         List<Join> predicateJoins = parsedQuery.getPredicateJoins();
         PredicateTerm parsedTerm = parsedQuery.getPredicateTerm();

         final QueryDescriptor queryDescriptor = createFetchQuery(data, boqEvaluator.getPkValue(), predicateJoins, parsedTerm, groupBy);
         fetchValues(queryDescriptor, boq, new RowProcessor() {

            protected void processRow(ResultSet resultSet)
                        throws SQLException
            {
               long aiOid = resultSet.getLong(1);
               int aiState = resultSet.getInt(2);
               int aiBenchmarkValue = resultSet.getInt(3);
               Object boPk = resultSet.getObject(4);
               Object boName = resultSet.getObject(5);
               String name = boName == null ? boPk == null ? "" : boPk.toString() : boName.toString();

               String groupByName = null;
               if (groupBy != null && queryDescriptor.getQueryExtension().getSelection().length >= 6)
               {
                  Object boFk = resultSet.getObject(6);
                  groupByName = otherBOs.get(boFk);
               }

               switch (aiState)
               {
               case ActivityInstanceState.ABORTED:
               case ActivityInstanceState.ABORTING:
                  result.getBenchmarkStatistics().incrementAbortedPerItem(groupByName, name, aiOid);
                  break;
               case ActivityInstanceState.COMPLETED:
                  result.getBenchmarkStatistics().incrementCompletedPerItem(groupByName, name, aiOid);
                  break;
               //case ActivityInstanceState.APPLICATION:
               default:
                  System.err.println("Registered benchmark value grouped by '" + groupByName + "': '" + name
                        + "[" + aiOid + "," + ActivityInstanceState.getString(aiState) + "]=" + aiBenchmarkValue);
                  result.getBenchmarkStatistics().registerBenchmarkValue(groupByName, name, aiOid, aiBenchmarkValue);
               }
            }
         });
      }

      return result;
   }

   private static QueryDescriptor createFetchQuery(IData data, Object pkValue,
         List<Join> predicateJoins, PredicateTerm parsedTerm, BusinessObjectData groupBy)
   {
      QueryDescriptor desc = QueryDescriptor
            .from(ActivityInstanceBean.class);
      Join dvJoin = desc
            .innerJoin(DataValueBean.class)
            .on(ActivityInstanceBean.FR__PROCESS_INSTANCE, DataValueBean.FIELD__PROCESS_INSTANCE);

      BusinessObjectUtils.applyRestrictions(desc, predicateJoins, defaultFilterPredicate);

      List<PredicateTerm> predicates = CollectionUtils.newList();
      predicates.add(Predicates.isEqual(ActivityInstanceBean.FR__MODEL, (long) data.getModel().getModelOID()));
      predicates.add(Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), ModelManagerFactory.getCurrent().getRuntimeOid(data)));

      FieldRef pkValueField = createStructuredDataJoins(false, data.<String>getAttribute(PredefinedConstants.PRIMARY_KEY_ATT),
            data, desc, dvJoin, predicates, "pk_", pkValue);

      FieldRef nameValueField = createStructuredDataJoins(true, data.<String>getAttribute(PredefinedConstants.BUSINESS_OBJECT_NAMEEXPRESSION),
            data, desc, dvJoin, predicates, "name_", null);
      if (nameValueField == null)
      {
         nameValueField = pkValueField;
      }

      Column[] columns = new Column[] {ActivityInstanceBean.FR__OID, ActivityInstanceBean.FR__STATE, ActivityInstanceBean.FR__BENCHMARK_VALUE,
         pkValueField, nameValueField};

      if (groupBy != null)
      {
         Map<String, BusinessObjectRelationship> relationships = BusinessObjectUtils.getBusinessObjectRelationships(data);
         for (BusinessObjectRelationship rel : relationships.values())
         {
            if (new QName(rel.otherBusinessObject.modelId, rel.otherBusinessObject.id)
                  .equals(new QName(groupBy.getModelId(), groupBy.getBusinessObjectId())))
            {
               FieldRef fkValueField = createStructuredDataJoins(false, rel.otherForeignKeyField, data, desc, dvJoin,
                     predicates, "fk_", groupBy.getPrimaryKeyValues());
               if (fkValueField != null)
               {
                  columns = new Column[] {ActivityInstanceBean.FR__OID, ActivityInstanceBean.FR__STATE, ActivityInstanceBean.FR__BENCHMARK_VALUE,
                        pkValueField, nameValueField, fkValueField};
               }
               break;
            }
         }
      }

      if (parsedTerm != null)
      {
         predicates.add(parsedTerm);
      }

      desc.select(columns);
      desc.where(new AndTerm(predicates.toArray(new PredicateTerm[predicates.size()])));
      return desc;
   }
}