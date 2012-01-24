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
package org.eclipse.stardust.engine.core.runtime.beans.tokencache;

import java.util.*;
import java.util.Map.Entry;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;


public class LocalTokenManager implements ITokenManager
{
   
   private static final Logger trace = LogManager.getLogger(LocalTokenManager.class);
   
   private final Map /*<ITransition,<TransitionTokenBean>>*/ tokensByTransition = new HashMap();

   private ISecondLevelTokenCache secondLevelCache;

   public LocalTokenManager(ISecondLevelTokenCache secondLevelCache)
   {
      this.secondLevelCache = secondLevelCache;
   }
   
   private Set /*<TransitionTokenBean>*/ getTokenSet(ITransition transition)
   {
      return (Set) this.tokensByTransition.get(transition);
   }
   
   public void registerToken(ITransition transition, TransitionTokenBean token)
   {
      Set/*<TransitionTokenBean>*/ tokensForTransition = getTokenSet(transition);
      if (null == tokensForTransition)
      {
         // most transitions will only have one associated token, so optimize for this
         // case
         tokensForTransition = Collections.singleton(token);
         this.tokensByTransition.put(transition, tokensForTransition);
      }
      else
      {
         if (1 == tokensForTransition.size())
         {
            tokensForTransition = CollectionUtils.copySet(tokensForTransition);
            this.tokensByTransition.put(transition, tokensForTransition);
         }
         tokensForTransition.add(token);
      }

      secondLevelCache.addLocalToken(token);
   }

   public boolean removeToken(TransitionTokenBean token)
   {
      Set /*<TransitionTokenBean>*/ tokensForTransition = getTokenSet(token.getTransition());
      if (tokensForTransition == null || tokensForTransition.size() == 0)
      {
         return false;
      }
      else if ((1 == tokensForTransition.size()) && tokensForTransition.contains(token))
      {
         // most transitions will only have one associated token, so optimize for this
         // case
         this.tokensByTransition.remove(token.getTransition());

         return true;
      }
      else
      {
         return tokensForTransition.remove(token);
      }
   }
   
   public TransitionTokenBean lockFirstAvailableToken(ITransition transition)
   {
      Set /*<TransitionTokenBean>*/ tokensForTransition = getTokenSet(transition);
      if (tokensForTransition == null || tokensForTransition.size() == 0)
      {
         return null;
      }
      
      for (Iterator iterator = tokensForTransition.iterator(); iterator.hasNext();)
      {
         TransitionTokenBean token = (TransitionTokenBean) iterator.next();
         if (!token.isBound())
         {
            try
            {
               token.lock();
               if (trace.isDebugEnabled())
               {
                  trace.debug("token " + token + " locked.");
               }
               token.reload();
               if (!token.isBound())
               {
                  return token;
               }
            }
            catch (ConcurrencyException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Concurrent attempt to lock token: " + token);
               }
            }
            catch (PhantomException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Try to bind phantom token: " + token);
               }
            }
         }
      }
      return null;
   }

   public void flush()
   {
      for (Iterator i = this.tokensByTransition.entrySet().iterator(); i.hasNext();)
      {
         Entry e = (Entry)i.next();
         ITransition transition = (ITransition) e.getKey();
         Set /*<TransitionTokenBean>*/ tokensForTransition = (Set) e.getValue();
         for (Iterator j = tokensForTransition.iterator(); j.hasNext();)
         {
            TransitionTokenBean token = (TransitionTokenBean) j.next();
            if (!token.isPersistent() && !token.isConsumed())
            {
               token.persist();
            }
            if (!token.isConsumed())
            {
               this.secondLevelCache.registerToken(transition, token);
            }
         }
      }
      this.tokensByTransition.clear();
   }

   public TransitionTokenBean getTokenForTarget(ITransition transition,
         long targetActivityInstanceOid)
   {
      Set /*<TransitionTokenBean>*/ tokensForTransition = getTokenSet(transition);
      if (tokensForTransition == null || tokensForTransition.size() == 0)
      {
         return null;
      }
      
      for (Iterator i = tokensForTransition.iterator(); i.hasNext();)
      {
         TransitionTokenBean token = (TransitionTokenBean) i.next();
         if (token.getTarget() == targetActivityInstanceOid)
         {
            return token;
         }
      }
      return null;
   }

}
