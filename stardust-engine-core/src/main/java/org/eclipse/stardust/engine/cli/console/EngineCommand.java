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

import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;


public class EngineCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String INIT = "init";

   static
   {
      argTypes.register("-init", Options.NO_SHORTNAME, INIT,
            "Restores the engine to the startup state.", false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public String getSummary()
   {
      return "Manages the runtime engine.";
   }

   public int run(Map options)
   {
      if (options.containsKey(INIT))
      {
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
         try
         {
            serviceFactory.getAdministrationService().flushCaches();
         }
         finally
         {
            serviceFactory.close();
         }
      }

      return 0;
   }
}
