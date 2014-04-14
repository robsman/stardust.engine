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

import java.sql.SQLException;
import java.util.Map;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CreateSchemaCommand extends AuditTrailCommand
{
   private static final Logger trace = LogManager.getLogger(CreateSchemaCommand.class);

   public Options getOptions()
   {
      return new Options();
   }

   public void printCommand(Map options)
   {
      print("Create Infinity schema:\n");
   }

   public int doRun(Map options)
   {
      try
      {
         SchemaHelper.createSchema();
      }
      catch (SQLException e)
      {
         trace.warn("", e);
         throw new PublicException(BpmRuntimeError.CLI_SQL_EXCEPTION_OCCURED.raise(e
               .getMessage()));
      }
      print("Schema created.");
      return 0;
   }

   public String getSummary()
   {
      return "Creates the Infinity schema.";
   }
}
