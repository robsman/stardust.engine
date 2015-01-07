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
package org.eclipse.stardust.engine.core.model.gui;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


public class AccessPointTemplate extends ModelElementTemplate implements IAccessPoint
{
   private IDataType type;
   private Direction direction;

   public AccessPointTemplate(AccessPoint ap)
   {
      super();
      copyFrom(ap);
   }

   public AccessPointTemplate()
   {
   }

   public void copyFrom(AccessPoint ap)
   {
      if (ap != this)
      {
         setId(ap.getId());
         setName(ap.getName());
         setAllAttributes(ap.getAllAttributes());
         type = (IDataType) ap.getType();
         direction = ap.getDirection();
      }
   }

   public Direction getDirection()
   {
      return direction;
   }

   public void setDirection(Direction direction)
   {
      this.direction = direction;
      firePropertyChanged();
   }

   public PluggableType getType()
   {
      return type;
   }

   public void setDataType(IDataType type)
   {
      this.type = type;
      firePropertyChanged();
   }

   public void createAccessPoint(AccessPointOwner owner)
   {
      IAccessPoint ap = (IAccessPoint) owner.createAccessPoint(getId(), getName(),
            direction, getType((IModel) ((ModelElement) owner).getModel()), getElementOID());
      ap.setAllAttributes(getAllAttributes());
   }

   public void modifyAccessPoint(IAccessPoint ap)
   {
      // hint: (fh) it seems that the direction never changes.
      ap.setId(getId());
      ap.setName(getName());
      ap.setDataType(getType((IModel) ap.getModel()));
      ap.setAllAttributes(getAllAttributes());
   }

   private IDataType getType(IModel model)
   {
      return type == null ? model.findDataType(PredefinedConstants.SERIALIZABLE_DATA) : type;
   }
}
