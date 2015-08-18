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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IModeler;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;


/**
 *
 */
public class ModelerBean extends ModelParticipantBean
      implements IModeler
{
   private static final Logger trace = LogManager.getLogger(ModelerBean.class);

   private static final String NAME_SPACE = "Modeler::";

   private static final String PASSWORD_ATT = "Password";
   private String password;

   private static final String E_MAIL_ATT = "EMail";
   private String eMail;

   ModelerBean()
   {
   }

   public ModelerBean(String id, String name, String description, String password)
   {
      super(id, name, description);
      this.password = password;
   }

   public String toString()
   {
      return "Modeler: " + getName();
   }

   public String getEMail()
   {
      return this.eMail;
   }

   /**
    *
    */
   public void setEMail(String eMail)
   {
      markModified();

      this.eMail = eMail;
   }

   /**
    *
    */
   public boolean checkPassword(String password)
   {
      return this.password.equals(password);
   }

   /**
    *
    */
   public void setPassword(String password)
   {
      markModified();

      this.password = password;
   }

   /**
    *
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * Checks, wether the participant <code>participant</code> is equal to
    * this participant or, if the participant is an organization or role,
    * he is part of this organization and its suborganizations or is playing
    * this role.
    */
   public boolean isAuthorized(IModelParticipant participant)
   {
      Assert.isNotNull(participant);

      if (participant == this)
      {
         return true;
      }

      return false;
   }

   public boolean isAuthorized(IUser user)
   {
      return false;
   }

   public boolean isAuthorized(IUserGroup userGroup)
   {
      return false;      
   }
   
   public Iterator getAllParticipants()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public int getCardinality()
   {
      return Unknown.INT;
   }
}