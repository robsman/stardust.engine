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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.*;

/**
 * Hides the puzzling cache semantics from the activity thread. To see all updates during
 * an activity thread run this class behaves as follows:
 * The first time a token is queried for a (processinstance, transition pair), all already
 * persistent tokens for this pair are fetched and in turn retrieved from here.
 * The usage of the persistence framework cache is orthogonal to this.
 */
public class TokenCache
{
   private static final Logger trace = LogManager.getLogger(TokenCache.class);

   private final ITokenManager localTokenCache;
   private final ISecondLevelTokenCache secondLevelCache;
   private final ITokenManager globalTokenCache;

   private IProcessInstance processInstance;

   private long tokenChange = 0;

   public TokenCache(IProcessInstance processInstance)
   {
      this.processInstance = processInstance;

      this.secondLevelCache = TokenManagerRegistry.instance().getSecondLevelCache(processInstance);
      this.localTokenCache = new LocalTokenManager(this.secondLevelCache);
      this.globalTokenCache = new GlobalTokenManager(processInstance);
   }

   public TransitionTokenBean lockFreeToken(ITransition transition)
   {
      TransitionTokenBean lockedToken = this.localTokenCache.lockFirstAvailableToken(transition);

      if (lockedToken == null)
      {
         lockedToken = this.secondLevelCache.lockFirstAvailableToken(transition);
      }
      // in single-node scenario, rely on results from 2ndlevel cache
      if (lockedToken == null && !this.secondLevelCache.hasCompleteInformation())
      {
         lockedToken = this.globalTokenCache.lockFirstAvailableToken(transition);
      }

      return lockedToken;
   }

