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

import org.eclipse.stardust.engine.api.model.Participant;


/**
 * Description object for a state change event.
 * 
 * @author Stephan.Born
 *
 */
public interface HistoricalEventDescriptionStateChange extends HistoricalEventDescription
{
   /**
    * Valid index value for {@link #getItem(int)}. Will return state before state change was performed.
    * 
    * @see #getFromState()
    */
   static final int FROM_STATE_IDX = 0;
   
   /**
    * Valid index value for {@link #getItem(int)}. Will return state after state change was performed.
    * 
    * @see #getToState()
    */
   static final int TO_STATE_IDX = 1;
   
   /**
    * Valid index value for {@link #getItem(int)}. Will return performer after state change was performed.
    * 
    * @see #getToPerformer()
    */
   static final int TO_PERFORMER_IDX = 2;

   /**
    * Will return the state before state change was performed.
    * 
    * @return The state before state change was performed.
    */
   ActivityInstanceState getFromState();
   
   /**
    * Will return the state after state change was performed.
    * 
    * @return The state after state change was performed.
    */
   ActivityInstanceState getToState();
   
   /**
    * Will return the performer after state change was performed.
    * 
    * @return The performer after state change was performed.
    */
   Participant getToPerformer();

}