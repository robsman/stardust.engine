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
package org.eclipse.stardust.engine.ws.processinterface;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * This class provides some convenience methods for
 * working with XML DOM.
 * </p>
 *
 * @author Roland.Stamm
 */
public class DomUtils
{
   public static Element retrieveElementByXPath(final Node node, final String xPathQuery, final NamespaceContext xPathContext)
   {
      NodeList nodes = retrieveElementsByXPath(node, xPathQuery, xPathContext);

      ensureUniqueNode(nodes, xPathQuery);
      return (Element) nodes.item(0);
   }

   public static NodeList retrieveElementsByXPath(Node node, String xPathQuery,
         NamespaceContext xPathContext)
   {
      try
      {
         XPathFactory factory = XPathFactory.newInstance();
         XPath xpath = factory.newXPath();
         xpath.setNamespaceContext(xPathContext);
         XPathExpression expr = xpath.compile(xPathQuery);

         Object result = expr.evaluate(node, XPathConstants.NODESET);
         return (NodeList) result;
      }
      catch (XPathExpressionException e)
      {
         throw new XomException("Error in XPath query " + xPathQuery, e);
      }
   }

   public static void ensureUniqueNode(final NodeList nodes, final String xPathQuery)
   {
      if (nodes.getLength() != 1)
      {
         final String cause = nodes.getLength() < 1 ? "Not found" : "Ambiguity";
         throw new XomException("Unable to get " + xPathQuery + " (" + cause + ").");
      }
   }

   public static void appendAttributeValuePostfix(final Element element, final String attributeName, final String postfix)
   {
      final String newValue = element.getAttribute(attributeName) + postfix;
      element.setAttribute(attributeName, newValue);
   }

   public static Document createDocument(final InputStream in, final String docName)
   {
//      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//      Document doc = docBuilder.parse(in);
//      return doc;
      return XmlUtils.parseStream(in, true);
   }

   public static String formatDocument(final Document doc, final int indentSize, final String encoding)
   {
//      StreamResult result;
//      try
//      {
//         Transformer transformer = TransformerFactory.newInstance().newTransformer();
//
//         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//         transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
//
//         // initialize StreamResult with File object to save to file
//         result = new StreamResult(new StringWriter());
//         DOMSource source = new DOMSource(doc);
//
//         transformer.transform(source, result);
//         return result.getWriter().toString();
//      }
//      catch (TransformerConfigurationException e)
//      {
//        throw new XomException("Error transforming DOM", e);
//      }
//      catch (TransformerFactoryConfigurationError e)
//      {
//         throw new XomException("Error transforming DOM", e);
//      }
//      catch (TransformerException e)
//      {
//         throw new XomException("Error transforming DOM", e);
//      }
      Properties properties  = new Properties();
      properties.put(OutputKeys.ENCODING, encoding);
      properties.put(OutputKeys.INDENT, "yes");
      properties.put("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indentSize));
      return XmlUtils.toString(doc, properties);
   }

   private DomUtils()
   {
      /* utility class */
   }

   private static final class XomException extends RuntimeException
   {
      private static final long serialVersionUID = 294856919750466372L;

      public XomException(final String msg)
      {
         super(msg);
      }

      public XomException(final String msg, final Throwable e)
      {
         super(msg, e);
      }
   }

   public static Element getFirstChildElement(Element element)
   {
      Node node = element.getFirstChild();

      if (node instanceof Element)
      {
         return (Element) node;
      }
      else
      {
         Node nextSibling = node.getNextSibling();
         while (nextSibling != null)
         {
            if (nextSibling instanceof Element)
            {
               return (Element) nextSibling;
            }
            else
            {
               nextSibling = nextSibling.getNextSibling();
            }
         }
      }
      return null;
   }

   public static List<Element> getChildElements(Element element)
   {
      List<Element> elements = new ArrayList<Element>();
      NodeList childNodes = element.getChildNodes();

      for (int i = 0; i < childNodes.getLength(); i++ )
      {
         Node node = childNodes.item(i);
         if (node instanceof Element)
         {
            elements.add((Element) node);
         }
      }
      return elements;
   }
}
