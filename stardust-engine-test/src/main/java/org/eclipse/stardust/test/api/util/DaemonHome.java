/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.util;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.AcknowledgementState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.test.api.setup.TestRtEnvException;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;

/**
 * <p>
 * This utility class allows for starting and stopping <i>Stardust</i> daemons.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DaemonHome
{
   /**
    * <p>
    * Starts all available daemons.
    * </p>
    * 
    * <p>
    * Which daemons are started depends on the deployed runtime environment (i.e. the model).
    * </p>
    * 
    * @param adminService the service needed for starting the daemons
    */
   public static void startAllDaemons(final AdministrationService adminService)
   {
      if (adminService == null)
      {
         throw new NullPointerException("Administration Service must not be null.");
      }
      
      final List<Daemon> allDaemons = adminService.getAllDaemons(false);
      for (final Daemon d : allDaemons)
      {
         startDaemonInternal(adminService, d.getType());
      }
   }
   
   /**
    * <p>
    * Stops all running daemons, i.e. tries to stop a daemon only if it is running.
    * </p>
    * 
    * @param adminService the service needed for stopping the daemons
    */
   public static void stopAllRunningDaemons(final AdministrationService adminService)
   {
      if (adminService == null)
      {
         throw new NullPointerException("Administration Service must not be null.");
      }
      
      final List<Daemon> allDaemons = adminService.getAllDaemons(false);
      for (final Daemon d : allDaemons)
      {
         final Daemon daemon = adminService.getDaemon(d.getType(), false);
         if (daemon.isRunning())
         {
            stopDaemonInternal(adminService, d.getType());
         }
      }
   }
      
   /**
    * <p>
    * Starts the given daemon.
    * </p>
    * 
    * @param adminService the service needed for starting the daemon
    * @param daemonType the daemon to start
    */
   public static void startDaemon(final AdministrationService adminService, final DaemonType daemonType)
   {
      if (adminService == null)
      {
         throw new NullPointerException("Administration Service must not be null.");
      }
      
      startDaemonInternal(adminService, daemonType.id());
   }
   
   /**
    * <p>
    * Stops the given daemon.
    * </p>
    * 
    * @param adminService the service needed for stopping the daemon
    * @param daemonType the daemon to stop
    */
   public static void stopDaemon(final AdministrationService adminService, final DaemonType daemonType)
   {
      if (adminService == null)
      {
         throw new NullPointerException("Administration Service must not be null.");
      }
      
      stopDaemonInternal(adminService, daemonType.id());
   }

   private static void startDaemonInternal(final AdministrationService adminService, final String daemonType)
   {
      final Daemon daemon = adminService.startDaemon(daemonType, true);
      
      final boolean isRunning = daemon.isRunning();
      final boolean isAck = daemon.getAcknowledgementState().equals(AcknowledgementState.RespondedOK);
      if (!isRunning || !isAck)
      {
         throw new TestRtEnvException("Unable to start daemon '" + daemon + "'.", TestRtEnvAction.DAEMON_SETUP);
      }
   }
   
   private static void stopDaemonInternal(final AdministrationService adminService, final String daemonType)
   {
      final Daemon daemon = adminService.stopDaemon(daemonType, true);
      
      final boolean isRunning = daemon.isRunning();
      final boolean isAck = daemon.getAcknowledgementState().equals(AcknowledgementState.RespondedOK);
      if (isRunning || !isAck)
      {
         throw new TestRtEnvException("Unable to stop daemon '" + daemon + "'.", TestRtEnvAction.DAEMON_TEARDOWN);
      }      
   }
   
   /**
    * <p>
    * Represents a <i>Stardust</i> daemon.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static enum DaemonType
   {
      MAIL_DAEMON
      {
         @Override
         public String id()
         {
            return "mail.trigger";
         }
      },
      TIMER_TRIGGER_DAEMON
      {
         @Override
         public String id()
         {
            return "timer.trigger";
         }
      },
      EVENT_DAEMON
      {
         @Override
         public String id()
         {
            return "event.daemon";
         }
      },
      SYSTEM_DAEMON
      {
         @Override
         public String id()
         {
            return "system.daemon";
         }
      },
      PRIORITIZATION_DAEMON
      {
         @Override
         public String id()
         {
            return "criticality.daemon";
         }
      };
      
      public abstract String id();
   }
}
