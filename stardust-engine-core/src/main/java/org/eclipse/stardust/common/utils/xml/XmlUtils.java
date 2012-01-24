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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * Some helping hands for tackling common but tedious XML tasks.
 *
 * @author Robert Sauer (rsauer@carnot.ag)
 * @version $Revision$
 */
public class XmlUtils
{
   private static final Logger trace = LogManager.getLogger(XmlUtils.class);
   
   public static final String STREAM_ENCODING_ISO_8859_1 = "ISO8859_1";

   private static final DefaultHandler DEFAULT_SAX_HANDLER = new CarnotEntityResolver();
   
   private static final String YES = "yes";
   private static final String OUTPUT_METHOD_XML = "xml";

   private static Document defaultDocument;

   private static final String JAXP_SCHEMA_LANGUAGE =
         "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

   private static final String W3C_XML_SCHEMA =
         "http://www.w3.org/2001/XMLSchema";

   private static final String JAXP_SCHEMA_SOURCE =
         "http://java.sun.com/xml/jaxp/properties/schemaSource";

   private static final String XALAN_INDENT =
         "{http://xml.apache.org/xalan}indent-amount";

   private static final String SAXON_INDENT =
         "{http://saxon.sf.net/}indent-spaces";
   
   private static final String PRP_CACHED_DOM_BUILDER_PREFIX = XmlUtils.class.getName() + ".CachedDomBuilder.";

   /**
    * Returns a nonvalidating XML parser producing a DOM document.
    *
    * @return The parser instance.
    * @see #newDomBuilder(boolean validating)
    */
   public static DocumentBuilder newDomBuilder()
   {
      return newDomBuilder(false);
   }

   /**
    * Returns a XML parser producing a DOM document.
    *
    * @param validating Indicates if the returned parser should operate
    *       validating.
    * @return The parser instance.
    */
   public static DocumentBuilder newDomBuilder(boolean validating)
   {
      return newDomBuilder(true, validating);
   }

   /**
    * Returns a XML parser producing a DOM document.
    *
    * @param validating Indicates if the returned parser should operate
    *       validating.
    * @return The parser instance.
    */
   public static DocumentBuilder newDomBuilder(boolean namespaceAware, boolean validating)
   {
      try
      {
         String cacheKey = PRP_CACHED_DOM_BUILDER_PREFIX
               + (validating ? "Validating" : "Nonvalidating")
               + (namespaceAware ? ".NamespaceAware" : "");

         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         if (null != rtEnv)
         {
            DocumentBuilder domBuilder = (DocumentBuilder) rtEnv.get(cacheKey);
            if (null != domBuilder)
            {
               return domBuilder;
            }
         }
         
         final DocumentBuilderFactory domBuilderFactory = 
            newDocumentBuilderFactory(validating, true);

         domBuilderFactory.setNamespaceAware(namespaceAware);

         DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
         domBuilder.setErrorHandler(new DefaultHandler());

         if (null != rtEnv)
         {
            rtEnv.setProperty(cacheKey, domBuilder);
         }
         
         return domBuilder;
      }
      catch (ParserConfigurationException e)
      {
         throw new PublicException("Invalid JAXP setup.", e);
      }
   }

