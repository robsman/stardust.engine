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

import java.util.*;
import java.util.Map.Entry;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;

public class SecondLevelTokenManager implements ISecondLevelTokenCache
{
   // TODO (ab) review syncronization. currently, not synchronized collections
   // are used and all public methods are synchronized

   private static final Logger trace = LogManager.getLogger(SecondLevelTokenManager.class);

   private final Map<Long, Map<Long,TransitionTokenBean>> tokensByTransitionRtOid = new HashMap();
   private final Map<Object, Set<Long>> lockedTokensOidsByTransaction = new HashMap();
   private final Set<Long> lockedTokensOids = new HashSet();
   private final boolean singleNodeDeployment;
   private boolean seenStartToken = false;
   // these are only for counting purposes (getUnconsumedTokenCount()), they are otherwise invisible from here
   private final Map<Object, Set<TransitionTokenBean>> localCacheTokensByTransaction = new HashMap();

   public SecondLevelTokenManager()
   {
      this.singleNodeDeployment = Parameters.instance().getBoolean(KernelTweakingProperties.SINGLE_NODE_DEPLOYMENT, false);
   }

   private Map<Long, TransitionTokenBean> getTokenMap(ITransition transition)
   {
      return tokensByTransitionRtOid.get(getTransitionRtOid(transition));
   }

   private long getTransitionRtOid(ITransition transition)
   {
      if ((ActivityThread.START_TRANSITION == transition) || (null == transition))
      {
         return TransitionTokenBean.START_TRANSITION_RT_OID;
      }
      else
      {
         return ModelManagerFactory.getCurrent().getRuntimeOid(transition);
      }
   }

   public synchronized void addLocalToken(TransitionTokenBean token)
   {
      Object transaction = TransactionUtils.getCurrentTxStatus().getTransaction();
      Set<TransitionTokenBean> tokens = localCacheTokensByTransaction.get(transaction);

      if (tokens == null)
      {
         tokens = new HashSet();
         localCacheTokensByTransaction.put(transaction, tokens);
      }
      tokens.add(token);
   }

   private synchronized void removeLocalToken(TransitionTokenBean token)
   {
      Object transaction = TransactionUtils.getCurrentTxStatus().getTransaction();
      Set<TransitionTokenBean> tokens = localCacheTokensByTransaction.get(transaction);
      if (tokens != null)
      {
         boolean removed = tokens.remove(token);
         if (trace.isDebugEnabled())
         {
            if (removed)
            {
               trace.debug("removed token from local");
            }
            else
            {
               trace.debug("did not remove token from local");
            }
         }
      }
   }

   public synchronized void registerToken(ITransition transition, TransitionTokenBean token)
   {
      if (ActivityThread.START_TRANSITION.equals(transition))
      {
         seenStartToken = true;
      }

      Map<Long,TransitionTokenBean> tokensForTransition = getTokenMap(transition);
      if (tokensForTransition == null)
      {
         tokensForTransition = new HashMap();
         tokensByTransitionRtOid.put(getTransitionRtOid(transition), tokensForTransition);
      }
      Long tokenOid = token.getOID();
      tokensForTransition.put(tokenOid, token);
      // detach old persistence controller since the token is flushed to DB
      // TODO (ab) not sure if this is needed
      token.setPersistenceController(null);
      lockInMemory(tokenOid);

      // keep localCacheTokensByTransaction in sync
      removeLocalToken(token);
   }

