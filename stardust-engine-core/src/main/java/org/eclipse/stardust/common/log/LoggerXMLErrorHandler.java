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
package org.eclipse.stardust.common.log;

import org.apache.log4j.helpers.LogLog;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * This class is needed for validating a log4j.dtd derived XML file.  It
 * implements the org.xml.sax.ErrorHandler interface which the DOMParser uses to
 * report error to the caller.
 *
 * @author Sebastian Woelk
 * @version $Revision$
 * @see org.eclipse.stardust.common.log.LogManager
 *
 */
public class LoggerXMLErrorHandler implements ErrorHandler
{

   /**
    * @param message
    * @param exception
    */
   private void printMessage(String message, SAXParseException exception)
   {

      LogLog.error(
            message + exception.getMessage() + "\n\tat line=" +
            exception.getLineNumber() +
            " col=" + exception.getColumnNumber() +
            " of SystemId=\"" + exception.getSystemId() +
            "\" PublicID = \"" + exception.getPublicId() + '\"'
      );
   }

   /**
    * @param exception
    */
   public synchronized void warning(SAXParseException exception)
   {
      printMessage("WARNING: ", exception);
   }

   /**
    * @param exception
    */
   public synchronized void error(SAXParseException exception)
   {
      printMessage("ERROR: ", exception);
   }

   /**
    * @param exception
    */
   public synchronized void fatalError(SAXParseException exception)
   {
      printMessage("FATAL: ", exception);
   }
}
