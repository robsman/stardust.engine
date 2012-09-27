/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.common.log.ClientLogManager;
import org.eclipse.stardust.common.utils.console.DefaultConsoleProcessor;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.cli.common.VersionCommand;
import org.eclipse.stardust.engine.cli.console.EncryptCommand;
//import org.eclipse.stardust.engine.cli.sysconsole.patch.FixRuntimeOidCommand;

/**
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class Main
{
   private static Map commands = new TreeMap();
   private static Options argTypes = new Options();
   private static boolean delayExit;

   static
   {
      argTypes.register("-password", "-p", "password", "The password of the sysop user.", true);
      argTypes.register("-verbose", "-v", "verbose", "Makes output more verbose.", false);
      argTypes.register("-force", "-f", "force", "Forces the command to execute without any callback.", false);
      argTypes.register("-dbschema", Options.NO_SHORTNAME, "dbschema", "Audit trail DB schema to use.", true);
      argTypes.register("-dbuser", "-d", "dbuser", "Audit trail DB user to use.", true);
      argTypes.register("-dbpassword", "-s", "dbpassword", "Audit trail DB password to use.", true);
      argTypes.register("-dbdriver", "-r", "dbdriver", "The JDBC driver class to use", true);
      argTypes.register("-dburl", "-l", "dburl", "The JDBC URL to use", true);
      argTypes.register("-dbtype", "-t", "dbtype", "The database type", true);
      argTypes.register("-passfile", "-pf", "passfile", "Path to file containing encrypted password.", true);
      argTypes.register("-dbpassfile", "-dbpf", "dbpassfile", "Path to file containing encrypted audit trail password.", true);

      commands.put("upgraderuntime", UpgradeRuntimeCommand.class.getName());
      commands.put("upgrademodel", UpgradeModelCommand.class.getName());
      commands.put("createschema", CreateSchemaCommand.class.getName());
      commands.put("dropschema", DropSchemaCommand.class.getName());
      commands.put("ddl", DDLCommand.class.getName());
      commands.put("password", ChangePasswordCommand.class.getName());
      commands.put("version", VersionCommand.class.getName());
      commands.put("archive", ArchiveCommand.class.getName());
      commands.put("archiveDDL", ArchiveDdlCommand.class.getName());
      commands.put("auditTrail", AlterAuditTrailCommand.class.getName());
      commands.put("property", RuntimePropertyCommand.class.getName());
      commands.put("encrypt", EncryptCommand.class.getName());
//      commands.put(FixRuntimeOidCommand.COMMAND_NAME, FixRuntimeOidCommand.class.getName());
   }

   public static void main(String[] args)
   {
      ClientLogManager.bootstrap("sysconsole");

      int rc = run(args);
      if (rc != 0 || !delayExit)
      {
         System.exit(rc);
      }
   }

   public static int run(String[] args)
   {
      DefaultConsoleProcessor cp = new DefaultConsoleProcessor(
            "Infinity (TM) Process Platform Administration Console", "sysconsole");
      int rc = cp.execute(commands, argTypes, args);
      delayExit = cp.delayExit();
      return rc;
   }

   public Main()
   {
   }
}
