/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2010 CARNOT AG
 */
package org.eclipse.stardust.engine.ws;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class ModelCache
{
   private ConcurrentHashMap<Long, CachedModel> cache = new ConcurrentHashMap<Long, CachedModel>();

   public void reset()
   {
      cache.clear();
   }

   public DeployedModel getModel(long modelOid)
   {
      CachedModel cachedModel = cache.get(modelOid);
      return cachedModel == null ? null : cachedModel.getModel();
   }

   public void putModel(DeployedModel model)
   {
      CachedModel cachedModel = new CachedModel(model);
      cache.put((long) model.getModelOID(), cachedModel);
      if (model.isActive())
      {
         cache.put((long) PredefinedConstants.ACTIVE_MODEL, cachedModel);
      }
   }

   private static class CachedModel
   {
      private final long expirationInterval = 10L * 60L * 1000L; // 10m

      private DeployedModel model;
      private long expirationDate;

      public CachedModel(DeployedModel model)
      {
         this.model = model;
         expirationDate = System.currentTimeMillis() + expirationInterval;
      }

      public DeployedModel getModel()
      {
         return System.currentTimeMillis() < expirationDate ? model : null;
      }
   }
}
