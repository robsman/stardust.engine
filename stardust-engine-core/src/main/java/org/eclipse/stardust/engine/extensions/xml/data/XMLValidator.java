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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class XMLValidator implements DataValidator, Stateless
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

   /**
    * Creates a bridge object for the given access point and path.
    * 
    * @param point
    *           The access point.
    * @param path
    *           The access path.
    * @param direction
    *           The data flow direction, either {@link Direction#In} if a LHS bridge is
    *           requested or {@link Direction#Out} if a RHS bridge is requested
    * 
    * @return The corresponding bridge object.
    */
   public BridgeObject getBridgeObject(AccessPoint point, String path, Direction direction)
   {
      Class clazz;
      if (Direction.OUT.equals(direction))
      {
         Object pseudoResult = new XPathEvaluator().evaluate(Collections.EMPTY_MAP,
               "<emptyXml />", path);
         clazz = (null != pseudoResult) ? pseudoResult.getClass() : List.class;
      }
      else
      {
         clazz = String.class;
      }

      return new XMLBridgeObject(clazz, direction);
   }

   private class XMLBridgeObject extends BridgeObject
   {
      public XMLBridgeObject(Class clazz, Direction direction)
      {
         super(clazz, direction);
      }

      /**
       * Performs a static check if this bridge is valid as a data sink for the data
       * source represented by rhs. Basic validity requires compatible data flow
       * directions and type compatibility (if available).
       * 
       * @param rhs
       *           The data source to check compatibility against.
       * 
       * @return true if this bridge may accept assignments from the given data source,
       *         false if not.
       */
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
         return Reflect.isAssignable(Document.class, rhs.getEndClass())
               || Reflect.isAssignable(Element.class, rhs.getEndClass())
               || Reflect.isAssignable(getEndClass(), rhs.getEndClass());
      }
   }
}
