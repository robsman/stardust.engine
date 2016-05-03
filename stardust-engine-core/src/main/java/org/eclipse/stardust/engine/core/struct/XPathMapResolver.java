/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.struct;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.*;

public abstract class XPathMapResolver implements IXPathMap, IXPathMap.Resolver
{
   private Map<String, TypedXPath> roots;

   @Override
   public TypedXPath findXPath(List<String> parts)
   {
      TypedXPath xPath = null;
      if (parts != null)
      {
         xPath = safeGetXPath("");
         StringBuffer assembled = new StringBuffer();
         boolean isAny = false;
         for (String part : parts)
         {
            TypedXPath child = isAny
                  ? xPath.getChildXPath(part)
                  : safeGetXPath((assembled.length() == 0
                        ? assembled.append(part)
                        : assembled.append('/').append(part)).toString());
            if (child == null)
            {
               if (xPath.hasWildcards())
               {
                  child = getRootXPath(part);
                  isAny = true;
               }
            }
            xPath = child;
            if (xPath == null)
            {
               break;
            }
         }
      }
      return xPath;
   }

   @Override
   public TypedXPath resolve(QName xsiType, TypedXPath xPath)
   {
      TypedXPath other = getRootXPath(xsiType.toString());
      if (other != null)
      {
         return other;
      }
      return xPath;
   }

   private TypedXPath safeGetXPath(String xpath)
   {
      return containsXPath(xpath) ? getXPath(xpath) : null;
   }

   protected TypedXPath getRootXPath(String id)
   {
      TypedXPath xPath = null;

      if (roots == null)
      {
         roots = CollectionUtils.newMap();
      }
      if (roots.containsKey(id))
      {
         xPath = roots.get(id);
      }
      else
      {
         QName qId = QName.valueOf(id);
         Iterator<XPathProvider> provider = getXPathProviders();
         while (provider.hasNext())
         {
            XPathProvider declaration = provider.next();
            XpdlType type = declaration.getXpdlType();
            // (fh) do not force loading all external schemas.
            if (type instanceof ExternalReference)
            {
               String xref = ((ExternalReference) type).getXref();
               if (xref == null)
               {
                  continue;
               }
               int ix = xref.lastIndexOf("{");
               if (ix >= 0)
               {
                  xref = xref.substring(ix);
               }
               else
               {
                  ix = xref.lastIndexOf('/');
                  if (ix >= 0)
                  {
                     xref = xref.substring(ix + 1);
                  }
               }
               xref = xref.trim();
               if (xref.isEmpty())
               {
                  continue;
               }
               QName qName = QName.valueOf(xref);
               if (id.equals(qName.getLocalPart()))
               {
                  qId = new QName(qName.getNamespaceURI(), id);
               }
               else if (!qId.equals(qName))
               {
                  continue;
               }
            }
            if (type instanceof SchemaType || type instanceof ExternalReference)
            {
               Set<TypedXPath> paths = null;
               try
               {
                  paths = declaration.getAllXPaths();
               }
               catch (Exception ex)
               {
                  // (fh) just ignore and continue with next type declaration
               }
               if (paths != null && !paths.isEmpty())
               {
                  for (TypedXPath path : paths)
                  {
                     if (path.getXPath().isEmpty() && qId.getLocalPart().equals(path.getXsdElementName()) && qId.getNamespaceURI().equals(path.getXsdElementNs()))
                     {
                        xPath = path;
                        break;
                     }
                  }
               }
               if (xPath != null)
               {
                  break;
               }
            }
         }
         roots.put(qId.toString(), xPath);
      }

      return xPath;
   }

   protected abstract Iterator<XPathProvider> getXPathProviders();

   protected static interface XPathProvider
   {
      XpdlType getXpdlType();

      Set<TypedXPath> getAllXPaths();
   }
}
