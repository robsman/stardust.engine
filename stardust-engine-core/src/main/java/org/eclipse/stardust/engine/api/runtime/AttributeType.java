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
 * @author fherinean
 * @version $Revision$
 */
public class AttributeType extends IntKey
{
   /**
    * Request was sent, no response received.
    */
   public static final AttributeType Global =
         new AttributeType(0, "Global");

   /**
    * Request was sent, positive response received.
    */
   public static final AttributeType Process =
         new AttributeType(1, "Process");

   /**
    * Request was sent, daemon reported a problem.
    */
   public static final AttributeType Activity =
         new AttributeType(2, "Activity");

   private AttributeType(int id, String defaultName)
   {
      super(id, defaultName);
   }
}
