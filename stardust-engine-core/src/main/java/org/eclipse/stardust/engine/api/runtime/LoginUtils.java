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
package org.eclipse.stardust.engine.api.runtime;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;



public class LoginUtils
{
   /**
    * Overwrite loginProperties with existing credentials.
    *
    * @param loginProperties
    * @param credentials
    */
   public static void mergeCredentialProperties(Map loginProperties, Map credentials)
   {
      String partition = (String) credentials.get(SecurityProperties.CRED_PARTITION);
      if ( !StringUtils.isEmpty(partition))
      {
         loginProperties.put(SecurityProperties.PARTITION, partition);
      }

      String realm = (String) credentials.get(SecurityProperties.CRED_REALM);
      if ( !StringUtils.isEmpty(realm))
      {
         loginProperties.put(SecurityProperties.REALM, realm);
      }

      String domain = (String) credentials.get(SecurityProperties.CRED_DOMAIN);
      if ( !StringUtils.isEmpty(domain))
      {
         loginProperties.put(SecurityProperties.DOMAIN, domain);
      }
   }

   public static Map mergeDefaultCredentials(Map loginProperties)
   {
      return mergeDefaultCredentials(Parameters.instance(), loginProperties);
   }

   public static Map mergeDefaultCredentials(Parameters params, Map loginProperties)
   {
      String partition = (String) loginProperties.get(SecurityProperties.PARTITION);
      if (StringUtils.isEmpty(partition))
      {
         partition = params.getString(SecurityProperties.DEFAULT_PARTITION,
               PredefinedConstants.DEFAULT_PARTITION_ID);

         loginProperties.put(SecurityProperties.PARTITION, partition);
      }

      String realm = (String) loginProperties.get(SecurityProperties.REALM);
      if (StringUtils.isEmpty(realm))
      {
         realm = params.getString(SecurityProperties.DEFAULT_REALM,
               PredefinedConstants.DEFAULT_REALM_ID);

         loginProperties.put(SecurityProperties.REALM, realm);
      }

      String domain = (String) loginProperties.get(SecurityProperties.DOMAIN);
      if (StringUtils.isEmpty(domain))
      {
         domain = params.getString(SecurityProperties.DEFAULT_DOMAIN, partition);

         loginProperties.put(SecurityProperties.DOMAIN, domain);
      }

      return loginProperties;
   }

   public static String getPartitionId(Map properties)
   {
      String partitionId = (String) properties.get(SecurityProperties.PARTITION);
      if (StringUtils.isEmpty(partitionId))
      {
         throw new LoginFailedException(
               BpmRuntimeError.AUTHx_AUTH_PARTITION_NOT_SPECIFIED.raise(),
               LoginFailedException.UNKNOWN_PARTITION);
      }

      return partitionId;
   }

   public static String getUserDomainId(Map properties)
   {
      String domainId = (String) properties.get(SecurityProperties.DOMAIN);
      if (StringUtils.isEmpty(domainId))
      {
         throw new LoginFailedException(
               BpmRuntimeError.AUTHx_AUTH_DOMAIN_NOT_SPECIFIED.raise(),
               LoginFailedException.UNKNOWN_DOMAIN);
      }

      return domainId;
   }

   public static String getUserRealmId(Map properties)
   {
      String realmId = (String) properties.get(SecurityProperties.REALM);
      if (StringUtils.isEmpty(realmId))
      {
         throw new LoginFailedException(
               BpmRuntimeError.AUTHx_AUTH_REALM_NOT_SPECIFIED.raise(),
               LoginFailedException.UNKNOWN_REALM);
      }

      return realmId;
   }

   public static IAuditTrailPartition findPartition(Parameters params, Map properties)
   {
      String partitionId = getPartitionId(properties);
      return findPartition(params, partitionId);
   }

