/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics;

import static org.eclipse.stardust.engine.core.persistence.Predicates.inList;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.Functions;
import org.eclipse.stardust.engine.core.persistence.Functions.BoundFunction;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

public class QueryActivityInstanceProcessingTimeCommand implements ServiceCommand
{
   private static final long serialVersionUID = 2131157154875979598L;

   private final Set<Long> oids;

   public QueryActivityInstanceProcessingTimeCommand(final long oid)
   {
      this(Collections.singleton(oid));
   }

   public QueryActivityInstanceProcessingTimeCommand(final Set<Long> oids)
   {
      this.oids = oids;
   }

   @Override
   public ActivityInstanceProcessingTimes execute(final ServiceFactory ignored)
   {
      final ActivityInstanceProcessingTimes result = new ActivityInstanceProcessingTimes();

      final long now = TimestampProviderUtils.getTimeStamp().getTime();
      final BoundFunction sumOfDurations = Functions.constantExpression("SUM((CASE WHEN " + ActivityInstanceHistoryBean.FIELD__UNTIL + " = 0 THEN " + now + " ELSE " + ActivityInstanceHistoryBean.FIELD__UNTIL + " END) - " + ActivityInstanceHistoryBean.FIELD__FROM + ")");

      final QueryDescriptor queryDesc = QueryDescriptor.from(ActivityInstanceHistoryBean.class);
      queryDesc.where(Predicates.andTerm(isEqual(ActivityInstanceHistoryBean.FR__STATE, ActivityInstanceState.APPLICATION), inList(ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE, oids.iterator())));
      queryDesc.select(ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE, sumOfDurations);
      queryDesc.groupBy(ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE);

      final Session session = (Session) SessionFactory.getSession(SessionProperties.DS_NAME_AUDIT_TRAIL);
      final ResultSet rs = session.executeQuery(queryDesc);
      try
      {
         while (rs.next())
         {
            final long oid = rs.getLong(1);
            final long processingTime = rs.getLong(2);
            result.addProcessingTime(oid, processingTime);
         }
      }
      catch (final SQLException e)
      {
         throw new ServiceCommandException("Unable to evaluate result set.", e);
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }

      return result;
   }

   public static final class ActivityInstanceProcessingTimes implements Serializable
   {
      private static final long serialVersionUID = -4259445814904017303L;

      private final Set<ActivityInstanceProcessingTime> processingTimes = CollectionUtils.newHashSet();

      public void addProcessingTime(final long oid, final long processingTime)
      {
         processingTimes.add(new ActivityInstanceProcessingTime(oid, processingTime));
      }

      public Set<ActivityInstanceProcessingTime> processingTimes()
      {
         return Collections.unmodifiableSet(processingTimes);
      }

      @Override
      public String toString()
      {
         return processingTimes.toString();
      }
   }

   public static final class ActivityInstanceProcessingTime extends Pair<Long, Long>
   {
      private static final long serialVersionUID = 8628678559744677858L;

      public ActivityInstanceProcessingTime(final long oid, final long processingTime)
      {
         super(Long.valueOf(oid), Long.valueOf(processingTime));
      }

      public long oid()
      {
         return getFirst();
      }

      public long processingTime()
      {
         return getSecond();
      }
   }
}
