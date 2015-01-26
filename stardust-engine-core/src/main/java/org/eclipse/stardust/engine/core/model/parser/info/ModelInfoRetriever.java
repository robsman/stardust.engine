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
package org.eclipse.stardust.engine.core.model.parser.info;

import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.model.parser.filters.NamespaceFilter;
import org.eclipse.stardust.engine.core.model.parser.filters.StopFilter;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;

/**
 * Helper class to retrieve information from either an XPDL or an CWM model.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ModelInfoRetriever
{
   /**
    * The cached context. Needs only to be created once.
    */
   private static JAXBContext context;

   /**
    * Creates a parsing context for both XPDL and CWM model files.
    *
    * @return the parsing context.
    *
    * @throws JAXBException if JAXB is not correctly set up.
    */
   @SuppressWarnings("deprecation")
   private static JAXBContext getContext() throws JAXBException
   {
      if (context == null)
      {
         context = JAXBContext.newInstance(XpdlInfo.class, CwmInfo.class);
      }
      return context;
   }

   /**
    * Retrieves the model information from a file.
    *
    * @param file the file containing the model
    * @return the model information
    *
    * @throws JAXBException if JAXB is not correctly set up.
    * @throws SAXException if the xml document is invalid
    * @throws FileNotFoundException if the file does not exist
    */
   public static ModelInfo get(File file) throws JAXBException, SAXException, FileNotFoundException
   {
      return get(new InputSource(new FileInputStream(file)));
   }

   /**
    * Retrieves the model information from a memory byte array.
    *
    * @param content the byte array containing the model
    * @return the model information
    *
    * @throws JAXBException if JAXB is not correctly set up.
    * @throws SAXException if the xml document is invalid
    */
   public static ModelInfo get(byte[] content) throws JAXBException, SAXException
   {
      return get(new InputSource(new ByteArrayInputStream(content)));
   }

   /**
    * Retrieves the model information from a binary stream.
    *
    * @param stream the binary stream providing the model
    * @return the model information
    *
    * @throws JAXBException if JAXB is not correctly set up.
    * @throws SAXException if the xml document is invalid
    */
   public static ModelInfo get(InputStream stream) throws JAXBException, SAXException
   {
      return get(new InputSource(stream));
   }

   /**
    * Retrieves the model information from a character reader.
    *
    * @param content the character reader providing the model
    * @return the model information
    *
    * @throws JAXBException if JAXB is not correctly set up.
    * @throws SAXException if the xml document is invalid
    */
   public static ModelInfo get(Reader reader) throws JAXBException, SAXException
   {
      return get(new InputSource(reader));
   }

   /**
    * Retrieves the model information from a SAX input source.
    *
    * @param inputSource the data stream.
    * @return the model information
    *
    * @throws JAXBException if JAXB is not correctly set up.
    * @throws SAXException if the xml document is invalid
    */
   public static ModelInfo get(InputSource inputSource) throws SAXException, JAXBException
   {
      XMLFilter nameSpaceFilter = getNameSpaceFilter();
      StopFilter stopFilter = new StopFilter(nameSpaceFilter);
      stopFilter.addStopCondition(XMLConstants.NS_XPDL_2_1, "TypeDeclarations", "Participants", "Applications", "DataFields", "WorkflowProcesses", "ExtendedAttributes");

      SAXSource source = getModelSource(inputSource, stopFilter);
      JAXBContext context = getContext();
      Unmarshaller um = context.createUnmarshaller();
      return (ModelInfo) um.unmarshal(source);
   }

   /**
    * Get a {@link SAXSource} for parsing models
    * @param inputSource - the inputSource
    * @return
    * @throws SAXException
    */
   public static SAXSource getModelSource(InputSource inputSource) throws SAXException
   {
      return getModelSource(inputSource, getNameSpaceFilter());
}

   /**
    * Gets a {@link SAXSource} for parsing models
    * It will be configured with the supplied {@link XMLFilter}
    * and with the {@link XpdlUtils.DEFAULT_SAX_HANDLER} EntityResolver
    *
    * @param inputSource
    * @param xmlFilter
    * @return the preconfigured SAXSource
    */
   public static SAXSource getModelSource(InputSource inputSource, XMLFilter xmlFilter)
   {
      xmlFilter.setEntityResolver(XpdlUtils.DEFAULT_SAX_HANDLER);
      return new SAXSource(xmlFilter, inputSource);
   }

   private static XMLFilter getNameSpaceFilter() throws SAXException
   {
      NamespaceFilter nsFixer = new NamespaceFilter(XMLConstants.NS_XPDL_2_1, XMLConstants.NS_XPDL_2_0, XMLConstants.NS_XPDL_1_0);
      nsFixer.addReplacement("", XMLConstants.NS_CARNOT_WORKFLOWMODEL_31);
      nsFixer.addReplacement(XMLConstants.NS_CARNOT_WORKFLOWMODEL_30, XMLConstants.NS_CARNOT_WORKFLOWMODEL_31);
      return NamespaceFilter.createXMLFilter(nsFixer);
   }
}
