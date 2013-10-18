/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.runtime.beans.*;

public class TransientUser implements IUser
{
   private TransientUser()
   {
   }

   public static TransientUser getInstance()
   {
      return new TransientUser();
   }

   private IUserRealm realm = TransientRealm.getInstance();

   public void addPropertyValues(Map map)
   {
   }

   public Map getAllPropertyValues()
   {
      return null;
   }

   public Map getAllProperties()
   {
      return null;
   }

   public Serializable getPropertyValue(String name)
   {
      return null;
   }

   public void setPropertyValue(String name, Serializable value)
   {
   }

   public void setPropertyValue(String name, Serializable value, boolean force)
   {
   }

   public void removeProperty(String name)
   {
   }

   public void removeProperty(String name, Serializable value)
   {
   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      return null;
   }

   public long getOID()
   {
      return 0;
   }

   public void setOID(long oid)
   {
   }

   public void lock() throws ConcurrencyException
   {
   }

   public void delete()
   {
   }

   public void delete(boolean writeThrough)
   {
   }

   public PersistenceController getPersistenceController()
   {
      return null;
   }

   public void setPersistenceController(PersistenceController PersistenceController)
   {
   }

   public void disconnectPersistenceController()
   {
   }

   public void markModified()
   {
   }

   public void markModified(String fieldName)
   {
   }

   public void fetch()
   {
   }

   public void markCreated()
   {
   }

   public Integer getQualityAssuranceProbability()
   {
      return null;
   }

   public void setQualityAssuranceProbability(Integer probability)
   {
   }

   public boolean isPasswordExpired()
   {
      return false;
   }

   public void setPasswordExpired(boolean expired)
   {
   }

   public Date getLastLoginTime()
   {
      return null;
   }

   public Object getPrimaryKey()
   {
      return null;
   }

   public String getId()
   {
      return "internal";
   }

   public void setId(String id)
   {
   }

   public String getAccount()
   {
      return "internal";
   }

   public String getRealmQualifiedAccount()
   {
      return null;
   }

   public void setAccount(String account)
   {
   }

   public String getName()
   {
      return "internal";
   }

   public void setName(String name)
   {
   }

   public String getFirstName()
   {
      return null;
   }

   public void setFirstName(String firstName)
   {
   }

   public String getLastName()
   {
      return null;
   }

   public void setLastName(String lastName)
   {
   }

   public boolean checkPassword(String password)
   {
      return false;
   }

   public void setPassword(String password)
   {
   }

   public String getPassword()
   {
      return null;
   }

   public String getEMail()
   {
      return null;
   }

   public void setEMail(String eMail)
   {
   }

   public Date getValidFrom()
   {
      return null;
   }

   public void setValidFrom(Date validFrom)
   {
   }

   public Date getValidTo()
   {
      return null;
   }

   public void setValidTo(Date validTo)
   {
   }

   public boolean isValid()
   {
      return true;
   }

   public String getDescription()
   {
      return null;
   }

   public void setDescription(String description)
   {
   }

   public Iterator<IRole> getAllRoles()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public Iterator<IOrganization> getAllOrganizations()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public Iterator<UserParticipantLink> getAllParticipantLinks()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public Iterator<UserUserGroupLink> getAllUserGroupLinks()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public Iterator getAllParticipants()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public void addToParticipants(IModelParticipant participant, IDepartment department)
   {
   }

   public void addToParticipants(IModelParticipant participant, IDepartment department,
         long onBehalfOf)
   {
   }
   
   public void removeFromParticipants(IModelParticipant participant,
         IDepartment department)
   {
   }

   public boolean hasRole(String name)
   {
      if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(name))
      {
         return true;
      }
      return false;
   }

   public Iterator getAllUserGroups(boolean validOnly)
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public long getTargetWfmsWorktime()
   {
      return 0;
   }

   public long getTargetWorktime()
   {
      return 0;
   }

   public long getWorkingWeeks()
   {
      return 0;
   }

   public void clearAllParticipants()
   {
   }

   public boolean isAuthorizedForStarting(IProcessDefinition process)
   {
      return false;
   }

   public IUserRealm getRealm()
   {
      return realm;
   }

   public void setRealm(IUserRealm realm)
   {
   }

   public long getDomainOid()
   {
      return 0;
   }

   public String getDomainId()
   {
      return null;
   }

   public Map getProfile()
   {
      return null;
   }

   public void setProfile(Map profile)
   {
   }

   public boolean hasGrant(IModelParticipant participant)
   {
      return false;
   }
  
   @Override
   public boolean isPropertyAvailable(int pattern)
   {
      return false;
   }

   public Map<String, String> getSessionTokens()
   {
      return null;
   }

   public void SetSessionTokens(Map<String, String> sessionTokens)
   {
   }   
}