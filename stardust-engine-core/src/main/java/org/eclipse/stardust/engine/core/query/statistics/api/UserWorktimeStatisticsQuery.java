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

import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;

/**
 * @author rsauer
 * @version $Revision$
 */
public class UserWorktimeStatisticsQuery extends CustomUserQuery
{
   static final long serialVersionUID = 7292991947057793832L;

   public static final String ID = UserWorktimeStatisticsQuery.class.getName();

   private boolean calculateWaitTime;

   /**
    * Calculates worktime statistics including wait time.
    *
    * @return the query result
    */
   public static UserWorktimeStatisticsQuery forAllUsers()
   {
      return new UserWorktimeStatisticsQuery(true);
   }

   /**
    * Calculates worktime statistics excluding wait time.
    *
    * @return the query result
    */
   public static UserWorktimeStatisticsQuery forAllUsersWithoutWaitTime()
   {
      return new UserWorktimeStatisticsQuery(false);
   }

   protected UserWorktimeStatisticsQuery(boolean calculateWaitTime)
   {
      super(ID);
      this.calculateWaitTime = calculateWaitTime;
   }

   public boolean isCalculateWaitTime()
   {
      return calculateWaitTime;
   }

}
