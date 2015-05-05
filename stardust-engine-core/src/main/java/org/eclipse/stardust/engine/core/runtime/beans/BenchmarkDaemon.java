package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.greaterThan;
import static org.eclipse.stardust.engine.core.persistence.Predicates.notEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryDescriptor.from;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

public class BenchmarkDaemon implements IDaemon
{

   private static final Logger trace = LogManager.getLogger(BenchmarkDaemon.class);

   public static final Logger daemonLogger = RuntimeLog.DAEMON;

   public static final String ID = AdministrationService.BENCHMARK_DAEMON;

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
         List updateList = getPiUpdateList(batchSize);

         
          Map<Long, Integer> benchmarkPIMap = CollectionUtils.newMap();
          

         // Create update map
         for (Iterator i = updateList.iterator(); i.hasNext();)
         {
            long oid = (Long) i.next();

            IBenchmarkEvaluator evaluator = new BenchmarkEvaluator(
                  ProcessInstanceBean.findByOID(oid));
                        
            benchmarkPIMap.put(oid, evaluator.getBenchmarkForProcessInstance());
            lastPiOid = oid;
         }

         // jobManager.performSynchronousJob(new
         // SyncCriticalitiesToDiskAction(criticalityMap));
         currentPiOid = lastPiOid;
         // nInstance = nInstance + criticalityMap.size();

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

   private List getPiUpdateList(long batchSize)
   {
      List updateList = CollectionUtils.newList();

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      QueryDescriptor query = from(ProcessInstanceBean.class).select(
            ProcessInstanceBean.FR__OID, ProcessInstanceBean.FR__MODEL);
      query.getQueryExtension()
            .addJoin(
                  new Join(ModelPersistorBean.class).on(ProcessInstanceBean.FR__MODEL,
                        ModelPersistorBean.FIELD__OID).where(
                        Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                              SecurityProperties.getPartitionOid())))


            .setWhere(
                  andTerm(
                        andTerm(
                              notEqual(ProcessInstanceBean.FR__STATE,
                                    ProcessInstanceState.ABORTED),
                              notEqual(ProcessInstanceBean.FR__STATE,
                                    ProcessInstanceState.COMPLETED)),
                        greaterThan(ProcessInstanceBean.FR__BENCHMARK_OID, 0)))
            .addOrderBy(ProcessInstanceBean.FR__OID, true);

      ResultSet rs = session.executeQuery(query);

      try
      {
         int copiedRows = 0;
         while (rs.next() && copiedRows < batchSize)
         {
            long oid = rs.getLong(ProcessInstanceBean.FIELD__OID);
            updateList.add(oid);
            copiedRows++ ;
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

      return updateList;
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

}
