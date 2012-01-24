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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;

/**
 * @author rsauer
 * @version $Revision$
 */
public final class ActivityStateFilter implements FilterCriterion
{
   /**
    * Predefined filter matching activity instance being in states other than
    * {@link ActivityInstanceState#Aborted} and
    * {@link ActivityInstanceState#Completed}.
    *
    * @see ActivityInstanceState#Aborted
    * @see ActivityInstanceState#Completed
    */
   public static final ActivityStateFilter ALIVE =
         new ActivityStateFilter(false,
               new ActivityInstanceState[]{
                  ActivityInstanceState.Aborted, ActivityInstanceState.Completed
               }
         );
   /**
    * Predefined filter matching activity instance being in states
    * {@link ActivityInstanceState#Application},
    * {@link ActivityInstanceState#Interrupted},
    * {@link ActivityInstanceState#Suspended} or
    * {@link ActivityInstanceState#Hibernated}.
    *
    * @see ActivityInstanceState#Application
    * @see ActivityInstanceState#Interrupted
    * @see ActivityInstanceState#Suspended
    * @see ActivityInstanceState#Hibernated
    */
   public static final ActivityStateFilter PENDING =
         new ActivityStateFilter(true,
               new ActivityInstanceState[]{
                  ActivityInstanceState.Application,
                  ActivityInstanceState.Interrupted,
                  ActivityInstanceState.Suspended,
                  ActivityInstanceState.Hibernated
               }
         );
   /**
    * Predefined filter matching activity instance being in state
    * {@link ActivityInstanceState#Completed}.
    *
    * @see ActivityInstanceState#Completed
    */
   public static final ActivityStateFilter COMPLETED = new ActivityStateFilter(true,
         ActivityInstanceState.Completed);

   private final boolean inclusive;
   private final ActivityInstanceState[] state;

   /**
    * Initializes a filter matching activity instances being in the given state.
    *
    * @param state The state matching activity instance have to be in.
    *
    * @see #ActivityStateFilter(boolean, ActivityInstanceState)
    * @see #ActivityStateFilter(ActivityInstanceState[])
    */
   public ActivityStateFilter(ActivityInstanceState state)
   {
      this(true, state);
   }

   /**
    * Initializes a filter matching activity instances either being or not in the given
    * state.
    *
    * @param inclusive Flag indicating if the <code>state</code> is considered to be
    *                  inclusive or exclusive.
    * @param state The state matching activity instance have to be in.
    *
    * @see #ActivityStateFilter(ActivityInstanceState)
    */
   public ActivityStateFilter(boolean inclusive, ActivityInstanceState state)
   {
      this(inclusive, new ActivityInstanceState[]{state});
   }

   /**
    * Initializes a filter matching activity instances being in one of the given states.
    *
    * @param state The list of states matching activity instance have to be in.
    *
    * @see #ActivityStateFilter(boolean, ActivityInstanceState[])
    * @see #ActivityStateFilter(ActivityInstanceState)
    */
   public ActivityStateFilter(ActivityInstanceState[] state)
   {
      this(true, state);
   }

   /**
    * Initializes a filter matching activity instances either being or not in one of the
    * given states.
    *
    * @param inclusive Flag indicating if the <code>state</code> list is considered to be
    *                  inclusive or exclusive.
    * @param state The list of states matching activity instance have to be in or not.
    *
    * @see #ActivityStateFilter(ActivityInstanceState[])
    */
   public ActivityStateFilter(boolean inclusive, ActivityInstanceState[] state)
   {
      this.inclusive = inclusive;
      this.state = copyArray(state);
   }

   /**
    * Indicates if filter's state(s) are to be considered inclusive or exclusive.
    *
    * @return <code>true</code> if the filter matches activity instances being in the
    *         filter's state(s), <code>false</code> if the filter matches activity
    *         instances not being in the filter's state(s).
    *
    * @see #getStates()
    */
   public boolean isInclusive()
   {
      return inclusive;
   }

   /**
    * Returns the states the filter is defined to use for matching.
    *
    * @return The list of states the filter uses for activity instance matching.
    *
    * @see #isInclusive()
    */
   public ActivityInstanceState[] getStates()
   {
      return copyArray(state);
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
   
   /**
    * TODO in Java 1.6 evaluate the use of Arrays.copyOf()
    */
   private static final ActivityInstanceState[] copyArray(ActivityInstanceState[] src) {
      ActivityInstanceState[] dest = null;
      if(src != null) {
         int length = src.length;
         dest = new ActivityInstanceState[length];
         System.arraycopy(src, 0, dest, 0, length);
      }
      return dest;
   }
}
