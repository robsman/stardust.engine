package org.eclipse.stardust.engine.api.runtime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailTransitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;

public class AuditTrailHealthReportGenerator
{
   private org.eclipse.stardust.engine.core.persistence.jdbc.Session session;

   public AuditTrailHealthReportGenerator()
   {
      session = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
   }

   public static AuditTrailHealthReport getReport(boolean countOnly)
   {
      AuditTrailHealthReportGenerator generator = new AuditTrailHealthReportGenerator();
      AuditTrailHealthReport report = null;
      if (countOnly)
      {
         report = generator.createCountOnlyReport();
      }
      else
      {
         report = generator.createUnrestrictedReport();
      }
      return report;
   }

   private AuditTrailHealthReport createCountOnlyReport()
   {
      long nPendingPiCompletes = getNumberOfProcessInstancesLackingCompletion();
      long nPendingPiAborts = getNumberOfProcessInstancesLackingAbortion();
      long nPendingAiAborts = getNumberOfActivityInstancesLackingAbortion();
      long nPisHavingCrashedAis = getNumberOfProcessInstancesHavingCrashedActivities();
      long nPisHavingCrashedThreads = getProcessInstancesHavingCrashedThreadsSet().size();
      long nPisHavingCrashedEventBindings = EventUtils.countDeactiveEventBindings();

      return new AuditTrailHealthReport(nPendingPiCompletes, nPendingPiAborts,
            nPendingAiAborts, nPisHavingCrashedAis, nPisHavingCrashedThreads,
            nPisHavingCrashedEventBindings);
   }

   private AuditTrailHealthReport createUnrestrictedReport()
   {
      Set<Long> nPendingPiCompletesSet = getProcessInstancesLackingCompletionSet();
      Set<Long> nPendingPiAbortsSet = getProcessInstancesLackingAbortionSet();
      Set<Long> nPendingAiAbortsSet = getActivityInstancesLackingAbortionSet();
      Set<Long> nPisHavingCrashedAisSet = getProcessInstancesHavingCrashedActivitiesSet();
      Set<Long> nPisHavingCrashedThreadsSet = getProcessInstancesHavingCrashedThreadsSet();
      Set<Long> nPisHavingCrashedEventBindingsSet = EventUtils.getDeactiveEventBindings();
      return new AuditTrailHealthReport(nPendingPiCompletesSet, nPendingPiAbortsSet,
            nPendingAiAbortsSet, nPisHavingCrashedAisSet, nPisHavingCrashedThreadsSet,
            nPisHavingCrashedEventBindingsSet);
   }

   private long getNumberOfActivityInstancesLackingAbortion()
   {
      return getNumberOfQuery(getActivityInstancesLackingAbortionQuery());
   }

   private long getNumberOfProcessInstancesLackingAbortion()
   {
      return getNumberOfQuery(getProcessInstancesLackingAbortionQuery());
   }

   private long getNumberOfProcessInstancesLackingCompletion()
   {
      return getNumberOfQuery(getProcessInstancesLackingCompletionQuery());
   }

   private long getNumberOfProcessInstancesHavingCrashedActivities()
   {
      return getNumberOfQuery(getProcessInstancesHavingCrashedActivitiesQuery());
   }

   private long getNumberOfQuery(QueryDescriptor query)
   {
      return session.getCount(query.getType(), query.getQueryExtension());
   }

   private Set<Long> getProcessInstancesHavingCrashedThreadsSet()
   {
      return calculateCrashedPIs();
   }

