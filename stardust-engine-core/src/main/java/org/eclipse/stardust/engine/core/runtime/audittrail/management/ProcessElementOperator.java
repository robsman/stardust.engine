/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;

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

}
