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

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.MultiAttribute;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.security.HMAC;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PWHConstants;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils;
import org.eclipse.stardust.engine.api.runtime.UserPK;
import org.eclipse.stardust.engine.core.cache.CacheHelper;
import org.eclipse.stardust.engine.core.cache.CacheInputStream;
import org.eclipse.stardust.engine.core.cache.CacheOutputStream;
import org.eclipse.stardust.engine.core.cache.Cacheable;
import org.eclipse.stardust.engine.core.cache.UsersCache;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.PersistentVector;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;


/**
 *
 */
public class UserBean extends AttributedIdentifiablePersistentBean implements IUser, Serializable, Cacheable
{
   private static final long serialVersionUID = -1840799806267424871L;

   private static final Logger trace = LogManager.getLogger(UserBean.class);

   private static final int EXTENDED_STATE_PASSWORD_EXPIRED = 1;  // first bit
   private static final int EXTENDED_STATE_QUALITY_CODE_SIZE = 2; // 2nd bit

   public static final int EXTENDED_STATE_FLAG_DEPUTY_OF_PROP = 4; // 3rd bit

   @SuppressWarnings("unused")
   private static final int EXTENDED_STATE_FLAG_ALL = ~0; // all bits

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ACCOUNT = "account";
   public static final String FIELD__FIRST_NAME = "firstName";
   public static final String FIELD__LAST_NAME = "lastName";
   public static final String FIELD__PASSWORD = "password";
   public static final String FIELD__EMAIL = "eMail";
   public static final String FIELD__VALID_FROM = "validFrom";
   public static final String FIELD__VALID_TO = "validTo";
   public static final String FIELD__DESCRIPTION = "description";
   public static final String FIELD__FAILED_LOGIN_COUNT = "failedLoginCount";
   public static final String FIELD__LAST_LOGIN_TIME = "lastLoginTime";
   public static final String FIELD__REALM = "realm";
   public static final String FIELD__EXTENDED_STATE = "extendedState";

   public static final FieldRef FR__OID = new FieldRef(UserBean.class, FIELD__OID);
   public static final FieldRef FR__ACCOUNT = new FieldRef(UserBean.class, FIELD__ACCOUNT);
   public static final FieldRef FR__FIRST_NAME = new FieldRef(UserBean.class, FIELD__FIRST_NAME);
   public static final FieldRef FR__LAST_NAME = new FieldRef(UserBean.class, FIELD__LAST_NAME);
   public static final FieldRef FR__PASSWORD = new FieldRef(UserBean.class, FIELD__PASSWORD);
   public static final FieldRef FR__EMAIL = new FieldRef(UserBean.class, FIELD__EMAIL);
   public static final FieldRef FR__VALID_FROM = new FieldRef(UserBean.class, FIELD__VALID_FROM);
   public static final FieldRef FR__VALID_TO = new FieldRef(UserBean.class, FIELD__VALID_TO);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(UserBean.class, FIELD__DESCRIPTION);
   public static final FieldRef FR__FAILED_LOGIN_COUNT = new FieldRef(UserBean.class, FIELD__FAILED_LOGIN_COUNT);
   public static final FieldRef FR__LAST_LOGIN_TIME = new FieldRef(UserBean.class, FIELD__LAST_LOGIN_TIME);
   public static final FieldRef FR__REALM = new FieldRef(UserBean.class, FIELD__REALM);
   public static final FieldRef FR__EXTENDED_STATE = new FieldRef(UserBean.class, FIELD__EXTENDED_STATE);

   public static final String LINK__PARTICIPANT_LINKS = "participantLinks";
   public static final String LINK__USER_GROUP_LINKS = "userGroupLinks";

