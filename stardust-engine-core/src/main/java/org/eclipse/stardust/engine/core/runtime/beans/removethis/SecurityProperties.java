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
package org.eclipse.stardust.engine.core.runtime.beans.removethis;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.annotations.ConfigurationProperty;
import org.eclipse.stardust.common.annotations.PropertyValueType;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserDomain;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationProvider;
import org.eclipse.stardust.engine.core.spi.security.DynamicParticipantSynchronizationStrategy;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginProvider;

/**
 * @author rsauer
 * @version $Revision$
 */
public final class SecurityProperties
{
   public static final int PARTION_OID_UNDEFINED = -1;

   public static final String ENV_VAR_DEFAULT_DOMAIN = "CARNOT_DOMAIN";

   public static final String ENV_VAR_DEFAULT_REALM = "CARNOT_REALM";

   public static final String ENV_VAR_DEFAULT_USER = "CARNOT_USER";

   public static final String DEFAULT_DOMAIN = "Security.DefaultDomain";

   public static final String DEFAULT_REALM = "Security.DefaultRealm";

   public static final String DEFAULT_PARTITION = "Security.DefaultPartition";

   public static final String DEFAULT_USER = "Security.DefaultUser";

   public static final String DOMAIN = "Security.Domain";

   public static final String REALM = "Security.Realm";

   public static final String PARTITION = "Security.Partition";

   public static final String CRED_USER = "user";

   public static final String CRED_PASSWORD = "password";

   public static final String CRED_REALM = "realm";

   public static final String CRED_DOMAIN = "domain";

   public static final String CRED_PARTITION = "partition";

   public static final String PROMPT_FOR_PARTITION = "Security.PromptPartition";

   public static final String PROMPT_FOR_DOMAIN = "Security.PromptDomain";

   public static final String PROMPT_FOR_REALM = "Security.PromptRealm";

   @ConfigurationProperty(status = Status.Stable, useRestriction = UseRestriction.Public)
   public static final String AUTHENTICATION_MODE_PROPERTY = "Security.Authentication.Mode";

   public static final String AUTHENTICATION_MODE_INTERNAL = "internal";

   public static final String AUTHENTICATION_MODE_PRINCIPAL = "principal";

   public static final String AUTHENTICATION_MODE_JAAS = "jaas";

   public static final String AUTHENTICATION_MODE_IMPLICIT = "implicit";

   @ConfigurationProperty(status = Status.Stable, useRestriction = UseRestriction.Public)
   public static final String AUTHORIZATION_MODE_PROPERTY = "Security.Authorization.Mode";

   public static final String AUTHORIZATION_MODE_INTERNAL = "internal";

   public static final String AUTHORIZATION_MODE_EXTERNAL = "external";

   public static final String AUTHORIZATION_USE_PREFERENCES_STORE_PROPERTY = "Security.Authorization.UsePreferencesStore";

   /**
    *
    */
   public static final String AUTHENTICATION_CONFIGURATION_NAME_PROPERTY = "Security.Authentication.ConfigurationName";

   public static final String AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY = "Security.Authentication.PrincipalProvider";

   /** The class to filter out as the principal used. */
   public static final String AUTHENTICATION_PRINCIPAL_CLASS_PROPERTY = "Security.Authentication.PrincipalClass";

   /** name of the option for the maximum number of login retries option */
   public static final String MAXIMUM_LOGIN_RETRIES_PROPERTY = "Security.Authentication.MaximumNumberLoginRetries";

   /** name of the option for the invalidation time in minutes option */
   public static final String INVALIDATION_TIME_PROPERTY = "Security.Authentication.InvalidationTimeInMinutes";

   @ConfigurationProperty(status = Status.Stable, useRestriction = UseRestriction.Public)
   @PropertyValueType(ExternalLoginProvider.class)
   public static String AUTHENTICATION_SERVICE_PROPERTY = "Security.Authentication.LoginService";

   @ConfigurationProperty(status = Status.Stable, useRestriction = UseRestriction.Public)
   @PropertyValueType(DynamicParticipantSynchronizationProvider.class)
   public static String AUTHORIZATION_SYNC_CLASS_PROPERTY = "Security.Authorization.SynchronizationProvider";

