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


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class PWHCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {/*
      switch (processWarehouseOperation)
      {
         case EXTRACT_OPERATION:
            {
               ProcessWarehouseBuilder pwh = ProcessWarehouseBuilderFactory.instance().createLocal();
               pwh.extract(cleanupAuditTrail, exportUsersAnonymously);
               break;
            }
         default:
            {
               throw new IllegalUsageException("Unknown process warehouse operation.");
            }
      }

      // another snippet

      case CLEANUP_OLD_DATA:
         {
            ProcessWarehouseBuilder pwh = ProcessWarehouseBuilderFactory.instance().createLocal();
            pwh.clearAuditTrail();
            break;
         }
      */
      return 0;
   }

   public String getSummary()
   {
      return "todo";
   }
}
