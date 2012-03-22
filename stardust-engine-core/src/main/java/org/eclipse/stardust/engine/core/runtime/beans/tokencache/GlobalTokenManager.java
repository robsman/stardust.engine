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
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;


public class GlobalTokenManager implements ITokenManager
{
   private static final Logger trace = LogManager.getLogger(GlobalTokenManager.class);
   private final Map /*<Long,<Long,TransitionTokenBean>>*/ tokensByTransition = new HashMap();
   private final IProcessInstance processInstance;

   public GlobalTokenManager(IProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   private Object getTokenMap(ITransition transition)
   {
      Long transRtOid = getTransitionRtOid(transition);
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
   
   private Long getTransitionRtOid(ITransition transition)
   {
      if ((ActivityThread.START_TRANSITION == transition) || (null == transition))
      {
         return TransitionTokenBean.START_TRANSITION_RT_OID;
      }
      else
      {
         return new Long(ModelManagerFactory.getCurrent().getRuntimeOid(transition));
      }
   }

   private int getModelOid(ITransition transition)
   {
      if ((ActivityThread.START_TRANSITION == transition) || (null == transition))
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
      if (null == tokens)
      {
         this.tokensByTransition.put(getTransitionRtOid(transition), token);
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
      if (null == tokens || ((tokens instanceof List) && ((List) tokens).isEmpty()))
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
      if (null == tokens || ((tokens instanceof List) && ((List) tokens).isEmpty()))
      {
         return null;
      }

      if (tokens instanceof TransitionTokenBean)
      {
         TransitionTokenBean token = (TransitionTokenBean) tokens;
         if ( !token.isBound())
         {
            try
            {
               token.lock();
               if (trace.isDebugEnabled())
               {
                  trace.debug("token " + token + " locked.");
               }
               token.reload();
               if ( !token.isBound())
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
      else
      {
         List tokenList = (List) tokens;
         for (int i = 0; i < tokenList.size(); ++i)
         {
            TransitionTokenBean token = (TransitionTokenBean) tokenList.get(i);
            if ( !token.isBound())
            {
               try
               {
                  token.lock();
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("token " + token + " locked.");
                  }
                  token.reload();
                  if ( !token.isBound())
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
      
      this.tokensByTransition.clear();
   }

   public TransitionTokenBean getTokenForTarget(ITransition transition,
         long targetActivityInstanceOid)
   {
      Object tokens = getTokenMap(transition);
      if (null == tokens || ((tokens instanceof List) && ((List) tokens).isEmpty()))
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
         List tokenList = (List) tokens;
         for (int i = 0; i < tokenList.size(); ++i)
         {
            TransitionTokenBean token = (TransitionTokenBean) tokenList.get(i);
            if (token.getTarget() == targetActivityInstanceOid)
            {
               return token;
            }
         }
      }

      return null;
   }

}
