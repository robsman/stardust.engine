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

import java.util.*;

import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.security.utils.SecurityUtils;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.dto.UserDetails.AddedGrant;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


public final class UserUtils
{
   public static final List<String> PROTECTED_ATTRIBUTES = Arrays.asList(new String[] {
         SecurityUtils.LAST_PASSWORDS
   });

   /**
    * The given user is tried to be matched with one of the IDs (Accounts) or OIDs
    * given in the comma separated list in userIdSpec. Numeric values are interpreted as
    * user OIDs, string values as user IDs (Accounts). In order to match any user the
    * special userIdSpec "*" can be provided.
    * 
    * @param user the user
    * @param userIdSpec comma separated list of user IDs (Accounts) and OIDs. 
    * @return true if the user matches one of the IDs or OIDs, otherwise false. 
    */
   public static boolean isUserMatchingIdSpec(IUser user, String userIdSpec)
   {
      boolean result = false;

      if ("*".equals(userIdSpec))
      {
         result = true;
      }
      else
      {
         for (Iterator userIds = StringUtils.split(userIdSpec, ","); userIds.hasNext();)
         {
            String userId = ((String) userIds.next()).trim();
            long oid = -1;
            try
            {
               oid = Long.parseLong(userId);

               if (oid == user.getOID())
               {
                  result = true;
                  break;
               }
            }
            catch (NumberFormatException e)
            {
               if (CompareHelper.areEqual(userId, user.getAccount()))
               {
                  result = true;
                  break;
               }
            }
         }
      }

      return result;
   }
   
   public static boolean isUserGrantOrGroupModified(User user)
   {
      if (UserDetailsLevel.Full.equals(user.getDetailsLevel()) &&
            SecurityProperties.isInternalAuthorization())
      {
         return grantsChanged(user) || groupsChanged(user);
      }
      return false;
   }

   private static boolean grantsChanged(User user)
   {
      List<GrantKey> newGrants = CollectionUtils.newList();
      Collection<AddedGrant> addedGrants = ((UserDetails) user).getNewGrants();
      Iterator<AddedGrant> gIter = addedGrants.iterator();
      while (gIter.hasNext())
      {
         AddedGrant addedGrant = gIter.next();
         newGrants.add(new GrantKey(addedGrant));
      }
      
      List<GrantKey> existingGrants = CollectionUtils.newList();
      List<Grant> grants = user.getAllGrants();
      for (int i = 0; i < grants.size(); i++)
      {
         existingGrants.add(new GrantKey(grants.get(i)));
      }
      
      List<GrantKey> oldGrants = CollectionUtils.newList(existingGrants);
      oldGrants.removeAll(newGrants);
      newGrants.removeAll(existingGrants);
      
      return newGrants.size() != oldGrants.size() ||
            newGrants.size() > 0 || oldGrants.size() > 0;
   }

   private static boolean groupsChanged(User user)
   {
      List groups = CollectionUtils.newArrayList(user.getAllGroups());
      List newGroupIds = CollectionUtils.newArrayList(
            ((UserDetails) user).getNewGroupIds());
      if (groups.size() != newGroupIds.size())
      {
         return true;
      }

      for (int i = 0; i < groups.size(); i++)
      {
         UserGroup ug = (UserGroup) groups.get(i);
         groups.set(i, ug.getId());
      }
      List<String> oldGroups = CollectionUtils.newArrayList(groups);
      groups.removeAll(newGroupIds);
      newGroupIds.removeAll(oldGroups);
      return groups.size() != newGroupIds.size() ||
            groups.size() > 0 || newGroupIds.size() > 0;
   }
   
   public static boolean isUserDataModified(User user)
   {
      boolean userDetailsChanged = false;
      IUser orgUser = UserBean.findByOid(user.getOID());
      // check basic fields
      if (!CompareHelper.areEqual(user.getAccount(), orgUser.getAccount())
            || !CompareHelper.areEqual(user.getEMail(), orgUser.getEMail())
            || !CompareHelper.areEqual(user.getFirstName(), orgUser.getFirstName())
            || !CompareHelper.areEqual(user.getLastName(), orgUser.getLastName())
            || !CompareHelper.areEqual(user.getDescription(), orgUser.getDescription())
            || !CompareHelper.areEqual(user.getValidFrom(), orgUser.getValidFrom())
            || !CompareHelper.areEqual(user.getValidTo(), orgUser.getValidTo())
            || (((UserDetails)user).getPassword() != null 
                  && !orgUser.checkPassword(((UserDetails)user).getPassword())))
      {
         userDetailsChanged = true;
      }
      // check properties 
      if (!userDetailsChanged 
            && (UserDetailsLevel.Full.equals(user.getDetailsLevel())
                  || UserDetailsLevel.WithProperties.equals(user.getDetailsLevel())))
      {
         Map untouchedProps = CollectionUtils.newMap();
         collectProperties(orgUser, untouchedProps);
         userDetailsChanged = !filterNullValues(user.getAllProperties()).equals(untouchedProps);
      }
      return userDetailsChanged;
   }

   private static Map<String, Object> filterNullValues(Map<String, Object> allProperties)
   {
      Map<String, Object> filtered = CollectionUtils.newMap();
      for (Map.Entry<String, Object> entry : allProperties.entrySet())
      {
         Object value = entry.getValue();
         if (value != null)
         {
            filtered.put(entry.getKey(), value);
         }
      }
      return filtered;
   }

   public static void collectProperties(IUser theUser, Map<String, Object> theProperties)
   {
      for (Iterator i = theUser.getAllProperties().values().iterator(); i.hasNext();)
      {
         Attribute property = (Attribute) i.next();
         Object value = property.getValue();
         if (value != null)
         {
            String name = property.getName();
            if (!PROTECTED_ATTRIBUTES.contains(name))
            {
               theProperties.put(name, value);                  
            }
         }
      }
   }
   
   private static class GrantKey
   {
      private String id;
      private long dptmtOid;

      public GrantKey(AddedGrant grant)
      {
         set(grant.getQualifiedId(), grant.getDepartment());
      }
      
      public GrantKey(Grant grant)
      {
         set(grant.getQualifiedId(), grant.getDepartment());
      }

      private void set(String grant, DepartmentInfo dptmt)
      {
         id = grant;
         dptmtOid = dptmt == null ? 0 : dptmt.getOID();
      }

      @Override
      public int hashCode()
      {
         int result = 1;
         result = 31 * result + (int) (dptmtOid ^ (dptmtOid >>> 32));
         result = 31 * result + ((id == null) ? 0 : id.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         GrantKey other = (GrantKey) obj;
         return dptmtOid == other.dptmtOid && CompareHelper.areEqual(id, other.id);
      }

      @Override
      public String toString()
      {
         return "GrantKey [id=" + id + ", dptmtOid=" + dptmtOid + "]";
      }
   }

   private UserUtils() {}
}
