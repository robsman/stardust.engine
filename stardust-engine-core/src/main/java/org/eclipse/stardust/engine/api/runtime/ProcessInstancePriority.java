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

import org.eclipse.stardust.common.IntKey;

/**
 * A representation of the priority of an process instance.
 *
 * This class also provides human readable values for the process instance priorities.
 *
 * @author sborn
 */
public class ProcessInstancePriority extends IntKey
{
   /**
    * The process instance has lower priority than normal.
    */
   public static final int LOW = -1;
   /**
    * The process instance has normal priority (default).
    */
   public static final int NORMAL = 0;
   /**
    * The process instance has higher priority than normal.
    */
   public static final int HIGH = 1;

   public static final ProcessInstancePriority Low = new ProcessInstancePriority(LOW,
         "Low");
   public static final ProcessInstancePriority Normal = new ProcessInstancePriority(
         NORMAL, "Normal");
   public static final ProcessInstancePriority High = new ProcessInstancePriority(HIGH,
         "High");

   private static final int IDX_SHIFT = 1;
   private static final ProcessInstancePriority[] KEYS = new ProcessInstancePriority[] {
      Low,
      Normal,
      High
   };
   
   /**
    * Factory method to get the ProcessInstancePriority corresponding to the given code.
    *
    * @param value one of the ProcessInstancePriority codes.
    *
    * @return one of the predefined ProcessInstancePriority or null if it's an invalid code.
    */
   public static ProcessInstancePriority getPriority(int value)
   {
      value = value+IDX_SHIFT;
      if ((value < KEYS.length) && (value >= 0) && (null != KEYS[value]))
      {
         return KEYS[value];
      }
      return (ProcessInstancePriority) getKey(ProcessInstancePriority.class, value-IDX_SHIFT);
   }

   private ProcessInstancePriority(int value, String name)
   {
      super(value, name);
   }
}