   @ConfigurationProperty(status = Status.Stable, useRestriction = UseRestriction.Public)
   @PropertyValueType(DynamicParticipantSynchronizationStrategy.class)
   public static final String AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY = "Security.Authorization.SynchronizationStrategy";

   public static final String AUTHORIZATION_SYNC_ADMIN_PROPERTY = "Security.Authorization.SynchronizeOnAdministrationSession";

   public static final String AUTHORIZATION_SYNC_LOGIN_PROPERTY = "Security.Authorization.SynchronizeOnLogin";

   public static final String AUTHORIZATION_SYNC_LOAD_PROPERTY = "Security.Authorization.SynchronizeOnLoad";

   /** Enables tracing for user/userGroup synchronization with external registry. */
   public static final String AUTHORIZATION_SYNC_TRACE_PROPERTY = "Security.Authorization.TraceSynchronization";

   /**
    * Sets the validTo property of an user/userGroup to current date (invalidates it) in
    * case that it does not exitst in external registry.
    */
   public static final String AUTHORIZATION_SYNC_INVALIDATE_NONEXISTING_PARTICIPANTS_PROPERTY = "Security.Authorization.InvalidateNonexistingParticipants";

   public static final String AUTHORIZATION_SYNC_CONDITIONAL_PERFORMER_PROPERTY = "Security.Authorization.SynchronizeConditionalPerformer";

   public static final String AUTHORIZATION_SYNC_STRATEGY_USER_SYNC_TIMEOUT = "Security.Authorization.TimebasedSynchronizationStrategy.UserSyncTimeout";

   public static final String AUTHORIZATION_SYNC_STRATEGY_USER_GROUP_SYNC_TIMEOUT = "Security.Authorization.TimebasedSynchronizationStrategy.UserGroupSyncTimeout";

   // todo: rename or remove
   public static final String AUTHENTICATION_IMPLICIT_CLIENT_IDENTITY_PROPERTY = "DefaultJAASAuthenticatedBeanFactory.ImplicitClientIdentity";

   public static final String LOGIN_USERS_WITHOUT_TIMESTAMP = "Security.LoginUsersWithoutTimestamp";

   public static final String LOGIN_USERS_WITHOUT_LOGIN_LOGGING = "Security.LoginUsersWithoutLoginLogging";

   public static final String CURRENT_USER = "Current.User";

   public static final String CURRENT_DOMAIN = "Current.Domain";

   public static final String CURRENT_DOMAIN_OID = "Current.DomainOid";

   public static final String CURRENT_PARTITION = "Current.Partition";

   public static final String CURRENT_PARTITION_OID = "Current.PartitionOid";

   public static final String CREDENTIAL_PROVIDER = "Credential.Provider";

   public static final String SECURE_SESSION_FACTORY = "Secure.Session.Factory";

   public static final String DEFAULT_AUTHENTICATION_CONFIGURATION_NAME = "carnot";

   public static final String PRINCIPAL_SECRET = "Security.Principal.Secret";

   public static final String PRINCIPAL_VALIDATOR_PROPERTY = "Security.Principal.Validator";

   public static final String PRINCIPAL_VALIDATOR_DEFAULT_VALUE = "org.eclipse.stardust.engine.core.spi.security.AlwaysValidPrincipalValidator";

   public static boolean isInternalAuthentication()
   {

      final String authenticationMode = Parameters.instance().getString(
            AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);

      return AUTHENTICATION_MODE_INTERNAL.equalsIgnoreCase(authenticationMode);

   }

   public static boolean isInternalAuthorization()
   {
      final String authorizationMode = Parameters.instance().getString(
            AUTHORIZATION_MODE_PROPERTY, AUTHORIZATION_MODE_INTERNAL);

      return AUTHORIZATION_MODE_INTERNAL.equalsIgnoreCase(authorizationMode);
   }

   public static boolean isPrincipalBasedLogin()
   {
      return isPrincipalBasedLogin(Parameters.instance());
   }

   public static boolean isPrincipalBasedLogin(Parameters parameters)
   {
      return AUTHENTICATION_MODE_PRINCIPAL.equalsIgnoreCase(parameters.getString(AUTHENTICATION_MODE_PROPERTY));
   }

