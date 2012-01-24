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
import org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;


/**
 * @author rpielmann
 * @version $Revision: 9652 $
 */
public class DeleteCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String MODEL = "model";

   static
   {
      argTypes.register("-model", "-m", MODEL,
            "Deletes model with given OID from the audit trail.", true);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      if (options.containsKey(MODEL))
      {
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
         try
         {
            long modelOID = Long.parseLong(((String) options.get(MODEL)));
            
            if (!confirm("Do you want to delete model with OID '" + modelOID + " ? (Y/N): "))
            {
               return -1;
            };
            
            DeploymentInfo dinfo = serviceFactory.getAdministrationService().deleteModel(
                  modelOID);
            
         }
         finally
         {
            serviceFactory.close();
         }
      }
      return 0;
   }

   public String getSummary()
   {
      return "Delete a model from the audit trail.";
   }
}
