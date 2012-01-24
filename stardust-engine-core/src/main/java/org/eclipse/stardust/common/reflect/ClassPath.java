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
package org.eclipse.stardust.common.reflect;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Singleton to represent all packages and classes being loaded in a JVM, the
 * ones loaded from the class path and those loaded dynamically.
 */
public class ClassPath
{
   private static final Logger trace = LogManager.getLogger(ClassPath.class);

   private static ClassPath singleton;

   private URLClassLoader classLoader;
   /**
    * Stores the URLs additionally to the class path.
    */
   private TreeMap additionalURLs;
   private TreeMap packages;
   private TreeMap classes;
   private TreeMap entries;

   /**
    *
    */
   public static ClassPath instance()
   {
      if (singleton == null)
      {
         singleton = new ClassPath();
      }

      return singleton;
   }

   /**
    *
    */
   public ClassLoader getClassLoader()
   {
      if (classLoader == null)
      {
         return ClassLoader.getSystemClassLoader();
      }

      return classLoader;
   }

   /**
    *
    */
   private void addURL(URL url)
   {
      if (additionalURLs.get(url.toExternalForm()) != null)
      {
         return;
      }

      classLoader = new URLClassLoader(new URL[]{url}, getClassLoader());
      additionalURLs.put(url.toExternalForm(), url);
   }

   /**
    *
    */
   private ClassPath()
   {
      String classPathString;

      additionalURLs = new TreeMap();
      packages = new TreeMap();
      classes = new TreeMap();
      entries = new TreeMap();

      try
      {
         classPathString = System.getProperty("java.class.path");
         String pathSeparator = System.getProperty("path.separator");

         int index;
         int oldIndex = 0;

         while ((index = classPathString.indexOf(pathSeparator, oldIndex)) > 0)
         {
            addEntry(classPathString.substring(oldIndex, index));

            oldIndex = index + 1;
         }
      }
      catch (Exception x)
      {
         throw new InternalException(x);
      }
   }

   /**
    * Returns an iterator for all class path entries.
    */
   public java.util.Iterator getAllEntries()
   {
      return entries.values().iterator();
   }

   /**
    * Returns an iterator for all top level packages of this package
    */
   public java.util.Iterator getAllPackages()
   {
      return packages.values().iterator();
   }

   /**
    * Returns an iterator for all top level classes of this package
    */
   public java.util.Iterator getAllClasses()
   {
      return classes.values().iterator();
   }

   /**
    *
    */
   public void addEntryFromURL(String urlString)
   {
      try
      {
         if (!addEntryFromURL(new URL(urlString)))
         {
            throw new PublicException(urlString+" can not be loaded.");
         }

         trace.debug( urlString + " added.");
      }
      catch (MalformedURLException x)
      {
         throw new PublicException(x);
      }
   }

   /**
    *
    */
   public void addEntryFromURL(File file)
   {
      try
      {
         addEntryFromURL(new URL("file:///" + file.getPath()));
         trace.debug( "file:///" + file.getPath() + " added.");
      }
      catch (MalformedURLException x)
      {
         throw new PublicException(x);
      }
   }

   /**
    *
    */
   public boolean addEntryFromURL(URL url)
   {
      // Add URL to additional URLs

      addURL(url);

      // Currently, we only support local files

      boolean _added = addEntry(url.getFile());
      if (_added)
      {
         trace.debug( "File \"" + url.getFile() + "\" added.");
      }
      else
      {
         trace.debug( "File \"" + url.getFile() + "\" NOT added.");
      }
      return _added;
   }

   /**
    *
    */
   public boolean addEntry(String path)
   {
      File file;
      JarFile jarFile;

      try
      {
         file = new File(path);

         trace.debug( "Adding file \"" + file.getPath() + "\".");

         if (file.isDirectory())
         {
            entries.put(file.getPath(), file);

            File[] files = file.listFiles();

            for (int n = 0; n < files.length; ++n)
            {
               if (files[n].isDirectory())
               {
                  Package _package = (Package) packages.get(files[n].getName());

                  if (_package == null)
                  {
                     packages.put(files[n].getName(),
                           new org.eclipse.stardust.common.reflect.Package(files[n].getName(), files[n].getName(),
                                 files[n]));
                  }
                  else
                  {
                     _package.addDirectory(files[n]);
                  }
               }
               else if (files[n].getName().endsWith(".class"))
               {
                  try
                  {
                     Class type = getClassLoader().loadClass(files[n].getName().substring(0, files[n].getName().indexOf(".class")));

                     classes.put(type.getName(), type);
                  }
                  catch (ClassNotFoundException x)
                  {
                  }
                  catch (NoClassDefFoundError x)
                  {
                  }
                  catch (UnsatisfiedLinkError x)
                  {
                  }
                  catch (Throwable x)
                  {
                     // Must catch that because something may occur in static initializers
                  }
               }
            }
         }
         else if (file.getName().endsWith(".jar"))
         {
            jarFile = new JarFile(file);

            entries.put(file.getPath(), file);

            Enumeration e = jarFile.entries();

            JarEntry entry;

            while (e.hasMoreElements())
            {
               entry = (JarEntry) e.nextElement();

               // Convert slashes

               String _entryName = entry.getName().replace('\\', '/');
               if ((entry.isDirectory()
                     && !_entryName.startsWith("META-INF")) || ((_entryName.indexOf('/') > 0
                     && _entryName.endsWith(".class"))))
               {
                  String _packageName = _entryName;

                  if (_packageName.indexOf('/') >= 0)
                  {
                     _packageName = _packageName.substring(0, _packageName.indexOf('/'));
                  }

                  Assert.condition(_packageName.indexOf('/') < 0, "Top level package does not contain trailing slash.");

                  Package _package = (Package) packages.get(_packageName);

                  if (_package == null)
                  {
                     packages.put(_packageName,
                           new org.eclipse.stardust.common.reflect.Package(_packageName, jarFile,
                                 _packageName));
                  }
                  else
                  {
                     _package.addJarFile(jarFile);
                  }
               }
               else if (_entryName.indexOf('/') < 0
                     && _entryName.endsWith(".class"))
               {
                  try
                  {
                     Class type = getClassLoader().loadClass(_entryName.substring(0, _entryName.indexOf(".class")));

                     classes.put(type.getName(), type);
                  }
                  catch (ClassNotFoundException x)
                  {
                  }
                  catch (NoClassDefFoundError x)
                  {
                  }
                  catch (UnsatisfiedLinkError x)
                  {
                  }
                  catch (Throwable x)
                  {
                     // Must catch that because something may occur in static initializers
                  }
               }

            }

         }
      }
      catch (Exception x)
      {
         trace.debug( "Exception loading entry \"" + path + "\": " + x);
         return false;
         // Catch all exceptions to prevent interruption and read the complete classpath
      }
      return true;
   }
}
