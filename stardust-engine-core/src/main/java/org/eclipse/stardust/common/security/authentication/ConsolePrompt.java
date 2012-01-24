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
package org.eclipse.stardust.common.security.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * Utility class used to prompt for and reading user credentials when login is done 
 * from console ({@link System#out}) .
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ConsolePrompt
{
   // TODO: These members should not be static.
   private static String username;
   private static String password;
   private static String partition;
   private static String domain;
   private static String realm;

   /**
    * Prompts for credentials and reads them from console ({@link System#out}).
    */
   public static void show()
   {
      try
      {
         BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in));
         writeToConsole("Username: ");
         username = readTrimmedLine(bufferedreader);
         writeToConsole("Password: ");
         password = readTrimmedLine(bufferedreader);
         
         if (Parameters.instance().getBoolean(SecurityProperties.PROMPT_FOR_PARTITION,
               false))
         {
            writeToConsole("Partition:");
            partition = readTrimmedLine(bufferedreader);
         }
         
         if (Parameters.instance().getBoolean(SecurityProperties.PROMPT_FOR_DOMAIN,
               false))
         {
            writeToConsole("Domain:");
            domain = readTrimmedLine(bufferedreader);
         }
         
         if (Parameters.instance().getBoolean(SecurityProperties.PROMPT_FOR_REALM,
               false))
         {
            writeToConsole("Realm:");
            realm = readTrimmedLine(bufferedreader);
         }
      }
      catch (IOException e)
      {
         throw new InternalException(e);
      }
   }

   /**
    * Writes the given message to the console ({@link System#out}) 
    * and flushes it to make sure that it is written.
    * 
    * @param message message to be written to the console. Even null is accepted.
    */
   public static final void writeToConsole(final String message) {
      System.out.print(message);
      System.out.flush();      
   }
   /**
    * Reads a single trimmed-line from the reader. 
    * Note: It is a blocking operation and blocks till a line is available to be read.
    * 
    * @param reader to read from
    * @return null if no line could be read or a trimmed line from the reader
    * @throws IOException propogates any IOException encountered while reading a line
    */
   public static final String readTrimmedLine(final BufferedReader reader) throws IOException {
      String result = null;
      for(String line = reader.readLine(); result == null && line != null; ) {
         result = line.trim();
      }  
      return result;
   }
   
   /**
    * @return the username. May be an empty string or null.
    */
   public static String getUsername()
   {
      return username;
   }

   /**
    * @return the password. May be an empty string or null.
    */
   public static String getPassword()
   {
      return password;
   }

   /**
    * @return the domain. May be an empty string or null.
    */
   public static String getDomain()
   {
      return domain;
   }

   /**
    * @return the partition. May be an empty string or null.
    */
   public static String getPartition()
   {
      return partition;
   }

   /**
    * @return the realm. May be an empty string or null.
    */
   public static String getRealm()
   {
      return realm;
   }
}
