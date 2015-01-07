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
 *
 */
public interface IUserDomainUser extends IdentifiablePersistent
{
   IUserDomain getUserDomain();

   void setUserDomain(IUserDomain domain);

   IUser getUser();

   void setUser(IUser user);

   public Date getValidFrom();

   public void setValidFrom(Date validFrom);

   public Date getValidTo();

   public void setValidTo(Date validTo);
}