   public static boolean isImplicitAuthentication(Parameters parameters)
   {
      return AUTHENTICATION_MODE_IMPLICIT.equalsIgnoreCase(parameters.getString(AUTHENTICATION_MODE_PROPERTY));
   }

   public static IUser getUser()
   {
      return (IUser) Parameters.instance().get(CURRENT_USER);
   }

   public static long getUserOID()
   {
      IUser result = getUser();
      if (result == null)
      {
         return 0;
      }
      else
      {
         return result.getOID();
      }
   }

   public static IUserDomain getUserDomain()
   {
      return (IUserDomain) Parameters.instance().get(CURRENT_DOMAIN);
   }

   public static long getUserDomainOid()
   {
      Long oid = (Long) Parameters.instance().get(CURRENT_DOMAIN_OID);

      if (null == oid)
      {
         IUserDomain result = getUserDomain();
         if (result == null)
         {
            return 0;
         }
         else
         {
            return result.getOID();
         }
      }

      return oid.longValue();
   }

   public static IUserRealm getUserRealm()
   {
      IUser result = getUser();
      if (result == null)
      {
         return null;
      }
      else
      {
         return result.getRealm();
      }
   }

   public static long getUserRealmOid()
   {
      IUserRealm result = getUserRealm();
      if (result == null)
      {
         return 0;
      }
      else
      {
         return result.getOID();
      }
   }

   public static IAuditTrailPartition getPartition()
   {
      return getPartition(Parameters.instance());
   }

   public static IAuditTrailPartition getPartition(Parameters params)
   {
      return (IAuditTrailPartition) params.get(CURRENT_PARTITION);
   }

   public static IAuditTrailPartition getPartition(boolean mayBeCached)
   {
      return getPartition(Parameters.instance(), mayBeCached);
   }

   public static IAuditTrailPartition getPartition(Parameters params, boolean mayBeCached)
   {
      IAuditTrailPartition partition = getPartition(params);
      if (null != partition)
      {
         if ( !mayBeCached && !(partition instanceof AuditTrailPartitionBean))
         {
            partition = AuditTrailPartitionBean.findById(partition.getId());
         }
      }
      return partition;
   }

   public static short getPartitionOid()
   {
      return getPartitionOid(Parameters.instance());
   }

   public static short getPartitionOid(Parameters params)
   {
      Short oid = (Short) params.get(CURRENT_PARTITION_OID);

      if (null == oid)
      {
         IAuditTrailPartition result = getPartition(params);
         if (result == null)
         {
            return PARTION_OID_UNDEFINED;
         }
         else
         {
            return result.getOID();
         }
      }

      return oid.shortValue();
   }

   public static boolean isTeamLeader(IUser user)
   {
      List<IModelParticipant> all = CollectionUtils.createList();

      IUser currentUser = getUser();
      for (Iterator i = currentUser.getAllParticipants(); i.hasNext();)
      {
         IModelParticipant participant = (IModelParticipant) i.next();
         if (participant instanceof IRole)
         {
            for (Iterator<IOrganization> teamsIter = ((IRole) participant).getAllTeams(); teamsIter.hasNext();)
            {
               IOrganization team = teamsIter.next();
               collectAllParticipants(all, team);
            }
         }
      }

      for (Iterator i = user.getAllParticipants(); i.hasNext();)
      {
         IModelParticipant participant = (IModelParticipant) i.next();
         if (all.contains(participant))
         {
            return true;
         }
      }

      return false;
   }

   private static void collectAllParticipants(List<IModelParticipant> all,
         IOrganization team)
   {
      if ( !all.contains(team))
      {
         all.add(team);

         for (Iterator i = team.getAllParticipants(); i.hasNext();)
         {
            IModelParticipant childParticipant = (IModelParticipant) i.next();
            if (childParticipant instanceof IOrganization)
            {
               if ( !all.contains(childParticipant))
               {
                  all.add(childParticipant);
                  collectAllParticipants(all, (IOrganization) childParticipant);
               }
            }
            else
            {
               all.add(childParticipant);
            }
         }
      }
   }
}