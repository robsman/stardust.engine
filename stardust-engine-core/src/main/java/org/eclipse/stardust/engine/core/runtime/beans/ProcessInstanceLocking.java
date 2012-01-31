package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.Session;
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

   // cached PIs for complete sub process hierarchy. Filled in {@link lockStartTockens}.
   private final List<IProcessInstance> piTransitionsCache = CollectionUtils.newArrayList();

   /**
    * @param pi the process instance to lock all transition tokens for, including subprocesses.
    * @return list of process instances the transition tokens are locked for.
    */
   public List<IProcessInstance> lockAllTransitions(IProcessInstance processInstance)
   {
      processInstance.lock();

      boolean foundNewTokens;
      do
      {
         foundNewTokens = lockStartTockens(processInstance);
      }
      while (foundNewTokens);

      return piTransitionsCache;

   }

   private boolean lockStartTockens(IProcessInstance processInstance)
         throws ConcurrencyException
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (trace.isDebugEnabled())
      {
         trace.debug(MessageFormat.format("Fetching process hierarchy for {0}.",
               new Object[] {processInstance}));
      }

      Iterator piOidIter;
      PredicateTerm predicate = Predicates.isEqual(
            ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, processInstance.getOID());
      if ( !piTransitionsCache.isEmpty())
      {
         piOidIter = new TransformingIterator(piTransitionsCache.iterator(), new Functor()
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
            piTransitionsCache.add(pi);

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
                  new Object[] {processInstance}));
         }

         // Select all start transition and unbound tokens for the sub process hierarchy.
         ResultIterator ttIter = session.getIterator(
               TransitionTokenBean.class,
               QueryExtension.where( //
               Predicates.andTerm( //
                     Predicates.inList(TransitionTokenBean.FR__PROCESS_INSTANCE, newOids), //
                     Predicates.orTerm(
                           //
                           Predicates.andTerm(
                                 //
                                 Predicates.isEqual(TransitionTokenBean.FR__SOURCE, 0),
                                 Predicates.isNotNull(TransitionTokenBean.FR__TARGET)),
                           Predicates.andTerm(
                                 //
                                 Predicates.isNotNull(TransitionTokenBean.FR__SOURCE),
                                 Predicates.isEqual(TransitionTokenBean.FR__IS_CONSUMED,
                                       0))))));

         while (ttIter.hasNext())
         {
            TransitionTokenBean tt = (TransitionTokenBean) ttIter.next();
            tt.lock();
         }
      }

      return !newOids.isEmpty();
   }

   /**
    * Clears the cache. All process instances will be processed in the next call.
    */
   public void flushCaches()
   {
      piTransitionsCache.clear();
   }
}
