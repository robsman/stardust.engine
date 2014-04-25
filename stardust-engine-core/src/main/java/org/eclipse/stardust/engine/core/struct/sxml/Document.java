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

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.xml.stream.StaxUtils;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter;



/**
 * XML document
 *
 * @author robert.sauer
 */
public class Document extends ParentNode
{
   /**
    * Creates a new document, containing the provided element (must not be null) as its
    * root.
    */
   public Document(Element rootElement)
   {
      if (null == rootElement)
      {
         throw new NullPointerException("Root element must not be null.");
      }
      if (rootElement.getParent() != null)
      {
         throw new InternalException("Element must be detached.");
      }

      appendChild(rootElement);
   }

   @Override
   public Node copy()
   {
      return new Document(getRootElement().copy());
   }

   @Override
   public String toXML()
   {
      StringWriter xmlBuffer = new StringWriter();
      try
      {
         XMLOutputFactory outputFactory = StaxUtils.getXmlOutputFactory(true);
         XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(xmlBuffer);

         toXML(xmlWriter);
      }
      catch (XMLStreamException e)
      {
         throw new PublicException(BpmRuntimeError.SDT_FAILED_GENERATING_XML.raise(), e);
      }
      finally
      {
         xmlBuffer.flush();
      }

      return xmlBuffer.toString();
   }

   @Override
   public String getValue()
   {
      return getRootElement().getValue();
   }

   @Override
   protected boolean isValidChild(Node child)
   {
      return (child instanceof Element) && ((0 == getChildCount()) || (null == getRootElement()));
   }

   /**
    * @return the document's root element
    */
   public Element getRootElement() throws NullPointerException
   {
      for (int i = 0; i < getChildCount(); ++i)
      {
         Node child = getChild(i);
         if (child instanceof Element)
         {
            return (Element) child;
         }
      }

      throw new NullPointerException("Root element must not be null.");
   }

   @Override
   void toXML(XMLStreamWriter xmlWriter) throws XMLStreamException
   {
      xmlWriter.writeStartDocument();

      super.toXML(xmlWriter);

      xmlWriter.writeEndDocument();
   }

   /**
    * @return a DOM representation of this document
    */
   public org.w3c.dom.Document toDOM()
   {
      org.w3c.dom.Document domRepresentation = DOMConverter.convert(this);

      return domRepresentation;
   }
}
