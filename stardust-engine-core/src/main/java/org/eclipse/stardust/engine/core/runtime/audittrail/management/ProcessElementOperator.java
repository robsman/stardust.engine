/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.util.Collection;

import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;

/**
 * @author jsaayman
 * @version $Revision: $
 */
public interface ProcessElementOperator
{

   public int operate(Session session, Class partType, FieldRef fkPiPartField, Class piPartType,
         String piPartPkName, PredicateTerm predicate);

   public int operate(Session session, Class partType, PredicateTerm predicate);
   
   public void operateOnLockTable(Session session, Class partType, PredicateTerm predicate,
         TypeDescriptor tdType);

   public void operateOnLockTable(Session session, Class partType, FieldRef fkPiPartField,
         Class piPartType, String piPartPkName, PredicateTerm predicate,
         TypeDescriptor tdType);
   
   public void finishVisit();
   
   public int getStatementBatchSize();

   public void visitDataClusterValues(Session session, DataCluster dCluster, Collection piOids);
}