   public static IAuditTrailPartition findPartition(Parameters params, String partitionId)
   {
      try
      {
         boolean cachePartitions = params.getBoolean(
               KernelTweakingProperties.CACHE_PARTITIONS, true);
         Map partitionCache = cachePartitions
               ? (Map) params.get(CachedAuditTrailPartitionBean.PRP_PARTITION_CACHE)
               : null;

         IAuditTrailPartition partition = null;
         if (cachePartitions)
         {
            if (null != partitionCache)
            {
               partition = (IAuditTrailPartition) partitionCache.get(partitionId);
            }
         }

         if (null == partition)
         {
            partition = AuditTrailPartitionBean.findById(partitionId);
            if (cachePartitions)
            {
               if (null == partitionCache)
               {
                  partitionCache = new HashMap();
                  params.set(CachedAuditTrailPartitionBean.PRP_PARTITION_CACHE,
                        partitionCache);
               }

               partition = new CachedAuditTrailPartitionBean(partition);
               partitionCache.put(partitionId, partition);
            }
         }

         return partition;
      }
      catch (ObjectNotFoundException e)
      {
         throw new LoginFailedException(e.getError(),
               LoginFailedException.UNKNOWN_PARTITION);
      }
   }

   public static IAuditTrailPartition findPartition(Parameters params, short partitionOid)
   {
      try
      {
         boolean cachePartitions = params.getBoolean(
               KernelTweakingProperties.CACHE_PARTITIONS, true);
         Map partitionCache = cachePartitions
               ? (Map) params.get(CachedAuditTrailPartitionBean.PRP_PARTITION_CACHE)
               : null;

         IAuditTrailPartition partition = null;
         if (cachePartitions)
         {
            if (null != partitionCache)
            {
               for (Iterator i = partitionCache.values().iterator(); i.hasNext();)
               {
                  IAuditTrailPartition candidate = (IAuditTrailPartition) i.next();
                  if (candidate.getOID() == partitionOid)
                  {
                     partition = candidate;
                     break;
                  }
               }
            }
         }

         if (null == partition)
         {
            partition = AuditTrailPartitionBean.findByOID(partitionOid);
            if (cachePartitions)
            {
               if (null == partitionCache)
               {
                  partitionCache = new HashMap();
                  params.set(CachedAuditTrailPartitionBean.PRP_PARTITION_CACHE,
                        partitionCache);
               }

               partition = new CachedAuditTrailPartitionBean(partition);
               partitionCache.put(partition.getId(), partition);
            }
         }

         return partition;
      }
      catch (ObjectNotFoundException e)
      {
         throw new LoginFailedException(e.getError(),
               LoginFailedException.UNKNOWN_PARTITION);
      }
   }

   /**
    *
    * @param properties
    * @return
    */
   public static IUserDomain findUserDomain(Parameters params,
         IAuditTrailPartition partition, Map properties)
   {
      String domainId = getUserDomainId(properties);
      return findUserDomain(params, partition, domainId);
   }

   public static IUserDomain findUserDomain(Parameters params,
         IAuditTrailPartition partition, String domainId)
   {
      IUserDomain domain = null;

      try
      {
         boolean cacheDomains = params.getBoolean(KernelTweakingProperties.CACHE_DOMAINS,
               true);

         if (cacheDomains && (partition instanceof CachedAuditTrailPartitionBean))
         {
            domain = ((CachedAuditTrailPartitionBean) partition).findCachedDomain(domainId);
         }

         if (null == domain)
         {
            domain = UserDomainBean.findById(domainId, partition.getOID());

            if ((null != domain) && cacheDomains
                  && (partition instanceof CachedAuditTrailPartitionBean))
            {
               ((CachedAuditTrailPartitionBean) partition).cachedDomain(domain);
            }
         }
         return domain;
      }
      catch (ObjectNotFoundException e)
      {
         throw new LoginFailedException(e.getError(),
               LoginFailedException.UNKNOWN_DOMAIN);
      }
   }

