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

import static org.eclipse.stardust.common.CollectionUtils.copyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.*;

import org.eclipse.stardust.common.StringUtils;

public abstract class ExternalUserConfiguration
{
   /**
    * Gets the Session Tokens for the external user.
    * 
    * @return Key/Value pairs.
    */   
   public Map<String, String> getSessionTokens()
   {
      return Collections.EMPTY_MAP;
   }
   
   /**
    * Gets the first name of the external user.
    * 
    * @return The first name.
    */
   public abstract String getFirstName();

   /**
    * Gets the last name of the external user.
    * 
    * @return The last name.
    */
   public abstract String getLastName();

   /**
    * Gets the email address of the external user.
    * 
    * @return The email address.
    */
   public abstract String getEMail();

   /**
    * Gets the description of the external user.
    * 
    * @return The description.
    */
   public abstract String getDescription();

   /**
    * Gets custom properties of the external user.
    * 
    * @return The set of property (name, value) pairs.
    */
   public abstract Map getProperties();

   /**
    * Gets the list of participants the external user has grants for.
    *
    * @deprecated use {@link ExternalUserConfiguration#getModelParticipantsGrants()} instead which is
    *    able to cope with scoped grants
    * @return A collection with IDs of the granted model participants.
    */
   @Deprecated
   public Collection getGrantedModelParticipants()
   {
      final Set<String> participantGrants = newHashSet();
      for (final GrantInfo grant : getModelParticipantsGrants())
      {
         if (grant != null)
         {
            participantGrants.add(grant.getParticipantId());            
         }
      }
      
      return new ArrayList<String>(participantGrants);
   }

   /**
    * Gets the list of user groups the external user is member of.
    * 
    * <p>The default implementation returns an empty list.</p>
    *
    * @return A collection with IDs of the user groups the user is member of.
    */
   public Collection getUserGroupMemberships()
   {
      return Collections.EMPTY_LIST;
   }
   
   /**
    * Gets the set of participants the external user has grants for.
    *
    * @return A set with IDs of the granted model participants.
    */
   public Set<GrantInfo> getModelParticipantsGrants()
   {
      return Collections.emptySet();
   }
   
   public static class GrantInfo
   {
      private final String participantId;
      private final List<String> departmentKey;

      public GrantInfo(String participantId, List<String> departmentKey)
      {
         if (participantId == null)
         {
            throw new NullPointerException("participantId must not be null.");
         }
         if (StringUtils.isEmpty(participantId))
         {
            throw new IllegalArgumentException("participantId must not be empty.");
         }
         
         this.participantId = participantId;
         this.departmentKey = getAdjustedList(departmentKey);
      }
      
      public GrantInfo(String participantId, String... departmentKey)
      {
         this(participantId, Arrays.asList(departmentKey));
      }

      public String getParticipantId()
      {
         return participantId;
      }

      public List<String> getDepartmentKey()
      {
         return departmentKey;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + departmentKey.hashCode();
         result = prime * result + participantId.hashCode();
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if (obj == null)
         {
            return false;
         }
         if (getClass() != obj.getClass())
         {
            return false;
         }
         
         final GrantInfo other = (GrantInfo) obj;
         if ( !departmentKey.equals(other.departmentKey))
         {
            return false;
         }
         if ( !participantId.equals(other.participantId))
         {
            return false;
         }

         return true;
      }
      
      @Override
      public String toString()
      {
         final StringBuilder sb = new StringBuilder();
         sb.append(participantId);
         if ( !departmentKey.isEmpty())
         {
            sb.append("<");         
            for (final String s : departmentKey)
            {
               sb.append(s + ",");
            }
            sb.setCharAt(sb.length() - 1, '>');
         }
         return sb.toString();
      }
      
      private List<String> getAdjustedList(final List<String> departmentKey)
      {
         if (departmentKey == null)
         {
            return newArrayList();
         }
         
         final int firstIndexOfNull = departmentKey.indexOf(null);
         if (firstIndexOfNull != -1)
         {
            return copyList(departmentKey.subList(0, firstIndexOfNull));
         }
         else
         {
            return copyList(departmentKey);
         }
      }
   }
}
