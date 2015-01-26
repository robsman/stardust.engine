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
package org.eclipse.stardust.engine.core.extensions.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.AbstractDataFilter;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.IJoinFactory;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;



/**
 * Default behavior for most of data types
 */
public class DefaultDataFilterExtension implements DataFilterExtension
{

   public PredicateTerm createPredicateTerm(Join dvJoin,
         AbstractDataFilter dataFilter,
         Map<Long, IData> dataMap, DataFilterExtensionContext dataFilterContext)
   {
      Serializable operand = dataFilter.getOperand();
      if (operand instanceof Enum)
      {
         for (IData data : dataMap.values())
         {
            if (JavaDataTypeUtils.isJavaEnumeration(data))
            {
               Class enumClass = JavaDataTypeUtils.getReferenceClass(data, true);
               if (enumClass != null && enumClass.isEnum() && enumClass.isInstance(operand))
               {
                  operand = ((Enum) operand).name();
                  break;
               }
            }
         }
      }
      return DataValueBean.matchDataInstancesPredicate(
            dvJoin, dataFilter.getOperator(),
            operand, dataFilter);
   }

   public void extendOrderCriteria(Join piJoin, Join pisJoin,
         OrderCriteria orderCriteria, DataOrder order, Map<Long, IData> dataMap,
         Map<String, Join> dataOrderJoins)
   {
      final String alias = "DVO" + (dataOrderJoins.size() + 1);
      Join dvJoin;
      if (null != pisJoin)
      {
         dvJoin = new Join(DataValueBean.class, alias)//
            .on(pisJoin.fieldRef(ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE),
                  DataValueBean.FIELD__PROCESS_INSTANCE);

         dvJoin.setDependency(pisJoin);
      }
      else if (null != piJoin)
      {
         dvJoin = new Join(DataValueBean.class, alias)//
            .on(piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE),
                  DataValueBean.FIELD__PROCESS_INSTANCE);

         dvJoin.setDependency(piJoin);
      }
      else
      {
         dvJoin = new Join(DataValueBean.class, alias)//
            .on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                  DataValueBean.FIELD__PROCESS_INSTANCE);
      }
      dvJoin.where(Predicates.inList(dvJoin.fieldRef(DataValueBean.FIELD__DATA),
            dataMap.keySet().iterator()));
      dvJoin.setRequired(false);

      boolean useNumericColumn = false;
      boolean useStringColumn = false;
      boolean useDoubleColumn = false;

      for (IData data: dataMap.values())
      {
         final int typeClassification = LargeStringHolderBigDataHandler
               .classifyTypeForSorting(data);
         useNumericColumn |= (BigData.NUMERIC_VALUE == typeClassification);
         useStringColumn |= (BigData.STRING_VALUE == typeClassification);
         useDoubleColumn |= (BigData.DOUBLE_VALUE == typeClassification);
      }

      if (useNumericColumn)
      {
         orderCriteria.add(dvJoin.fieldRef(DataValueBean.FIELD__NUMBER_VALUE),
               order.isAscending());
      }

      if (useStringColumn)
      {
         orderCriteria.add(dvJoin.fieldRef(DataValueBean.FIELD__STRING_VALUE),
               order.isAscending());
      }

      if (useDoubleColumn)
      {
         orderCriteria.add(dvJoin.fieldRef(DataValueBean.FIELD__DOUBLE_VALUE),
               order.isAscending());
      }

      dataOrderJoins.put(order.getDataID(), dvJoin);
   }

   public void appendDataIdTerm(AndTerm andTerm, Map<Long, IData> dataIds, Join dvJoin,
         AbstractDataFilter dataFilter)
   {
      if (dataIds.isEmpty())
      {
         andTerm.add(Predicates.isNull(dvJoin.fieldRef(DataValueBean.FIELD__DATA)));
      }
      else
      {
         andTerm.add(Predicates.inList(dvJoin.fieldRef(DataValueBean.FIELD__DATA),
               dataIds.keySet().iterator()));
      }
   }

   public Join createDvJoin(QueryDescriptor query, AbstractDataFilter dataFilter, int index, DataFilterExtensionContext dataFilterExtensionContext, boolean isAndTerm, IJoinFactory joinFactory)
   {
      if ( !StringUtils.isEmpty(dataFilter.getAttributeName()))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.QUERY_XPATH_ON_NON_STRUCT_DATA.raise(dataFilter
                     .getDataID()));
      }

      return joinFactory.createDataFilterJoins(dataFilter.getFilterMode(), index, DataValueBean.class, DataValueBean.FR__PROCESS_INSTANCE);
   }

   public List<FieldRef> getPrefetchSelectExtension(ITableDescriptor descriptor)
   {
      List<FieldRef> cols = CollectionUtils.newArrayList();

      cols.add(descriptor.fieldRef(DataValueBean.FIELD__TYPE_KEY));
      cols.add(descriptor.fieldRef(DataValueBean.FIELD__STRING_VALUE));
      cols.add(descriptor.fieldRef(DataValueBean.FIELD__NUMBER_VALUE));

      return cols;
   }

}
