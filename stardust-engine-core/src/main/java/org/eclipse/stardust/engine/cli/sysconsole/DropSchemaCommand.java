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

import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DropSchemaCommand extends AuditTrailCommand
{
   public Options getOptions()
   {
      return new Options();
   }

   public int doRun(Map options)
   {
      String password = (String) globalOptions.get("password");

      SchemaHelper.dropSchema(password, null /*use default from dbDescriptor*/);
      print("Schema dropped.");
      return 0;
   }

   public void printCommand(Map options)
   {
      print("Drop Infinity schema:\n");
   }

   public String getSummary()
   {
      return "Drops the Infinity schema.";
   }
}
