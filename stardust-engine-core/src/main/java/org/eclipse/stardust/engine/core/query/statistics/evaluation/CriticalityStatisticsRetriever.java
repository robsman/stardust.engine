package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.utils.IResultSetTemplate;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.utils.*;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;
import org.eclipse.stardust.engine.core.spi.query.IActivityInstanceQueryEvaluator;

/**
 *
 * @author thomas.wolfram
 *
 */
public class CriticalityStatisticsRetriever implements IActivityInstanceQueryEvaluator
{

   private static Column[] columns = {
         ActivityInstanceBean.FR__OID, ActivityInstanceBean.FR__MODEL,
         ActivityInstanceBean.FR__ACTIVITY, ProcessInstanceBean.FR__PRIORITY,
         ActivityInstanceBean.FR__START_TIME,
         ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
         ActivityInstanceBean.FR__CURRENT_PERFORMER,
         ActivityInstanceBean.FR__CURRENT_USER_PERFORMER,
         ActivityInstanceBean.FR__CURRENT_DEPARTMENT,
         ActivityInstanceBean.FR__CRITICALITY};

   public CustomActivityInstanceQueryResult evaluateQuery(
         CustomActivityInstanceQuery query)
   {
      if ( !(query instanceof CriticalityStatisticsQuery))
      {
         throw new InternalException(
               "Illegal argument: the query must be an instance of "
                     + CriticalityStatisticsQuery.class.getName());
      }

      final CriticalityStatisticsQuery csq = (CriticalityStatisticsQuery) query;
      final CriticalityStatisticsResult result = new CriticalityStatisticsResult(csq);

      final Date now = new Date();

      final Set<Long> processRtOidFilter = StatisticsQueryUtils.extractProcessFilter(csq.getFilter());
      for (Iterator<List<Long>> iterator = getSubLists(processRtOidFilter, 100).iterator(); iterator.hasNext();)
      {
         List<Long> processRtOidSubList = iterator.next();

         QueryDescriptor sqlQuery = QueryDescriptor.from(ActivityInstanceBean.class) //
               .select(columns);

         // join PI
         Join piJoin = sqlQuery.innerJoin(ProcessInstanceBean.class)
               //
               .on(ActivityInstanceBean.FR__PROCESS_INSTANCE,
                     ProcessInstanceBean.FIELD__OID)
               .where(
                     Predicates.inList(ProcessInstanceBean.FR__PROCESS_DEFINITION,
                           processRtOidSubList));

         // restrict PIs to current partition
         /*
          * sqlQuery.innerJoin(ModelPersistorBean.class) //
          * .on(ActivityInstanceBean.FR__MODEL, ModelPersistorBean.FIELD__OID)
          * .andWhere(Predicates.isEqual( // ModelPersistorBean.FR__PARTITION, //
          * SecurityProperties.getPartitionOid()));
          */

         MultiPartPredicateTerm predicate = new AndTerm();
         boolean singlePartition = Parameters.instance().getBoolean(
               KernelTweakingProperties.SINGLE_PARTITION, false);
         if ( !singlePartition)
         {
            List<Integer> modelRtOids = new ArrayList<Integer>();
            for (Iterator iter = ModelManagerFactory.getCurrent().getAllModels(); iter.hasNext();)
            {
               IModel model = (IModel) iter.next();
               modelRtOids.add(model.getModelOID());
            }
            PredicateTerm modelRtOidsPredicate = Predicates.inList(
                  ActivityInstanceBean.FR__MODEL, modelRtOids);
            predicate.add(modelRtOidsPredicate);
         }

         // count only non-terminated PIs
         ComparisonTerm activityStateTerm = Predicates.notInList(
               ActivityInstanceBean.FR__STATE, new int[] {
                     ActivityInstanceState.COMPLETED, ActivityInstanceState.ABORTED});
         predicate.add(activityStateTerm);

         final AttributedScopedFilter activityCriticalityFilter = extractCriticalityFiler(csq.getFilter());

         if (activityCriticalityFilter != null)
         {
            ComparisonTerm activityCriticalityTerm = Predicates.genericComparison(
                  ActivityInstanceBean.FR__CRITICALITY, activityCriticalityFilter);
            if (activityCriticalityTerm != null)
            {
               predicate.add(activityCriticalityTerm);
            }
         }

         sqlQuery.where(predicate);

         final AuthorizationContext ctx = AuthorizationContext.create(ClientPermission.READ_ACTIVITY_INSTANCE_DATA);
         final boolean guarded = Parameters.instance().getBoolean("QueryService.Guarded",
               true)
               && !ctx.isAdminOverride();
         final AbstractAuthorization2Predicate authPredicate = new AbstractAuthorization2Predicate(
               ctx)
         {
         };

         authPredicate.addRawPrefetch(sqlQuery,
               piJoin.fieldRef(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE));

         StatisticsQueryUtils.executeQuery(sqlQuery, new IResultSetTemplate()
         {

            private final Date tsAiStart = new Date();

            public void handleRow(ResultSet rs) throws SQLException
            {
               authPredicate.accept(rs);

               long aiOid = rs.getLong(1);
               long modelOid = rs.getLong(2);
               long activityRtOid = rs.getLong(3);
               int priority = rs.getInt(4);
               long startTime = rs.getLong(5);
               long scopePiOid = rs.getLong(6);
               long currentPerformer = rs.getLong(7);
               long currentUserPerformer = rs.getLong(8);
               long department = rs.getLong(9);
               double criticality = rs.getDouble(10);

               ctx.setActivityDataWithScopePi(scopePiOid, activityRtOid, modelOid,
                     currentPerformer, currentUserPerformer, department);
               if ( !guarded || Authorization2.hasPermission(ctx))
               {
                  tsAiStart.setTime(startTime);
                  IActivity activity = (IActivity) ctx.getModelElement();

                  String pdId = ModelUtils.getQualifiedId(activity.getProcessDefinition());
                  String aId = ModelUtils.getQualifiedId(activity);
                  result.addInstances(pdId, aId, criticality, aiOid);
               }

            }
         });
      }

      return result;
   }

