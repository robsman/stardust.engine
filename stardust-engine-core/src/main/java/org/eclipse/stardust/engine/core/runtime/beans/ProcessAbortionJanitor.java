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

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.TimestampProviderUtils;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;



/**
 *
 * @author sborn
 * @version $Revision: $
 */
public class ProcessAbortionJanitor extends SecurityContextAwareAction
{
   public static final Logger trace = LogManager.getLogger(ProcessAbortionJanitor.class);

   protected static final String PRP_RETRY_COUNT = "Infinity.Engine.ProcessAbortion.Failure.RetryCount";
   protected static final String PRP_RETRY_PAUSE = "Infinity.Engine.ProcessAbortion.Failure.RetryPause";

   private long processInstanceOid;
   private int triesLeft;

   // cached PIs for complete sub process hierarchy. Filled in {@link lockStartTockens}.
   private final List piCache = CollectionUtils.newArrayList();

   public ProcessAbortionJanitor(AbortionJanitorCarrier carrier)
   {
      super(carrier);
      this.processInstanceOid = carrier.getProcessInstanceOid();
      this.triesLeft = carrier.getTriesLeft();
   }

   public Object execute()
   {
      if (Parameters.instance().getBoolean(
            KernelTweakingProperties.PREVENT_ABORTING_TO_ABORTED_STATE_CHANGE, false))
      {
         return Boolean.TRUE;
      }

      triesLeft -= 1;

      boolean performed = false;
      ProcessInstanceBean pi = ProcessInstanceBean.findByOID(processInstanceOid);

      if ( !pi.isTerminated())
      {
         try
         {
            pi.lock();

            boolean foundNewTokens;
            do
            {
               foundNewTokens = lockStartTockens(pi);
            }
            while (foundNewTokens);

            abortAllProcessInstances();
            removePiFromAbortingList(pi);
            abortStartingActivityInstance(pi);

            performed = true;
         }
         catch (ConcurrencyException e)
         {
            if (triesLeft > 0)
            {
               trace.info(MessageFormat.format(
                     "Cannot run abortion janitor for {0} due to a locking conflict, scheduling a new one. Tries left: {1}.",
                     new Object[] { pi, new Integer(triesLeft) }));

               try
               {
                  Thread.sleep(Parameters.instance().getLong(PRP_RETRY_PAUSE, 500));
               }
               catch (InterruptedException x)
               {
               }
               scheduleJanitor(new AbortionJanitorCarrier(processInstanceOid, triesLeft));
            }
            else
            {
               trace.warn(MessageFormat.format("Could not run abortion janitor for {0}.",
                     new Object[] { pi }));
            }
         }
      }

      return performed ? Boolean.TRUE : Boolean.FALSE;
   }

   public String toString()
   {
      return "Process abortion janitor, pi = " + processInstanceOid;
   }

   private boolean lockStartTockens(ProcessInstanceBean processInstance)
         throws ConcurrencyException
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (trace.isDebugEnabled())
      {
         trace.debug(MessageFormat.format("Fetching process hierarchy for {0}.",
               new Object[] { processInstance }));
      }

      Iterator piOidIter;
      PredicateTerm predicate = Predicates.isEqual(
            ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, processInstance.getOID());
      if ( !piCache.isEmpty())
      {
         piOidIter = new TransformingIterator(piCache.iterator(), new Functor()
         {
            public Object execute(Object source)
            {
               ProcessInstanceBean pi = (ProcessInstanceBean) source;

               return new Long(pi.getOID());
            }
         });

         predicate = Predicates.andTerm( //
               predicate, //
               Predicates.notInList(
                     ProcessInstanceHierarchyBean.FR__SUB_PROCESS_INSTANCE, piOidIter));
      }
      // Find already persisted subprocesses which are not loaded before.
      ResultIterator pihIter = session.getIterator(ProcessInstanceHierarchyBean.class,
            QueryExtension.where(predicate));

