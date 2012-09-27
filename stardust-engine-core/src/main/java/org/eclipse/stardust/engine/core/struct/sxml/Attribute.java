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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML attribute
 *
 * @author robert.sauer
 */
public class Attribute extends LeafNode implements NamedNode
{
   private final String localName;

   private final String nsUri;

   private final String nsPrefix;

   private String value;

   /**
    * Creates an attribute without namespace.
    */
   public Attribute(String localName, String value)
   {
      this(localName, "", value);
   }

   /**
    * Creates an attribute with namespace.
    */
   public Attribute(String localName, String nsUri, String value)
   {
      int idxPrefixSeparator = localName.indexOf(":");
      if (-1 == idxPrefixSeparator)
      {
         this.localName = localName;
         this.nsPrefix = null;
      }
      else
      {
         this.localName = localName.substring(idxPrefixSeparator + 1);
         this.nsPrefix = localName.substring(0, idxPrefixSeparator);
      }
      this.nsUri = nsUri;
      this.value = value;
   }

   /**
    * Creates an attribute with namespace and prefix.
    */
   Attribute(String localName, String nsUri, String nsPrefix, String value)
   {
      this.localName = localName;
      this.nsUri = nsUri;
      this.nsPrefix = nsPrefix;
      this.value = value;
   }

   @Override
   public String toString()
   {
      return toXML();
   }

   @Override
   public Attribute copy()
   {
      return new Attribute(getQualifiedName(), getNamespaceURI(), getValue());
   }

   @Override
   public String toXML()
   {
      return getQualifiedName() + "=\"" + getValue() + "\"";
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

   @Override
   public String getValue()
   {
      return value;
   }

   /**
    * Set the attribute's value (must not be null).
    */
   public void setValue(String value)
   {
      if (null == value)
      {
         throw new NullPointerException("Attribute value must not be null.");
      }

      this.value = value;
   }

   @Override
   void toXML(XMLStreamWriter xmlWriter) throws XMLStreamException
   {
      String val = getValue();
      if(val == null)
      {
         val = "";
      }
      
      xmlWriter.writeAttribute(getNamespacePrefix(), getNamespaceURI(), getLocalName(),
            val);
   }
}
