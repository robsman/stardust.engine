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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.FactoryFinder;
import org.eclipse.stardust.common.config.FactoryFinder.ConfigurationError;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.spi.runtime.ISystemAction;


/**
 * @author ubirkemeyer
 * @version $Revision: 31061 $
 */
public class SystemDaemon implements IDaemon
{
   private static final Logger trace = LogManager.getLogger(SystemDaemon.class);
   public static final Logger daemonLogger = RuntimeLog.DAEMON;

   public static final String ID = AdministrationService.SYSTEM_DAEMON;

   private List<ISystemAction> actions;

   private int currentAction;

   private List<ISystemAction> getActions() throws ConfigurationError
   {
      if (actions == null)
      {
         actions = CollectionUtils.newList();
         List<ISystemAction.Factory> factories = FactoryFinder.findFactories(ISystemAction.Factory.class, null, null);
         for (ISystemAction.Factory factory : factories)
         {
            List<ISystemAction> newActions = factory.createActions();
            if (newActions != null)
            {
               actions.addAll(newActions);
            }
            trace.info("Registered system action factory: " + factory.getId());
         }
         currentAction = 0;
      }
      return actions;
   }

   public ExecutionResult execute(long batchSize)
   {
      List<ISystemAction> actions = getActions();
      if (actions.size() == 0)
      {
         return IDaemon.WORK_DONE;
      }

      IModel model = ModelManagerFactory.getCurrent().findActiveModel();
      if (model == null)
      {
         return IDaemon.WORK_DONE;
      }

      long nActions = 0;

      while (nActions < batchSize)
      {
         ISystemAction action = actions.get(currentAction++);
         try
         {
            daemonLogger.info("System Daemon, process action '" + action.toString() + "'.");
            action.run();
         }
         catch (Throwable t)
         {
            trace.error("Exception while executing action: " + action.getId(), t);
         }
         nActions++;
         if (currentAction >= actions.size())
         {
            currentAction = 0;
         }
         if (nActions >= actions.size())
         {
            return IDaemon.WORK_DONE;
         }
      }

      return (nActions >= batchSize) ? IDaemon.WORK_PENDING : IDaemon.WORK_DONE;
   }

   public String getType()
   {
      return ID;
   }

   public long getDefaultPeriodicity()
   {
      return 5;
   }
}