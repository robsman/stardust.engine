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

import static org.eclipse.stardust.engine.core.runtime.beans.tokencache.GlobalTokenManager.filter;
import static org.eclipse.stardust.engine.core.runtime.beans.tokencache.GlobalTokenManager.lockNextToken;

import java.util.*;
import java.util.Map.Entry;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;

public class LocalTokenManager implements ITokenManager
{
   private final Map<ITransition,Set<TransitionTokenBean>> tokensByTransition = new HashMap();

   private ISecondLevelTokenCache secondLevelCache;

   public LocalTokenManager(ISecondLevelTokenCache secondLevelCache)
   {
      this.secondLevelCache = secondLevelCache;
   }

   private Set<TransitionTokenBean> getTokenSet(ITransition transition)
   {
      return tokensByTransition.get(transition == ActivityThread.START_TRANSITION && !tokensByTransition.containsKey(transition)
            ? null : transition);
   }

   @Override
   public boolean hasUnconsumedTokens(Set<ITransition> transitions)
   {
      for (ITransition transition : transitions)
      {
         if (TokenCache.containsUnconsumedToken(getTokenSet(transition)))
         {
            return true;
         }
      }
      return false;
   }

   public void registerToken(ITransition transition, TransitionTokenBean token)
   {
      Set<TransitionTokenBean> tokensForTransition = getTokenSet(transition);
      if (null == tokensForTransition)
      {
         // most transitions will only have one associated token, so optimize for this
         // case
         tokensForTransition = Collections.singleton(token);
         tokensByTransition.put(transition, tokensForTransition);
      }
      else
      {
         if (tokensForTransition.size() == 1)
         {
            tokensForTransition = CollectionUtils.copySet(tokensForTransition);
            tokensByTransition.put(transition, tokensForTransition);
         }
         tokensForTransition.add(token);
      }

      secondLevelCache.addLocalToken(token);
   }

   public boolean removeToken(TransitionTokenBean token)
   {
      Set<TransitionTokenBean> tokensForTransition = getTokenSet(token.getTransition());
      if (tokensForTransition == null || tokensForTransition.size() == 0)
      {
         return false;
      }
      else if (tokensForTransition.size() == 1 && tokensForTransition.contains(token))
      {
         // most transitions will only have one associated token, so optimize for this
         // case
         tokensByTransition.remove(token.getTransition());
         return true;
      }
      else
      {
         return tokensForTransition.remove(token);
      }
   }

   public TransitionTokenBean lockFirstAvailableToken(ITransition transition)
   {
      Set<TransitionTokenBean> tokensForTransition = getTokenSet(transition);
      if (tokensForTransition == null || tokensForTransition.isEmpty())
      {
         return null;
      }

      for (TransitionTokenBean token : tokensForTransition)
      {
         if (!token.isBound())
         {
            if (token.lockAndReload() == 0 && !token.isBound())
            {
               return token;
            }
         }
      }
      return null;
   }

   public void flush()
   {
      for (Entry<ITransition, Set<TransitionTokenBean>> e : tokensByTransition.entrySet())
      {
         ITransition transition = e.getKey();
         for (TransitionTokenBean token : e.getValue())
         {
            if (!token.isPersistent() && !token.isConsumed())
            {
               token.persist();
            }
            if (!token.isConsumed())
            {
               secondLevelCache.registerToken(transition, token);
            }
         }
      }
      tokensByTransition.clear();
   }

   public TransitionTokenBean getTokenForTarget(ITransition transition,
         long targetActivityInstanceOid)
   {
      Set<TransitionTokenBean> tokensForTransition = getTokenSet(transition);
      if (tokensForTransition == null || tokensForTransition.isEmpty())
      {
         return null;
      }

      for (TransitionTokenBean token : tokensForTransition)
      {
         if (token.getTarget() == targetActivityInstanceOid)
         {
            return token;
         }
      }
      return null;
   }

   @Override
   public TransitionTokenBean lockSourceAndOtherToken(final TransitionTokenBean sourceToken)
   {
      Set<TransitionTokenBean> tokenSet = getTokenSet(null);
      if (tokenSet == null || tokenSet.isEmpty()
            || !tokenSet.contains(sourceToken) || sourceToken.lockAndReload() > 0)
      {
         return null;
      }

      return lockNextToken(sourceToken, filter(sourceToken, tokenSet));
   }
}
