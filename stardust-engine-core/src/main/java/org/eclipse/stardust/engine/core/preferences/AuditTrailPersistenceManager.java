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
package org.eclipse.stardust.engine.core.preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.runtime.beans.PreferencesBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



public class AuditTrailPersistenceManager implements IPreferencesPersistenceManager
{
   public static final String SYSTEM_FOLDER = DocumentRepositoryFolderNames.SYSTEM_FOLDER;

   public static final String PARTITIONS_FOLDER = DocumentRepositoryFolderNames.PARTITIONS_FOLDER;

   public static final String REALMS_FOLDER = DocumentRepositoryFolderNames.REALMS_FOLDER;

   public static final String USERS_FOLDER = DocumentRepositoryFolderNames.USERS_FOLDER;

   public static final String PREFS_FOLDER = DocumentRepositoryFolderNames.PREFS_FOLDER;

   public AuditTrailPersistenceManager()
   {
   }

   public Preferences loadPreferences(IUser user, PreferenceScope scope, //
         String moduleId, String preferencesId, //
         IPreferencesReader loader)
   {
      final long oid;
      if (PreferenceScope.USER.equals(scope))
      {
         if (user == null)
         {
            throw new PublicException(
                  BpmRuntimeError.PREF_NO_USER_SPECIFIED_PREFSCOPE_USER_AND_REALM_NOT_AVAILABLE
                        .raise());
         }
         oid = user.getOID();
      }
      else if (PreferenceScope.REALM.equals(scope))
      {
         if (user == null)
         {
            throw new PublicException(
                  BpmRuntimeError.PREF_NO_USER_SPECIFIED_PREFSCOPE_USER_AND_REALM_NOT_AVAILABLE
                        .raise());
         }
         oid = user.getRealm().getOID();
      }
      else if (PreferenceScope.PARTITION.equals(scope))
      {
         IAuditTrailPartition partition = SecurityProperties.getPartition();

         if (partition == null)
         {
            throw new PublicException(
                  BpmRuntimeError.PREF_NO_CURRENT_PARTITION_FOUND.raise());
         }
         oid = partition.getOID();
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.PREF_AUDITTRAIL_PERSISTENCE_NOT_SUPPORTED_FOR_PREFERENCESSCOPE
                     .raise(scope));
      }

