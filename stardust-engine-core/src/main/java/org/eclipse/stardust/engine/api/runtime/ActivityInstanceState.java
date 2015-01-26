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

import org.eclipse.stardust.common.IntKey;

/**
 * A representation of the state of an activity instance.
 *
 * This class also provides human readable values for the activity instance states.
 *
 * An activity instance changes its state in the course of processing in a well defined
 * manner:
 * <ol>
 * <li>It is created with state <code>Created</code>.
 * <li>If the corresponding activity is interactive the state is set to <code>Suspended</code>
 *     as long as the activity instance is in somebody's worklist
 * <li>While the activity instance is executed, it is in state <code>Application</code>
 * <li>If the activity is asynchronous and is waiting for awakening, it is in state
 * <code>Hibernated</code>
 * <li>After successful completion it is in state <code>Completed</code>.
 * </ol>
 * For exceptional situations there are two special states:
 * <ul>
 * <li><code>Interrupted</code> as an intermediate state if there were system level
 * exception during execution of the activity instance
 * <li><code>Aborted</code> as a termination state for non successful completion.
 * </ul>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ActivityInstanceState extends IntKey
{
   private static final long serialVersionUID = -2106226646197082434L;

   /**
    * The activity instance has just been created.
    */
   public static final int CREATED = 0;
   /**
    * The activity instance is currently performed either interactively or
    * automatically.
    */
   public static final int APPLICATION = 1;
   /**
    * The activity instance has been completed.
    */
   public static final int COMPLETED = 2;
   /**
    * Completion of the activity instance has caused exceptions.
    */
   public static final int INTERRUPTED = 4;
   /**
    * The (interactive) activity instance has been suspended to the worklist of
    * a user, a role or an organization.
    */
   public static final int SUSPENDED = 5;
   /**
    * The activity instance has been aborted directly by a user or by explicitely
    * aborting the process instance.
    */
   public static final int ABORTED = 6;
   /**
    * The activity instance has an asynchronous receiving part and is hibernated
    * to wait for an awakening event.
    */
   public static final int HIBERNATED = 7;
   /**
    * The activity instance is in progress of being aborted directly by a user.
    */
   public static final int ABORTING = 8;

   public static final ActivityInstanceState Created =
         new ActivityInstanceState(CREATED, "Created");
   public static final ActivityInstanceState Application =
         new ActivityInstanceState(APPLICATION, "Application");
   public static final ActivityInstanceState Completed =
         new ActivityInstanceState(COMPLETED, "Completed");
   public static final ActivityInstanceState Interrupted =
         new ActivityInstanceState(INTERRUPTED, "Interrupted");
   public static final ActivityInstanceState Suspended =
         new ActivityInstanceState(SUSPENDED, "Suspended");
   public static final ActivityInstanceState Aborted =
         new ActivityInstanceState(ABORTED, "Aborted");
   public static final ActivityInstanceState Hibernated =
      new ActivityInstanceState(HIBERNATED, "Hibernated");
   public static final ActivityInstanceState Aborting =
      new ActivityInstanceState(ABORTING, "Aborting");

   private static final ActivityInstanceState[] KEYS = new ActivityInstanceState[]
         {
            Created,
            Application,
            Completed,
            null,
            Interrupted,
            Suspended,
            Aborted,
            Hibernated,
            Aborting
         };
   
   private ActivityInstanceState(int value, String name)
   {
      super(value, name);
   }

   /**
    * Gets the name of the ActivityInstanceState corresponding to the given code.
    *
    * @param value one of the ActivityInstanceState codes.
    *
    * @return the name of the corresponding ActivityInstanceState.
    */
   // todo: (france, fh) remove / inline.
   public static String getString(int value)
   {
      ActivityInstanceState state = getState(value);

      return state != null ? state.toString() : "Unknown (" + value + ")";
   }

   /**
    * Factory method to get the ActivityInstanceState corresponding to the given code.
    *
    * @param value one of the ActivityInstanceState codes.
    *
    * @return one of the predefined ActivityInstanceStates or null if it's an invalid code.
    */
   public static ActivityInstanceState getState(int value)
   {
      if ((value < KEYS.length) && (null != KEYS[value]))
      {
         return KEYS[value];
      }
      
      return (ActivityInstanceState) getKey(ActivityInstanceState.class, value);
   }
}
