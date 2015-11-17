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

import org.eclipse.stardust.common.RuntimeAttributeHolder;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataLoader;
import org.eclipse.xsd.XSDSchema;

public class DataXPathMap extends XPathMapResolver
      implements IXPathMap, Serializable
{
   private static final long serialVersionUID = 1632169933703923771L;

   private Map<Long, TypedXPath> oidToXPath;

   private Map<String, Long> xPathToOid;

   private Map<String, TypedXPath> xPathToTypedXPath;

   private Set<Long> allXPathOids;

   private final Set<TypedXPath> allXPaths;

   private IModel model;

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

   @Deprecated
   public DataXPathMap(Map<Long, TypedXPath> xPaths)
   {
      this(xPaths, (IModel) null);
   }

   public DataXPathMap(Map<Long, TypedXPath> xPaths, IAccessPoint accessPoint)
   {
      this(xPaths, accessPoint == null ? null : (IModel) accessPoint.getModel());
   }

   public DataXPathMap(Map<Long, TypedXPath> xPaths, IModel model)
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
      this.model = model;
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
         throw new IllegalOperationException(
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

   @Override
   protected Iterator<XPathProvider> getXPathProviders()
   {
      if (model == null)
      {
         return Collections.<XPathProvider>emptyList().iterator();
      }
      final Iterator<ITypeDeclaration> declarations = model.getTypeDeclarations().iterator();
      return new Iterator<XPathProvider>()
      {
         @Override
         public boolean hasNext()
         {
            return declarations.hasNext();
         }

         @Override
         public XPathProvider next()
         {
            final ITypeDeclaration declaration = declarations.next();
            return new XPathProvider()
            {
               @Override
               public XpdlType getXpdlType()
               {
                  IXpdlType type = declaration.getXpdlType();
                  if (type instanceof IExternalReference)
                  {
                     final IExternalReference ref = (IExternalReference) type;
                     return new ExternalReference()
                     {
                        @Override
                        public String getLocation()
                        {
                           return ref.getLocation();
                        }

                        @Override
                        public String getNamespace()
                        {
                           return ref.getNamespace();
                        }

                        @Override
                        public String getXref()
                        {
                           return ref.getXref();
                        }

                        @Override
                        public XSDSchema getSchema(Model model)
                        {
                           throw new UnsupportedOperationException();
                        }
                     };
                  }
                  else if (type instanceof ISchemaType)
                  {
                     //final ISchemaType schema = (ISchemaType) type;
                     return new SchemaType()
                     {
                        @Override
                        public XSDSchema getSchema()
                        {
                           throw new UnsupportedOperationException();
                        }
                     };
                  }
                  return null;
               }

               @Override
               public Set<TypedXPath> getAllXPaths()
               {
                  return StructuredTypeRtUtils.getAllXPaths(model, declaration);
               }
            };
         }

         @Override
         public void remove()
         {
            throw new UnsupportedOperationException();
         }
      };
   }
}
