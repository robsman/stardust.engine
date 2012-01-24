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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ITriggerType;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DaemonFactory
{
   private static final String CACHED_DAEMON_FACTORY = "cached.daemon.factory";

   private static Map<Short, DaemonFactory> daemonFactoryPartitions = CollectionUtils.newHashMap();
   
   private volatile Map<String, IDaemon> daemons;
   
   private Map<String, IDaemon> defaultDaemons;

   private DaemonFactory()
   {
      defaultDaemons = CollectionUtils.newMap();
      defaultDaemons.put(EventDaemon.ID, new EventDaemon());
      defaultDaemons.put(CriticalityDaemon.ID, new CriticalityDaemon());
      defaultDaemons.put(SystemDaemon.ID, new SystemDaemon());
   }

   private void bootstrap()
   {
      IModel model = ModelManagerFactory.getCurrent().findActiveModel();
      if (model != null)
      {
         Map<String, IDaemon> computedDaemons = (Map<String, IDaemon>)
            model.getRuntimeAttribute(CACHED_DAEMON_FACTORY);
         if (computedDaemons == null)
         {
            computedDaemons = CollectionUtils.newMap();
            for (Iterator i = model.getAllTriggerTypes(); i.hasNext();)
            {
               ITriggerType type = (ITriggerType) i.next();
               if (type.isPullTrigger())
               {
                  String name = type.getId() + ".trigger";
                  computedDaemons.put(name, new TriggerDaemon(type, name));
               }
            }
            computedDaemons.putAll(defaultDaemons);
            model.setRuntimeAttribute(CACHED_DAEMON_FACTORY, computedDaemons);
         }
         daemons = computedDaemons;
      }
      else
      {
         daemons = defaultDaemons;
      }
   }

   public static synchronized DaemonFactory instance()
   {
      Short partitionOid = new Short(SecurityProperties.getPartitionOid());
      DaemonFactory instance = (DaemonFactory) daemonFactoryPartitions.get(partitionOid);
      if (instance == null)
      {
         instance = new DaemonFactory();
         daemonFactoryPartitions.put(partitionOid, instance);
      }
      instance.bootstrap();
      return instance;
   }

   public IDaemon get(String type)
   {
      return (IDaemon) daemons.get(type);
   }

   public Iterator<IDaemon> getAllDaemons()
   {
      return daemons.values().iterator();
   }
}
