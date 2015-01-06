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
package org.eclipse.stardust.engine.core.struct.sxml.converters;

import static org.eclipse.stardust.common.CompareHelper.areEqual;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.sxml.Attribute;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.MissingImplementationException;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.Text;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;


/**
 * DOM <--> Document conversion
 *
 * @author robert.sauer
 */
public class DOMConverter
{
   private static final Logger trace = LogManager.getLogger(DOMConverter.class);

   /**
    * @return a document reflecting the same content than the given DOM
    */
   public static org.eclipse.stardust.engine.core.struct.sxml.Document convert(org.w3c.dom.Document dom)
   {
      return new Document(convert(dom.getDocumentElement()));
   }

   /**
    * @return an element reflecting the same content than the given DOM element
    */
   public static org.eclipse.stardust.engine.core.struct.sxml.Element convert(org.w3c.dom.Element element)
   {
      Element result = new Element(getLocalElementName(element), element.getNamespaceURI(),
            element.getPrefix());

      copyChildren(element, result);

      return result;
   }

   /**
    * @return a DOM representation of the given document
    */
   public static org.w3c.dom.Document convert(org.eclipse.stardust.engine.core.struct.sxml.Document sxmlDoc)
   {
      return convert(sxmlDoc, null);
   }

   /**
    * @return a DOM representation of the given document, optionally built with the given
    *         DOM implementation
    */
   public static org.w3c.dom.Document convert(org.eclipse.stardust.engine.core.struct.sxml.Document sxmlDoc, DOMImplementation domImpl)
   {
      org.w3c.dom.Document domDoc = null;

      Element sxmlRoot = sxmlDoc.getRootElement();
      if (null != sxmlRoot)
      {
         org.w3c.dom.Element domRoot;
         if (null != domImpl)
         {
            domDoc = domImpl.createDocument( //
                  isEmpty(sxmlRoot.getNamespaceURI()) ? null : sxmlRoot.getNamespaceURI(), //
                  sxmlRoot.getLocalName(), null);

            domRoot = domDoc.getDocumentElement();
         }
         else
         {
            domDoc = XmlUtils.newDocument();
            if (isEmpty(sxmlRoot.getNamespaceURI()))
            {
               domRoot = domDoc.createElement(sxmlRoot.getLocalName());
            }
            else
            {
               domRoot = domDoc.createElementNS(sxmlRoot.getNamespaceURI(),
                     sxmlRoot.getLocalName());
               if ( !isEmpty(sxmlRoot.getNamespacePrefix()))
               {
                  domRoot.setPrefix(sxmlRoot.getNamespacePrefix());
               }
            }

            domDoc.appendChild(domRoot);
         }

         copyChildren(sxmlRoot, domRoot);
      }

      return domDoc;
   }

   private static void copyChildren(Element sxmlSrc, org.w3c.dom.Element domTarget)
   {
      org.w3c.dom.Document domDoc = domTarget.getOwnerDocument();

      for (int i = 0, nAttribs = sxmlSrc.getAttributeCount(); i < nAttribs; i++ )
      {
         Attribute attr = sxmlSrc.getAttribute(i);
         if (isEmpty(attr.getNamespaceURI()))
         {
            domTarget.setAttribute(attr.getLocalName(), attr.getValue());
         }
         else
         {
            domTarget.setAttributeNS(attr.getNamespaceURI(), attr.getQualifiedName(), attr.getValue());
         }
      }

      for (int i = 0, nChilds = sxmlSrc.getChildCount(); i < nChilds; i++ )
      {
         Node child = sxmlSrc.getChild(i);
         if (child instanceof Element)
         {
            Element sxmlChild = (Element) child;
            org.w3c.dom.Element domChild;
            if (isEmpty(sxmlChild.getNamespaceURI()))
            {
               domChild = domDoc.createElement(sxmlChild.getLocalName());
            }
            else
            {
               domChild = domDoc.createElementNS(sxmlChild.getNamespaceURI(),
                     sxmlChild.getLocalName());
               if ( !isEmpty(sxmlChild.getNamespacePrefix()))
               {
                  domChild.setPrefix(sxmlChild.getNamespacePrefix());
               }
            }
            // TODO review to avoid stack based recursion
            copyChildren(sxmlChild, domChild);

            domTarget.appendChild(domChild);
         }
         else if (child instanceof Text)
         {
            Text sxmlChild = (Text) child;

            org.w3c.dom.Text domChild = domDoc.createTextNode(sxmlChild.getValue());

            domTarget.appendChild(domChild);
         }
         else
         {
            throw new MissingImplementationException("Unexpected child node: " + child);
         }
      }
   }

   private static void copyChildren(org.w3c.dom.Element domSrc, Element xmlTarget)
   {
      org.w3c.dom.NamedNodeMap attrs = domSrc.getAttributes();
      if (null != attrs)
      {
         for (int i = 0, nAttrs = attrs.getLength(); i < nAttrs; i++ )
         {
            org.w3c.dom.Attr attr = (Attr) attrs.item(i);
            if ( !(areEqual("xmlns", attr.getName()) || areEqual("xmlns",
                  attr.getPrefix())))
            {
               // convert all attributes but namespace declarations
               xmlTarget.addAttribute(new Attribute(attr.getName(), attr.getNamespaceURI(),
                     attr.getValue()));
            }
         }
      }

      org.w3c.dom.Node child = domSrc.getFirstChild();
      while (null != child)
      {
         if (child instanceof org.w3c.dom.Element)
         {
            org.w3c.dom.Element domChild = (org.w3c.dom.Element) child;
            Element sxmlChild = new Element(getLocalElementName(domChild),
                  domChild.getNamespaceURI(), domChild.getPrefix());

            copyChildren(domChild, sxmlChild);

            xmlTarget.appendChild(sxmlChild);
         }
         else if (child instanceof org.w3c.dom.Text)
         {
            org.w3c.dom.Text domChild = (org.w3c.dom.Text) child;

            xmlTarget.appendChild(new Text(domChild.getTextContent()));
         }
         else
         {
            trace.debug("Ignoring DOM node during conversion: " + child);
         }

         child = child.getNextSibling();
      }
   }
   
   private static String getLocalElementName(org.w3c.dom.Element element)
   {
      return element.getLocalName() == null ? element.getNodeName() : element.getLocalName();
   }
}
