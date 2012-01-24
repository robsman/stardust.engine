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
 * Description object for a delegation event.
 * 
 * @author Stephan.Born
 *
 */
public interface HistoricalEventDescriptionDelegation extends HistoricalEventDescription
{
   /**
    * Valid index value for {@link #getItem(int)}. Will return performer before delegation was performed.
    * 
    * @see #getFromPerformer()
    */
   static final int FROM_PERFORMER_IDX = 0;
   
   /**
    * Valid index value for {@link #getItem(int)}. Will return performer after delegation was performed.
    * 
    * @see #getToPerformer()
    */
   static final int TO_PERFORMER_IDX = 1;

   /**
    * Will return the performer before delegation was performed.
    * 
    * @return The performer before delegation was performed.
    */
   Participant getFromPerformer();
   
   /**
    * Will return the performer after delegation was performed.
    * 
    * @return The performer after delegation was performed.
    */
   Participant getToPerformer();

}