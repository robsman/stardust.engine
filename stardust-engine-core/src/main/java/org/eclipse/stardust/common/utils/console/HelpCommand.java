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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class HelpCommand extends ConsoleCommand
{
   private static final Options myArgTypes = new Options();

   static
   {
      myArgTypes.register("-command", "-c", "command", "Shows help for the specified command.", true);
      myArgTypes.register("-commands", "-s", "commands", "Shows a summary of available commands.", false);
      myArgTypes.register("-options", "-o", "options", "Shows a summary of global options.", false);
   }

   private Options argTypes;
   private Map commands;

   public HelpCommand(Map commands, Options argTypes)
   {
      this.commands = commands;
      this.argTypes = argTypes;
   }

   public Options getOptions()
   {
      return myArgTypes;
   }

   public int run(Map options)
   {
      if (options.size() == 0)
      {
         describeCommand(this);
         return 0;
      }
      String commandName = (String) options.get("command");
      if (commandName != null)
      {
         if (commandName.equals("help"))
         {
            describeCommand(this);
         }
         else
         {
         String commandClass = (String) commands.get(commandName);
         if (commandClass == null)
         {
            System.out.println("Command '" + commandName + "' not known.");
         }
         else
         {
            ConsoleCommand command = (ConsoleCommand) Reflect.createInstance(commandClass);
            describeCommand(command);
         }
         }
      }
      else if (options.containsKey("commands"))
      {
         for (Iterator i = commands.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            String commandClass = (String) entry.getValue();
            ConsoleCommand command = (ConsoleCommand) Reflect.createInstance(commandClass);
            String rawName = (String) entry.getKey();
            StringBuffer name = new StringBuffer("               ");
            name.replace(0, Math.min(rawName.length(), name.length()), rawName);
            String summary = StringUtils.replace(command.getSummary(), "\n", "\n                ");
            System.out.println( name + " " + summary);
         }
      }
      else if (options.containsKey("options"))
      {
         System.out.println("Global options:\n");
         describeOptions(argTypes.getAllOptions());
      };

      return 0;
   }

   private void describeCommand(ConsoleCommand command)
   {
      System.out.println(command.getSummary()+ "\n");
      System.out.println("Options:\n");
      describeOptions(command.getOptions().getAllOptions());
   }

   private void describeOptions(Iterator options)
   {
      for (Iterator i = options; i.hasNext();)
      {
        Options.Option option = (Options.Option) i.next();

         StringBuffer message = new StringBuffer(100);
         
         message.append("  ").append(option.getLongname());
         
         if ( !StringUtils.isEmpty(option.getShortname()))
         {
            message.append(", ").append(option.getShortname());
         }
         
         if (option.hasArg())
         {
            message.append(" 'arg'");
         }
         System.out.println(message.toString());
         String summary = StringUtils.replace(option.getSummary(), "\n", "\n    ");
         System.out.println("    " +  summary);
      }
   }

   public String getSummary()
   {
      return "Provides help for the application.";
   }
}

