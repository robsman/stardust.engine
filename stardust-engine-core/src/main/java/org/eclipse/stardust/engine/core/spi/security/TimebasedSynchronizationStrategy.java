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
package org.eclipse.stardust.engine.core.spi.security;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.Flushable;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * Concrete implementation of the DynamicParticipantSynchronizationStrategy based on the
 * time lapsed since the last synchronization.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TimebasedSynchronizationStrategy
      extends DynamicParticipantSynchronizationStrategy implements Flushable
{
   private static final long DEFAULT_TIMEOUT_IN_SECONDS = 10;
   
   private static final String USER_SYNC_STAMPS = TimebasedSynchronizationStrategy.class.getName()
         + ".USER_SYNC_STAMPS";
   
   private static final String USER_GROUP_SYNC_STAMPS = TimebasedSynchronizationStrategy.class.getName()
         + ".USER_GROUP_SYNC_STAMPS";

   /**
    * Checks if this user needs to be synchronized.
    * 
    * @param user the user to be checked.
    * @return true if the time lapsed since the last synchronization
    *   is greater then the value defined in the
    *   Security.Authorization.TimebasedSynchronizationStrategy.UserSyncTimeout
    *   property.
    */
   public boolean isDirty(IUser user)
   {
      Map userSyncTimeStamps = getSyncTimeStamps(USER_SYNC_STAMPS);
      
      if (!userSyncTimeStamps.containsKey(user.getAccount()))
      {
         return true;
      }
      
      final long timeout = Parameters.instance().getLong(
            SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_USER_SYNC_TIMEOUT,
            DEFAULT_TIMEOUT_IN_SECONDS) * 1000;

      long lastSyncTime = ((Long) userSyncTimeStamps.get(user.getAccount())).longValue();

      return timeout < (TimestampProviderUtils.getTimeStampValue() - lastSyncTime);
   }

   /**
    * Callback method to inform the SynchronizationStrategy that the user have been
    * successfuly synchronized.<br>
    * This call will update the synchronization timestamp associated with this user.
    * 
    * @param user the user that has been synchronized.
    */
   public void setSynchronized(IUser user)
   {
      Map userSyncTimeStamps = getSyncTimeStamps(USER_SYNC_STAMPS);
      
      userSyncTimeStamps.put(user.getAccount(), TimestampProviderUtils.getTimeStampValue());
   }

   /**
    * Checks if this user group needs to be synchronized.
    * 
    * @param userGroup the user group to be checked.
    * @return true if the time lapsed since the last synchronization
    *   is greater then the value defined in the
    *   Security.Authorization.TimebasedSynchronizationStrategy.UserGroupSyncTimeout
    *   property.
    */
   public boolean isDirty(IUserGroup userGroup)
   {
      Map userGroupSyncTimeStamps = getSyncTimeStamps(USER_GROUP_SYNC_STAMPS);
      
      if (!userGroupSyncTimeStamps.containsKey(userGroup.getId()))
      {
         return true;
      }

      final long timeout = Parameters.instance().getLong(
            SecurityProperties.AUTHORIZATION_SYNC_STRATEGY_USER_GROUP_SYNC_TIMEOUT,
            DEFAULT_TIMEOUT_IN_SECONDS) * 1000;

      long lastSyncTime = ((Long) userGroupSyncTimeStamps.get(userGroup.getId())).longValue();

      return timeout < (TimestampProviderUtils.getTimeStampValue() - lastSyncTime);
   }

   /**
    * Callback method to inform the SynchronizationStrategy that the user group has been
    * successfully synchronized.<br>
    * This call will update the synchronization timestamp associated with this user group.
    *
    * @param userGroup
    *           the user group that has been synchronized.
    */
   public void setSynchronized(IUserGroup userGroup)
   {
      Map userGroupSyncTimeStamps = getSyncTimeStamps(USER_GROUP_SYNC_STAMPS);
      
      userGroupSyncTimeStamps
            .put(userGroup.getId(), TimestampProviderUtils.getTimeStampValue());
   }

   public synchronized void flush()
   {
      final Parameters parameters = Parameters.instance();
      
      parameters.set(USER_SYNC_STAMPS, new HashMap());
      parameters.set(USER_GROUP_SYNC_STAMPS, new HashMap());
   }

   private static Map getSyncTimeStamps(String tag)
   {
      // don't synchronize retrieval of map, as resulting probability of lost updates does
      // not yield much harm, will just synchronize once to much
      Map userSyncTimeStamps = (Map) Parameters.instance().get(tag);
      if (null == userSyncTimeStamps)
      {
         userSyncTimeStamps = new HashMap();
         Parameters.instance().set(tag, userSyncTimeStamps);
      }
      return userSyncTimeStamps;
   }
}
