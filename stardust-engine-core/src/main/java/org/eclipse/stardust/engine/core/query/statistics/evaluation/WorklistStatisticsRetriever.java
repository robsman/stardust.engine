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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.Functions;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.query.statistics.api.ParticipantDepartmentPair;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatistics.ParticipantStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistStatistics.UserStatistics;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IUserQueryEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class WorklistStatisticsRetriever implements IUserQueryEvaluator
{
   public CustomUserQueryResult evaluateQuery(CustomUserQuery query)
   {
      if ( !(query instanceof WorklistStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + WorklistStatisticsQuery.class.getName());
      }

      final WorklistStatisticsQuery wsq = (WorklistStatisticsQuery) query;

      final Map<Long, UserStatistics> userStatistics = CollectionUtils.newMap();
      final Map<Long, Set<ParticipantDepartmentPair>> linkedRoles = CollectionUtils.newMap();

      Users users = initializeUsers(wsq, userStatistics, linkedRoles);

      // link users and roles, retrieve grants directly per SQL
      QueryDescriptor sqlQuery = QueryDescriptor.from(UserParticipantLink.class)
            .select(
                  UserParticipantLink.FR__USER,
                  UserParticipantLink.FR__PARTICIPANT,
                  UserParticipantLink.FR__DEPARTMENT)
            .orderBy(UserParticipantLink.FR__USER);
      
      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);
      if (!singlePartition)
      {
         Join participantJoin = sqlQuery.innerJoin(AuditTrailParticipantBean.class)
            .on(UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FIELD__OID);
         sqlQuery.innerJoin(ModelPersistorBean.class)
            .on(participantJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), ModelPersistorBean.FIELD__OID)
            .andWhere(Predicates.isEqual(
                  ModelPersistorBean.FR__PARTITION,
                  SecurityProperties.getPartitionOid()));
      }

      final Map<ParticipantDepartmentPair, ParticipantStatistics> mpStatistics = CollectionUtils.newMap();
      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         private ModelManager modelManager = ModelManagerFactory.getCurrent();

         public void handleRow(ResultSet rs) throws SQLException
         {
            Long userOid = rs.getLong(1);
            long mpRtOid = rs.getLong(2);
            long depOid = rs.getLong(3);
            
            UserStatistics userEntry = userStatistics.get(userOid);
            if (null != userEntry)
            {
               IModelParticipant mp = modelManager.findModelParticipant(PredefinedConstants.ANY_MODEL, mpRtOid);
               if (null != mp)
               {
                  String qualifiedMPId = ModelUtils.getQualifiedId(mp);
                  ParticipantDepartmentPair mpId = new ParticipantDepartmentPair(qualifiedMPId, depOid); 
                  ParticipantStatistics mpEntry = mpStatistics.get(mpId);
                  if (null == mpEntry)
                  {
                     mpEntry = new ParticipantStatistics(mp.getId(), qualifiedMPId, depOid);
                     mpStatistics.put(mpId, mpEntry);
                  }

                  // grant only once in case of multiple model versions
                  Set<ParticipantDepartmentPair> grantedRoles = linkedRoles.get(userOid);
                  if ( !grantedRoles.contains(mpId))
                  {
                     grantedRoles.add(mpId);
                     
                     userEntry.nGrants += 1;
                     mpEntry.nUsers += 1;
                  }
               }
            }
         }
      });

      retrieveWorklistStatistics(userStatistics, mpStatistics);
      retrieveLoginStatus(userStatistics);
      
      for (Iterator<User> i = users.iterator(); i.hasNext();)
      {
         User user = i.next();

         final UserStatistics userEntry = userStatistics.get(user.getOID());

         Set<ParticipantDepartmentPair> grantedRoles = linkedRoles.get(user.getOID());
         for (Iterator<ParticipantDepartmentPair> j = grantedRoles.iterator(); j.hasNext();)
         {
            final ParticipantStatistics mpEntry = mpStatistics.get(j.next());
            
            if (userEntry.loggedIn)
            {
               mpEntry.nLoggedInUsers += 1;
            }
            
            userEntry.nSharedWorkitems += mpEntry.nWorkitems;
         }
      }

      return new WorklistStatisticsResult(wsq, users, userStatistics, mpStatistics);
   }

   private Users initializeUsers(WorklistStatisticsQuery wsq,
         Map<Long, UserStatistics> userStatistics,
         Map<Long, Set<ParticipantDepartmentPair>> linkedRoles)
   {
      Users users = QueryServiceUtils.evaluateUserQuery(wsq);

      for (int i = 0; i < users.size(); ++i)
      {
         User user = (User) users.get(i);

         Long userOid = user.getOID();
         if ( !userStatistics.containsKey(userOid))
         {
            userStatistics.put(userOid, new UserStatistics(userOid));
         }

         if ( !linkedRoles.containsKey(userOid))
         {
            linkedRoles.put(userOid, CollectionUtils.<ParticipantDepartmentPair>newSet());
         }
      }
      
      return users;
   }

   private void retrieveWorklistStatistics(
         final Map<Long, UserStatistics> userStatictics,
         final Map<ParticipantDepartmentPair, ParticipantStatistics> mpStatistics)
   {
      // select any activity associated with a worklist
      QueryDescriptor sqlQuery = QueryDescriptor.from(WorkItemBean.class)
            .select(
                  WorkItemBean.FR__PERFORMER_KIND,
                  WorkItemBean.FR__PERFORMER,
                  WorkItemBean.FR__DEPARTMENT,
                  Functions.rowCount());

      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);
      if (!singlePartition)
      {
         // restrict to current partition
         sqlQuery.innerJoin(ModelPersistorBean.class)
               .on(WorkItemBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
               .where(Predicates.isEqual(
                     ModelPersistorBean.FR__PARTITION,
                     SecurityProperties.getPartitionOid()));
      }

      sqlQuery.groupBy(WorkItemBean.FR__PERFORMER_KIND, WorkItemBean.FR__PERFORMER, 
            WorkItemBean.FR__DEPARTMENT);
      sqlQuery.orderBy(WorkItemBean.FR__PERFORMER_KIND, WorkItemBean.FR__PERFORMER, 
            WorkItemBean.FR__DEPARTMENT);

      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         public void handleRow(ResultSet rs) throws SQLException
         {
            PerformerType performerKind = PerformerType.get(rs.getInt(1));
            long performerOid = rs.getLong(2);
            long depOid = rs.getLong(3);
            int nEntries = rs.getInt(4);

            if (PerformerType.ModelParticipant.equals(performerKind))
            {
               IModelParticipant mp = StatisticsModelUtils.findModelParticipant(performerOid);
               String fqId = ModelUtils.getQualifiedId(mp);
               
               ParticipantDepartmentPair mpIdP = new ParticipantDepartmentPair(fqId, depOid);

               ParticipantStatistics statistics = mpStatistics.get(mpIdP);
               
               if ((null == statistics) && (null != fqId))
               {
                  statistics = new ParticipantStatistics(mp.getId(), fqId, depOid);
                  mpStatistics.put(mpIdP, statistics);
               }

               statistics.nWorkitems += nEntries;
            }
            else if (PerformerType.User.equals(performerKind))
            {
               UserStatistics statistics = (UserStatistics) userStatictics.get(performerOid);
               
               if (null == statistics)
               {
                  statistics = new UserStatistics(performerOid);
                  userStatictics.put(performerOid, statistics);
               }
               
               statistics.nPrivateWorkitems += nEntries;
            }
         }
      });
   }
   
   private void retrieveLoginStatus(final Map<Long, UserStatistics> userStatistics)
   {
      long currentTime = Calendar.getInstance().getTimeInMillis();

      QueryDescriptor sqlQuery = QueryDescriptor.from(UserSessionBean.class)
            .selectDistinct(UserSessionBean.FIELD__USER)
            .where(Predicates.andTerm(
                  Predicates.lessOrEqual(UserSessionBean.FR__START_TIME, currentTime),
                  Predicates.greaterThan(UserSessionBean.FR__EXPIRATION_TIME, currentTime)));
      
      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         public void handleRow(ResultSet rs) throws SQLException
         {
            long userOid = rs.getLong(1);

            UserStatistics statistics = userStatistics.get(userOid);
            if (null != statistics)
            {
               statistics.loggedIn = true;
            }
         }
      });
   }

}
