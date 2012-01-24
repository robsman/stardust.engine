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
package org.eclipse.stardust.common.utils.xml;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Configurable, static namespace context.
 *
 * @author robert.sauer
 */
public class StaticNamespaceContext implements NamespaceContext
{
   private final Map<String, String> nsRegistry;

   private String defaultNs = XMLConstants.NULL_NS_URI;

   public StaticNamespaceContext()
   {
      this.nsRegistry = newHashMap();
   }

   public StaticNamespaceContext(Map<String, String> nsMappings)
   {
      this();

      for (Map.Entry<String, String> nsMapping : nsMappings.entrySet())
      {
         defineNamespace(nsMapping.getKey(), nsMapping.getValue());
      }
   }

   public void defineNamespace(String prefix, String namespaceURI)
   {
      if (null == prefix)
      {
         throw new IllegalArgumentException("Namespace prefix must not be null");
      }
      if (null == namespaceURI)
      {
         throw new IllegalArgumentException("Namespace URI must not be null");
      }

      if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix))
      {
         this.defaultNs = namespaceURI;
      }
      else if ( !XMLConstants.XML_NS_PREFIX.equals(prefix)
            && !XMLConstants.XML_NS_URI.equals(namespaceURI)
            && !XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)
            && !XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI))
      {
         nsRegistry.put(prefix, namespaceURI);
      }
   }

   public String getNamespaceURI(String prefix)
   {
      if (null == prefix)
      {
         throw new IllegalArgumentException("Namespace prefix must not be null");
      }

      if (XMLConstants.XML_NS_PREFIX.equals(prefix))
      {
         return XMLConstants.XML_NS_URI;
      }
      else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
      {
         return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
      }
      else if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix))
      {
         return defaultNs;
      }
      else if (nsRegistry.containsKey(prefix))
      {
         return nsRegistry.get(prefix);
      }
      else
      {
         return XMLConstants.NULL_NS_URI;
      }
   }

   public String getPrefix(String namespaceURI)
   {
      if (null == namespaceURI)
      {
         throw new IllegalArgumentException("Namespace URI must not be null");
      }

      if (XMLConstants.XML_NS_URI.equals(namespaceURI))
      {
         return XMLConstants.XML_NS_PREFIX;
      }
      else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI))
      {
         return XMLConstants.XMLNS_ATTRIBUTE;
      }
      else if ((null != defaultNs) && defaultNs.equals(namespaceURI))
      {
         return XMLConstants.DEFAULT_NS_PREFIX;
      }
      else
      {
         for (Map.Entry<String, String> mapping : nsRegistry.entrySet())
         {
            if ((null != mapping.getValue()) && mapping.getValue().equals(namespaceURI))
            {
               return mapping.getKey();
            }
         }

         return null;
      }
   }

   public Iterator getPrefixes(String namespaceURI)
   {
      if (null == namespaceURI)
      {
         throw new IllegalArgumentException("Namespace URI must not be null");
      }

      if (XMLConstants.XML_NS_URI.equals(namespaceURI))
      {
         return singletonList(XMLConstants.XML_NS_PREFIX).iterator();
      }
      else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI))
      {
         return singletonList(XMLConstants.XMLNS_ATTRIBUTE).iterator();
      }
      else
      {
         List<String> prefixes = newArrayList();

         if ((null != defaultNs) && defaultNs.equals(namespaceURI))
         {
            prefixes.add(XMLConstants.DEFAULT_NS_PREFIX);
         }

         for (Map.Entry<String, String> mapping : nsRegistry.entrySet())
         {
            if ((null != mapping.getValue()) && mapping.getValue().equals(namespaceURI))
            {
               prefixes.add(mapping.getKey());
            }
         }

         return unmodifiableList(prefixes).iterator();
      }
   }

}
