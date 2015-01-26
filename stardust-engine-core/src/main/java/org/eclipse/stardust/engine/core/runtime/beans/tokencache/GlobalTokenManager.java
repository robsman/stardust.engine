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

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;

public class GlobalTokenManager implements ITokenManager
{
   private final Map<Long, Object> tokensByTransition = new HashMap();
   private final IProcessInstance processInstance;

   public GlobalTokenManager(IProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   private Object getTokenMap(ITransition transition)
   {
      long transRtOid = getTransitionRtOid(transition);
      int transModelOid = getModelOid(transition);

      Object tokens = tokensByTransition.get(transRtOid);
      if (tokens == null || tokens instanceof List && ((List) tokens).isEmpty())
      {
         tokens = TransitionTokenBean.findUnconsumedForTransition(processInstance,
               transRtOid, transModelOid);

         // TODO (ab) is this needed? this could be needed for DB2: there the committed lines are readable
         // for other transactions before commit - duplicate tokens (local an global cache can be created this way?)
         // but on the other hand: local tokens are only written to DB in flush(), so it could not happen?
         //         Set localTokens = this.localTokenCache.getAllTokens(transition);
         //         for (Iterator i = localTokens.iterator(); i.hasNext();)
         //         {
         //            final TransitionTokenBean localToken = (TransitionTokenBean) i.next();
         //            for (Iterator j = globalTokens.iterator(); j.hasNext();)
         //            {
         //               if (((TransitionTokenBean) j.next()).getOID() == localToken.getOID())
         //               {
         //                  j.remove();
         //                  break;
         //               }
         //            }
         //         }

         if (tokens != null)
         {
            tokensByTransition.put(transRtOid, tokens);
         }
      }

      return tokens;
   }

   private long getTransitionRtOid(ITransition transition)
   {
      if (ActivityThread.START_TRANSITION == transition || transition == null)
      {
         return TransitionTokenBean.START_TRANSITION_RT_OID;
      }
      else
      {
         return ModelManagerFactory.getCurrent().getRuntimeOid(transition);
      }
   }

   private int getModelOid(ITransition transition)
   {
      if (ActivityThread.START_TRANSITION == transition || transition == null)
      {
         return TransitionTokenBean.START_TRANSITION_MODEL_OID.intValue();
      }
      else
      {
         return transition.getModel().getModelOID();
      }
   }

   public void registerToken(ITransition transition, TransitionTokenBean token)
   {
      Object tokens = getTokenMap(transition);
      if (tokens == null)
      {
         tokensByTransition.put(getTransitionRtOid(transition), token);
      }
      else if (tokens instanceof TransitionTokenBean)
      {
         TransitionTokenBean existingToken = (TransitionTokenBean) tokens;
         tokens = CollectionUtils.newArrayList();
         ((List) tokens).add(existingToken);
         ((List) tokens).add(token);
      }
      else
      {
         ((List) tokens).add(token);
      }
   }

   public boolean removeToken(TransitionTokenBean token)
   {
      Object tokens = getTokenMap(token.getTransition());
      if (tokens == null || (tokens instanceof List && ((List) tokens).isEmpty()))
      {
         return false;
      }

      if (tokens instanceof TransitionTokenBean)
      {
         if (((TransitionTokenBean) tokens).getOID() == token.getOID())
         {
            tokensByTransition.remove(getTransitionRtOid(token.getTransition()));
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return ((List) tokens).remove(token);
      }
   }

   public TransitionTokenBean lockFirstAvailableToken(ITransition transition)
   {
      Object tokens = getTokenMap(transition);
      if (tokens == null || (tokens instanceof List && ((List) tokens).isEmpty()))
      {
         return null;
      }

      List<TransitionTokenBean> tokenList = tokens instanceof TransitionTokenBean
            ? Collections.singletonList((TransitionTokenBean) tokens)
            : (List<TransitionTokenBean>) tokens;
      for (TransitionTokenBean token : tokenList)
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
      final Object startTokens = tokensByTransition.get(TransitionTokenBean.START_TRANSITION_RT_OID);

      if (startTokens instanceof TransitionTokenBean)
      {
         TransitionTokenBean token = (TransitionTokenBean) startTokens;
         if (token.isStartToken() && token.isConsumed())
         {
            token.delete();
         }
      }
      else
      {
         List startTokenList = (List) startTokens;
         if ((null != startTokenList) && !startTokenList.isEmpty())
         {
            for (int i = 0; i < startTokenList.size(); ++i)
            {
               TransitionTokenBean token = (TransitionTokenBean) startTokenList.get(i);
               if (token.isStartToken() && token.isConsumed())
               {
                  token.delete();
               }
            }
         }
      }

      tokensByTransition.clear();
   }

   public TransitionTokenBean getTokenForTarget(ITransition transition,
         long targetActivityInstanceOid)
   {
      Object tokens = getTokenMap(transition);
      if (tokens == null || (tokens instanceof List && ((List) tokens).isEmpty()))
      {
         return null;
      }

      if (tokens instanceof TransitionTokenBean)
      {
         if (((TransitionTokenBean) tokens).getTarget() == targetActivityInstanceOid)
         {
            return (TransitionTokenBean) tokens;
         }
      }
      else
      {
         for (TransitionTokenBean token :  (List<TransitionTokenBean>) tokens)
         {
            if (token.getTarget() == targetActivityInstanceOid)
            {
               return token;
            }
         }
      }

      return null;
   }

   @Override
   public TransitionTokenBean lockSourceAndOtherToken(final TransitionTokenBean sourceToken)
   {
      Object tokens = getTokenMap(null);

      List<TransitionTokenBean> tokenList = tokens instanceof TransitionTokenBean
            ? Collections.singletonList((TransitionTokenBean) tokens)
            : tokens instanceof List ? (List<TransitionTokenBean>) tokens : Collections.<TransitionTokenBean>emptyList();

      if (tokenList.isEmpty() || !tokenList.contains(sourceToken) || sourceToken.lockAndReload() > 0)
      {
         return null;
      }

      return lockNextToken(sourceToken, filter(sourceToken, tokenList));
   }

   @Override
   public boolean hasUnconsumedTokens(Set<ITransition> transitions)
   {
      for (ITransition transition : transitions)
      {
         Object tokens = getTokenMap(transition);
         if (tokens != null)
         {
            if (tokens instanceof TransitionTokenBean)
            {
               if (!((TransitionTokenBean) tokens).isConsumed())
               {
                  return true;
               }
            }
            else if (tokens instanceof List)
            {
               if (TokenCache.containsUnconsumedToken((List) tokens))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   static TransitionTokenBean[] filter(final TransitionTokenBean sourceToken,
         Collection<TransitionTokenBean> tokenList)
   {
      TransitionTokenBean[] filtered = new TransitionTokenBean[8];
      long source = sourceToken.getSource();
      long pi = sourceToken.getProcessInstanceOID();
      for (TransitionTokenBean token : tokenList)
      {
         if (token != sourceToken && token.getSource() == source && token.getProcessInstanceOID() == pi)
         {
            int index = token.getMultiInstanceIndex();
            if (filtered.length <= index)
            {
               filtered = Arrays.copyOf(filtered, index + 8);
            }
            filtered[index] = token;
         }
      }
      return filtered;
   }

   static TransitionTokenBean lockNextToken(final TransitionTokenBean sourceToken,
         TransitionTokenBean[] tokens)
   {
      boolean allLocked = true;
      for (TransitionTokenBean token : tokens)
      {
         if (token != null)
         {
            int lockStatus = token.lockAndReload();
            if (lockStatus == 0)
            {
               if (!token.isConsumed())
               {
                  return token;
               }
            }
            else if (lockStatus == 1)
            {
               allLocked = false;
            }
         }
      }
      return allLocked ? sourceToken : null;
   }
}
