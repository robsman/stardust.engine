/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole.patch;

import java.util.Map;

import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.cli.sysconsole.AuditTrailCommand;

public class FixRuntimeOidCommand extends AuditTrailCommand
{
   private static final Options argTypes = new Options();
   
   public static final String LOG_ONLY_ARG = "-logonly";
   
   public static final String NO_LOG_ARG = "-nolog";

   public static final String COMMAND_NAME = "fixruntimeoids";
   
   static
   {
      argTypes.register(LOG_ONLY_ARG, Options.NO_SHORTNAME, LOG_ONLY_ARG, "If specified, no database operation will be performed", false);
      
      argTypes.register(NO_LOG_ARG, Options.NO_SHORTNAME, NO_LOG_ARG, "If specified, no log file will be written", false);
   }
   
   @Override
   public int doRun(Map options)
   {   
      boolean logOnly = options.containsKey(LOG_ONLY_ARG);
      boolean noLog = options.containsKey(NO_LOG_ARG);
      
      RuntimeOidPatcher patcher = new RuntimeOidPatcher(logOnly, noLog);
      patcher.patch();
      
      return 0;
   }

   @Override
   public void printCommand(Map options)
   {
      print("Fixing invalid runtime oids in the audit trail DB:\n");
   }

   @Override
   public Options getOptions()
   {
      return argTypes;
   }

   @Override
   public String getSummary()
   {
      return "Fixing invalid runtime oids in the audit trail DB:\n";
   }

   
}