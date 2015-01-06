/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.util.List;

/**
 * Descriptor of a transition target.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public final class TransitionTarget extends TransitionInfo
{
   private static final long serialVersionUID = 1L;

   private boolean forward;
   private List<TransitionStep> transitionSteps;

   TransitionTarget(long activityInstanceOid, long modelOid, long activityRuntimeOid,
         String activityId, String activityName, String processId, String processName,
         String modelId, String modelName, List<TransitionStep> transitionSteps, boolean forward)
   {
      super(activityInstanceOid, modelOid, activityRuntimeOid, activityId,
            activityName, processId, processName, modelId, modelName);
      this.transitionSteps = transitionSteps;
      this.forward = forward;
   }
   
   /**
    * Retrieves the list of intermediate steps that must be performed during transition.
    * 
    * @return a non-null list of transition steps. Can be empty if no intermediate steps are required.
    */
   public List<TransitionStep> getTransitionSteps()
   {
      return transitionSteps;
   }

   public String toString()
   {
      return transitionSteps.isEmpty() ? getActivityId() : getActivityId() + transitionSteps;
   }

   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + (forward ? 1231 : 1237);
      result = prime * result + ((transitionSteps == null) ? 0 : transitionSteps.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (!super.equals(obj))
      {
         return false;
      }
      TransitionTarget other = (TransitionTarget) obj;
      if (forward != other.forward)
      {
         return false;
      }
      if (transitionSteps == null)
      {
         return other.transitionSteps == null;
      }
      return transitionSteps.equals(other.transitionSteps);
   }
}
