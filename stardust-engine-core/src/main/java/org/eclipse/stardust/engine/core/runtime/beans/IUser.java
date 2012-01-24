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

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.runtime.IDynamicParticipant;


/**
 *
 */
public interface IUser extends AttributedIdentifiablePersistent, IDynamicParticipant
{
   String LAST_LOGIN_TIMESTAMP_PROPERTY = "LastLoginTimestamp";
   String LAST_FAILED_LOGIN_TIMESTAMP_PROPERTY = "LastFailedLoginTimestamp";
   String FAILED_LOGIN_RETRIES_COUNT_PROPERTY = "FailedLoginRetriesCount";

   /**
    * Get the probability for this user
    * @return the probability for this user
    */
   Integer getQualityAssuranceProbability();
   
   /**
    * Set the probability for quality assurance.
    * The current user is not allowed to change his own probability. 
    * 
    * @param probability - if null, this value will be ignored.
    */
   void setQualityAssuranceProbability(Integer probability);
   
   /**
    * Returns if the password is expired.
    * 
    * @return true if password is expired.
    */
   public boolean isPasswordExpired();
   
   /**
    * Set password expired for this user.
    * 
    * @param expired
    */
   public void setPasswordExpired(boolean expired);
   
   /**
   *
   */
  Date getLastLoginTime();   
   
   /**
    *
    */
   Object getPrimaryKey();

   /**
    *
    */
   String getId();

   /**
    *
    */
   void setId(String id);

   /**
    *
    */
   String getAccount();

  /**
   *
   */
  String getRealmQualifiedAccount();

   /**
    *
    */
   void setAccount(String account);

   /**
    *
    */
   String getName();

   /**
    *
    */
   void setName(String name);

   /**
    *
    */
   String getFirstName();

   /**
    *
    */
   void setFirstName(String firstName);

   /**
    *
    */
   String getLastName();

   /**
    *
    */
   void setLastName(String lastName);

   /**
    * Checks the password provided with <tt>password</tt> against the - possibly hashed
    * password of the user.
    */
   boolean checkPassword(String password);

   /**
    *
    */
   void setPassword(String password);

   /**
   *
   */
   String getPassword();
   
   /**
    *
    */
   String getEMail();

   /**
    *
    */
   void setEMail(String eMail);

   /**
    *
    */
   Date getValidFrom();

   /**
    *
    */
   void setValidFrom(Date validFrom);

   /**
    *
    */
   Date getValidTo();

   /**
    *
    */
   void setValidTo(Date validTo);

   /**
    *
    */
   boolean isValid();

   /**
    *
    */
   String getDescription();

   /**
    *
    */
   void setDescription(String description);

   Iterator<IRole> getAllRoles();

   Iterator<IOrganization> getAllOrganizations();

   Iterator<UserParticipantLink> getAllParticipantLinks();
   
   Iterator<UserUserGroupLink> getAllUserGroupLinks();
   
   Iterator getAllParticipants();

   void addToParticipants(IModelParticipant participant, IDepartment department);

   void removeFromParticipants(IModelParticipant participant, IDepartment department);

   boolean hasRole(String name);

   Iterator getAllUserGroups(boolean validOnly);

   /**
    *
    */
   long getTargetWfmsWorktime();

   /**
    *
    */
   long getTargetWorktime();

   /**
    *
    */
   long getWorkingWeeks();

   void clearAllParticipants();

   /**
    * Checks, wether a user is authorized to start this process.
    */
   boolean isAuthorizedForStarting(IProcessDefinition process);

   public IUserRealm getRealm();
   
   public void setRealm(IUserRealm realm);
   
   public long getDomainOid();
   
   public String getDomainId();
   
   public Map getProfile();
   
   public void setProfile(Map profile);

   public boolean hasGrant(IModelParticipant participant);
}