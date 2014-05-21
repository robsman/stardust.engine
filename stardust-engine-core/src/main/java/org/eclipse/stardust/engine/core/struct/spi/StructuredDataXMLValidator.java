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
package org.eclipse.stardust.engine.core.struct.spi;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.Utils;



/**
 * DataValidator for structured data
 *
 * @version $Revision$
 */
public class StructuredDataXMLValidator implements ExtendedDataValidator, Stateless
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
   public BridgeObject getBridgeObject(AccessPoint point, String path,
         Direction direction, AccessPathEvaluationContext context)
   {

      if (point == null)
      {
         throw new PublicException(
               BpmRuntimeError.SDT_CANNOT_DETERMINE_BRIDGEOBJECT_FROM_ACCESSPOINT
                     .raise(point));
      }

      StructuredDataTransformation transformation = StructuredDataTransformation.valueOf(path);
      if (transformation.isToDOM())
      {
         return new XMLBridgeObject(Element.class, direction, context);
      }

      IXPathMap xPathMap = DataXPathMap.getXPathMap(point);

      if (Direction.IN.equals(direction))
      {
         // check for incompatible XPaths
         if ( !StructuredDataXPathUtils.canBeUsedForInDataMapping(path, xPathMap))
         {
            throw new PublicException(
                  BpmRuntimeError.SDT_XPATH_CANNOT_BE_USED_FOR_IN_DATA_MAPPING_SINCE_IT_CAN_RETURN_SEVERAL_ITEMS_FROM_DIFFERENT_LEVELS
                        .raise(path));
         }
      }

      if (StructuredDataXPathUtils.canReturnList(path, xPathMap))
      {
         return new XMLBridgeObject(List.class, direction, context);
      }

      if ( !StructuredDataXPathUtils.returnsSinglePrimitive(path, xPathMap))
      {
         return new XMLBridgeObject(Map.class, direction, context);
      }

      // OTHERWISE XPath returns a primitive, compute its class:

      String xPathWithoutIndexes = StructuredDataXPathUtils.getXPathWithoutIndexes(path);

      TypedXPath xPath = xPathMap.getXPath(xPathWithoutIndexes);

      if (xPath == null)
      {
         throw new IllegalOperationException(
               BpmRuntimeError.MDL_UNKNOWN_XPATH_FOR_DATA_ID.raise(xPath, path,
                     point.getId()));
      }

      Class clazz = Utils.getJavaTypeForTypedXPath(xPath);

      return new XMLBridgeObject(clazz, direction, context);
   }

   private class XMLBridgeObject extends BridgeObject
   {
      private AccessPathEvaluationContext context;
      public XMLBridgeObject(Class clazz, Direction direction,
            AccessPathEvaluationContext context)
      {
         super(clazz, direction);
         this.context = context;
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

         if (null != context
               && StructDataMappingUtils.isVizRulesApplication(context.getActivity())
               && Map.class.isAssignableFrom(getEndClass())
               && !rhs.getEndClass().isPrimitive()
               && Serializable.class.isAssignableFrom(rhs.getEndClass()))
         {
            return true;
         }
         else
         {
            if (context != null && rhs instanceof XMLBridgeObject)
            {
               String thisType = this.context.getTargetAccessPointDefinition()
                     .getStringAttribute("carnot:engine:dataType");
               XMLBridgeObject rhsBridgeObject = (XMLBridgeObject) rhs;
               if (rhsBridgeObject.context != null)
               {
                  String rhsType = rhsBridgeObject.context.getTargetAccessPointDefinition()
                        .getStringAttribute("carnot:engine:dataType");
                  if (thisType != null && rhsType != null)
                  {
                     rhsType = removeExternalQualifier(rhsType);
                     thisType = removeExternalQualifier(thisType);
                     if (!thisType.equals(rhsType))
                     {
                        return false;
                     }
                  }
               }
            }
            return Reflect.isAssignable(getEndClass(), rhs.getEndClass());
         }
      }

      private String removeExternalQualifier(String type)
      {
         if (type.startsWith("typeDeclaration:"))
         {
            type = type.substring(type.indexOf("}") + 1, type.length());
         }
         return type;
      }
   }
}
