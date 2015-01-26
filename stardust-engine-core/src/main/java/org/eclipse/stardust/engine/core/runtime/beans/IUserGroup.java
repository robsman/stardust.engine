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

import org.eclipse.stardust.engine.api.runtime.IDynamicParticipant;


/**
 *
 */
public interface IUserGroup extends AttributedIdentifiablePersistent, IDynamicParticipant
{
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
   String getName();

   /**
    *
    */
   void setName(String name);

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
   
   /**
    * 
    */
   public AuditTrailPartitionBean getPartition();
   
   /**
    * 
    */
   public void setPartition(AuditTrailPartitionBean partition);

   /**
    * @return  An iterator over all users which belong to this user group. 
    */
   Iterator findAllUsers();

   /**
    * @param user Adds a given user to this user group.
    */
   void addUser(IUser user);

   /**
    * @param user Removes a given user from this user group.
    */
   void removeUser(IUser user);
}
