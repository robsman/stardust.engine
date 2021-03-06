/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonLog;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.GetDaemonLogAction;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemDescription;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.ItemLocatorUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author born
 * @version $Revision$
 */
public class Utils
{

   public static void flushSession()
   {
      org.eclipse.stardust.engine.core.persistence.Session session
         = SessionFactory.getSession(SessionProperties.DS_NAME_AUDIT_TRAIL);
      if(session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession
            = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;
         jdbcSession.save(false);
      }
   }

   public static void initCarnotEngine(String partitionId)
   {
      initCarnotEngine(partitionId, Collections.emptyMap(), InitOptions.DEFAULT);
   }

   public static void initCarnotEngine(String partitionId, InitOptions initOptions)
   {
      initCarnotEngine(partitionId, Collections.emptyMap(), initOptions);
   }

   public static void initCarnotEngine(String partitionId, Map properties)
   {
      initCarnotEngine(partitionId, properties, InitOptions.DEFAULT);
   }

   public static void initCarnotEngine(String partitionId, Map properties, InitOptions initOptions)
   {
      try
      {
         if (initOptions.flushBeforeInit)
         {
            Parameters.instance().flush();
         }

         if ( !properties.isEmpty())
         {
            ParametersFacade.pushLayer(properties);
         }

         Parameters params = Parameters.instance();

         Session session = SessionFactory
               .createSession(SessionProperties.DS_NAME_AUDIT_TRAIL);

         Map locals = new HashMap();
         locals.put(SessionProperties.DS_NAME_AUDIT_TRAIL
               + SessionProperties.DS_SESSION_SUFFIX, session);
         ParametersFacade.pushLayer(locals);

         // simulating bootstrapping
         ItemLocatorUtils.registerDescription(ModelManagerFactory.ITEM_NAME,
               new ItemDescription(new ModelManagerLoader(), params.getString(
                     EngineProperties.WATCHER_PROPERTY, NullWatcher.class.getName())));

         params.setBoolean(EngineService.BOOTSTRAPPED, true);

         params.setString(SecurityProperties.DEFAULT_PARTITION, partitionId);
         params.setString(SecurityProperties.DEFAULT_REALM, "carnot"); //$NON-NLS-1$
         params.setString(SecurityProperties.PARTITION, partitionId);

         IAuditTrailPartition partition = SynchronizationService.getPartition(Collections
               .singletonMap(SecurityProperties.PARTITION, params
                     .getString(SecurityProperties.PARTITION)));
         if (null != partition)
         {
            params.set(SecurityProperties.CURRENT_PARTITION, partition);
            params.set(SecurityProperties.CURRENT_PARTITION_OID, new Short(partition
                  .getOID()));
         }

         params.set(EngineProperties.FORKING_SERVICE_HOME, new ForkingServiceFactory()
         {
            public ForkingService get()
            {
               return new ForkingService()
               {
                  public void fork(IActionCarrier action, boolean transacted)
                  {

                  }

                  public Object isolate(Action action) throws PublicException
                  {
                     if (action instanceof GetDaemonLogAction)
                     {
                        return new DaemonLog();
                     }
                     else
                     {
                        return null;
                     }
                  }
               };
            }

            public IJobManager getJobManager()
            {
               return new ForkingServiceJobManager(get());
            }

            public void release(ForkingService service)
            {
            }

            public void release(IJobManager jobManager)
            {
               if (jobManager instanceof ForkingServiceJobManager)
               {
                  release(((ForkingServiceJobManager) jobManager).getForkingService());
               }
            }
         });
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   public static class InitOptions
   {
      public static final InitOptions DEFAULT = new InitOptions();
      public static final InitOptions NO_FLUSH_BEFORE_INIT = new InitOptions(false);

      private final boolean flushBeforeInit;

      public InitOptions()
      {
         this(true);
      }

      public InitOptions(boolean flushBeforeInit)
      {
         this.flushBeforeInit = flushBeforeInit;
      }

      public boolean isFlushBeforeInit()
      {
         return flushBeforeInit;
      }
   }

   private Utils()
   {

   }
}
