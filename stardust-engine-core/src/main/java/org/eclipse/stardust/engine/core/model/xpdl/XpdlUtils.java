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
package org.eclipse.stardust.engine.core.model.xpdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.StringTokenizer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.beans.DefaultConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLWriter;
import org.eclipse.stardust.engine.core.model.beans.IConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.NullConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;

public class XpdlUtils
{
   public static final DefaultHandler DEFAULT_SAX_HANDLER = new CarnotEntityResolver();

   public static final String EXT_XPDL = "xpdl";
   public static final String EXT_CWM = "cwm";
   public static final String EXT_ZIP = "zip";

   public static String NS_XPDL_1_0 = "http://www.wfmc.org/2002/XPDL1.0";
   public static String NS_XPDL_2_1 = "http://www.wfmc.org/2008/XPDL2.1";

   public static final String UTF8_ENCODING = "UTF-8";

   public static final String ISO8859_1_ENCODING = "ISO-8859-1";

   public static final String XPDL_1_0_XSD = "TC-1025_schema_10_xpdl.xsd";
   public static final String XPDL_1_0_XSD_URL = "http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd";

   public static final String XPDL_2_1_XSD = "bpmnxpdl_31.xsd";
   public static final String XPDL_2_1_XSD_URL = "http://www.wfmc.org/standards/docs/bpmnxpdl_31.xsd";

   public static final String CARNOT_XPDL_XSD = "carnot-xpdl.xsd";
   public static final String CARNOT_XPDL_XSD_URL = "http://www.carnot.ag/xpdl/3.1";

   private static final String XPDL_2_WFM_XSLT = "xpdl2carnot.xslt";
   private static final String WFM_2_XPDL_XSLT = "carnot2xpdl.xslt";

   public static final String XPDL_XSD = "xpdl.xsd";
   public static final String XPDL_EXTENSIONS_XSD = "xpdl.extensions.xsd";


   public static URL getXpdlExtensionsSchema()
   {
      return XpdlUtils.class.getResource(XPDL_EXTENSIONS_XSD);
   }

   public static URL getXpdlSchema()
   {
      return XpdlUtils.class.getResource(XPDL_XSD);
   }

   public static URL getCarnotXpdlSchema()
   {
      return XpdlUtils.class.getResource(CARNOT_XPDL_XSD);
   }

   public static URL getXpdl_10_Schema()
   {
      return XpdlUtils.class.getResource(XPDL_1_0_XSD);
   }

   public static URL getXpdl_21_Schema()
   {
      return XpdlUtils.class.getResource(XPDL_2_1_XSD);
   }

   public static URL getXpdl2CarnotStylesheet()
   {
      return XpdlUtils.class.getResource(XPDL_2_WFM_XSLT);
   }

   public static URL getCarnot2XpdlStylesheet()
   {
      return XpdlUtils.class.getResource(WFM_2_XPDL_XSLT);
   }

   public static String getCarnotVersion()
   {
      return CurrentVersion.getVersionName();
   }

   // code adapted from QName.valueOf()
   // TODO: fixit with regards to java 5.0
   public static String getQnameLocalPart(String qNameAsString)
   {
      if (StringUtils.isEmpty(qNameAsString))
      {
          return "";
      }

      // local part only?
      if (qNameAsString.charAt(0) != '{')
      {
          return qNameAsString;
      }

      // Namespace URI and local part specified
      int endOfNamespaceURI = qNameAsString.indexOf('}');
      if (endOfNamespaceURI == -1)
      {
          throw new IllegalArgumentException(
              "cannot create QName from \""
                  + qNameAsString
                  + "\", missing closing \"}\"");
      }

      return qNameAsString.substring(endOfNamespaceURI + 1);
   }

