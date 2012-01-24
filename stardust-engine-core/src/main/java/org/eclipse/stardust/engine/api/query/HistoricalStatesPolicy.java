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

/**
 * Evaluation policy for specifying retrieval of historical states.
 * 
 * @author born
 * @version $Revision$
 */
public enum HistoricalStatesPolicy implements EvaluationPolicy
{
   /**
    * Retrieve no historical states. This is the default.
    */
   NO_HIST_STATES,
   
   /**
    * Retrieve the last historical state only.
    */
   WITH_LAST_HIST_STATE,
   
   /**
    * Retrieve the last user performer from historical states.
    */
   WITH_LAST_USER_PERFORMER,
   
   /**
    * Retrieve all historical states.
    */
   WITH_HIST_STATES;
   
   private static final long serialVersionUID = 2L;

   public static final String PRP_PROPVIDE_HIST_STATES = HistoricalStatesPolicy.class.getName() + ".Enabled";

   /**
    * Determines whether this policy can be used in order to retrieve historical states.
    * 
    * @return <code>true</code> if this policy can be used in order to retrieve historical states. Otherwise <code>false</code>.
    */
   public boolean includeHistStates()
   {
      return this != NO_HIST_STATES;
   }

   /**
    * Determines whether this policy can be used in order to retrieve complete states history.
    * 
    * @return <code>true</code> if this policy can be used in order to retrieve complete states history. Otherwise <code>false</code>.
    */
   public boolean isCompleteHistory()
   {
      return this == WITH_HIST_STATES;
   }
}