   public void bindToken(TransitionTokenBean token, IActivityInstance activityInstance)
   {
      token.setTarget(activityInstance);

      if (trace.isDebugEnabled())
      {
         trace.debug("Bound token " + token + " to activity instance "
               + activityInstance);
      }

      if (token.getSource() == 0)
      {
         new TransitionInstanceBean(processInstance, token.getTransition(),
            null, activityInstance);
      }
      else
      {
         try
         {
            ActivityInstanceBean source = ActivityInstanceBean.findByOID(token.getSource());
            new TransitionInstanceBean(processInstance, token.getTransition(), source,
                  activityInstance);
         }
         catch (ObjectNotFoundException e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Skipping creation of transition instance as of unknown "
                     + "source activity.", e);
            }
         }
      }
   }

   /**
    * Returns the bound tokens of the current activity instance.
    * @param activity
    */
   public List<TransitionTokenBean> getBoundInTokens(IActivityInstance boundActivityInstance, IActivity activity)
   {
      List<TransitionTokenBean> result = null;
      IProcessDefinition pd = activity.getProcessDefinition();
      ITransition relocationTransition = pd.findTransition(PredefinedConstants.RELOCATION_TRANSITION_ID);
      if (relocationTransition != null)
      {
         TransitionTokenBean token = getBoundInToken(boundActivityInstance, relocationTransition);
         if (token != null)
         {
            return Collections.singletonList(token);
         }
      }
      if (activity.getId().equals(processInstance.getProcessDefinition().getRootActivity().getId()))
      {
         TransitionTokenBean token = getBoundInToken(boundActivityInstance, ActivityThread.START_TRANSITION);
         if (token != null)
         {
            return Collections.singletonList(token);
         }
      }
      ModelElementList<ITransition> inTransitions = boundActivityInstance.getActivity().getInTransitions();
      for (ITransition transition : inTransitions)
      {
         TransitionTokenBean token = getBoundInToken(boundActivityInstance, transition);
         if (token != null)
         {
            if (inTransitions.size() == 1)
            {
               return Collections.singletonList(token);
            }
            else
            {
               if (result == null)
               {
                  result = CollectionUtils.newList(inTransitions.size());
               }
               result.add(token);
            }
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("token for inbound not found from " + transition.getFromActivity());
            }
         }
      }
      if (result == null)
      {
         // extra try if the start token was created during relocation or
         // if the bound activity instance is a member of a multi instance activity
         TransitionTokenBean token = getBoundInToken(boundActivityInstance, ActivityThread.START_TRANSITION);
         if (token != null)
         {
            return Collections.singletonList(token);
         }
      }
      return result == null ? Collections.<TransitionTokenBean>emptyList() : result;
   }

   // should not be called if we are still in a chain
   public void flush()
   {
      localTokenCache.flush();
      secondLevelCache.flush();
      globalTokenCache.flush();
   }

   public TransitionTokenBean createToken(ITransition transition, IActivityInstance activityInstance)
   {
      TransitionTokenBean token = new TransitionTokenBean(processInstance, transition, activityInstance.getOID());
      localTokenCache.registerToken(transition, token);
      tokenChange++;
      return token;
   }

   public TransitionTokenBean createMultiInstanceToken(IActivityInstance activityInstance, int index)
   {
      TransitionTokenBean token = new TransitionTokenBean(processInstance, activityInstance.getOID(), index);
      localTokenCache.registerToken(null, token);
      tokenChange++;
      return token;
   }

   public void registerToken(ITransition transition, TransitionTokenBean token)
   {
      localTokenCache.registerToken(transition, token);
   }

   public void consumeToken(TransitionTokenBean token)
   {
      // TODO (ab) additional exception handling if all the caches did not consume!
      if (!localTokenCache.removeToken(token))
      {
         // in single-node scenario, rely on results from 2ndlevel cache
         if (!secondLevelCache.removeToken(token) && !this.secondLevelCache.hasCompleteInformation())
         {
            globalTokenCache.removeToken(token);
         }
      }
      token.setConsumed(true);
      tokenChange--;
   }

   public void updateInBindings(IActivityInstance oldBinding, IActivityInstance newBinding, IActivity activity)
   {
      List<TransitionTokenBean> tokens = getBoundInTokens(oldBinding, activity);
      registerPersistenceControllers(tokens);
      for (TransitionTokenBean token : tokens)
      {
         token.setTarget(newBinding);
      }
   }

   public List<TransitionTokenBean> getFreeOutTokens(List<ITransition> enabledOutTransitions)
   {
      List<TransitionTokenBean> result = null;

      for (ITransition transition : enabledOutTransitions)
      {
         TransitionTokenBean token = lockFreeToken(transition);
         if (token != null)
         {
            if (enabledOutTransitions.size() == 1)
            {
               result = Collections.singletonList(token);
            }
            else
            {
               if (result == null)
               {
                  result = CollectionUtils.newList(enabledOutTransitions.size());
               }
               result.add(token);
            }
         }
      }
      return result == null ? Collections.<TransitionTokenBean>emptyList() : result;
   }

   private TransitionTokenBean getBoundInToken(IActivityInstance boundActivityInstance,
         ITransition transition)
   {
      long boundActivityInstanceOID = boundActivityInstance.getOID();

      TransitionTokenBean token = localTokenCache.getTokenForTarget(transition, boundActivityInstanceOID);

      if (null == token)
      {
         token = secondLevelCache.getTokenForTarget(transition, boundActivityInstanceOID);
      }

      // in single-node scenario, rely on results from 2ndlevel cache
      if (null == token && !secondLevelCache.hasCompleteInformation())
      {
         token = globalTokenCache.getTokenForTarget(transition, boundActivityInstanceOID);
      }

      return token;
   }

   /**
    *
    *
    * @param sourceToken
    * @return An empty list if the source token could not be locked, or a list of locked tokens.
    *         If the returned list is not empty and the source token is not included in the list,
    *         it means that only some tokens were locked but not all of them.
    */
   public TransitionTokenBean lockSourceAndOtherToken(TransitionTokenBean sourceToken)
   {
      TransitionTokenBean token = localTokenCache.lockSourceAndOtherToken(sourceToken);

      if (token == null)
      {
         token = secondLevelCache.lockSourceAndOtherToken(sourceToken);
      }

      // in single-node scenario, rely on results from 2ndlevel cache
      if (token == null && !secondLevelCache.hasCompleteInformation())
      {
         token = globalTokenCache.lockSourceAndOtherToken(sourceToken);
      }

      return token;
   }

   public void unlockTokens(List tokens)
   {
      // TODO (ab) also unlock Local and Global?
      secondLevelCache.unlockTokens(tokens);
   }

   public void registerPersistenceControllers(List tokens)
   {
      secondLevelCache.registerPersistenceControllers(tokens);
   }

   public static enum TokenLocation
   {
      local, cache, global
   }

   public TokenLocation hasUnconsumedTokens(Set<ITransition> transitions)
   {
      if (localTokenCache.hasUnconsumedTokens(transitions))
      {
         return TokenLocation.local;
      }

      if (secondLevelCache.hasUnconsumedTokens(transitions))
      {
         return TokenLocation.cache;
      }

      // in single-node scenario, rely on results from 2ndlevel cache
      if (!secondLevelCache.hasCompleteInformation() && globalTokenCache.hasUnconsumedTokens(transitions))
      {
         return TokenLocation.global;
      }

      return null;
   }

   public static boolean containsUnconsumedToken(Collection<TransitionTokenBean> tokens)
   {
      if (tokens != null)
      {
         for (TransitionTokenBean token : tokens)
         {
            if (!token.isConsumed())
            {
               return true;
            }
         }
      }
      return false;
   }

   public long getTokenChange()
   {
      return tokenChange;
   }
}