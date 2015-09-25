/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.common.IntKey;

/**
 * Wrapper class for the historical event types.
 * It provides human readable names for the historical event type codes.
 *
 * @author Stephan.Born
 *
 */
public class HistoricalEventType extends IntKey
{
   private static final long serialVersionUID = 1L;

   /**
    * The HistoricalEvent describes a note event.
    */
   public final static int NOTE = 1;

   /**
    * The HistoricalEvent describes an exception event.
    */
   public final static int EXCEPTION = 2;

   /**
    * The HistoricalEvent describes a state change event.
    */
   public final static int STATE_CHANGE = 4;

   /**
    * The HistoricalEvent describes a data change
    */
   public final static int DATA_CHANGE = 5;
   
   /**
    * The HistoricalEvent describes a delegation event.
    */
   public final static int DELEGATION = 8;
   

   /**
    * The HistoricalEvent describes an event execution event.
    */
   public final static int EVENT_EXECUTION = 16;

   public final static HistoricalEventType Note = new HistoricalEventType(NOTE, "Note");
   public final static HistoricalEventType Exception = new HistoricalEventType(EXCEPTION, "Exception");
   public final static HistoricalEventType StateChange = new HistoricalEventType(STATE_CHANGE, "State Change");
   public final static HistoricalEventType Delegation = new HistoricalEventType(DELEGATION, "Delegation");
   public final static HistoricalEventType EventExecution = new HistoricalEventType(EVENT_EXECUTION, "EventExecution");
   public final static HistoricalEventType DataChange = new HistoricalEventType(DATA_CHANGE, "Data Change");
   /**
    * Factory method to get the HistoricalEventType object corresponding to the numerical code.
    *
    * @param value the numeric code of the HistoricalEventType.
    * @return the HistoricalEventType object.
    */
   public static HistoricalEventType get(int value)
   {
      return (HistoricalEventType) getKey(HistoricalEventType.class, value);
   }

   protected Object readResolve()
   {
      return super.readResolve();
   }

   private HistoricalEventType(int id, String defaultName)
   {
      super(id, defaultName);
   }

}
