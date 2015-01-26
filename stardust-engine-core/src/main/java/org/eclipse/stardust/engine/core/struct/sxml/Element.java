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

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CompareHelper.areEqual;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.xml.stream.StaxUtils;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;


/**
 * XML element
 *
 * @author robert.sauer
 */
public class Element extends ParentNode implements NamedNode
{
   private final String localName;

   private final String nsUri;

   private final String nsPrefix;

   private List<Attribute> attribs = emptyList();

   /**
    * Creates a new element, without namespace.
    */
   public Element(String localName)
   {
      this(localName, "");
   }

   /**
    * Creates a new element, with namespace URI.
    */
   public Element(String localName, String nsUri)
   {
      this(localName, nsUri, "");
   }

   /**
    * Creates a new element, with namesapce URI and prefix.
    */
   public Element(String localName, String nsUri, String prefix)
   {
      this.localName = localName;
      this.nsUri = nsUri;
      this.nsPrefix = prefix;
   }

   @Override
   public String toString()
   {
      if (0 == getChildCount())
      {
         return "<" + getQualifiedName() + " />";
      }
      else
      {
         return "<" + getQualifiedName() + "> ... </" + getQualifiedName() + ">";
      }
   }

   @Override
   public Element copy()
   {
      Element copy = new Element(getLocalName(), getNamespaceURI(), getNamespacePrefix());

      for (int i = 0; i < getAttributeCount(); ++i)
      {
         copy.addAttribute(getAttribute(i).copy());
      }

      for (int i = 0; i < getChildCount(); ++i)
      {
         copy.appendChild(getChild(i).copy());
      }

      return copy;
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
         xmlWriter.flush();
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
      if ((1 == getChildCount()) && (getChild(0) instanceof Text))
      {
         return getChild(0).getValue();
      }
      else
      {
         StringBuilder buffer = new StringBuilder();
         for (int i = 0; i < getChildCount(); i++ )
         {
            buffer.append(getChild(i).getValue());
         }

         return buffer.toString();
      }
   }

   @Override
   protected boolean isValidChild(Node child)
   {
      return (child instanceof Element) || (child instanceof Text);
   }

   public String getLocalName()
   {
      return localName;
   }

   public String getQualifiedName()
   {
      return isEmpty(getNamespacePrefix()) //
            ? getLocalName()
            : getNamespacePrefix() + ":" + getLocalName();
   }

   public String getNamespaceURI()
   {
      return (null != nsUri) ? nsUri : "";
   }

   public String getNamespacePrefix()
   {
      return (null != nsPrefix) ? nsPrefix : "";
   }

   /**
    * @return an unmodifiable list of all child elements
    */
   public List<Element> getChildElements()
   {
      List<Element> elements = newArrayList();
      for (int i = 0; i < getChildCount(); ++i)
      {
         Node child = getChild(i);
         if (child instanceof Element)
         {
            elements.add((Element) child);
         }
      }
      return unmodifiableList(elements);
   }

   /**
    * @return an unmodifiable list of child elements matching the given name
    */
   public List<Element> getChildElements(String localName)
   {
      return getChildElements(localName, "");
   }

   /**
    * @return an unmodifiable list of child elements matching the given name and namespace
    *         URI
    */
   public List<Element> getChildElements(String localName, String nsUri)
   {
      List<Element> elements = newArrayList();
      for (int i = 0; i < getChildCount(); ++i)
      {
         Node child = getChild(i);
         if (child instanceof Element)
         {
            Element element = (Element) child;
            if (areEqual(element.getLocalName(), localName)
                  && areEqual(element.getNamespaceURI(), nsUri))
            {
               elements.add(element);
            }
         }
      }

      return unmodifiableList(elements);
   }

   /**
    * @return the first child element matching the given name, or null
    */
   public Element getFirstChildElement(String localName)
   {
      return getFirstChildElement(localName, "");
   }

   /**
    * @return the first child element matching the given name and namespace URI, or null
    */
   public Element getFirstChildElement(String localName, String nsUri)
   {
      for (int i = 0; i < getChildCount(); ++i)
      {
         Node child = getChild(i);
         if (child instanceof Element)
         {
            Element element = (Element) child;
            if (areEqual(element.getLocalName(), localName)
                  && areEqual(element.getNamespaceURI(), nsUri))
            {
               return element;
            }
         }
      }

      return null;
   }

   /**
    * @return the number of the element's attributes
    */
   public int getAttributeCount()
   {
      return attribs.size();
   }

   /**
    * @return the element's attribute at the given position
    */
   public Attribute getAttribute(int pos)
   {
      return attribs.get(pos);
   }

   /**
    * @return the element's attribute matching the given name
    */
   public Attribute getAttribute(String name)
   {
      return getAttribute(name, "");
   }

   /**
    * @return the element's attribute matching the given name and namespace URI
    */
   public final Attribute getAttribute(String name, String nsURI)
   {
      for (Attribute attrib : attribs)
      {
         if (areEqual(name, attrib.getLocalName())
               && areEqual(nsURI, attrib.getNamespaceURI()))
         {
            return attrib;
         }
      }

      return null;
   }

   /**
    * @return the value of the element's attribute matching the given name, or null
    */
   public String getAttributeValue(String name)
   {
      Attribute attr = getAttribute(name);

      return (null != attr) ? attr.getValue() : null;
   }

   /**
    * @return the value of the element's attribute matching the given name and namespace
    *         URI, or null
    */
   public String getAttributeValue(String name, String nsURI)
   {
      Attribute attr = getAttribute(name, nsURI);

      return (null != attr) ? attr.getValue() : null;
   }

   /**
    * Adds the given attribute to the list, possibly replacing an existing attribute
    * matching the new attribute's name and namespace URI.
    */
   public void addAttribute(Attribute attr)
   {
      if (null != attr.getParent())
      {
         throw new PublicException(BpmRuntimeError.SDT_ATTRIBUTE_MUST_BE_DETACHED.raise());
      }

      if (attribs.isEmpty())
      {
         this.attribs = newArrayList();
      }

      for (int i = 0; i < attribs.size(); ++i)
      {
         Attribute attrib = attribs.get(i);
         if (areEqual(attrib.getLocalName(), attr.getLocalName())
               && areEqual(attrib.getNamespaceURI(), attr.getNamespaceURI()))
         {
            attrib.setParent(null);
            attribs.set(i, attr);
            attr.setParent(this);
            return;
         }
      }
      attribs.add(attr);
      attr.setParent(this);
   }

   /**
    * Removes the given attribute from the list.
    */
   void removeAttribute(Attribute attr)
   {
      int pos = attribs.indexOf(attr);
      if ( -1 == pos)
      {
         throw new PublicException(BpmRuntimeError.SDT_NO_SUCH_ATTRIBUTE.raise(attr));
      }
      else
      {
         attribs.remove(pos);
         attr.setParent(null);
      }
   }

   /**
    * Removes all child nodes from this element.
    */
   public void removeChildren()
   {
      for (int i = 0; i < getChildCount(); ++i)
      {
         Node child = getChild(i);
         child.setParent(null);
      }

      clearChildren();
   }

   @Override
   void toXML(XMLStreamWriter xmlWriter) throws XMLStreamException
   {
      xmlWriter.writeStartElement(getNamespacePrefix(), getLocalName(), getNamespaceURI());

      // write attribute values
      for (int i = 0; i < getAttributeCount(); i++ )
      {
         getAttribute(i).toXML(xmlWriter);
      }

      super.toXML(xmlWriter);

      xmlWriter.writeEndElement();
   }
}
