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

import org.eclipse.stardust.engine.api.dto.UserGroupDetailsLevel;
import org.eclipse.stardust.engine.api.model.DynamicParticipant;


/**
 * @author sborn
 * @version $Revision$
 */
public interface UserGroup extends DynamicParticipant, UserGroupInfo
{
   /**
    * Sets the name of the user group. The actual name will not be changed until
    * {@link UserService#modifyUserGroup(UserGroup)} is invoked.
    * 
    * @param name
    *           the new name.
    * 
    * @see UserService#modifyUserGroup(UserGroup)
    */
   void setName(String name);

   /**
    * Sets the validity start date of the user group. The actual date will not be changed
    * until {@link UserService#modifyUserGroup(UserGroup)} is invoked.
    * 
    * @param validFrom
    *           the validity start date, or null if unlimited.
    * 
    * @see UserService#modifyUserGroup(UserGroup)
    */
   void setValidFrom(Date validFrom);

   /**
    * Sets the validity end date of the user group. The actual date will not be changed
    * until {@link UserService#modifyUserGroup(UserGroup)} is invoked.
    * 
    * @param validTo
    *           the validity end date, or null if unlimited.
    * 
    * @see UserService#modifyUserGroup(UserGroup)
    */
   void setValidTo(Date validTo);

   /**
    * Retrieves this user group's description.
    * 
    * @return the description.
    */
   String getDescription();

   /**
    * Sets the description of the user group. The actual description will not be changed
    * until {@link UserService#modifyUserGroup(UserGroup)} is invoked.
    * 
    * @param description
    *           the description.
    * 
    * @see UserService#modifyUserGroup(UserGroup)
    */
   void setDescription(String description);
   
   /**
    * @param name
    * @param value
    */
   void setAttribute(String name, Serializable value);

   /**
    * Sets a custom property. The actual propertY will not be changed until
    * {@link UserService#modifyUserGroup(UserGroup)} is invoked.
    * 
    * @param name
    *           the name of the property.
    * @param value
    *           the value of the property.
    * 
    * @see UserService#modifyUserGroup(UserGroup)
    */
   // void setProperty(String name, Serializable value);

   /**
    * Sets all the custom properties of the user group. This method will clear any
    * previous custom properties the user group may have. The actual properties will not
    * be changed until {@link UserService#modifyUserGroup(UserGroup)} is invoked.
    * 
    * @param properties
    *           a Map containing name-value pair of custom properties.
    * 
    * @see UserService#modifyUserGroup(UserGroup)
    */
   // void setAllProperties(Map properties);
   
   public UserGroupDetailsLevel getDetailsLevel();
}
