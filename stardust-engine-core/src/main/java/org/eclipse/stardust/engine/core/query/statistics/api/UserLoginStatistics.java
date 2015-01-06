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
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class UserLoginStatistics extends CustomUserQueryResult
{
   private static final long serialVersionUID = 1l;

   protected final Map<Long, LoginStatistics> loginStatistics;
   
   protected UserLoginStatistics(UserLoginStatisticsQuery query, Users users)
   {
      super(query, users);
      
      this.loginStatistics = CollectionUtils.newMap();
   }

   public LoginStatistics getLoginStatistics(long userOid)
   {
      return (LoginStatistics) loginStatistics.get(userOid);
   }

   public static class LoginStatistics implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final long userOid;
      
      public boolean currentlyLoggedIn;
      
      public Date timeLoggedInToday;

      public Date timeLoggedInThisWeek;

      public Date timeLoggedInThisMonth;

      public LoginStatistics(long userOid)
      {
         this.userOid = userOid;
         
         this.timeLoggedInToday = new Date(0l);
         this.timeLoggedInThisWeek = new Date(0l);
         this.timeLoggedInThisMonth = new Date(0l);
      }
   }

}
