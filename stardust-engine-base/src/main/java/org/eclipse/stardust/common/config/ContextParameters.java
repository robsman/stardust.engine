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
package org.eclipse.stardust.common.config;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.Context;

import org.eclipse.stardust.common.constants.BaseConfigParameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author fherinean
 * @version $Revision$
 */
class ContextParameters
{
   private static final Logger trace = LogManager.getLogger(ContextParameters.class);

   private static final String PRP_SHARED_INSTANCES = ContextParameters.class.getName()
         + ".SharedInstances";
   
   private Stack<ContextCache> contexts;

   private static AtomicReference<ContextCache> contextCache = new AtomicReference<ContextCache>();

   private static ConcurrentHashMap<String, String> scopes = new ConcurrentHashMap<String, String>();

   ContextParameters()
   {
      contexts = new Stack<ContextCache>();
   }

   Object get(String name)
   {
      Object value = get(name, contexts.isEmpty() ? null : contexts.peek());
      return value == null ? get(name, getContextCache()) : value;
   }

   private ContextCache getContextCache()
   {
      return contextCache.get();
   }

   private Object get(String name, ContextCache context)
   {
      return context == null ? null : context.get(name);
   }

   final ContextCache getCachedContext(String scope)
   {
      return getCachedContext(Parameters.instance(), scope);
   }

   final ContextCache getCachedContext(Parameters params, String scope)
   {
      ContextCache result = null;
      
      if (params.getBoolean(BaseConfigParameters.SHARED_CONTEXT_CACHE, true))
      {
         @SuppressWarnings("unchecked")
         ConcurrentHashMap<String, ContextCache> sharedInstances = (ConcurrentHashMap<String, ContextCache>) params.get(PRP_SHARED_INSTANCES);

         if (null != sharedInstances)
         {
            result = sharedInstances.get(scope);
         }
      }
      
      return result;
   }

   final void pushContext(Context context, String scope)
   {
      final Parameters params = Parameters.instance();

      ContextCache ctx = null;

      boolean shareInstances = params.getBoolean(
            BaseConfigParameters.SHARED_CONTEXT_CACHE, true);
      if (shareInstances)
      {
         @SuppressWarnings("unchecked")
         ConcurrentHashMap<String, ContextCache> sharedInstances = (ConcurrentHashMap<String, ContextCache>) params.get(PRP_SHARED_INSTANCES);
         if (null == sharedInstances)
         {
            sharedInstances = new ConcurrentHashMap<String, ContextCache>();
            params.set(PRP_SHARED_INSTANCES, sharedInstances);
         }

         ctx = sharedInstances.get(scope);

         if (null == ctx)
         {
            synchronized (sharedInstances)
            {
               ctx = sharedInstances.get(scope);
               if (null == ctx)
               {
                  ctx = new ContextCache(null);
                  sharedInstances.put(scope, ctx);
                  
                  setContext(context, scope, ctx);
               }
            }
         }
      }
      else
      {
         ctx = new ContextCache(null);
         
         setContext(context, scope, ctx);
      }

      pushContext(ctx);
   }

   final void pushContext(ContextCache cachedContext)
   {
      contexts.push(cachedContext);
   }

   void popContext()
   {
      contexts.pop();
   }

   void setGlobalContext(Context context, String scope)
   {
      contextCache.set(new ContextCache(null));

      setContext(context, scope, getContextCache());
   }

   private void setContext(Context context, String scope, ContextCache cache)
   {
      cache.setContext(context);

      if (null == scopes.putIfAbsent(scope, scope))
      {
         trace.info("Properties added from scope '" + scope + "':");
         LogUtils.listContext("  ", context);
      }
   }
}
