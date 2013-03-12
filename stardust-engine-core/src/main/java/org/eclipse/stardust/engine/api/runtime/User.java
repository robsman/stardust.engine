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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.DynamicParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;


/**
 * The <code>User</code> represents a snapshot of the user state.
 * <p>It contains general user information, as well as information regarding the
 * permissions (<code>{@link Grant}</code>) the user currently have.</p>
 * <p>The User instance can be modified and used to update the user's information.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface User extends DynamicParticipant, UserInfo
{
   /**
    * Get the probability for this user
    * @return the probability for this user
    */
   Integer getQualityAssuranceProbability();
   
   /**
    * Set the probability for quality assurance.
    * The current is not allowed to change his own probability. 
    * 
    * @param propability
    */
   void setQualityAssuranceProbability(Integer probability) throws InvalidValueException;
   
   /**
    * Returns if user is admin.
    * 
    * @return true if user is admin.
    * @throws IllegalStateException if details level is {@link UserDetailsLevel#MINIMAL}
    */
   public boolean isAdministrator();   
   
   /**
    * Checks if user password expired.
    * 
    * @return true if expired flag is set.
    */
   // no restrictions
   boolean isPasswordExpired();   
   
   /**
    * Returns the date of the previous login.
    * 
    * @return Date.
    */
   Date getPreviousLoginTime();
   
   /**
    * Retrieves the OID of the users realm.
    * 
    * @return a unique identifier of the users realm in the audit trail.
    * 
    * @deprecated Use getRealm().getOID();
    */
   long getRealmOID();
   
   /**
    * Retrieves the id of the users realm.
    * 
    * @return the users realm id.
    * 
    * @deprecated Use getRealm().getId();
    */
   String getRealmId();
   
   /**
    * Retrieves the realm of the user.
    * 
    * @return the user realm.
    */
   UserRealm getRealm();
   
   /**
    * Retrieves the account (login name) of the user. Same as
    * {@link DynamicParticipant#getId()}.
    * 
    * @return the account name.
    */
   String getAccount();

   /**
    * Retrieves the first name of the user.
    *
    * @return the first name.
    */
   String getFirstName();

   /**
    * Retrieves the last name (family name) of the user.
    *
    * @return the last name.
    */
   String getLastName();

   /**
    * Retrieves the email address of the user.
    *
    * @return the email address.
    */
   String getEMail();

   /**
    * Retrieves the date from which this user is valid.
    *
    * @return the validity start date, or null if unlimited.
    */
   Date getValidFrom();

   /**
    * Retrieves the date until this user is valid.
    *
    * @return the validity end date, or null if unlimited.
    */
   Date getValidTo();

   /**
    * Retrieves this user description.
    *
    * @return the user description.
    */
   String getDescription();

   /**
    * Retrieves a custom property value for user.
    *
    * @param name the property name.
    *
    * @return the value of the property.
    */
   Serializable getProperty(String name);

   /**
    * Retrieves all user custom properties.
    *
    * @return a Map of name-value pairs of user properties.
    */
   Map<String, Object> getAllProperties();
   
   /**
    * Retrieves the level of details for user.
    *
    * @return the user details level.
    */
   UserDetailsLevel getDetailsLevel();

   /**
    * Sets the password of the user. The actual password will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param password the new password.
    *
    * @see UserService#modifyUser
    */
   void setPassword(String password);

   /**
    * Sets the account name of the user. The actual account will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param account the new account name
    *
    * @see UserService#modifyUser
    */
   void setAccount(String account);

   /**
    * Sets the description of the user. The actual description will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param description the user description.
    *
    * @see UserService#modifyUser
    */
   void setDescription(String description);

   /**
    * Sets the email address of the user. The actual email address will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param eMail the new email address.
    *
    * @see UserService#modifyUser
    */
   void setEMail(String eMail);

   /**
    * Sets the first name of the user. The actual first name will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param firstName the new first name of the user.
    *
    * @see UserService#modifyUser
    */
   void setFirstName(String firstName);

   /**
    * Sets the last name (family name) of the user. The actual name will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param lastName the user last name.
    *
    * @see UserService#modifyUser
    */
   void setLastName(String lastName);

   /**
    * Sets the validity start date of the account. The actual date will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param validFrom the validity start date, or null if unlimited.
    *
    * @see UserService#modifyUser
    */
   void setValidFrom(Date validFrom);

   /**
    * Sets the validity end date of the account. The actual date will not be changed until
    * UserService.modifyUser(user) is invoked.
    *
    * @param validTo the validity end date, or null if unlimited.
    *
    * @see UserService#modifyUser
    */
   void setValidTo(Date validTo);

   /**
    * Sets a custom property.
    *
    * @param name the name of the property.
    * @param value the value of the property.
    */
   void setProperty(String name, Serializable value);

   /**
    * Sets all the custom properties of the user. This method will clear any previous
    * custom properties the user may have.
    *
    * @param properties a Map containing name-value pair of custom properties.
    *
    * @see UserService#modifyUser
    */
   void setAllProperties(Map<String, Object> properties);

   /**
    * Returns all the grants (permissions) given to the user.
    *
    * Contains the organization grants before the role grants.
    *
    * @return a List of {@link Grant} objects.
    */
   List<Grant> getAllGrants();

   /**
    * Adds a new grant to the user from the active model. The grant will not be actually
    * given until UserService.modifyUser(user) is invoked.
    *
    * @param id the ID of the participant (role or organization) in the active model.
    *
    * @see UserService#modifyUser
    * @see Grant
    * @deprecated Please use {@link #addGrant(ModelParticipantInfo participant)} instead
    */
   void addGrant(String id);

   /**
    * Marks that grants for this participant should be added to all model versions. The grant will not be actually
    * given until UserService.modifyUser(user) is invoked.
    * 
    * @param participant the participants (bound or not to a department) for which the grants will be added.
    * @throws InvalidArgumentException if the participant is null.
    */
   void addGrant(ModelParticipantInfo participant);

   /**
    * Removes a grant from the user from the active model. The grant will not be actually
    * removed until UserService.modifyUser(user) is invoked.
    *
    * @param id the ID of the participant (role or organization) in the active model.
    *
    * @see UserService#modifyUser
    * @see Grant
    * @deprecated Please use {@link #removeGrant(ModelParticipantInfo participant)} instead
    */
   void removeGrant(String id);

   /**
    * Marks the grants for this participant to be removed from all model versions. The grant will not be actually
    * removed until UserService.modifyUser(user) is invoked.
    * 
    * @param participant the participants (bound or not to a department) for which the grants will be removed.
    * @throws InvalidArgumentException if the participant is null.
    */
   void removeGrant(ModelParticipantInfo participant);

   /**
    * Removes all user grants. The grants will not be actually removed until
    * UserService.modifyUser(user) is invoked.
    *
    * @see UserService#modifyUser
    * @see Grant
    */
   void removeAllGrants();
   
   /**
    * Lists all user groups this user is a member of.
    * 
    * @return a list of {@link UserGroup} objects.
    */
   List<UserGroup> getAllGroups();
   
   /**
    * Marks this user's desire to join the user group identified by the given id.
    * 
    * Note: The actual join operation will only be performed when
    * {@link UserService#modifyUser(User)} is invoked.
    * 
    * @param id The id of the user group to be joined.
    */
   void joinGroup(String id);
   
   /**
    * Marks this user's desire to leave the user group identified by the given id.
    *
    * Note: The actual join operation will only be performed when
    * {@link UserService#modifyUser(User)} is invoked.
    * 
    * @param id The id of the user group to be left from.
    */
   void leaveGroup(String id);
   
   /**
    * Returns the permission state of the given permission id for the user.
    *  
    * @param permissionId
    * @return Granted if the the permission was granted to the user, Denied if the permission
    *    was denied to the user or Unknown if the permission is invalid for this user.
    */
   PermissionState getPermission(String permissionId);
}