   public static IUserDomain findUserDomain(Parameters params,
         IAuditTrailPartition partition, long domainOid)
   {
      IUserDomain domain = null;

      try
      {
         boolean cacheDomains = params.getBoolean(KernelTweakingProperties.CACHE_DOMAINS,
               true);

         if (cacheDomains && (partition instanceof CachedAuditTrailPartitionBean))
         {
            domain = ((CachedAuditTrailPartitionBean) partition).findCachedDomain(domainOid);
         }

         if (null == domain)
         {
            domain = UserDomainBean.findByOID(domainOid);

            if ((null != domain) && cacheDomains
                  && (partition instanceof CachedAuditTrailPartitionBean))
            {
               ((CachedAuditTrailPartitionBean) partition).cachedDomain(domain);
            }
         }
         return domain;
      }
      catch (ObjectNotFoundException e)
      {
         throw new LoginFailedException(e.getError(),
               LoginFailedException.UNKNOWN_DOMAIN);
      }
   }

   public static IUserRealm findUserRealm(Map properties)
   {
      return findUserRealm(Parameters.instance(), properties);
   }

   public static IUserRealm findUserRealm(Parameters params, Map properties)
   {
      String realmId = getUserRealmId(properties);
      IAuditTrailPartition partition = findPartition(params, properties);
      try
      {
         return UserRealmBean.findById(realmId, partition.getOID());
      }
      catch (ObjectNotFoundException e)
      {
         throw new LoginFailedException(e.getError(), LoginFailedException.UNKNOWN_REALM);
      }
   }

   /**
    * @param account
    * @param properties
    * @return
    */
   public static IUser findLoginUser(String account, Map properties)
   {
      return UserBean.findByAccount(account, findUserRealm(properties));
   }

   public static boolean isLoginUserWithoutTimestamp(IUser user)
   {
      String userIdSpec = Parameters.instance().getString(
            SecurityProperties.LOGIN_USERS_WITHOUT_TIMESTAMP, "").trim();

      return UserUtils.isUserMatchingIdSpec(user, userIdSpec);
   }

   public static boolean isLoginLoggingDisabled(IUser user)
   {
      String userIdSpec = Parameters.instance().getString(
            SecurityProperties.LOGIN_USERS_WITHOUT_LOGIN_LOGGING, "").trim();

      return UserUtils.isUserMatchingIdSpec(user, userIdSpec);
   }

   public static boolean isUserExpired(IUser user)
   {
      Date now = new Date();

      if ( ((user.getValidFrom() == null || user.getValidFrom().before(now)) && //
            (user.getValidTo() == null || user.getValidTo().after(now))))
      {
         return false;
      }

      return true;
   }

   public static LoginFailedException createAccountExpiredException(IUser user)
   {
      return new LoginFailedException(BpmRuntimeError.AUTHx_EXP_ACCOUNT_EXPIRED.raise(user
            .getRealmQualifiedAccount()), LoginFailedException.ACCOUNT_EXPIRED);
   }

   /**
    * Merges the re-authentication properties of the outer {@link InvokerPrincipal} to the
    * inner {@link InvokerPrincipal}.
    *
    * @param outer
    *           Can contain property {@link AbstractLoginInterceptor#REAUTH_USER_ID}
    * @param inner
    *           The inner principal exists after the first call and is reused.
    * @return If the outer principal contains re-authentication properties a new principal
    *         based on the inner principal with re-authentication properties of the outer
    *         principal, else the inner principal.
    */
   public static InvokerPrincipal getReauthenticationPrincipal(InvokerPrincipal outer,
         InvokerPrincipal inner)
   {
      if (outer != null && inner != null)
      {
         Map outerProperties = outer.getProperties();
         if (outerProperties != null && outerProperties.containsKey(AbstractLoginInterceptor.REAUTH_USER_ID))
         {
            Map mergedProperties = new HashMap(inner.getProperties());
            mergedProperties.put(AbstractLoginInterceptor.REAUTH_USER_ID, outerProperties.get(AbstractLoginInterceptor.REAUTH_USER_ID));
            mergedProperties.put(AbstractLoginInterceptor.REAUTH_PASSWORD,
                  outerProperties.get(AbstractLoginInterceptor.REAUTH_PASSWORD));

            return new InvokerPrincipal(inner.getName(), mergedProperties,
                  inner.getSignature());
         }
      }
      return inner;
   }

}
