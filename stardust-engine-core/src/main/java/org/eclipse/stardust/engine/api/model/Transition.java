/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.model;

import java.io.Serializable;

/**
 * The client view of a workflow transition.
 * <p>A Transition represents the flow from a source activity to a target activity and may
 * be restricted by conditions.</p>
 *
 * @author fherinean
 */
public interface Transition extends Serializable
{
   /**
    * Enumeration of possible condition types.
    */
   public enum ConditionType
   {
      /**
       * Normal condition type.
       */
      Condition,

      /**
       * Condition will be executed if no other condition evaluates to true.
       */
      Otherwise
   }

   /**
    * Gets the id of the transition.
    */
   String getId();

   /**
    * Gets the id of the source activity ("from").
    */
   String getSourceActivityId();

   /**
    * Gets the id of the target activity ("to").
    */
   String getTargetActivityId();

   /**
    * Gets the transition condition. May be null.
    */
   String getCondition();

   /**
    * Gets the condition type.
    */
   ConditionType getConditionType();

   //boolean isForkOnTraversal();
}
