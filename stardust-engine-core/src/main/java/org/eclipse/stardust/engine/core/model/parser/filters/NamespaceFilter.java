/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.parser.filters;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Filter to replace/fix namespaces.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class NamespaceFilter
{
   private static XMLInputFactory inputFactory = null;

   static synchronized XMLInputFactory getXMLInputFactory()
   {
      if (inputFactory == null)
      {
         try
         {
            inputFactory = XMLInputFactory.newFactory();
         }
         catch (NoSuchMethodError error)
         {
            // fallback to old newInstance method
            inputFactory = XMLInputFactory.newInstance();
         }
      }
      return inputFactory;
   }

   public static XMLFilter createXMLFilter(String replacementUri, String... namespaceUri) throws SAXException
   {
      return createXMLFilter(new NamespaceFilter(replacementUri, namespaceUri));
   }

   public static XMLFilter createXMLFilter(NamespaceFilter filter) throws SAXException
   {
      return createXMLFilter(filter, XMLReaderFactory.createXMLReader());
   }

   public static XMLFilter createXMLFilter(NamespaceFilter filter, XMLReader reader) throws SAXException
   {
      return new NamespaceXMLFilter(filter, reader);
   }

   public static XMLStreamReader createXMLStreamReader(Source source, String replacementUri, String... namespaceUri) throws XMLStreamException
   {
      return createXMLStreamReader(new NamespaceFilter(replacementUri, namespaceUri), source);
   }

   public static XMLStreamReader createXMLStreamReader(NamespaceFilter filter, Source source) throws XMLStreamException
   {
      return createXMLStreamReader(filter, createXMLStreamReader(source));
   }

   public static XMLStreamReader createXMLStreamReader(NamespaceFilter filter, XMLStreamReader reader)
   {
      return new NamespaceStreamReaderDelegate(filter, reader);
   }

   private static XMLStreamReader createXMLStreamReader(Source source)
         throws XMLStreamException
   {
      try
      {
         return getXMLInputFactory().createXMLStreamReader(source);
      }
      catch (RuntimeException ex)
      {
         if (!(source instanceof DOMSource))
         {
            throw ex;
         }
      }
      catch (XMLStreamException stex)
      {
         if (!(source instanceof DOMSource))
         {
            throw stex;
         }
      }
      return createXMLStreamReader(toStreamSource((DOMSource) source));
   }

   private static Source toStreamSource(DOMSource source)
   {
      Node node = source.getNode();
      String xml = XmlUtils.toString(node);
      return new StreamSource(new StringReader(xml));
   }

   private Map<String, String> replacedNamespaces = new HashMap<String, String>();

   public NamespaceFilter()
   {}

   public NamespaceFilter(String replacementUri, String... namespaceUri)
   {
      if (namespaceUri != null)
      {
         for (String uri : namespaceUri)
         {
            replacedNamespaces.put(uri, replacementUri);
         }
      }
   }

   /**
    * Specifies a replacement namespace uri.<br>
    * <br>
    * Example:
    * <code>setReplacement("http://www.wfmc.org/2002/XPDL1.0",
    *       "http://www.wfmc.org/2008/XPDL2.1")</code>
    * will specify that all occurrences of the XPDL 1.0 namespace uri should be replaced with the XPDL 2.1 namespace uri.<br>
    * <br>
    * This method can be used to specify more than one replacement.
    * <br>
    * @param namespaceUri the namespace to be replaced.
    * @param replacementUri the replacement uri.
    */
   public void addReplacement(String namespaceUri, String replacementUri)
   {
      replacedNamespaces.put(namespaceUri, replacementUri);
   }

   private String replace(String uri)
   {
      String replacement = replacedNamespaces.get(uri);
      return replacement == null ? uri : replacement;
   }

   private static class NamespaceXMLFilter extends XMLFilterImpl
   {
      private NamespaceFilter filter;

      private NamespaceXMLFilter(NamespaceFilter filter, XMLReader parent)
      {
         super(parent);
         this.filter = filter;
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
      {
         super.startElement(filter.replace(uri), localName, qName, atts);
      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException
      {
         super.endElement(filter.replace(uri), localName, qName);
      }

      @Override
      public void startPrefixMapping(String prefix, String uri) throws SAXException
      {
         super.startPrefixMapping(prefix, filter.replace(uri));
      }
   }

   private static class NamespaceStreamReaderDelegate extends StreamReaderDelegate
   {
      private NamespaceFilter filter;

      public NamespaceStreamReaderDelegate(NamespaceFilter filter, XMLStreamReader reader)
      {
         super(reader);
         this.filter = filter;
      }

      @Override
      public NamespaceContext getNamespaceContext()
      {
         return null;
         //return super.getNamespaceContext();
      }

      @Override
      public void require(int type, String namespaceURI, String localName)
            throws XMLStreamException
      {
         throw new UnsupportedOperationException();
         //super.require(type, namespaceURI, localName);
      }

      @Override
      public String getNamespaceURI(String prefix)
      {
         return filter.replace(super.getNamespaceURI(prefix));
      }

      @Override
      public String getAttributeNamespace(int index)
      {
         return filter.replace(super.getAttributeNamespace(index));
      }

      @Override
      public String getNamespaceURI(int index)
      {
         return filter.replace(super.getNamespaceURI(index));
      }

      @Override
      public String getNamespaceURI()
      {
         return filter.replace(super.getNamespaceURI());
      }
   }
}