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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.Model;



public class ClientXPathMap implements IXPathMap, Serializable
{

   private static final long serialVersionUID = -4818442739246293253L;
   
   private Map /*<String,TypedXPath>*/ typedXPaths;
   private Set /*<TypedXPath>*/ allXPaths;
   
   /**
    * Retrieves the XPath map corresponding to the data argument.
    * 
    * @param model the model containing the type declaration the data is referring to.
    * @param data the data object for which we want to retrive the xpath map.
    * @return the XPath map (may be empty).
    */
   public static IXPathMap getXpathMap(Model model, Data data)
   {
      return StructuredTypeRtUtils.getXPathMap(model, data); 
   }
   
   public ClientXPathMap (Set /*<TypedXPath>*/ allXPaths)
   {
      this.allXPaths = Collections.unmodifiableSet(allXPaths);
      this.typedXPaths = new HashMap();
      for (Iterator i = allXPaths.iterator(); i.hasNext();)
      {
         TypedXPath p = (TypedXPath) i.next();
         typedXPaths.put(p.getXPath(), p);
      }
   }
   
   public Set getAllXPaths()
   {
      return this.allXPaths;
   }

   public TypedXPath getXPath(long pathOID)
   {
      throw new RuntimeException("Client XPath map does not know XPath OIDs");
   }

   public TypedXPath getXPath(String path)
   {
      return (TypedXPath) this.typedXPaths.get(path);
   }

   public TypedXPath getRootXPath()
   {
      return this.getXPath("");
   }
   
   public Long getXPathOID(String path)
   {
      throw new RuntimeException("Client XPath map does not know XPath OIDs");
   }

   public Long getRootXPathOID()
   {
      throw new RuntimeException("Client XPath map does not know XPath OIDs");
   }
   
   public Set getAllXPathOids()
   {
      throw new RuntimeException("Client XPath map does not know XPath OIDs");
   }

   public boolean containsXPath(String xPath)
   {
      return this.typedXPaths.containsKey(xPath);
   }

}
