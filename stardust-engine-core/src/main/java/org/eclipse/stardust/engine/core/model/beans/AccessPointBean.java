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
package org.eclipse.stardust.engine.core.model.beans;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.IAccessPoint;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;


/**
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public class AccessPointBean extends IdentifiableElementBean
      implements IAccessPoint
{
   private static final String DIRECTION_ATT = "Direction";
   private Direction direction;

   // @todo (france, ub): make bidirectional
   private SingleRef dataType = new SingleRef(this, "Data Type");

   AccessPointBean()
   {
   }

   /**
    * Canonical constructor.
    * 
    * @param id        The unique id identifying this access point.
    * @param name      The name of the access point. Serves as human readable representation.
    * @param direction The direction of the access point.
    */
   AccessPointBean(String id, String name, Direction direction)
   {
      super(id, name);

      this.direction = direction;
   }

   public Direction getDirection()
   {
      return direction;
   }

   public String toString()
   {
      return "Access point: " + getId();
   }

   public String getUniqueId()
   {
      return getId() + ":direction:" + direction;
   }

   public PluggableType getType()
   {
      return (IDataType) dataType.getElement();
   }

   public void setDataType(IDataType type)
   {
      Assert.isNotNull(type);
      this.dataType.setElement(type);
   }
}
