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
package org.eclipse.stardust.engine.extensions.dms.data;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;


/**
 * The only difference to a regular BridgeObject is that
 * the FallbackBridgeObject tries to check assigment possibility
 * against the arbitrary fallbackClass specified in constructor
 */
public class FallbackBridgeObject extends BridgeObject
{

   private Class fallbackClass;

   public FallbackBridgeObject(Class endClass, Direction direction, Class fallbackClass)
   {
      super(endClass, direction);
      this.fallbackClass = fallbackClass;
   }

   public boolean acceptAssignmentFrom(BridgeObject rhs)
   {
      boolean result = super.acceptAssignmentFrom(rhs);
      
      if (result == false)
      {
         result = this.fallbackClass.isAssignableFrom(rhs.getEndClass());  
      }
      return result;
   }

}
