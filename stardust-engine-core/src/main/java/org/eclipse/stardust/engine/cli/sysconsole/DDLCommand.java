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
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DDLCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String STATEMENT_DELIMITER = "statementDelimiter";

   static
   {
      argTypes.register("-file", "-f", "file", "The DDL file name.", true);
      argTypes.register("-drop", "-d", "drop", "Creates DDL for dropping the schema.", false);
      argTypes.register("-schemaName", "-s", "schemaName", "Specifies the schema name to be used.", true);
      argTypes.register("-" + STATEMENT_DELIMITER, "-sd", STATEMENT_DELIMITER,
            "Specifies the delimiter applied after each statement.", true);
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
      final String file = (String) options.get("file");
      if (file == null)
      {
         throw new PublicException(BpmRuntimeError.CLI_NO_FILE_NAME_PROVIDED.raise());
      }
      final String schemaName = (String) options.get("schemaName");
      final String statementDelimiter = (String) options.get(STATEMENT_DELIMITER);

      if (options.containsKey("drop"))
      {
         print("Writing DDL for Infinity schema drop to '" + file + "'.");
         SchemaHelper.generateDropSchemaDDL(file, schemaName, statementDelimiter);
         print("DDL file '" + file + "' generated.");
      }
      else
      {
         print("Writing DDL for Infinity schema generation to '" + file + "'.");
         SchemaHelper.generateCreateSchemaDDL(file, schemaName, statementDelimiter);
         print("DDL file '" + file + "' generated.");
      }
      return 0;
   }

   public String getSummary()
   {
      return "Creates DDL for the Infinity schema";
   }
}
