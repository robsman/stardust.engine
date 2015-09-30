/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryUtils;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

public abstract class AbstractBenchmarkStatisticsRetriever
{
   protected static abstract class RowProcessor
   {
      abstract void processRow(ResultSet resultSet) throws SQLException;
   }

   protected static final Predicate defaultFilterPredicate = new Predicate()
   {
      @Override
      public boolean accept(Object o)
      {
         return true;
      }
   };

   protected static FieldRef createStructuredDataJoins(boolean outerJoin, String fieldName,
         IData data, QueryDescriptor queryDescriptor, Join dataValueJoin, List<PredicateTerm> predicates, String prefix, Object value)
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

   private static FieldRef getFieldRef(IData data, String name, Join sdvJoin)
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

   protected static long fetchValues(QueryDescriptor queryDescriptor, Query query, RowProcessor processor)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ResultSet resultSet = session.executeQuery(queryDescriptor, QueryUtils.getTimeOut(query));
      SubsetPolicy subsetPolicy = QueryUtils.getSubset(query);
      try
      {
         long startFrom = subsetPolicy.getSkippedEntries();
         long maxSize = subsetPolicy.getMaxSize();
         boolean isEvaluatingTotalCount = subsetPolicy.isEvaluatingTotalCount();

         long skipped = 0;
         long count = 0;
         long total = 0;
         while (resultSet.next())
         {
            total++;
            if (skipped < startFrom)
            {
               skipped++;
            }
            else
            {
               if (count < maxSize)
               {
                  count++;
                  processor.processRow(resultSet);
               }
               else
               {
                  if (!isEvaluatingTotalCount)
                  {
                     break;
                  }
               }
            }
         }
         return isEvaluatingTotalCount ? total : 0;
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
}
