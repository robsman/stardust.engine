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

/**
 * Descriptor of a transition step.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public final class TransitionStep extends TransitionInfo
{
   private static final long serialVersionUID = 1L;

   TransitionStep(long activityInstanceOid, long modelOid, long activityRuntimeOid, String activityId,
         String activityName, String processId, String processName, String modelId, String modelName)
   {
      super(activityInstanceOid, modelOid, activityRuntimeOid, activityId,
            activityName, processId, processName, modelId, modelName);
   }
   
   /**
    * Retrieves the step direction.
    * 
    * @return true if the step is performed out of the sub process, false if it's a step into a sub process.
    */
   public boolean isUpwards()
   {
      return getActivityInstanceOid() != -1;
   }

   public String toString()
   {
      return (isUpwards() ? '-' : '+') + getActivityId();
   }
}
