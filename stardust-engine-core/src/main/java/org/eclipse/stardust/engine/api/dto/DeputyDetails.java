/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.dto;

import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;

public class DeputyDetails implements Deputy
{
   private UserInfo user;
   private UserInfo deputyUser;
   private Date fromDate;
   private Date toDate;
   
   private static final long serialVersionUID = 6191254357079939981L;

   public DeputyDetails(IUser user, IUser deputyUser, Date fromDate, Date toDate)
   {
      this.user = DetailsFactory.create(user);
      this.deputyUser = DetailsFactory.create(deputyUser);
      this.fromDate = fromDate;
      this.toDate = toDate;
   }

   @Override
   public UserInfo getUser()
   {
      return user;
   }

   @Override
   public UserInfo getDeputyUser()
   {
      return deputyUser;
   }

   @Override
   public Date getFromDate()
   {
      return fromDate;
   }

   @Override
   public Date getUntilDate()
   {
      return toDate;
   }
   
   @Override
   public String toString()
   {
      return "Deputy " + deputyUser + " for user " + user + ". Active in timeframe from "
            + fromDate + " until " + toDate;
   }

}