      PreferencesBean preferencesBean = PreferencesBean.find(oid, scope.name(),
            moduleId, preferencesId);
      Preferences preferences = getPreferences(scope, moduleId, preferencesId, loader,
            preferencesBean);
      setPreferencesOwnOrigin(preferences);
      return preferences;
   }

   public Preferences loadPreferences(PreferenceScope scope, //
         String moduleId, String preferencesId, //
         IPreferencesReader loader)
   {
      IUser user = SecurityProperties.getUser();
      return loadPreferences(user, scope, moduleId, preferencesId, loader);
   }

   private Preferences getPreferences(PreferenceScope scope, String moduleId,
         String preferencesId, IPreferencesReader loader, PreferencesBean preferencesBean)
   {
      Map loadedPreferences = null;
      if (preferencesBean != null)
      {
         final String stringValue = preferencesBean.getStringValue();
         byte[] fileContent = stringValue == null ? null : stringValue.getBytes();

         if ((null != fileContent) && (0 < fileContent.length))
         {
            InputStream isFileContent = new ByteArrayInputStream(fileContent);
            try
            {
               loadedPreferences = loader.readPreferences(isFileContent);
            }
            catch (IOException ioe)
            {
               // TODO
            }
            finally
            {
               try
               {
                  isFileContent.close();
               }
               catch (IOException ioe)
               {
                  // ignore
               }
            }
         }
      }

      if (loadedPreferences == null)
      {
         loadedPreferences = CollectionUtils.newHashMap();
      }

      Preferences preferences = new Preferences(scope, moduleId, preferencesId,
            loadedPreferences);

      return preferences;
   }

   private void setPreferencesOwnOrigin(Preferences preferences)
   {
      String partitionId = SecurityProperties.getPartition().getId();
      IUser user = SecurityProperties.getUser();
      if (PreferenceScope.PARTITION.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
      }
      else if (PreferenceScope.REALM.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
         if (user != null)
         {
            preferences.setRealmId(user.getRealm().getId());
         }
      }
      else if (PreferenceScope.USER.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
         if (user != null)
         {
            preferences.setRealmId(user.getRealm().getId());
            preferences.setUserId(user.getId());
         }
      }
   }

   public void updatePreferences(Preferences preferences, IPreferencesWriter writer)
   {
      final IUser currentUser = SecurityProperties.getUser();

      String partitionId = SecurityProperties.getPartition().getId();
      PreferenceScope scope = preferences.getScope();
      String realmId = currentUser == null ? null : currentUser.getRealm().getId();
      String userId = currentUser == null ? null : currentUser.getId();

      if (preferences.getRealmId() != null && !preferences.getRealmId().equals(realmId))
      {
         realmId = preferences.getRealmId();
      }
      if (preferences.getUserId() != null && !preferences.getUserId().equals(userId))
      {
         userId = preferences.getUserId();
      }

      long oid;
      final short partitionOid = getPartitionOid(partitionId);
      if (PreferenceScope.USER.equals(scope))
      {
         if (userId == null)
         {
            throw new PublicException(
                  BpmRuntimeError.PREF_NO_USER_SPECIFIED_PREFSCOPE_USER_AND_REALM_NOT_AVAILABLE
                        .raise());
         }

         final UserBean userBean = UserBean.findByAccount(userId, getRealmBean(realmId,
               partitionOid));

         if (userBean != null)
         {
            oid = userBean.getOID();
         }
         else
         {
            throw new ObjectNotFoundException(BpmRuntimeError.ATDB_UNKNOWN_USER_ID.raise(
                  userId, realmId));
         }
      }
      else if (PreferenceScope.REALM.equals(scope))
      {
         if (realmId == null)
         {
            throw new PublicException(
                  BpmRuntimeError.PREF_NO_USER_SPECIFIED_PREFSCOPE_USER_AND_REALM_NOT_AVAILABLE
                        .raise());
         }

         UserRealmBean realmBean = getRealmBean(realmId, partitionOid);

         if (realmBean != null)
         {
            oid = realmBean.getOID();
         }
         else
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.ATDB_UNKNOWN_USER_REALM_ID.raise(realmId, new Long(
                        partitionOid)));
         }
      }
      else if (PreferenceScope.PARTITION.equals(scope))
      {
         oid = partitionOid;
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.PREF_AUDITTRAIL_PERSISTENCE_NOT_SUPPORTED_FOR_PREFERENCESSCOPE
                     .raise(scope));
      }

      byte[] content = writePreferencesContent(preferences, writer);

      String stringValue = new String(content);

      PreferencesBean preferencesBean = PreferencesBean.find(oid, scope.name(),
            preferences.getModuleId(), preferences.getPreferencesId());

      if (preferencesBean != null)
      {
         preferencesBean.setStringValue(stringValue);
      }
      else
      {
         preferencesBean = new PreferencesBean(oid, scope.name(),
               preferences.getModuleId(), preferences.getPreferencesId(), partitionOid, stringValue);
      }

   }

   private byte[] writePreferencesContent(Preferences preferences,
         IPreferencesWriter writer)
   {
      ByteArrayOutputStream baosPrefsContent = new ByteArrayOutputStream();

      byte[] content;
      try
      {
         writer.writePreferences(baosPrefsContent, preferences.getModuleId(),
               preferences.getPreferencesId(), preferences.getPreferences());
         content = baosPrefsContent.toByteArray();
      }
      catch (IOException ioe)
      {
         // TODO
         content = null;
         throw new PublicException(ioe);
      }
      finally
      {
         try
         {
            baosPrefsContent.close();
         }
         catch (IOException ioe)
         {
            // ignore
         }
      }

      return content;
   }

   private UserRealmBean getRealmBean(String realmId, short partitionOid)
   {
      final UserRealmBean userRealmBean = UserRealmBean.findById(realmId, partitionOid);
      if (userRealmBean != null)
      {
         return userRealmBean;
      }
      else
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_USER_REALM_ID.raise(realmId, new Long(
                     partitionOid)));
      }
   }

   private short getPartitionOid(String partitionId)
   {
      final AuditTrailPartitionBean partitionBean = AuditTrailPartitionBean.findById(partitionId);
      if (partitionBean != null)
      {
         return partitionBean.getOID();
      }
      else
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PARTITION_ID.raise(partitionId), partitionId);
      }
   }

   public List<Preferences> getAllPreferences(ParsedPreferenceQuery eval,
         IPreferencesReader xmlPreferenceReader)
   {
      if (PreferenceScope.DEFAULT.equals(eval.getScope()))
      {
         throw new PublicException(
               BpmRuntimeError.PREF_AUDITTRAIL_PERSISTENCE_NOT_SUPPORTED_FOR_PREFERENCESSCOPE
                     .raise(eval.getScope()));
      }
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      List<Preferences> prefList = CollectionUtils.newLinkedList();

      final String realmId = eval.getRealmId() == null ? "%" : eval.getRealmId();
      final String userId = eval.getUserId() == null ? "%" : eval.getUserId();
      final String moduleId = eval.getModuleId() == null ? "%" : eval.getModuleId();
      final String preferencesId = eval.getPreferencesId() == null ? "%" : eval
            .getPreferencesId();

      List<Long> ownerOidList = fetchOwnerOids(session, eval.getScope(), realmId, userId);

      if ( !ownerOidList.isEmpty())
      {
         Iterator<PreferencesBean> iterator = session.getIterator(PreferencesBean.class,
               QueryExtension.where(Predicates.andTerm(//
                     Predicates.inList(PreferencesBean.FR__OWNER_ID, ownerOidList),//
                     Predicates.isEqual(PreferencesBean.FR__OWNER_TYPE, eval.getScope()
                           .name()),//
                     Predicates.isLike(PreferencesBean.FR__MODULE_ID,
                           moduleId.replace("*", "%")),//
                     Predicates.isLike(PreferencesBean.FR__PREFERENCES_ID,
                           preferencesId.replace("*", "%")))));

         while (iterator.hasNext())
         {
            PreferencesBean preferencesBean = iterator.next();

            Preferences preferences = getPreferences(eval.getScope(),
                  preferencesBean.getModuleId(), preferencesBean.getPreferencesId(),
                  xmlPreferenceReader, preferencesBean);
            setPreferencesOrigin(preferences, preferencesBean);
            prefList.add(preferences);
         }
      }
      return prefList;
   }

   private void setPreferencesOrigin(Preferences preferences,
         PreferencesBean preferencesBean)
   {
      String partitionId = SecurityProperties.getPartition().getId();
      if (PreferenceScope.PARTITION.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
      }
      else if (PreferenceScope.REALM.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
         IUserRealm realm = UserRealmBean.findByOID(preferencesBean.getOwnerId());
         if (realm != null)
         {
            preferences.setRealmId(realm.getId());
         }
      }
      else if (PreferenceScope.USER.equals(preferences.getScope()))
      {
         preferences.setPartitionId(partitionId);
         IUser user = UserBean.findByOid(preferencesBean.getOwnerId());
         if (user != null)
         {
            preferences.setRealmId(user.getRealm().getId());
            preferences.setUserId(user.getId());
         }
      }

   }

   private List<Long> fetchOwnerOids(Session session, PreferenceScope scope,
         String realmId, String userId)
   {
      List<Long> oidList = CollectionUtils.newLinkedList();
      if (PreferenceScope.USER.equals(scope))
      {
         List realmOidList = fetchMatchingRealmOids(session, realmId);

         fetchMatchingUserOids(oidList, session, userId, realmOidList);
      }
      else if (PreferenceScope.REALM.equals(scope))
      {
         oidList = fetchMatchingRealmOids(session, realmId);
      }
      else if (PreferenceScope.PARTITION.equals(scope))
      {
         oidList.add(Long.valueOf(SecurityProperties.getPartitionOid()));
      }
      return oidList;
   }

   private void fetchMatchingUserOids(List<Long> targetOidList, Session session,
         String userId, List realmOidList)
   {
      if (realmOidList.isEmpty())
      {
         return;
      }

      Iterator<UserBean> userBeanIterator = session.getIterator(UserBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isLike(UserBean.FR__ACCOUNT, userId.replace("*", "%")),
                  Predicates.inList(UserBean.FR__REALM, realmOidList))));

      while (userBeanIterator.hasNext())
      {
         UserBean userBean = userBeanIterator.next();
         targetOidList.add(userBean.getOID());
      }
   }

   private List fetchMatchingRealmOids(Session session, String realmId)
   {
      List realmOidList = CollectionUtils.newLinkedList();

      Iterator<UserRealmBean> realmBeanIterator = session.getIterator(
            UserRealmBean.class, QueryExtension.where(Predicates.andTerm(
                  Predicates.isLike(UserRealmBean.FR__ID, realmId.replace("*", "%")),
                  Predicates.isEqual(UserRealmBean.FR__PARTITION,
                        SecurityProperties.getPartitionOid()))));

      while (realmBeanIterator.hasNext())
      {
         UserRealmBean userRealmBean = realmBeanIterator.next();
         realmOidList.add(userRealmBean.getOID());
      }
      return realmOidList;
   }
}
