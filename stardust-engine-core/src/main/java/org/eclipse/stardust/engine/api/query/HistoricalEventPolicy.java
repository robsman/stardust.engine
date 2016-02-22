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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;

/**
 * Evaluation policy determining the inclusion of the given event types in process and activity instances.
 *
 * @author rottstock
 * @version $Revision: $
 */
public class HistoricalEventPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;
   
   public static final String PRP_PROPVIDE_EVENT_TYPES = HistoricalEventPolicy.class
      .getName() + ".Types";
   
   private final int eventTypes;
   
   /**
    * Predefined policy to include all event types in the process or activity instance.
    */
   public final static HistoricalEventPolicy ALL_EVENTS = new HistoricalEventPolicy(
         HistoricalEventType.EXCEPTION | HistoricalEventType.STATE_CHANGE
               | HistoricalEventType.NOTE | HistoricalEventType.DELEGATION
               | HistoricalEventType.EVENT_EXECUTION | HistoricalEventType.DATA_CHANGE);
   
   /**
    * Creates a policy for a given event type resp. event types.
    * <p>To add more than one event type, just concatenate the types via the logical <em>or</em> operator
    * e.g. <code>new HistoricalEventPolicy(HistoricalEventType.EXCEPTION | HistoricalEventType.DELEGATION)</code>.</p>
    * @param eventTypes events types which should be resolved
    */
   public HistoricalEventPolicy(int eventTypes)
   {
      this.eventTypes = eventTypes;
   }
   
   /**
    * Creates a policy for the given events.
    * @param eventTypes events types which should be resolved
    */
   public HistoricalEventPolicy(HistoricalEventType[] eventTypes)
   {
      int types = 0;
      if(eventTypes != null)
      {
         for(int i = 0; i< eventTypes.length; i++)
         {
            types |= eventTypes[i].getValue();
         }
      }
      this.eventTypes = types;
   }
   
   /**
    * Gets the requested event types
    * @return event types
    */
   public int getEventTypes()
   {
      return eventTypes;
   }
}
