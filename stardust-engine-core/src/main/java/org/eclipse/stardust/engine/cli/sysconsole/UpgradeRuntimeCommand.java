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
package org.eclipse.stardust.engine.cli.sysconsole;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeItem;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.eclipse.stardust.engine.core.upgrade.framework.Upgrader;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class UpgradeRuntimeCommand extends AuditTrailCommand
{
   private static final Options argTypes = new Options();

   public static final String UPGRADE_KEY_IGNORELOCK = "ignorelock";
   public static final String UPGRADE_KEY_RECOVER = "recover";
   public static final String UPGRADE_KEY_DESCRIBE = "describe";
   public static final String UPGRADE_KEY_DDL = "ddl";
   public static final String UPGRADE_KEY_DATA = "data";
   public static final String UPGRADE_KEY_STEP = "step";

   static
   {
      argTypes.register("-" + UPGRADE_KEY_IGNORELOCK, "-i", UPGRADE_KEY_IGNORELOCK,
            "Forces an upgrade run even if the audit trail DB is already locked for upgrade.",
            false);
      argTypes.register("-" + UPGRADE_KEY_RECOVER, "-r", UPGRADE_KEY_RECOVER,
            "To force a recovery run of the upgrade", false);
      argTypes.register("-" + UPGRADE_KEY_DESCRIBE, "-d", UPGRADE_KEY_DESCRIBE,
            "Describes the migration steps involved, including any temporary schema versions. No modifications to the audittrail will be performed.",
            false);
      argTypes.register("-" + UPGRADE_KEY_DDL, "-l", UPGRADE_KEY_DDL,
            "Spools the SQL DDL defining the audittrail schema migration into the specified file. No modifications to the audittrail will be performed.",
            true);
      argTypes.register("-" + UPGRADE_KEY_DATA, "-a", UPGRADE_KEY_DATA,
            "Performs only data migration, preventing any SQL DDL execution. Combine with -step to perform migrations involving temporary schema versions.",
            false);
      argTypes.register("-" + UPGRADE_KEY_STEP, "-s", UPGRADE_KEY_STEP,
            "Performs exactly one migration step. May require multiple invocations to fully perform migrations involving temporary schema versions.",
            false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int doRun(Map options)
   {
      String password = (String) globalOptions.get("password");
      boolean ignorelock = options.containsKey(UPGRADE_KEY_IGNORELOCK);
      boolean recovery = options.containsKey(UPGRADE_KEY_RECOVER);

      if (options.containsKey(UPGRADE_KEY_DESCRIBE))
      {
         Parameters.instance().setBoolean(Upgrader.UPGRADE_DRYRUN, true);
      }
      String ddlFileName = null;
      if (options.containsKey(UPGRADE_KEY_DDL))
      {
         ddlFileName = (String) options.get(UPGRADE_KEY_DDL);
         
         Parameters.instance().setBoolean(Upgrader.UPGRADE_DRYRUN, true);
         Parameters.instance().setBoolean(RuntimeUpgrader.UPGRADE_SCHEMA, true);
      }
      if (options.containsKey(UPGRADE_KEY_DATA))
      {
         Parameters.instance().setBoolean(RuntimeUpgrader.UPGRADE_DATA, true);
      }
      if (options.containsKey(UPGRADE_KEY_STEP))
      {
         Parameters.instance().setBoolean(RuntimeUpgrader.UPGRADE_STEP, true);
      }

      RuntimeItem runtime = new RuntimeItem(
            Parameters.instance().getString("AuditTrail.Type"),
            Parameters.instance().getString("AuditTrail.DriverClass"),
            Parameters.instance().getString("AuditTrail.URL"),
            Parameters.instance().getString("AuditTrail.User"),
            Parameters.instance().getString("AuditTrail.Password"));
      if (!StringUtils.isEmpty(ddlFileName))
      {
         try
         {
            runtime.initSqlSpoolDevice(new File(ddlFileName));
         }
         catch (IOException e)
         {
            throw new UpgradeException("Unable to use DDL spool file '" + ddlFileName
                  + "': " + e.getMessage());
         }
      }
      RuntimeUpgrader upgrader = new RuntimeUpgrader(runtime, password, ignorelock);
      upgrader.upgrade(recovery);
      return 0;
   }

   public void printCommand(Map options)
   {
      print("Upgrading audit trail DB:\n");
   }

   public String getSummary()
   {
      return "Upgrades the audit trail from a previous Infinity version";
   }
}
