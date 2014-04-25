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
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;

/**
 * Supports URLs with scheme "classpath:/". Searches for resources in CLASSPATH
 */
public class ClasspathUriConverter extends ExtensibleURIConverterImpl
{
   private static final Logger trace = LogManager.getLogger(ClasspathUriConverter.class);

   public static final String CLASSPATH_SCHEME = "classpath";

   public ClasspathUriConverter()
   {
      super();
      getURIHandlers().add(0, new URIHandlerImpl()
      {
         public void setAttributes(URI uri, Map<String, ?> attributes, Map<?, ?> options) throws IOException
         {
            // does nothing
         }

         public Map<String, ?> getAttributes(URI uri, Map<?, ?> options)
         {
            return Collections.emptyMap();
         }

         public boolean exists(URI uri, Map<?, ?> options)
         {
            // TODO (fh) implement
            throw new RuntimeException("Not supported.");
         }

         public void delete(URI uri, Map<?, ?> options) throws IOException
         {
            throw new RuntimeException("Not supported.");
         }

         public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException
         {
            throw new RuntimeException("Not supported.");
         }

         public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException
         {
            InputStream result = null;
            String scheme = uri.scheme();
            if (CLASSPATH_SCHEME.equals(scheme) || scheme == null)
            {
               result = createClasspathInputStream(uri);
            }
            if (result == null)
            {
               throw new PublicException(
                     BpmRuntimeError.SDT_COULD_NOT_FIND_XSD_IN_CLASSPATH.raise(uri.path()));
            }
            return result;
         }

         private InputStream createClasspathInputStream(URI uri) throws IOException
         {
            String path = uri.path();
            boolean isAbsolute = path.startsWith("/");

            // (fh) treat all paths as absolute paths
            if (trace.isDebugEnabled())
            {
               trace.debug("Getting resource from context class loader: " + path);
            }
            ClassLoader ctxCl = Thread.currentThread().getContextClassLoader();
            // (fh) classloaders are considering all paths to be absolute
            // a path starting with a "/" is incorrect since first segment would then be empty
            URL resourceUrl = ctxCl.getResource(isAbsolute ? path.substring(1) : path);

            if (resourceUrl == null)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Getting resource from class: " + path);
               }
               // (fh) classes are considering paths to be absolute only if they have a leading "/"
               // otherwise they are relative to the class package.
               resourceUrl = ClasspathUriConverter.class.getResource(isAbsolute ? path : "/" + path);
               if (resourceUrl == null)
               {
                  return null;
               }
            }

            if (trace.isDebugEnabled())
            {
               trace.debug("Resolved '" + uri + "' to '" + resourceUrl + "'.");
            }
            return resourceUrl.openStream();
         }

         public boolean canHandle(URI uri)
         {
            return accept(uri);
         }
      });
   }

   public URI normalize(URI uri)
   {
      return accept(uri) ? uri : super.normalize(uri);
   }

   private boolean accept(URI uri)
   {
      String scheme = uri.scheme();
      return scheme == null || scheme.equals(CLASSPATH_SCHEME);
   }
}
