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
package org.eclipse.stardust.common.utils.console;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.cli.console.EncryptCommand;
import org.eclipse.stardust.engine.cli.console.Encrypter;


/**
 * @author mgille
 * @version $Revision$
 */
public class DefaultConsoleProcessor
{
   private static final Logger trace = LogManager.getLogger(
         DefaultConsoleProcessor.class);

   private String displayName;
   private String commandLineName;
   private boolean delayExit;

   public DefaultConsoleProcessor(String displayName, String commandLineName)
   {
      this.commandLineName = commandLineName;
      this.displayName = displayName;
   }

   private void printOutput(String message)
   {
      System.out.println(message);
   }

   private void printCopyright()
   {
      printOutput(displayName + ", Version " + CurrentVersion.getVersionName());
      printOutput("Copyright (C) SunGard Systeme GmbH, " + CurrentVersion.COPYRIGHT_YEARS
            + ". All rights reserved.\n");
   }

   private void printUsage(String message)
   {
      printOutput(message);
      printOutput("\n");
      printOutput("Usage: " + commandLineName + " [global-options] command [options]");
   }

   public int execute(Map commands, Options globalArgTypes, String[] args)
   {
      ConsoleCommand command = null;

      Map globalOptions = new HashMap();
      Map options = new HashMap();
      Options commandArgTypes = null;

      printCopyright();

      boolean commandEaten = false;
      try
      {
         for (int i = 0; i < args.length; ++i)
         {
            if (!args[i].startsWith("-"))
            {
               if (commandEaten)
               {
                  throw new IllegalUsageException("Only one command can be provided.");
               }
               if (args[i].equals("help"))
               {
                  command = new HelpCommand(commands, globalArgTypes);
               }
               else
               {
                  String commandClass = (String) commands.get(args[i]);
                  if (commandClass == null)
                  {
                     throw new IllegalUsageException("Unknown command '" + args[i] + "'");
                  }
                  command = (ConsoleCommand) Reflect.createInstance(commandClass);
               }               
               if (globalOptions.get("passfile") != null) {
                   String passFilePath = (String) globalOptions.get("passfile");
                   globalOptions.put("password", Encrypter.decryptFromFile(passFilePath));
               }
               command.bootstrapGlobalOptions(globalOptions);
               commandArgTypes = command.getOptions();
               commandEaten = true;
            }
            else if (commandEaten)
            {
               i = commandArgTypes.eat(options, args, i);
            }
            else
            {
               i = globalArgTypes.eat(globalOptions, args, i);
            }
         }
         if (command == null)
         {
            throw new IllegalUsageException("No command provided.");
         }
         delayExit = command.delayExit();
         globalArgTypes.checkRules(globalOptions);
         commandArgTypes.checkRules(options);
         command.preprocessOptions(options);
         return command.run(options);
      }
      catch (IllegalUsageException e)
      {
         printUsage(e.getMessage());
         return -1;
      }
      catch (Exception x)
      {
         String msg = "Operation could not be performed: " + x.getMessage();
         System.out.println(msg);
         trace.warn(msg);
         if (!(x instanceof PublicException))
         {
            trace.warn("", x);
         }
         return -100;
      }
   }

   public boolean delayExit()
   {
      return delayExit;
   }
}
