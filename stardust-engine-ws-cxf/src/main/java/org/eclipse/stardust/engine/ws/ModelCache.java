/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
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
   private final long expirationInterval = 10L * 60L * 1000L; // 10m
   
   private ConcurrentHashMap<Long, CachedModel> cache = new ConcurrentHashMap<Long, CachedModel>();
   
   public void reset()
   {
      cache.clear();
   }
   
   public DeployedModel getModel(int modelOid)
   {
      Long key = new Long(modelOid);
      
      CachedModel cachedModel = cache.get(key);

      if ((null == cachedModel)
            || (System.currentTimeMillis() > cachedModel.expirationDate))
      {
         return null;
      }
      else
      {
         return cachedModel.model;
      }
   }

   public void putModel(DeployedModel model)
   {
      CachedModel cachedModel = new CachedModel(model, System.currentTimeMillis()
            + expirationInterval);
      
      cache.put(new Long(model.getModelOID()), cachedModel);
      
      if (model.isActive())
      {
         cache.put(new Long(PredefinedConstants.ACTIVE_MODEL), cachedModel);
      }
   }
   
   private static class CachedModel
   {
      final DeployedModel model;
      
      final long expirationDate;
      
      public CachedModel(DeployedModel model, long expirationDate)
      {
         this.model = model;
         this.expirationDate = expirationDate;
      }
   }
}
