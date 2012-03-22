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

import org.eclipse.stardust.engine.core.model.parser.filters.NamespaceFilter;
import org.eclipse.stardust.engine.core.model.parser.filters.StopFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Helper class to retrieve information from either an XPDL or an CWM model.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ModelInfoRetriever
{
   private static final String XPDL_1_0 = "http://www.wfmc.org/2002/XPDL1.0";
   private static final String XPDL_2_0 = "http://www.wfmc.org/2004/XPDL2.0alpha";
   private static final String XPDL_2_1 = "http://www.wfmc.org/2008/XPDL2.1";
   
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
      NamespaceFilter fixer = new NamespaceFilter(XMLReaderFactory.createXMLReader());
      fixer.addReplacement(XPDL_1_0, XPDL_2_1);
      fixer.addReplacement(XPDL_2_0, XPDL_2_1);
      StopFilter stopper = new StopFilter(fixer);
      stopper.addStopCondition(XPDL_2_1, "TypeDeclarations", "Participants", "Applications", "DataFields", "WorkflowProcesses", "ExtendedAttributes");
      SAXSource source = new SAXSource(stopper, inputSource);

      JAXBContext context = getContext();
      Unmarshaller um = context.createUnmarshaller();
      return (ModelInfo) um.unmarshal(source);
   }
}
