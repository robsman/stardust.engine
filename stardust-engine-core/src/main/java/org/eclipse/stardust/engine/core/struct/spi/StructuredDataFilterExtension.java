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
package org.eclipse.stardust.engine.core.struct.spi;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.SqlBuilderBase.DataAttributeKey;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.OrderCriteria;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

public class StructuredDataFilterExtension implements DataFilterExtension, Stateless
{
   static final Logger trace = LogManager.getLogger(StructuredDataFilterExtension.class);

   // TODO )ab) write a junit test (see JoinTest)

   private void preprocessJoins(QueryDescriptor query,
         DataFilterExtensionContext dataFilterExtensionContext, boolean isAndTerm,
         IJoinFactory joinFactory)
   {
      StructuredDataFilterExtensionContext extensionContext = new StructuredDataFilterExtensionContext();
      dataFilterExtensionContext.setContent(extensionContext);

      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      int cnt = 0;
      Iterator<List<AbstractDataFilter>> iter = new FilteringIterator(dataFilterExtensionContext
            .getDataFiltersByDataId().values().iterator(), new Predicate()
      {
         public boolean accept(Object o)
         {
            if (o instanceof List)
            {
               List<AbstractDataFilter> dataFiltersForOneData = (List) o;
               if (!dataFiltersForOneData.isEmpty())
               {
                  AbstractDataFilter dataFilter = dataFiltersForOneData.get(0);
                  String dataID = dataFilter.getDataID();

                  String namespace = null;
                  if (dataID.startsWith("{"))
                  {
                     QName qname = QName.valueOf(dataID);
                     namespace = qname.getNamespaceURI();
                     dataID = qname.getLocalPart();
                  }

                  List<IModel> candidates = StringUtils.isEmpty(namespace)
                        ? modelManager.getModels()
                        : modelManager.getModelsForId(namespace);
                  for (IModel model : candidates)
                  {
                     IData data = model.findData(dataID);
                     if (null != data)
                     {
                        PluggableType type = data.getType();
                        if (type != null)
                        {
                           String typeId = type.getId();
                           if (StructuredTypeRtUtils.isDmsType(typeId) || StructuredTypeRtUtils.isStructuredType(typeId))
                           {
                              return true;
                           }
                        }
                     }
                  }
               }
            }

            return false;
         }
      });
      while (iter.hasNext())
      {
         List<AbstractDataFilter> dataFiltersForOneData = iter.next();

         for (AbstractDataFilter filter : dataFiltersForOneData)
         {
            cnt++;
            boolean dataFiltersDoesNotContainListXPaths = dataFiltersDoesNotContainListXPaths(
                  filter.getDataID(), dataFiltersForOneData);
            if (!dataFiltersDoesNotContainListXPaths)
            {
               dataFilterExtensionContext.useDistinct(true);
            }

            if (!extensionContext.contains(filter))
            {
               StructuredDataFilterContext filterContext = getContextForDataFilter(
                     joinFactory, dataFilterExtensionContext, filter, isAndTerm, cnt);
               extensionContext.setContext(filter, filterContext);
            }
         }
      }
   }


   private StructuredDataFilterContext getContextForDataFilter(IJoinFactory joinFactory,
         DataFilterExtensionContext dataFilterExtensionContext,
         AbstractDataFilter filter, boolean isAndTerm, int cnt)
   {

      int id = cnt;

      StructuredDataFilterContext context = new StructuredDataFilterContext(id, filter);
      validateXPath(filter.getDataID(), filter.getAttributeName(), true);

      Join join = joinFactory.createDataFilterJoins(filter.getFilterMode(), id,
            StructuredDataValueBean.class, StructuredDataValueBean.FR__PROCESS_INSTANCE);
      dataFilterExtensionContext.addJoin(join);
      context.setJoin(join);

      return context;
   }

