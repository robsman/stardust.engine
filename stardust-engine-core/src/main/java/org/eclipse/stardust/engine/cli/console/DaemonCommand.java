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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DaemonCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();
   private static final String NAME = "name";
   private static final String START = "start";
   private static final String STOP = "stop";
   private static final String STATUS = "status";
   private static final String LIST = "list";
   private static final String ACK = "ack";

   static
   {
      argTypes.register("-name", "-n", NAME, "The name of the daemon.", true);
      argTypes.register("-start", "-a", START, "Starts the daemon", false);
      argTypes.register("-stop", "-z", STOP, "Stops the daemon.", false);
      argTypes.register("-status", "-s", STATUS, "Shows the daemon state.", false);
      argTypes.register("-list", "-l", LIST, "Lists all daemons.", false);
      argTypes.register("-ack", "-c", ACK, "Executes a daemon command with "
            + "acknowledgement.", false);
      argTypes.addExclusionRule(new String[] {START, STOP, STATUS, LIST}, true);
      argTypes.addInclusionRule(START, NAME);
      argTypes.addInclusionRule(STOP, NAME);
      argTypes.addInclusionRule(STATUS, NAME);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public boolean delayExit()
   {
      return Parameters.instance().getString(EngineProperties.CLIENT_SERVICE_FACTORY,
            PredefinedConstants.POJO_SERVICEFACTORY_CLASS).equals(
                  PredefinedConstants.POJO_SERVICEFACTORY_CLASS);
   }

   public int run(Map options)
   {
      boolean ack = options.containsKey(ACK);
      String name = (String) options.get(NAME);

      ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);

      try
      {
         if (options.containsKey(START))
         {
            serviceFactory.getAdministrationService().startDaemon(name, ack);
            print("Daemon '" + name +"' started.");
         }
         else if (options.containsKey(STOP))
         {
            serviceFactory.getAdministrationService().stopDaemon(name, ack);
            print("Daemon '" + name +"' stopped.");

         }
         else if (options.containsKey(STATUS))
         {
            Daemon daemon = serviceFactory.getAdministrationService().getDaemon(name, ack);
            if (daemon.isRunning())
            {
               print("Daemon '" + name +"' is running.\n");
            }
            else
            {
               print("Daemon '" + name +"' is not running.\n");
            }
            Date lastExecutionTime = daemon.getLastExecutionTime();
            if (lastExecutionTime != null)
            {
               print("Last executed: " + DateUtils.getInteractiveDateFormat().format(lastExecutionTime));
            }
            Date startTime = daemon.getStartTime();
            if (startTime != null)
            {
               print("Last started : " + DateUtils.getInteractiveDateFormat().format(startTime));
            }
         }
         else if (options.containsKey(LIST))
         {
            // @todo (france, ub): exploit noack
            print("Registered daemons:\n");
            Collection daemons = serviceFactory.getAdministrationService().getAllDaemons(true);
            for (Iterator i = daemons.iterator(); i.hasNext();)
            {
               Daemon daemon = (Daemon) i.next();
               print(daemon.getType() + " ("
                     + (daemon.isRunning() ? "started" : "stopped")
                     + ", ackState: "
                     + daemon.getAcknowledgementState().getName() + ")");
            }
         }
      }
      finally
      {
         serviceFactory.close();
      }
      return 0;
   }

   public String getSummary()
   {
      return "Manages a daemon";
   }
}
