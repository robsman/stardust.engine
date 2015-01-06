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
package org.eclipse.stardust.engine.extensions.transformation.format;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Tries to find entities in the CLASSPATH, if not successful, the control
 * will be passed to the parser ("return null;"), no exception will be thrown
 */
public class ClasspathEntityResolver implements EntityResolver
{

   private static final Logger trace = LogManager.getLogger(ClasspathEntityResolver.class);
         
   public InputSource resolveEntity(String publicId, String systemId)
         throws SAXException, IOException
   {
      if ( !StringUtils.isEmpty(systemId))
      {
         URL entityUrl;
         try 
         {
            entityUrl = new URL(systemId);
         }
         catch (Exception e)
         {
            trace.info("System ID '"+systemId+"' is not an URL, passing control to the parser");
            return null;
         }

         if ("file".equals(entityUrl.getProtocol()))
         {
            // special handling for filesystem urls
            File f = new File(entityUrl.getFile());
            if ( !f.exists())
            {
               String fileName = f.getName();
               trace.info("file denoted by '"+f.getAbsolutePath()+"' could not be found, trying to locate '"+fileName+"' in the CLASSPATH");
               
               entityUrl = Thread.currentThread().getContextClassLoader().getResource(fileName);
               if (entityUrl == null)
               {
                  trace.warn("Could not resolve CLASSPATH resource '" + fileName
                        + "' needed to parse XML (systemId '" + systemId + "' publicId '"
                        + publicId + "', passing control to the parser");
                  return null;
               }
               else
               {
                  return new InputSource(entityUrl.openStream());
               }
            }
         }
         else
         {
            return new InputSource(entityUrl.openStream());
         }
      }
      
      // can not resolve, give up
      return null;
   }

}
