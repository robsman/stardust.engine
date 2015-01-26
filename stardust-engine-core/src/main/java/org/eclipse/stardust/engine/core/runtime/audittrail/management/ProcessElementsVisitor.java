/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

/**
 * @author jsaayman
 * 
 */
public class ProcessElementsVisitor
{
   private static final Logger trace = LogManager.getLogger(ProcessElementsVisitor.class);

   private static final String STMT_BATCH_SIZE = KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE;

   private static final int DEFAULT_STATEMENT_BATCH_SIZE = 100;

   private final ProcessElementOperator operator;

   @SuppressWarnings("unused")
   private ProcessElementsVisitor()
   {
      this.operator = null;
   }

   public ProcessElementsVisitor(ProcessElementOperator operator)
   {
      if (operator == null)
      {
         throw new IllegalStateException("ProcessElementOperator is required");
      }
      this.operator = operator;
   }

   public int visitProcessInstances(List<Long> piOids, Session session)
   {
      try
      {
         if (piOids.isEmpty())
         {
            return 0;
         }

         int count = visitPiParts(piOids, ProcessInstanceBean.class,
               ProcessInstanceBean.FR__OID, null, session);
         
         visitPiParts(piOids, TransitionTokenBean.class,
               TransitionTokenBean.FR__PROCESS_INSTANCE, session);

         visitPiParts(piOids, TransitionInstanceBean.class,
               TransitionInstanceBean.FR__PROCESS_INSTANCE, session);

         visitAiParts(piOids, LogEntryBean.class, LogEntryBean.FR__ACTIVITY_INSTANCE,
               session);

         visitAiParts(piOids, ActivityInstanceLogBean.class,
               ActivityInstanceLogBean.FR__ACTIVITY_INSTANCE, session);

         visitAiParts(piOids, ActivityInstanceHistoryBean.class,
               ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE, session);

         visitAiParts(piOids, EventBindingBean.class, EventBindingBean.FR__OBJECT_OID,
               Predicates.isEqual(EventBindingBean.FR__TYPE, Event.ACTIVITY_INSTANCE),
               session);

         visitAiParts(piOids, ActivityInstanceProperty.class,
               ActivityInstanceProperty.FR__OBJECT_OID, session);

         visitPiParts(piOids, ActivityInstanceBean.class,
               ActivityInstanceBean.FR__PROCESS_INSTANCE, session);

         // TODO (ab) SPI
         List<Long> structuredDataOids = findAllStructuredDataOids(
               SecurityProperties.getPartitionOid(), session);
         if (structuredDataOids.size() != 0)
         {
            visit2ndLevelPiParts(
                  piOids,
                  LargeStringHolder.class,
                  LargeStringHolder.FR__OBJECTID,
                  StructuredDataValueBean.class,
                  StructuredDataValueBean.FR__PROCESS_INSTANCE,
                  Predicates.isEqual(LargeStringHolder.FR__DATA_TYPE,
                        TypeDescriptor.getTableName(StructuredDataValueBean.class)),
                  session);

            visitPiParts(piOids, StructuredDataValueBean.class,
                  StructuredDataValueBean.FR__PROCESS_INSTANCE, session);

            visit2ndLevelPiParts(piOids, ClobDataBean.class, ClobDataBean.FR__OID,
                  DataValueBean.class, DataValueBean.FIELD__NUMBER_VALUE,
                  DataValueBean.FR__PROCESS_INSTANCE,
                  Predicates.inList(DataValueBean.FR__DATA, structuredDataOids), session);
         }

         visitDvParts(
               piOids,
               LargeStringHolder.class,
               LargeStringHolder.FR__OBJECTID,
               Predicates.isEqual(LargeStringHolder.FR__DATA_TYPE,
                     TypeDescriptor.getTableName(DataValueBean.class)), session);

         visitPiParts(piOids, DataValueBean.class, DataValueBean.FR__PROCESS_INSTANCE,
               session);

         visitPiParts(piOids, LogEntryBean.class, LogEntryBean.FR__PROCESS_INSTANCE,
               session);

         visitPiParts(piOids, EventBindingBean.class, EventBindingBean.FR__OBJECT_OID,
               Predicates.isEqual(EventBindingBean.FR__TYPE, Event.PROCESS_INSTANCE),
               session);

         visit2ndLevelPiParts(piOids,
               LargeStringHolder.class,
               LargeStringHolder.FR__OBJECTID, //
               ProcessInstanceProperty.class,
               ProcessInstanceProperty.FR__OBJECT_OID, //
               isEqual(LargeStringHolder.FR__DATA_TYPE,
                     ProcessInstanceProperty.TABLE_NAME), session);
         visitPiParts(piOids, ProcessInstanceProperty.class,
               ProcessInstanceProperty.FR__OBJECT_OID, session);

         visitPiParts(piOids, ProcessInstanceLinkBean.class,
               ProcessInstanceLinkBean.FR__LINKED_PROCESS_INSTANCE, session);
         visitPiParts(piOids, ProcessInstanceLinkBean.class,
               ProcessInstanceLinkBean.FR__PROCESS_INSTANCE, session);

         visitPiParts(piOids, ProcessInstanceHierarchyBean.class,
               ProcessInstanceHierarchyBean.FR__SUB_PROCESS_INSTANCE, session);
         visitPiParts(piOids, ProcessInstanceHierarchyBean.class,
               ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, session);

         visitPiParts(piOids, ProcessInstanceScopeBean.class,
               ProcessInstanceScopeBean.FR__PROCESS_INSTANCE, session);

         visitPiParts(piOids, WorkItemBean.class, WorkItemBean.FR__PROCESS_INSTANCE,
               session);

         visitDataClusterValues(piOids, session);

         return count;
      }
      finally
      {
         operator.finishVisit();
      }
   }

