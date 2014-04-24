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

import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;

/**
 * @author rsauer
 * @version $Revision$
 */
public class ProcessStateFilter implements FilterCriterion
{
   /**
    * Filter for finding alive process instances.
    * <p/>
    * <p>Alive means not being in states {@link ProcessInstanceState#ABORTED} or
    * {@link ProcessInstanceState#COMPLETED}</p>.
    */
   public static final ProcessStateFilter ALIVE =
         new ProcessStateFilter(false,
               new ProcessInstanceState[]{
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed
               }
         );

   /**
    * Filter for finding process instances in state
    * {@link ProcessInstanceState#ACTIVE}.
    */
   public static final ProcessStateFilter ACTIVE = new ProcessStateFilter(
         ProcessInstanceState.Active);

   /**
    * Filter for finding pending process instances.
    * <p/>
    * <p>Pending means being in state {@link ProcessInstanceState#INTERRUPTED}.</p>.
    */
   public static final ProcessStateFilter INTERRUPTED = new ProcessStateFilter(
         ProcessInstanceState.Interrupted);

   /**
    * Filter for finding process instances in state
    * {@link ProcessInstanceState#COMPLETED}.
    */
   public static final ProcessStateFilter COMPLETED = new ProcessStateFilter(
         ProcessInstanceState.Completed);

   /**
    * Filter for finding process instances in state
    * {@link ProcessInstanceState#ABORTED}.
    */
   public static final ProcessStateFilter ABORTED = new ProcessStateFilter(
         ProcessInstanceState.Aborted);

   private final boolean inclusive;
   private final ProcessInstanceState[] state;

   public ProcessStateFilter(ProcessInstanceState state)
   {
      this(true, state);
   }

   public ProcessStateFilter(boolean inclusive, ProcessInstanceState state)
   {
      this(inclusive, new ProcessInstanceState[]{state});
   }

   public ProcessStateFilter(ProcessInstanceState[] state)
   {
      this(true, state);
   }

   public ProcessStateFilter(boolean inclusive, ProcessInstanceState[] state)
   {
      this.inclusive = inclusive;
      this.state = copyArray(state);
   }

   public final boolean isInclusive()
   {
      return inclusive;
   }

   public final ProcessInstanceState[] getStates()
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
   private static final ProcessInstanceState[] copyArray(ProcessInstanceState[] src) {
      ProcessInstanceState[] dest = null;
      if(src != null) {
         int length = src.length;
         dest = new ProcessInstanceState[length];
         System.arraycopy(src, 0, dest, 0, length);
      }
      return dest;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("STATE");
      if (state != null && state.length > 0)
      {
         switch (state.length)
         {
         case 0:
            break;
         case 1:
            sb.append(" = ");
            sb.append(state[0]);
            break;
         default:
            sb.append(" IN (");
            boolean first = true;
            for (ProcessInstanceState piState : state)
            {
               if (first)
               {
                  first = false;
               }
               else
               {
                  sb.append(' ');
               }
               sb.append(piState);
            }
            sb.append(')');
         }
      }
      if (inclusive)
      {
         sb.append(" INCLUSIVE");
      }
      return sb.toString();
   }
}