   public synchronized boolean removeToken(TransitionTokenBean token)
   {
      Map<Long,TransitionTokenBean> tokensForTransition = getTokenMap(token.getTransition());
      if (tokensForTransition == null || tokensForTransition.size() == 0)
      {
         return false;
      }
      Long tokenOid = token.getOID();
      if (tokensForTransition.remove(tokenOid) == null)
      {
         return false;
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Removed token " + tokenOid + " (consumed)");
         }
         registerPersistenceController(tokenOid, token);
         return true;
      }
   }

   public synchronized TransitionTokenBean lockFirstAvailableToken(ITransition transition)
   {
      Map<Long,TransitionTokenBean> tokensForTransition = getTokenMap(transition);
      if (tokensForTransition == null || tokensForTransition.size() == 0)
      {
         return null;
      }

      for (Entry<Long, TransitionTokenBean> entry : tokensForTransition.entrySet())
      {
         Long tokenOid = entry.getKey();
         TransitionTokenBean token = entry.getValue();

         if (!isLockedInMemory(tokenOid))
         {
            if (singleNodeDeployment)
            {
               lockInMemory(tokenOid);
               return token;
            }
            else
            {
               // TODO (ab) lockInDb does not work
               //registerPersistenceController(tokenOid, token);
               if (!token.isBound())
               {
                  if (token.lockAndReload() == 0 && !token.isBound())
                  {
                     lockInMemory(tokenOid);
                     return token;
                  }
               }
            }
         }
      }
      return null;
   }

   private synchronized boolean isLockedInMemory(Long tokenOid)
   {
      if (lockedTokensOids.contains(tokenOid))
      {
         Object transaction = TransactionUtils.getCurrentTxStatus().getTransaction();
         Set<Long> lockedForCurrentTransaction = lockedTokensOidsByTransaction.get(transaction);
         if (lockedForCurrentTransaction == null ||
               lockedForCurrentTransaction != null && !lockedForCurrentTransaction.contains(tokenOid))
         {
            // locked by another transaction
            return true;
         }
      }
      return false;
   }

   private synchronized void lockInMemory(Long tokenOid)
   {
      lockedTokensOids.add(tokenOid);
      if (trace.isDebugEnabled())
      {
         trace.debug("Locked token " + tokenOid + " in memory");
      }
      Object transaction = TransactionUtils.getCurrentTxStatus().getTransaction();
      Set<Long> lockedForCurrentTransaction = lockedTokensOidsByTransaction.get(transaction);
      if (lockedForCurrentTransaction == null)
      {
         lockedForCurrentTransaction = new HashSet();
         lockedTokensOidsByTransaction.put(transaction, lockedForCurrentTransaction);
      }
      lockedForCurrentTransaction.add(tokenOid);
   }

   private synchronized void registerPersistenceController(Long tokenOid, TransitionTokenBean token)
   {
      Session session = (Session)SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (!token.isPersistent() || (token.isPersistent() && token.getPersistenceController().getSession() != session))
      {
         DmlManager dmlManager = session.getDMLManager(token.getClass());
         session.addToPersistenceControllers(tokenOid, dmlManager.createPersistenceController(session, token));
         if (trace.isDebugEnabled())
         {
            trace.debug("registered persistence controller for tokenOid: "+tokenOid);
         }
      }
   }

   public synchronized void flush()
   {
      // do nothing, unlocking is done via unlockForTransaction
   }

   public synchronized TransitionTokenBean getTokenForTarget(ITransition transition,
         long targetActivityInstanceOid)
   {
      // TODO (ab) give a read-only version of token from here!
      Map<Long, TransitionTokenBean> tokensForTransition = getTokenMap(transition);
      if (tokensForTransition == null || tokensForTransition.size() == 0)
      {
         return null;
      }

      for (Entry<Long, TransitionTokenBean> entry : tokensForTransition.entrySet())
      {
         Long tokenOid = entry.getKey();
         TransitionTokenBean token = entry.getValue();
         if (token.getTarget() == targetActivityInstanceOid && !isLockedInMemory(tokenOid))
         {
            lockInMemory(tokenOid);
            return token;
         }
      }
      return null;
   }

   @Override
   public synchronized TransitionTokenBean lockSourceAndOtherToken(final TransitionTokenBean sourceToken)
   {
      Map<Long, TransitionTokenBean> tokenList = getTokenMap(null);
      if (tokenList == null || tokenList.size() == 0
            || !tokenList.containsKey(sourceToken.getOID()) || sourceToken.lockAndReload() > 0)
      {
         return null;
      }

      return lockNextToken(sourceToken, filter(sourceToken, tokenList.values()));
   }

   private TransitionTokenBean lockNextToken(final TransitionTokenBean sourceToken,
         TransitionTokenBean[] tokens)
   {
      boolean allLocked = true;
      for (TransitionTokenBean token : tokens)
      {
         Long tokenOid = token.getOID();
         if (!isLockedInMemory(tokenOid))
         {
            if (singleNodeDeployment)
            {
               lockInMemory(tokenOid);
               if (!token.isConsumed())
               {
                  return token;
               }
            }
            else
            {
               int lockStatus = token.lockAndReload();
               if (lockStatus == 0)
               {
                  lockInMemory(tokenOid);
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
         else
         {
            allLocked = false;
         }
      }
      return allLocked ? sourceToken : null;
   }

   public boolean hasCompleteInformation()
   {
      return singleNodeDeployment && seenStartToken;
   }

   public synchronized void unlockForTransaction(Object transaction)
   {
      Set<Long> lockedForTransaction = lockedTokensOidsByTransaction.remove(transaction);
      if (lockedForTransaction != null)
      {
         lockedTokensOids.removeAll(lockedForTransaction);
      }

      // keep localCacheTokensByTransaction in sync
      localCacheTokensByTransaction.remove(transaction);
   }

   public synchronized void unlockTokens(List tokens)
   {
      Object transaction = TransactionUtils.getCurrentTxStatus().getTransaction();
      for (TransitionTokenBean token : (List<TransitionTokenBean>) tokens)
      {
         Long tokenOid = token.getOID();
         if (lockedTokensOids.remove(tokenOid))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Unlocked token "+tokenOid+" again because not all tokens are there to execute the activity");
            }
            Set<Long> lockedForCurrentTransaction = lockedTokensOidsByTransaction.get(transaction);
            if (lockedForCurrentTransaction != null)
            {
               lockedForCurrentTransaction.remove(tokenOid);
            }
         }
      }
   }

   public synchronized void registerPersistenceControllers(List tokens)
   {
      for (TransitionTokenBean token : (List<TransitionTokenBean>) tokens)
      {
         Long tokenOid = token.getOID();
         if (lockedTokensOids.contains(tokenOid))
         {
            registerPersistenceController(tokenOid, token);
         }
      }
   }

   public void registerTransaction(Object transaction)
   {
      // do nothing, currently only supported for Spring
   }

   public synchronized long getUnconsumedTokenCount()
   {
      long unconsumedTokenCount = 0;

      // count all unconsumed tokens in local caches of _other_ transactions
      Object currentTransaction = TransactionUtils.getCurrentTxStatus().getTransaction();
      for (Entry<?, Set<TransitionTokenBean>> entry : localCacheTokensByTransaction.entrySet())
      {
         Set<TransitionTokenBean> tokens = entry.getValue();

         if (currentTransaction != entry.getKey())
         {
            unconsumedTokenCount += tokens.size();
         }
      }

      for (Map<Long,TransitionTokenBean> tokenMap : tokensByTransitionRtOid.values())
      {
         unconsumedTokenCount += tokenMap.size();
      }
      return unconsumedTokenCount;
   }
}
