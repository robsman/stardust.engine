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
package org.eclipse.stardust.common;

import java.io.*;
import java.util.HashMap;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author Sebastian Woelk
 * @version $Revision$
 */
public class Serialization
{
   public static final String SUID_MAPPER_ATTR = "SUID.mapper.file.name";

   private static final Logger trace = LogManager.getLogger(Serialization.class);

   private static HashMap<String, Class<?>> criticalClasses;

   /**
    *
    */
   public static byte[] serializeObject(Serializable serializable)
         throws IOException
   {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);

      objectOutStream.writeObject(serializable);
      objectOutStream.flush();
      
      byte[] serializedValue = outStream.toByteArray();

      outStream.close();

      return serializedValue;
   }

   /**
    *
    */
   public static Serializable deserializeObject(byte[] serializedValue)
         throws IOException, ClassNotFoundException
   {
      if (serializedValue == null || serializedValue.length == 0)
      {
         return null;
      }
      
      initializeCriticalClasses();
      
      ByteArrayInputStream inStream = new ByteArrayInputStream(serializedValue);
      ObjectInputStream objectInStream = new ObjectInputStream(inStream)
      {
         protected ObjectStreamClass readClassDescriptor() throws IOException,
            ClassNotFoundException
         {
            ObjectStreamClass osc = super.readClassDescriptor();
            Class<?> mappedClass = criticalClasses.get(osc.getName());
            if (mappedClass != null)
            {
               ObjectStreamClass localOsc = ObjectStreamClass.lookup(mappedClass);
               trace.warn("Replacing serialVersionUID for class " + osc.getName() + ": "
                     + osc.getSerialVersionUID() + "-->" + localOsc.getSerialVersionUID());
               osc = localOsc;
            }
            return osc;
         }
      };

      Serializable object = (Serializable)objectInStream.readObject();

      inStream.close();

      return object;
   }
   
   public static void declareCritical(Class<?> clazz)
   {
      if (criticalClasses == null)
      {
         initializeCriticalClasses();
      }
      criticalClasses.put(clazz.getName(), clazz);
   }
   
   private static Reader getFileReader(String fileName)
   {
      try
      {
         return new FileReader(fileName);
      }
      catch (IOException ex)
      {
         return null;
      }
   }
   
   private static Reader getResourceReader(final String resName)
   {
      final ClassLoader loader = Serialization.class.getClassLoader();
      InputStream stream = java.security.AccessController.doPrivileged(
         new java.security.PrivilegedAction<InputStream>()
         {
            public InputStream run()
            {
               if (loader != null)
               {
                  return loader.getResourceAsStream(resName);
               }
               else
               {
                  return ClassLoader.getSystemResourceAsStream(resName);
               }
            }
         }
      );
      return stream == null ? null : new InputStreamReader(stream);
   }

   private static void initializeCriticalClasses()
   {
      if (criticalClasses == null)
      {
         criticalClasses = CollectionUtils.newHashMap();
         declareCritical(IntKey.class);
         declareCritical(StringKey.class);
         declareCritical(Key.class);
         String mapperFileName = Parameters.instance().getString(SUID_MAPPER_ATTR);
         if (mapperFileName != null)
         {
            Reader reader = null;
            try
            {
               reader = getFileReader(mapperFileName);
               if (reader == null)
               {
                  reader = getResourceReader(mapperFileName);
               }
               if (reader == null)
               {
                  trace.warn("Cannot find: " + mapperFileName);
               }
               else
               {
                  parseInput(reader);
               }
            }
            catch (IOException e)
            {
               trace.warn("Error reading: " + mapperFileName);
            }
            finally
            {
               if (reader != null)
               {
                  try
                  {
                     reader.close();
                  }
                  catch (Exception ex)
                  {
                     // ignore
                  }
               }
            }
         }
      }
   }

   private static void parseInput(Reader input) throws IOException
   {
      BufferedReader reader = new BufferedReader(input);
      String line = null;
      while ((line = reader.readLine()) != null)
      {
         if (!line.startsWith("#"))
         {
            try
            {
               Class<?> clazz = Class.forName(line);
               declareCritical(clazz);
            }
            catch (ClassNotFoundException e)
            {
               trace.warn("Mapped class not found: " + line);
            }
         }
      }
   }
}
