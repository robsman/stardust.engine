/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;

import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;

public class DataClusterRuntimeInfo
{
   private Set<ProcessInstanceState> requiredClusterPiStates;

   private boolean requiredClusterPiStatesSet = false;
   
   
   public Set<ProcessInstanceState> getRequiredClusterPiStates()
   {
      if(requiredClusterPiStates != null && !requiredClusterPiStates.isEmpty())
      {
         return requiredClusterPiStates;
      }
      
      return ProcessInstanceState.getAllStates();
   }

   public void setRequiredClusterPiStates(Set<ProcessInstanceState> requiredClusterPiStates)
   {
      this.requiredClusterPiStates = requiredClusterPiStates;
      requiredClusterPiStatesSet = true;
   }

   public boolean isRequiredClusterPiStatesSet()
   {
      return requiredClusterPiStatesSet;
   }
}
