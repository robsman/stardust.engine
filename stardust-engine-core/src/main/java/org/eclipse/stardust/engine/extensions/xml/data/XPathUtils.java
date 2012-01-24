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

import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;


public class XPathUtils
{
   public static Set<TypedXPath> getXPaths(Model model, DataMapping dm)
   {
      Data data = model.getData(dm.getDataId());
      String typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
      return getXPaths(model, typeDeclarationId, dm.getDataPath());
   }

   public static Set<TypedXPath> getXPaths(Model model, String typeDeclarationId)
   {
      TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
      return StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);
   }

   public static Set<TypedXPath> getXPaths(Model model, String typeDeclarationId, String derefPath)
   {
      TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
      Set<TypedXPath> allXPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);
      if (StringUtils.isEmpty(derefPath))
      {
         return allXPaths;
      }
      Set<TypedXPath> xpaths = CollectionUtils.newSet();
      for (TypedXPath xpath : allXPaths)
      {
         String path = xpath.getXPath();
         if (path.equals(derefPath))
         {
            addChildren(xpaths, null, xpath, derefPath);
            break;
         }
      }
      return xpaths;
   }

   private static void addChildren(Set<TypedXPath> xpaths, TypedXPath newParent, TypedXPath xpath,
         String derefPath)
   {
      String newPath = xpath.getXPath().substring(derefPath.length());
      if (newPath.startsWith("/"))
      {
         newPath = newPath.substring(1);
      }
      TypedXPath newXPath = new TypedXPath(newParent, xpath.getOrderKey(), newPath, xpath.isAttribute(), xpath.getXsdElementName(),
            xpath.getXsdElementNs(), xpath.getXsdTypeName(), xpath.getXsdTypeNs(), xpath.getType(),
            xpath.isList(), xpath.getAnnotations(), xpath.getEnumerationValues());
      xpaths.add(newXPath);
      for (TypedXPath childXPath : xpath.getChildXPaths())
      {
         addChildren(xpaths, newXPath, childXPath, derefPath);
      }
   }
}
