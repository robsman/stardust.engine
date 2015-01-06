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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.Functions;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.Functions.BoundFunction;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceHierarchyBean;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * <p>
 * A {@link ServiceCommand} that queries the database for the <i>Processing Time</i> of one
 * or more given <i>Process Instances</i>. <i>Processing Time</i> is defined as the overall time
 * of the <i>Process Instance's</i> constituent <i>Activity Instances</i> (including those in subprocesses)
 * spent in state {@link ActivityInstanceState#Application} and only applies to <i>Interactive
 * Activities</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class QueryProcessInstanceProcessingTimeCommand implements ServiceCommand
{
   private static final long serialVersionUID = 7835839785006007895L;

   private final Set<Long> oids;

   /**
    * <p>
    * ctor
    * </p>
    *
    * @param oid the process instance oid to query
    */
   public QueryProcessInstanceProcessingTimeCommand(final long oid)
   {
      this(Collections.singleton(oid));
   }

   /**
    * <p>
    * ctor
    * </p>
    *
    * @param oids the process instance oids to query
    */
   public QueryProcessInstanceProcessingTimeCommand(final Set<Long> oids)
   {
      this.oids = oids;
   }

   /**
    * <p>
    * Calculates the <i>Process Instance Processing Times</i>.
    * </p>
    *
    * @param ignored a {@link ServiceFactory} instance which is <b>not</b> used during the call and therefore may be {@code null}
    *
    * @return the process instance processing times
    */
   @Override
   public ProcessingTimes execute(final ServiceFactory ignored)
   {
      final ProcessingTimes result = new ProcessingTimes();

      final long now = TimestampProviderUtils.getTimeStamp().getTime();
      final BoundFunction sumOfDurations = Functions.constantExpression("SUM((CASE WHEN " + ActivityInstanceHistoryBean.FIELD__UNTIL + " = 0 THEN " + now + " ELSE " + ActivityInstanceHistoryBean.FIELD__UNTIL + " END) - " + ActivityInstanceHistoryBean.FIELD__FROM + ")");

      final QueryDescriptor queryDesc = QueryDescriptor.from(ActivityInstanceHistoryBean.class);
      queryDesc.select(ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, sumOfDurations);
      queryDesc.innerJoin(ProcessInstanceHierarchyBean.class)
               .on(ActivityInstanceHistoryBean.FR__PROCESS_INSTANCE, ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE)
               .where(
                     andTerm(
                           inList(ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, oids.iterator()),
                           isEqual(ActivityInstanceHistoryBean.FR__STATE, ActivityInstanceState.APPLICATION),
                           inList(ActivityInstanceHistoryBean.FR__PERFORMER_KIND, new int[] { PerformerType.USER, PerformerType.MODEL_PARTICIPANT, PerformerType.USER_GROUP })));
      queryDesc.groupBy(ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE);

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
}
