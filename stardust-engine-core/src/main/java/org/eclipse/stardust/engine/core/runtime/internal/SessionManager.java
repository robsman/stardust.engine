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
package org.eclipse.stardust.engine.core.runtime.internal;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Parameters.IDisposable;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IJobDescriptor;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.UserUtils;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SessionManager implements IDisposable
{
   private static final Logger trace = LogManager.getLogger(SessionManager.class);
   
   public static final String PRP_SESSION_MANAGER_INSTANCE = SessionManager.class.getName()
         + ".INSTANCE";

   public static final String PRP_SESSION_PREFIX = "Carnot.AuditTrail.Session";

   public static final String PRP_SESSION_EXPIRATION_INTERVAL = PRP_SESSION_PREFIX + ".ExpirationInterval";
   
   public static final String PRP_SESSION_UPDATE_DELAY = PRP_SESSION_PREFIX + ".UserActivityUpdateDelay";
   
   public static final String PRP_SESSION_NO_TRACKING = PRP_SESSION_PREFIX + ".NoSessionTracking";
   
   private final int automaticSessionTimeout;

   private final Map/*<Long, Date>*/ lastModificationTimes;
   
   private final int syncToDiskInterval;
   
   private Long nextSynchToDiskTime;
   
   public static SessionManager instance()
   {
      GlobalParameters globals = GlobalParameters.globals();

      SessionManager manager = (SessionManager) globals.get(PRP_SESSION_MANAGER_INSTANCE);

      if (null == manager)
      {
         synchronized (globals)
         {
            // double checked locking is fine here as we read from a map, not from a field
            manager = (SessionManager) globals.get(PRP_SESSION_MANAGER_INSTANCE);

            if (null == manager)
            {
               manager = new SessionManager();
               globals.set(PRP_SESSION_MANAGER_INSTANCE, manager);
            }
         }
      }

      return manager;
   }

   /**
    * @param user the user.
    * @return true if session tracking is disabled for the given user, otherwise false.
    */
   public static boolean isUserSessionTrackingDisabled(IUser user)
   {
      String userIdSpec = Parameters.instance().getString(PRP_SESSION_NO_TRACKING, "")
            .trim();
      return UserUtils.isUserMatchingIdSpec(user, userIdSpec);
   }

   public SessionManager()
   {
      this.automaticSessionTimeout = Parameters.instance().getInteger(
            PRP_SESSION_EXPIRATION_INTERVAL, 60);

      this.syncToDiskInterval = Parameters.instance().getInteger(
            PRP_SESSION_UPDATE_DELAY, 10);
      
      this.lastModificationTimes = CollectionUtils.createMap();
   }
   
   public long getExpirationTime(long lastModificationTime)
   {
      final Calendar expirationTimeCalculator = Calendar.getInstance();
      
      expirationTimeCalculator.setTimeInMillis(lastModificationTime);
      expirationTimeCalculator.add(Calendar.MINUTE, automaticSessionTimeout);
      return expirationTimeCalculator.getTimeInMillis();
   }
   
   public synchronized void updateLastModificationTime(IUser user)
   {
      // no tracking for configured users
      if (isUserSessionTrackingDisabled(user))
      {
         return;
      }
      
      Date lastSeen = (Date) lastModificationTimes.get(new Long(user.getOID()));
      if (null == lastSeen)
      {
         lastSeen = new Date();
         lastModificationTimes.put(new Long(user.getOID()), lastSeen);
      }
      lastSeen.setTime(System.currentTimeMillis());

      synchToDiskIfNeeded();
   }

   private void synchToDiskIfNeeded()
   {
      if (null == nextSynchToDiskTime)
      {
         this.nextSynchToDiskTime = new Long(scheduleNextSyncToDisk(syncToDiskInterval));
      }
      
      if (System.currentTimeMillis() >= nextSynchToDiskTime.longValue())
      {
         try
         {
            triggerSynchToDisk();
            
            this.nextSynchToDiskTime = null;
         }
         catch (RuntimeException re)
         {
            trace.warn(
                  MessageFormat.format(
                        "Failed triggering asynchronous write of user session updates to the audit trail. Trying again in {0}ms.",
                        new Object[] {new Integer(syncToDiskInterval)}), re);
            
            this.nextSynchToDiskTime = new Long(scheduleNextSyncToDisk(syncToDiskInterval));
         }
      }
   }
   
   private synchronized void triggerSynchToDisk()
   {
      if ( !lastModificationTimes.isEmpty())
      {
         ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance()
               .get(EngineProperties.FORKING_SERVICE_HOME);
         if (factory != null)
         {
            IJobManager jobManager = factory.getJobManager();
            try
            {
               // immediately start writing asynchronously to disk
               jobManager.startAsynchronousJob(new IJobDescriptor()
               {
                  public ActionCarrier getCarrier()
                  {
                     return new SynchUserSessionsToDiskCarrier(
                           CollectionUtils.copyMap(lastModificationTimes));
                  }
               });
               
               // if writing was successfully triggered clear transient cache 
               lastModificationTimes.clear();
            }
            finally
            {
               factory.release(jobManager);
            }
         }
      }
   }

   private static long scheduleNextSyncToDisk(int syncToDiskInterval)
   {
      // schedule next write
      Calendar nextSyncTimeCalculator = Calendar.getInstance();
      nextSyncTimeCalculator.add(Calendar.SECOND, syncToDiskInterval);

      return nextSyncTimeCalculator.getTimeInMillis();
   }
   
   public void dispose()
   {
      triggerSynchToDisk();
   }
}
