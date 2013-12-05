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

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.dto.UserDetails.AddedGrant;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.persistence.ClosableIterator;
import org.eclipse.stardust.engine.core.runtime.beans.DeputyBean.GrantBean;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;


public final class UserUtils
{
   public static final String IS_DEPUTY_OF = "Infinity.Deputy.IsDeputyOf";
   public static final String IS_DEPUTY_OF_PROP_PREFIX_PATTERN = "<d><u>{0}</u>%";
   
   public static final List<String> PROTECTED_ATTRIBUTES = Arrays.asList(new String[] {
         SecurityUtils.LAST_PASSWORDS, 
         QualityAssuranceUtils.QUALITY_ASSURANCE_USER_PROBABILITY,
         IS_DEPUTY_OF
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
   
   public static boolean isAuthorized(IUser user, Predicate<IModelParticipant> predicate)
   {
      Set<IModelParticipant> visited = CollectionUtils.newHashSet();
      Iterator<UserParticipantLink> source = user.getAllParticipantLinks();
      while (source.hasNext())
      {
         UserParticipantLink link = source.next();
         IModelParticipant participant = link.getParticipant();
         if ( !visited.contains(participant))
         {
            if (predicate.accept(participant))
            {
               setOnBehalfOf(link.getOnBehalfOf());
               return true;
            }
            visited.add(participant);
         }
      }
      return false;
   }

   public static boolean isOnBehalfOf()
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      return rtEnv == null ? false : rtEnv.getAuthorizedOnBehalfOf() != 0;
   }

   public static void clearOnBehalfOf()
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      if (rtEnv != null)
      {
         rtEnv.setAuthorizedOnBehalfOf(0);
      }
   }

   public static void setOnBehalfOf(long onBehalfOf)
   {
      if (onBehalfOf != 0)
      {
         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         if (rtEnv != null)
         {
            rtEnv.setAuthorizedOnBehalfOf(onBehalfOf);
         }
      }
   }