   private Set<Long> calculateCrashedPIs()
   {
      QueryDescriptor q = getProcessInstancesHavingCrashedThreadsQuery();
      HashMap<Long, Set<Long>> zeroTargetTokensPerProcessInstance = new HashMap<Long, Set<Long>>();
      HashMap<Long, Long> transitionOidToModel = new HashMap<Long, Long>();
      Set<Long> involvedTransitionOids = new HashSet<Long>();
      ResultSet resultSet = session.executeQuery(q.getType(), q.getQueryExtension());
      try
      {
         while (resultSet.next())
         {
            Long oid = resultSet.getLong(1);
            Long transitionOid = resultSet.getLong(2);
            Long modelOid = resultSet.getLong(3);

            involvedTransitionOids.add(transitionOid);
            transitionOidToModel.put(transitionOid, modelOid);

            Set<Long> transitionOids = zeroTargetTokensPerProcessInstance.get(oid);
            if (transitionOids == null)
            {
               transitionOids = new HashSet<Long>();
            }
            transitionOids.add(transitionOid);
            zeroTargetTokensPerProcessInstance.put(oid, transitionOids);
         }
      }
      catch (SQLException e)
      {
         throw new PublicException("Failed evaluating recovery status.", e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }

      // Retrieve targetActivityOid from transitionOid.
      Map<Long, Long> targetActivityOidPerTransitionOid = new HashMap<Long, Long>();
      for (Long transitionOid : involvedTransitionOids)
      {
         AuditTrailTransitionBean transition = AuditTrailTransitionBean.findByOid(
               transitionOid, transitionOidToModel.get(transitionOid));
         if (transition != null)
         {
            targetActivityOidPerTransitionOid.put(transitionOid,
                  transition.getTargetActivity());
         }
      }

      // Retrieve count of all source activities of a target activity.
      Map<Long, Long> countOfTransitionSourcesPerTargetActivity = new HashMap<Long, Long>();
      for (Long targetActivityOid : targetActivityOidPerTransitionOid.values())
      {
         QueryDescriptor query = QueryDescriptor
               .from(AuditTrailTransitionBean.class)
               .select(AuditTrailTransitionBean.FR__OID)
               .where(
                     Predicates.isEqual(AuditTrailTransitionBean.FR__TGT_ACTIVITY,
                           targetActivityOid));
         long transitionCount = session.getCount(AuditTrailTransitionBean.class,
               query.getQueryExtension());
         countOfTransitionSourcesPerTargetActivity
               .put(targetActivityOid, transitionCount);
      }
      QueryUtils.closeResultSet(resultSet);

      // Iterate over all existing trans_token entries having '0' targetActivityInstance
      // to see if ALL source activities are contained.
      Set<Long> crashedPIs = new HashSet<Long>();
      for (Map.Entry<Long, Set<Long>> entry : zeroTargetTokensPerProcessInstance
            .entrySet())
      {
         Set<Long> transitionOids = entry.getValue();

         Map<Long, Long> countOfInvolvedActivityOids = new HashMap<Long, Long>();
         for (Long transitionOid : transitionOids)
         {
            Long activityOid = targetActivityOidPerTransitionOid.get(transitionOid);
            Long count = countOfInvolvedActivityOids.get(activityOid);
            if (count == null)
            {
               count = Long.valueOf(0);
            }
            count++;
            countOfInvolvedActivityOids.put(activityOid, count);
         }

         for (Map.Entry<Long, Long> entry2 : countOfInvolvedActivityOids.entrySet())
         {
            // Only crashed candidate if trans_token targetActivityInstanceOids of ALL
            // source activities are '0'
            if (countOfTransitionSourcesPerTargetActivity.get(entry2.getKey()) == entry2
                  .getValue())
            {
               crashedPIs.add(entry.getKey());
            }
         }
      }
      return crashedPIs;
   }

   private QueryDescriptor getProcessInstancesHavingCrashedThreadsQuery()
   {
      QueryDescriptor q = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(
                  new Column[] {
                        ProcessInstanceBean.FR__OID, TransitionTokenBean.FR__TRANSITION,
                        TransitionTokenBean.FR__MODEL})
            .where(
                  Predicates.notInList(ProcessInstanceBean.FR__STATE, new int[] {
                        ProcessInstanceState.COMPLETED, ProcessInstanceState.ABORTED}));
      Join join = new Join(TransitionTokenBean.class).on(ProcessInstanceBean.FR__OID,
            TransitionTokenBean.FIELD__PROCESS_INSTANCE).andOnConstant(
            TransitionTokenBean.FR__TARGET, "0");
      q.getQueryExtension().addJoin(join);
      return q;
   }

   private QueryDescriptor getProcessInstancesHavingCrashedActivitiesQuery()
   {
      QueryDescriptor q = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(ProcessInstanceBean.FR__OID)
            .where(
                  Predicates.andTerm(
                        Predicates.isNotNull(TransitionTokenBean.FR__PROCESS_INSTANCE),
                        Predicates.notInList(ProcessInstanceBean.FR__STATE, new int[] {
                              ProcessInstanceState.COMPLETED,
                              ProcessInstanceState.ABORTED})));
      Join join = new Join(TransitionTokenBean.class).on(ProcessInstanceBean.FR__OID,
            TransitionTokenBean.FIELD__PROCESS_INSTANCE).andOnConstant(
            TransitionTokenBean.FR__IS_CONSUMED, "0");
      join.setRequired(false);
      Join join2 = new Join(ActivityInstanceBean.class).on(
            TransitionTokenBean.FR__TARGET, ActivityInstanceBean.FIELD__OID).andWhere(
            Predicates.inList(ActivityInstanceBean.FR__STATE, new int[] {
                  ActivityInstanceState.CREATED, ActivityInstanceState.INTERRUPTED}));
      q.getQueryExtension().addJoin(join).addJoin(join2);
      return q;
   }

   private QueryDescriptor getProcessInstancesLackingCompletionQuery()
   {
      QueryDescriptor q = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(ProcessInstanceBean.FR__OID)
            .where(
                  Predicates.andTerm(Predicates
                        .isNull(TransitionTokenBean.FR__PROCESS_INSTANCE), Predicates
                        .notInList(ProcessInstanceBean.FR__STATE,
                              new int[] {
                                    ProcessInstanceState.COMPLETED,
                                    ProcessInstanceState.ABORTED,
                                    ProcessInstanceState.ABORTING})));
      Join join = new Join(TransitionTokenBean.class).on(ProcessInstanceBean.FR__OID,
            TransitionTokenBean.FIELD__PROCESS_INSTANCE).andOnConstant(
            TransitionTokenBean.FR__IS_CONSUMED, "0");
      join.setRequired(false);
      q.getQueryExtension().addJoin(join);
      return q;
   }

   private Set<Long> getProcessInstancesHavingCrashedActivitiesSet()
   {
      return getQueryResult(getProcessInstancesHavingCrashedActivitiesQuery());
   }

   private Set<Long> getProcessInstancesLackingCompletionSet()
   {
      return getQueryResult(getProcessInstancesLackingCompletionQuery());
   }

   private Set<Long> getQueryResult(QueryDescriptor q)
   {
      return getQueryResult(q, ProcessInstanceBean.FIELD__OID);
   }

   private Set<Long> getQueryResult(QueryDescriptor q, String columnLabel)
   {
      Set<Long> pis = CollectionUtils.newSet();
      ResultSet resultSet = session.executeQuery(q);
      try
      {
         while (resultSet.next())
         {
            pis.add(resultSet.getLong(columnLabel));
         }
      }
      catch (SQLException e)
      {
         throw new PublicException("Failed evaluating recovery status.", e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
      return pis;
   }

   private Set<Long> getProcessInstancesLackingAbortionSet()
   {
      return getQueryResult(getProcessInstancesLackingAbortionQuery());
   }

   private QueryDescriptor getProcessInstancesLackingAbortionQuery()
   {
      QueryDescriptor q = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(ProcessInstanceBean.FR__OID)
            .where(
                  Predicates.inList(ProcessInstanceBean.FR__STATE,
                        new int[] {ProcessInstanceState.ABORTING}));
      return q;
   }

   private Set<Long> getActivityInstancesLackingAbortionSet()
   {
      return getQueryResult(getActivityInstancesLackingAbortionQuery(),
            ActivityInstanceBean.FIELD__PROCESS_INSTANCE);
   }

   private QueryDescriptor getActivityInstancesLackingAbortionQuery()
   {
      QueryDescriptor q = QueryDescriptor
            .from(ActivityInstanceBean.class)
            .select(ActivityInstanceBean.FR__PROCESS_INSTANCE)
            .where(
                  Predicates.inList(ActivityInstanceBean.FR__STATE,
                        new int[] {ActivityInstanceState.ABORTING}));
      return q;
   }

}
