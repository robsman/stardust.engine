/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;

/**
 * Handles locking strategies commonly usable by the runtime.<p>
 * References to all affected process instances are held in a cache which can be reset with {@link #flushCaches()}.
 * Process instances held in the cache are not processed in a subsequent call.
 *
 * @author roland.stamm
 *
 */
public class ProcessInstanceLocking implements Serializable
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ProcessInstanceLocking.class);

   /**
    *  cached PIs for complete sub process hierarchy. Filled in {@link #lockStartTokens}.
    */
   private final Map<Long, IProcessInstance> piTransitionsCache = CollectionUtils.newMap();

   /**
    * @param pi the process instance to lock all transition tokens for, including subprocesses.
    * @return collection of process instances the transition tokens are locked for.
    */
   public Collection<IProcessInstance> lockAllTransitions(IProcessInstance processInstance)
   {
      processInstance.lock();

      boolean foundNewTokens;
      do
      {
         foundNewTokens = lockStartTokens(processInstance);
      }
      while (foundNewTokens);

      return piTransitionsCache.values();

   }

   private boolean lockStartTokens(IProcessInstance processInstance)
         throws ConcurrencyException
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (trace.isDebugEnabled())
      {
         trace.debug(MessageFormat.format("Fetching process hierarchy for {0}.",
               new Object[] {processInstance}));
      }

      // Search for all PIH entries for specific root PI. Not restricted by any notInList-predicate
      // as this might exceed some SQL thresholds for huge PI hierarchies
      ResultIterator<ProcessInstanceHierarchyBean> pihIter = session.getIterator(
            ProcessInstanceHierarchyBean.class, QueryExtension.where(Predicates.isEqual(
                  ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE,
                  processInstance.getOID())));

      Set<Long> oldPis = CollectionUtils.newSetFromIterator(piTransitionsCache.keySet().iterator());
      // Create iterator returning the oids for the subprocesses.
      Iterator<Long> piOidIter = new TransformingIterator(pihIter,
            new Functor<ProcessInstanceHierarchyBean, Long>()
            {
               public Long execute(ProcessInstanceHierarchyBean pih)
               {
                  ProcessInstanceHierarchyBean pihItem = (ProcessInstanceHierarchyBean) pih;

                  // cache the PI for later usage.
                  final IProcessInstance pi = pihItem.getSubProcessInstance();
                  final Long piOid = Long.valueOf(pi.getOID());
                  piTransitionsCache.put(piOid, pi) ;

                  return piOid;
               }
            });
      Set<Long> latestPiOids = CollectionUtils.newSetFromIterator(piOidIter);

      // remove all already known piOids
      latestPiOids.removeAll(oldPis);

      if ( !latestPiOids.isEmpty())
      {
         if (trace.isDebugEnabled())
         {
            trace.debug(MessageFormat.format(
                  "Fetching starting transition tokens for {0} process hierarchy.",
                  new Object[] {processInstance}));
         }

         // Select all start transition and unbound tokens for the sub process hierarchy...
         final QueryExtension query = QueryExtension.where( //
               Predicates.orTerm( //
                     Predicates.andTerm( //
                           Predicates.isEqual(TransitionTokenBean.FR__SOURCE, 0),
                           Predicates.isNotNull(TransitionTokenBean.FR__TARGET)),
                     Predicates.andTerm( //
                           Predicates.isNotNull(TransitionTokenBean.FR__SOURCE),
                           Predicates.isEqual(TransitionTokenBean.FR__IS_CONSUMED,
                                 0))));

         // ... which is defined by this join
         Join pihJoin = new Join(ProcessInstanceHierarchyBean.class)
               .on(TransitionTokenBean.FR__PROCESS_INSTANCE,
                   ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE)
               .where(Predicates.isEqual(ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, 1));
         pihJoin.isRequired();
         query.addJoin(pihJoin);

         TransTokenFetchPredicate fetchPredicate = new TransTokenFetchPredicate(latestPiOids);

         // fetch all transition tokens which are not fetched in previous iteration
         ResultIterator ttIter = session.getIterator(TransitionTokenBean.class, query, 0,
               -1, fetchPredicate, false, Session.NO_TIMEOUT);

         while (ttIter.hasNext())
         {
            TransitionTokenBean tt = (TransitionTokenBean) ttIter.next();
            tt.lock();
         }
      }

      return !latestPiOids.isEmpty();
   }

   /**
    * Clears the cache. All process instances will be processed in the next call.
    */
   public void flushCaches()
   {
      piTransitionsCache.clear();
   }

   private static final class TransTokenFetchPredicate implements FetchPredicate
   {
      private static final FieldRef[] REFERENCED_TT_FIELDS = new FieldRef[] { TransitionTokenBean.FR__PROCESS_INSTANCE };

      final Set processOIDs;

      public TransTokenFetchPredicate(Set processOIDs)
      {
         this.processOIDs = processOIDs;
      }

      @Override
      public boolean accept(Object o)
      {
         long piOid = 0;

         if (o instanceof ResultSet)
         {
            ResultSet result = (ResultSet) o;

            try
            {
               piOid = result.getLong(TransitionTokenBean.FIELD__PROCESS_INSTANCE);
               return contains(piOid);
            }
            catch (SQLException e)
            {
               trace.warn(MessageFormat.format(
                     "Ignoring transition token for process instance with oid 0 {0}.",
                     new Object[] { piOid }), e);

               return false;
            }
         }
         else if (o instanceof TransitionTokenBean)
         {
            piOid = ((TransitionTokenBean) o).getProcessInstanceOID();
            return contains(piOid);
         }
         else
         {
            return false;
         }
      }

      @Override
      public FieldRef[] getReferencedFields()
      {
         return REFERENCED_TT_FIELDS;
      }

      private boolean contains(Long piOid)
      {
         return processOIDs.contains(piOid);
      }
   }
}
