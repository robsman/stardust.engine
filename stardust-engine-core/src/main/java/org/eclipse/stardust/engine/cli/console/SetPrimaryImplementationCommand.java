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

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LinkingOptions;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;


/**
 * @author rpielmann
 * @version $Revision: 9652 $
 */
public class SetPrimaryImplementationCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String INTERFACE_MODEL_OID = "interfaceModelOid";
   private static final String INTERFACE_PROCESS_ID = "processId";
   private static final String IMPLEMENTATION_MODEL_ID = "implementationModelId";
   private static final String COMMENT = "comment";

   static
   {
      argTypes.register("-interfaceModelOid", "-moid", INTERFACE_MODEL_OID, "interfaceModelOid", true);
      argTypes.register("-processId", "-ipid", INTERFACE_PROCESS_ID, "processId", true);
      argTypes.register("-implementationModelId", "-imid", IMPLEMENTATION_MODEL_ID, "implementationModelId", true);
      argTypes.register("-comment", "-c", COMMENT, "comment", true);
   }

   //console engine setPrimaryImplementation -interfaceModelOid 'param1' -processId 'param2' -implementationModelId 'param3' -comment 'param4'


   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
      try
      {
         if (options.get(INTERFACE_MODEL_OID) == null)
         {
            throw new PublicException(
                  BpmRuntimeError.CLI_INTERFACE_MODEL_OID_NOT_PROVIDED.raise());
         }
         if (options.get(INTERFACE_PROCESS_ID) == null)
         {
            throw new PublicException(
                  BpmRuntimeError.CLI_PROCESS_ID_NOT_PROVIDED.raise());
         }
         if (options.get(IMPLEMENTATION_MODEL_ID) == null)
         {
            throw new PublicException(
                  BpmRuntimeError.CLI_IMPLEMENTATION_MODEL_ID_NOT_PROVIDED.raise());
         }
         long interfaceModelOID = Long.parseLong(((String) options
               .get(INTERFACE_MODEL_OID)));
         String processID = (String) options.get(INTERFACE_PROCESS_ID);
         String implementationModelID = (String) options.get(IMPLEMENTATION_MODEL_ID);
         String comment = (String) options.get(COMMENT);
         LinkingOptions linkingOptions = new LinkingOptions();
         linkingOptions.setComment(comment);

         if (!confirm("Do you want to set model '" + implementationModelID + "' as primary implementation for process '" + processID + "'? (Y/N): "))
         {
            return -1;
         };

         serviceFactory.getAdministrationService().setPrimaryImplementation(
               interfaceModelOID, processID, implementationModelID, linkingOptions);
         print("Primary implementation set.");
      }
      finally
      {
         serviceFactory.close();
      }

      return 0;
   }

   public String getSummary()
   {
      return "Set a primary process immplementation.";
   }
}
