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
import java.sql.ResultSet;
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

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.AndTerm;
import org.eclipse.stardust.engine.core.persistence.EvaluationOptions;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Operator;
import org.eclipse.stardust.engine.core.persistence.OrTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceHierarchyBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ProcessHierarchyAndDataPreprocessor extends ProcessHierarchyPreprocessor
{
   protected Node performTermLevelPreprocessing(FilterTerm filter,
         VisitationContext context)
   {
      List dataFilters = new ArrayList(filter.getParts().size());

      for (Iterator itr = filter.getParts().iterator(); itr.hasNext();)
      {
         FilterCriterion criterion = (FilterCriterion) itr.next();
         if (criterion instanceof AbstractDataFilter)
         {
            dataFilters.add(criterion);
         }
      }

      final Node result;
      if (!dataFilters.isEmpty())
      {
         boolean isAndTerm = filter.getKind().equals(FilterTerm.AND);
         if (isAndTerm)
         {
            result = evaluateDataFilters(dataFilters, isAndTerm, context);
         }
         else
         {
            // for OR-Terms the evaluation is done per filter scope mode
            // (simulating SQL-union).
            Map filtersByMode = new HashMap();
            for (Iterator iter = dataFilters.iterator(); iter.hasNext();)
            {
               AbstractDataFilter dataFilter = (AbstractDataFilter) iter.next();
               Integer currentScopeMode = new Integer(dataFilter
                                    .getFilterMode());

               List filters = (List) filtersByMode.get(currentScopeMode);
               if (null == filters)
               {
                  filters = new ArrayList();
                  filtersByMode.put(currentScopeMode, filters);
               }

               filters.add(dataFilter);
            }

            result = new Node(null, new HashSet());
            for (Iterator iter = filtersByMode.keySet().iterator(); iter.hasNext();)
            {
               List filters = (List) filtersByMode.get(iter.next());
               Node resultByMode = evaluateDataFilters(filters, isAndTerm, context);
               result.getProcessOIDs().addAll(resultByMode.getProcessOIDs());
            }
         }
      }
      else
      {
         result = new Node(null, null);
      }

      return result;
   }

   public Object visit(AbstractDataFilter filter, Object context)
   {
      // DataFilters will be evaluated during term-level preprocessing for improved
      // efficiency, so here the filter criterion will be simply removed
      return new Node(null);
   }

   private Node evaluateDataFilters(List dataFilters, boolean isAndTerm,
         VisitationContext context)
   {
      // TODO
      //final String hint = Parameters.instance().getString(KernelTweakingProperties.DATA_FILTER_HINT, "");

      final ModelManager modelManager = context.getEvaluationContext().getModelManager();

      QueryDescriptor query = QueryDescriptor//
            .from(ProcessInstanceBean.class)//
            .select(ProcessInstanceBean.FIELD__OID,
                  ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE);

      final Set processStateRestriction = context.getProcessStateRestriction();

      DataFilterExtensionContext dataFilterContext = new DataFilterExtensionContext(dataFilters);
      AndTerm predicateTerm;
      IJoinFactory joinFactory = new JoinFactory(query);
      if (isAndTerm)
      {
         predicateTerm = new AndTerm();

         int idx = 1;
         for (Iterator filterItr = dataFilters.iterator(); filterItr.hasNext(); ++idx)
         {
            AbstractDataFilter dataFilter = (AbstractDataFilter) filterItr.next();
            Map /*<Long,IData>*/ dataMap = this.findAllDataRtOids(dataFilter.getDataID(), modelManager);
            DataFilterExtension dataFilterExtension = SpiUtils.createDataFilterExtension(dataMap);
            Join dvJoin = dataFilterExtension.createDvJoin(query, dataFilter, idx, dataFilterContext, true, joinFactory);


            if (dataMap.isEmpty())
            {
               trace.warn("Invalid data ID used for data filter predicate: " + dataFilter.getDataID() + ".");
               dataFilterExtension.appendDataIdTerm(predicateTerm, Collections.EMPTY_MAP, dvJoin, dataFilter);
            }
            else
            {
               AndTerm dataFilterTerm = new AndTerm();
               dataFilterExtension.appendDataIdTerm(dataFilterTerm, dataMap, dvJoin, dataFilter);
               dataFilterTerm.add(dataFilterExtension.createPredicateTerm(dvJoin, dataFilter, dataMap, dataFilterContext));
               predicateTerm.add(dataFilterTerm);
            }
         }

         // TODO reintroduce "hint"
      }
      else
      {
    	 Map /*<String, Join>*/ dataFilterExtensions = new HashMap /*<String, Join> */();

         OrTerm dataPredicates = new OrTerm();
         int idx = 1;
         for (Iterator filtersByDataIdItr = dataFilterContext.getDataFiltersByDataId().entrySet().iterator(); filtersByDataIdItr.hasNext(); )
         {
            Entry e = (Entry) filtersByDataIdItr.next();
            String dataId = (String)e.getKey();
            List /*<AbstractDataFilter>*/ filtersForDataId = (List) e.getValue();

            AndTerm dataTerm = null;
            OrTerm dvPredicateTerm = null;
            Map /*<Long,IData>*/ dataMap = findAllDataRtOids(dataId, modelManager);
            for (Iterator filterItr = filtersForDataId.iterator(); filterItr.hasNext(); idx++)
            {
                  AbstractDataFilter dataFilter = (AbstractDataFilter) filterItr.next();
                  DataFilterExtension dataFilterExtension = SpiUtils.createDataFilterExtension(findAllDataRtOids(dataFilter.getDataID(), modelManager));
                  String extensionName = dataFilterExtension.getClass().getName();

                  Join dvJoin = (Join) dataFilterExtensions.get(extensionName);
                  if(dvJoin == null)
                  {
                	  dvJoin = dataFilterExtension.createDvJoin(query, dataFilter, idx, dataFilterContext, false, joinFactory);
                	  dvJoin.setRequired(false);
                	  dataFilterExtensions.put(extensionName, dvJoin);
                  }

                  if (null == dataTerm)
                  {
                     if (dataMap.isEmpty())
                     {
                        trace.warn("Invalid data ID used for data filter predicate: "
                              + dataFilter.getDataID() + ".");
                     }

                     dvPredicateTerm = new OrTerm();
                     dataTerm = new AndTerm();
                     dataFilterExtension.appendDataIdTerm(dataTerm, dataMap, dvJoin, dataFilter);
                     dataTerm.add(dvPredicateTerm);
                  }

                  dvPredicateTerm.add(dataFilterExtension.createPredicateTerm(dvJoin, dataFilter, dataMap, dataFilterContext));
            }

            if (null != dataTerm)
            {
               dataPredicates.add(dataTerm);
            }
         }

         predicateTerm = new AndTerm();
         if ((null != dataPredicates) && !dataPredicates.getParts().isEmpty())
         {
            predicateTerm.add(dataPredicates);
         }
      }

      QueryUtils.addModelVersionPredicate(predicateTerm,
            query.fieldRef(ProcessInstanceBean.FIELD__MODEL), context.getQuery(),
            modelManager);

      // optionally reduce working set by leveraging state filters
      if ( !processStateRestriction.isEmpty()
            && !processStateRestriction.equals(getFullProcessStateSet()))
      {
         List states = new ArrayList(processStateRestriction.size());
         for (Iterator i = processStateRestriction.iterator(); i.hasNext(); )
         {
            states.add(new Long(((ProcessInstanceState) i.next()).getValue()));
         }
         predicateTerm.add(Predicates.inList(
               query.fieldRef(ProcessInstanceBean.FIELD__STATE), states));
      }

      query.setPredicateTerm(predicateTerm);
      if (trace.isDebugEnabled())
      {
         trace.debug("Evaluating data filter: " + query.getQueryExtension().toString());
      }

      List largeDataFilters = new ArrayList(dataFilters.size());
      for (Iterator i = dataFilters.iterator(); i.hasNext();)
      {
         AbstractDataFilter dataFilter = (AbstractDataFilter) i.next();
         if (DataValueBean.isLargeValue(dataFilter.getOperand()))
         {
            largeDataFilters.add(dataFilter);
         }
      }

      final Set processOIDs = new HashSet();

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ResultSet piCandidates = session.executeQuery(query);
      try
      {
         final int PROCESS_INSTANCE_OID_IDX = 1;
         final int SCOPE_PROCESS_INSTANCE_OID_IDX = 2;

         while (piCandidates.next())
         {
            // only perform in-JVM match if SQL match may be inexact

            final long processOID = piCandidates.getLong(PROCESS_INSTANCE_OID_IDX);
            boolean isMatch = true;

            if (!largeDataFilters.isEmpty())
            {
               for (Iterator filterItr = largeDataFilters.iterator(); filterItr.hasNext();)
               {
                  AbstractDataFilter dataFilter = (AbstractDataFilter) filterItr.next();

                  final long scopeProcessInstanceOid = piCandidates.getLong(
                        SCOPE_PROCESS_INSTANCE_OID_IDX);

                  ValueProvider vp = getDataValueProvider(session,
                        scopeProcessInstanceOid, dataFilter, context);

                  isMatch &= isMatchingData(dataFilter, vp);
               }
            }

            if (isMatch)
            {
               processOIDs.add(new Long(processOID));
            }
         }
      }
      catch (Exception e)
      {
         trace.warn("Failed preevaluating data filters.", e);
      }
      finally
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet(piCandidates);
      }

      return new Node(null, processOIDs);
   }

   private Map /*<Long,IData>*/ findAllDataRtOids(String dataID, ModelManager modelManager)
   {
      Map /*<Long,IData>*/ dataMap = new HashMap();

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
            dataMap.put(new Long(modelManager.getRuntimeOid(data)), data);
         }
      }
      return dataMap;
   }

   private ValueProvider getDataValueProvider(Session session,
         long scopeProcessInstanceOid, AbstractDataFilter dataFilter,
         VisitationContext context)
   {
      ValueProvider valueProvider = null;
      String dataID = dataFilter.getDataID();
      String xpath = dataFilter.getAttributeName();
      boolean isStructuredType = StringUtils.isNotEmpty(xpath);

      ModelManager modelManager = context.getEvaluationContext().getModelManager();

      Set dataRtOids = new HashSet();
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
            if (isStructuredType)
            {
               dataRtOids.add(new Long(modelManager.getRuntimeOid(data, xpath)));
            }
            else
            {
               dataRtOids.add(new Long(modelManager.getRuntimeOid(data)));
            }
         }
      }

      ResultIterator result = null;
      if (isStructuredType)
      {
         result = session.getIterator(
               StructuredDataValueBean.class,
               QueryExtension.where(Predicates.andTerm(Predicates.isEqual(new FieldRef(
                     StructuredDataValueBean.class,
                     StructuredDataValueBean.FIELD__PROCESS_INSTANCE),
                     scopeProcessInstanceOid), Predicates.inList(
                     new FieldRef(StructuredDataValueBean.class,
                           StructuredDataValueBean.FIELD__XPATH), dataRtOids.iterator()))));
      }
      else
      {
         result = session.getIterator(DataValueBean.class,
               QueryExtension.where(Predicates.andTerm(Predicates.isEqual(new FieldRef(
                     DataValueBean.class, DataValueBean.FIELD__PROCESS_INSTANCE),
                     scopeProcessInstanceOid), Predicates.inList(new FieldRef(
                     DataValueBean.class, DataValueBean.FIELD__DATA),
                     dataRtOids.iterator()))));
      }

      try
      {
         if (result.hasNext())
         {
            valueProvider = (ValueProvider) result.next();
         }
      }
      finally
      {
         result.close();
      }

      return valueProvider;
   }

   private boolean isMatchingData(AbstractDataFilter filter, ValueProvider valueProvider)
   {
      if (filter == null || valueProvider == null)
      {
         return false;
      }

      boolean isMatching;

      Object lhsOperand = valueProvider.getValue();
      Serializable rhsOperand = filter.getOperand();

      if ( !EvaluationOptions.isCaseSensitive(filter))
      {
         if (lhsOperand instanceof String)
         {
            lhsOperand = ((String) lhsOperand).toLowerCase();
         }
         if (rhsOperand instanceof String)
         {
            rhsOperand = ((String) rhsOperand).toLowerCase();
         }
      }

      if (filter.getOperator().equals(Operator.IS_EQUAL))
      {
         isMatching = (0 == CompareHelper.compare(lhsOperand, rhsOperand));
      }
      else if (filter.getOperator().equals(Operator.NOT_EQUAL))
      {
         isMatching = (0 != CompareHelper.compare(lhsOperand, rhsOperand));
      }
      else if (filter.getOperator().equals(Operator.LESS_THAN))
      {
         isMatching = (0 > CompareHelper.compare(lhsOperand, rhsOperand));
      }
      else if (filter.getOperator().equals(Operator.LESS_OR_EQUAL))
      {
         isMatching = (0 >= CompareHelper.compare(lhsOperand, rhsOperand));
      }
      else if (filter.getOperator().equals(Operator.GREATER_OR_EQUAL))
      {
         isMatching = (0 <= CompareHelper.compare(lhsOperand, rhsOperand));
      }
      else if (filter.getOperator().equals(Operator.GREATER_THAN))
      {
         isMatching = (0 < CompareHelper.compare(lhsOperand, rhsOperand));
      }
      else if (filter.getOperator().equals(Operator.IN))
      {
         isMatching = false;

         for (Iterator i = ((Collection) rhsOperand).iterator(); i.hasNext();)
         {
            isMatching |= (0 == CompareHelper.compare(lhsOperand, i.next()));
         }
      }
      else if (filter.getOperator().equals(Operator.BETWEEN))
      {
         Pair pair = (Pair) rhsOperand;

         isMatching = (0 <= CompareHelper.compare(lhsOperand, pair.getFirst()))
               && (0 >= CompareHelper.compare(lhsOperand, pair.getSecond()));
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.QUERY_UNSUPPORTED_DATAFILTER_OPERATOR_FOR_BIG_DATA_VALUE
                     .raise(filter.getOperator()));
      }

      return isMatching;
   }


   // TODO (peekaboo): Refactor this and other classes with same name and semantic into common (base) class
   private static class JoinFactory implements IJoinFactory
   {
      final private QueryDescriptor query;

      public JoinFactory(QueryDescriptor query)
      {
         this.query = query;
      }

      public Join createDataFilterJoins(int dataFilterMode, int index, Class dvClass, FieldRef dvProcessInstanceField)
      {
         final Join dvJoin;
         final String idx = index == -1 ? "" : "" + index;
         final String dvAlias = "PR_"+ dvProcessInstanceField.getType().getTableAlias()+ idx;

         if (AbstractDataFilter.MODE_ALL_FROM_SCOPE == dataFilterMode)
         {
            dvJoin = query.innerJoin(dvClass, dvAlias)//
                  .on(query.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);
         }
         else if (AbstractDataFilter.MODE_SUBPROCESSES == dataFilterMode)
         {
            Join fromSubProcJoin = query.innerJoin(ProcessInstanceHierarchyBean.class, "PR_PIH" + idx)//
                  .on(query.fieldRef(ProcessInstanceBean.FIELD__OID),
                        ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE);

            dvJoin = query.innerJoin(dvClass, dvAlias)//
                  .on(fromSubProcJoin.fieldRef(ProcessInstanceHierarchyBean.FIELD__PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);
         }
         else if (AbstractDataFilter.MODE_ALL_FROM_HIERARCHY == dataFilterMode)
         {
            Join scopeJoin = query.innerJoin(ProcessInstanceScopeBean.class, "PR_PIS" + idx)//
                  .on(query.fieldRef(ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE),
                        ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE);

            dvJoin = query.innerJoin(dvClass, dvAlias)//
                  .on(scopeJoin.fieldRef(ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);
         }
         else
         {
            throw new InternalException(MessageFormat.format(
                  "Invalid DataFilter mode: {0}.", new Object[] {new Integer(
                        dataFilterMode)}));
         }

         return dvJoin;
      }
   }
}