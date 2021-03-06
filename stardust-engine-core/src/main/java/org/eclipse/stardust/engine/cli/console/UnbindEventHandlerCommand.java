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

import org.eclipse.stardust.engine.api.runtime.WorkflowService;


/**
 * @author holger.prause
 * @version
 * 
 * Unbinds event handlers via the console
 */
public class UnbindEventHandlerCommand extends EventHandlerCommand
{

   @Override
   public int run(Map options)
   {
      EventHandlerConfig config = getConfig(options);
      WorkflowService workflowService = getWorkflowService(config);
      if (config.getType().equals(TYPE_OPTION_AI_VALUE))
      {
         workflowService.unbindActivityEventHandler(config.getOid(), config.getHandler());
      }
      else if (config.getType().equals(TYPE_OPTION_PI_VALUE))
      {
         workflowService.unbindProcessEventHandler(config.getOid(), config.getHandler());
      }

      return 1;
   }

   @Override
   public String getSummary()
   {
      return "Unbinds an EventHandler from an ActivityInstance or ProcessInstance";
   }
}