   // code adapted from QName.valueOf()
   // TODO: fixit with regards to java 5.0
   public static String getQnameNsUri(String qNameAsString)
   {
      if (StringUtils.isEmpty(qNameAsString))
      {
          return "";
      }

      // local part only?
      if (qNameAsString.charAt(0) != '{')
      {
          return "";
      }

      // Namespace URI and local part specified
      int endOfNamespaceURI = qNameAsString.indexOf('}');
      if (endOfNamespaceURI == -1)
      {
          throw new IllegalArgumentException(
              "cannot create QName from \""
                  + qNameAsString
                  + "\", missing closing \"}\"");
      }

      return qNameAsString.substring(1, endOfNamespaceURI);
   }

   public static String encodeMethodName(String methodName)
   {
      StringBuffer encodedName = new StringBuffer(methodName.length());

      StringTokenizer parser = new StringTokenizer(methodName, "(), ", true);
      while (parser.hasMoreTokens())
      {
         String token = parser.nextToken();
         if ("(".equals(token) || ",".equals(token))
         {
            encodedName.append(':');
         }
         else if (" ".equals(token) || ")".equals(token))
         {
            continue;
         }
         else
         {
            encodedName.append(token);
         }
      }

      return encodedName.toString();
   }

   public static String decodeMethodName(String encodedName)
   {
      StringBuffer methodName = new StringBuffer(2 * encodedName.length());

      // TODO
      StringTokenizer parser = new StringTokenizer(encodedName, ":", true);
      String delimiter = "(";
      while (parser.hasMoreTokens())
      {
         String token = parser.nextToken();
         if (":".equals(token))
         {
            methodName.append(delimiter);
            delimiter = ", ";
         }
         else
         {
            methodName.append(token);
         }
      }
      methodName.append(")");

      return methodName.toString();
   }

   public static String convertCarnot2Xpdl(String carnotXml)
   {
      return convertCarnot2Xpdl(carnotXml, true);
   }

   public static String convertCarnot2Xpdl(String carnotXml, boolean fixPre31Ns)
   {
      Source cwmSource;
      if (fixPre31Ns)
      {
         Document carnotDom = XmlUtils.parseString(carnotXml, DEFAULT_SAX_HANDLER);

         String ns = carnotDom.getDocumentElement().getNamespaceURI();
         if (StringUtils.isEmpty(ns) //
               || !XMLConstants.NS_CARNOT_WORKFLOWMODEL_31.equals(ns))
         {
            // convert carnot document into to namespace workflowmodel 3.1
            DefaultXMLReader reader = new DefaultXMLReader(true, new NullConfigurationVariablesProvider());
            IModel model = reader.loadModel(carnotDom.getDocumentElement());
            DefaultXMLWriter writer = new DefaultXMLWriter(true);
            carnotDom = XmlUtils.newDocument();
            writer.exportAsXML(model, carnotDom);
         }

         cwmSource = new DOMSource(carnotDom.getDocumentElement());
      }
      else
      {
         XMLReader cwmReader = XmlUtils.newXmlReader(false);

         cwmReader.setErrorHandler(DEFAULT_SAX_HANDLER);
         cwmReader.setEntityResolver(DEFAULT_SAX_HANDLER);

         try
         {
            cwmReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
            cwmReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", XpdlUtils.NS_XPDL_1_0); //$NON-NLS-1$
         }
         catch (SAXException se)
         {
            // ignore
         }

         cwmSource = new SAXSource(cwmReader,
               new InputSource(new StringReader(carnotXml)));
      }

      // assume XPDL representation will need about same order of characters as CARNOT
      // representation, but is known to be more verbose
      StringWriter xpdlWriter = new StringWriter( !StringUtils.isEmpty(carnotXml)
            ? (int) 1.5 * carnotXml.length()
            : 100);
      convertCarnot2Xpdl(cwmSource, new StreamResult(xpdlWriter));

      return xpdlWriter.toString();
   }

