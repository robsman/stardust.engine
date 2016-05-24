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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.DefaultPersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.DeferredInsertable;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.tokencache.ISecondLevelTokenCache;
import org.eclipse.stardust.engine.core.runtime.beans.tokencache.TokenManagerRegistry;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TransitionTokenBean extends IdentifiablePersistentBean
      implements DeferredInsertable, IProcessInstanceAware
{
   public static final Logger trace = LogManager.getLogger(TransitionTokenBean.class);

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__TRANSITION = "transition";
   public static final String FIELD__SOURCE = "source";
   public static final String FIELD__TARGET = "target";
   public static final String FIELD__IS_CONSUMED = "isConsumed";

   public static final FieldRef FR__OID = new FieldRef(TransitionTokenBean.class, FIELD__OID);
   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(TransitionTokenBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__MODEL = new FieldRef(TransitionTokenBean.class, FIELD__MODEL);
   public static final FieldRef FR__TRANSITION = new FieldRef(TransitionTokenBean.class, FIELD__TRANSITION);
   public static final FieldRef FR__SOURCE = new FieldRef(TransitionTokenBean.class, FIELD__SOURCE);
   public static final FieldRef FR__TARGET = new FieldRef(TransitionTokenBean.class, FIELD__TARGET);
   public static final FieldRef FR__IS_CONSUMED = new FieldRef(TransitionTokenBean.class, FIELD__IS_CONSUMED);

   public static final Long START_TRANSITION_RT_OID = new Long(-1L);
   public static final Long START_TRANSITION_MODEL_OID = new Long(0L);

   public static final String TABLE_NAME = "trans_token";
   public static final String DEFAULT_ALIAS = "tk";
   public static final String LOCK_TABLE_NAME = "trans_token_lck";
   public static final String LOCK_INDEX_NAME = "trans_tok_lck_idx";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "trans_token_seq";
   public static final boolean TRY_DEFERRED_INSERT = true;
   public static final String[] trans_token_idx1_INDEX = new String[] {FIELD__PROCESS_INSTANCE};
   public static final String[] trans_token_idx2_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] trans_token_idx3_INDEX = new String[] {
         FIELD__PROCESS_INSTANCE, FIELD__TRANSITION, FIELD__MODEL, FIELD__IS_CONSUMED};

   @ForeignKey (persistentElement=ProcessInstanceBean.class)
   private long processInstance;
   @ForeignKey (modelElement=ModelBean.class)
   private long model;
   private long transition;
   private long source;
   private long target;
   private int isConsumed;

   public static void createStartToken(IProcessInstance processInstance)
   {
      TransitionTokenBean startToken = new TransitionTokenBean(processInstance, null, 0);
      startToken.setTarget(processInstance.getStartingActivityInstance());

      startToken.persist();

      // register with 2nd level cache if existent
      final ISecondLevelTokenCache secondLevelTokenCache = TokenManagerRegistry.instance()
            .createSecondLevelCache(processInstance);
      secondLevelTokenCache.registerToken(ActivityThread.START_TRANSITION, startToken);
   }

   /**
    * @return the unconsumed tokens available INCLUDING the one currently in the process of being consumed
    */
   public static long countUnconsumedForProcessInstance(IProcessInstance pi, int timeout)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      // make sure we count start tokens still being in CREATED state (not yet in the DB)
      long nAvailableTokens = 0l;

      boolean scanDb = true;

      if ((pi instanceof ProcessInstanceBean)
            && ((ProcessInstanceBean) pi).isPersistent()
            && ((ProcessInstanceBean) pi).getPersistenceController().isCreated())
      {
         // optimization for not yet INSERTed PIs, there is no need to scan the DB as
         // all existing tokens will be in the session's cache

         scanDb = false;
         Object cachedTokens = findUnconsumedInSessionCache(session, pi.getOID(), null);
         if (null != cachedTokens)
         {
            nAvailableTokens = (cachedTokens instanceof TransitionTokenBean)
                  ? 1
                  : ((List) cachedTokens).size();
            // since this method requires to return all unconsumed tokens plus the one being
            // consumed right now, we'll have to +1 since in the session cache the token currently
            // processed is already marked as being consumed
            nAvailableTokens++;
         }
      }

      if (scanDb)
      {
         // in memory view can not be trusted, so must scan DB
         nAvailableTokens += session.getCount(TransitionTokenBean.class, QueryExtension.where(
               Predicates.andTerm(
                     Predicates.isEqual(TransitionTokenBean.FR__PROCESS_INSTANCE, pi.getOID()),
                     Predicates.isEqual(TransitionTokenBean.FR__IS_CONSUMED, 0))), timeout);
      }

      return nAvailableTokens;
   }

   public static int getMultiInstanceIndex(long aiOid)
   {
      TransitionTokenBean token =  SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).findFirst(
            TransitionTokenBean.class,
            QueryExtension.where(Predicates.isEqual(
                  TransitionTokenBean.FR__TARGET, aiOid)));
      return token == null ? -1 : token.getMultiInstanceIndex();
   }

   public static ResultIterator findForProcessInstance(long piOid)
   {
      // no need to look for transient start tokens as recovery will be performed in its
      // own TX

      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            TransitionTokenBean.class,
            QueryExtension.where(Predicates.isEqual(
                  TransitionTokenBean.FR__PROCESS_INSTANCE, piOid)));
   }

   public static ResultIterator findUnconsumedForProcessInstance(long piOid)
   {
      // no need to look for transient start tokens as recovery will be performed in its
      // own TX

      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(TransitionTokenBean.class,
            QueryExtension.where(
                  Predicates.andTerm(
                        Predicates.isEqual(TransitionTokenBean.FR__PROCESS_INSTANCE, piOid),
                        Predicates.isEqual(TransitionTokenBean.FR__IS_CONSUMED, 0))));
   }

   public static Object findUnconsumedForTransition(IProcessInstance pi,
         Long transitionRtOid, long modelOid)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      Object result = null;
      boolean scanDb = true;

      if ((pi instanceof ProcessInstanceBean)
            && ((ProcessInstanceBean) pi).isPersistent()
            && ((ProcessInstanceBean) pi).getPersistenceController().isCreated())
      {
         // optimization for not yet INSERTed PIs, there is no need to scan the DB as
         // all existing tokens will be in the session's cache

         scanDb = false;
         final Object cachedTokens = findUnconsumedInSessionCache(session, pi.getOID(),
               transitionRtOid);
         if (null != cachedTokens)
         {
            // bingo, found available tokens in cache
            result = cachedTokens;
         }
      }

      if (scanDb)
      {
         result = session.getVector(TransitionTokenBean.class, QueryExtension.where(
               Predicates.andTerm(
                     Predicates.isEqual(TransitionTokenBean.FR__PROCESS_INSTANCE, pi.getOID()),
                     Predicates.isEqual(TransitionTokenBean.FR__TRANSITION, transitionRtOid),
                     transitionRtOid == null || transitionRtOid == -1
                        ? Predicates.lessOrEqual(TransitionTokenBean.FR__MODEL, modelOid)
                        : Predicates.isEqual(TransitionTokenBean.FR__MODEL, modelOid),
                     Predicates.isEqual(TransitionTokenBean.FR__IS_CONSUMED, 0))));
      }

      return result;
   }

   private static Object findUnconsumedInSessionCache(Session session, long piOid,
         Long transitionRtOid)
   {
      Object result = null;

      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         final Collection cachedTokens = ((org.eclipse.stardust.engine.core.persistence.jdbc.Session) session).getCache(TransitionTokenBean.class);

         if ((null != cachedTokens) && !cachedTokens.isEmpty())
         {
            for (Iterator i = cachedTokens.iterator(); i.hasNext();)
            {
               final PersistenceController pcToken = (PersistenceController) i.next();
               final TransitionTokenBean token = (TransitionTokenBean) pcToken.getPersistent();

               boolean isDeleted = (pcToken instanceof DefaultPersistenceController)
                     && ((DefaultPersistenceController) pcToken).isDeleted();

               if ( !isDeleted
                     && (piOid == token.getProcessInstanceOID())
                     && !token.isConsumed()
                     && ((null == transitionRtOid) || (transitionRtOid.longValue() == token.getTransitionOID())))
               {
                  // bingo, found token in cache
                  if (null == result)
                  {
                     result = token;
                  }
                  else
                  {
                     if (result instanceof TransitionTokenBean)
                     {
                        TransitionTokenBean existingToken = (TransitionTokenBean) result;
                        result = CollectionUtils.newList();
                        ((List) result).add(existingToken);
                     }

                     ((List) result).add(token);
                  }
               }
            }
         }
      }

      return result;
   }

   public TransitionTokenBean()
   {
   }

   public TransitionTokenBean(IProcessInstance processInstance, ITransition transition,
         long source)
   {
      this.processInstance = processInstance.getOID();
      this.model = transition == null
            ? START_TRANSITION_MODEL_OID
            : transition.getModel().getModelOID();
      this.transition = transition == null
            ? START_TRANSITION_RT_OID
            : ModelManagerFactory.getCurrent().getRuntimeOid(transition);
      this.source = source;

      if (trace.isDebugEnabled())
      {
         trace.debug(this + ": created.");
      }
   }

   public TransitionTokenBean(IProcessInstance processInstance, long source, int index)
   {
      this.processInstance = processInstance.getOID();
      this.model = -(index + 1);
      this.transition = START_TRANSITION_RT_OID;
      this.source = source;

      if (trace.isDebugEnabled())
      {
         trace.debug(this + ": created.");
      }
   }

   public boolean deferInsert()
   {
      return isStartToken();
   }

   public boolean isStartToken()
   {
      return (TransitionTokenBean.START_TRANSITION_RT_OID == getTransitionOID())
            && (TransitionTokenBean.START_TRANSITION_MODEL_OID == getModelOID());
   }

   public void setTarget(IActivityInstance target)
   {
      fetch();

      final long targetAiOid = (null != target) ? target.getOID() : 0;
      if (this.target != targetAiOid)
      {
         markModified(FIELD__TARGET);
         this.target = targetAiOid;

         long targetPiOid = target.getProcessInstanceOID();
         if (this.processInstance != targetPiOid)
         {
            markModified(FIELD__PROCESS_INSTANCE);
            this.processInstance = targetPiOid;
         }
      }
      if (trace.isDebugEnabled())
      {
         trace.debug(this + ": bound to " + target);
      }
   }

   public boolean isBound()
   {
      fetch();
      return target != 0;
   }

   public ITransition getTransition()
   {
      fetch();
      return transition == START_TRANSITION_RT_OID
            ? null
            : ModelManagerFactory.getCurrent().findTransition(model, transition);
   }

   public long getModelOID()
   {
      fetch();
      return model < 0 ? START_TRANSITION_MODEL_OID : model;
   }

   public long getTransitionOID()
   {
      fetch();
      return transition;
   }

   public long getProcessInstanceOID()
   {
      fetch();
      return processInstance;
   }

   public void setConsumed(boolean consumed)
   {
      fetch();

      if (isConsumed() != consumed)
      {
         markModified(FIELD__IS_CONSUMED);
         this.isConsumed = consumed ? 1 : 0;

         if (trace.isDebugEnabled())
         {
            trace.debug(this + ": consumed.");
         }

         if (isStartToken() && isPersistent() && getPersistenceController().isCreated())
         {
            // prevent transient start token from finally getting persisted
            delete();
         }
      }
   }

   public String toString()
   {
      return "Token: transition = " + getTransition() + "/ oid = " + getOID()
            + " (Process instance: " + getProcessInstanceOID() + ")";
   }

   public long getSource()
   {
      fetch();
      return source;
   }

   public long getTarget()
   {
      fetch();
      return target;
   }

   public int getMultiInstanceIndex()
   {
      fetch();
      return model < 0 ? -((int) model + 1) : 0;
   }

   public boolean isConsumed()
   {
      fetch();
      return isConsumed != 0;
   }

   public void persist()
   {
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);

      if (trace.isDebugEnabled())
      {
         trace.debug(this + ": persisted.");
      }
   }

   /**
    * Locks and reloads this token.
    *
    * @return an integer specifying the result of the operation:
    *   0 - operation was successful
    *   1 - a ConcurrencyException was logged
    *   2 - a PhantomException was logged
    */
   public int lockAndReload()
   {
      if (getPersistenceController() != null && getPersistenceController().isLocked())
      {
         return 0;
      }
      try
      {
         lock();
         if (trace.isDebugEnabled())
         {
            trace.debug("token " + this + " locked.");
         }
         try
         {
            reload();
            return 0;
         }
         catch (PhantomException ex)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Try to bind phantom token: " + this);
            }
            return 2;
         }
      }
      catch (ConcurrencyException ex)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Concurrent attempt to lock token: " + this);
         }
         return 1;
      }
   }

   @Override
   public IProcessInstance getProcessInstance()
   {
      return ProcessInstanceBean.findByOID(getProcessInstanceOID());
   }
}
