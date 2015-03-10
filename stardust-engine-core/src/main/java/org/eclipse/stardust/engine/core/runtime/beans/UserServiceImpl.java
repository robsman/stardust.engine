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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.dto.UserGroupDetailsLevel;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.security.utils.PasswordGenerator;
import org.eclipse.stardust.engine.core.security.utils.PasswordValidation;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class UserServiceImpl implements UserService, Serializable
{
   private static final long serialVersionUID = 2L;

   private static final Logger trace = LogManager.getLogger(UserServiceImpl.class);

   public String startSession(String clientId)
   {
      // no tracking on archives
      if (Parameters.instance().getBoolean(Constants.CARNOT_ARCHIVE_AUDITTRAIL, false))
      {
         return UserService.ARCHIVE;
      }

      // no tracking for configured users
      IUser currentUser = SecurityProperties.getUser();
      if (SessionManager.isUserSessionTrackingDisabled(currentUser))
      {
         return UserService.DISABLED_FOR_USER;
      }

      // TODO optionally prevent multiple session for same clientId

      // TODO check for limit on concurrent sessions

      IUserSession session = new UserSessionBean(SecurityProperties.getUser(), clientId);

      // TODO finalize decision on encoding
      String sessionId = Long.toHexString(session.getOID());

      // TODO crypt session ID

      return sessionId;
   }

   public void closeSession(String sessionId)
   {
      // no tracking on archives
      if (UserService.ARCHIVE.equals(sessionId))
      {
         return;
      }

      // no tracking for configured users
      if (UserService.DISABLED_FOR_USER.equals(sessionId))
      {
         return;
      }

      // TODO Auto-generated method stub

      // TODO decrypt

      long sessionOid;
      try
      {
         sessionOid = Long.decode("0x" + sessionId).longValue();
      }
      catch (NumberFormatException nfe)
      {
         // TODO
         sessionOid = 0;
      }

      try
      {
         IUserSession session = UserSessionBean.findByOid(sessionOid);

         final Date now = TimestampProviderUtils.getTimeStamp();
         session.setLastModificationTime(now);
         session.setExpirationTime(now);
      }
      catch (ObjectNotFoundException ex)
      {
         trace.warn("Unknown session: " + sessionOid);
      }
   }

   public boolean isTeamLeader(IUser user)
   {
      return SecurityProperties.isTeamLeader(user);
   }

   /**
    * @deprecated Superseded by {@link #isInternalAuthentication()} due to bad wording.
    */
   public boolean isInternalAuthentified()
   {
      return isInternalAuthentication();
   }

   public boolean isInternalAuthentication()
   {
      return SecurityProperties.isInternalAuthentication();
   }

   public boolean isInternalAuthorization()
   {
      return SecurityProperties.isInternalAuthorization();
   }

   public IUser internalGetUser(String realm, String account) throws ObjectNotFoundException
   {
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(SecurityProperties.REALM, realm);
      properties.put(SecurityProperties.DOMAIN, SecurityProperties.getUserDomain().getId());
      properties.put(SecurityProperties.PARTITION, SecurityProperties.getPartition().getId());

      return SynchronizationService.synchronize(account,
            getModel(),
            Parameters.instance().getBoolean(SecurityProperties.AUTHORIZATION_SYNC_ADMIN_PROPERTY, true),
            properties);
   }

   public User getUser()
   {
      return (User) DetailsFactory.create(SecurityProperties.getUser(),
            IUser.class, UserDetails.class);
   }

   public User modifyLoginUser(String oldPassword, String firstName,
         String lastName, String newPassword, String eMail)
   {
      checkInternalAuthentified();

      IUser user = SecurityProperties.getUser();

      user.lock();

      if (!user.checkPassword(oldPassword))
      {
         throw new PublicException(BpmRuntimeError.AUTHx_CHANGE_PASSWORD_OLD_PW_VERIFICATION_FAILED.raise());
      }

      if (newPassword == null)
      {
         throw new PublicException(BpmRuntimeError.AUTHx_CHANGE_PASSWORD_NEW_PW_MISSING.raise());
      }

      try
      {
         PasswordValidation.validate(newPassword.toCharArray(),
               SecurityUtils.getPasswordRules(SecurityProperties.getPartitionOid()),
               SecurityUtils.getPasswordRules(SecurityProperties.getPartitionOid()) != null ? SecurityUtils.getPreviousPasswords(user, oldPassword) : null);
      }
      catch (InvalidPasswordException e)
      {
         throw new InvalidPasswordException(
               BpmRuntimeError.AUTHx_CHANGE_PASSWORD_NEW_PW_VERIFICATION_FAILED.raise(),
               e.getFailureCodes());
      }

      user.setAccount(user.getAccount());
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setDescription(user.getDescription());

      if (newPassword != null)
      {
         user.setPassword(newPassword);
         SecurityUtils.changePassword(user, oldPassword, newPassword);
      }

      // we must reset the flag
      user.setPasswordExpired(false);

      user.setEMail(eMail);

      return (User) DetailsFactory.create(SecurityProperties.getUser(), IUser.class, UserDetails.class);
   }

   public User modifyUser(User changes)
   {
      return modifyUser(changes, false);
   }

   public User modifyUser(User changes, boolean generatePassword)
   {
      if ( !isFullyInitialized(changes))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_OPERATION_FAILED_USER_OID_NOT_FULLY_INITIALIZED
                     .raise(changes.getOID()));
      }

      if ( !isInternalAuthentication() && !isInternalAuthorization())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_OPERATION_FAILED_REQUIRES_INTERNAL_AUTH.raise());
      }

      UserBean user = UserBean.findByOid(changes.getOID());

      user.lock();

      if (isInternalAuthentication())
      {
         if(isTeamLeader(user) || SecurityProperties.getUser().hasRole(PredefinedConstants.ADMINISTRATOR_ROLE));
         {
            user.setQualityAssuranceProbability(changes.getQualityAssuranceProbability());
         }

         user.setQualityAssuranceProbability(changes.getQualityAssuranceProbability());

         String previousPassword = user.getPassword();
         String newPassword = null;

         if (generatePassword)
         {
            PasswordRules rules = SecurityUtils.getPasswordRules(SecurityProperties.getPartitionOid());
            List<String> history = SecurityUtils.getPreviousPasswords(user, previousPassword);
            newPassword = new String(PasswordGenerator.generatePassword(rules, history));
         }
         else
         {
            newPassword = ((UserDetails) changes).getPassword();
         }

         if (newPassword != null)
         {
            try
            {
               PasswordRules rules = SecurityUtils.getPasswordRules(SecurityProperties.getPartitionOid());
               List<String> history = SecurityUtils.getPreviousPasswords(user, previousPassword);
               PasswordValidation.validate(newPassword.toCharArray(), rules, history);
            }
            catch (InvalidPasswordException e)
            {
               throw new InvalidPasswordException(
                     BpmRuntimeError.AUTHx_CHANGE_PASSWORD_NEW_PW_VERIFICATION_FAILED
                           .raise(),
                     e.getFailureCodes());
            }
         }

         user.setAccount(changes.getAccount());
         user.setFirstName(changes.getFirstName());
         user.setLastName(changes.getLastName());
         user.setDescription(changes.getDescription());

         if(generatePassword)
         {
            user.setPassword(newPassword);
            SecurityUtils.publishGeneratedPassword(user, newPassword);

            user.setPasswordExpired(true);
            SecurityUtils.changePassword(user, previousPassword, newPassword);
         }
         else if(newPassword != null)
         {
            user.setPassword(newPassword);
            SecurityUtils.changePassword(user, previousPassword, newPassword);
         }

         user.setEMail(changes.getEMail());
         user.setValidFrom(changes.getValidFrom());

         // user is disabled
         if(user.isPasswordExpired() && SecurityUtils.isUserInvalid(user))
         {
            SecurityUtils.generatePassword(user);
         }
         user.setValidTo(changes.getValidTo());
      }

      if (isInternalAuthorization())
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();

         Collection<UserDetails.AddedGrant> newGrants = ((UserDetails) changes).getNewGrants();
         for (UserDetails.AddedGrant grant : newGrants)
         {
            QName qualifiedId = QName.valueOf(grant.getQualifiedId());

            ProcessInstanceGroupUtils.assertNotCasePerformer(grant.getQualifiedId());

            DepartmentInfo departmentInfo = grant.getDepartment();
            IDepartment department = departmentInfo == null || departmentInfo == Department.DEFAULT
                  ? null : DepartmentBean.findByOID(departmentInfo.getOID());
            for (Iterator j = modelManager.getAllModels(); j.hasNext();)
            {
               IModel model = (IModel) j.next();
               if (StringUtils.isEmpty(qualifiedId.getNamespaceURI()) || CompareHelper.areEqual(model.getId(), qualifiedId.getNamespaceURI()))
               {
                  IModelParticipant participant = model.findParticipant(qualifiedId.getLocalPart());
                  if (participant != null)
                  {
                     addToParticipants(modelManager, user, participant, department);
                     // (fh) since the grants are model agnostic, we add only the
                     // first matching participant, respecting the constraints associated with it.
                     break;
                  }
               }
            }
         }

         for (Iterator<UserParticipantLink> i = user.getAllParticipantLinks(); i.hasNext();)
         {
            UserParticipantLink grant = i.next();
            IDepartment department = grant.getDepartment();
            boolean match = false;
            for (Iterator j = newGrants.iterator(); j.hasNext();)
            {
               UserDetails.AddedGrant newGrant = (UserDetails.AddedGrant) j.next();
               QName qualifiedNewGrantId = QName.valueOf(newGrant.getQualifiedId());
               String grantParticipantId;
               if(StringUtils.isEmpty(qualifiedNewGrantId.getNamespaceURI()))
               {
                  grantParticipantId = grant.getParticipant().getId();
               }
               else
               {
                  grantParticipantId = ModelUtils.getQualifiedId(grant.getParticipant());
               }


               if (qualifiedNewGrantId.toString().equals(grantParticipantId)
                     && areEqual(department, newGrant.getDepartment()))
               {
                  match = true;
                  break;
               }
            }
            if (!match)
            {
               user.removeFromParticipants(grant.getParticipant(), department);
            }
         }

         for (Iterator i = changes.getAllProperties().entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            user.setPropertyValue((String) entry.getKey(), (Serializable) entry.getValue());
         }

         final Collection newGroupIds = ((UserDetails) changes).getNewGroupIds();
         for (Iterator i = newGroupIds.iterator(); i.hasNext();)
         {
            IUserGroup group = null;

            group = UserGroupBean.findById((String) i.next(), SecurityProperties
                  .getPartitionOid());
            group.addUser(user);
         }

         for (Iterator i = user.getAllUserGroups(false); i.hasNext();)
         {
            IUserGroup oldGroup = (IUserGroup) i.next();
            if (false == newGroupIds.contains(oldGroup.getId()))
            {
               oldGroup.removeUser(user);
            }
         }
      }

      return (User) DetailsFactory.create(user, IUser.class, UserDetails.class);
   }

   public void generatePasswordResetToken(String realm, String account)
   {
      IUserRealm userRealm = SecurityProperties.getUserRealm();
      if(!StringUtils.isEmpty(realm))
      {
         IAuditTrailPartition partition = SecurityProperties.getPartition();

         try
         {
            userRealm = UserRealmBean.findById(realm, partition.getOID());
         }
         catch (ObjectNotFoundException e)
         {
         }
      }
      IUser user = UserBean.findByAccount(account, userRealm);
      SecurityUtils.generatePasswordResetToken(user);
   }

   public void resetPassword(String account, Map properties, String token) throws ConcurrencyException,
         ObjectNotFoundException, IllegalOperationException
   {
      if ( !isInternalAuthentication())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_OPERATION_FAILED_REQUIRES_INTERNAL_AUTH.raise());
      }

      IModel model = ModelManagerFactory.getCurrent().findActiveModel();
      if (model == null)
      {
         model = ModelManagerFactory.getCurrent().findLastDeployedModel();
      }
      boolean allowNewAccount = true;

      IUser user = SynchronizationService.synchronize(account, model, allowNewAccount, properties);

      if ( !user.isValid())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_EXP_ACCOUNT_EXPIRED.raise(user
                     .getRealmQualifiedAccount()));
      }

      user.lock();

      if (isInternalAuthentication())
      {
         SecurityUtils.generatePassword(user, token);
      }
   }

   private void addToParticipants(ModelManager manager, UserBean user, IModelParticipant participant,
         IDepartment department)
   {
      if (department != null)
      {
         IOrganization parent = (IOrganization) manager.findModelParticipant(
               participant.getModel().getModelOID(), department.getRuntimeOrganizationOID());
         if (participant != parent && !DepartmentUtils.isChild(participant, parent))
         {
            throw new IllegalOperationException(
                  BpmRuntimeError.AUTHx_AUTH_INVALID_GRANT.raise(participant.getId(),
                        department.getOID()));
         }
      }
      user.addToParticipants(participant, department);
   }

   private boolean areEqual(IDepartment departmentBean, DepartmentInfo department)
   {
      if (department == Department.DEFAULT)
      {
         department = null;
      }
      return departmentBean == null
         ? department == null
         : department != null && departmentBean.getOID() == department.getOID();
   }

   public User createUser(String account, String firstName, String lastName,
         String description, String password, String eMail,
         Date validFrom, Date validTo)
   {
      String realm = SecurityProperties.getUserRealm().getId();

      return createUser(realm, account, firstName, lastName, description, password,
            eMail, validFrom, validTo);
   }

   public User createUser(String realm, String account, String firstName,
         String lastName, String description, String password, String eMail,
         Date validFrom, Date validTo)
   {
      if (StringUtils.isEmpty(realm))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("realm"));
      }
      if (StringUtils.isEmpty(account))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("account"));
      }
      if (StringUtils.isEmpty(firstName))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("firstName"));
      }
      if (StringUtils.isEmpty(lastName))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("lastName"));
      }
      if (StringUtils.isEmpty(password))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("password"));
      }

      checkInternalAuthentified();

      IAuditTrailPartition partition = SecurityProperties.getPartition();

      try
      {
         IUserRealm realmBean = UserRealmBean.findById(realm, partition.getOID());
         UserBean.findByAccount(account, realmBean);
         throw new UserExistsException(account, realm);
      }
      catch (ObjectNotFoundException e)
      {
      }

      try
      {
         PasswordValidation.validate(password.toCharArray(),
               SecurityUtils.getPasswordRules(partition.getOID()), null);
      }
      catch (InvalidPasswordException e)
      {
         throw new InvalidPasswordException(
               BpmRuntimeError.AUTHx_CHANGE_PASSWORD_NEW_PW_VERIFICATION_FAILED.raise(),
               e.getFailureCodes());
      }

      UserBean user = new UserBean(account, firstName, lastName, UserRealmBean.findById(
            realm, partition.getOID()));

      user.setDescription(description);
      user.setPassword(password);
      SecurityUtils.updatePasswordHistory(user, password);
      user.setEMail(eMail);
      user.setValidFrom(validFrom);
      user.setValidTo(validTo);

      trace.info("Created user '" + user.getRealmQualifiedAccount() + "', oid = "
            + user.getOID());

      MonitoringUtils.partitionMonitors().userCreated(user);

      return (User) DetailsFactory.create(user, IUser.class, UserDetails.class);
   }

   public User getUser(String account) throws ObjectNotFoundException,
         IllegalOperationException
   {
      String realm = SecurityProperties.getUserRealm().getId();
      return getUser(realm, account);
   }

   public User getUser(String realm, String account) throws ObjectNotFoundException
   {
      IUser user = internalGetUser(realm, account);
      return (User) DetailsFactory.create(user, IUser.class, UserDetails.class);
   }

   public User getUser(long oid) throws ObjectNotFoundException
   {
      IUser user = UserBean.findByOid(oid);

      if ( !isInternalAuthentication())
      {
         // TODO (sb): is it neccessary to synchronize? It's already done by UserLoader.
         SynchronizationService.synchronize(user);
      }

      return (User) DetailsFactory.create(user, IUser.class, UserDetails.class);
   }

   public User invalidate(String account)
   {
      return invalidateUser(account);
   }

   public void changeUserPassword(String oldPassword, String newPassword)
   {
      IUser user = SecurityProperties.getUser();
      if ( !user.checkPassword(oldPassword))
      {
         throw new PublicException(
               BpmRuntimeError.AUTHx_CHANGE_PASSWORD_OLD_PW_VERIFICATION_FAILED.raise());
      }

      try
      {
         PasswordValidation.validate(newPassword.toCharArray(),
               SecurityUtils.getPasswordRules(SecurityProperties.getPartitionOid()),
               SecurityUtils.getPasswordRules(SecurityProperties.getPartitionOid()) != null ? SecurityUtils.getPreviousPasswords(user, oldPassword) : null);
      }
      catch (InvalidPasswordException e)
      {
         throw new InvalidPasswordException(
               BpmRuntimeError.AUTHx_CHANGE_PASSWORD_NEW_PW_VERIFICATION_FAILED.raise(),
               e.getFailureCodes());
      }

      user.setPassword(newPassword);
      SecurityUtils.changePassword(user, oldPassword, newPassword);
   }

   public IModel getModel()
   {
      return ModelManagerFactory.getCurrent().findActiveModel();
   }

   private void checkInternalAuthentified()
   {
      if ( !isInternalAuthentication())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_OPERATION_FAILED_REQUIRES_INTERNAL_AUTH.raise());
      }
   }

   public User invalidateUser(String account) throws ObjectNotFoundException,
         IllegalOperationException
   {
      String realm = SecurityProperties.getUserRealm().getId();
      return invalidateUser(realm, account);
   }

   public User invalidateUser(String realm, String account)
         throws ObjectNotFoundException, IllegalOperationException
   {
      checkInternalAuthentified();

      IUserRealm realmBean = UserRealmBean.findById(realm, SecurityProperties
            .getPartition().getOID());
      IUser user = UserBean.findByAccount(account, realmBean);
      user.lock();

      user.setValidTo(TimestampProviderUtils.getTimeStamp());
      user.clearAllParticipants();

      MonitoringUtils.partitionMonitors().userDisabled(user);

      return (User) DetailsFactory.create(user, IUser.class, UserDetails.class);
   }

   public UserGroup createUserGroup(String id, String name, String description,
         Date validFrom, Date validTo) throws UserGroupExistsException,
         IllegalOperationException, InvalidArgumentException
   {
      if (StringUtils.isEmpty(id))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("id","empty"));
      }
      if (StringUtils.isEmpty(name))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("name","empty"));
      }
      if (description == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("description","null"));
      }

      try
      {
         UserGroupBean.findById(id, SecurityProperties.getPartitionOid());
         throw new UserGroupExistsException(id);
      }
      catch (ObjectNotFoundException e)
      {
      }

      UserGroupBean userGroup = new UserGroupBean(id, name,
            (AuditTrailPartitionBean) SecurityProperties.getPartition(false));

      userGroup.setDescription(description);
      userGroup.setValidFrom(validFrom);
      userGroup.setValidTo(validTo);

      trace.info("Created user group '" + id + "', oid = " + userGroup.getOID());

      return (UserGroup) DetailsFactory.create(userGroup, IUserGroup.class,
            UserGroupDetails.class);
   }

   public UserGroup modifyUserGroup(UserGroup changes) throws ConcurrencyException,
         ObjectNotFoundException, IllegalOperationException
   {
      checkInternalAuthentified();

      if ( !isFullyInitialized(changes))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_OPERATION_FAILED_USER_GROUP_OID_NOT_FULLY_INITIALIZED
                     .raise(changes.getOID()));
      }

      UserGroupBean userGroup = UserGroupBean.findByOid(changes.getOID());

      userGroup.lock();

      userGroup.setId(changes.getId());
      userGroup.setName(changes.getName());
      userGroup.setDescription(changes.getDescription());
      userGroup.setValidFrom(changes.getValidFrom());
      userGroup.setValidTo(changes.getValidTo());

      for (Iterator i = changes.getAllAttributes().entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         userGroup.setPropertyValue((String) entry.getKey(), (Serializable) entry
               .getValue());
      }

      return (UserGroup) DetailsFactory.create(userGroup, IUserGroup.class,
            UserGroupDetails.class);
   }

   public UserGroup getUserGroup(String id) throws ObjectNotFoundException
   {
      IUserGroup userGroup = SynchronizationService.synchronizeUserGroup(id);

      return (UserGroup) DetailsFactory.create(userGroup, IUserGroup.class,
            UserGroupDetails.class);
   }

   public UserGroup getUserGroup(long userGroupOid) throws ObjectNotFoundException
   {
      IUserGroup userGroup = UserGroupBean.findByOid(userGroupOid);

      if ( !isInternalAuthentication())
      {
         // TODO (sb): is it neccessary to synchronize? It's already done by
         // UserGroupLoader.
         SynchronizationService.synchronize(userGroup);
      }

      return (UserGroup) DetailsFactory.create(userGroup, IUserGroup.class,
            UserGroupDetails.class);
   }

   public UserGroup invalidateUserGroup(String id) throws ConcurrencyException,
         ObjectNotFoundException, IllegalOperationException
   {
      checkInternalAuthentified();

      IUserGroup userGroup = UserGroupBean.findById(id, SecurityProperties
            .getPartitionOid());
      userGroup.lock();

      userGroup.setValidTo(TimestampProviderUtils.getTimeStamp());

      return (UserGroup) DetailsFactory.create(userGroup, IUserGroup.class,
            UserGroupDetails.class);
   }

   public UserGroup invalidateUserGroup(long userGroupOid) throws ConcurrencyException,
         ObjectNotFoundException, IllegalOperationException
   {
      checkInternalAuthentified();

      IUserGroup userGroup = UserGroupBean.findByOid(userGroupOid);
      userGroup.lock();

      userGroup.setValidTo(TimestampProviderUtils.getTimeStamp());

      return (UserGroup) DetailsFactory.create(userGroup, IUserGroup.class,
            UserGroupDetails.class);
   }

   public UserRealm createUserRealm(String id, String name, String description)
         throws UserRealmExistsException, IllegalOperationException
   {
      try
      {
         short partitionOid = SecurityProperties.getPartition().getOID();
         UserRealmBean.findById(id, partitionOid);
         throw new UserRealmExistsException(id);
      }
      catch (ObjectNotFoundException e)
      {
      }

      UserRealmBean userRealm = new UserRealmBean(id, name,
            (AuditTrailPartitionBean) SecurityProperties.getPartition(false));
      userRealm.setDescription(description);

      trace.info(MessageFormat.format("Created user realm ''{0}'', oid = {1}.",
            new Object[] {id, new Long(userRealm.getOID())}));

      MonitoringUtils.partitionMonitors().userRealmCreated(userRealm);

      return (UserRealm) DetailsFactory.create(userRealm, IUserRealm.class,
            UserRealmDetails.class);
   }

   public void dropUserRealm(String id) throws ConcurrencyException,
         ObjectNotFoundException, IllegalOperationException
   {
      short partitionOid = SecurityProperties.getPartition().getOID();
      UserRealmBean userRealm = UserRealmBean.findById(id, partitionOid);
      long oid = userRealm.getOID();

      Persistent persistent = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(UserBean.class,
                  QueryExtension.where(Predicates.isEqual(UserBean.FR__REALM, oid)));
      if (null != persistent)
      {
         throw new IllegalOperationException(
               BpmRuntimeError.ATDB_DELETION_FAILED_USER_REALM_ID_DANGLING_REFERENCE
                     .raise(id));
      }

      userRealm.delete();

      trace.info(MessageFormat.format("Dropped user realm ''{0}'', oid = {1}.",
            new Object[] {id, new Long(oid)}));

      MonitoringUtils.partitionMonitors().userRealmDropped(userRealm);
   }

   public List getUserRealms() throws ConcurrencyException, IllegalOperationException
   {
      List userRealms = new ArrayList();
      short partitionOid = SecurityProperties.getPartition().getOID();

      Iterator iter = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            UserRealmBean.class, QueryExtension.where(//
                  Predicates.isEqual(//
                        UserRealmBean.FR__PARTITION, partitionOid)));

      while (iter.hasNext())
      {
         userRealms.add(DetailsFactory.create(iter.next(), IUserRealm.class,
               UserRealmDetails.class));
      }

      return Collections.unmodifiableList(userRealms);
   }

   /**
    * @param changes
    * @return
    */
   private boolean isFullyInitialized(User changes)
   {
      return UserDetailsLevel.Full == changes.getDetailsLevel();
   }

   /**
    * @param changes
    * @return
    */
   private boolean isFullyInitialized(UserGroup changes)
   {
      return UserGroupDetailsLevel.Full == changes.getDetailsLevel();
   }

   @Override
   public Deputy addDeputy(UserInfo user, UserInfo deputyUser, DeputyOptions options)
   {
      if (user.getOID() != deputyUser.getOID())
      {
         if (options == null)
         {
            options = DeputyOptions.DEFAULT;
         }
         UserBean userBean = UserBean.findByOid(user.getOID());
         UserBean deputyUserBean = UserBean.findByOid(deputyUser.getOID());

         UserUtils.removeExistingDeputy(user.getOID(), deputyUserBean);

         DeputyBean db = new DeputyBean(userBean.getOID(), options.getFromDate(),
               options.getToDate(), options.getParticipants());

         deputyUserBean.setPropertyValue(UserUtils.IS_DEPUTY_OF, db.toString());

         UserUtils.updateDeputyGrants(deputyUserBean);

         return db.createDeputyDetails(deputyUser);
      }
      else
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.ATDB_DEPUTY_SELF_REFERENCE_NOT_ALLOWED.raise(user.getOID()));
      }
   }

   @Override
   public Deputy modifyDeputy(UserInfo user, UserInfo deputyUser, DeputyOptions options)
   {
      if (options == null)
      {
         options = DeputyOptions.DEFAULT;
      }

      List<Deputy> deputies = getUsersBeingDeputyFor(deputyUser);
      for (Deputy deputy : deputies)
      {
         if (deputy.getUser().equals(user))
         {
            removeDeputy(user, deputyUser);
            return addDeputy(user, deputyUser, options);
         }
      }

      throw new ObjectNotFoundException(
            BpmRuntimeError.ATDB_DEPUTY_DOES_NOT_EXISTS.raise(deputyUser.getOID(),
                  user.getOID()));
   }

   @Override
   public void removeDeputy(UserInfo user, UserInfo deputyUser)
   {
      UserBean deputyUserBean = UserBean.findByOid(deputyUser.getOID());
      UserUtils.removeExistingDeputy(user.getOID(), deputyUserBean);

      UserUtils.updateDeputyGrants(deputyUserBean);
   }

   @Override
   public List<Deputy> getDeputies(UserInfo user)
   {
      UserBean userBean = UserBean.findByOid(user.getOID());

      String likeOpPattern = MessageFormat.format(
            UserUtils.IS_DEPUTY_OF_PROP_PREFIX_PATTERN,
            new Object[] {Long.valueOf(userBean.getOID()).toString()});

      ResultIterator<UserProperty> iterator = SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).getIterator(
            UserProperty.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(UserProperty.FR__NAME, UserUtils.IS_DEPUTY_OF),
                  Predicates.isLike(UserProperty.FR__STRING_VALUE, likeOpPattern))));

      Map<Long, Deputy> deputyMap = CollectionUtils.newHashMap();
      while (iterator.hasNext())
      {
         UserProperty userProperty = iterator.next();

         final long deputyUserOid = userProperty.getObjectOID();
         if ( !deputyMap.containsKey(deputyUserOid))
         {
            String stringValue = (String) userProperty.getValue();
            DeputyBean deputyBean = DeputyBean.fromString(stringValue);

            UserInfo deputyUserInfo = DetailsFactory.create(UserBean.findByOid(deputyUserOid));

            deputyMap.put(deputyUserOid, deputyBean.createDeputyDetails(deputyUserInfo));
         }

      }

      return CollectionUtils.newArrayList(deputyMap.values());

   }

   @Override
   public List<Deputy> getUsersBeingDeputyFor(UserInfo deputyUser)
   {
      List<Deputy> result = CollectionUtils.newArrayList();

      UserBean deputyUserBean = UserBean.findByOid(deputyUser.getOID());
      List<DeputyBean> deputies = UserUtils.getDeputies(deputyUserBean);

      for (DeputyBean deputy : deputies)
      {
         result.add(deputy.createDeputyDetails(deputyUser));
      }

      return result;
   }
}