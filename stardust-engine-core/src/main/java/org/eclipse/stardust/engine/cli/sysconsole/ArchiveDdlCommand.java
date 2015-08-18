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

import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;


/**
 * @author ubirkemeyer, rsauer
 * @version $Revision$
 */
public class ArchiveDdlCommand extends ConsoleCommand
{
   private static final String FILE = "file";

   private static final String SCHEMA_NAME = "schemaName";

   private static final Options argTypes = new Options();

   static
   {
      argTypes.register("-" + FILE, "-f", FILE, "The DDL file name.", true);
      argTypes.register("-" + SCHEMA_NAME, "-s", SCHEMA_NAME, "Specifies the schema supposed to contain the backup tables.", true);

      argTypes.addMandatoryRule(FILE);
      argTypes.addMandatoryRule(SCHEMA_NAME);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      if (globalOptions.containsKey("dbtype"))
      {
         Parameters.instance().set("AuditTrail.Type", globalOptions.get("dbtype"));
      }

      String file = (String) options.get(FILE);
      if (file == null)
      {
         throw new PublicException(BpmRuntimeError.CLI_NO_FILE_NAME_PROVIDED.raise());
      }
      String schemaName = (String) options.get(SCHEMA_NAME);
      if (schemaName == null)
      {
         throw new PublicException(BpmRuntimeError.CLI_NO_SCHEMA_NAME_PROVIDED.raise());
      }

      print("Writing DDL for Infinity archive schema generation to '" + file + "'.");
      SchemaHelper.generateCreateArchiveSchemaDDL(file, schemaName);
      print("DDL file '" + file + "' generated.");
      return 0;
   }

   public String getSummary()
   {
      return "Creates DDL for the Infinity archive schema";
   }
}
