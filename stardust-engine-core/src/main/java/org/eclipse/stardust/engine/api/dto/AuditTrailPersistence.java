/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.api.dto;

/**
 * <p>
 * Encapsulates the <i>Audit Trail Persistence</i> mode the
 * engine is running in. 
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public enum AuditTrailPersistence {
   
   /**
    * <p>
    * The <i>Audit Trail Persistence</i> mode if nothing is defined explicitly:
    * meaning that it falls back to the standard behavior (see {@link #IMMEDIATE}).
    * </p>
    */
   ENGINE_DEFAULT,
   
   /**
    * <p>
    * Running in this mode the process instance will never be written to the Audit Trail.
    * </p>
    */
   TRANSIENT,
   
   /**
    * <p>
    * Running in this mode the process instance will be written to the Audit Trail
    * only after the process instance has been completed.
    * </p>
    */
   DEFERRED,
   
   /**
    * <p>
    * Running in this mode the process instance will be written to the Audit Trail
    * after every single transaction (standard behavior).
    * </p>
    */
   IMMEDIATE;
   
   /**
    * <p>
    * Determines whether the given <i>Audit Trail Persistence</i> mode mandates transient
    * process instance execution or not.
    * </p>
    * 
    * @param auditTrailPersistence the audit trail persistence to be checked
    * @return whether the execution is transient or not
    */
   public static boolean isTransientExecution(final AuditTrailPersistence auditTrailPersistence)
   {
      final boolean isTransient = auditTrailPersistence == AuditTrailPersistence.TRANSIENT;
      final boolean isDeferred = auditTrailPersistence == AuditTrailPersistence.DEFERRED;
      return isTransient || isDeferred;
   }
}
