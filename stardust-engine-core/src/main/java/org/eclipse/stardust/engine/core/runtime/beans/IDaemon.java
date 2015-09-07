/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Collection;

import org.eclipse.stardust.common.StringKey;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface IDaemon
{
   public static final ExecutionResult WORK_CANCELLED = ExecutionResult.ER_WORK_CANCELLED;
   public static final ExecutionResult WORK_DONE = ExecutionResult.ER_WORK_DONE;
   public static final ExecutionResult WORK_PENDING = ExecutionResult.ER_WORK_PENDING;

   /**
    * Performs the actual daemon work, most probably in a separate TX.
    * <p />
    * To keep control over the maximum workload per TX, the daemon implementation must
    * consider the passed <code>batchSize</code> parameter and constrain the amount of
    * work done during one invocation.
    *
    * @param batchSize A constraint on the maximum number of work to be performed during
    *        one invocation. While it is dependent on the nature of the daemon what
    *        actually defines the work done, typically this constrains the number of
    *        events processed synchronously or the number of runtime items affected.
    *
    * @return A status indicating the outcome.
    */
   public ExecutionResult execute(long batchSize);

   // @todo (france, ub): usage of getType is dubious (not 1-1)
   String getType();

   long getDefaultPeriodicity();

   DaemonExecutionLog getExecutionLog();   
   
   public static final class ExecutionResult extends StringKey
   {
      private static final long serialVersionUID = 1;

      public static final ExecutionResult ER_WORK_CANCELLED = new ExecutionResult(
            "cancelled", "Work cancelled.");
      public static final ExecutionResult ER_WORK_DONE = new ExecutionResult(
            "done", "Work done.");
      public static final ExecutionResult ER_WORK_PENDING = new ExecutionResult(
            "pending", "Work pending.");

      private ExecutionResult(String id, String name)
      {
         super(id, name);
      }
   }

   public static interface Factory
   {
      Collection<IDaemon> getDaemons();
   }
}