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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;


/**
 * Representation of an event. Indicates the event source and allows for controlled state
 * changes of the event source.
 * 
 * This class is not intended to be subclassed by third parties.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Event
{
   /**
    * Used to mark field {@link Event#handlerOID} or {@link Event#handlerModelElementOID}
    * as undefined
    */
   public static final int OID_UNDEFINED = -1;
   
   /**
    * Indicates the event source to be an activity instance.
    */
   public static final int ACTIVITY_INSTANCE = 1;
   /**
    * Indicates the event source to be a process instance.
    */
   public static final int PROCESS_INSTANCE = 2;

   /**
    * Indicates an engine internal, push-type event.
    */
   public static final int ENGINE_EVENT = 1;
   /**
    * Indicates an arbitrary, pull-type event. Usually generated by some polling
    * mechanism.
    */
   public static final int PULL_EVENT = 2;
   /**
    * Indicates an arbitrary, push-type event. Usually generated by some external
    * mechanism.
    */
   public static final int PUSH_EVENT = 3;

   private int type;
   private long objectOID;
   private long handlerOID;
   private long handlerModelElementOID;
   private Map attributes = new HashMap();
   private int emitterType;
   private ActivityInstanceState intendedState;

   public Event(int type, long objectOID, long handlerRuntimeOID, long handlerModelElementOID, int emitterType)
   {
      this.type = type;
      this.objectOID = objectOID;
      this.handlerOID = handlerRuntimeOID;
      this.handlerModelElementOID = handlerModelElementOID;
      this.emitterType = emitterType;
   }

   /**
    * Sets an event specific attribute.
    * 
    * @param name The name of the attribute to be set.
    * @param value The actual attribute value.
    */
   public void setAttribute(String name, Object value)
   {
      attributes.put(name, value);
   }
   
   /**
    * Sets event specific attributes.
    * 
    * @param attributes The map containing.
    */
   public void setAttributes(Map attributes)
   {
      this.attributes.putAll(attributes);
   }

   /**
    * Gets an event specific attribute.
    * 
    * @param name The name of the attribute to be retrieved.
    * @return The value of the attribute, or <code>null</code>.
    */
   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }
   
   /**
    * Gets all event specific attributes.
    * 
    * @return The attributes
    */
   public Map getAttributes()
   {
      return Collections.unmodifiableMap(attributes);
   }

   /**
    * Gets the runtime OID of the event handler this event is targeting.
    * 
    * @return The current event handler runtime OID.
    */
   public long getHandlerOID()
   {
      return handlerOID;
   }

   /**
    * Sets the runtime OID of the event handler this event is targeting.
    * 
    * @param handlerOID - the runtime OID of the event handler this event is targeting
    */
   public void setHandlerOID(long handlerOID)
   {
      this.handlerOID = handlerOID;
   }

   /**
    * Gets the model element OID of the event handler this event is targeting.
    * 
    * @return The current event handler model element OID.
    */
   public long getHandlerModelElementOID()
   {
      return handlerModelElementOID;
   }

   /**
    * Sets the model element OID of the event handler this event is targeting.
    * 
    * @param handlerModelElementOID - the model element OID of the event handler this event is targeting
    */
   public void setHandlerModelElementOID(long handlerModelElementOID)
   {
      this.handlerModelElementOID = handlerModelElementOID;
   }

   /**
    * Gets the runtime OID of the event source.
    *
    * @return The event source runtime OID.
    * @see #getEmitterType()
    */
   public long getObjectOID()
   {
      return objectOID;
   }

   /**
    * Gets the event type.
    *
    * @return The event type.
    * 
    * @see #ENGINE_EVENT
    * @see #PULL_EVENT
    */
   public int getType()
   {
      return type;
   }

   /**
    * Gets the type of the event source.
    *
    * @return The event source type.
    * 
    * @see #ACTIVITY_INSTANCE
    * @see #PROCESS_INSTANCE
    */
   public int getEmitterType()
   {
      return emitterType;
   }

   /**
    * Gets the intended state of activity event sources, allowing for safe state changes.
    *
    * @return The intended activity state.
    */
   public ActivityInstanceState getIntendedState()
   {
      return intendedState;
   }

   /**
    * Sets the intended target state of activity event sources, allowing event actions to
    * trigger safe state changes.
    * <p />
    * Can only be set once during event processing, usually in the final event action.
    *
    * @param state The intended target state.
    * @throws IllegalStateChangeException if there was already another state change
    *       scheduled.
    */
   public void setIntendedState(ActivityInstanceState state)
      throws IllegalStateChangeException
   {
      if (intendedState != null)
      {
         throw new IllegalStateChangeException(intendedState, state);
      }
      this.intendedState = state;
   }

   /**
    * Produces a human readable representation of this event.
    * 
    * @return A human readable event representation.
    */
   public String toString()
   {
      StringBuffer result = new StringBuffer("Event: [");
      result.append("objectOID = ").append(objectOID).append(", ");
      result.append("type = ").append(type).append(", ");
      result.append("handlerOID = ").append(handlerOID).append(", ");
      result.append("handlerModelElementOID = ").append(handlerModelElementOID);
      result.append("]");
      return result.toString();
   }

   
}
