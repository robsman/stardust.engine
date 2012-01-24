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


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CleanupCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String KEEP_USERS = "keepUsers";
   private static final String MODELS = "models";
   private static final String MODEL = "model";
   private static final String AUDITTRAIL ="audittrail";

   static
   {
      argTypes.register("-models", "-s", MODELS, "Deletes all deployed models too.", false);
      argTypes.register("-audittrail", "-a", AUDITTRAIL, "Deletes the audit trail.", false);
      argTypes.register("-model", "-m", MODEL, "Deletes audit trail for the model with "
            + "the specified OID.", true);
      argTypes.register("-keepUsers", "-k", KEEP_USERS, "Keeps users and their roles.",
            false);
      argTypes.addExclusionRule(new String[] {MODELS, KEEP_USERS}, false);
      argTypes.addExclusionRule(new String[] {AUDITTRAIL, MODEL}, true);
      argTypes.addExclusionRule(new String[] {MODEL, MODELS}, false);
      argTypes.addExclusionRule(new String[] {MODEL, KEEP_USERS}, false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      if (options.containsKey(AUDITTRAIL))
      {
         if (options.containsKey(MODELS))
         {
            if (!force() && !confirm("You are going to cleanup all model and audit trail "
                  + "data. Continue?"))
            {
               return -1;
            }
            ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
            try
            {
               serviceFactory.getAdministrationService().cleanupRuntimeAndModels();
            }
            finally
            {
               serviceFactory.close();
            }
            print("Audit trail cleaned up.");
         }
         else
         {
            boolean keepUsers = options.containsKey(KEEP_USERS);
            String exclusionDescription;
            if (keepUsers)
            {
               exclusionDescription = "models and users";
            }
            else
            {
               exclusionDescription = "models";
            }
            if (!force()
                  && !confirm("You are going to cleanup all audit trail data except "
                  + exclusionDescription+ ". Continue?: "))
            {
               return -1;
            }
            ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
            try
            {
               serviceFactory.getAdministrationService().cleanupRuntime(keepUsers);
            }
            finally
            {
               serviceFactory.close();
            }
            print("Infinity Process Platform runtime environment cleaned up; "
                  + exclusionDescription + " were preserved.");
         }
      }
      else if (options.containsKey(MODEL))
      {
         int oid = getIntegerOption(options, MODEL);
         if (!force() && !confirm("You are going to cleanup all audit trail data for the "
               + "model with OID " + oid + ". Continue?"))
         {
            return -1;
         }
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
         try
         {
            serviceFactory.getAdministrationService().deleteModel(oid);
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
      return "Deletes the audit trail or parts of it.";
   }
}
