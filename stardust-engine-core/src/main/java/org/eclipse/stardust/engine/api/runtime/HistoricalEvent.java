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
import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;


/**
 * The <code>HistoricalEvent</code> represents a single event which was recorded 
 * during lifetime of an {@link ActivityInstance} or {@link ProcessInstance}.
 * 
 * @author Stephan.Born
 *
 */
public interface HistoricalEvent extends Serializable
{
   /**
    * Will return the event type.
    * 
    * @return The event type.
    */
   HistoricalEventType getEventType();
   
   /**
    * Will return the event time.
    * 
    * @return The time the event occurred on.
    */
   Date getEventTime();
   
   /**
    * Getter to retrieve an object which contains more specific information about the event.
    * The concrete type of this details object depends on the {@link HistoricalEventType}:
    * <li> {@link HistoricalEventType#Delegation}: details contained in {@link HistoricalEventDescriptionDelegation} instance.
    * <li> {@link HistoricalEventType#StateChange}: details contained in {@link HistoricalEventDescriptionStateChange} instance.
    * <li> {@link HistoricalEventType#Exception}: details contained in a {@link String}.
    * <li> {@link HistoricalEventType#Note}: details contained in a {@link String}.
    * 
    * @return The details for the event.
    */
   Serializable getDetails();
   
   /**
    * Will return the user which was responsible for the event.
    * @return The user object.
    */
   User getUser();
}
