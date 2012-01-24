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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.model.utils.RuntimeAttributeHolder;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataLoader;



/**
 *  
 */
public class DataXPathMap implements IXPathMap, Serializable
{
   
   private static final long serialVersionUID = 1632169933703923771L;

   private Map /*<Long,TypedXPath>*/ oidToXPath;

   private Map /*<String,Long>*/ xPathToOid;
   
   private Map /*<String,TypedXPath>*/ xPathToTypedXPath;

   private Set /*<Long>*/ allXPathOids;

   private final Set /*<TypedXPath>*/ allXPaths;
   
   public static IXPathMap getXPathMap(AccessPoint accessPoint)
   {
      // XPathMap is lazy loaded
      IXPathMap xPathMap = null;
      RuntimeAttributeHolder rtah = accessPoint instanceof RuntimeAttributeHolder
         ? (RuntimeAttributeHolder) accessPoint : null;
      if (rtah != null)
      {
         xPathMap = (IXPathMap) rtah.getRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT);
      }
      if (xPathMap == null)
      {
         synchronized (accessPoint)
         {
            if (rtah != null)
            {
               xPathMap = (IXPathMap) rtah.getRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT);
            }
            if (xPathMap == null)
            {
               StructuredDataLoader structuredDataLoader = new StructuredDataLoader();
               if (accessPoint instanceof IData)
               {
                  IData data = (IData)accessPoint;
                  structuredDataLoader.loadData(data);
                  xPathMap = (IXPathMap) data.getRuntimeAttribute(StructuredDataLoader.ALL_DATA_XPATHS_ATT);
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

   public DataXPathMap(Map /*<Long,TypedXPath>*/xPaths)
   {
      this.oidToXPath = new HashMap(xPaths);
      
      this.xPathToOid = new HashMap(xPaths.size());
      this.allXPathOids = new HashSet(xPaths.size());
      this.xPathToTypedXPath = new HashMap(xPaths.size());
      
      for (Iterator i = xPaths.entrySet().iterator(); i.hasNext();)
      {
         Entry e = (Entry)i.next();
         Long oid = (Long) e.getKey();
         TypedXPath xPath = (TypedXPath) e.getValue();
         xPathToOid.put(xPath.getXPath(), oid);
         xPathToTypedXPath.put(xPath.getXPath(), xPath);
         allXPathOids.add(oid);
      }
      
      this.allXPaths = Collections.unmodifiableSet(new HashSet(this.oidToXPath.values()));
   }

   public TypedXPath getXPath(long xPathOID)
   {
      return (TypedXPath) this.oidToXPath.get(new Long(xPathOID));
   }
   
   public TypedXPath getXPath(String xPath)
   {
      TypedXPath typedPath = (TypedXPath) this.xPathToTypedXPath.get(xPath);
      if (typedPath == null)
      {
         throw new IllegalOperationException(
               BpmRuntimeError.MDL_UNKNOWN_XPATH.raise(xPath));
      }
      
      return typedPath;
   }

   public Long getXPathOID(String xPath)
   {
      return (Long) this.xPathToOid.get(xPath);
   }

   public Set getAllXPaths()
   {
      return this.allXPaths;
   }

   public Long getRootXPathOID()
   {
      return (Long) this.xPathToOid.get("");
   }

   public TypedXPath getRootXPath()
   {
      return this.getXPath("");
   }
   
   public Set getAllXPathOids()
   {
      return Collections.unmodifiableSet(this.allXPathOids);
   }

   public boolean containsXPath(String xPath)
   {
      return this.xPathToOid.containsKey(xPath);
   }

}
