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
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.BusinessObjectPolicy.BusinessObjectData;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectRelationship;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IProcessInstanceQueryEvaluator;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsRetriever
      implements IProcessInstanceQueryEvaluator
{
   private static final Predicate piFilterPredicate = new Predicate()
   {
      @Override
      public boolean accept(Object o)
      {
         return true;
      }
   };

   private static final Predicate joinFilterPredicate = new Predicate()
   {
      @Override
      public boolean accept(Object o)
      {
         return true;
      }
   };

   static abstract class RowProcessor
   {
      abstract void processRow(ResultSet resultSet) throws SQLException;
   }

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
               int benchmarkValue = process.getBenchmarkValue();

               result.registerProcessBenchmarkCategory(qualifiedProcessId, benchmarkValue);
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

   private CustomProcessInstanceQueryResult evaluateBusinessObjectStatisticsQuery(
         BenchmarkProcessStatisticsQuery query, BusinessObjectPolicy boPolicy)
   {
      final BenchmarkBusinessObjectStatisticsResult result =
            new BenchmarkBusinessObjectStatisticsResult(query);

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

      final Map<Object, String> otherBOs = groupBy == null ? Collections.<Object, String>emptyMap() : fetchReferencedBOs(groupBy);

      for (IData data : allData)
      {
         ProcessInstanceQuery pi = ProcessInstanceQuery.findAll();
         BusinessObjectUtils.copyFilters(query.getFilter(), pi.getFilter(), piFilterPredicate);
         ProcessInstanceQueryEvaluator eval = new ProcessInstanceQueryEvaluator(pi, QueryServiceUtils.getDefaultEvaluationContext());
         ParsedQuery parsedQuery = eval.parseQuery();
         List<Join> predicateJoins = parsedQuery.getPredicateJoins();
         PredicateTerm parsedTerm = parsedQuery.getPredicateTerm();

         final QueryDescriptor queryDescriptor = createFetchQuery(data, boqEvaluator.getPkValue(), false, predicateJoins, parsedTerm, groupBy);
         fetchValues(queryDescriptor, boq, new RowProcessor() {

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
               case ProcessInstanceState.ABORTING:
                  result.addAbortedInstance(groupByName, name, piOid);
                  break;
               case ProcessInstanceState.COMPLETED:
                  result.addCompletedInstance(groupByName, name, piOid);
                  break;
               case ProcessInstanceState.ACTIVE:
                  result.registerBusinessObjectBenchmarkCategory(groupByName, name, piOid, piBenchmarkValue);
               }
            }
         });
      }
      return result;
   }

   private static Map<Object, String> fetchReferencedBOs(BusinessObjectData groupBy)
   {
      QName boName = new QName(groupBy.getModelId(), groupBy.getBusinessObjectId());
      BusinessObjectQuery boq = groupBy.getPrimaryKeyValues() == null
            ? BusinessObjectQuery.findForBusinessObject(boName.toString())
            : BusinessObjectQuery.findWithPrimaryKey(boName.toString(), groupBy.getPrimaryKeyValues());

      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      BusinessObjectQueryPredicate boqEvaluator = new BusinessObjectQueryPredicate(boq);
      Set<IData> allData = BusinessObjectUtils.collectData(modelManager, boqEvaluator);
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
         QueryDescriptor queryDescriptor = createFetchQuery(data, boqEvaluator.getPkValue(), true, null, null, null);
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

   protected static int fetchValues(QueryDescriptor queryDescriptor, Query query, RowProcessor processor)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ResultSet resultSet = session.executeQuery(queryDescriptor, QueryUtils.getTimeOut(query));
      SubsetPolicy subsetPolicy = QueryUtils.getSubset(query);
      try
      {
         int skipped = 0;
         int count = 0;
         int total = 0;
         while (resultSet.next())
         {
            total++;
            if (skipped < subsetPolicy.getSkippedEntries())
            {
               skipped++;
            }
            else
            {
               if (count < subsetPolicy.getMaxSize())
               {
                  count++;
                  processor.processRow(resultSet);
               }
               else
               {
                  if (!subsetPolicy.isEvaluatingTotalCount())
                  {
                     break;
                  }
               }
            }
         }
         return subsetPolicy.isEvaluatingTotalCount() ? total : 0;
      }
      catch (Exception e)
      {
         throw new PublicException(e);
      }
      finally
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet(resultSet);
      }
   }

   protected static QueryDescriptor createFetchQuery(IData data, Object pkValue, boolean boDataOnly,
         List<Join> predicateJoins, PredicateTerm parsedTerm, BusinessObjectData groupBy)
   {
      QueryDescriptor desc = QueryDescriptor
            .from(ProcessInstanceBean.class);
      Join dvJoin = desc
            .innerJoin(DataValueBean.class)
            .on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);

      BusinessObjectUtils.applyRestrictions(desc, predicateJoins, joinFilterPredicate);

      List<PredicateTerm> predicates = CollectionUtils.newList();
      predicates.add(boDataOnly
            ? Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1)
            : Predicates.notEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1));
      predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__MODEL, (long) data.getModel().getModelOID()));
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

   protected static FieldRef createStructuredDataJoins(boolean outerJoin, String fieldName, IData data,
         QueryDescriptor queryDescriptor, Join dataValueJoin,
         List<PredicateTerm> predicates, String prefix, Object value)
   {
      if (fieldName == null || fieldName.trim().isEmpty())
      {
         return null;
      }

      Join structDataValueJoin = outerJoin
            ? queryDescriptor.leftOuterJoin(StructuredDataValueBean.class, prefix + StructuredDataValueBean.DEFAULT_ALIAS)
            : queryDescriptor.innerJoin(StructuredDataValueBean.class, prefix + StructuredDataValueBean.DEFAULT_ALIAS);
      structDataValueJoin.on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);

      Join structDataJoin = outerJoin
            ? queryDescriptor.leftOuterJoin(StructuredDataBean.class, prefix + StructuredDataBean.DEFAULT_ALIAS)
            : queryDescriptor.innerJoin(StructuredDataBean.class, prefix + StructuredDataBean.DEFAULT_ALIAS);
      structDataJoin.on(structDataValueJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
              .andOn(dataValueJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
              .andOn(dataValueJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);
      predicates.add(Predicates.isEqual(structDataJoin.fieldRef(StructuredDataBean.FIELD__XPATH), fieldName));

      FieldRef valueField = getFieldRef(data, fieldName, structDataValueJoin);

      if (value instanceof Collection)
      {
         if (((Collection) value).isEmpty())
         {
            value = null;
         }
      }
      if (value != null)
      {
         if (value instanceof Collection)
         {
            predicates.add(Predicates.inList(valueField, CollectionUtils.newList((Collection) value)));
         }
         else
         {
            switch (LargeStringHolderBigDataHandler.classifyType(data, fieldName))
            {
            case BigData.STRING_VALUE:
               predicates.add(Predicates.isEqual(valueField, value.toString()));
               break;
            case BigData.NUMERIC_VALUE:
               predicates.add(Predicates.isEqual(valueField, ((Number) value).longValue()));
               break;
            default:
               // (fh) throw internal exception ?
            }
         }
      }

      return valueField;
   }

   protected static FieldRef getFieldRef(IData data, String name, Join sdvJoin)
   {
      FieldRef nameValueField = null;
      switch (LargeStringHolderBigDataHandler.classifyType(data, name))
      {
      case BigData.STRING_VALUE:
         nameValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__STRING_VALUE);
         break;
      case BigData.NUMERIC_VALUE:
         nameValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__NUMBER_VALUE);
         break;
      default:
         // (fh) throw internal exception ?
      }
      return nameValueField;
   }
}