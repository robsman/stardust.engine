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

import java.util.Collections;
import java.util.List;

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

      if (null == lockedToken)
      {
         lockedToken = this.secondLevelCache.lockFirstAvailableToken(transition);
      }
      // in single-node scenario, rely on results from 2ndlevel cache
      if (null == lockedToken && !this.secondLevelCache.hasCompleteInformation())
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
         if (null != token)
         {
            return Collections.singletonList(token);
         }
      }
      if (activity.getId().equals(processInstance.getProcessDefinition().getRootActivity().getId()))
      {
         TransitionTokenBean token = getBoundInToken(boundActivityInstance, ActivityThread.START_TRANSITION);
         if (null != token)
         {
            return Collections.singletonList(token);
         }
      }
      ModelElementList inTransitions = boundActivityInstance.getActivity().getInTransitions();
      for (int i = 0; i < inTransitions.size(); ++i)
      {
         ITransition transition = (ITransition) inTransitions.get(i);

         TransitionTokenBean token = getBoundInToken(boundActivityInstance, transition);
         if (null != token)
         {
            if (1 == inTransitions.size())
            {
               return Collections.singletonList(token);
            }
            else
            {
               if (null == result)
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
         // extra try if the start token was created during relocation
         TransitionTokenBean token = getBoundInToken(boundActivityInstance, ActivityThread.START_TRANSITION);
         if (null != token)
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
      TransitionTokenBean token = new TransitionTokenBean(processInstance, transition,
            activityInstance.getOID());

      this.localTokenCache.registerToken(transition, token);
      
      return token;
   }
   
   public void registerToken(ITransition transition, TransitionTokenBean token)
   {
      this.localTokenCache.registerToken(transition, token);
   }

   public void consumeToken(TransitionTokenBean token)
   {
      // TODO (ab) additional exception handling if all the caches did not consume!
      if ( !this.localTokenCache.removeToken(token))
      {
         // in single-node scenario, rely on results from 2ndlevel cache
         if ( !this.secondLevelCache.removeToken(token) && !this.secondLevelCache.hasCompleteInformation())
         {
            this.globalTokenCache.removeToken(token);
         }
      }
      token.setConsumed(true);
   }

   public void updateInBindings(IActivityInstance oldBinding, IActivityInstance newBinding, IActivity activity)
   {
      List tokens = getBoundInTokens(oldBinding, activity);
      this.registerPersistenceControllers(tokens);
      for (int i = 0; i < tokens.size(); ++i)
      {
         TransitionTokenBean token = (TransitionTokenBean) tokens.get(i);
         token.setTarget(newBinding);
      }
   }

   public List<TransitionTokenBean> getFreeOutTokens(List<ITransition> enabledOutTransitions)
   {
      List<TransitionTokenBean> result = null;

      for (int i = 0; i < enabledOutTransitions.size(); ++i)
      {
         ITransition transition = (ITransition) enabledOutTransitions.get(i);
         TransitionTokenBean token = lockFreeToken(transition);
         if (token != null)
         {
            if (1 == enabledOutTransitions.size())
            {
               result = Collections.singletonList(token);
            }
            else
            {
               if (null == result)
               {
                  result = CollectionUtils.newList(enabledOutTransitions.size());
               }
               result.add(token);
            }
         }
      }
      if(result == null)
      {
         return Collections.emptyList();
      }
      return result;
   }

   private TransitionTokenBean getBoundInToken(IActivityInstance boundActivityInstance,
         ITransition transition)
   {
      TransitionTokenBean token = this.localTokenCache.getTokenForTarget(transition, boundActivityInstance.getOID());

      if (null == token)
      {
         token = this.secondLevelCache.getTokenForTarget(transition, boundActivityInstance.getOID());
      }
      
      // in single-node scenario, rely on results from 2ndlevel cache
      if (null == token && !this.secondLevelCache.hasCompleteInformation())
      {
         token = this.globalTokenCache.getTokenForTarget(transition, boundActivityInstance.getOID());
      }

      return token;
   }

   public void unlockTokens(List tokens)
   {
      // TODO (ab) also unlock Local and Global?
      this.secondLevelCache.unlockTokens(tokens);
   }

   public void registerPersistenceControllers(List tokens)
   {
      this.secondLevelCache.registerPersistenceControllers(tokens);
   }

}