   public static byte[] convertCarnot2Xpdl(byte[] content, String encoding)
   {
      XMLReader xpdlReader = XmlUtils.newXmlReader(false);

      xpdlReader.setErrorHandler(DEFAULT_SAX_HANDLER);
      xpdlReader.setEntityResolver(DEFAULT_SAX_HANDLER);

      try
      {
         xpdlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
         xpdlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", XpdlUtils.NS_XPDL_1_0); //$NON-NLS-1$
      }
      catch (SAXException se)
      {
         // ignore
      }

      // assume CARNOT representation will need about same order of characters as XPDL
      // representation
      ByteArrayOutputStream output = new ByteArrayOutputStream(content == null ? 100 : content.length);
      InputSource inputSource = new InputSource(new ByteArrayInputStream(content));
      SAXSource source = new SAXSource(xpdlReader, inputSource);
      StreamResult result = new StreamResult(output);
      convertCarnot2Xpdl(source, result, encoding);

      return output.toByteArray();
   }

   public static void convertCarnot2Xpdl(Source carnotXml, Result result)
   {
      convertCarnot2Xpdl(carnotXml, result, UTF8_ENCODING);
   }

   public static void convertCarnot2Xpdl(Source carnotXml, Result result, String encoding)
   {
      final long tsStart = System.currentTimeMillis();

      final URL xsltURL = XpdlUtils.class.getResource(WFM_2_XPDL_XSLT);
      if (xsltURL == null)
      {
         throw new InternalException("Unable to find XPDL export stylesheet.");
      }

      try
      {
         TransformerFactory transformerFactory = XmlUtils.newTransformerFactory();
         Transformer xpdlTrans;
         try
         {
            transformerFactory.setURIResolver(new URIResolver()
            {
               public Source resolve(String href, String base) throws TransformerException
               {
                  // TODO Auto-generated method stub
                  if (null != href)
                  {
                     if (XMLConstants.WORKFLOWMODEL_30_DTD_URL.equals(href)
                           || XMLConstants.WORKFLOWMODEL_31_DTD_URL.equals(href)
                           || href.endsWith(XMLConstants.DTD_NAME))
                     {
                        // strip old DTD (not doing so would include the DTD inline on save)
                        return new StreamSource(new StringReader(""));
                     }
                     else if (XMLConstants.WORKFLOWMODEL_31_XSD_URL.equals(href)
                           || href.endsWith(XMLConstants.WORKFLOWMODEL_XSD))
                     {
                        try
                        {
                           URL xsdUrl = ModelBean.class.getResource(XMLConstants.WORKFLOWMODEL_XSD);
                           if (null != xsdUrl)
                           {
                              return new StreamSource(xsdUrl.openStream());
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
            });
            xpdlTrans = transformerFactory.newTransformer(new StreamSource(
                  xsltURL.openStream()));

            xpdlTrans.setURIResolver(transformerFactory.getURIResolver());
         }
         catch (IOException e)
         {
            throw new PublicException(
                  BpmRuntimeError.BPMRT_UNABLE_TO_LOAD_XPDL_EXPORT_STYLESHEET.raise(), e);
         }

         final long tsAfterStylesheetLoaded = System.currentTimeMillis();

         final ClassLoader cclBackup = Thread.currentThread().getContextClassLoader();
         try
         {
            Thread.currentThread().setContextClassLoader(XpdlUtils.class.getClassLoader());

            XmlUtils.transform(carnotXml, xpdlTrans, result,
                  DefaultXMLWriter.CDATA_ELEMENTS, 3, encoding);

            final long tsAfterConversion = System.currentTimeMillis();

            if (RuntimeLog.PERFORMANCE.isDebugEnabled())
            {
               RuntimeLog.PERFORMANCE.debug("Converting the model from its CARNOT internal representation to XPDL took "
                     + (tsAfterConversion - tsStart + 1)
                     + "ms (setup took "
                     + (tsAfterStylesheetLoaded - tsStart + 1) + "ms).");
            }
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(cclBackup);
         }
      }
      catch (TransformerConfigurationException e)
      {
         throw new PublicException(BpmRuntimeError.BPMRT_INVALID_JAXP_SETUP.raise(), e);
      }
   }

   public static String convertXpdl2Carnot(String xpdlString)
   {
      return convertXpdl2Carnot(xpdlString, UTF8_ENCODING);
   }

   public static String convertXpdl2Carnot(String xpdlString, String encoding)
   {
      XMLReader xpdlReader = XmlUtils.newXmlReader(false);

      xpdlReader.setErrorHandler(DEFAULT_SAX_HANDLER);
      xpdlReader.setEntityResolver(DEFAULT_SAX_HANDLER);

      try
      {
         xpdlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
         xpdlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", XpdlUtils.NS_XPDL_1_0); //$NON-NLS-1$
      }
      catch (SAXException se)
      {
         // ignore
      }

      // assume CARNOT representation will need about same order of characters as XPDL
      // representation
      StringWriter xmlWriter = new StringWriter( !StringUtils.isEmpty(xpdlString)
            ? xpdlString.length()
            : 100);

      convertXpdl2Carnot(new SAXSource(xpdlReader, new InputSource(new StringReader(
            xpdlString))), new StreamResult(xmlWriter), encoding);

      return xmlWriter.toString();
   }

   public static byte[] convertXpdl2Carnot(byte[] content, String encoding)
   {
      XMLReader xpdlReader = XmlUtils.newXmlReader(false);

      xpdlReader.setErrorHandler(DEFAULT_SAX_HANDLER);
      xpdlReader.setEntityResolver(DEFAULT_SAX_HANDLER);

      try
      {
         xpdlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
         xpdlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", XpdlUtils.NS_XPDL_1_0); //$NON-NLS-1$
      }
      catch (SAXException se)
      {
         // ignore
      }

      // assume CARNOT representation will need about same order of characters as XPDL
      // representation
      ByteArrayOutputStream output = new ByteArrayOutputStream(content == null ? 100 : content.length);
      InputSource inputSource = new InputSource(new ByteArrayInputStream(content));
      SAXSource source = new SAXSource(xpdlReader, inputSource);
      StreamResult result = new StreamResult(output);
      convertXpdl2Carnot(source, result, encoding);

      return output.toByteArray();
   }

   public static void convertXpdl2Carnot(Source xpdlSource, Result result)
   {
      convertXpdl2Carnot(xpdlSource, result, UTF8_ENCODING);
   }

   public static void convertXpdl2Carnot(Source xpdlSource, Result result, String encoding)
   {
      final long tsStart = System.currentTimeMillis();

      final URL xsltURL = XpdlUtils.class.getResource(XPDL_2_WFM_XSLT);
      if (xsltURL == null)
      {
         throw new InternalException("Unable to find XPDL import stylesheet.");
      }

      try
      {
         TransformerFactory transformerFactory = XmlUtils.newTransformerFactory();
         Transformer xpdlTrans;
         try
         {
            transformerFactory.setURIResolver(new URIResolver()
            {
               public Source resolve(String href, String base) throws TransformerException
               {
                  // TODO Auto-generated method stub
                  return null;
               }
            });
            xpdlTrans = transformerFactory.newTransformer(new StreamSource(
                  xsltURL.openStream()));
         }
         catch (IOException e)
         {
            throw new PublicException(
                  BpmRuntimeError.BPMRT_UNABLE_TO_LOAD_XPDL_EXPORT_STYLESHEET.raise(), e);
         }

         final long tsAfterStylesheetLoaded = System.currentTimeMillis();

         final ClassLoader cclBackup = Thread.currentThread().getContextClassLoader();
         try
         {
            Thread.currentThread().setContextClassLoader(XpdlUtils.class.getClassLoader());

            XmlUtils.transform(xpdlSource, xpdlTrans, result,
                  DefaultXMLWriter.CDATA_ELEMENTS, 3, encoding);

            final long tsAfterConversion = System.currentTimeMillis();

            if (RuntimeLog.PERFORMANCE.isDebugEnabled())
            {
               RuntimeLog.PERFORMANCE.debug("Converting the model from XPDL to its CARNOT internal representation took "
                     + (tsAfterConversion - tsStart + 1)
                     + "ms (setup took "
                     + (tsAfterStylesheetLoaded - tsStart + 1) + "ms).");
            }
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(cclBackup);
         }
      }
      catch (TransformerConfigurationException e)
      {
         throw new PublicException(BpmRuntimeError.BPMRT_INVALID_JAXP_SETUP.raise(), e);
      }
   }

   public static IModel loadXpdlModel(File xpdlFile)
   {
      return loadXpdlModel(xpdlFile, new DefaultConfigurationVariablesProvider());
   }

   public static IModel loadXpdlModel(File xpdlFile,
         IConfigurationVariablesProvider confVarProvider)
   {
      return loadXpdlModel(xpdlFile, confVarProvider, true);
   }

   public static IModel loadXpdlModel(File xpdlFile, boolean includeDiagrams)
   {
      return loadXpdlModel(xpdlFile, new DefaultConfigurationVariablesProvider(),
            includeDiagrams);
   }

   public static IModel loadXpdlModel(File xpdlFile,
         IConfigurationVariablesProvider confVarProvider, boolean includeDiagrams)
   {
      // read XPDL, transform into Carnot XML

      Document xpdlDoc;
      try
      {
         xpdlDoc = XmlUtils.parseSource(new InputSource(new FileInputStream(xpdlFile)), DEFAULT_SAX_HANDLER);
      }
      catch (FileNotFoundException e)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_FAILED_READING_XPDL_MODEL_FILE.raise(), e);
      }
      return loadXpdlModel(new DOMSource(xpdlDoc.getDocumentElement()), confVarProvider,
            includeDiagrams);
   }

   public static IModel loadXpdlModel(String xpdlString, boolean includeDiagrams)
   {
      return loadXpdlModel(xpdlString, new DefaultConfigurationVariablesProvider(),
            includeDiagrams);
   }

   public static IModel loadXpdlModel(String xpdlString,
         IConfigurationVariablesProvider confVarProvider, boolean includeDiagrams)
   {
      Document xpdlDoc = XmlUtils.parseString(xpdlString, DEFAULT_SAX_HANDLER);

      return loadXpdlModel(new DOMSource(xpdlDoc.getDocumentElement()), confVarProvider,
            includeDiagrams);
   }

   public static IModel loadXpdlModel(Source xpdlSource, boolean includeDiagrams)
   {
      return loadXpdlModel(xpdlSource, new DefaultConfigurationVariablesProvider(),
            includeDiagrams);
   }

   public static IModel loadXpdlModel(Source xpdlSource,
         IConfigurationVariablesProvider confVarProvider, boolean includeDiagrams)
   {
      DOMResult carnotDom = new DOMResult();
      convertXpdl2Carnot(xpdlSource, carnotDom);

      Element modelElement;
      if (carnotDom.getNode() instanceof Element)
      {
         modelElement = (Element) carnotDom.getNode();
      }
      else if (carnotDom.getNode() instanceof Document)
      {
         modelElement = ((Document) carnotDom.getNode()).getDocumentElement();
      }
      else
      {
         throw new InternalException("Invalid result of XPDL import: "
               + carnotDom.getNode());
      }

      return new DefaultXMLReader(includeDiagrams, confVarProvider).loadModel(
            modelElement, true);
   }

   public static void writeModel(IModel model, File target)
   {
      writeModel(model, new StreamResult(target));
   }

   public static void writeModel(IModel model, Result target)
   {
      writeModel(model, true, target);
   }

   public static void writeModel(IModel model, boolean includeDiagrams, Result target)
   {
      Document document = XmlUtils.newDocument();
      new DefaultXMLWriter(includeDiagrams).exportAsXML(model, document);

      convertCarnot2Xpdl(new DOMSource(document.getDocumentElement()), target);
   }

   private static class CarnotEntityResolver extends
         org.eclipse.stardust.engine.core.runtime.utils.XmlUtils.CarnotEntityResolver
   {
      public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
      {
         if (null != systemId)
         {
            if (XPDL_1_0_XSD_URL.equals(systemId)
                  || systemId.endsWith(XPDL_1_0_XSD))
            {
               try
               {
                  URL xsdUrl = XpdlUtils.class.getResource(XPDL_1_0_XSD);
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
            else
            {
               return super.resolveEntity(publicId, systemId);
            }
         }

         return null;
      }
   }
}