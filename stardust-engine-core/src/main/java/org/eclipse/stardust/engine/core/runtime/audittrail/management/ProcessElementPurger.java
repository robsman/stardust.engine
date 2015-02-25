/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;

/**
 * @author jsaayman
 * @version $Revision: $
 */
public class ProcessElementPurger implements ProcessElementOperator
{
   private static final int PK_OID = 0;
   
   public static final String PURGE_BATCH_SIZE = "purgeBatchSize";

   private static final int DEFAULT_PURGE_BATCH_SIZE = 100;
   
   private static final Logger trace = LogManager.getLogger(ProcessElementPurger.class);

   @Override
   public int operate(Session session, Class partType, FieldRef fkPiPartField, Class piPartType,
         String piPartPkName, PredicateTerm predicate)
   {
      DeleteDescriptor delete = DeleteDescriptor.from(partType);

      delete.innerJoin(piPartType)
            .on(fkPiPartField, piPartPkName);

      return session.executeDelete(delete.where(predicate));
   }

   @Override
   public int operate(Session session, Class partType, PredicateTerm predicate)
   {
      DeleteDescriptor delete = DeleteDescriptor.from(partType).where(predicate);

      return session.executeDelete(delete);
   }

   public void operateOnLockTable(Session session, Class partType, PredicateTerm predicate,
         TypeDescriptor tdType)
   {
      DeleteDescriptor delete = DeleteDescriptor.fromLockTable(partType);

      String partOid = tdType.getPkFields()[PK_OID].getName();
      PredicateTerm lockRowsPredicate = Predicates.inList(delete.fieldRef(partOid),
            QueryDescriptor.from(partType).select(partOid).where(predicate));

      session.executeDelete(delete.where(lockRowsPredicate));
   }
  
   
   @Override
   public void operateOnLockTable(Session session, Class partType, FieldRef fkPiPartField,
         Class piPartType, String piPartPkName, PredicateTerm predicate,
         TypeDescriptor tdType)
   {

      String partOid = tdType.getPkFields()[PK_OID].getName();

      QueryDescriptor lckSubselect = QueryDescriptor.from(partType).select(partOid);

      lckSubselect.innerJoin(piPartType).on(fkPiPartField, piPartPkName);

      DeleteDescriptor delete = DeleteDescriptor.fromLockTable(partType);
      delete.where(Predicates.inList(delete.fieldRef(partOid),
            lckSubselect.where(predicate)));

      session.executeDelete(delete);
   }
   
   
   
   @Override
   public void visitDataClusterValues(Session session, DataCluster dCluster, Collection piOids)
   {
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

   public int getStatementBatchSize()
   {
      return Parameters.instance().getInteger(PURGE_BATCH_SIZE,
            DEFAULT_PURGE_BATCH_SIZE);
   }
   
   @Override
   public void finishVisit()
   {
   }

}