   public static long getOnBehalfOf()
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      return rtEnv == null ? 0 : rtEnv.getAuthorizedOnBehalfOf();
   }      
   
   static void removeExistingDeputy(long oid, IUser deputyUser)
   {
      deputyUser.removeProperty(IS_DEPUTY_OF, Long.valueOf(oid));
   }

   public static void updateDeputyGrants(IUser user)
   {
      Map<Long, Map<Long, Set<Long>>> participantMap = getParticipantsFromDeputees(user);

      if (participantMap == null)
      {
         Iterator<UserParticipantLink> links = user.getAllParticipantLinks();
         while (links.hasNext())
         {
            UserParticipantLink link = links.next();
            if (link.getOnBehalfOf() != 0)
            {
               user.removeFromParticipants(link.getParticipant(), link.getDepartment());
            }
         }
      }
      else
      {
         // filter out user's own grants and already existing grants
         Iterator<UserParticipantLink> links = user.getAllParticipantLinks();
         while (links.hasNext())
         {
            UserParticipantLink link = links.next();
            long runtimeParticipantOid = link.getRuntimeParticipantOid();
            long departmentOid = link.getDepartmentOid();
            long onBehalfOf = link.getOnBehalfOf();
            Map<Long, Set<Long>> departmentMap = participantMap.get(runtimeParticipantOid);
            Set<Long> users = departmentMap == null
                  ? null
                  : departmentMap.get(departmentOid);
            if (onBehalfOf == 0)
            {
               // user has it's own grant, remove all inherited grants
               if (departmentMap != null)
               {
                  departmentMap.remove(departmentOid);
               }
            }
            else
            {
               // this inherited grant is no longer available, remove it
               if (users == null || !users.remove(onBehalfOf))
               {
                  user.removeFromParticipants(link.getParticipant(), link.getDepartment());
               }
               if (users != null && users.isEmpty())
               {
                  departmentMap.remove(departmentOid);
               }
            }
            if (departmentMap != null && departmentMap.isEmpty())
            {
               participantMap.remove(runtimeParticipantOid);
            }
         }

         // now add grants left in the participant map, if any
         if ( !participantMap.isEmpty())
         {
            for (long runtimeParticipantOid : participantMap.keySet())
            {
               Map<Long, Set<Long>> departmentMap = participantMap.get(runtimeParticipantOid);
               for (long departmentOid : departmentMap.keySet())
               {
                  Set<Long> users = departmentMap.get(departmentOid);
                  // create a grant only for the first user
                  IModelParticipant participant = ModelManagerFactory.getCurrent()
                        .findModelParticipant(PredefinedConstants.ANY_MODEL,
                              runtimeParticipantOid);
                  IDepartment department = departmentOid == 0
                        ? null
                        : DepartmentBean.findByOID(departmentOid);
                  user.addToParticipants(participant, department, users.iterator().next());
               }
            }
         }
      }
   }

   private static Map<Long/*participant*/, Map<Long/*department*/, Set<Long/*onBehalfOf*/>>> getParticipantsFromDeputees(
         IUser user)
   {
      
      Map<Long/*user*/, Map<Long/*participant*/, Set<Long/*department*/>>> others = getUpdatedUserOids(user);
      if (others == null || others.isEmpty())
      {
         return null;
      }

      Map<Long/*participant*/, Map<Long/*department*/, Set<Long/*onBehalfOf*/>>> participantMap = CollectionUtils.newMap();
      
      int i = 0;
      long[] oids = new long[others.size()];
      for (Long oid : others.keySet())
      {
         oids[i++ ] = oid;
      }
      // TODO: performance issue - this is fetching users too and we don't need that, only
      // user oids
      ClosableIterator<UserParticipantLink> itr = UserParticipantLink.findForUsers(oids);
      try
      {
         while (itr.hasNext())
         {
            UserParticipantLink link = itr.next();
            long participantRuntimeOid = link.getRuntimeParticipantOid();
            long departmentOid = link.getDepartmentOid();
            Map<Long/* participant */, Set<Long/* department */>> allowed = others.get(link.getUser()
                  .getOID());
            if (isAllowed(participantRuntimeOid, departmentOid, allowed))
            {
               Map<Long/* department */, Set<Long/* onBehalfOf */>> departmentMap = participantMap.get(participantRuntimeOid);
               if (departmentMap == null)
               {
                  departmentMap = CollectionUtils.newMap();
                  participantMap.put(participantRuntimeOid, departmentMap);
               }
               Set<Long/* onBehalfOf */> userSet = departmentMap.get(departmentOid);
               if (userSet == null)
               {
                  userSet = CollectionUtils.newSet();
                  departmentMap.put(departmentOid, userSet);
               }
               userSet.add(link.getUser().getOID());
            }
         }
      }
      finally
      {
         if (itr != null)
         {
            itr.close();
         }
      }

      return participantMap;
   }

   private static boolean isAllowed(long participantRuntimeOid, long departmentOid,
         Map<Long/* participant */, Set<Long/* department */>> allowed)
   {
      if (allowed.isEmpty())
      {
         return true;
      }
      Set<Long> depts = allowed.get(participantRuntimeOid);
      return depts != null && depts.contains(departmentOid);
   }            
   
   /**
    * @return the user OIDs for which the user is a deputy
    */
   private static Map<Long/* user */, Map<Long/* participant */, Set<Long/* department */>>> getUpdatedUserOids(
         IUser user)
   {
      if ( !isDeputyOfAny(user))
      {
         return null;
      }
      List<Attribute> existing = (List<Attribute>) user.getPropertyValue(IS_DEPUTY_OF);
      if (existing == null || existing.isEmpty())
      {
         return null;
      }
      List<String> toRemove = null;      
      Date now = new Date();
      Map<Long/* user */, Map<Long/* participant */, Set<Long/* department */>>> others = CollectionUtils.newMap();
      for (Attribute attr : existing)
      {
         String value = (String) attr.getValue();
         DeputyBean db = DeputyBean.fromString(value);
         if (db.isActive(now))
         {
            others.put(db.user, collectGrants(db.grants));
         }
         else if (db.isExpired(now))
         {
            // cleanup expired deputies
            if (toRemove == null)
            {
               toRemove = CollectionUtils.newList();
            }
            toRemove.add(value);
         }
      }
      if (toRemove != null)
      {
         for (String value : toRemove)
         {
            user.removeProperty(IS_DEPUTY_OF, value);
         }
      }
      return others;
   }   
     
   public static boolean isDeputyOfAny(IUser user)
   {
      return user.isPropertyAvailable(UserBean.EXTENDED_STATE_FLAG_DEPUTY_OF_PROP);
   }   
   
   private static Map<Long/* participant */, Set<Long/* department */>> collectGrants(
         List<GrantBean> grants)
   {
      if (grants == null || grants.isEmpty())
      {
         return Collections.emptyMap();
      }
      Map<Long/* participant */, Set<Long/* department */>> result = CollectionUtils.newMap();
      for (GrantBean grant : grants)
      {
         Set<Long/* department */> depts = result.get(grant.participant);
         if (depts == null)
         {
            depts = CollectionUtils.newSet();
            result.put(grant.participant, depts);
         }
         depts.add(grant.department);
      }
      return result;
   }
   
   public static boolean isDeputyOf(IUser user, long otherOID)
   {
      if (isDeputyOfAny(user))
      {
         Date now = new Date();

         List<UserProperty> propertyList = (List<UserProperty>) user.getPropertyValue(UserUtils.IS_DEPUTY_OF);

         for (UserProperty userProperty : propertyList)
         {
            String stringValue = (String) userProperty.getValue();
            DeputyBean deputyBean = DeputyBean.fromString(stringValue);
            if (deputyBean.user == otherOID)
            {
               return deputyBean.isActive(now);
            }
         }
      }
      return false;
   }   
   
   public static List<DeputyBean> getDeputies(IUser deputyUser)
   {
      List<DeputyBean> result = CollectionUtils.newArrayList();

      if (isDeputyOfAny(deputyUser))
      {
         List<UserProperty> propertyList = (List<UserProperty>) deputyUser.getPropertyValue(UserUtils.IS_DEPUTY_OF);

         for (UserProperty userProperty : propertyList)
         {
            String stringValue = (String) userProperty.getValue();
            DeputyBean deputyBean = DeputyBean.fromString(stringValue);
            result.add(deputyBean);
         }
      }

      return result;
   }
   
   private UserUtils() {}
}
