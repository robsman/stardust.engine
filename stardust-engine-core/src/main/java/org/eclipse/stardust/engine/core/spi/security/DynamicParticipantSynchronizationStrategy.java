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

import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;

/**
 * The DynamicParticipantSynchronizationStrategy is used to determine if a given user or
 * user group needs to be synchronized with an external.
 */
public abstract class DynamicParticipantSynchronizationStrategy
{
   /**
    * Checks if this user needs to be synchronized.
    * 
    * @param user the user to be checked.
    * @return 
    */
   public abstract boolean isDirty(IUser user);

   /**
    * Callback method to inform the SynchronizationStrategy that the user have been
    * successfuly synchronized.
    * 
    * @param user the user that has been synchronized.
    */
   public abstract void setSynchronized(IUser user);

   /**
    * Checks if this user group needs to be synchronized.
    *
    * <p>The default implementation is to never flag the user group as dirty.</p>
    * 
    * @param userGroup
    *           the user group to be checked.
    * @return
    */
   public boolean isDirty(IUserGroup userGroup)
   {
      return false;
   }

   /**
    * Callback method to inform the SynchronizationStrategy that the user group has been
    * successfully synchronized.
    * 
    * <p>The default implementation is to do nothing.</p>
    *
    * @param userGroup
    *           the user group that has been synchronized.
    */
   public void setSynchronized(IUserGroup userGroup)
   {
   }
   
   /**
    * Checks if this department needs to be synchronized.
    *
    * <p>The default implementation is to never flag the department as dirty.</p>
    *
    * @param department
    *           the department to be checked.
    * @return
    */
   public boolean isDirty(IDepartment department)
   {
      return false;
   }

   /**
    * Callback method to inform the SynchronizationStrategy that the department has been
    * successfully synchronized.
    *
    * <p>The default implementation is to do nothing.</p>
    *
    * @param department
    *           the department that has been synchronized.
    */
   public void setSynchronized(IDepartment department)
   {
   }
}
