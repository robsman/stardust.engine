/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ResultSetIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;

/**
 * @author jsaayman
 * @version $Revision: $
 */
public class ProcessElementExporter implements ProcessElementOperator
{

   private final ByteArrayBlobBuilder blobBuilder;

   public ProcessElementExporter()
   {
      blobBuilder = new ByteArrayBlobBuilder();
      blobBuilder.init(null);
   }

   @Override
   public int operate(Session session, Class partType, FieldRef fkPiPartField,
         Class piPartType, String piPartPkName, PredicateTerm predicate)
   {
      if (partType == ProcessInstanceScopeBean.class) {
         return 0;
      }
      QueryDescriptor query = QueryDescriptor.from(partType, "part");

      query.innerJoin(piPartType).on(fkPiPartField, piPartPkName);
      query.where(predicate);
      List<Persistent> instances = findPersistents(session, query, partType);
      TypeDescriptor td = TypeDescriptor.get(partType);
      ProcessBlobWriter.writeInstances(blobBuilder, td, instances);
      return instances.size();
   }

   @Override
   public int operate(Session session, Class partType, PredicateTerm predicate)
   {
      if (partType == ProcessInstanceScopeBean.class) {
         return 0;
      }
      QueryDescriptor query = QueryDescriptor.from(partType).where(predicate);

      List<Persistent> instances = findPersistents(session, query, partType);
      TypeDescriptor td = TypeDescriptor.get(partType);
      ProcessBlobWriter.writeInstances(blobBuilder, td, instances);
      return instances.size();
   }

   private List<Persistent> findPersistents(Session session, QueryDescriptor query,
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
      blobBuilder.persistAndClose();
   }
   /**
    * @return
    */
   public byte[] getBlob()
   {
      return blobBuilder.getBlob();
   }

   public void operateOnLockTable(Session session, Class partType, PredicateTerm predicate,
         TypeDescriptor tdType)
   {
   }
  
   
   @Override
   public void operateOnLockTable(Session session, Class partType, FieldRef fkPiPartField,
         Class piPartType, String piPartPkName, PredicateTerm predicate,
         TypeDescriptor tdType)
   {
   }
   
}
