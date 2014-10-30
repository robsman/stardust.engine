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

import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.inList;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.Predicates.orTerm;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.Functions;
import org.eclipse.stardust.engine.core.persistence.Functions.BoundFunction;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * <p>
 * A {@link ServiceCommand} that queries the database for the <i>Processing Time</i> of one
 * or more given <i>Activity Instances</i>. <i>Processing Time</i> is defined as the overall time
 * spent in state {@link ActivityInstanceState#Application} and only applies to <i>Interactive
 * Activities</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class QueryActivityInstanceProcessingTimeCommand implements ServiceCommand
{
   private static final long serialVersionUID = 2131157154875979598L;

   private final Set<Long> oids;

   /**
    * <p>
    * ctor
    * </p>
    *
    * @param oid the activity instance oid to query
    */
   public QueryActivityInstanceProcessingTimeCommand(final long oid)
   {
      this(Collections.singleton(oid));
   }

   /**
    * <p>
    * ctor
    * </p>
    *
    * @param oids the activity instance oids to query
    */
   public QueryActivityInstanceProcessingTimeCommand(final Set<Long> oids)
   {
      this.oids = oids;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.command.ServiceCommand#execute(org.eclipse.stardust.engine.api.runtime.ServiceFactory)
    */
   @Override
   public ActivityInstanceProcessingTimes execute(final ServiceFactory ignored)
   {
      final ActivityInstanceProcessingTimes result = new ActivityInstanceProcessingTimes();

      final long now = TimestampProviderUtils.getTimeStamp().getTime();
      final BoundFunction sumOfDurations = Functions.constantExpression("SUM((CASE WHEN " + ActivityInstanceHistoryBean.FIELD__UNTIL + " = 0 THEN " + now + " ELSE " + ActivityInstanceHistoryBean.FIELD__UNTIL + " END) - " + ActivityInstanceHistoryBean.FIELD__FROM + ")");

      final QueryDescriptor queryDesc = QueryDescriptor.from(ActivityInstanceHistoryBean.class);
      queryDesc.where(
            andTerm(
                  isEqual(ActivityInstanceHistoryBean.FR__STATE, ActivityInstanceState.APPLICATION),
                  inList(ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE, oids.iterator()),
                  orTerm(
                        isEqual(ActivityInstanceHistoryBean.FR__PERFORMER_KIND, PerformerType.USER),
                        isEqual(ActivityInstanceHistoryBean.FR__PERFORMER_KIND, PerformerType.MODEL_PARTICIPANT))));
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

   /**
    * <p>
    * The class holding the query results as a {@link Set} of {@link ActivityInstanceProcessingTime}s.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static final class ActivityInstanceProcessingTimes implements Serializable
   {
      private static final long serialVersionUID = -4259445814904017303L;

      private final Set<ActivityInstanceProcessingTime> processingTimes = CollectionUtils.newHashSet();

      /* package-private */ void addProcessingTime(final long oid, final long processingTime)
      {
         processingTimes.add(new ActivityInstanceProcessingTime(oid, processingTime));
      }

      /**
       * @return the processing times
       */
      public Set<ActivityInstanceProcessingTime> processingTimes()
      {
         return Collections.unmodifiableSet(processingTimes);
      }

      /* (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return processingTimes.toString();
      }
   }

   /**
    * <p>
    * The class holding the processing time for a particular activity instance.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static final class ActivityInstanceProcessingTime extends Pair<Long, Long>
   {
      private static final long serialVersionUID = 8628678559744677858L;

      /* package-private */ ActivityInstanceProcessingTime(final long oid, final long processingTime)
      {
         super(Long.valueOf(oid), Long.valueOf(processingTime));
      }

      /**
       * @return the activity instance's OID
       */
      public long oid()
      {
         return getFirst();
      }

      /**
       * @return the processing time in ms
       */
      public long processingTime()
      {
         return getSecond();
      }
   }
}
