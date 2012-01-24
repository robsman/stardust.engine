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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.TypedXPath;


/**
 * Gives access to xpaths of one data
 */
public class TestXPathMap implements IXPathMap
{

   private Map /* <Long,TypedXPath> */oidToXPath = new HashMap();

   private Map /* <String,Long> */xPathToOid = new HashMap();

   private Set /* <Long> */ allXPathOids = new HashSet();
   
   public TestXPathMap(Set /* <TypedXPath> */xPaths)
   {
      long xPathOid = 0;
      for (Iterator i = xPaths.iterator(); i.hasNext();)
      {
         TypedXPath xPath = (TypedXPath) i.next();
         Long oid = new Long(xPathOid);
         oidToXPath.put(oid, xPath);
         xPathToOid.put(xPath.getXPath(), oid);
         allXPathOids.add(oid);
         xPathOid++ ;
      }
   }

   public TypedXPath getXPath(long xPathOID)
   {
      return (TypedXPath) this.oidToXPath.get(new Long(xPathOID));
   }

   public TypedXPath getXPath(String xPath)
   {
      Long xPathOID = getXPathOID(xPath);
      if (xPathOID == null)
      {
         throw new PublicException("XPath '" + xPath + "' is not defined");
      }

      return (TypedXPath) this.oidToXPath.get(xPathOID);
   }

   public Long getXPathOID(String xPath)
   {
      return (Long) this.xPathToOid.get(xPath);
   }

   public Set getAllXPaths()
   {
      return Collections.unmodifiableSet(new HashSet(this.oidToXPath.values()));
   }

   public Long getRootXPathOID()
   {
      return this.getXPathOID("");
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
