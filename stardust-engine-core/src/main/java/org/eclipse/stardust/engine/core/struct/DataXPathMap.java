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
package org.eclipse.stardust.engine.core.struct;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.RuntimeAttributeHolder;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataLoader;

public class DataXPathMap implements IXPathMap, Serializable
{
   private static final long serialVersionUID = 1632169933703923771L;

   private Map<Long, TypedXPath> oidToXPath;

   private Map<String, Long> xPathToOid;
   
   private Map<String, TypedXPath> xPathToTypedXPath;

   private Set<Long> allXPathOids;

   private final Set<TypedXPath> allXPaths;

   private IAccessPoint accessPoint;

   private Map<String, TypedXPath> roots;

   public static IXPathMap getXPathMap(AccessPoint accessPoint)
   {
      // XPathMap is lazy loaded
      IXPathMap xPathMap = null;
      RuntimeAttributeHolder rtah = accessPoint instanceof RuntimeAttributeHolder
         ? (RuntimeAttributeHolder) accessPoint : null;
      if (rtah != null)
      {
         xPathMap = rtah.getRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT);
      }
      if (xPathMap == null)
      {
         synchronized (accessPoint)
         {
            if (rtah != null)
            {
               xPathMap = rtah.getRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT);
            }
            if (xPathMap == null)
            {
               StructuredDataLoader structuredDataLoader = new StructuredDataLoader();
               if (accessPoint instanceof IData)
               {
                  IData data = (IData) accessPoint;
                  structuredDataLoader.loadData(data);
                  xPathMap = data.getRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT);
               }
               else
               {
                  xPathMap = structuredDataLoader.loadAccessPoint(accessPoint);
               }
            }
         }
      }
      return xPathMap;
   }

   public DataXPathMap(Map<Long, TypedXPath> xPaths)
   {
      this(xPaths, null);
   }

   public DataXPathMap(Map<Long, TypedXPath> xPaths, IAccessPoint accessPoint)
   {
      oidToXPath = new HashMap(xPaths);
      
      xPathToOid = new HashMap(xPaths.size());
      allXPathOids = new HashSet(xPaths.size());
      xPathToTypedXPath = new HashMap(xPaths.size());
      
      for (Entry<Long, TypedXPath> e : xPaths.entrySet())
      {
         Long oid = e.getKey();
         TypedXPath xPath = e.getValue();
         xPathToOid.put(xPath.getXPath(), oid);
         xPathToTypedXPath.put(xPath.getXPath(), xPath);
         allXPathOids.add(oid);
      }
      
      allXPaths = Collections.unmodifiableSet(new HashSet(oidToXPath.values()));
      this.accessPoint = accessPoint;
   }

   public TypedXPath getXPath(long xPathOID)
   {
      return oidToXPath.get(xPathOID);
   }
   
   public TypedXPath getXPath(String xPath)
   {
      TypedXPath typedPath = xPathToTypedXPath.get(xPath);
      if (typedPath == null)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.MDL_UNKNOWN_XPATH.raise(xPath));
      }
      
      return typedPath;
   }

   public Long getXPathOID(String xPath)
   {
      return xPathToOid.get(xPath);
   }

   public Set getAllXPaths()
   {
      return allXPaths;
   }

   public Long getRootXPathOID()
   {
      return xPathToOid.get("");
   }

   public TypedXPath getRootXPath()
   {
      return getXPath("");
   }
   
   public Set getAllXPathOids()
   {
      return Collections.unmodifiableSet(allXPathOids);
   }

   public boolean containsXPath(String xPath)
   {
      return xPathToOid.containsKey(xPath);
   }

   TypedXPath findXPath(List<String> parts)
   {
      TypedXPath xPath = null;
      if (parts != null)
      {
         xPath = xPathToTypedXPath.get("");
         StringBuffer assembled = new StringBuffer();
         boolean isAny = false;
         for (String part : parts)
         {
            TypedXPath child = isAny
                  ? xPath.getChildXPath(part)
                  : xPathToTypedXPath.get((assembled.length() == 0
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

   TypedXPath getRootXPath(String id)
   {
      TypedXPath xPath = null;
      if (accessPoint != null)
      {
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
            IModel model = (IModel) accessPoint.getModel();
            for (ITypeDeclaration declaration : model .getTypeDeclarations())
            {
               IXpdlType type = declaration.getXpdlType();
               // (fh) do not force loading all external schemas.
               if (type instanceof IExternalReference)
               {
                  String xref = ((IExternalReference) type).getXref();
                  if (xref == null)
                  {
                     continue;
                  }
                  int ix = xref.lastIndexOf('/');
                  if (ix >= 0)
                  {
                     xref = xref.substring(ix + 1);
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
               if (hasSchema(declaration))
               {
                  Set<TypedXPath> paths = null;
                  try
                  {
                     paths = StructuredTypeRtUtils.getAllXPaths(model, declaration);
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
      }
      return xPath;
   }

   private static boolean hasSchema(ITypeDeclaration declaration)
   {
      IXpdlType type = declaration.getXpdlType();
      return type instanceof ISchemaType || type instanceof IExternalReference;
   }
}
