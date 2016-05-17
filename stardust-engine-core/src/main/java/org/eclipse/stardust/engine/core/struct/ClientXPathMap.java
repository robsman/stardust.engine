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

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.model.XpdlType;

public class ClientXPathMap extends XPathMapResolver
      implements IXPathMap, Serializable
{
   private static final long serialVersionUID = -4818442739246293253L;

   private Map<String, TypedXPath> typedXPaths;

   private Set<TypedXPath> allXPaths;

   private Model model;

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

   /**
    *
    * @param allXPaths
    *
    * @deprecated
    */
   public ClientXPathMap (Set/*<TypedXPath>*/ allXPaths)
   {
      this(allXPaths, null);
   }

   public ClientXPathMap (Set<TypedXPath> allXPaths, Model model)
   {
      this.model = model;
      this.allXPaths = Collections.unmodifiableSet(allXPaths);
      typedXPaths = new HashMap();
      for (Iterator i = allXPaths.iterator(); i.hasNext();)
      {
         TypedXPath p = (TypedXPath) i.next();
         typedXPaths.put(p.getXPath(), p);
      }
   }

   public Set getAllXPaths()
   {
      return allXPaths;
   }

   public TypedXPath getXPath(long pathOID)
   {
      throw new RuntimeException("Client XPath map does not know XPath OIDs");
   }

   public TypedXPath getXPath(String path)
   {
      return (TypedXPath) typedXPaths.get(path);
   }

   public TypedXPath getRootXPath()
   {
      return getXPath("");
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
      return typedXPaths.containsKey(xPath);
   }

   @Override
   protected Iterator<XPathProvider> getXPathProviders()
   {
      if (model == null)
      {
         return Collections.<XPathProvider>emptyList().iterator();
      }
      final Iterator<TypeDeclaration> declarations = model.getAllTypeDeclarations().iterator();
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
            final TypeDeclaration declaration = declarations.next();
            return new XPathProvider()
            {
               @Override
               public XpdlType getXpdlType()
               {
                  return declaration.getXpdlType();
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