   /**
    * Returns a XML parser producing a DOM document.
    *
    * @param validating Indicates if the returned parser should operate
    *       validating.
    * @return The parser instance.
    */
   public static DocumentBuilder newDomBuilder(boolean validating, String schemaUri)
   {
      try
      {
         final DocumentBuilderFactory domBuilderFactory = newDocumentBuilderFactory(validating, true);

         if (null != schemaUri)
         {
            domBuilderFactory.setNamespaceAware(true);
            try
            {
               domBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            }
            catch (IllegalArgumentException x)
            {
               // This can happen if the parser does not support JAXP 1.2
               trace.warn("Unable to use XML Schema for model validation", x);
            }

            try
            {
               // Specify other factory configuration settings
               domBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemaUri);
            }
            catch (IllegalArgumentException e)
            {
               // This can happen if the parser does not support JAXP 1.2
               trace.debug("Unable to set default XML Schema URI for model validation.",
                     e);
            }
         }

         DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
         domBuilder.setErrorHandler(new DefaultHandler());
         return domBuilder;
      }
      catch (ParserConfigurationException e)
      {
         throw new PublicException("Invalid JAXP setup.", e);
      }
   }
   
   public static SAXParserFactory newSaxParserFactory(boolean validating)
   {
      SAXParserFactory result = null;
      
      String parserFactoryOverride;
      if (validating)
      {
         parserFactoryOverride = Parameters.instance().getString(
               XmlProperties.VALIDATING_SAX_PARSER_FACTORY);
      }
      else
      {
         parserFactoryOverride = Parameters.instance().getString(
               XmlProperties.NONVALIDATING_SAX_PARSER_FACTORY);
      }
      
      if ( !StringUtils.isEmpty(parserFactoryOverride))
      {
         try
         {
            Object rawResult = Reflect.createInstance(parserFactoryOverride,
                  Thread.currentThread().getContextClassLoader());
            
            if (rawResult instanceof SAXParserFactory)
            {
               result = (SAXParserFactory) rawResult;
            }
            else
            {
               trace.warn("SAX Parser Factory (" //
                     + (validating ? "validating" : "nonvalidating") //
                     + ") override " + parserFactoryOverride + " is of invalid type.");
            }
         }
         catch (InternalException ie)
         {
            trace.warn("SAX Parser Factory (" //
                  + (validating ? "validating" : "nonvalidating") //
                  + ") override " + parserFactoryOverride + " can not be instantiated.",
                  ie);
         }
      }
      
      if (null == result)
      {
         result = SAXParserFactory.newInstance();
      }
      
      result.setNamespaceAware(true);
      result.setValidating(validating);
      
      return result;
   }
   
   public static XMLReader newXmlReader(boolean validating)
   {
      try
      {
         SAXParser saxParser = newSaxParserFactory(validating).newSAXParser();

         XMLReader result = (null != saxParser.getXMLReader())
               ? saxParser.getXMLReader()
               : saxParser instanceof XMLReader
                     ? (XMLReader) saxParser
                     : XMLReaderFactory.createXMLReader();

         return result;
      }
      catch (SAXException se)
      {
         throw new PublicException("Invalid JAXP setup.", se);
      }
      catch (ParserConfigurationException pce)
      {
         throw new PublicException("Invalid JAXP setup.", pce);
      }
   }

   public static DocumentBuilderFactory newDocumentBuilderFactory(boolean validating
         , boolean readOnly)
   {
      // TODO process readOnly flag
      DocumentBuilderFactory result = null;
      
      String domBuilderFactoryOverride;
      if (validating)
      {
         domBuilderFactoryOverride = Parameters.instance().getString(
               XmlProperties.VALIDATING_DOM_BUILDER_FACTORY);
      }
      else if (readOnly)
      {
         domBuilderFactoryOverride = Parameters.instance().getString(
               XmlProperties.NONVALIDATING_DOM_BUILDER_FACTORY);
      }
      else
      {
         domBuilderFactoryOverride = Parameters.instance().getString(
               XmlProperties.VALIDATING_DOM_BUILDER_FACTORY);
      }
      
      if ( !StringUtils.isEmpty(domBuilderFactoryOverride))
      {
         try
         {
            Object rawResult = Reflect.createInstance(domBuilderFactoryOverride,
                  Thread.currentThread().getContextClassLoader());
            
            if (rawResult instanceof DocumentBuilderFactory)
            {
               result = (DocumentBuilderFactory) rawResult;
            }
            else
            {
               trace.warn("Document Builder Factory (" //
                     + (validating ? "validating" : "nonvalidating") //
                     + ") override " + domBuilderFactoryOverride + " is of invalid type.");
            }
         }
         catch (InternalException ie)
         {
            trace.warn("Document Builder Factory (" //
                  + (validating ? "validating" : "nonvalidating") //
                  + ") override "
                  + domBuilderFactoryOverride
                  + " can not be instantiated.", ie);
         }
      }
      
      if (null == result)
      {
         result = DocumentBuilderFactory.newInstance();
      }
      
      result.setNamespaceAware(true);
      result.setValidating(validating);
      
      return result;
   }
   
   public static TransformerFactory newTransformerFactory()
   {
      TransformerFactory result = null;
      
      String traxFactoryOverride = Parameters.instance().getString(
            XmlProperties.XSLT_TRANSFORMER_FACTORY/*,
            XmlProperties.XSLT_TRANSFORMER_FACTORY_XALAN*/);
      
      if ( !StringUtils.isEmpty(traxFactoryOverride))
      {
         try
         {
            Object rawResult = Reflect.createInstance(traxFactoryOverride,
                  Thread.currentThread().getContextClassLoader());
            
            if (rawResult instanceof TransformerFactory)
            {
               result = (TransformerFactory) rawResult;
            }
            else
            {
               trace.warn("TrAX Transformer Factory override " + traxFactoryOverride
                     + " is of invalid type.");
            }
         }
         catch (InternalException ie)
         {
            trace.warn("TrAX Transformer Factory override " + traxFactoryOverride
                  + " can not be instantiated.", ie);
         }
      }
      
      if (null == result)
      {
         result = TransformerFactory.newInstance();
      }
      
      return result;
   }

   public static Document newDocument()
   {
      // most clients request an updateable DOM, so request a validating DOM builder to
      // workaround Saxon's restriction of delivering read-only DOMs
      return newDomBuilder(true).newDocument();
   }

   public static Document readDocument(String systemID, File file, ErrorHandler errorHandler)
   {
      DocumentBuilder domBuilder = newDomBuilder(!StringUtils.isEmpty(systemID));

      try
      {
         String encoding = Parameters.instance().getObject(
               PredefinedConstants.XML_ENCODING, XpdlUtils.ISO8859_1_ENCODING);
         InputSource inputSource = new InputSource(new InputStreamReader(new FileInputStream(file),
               encoding));
         if (!StringUtils.isEmpty(systemID))
         {
           inputSource.setSystemId(systemID);
         }

         if (errorHandler != null)
         {
            domBuilder.setErrorHandler(errorHandler);
         }
         return domBuilder.parse(inputSource);
      }
      catch (SAXException e)
      {
         throw new PublicException(e);
      }
      catch (FileNotFoundException e)
      {
         throw new PublicException(e);
      }
      catch (IOException e)
      {
         throw new InternalException(e);
      }

   }

   /**
    * Serializes the given DOM document in a XML character stream.
    *
    * Attention: As there is currently no platform neutral way to specify the
    * amount of indentation to use the <code>indent</code>-option does only
    * work an Xalan as expected.
    *
    * @param rootNode The root of the document to serialize.
    * @param target The stream the document gets serialized into.
    * @param indent The amount of columns to be used for indenting the produced
    *       XML.
    * @see #serialize(org.w3c.dom.Node,javax.xml.transform.stream.StreamResult,java.lang.String,int,java.lang.String,java.lang.String)
    */
   public static void serialize(Node rootNode, OutputStream target, int indent)
   {
      serialize(rootNode, new StreamResult(target), null, indent, null, null);
   }

   /**
    * Serializes the given DOM document in a XML character stream.
    *
    * Attention: As there is currently no platform neutral way to specify the
    * amount of indentation to use the <code>indent</code>-option does only
    * work an Xalan as expected.
    *
    * @param rootNode The root of the document to serialize.
    * @param target The stream the document gets serialized into.
    * @param indent The amount of columns to be used for indenting the produced
    *       XML.
    * @see #serialize(org.w3c.dom.Node,javax.xml.transform.stream.StreamResult,java.lang.String,int,java.lang.String,java.lang.String)
    */
   public static void serialize(Node rootNode, StreamResult target, int indent)
   {
      serialize(rootNode, target, null, indent, null, null);
   }

   /**
    * Serializes the given DOM document in a XML character stream by applying an identity
    * transformation.
    *
    * Attention: As there is currently no platform neutral way to specify the
    * amount of indentation to use the <code>indent</code>-option does only
    * work an Xalan as expected.
    *
    * @param rootNode The root of the document to serialize.
    * @param target The stream the document gets serialized into.
    * @param encoding The encoding to use during serialization.
    * @param indent The amount of columns to be used for indenting the produced
    *       XML.
    *
    * @see #serialize(Node, Transformer, StreamResult, String, int, String, String)
    */
   public static void serialize(Node rootNode, StreamResult target,
         String encoding, int indent, String publicId, String systemId)
   {
      // This creates a transformer that does a simple identity transform, and
      // thus can be used for all intents and purposes as a serializer.

      serialize(rootNode, null, target, encoding, indent, publicId, systemId);
   }

   /**
    * Serializes the given DOM document in a XML character stream by applying an
    * tranformation given. Giving an identity tranformation just writes the DOM as is.
    *
    * Attention: As there is currently no platform neutral way to specify the amount of
    * indentation to use the <code>indent</code>-option does only work an Xalan as
    * expected.
    *
    * @param rootNode
    *           The root of the document to serialize.
    * @param transformation
    *           The XSLT tranformation to be applied. If <code>null</code> an identity
    *           transformation will be applied.
    * @param target
    *           The stream the document gets serialized into.
    * @param encoding
    *           The encoding to use during serialization.
    * @param indent
    *           The amount of columns to be used for indenting the produced XML.
    *
    * @see #serialize(Node, StreamResult, String, int, String, String)
    */
   public static void serialize(Node rootNode, Transformer transformation,
         StreamResult target, String encoding, int indent, String publicId,
         String systemId)
   {
      if (null == transformation)
      {
         // obtain identity transformation
         try
         {
            transformation = newTransformerFactory().newTransformer();
         }
         catch (TransformerConfigurationException e)
         {
            throw new PublicException("Invalid JAXP setup.", e);
         }
      }

      if (publicId != null)
      {
         transformation.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
      }
      if (systemId != null)
      {
         transformation.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
      }

      transformation.setOutputProperty(OutputKeys.INDENT, YES);
      transformation.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD_XML);
      if (null != encoding)
      {
         transformation.setOutputProperty(OutputKeys.ENCODING, encoding);
      }

      transformation.setOutputProperty(XALAN_INDENT, Integer.toString(indent));
      transformation.setOutputProperty(SAXON_INDENT, Integer.toString(indent));

      try
      {
         transformation.transform(new DOMSource(rootNode), target);
      }
      catch (TransformerException e)
      {
         throw new PublicException("Error during XML serialization.", e);
      }
   }
   
   public static void transform(Source source, Transformer transformation,
         Result result, String cdataElements, int indent, String encoding)
   {
      if (null == transformation)
      {
         // obtain identity transformation
         try
         {
            transformation = newTransformerFactory().newTransformer();
         }
         catch (TransformerConfigurationException e)
         {
            throw new PublicException("Invalid JAXP setup.", e);
         }
      }

      transformation.setOutputProperty(OutputKeys.INDENT, YES);
      transformation.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD_XML);
      transformation.setOutputProperty(XALAN_INDENT, Integer.toString(indent));
      transformation.setOutputProperty(SAXON_INDENT, Integer.toString(indent));

      if (null != encoding)
      {
         transformation.setOutputProperty(OutputKeys.ENCODING, encoding);
      }

      if (!StringUtils.isEmpty(cdataElements))
      {
         transformation.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
               cdataElements + " value");
      }

      try
      {
         transformation.transform(source, result);
      }
      catch (TransformerException e)
      {
         throw new PublicException("Error during XML serialization.", e);
      }
   }

   public static String resolveResourceUri(String uri)
   {
      String result = uri;

      if (!StringUtils.isEmpty(uri))
      {
         try
         {
            URL url = new URL(uri);
            if (StringUtils.isEmpty(url.getProtocol()))
            {
               throw new MalformedURLException("Empty protocol.");
            }
         }
         catch (MalformedURLException e)
         {
            URL resourceUrl = getClassLoader().getResource(
                  uri.startsWith("/") ? uri.substring(1) : uri);
            if (null != resourceUrl)
            {
               result = resourceUrl.toString();
            }
         }
      }
         
      return result;
   }

   public static String toString(Node doc)
   {
      return toString(doc, null);
   }
   
   /**
    * A DOM node will be stringified into XML.
    * 
    * @param doc The DOM node or document.
    * @param transformProperties Additional properties which will change the default behavior
    * of the internally used {@link Transformer}. These properties set new or overwrite default
    * properties with {@link Transformer#setOutputProperty(String, String)}.
    * <br>
    * The default properies are: <br>
    * {@link OutputKeys#OMIT_XML_DECLARATION} = "yes" <br>
    * {@link OutputKeys#ENCODING} = {@link XMLConstants#ENCODING_ISO_8859_1} <br>
    * {@link OutputKeys#INDENT} = "yes" <br>
    * {@link OutputKeys#METHOD} = "xml" <br>
    * "{http://xml.apache.org/xalan}indent-amount" = "2" <br>
    * and additionally if doc is no instance of {@link Document} then <br>
    * {@link OutputKeys#OMIT_XML_DECLARATION} = "yes"
    * 
    * @return The stringified DOM node/document or null if doc is null.
    */
   public static String toString(Node doc, Properties transformProperties)
   {
      String string = null;
      if (doc != null)
      {
         StringWriter writer = new StringWriter();
         StreamResult result = new StreamResult(writer);

         // obtain identity transformation
         Transformer transformation = null;
         try
         {
            transformation = newTransformerFactory().newTransformer();
         }
         catch (TransformerConfigurationException e)
         {
            throw new PublicException("Invalid JAXP setup.", e);
         }
         
         if (doc instanceof Document)
         {
            // setting of the encoding will automatically enforce the xml declaration, regardless
            // of the OMIT_XML_DECLARATION output property.
            transformation.setOutputProperty(OutputKeys.ENCODING, XMLConstants.ENCODING_ISO_8859_1);
         }
         else
         {
            transformation.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, YES);
         }

         transformation.setOutputProperty(OutputKeys.INDENT, YES);
         transformation.setOutputProperty(OutputKeys.METHOD, OUTPUT_METHOD_XML);
         
         transformation.setOutputProperty(XALAN_INDENT, Integer.toString(2));
         transformation.setOutputProperty(SAXON_INDENT, Integer.toString(2));

         if (null != transformProperties)
         {
            for (Iterator i = transformProperties.entrySet().iterator(); i.hasNext();)
            {
               Map.Entry entry = (Map.Entry) i.next();
               
               transformation.setOutputProperty((String) entry.getKey(), (String) entry
                     .getValue());
            }
         }
         
         try
         {
            transformation.transform(new DOMSource(doc), result);
         }
         catch (TransformerException e)
         {
            throw new PublicException("Error during XML serialization.", e);
         }

         string = writer.toString();
      }
      return string;
   }

   public static Document parseString(String text)
   {
      // force validating parser for compatibility
      return parseString(text, null, true, false);
   }

   public static Document parseString(String text, boolean ignoreWhiteSpace)
   {
      // force validating parser for compatibility
      return parseString(text, null, true, ignoreWhiteSpace);
   }

   public static Document parseString(String text, EntityResolver entityResolver)
   {
      return parseString(text, entityResolver, false, false);
   }
   
   public static Document parseString(String text, EntityResolver entityResolver,
         boolean validating)
   {
      return parseString(text, entityResolver, validating, false);
   }
   
   private static Document parseString(String text, EntityResolver entityResolver,
         boolean validating, boolean ignoreWhiteSpace)
   {
      final InputSource inputSource = new InputSource(new StringReader(text));
      inputSource.setSystemId("");
      return parseSource(inputSource, entityResolver, validating, ignoreWhiteSpace);
   }
   
   public static Document parseStream(InputStream stream)
   {
      // force validating parser for compatibility
      return parseStream(stream, null, true, false);
   }

   public static Document parseStream(InputStream stream, boolean ignoreWhiteSpace)
   {
      // force validating parser for compatibility
      return parseStream(stream, null, true, ignoreWhiteSpace);
   }

   public static Document parseStream(InputStream stream, EntityResolver entityResolver)
   {
      return parseStream(stream, entityResolver, false, false);
   }

   public static Document parseStream(InputStream stream, EntityResolver entityResolver,
         boolean validating)
   {
      return parseStream(stream, entityResolver, validating, false);
   }

   private static Document parseStream(InputStream stream, EntityResolver entityResolver,
         boolean validating, boolean ignoreWhiteSpace)
   {
      final InputSource inputSource = new InputSource(stream);
      inputSource.setSystemId("");
      return parseSource(inputSource, entityResolver, validating, ignoreWhiteSpace);
   }

   public static Document parseSource(InputSource inputSource,
         EntityResolver entityResolver)
   {
      return parseSource(inputSource, entityResolver, false, false);
   }
   
   public static Document parseSource(InputSource inputSource,
         EntityResolver entityResolver, boolean validating)
   {
      return parseSource(inputSource, entityResolver, validating, false);
   }
   
   private static Document parseSource(InputSource inputSource,
         EntityResolver entityResolver, boolean validating, boolean ignoreWhiteSpace)
   {
      try
      {
         // optionally upgrade to validating parser, as Aelfred (default nonvalidating
         // parser) does not support entity resolvers
         DocumentBuilder domBuilder = newDomBuilderNS(validating
               || (null != entityResolver), ignoreWhiteSpace);
         if (null != entityResolver)
         {
            domBuilder.setEntityResolver(entityResolver);
         }
         
         return domBuilder.parse(inputSource);
      }
      catch (SAXException e)
      {
         throw new PublicException(e);
      }
      catch (IOException e)
      {
         throw new InternalException(e);
      }
   }

   public static DocumentBuilder newDomBuilderNS(boolean validating, boolean ignoreWhitespace)
   {
      DocumentBuilderFactory domBuilderFactory = newDocumentBuilderFactory(validating, false);
      domBuilderFactory.setNamespaceAware(true);
      if (ignoreWhitespace)
      {
         domBuilderFactory.setIgnoringElementContentWhitespace(ignoreWhitespace);
      }

      DocumentBuilder domBuilder = null;
      try
      {
         domBuilder = domBuilderFactory.newDocumentBuilder();
      }
      catch (ParserConfigurationException e)
      {
         throw new PublicException("Invalid JAXP setup.", e);
      }
      domBuilder.setErrorHandler(new DefaultHandler());
      domBuilder.setEntityResolver(new EntityResolver() {
         public InputSource resolveEntity(String publicId,
                                          String systemId)
               throws SAXException, IOException
         {
            return new InputSource(systemId);
         }
      });
      return domBuilder;
   }

   public static Element createElement(String name)
   {
      return createElement(null, name, null);
   }

   public static Element createElement(String namespace, String name)
   {
      return createElement(namespace, name, null);
   }

   public static Element createElement(String namespace, String name, String prefix)
   {
      if (StringUtils.isEmpty(namespace))
      {
         return getDefaultDocument().createElement(name);
      }
      Element element = getDefaultDocument().createElementNS(namespace, name);
      if (!StringUtils.isEmpty(prefix))
      {
         element.setPrefix(prefix);
      }
      return element;
   }

   public static Document newDocumentNS()
   {
      return newDomBuilderNS(false, false).newDocument();
   }

   public static Attr createAttribute(QName name)
   {
      if (StringUtils.isEmpty(name.getNamespaceURI()))
      {
         return getDefaultDocument().createAttribute(name.getLocalPart());
      }
      Attr attribute = getDefaultDocument().createAttributeNS(name.getNamespaceURI(), name.getLocalPart());
/*      if (!StringUtils.isEmpty(name.getPrefix()))
      {
         attribute.setPrefix(name.getPrefix());
      }*/
      return attribute;
   }

   public static Node createTextNode(String value)
   {
      return getDefaultDocument().createTextNode((null != value) ? value : "");
   }

   private static ClassLoader getClassLoader()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return loader == null ? XmlUtils.class.getClassLoader() : loader;
   }

   private static Document getDefaultDocument()
   {
      if (defaultDocument == null)
      {
         defaultDocument = newDocumentNS();
      }
      return defaultDocument;
   }

   // TODO (sb): use instances of ParseErrorHandler in setErrorHandler(...)
   // instead of DefaultHandler?
   private static class ParseErrorHandler implements ErrorHandler
   {
      public void warning(SAXParseException exception) throws SAXException
      {
         trace.warn(formatParseException("Warning", exception));
      }
   
      public void error(SAXParseException exception) throws SAXException
      {
         trace.error(formatParseException("Error", exception));
      }
   
      public void fatalError(SAXParseException exception) throws SAXException
      {
         trace.error(formatParseException("Fatal Error", exception));
      }
   
      private String formatParseException(String label,
            SAXParseException e)
      {
         StringBuffer buffer = new StringBuffer(100);
   
         buffer.append(label).append(" (").append(e.getLineNumber()).append(", ").append(
               e.getColumnNumber()).append(") ");
   
         buffer.append(e.getMessage());
   
         return buffer.toString();
      }
   }

   public static class CarnotEntityResolver extends DefaultHandler
   {
      public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
      {
         if (null != systemId)
         {
            if (XMLConstants.WORKFLOWMODEL_30_DTD_URL.equals(systemId)
                  || XMLConstants.WORKFLOWMODEL_31_DTD_URL.equals(systemId)
                  || systemId.endsWith(XMLConstants.DTD_NAME))
            {
               // strip old DTD (not doing so would include the DTD inline on save)
               return new InputSource(new StringReader(""));
            }
            else if (XMLConstants.WORKFLOWMODEL_31_XSD_URL.equals(systemId)
                  || systemId.endsWith(XMLConstants.WORKFLOWMODEL_XSD))
            {
               try
               {
                  URL xsdUrl = ModelBean.class.getResource(XMLConstants.WORKFLOWMODEL_XSD);
                  if (null != xsdUrl)
                  {
                     return new InputSource(xsdUrl.openStream());
                  }
               }
               catch (Exception e)
               {
                  // e.printStackTrace();
               }
            }
         }
         
         return null;
      }
   }

   public static String getXMLString(String fileName)
   {
      try
      {
         return getXMLString(new InputSource(new FileInputStream(fileName)));
      }
      catch (FileNotFoundException e)
      {
         throw new PublicException("File not found: '" + fileName + ".");
      }
   }
      
   public static String getXMLString(byte[] content)
   {
      return getXMLString(new InputSource(new ByteArrayInputStream(content)));
   }
   
   public static String getXMLString(InputSource input)
   {
      try
      {
         XMLReader xmlReader = XmlUtils.newXmlReader(false);
         xmlReader.setErrorHandler(new ParseErrorHandler());
         xmlReader.setEntityResolver(DEFAULT_SAX_HANDLER);
         SAXSource source = new SAXSource(xmlReader, input);
         
         StringWriter writer = new StringWriter();
         StreamResult target = new StreamResult(writer);
         
         TransformerFactory factory = XmlUtils.newTransformerFactory();
         Transformer transformer = factory.newTransformer();
         transformer.transform(source, target);
         
         return writer.toString();
      }
      catch (TransformerConfigurationException e)
      {
         throw new PublicException("Error reading xml.", e);
      }
      catch (TransformerException e)
      {
         throw new PublicException("Error reading xml.", e);
      }
   }
   
   public static byte[] getContent(String fileName) throws IOException
   {
      return getContent(new File(fileName));
   }

   public static byte[] getContent(URI fileURI) throws IOException
   {
      return getContent(new File(fileURI));
   }

   public static byte[] getContent(File file) throws FileNotFoundException, IOException
   {
      int len = (int) Math.min(file.length(), Integer.MAX_VALUE);
      byte[] content = new byte[len];
      BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
      int read = 0;
      while (read < len)
      {
         read += is.read(content, read, len - read);
      }
      return content;
   }
      
   public static String getOldXMLString(String fileName)
   {
      BufferedReader inStream = null;
      try
      {
         inStream = new BufferedReader(new FileReader(fileName));
      }
      catch (FileNotFoundException e)
      {
         throw new PublicException("File not found: '" + fileName + ".");
      }
      try
      {
         StringBuffer xmlString = new StringBuffer();
         String line;
         while ((line = inStream.readLine()) != null)
         {
            xmlString.append(line).append('\r');
         }
         inStream.close();
         return xmlString.toString();
      }
      catch (IOException e)
      {
         throw new InternalException(e);
      }
   }
   
   public static void main(String[] args)
   {
      String source = "C:\\development\\branches\\trunk\\runtime-New_configuration\\test\\models\\WorkflowModel.xpdl";
      try
      {
         byte[] content = XmlUtils.getContent(source);
         System.out.println(content.length);
         System.out.println(XmlUtils.getXMLString(content));
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }
}
