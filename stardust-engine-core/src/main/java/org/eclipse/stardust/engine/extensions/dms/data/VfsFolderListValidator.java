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

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataValidator;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXMLValidator;


// TODO (ab) implement 
public class VfsFolderListValidator implements DataValidator
{
   public BridgeObject getBridgeObject(AccessPoint point, String path, Direction direction)
   {
//      Direction bridgeDirection = null;
//
//      Class currentType = List.class;

      // TODO (ab) fine-tune the validation
      
      BridgeObject structuredBridgeObject = new StructuredDataXMLValidator()
            .getBridgeObject(point, path, direction, null);
      
      if (StringUtils.isEmpty(path))
      {
         return new FallbackBridgeObject(List.class, direction, structuredBridgeObject.getEndClass());
      }
      else
      {
         IXPathMap xPathMap = DataXPathMap.getXPathMap(point);
         String pathWithoutIndexes = StructuredDataXPathUtils.getXPathWithoutIndexes(path);
         if (xPathMap.containsXPath(pathWithoutIndexes))
         {
            if (pathWithoutIndexes.equals(AuditTrailUtils.FOLDERS_FOLDERS) 
                  && !StructuredDataXPathUtils.canReturnList(path, xPathMap))
            {
               return new FallbackBridgeObject(Folder.class, direction, structuredBridgeObject.getEndClass());
            }
            else
            {
               return structuredBridgeObject;
            }
         }
         else
         {
            return structuredBridgeObject;
         }
      }
//      }
//
//      if ( !StringUtils.isEmpty(path))
//      {
//         bridgeDirection = Direction.OUT;
//         for (Iterator i = JavaDataTypeUtils.parse(path).iterator(); i.hasNext();)
//         {
//            String element = (String) i.next();
//            try
//            {
//               if ( !i.hasNext() && !element.endsWith("()"))
//               {
//                  //we have found a setter here
//                  currentType = Reflect.decodeMethod(currentType, element)
//                        .getParameterTypes()[0];
//                  bridgeDirection = Direction.IN;
//                  // todo: (france, fh) shouldn't be the setter the endpoint ???
//               }
//               else
//               {
//                  currentType = Reflect.decodeMethod(currentType, element).getReturnType();
//               }
//            }
//            catch (InternalException x)
//            {
//               throw new PublicException(
//                     "Method '" + element + "' not available or not accessible.", x);
//            }
//         }
//      }
//      
//      return new BridgeObject(currentType, bridgeDirection);
   }

   public List validate(Map attributes)
   {
      Vector inconsistencies = new Vector();

      // TODO (ab) see model time validation
      
      return inconsistencies;
   }

}
