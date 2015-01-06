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

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ITriggerType;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public final class DaemonFactory
{
   private static final ServiceLoader<IDaemon.Factory> factoryLoader = ServiceLoader.load(IDaemon.Factory.class);

   private Map<String, IDaemon> daemons = CollectionUtils.newMap();

   private DaemonFactory()
   {
      for (IDaemon.Factory factory : DaemonFactory.factoryLoader)
      {
         for (IDaemon daemon : factory.getDaemons())
         {
            daemons.put(daemon.getType(), daemon);
         }
      }
   }

   public static DaemonFactory instance()
   {
      return new DaemonFactory();
   }

   public IDaemon get(String type)
   {
      return (IDaemon) daemons.get(type);
   }

   public Iterator<IDaemon> getAllDaemons()
   {
      return daemons.values().iterator();
   }

   public static final class PredefinedDaemonsFactory implements IDaemon.Factory
   {
      private static final Collection<IDaemon> predefinedDaemons = Arrays.asList(new IDaemon[] {
            new EventDaemon(),
            new CriticalityDaemon(),
            new SystemDaemon()
      });

      @Override
      public Collection<IDaemon> getDaemons()
      {
         return predefinedDaemons;
      }
   }

   public static final class TriggerDaemonsFactory implements IDaemon.Factory
   {
      private static final String CACHED_DAEMON_FACTORY = "cached.daemon.factory";

      @Override
      public Collection<IDaemon> getDaemons()
      {
         List<IModel> models = ModelManagerFactory.getCurrent().findActiveModels();
         if (!models.isEmpty())
         {
            Set<String> ids = CollectionUtils.newSet();
            List<IDaemon> daemons = CollectionUtils.newList();
            for (IModel model : models)
            {
               // Compute and store the model dependent daemons in model for faster lookup
               List<IDaemon> modelDependentDaemons;
               synchronized (model)
               {
                  modelDependentDaemons = (List<IDaemon>)
                        model.getRuntimeAttribute(CACHED_DAEMON_FACTORY);
                  if (modelDependentDaemons == null)
                  {
                     modelDependentDaemons = CollectionUtils.newList();
                     for (Iterator i = model.getAllTriggerTypes(); i.hasNext();)
                     {
                        ITriggerType type = (ITriggerType) i.next();
                        if (type.isPullTrigger())
                        {
                           modelDependentDaemons.add(new TriggerDaemon(type));
                        }
                     }
                     model.setRuntimeAttribute(CACHED_DAEMON_FACTORY, modelDependentDaemons);
                  }
               }
               // Compute union of all daemons
               // This always has to be computed as the daemons of a model might change
               // if new model version is deployed
               for (IDaemon daemon : modelDependentDaemons)
               {
                  String type = daemon.getType();
                  if (!ids.contains(type))
                  {
                     daemons.add(daemon);
                     ids.add(type);
                  }
               }
            }
            return daemons;
         }
         return Collections.emptyList();
      }
   }
}
