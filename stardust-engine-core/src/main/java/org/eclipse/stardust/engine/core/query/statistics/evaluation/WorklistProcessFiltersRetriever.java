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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.ProcessCumulationPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistProcessFiltersQuery;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.*;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IActivityInstanceQueryEvaluator;


/**
 * @author florin.herinean
 * @version $Revision: $
 */
public class WorklistProcessFiltersRetriever implements IActivityInstanceQueryEvaluator
{
   public CustomActivityInstanceQueryResult evaluateQuery(CustomActivityInstanceQuery query)
   {
      if (!(query instanceof WorklistProcessFiltersQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + WorklistProcessFiltersQuery.class.getName());
      }

      final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_ACTIVITY_INSTANCE_DATA);
      final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded", true)
            && !ctx.isAdminOverride();
      final AbstractAuthorization2Predicate predicate = new AbstractAuthorization2Predicate(ctx) {};

      WorklistProcessFiltersQuery wpfq = (WorklistProcessFiltersQuery) query;

      QueryDescriptor sqlQuery = QueryDescriptor.from(WorkItemBean.class) //
            .select(new Column[] {
                  ProcessInstanceBean.FR__PROCESS_DEFINITION,
                  WorkItemBean.FR__MODEL,
                  WorkItemBean.FR__ACTIVITY,
                  WorkItemBean.FR__PERFORMER,
                  WorkItemBean.FR__PERFORMER_KIND,
                  WorkItemBean.FR__DEPARTMENT,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                  Functions.rowCount(),
                  })
            .groupBy(new FieldRef[] {
                  ProcessInstanceBean.FR__PROCESS_DEFINITION,
                  WorkItemBean.FR__MODEL,
                  WorkItemBean.FR__ACTIVITY,
                  WorkItemBean.FR__PERFORMER,
                  WorkItemBean.FR__PERFORMER_KIND,
                  WorkItemBean.FR__DEPARTMENT,
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
            });

      ParticipantInfo participant = wpfq.getParticipant();
      if (participant != null)
      {
         long performerOid = 0;
         int performerKind = PerformerType.NONE;
         if (participant instanceof UserInfo)
         {
            performerOid = ((UserInfo) participant).getOID();
            performerKind = PerformerType.USER;
         }
         else if (participant instanceof UserGroupInfo)
         {
            performerOid = -((UserGroupInfo) participant).getOID();
            performerKind = PerformerType.USER_GROUP;
         }
         else if (participant instanceof ModelParticipantInfo)
         {
            performerOid = ((ModelParticipantInfo) participant).getRuntimeElementOID();
            performerKind = PerformerType.MODEL_PARTICIPANT;
            if (performerOid == 0)
            {
               ModelManager current = ModelManagerFactory.getCurrent();
               IModelParticipant performer = current.findModelParticipant((ModelParticipantInfo) participant);
               performerOid = current.getRuntimeOid(performer);
            }
         }
         else
         {
            throw new InternalException("Unsupported participant type: " + participant.getClass());
         }

         ComparisonTerm performerTerm = Predicates.isEqual(WorkItemBean.FR__PERFORMER, performerOid);
         ComparisonTerm performerKindTerm = Predicates.isEqual(WorkItemBean.FR__PERFORMER_KIND, performerKind);
         DepartmentInfo department = participant instanceof ModelParticipantInfo ?
               ((ModelParticipantInfo) participant).getDepartment() : null;
         if (department != null)
         {
            ComparisonTerm departmentTerm = Predicates.isEqual(WorkItemBean.FR__DEPARTMENT, department.getOID());
            sqlQuery.where(Predicates.andTerm(performerTerm, performerKindTerm, departmentTerm));
         }
         else
         {
            sqlQuery.where(Predicates.andTerm(performerTerm, performerKindTerm));
         }
      }

      // join PI
      StatisticsQueryUtils.joinCumulationPi(ProcessCumulationPolicy.WITH_PI, sqlQuery,
            WorkItemBean.FR__PROCESS_INSTANCE);

      boolean singlePartition = Parameters.instance().getBoolean(
            KernelTweakingProperties.SINGLE_PARTITION, false);
      if (!singlePartition)
      {
         // restrict PIs to current partition
         sqlQuery.innerJoin(ModelPersistorBean.class) //
               .on(WorkItemBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
               .andWhere(Predicates.isEqual( //
                     ModelPersistorBean.FR__PARTITION, //
                     SecurityProperties.getPartitionOid()));
      }

      predicate.addRawPrefetch(sqlQuery, WorkItemBean.FR__SCOPE_PROCESS_INSTANCE);

      final WorklistProcessFiltersResult result = new WorklistProcessFiltersResult(wpfq);

      StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
      {
         public void handleRow(ResultSet rs) throws SQLException
         {
            predicate.accept(rs);

            long processRtOid = rs.getLong(1);
            long modelOid = rs.getLong(2);
            long activityRtOid = rs.getLong(3);
            long performerOid = rs.getLong(4);
            int performerKind = rs.getInt(5);
            long department = rs.getLong(6);
            long scopePiOid = rs.getLong(7);
            long count = rs.getLong(8);

            long currentPerformer = 0;
            long currentUserPerformer = 0;
            switch (performerKind)
            {
            case PerformerType.USER:
               currentPerformer = 0;
               currentUserPerformer = performerOid;
               break;
            case PerformerType.USER_GROUP:
               currentPerformer = -performerOid;
               currentUserPerformer = 0;
               break;
            case PerformerType.MODEL_PARTICIPANT:
               currentPerformer = performerOid;
               currentUserPerformer = 0;
               break;
            }

            ctx.setActivityDataWithScopePi(scopePiOid, activityRtOid, modelOid,
                  currentPerformer, currentUserPerformer, department);
            if (!guarded || Authorization2.hasPermission(ctx))
            {
               ModelManager manager = ctx.getModelManager();
               IProcessDefinition process = manager.findProcessDefinition(modelOid, processRtOid);
               String elementId = ModelUtils.getQualifiedId(process);
               result.update(elementId, count);
            }
         }
      });

      return result;
   }
}