   private void validateXPath(String dataId, String xPath, boolean canReturnLists)
   {
      if (StringUtils.isEmpty(xPath))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.QUERY_MISSING_XPATH_ON_NON_STRUCT_DATA
                     .raise(dataId, xPath));
      }

      boolean isValid = false;

      Collection<IData> allData = this.findAllDatas(dataId, ModelManagerFactory.getCurrent());
      for (Iterator<IData> i = allData.iterator(); i.hasNext(); )
      {
         IData data = (IData) i.next();
         IXPathMap xPathMap = DataXPathMap.getXPathMap(data);

         // throws a PublicException if XPath is not defined
         TypedXPath typedXPath = null;
         try
         {
            typedXPath = xPathMap.getXPath(xPath);
            isValid = true;
         }
         catch (IllegalOperationException e)
         {
            // check if indexed
            if(StructuredDataXPathUtils.isIndexedXPath(xPath))
            {
               throw new IllegalOperationException(BpmRuntimeError.BPMRT_INVALID_INDEXED_XPATH.raise());
            }
         }

         if (typedXPath != null && typedXPath.getType() == BigData.NULL)
         {
            // complex types or lists of complex types are not allowed as query attributes
            throw new IllegalOperationException(
                  BpmRuntimeError.QUERY_XPATH_ON_STRUCT_DATA_MUST_POINT_TO_PRIMITIVE
                        .raise(dataId, xPath));
         }

         if (typedXPath != null && !canReturnLists)
         {
            if (StructuredDataXPathUtils.canReturnList(typedXPath.getXPath(), xPathMap))
            {
               throw new IllegalOperationException(
                     BpmRuntimeError.QUERY_XPATH_ON_STRUCT_DATA_ORDER_BY_MUST_POINT_TO_PRIMITIVE
                           .raise(dataId, xPath));
            }
         }
      }

      if(!isValid)
      {
         throw new IllegalOperationException(
               BpmRuntimeError.MDL_UNKNOWN_XPATH
                     .raise(xPath));
      }
   }

   private boolean dataFiltersDoesNotContainListXPaths(String dataId, List<AbstractDataFilter> filtersForData)
   {
      Set<IData> allData = this.findAllDatas(dataId, ModelManagerFactory.getCurrent());

      for (Iterator<IData> i = allData.iterator(); i.hasNext(); )
      {
         IData data = i.next();
         IXPathMap xPathMap = DataXPathMap.getXPathMap(data);

         for (Iterator<AbstractDataFilter> f = filtersForData.iterator(); f.hasNext(); )
         {
            AbstractDataFilter df = f.next();
            TypedXPath typedXPath = null;

            try
            {
               typedXPath = xPathMap.getXPath(df.getAttributeName());
            }
            catch (Exception e)
            {
            }

            if (typedXPath != null)
            {
               if (StructuredDataXPathUtils.canReturnList(typedXPath.getXPath(), xPathMap))
               {
                  return false;
               }
            }
         }
      }
      return true;
   }

   private Set<IData> findAllDatas(String dataID, ModelManager modelManager)
   {
      Set datas = new HashSet<IData>();

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
            datas.add(data);
         }
      }

      return datas;
   }

   public PredicateTerm createPredicateTerm(Join dvJoin, AbstractDataFilter dataFilter,
         Map<Long, IData> dataMap, DataFilterExtensionContext dataFilterExtensionContext)
   {
      if ( !Operator.NOT_ANY_OF.equals(dataFilter.getOperator()))
      {
         // override dvJoin with join specific for this dataFilter
         StructuredDataFilterExtensionContext extensionContext = dataFilterExtensionContext
               .getContent();
         dvJoin = extensionContext.getJoin(dataFilter);
      }
      boolean filterUsedInAndTerm = dataFilterExtensionContext.isFilterUsedInAndTerm();

      return matchDataInstancesPredicate(dvJoin, dataFilter.getAttributeName(),
            dataFilter.getOperator(), dataFilter.getOperand(), dataMap, dataFilter,
            filterUsedInAndTerm);
   }

   /**
    * Builds a predicate fragment for matching data instances having the given value.
    * Depending on the data type this predicate may result in an exact match (if the value
    * can be represented inline in the <code>data_value</code> table) or just match a
    * set of candidate instances (if the value's representations has to be sliced for
    * storage).
    * @param xPathString
    *           xPath
    * @param value
    *           The generic representation of the data value to match with.
    * @param dataMap
    * @param evaluationOptions TODO
    *
    * @return A predicate term for matching data instances possibly having the given
    *         value.
    * @see #isLargeValue
    */
   private PredicateTerm matchDataInstancesPredicate(Join dvJoin, String xPathString,
         Operator operator, Object value, Map<Long, IData> dataMap,
         final IEvaluationOptionProvider evaluationOptions, boolean filterUsedInAndTerm)
   {
      final LargeStringHolderBigDataHandler.Representation canonicalValue = LargeStringHolderBigDataHandler.canonicalizeDataValue(
            StructuredDataValueBean.string_value_COLUMN_LENGTH, value);

      FieldRef valueColumn;
      Object matchValue = canonicalValue.getRepresentation();

      switch (canonicalValue.getClassificationKey())
      {
         case BigData.NULL_VALUE:
            valueColumn = null;
            break;

         case BigData.NUMERIC_VALUE:
            valueColumn = dvJoin.fieldRef(StructuredDataValueBean.FIELD__NUMBER_VALUE);
            break;

         case BigData.STRING_VALUE:
            valueColumn = dvJoin.fieldRef(StructuredDataValueBean.FIELD__STRING_VALUE);
            break;

         default:
            throw new InternalException("Unsupported BigData type classification: "
                  + canonicalValue.getClassificationKey());
      }

      final AndTerm resultTerm = new AndTerm();

      if (operator instanceof Operator.Unary)
      {
         resultTerm.add(new ComparisonTerm(
               dvJoin.fieldRef(StructuredDataValueBean.FIELD__TYPE_KEY),
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
                  orTerm.add(new ComparisonTerm(dvJoin
                        .fieldRef(StructuredDataValueBean.FIELD__TYPE_KEY),
                        Operator.IS_NULL));
               }
               orTerm.add(new ComparisonTerm(dvJoin
                     .fieldRef(StructuredDataValueBean.FIELD__TYPE_KEY),
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
            if (Operator.LIKE.equals(operator)
                  && (BigData.STRING == canonicalValue.getTypeKey()))
            {
               resultTerm.add(Predicates.inList(
                     dvJoin.fieldRef(StructuredDataValueBean.FIELD__TYPE_KEY),
                     new int[] {BigData.STRING, BigData.BIG_STRING}));
            }
            else
            {
               resultTerm.add(Predicates.isEqual(
                     dvJoin.fieldRef(StructuredDataValueBean.FIELD__TYPE_KEY),
                     canonicalValue.getTypeKey()));
            }

            if ( !EvaluationOptions.isCaseSensitive(evaluationOptions))
            {
               // ignore case by applying LOWER(..) SQL function
               valueColumn = Functions.strLower(valueColumn);
            }

            if (operator.isBinary())
            {
               if (matchValue instanceof Collection)
               {
                  final boolean isNotAnyOfFilter = Operator.NOT_ANY_OF.equals(operator);

                  List<List< ? >> subLists = CollectionUtils.split(
                        (Collection) matchValue, 1000);
                  MultiPartPredicateTerm mpTerm = new OrTerm();
                  for (List< ? > subList : subLists)
                  {
                     Iterator valuesIter = new TransformingIterator(subList.iterator(),
                           new Functor()
                           {
                              public Object execute(Object source)
                              {
                                 return getInlineComparisonValue(source,
                                       evaluationOptions);
                              }
                           });


                     if (Operator.NOT_IN.equals(operator))
                     {
                        mpTerm.add(Predicates.notInList(valueColumn, valuesIter));
                     }
                     else if (Operator.IN.equals(operator))
                     {
                        mpTerm.add(Predicates.inList(valueColumn, valuesIter));
                     }
                     else if (isNotAnyOfFilter)
                     {
                        // add the following only once to this join - driven by content of mpTerm
                        if (mpTerm.getParts().isEmpty())
                        {
                           // add mpTerm itself
                           dvJoin.getRestriction().add(mpTerm);

                           // also add the current resultTerm
                           dvJoin.getRestriction().add(resultTerm);
                        }

                        // add inList predicate to mpTerm
                        mpTerm.add(Predicates.inList(valueColumn, valuesIter));
                     }
                  }

                  if (isNotAnyOfFilter)
                  {
                     // return a single predicate for this kind of operator as all others have been added to join
                     return Predicates.isNull(dvJoin
                           .fieldRef(StructuredDataValueBean.FIELD__PROCESS_INSTANCE));
                  }
                  else
                  {
                     resultTerm.add(mpTerm);
                  }
               }
               else
               {
                  resultTerm.add(new ComparisonTerm(valueColumn,
                        (Operator.Binary) operator, getInlineComparisonValue(matchValue,
                              evaluationOptions)));
               }
            }
            else if (operator.isTernary())
            {
               if ( !(matchValue instanceof Pair))
               {
                  throw new PublicException("Inconsistent operator use " + operator
                        + " --> " + matchValue);
               }

               Pair pair = (Pair) matchValue;
               resultTerm.add(new ComparisonTerm(valueColumn,
                     (Operator.Ternary) operator, new Pair(
                           getInlineComparisonValue(pair.getFirst(), evaluationOptions),
                           getInlineComparisonValue(pair.getSecond(), evaluationOptions))));
            }
         }
      }

      return resultTerm;
   }

   private Set<Long> findAllOids(Map<Long, IData> dataMap, String xPathString)
   {
      // find all rt oids for xPathString
      Set <Long> xPathOids = new HashSet<Long>();
      for (Iterator<IData> i = dataMap.values().iterator(); i.hasNext();)
      {
         IData data = (IData) i.next();
         IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
         Long xPathOid = xPathMap.getXPathOID(xPathString);
         if (xPathOid != null)
         {
            xPathOids.add(xPathOid);
         }
      }
      return xPathOids;
   }

   private static final Object getInlineComparisonValue(Object value, IEvaluationOptionProvider options)
   {
      Object result;

      if (value instanceof String
            && ((String) value).length() > StructuredDataValueBean.string_value_COLUMN_LENGTH)
      {
         // strip for maximum inline slice size

         result = ((String) value).substring(0,
               StructuredDataValueBean.string_value_COLUMN_LENGTH);
      }
      else
      {
         result = value;
      }

      if (( !EvaluationOptions.isCaseSensitive(options)) && (result instanceof String))
      {
         result = ((String) result).toLowerCase();
      }

      return result;
   }

   public void extendOrderCriteria(Join piJoin, Join pisJoin,
         OrderCriteria orderCriteria, DataOrder order, Map<Long, IData> dataMap,
         Map<String, Join> dataOrderJoins)
   {
      validateXPath(order.getDataID(), order.getAttributeName(), false);

      String alias = "DVO" + (dataOrderJoins.size() + 1);
      Join dvJoin;
      if (null != pisJoin)
      {
         dvJoin = new Join(StructuredDataValueBean.class, alias)//
            .on(pisJoin.fieldRef(ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE),
                  StructuredDataValueBean.FIELD__PROCESS_INSTANCE);

         dvJoin.setDependency(pisJoin);
      }
      else if (null != piJoin)
      {
         dvJoin = new Join(StructuredDataValueBean.class, alias)//
            .on(piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE),
                  StructuredDataValueBean.FIELD__PROCESS_INSTANCE);

         dvJoin.setDependency(piJoin);
      }
      else
      {
         dvJoin = new Join(StructuredDataValueBean.class, alias)//
            .on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                  StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
      }
      Set<Long> xPathOids = findAllOids(dataMap, order.getAttributeName());
      dvJoin.where(Predicates.inList(
            dvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH),
            xPathOids.iterator()));
      Assert.isNotEmpty(xPathOids, "XPath '" + order.getAttributeName()
            + "' is not defined");

      dvJoin.setRequired(false);

      alias = alias + "_SD";


      boolean useNumericColumn = false;
      boolean useStringColumn = false;
      boolean useDoubleColumn = false;

      for (Iterator<IData> i = dataMap.values().iterator(); i.hasNext();)
      {
         IData data = i.next();
         final int typeClassification = LargeStringHolderBigDataHandler
               .classifyTypeForSorting(data, order.getAttributeName());
         useNumericColumn |= (BigData.NUMERIC_VALUE == typeClassification);
         useStringColumn |= (BigData.STRING_VALUE == typeClassification);
         useDoubleColumn |= (BigData.DOUBLE_VALUE == typeClassification);
      }

      if (useNumericColumn)
      {
         orderCriteria.add(dvJoin.fieldRef(StructuredDataValueBean.FIELD__NUMBER_VALUE),
               order.isAscending());
      }

      if (useStringColumn)
      {
         orderCriteria.add(dvJoin.fieldRef(StructuredDataValueBean.FIELD__STRING_VALUE),
               order.isAscending());
      }

      if (useDoubleColumn)
      {
         orderCriteria.add(dvJoin.fieldRef(StructuredDataValueBean.FIELD__DOUBLE_VALUE),
               order.isAscending());
      }

      // else do nothing (compatible to behavior of other data types!)
      String dataId = order.getDataID() + "/" + order.getAttributeName();
      dataOrderJoins.put(dataId, dvJoin);
   }

   public void appendDataIdTerm(AndTerm andTerm, Map<Long, IData> dataIds, Join dvJoin,
         AbstractDataFilter dataFilter)
   {
      final String xPathString = dataFilter.getAttributeName();
      if (xPathString == null)
      {
         throw new InternalException(
               "DataFilter for structured data should specify xpath in the attribute name.");
      }

      Set<Long> xPathOids = findAllOids(dataIds, xPathString);

      /*
      if(dvJoin.getRestriction() != null)
      {
         FieldRef xPathField = dvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH);
         if(inflateXPathTerm(dvJoin.getRestriction().getParts(), xPathField, xPathOids))
         {
            return;
         }
      }
      */

      if(!xPathOids.isEmpty())
      {
         andTerm.add(Predicates.inList(
               dvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH),
               xPathOids.iterator()));
      }
   }

   /*private boolean inflateXPathTerm(List<PredicateTerm> parts, FieldRef xPathField, Set<Long> xPathOids)
   {
      boolean inflated = false;
      if(parts == null || xPathOids.size() == 0)
      {
         return false;
      }
      for(int i = 0; i < parts.size() && inflated == false; i++)
      {
         PredicateTerm term = parts.get(i);
         if(term instanceof MultiPartPredicateTerm)
         {
            inflated = inflateXPathTerm(((MultiPartPredicateTerm)term).getParts(), xPathField, xPathOids);
         }
         else if(term instanceof ComparisonTerm)
         {
            ComparisonTerm cTerm = (ComparisonTerm)term;
            Object values = cTerm.getValueExpr();
            if(xPathField.equals(cTerm.getLhsField()) &&
                  values instanceof List)
            {
               ((List)values).addAll(xPathOids);
               inflated = true;
            }
         }
      }
      return inflated;
   }*/

   public Join createDvJoin(QueryDescriptor query, AbstractDataFilter dataFilter,
         int index, DataFilterExtensionContext dataFilterExtensionContext,
         boolean isAndTerm, IJoinFactory joinFactory)
   {
      // notAnyOf Filter definitively need their own join
      if (Operator.NOT_ANY_OF.equals(dataFilter.getOperator()))
      {
         validateXPath(dataFilter.getDataID(), dataFilter.getAttributeName(), true);
         return joinFactory.createDataFilterJoins(dataFilter.getFilterMode(), index,
               StructuredDataValueBean.class,
               StructuredDataValueBean.FR__PROCESS_INSTANCE);
      }

      if (dataFilterExtensionContext.getContent() == null)
      {
         // preprocess joins first
         preprocessJoins(query, dataFilterExtensionContext, isAndTerm, joinFactory);
      }

      StructuredDataFilterExtensionContext extensionContext = dataFilterExtensionContext.getContent();
      return extensionContext.getJoin(dataFilter);
   }

   public List<FieldRef> getPrefetchSelectExtension(ITableDescriptor descriptor)
   {
      List<FieldRef> cols = CollectionUtils.newArrayList();

      cols.add(descriptor.fieldRef(StructuredDataValueBean.FIELD__TYPE_KEY));
      cols.add(descriptor.fieldRef(StructuredDataValueBean.FIELD__STRING_VALUE));
      cols.add(descriptor.fieldRef(StructuredDataValueBean.FIELD__NUMBER_VALUE));

      return cols;
   }

   public boolean isStateless()
   {
      return true;
   }

   // (fh) out of inspiration for names
   private static class StructuredDataFilterExtensionContext
   {
      private Map<DataAttributeKey, StructuredDataFilterContext> contexts = CollectionUtils.newHashMap();

      private Join getJoin(AbstractDataFilter dataFilter)
      {
         DataAttributeKey searchKey = getSearchKey(dataFilter);
         StructuredDataFilterContext structuredDataFilterContext = contexts.get(searchKey);
         return structuredDataFilterContext.getJoin();
      }

      private void setContext(AbstractDataFilter filter, StructuredDataFilterContext filterContext)
      {
         DataAttributeKey searchKey = getSearchKey(filter);
         contexts.put(searchKey, filterContext);
      }

      private boolean contains(AbstractDataFilter filter)
      {
         DataAttributeKey searchKey = getSearchKey(filter);
         return contexts.containsKey(searchKey);
      }

      private DataAttributeKey getSearchKey(AbstractDataFilter filter)
      {
         return new DataAttributeKey(filter, false);
      }
   }
}