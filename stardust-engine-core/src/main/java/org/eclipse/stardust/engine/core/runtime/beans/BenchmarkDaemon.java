/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.greaterThan;
import static org.eclipse.stardust.engine.core.persistence.Predicates.notEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryDescriptor.from;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.internal.SyncBenchmarksToDiskAction;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class BenchmarkDaemon implements IDaemon
{

   private static final Logger trace = LogManager.getLogger(BenchmarkDaemon.class);

   public static final Logger daemonLogger = RuntimeLog.DAEMON;

   public static final String ID = AdministrationService.BENCHMARK_DAEMON;

   private Map<Long, IBenchmarkEvaluator> evaluatorMap;

   private long currentPiOid = 0;

   private int nInstance;

   @Override
   public ExecutionResult execute(long batchSize)
   {
      daemonLogger.info("Benchmark Daemon, perform synchronisation.");

      ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);

      nInstance = 0;

      final IJobManager jobManager = factory.getJobManager();

      try
      {
         long lastPiOid = currentPiOid;

         Map<Long, Integer> benchmarkPIMap = CollectionUtils.newMap();
         Map<Long, Integer> benchmarkAIMap = CollectionUtils.newMap();

         // Create update map
         Map<Long, PiBenchmarkDetails> benchmarkUpdateMap = this.getBenchmarkUpdateMap(batchSize);

         Set<Long> updateList = benchmarkUpdateMap.keySet();

         for (Iterator i = updateList.iterator(); i.hasNext();)
         {
            if (this.evaluatorMap == null)
            {
               evaluatorMap = CollectionUtils.newMap();
            }

            long oid = (Long) i.next();

            PiBenchmarkDetails benchmarkDetails = benchmarkUpdateMap.get(oid);

            IBenchmarkEvaluator evaluator;

            if (evaluatorMap.containsKey(benchmarkDetails.getBenchmarkOid()))
            {
               evaluator = evaluatorMap.get(benchmarkDetails.getBenchmarkOid());
            }
            else
            {
               evaluator = new BenchmarkEvaluator(benchmarkUpdateMap.get(oid)
                     .getBenchmarkOid());
               evaluatorMap.put(benchmarkDetails.getBenchmarkOid(), evaluator);
            }

            trace.info("Adding PI <" + oid + "> to benchmarkPIMap");
            benchmarkPIMap.put(oid, evaluator.getBenchmarkForProcessInstance(oid));

            Map<Long, String> aiBenchmarkMap = benchmarkDetails.getAiBenchmarkMap();
            Set<Long> aiUpdateList = aiBenchmarkMap.keySet();

            for (Iterator ai = aiUpdateList.iterator(); ai.hasNext();)
            {
               long aiOid = (Long) ai.next();

               benchmarkAIMap.put(
                     oid,
                     evaluator.getBenchmarkForActivityInstance(aiOid,
                           aiBenchmarkMap.get(aiOid)));
            }
            lastPiOid = oid;
         }

         jobManager.performSynchronousJob(new SyncBenchmarksToDiskAction(benchmarkPIMap,
               benchmarkAIMap));
         
         currentPiOid = lastPiOid;
         nInstance = nInstance + benchmarkPIMap.size();

      }
      catch (Exception e)
      {
      }
      finally
      {
         factory.release(jobManager);
      }

      if (nInstance < batchSize)
      {
         this.currentPiOid = 0;
         return IDaemon.WORK_DONE;
      }
      return IDaemon.WORK_PENDING;
   }

   private Map<Long, PiBenchmarkDetails> getBenchmarkUpdateMap(long batchSize)
   {
      Map<Long, PiBenchmarkDetails> piBenchmarkMap = CollectionUtils.newMap();

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      QueryDescriptor query = from(ActivityInstanceBean.class).select(
            ActivityInstanceBean.FR__OID, AuditTrailActivityBean.FR__ID,
            ProcessInstanceBean.FR__OID, ProcessInstanceBean.FR__BENCHMARK_OID);
      query.getQueryExtension()

            .addJoin(
                  new Join(ModelPersistorBean.class).on(ActivityInstanceBean.FR__MODEL,
                        ModelPersistorBean.FIELD__OID).where(
                        Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                              SecurityProperties.getPartitionOid())))
            .addJoin(
                  new Join(AuditTrailActivityBean.class).on(
                        ActivityInstanceBean.FR__ACTIVITY,
                        AuditTrailActivityBean.FIELD__OID))

            .addJoin(
                  new Join(ProcessInstanceBean.class).on(
                        ActivityInstanceBean.FR__PROCESS_INSTANCE,
                        ProcessInstanceBean.FIELD__OID));

      query.getQueryExtension().

      setWhere(
            andTerm(
                  andTerm(
                        andTerm(
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.ABORTED),
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.COMPLETED)),
                        greaterThan(ProcessInstanceBean.FR__BENCHMARK_OID, 0)),
                  greaterThan(ProcessInstanceBean.FR__OID, this.currentPiOid)));

      ResultSet rs = session.executeQuery(query);

      try
      {
         int copiedRows = 0;
         while (rs.next() && copiedRows < batchSize)
         {
            long oid = rs.getLong(ProcessInstanceBean.FIELD__OID);
            long aiOid = rs.getLong(ActivityInstanceBean.FIELD__OID);
            String activityId = rs.getString(AuditTrailActivityBean.FIELD__ID);
            long benchmarkOid = rs.getLong(ProcessInstanceBean.FIELD__BENCHMARK_OID);

            if ( !piBenchmarkMap.containsKey(oid))
            {
               trace.info("Add PI with OID <" + oid + "> to update map");
               piBenchmarkMap.put(oid, new PiBenchmarkDetails(benchmarkOid));
               copiedRows++ ;
            }

            piBenchmarkMap.get(oid).addAiBenchmark(aiOid, activityId);
         }
      }
      catch (SQLException sqlex)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_COULD_NOT_RETRIEVE_ACTIVITY_INSTANCE_FOR_CRITICALITY_UPDATE.raise());
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }

      return piBenchmarkMap;
   }

   @Override
   public String getType()
   {
      return ID;
   }

   @Override
   public long getDefaultPeriodicity()
   {
      return 5;
   }

   private class PiBenchmarkDetails
   {
      private long benchmarkOid;

      private Map<Long, String> aiBenchmarkMap;

      public PiBenchmarkDetails(long benchmarkOid)
      {
         this.benchmarkOid = benchmarkOid;
         aiBenchmarkMap = CollectionUtils.newHashMap();
      }

      public Map<Long, String> getAiBenchmarkMap()
      {
         return this.aiBenchmarkMap;
      }

      public void addAiBenchmark(long aiOid, String activityId)
      {
         this.aiBenchmarkMap.put(aiOid, activityId);
      }

      public long getBenchmarkOid()
      {
         return this.benchmarkOid;
      }

   }

}
