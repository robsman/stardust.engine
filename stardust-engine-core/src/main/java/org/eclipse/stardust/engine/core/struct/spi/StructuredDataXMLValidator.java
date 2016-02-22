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
import java.util.Vector;

import org.w3c.dom.Element;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.spi.extensions.model.*;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.*;

/**
 * DataValidator for structured data
 *
 * @version $Revision$
 */
public class StructuredDataXMLValidator implements ExtendedDataValidator, Stateless, StructuredDataValidator
{
   public boolean isStateless()
   {
      return true;
   }

   public List validate(IData data)
   {
      Vector inconsistencies = new Vector();
      
      String typeId = data.getType().getId();
      if (StructuredDataConstants.STRUCTURED_DATA.equals(typeId))
      {
         IReference ref = data.getExternalReference();
         // local data only 
         if(ref == null)
         {
            IXPathMap xPathMap = StructuredTypeRtUtils.getXPathMap(data);
            if (xPathMap == null)
            {
               BpmValidationError error = BpmValidationError.DATA_NO_SCHEMA_FOUND_FOR_STRUCTURED_DATA.raise(data.getId()); 
               inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
            }
         }
      }
      
      return inconsistencies;
   }
      
   public List validate(Map attributes)
   {
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

               try
               {
                  XMLBridgeObject rhsBridgeObject = (XMLBridgeObject) rhs;

                  String thisType = this.context.getTargetAccessPointDefinition().getStringAttribute("carnot:engine:dataType");
                  String rhsType = ((XMLBridgeObject) rhs).context.getTargetAccessPointDefinition().getStringAttribute("carnot:engine:dataType");

                  ITypeDeclaration thisDecl = StructuredTypeRtUtils.getTypeDeclaration(this.context.getTargetAccessPointDefinition());
                  ITypeDeclaration rhsDecl = StructuredTypeRtUtils.getTypeDeclaration(rhsBridgeObject.context.getTargetAccessPointDefinition());

                  if (this.context.getTargetPath() != null)
                  {
                     IXPathMap thisMap = StructuredTypeRtUtils.getXPathMap((IData) this.context.getTargetAccessPointDefinition());
                     if (thisMap != null)
                     {
                        TypedXPath thisXPath = thisMap.getXPath(this.context.getTargetPath());
                        if (thisXPath != null)
                        {
                           thisType = thisXPath.getXsdTypeName();
                           IModel model = (IModel) thisDecl.getModel();
                           if (model != null)
                           {
                              thisDecl = model.findTypeDeclaration(thisType);
                           }
                        }
                     }
                  }

                  if (rhsBridgeObject.context.getTargetPath() != null)
                  {
                     IXPathMap rhsMap = StructuredTypeRtUtils.getXPathMap((IData) rhsBridgeObject.context.getTargetAccessPointDefinition());
                     if (rhsMap != null)
                     {
                        TypedXPath rhsXPath = rhsMap.getXPath(rhsBridgeObject.context.getTargetPath());
                        if (rhsXPath != null)
                        {
                           rhsType = rhsXPath.getXsdTypeName();
                           IModel model = (IModel) rhsDecl.getModel();
                           if (model != null)
                           {
                              rhsDecl = model.findTypeDeclaration(rhsType);
                           }
                        }
                     }
                  }

                  if (thisDecl != null && rhsDecl != null)
                  {
                     // thisDecl.equals(rhsDecl) does not work here as expected - see
                     // CRNT-33472
                     return declarationsAreEqual(thisDecl, rhsDecl);
                  }


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
               catch (Throwable t)
               {
                  return Reflect.isAssignable(getEndClass(), rhs.getEndClass());
            }
            }
            return Reflect.isAssignable(getEndClass(), rhs.getEndClass());
         }
      }

      private boolean declarationsAreEqual(ITypeDeclaration decl1, ITypeDeclaration decl2)
      {
         boolean isEqual = (decl1 == decl2);

         if (!isEqual)
         {

            if (null != decl1.getModel() && (null != decl2.getModel()))
            {
               if (decl1.getElementOID() != decl2.getElementOID())
               {
                  isEqual = false;
               }
               if (decl1.getElementOID() == -1 && decl2.getElementOID() == -1)
               {
                  if (decl1.getId().equals(decl2.getId()))
                  {
                     IModel model1 = (IModel) decl1.getModel();
                     IModel model2 = (IModel) decl2.getModel();
                     if (model1.getModelOID() != model2.getModelOID())
                     {
                        isEqual = false;
                     }
                     if (model1.getModelOID() == 0 && model1.getModelOID() == 0)
                     {
                        isEqual = (model1.getId() == model2.getId());
                     }
                  }
               }
            }
         }
         return isEqual;
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