   public static final String TABLE_NAME = "workflowuser";
   public static final String DEFAULT_ALIAS = "u";
   public static final String PK_FIELD = FIELD__OID;
   protected static final String PK_SEQUENCE = "user_seq";
   public static final String[] workflowuser_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] workflowuser_idx2_UNIQUE_INDEX = new String[] {FIELD__ACCOUNT, FIELD__REALM};
   protected static final Class LOADER = UserLoader.class;

   private static final int account_COLUMN_LENGTH = 50;
   private String account;
   private static final int firstName_COLUMN_LENGTH = 150;
   private String firstName;
   private static final int lastName_COLUMN_LENGTH = 150;
   private String lastName;
   /** Stores the value of the users password hashed by <tt>org.eclipse.stardust.common.security.HMAC.hash()</tt>. */
   private String password;
   private static final int eMail_COLUMN_LENGTH = 150;
   private String eMail;
   private Date validFrom;
   private Date validTo;
   private String description;
   private long failedLoginCount;
   private Date lastLoginTime;
   private int extendedState;

   public PersistentVector participantLinks;
   static final String participantLinks_TABLE_NAME = UserParticipantLink.TABLE_NAME;
   static final String participantLinks_CLASS = UserParticipantLink.class.getName();
   static final String participantLinks_OTHER_ROLE = UserParticipantLink.FIELD__USER;
   static final String participantLinks_OWNED = "true";

   static final boolean password_SECRET = true;

   public PersistentVector userGroupLinks;
   static final String userGroupLinks_TABLE_NAME = UserUserGroupLink.TABLE_NAME;
   static final String userGroupLinks_CLASS = UserUserGroupLink.class.getName();
   static final String userGroupLinks_OTHER_ROLE = UserUserGroupLink.FIELD__USER;
   static final String userGroupLinks_OWNED = "true";

   private UserRealmBean realm;

   private transient Integer qualityAssurancePropability = null;
   private transient PropertyIndexHandler propIndexHandler = new PropertyIndexHandler();

   /**
    * Holds a cached version of profile
    */
   private transient Map cachedProfile = null;

   private transient Set<Long> grantsCache = null;

   public static long countActiveUsers()
   {
      return (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)).getCount(
            UserBean.class,
            QueryExtension.where(
                  Predicates.orTerm(
                        Predicates.greaterThan(FR__VALID_TO, System.currentTimeMillis()),
                        Predicates.isEqual(FR__VALID_TO, 0))));
   }

   public static IUser createTransientUser(String account, String firstName,
         String lastName, UserRealmBean realm)
   {
      IUser user = new UserBean();

      user.setOID(0);
      user.setAccount(account);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setRealm(realm);

      return user;
   }

   /**
    * @deprecated Superseded by {@link #findByOid(long)}
    */
   public static UserBean findByOID(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return findByOid(oid);
   }

   public static UserBean findByOid(long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_OID.raise(0), 0);
      }
      UserBean result = (UserBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findByOID(UserBean.class, oid);
      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_OID.raise(oid), oid);
      }
      return result;
   }

   public static UserBean findByAccount(String account, IUserRealm realm)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      UserBean result = null;
      if (CacheHelper.isCacheable(UserBean.class))
      {
         result = UsersCache.instance().findById(account, realm == null ? 0 : realm.getOID());
         if (result != null)
         {
            return result;
         }
      }

      result = (UserBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(UserBean.class,
                  QueryExtension.where(Predicates.andTerm(
                        Predicates.isEqual(FR__REALM, realm.getOID()),
                        Predicates.isEqual(FR__ACCOUNT, account))));

      if (result == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_UNKNOWN_USER_ID.raise(
               account, realm.getId()));
      }
      return result;
   }

   /**
    * @return A collection with all users being assigned to this participant.
    */
   public static List findAllForParticipant(IModelParticipant participant)
   {
      List result = CollectionUtils.newList();

      for (Iterator i = UserParticipantLink.findAllFor(participant); i.hasNext();)
      {
         result.add(((UserParticipantLink) i.next()).getUser());
      }

      for (Iterator i = participant.getAllParticipants(); i.hasNext();)
      {
         result.addAll(findAllForParticipant((IModelParticipant) i.next()));
      }

      return result;
   }

   public UserBean()
   {
      this.participantLinks = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .createPersistentVector();
      this.userGroupLinks = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .createPersistentVector();
   }

   public UserBean(String account, String firstName, String lastName, UserRealmBean realm)
   {
      this();
      ejbCreate(account, firstName, lastName, realm);
   }

   public UserPK getPK()
   {
      return new UserPK(getOID());
   }

   public void delete()
   {
   }

   public String toString()
   {
      return "User: " + getRealmQualifiedAccount() + " (" + lastName + ", " + firstName
            + ")";
   }

   public String getAccount()
   {
      fetch();
      return account;
   }

   public String getRealmQualifiedAccount()
   {
      return MessageFormat.format("{0} (Realm: {1})", new Object[] { getAccount(),
            getRealm().getId() });
   }

   public void setAccount(String account)
   {
      fetch();
      if (!CompareHelper.areEqual(this.account, account))
      {
         // TODO: remove secondary keys
         markModified(FIELD__ACCOUNT);
         this.account = StringUtils.cutString(account, account_COLUMN_LENGTH);
      }
   }

   public String getFirstName()
   {
      fetch();
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.firstName, firstName))
      {
         markModified(FIELD__FIRST_NAME);
         this.firstName = StringUtils.cutString(firstName, firstName_COLUMN_LENGTH);
      }
   }

   public String getLastName()
   {
      fetch();
      return lastName;
   }

   public void setLastName(String lastName)
   {
      if ( !CompareHelper.areEqual(this.lastName, lastName))
      {
         markModified(FIELD__LAST_NAME);
         this.lastName = StringUtils.cutString(lastName, lastName_COLUMN_LENGTH);
      }
   }

   public String getId()
   {
      return getAccount();
   }

   public void setId(String id)
   {
      setAccount(id);
   }

   /**
    *
    */
   public String getName()
   {
      return getLastName();
   }

   /**
    *
    */
   public void setName(String name)
   {
      setLastName(name);
   }

   /**
    *
    */
   public String getEMail()
   {
      fetch();
      return eMail;
   }

   /**
    *
    */
   public void setEMail(String eMail)
   {
      if ( !CompareHelper.areEqual(this.eMail, eMail))
      {
         markModified(FIELD__EMAIL);
         this.eMail = StringUtils.cutString(eMail, eMail_COLUMN_LENGTH);
      }
   }

   /**
    *
    */
   public Date getValidFrom()
   {
      fetch();
      return validFrom;
   }

   public void setValidFrom(Date validFrom)
   {
      if ( !CompareHelper.areEqual(this.validFrom, validFrom))
      {
         markModified(FIELD__VALID_FROM);
         this.validFrom = validFrom;
      }
   }

   public Date getValidTo()
   {
      fetch();
      return validTo;
   }

   public void setValidTo(Date validTo)
   {
      if (!CompareHelper.areEqual(this.validTo, validTo))
      {
         markModified(FIELD__VALID_TO);
         this.validTo = validTo;
      }
   }

   // @todo (france, ub): introduce an additional boolean field for temporary disabling
   public boolean isValid()
   {
      fetch();
      Date now = new Date();

      return ((validFrom == null) || (validFrom.getTime() <= now.getTime()))
            && ((validTo == null) || (validTo.getTime() > now.getTime()));
   }

   /**
    * Checks the password provided with <tt>password</tt> against the - possibly hashed
    * password of the user.
    */
   public boolean checkPassword(String password)
   {
      fetch();

      boolean encryptionOn = isPasswordEncryption();

      if (this.password == null)
      {
         //throw new InternalException("Stored password is null.");
         trace.warn("Stored password is null, provided password is " + password);
         return true;
      }

      if (!encryptionOn)
      {
         return this.password.equals(password);
      }
      else
      {
         try
         {
            HMAC hmac = new HMAC(HMAC.MD5);

            if(!hmac.isHashed(this.password))
            {
               setPassword(this.password);
            }

            return hmac.compare(getOID(), password, this.password);
         }
         catch (Exception x)
         {
            throw new InternalException(x);
         }
      }
   }

   /**
    *
    */
   public void setPassword(String password)
   {
      boolean encryptionOn = isPasswordEncryption();

      if ( !encryptionOn)
      {
         if ( !CompareHelper.areEqual(this.password, password))
         {
            markModified(FIELD__PASSWORD);
            this.password = password;
         }
      }
      else
      {
         try
         {
            HMAC hmac = new HMAC(HMAC.MD5);

            markModified(FIELD__PASSWORD);
            this.password = hmac.hashToString(getOID(), password);
         }
         catch (Exception x)
         {
            throw new InternalException(x);
         }
      }
   }

   /**
   *
   */
   public String getPassword()
   {
     fetch();
     return password;
   }

   private boolean isPasswordEncryption()
   {
      return Parameters.instance().getBoolean(SecurityUtils.PASSWORD_ENCRYPTION, false);
   }

   /**
    *
    */
   public String getDescription()
   {
      fetch();
      return description;
   }

   /**
    *
    */
   public void setDescription(String description)
   {
      if ( !CompareHelper.areEqual(this.description, description))
      {
         markModified(FIELD__DESCRIPTION);
         this.description = description;
      }
   }

   public void addToParticipants(IModelParticipant participant, IDepartment department)
   {
      addToParticipants(participant, department, 0);
   }

   public void addToParticipants(IModelParticipant participant, IDepartment department, long onBehalfOf)
   {
      fetch();

      int cardinality = participant.getCardinality();
      if (cardinality != Unknown.INT)
      {
         long count = UserParticipantLink.countAllFor(participant);
         if (count >= participant.getCardinality())
         {
            throw new PublicException(
                  BpmRuntimeError.BPMRT_CANNOT_ASSIGN_MORE_USERS_TO_PARTICIPANT_CARDINALITY_EXCEEDED
                        .raise(participant.getCardinality()));
         }
      }

      if (findParticipantLink(participant, department) != null)
      {
         return;
      }

      if (CacheHelper.isCacheable(UserBean.class))
      {
         // mark modified to trigger 2nd level cache update
         markModified();
      }

      UserParticipantLink link = new UserParticipantLink(this, participant, department, onBehalfOf);

      StringBuffer buffer = new StringBuffer();
      buffer.append("Granting ")//
            .append(participant)
            .append(" to ")
            .append(this);
      if (department != null)
      {
         buffer.append(" in ")
               .append(department);
      }
      if (onBehalfOf != 0)
      {
         buffer.append(" on behalf of ").append(onBehalfOf);
      }
      buffer.append(".");
      AuditTrailLogger.getInstance(LogCode.SECURITY, this).info(buffer.toString());

      this.grantsCache = null;

      participantLinks.add(link);

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(link);
   }

   private UserParticipantLink findParticipantLink(IModelParticipant participant, IDepartment department)
   {
      long participantOID = ModelManagerFactory.getCurrent().getRuntimeOid(participant);
      for (Iterator<UserParticipantLink> i = getAllParticipantLinks(); i.hasNext();)
      {
         UserParticipantLink link = i.next();
         IModelParticipant linkParticipant = link.getParticipant();
         if (linkParticipant != null)
         {
            long linkParticipantOid = ModelManagerFactory.getCurrent().getRuntimeOid(linkParticipant);
            if(participantOID == linkParticipantOid && CompareHelper.areEqual(department, link.getDepartment()))
            {
               return link;
            }
         }
      }
      return null;
   }

   public void removeFromParticipants(IModelParticipant participant, IDepartment department)
   {
      UserParticipantLink link = findParticipantLink(participant, department);

      if (link != null)
      {
         if (CacheHelper.isCacheable(UserBean.class))
         {
            // mark modified to trigger 2nd level cache update
            markModified();
         }

         StringBuffer buffer = new StringBuffer();
         buffer.append("Removing grant for ")//
               .append(participant)
               .append(" (model OID ")
               .append(participant.getModel().getModelOID())
               .append(") from ")
               .append(this);
         if (department != null)
         {
            buffer.append(" in ")
                  .append(department);
         }
         buffer.append(".");

         this.grantsCache = null;

         AuditTrailLogger.getInstance(LogCode.SECURITY, this).info(buffer.toString());
         link.delete();
      }

      fetchVector(LINK__PARTICIPANT_LINKS);
   }

   public void clearAllParticipants()
   {
      fetchVector(LINK__PARTICIPANT_LINKS);
      markModified();

      this.grantsCache = null;

      for (Iterator<UserParticipantLink> i = getAllParticipantLinks();i.hasNext();)
      {
         UserParticipantLink link = i.next();
         StringBuffer buffer = new StringBuffer();
         buffer.append("Removing grant for ")//
               .append(link.getParticipant())
               .append(" (model OID ")
               .append(link.getParticipant().getModel().getModelOID())
               .append(") from ")
               .append(this)
               .append(".");
         AuditTrailLogger.getInstance(LogCode.SECURITY, this).info(buffer.toString());
         link.delete();
      }
      participantLinks.clear();
   }

   public Iterator<UserParticipantLink> getAllParticipantLinks()
   {
      fetchVector(LINK__PARTICIPANT_LINKS);

      for (Iterator<UserParticipantLink> i = participantLinks.scan(); i.hasNext();)
      {
         UserParticipantLink participantLink = i.next();
         IModelParticipant participant = participantLink.getParticipant();
         if(participant == null)
         {
            participantLinks.remove(participantLink);
            trace.warn("ParticipantLink without Participant will be removed: " + participantLink.toString());
         }
      }

      return participantLinks.scan();
   }

   public Iterator<UserUserGroupLink> getAllUserGroupLinks()
   {
      fetchVector(LINK__USER_GROUP_LINKS);
      return userGroupLinks.scan();
   }

   public Iterator getAllParticipants()
   {
      Functor<UserParticipantLink, IModelParticipant> transformer = new Functor<UserParticipantLink, IModelParticipant>()
      {
         public IModelParticipant execute(UserParticipantLink source)
         {
            IModelParticipant result = source.getParticipant();
            if (result == null)
            {
               trace.warn("Dangling link for user '" + getOID()
                     + "', participantOID = '" + ((UserParticipantLink) source).getPersistenceController());
            }
            return result;
         }
      };
      Predicate<UserParticipantLink> predicate = new Predicate<UserParticipantLink>()
      {
         Set<IModelParticipant> visited = CollectionUtils.newHashSet();
         public boolean accept(UserParticipantLink source)
         {
            IModelParticipant participant = source.getParticipant();
            if (participant == null || visited.contains(participant))
            {
               return false;
            }
            visited.add(participant);
            return true;
         }
      };
      Iterator<UserParticipantLink> source = getAllParticipantLinks();
      return new TransformingIterator<UserParticipantLink, IModelParticipant>(source, transformer, predicate);
   }

   public Iterator<IRole> getAllRoles()
   {
      return new FilteringIterator(getAllParticipants(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return o instanceof IRole;
         }

      });
   }

   public Iterator<IOrganization> getAllOrganizations()
   {
      return new FilteringIterator(getAllParticipants(), new Predicate()
      {
         public boolean accept(Object o)
         {
            return o instanceof IOrganization;
         }

      });
   }

   public boolean hasRole(String roleId)
   {
      if (roleId == null)
      {
         return false;
      }

      for (Iterator i = getAllRoles();i.hasNext();)
      {
         IRole role = (IRole) i.next();
         if (role != null && roleId.equals(role.getQualifiedId()))
         {
            return true;
         }
      }
      return false;
   }

   public boolean hasGrant(IModelParticipant participant)
   {
      if (null == grantsCache)
      {
         // iterate over links once, building a transient cache of grants
         this.grantsCache = CollectionUtils.newHashSet();
         Iterator<UserParticipantLink> links = getAllParticipantLinks();
         while (links.hasNext())
         {
            grantsCache.add(links.next().getRuntimeParticipantOid());
         }
      }
      long runtimeOID = ModelManagerFactory.getCurrent().getRuntimeOid(participant);
      return grantsCache.contains(runtimeOID);
   }

   public Iterator getAllUserGroups(final boolean validOnly)
   {
      // TODO (sb): consider more performant solution using something like
      // return UserGroupBean.findForUser(getOID(), validOnly);
      return new TransformingIterator(getAllUserGroupLinks(),
            new Functor()
            {
               public Object execute(Object source)
               {
                  final UserUserGroupLink link = (UserUserGroupLink) source;
                  IUserGroup result = link.getUserGroup();
                  if (result == null)
                  {
                     trace.warn("Dangling link for user '" + getOID()
                           + "', userGroupOID = '" + link.getPersistenceController());
                  }
                  return result;
               }
            },
            new Predicate()
            {
               public boolean accept(Object o)
               {
                  boolean isAcceptable = true;

                  if (true == validOnly)
                  {
                     final UserUserGroupLink link = (UserUserGroupLink) o;
                     IUserGroup result = link.getUserGroup();

                     if (null == result || false == result.isValid())
                     {
                        isAcceptable = false;
                     }
                  }

                  return isAcceptable;
               }
            });
   }

   public Object getPrimaryKey()
   {
      return new UserPK(this);
   }

   public void ejbCreate(String account, String firstName, String lastName, UserRealmBean realm)
   {
      String trimmed_account = StringUtils.cutString(account, account_COLUMN_LENGTH);

      if (SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).exists(UserBean.class,
            QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(FR__REALM, realm.getOID()),//
                  Predicates.isEqual(FR__ACCOUNT, trimmed_account)))))
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_USER_WITH_ACCOUNT_ALREADY_EXISTS_IN_USER_REALM
                     .raise(trimmed_account, realm.getId()));
      }

      this.account = trimmed_account;
      this.firstName = StringUtils.cutString(firstName, firstName_COLUMN_LENGTH);
      this.lastName = StringUtils.cutString(lastName, lastName_COLUMN_LENGTH);

      this.realm = realm;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   /*
    *
    */
   public void ejbFindByPrimaryKey(String Account)
   {
      // @todo rsauer what about throwing ObjectNotFound

      //      RuntimeDatabaseContext.getCurrent().findByPrimaryKey(this, pk);
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).findFirst(UserBean.class,
            QueryExtension.where(Predicates.isEqual(FR__ACCOUNT, Account)));
   }

   /*
    *
    */
   public void ejbFindByPrimaryKey(UserPK pk)
   {
      // nothing to initialize
   }

   /*
    *
    */
   public long getTargetWfmsWorktime()
   {
      Float i = (Float) getPropertyValue(PWHConstants.TARGET_WFMS_WORKTIME);

      if (i != null)
      {
         return i.intValue();
      }
      return Unknown.LONG;

   }

   /*
    *
    */
   public long getTargetWorktime()
   {
      Integer i = (Integer) getPropertyValue(PWHConstants.TARGET_WORKTIME);

      if (i != null)
      {
         return i.intValue();
      }
      return Unknown.LONG;
   }

   /*
    *
    */
   public long getWorkingWeeks()
   {
      Integer i = (Integer) getPropertyValue(PWHConstants.WORKING_WEEKS);

      if (i != null)
      {
         return i.intValue();
      }
      return Unknown.LONG;
   }

   public void setFailedLoginCount(long count)
   {
      if (this.failedLoginCount != count)
      {
         markModified(FIELD__FAILED_LOGIN_COUNT);
         this.failedLoginCount = count;
      }
   }

   public void setLastLoginTime(Date time)
   {
      if ( !CompareHelper.areEqual(this.lastLoginTime, time))
      {
         markModified(FIELD__LAST_LOGIN_TIME);
         lastLoginTime = time;
      }
   }

   public Date getLastLoginTime()
   {
      fetch();
      return lastLoginTime;
   }

   public long getFailedLoginCount()
   {
      fetch();
      return failedLoginCount;
   }

   public boolean isAuthorizedForStarting(IProcessDefinition process)
   {
      ModelElementList triggers = process.getTriggers();
      for  (int i = 0; i < triggers.size(); i++)
      {
         ITrigger trigger = (org.eclipse.stardust.engine.api.model.ITrigger) triggers.get(i);

         if (trigger.getType().getId().equals(PredefinedConstants.MANUAL_TRIGGER))
         {
            String participantId = (String) trigger.getAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);
            IModelParticipant participant = ((IModel) process.getModel()).findParticipant(participantId);

            if (participant != null && participant.isAuthorized(this))
            {
               return true;
            }
         }
      }
      return false;
   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      return new UserProperty(getOID(), name, value);
   }

   public Class getPropertyImplementationClass()
   {
      return UserProperty.class;
   }

   protected Map getAllPropertiesFromAuditTrail()
   {
      Map propsWithNoScope = CollectionUtils.newHashMap();

      Map allProperties = super.getAllPropertiesFromAuditTrail();
      for (Iterator iterator = allProperties.entrySet().iterator(); iterator.hasNext();)
      {
         Map.Entry entry = (Map.Entry) iterator.next();
         Attribute rawProperty = (Attribute) entry.getValue();
         if (rawProperty instanceof MultiAttribute)
         {
            propsWithNoScope.put(entry.getKey(), rawProperty);
         }
         else {
            UserProperty property = (UserProperty) entry.getValue();
            if (StringUtils.isEmpty(property.getScope())) {
               propsWithNoScope.put(entry.getKey(), property);
            }
         }

      }

      return propsWithNoScope;
   }

   public IUserRealm getRealm()
   {
      fetchLink(FIELD__REALM);
      return realm;
   }

   public void setRealm(IUserRealm realm)
   {
      if (this.realm != realm)
      {
         markModified(FIELD__REALM);
         this.realm = (UserRealmBean) realm;
      }
   }

   public String getDomainId()
   {
      IUserDomain domain = SecurityProperties.getUserDomain();
      if (null != domain)
      {
         return domain.getId();
      }

      return null;
   }

   public long getDomainOid()
   {
      return SecurityProperties.getUserDomainOid();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.IUser#getProfile()
    */
   public Map getProfile()
   {
      if (null == cachedProfile)
      {
         cachedProfile = CollectionUtils.newHashMap();

         Map allProperties = super.getAllPropertiesFromAuditTrail();
         for (Iterator iterator = allProperties.entrySet().iterator(); iterator.hasNext();)
         {
            Map.Entry entry = (Map.Entry) iterator.next();
            UserProperty property = (UserProperty) entry.getValue();
            if (UserProperty.PROFILE_SCOPE.equals(property.getScope()))
            {
               cachedProfile.put(entry.getKey(), property);
            }
         }
      }

      return cachedProfile;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.IUser#setProfile(java.util.Map)
    */
   public void setProfile(Map profile)
   {
      cachedProfile = profile;
   }

   public boolean isPasswordExpired()
   {
      fetch();
      return (extendedState & EXTENDED_STATE_PASSWORD_EXPIRED) == EXTENDED_STATE_PASSWORD_EXPIRED;
   }

   public void setPasswordExpired(boolean expired)
   {
      if(expired)
      {
         extendedState = extendedState | EXTENDED_STATE_PASSWORD_EXPIRED;
      }
      else
      {
         extendedState = extendedState & ~EXTENDED_STATE_PASSWORD_EXPIRED;
      }
      markModified(FIELD__EXTENDED_STATE);
   }

   public void retrieve(byte[] bytes) throws IOException
   {
      CacheInputStream cis = new CacheInputStream(bytes);
      oid = cis.readLong();
      account = cis.readString();
      firstName = cis.readString();
      lastName = cis.readString();
      password = cis.readString();
      eMail = cis.readString();
      validFrom = cis.readDate();
      validTo = cis.readDate();
      description = cis.readString();
      failedLoginCount = cis.readLong();
      lastLoginTime = cis.readDate();

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      long realm = cis.readLong();
      if (realm >= 0)
      {
         this.realm = (UserRealmBean) session.
            findByOID(UserRealmBean.class, realm);
      }

      extendedState = cis.readInt();

      // read participantLinks
      int size = cis.readInt();
      for (int i = 0; i < size; i++)
      {
         long oid = cis.readLong();
         long participant = cis.readLong();
         long department = cis.readLong();
         if (session.existsInCache(UserParticipantLink.class, oid))
         {
            participantLinks.add(session.findByOID(UserParticipantLink.class, oid));
         }
         else
         {
            UserParticipantLink link = new UserParticipantLink(oid, this, participant, department);
            addController(session, oid, link);
            participantLinks.add(link);
         }
      }

      // read usergroupLinks
      size = cis.readInt();
      for (int i = 0; i < size; i++)
      {
         long oid = cis.readLong();
         long groupOid = cis.readLong();
         if (session.existsInCache(UserUserGroupLink.class, oid))
         {
            userGroupLinks.add(session.findByOID(UserUserGroupLink.class, oid));
         }
         else
         {
            IUserGroup group = groupOid > 0 ? UserGroupBean.findByOid(groupOid) : null;
            UserUserGroupLink link = new UserUserGroupLink(oid, this, group);
            addController(session, oid, link);
            userGroupLinks.add(link);
         }
      }

      // TODO: (fh) user properties

      cis.close();
   }

   private void addController(Session session, Long key, Persistent persistent)
   {
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;
         PersistenceController controller = persistent.getPersistenceController();
         if (controller == null)
         {
            DmlManager dmlManager = jdbcSession.getDMLManager(persistent.getClass());
            controller = dmlManager.createPersistenceController(jdbcSession, persistent);
         }
         jdbcSession.addToPersistenceControllers(key, controller);
      }
   }

   public byte[] store() throws IOException
   {
      fetch();
      CacheOutputStream cos = new CacheOutputStream();
      cos.writeLong(oid);
      cos.writeString(account);
      cos.writeString(firstName);
      cos.writeString(lastName);
      cos.writeString(password);
      cos.writeString(eMail);
      cos.writeDate(validFrom);
      cos.writeDate(validTo);
      cos.writeString(description);
      cos.writeLong(failedLoginCount);
      cos.writeDate(lastLoginTime);

      fetchLink(FIELD__REALM);
      cos.writeLong(realm == null ? 0 : realm.getOID());

      cos.writeInt(extendedState);

      // write participantLinks
      fetchVector(LINK__PARTICIPANT_LINKS);
      int size = participantLinks.size();
      cos.writeInt(size);
      Iterator<UserParticipantLink> itp = participantLinks.scan();
      for (int i = 0; i < size; i++)
      {
         UserParticipantLink link = itp.next();
         cos.writeLong(link.getOID());
         cos.writeLong(link.getRuntimeParticipantOid());
         cos.writeLong(link.getDepartmentOid());
      }

      // write userGroupLinks
      fetchVector(LINK__USER_GROUP_LINKS);
      size = userGroupLinks.size();
      cos.writeInt(size);
      Iterator<UserUserGroupLink> itg = userGroupLinks.scan();
      for (int i = 0; i < size; i++)
      {
         UserUserGroupLink link = itg.next();
         IUserGroup group = link.getUserGroup();
         cos.writeLong(link.getOID());
         cos.writeLong(group == null ? 0 : group.getOID());
      }

      // TODO: (fh) user properties

      cos.flush();
      byte[] bytes = cos.getBytes();
      cos.close();
      return bytes;
   }

   public void setQualityAssuranceProbability(Integer probability)
   {
      if(probability != null)
      {
         qualityAssurancePropability = probability;

         setPropertyValue(QualityAssuranceUtils.QUALITY_ASSURANCE_USER_PROBABILITY, probability);
      }
      else
      {
         qualityAssurancePropability = probability;

         removeProperty(QualityAssuranceUtils.QUALITY_ASSURANCE_USER_PROBABILITY);
      }
   }

   public Integer getQualityAssuranceProbability()
   {
      fetch();
      if((extendedState & EXTENDED_STATE_QUALITY_CODE_SIZE) == EXTENDED_STATE_QUALITY_CODE_SIZE)
      {
         Serializable value = getPropertyValue(QualityAssuranceUtils.QUALITY_ASSURANCE_USER_PROBABILITY);
         if(value instanceof Integer)
         {
            qualityAssurancePropability = (Integer) value;
         }
      }

      return qualityAssurancePropability;
   }

   private boolean isQualityAssuranceProbabilitySet()
   {
      Attribute property = (Attribute) getAllProperties().get(
            QualityAssuranceUtils.QUALITY_ASSURANCE_USER_PROBABILITY);
      return propertyExists(property);
   }

   private boolean isDeputyOfAny()
   {
      Attribute property = (Attribute) getAllProperties().get(UserUtils.IS_DEPUTY_OF);
      return propertyExists(property);
   }

   public boolean isPropertyAvailable(int pattern)
   {
      fetch();
      return (extendedState & pattern) == pattern ? true : false;
   }


   @Override
   protected String[] supportedMultiAttributes() {
      return new String[] { UserUtils.IS_DEPUTY_OF };
   }


   @Override
   public void addPropertyValues(Map attributes)
   {
      super.addPropertyValues(attributes);

      propIndexHandler.handleIndexForQAProperty(isQualityAssuranceProbabilitySet());
      propIndexHandler.handleIndexForDeputyOfProperty(isDeputyOfAny());
   }

   @Override
   public void setPropertyValue(String name, Serializable value)
   {
      super.setPropertyValue(name, value);

      propIndexHandler.handleIndexForQAProperty(isQualityAssuranceProbabilitySet());
      propIndexHandler.handleIndexForDeputyOfProperty(isDeputyOfAny());
   }

   @Override
   public void removeProperty(String name)
   {
      super.removeProperty(name);

      propIndexHandler.handleIndexForQAProperty(isQualityAssuranceProbabilitySet());
      propIndexHandler.handleIndexForDeputyOfProperty(isDeputyOfAny());
   }

   @Override
   public void removeProperty(String name, Serializable value)
   {
      if (UserUtils.IS_DEPUTY_OF.equals(name) && value instanceof Long)
      {
         if (UserUtils.isDeputyOfAny(this))
         {
            List<Attribute> existing = (List<Attribute>) getPropertyValue(UserUtils.IS_DEPUTY_OF);
            if (existing != null)
            {
               long originalUserOid = (Long) value;
               for (Iterator<Attribute> attributes = existing.iterator(); attributes.hasNext();)
               {
                  Attribute attribute = attributes.next();
                  String attributeValue = (String) attribute.getValue();
                  DeputyBean db = DeputyBean.fromString(attributeValue);
                  if (db.user == originalUserOid)
                  {
                     super.removeProperty(name, attributeValue);
                     break;
                  }
               }
            }
         }
      }
      else
      {
         super.removeProperty(name, value);
      }

      propIndexHandler.handleIndexForQAProperty(isQualityAssuranceProbabilitySet());
      propIndexHandler.handleIndexForDeputyOfProperty(isDeputyOfAny());
   }

   private class PropertyIndexHandler
   {
      public void handleIndexForQAProperty(boolean qualityAssuranceAvailable)
      {
         if (qualityAssuranceAvailable)
         {
            markPropertyAsAvailable(EXTENDED_STATE_QUALITY_CODE_SIZE);
         }
         else
         {
            unmarkPropertyAsAvailable(EXTENDED_STATE_QUALITY_CODE_SIZE);
         }
      }

      public void handleIndexForDeputyOfProperty(boolean isDeputyOfAny)
      {
         if (isDeputyOfAny)
         {
            markPropertyAsAvailable(EXTENDED_STATE_FLAG_DEPUTY_OF_PROP);
         }
         else
         {
            unmarkPropertyAsAvailable(EXTENDED_STATE_FLAG_DEPUTY_OF_PROP);
         }
      }

      private void markPropertyAsAvailable(int pattern)
      {
         fetch();

         int tempPropsAvailable = extendedState | pattern;
         if (extendedState != tempPropsAvailable)
         {
            extendedState = tempPropsAvailable;
            markModified(FIELD__EXTENDED_STATE);
         }
      }

      private void unmarkPropertyAsAvailable(int pattern)
      {
         fetch();

         int tempPropsAvailable = extendedState & ~pattern;
         if (extendedState != tempPropsAvailable)
         {
            extendedState = tempPropsAvailable;
            markModified(FIELD__EXTENDED_STATE);
         }
      }
   }
}