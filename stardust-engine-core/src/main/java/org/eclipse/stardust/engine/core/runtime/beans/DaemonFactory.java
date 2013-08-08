/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
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
import java.util.List;
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

   private final Map<String, IDaemon> defaultDaemons;

   private DaemonFactory()
   {
      defaultDaemons = CollectionUtils.newMap();
      defaultDaemons.put(EventDaemon.ID, new EventDaemon());
      defaultDaemons.put(CriticalityDaemon.ID, new CriticalityDaemon());
      defaultDaemons.put(SystemDaemon.ID, new SystemDaemon());
   }

   private void bootstrap()
   {
      List<IModel> models = ModelManagerFactory.getCurrent().findActiveModels();
      if (models != null && !models.isEmpty())
      {
         // Initialize daemons map with default daemons as these are not model dependent
         Map<String, IDaemon> computedDaemons = CollectionUtils.copyMap(defaultDaemons);

         for (IModel model : models)
         {
            // Compute and store the model dependent daemons in model for faster lookup
            Map<String, IDaemon> modelDependentDaemons = (Map<String, IDaemon>) model
                  .getRuntimeAttribute(CACHED_DAEMON_FACTORY);
            if (modelDependentDaemons == null)
            {
               modelDependentDaemons = CollectionUtils.newMap();
               for (Iterator i = model.getAllTriggerTypes(); i.hasNext();)
               {
                  ITriggerType type = (ITriggerType) i.next();
                  if (type.isPullTrigger())
                  {
                     String name = type.getId() + ".trigger";
                     modelDependentDaemons.put(name, new TriggerDaemon(type, name));
                  }
               }
               model.setRuntimeAttribute(CACHED_DAEMON_FACTORY, modelDependentDaemons);
            }

            // Compute union of all daemons
            // This always has to be computed as the daemons of a model might change
            // if new model version is deployed
            computedDaemons.putAll(modelDependentDaemons);
         }

         // set union of daemons for all active models
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
