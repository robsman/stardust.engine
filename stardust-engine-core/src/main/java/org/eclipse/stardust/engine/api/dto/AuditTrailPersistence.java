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
 * process instance is running in.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public enum AuditTrailPersistence {

   /**
    * <p>
    * The <i>Audit Trail Persistence</i> mode if nothing is defined explicitly, meaning
    * that it falls back to the standard behavior: If it's an asynchronous process instance,
    * it will inherit the {@link AuditTrailPersistence} of the originating process instance,
    * otherwise it will be {@link #IMMEDIATE} effectively.
    * </p>
    */
   ENGINE_DEFAULT
   {
      @Override
      public void assertThatStateChangeIsAllowedTo(final AuditTrailPersistence to, final boolean notYetWrittenToTheDb)
      {
         if (notYetWrittenToTheDb)
         {
            /* all state changes are allowed */
            return;
         }

         if (to == ENGINE_DEFAULT)
         {
            /* allowed */
            return;
         }

         throw new IllegalStateException(createStateChangeForbiddenMessage(this, to));
      }
   },

   /**
    * <p>
    * Running in this mode the process instance will never be written to the Audit Trail.
    * </p>
    */
   TRANSIENT
   {
      @Override
      public void assertThatStateChangeIsAllowedTo(final AuditTrailPersistence to, final boolean ignored)
      {
         if (to == TRANSIENT || to == DEFERRED || to == IMMEDIATE)
         {
            /* allowed */
            return;
         }

         throw new IllegalStateException(createStateChangeForbiddenMessage(this, to));
      }
   },

   /**
    * <p>
    * Running in this mode the process instance will be written to the Audit Trail
    * only after the process instance has been completed.
    * </p>
    */
   DEFERRED
   {
      @Override
      public void assertThatStateChangeIsAllowedTo(final AuditTrailPersistence to, final boolean ignored)
      {
         if (to == TRANSIENT || to == DEFERRED || to == IMMEDIATE)
         {
            /* allowed */
            return;
         }

         throw new IllegalStateException(createStateChangeForbiddenMessage(this, to));
      }
   },

   /**
    * <p>
    * Running in this mode the process instance will be written to the Audit Trail
    * after every single transaction (standard behavior).
    * </p>
    */
   IMMEDIATE
   {
      @Override
      public void assertThatStateChangeIsAllowedTo(final AuditTrailPersistence to, final boolean ignored)
      {
         if (to == IMMEDIATE)
         {
            /* allowed */
            return;
         }

         throw new IllegalStateException(createStateChangeForbiddenMessage(this, to));
      }
   };

   /**
    * <p>
    * Asserts that the specified state change is allowed, i.e. if it's allowed, the method silently returns,
    * whereas it throws an exception if the state change is forbidden.
    * </p>
    *
    * @param to the state for which it should be checked whether a state change from the current state is allowed
    * @param notYetWrittenToTheDb whether the corresponding process instance has not been written to the database yet
    *
    * @throws IllegalStateException if the state change is not allowed
    */
   public abstract void assertThatStateChangeIsAllowedTo(final AuditTrailPersistence to, final boolean notYetWrittenToTheDb);

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

   private static String createStateChangeForbiddenMessage(final AuditTrailPersistence from, final AuditTrailPersistence to)
   {
      return "State change '" + from.name() + "' -> '" + to.name() + "' is forbidden.";
   }
}