   private void visitDataClusterValues(List piOids, Session session)
   {
      if (piOids.isEmpty())
      {
         // if no PI oids are specified then deletion of data cluster values will be
         // skipped.
         return;
      }

      // finally deleting rows from data clusters
      final DataCluster[] dClusters = RuntimeSetup.instance().getDataClusterSetup();

      for (int idx = 0; idx < dClusters.length; ++idx)
      {
         final DataCluster dCluster = dClusters[idx];

         Statement stmt = null;
         try
         {
            stmt = session.getConnection().createStatement();
            StringBuffer buffer = new StringBuffer(100 + piOids.size() * 10);
            buffer.append("DELETE FROM ").append(dCluster.getQualifiedTableName())
                  .append(" WHERE ").append(dCluster.getProcessInstanceColumn())
                  .append(" IN (").append(StringUtils.join(piOids.iterator(), ", "))
                  .append(")");
            if (trace.isDebugEnabled())
            {
               trace.debug(buffer);
            }
            stmt.executeUpdate(buffer.toString());
         }
         catch (SQLException e)
         {
            throw new PublicException(
                  BpmRuntimeError.JDBC_FAILED_DELETING_ENRIES_FROM_DATA_CLUSTER_TABLE.raise(dCluster
                        .getTableName()), e);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   private void visitDvParts(List<Long> piOids, Class partType, FieldRef fkDvField,
         PredicateTerm restriction, Session session)
   {
      visit2ndLevelPiParts(piOids, partType, fkDvField, DataValueBean.class,
            DataValueBean.FR__PROCESS_INSTANCE, restriction, session);
   }

   private static List<Long> findAllStructuredDataOids(short partitionOid, Session session)
   {
      QueryDescriptor structDataQuery = QueryDescriptor.from(StructuredDataBean.class) //
            .select(StructuredDataBean.FR__DATA) //
            .groupBy(StructuredDataBean.FR__DATA) //
            .where(Predicates.isEqual(ModelPersistorBean.FR__PARTITION, partitionOid));

      structDataQuery.innerJoin(ModelPersistorBean.class) //
            .on(StructuredDataBean.FR__MODEL, ModelPersistorBean.FIELD__OID);

      List<Long> dataOids = CollectionUtils.newArrayList();

      ResultSet dataRtOids = session.executeQuery(structDataQuery);
      try
      {
         while (dataRtOids.next())
         {
            dataOids.add(dataRtOids.getLong(1));
         }
      }
      catch (SQLException sqle)
      {
         throw new PublicException(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED.raise(), sqle);
      }
      finally
      {
         closeResultSet(dataRtOids);
      }

      return dataOids;
   }

   private int visitPiParts(List<Long> piOids, Class partType, FieldRef fkPiField,
         Session session)
   {
      return visitPiParts(piOids, partType, fkPiField, null, session);
   }

   private int visitPiParts(List<Long> piOids, Class partType, FieldRef fkPiField,
         PredicateTerm restriction, Session session)
   {
      int processedItems = 0;
      int batchSize = getStatementBatchSize();

      for (Iterator<List<Long>> iterator = getChunkIterator(piOids, batchSize); iterator
            .hasNext();)
      {
         List piOidsBatch = (List) iterator.next();

         PredicateTerm piPredicate = Predicates.inList(fkPiField, piOidsBatch);

         PredicateTerm predicate = (null != restriction) ? Predicates.andTerm(
               piPredicate, restriction) : piPredicate;

         // delete lock rows

         TypeDescriptor tdType = TypeDescriptor.get(partType);
         if (session.isUsingLockTables() && tdType.isDistinctLockTableName())
         {
            Assert.condition(1 == tdType.getPkFields().length,
                  "Lock-tables are not supported for types with compound PKs.");

            operator.operateOnLockTable(session, partType, predicate, tdType);
         }

         // delete data rows

         processedItems += operator.operate(session, partType, predicate);
      }

      return processedItems;
   }

   private void visitAiParts(List<Long> piOids, Class partType, FieldRef fkAiField,
         Session session)
   {
      visitAiParts(piOids, partType, fkAiField, null, session);
   }

   private void visitAiParts(List<Long> piOids, Class partType, FieldRef fkAiField,
         PredicateTerm restriction, Session session)
   {
      visit2ndLevelPiParts(piOids, partType, fkAiField, ActivityInstanceBean.class,
            ActivityInstanceBean.FR__PROCESS_INSTANCE, restriction, session);
   }

   private int visit2ndLevelPiParts(List<Long> piOids, Class partType,
         FieldRef fkPiPartField, Class piPartType, FieldRef piOidField,
         PredicateTerm restriction, Session session)
   {
      TypeDescriptor tdPiPart = TypeDescriptor.get(piPartType);
      return visit2ndLevelPiParts(piOids, partType, fkPiPartField, piPartType,
            tdPiPart.getPkFields()[0].getName(), piOidField, restriction, session);
   }

   private int visit2ndLevelPiParts(List<Long> piOids, Class partType,
         FieldRef fkPiPartField, Class piPartType, String piPartPkName,
         FieldRef piOidField, PredicateTerm restriction, Session session)
   {
      int processedItems = 0;
      int batchSize = getStatementBatchSize();

      for (Iterator<List<Long>> iterator = getChunkIterator(piOids, batchSize); iterator
            .hasNext();)
      {
         List<Long> piOidsBatch = iterator.next();

         PredicateTerm predicate = Predicates.andTerm(Predicates.inList(piOidField,
               piOidsBatch), (null != restriction) ? restriction : Predicates.TRUE);

         // delete lock rows
         TypeDescriptor tdType = TypeDescriptor.get(partType);
         if (session.isUsingLockTables() && tdType.isDistinctLockTableName())
         {
            Assert.condition(1 == tdType.getPkFields().length,
                  "Lock-tables are not supported for types with compound PKs.");

            operator.operateOnLockTable(session, partType, fkPiPartField, piPartType,
                  piPartPkName, predicate, tdType);
         }

         processedItems += operator.operate(session, partType, fkPiPartField, piPartType,
               piPartPkName, predicate);
      }

      return processedItems;
   }

   /**
    * @return
    */
   private static int getStatementBatchSize()
   {
      return Parameters.instance().getInteger(STMT_BATCH_SIZE,
            DEFAULT_STATEMENT_BATCH_SIZE);
   }

   private static <E> Iterator<List<E>> getChunkIterator(List<E> list, int chunkSize)
   {
      return new ListChunkIterator<E>(list, chunkSize);
   }

   private static final class ListChunkIterator<E> implements Iterator<List<E>>
   {
      private final int chunkSize;

      private final ArrayList<E> list;

      private int offset = 0;

      public ListChunkIterator(List<E> list, int chunkSize)
      {
         super();

         if (chunkSize <= 0)
         {
            throw new IllegalArgumentException(
                  "Argument chunkSize must be greater than 0.");
         }

         if (null == list)
         {
            throw new IllegalArgumentException("Argument list must not be null.");
         }

         this.chunkSize = chunkSize;
         this.list = new ArrayList<E>(list);
      }

      public boolean hasNext()
      {
         return offset < list.size();
      }

      public List<E> next()
      {
         ArrayList<E> nextListChunk = new ArrayList<E>(chunkSize);

         int upperLimit = Math.min(list.size() - offset, chunkSize);
         for (int idx = 0; idx < upperLimit; ++idx)
         {
            nextListChunk.add(list.get(offset + idx));
         }

         offset += upperLimit;

         return nextListChunk;
      }

      public void remove()
      {
         throw new UnsupportedOperationException();
      }
   }

}
