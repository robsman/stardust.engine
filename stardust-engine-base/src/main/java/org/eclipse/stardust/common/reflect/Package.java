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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 *
 */
public class Package
{
   private static final Logger trace = LogManager.getLogger(Package.class);

   /**
    * (Stripped) name of the package itself (e.g. "swing" for "javax.swing").
    */
   private String name;
   /**
    * A package may need to load from multiple subdirectories
    */
   private TreeMap directories;
   /**
    * A package may need to load from multiple JAR files
    */
   private TreeMap jarFiles;
   /**
    * Full path name of the package (e.g. "javax.swing" for "javax.swing")
    */
   private String path;
   private TreeMap packages;
   private TreeMap classes;
   private boolean needsReload;

   /**
    * Constructor for packages directly from the classpath.
    */
   public Package(String name, String path, File directory)
   {
      this.name = name;
      this.path = path;

      directories = new TreeMap();
      jarFiles = new TreeMap();
      needsReload = true;

      addDirectory(directory);
   }

   /**
    * Constructor for packages from JAR files.
    */
   public Package(String name, JarFile jarFile, String path)
   {
      this.name = name;
      this.path = path;

      directories = new TreeMap();
      jarFiles = new TreeMap();
      needsReload = true;

      addJarFile(jarFile);
   }

   /**
    *
    */
   public void addDirectory(File directory)
   {
      Assert.isNotNull(directory);
      Assert.isNotNull(directories);
      Assert.condition(directory.isDirectory());

      if (!directories.containsKey(directory.getName()))
      {
         directories.put(directory.getName(), directory);
      }

      needsReload = true;
   }

   /**
    * Adds a jar file which contains classes and/or subpackages of this
    * package.
    */
   public void addJarFile(JarFile jarFile)
   {
      Assert.isNotNull(jarFile);
      Assert.isNotNull(jarFiles);

      if (!jarFiles.containsKey(jarFile.getName()))
      {
         jarFiles.put(jarFile.getName(), jarFile);
         trace.debug( "Added JAR file " + jarFile.getName());
      }

      needsReload = true;
   }

   /**
    * Second call is a no-op.
    */
   public void load()
   {
      if (!needsReload)
      {
         return;
      }

      packages = new TreeMap();
      classes = new TreeMap();

      Iterator _iterator = directories.values().iterator();

      while (_iterator.hasNext())
      {
         load((File) _iterator.next());
      }

      _iterator = jarFiles.values().iterator();

      while (_iterator.hasNext())
      {
         load((JarFile) _iterator.next());
      }

      needsReload = false;
   }

   /**
    * Loads the classes whose byte code resides in this (plain) directory to
    * the package.
    */
   private void load(File directory)
   {
      Assert.isNotNull(directory);
      Assert.condition(directory.isDirectory());

      File[] files = directory.listFiles();

      if (files == null)
      {
         return;
      }

      for (int n = 0; n < files.length; ++n)
      {
         if (files[n].isDirectory())
         {
            Package _package = (Package) packages.get(files[n].getName());

            if (_package == null)
            {
               packages.put(files[n].getName(), new org.eclipse.stardust.common.reflect.Package(files[n].getName(), path + "." + files[n].getName(), files[n]));
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
               Class type = Class.forName(path + "." + files[n].getName().substring(0, files[n].getName().indexOf(".class")));

               classes.put(type.getName(), type);
            }
            catch (ClassNotFoundException x)
            {
            }
            catch (NoClassDefFoundError x)
            {
            }
            catch (Throwable x)
            {
               // This must be catched, because the implicit constructor call may throw other exceptions
            }
         }
      }
   }

