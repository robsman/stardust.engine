/**********************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * <p>
 * This class is capable of creating a {@link java.io.File} representation
 * for a {@link org.springframework.core.io.ClassPathResource} regardless of
 * whether the resource is located directly in the classpath or inside a JAR. 
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ClassPathFile
{
   private static final String JAR_FILE_URL_PREFIX = "jar:file:/";
   
   private static final String TEMP_FILE_PREFIX = "class-path-file-";
   
   private final ClassPathResource classPathResource;
   
   public ClassPathFile(final ClassPathResource classPathResource)
   {
      if (classPathResource == null)
      {
         throw new NullPointerException("Class Path Resource must not be null.");
      }
      if ( !classPathResource.exists())
      {
         throw new IllegalStateException("The class path resource '" + classPathResource + "' does not exist.");
      }
      
      this.classPathResource = classPathResource;
   }
   
   public ClassPathResource classPathResource()
   {
      return classPathResource;
   }
   
   public File file() throws IOException
   {
      if (classPathResource.getURL().toString().startsWith(JAR_FILE_URL_PREFIX))
      {
         final File tmpJcrRepoConfig = File.createTempFile(TEMP_FILE_PREFIX, null);
         tmpJcrRepoConfig.deleteOnExit();
         FileUtils.copyInputStreamToFile(classPathResource.getInputStream(), tmpJcrRepoConfig);
         return tmpJcrRepoConfig;
      }
      else
      {
         return classPathResource.getFile();
      }
   }
}
