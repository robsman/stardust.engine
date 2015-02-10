/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;

/**
 * @author jsaayman
 * @version $Revision: $
 */
public class ProcessElementPurger implements ProcessElementOperator
{
   private static final int PK_OID = 0;
   
   public static final String PURGE_BATCH_SIZE = "purgeBatchSize";

   private static final int DEFAULT_PURGE_BATCH_SIZE = 100;

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
