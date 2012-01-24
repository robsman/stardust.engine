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

import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;


/**
 * Supports activity instance state change logging for process warehouse
 * data retrieval.
 */
public interface IUserSession extends IdentifiablePersistent
{
   /*
    * Retrieves the session's user.
    */
   public IUser getUser();
   
   public String getClientId();
   
   public Date getStartTime();

   public Date getLastModificationTime();
   
   public void setLastModificationTime(Date lastModificationTime);
   
   public Date getExpirationTime();

   public void setExpirationTime(Date expirationTime);
}