      // Create iterator returning the oids for the subprocesses.
      piOidIter = new TransformingIterator(pihIter, new Functor()
      {
         public Object execute(Object source)
         {
            ProcessInstanceHierarchyBean pihItem = (ProcessInstanceHierarchyBean) source;

            // cache the PI for later usage.
            final IProcessInstance pi = pihItem.getSubProcessInstance();
            piCache.add(pi);

            return new Long(pi.getOID());
         }
      });
      List newOids = CollectionUtils.newListFromIterator(piOidIter);

      if ( !newOids.isEmpty())
      {
         if (trace.isDebugEnabled())
         {
            trace.debug(MessageFormat.format(
                  "Fetching starting transition tokens for {0} process hierarchy.",
                  new Object[] { processInstance }));
         }

         // Select all start transition and unbound tokens for the sub process hierarchy.
         ResultIterator ttIter = session.getIterator(TransitionTokenBean.class,
               QueryExtension.where( //
                     Predicates.andTerm( //
                           Predicates.inList(TransitionTokenBean.FR__PROCESS_INSTANCE,
                                 newOids), //
                                 Predicates.orTerm( //
                                       Predicates.andTerm( //
                                             Predicates.isEqual(TransitionTokenBean.FR__SOURCE, 0),
                                             Predicates.isNotNull(TransitionTokenBean.FR__TARGET)),
                                       Predicates.andTerm( //
                                             Predicates.isNotNull(TransitionTokenBean.FR__SOURCE),
                                             Predicates.isEqual(TransitionTokenBean.FR__IS_CONSUMED, 0)))
                           )));

         while (ttIter.hasNext())
         {
            TransitionTokenBean tt = (TransitionTokenBean) ttIter.next();
            tt.lock();
         }
      }

      return !newOids.isEmpty();
   }

   private void abortAllProcessInstances()
   {
      for (Iterator piIter = piCache.iterator(); piIter.hasNext();)
      {
         ProcessInstanceBean pi = (ProcessInstanceBean) piIter.next();

         abort(pi);
      }
   }

   private void abort(ProcessInstanceBean pi)
   {
      if ( !pi.isTerminated())
      {
         pi.lock();

         pi.setTerminationTime(TimestampProviderUtils.getTimeStamp());
         pi.setState(ProcessInstanceState.ABORTED);

         for (Iterator aiIter = ActivityInstanceBean.getAllForProcessInstance(pi); aiIter
               .hasNext();)
         {
            final ActivityInstanceBean activityInstance = (ActivityInstanceBean) aiIter
                  .next();

            if ( !activityInstance.isTerminated())
            {
               activityInstance.lock();

               activityInstance.setState(ActivityInstanceState.ABORTED);
               activityInstance.removeFromWorklists();
               EventUtils.detachAll(activityInstance);
            }
         }

         EventUtils.detachAll(pi);
         MonitoringUtils.processExecutionMonitors().processAborted(pi);

         AuditTrailLogger.getInstance(LogCode.ENGINE, pi).info(
               "Process instance aborted.");
      }
   }

   private void abortStartingActivityInstance(ProcessInstanceBean pi)
   {
      final ActivityInstanceBean startingActivityInstance = (ActivityInstanceBean) pi
            .getStartingActivityInstance();
      if (null != startingActivityInstance)
      {
         ActivityInstanceUtils
               .scheduleNewActivityThread(startingActivityInstance);
      }
   }

   private void removePiFromAbortingList(ProcessInstanceBean pi)
   {
      IProcessInstance rootPi = pi.getRootProcessInstance();
      rootPi.lock();
      rootPi.removeAbortingPiOid(processInstanceOid);
   }

   public static void scheduleJanitor(ActionCarrier carrier)
   {
      ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);
      ForkingService service = null;
      try
      {
         service = factory.get();
         service.fork(carrier, true);
      }
      finally
      {
         factory.release(service);
      }
   }
}
