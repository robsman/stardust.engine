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

import java.io.Serializable;

/**
 * Description object for an historical event.
 * 
 * @author Stephan.Born
 *
 */
public interface HistoricalEventDescription extends Serializable
{
   /**
    * Getter which allows to retrieve concrete aspects for this historical event. Valid values for idx are defined in subclasses, e.g. {@link HistoricalEventDescriptionDelegation#FROM_PERFORMER_IDX} or {@link HistoricalEventDescriptionStateChange#FROM_STATE_IDX}. 
    * @param idx Index for historical event description aspect.
    * @return Historical event description aspect.
    * 
    * @see org.eclipse.stardust.engine.api.runtime.HistoricalEventDescriptionDelegation
    * @see org.eclipse.stardust.engine.api.runtime.HistoricalEventDescriptionStateChange
    */
   Serializable getItem(int idx);
}
