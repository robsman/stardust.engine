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
package org.eclipse.stardust.engine.core.runtime.setup;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Instances of this {@link DocumentBuilder} are preconfigured for {@link RuntimeSetup}
 * configuration. The preconfiguration includes validation, namespace-awareness, 
 * tracing error handler and entity resolver.
 *    
 * @author sborn
 * @version $Revision$
 */
public class RuntimeSetupDocumentBuilder extends DocumentBuilder implements XMLConstants
{
   private static final Logger trace = LogManager
         .getLogger(RuntimeSetupDocumentBuilder.class);

   private DocumentBuilder domBuilder;

   public RuntimeSetupDocumentBuilder()
   {
      domBuilder = XmlUtils.newDomBuilder(true, NS_CARNOT_RUNTIME_SETUP);
      domBuilder.setErrorHandler(new ParseErrorHandler());
      domBuilder.setEntityResolver(new ParseEntityResolver());
   }

   public boolean isNamespaceAware()
   {
      return domBuilder.isNamespaceAware();
   }

   public boolean isValidating()
   {
      return domBuilder.isValidating();
   }

   public DOMImplementation getDOMImplementation()
   {
      return domBuilder.getDOMImplementation();
   }

   public Document newDocument()
   {
      return domBuilder.newDocument();
   }

   public void setEntityResolver(EntityResolver er)
   {
      domBuilder.setEntityResolver(er);
   }

   public void setErrorHandler(ErrorHandler eh)
   {
      domBuilder.setErrorHandler(eh);
   }

   public Document parse(InputSource is) throws SAXException, IOException
   {
      return domBuilder.parse(is);
   }

   /**
    * @author sborn
    * @version $Revision$
    */
   private static final class ParseErrorHandler implements ErrorHandler
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

      private String formatParseException(String label, SAXParseException exception)
      {
         StringBuffer buffer = new StringBuffer();

         buffer.append(label).append(" (").append(exception.getLineNumber()).append(", ")
               .append(exception.getColumnNumber()).append(") ");

         buffer.append(exception.getMessage());

         return buffer.toString();
      }
   }

   /**
    * @author sborn
    * @version $Revision$
    */
   private static final class ParseEntityResolver implements EntityResolver, XMLConstants
   {
      private final URL xsdURL;

      public ParseEntityResolver()
      {
         xsdURL = RuntimeSetupDocumentBuilder.class.getResource(RUNTIME_SETUP_XSD);
         if (xsdURL == null)
         {
            throw new InternalException("Unable to find " + RUNTIME_SETUP_XSD);
         }
      }

      public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
      {
         if ((null != systemId)
               && (RUNTIME_SETUP_XSD_URL.equals(systemId) || 
                     NS_CARNOT_RUNTIME_SETUP.equals(systemId) ||
                     systemId.endsWith(RUNTIME_SETUP_XSD)))
         {
            return new InputSource(xsdURL.openStream());
         }

         return null;
      }
   };
}
