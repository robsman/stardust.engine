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
public class ChangePasswordCommand extends AuditTrailCommand
{
   private static final Options argTypes = new Options();

   static
   {
      argTypes.register("-new", "-n", "newpassword", "The new password.", true);
   }

   public Options getOptions()
   {
      return argTypes;
   }
   public int doRun(Map options)
   {
      String password = (String) globalOptions.get("password");
      String newpassword = (String) options.get("newpassword");
      SchemaHelper.changeSysOpPassword(password, newpassword);
      print("Password for sysop changed.");
      return 0;
   }

   public void printCommand(Map options)
   {
      print("Change sysop password:\n");
   }

   public String getSummary()
   {
      return "Changes the password of the sysop user";
   }
}
