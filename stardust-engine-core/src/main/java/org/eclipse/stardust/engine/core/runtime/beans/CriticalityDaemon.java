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
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.greaterThan;
import static org.eclipse.stardust.engine.core.persistence.Predicates.notEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryDescriptor.from;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.internal.SyncCriticalitiesToDiskAction;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 *
 * @author thomas.wolfram
 *
 */
public class CriticalityDaemon implements IDaemon
{
   private static final Logger trace = LogManager.getLogger(CriticalityDaemon.class);
   public static final Logger daemonLogger = RuntimeLog.DAEMON;

   public static final String ID = AdministrationService.CRITICALITY_DAEMON;

   private long currentAiOid = 0;

   private int nInstance;

   public CriticalityDaemon()
   {
   }

   public ExecutionResult execute(final long batchSize)
   {

      ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);

      nInstance = 0;

      final IJobManager jobManager = factory.getJobManager();

      try
      {
         long lastAiOid = currentAiOid;
         List updateList = getAiUpdateList(batchSize);
         Map criticalityMap = CollectionUtils.newMap();

         // Create update map
         for (Iterator i = updateList.iterator(); i.hasNext();)
         {
            long oid = (Long) i.next();
            criticalityMap.put(oid, CriticalityEvaluator.recalculateCriticality(oid));
            lastAiOid = oid;
         }

         daemonLogger.info("Criticality Daemon, perform synchronisation.");
         jobManager.performSynchronousJob(new SyncCriticalitiesToDiskAction(
               criticalityMap));
         currentAiOid = lastAiOid;
         nInstance = nInstance + criticalityMap.size();

      }
      catch (Exception e)
      {

         trace.warn("Failed to execute batch update for criticalities. Trying to update by single activity instances.");

         // Perform transactions in single jobs
         long lastAiOid = currentAiOid;
         List updateList = getAiUpdateList(batchSize);
         for (Iterator i = updateList.iterator(); i.hasNext();)
         {
            try
            {
               long oid = (Long) i.next();
               Map criticalityMap = CollectionUtils.newMap();
               criticalityMap.put(oid, CriticalityEvaluator.recalculateCriticality(oid));

               daemonLogger.info("Criticality Daemon, perform synchronisation.");
               jobManager.performSynchronousJob(new SyncCriticalitiesToDiskAction(
                     criticalityMap));
               lastAiOid = oid;
            }
            catch (Exception ex)
            {
               AuditTrailLogger.getInstance(LogCode.DAEMON)
                     .warn(MessageFormat.format(
                           "Failed to recalculate criticality for activity instance {0}, Skipping this instance.",
                           new Object[] {lastAiOid}, ex));
            }
            nInstance++ ;
            currentAiOid = lastAiOid;
         }
      }
      finally
      {
         factory.release(jobManager);
      }

      if (nInstance < batchSize)
      {
         this.currentAiOid = 0;
         return IDaemon.WORK_DONE;
      }
      return IDaemon.WORK_PENDING;
   }

   private List getAiUpdateList(long batchSize)
   {
      List updateList = CollectionUtils.newList();

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      QueryDescriptor query = from(ActivityInstanceBean.class).select(
            ActivityInstanceBean.FR__OID, ActivityInstanceBean.FR__MODEL);
      query.getQueryExtension()
            .addJoin(
                  new Join(ModelPersistorBean.class).on(ActivityInstanceBean.FR__MODEL,
                        ModelPersistorBean.FIELD__OID).where(
                  Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                  SecurityProperties.getPartitionOid())))
            .setWhere(
                  andTerm(
                        andTerm(
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.ABORTED),
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.ABORTED),
                              notEqual(ActivityInstanceBean.FR__STATE,
                                    ActivityInstanceState.COMPLETED)),
                        greaterThan(ActivityInstanceBean.FR__OID, this.currentAiOid)))
            .addOrderBy(ActivityInstanceBean.FR__OID, true);

      ResultSet rs = session.executeQuery(query);

      try
      {
         int copiedRows = 0;
         while (rs.next() && copiedRows < batchSize)
         {
            long oid = rs.getLong(ActivityInstanceBean.FIELD__OID);
            updateList.add(oid);
            copiedRows++ ;
         }
      }
      catch (SQLException sqlex)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_COULD_NOT_RETRIEVE_ACTIVITY_INSTANCE_FOR_CRITICALITY_UPDATE
                     .raise());
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }

      return updateList;
   }

   public String getType()
   {
      return ID;
   }


   public long getDefaultPeriodicity()
   {
      return 5;
   }

   @Override
   public DaemonExecutionLog getExecutionLog()
   {
      // TODO Auto-generated method stub
      return null;
   }
}