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
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics.LoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.UserSessionBean;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IUserQueryEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class UserLoginStatisticsRetriever implements IUserQueryEvaluator
{

   public CustomUserQueryResult evaluateQuery(CustomUserQuery query)
   {
      if ( !(query instanceof UserLoginStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + UserLoginStatisticsQuery.class.getName());
      }

      final UserLoginStatisticsQuery wsq = (UserLoginStatisticsQuery) query;

      final Users users = QueryServiceUtils.evaluateUserQuery(wsq);

      // retrieve login times

      final Date now = new Date();

      final Calendar cal = Calendar.getInstance();

      cal.setTime(now);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      final Date beginOfDay = cal.getTime();

      cal.setTime(beginOfDay);
      cal.setFirstDayOfWeek(Calendar.MONDAY);
      cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      final Date beginOfWeek = cal.getTime();

      cal.setTime(beginOfDay);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      final Date beginOfMonth = cal.getTime();

      QueryDescriptor sqlQuery = QueryDescriptor //
            .from(UserSessionBean.class) //
            .select( //
                  UserSessionBean.FR__USER, //
                  UserSessionBean.FR__START_TIME, //
                  UserSessionBean.FR__EXPIRATION_TIME)
            .where(
                  Predicates.greaterOrEqual(UserSessionBean.FR__EXPIRATION_TIME,
                        beginOfMonth.getTime()))
            .orderBy( //
                  UserSessionBean.FR__USER, //
                  UserSessionBean.FR__START_TIME, //
                  UserSessionBean.FR__EXPIRATION_TIME);

      // TODO configure this?
      if (users.size() <= 100)
      {
         final List<Long> userOids = CollectionUtils.newList();
         for (Iterator i1 = users.iterator(); i1.hasNext();)
         {
            User user = (User) i1.next();

            userOids.add(new Long(user.getOID()));
         }
         sqlQuery.where(Predicates.inList(UserSessionBean.FR__USER, userOids));
      }

      // TODO implement

      final Date loggedInFrom = new Date();
      final Date loggedInUntil = new Date();

      final Map<Long, Date> lastActivityPerUser = CollectionUtils.newMap();
      final Map<Long, LoginStatistics> loginStatistics = CollectionUtils.newMap();
      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {

         public void handleRow(ResultSet rs) throws SQLException
         {
            Long userOid = rs.getLong(1);
            loggedInFrom.setTime(rs.getLong(2));
            loggedInUntil.setTime(rs.getLong(3));

            Date lastActivity = (Date) lastActivityPerUser.get(userOid);
            if (null == lastActivity)
            {
               // initialize to first login, will be shifted to logout later
               lastActivity = new Date(loggedInFrom.getTime());
               lastActivityPerUser.put(userOid, lastActivity);
            }

            // only count if logout occured after last seen logout
            if (lastActivity.before(loggedInUntil))
            {
               if (loggedInFrom.before(lastActivity))
               {
                  // remove overlapping periods
                  loggedInFrom.setTime(lastActivity.getTime());
               }
               lastActivity.setTime(loggedInUntil.getTime());

               LoginStatistics loginTimes = (LoginStatistics) loginStatistics.get(userOid);
               if (null == loginTimes)
               {
                  loginTimes = new LoginStatistics(userOid.longValue());
                  loginStatistics.put(userOid, loginTimes);
               }

               if (loggedInUntil.after(now))
               {
                  loginTimes.currentlyLoggedIn = true;
               }

               addLoginTimeForPeriod(beginOfMonth, now, loggedInFrom, loggedInUntil,
                     loginTimes.timeLoggedInThisMonth);
               addLoginTimeForPeriod(beginOfWeek, now, loggedInFrom, loggedInUntil,
                     loginTimes.timeLoggedInThisWeek);
               addLoginTimeForPeriod(beginOfDay, now, loggedInFrom, loggedInUntil,
                     loginTimes.timeLoggedInToday);
            }
         }

      });

      return new UserLoginStatisticsResult(wsq, users, loginStatistics);
   }

   protected static void addLoginTimeForPeriod(Date periodBegin, Date periodEnd,
         Date loggedInFrom, Date loggedInUntil, Date loginTime)
   {
      if (loggedInUntil.after(periodBegin))
      {
         Date from = loggedInFrom.before(periodBegin) ? periodBegin : loggedInFrom;
         Date until = loggedInUntil.after(periodEnd) ? periodEnd : loggedInUntil;

         final long duration = until.getTime() - from.getTime();

         if (0l < duration)
         {
            loginTime.setTime(loginTime.getTime() + duration);
         }
      }
   }
}
