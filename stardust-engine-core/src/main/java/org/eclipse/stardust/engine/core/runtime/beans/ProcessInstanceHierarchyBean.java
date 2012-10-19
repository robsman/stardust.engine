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

import java.util.*;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.FetchType;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.Session.FilterOperation;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;

public class ProcessInstanceHierarchyBean extends PersistentBean implements
      IProcessInstanceHierarchy
{
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__SUB_PROCESS_INSTANCE = "subProcessInstance";

   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(ProcessInstanceHierarchyBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__SUB_PROCESS_INSTANCE = new FieldRef(ProcessInstanceHierarchyBean.class, FIELD__SUB_PROCESS_INSTANCE);

   public static final String TABLE_NAME = "procinst_hierarchy";
   public static final String DEFAULT_ALIAS = "pih";
   private static final String[] PK_FIELD = new String[] {FIELD__PROCESS_INSTANCE, FIELD__SUB_PROCESS_INSTANCE};
   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] procinst_hier_idx1_UNIQUE_INDEX = new String[]{FIELD__PROCESS_INSTANCE, FIELD__SUB_PROCESS_INSTANCE};
   public static final String[] procinst_hier_idx2_UNIQUE_INDEX = new String[]{FIELD__SUB_PROCESS_INSTANCE, FIELD__PROCESS_INSTANCE};

   private long processInstance;
   private static final String processInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String processInstance_MANDATORY = Boolean.TRUE.toString();

   private long subProcessInstance;
   private static final String subProcessInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String subProcessInstance_MANDATORY = Boolean.TRUE.toString();

   public ProcessInstanceHierarchyBean()
   {
   }

   public ProcessInstanceHierarchyBean(IProcessInstance processInstance, IProcessInstance subProcessInstance)
   {
      super();
      this.processInstance = processInstance.getOID();
      this.subProcessInstance = subProcessInstance.getOID();

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      session.cluster(this);
   }

   /**
    * Returns the parent of the process instance with the OID <tt>oid</tt>. Returns null
    * if the process instance is not found.
    *
    * @throws ObjectNotFoundException
    *            if parameter oid is 0.
    */
   public static IProcessInstance findParentForSubProcessInstanceOid(long oid)
         throws ObjectNotFoundException
   {
      if (oid == 0)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PROCESS_INSTANCE_OID.raise(0), 0);
      }
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      QueryExtension query = QueryExtension.where(new AndTerm().add(
            Predicates.isEqual(FR__SUB_PROCESS_INSTANCE, oid)).add(
            Predicates.notEqual(FR__PROCESS_INSTANCE, oid)));

      query.setOrderCriteria(new OrderCriteria(FR__PROCESS_INSTANCE, false));
      
      IProcessInstance pi = null;
      ResultIterator<ProcessInstanceHierarchyBean> itr = session.getIterator(ProcessInstanceHierarchyBean.class, query);
      try
      {
         while (itr.hasNext())
         {
            ProcessInstanceHierarchyBean pih = itr.next();
            pi = pih.getProcessInstance();
            if (!pi.isCaseProcessInstance())
            {
               return pi;
            }
         }
      }
      finally
      {
         itr.close();
      }

      return pi;
   }

   public static List<IProcessInstance> findChildren(IProcessInstance pi)
   {
      final long oid = pi.getOID();
      final Iterator<ProcessInstanceHierarchyBean> pihIter;

      if (pi.getPersistenceController().isCreated())
      {
         final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         pihIter = session.getSessionCacheIterator(ProcessInstanceHierarchyBean.class, new FilterOperation<ProcessInstanceHierarchyBean>()
         {
            @Override
            public FilterResult filter(final ProcessInstanceHierarchyBean persistentToFilter)
            {
               final boolean isPi = persistentToFilter.getProcessInstance().getOID() == oid;
               final boolean isNotSubPi = persistentToFilter.getSubProcessInstance().getOID() != oid;
               return (isPi && isNotSubPi) ? FilterResult.ADD : FilterResult.OMIT;
            }
         });
      }
      else
      {
         final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
   
         QueryExtension query = QueryExtension.where(new AndTerm().add(
               Predicates.isEqual(FR__PROCESS_INSTANCE, oid)).add(
               Predicates.notEqual(FR__SUB_PROCESS_INSTANCE, oid)));
   
         query.setOrderCriteria(new OrderCriteria(FR__PROCESS_INSTANCE, false));
   
         pihIter = session.getIterator(
               ProcessInstanceHierarchyBean.class, query);
      }

      List<IProcessInstance> pis = new LinkedList<IProcessInstance>();
      if (pihIter != null)
      {
         while (pihIter.hasNext())
         {
            ProcessInstanceHierarchyBean pih = pihIter.next();

            pis.add(pih.getSubProcessInstance());
         }
      }
      return pis;
   }
   
   public static boolean isSubprocess(IProcessInstance parent, IProcessInstance sub)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      PredicateTerm predicate = Predicates.andTerm(
            Predicates.isEqual(FR__PROCESS_INSTANCE, parent.getOID()),
            Predicates.isEqual(FR__SUB_PROCESS_INSTANCE, sub.getOID()));

      return session.exists(ProcessInstanceHierarchyBean.class, QueryExtension.where(predicate));
   }

   public static void delete(IProcessInstance pi)
   {
      long piOID = pi.getOID();

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      boolean isJdbc = session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session;
      
      // delete from session
      if (isJdbc)
      {
         Set<Long> oids = CollectionUtils.newSet();
         Collection<PersistenceController> cache = ((org.eclipse.stardust.engine.core.persistence.jdbc.Session) session).getCache(ProcessInstanceHierarchyBean.class);
         for (PersistenceController ctrl : cache)
         {
            ProcessInstanceHierarchyBean pih = (ProcessInstanceHierarchyBean) ctrl.getPersistent();
            if (pih.processInstance == piOID)
            {
               oids.add(pih.subProcessInstance);
            }
         }
         for (PersistenceController ctrl : cache)
         {
            ProcessInstanceHierarchyBean pih = (ProcessInstanceHierarchyBean) ctrl.getPersistent();
            if (ctrl.isCreated() && oids.contains(pih.subProcessInstance))
            {
               pih.delete();
            }
         }
      }
      
      // delete from audit trail
      if (!isJdbc || !pi.getPersistenceController().isCreated())
      {
         session.delete(
               ProcessInstanceHierarchyBean.class,
               Predicates.inList(ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, QueryDescriptor
                     .from(ProcessInstanceHierarchyBean.class)
                     .select(ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE)
                     .where(Predicates.isEqual(FR__PROCESS_INSTANCE, piOID))),
               true);
      }
   }

   public static void delete(IProcessInstance pi, IProcessInstance sub)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      PredicateTerm where = Predicates.andTerm(
            Predicates.isEqual(FR__PROCESS_INSTANCE, pi.getOID()),
            Predicates.isEqual(FR__SUB_PROCESS_INSTANCE, sub.getOID()));

      session.delete(ProcessInstanceHierarchyBean.class, where, false);
   }

   public IProcessInstance getProcessInstance()
   {
      fetch();
      return ProcessInstanceBean.findByOID(processInstance);
   }

   public IProcessInstance getSubProcessInstance()
   {
      fetch();
      return ProcessInstanceBean.findByOID(subProcessInstance);
   }
}
