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
package org.eclipse.stardust.engine.extensions.xml.data;

import static java.util.Collections.emptyMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.xml.XPathUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;

/**
 * @author rsauer
 * @version $Revision$
 */
public class XPathEvaluator implements AccessPathEvaluator, Stateless
{
   private static final Logger trace = LogManager.getLogger(XPathEvaluator.class);

   /**
    * Evaluates an out data path by applying the outPath expression against the given
    * accessPoint and returning the resulting value.
    *
    * @param attributes
    *           The access point definition's attributes.
    * @param accessPoint
    *           The actual access point.
    * @param outPath
    *           The dereference path to be applied.
    * @return The resulting value.
    */
   public Object evaluate(Map attributes, Object accessPoint, String outPath)
   {
      if (null == accessPoint)
      {
         return null;
      }

      Element contextElement = getContextElement(accessPoint);
      if (StringUtils.isEmpty(outPath))
      {
         return XmlUtils.toString(contextElement);
      }
      else
      {
         try
         {
            Pair<String, Map<String, String>> parsedPath = parseData(outPath);
            return XPathUtils.evaluateXPath(contextElement, parsedPath.getFirst(), parsedPath.getSecond());
         }
         catch (Exception e)
         {
            trace.debug(e.getMessage(), e);
            throw new PublicException(
                  BpmRuntimeError.MDL_FAILED_EVALUATING_XPATH_EXPRESSION.raise(outPath));
         }
      }
   }

