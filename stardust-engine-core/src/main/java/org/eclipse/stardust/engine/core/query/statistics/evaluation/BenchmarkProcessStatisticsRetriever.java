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
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy.BusinessObjectData;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectRelationship;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IProcessInstanceQueryEvaluator;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsRetriever
      extends AbstractBenchmarkStatisticsRetriever
      implements IProcessInstanceQueryEvaluator
{
   public CustomProcessInstanceQueryResult evaluateQuery(CustomProcessInstanceQuery query)
   {
      if (!(query instanceof BenchmarkProcessStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + BenchmarkProcessStatisticsQuery.class.getName());
      }

      final BenchmarkProcessStatisticsQuery psq = (BenchmarkProcessStatisticsQuery) query;

      BusinessObjectPolicy boPolicy = (BusinessObjectPolicy)
            psq.getPolicy(BusinessObjectPolicy.class);

      return boPolicy == null ? evaluateProcessStatisticsQuery(psq) :
         evaluateBusinessObjectStatisticsQuery(psq, boPolicy);
   }

   private CustomProcessInstanceQueryResult evaluateProcessStatisticsQuery(
         BenchmarkProcessStatisticsQuery query)
   {
      final BenchmarkProcessStatisticsResult result = new BenchmarkProcessStatisticsResult(
            query);
      ResultIterator rawResult = null;
      try
      {
         rawResult = new ProcessInstanceQueryEvaluator(query,
               QueryServiceUtils.getDefaultEvaluationContext()).executeFetch();

         while (rawResult.hasNext())
         {
            ProcessInstanceBean process = (ProcessInstanceBean) rawResult.next();
            String qualifiedProcessId = ModelUtils.getQualifiedId(process
                  .getProcessDefinition());

            if (ProcessInstanceState.Completed.equals(process.getState()))
            {
               result.addCompletedInstance(qualifiedProcessId);
            }
            else if (ProcessInstanceState.Aborted.equals(process.getState()))
            {
               result.addAbortedInstance(qualifiedProcessId);
            }
            else
            {
               // Count benchmark results only for Alive processes.
               // ProcessInstanceState.Active, ProcessInstanceState.Interrupted,
               // ProcessInstanceState.Aborting
               int benchmarkValue = process.getBenchmarkValue();

               result.registerProcessBenchmarkCategory(qualifiedProcessId, benchmarkValue);
            }
         }

         if (rawResult.hasTotalCount() && QueryUtils.getSubset(query).isEvaluatingTotalCount())
         {
            result.setTotalCount(rawResult.getTotalCount());
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

   private CustomProcessInstanceQueryResult evaluateBusinessObjectStatisticsQuery(
         BenchmarkProcessStatisticsQuery query, BusinessObjectPolicy boPolicy)
   {
      final BenchmarkBusinessObjectProcessStatisticsResult result =
            new BenchmarkBusinessObjectProcessStatisticsResult(query);

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

      final Map<Object, String> otherBOs = fetchReferencedBOs(groupBy);

      long total = 0;
      for (IData data : allData)
      {
         ProcessInstanceQuery pi = ProcessInstanceQuery.findAll();
         BusinessObjectUtils.copyFilters(query.getFilter(), pi.getFilter(), defaultFilterPredicate);
         ProcessInstanceQueryEvaluator eval = new ProcessInstanceQueryEvaluator(pi, QueryServiceUtils.getDefaultEvaluationContext());
         ParsedQuery parsedQuery = eval.parseQuery();
         List<Join> predicateJoins = parsedQuery.getPredicateJoins();
         PredicateTerm parsedTerm = parsedQuery.getPredicateTerm();

         final QueryDescriptor queryDescriptor = createProcessInstanceFetchQuery(data, boqEvaluator.getPkValue(), false, predicateJoins, parsedTerm, groupBy);
         total += fetchValues(queryDescriptor, boq, new RowProcessor() {

            protected void processRow(ResultSet resultSet)
                        throws SQLException
            {
               long piOid = resultSet.getLong(1);
               int piState = resultSet.getInt(2);
               int piBenchmarkValue = resultSet.getInt(3);
               Object boPk = resultSet.getObject(4);
               Object boName = resultSet.getObject(5);
               String name = boName == null ? boPk == null ? "" : boPk.toString() : boName.toString();

               String groupByName = null;
               if (groupBy != null && queryDescriptor.getQueryExtension().getSelection().length >= 6)
               {
                  Object boFk = resultSet.getObject(6);
                  groupByName = otherBOs.get(boFk);
               }

               switch (piState)
               {
                  case ProcessInstanceState.ABORTED:
                     result.getBenchmarkStatistics().incrementAbortedPerItem(groupByName, name, piOid);
                     break;
                  case ProcessInstanceState.COMPLETED:
                     result.getBenchmarkStatistics().incrementCompletedPerItem(groupByName, name, piOid);
                     break;
                  default:
                     // ProcessInstanceState.ACTIVE, ProcessInstanceState.INTERRUPTED,
                     // ProcessInstanceState.ABORTING
                     result.getBenchmarkStatistics().registerBenchmarkValue(groupByName, name, piOid, piBenchmarkValue);
               }
            }
         });
      }

      if (QueryUtils.getSubset(query).isEvaluatingTotalCount())
      {
         result.setTotalCount(total);
      }
      return result;
   }

   static Map<Object, String> fetchReferencedBOs(BusinessObjectData boData)
   {
      if (boData == null)
      {
         return Collections.emptyMap();
      }

      QName boName = new QName(boData.getModelId(), boData.getBusinessObjectId());
      BusinessObjectQuery boq = boData.getPrimaryKeyValues() == null
            ? BusinessObjectQuery.findForBusinessObject(boName.toString())
            : BusinessObjectQuery.findWithPrimaryKey(boName.toString(), boData.getPrimaryKeyValues());

      BusinessObjectQueryPredicate boqEvaluator = new BusinessObjectQueryPredicate(boq);
      Set<IData> allData = BusinessObjectUtils.collectData(ModelManagerFactory.getCurrent(), boqEvaluator);
      if (allData.isEmpty())
      {
         return Collections.emptyMap();
      }

      if (!BusinessObjectUtils.isUniqueBusinessObject(allData))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("query","Business Object not uniquely specified."));
      }

      final Map<Object, String> result = new HashMap<Object, String>();
      for (IData data : allData)
      {
         QueryDescriptor queryDescriptor = createProcessInstanceFetchQuery(data, boqEvaluator.getPkValue(), true, null, null, null);
         fetchValues(queryDescriptor, boq, new RowProcessor() {

            @Override
            void processRow(ResultSet resultSet) throws SQLException
            {
               Object boPk = resultSet.getObject(1);
               Object boName = resultSet.getObject(2);

               if (!result.containsKey(boPk))
               {
                  result.put(boPk, boName == null ? boPk == null ? "" : boPk.toString() : boName.toString());
               }
            }

         });
      }
      return result;
   }

   private static QueryDescriptor createProcessInstanceFetchQuery(IData data, Object pkValue, boolean boDataOnly,
         List<Join> predicateJoins, PredicateTerm parsedTerm, BusinessObjectData groupBy)
   {
      QueryDescriptor desc = QueryDescriptor
            .from(ProcessInstanceBean.class);
      Join dvJoin = desc
            .innerJoin(DataValueBean.class)
            .on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);

      BusinessObjectUtils.applyRestrictions(desc, predicateJoins, defaultFilterPredicate);

      List<PredicateTerm> predicates = CollectionUtils.newList();
      predicates.add(boDataOnly
            ? Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1)
            : Predicates.notEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1));
      if (boDataOnly)
      {
         predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__MODEL, (long) data.getModel().getModelOID()));
      }
      else
      {
         List<Long> modelOids = CollectionUtils.newList();
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         for (Iterator<IModel> models = modelManager.getAllModels(); models.hasNext();)
         {
            IModel model = models.next();
            if (model.findData(data.getId()) == data)
            {
               modelOids.add((long) model.getModelOID());
            }
         }
         predicates.add(Predicates.inList(ProcessInstanceBean.FR__MODEL, modelOids));
      }
      predicates.add(Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), ModelManagerFactory.getCurrent().getRuntimeOid(data)));

      FieldRef pkValueField = createStructuredDataJoins(false, data.<String>getAttribute(PredefinedConstants.PRIMARY_KEY_ATT),
            data, desc, dvJoin, predicates, "pk_", pkValue);

      FieldRef nameValueField = createStructuredDataJoins(true, data.<String>getAttribute(PredefinedConstants.BUSINESS_OBJECT_NAMEEXPRESSION),
            data, desc, dvJoin, predicates, "name_", null);
      if (nameValueField == null)
      {
         nameValueField = pkValueField;
      }

      Column[] columns = new Column[] {pkValueField, nameValueField};

      if (!boDataOnly)
      {
         columns = new Column[] {ProcessInstanceBean.FR__OID, ProcessInstanceBean.FR__STATE, ProcessInstanceBean.FR__BENCHMARK_VALUE,
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
                     columns = new Column[] {ProcessInstanceBean.FR__OID, ProcessInstanceBean.FR__STATE, ProcessInstanceBean.FR__BENCHMARK_VALUE,
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
      }

      desc.select(columns);
      desc.where(new AndTerm(predicates.toArray(new PredicateTerm[predicates.size()])));
      return desc;
   }
}