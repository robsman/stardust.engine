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
package org.eclipse.stardust.engine.core.persistence.jdbc.transientpi;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public enum AuditTrailPersistence {
   /**
    * <p>
    * If it's a sub process instance it inherits the behavior
    * from the parent process; if it's a parent process it falls
    * back to the standard behavior (see {@link #IMMEDIATE})
    * </p>
    */
   ENGINE_DEFAULT,
   
   /**
    * <p>
    * The process instance will never be written to the Audit Trail
    * </p>
    */
   TRANSIENT,
   
   /**
    * <p>
    * The process instance will be written to the Audit Trail
    * only after the process instance has been completed
    * </p>
    * 
    * <p>
    * Overrides {@link TRANSIENT}
    * </p>
    */
   DEFERRED,
   
   /**
    * <p>
    * The process instance will be written to the Audit Trail
    * after every single transaction (standard behavior)
    * </p>
    * 
    * <p>
    * Overrides {@link #DEFERRED} and {@link #TRANSIENT}
    * </p>
    */
   IMMEDIATE;
   
   public static boolean isTransientExecution(final AuditTrailPersistence auditTrailPersistence)
   {
      final boolean isTransient = auditTrailPersistence == AuditTrailPersistence.TRANSIENT;
      final boolean isLatePersist = auditTrailPersistence == AuditTrailPersistence.DEFERRED;
      return isTransient || isLatePersist;
   }
}
