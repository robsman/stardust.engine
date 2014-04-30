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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

public class TokenManagerRegistry
{
   /**
    * Property key to be used for storage in Parameters
    */
   private static String TOKEN_MANAGER_REGISTRY = "TokenManagerRegistry";

   private final ConcurrentHashMap<Long, ISecondLevelTokenCache> cacheByProcessInstanceOid = new ConcurrentHashMap();

   private final ConcurrentHashMap<Object, Set<Long>> processInstanceOidsByTransaction = new ConcurrentHashMap();

   public static TokenManagerRegistry instance()
   {
      GlobalParameters globals = GlobalParameters.globals();

      TokenManagerRegistry tokenManagerRegistry = (TokenManagerRegistry) globals.get(TOKEN_MANAGER_REGISTRY);
      if (tokenManagerRegistry == null)
      {
         tokenManagerRegistry = (TokenManagerRegistry) globals.getOrInitialize(
               TOKEN_MANAGER_REGISTRY, new ValueProvider()
               {
                  public Object getValue()
                  {
                     return new TokenManagerRegistry();
                  }
               });
      }

      return tokenManagerRegistry;
   }

   public ISecondLevelTokenCache getSecondLevelCache(IProcessInstance processInstance)
   {
      ISecondLevelTokenCache result = null;

      if (!cacheByProcessInstanceOid.isEmpty())
      {
         long processInstanceOid = processInstance.getOID();

         result = (ISecondLevelTokenCache) cacheByProcessInstanceOid.get(processInstanceOid);

         if (result != null)
         {
            registerTransaction(processInstanceOid, result);
         }
      }

      return result == null ? SecondLevelCacheFactory.NULL_TOKEN_MANAGER : result;
   }

   public ISecondLevelTokenCache createSecondLevelCache(IProcessInstance processInstance)
   {
      ISecondLevelTokenCache secondLevelCache = SecondLevelCacheFactory.createSecondLevelCache();
      if (SecondLevelCacheFactory.NULL_TOKEN_MANAGER != secondLevelCache)
      {
         long processInstanceOid = processInstance.getOID();

         if (null != cacheByProcessInstanceOid.putIfAbsent(processInstanceOid, secondLevelCache))
         {
            Assert.condition( false, "Token cache already exist for this process instance");
         }

         registerTransaction(processInstanceOid, secondLevelCache);
      }

      return secondLevelCache;
   }

   private void registerTransaction(Long processInstanceOid, ISecondLevelTokenCache secondLevelCache)
   {
      Object transaction = TransactionUtils.getCurrentTxStatus().getTransaction();
      if (transaction == null)
      {
         // no access to transaction object, no register
         return;
      }

      Set<Long> processInstanceOids = processInstanceOidsByTransaction.get(transaction);
      if (processInstanceOids == null)
      {
         Set newSet = CollectionUtils.newSet();
         processInstanceOids = processInstanceOidsByTransaction.putIfAbsent(transaction, newSet);
         if (newSet != processInstanceOids)
         {
            // nothing there for this transaction, it must be registered first
            secondLevelCache.registerTransaction(transaction);
         }
      }

      processInstanceOids.add(processInstanceOid);
   }

   public void removeSecondLevelCache(IProcessInstance processInstance)
   {
      if (!cacheByProcessInstanceOid.isEmpty())
      {
         cacheByProcessInstanceOid.remove(processInstance.getOID());
      }
   }

   public void unlockTokensForTransaction(Object transaction)
   {
      Set<Long> processInstanceOids = processInstanceOidsByTransaction.remove(transaction);
      if (processInstanceOids != null)
      {
         for (long processInstanceOid : processInstanceOids)
         {
            ISecondLevelTokenCache secondLevelTokenManager = cacheByProcessInstanceOid.get(processInstanceOid);
            if (secondLevelTokenManager != null)
            {
               secondLevelTokenManager.unlockForTransaction(transaction);
            }
         }
      }
   }

   // TODO (ab) LRU of SecondLevelTokenManager with a limited size (~/workspace/ccbutil/src/main/java/com/ser/ccb/util/cache/FixedSizeCacheMap.java)
   // TODO (ab) when LRU is implemented, differentiate between "there is a 2ndlevelCache" and "it was just recreated because the old one was removed"
   // TODO (ab) why on XOR, the search of tokens continue, even if a IN or OUT token has already been found? - results in selects
   // TODO (ab) (recovery) -> transfer global -> 2nd level
   // TODO (ab) test recovery
   // TODO (ab) do we need a lock and reload attribute on PI in single node mode?
   // TODO (ab) test loops
   // TODO (ab) document classes in tockencache package
}
