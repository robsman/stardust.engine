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
package org.eclipse.stardust.engine.core.struct.sxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.xml.stream.StaxUtils;


/**
 * Factory class to create documents from an external XML representation.
 *
 * @author robert.sauer
 */
public class DocumentBuilder
{
   private static final Logger trace = LogManager.getLogger(DocumentBuilder.class);

   /**
    * @return the parsed document
    */
   public static Document buildDocument(Reader reader) throws PublicException, IOException
   {
      Document doc = null;
      try
      {
         XMLInputFactory inputFactory = StaxUtils.getXmlInputFactory();
         XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);

         try
         {
            doc = buildDocument(xmlReader);
         }
         finally
         {
            xmlReader.close();
         }
      }
      catch (XMLStreamException e)
      {
         throw new PublicException("Failed parsing XML document", e);
      }

      return doc;
   }

   /**
    * @return the parsed document
    */
   public static Document buildDocument(InputStream is) throws PublicException, IOException
   {
      Document doc = null;
      try
      {
         XMLInputFactory inputFactory = StaxUtils.getXmlInputFactory();
         XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(is);

         try
         {
            doc = buildDocument(xmlReader);
         }
         finally
         {
            xmlReader.close();
         }
      }
      catch (XMLStreamException e)
      {
         throw new PublicException("Failed parsing XML document", e);
      }

      return doc;
   }

   /**
    * Parses the XML via StAX.
    */
   static Document buildDocument(XMLStreamReader xmlReader) throws XMLStreamException, IOException
   {
      Document doc = null;

      Stack<Element> elements = new Stack<Element>();
      while (xmlReader.hasNext())
      {
         int eventType = xmlReader.next();
         switch (eventType)
         {
         case XMLEvent.START_DOCUMENT:
            doc = null;
            break;

         case XMLEvent.START_ELEMENT:
            // StartElement startElement = event.asStartElement();

            Element subElement;
            if (null != xmlReader.getPrefix())
            {
               subElement = new Element(xmlReader.getLocalName(),
                     xmlReader.getNamespaceURI(), xmlReader.getPrefix());
            }
            else
            {
               subElement = new Element(xmlReader.getLocalName(),
                     xmlReader.getNamespaceURI());
            }

            if (elements.isEmpty())
            {
               doc = new Document(subElement);
            }
            else
            {
               elements.peek().appendChild(subElement);
            }

            for (int i = 0; i < xmlReader.getAttributeCount(); ++i)
            {
               if (null != xmlReader.getAttributePrefix(i))
               {
                  subElement.addAttribute(new org.eclipse.stardust.engine.core.struct.sxml.Attribute(
                        xmlReader.getAttributeLocalName(i),
                        xmlReader.getAttributeNamespace(i),
                        xmlReader.getAttributePrefix(i), xmlReader.getAttributeValue(i)));
               }
               else
               {
                  subElement.addAttribute(new org.eclipse.stardust.engine.core.struct.sxml.Attribute(
                        xmlReader.getAttributeLocalName(i),
                        xmlReader.getAttributeNamespace(i), xmlReader.getAttributeValue(i)));
               }
            }

            elements.push(subElement);
            break;

         case XMLEvent.CHARACTERS:
            org.eclipse.stardust.engine.core.struct.sxml.Text text = new org.eclipse.stardust.engine.core.struct.sxml.Text(xmlReader.getText());
            if ( !elements.isEmpty())
            {
               elements.peek().appendChild(text);
            }
            else
            {
               trace.info("Ignoring text placed outside of any element: " + xmlReader.getText());
            }
            break;

         case XMLEvent.SPACE:
            trace.debug("Ignoring whitespace inside XML.");
            break;

         case XMLEvent.END_ELEMENT:
            elements.pop();
            break;

         case XMLEvent.END_DOCUMENT:
            break;

         default:
            trace.error("Unsupported StAX event type: " + eventType);
         }
      }

      return doc;
   }

}
