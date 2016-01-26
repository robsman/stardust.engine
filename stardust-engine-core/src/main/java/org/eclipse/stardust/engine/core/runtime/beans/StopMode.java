/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

public enum StopMode
{
   ABORT ("Abort", "Aborting", "Aborted"),
   HALT ("Halt", "Halting", "Halted");

   private final String modeName;
   private final String transitionState;
   private final String finalState;

   private StopMode(String modeName, String transitionState, String finalState)
   {
      this.modeName = modeName;
      this.transitionState = transitionState;
      this.finalState = finalState;
   }

   public String getModeName()
   {
      return modeName;
   }

   public String getTransitionState()
   {
      return transitionState;
   }

   public String getFinalState()
   {
      return finalState;
   }

}
