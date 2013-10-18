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
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Flushable;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.annotations.SharedInstance;
import org.eclipse.stardust.common.annotations.Stateless;
import org.eclipse.stardust.common.config.*;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.OrganizationInfoDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.extensions.ExtensionService;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.spi.security.*;
import org.eclipse.stardust.engine.core.spi.security.ExternalUserConfiguration.GrantInfo;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class SynchronizationService
{
   public static final Logger trace = LogManager.getLogger(SynchronizationService.class);

   public static final String PRP_DISABLE_SYNCHRONIZATION = SynchronizationService.class.getName()
         + ".DisableSynchronization";

   public static final String PRP_SYNC_PROVIDER_CACHE = SynchronizationService.class.getName()
         + ".SyncProviderCache";

   private static final String GLOBAL = SynchronizationService.class.getName()
         + ".Global";

   private Map properties;

   public static synchronized void flush()
   {
      if ( !SecurityProperties.isInternalAuthentication())
      {
         DynamicParticipantSynchronizationStrategy synchronizationStrategy = getSynchronizationStrategy();
         if (synchronizationStrategy instanceof Flushable)
         {
            Flushable flushableStrategy = (Flushable) synchronizationStrategy;
            flushableStrategy.flush();
         }
      }
   }

   public static void synchronize(IUser user)
   {
      if (Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Skipping synchronization of user '"
                  + user.getRealmQualifiedAccount()
                  + "'. Synchronization is not enabled for archive audit trails.");
         }
      }
      else
      {
         new IsolatedCreateSyncService(Collections.EMPTY_MAP).synchronizeUnguarded(user);
      }
   }

   public static void synchronize(IUserGroup userGroup)
   {
      if (Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Skipping synchronization of user group '" + userGroup.getId()
                  + "'. Synchronization is not enabled for archive audit trails.");
         }
      }
      else
      {
         new IsolatedCreateSyncService(Collections.EMPTY_MAP)
               .synchronizeUnguarded(userGroup);
      }
   }

   public static void synchronize(final IDepartment department)
   {
      if (Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Skipping synchronization of department '" + department.getId()
                  + "'. Synchronization is not enabled for archive audit trails.");
         }
      }
      else
      {
         new IsolatedCreateSyncService(Collections.EMPTY_MAP)
               .synchronizeUnguarded(department, PredefinedConstants.ANY_MODEL);
      }
   }

   public static IUser synchronize(String account, IModel model,
         boolean allowNewAccount, Map properties) throws ObjectNotFoundException
   {
      Map mergedProperties = new HashMap(properties);
      LoginUtils.mergeDefaultCredentials(mergedProperties);

      IUser user;

      if (SecurityProperties.isInternalAuthentication()
            || Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         String realmId = LoginUtils.getUserRealmId(mergedProperties);
         UserRealmBean realm = UserRealmBean.findById(realmId,
               getPartitionOid(properties));
         user = UserBean.findByAccount(account, realm);
      }
      else
      {
         SynchronizationService syncService = new IsolatedCreateSyncService(mergedProperties);
         if (null != model)
         {
            user = syncService.synchronizeExternalUser(account, allowNewAccount);
         }
         else
         {
            user = syncService.synchronizeExternalAdministrator(account, allowNewAccount);
         }
      }

      return user;
   }

   public static IUserGroup synchronizeUserGroup(String id)
         throws ObjectNotFoundException
   {
      IUserGroup group;

      if (SecurityProperties.isInternalAuthentication()
            || Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL,
                  false))
      {
         group = UserGroupBean.findById(id, SecurityProperties.getPartitionOid());
      }
      else
      {
         group = new IsolatedCreateSyncService(Collections.EMPTY_MAP)
               .synchronizeExternalUserGroup(id);
      }

      return group;
   }

   public static Pair<IDepartment, Boolean> synchronizeDepartment(
         final String participantId, long modelOid, final List<String> departmentKeys)
         throws ObjectNotFoundException
   {
      Pair<IDepartment, Boolean> departmentPair;

      if (SecurityProperties.isInternalAuthentication()
            || Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL,
                  false))
      {
         final IDepartment department = findDepartment(participantId, departmentKeys);
         departmentPair = new Pair<IDepartment, Boolean>(department, true);
      }
      else
      {
         departmentPair = new IsolatedCreateSyncService(Collections.EMPTY_MAP)
               .synchronizeExternalDepartment(participantId, modelOid, departmentKeys);
      }

      return departmentPair;
   }

   public static Pair<String, List<String>> getDepartmentPairFor(final IDepartment department, long modelOid)
   {
      final IOrganization participant = DepartmentUtils.getOrganization(department, modelOid);
      final String participantId = participant.getQualifiedId();

      final List<String> departmentKeys = newArrayList();
      createDepartmentKeys(department, departmentKeys);

      return new Pair(participantId, departmentKeys);
   }

   public static Pair<String, List<String>> getDepartmentPairFor(final String departmentId,
         final String participantId, final DepartmentInfo parent)
   {
      final List<String> departmentKeys = newArrayList();
      if (parent != null)
      {
         final IDepartment department = DepartmentUtils.getDepartment(parent);
         createDepartmentKeys(department, departmentKeys);
      }
      departmentKeys.add(departmentId);
      return new Pair<String, List<String>>(participantId, departmentKeys);
   }

   protected SynchronizationService(Map properties)
   {
      this.properties = properties;
   }

   protected IUser synchronizeExternalUser(String account, boolean allowNewAccount)
         throws ObjectNotFoundException
   {
      IUser user = null;

      try
      {
         String realmId = LoginUtils.getUserRealmId(properties);
         IUserRealm realm = UserRealmBean.findById(realmId, getPartitionOid(properties));

         Parameters params = Parameters.instance();
         try
         {
            PropertyLayer props = ParametersFacade.pushLayer(params,
                  Collections.EMPTY_MAP);
            props.setProperty(PRP_DISABLE_SYNCHRONIZATION, Boolean.TRUE.toString());

            user = UserBean.findByAccount(account, realm);
         }
         finally
         {
            ParametersFacade.popLayer(params);
         }

         if (params.getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Skipping synchronization of user '"
                     + user.getRealmQualifiedAccount()
                     + "'. Synchronization is not enabled for archive audit trails.");
            }
         }
         else
         {
            synchronizeUnguarded(user);
         }
      }
      catch (ObjectNotFoundException e)
      {
         // probably first synchronization of this account
         user = importExternalUser(account, allowNewAccount);
      }

      return user;
   }

   protected IUserGroup synchronizeExternalUserGroup(String id)
         throws ObjectNotFoundException
   {
      IUserGroup group;

      try
      {
         Parameters params = Parameters.instance();
         try
         {
            PropertyLayer props = ParametersFacade.pushLayer(params,
                  Collections.EMPTY_MAP);
            props.setProperty(PRP_DISABLE_SYNCHRONIZATION, Boolean.TRUE.toString());

            group = UserGroupBean.findById(id, getPartitionOid(properties));
         }
         finally
         {
            ParametersFacade.popLayer(params);
         }
         synchronizeUnguarded(group);
      }
      catch (ObjectNotFoundException e)
      {
         // probably first synchronization of this group
         group = importExternalGroup(id);
      }

      return group;
   }

   protected Pair<IDepartment, Boolean> synchronizeExternalDepartment(final String participantId,
         long modelOid, final List<String> departmentKeys)
   {
      Pair<IDepartment, Boolean> departmentPair;

      try
      {
         Parameters params = Parameters.instance();
         try
         {
            PropertyLayer props = ParametersFacade.pushLayer(params,
                  Collections.EMPTY_MAP);
            props.setProperty(PRP_DISABLE_SYNCHRONIZATION, Boolean.TRUE.toString());

            final IDepartment department = findDepartment(participantId, departmentKeys);
            departmentPair = new Pair<IDepartment, Boolean>(department, true);
         }
         finally
         {
            ParametersFacade.popLayer(params);
         }
         synchronizeUnguarded(departmentPair.getFirst(), modelOid);
      }
      catch (ObjectNotFoundException e)
      {
         final ModelManager modelManager = ModelManagerFactory.getCurrent();
         final IModelParticipant participant = findModelParticipantFor(participantId, modelManager);
         final IModelParticipant scopedParticipant = DepartmentUtils.getFirstScopedOrganization(participant);
         if (scopedParticipant != null)
         {
            final String scopedParticipantId = scopedParticipant.getQualifiedId();
            final DynamicParticipantSynchronizationProvider provider = initializeProvider();
            if (  provider != null &&
                  provider.provideValidDepartmentConfiguration(scopedParticipantId, departmentKeys, properties) != null)
            {
               departmentPair = importDepartmentHierarchy(scopedParticipantId, departmentKeys);
            }
            else
            {
               departmentPair = new Pair<IDepartment, Boolean>(null, false);
            }
         }
         else
         {
            departmentPair = new Pair<IDepartment, Boolean>(null, false);
         }
      }

      return departmentPair;
   }

   private IUser synchronizeExternalAdministrator(String account, boolean allowNewAccount)
         throws ObjectNotFoundException
   {
      DynamicParticipantSynchronizationProvider provider = initializeProvider();
      if (null == provider)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.AUTHx_SYNC_MISSING_SYNCHRONIZATION_PROVIDER.raise());
      }

      String realmId = LoginUtils.getUserRealmId(properties);

      ExternalUserConfiguration adminConf = provider.provideValidUserConfiguration(realmId,
            account, properties);
      if (null == adminConf)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.AUTHx_SYNC_UNKNOWN_USER.raise(account), account);
      }

      final boolean isAdmin = isAdmin(adminConf);

      IUser user = synchronizeExternalUser(account, isAdmin || allowNewAccount);
      if (isAdmin)
      {
         user = (IUser) Proxy.newProxyInstance(
               user.getClass().getClassLoader(), new Class[] {IUser.class},
               new TransientAdministratorDecorator(user));
      }
      return user;
   }

   @SuppressWarnings("deprecation")
   private boolean isAdmin(final ExternalUserConfiguration adminConf)
   {
      boolean isAdmin;
      Set<GrantInfo> grants = adminConf.getModelParticipantsGrants();
      if (CollectionUtils.isEmpty(grants))
      {
         isAdmin = adminConf.getGrantedModelParticipants().contains(
               PredefinedConstants.ADMINISTRATOR_ROLE);
         trace.debug("Detected use of a to be deprecated SPI. Please refer to ExternalUserConfiguration#getModelParticipantsGrants().");
      }
      else
      {
         isAdmin = grants.contains(new GrantInfo(
               PredefinedConstants.ADMINISTRATOR_ROLE, Collections.<String>emptyList()));
         if (CollectionUtils.isNotEmpty(adminConf.getGrantedModelParticipants()))
         {
            trace.warn("Received grants from both the department aware as well as the to be deprecated SPI. Ignoring the latter.");
         }
      }
      return isAdmin;
   }


   private IUser importExternalUser(String account, boolean allowNewAccount)
         throws ObjectNotFoundException
   {
      if (Parameters.instance().getBoolean(
            Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_ARCHIVE_AUDIT_TRAIL_WRITE_PROTECTED.raise());
      }
      else if ( !allowNewAccount)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.AUTHx_SYNC_IMPORTING_USERS_NOT_ALLOWED.raise(), account);
      }

      DynamicParticipantSynchronizationProvider provider = initializeProvider();
      if (null == provider)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.AUTHx_SYNC_MISSING_SYNCHRONIZATION_PROVIDER.raise());
      }
      
      ExtensionService.initializeRealmExtensions();

      String realmId = LoginUtils.getUserRealmId(properties);

      ExternalUserConfiguration userConf = provider.provideValidUserConfiguration(realmId,
            account, properties);
      if (null == userConf)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.AUTHx_SYNC_UNKNOWN_USER.raise(account), account);
      }

      IUser user = null;

      int nRetries = 3;
      while (null == user)
      {
         try
         {
            performCreateAction(new CreateUserAction(realmId, account, userConf,
                  properties));
         }
         catch (Exception e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Failed to synchronize user '" + account
                     + "' to audit trail. Trying again.", e);
            }
         }

         try
         {
            IUserRealm realm = UserRealmBean.findById(realmId,
                  getPartitionOid(properties));
            user = UserBean.findByAccount(account, realm);
         }
         catch (ObjectNotFoundException e)
         {
            if (0 >= --nRetries)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.AUTHx_SYNC_FAILED_IMPORTING_USER.raise(account), e);
            }
         }
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Synchronizing new user '" + account + "' with oid " + user.getOID()
               + ".");
      }

      // force initial synchronization
      synchronizeUnguarded(user, userConf);

      return user;
   }

   private IUserGroup importExternalGroup(String id)
         throws ObjectNotFoundException
   {
      if (Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_ARCHIVE_AUDIT_TRAIL_WRITE_PROTECTED.raise());
      }

      DynamicParticipantSynchronizationProvider provider = initializeProvider();
      if (null == provider)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.AUTHx_SYNC_MISSING_SYNCHRONIZATION_PROVIDER.raise());
      }

      ExternalUserGroupConfiguration groupConf = provider.provideValidUserGroupConfiguration(
            id, properties);
      if (null == groupConf)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.AUTHx_SYNC_UNKNOWN_USER_GROUP.raise(id), id);
      }

      IUserGroup group = null;

      int nRetries = 3;
      while (null == group)
      {
         try
         {
            performCreateAction(new CreateUserGroupAction(id, groupConf, properties));
         }
         catch (Exception e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Failed to synchronize user group '" + id
                     + "' to audit trail. Trying again.", e);
            }
         }

         try
         {
            group = UserGroupBean.findById(id, getPartitionOid(properties));
         }
         catch (ObjectNotFoundException e)
         {
            if (0 >= --nRetries)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.AUTHx_SYNC_FAILED_IMPORTING_USER_GROUP.raise(id), e);
            }
         }
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Synchronizing user group '" + id + "' with oid " + group.getOID()
               + ".");
      }

      // force initial synchronization
      synchronizeUnguarded(group, groupConf);

      return group;
   }

   private IDepartment importExternalDepartment(final String id, final IDepartment parentDepartment,
         final IOrganization org)
   {
      IDepartment department = null;

      int nRetries = 3;
      while (department == null)
      {
         try
         {
            performCreateAction(new CreateDepartmentAction(id, parentDepartment, org));
         }
         catch (Exception e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Failed to synchronize department '" + id
                     + "' to audit trail. Trying again.", e);
            }
         }

         try
         {
            department = DepartmentBean.findById(id, parentDepartment, org);
         }
         catch (ObjectNotFoundException e)
         {
            if (0 >= --nRetries)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.AUTHx_SYNC_FAILED_IMPORTING_DEPARTMENT.raise(id, parentDepartment.getId()), e);
            }
         }
      }

      return department;
   }

   protected abstract void performCreateAction(Action createAction) throws Exception;

   protected void synchronizeUnguarded(IUser user)
   {
      if ( !SecurityProperties.isInternalAuthentication())
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         if (!session.isSynchronized(user) && getSynchronizationStrategy().isDirtyLogAware(user))
         {
            DynamicParticipantSynchronizationProvider provider = initializeProvider();
            if (null != provider)
            {
               ExternalUserConfiguration userConf = provider.provideValidUserConfiguration(
                     user.getRealm().getId(), user.getAccount(), properties);

               if (null != userConf)
               {
                  synchronizeUnguarded(user, userConf);
               }
               else
               {
                  if (Parameters.instance()
                        .getBoolean(
                              SecurityProperties.AUTHORIZATION_SYNC_INVALIDATE_NONEXISTING_PARTICIPANTS_PROPERTY,
                              false))
                  {
                     Date validTo = user.getValidTo();
                     if (validTo == null || validTo.after(Calendar.getInstance().getTime()))
                     {
                        trace.info("User '"
                              + user.getRealmQualifiedAccount()
                              + "' does not exist in external registry. User gets invalidated.");
                        user.setValidTo(Calendar.getInstance().getTime());
                     }
                  }
                  else
                  {
                     trace.info("User '"
                           + user.getRealmQualifiedAccount()
                           + "' does not exist in external registry. Skipping synchronization.");
                  }
               }
            }
            else
            {
               trace.warn("Skipping synchronization of user '"
                     + user.getRealmQualifiedAccount()
                     + "'. Invalid synchronization provider configuration.");
            }
         }
      }
   }

   protected void synchronizeUnguarded(IUser user, ExternalUserConfiguration userConf)
   {
      if (null != userConf)
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         if (!session.isSynchronized(user) && getSynchronizationStrategy().isDirtyLogAware(user))
         {
            if (Parameters.instance().getBoolean(
                  SecurityProperties.AUTHORIZATION_SYNC_TRACE_PROPERTY, false))
            {
               trace.info("Synchronizing user '" + user.getRealmQualifiedAccount()
                     + "' with external registry.");
            }

            synchronizeUserAttributes(user, userConf);
            if ( !SecurityProperties.isInternalAuthorization())
            {
               if (ModelManagerFactory.getCurrent().getAllModels().hasNext())
               {
                  synchronizeModelParticipantGrants(user, userConf);
               }
               synchronizeUserGroupMemberships(user, userConf);
            }

            session.setSynchronized(user);
            getSynchronizationStrategy().setSynchronizedLogAware(user);
         }
      }
      else
      {
         trace.warn("Skipping synchronization of user '"
               + user.getRealmQualifiedAccount() + "'. Missing user data.");
      }
   }

   protected void synchronizeUnguarded(IUserGroup group)
   {
      if (!SecurityProperties.isInternalAuthentication())
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         if (!session.isSynchronized(group) && getSynchronizationStrategy().isDirty(group))
         {
            DynamicParticipantSynchronizationProvider provider = initializeProvider();
            if (null != provider)
            {
               ExternalUserGroupConfiguration groupConf = provider
                     .provideValidUserGroupConfiguration(group.getId(), properties);
               if (null != groupConf)
               {
                  synchronizeUnguarded(group, groupConf);
               }
               else
               {
                  if (Parameters.instance()
                        .getBoolean(
                              SecurityProperties.AUTHORIZATION_SYNC_INVALIDATE_NONEXISTING_PARTICIPANTS_PROPERTY,
                              false))
                  {
                     Date validTo = group.getValidTo();
                     if (validTo == null || validTo.after(Calendar.getInstance().getTime()))
                     {
                        trace.info("User group '"
                              + group.getId()
                              + "' does not exist in external registry. User group gets invalidated.");
                        group.setValidTo(Calendar.getInstance().getTime());
                     }
                  }
                  else
                  {
                     trace.info("User group '"
                           + group.getId()
                           + "' does not exist in external registry. Skipping synchronization.");
                  }
               }
            }
            else
            {
               trace.warn("Skipping synchronization of user group '" + group.getId()
                     + "'. Invalid synchronization provider configuration.");
            }
         }
      }
   }

   protected void synchronizeUnguarded(IUserGroup group,
         ExternalUserGroupConfiguration groupConf)
   {
      if (null != groupConf)
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         if (!session.isSynchronized(group) && getSynchronizationStrategy().isDirty(group))
         {
            if (Parameters.instance().getBoolean(
                  SecurityProperties.AUTHORIZATION_SYNC_TRACE_PROPERTY, false))
            {
               trace.info("Synchronizing user group '" + group.getId()
                     + "' with external registry.");
            }

            synchronizeUserGroupAttributes(group, groupConf);

            session.setSynchronized(group);
            getSynchronizationStrategy().setSynchronized(group);
         }
      }
   }

   protected void synchronizeUnguarded(final IDepartment department, long modelOid)
   {
      /* null department does not need to be synchronized */
      if (department == null) return;

      /* do not synchronize if internal authentication is used */
      if (SecurityProperties.isInternalAuthentication()) return;

      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (!session.isSynchronized(department) && getSynchronizationStrategy().isDirty(department))
      {
         final DynamicParticipantSynchronizationProvider provider = initializeProvider();
         if (null != provider)
         {
            final Pair<String, List<String>> departmentPair = getDepartmentPairFor(department, modelOid);
            final ExternalDepartmentConfiguration departmentConf = provider
                  .provideValidDepartmentConfiguration(departmentPair.getFirst(),
                        departmentPair.getSecond(), properties);
            if (departmentConf != null)
            {
               synchronizeUnguarded(department, departmentConf);
            }
            else
            {
               // TODO (nw) what to do if department does not exist in external registry
            }
         }
         else
         {
            trace.warn("Skipping synchronization of department '" + department.getId()
                  + "'. Invalid synchronization provider configuration.");
         }
      }
   }

   protected void synchronizeUnguarded(final IDepartment department,
         final ExternalDepartmentConfiguration departmentConf)
   {
      if (null != departmentConf)
      {
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         if (!session.isSynchronized(department) && getSynchronizationStrategy().isDirty(department))
         {
            if (Parameters.instance().getBoolean(
                  SecurityProperties.AUTHORIZATION_SYNC_TRACE_PROPERTY, false))
            {
               trace.info("Synchronizing department '" + department.getId()
                     + "' with external registry.");
            }

            synchronizeDepartmentAttributes(department, departmentConf);

            session.setSynchronized(department);
            getSynchronizationStrategy().setSynchronized(department);
         }
      }
   }

   /**
    * Checks whether the passed principal is a carnot known principal class and
    * authentified external. If so then the attributes are extracted from the
    * principal object and inserted into the user object.
    */
   private void synchronizeUserAttributes(IUser user, ExternalUserConfiguration userConf)
   {
      if (!user.isValid())
      {
         // user is revalidated
         ExtensionService.initializeRealmExtensions();

         user.setValidFrom(new Date());
      }

      user.setValidTo(null);

      user.setFirstName(userConf.getFirstName());
      user.setLastName(userConf.getLastName());
      user.setEMail(userConf.getEMail());
      user.setDescription(userConf.getDescription());
      user.setSessionTokens(userConf.getSessionTokens());
      
      Map properties = userConf.getProperties();

      if ((null != properties) && !properties.isEmpty())
      {
         for (Iterator i = properties.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();

            user.setPropertyValue((String) entry.getKey(), (String) entry.getValue());
         }
      }
   }

   private void synchronizeModelParticipantGrants(final IUser user,
         final ExternalUserConfiguration userConf)
   {
      final Set<ScopedParticipant> validGrants = determineValidGrants(user, userConf);

      final Set<ScopedParticipant> existingGrants = CollectionUtils.newHashSet();
      final List<ScopedParticipant> invalidGrants = CollectionUtils.newArrayList();

      determineGrantDelta(user, validGrants, existingGrants, invalidGrants);
      removeInvalidGrants(user, invalidGrants);
      addValidGrants(user, validGrants, existingGrants);
   }

   private void determineGrantDelta(final IUser user, final Set<ScopedParticipant> validGrants,
         final Set<ScopedParticipant> existingGrants,
         final List<ScopedParticipant> invalidGrants)
   {
      for (Iterator<UserParticipantLink> i = user.getAllParticipantLinks(); i.hasNext();)
      {
         final UserParticipantLink link = i.next();
         final IModelParticipant participant = link.getParticipant();
         final IDepartment department = link.getDepartment();
         final ScopedParticipant scopedParticipant = ScopedParticipant
               .factory(participant, department);

         if (validGrants.contains(scopedParticipant))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Model participant " + participant.getId() + " (model OID "
                     + participant.getModel().getModelOID() + ") for department "
                     + (department != null ? department.getId() : "null") + " remains granted to user "
                     + user.getId());
            }
            existingGrants.add(scopedParticipant);
         }
         else
         {
            trace.warn("Model participant " + participant.getId() + " (model OID "
                  + participant.getModel().getModelOID() + ") for department "
                  + (department != null ? department.getId() : "null") + " is no longer granted to user "
                  + user.getId());

            invalidGrants.add(scopedParticipant);
         }
      }
   }

   private void addValidGrants(IUser user, final Set<ScopedParticipant> validGrants,
         final Set<ScopedParticipant> existingGrants)
   {
      for (Iterator<ScopedParticipant> i = validGrants.iterator(); i.hasNext();)
      {
         final ScopedParticipant scopedParticipant = i.next();

         if (!existingGrants.contains(scopedParticipant))
         {
            final IModelParticipant participant = scopedParticipant.getParticipant();
            final IDepartment department = scopedParticipant.getDepartment();
            trace.info("Adding grant for model participant " + participant.getId()
                  + " (model OID " + participant.getModel().getModelOID()
                  + ") for department " + (department != null ? department.getId() : "null")
                  + "for user " + user.getId());

            user.addToParticipants(participant, department);
         }
      }
   }

   private void removeInvalidGrants(IUser user,
         final List<ScopedParticipant> invalidGrants)
   {
      for (Iterator<ScopedParticipant> i = invalidGrants.iterator(); i.hasNext();)
      {
         final ScopedParticipant scopedParticipant = i.next();
         final IModelParticipant participant = scopedParticipant.getParticipant();
         final IDepartment department = scopedParticipant.getDepartment();
         user.removeFromParticipants(participant, department);
      }
   }

   private Set<ScopedParticipant> determineValidGrants(final IUser user,
         final ExternalUserConfiguration userConf)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();
      final Set<ScopedParticipant> validGrants = new HashSet<ScopedParticipant>();

      final Set<GrantInfo> grants = getModelParticipantsGrants(user, userConf);
      for (Iterator<GrantInfo> i = grants.iterator(); i.hasNext();)
      {
         final GrantInfo grant = i.next();
         final Set<IModelParticipant> participants = determineValidParticipants(modelManager, grant);
         if ( !participants.isEmpty())
         {
            for (final IModelParticipant participant : participants)
            {
               if (grant.getDepartmentKey().isEmpty())
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Synchronizing valid model participant (" + grant.getParticipantId()+ ")");
                  }
                  validGrants.add(ScopedParticipant.factory(participant, null));
               }
               else
               {
                  addValidScopedGrants(modelManager, validGrants, grant, participant);
               }
            }
         }
         else
         {
            trace.warn("Skipping synchronization of invalid model participant (" + grant.getParticipantId() + ").");
         }
      }
      return validGrants;
   }

   private void addValidScopedGrants(final ModelManager modelManager,
         final Set<ScopedParticipant> validGrants,
         final GrantInfo grant,
         final IModelParticipant participant)
   {
      long modelOid = participant.getModel().getModelOID();
      
      final Pair<IDepartment, Boolean> departmentPair = synchronizeDepartment(participant.getQualifiedId(), modelOid, grant.getDepartmentKey());
      final boolean isValid = departmentPair.getSecond() != null && departmentPair.getSecond();

      if (isValid)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Synchronizing valid model participant (" + grant.getParticipantId()+ ", " + departmentPair.getFirst() + ")");
         }
         validGrants.add(ScopedParticipant.factory(participant, departmentPair.getFirst()));
      }
      else
      {
         trace.warn("Skipping synchronization of invalid model participant (" + grant.getParticipantId() + ", " + departmentPair.getFirst() + ")");
      }
   }

   private Set<IModelParticipant> determineValidParticipants(final ModelManager modelManager,
         final GrantInfo grant)
   {
      final String participantId = grant.getParticipantId();
      Iterator<IModelParticipant> participants = modelManager.getParticipantsForID(participantId);

      final Set<IModelParticipant> participantSet = new HashSet<IModelParticipant>();
      while (participants.hasNext())
      {
         participantSet.add(participants.next());
      }
      return participantSet;
   }

   private Set<GrantInfo> getModelParticipantsGrants(final IUser user, final ExternalUserConfiguration extUserConf)
   {
      Set<GrantInfo> grants = extUserConf.getModelParticipantsGrants();
      if (grants == null)
      {
         grants = CollectionUtils.newHashSet();
      }
      else
      {
         grants = CollectionUtils.copySet(grants);
      }

      if (CollectionUtils.isEmpty(grants))
      {
         @SuppressWarnings("deprecation")
         final Collection<String> grantCollection = extUserConf.getGrantedModelParticipants();
         if (CollectionUtils.isNotEmpty(grantCollection))
         {
            trace.debug("Detected use of a to be deprecated SPI. Please refer to ExternalUserConfiguration#getModelParticipantsGrants().");

            for (final String id : grantCollection)
            {
               grants.add(new GrantInfo(id, Collections.<String>emptyList()));
            }
         }
      }
      else
      {
         @SuppressWarnings("deprecation")
         final Collection<String> grantCollection = extUserConf.getGrantedModelParticipants();
         if (CollectionUtils.isNotEmpty(grantCollection))
         {
            trace.warn("Received grants from both the department aware as well as the to be deprecated SPI. Ignoring the latter.");
         }
      }

      return grants;
   }

   private void synchronizeUserGroupMemberships(IUser user,
         ExternalUserConfiguration userConf)
   {
      Map remoteGroupList = new HashMap();

      for (Iterator i = userConf.getUserGroupMemberships().iterator(); i.hasNext();)
      {
         String id = (String) i.next();

         IUserGroup group;
         try
         {
            // TODO: use some kind of batch load
            group = UserGroupBean.findById(id, getPartitionOid(properties));
            synchronize(group);
         }
         catch (ObjectNotFoundException e)
         {
            try
            {
               group = SecurityProperties.isInternalAuthentication()
                     ? null
                     : synchronizeExternalUserGroup(id);
            }
            catch (ObjectNotFoundException e1)
            {
               trace.warn("Failed synchronizing group membership for user " + user
                     + " and group '" + id + "'.", e1);
               group = null;
            }
         }

         if (null != group)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Considering user group '" + id + "' membership for user '"
                     + user.getRealmQualifiedAccount() + "'.");
            }

            remoteGroupList.put(group.getId(), group);
         }
      }

      List invalidMemberships = new ArrayList();
      for (Iterator i = user.getAllUserGroups(false); i.hasNext();)
      {
         IUserGroup group = (IUserGroup) i.next();

         if (remoteGroupList.containsKey(group.getId()))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("User group '" + group.getId()
                     + "' still contains user '" + user.getId() + "'.");
            }

            remoteGroupList.remove(group.getId());
         }
         else
         {
            trace.info("User group '" + group.getId()
                  + "' no longer contains user '" + user.getId() + "'.");

            invalidMemberships.add(group);
         }
      }

      for (Iterator i = invalidMemberships.iterator(); i.hasNext();)
      {
         ((IUserGroup) i.next()).removeUser(user);
      }

      for (Iterator i = remoteGroupList.values().iterator(); i.hasNext();)
      {
         IUserGroup group = (IUserGroup) i.next();

         trace.info("New user group " + group + " joined by user " + user.getId());

         group.addUser(user);
      }
   }

   /**
    * Checks whether the passed principal is a carnot known principal class and
    * authentified external. If so then the attributes are extracted from the
    * principal object and inserted into the user object.
    */
   private void synchronizeUserGroupAttributes(IUserGroup group,
         ExternalUserGroupConfiguration groupConf)
   {
      if (!group.isValid())
      {
         group.setValidFrom(new Date());
      }

      group.setValidTo(null);

      group.setName(groupConf.getName());
      group.setDescription(groupConf.getDescription());

      Map properties = groupConf.getProperties();

      if ((null != properties) && !properties.isEmpty())
      {
         for (Iterator i = properties.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();

            group.setPropertyValue((String) entry.getKey(), (String) entry.getValue());
         }
      }
   }

   private void synchronizeDepartmentAttributes(final IDepartment department,
         final ExternalDepartmentConfiguration departmentConf)
   {
      long oid = department.getOID();
      // since this will modify the cache, fetch new bean.
      DepartmentBean departmentBean = DepartmentBean.findByOID(oid);
      departmentBean.setName(departmentConf.getName());
      departmentBean.setDescription(departmentConf.getDescription());
   }

   private Pair<IDepartment, Boolean> importDepartmentHierarchy(final String participantId,
         final List<String> departmentKeys)
   {
      //the department hierarchy is changed during import for computing reasons so create a copy 
      List<String> departmentHierachy = new ArrayList<String>(departmentKeys);
      
      IDepartment department;
      boolean validDepartment;
      try
      {
         department = importDepartmentHierarchyInternal(participantId, departmentHierachy);
         validDepartment = true;
      }
      catch (DepartmentSynchronizationException e)
      {
         trace.warn("Department for Participant with id '" + participantId + "' could not be imported.");
         department = null;
         validDepartment = false;
      }

      return new Pair<IDepartment, Boolean>(department, validDepartment);
   }

   private IDepartment importDepartmentHierarchyInternal(final String participantId,
         final List<String> departmentKeys)
   {
      if (departmentKeys.isEmpty()) return null;

      final ModelManager modelManager = ModelManagerFactory.getCurrent();
      final IModelParticipant participant = findModelParticipantFor(participantId, modelManager);
      if (participant == null)
      {
         trace.warn("No valid participant found for id '" + participantId + "'.");
         throw new DepartmentSynchronizationException("Participant not found for id: " + participantId);
      }

      final IOrganization org = DepartmentUtils.getFirstScopedOrganization(participant);
      if (org == null)
      {
         return null;
      }

      final long scopeLevel = getScopeLevel(org);
      if (scopeLevel < departmentKeys.size())
      {
         throw new DepartmentSynchronizationException("More department keys specified than permitted.");
      }
      while (scopeLevel > departmentKeys.size())
      {
         departmentKeys.add(null);
      }

      return importDepartment(org, departmentKeys);
   }

   private IDepartment importDepartment(final IOrganization org,
         final List<String> departmentKeys) throws DepartmentSynchronizationException
   {
      final String departmentKey = departmentKeys.remove(departmentKeys.size() - 1);
      final IOrganization parent = DepartmentUtils.getFirstScopedOrganization(DepartmentUtils.getParentOrg(org));

      IDepartment parentDepartment = null;
      if (parent != null)
      {
         parentDepartment = importDepartment(parent, departmentKeys);
      }

      if (departmentKey != null)
      {
         IDepartment department;
         try
         {
            department = DepartmentBean.findById(departmentKey, parentDepartment, org);
         }
         catch (ObjectNotFoundException e)
         {
            department = importExternalDepartment(departmentKey, parentDepartment, org);
         }
         
         long modelOid = org.getModel().getModelOID();
         synchronizeUnguarded(department, modelOid);
         return department;
      }
      else
      {
         return parentDepartment;
      }
   }

   private long getScopeLevel(final IModelParticipant participant)
   {
      final IOrganization parent = DepartmentUtils.getFirstScopedOrganization(DepartmentUtils.getParentOrg(participant));
      if (parent == null)
      {
         return 1;
      }
      else
      {
         return getScopeLevel(parent) + 1;
      }
   }

   private static IDepartment findDepartment(String participantId,
         List<String> departmentKeys) throws ObjectNotFoundException
   {
      if (departmentKeys == null || departmentKeys.isEmpty())
      {
         return null;
      }

      final int indexOfFirstNull = departmentKeys.indexOf(null);
      if (indexOfFirstNull >= 0)
      {
         departmentKeys = departmentKeys.subList(0, indexOfFirstNull);
      }
      if (departmentKeys.isEmpty())
      {
         return null;
      }

      final ModelManager modelManager = ModelManagerFactory.getCurrent();
      final IModelParticipant participant = findModelParticipantFor(participantId, modelManager);
      if (participant == null)
      {
         trace.warn("No valid participant found for id '" + participantId + "'.");
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_PARTICIPANT_ID.raise(participantId));
      }

      List<IOrganization> orgs = Authorization2.findRestricted(participant);
      IDepartment department = null;
      for (int i = 0, len = departmentKeys.size(); i < len; i++)
      {
         department = DepartmentBean.findById(departmentKeys.get(i), department, orgs.get(i));
      }

      if (department == null)
      {
         int size = departmentKeys.size();
         String id = departmentKeys.get(size - 1);
         String parentId = size > 1 ?  departmentKeys.get(size - 2) : null;
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_DEPARTMENT_ID2.raise(id, parentId));
      }
      return department;
   }

   public static IModelParticipant findModelParticipantFor(final String participantId,
         final ModelManager modelManager)
   {
      Iterator<IModelParticipant> participants = modelManager.getParticipantsForID(participantId);
      return participants.hasNext() ? participants.next() : null;
      /*final IModelParticipant[] participantBucket = new IModelParticipant[1];
      modelManager.findModel(new Predicate()
      {
         public boolean accept(Object o)
         {
            IModel model = (IModel) o;
            participantBucket[0] = model.findParticipant(participantId);
            return participantBucket[0] != null;
         }
      });

      return participantBucket[0];*/
   }

   private static void createDepartmentKeys(final IDepartment department,
         final List<String> departmentKeys)
   {
      final IDepartment parent = department.getParentDepartment();
      if (parent != null)
      {
         createDepartmentKeys(parent, departmentKeys);
      }
      departmentKeys.add(department.getId());
   }

   /**
    * For backwards compatibility, synch providers will not actively be cached. However,
    * annotations {@link Stateless} or {@link SharedInstance} might be used to indicate it
    * is safe to cache POJO implementations of this interface.
    *
    * @return
    */
   public static DynamicParticipantSynchronizationProvider getSynchronizationProvider()
   {
      return initializeProvider();
   }

   /**
    * Obtains a new synchronization provider instance.
    *
    * @return A new instance of the configured provider, or <code>null</code> if
    *         initialization failed.
    */
   private static DynamicParticipantSynchronizationProvider initializeProvider()
   {
      DynamicParticipantSynchronizationProvider provider = null;

      try
      {
         provider = ExtensionProviderUtils.getFirstExtensionProvider(
               DynamicParticipantSynchronizationProvider.class,
               SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY);
         if (null == provider)
         {
            if ( !isEmpty(Parameters.instance().getString(
                  SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY)))
            {
               trace.warn("Missing or invalid dynamic participant synchronization provider: "
                     + Parameters.instance().getString(
                           SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY));
            }
         }
      }
      catch (Exception e)
      {
         trace.warn(
               "Invalid dynamic participant synchronization provider configuration.", e);
      }

      return provider;
   }

   public static DynamicParticipantSynchronizationStrategy getSynchronizationStrategy()
   {
      ConcurrentMap<String, DynamicParticipantSynchronizationStrategy> syncStrategyCache = getSyncStrategyCache();

      IAuditTrailPartition currentPartition = SecurityProperties.getPartition();
      String partitionId = (null != currentPartition) ? currentPartition.getId() : GLOBAL;

      DynamicParticipantSynchronizationStrategy strategy = syncStrategyCache.get(partitionId);
      if (null == strategy)
      {
         syncStrategyCache.putIfAbsent(partitionId, initializeStrategy());
         strategy = syncStrategyCache.get(partitionId);
      }

      return strategy;
   }

   public static ConcurrentMap<String, DynamicParticipantSynchronizationStrategy> getSyncStrategyCache()
   {
      GlobalParameters globals = GlobalParameters.globals();
      ConcurrentMap<String, DynamicParticipantSynchronizationStrategy> syncProviderCache = (ConcurrentMap<String, DynamicParticipantSynchronizationStrategy>) globals.get(PRP_SYNC_PROVIDER_CACHE);
      if (null == syncProviderCache)
      {
         syncProviderCache = (ConcurrentMap<String, DynamicParticipantSynchronizationStrategy>) globals.getOrInitialize(
               PRP_SYNC_PROVIDER_CACHE, newConcurrentHashMap());
      }
      return syncProviderCache;
   }

   public static DynamicParticipantSynchronizationStrategy initializeStrategy()
   {
      DynamicParticipantSynchronizationStrategy strategy = null;

      try
      {
         strategy = ExtensionProviderUtils.getFirstExtensionProvider(
               DynamicParticipantSynchronizationStrategy.class,
               SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY);
         if (null == strategy)
         {
            if ( !isEmpty(Parameters.instance().getString(
                  SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY)))
            {
               trace.warn("Missing or invalid dynamic participant synchronization strategy: "
                     + Parameters.instance().getString(
                           SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_CLASS_PROPERTY));
            }
         }
      }
      catch (Exception e)
      {
         trace.warn(
               "Invalid dynamic participant synchronization strategy configuration.", e);
      }

      if (null == strategy)
      {
         strategy = new TimebasedSynchronizationStrategy();
      }

      return strategy;
   }

   protected Map getProperties()
   {
      return properties;
   }

   public static short getPartitionOid(Map properties)
   {
      short partitionOid = SecurityProperties.getPartitionOid();
      if (-1 == partitionOid)
      {
         IAuditTrailPartition partition = getPartition(properties);
         if (null != partition)
         {
            partitionOid = partition.getOID();
         }
      }
      return partitionOid;
   }

   public static IAuditTrailPartition getPartition(Map properties)
   {
      return getPartition(properties, true);
   }

   public static IAuditTrailPartition getPartition(Map properties, boolean mayBeCached)
   {
      IAuditTrailPartition partition = SecurityProperties.getPartition(mayBeCached);
      if (null == partition)
      {
         String partitionId = LoginUtils.getPartitionId(properties);
         partition = AuditTrailPartitionBean.findById(partitionId);
      }
      return partition;
   }

   /**
    * @author ubirkemeyer
    * @version $Revision$
    */
   public static class TransientAdministratorDecorator implements InvocationHandler
   {
      private final IUser delegate;

      public TransientAdministratorDecorator(IUser delegate)
      {
         this.delegate = delegate;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         if ("hasRole".equals(method.getName())
               && (1 == method.getParameterTypes().length)
               && String.class.equals(method.getParameterTypes()[0]))
         {
            return (PredefinedConstants.ADMINISTRATOR_ROLE.equals(args[0]))
                  ? Boolean.TRUE
                  : Boolean.FALSE;
         }
         else
         {
            return method.invoke(delegate, args);
         }
      }
   }

   private static class CreateUserAction implements Action
   {
      private final String realmId;
      private final String account;
      private final ExternalUserConfiguration userConf;
      private final Map properties;

      CreateUserAction(String realmId, String account,
            ExternalUserConfiguration userConf, Map properties)
      {
         this.realmId = realmId;
         this.account = account;
         this.userConf = userConf;
         this.properties = properties;
      }

      public Object execute()
      {
         short partitionOid = -1;
         UserRealmBean realm;
         try
         {
            partitionOid = getPartitionOid(properties);
            realm = UserRealmBean.findById(realmId, partitionOid);
         }
         catch (ObjectNotFoundException x)
         {
            realm = new UserRealmBean(realmId, realmId,
                  (AuditTrailPartitionBean) getPartition(properties, false));
            trace.info("New " + realm + " was created.");

            MonitoringUtils.partitionMonitors().userRealmCreated(realm);
         }

         IUser user = new UserBean(account, userConf.getFirstName(),
               userConf.getLastName(), realm);

         trace.info("New User " + user + " was created.");

         MonitoringUtils.partitionMonitors().userCreated(user);

         new NonisolatedCreateSyncService(properties)
               .synchronizeUnguarded(user, userConf);

         return Boolean.TRUE;
      }
   }

   private static class CreateUserGroupAction implements Action
   {
      private final String id;
      private final ExternalUserGroupConfiguration groupConf;
      private final Map properties;

      CreateUserGroupAction(String id, ExternalUserGroupConfiguration groupConf,
            Map properties)
      {
         this.id = id;
         this.groupConf = groupConf;
         this.properties = properties;
      }

      public Object execute()
      {
         IUserGroup group = new UserGroupBean(id, groupConf.getName(),
               (AuditTrailPartitionBean) getPartition(properties, false));

         trace.info("New User Group " + group + " was created.");

         new NonisolatedCreateSyncService(Collections.EMPTY_MAP).synchronizeUnguarded(
               group, groupConf);

         return Boolean.TRUE;
      }
   }

   private static class CreateDepartmentAction implements Action
   {
      private final String id;
      private final IDepartment parentDepartment;
      private final IOrganization org;

      public CreateDepartmentAction(final String id,
            final IDepartment parentDepartment, final IOrganization org)
      {
         this.id = id;
         this.parentDepartment = parentDepartment;
         this.org = org;
      }

      public Object execute()
      {
         final AuditTrailPartitionBean partition = (AuditTrailPartitionBean)
            SecurityProperties.getPartition(false);
         final ModelManager modelManager = ModelManagerFactory.getCurrent();
         final OrganizationInfo orgInfo = new OrganizationInfoDetails(
               modelManager.getRuntimeOid(org),
               org.getQualifiedId(), null, true, true, null);
         final DepartmentBean department = new DepartmentBean(id, id, partition,
               (DepartmentBean) parentDepartment, null, orgInfo);

         trace.info("New Department " + department + " was created.");

         return Boolean.TRUE;
      }
   }

   private static class IsolatedCreateSyncService extends SynchronizationService
   {
      public IsolatedCreateSyncService(Map properties)
      {
         super(properties);
      }

      protected void performCreateAction(Action createAction) throws Exception
      {
         ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance()
               .get(EngineProperties.FORKING_SERVICE_HOME);
         ForkingService service = null;
         try
         {
            service = factory.get();
            service.isolate(createAction);
         }
         finally
         {
            factory.release(service);
         }
      }
   }

   private static class NonisolatedCreateSyncService extends SynchronizationService
   {
      public NonisolatedCreateSyncService(Map properties)
      {
         super(properties);
      }

      protected void performCreateAction(Action createAction) throws Exception
      {
         createAction.execute();
      }
   }

   private static final class DepartmentSynchronizationException extends InternalException
   {
      private static final long serialVersionUID = -6175204231885854282L;

      public DepartmentSynchronizationException(final String message)
      {
         super(message);
      }
   }

   // TODO: Consider to remove that class and use ScopedModelParticipant instead.
   /**
    *
    * @author stephan.born
    *
    */
   private static final class ScopedParticipant extends
         Pair<IModelParticipant, IDepartment>
   {
      private static final long serialVersionUID = 1L;

      public static ScopedParticipant factory(IModelParticipant participant,
            IDepartment department)
      {
         return new ScopedParticipant(participant, department);
      }

      public ScopedParticipant(IModelParticipant participant, IDepartment department)
      {
         super(participant, department);
      }

      public IModelParticipant getParticipant()
      {
         return getFirst();
      }

      public IDepartment getDepartment()
      {
         return getSecond();
      }

      @Override
      public String toString()
      {
         return getParticipant() + " - " + getDepartment();
      }
   }
}