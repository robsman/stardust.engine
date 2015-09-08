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
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      final BenchmarkBusinessObjectStatisticsResult result =
            new BenchmarkBusinessObjectStatisticsResult(query);

      BusinessObjectData filter = boPolicy.getFilter();
      BusinessObjectData groupBy = boPolicy.getGroupBy();

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
      Set<IData> allData = BusinessObjectUtils.collectData(modelManager, boqEvaluator);
      if (allData.isEmpty())
      {
         return result;
      }

      if (!BusinessObjectUtils.isUniqueBusinessObject(allData))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("query","Business Object not uniquely specified."));
      }

      fetchValues(result, allData, boqEvaluator.getPkValue(), boq, query, groupBy);
      return result;
   }

   private static void fetchValues(BenchmarkBusinessObjectStatisticsResult result, Set<IData> allData,
         Object pkValue, Query query, BenchmarkProcessStatisticsQuery piQuery, BusinessObjectData groupBy)
   {
      Map<Object, String> otherBOs = groupBy == null ? Collections.<Object, String>emptyMap() : fetchReferencedBOs(groupBy);

      for (IData data : allData)
      {
         ProcessInstanceQuery pi = ProcessInstanceQuery.findAll();
         BusinessObjectUtils.copyFilters(piQuery.getFilter(), pi.getFilter(), piFilterPredicate);
         ProcessInstanceQueryEvaluator eval = new ProcessInstanceQueryEvaluator(pi, QueryServiceUtils.getDefaultEvaluationContext());
         ParsedQuery parsedQuery = eval.parseQuery();
         List<Join> predicateJoins = parsedQuery.getPredicateJoins();
         PredicateTerm parsedTerm = parsedQuery.getPredicateTerm();

         QueryDescriptor queryDescriptor = createFetchQuery(data, pkValue, predicateJoins, parsedTerm, groupBy);
         fetchValues(result, data, queryDescriptor, query, groupBy, otherBOs);
      }
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

      Map<Object, String> result = new HashMap<Object, String>();
      fetchPkAndNames(result, allData, boqEvaluator.getPkValue(), boq);
      return result;
   }

   private static void fetchPkAndNames(Map<Object, String> result, Set<IData> allData,
         Object pkValue, BusinessObjectQuery boq)
   {
      for (IData data : allData)
      {
         QueryDescriptor queryDescriptor = createFetchQuery(data, pkValue);
         fetchValues(result, queryDescriptor);
      }
   }

   protected static void fetchValues(Map<Object, String> result, QueryDescriptor queryDescriptor)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ResultSet resultSet = session.executeQuery(queryDescriptor);
      try
      {
         while (resultSet.next())
         {
            Object boPk = resultSet.getObject(1);
            Object boName = resultSet.getObject(2);
            String name = boName == null ? boPk == null ? "" : boPk.toString() : boName.toString();
            if (!result.containsKey(boPk))
            {
               result.put(boPk, name);
            }
         }
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

   protected static QueryDescriptor createFetchQuery(IData data, Object pkValue)
   {
      long modelOID = data.getModel().getModelOID();
      long dataRtOID = ModelManagerFactory.getCurrent().getRuntimeOid(data);

      String pk = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
      String name = data.getAttribute(PredefinedConstants.BUSINESS_OBJECT_NAMEEXPRESSION);

      QueryDescriptor desc = QueryDescriptor
            .from(ProcessInstanceBean.class);
      Join dvJoin = desc
            .innerJoin(DataValueBean.class)
            .on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);

      List<PredicateTerm> predicates = CollectionUtils.newList();
      predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1));
      predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOID));
      predicates.add(Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), dataRtOID));

      Join pkSdvJoin = desc
            .innerJoin(StructuredDataValueBean.class, "pk_" + StructuredDataValueBean.DEFAULT_ALIAS)
            .on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
      Join pkSdJoin = desc
            .innerJoin(StructuredDataBean.class, "pk_" + StructuredDataBean.DEFAULT_ALIAS)
            .on(pkSdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);
      predicates.add(Predicates.isEqual(pkSdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), pk));

      Join nameSdvJoin = desc
            .leftOuterJoin(StructuredDataValueBean.class, "name_" + StructuredDataValueBean.DEFAULT_ALIAS)
            .on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
      Join nameSdJoin = desc
            .leftOuterJoin(StructuredDataBean.class, "name_" + StructuredDataBean.DEFAULT_ALIAS)
            .on(nameSdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);
      predicates.add(Predicates.isEqual(nameSdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), name));

      FieldRef pkValueField = getFieldRef(data, pk, pkSdvJoin);
      FieldRef nameValueField = getFieldRef(data, name, nameSdvJoin);

      if (pkValue instanceof Collection)
      {
         if (((Collection) pkValue).isEmpty())
         {
            pkValue = null;
         }
      }
      if (pkValue != null)
      {
         int pkType = LargeStringHolderBigDataHandler.classifyType(data, pk);
         if (pkValue instanceof Collection)
         {
            predicates.add(Predicates.inList(pkValueField, CollectionUtils.newList((Collection) pkValue)));
         }
         else
         {
            predicates.add(getIsEqualPredicate(pkType, pkValueField, pkValue));
         }
      }

      desc.select(pkValueField, nameValueField);
      desc.where(new AndTerm(predicates.toArray(new PredicateTerm[predicates.size()])));
      return desc;
   }

   protected static int fetchValues(BenchmarkBusinessObjectStatisticsResult result,
         IData data, QueryDescriptor queryDescriptor, Query query, BusinessObjectData groupBy, Map<Object, String> otherBOs)
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
                  long piOid = resultSet.getLong(1);
                  int piState = resultSet.getInt(2);
                  int piBenchmarkValue = resultSet.getInt(3);
                  Object boPk = resultSet.getObject(4);
                  Object boName = resultSet.getObject(5);
                  String name = boName == null ? boPk == null ? "" : boPk.toString() : boName.toString();

                  String groupByName = null;
                  if (groupBy != null)
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

   protected static QueryDescriptor createFetchQuery(IData data, Object pkValue,
         List<Join> predicateJoins, PredicateTerm parsedTerm, BusinessObjectData groupBy)
   {
      long modelOID = data.getModel().getModelOID();
      long dataRtOID = ModelManagerFactory.getCurrent().getRuntimeOid(data);

      String pk = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
      String name = data.getAttribute(PredefinedConstants.BUSINESS_OBJECT_NAMEEXPRESSION);

      QueryDescriptor desc = QueryDescriptor
            .from(ProcessInstanceBean.class);
      Join dvJoin = desc
            .innerJoin(DataValueBean.class)
            .on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);
      BusinessObjectUtils.applyRestrictions(desc, predicateJoins, joinFilterPredicate);

      List<PredicateTerm> predicates = CollectionUtils.newList();
      predicates.add(Predicates.notEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1));
      predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOID));
      predicates.add(Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), dataRtOID));

      Join pkSdvJoin = desc
            .innerJoin(StructuredDataValueBean.class, "pk_" + StructuredDataValueBean.DEFAULT_ALIAS)
            .on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
      Join pkSdJoin = desc
            .innerJoin(StructuredDataBean.class, "pk_" + StructuredDataBean.DEFAULT_ALIAS)
            .on(pkSdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);
      predicates.add(Predicates.isEqual(pkSdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), pk));

      Join nameSdvJoin = desc
            .leftOuterJoin(StructuredDataValueBean.class, "name_" + StructuredDataValueBean.DEFAULT_ALIAS)
            .on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
      Join nameSdJoin = desc
            .leftOuterJoin(StructuredDataBean.class, "name_" + StructuredDataBean.DEFAULT_ALIAS)
            .on(nameSdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);
      predicates.add(Predicates.isEqual(nameSdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), name));

      FieldRef pkValueField = getFieldRef(data, pk, pkSdvJoin);
      FieldRef nameValueField = getFieldRef(data, name, nameSdvJoin);

      if (pkValue instanceof Collection)
      {
         if (((Collection) pkValue).isEmpty())
         {
            pkValue = null;
         }
      }
      if (pkValue != null)
      {
         int pkType = LargeStringHolderBigDataHandler.classifyType(data, pk);
         if (pkValue instanceof Collection)
         {
            predicates.add(Predicates.inList(pkValueField, CollectionUtils.newList((Collection) pkValue)));
         }
         else
         {
            predicates.add(getIsEqualPredicate(pkType, pkValueField, pkValue));
         }
      }

      Column[] columns = new Column[] {ProcessInstanceBean.FR__OID, ProcessInstanceBean.FR__STATE, ProcessInstanceBean.FR__BENCHMARK_VALUE, pkValueField, nameValueField};

      if (groupBy != null)
      {
         Map<String, BusinessObjectRelationship> relationships = BusinessObjectUtils.getBusinessObjectRelationships(data);
         for (BusinessObjectRelationship rel : relationships.values())
         {
            if (new QName(rel.otherBusinessObject.modelId, rel.otherBusinessObject.id)
                  .equals(new QName(groupBy.getModelId(), groupBy.getBusinessObjectId())))
            {
               String fk = rel.otherForeignKeyField;
               Join fkSdvJoin = desc
                     .innerJoin(StructuredDataValueBean.class, "fk_" + StructuredDataValueBean.DEFAULT_ALIAS)
                     .on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
               Join fkSdJoin = desc
                     .innerJoin(StructuredDataBean.class, "fk_" + StructuredDataBean.DEFAULT_ALIAS)
                     .on(fkSdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
                     .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
                     .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);
               predicates.add(Predicates.isEqual(fkSdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), fk));

               FieldRef fkValueField = getFieldRef(data, fk, fkSdvJoin);
               columns = new Column[] {ProcessInstanceBean.FR__OID, ProcessInstanceBean.FR__STATE, ProcessInstanceBean.FR__BENCHMARK_VALUE, pkValueField, nameValueField, fkValueField};

               Object fkValue = groupBy.getPrimaryKeyValues();
               if (fkValue instanceof Collection)
               {
                  if (((Collection) fkValue).isEmpty())
                  {
                     fkValue = null;
                  }
               }
               if (fkValue != null)
               {
                  int fkType = LargeStringHolderBigDataHandler.classifyType(data, fk);
                  if (fkValue instanceof Collection)
                  {
                     predicates.add(Predicates.inList(fkValueField, CollectionUtils.newList((Collection) fkValue)));
                  }
                  else
                  {
                     predicates.add(getIsEqualPredicate(fkType, fkValueField, fkValue));
                  }
               }
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

   protected static ComparisonTerm getIsEqualPredicate(int pkType, FieldRef pkValueField,
         Object pkValue)
   {
      return pkType == BigData.NUMERIC_VALUE
            ? Predicates.isEqual(pkValueField, ((Number) pkValue).longValue())
            : Predicates.isEqual(pkValueField, pkValue.toString());
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