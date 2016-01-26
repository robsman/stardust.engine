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
package org.eclipse.stardust.engine.api.runtime;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.IntKey;

/**
 * A representation of the state of an process instance.
 *
 * This class also provides human readable values for the process instance states.
 *
 * An process instance changes its state in the course of processing as follows:
 * <ol>
 * <li>It is created with state <code>Created</code>.
 * <li>While the process instance is executed, it is in state <code>Active</code>
 * <li>After successful completion it is in state <code>Completed</code>.
 * </ol>
 * For exceptional situations there are two special states:
 * <ul>
 * <li><code>Interrupted</code> as an intermediate state if there were system level
 * exception during execution of one of the activity instances.
 * <li><code>Aborted</code> as a termination state for non successful completion.
 * </ul>
 *
 * @author ubirkemeyer
 */
public class ProcessInstanceState extends IntKey
{
   private static final long serialVersionUID = -4498255161862762726L;

   /**
    * The process instance has just been created.
    */
   public static final int CREATED = -1;
   /**
    * The process instance is running.
    */
   public static final int ACTIVE = 0;
   /**
    * The process instance has been aborted.
    */
   public static final int ABORTED = 1;
   /**
    * The process instance has been completed.
    */
   public static final int COMPLETED = 2;
   /**
    * The process instance is interrupted - one of it's activity instances is interrupted.
    */
   public static final int INTERRUPTED = 3;
   /**
    * The process instance is in progress of being aborted directly by a user.
    */
   public static final int ABORTING = 4;
   /**
    * The process instance is in progress of being halted directly by a user.
    */
   public static final int HALTING = 5;
   /**
    * The process instance has been halted.
    */
   public static final int HALTED = 6;

   public static final ProcessInstanceState Created =
         new ProcessInstanceState(CREATED, "Created");
   public static final ProcessInstanceState Active =
         new ProcessInstanceState(ACTIVE, "Active");
   public static final ProcessInstanceState Aborted =
         new ProcessInstanceState(ABORTED, "Aborted");
   public static final ProcessInstanceState Completed =
         new ProcessInstanceState(COMPLETED, "Completed");
   public static final ProcessInstanceState Interrupted =
         new ProcessInstanceState(INTERRUPTED, "Interrupted");
   public static final ProcessInstanceState Aborting =
         new ProcessInstanceState(ABORTING, "Aborting");
   public static final ProcessInstanceState Halting =
         new ProcessInstanceState(HALTING, "Halting");
   public static final ProcessInstanceState Halted =
         new ProcessInstanceState(HALTED, "Halted");

   private static final ProcessInstanceState[] KEYS = new ProcessInstanceState[]
         {
            Active,
            Aborted,
            Completed,
            Interrupted,
            Aborting,
            Halting,
            Halted
         };

   /**
    *
    * @return returns all possible {@link ProcessInstanceState}
    */
   public static Set<ProcessInstanceState> getAllStates()
   {
      Set<ProcessInstanceState> allStates = new HashSet<ProcessInstanceState>();
      for(ProcessInstanceState key: KEYS)
      {
         allStates.add(key);
      }

      return allStates;
   }

   /**
    * Factory method to get the ProcessInstanceState corresponding to the given code.
    *
    * @param value one of the ProcessInstanceState codes.
    *
    * @return one of the predefined ProcessInstanceStates or null if it's an invalid code.
    */
   public static ProcessInstanceState getState(int value)
   {
      if (-1 == value)
      {
         return Created;
      }
      else if (value < KEYS.length)
      {
         return KEYS[value];
      }

      return (ProcessInstanceState) getKey(ProcessInstanceState.class, value);
   }

   private ProcessInstanceState(int value, String name)
   {
      super(value, name);
   }
}
