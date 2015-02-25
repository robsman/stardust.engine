/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.archive.ExportResult;
import org.eclipse.stardust.engine.core.persistence.jdbc.ResultSetIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

/**
 * @author jsaayman
 * @version $Revision: $
 */
public class ProcessElementExporter implements ProcessElementOperator
{

   public static final String EXPORT_BATCH_SIZE = "exportBatchSize";

   private static final int DEFAULT_EXPORT_BATCH_SIZE = 5;

   
   private final ExportResult exportResult;


   public ProcessElementExporter(ExportResult exportResult)
   {
      this.exportResult = exportResult;
   }

   @Override
   public int operate(Session session, Class partType, FieldRef fkPiPartField,
         Class piPartType, String piPartPkName, PredicateTerm predicate)
   {
      QueryDescriptor query = QueryDescriptor.from(partType, "part");

      query.innerJoin(piPartType).on(fkPiPartField, piPartPkName);
      query.where(predicate);
      List<Persistent> instances = exportPersistents(session, query, partType);
      return instances.size();
   }

   @Override
   public int operate(Session session, Class partType, PredicateTerm predicate)
   {
      if (partType == ProcessInstanceScopeBean.class)
      {
         return 0;
      }
      QueryDescriptor query = QueryDescriptor.from(partType).where(predicate);
      List<Persistent> instances = exportPersistents(session, query, partType);
      return instances.size();
   }

   private List<Persistent> exportPersistents(Session session, QueryDescriptor query,
         Class partType)
   {
      ResultSet resultSet = null;
      List<Persistent> results = new ArrayList<Persistent>();
      try
      {
         resultSet = session.executeQuery(query, Session.NO_TIMEOUT);

         ResultIterator iterator = new ResultSetIterator(session, partType, true,
               resultSet, 0, -1, null, false);
         while (iterator.hasNext())
         {
            Persistent p = (Persistent) iterator.next();
            results.add(p);
            long processInstanceOid = -1;
            if (partType == ProcessInstanceBean.class)
            {
               exportResult.addResult(((ProcessInstanceBean)p));
            }
            else if (!(p instanceof ProcessInstanceScopeBean))
            {
               if (p instanceof IProcessInstanceAware)
               {
                  processInstanceOid = ((IProcessInstanceAware) p).getProcessInstance()
                        .getOID();
               }
               else if (p instanceof IActivityInstanceAware)
               {
                  processInstanceOid = ((IActivityInstanceAware) p).getActivityInstance()
                        .getProcessInstance().getOID();
               }
               else if (p instanceof LargeStringHolder)
               {
                  LargeStringHolder str = ((LargeStringHolder) p);
                  Long structureDataOid =  str.getObjectID();
                  if (StructuredDataValueBean.TABLE_NAME.equals(str.getDataType()))
                  {
                     StructuredDataValueBean dataBean = (StructuredDataValueBean)session.findByOID(StructuredDataValueBean.class, structureDataOid);
                     processInstanceOid = dataBean.getProcessInstance().getOID();
                  }
                  else if (DataValueBean.TABLE_NAME.equals(str.getDataType()))
                  {
                     DataValueBean dataBean = (DataValueBean)session.findByOID(DataValueBean.class, structureDataOid);
                     processInstanceOid = dataBean.getProcessInstance().getOID();
                  }
                  else
                  {
                     throw new IllegalStateException(
                           "Can't determine related process instance. LargeStringHolder type is :"
                                 + str.getDataType());
                  }
               }
               else
               {
                  throw new IllegalStateException(
                        "Can't determine related process instance. Not a clob, IProcessInstanceAware or IActivityInstanceAware:"
                              + p.getClass().getName());
               }
   
               if (processInstanceOid == -1)
               {
                  throw new IllegalStateException(
                        "Can't determine related process instance." + p.getClass().getName());
               }
               exportResult.addResult(p, processInstanceOid);
            }
         }

      }
      finally
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils
               .closeResultSet(resultSet);
      }
      return results;
   }

   
   @Override
   public void finishVisit()
   {
      exportResult.close();
   }

   public void operateOnLockTable(Session session, Class partType,
         PredicateTerm predicate, TypeDescriptor tdType)
   {
      // TODO what about this
   }

   @Override
   public void operateOnLockTable(Session session, Class partType,
         FieldRef fkPiPartField, Class piPartType, String piPartPkName,
         PredicateTerm predicate, TypeDescriptor tdType)
   {
      // TODO what about this
   }

   @Override
   public void visitDataClusterValues(Session session, DataCluster dCluster, Collection piOids)
   {
      // TODO what about this
   }

   public int getStatementBatchSize()
   {
      return Parameters.instance().getInteger(EXPORT_BATCH_SIZE,
            DEFAULT_EXPORT_BATCH_SIZE);
   }
}
