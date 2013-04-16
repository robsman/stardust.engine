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
package org.eclipse.stardust.engine.core.struct.emfxsd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * Supports URLs with scheme "classpath:/". Searches for resources in CLASSPATH
 */
public class ClasspathUriConverter extends CustomURIConverter
{
   private static final Logger trace = LogManager.getLogger(ClasspathUriConverter.class);

   public static final String CLASSPATH_SCHEME = "classpath";

   
   @Override
   public URI normalize(URI uri)
   {
      String uriPath = uri.path();
      if(uriPath.startsWith("/"))
      {
         uriPath = uriPath.substring(1);
      }
      
      return URI.createURI(CLASSPATH_SCHEME + ":/" + uriPath);
   }
   
   public InputStream createInputStream(URI uri, Map< ? , ? > arg1) throws IOException
   {
      URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(uri.path());
      if (resourceUrl == null)
      {
         resourceUrl = ClasspathUriConverter.class.getClassLoader().getResource(uri.path());
         if (resourceUrl == null)
         {
            resourceUrl = ClasspathUriConverter.class.getResource(uri.path());
            if (resourceUrl == null)
            {
               throw new PublicException("Could not find XSD '" + uri.path() + "' in CLASSPATH");
            }
         }
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("Resolved '" + uri + "' to '" + resourceUrl + "'.");
      }
      return resourceUrl.openStream();
   }
}
