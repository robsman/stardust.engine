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
package org.eclipse.stardust.engine.extensions.xml.data;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class XMLAccessPoint extends IdentifiableElementBean implements AccessPoint
{
   private Direction direction;

   public XMLAccessPoint(String id, String name, Direction direction)
   {
      super(id, name);
      this.direction = direction;
      setTransient(true);
   }

   /**
    * Gets the data flow direction of this access point, may be either
    * {@link Direction#In}, {@link Direction#Out} or {@link Direction#InOut}.
    * 
    * @return The data flow direction.
    */
   public Direction getDirection()
   {
      return direction;
   }

   public PluggableType getType()
   {
      return new XMLDataType();
   }

   public void markModified()
   {}
}