   /**
    * Loads all entries from a JAR file.
    */
   private void load(JarFile jarFile)
   {
      try
      {
         Enumeration e = jarFile.entries();
         JarEntry entry;
         String _filter = path.replace('.', '/') + "/";
         String _entryName;

         URLClassLoader _loader = new URLClassLoader(new URL[]{new URL("file:///" + jarFile.getName())},
               ClassLoader.getSystemClassLoader());
         trace.debug( "Created class loader for URL: " + "file:///" + jarFile.getName());

         while (e.hasMoreElements())
         {
            entry = (JarEntry) e.nextElement();

            // Convert slashes

            _entryName = entry.getName().replace('\\', '/');

            if (_entryName.startsWith(_filter))
            {
               if (entry.isDirectory())
               {
                  // Entries are expected to end with a slash (ZIP format)

                  if (!_entryName.endsWith("/"))
                  {
                     _entryName += "/";
                  }

                  // Avoid loading the package recursively

                  if (_entryName.equals(_filter))
                  {
                     continue;
                  }

                  String _packageName = _entryName.substring(_entryName.indexOf(_filter)
                        + _filter.length());

                  if (_packageName.indexOf('/') >= 0)
                  {
                     _packageName = _packageName.substring(0, _packageName.indexOf('/'));
                  }

                  Package _package = (Package) packages.get(_packageName);

                  if (_package == null)
                  {
                     packages.put(_packageName,
                           new org.eclipse.stardust.common.reflect.Package(_packageName, jarFile,
                                 path + "." + _packageName));
                  }
                  else
                  {
                     _package.addJarFile(jarFile);
                  }
               }

               else if (_entryName.endsWith(".class")
                     && _entryName.substring(_entryName.indexOf(_filter) + _filter.length() + 1).indexOf('/') < 0)
               {
                  String _className = _entryName.substring(0, _entryName.indexOf(".class")).replace('/', '.');


                  try
                  {
                     Class type = _loader.loadClass(_className);
                     trace.debug( "Loaded class " + _className);
                     classes.put(type.getName(), type);
                  }
                  catch (ClassNotFoundException x)
                  {
                     trace.debug( x.getMessage());
                  }
                  catch (NoClassDefFoundError x)
                  {
                     trace.debug( x.getMessage());
                  }
                  catch (Throwable x)
                  {
                     // Must catch exceptions in static initializers, can be virtually everything

                     trace.debug( x.getMessage());
                  }
               }
               else if (_entryName.endsWith(".class")
                     && _entryName.substring(_entryName.indexOf(_filter) + _filter.length() + 1).indexOf('/') > 0)
               {
                  _entryName = _entryName.substring(0, _entryName.lastIndexOf('/'));

                  if (!_entryName.endsWith("/"))
                  {
                     _entryName += "/";
                  }
                  String _packageName = _entryName.substring(_entryName.indexOf(_filter)
                        + _filter.length());

                  if (_packageName.indexOf('/') >= 0)
                  {
                     _packageName = _packageName.substring(0, _packageName.indexOf('/'));
                  }

                  Package _package = (Package) packages.get(_packageName);

                  if (_package == null)
                  {
                     packages.put(_packageName,
                           new org.eclipse.stardust.common.reflect.Package(_packageName, jarFile,
                                 path + "." + _packageName));
                  }
                  else
                  {
                     _package.addJarFile(jarFile);
                  }
               }

            }
         }
      }
      catch (Exception x)
      {
         trace.warn("", x);

         throw new InternalException(x);
      }
   }

   /**
    *
    */
   public String toString()
   {
      return getFullName();
   }

   /**
    *
    */
   public String getName()
   {
      return name;
   }

   /**
    *
    */
   public String getFullName()
   {
      return path;
   }

   /**
    * Returns an iterator for all subpackages of this package
    */
   public java.util.Iterator getAllPackages()
   {
      load();

      return packages.values().iterator();
   }

   /**
    * Returns an iterator for all classes of this package
    */
   public java.util.Iterator getAllClasses()
   {
      load();

      return classes.values().iterator();
   }

   /**
    * returns a package according to a name
    */
   public static Package packageForName(String packageName)
   {
      Package _actPackage = null;
      StringBuffer _actPackageFullName = new StringBuffer();
      ClassPath _classPath = ClassPath.instance();
      Iterator _itr = _classPath.getAllPackages();
      StringTokenizer _tokennizer = new StringTokenizer(packageName, ".", false);
      while (_tokennizer.hasMoreElements())
      {
         String _token = (String) _tokennizer.nextElement();
         while (_itr.hasNext())
         {
            Package _package = (Package) _itr.next();
            if (_token.equals(_package.getName()))
            {
               _itr = _package.getAllPackages();
               _actPackage = _package;
               _actPackageFullName.append(_token);
               if (_tokennizer.hasMoreElements())
               {
                  _actPackageFullName.append(".");
               }
               break;
            }
         }
      }
      if (!_actPackageFullName.toString().equals(packageName))
      {
         return null;
      }
      return _actPackage;
   }

   public Class classForName(String name)
   {
      load();
      return (Class)classes.get(name);
   }

}