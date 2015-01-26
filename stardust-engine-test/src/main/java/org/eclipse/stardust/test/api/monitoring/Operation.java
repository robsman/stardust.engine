/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.monitoring;

/**
 * <p>
 * This enums holds the database operations we're monitoring.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public enum Operation
{
   INSERT, UPDATE, DELETE, SELECT;

   public static Operation fromTriggerType(final int triggerType)
   {
      switch (triggerType)
      {
         case 1:
            return INSERT;

         case 2:
            return UPDATE;

         case 4:
            return DELETE;

         case 8:
            return SELECT;

         default:
            throw new IllegalArgumentException("Unsupported trigger type: '" + triggerType + "' (must be of exactly one type).");
      }
   }
}