   /**
    * Evaluates an in data path by applying the inPath expression parametrized with the
    * given value against the given accessPoint and returns the result, if appropriate.
    *
    * @param attributes
    *           The access point definition's attributes.
    * @param accessPoint
    *           The actual access point.
    * @param inPath
    *           The dereference path to be used when applying the given value.
    * @param value
    *           The new value to be applied to the access point.
    * @return The new access point.
    */
   public Object evaluate(Map attributes, Object accessPoint, String inPath, Object value)
   {
      Element contextElement = null;
      String nodeValue = value == null ? "" : value.toString();
      if (StringUtils.isEmpty(inPath))
      {
         contextElement = getContextElement(value);
      }
      else
      {
         contextElement = getContextElement(accessPoint);
         if (contextElement == null)
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_CONTEXT_ELEMENT_NOT_INITIALIZED.raise());
         }
         Object result = null;
         try
         {
            Pair<String, Map<String, String>> parsedPath = parseData(inPath);
            result = XPathUtils.evaluateXPath(contextElement, parsedPath.getFirst(), parsedPath.getSecond());
         }
         catch (Exception e)
         {
            trace.debug(e.getMessage(), e);
            throw new PublicException(
                  BpmRuntimeError.MDL_FAILED_EVALUATING_XPATH_EXPRESSION.raise(inPath));
         }
         if ( !(result instanceof List) || ((List) result).isEmpty())
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_XPATH_EXPRESSION_UNABLE_TO_FIND_ANY_SUITABLE_NODE
                        .raise(inPath));
         }

         List<Node> nodes = (List<Node>) result;
         if (nodes.size() > 1)
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_XPATH_EXPRESSION_EVALUATED_TO_MULTIPLE_NODES
                        .raise(inPath));
         }
         if ( !(nodes.get(0) instanceof Node))
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_XPATH_EXPRESSION_EVALUATED_TO_A_NON_NODE_VALUE
                        .raise(inPath));
         }
         Node node = (Node) nodes.get(0);
         if (node instanceof Attr)
         {
            ((Attr) node).setValue(nodeValue);
         }
         else
         {
            // todo: (fh) it's not in the fs, but we should be able to accept as values
            // xml document fragments
            Element element = (Element) node;
            // remove all Text nodes children of the selected element and add a new one.
            List texts = new ArrayList();
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
               Node child = children.item(i);
               if (child.getNodeType() == Node.TEXT_NODE)
               {
                  texts.add(child);
               }
            }
            for (int i = 0; i < texts.size(); i++)
            {
               element.removeChild((Node) texts.get(i));
            }
            element.appendChild(element.getOwnerDocument().createTextNode(nodeValue));
         }
      }
      return contextElement == null ? null : validate(attributes, contextElement);
   }

   /**
    * Reads the schema type, url and type id from the attributes and validates the schema.
    *
    * @param attributes
    *           Contains the schema type, schema url and type id attribute.
    * @param contextElement
    *           The xml document's root element.
    * @return The validated context element.
    */
   protected Object validate(Map attributes, Element contextElement)
   {
      String schemaType = null;
      String schemaUrl = null;
      String typeId = null;
      if (attributes != null)
      {
         schemaType = (String) attributes
               .get(PredefinedConstants.PLAINXML_SCHEMA_TYPE_ATT);
         schemaUrl = (String) attributes.get(PredefinedConstants.PLAINXML_SCHEMA_URL_ATT);
         typeId = (String) attributes.get(PredefinedConstants.PLAINXML_TYPE_ID_ATT);
      }
      // do validation
      if (!StringUtils.isEmpty(typeId))
      {
         validateRootElement(typeId, contextElement);
      }
      if (!StringUtils.isEmpty(schemaType) && !StringUtils.isEmpty(schemaUrl))
      {
         if (PredefinedConstants.PLAINXML_SCHEMA_TYPE_XSD.equals(schemaType))
         {
            validateXsdSchema(schemaUrl, contextElement);
         }
         else if (PredefinedConstants.PLAINXML_SCHEMA_TYPE_DTD.equals(schemaType))
         {
            validateDtdSchema(schemaUrl, contextElement);
         }
         else if (PredefinedConstants.PLAINXML_SCHEMA_TYPE_WSDL.equals(schemaType))
         {
            trace.debug("Validation against WSDL schema not yet implemented.");
         }
      }
      return XmlUtils.toString(contextElement);
   }

   /**
    * Validates the xsd schema.
    *
    * @param schemaUrl
    *           The schema url.
    * @param contextElement
    *           The xml document's root element.
    * @throws InvalidValueException
    *            if errors are found in validation.
    */
   private void validateXsdSchema(String schemaUrl, Element contextElement)
         throws InvalidValueException
   {
      String xsdUrl = XmlUtils.resolveResourceUri(schemaUrl);

      DocumentBuilder domBuilder = XmlUtils.newDomBuilder(true, xsdUrl);

      final List errors = new ArrayList();
      domBuilder.setErrorHandler(new ErrorHandler()
      {
         public void warning(SAXParseException exception) throws SAXException
         {}

         public void error(SAXParseException exception) throws SAXException
         {
            errors.add(exception);
         }

         public void fatalError(SAXParseException exception) throws SAXException
         {
            errors.add(exception);
         }
      });

      // TODO directly parse from existing DOM to improve efficiency
      StringWriter buffer = new StringWriter();
      XmlUtils.serialize(contextElement, new StreamResult(buffer), 1);
      try
      {
         domBuilder
               .parse(new InputSource(new StringReader(buffer.getBuffer().toString())));
      }
      catch (SAXException e)
      {
         throw new PublicException(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED.raise(), e);
      }
      catch (IOException e)
      {
         throw new PublicException(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED.raise(), e);
      }

      if ( !errors.isEmpty())
      {
         throw new InvalidValueException(
               BpmRuntimeError.MDL_XSD_VALIDATION_FAILED
                     .raise(describeValidationErrors(errors)));
      }
   }
   /**
    * Validates dtd schema. Throws InvalidValueException if errors are found in
    * validation.
    *
    * @param schemaUrl
    *           The schema url.
    * @param contextElement
    *           The xml document's root element.
    */
   private void validateDtdSchema(String schemaUrl, Element contextElement)
   {
      String dtdUrl = XmlUtils.resolveResourceUri(schemaUrl);

      DocumentBuilder domBuilder = XmlUtils.newDomBuilder(true);

      final List errors = new ArrayList();
      domBuilder.setErrorHandler(new ErrorHandler()
      {
         public void warning(SAXParseException exception) throws SAXException
         {}

         public void error(SAXParseException exception) throws SAXException
         {
            errors.add(exception);
         }

         public void fatalError(SAXParseException exception) throws SAXException
         {
            errors.add(exception);
         }
      });

      // TODO directly parse from existing DOM to improve efficiency
      StringWriter buffer = new StringWriter();
      XmlUtils.serialize(contextElement, new StreamResult(buffer), "UTF-8", 1, null,
            dtdUrl);
      try
      {
         domBuilder
               .parse(new InputSource(new StringReader(buffer.getBuffer().toString())));
      }
      catch (SAXException e)
      {
         throw new PublicException(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED.raise(), e);
      }
      catch (IOException e)
      {
         throw new PublicException(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED.raise(), e);
      }

      if ( !errors.isEmpty())
      {
         throw new InvalidValueException(
               BpmRuntimeError.MDL_DTD_VALIDATION_FAILED
                     .raise(describeValidationErrors(errors)));
      }
   }

   /**
    * Formats the validation exception error messages.
    *
    * @param errors
    * @return The formatted error message string.
    */
   private static String describeValidationErrors(List errors)
   {
      StringBuffer buffer = new StringBuffer(errors.size() * 100);
      for (Iterator i = errors.iterator(); i.hasNext();)
      {
         Exception error = (Exception) i.next();
         buffer.append("-> ").append(error.getMessage()).append("\n");
      }
      return buffer.toString();
   }

   /**
    * Validates the root element. Throws an InvalidValueException if the document has no
    * root of the given type id.
    *
    * @param typeId
    * @param contextElement
    *           The xml document's root element.
    */
   private void validateRootElement(String typeId, Element contextElement)
   {
      QName type = QName.valueOf(typeId);
      String elementName = contextElement.getLocalName();
      if (elementName == null)
      {
         elementName = contextElement.getTagName();
      }
      if (!StringUtils.isEmpty(type.getLocalPart()))
      {
         if ( !type.getLocalPart().equals(elementName))
         {
            throw new InvalidValueException(
                  BpmRuntimeError.MDL_INVALID_DOCUMENT_ROOT_TYPE.raise(typeId));
         }
      }
      if (!StringUtils.isEmpty(type.getNamespaceURI()))
      {
         if ( !type.getNamespaceURI().equals(contextElement.getNamespaceURI()))
         {
            throw new InvalidValueException(
                  BpmRuntimeError.MDL_INVALID_DOCUMENT_ROOT_TYPE.raise(typeId));
         }
      }
   }

   public Object createInitialValue(Map data)
   {
      return null;
   }

   public Object createDefaultValue(Map attributes)
   {
      return null;
   }

   /**
    * Parses the in data path.
    *
    * @param path
    *           The in data path to be be parsed.
    * @return The resulted DOMXPath.
    * @throws UnsupportedEncodingException
    */
   private Pair<String, Map<String, String>> parseData(String path) throws UnsupportedEncodingException
   {
      int lastx = 0;
      int ix = path.indexOf(' ', lastx);
      while (ix > 0)
      {
         String mapping = path.substring(lastx, ix);
         if (!mapping.startsWith("xmlns:"))
         {
            break;
         }
         lastx = ix + 1;
         ix = path.indexOf(' ', lastx);
      }
      if (lastx > 0)
      {
         String xPathExpression = path.substring(lastx);
         Map<String, String> nsMappings = newHashMap();
         lastx = 0;
         ix = path.indexOf(' ', lastx);
         while (ix > 0)
         {
            String mapping = path.substring(lastx, ix);
            if (!mapping.startsWith("xmlns:"))
            {
               break;
            }
            int eqx = mapping.indexOf('=');
            nsMappings.put(mapping.substring("xmlns:".length(), eqx), URLDecoder.decode(mapping.substring(eqx + 1), "UTF-8"));
            lastx = ix + 1;
            ix = path.indexOf(' ', lastx);
         }
         return new Pair(xPathExpression, nsMappings);
      }
      else
      {
         // no namespace mappings
         return new Pair(path, emptyMap());
      }
   }

   /**
    * Returns the root element of the given xml access point value.
    *
    * @param value
    *           The xml value.
    * @return The document's root element. The given value if it is already the root
    *         element.
    */
   private Element getContextElement(Object value)
   {
      if (value == null)
      {
         return null;
      }
      if (value instanceof Document)
      {
         return ((Document) value).getDocumentElement();
      }
      else if (value instanceof Element)
      {
         return ((Element) value);
      }
      else
      {
         try
         {
            String xml = value.toString();
            // force validating parser, as Aelfred (default nonvalidating parser) is not
            // compatible with XPathAPI
            Document document = XmlUtils.parseString(xml, null, true);
            return document.getDocumentElement();
         }
         catch (Exception e)
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_INVALID_XML_ACCESS_POINT.raise(), e);
         }
      }
   }

   public boolean isStateless()
   {
      return true;
   }
}
