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
package org.eclipse.stardust.engine.cli.console;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.common.log.ClientLogManager;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.DefaultConsoleProcessor;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.common.utils.console.VersionCommand;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Main
{
   private static Map commands = new TreeMap();
   private static Options argTypes = new Options();
   private static boolean delayExit;

   static
   {
      argTypes.register("-partition", "-part", "partition", "The partition to be used when resolving the user.", true);
      argTypes.register("-domain", "-d", "domain", "The domain to be used when resolving the user.", true);
      argTypes.register("-realm", "-r", "realm", "The realm to be used when resolving the user.", true);
      argTypes.register("-user", "-u", "user", "The user to login to the audit trail.", true);
      argTypes.register("-password", "-p", "password", "The password to login to the audit trail.", true);
      argTypes.register("-verbose", "-v", "verbose", "Makes output more verbose.", false);
      argTypes.register("-"+ConsoleCommand.GLOBAL_OPTION_FORCE, "-f", ConsoleCommand.GLOBAL_OPTION_FORCE, "Forces the command to execute without any callback.", false);
      argTypes.register("-passfile", "-pf", "passfile", "Makes output more verbose.", true);

      commands.put("deploy", DeployCommand.class.getName());
      commands.put("daemon", DaemonCommand.class.getName());
      commands.put("list", ListCommand.class.getName());
      commands.put("cleanup", CleanupCommand.class.getName());
      commands.put("delete", DeleteCommand.class.getName());
      commands.put("setPrimaryImplementation", SetPrimaryImplementationCommand.class.getName());
      commands.put("recover", RecoverCommand.class.getName());
      commands.put("version", VersionCommand.class.getName());
      commands.put("realm", RealmCommand.class.getName());
      commands.put("createuser", CreateUserCommand.class.getName());
      commands.put("modifyuser", ModifyUserCommand.class.getName());
      commands.put("dashboard", DashboardCommand.class.getName());
      commands.put("engine", EngineCommand.class.getName());
      commands.put("configuration", ConfigurationCommand.class.getName());
      commands.put("preferenceStore", PreferenceStoreCommand.class.getName());
      commands.put("encrypt", EncryptCommand.class.getName());
      commands.put("createdepartment", CreateDepartmentCommand.class.getName());
      commands.put("deletedepartment", DeleteDepartmentCommand.class.getName());
      commands.put("bindeventhandler", BindEventHandlerCommand.class.getName());
      commands.put("unbindeventhandler", UnbindEventHandlerCommand.class.getName());
      commands.put("migraterepository", MigrateRepositoryCommand.class.getName());
      
   }

   public static void main(String[] args)
   {
      ClientLogManager.bootstrap("console");

      int rc = run(args);
      if (rc != 0 || !delayExit)
      {
         System.exit(rc);
      }
   }

   public static int run(String[] args)
   {
      DefaultConsoleProcessor cp = new DefaultConsoleProcessor(
            "Infinity (TM) Process Platform Administration Console", "console");
      int rc = cp.execute(commands, argTypes, args);
      delayExit = cp.delayExit();
      return rc;
   }

   private Main()
   {
   }
}
