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

import org.eclipse.stardust.common.security.Encrypter;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;


/**
 * @author rpielmann
 * @version $Revision: 9652 $
 */
public class EncryptCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String PASSWORD = "password";

   static
   {
      argTypes.register("-password", "-p", PASSWORD,
            "Provides an encrypted password.", true);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      if (options.containsKey(PASSWORD))
      {          
          String encrypted = Encrypter.encrypt((String) options.get(PASSWORD));
          System.out.println("Encrypted password:\n");
       	  System.out.println(encrypted);
      }
      return 0;
   }

public String getSummary() {	
	return "Provide an encrypted password.";
}
}