   private List<List<Long>> getSubLists(Set<Long> processRtOidFilter, int maxProcessOids)
   {
      List<Long> processRtOids = (processRtOidFilter != null) ? new ArrayList<Long>(processRtOidFilter) : new ArrayList<Long>();
      List<List<Long>> processRtOidSubLists = new ArrayList<List<Long>>();
      for (int i = 0; i < processRtOids.size(); i += maxProcessOids)
      {
         int toIndex = processRtOids.size() - i <= maxProcessOids
               ? processRtOids.size()
               : i + maxProcessOids;
         List<Long> subList = processRtOids.subList(i, toIndex);
         processRtOidSubLists.add(subList);
      }
      return processRtOidSubLists;
   }

   private AttributedScopedFilter extractCriticalityFiler(FilterAndTerm filter)
   {

      if ( !filter.getParts().isEmpty())
      {
         for (Iterator i = filter.getParts().iterator(); i.hasNext();)
         {
            FilterCriterion criterion = (FilterCriterion) i.next();
            if (criterion instanceof BinaryOperatorFilter)
            {
               BinaryOperatorFilter comparison = (BinaryOperatorFilter) criterion;
               if (ActivityInstanceQuery.class.isAssignableFrom(comparison.getScope())
                     && ActivityInstanceBean.FIELD__CRITICALITY.equals(comparison.getAttribute()))
               {
                  return comparison;
               }
            }
            else if (criterion instanceof TernaryOperatorFilter)
            {
               TernaryOperatorFilter comparison = (TernaryOperatorFilter) criterion;
               if (ActivityInstanceQuery.class.isAssignableFrom(comparison.getScope())
                     && ActivityInstanceBean.FIELD__CRITICALITY.equals(comparison.getAttribute()))
               {
                  return comparison;
               }
            }
         }

      }

      return null;
   }

}
