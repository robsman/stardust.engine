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
package org.eclipse.stardust.engine.extensions.ejb.data;

import java.util.Collections;
import java.util.Map;
import java.util.List;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataValidator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EntityBeanValidator implements DataValidator, Stateless
{

   public boolean isStateless()
   {
      return true;
   }

   public List validate(Map attributes)
   {
      // @todo (france, ub):
      return Collections.EMPTY_LIST;
   }

   public BridgeObject getBridgeObject(AccessPoint point, String path, Direction direction)
   {
      // @todo (rsauer) flag LHS bridges with paths containing getter().setter() invalid,
      //       as they only modify the local copy
      boolean is3x = EntityBeanConstants.VERSION_3_X.equals(point.getAttribute(EntityBeanConstants.VERSION_ATT));
      BridgeObject javaBridge = JavaDataTypeUtils.getBridgeObject(point, path);
      return new EntityBeanBridgeObject(is3x, javaBridge.getEndClass(), javaBridge.getDirection());
   }

   private class EntityBeanBridgeObject extends BridgeObject
   {
      private boolean is3x;

      public EntityBeanBridgeObject(boolean is3x, Class clazz, Direction direction)
      {
         super(clazz, direction);
         this.is3x = is3x;
      }

      public boolean acceptAssignmentFrom(BridgeObject rhs)
      {
         // direction must be in or inout or null
         if (getDirection() == Direction.OUT)
         {
            return false;
         }
         // rhs direction must be out, inout or null
         if (rhs.getDirection() == Direction.IN)
         {
            return false;
         }
         return Reflect.isAssignable(getEndClass(), rhs.getEndClass())
               || !is3x && (EJBObject.class.isAssignableFrom(rhs.getEndClass())
               || EJBLocalObject.class.isAssignableFrom(rhs.getEndClass())
               || Handle.class.isAssignableFrom(rhs.getEndClass()));
      }
   }
}
