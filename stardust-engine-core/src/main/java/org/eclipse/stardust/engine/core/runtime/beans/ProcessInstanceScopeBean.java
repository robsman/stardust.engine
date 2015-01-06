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

import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;

public class ProcessInstanceScopeBean extends PersistentBean implements
      IProcessInstanceScope
{
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__SCOPE_PROCESS_INSTANCE = "scopeProcessInstance";
   public static final String FIELD__ROOT_PROCESS_INSTANCE = "rootProcessInstance";

   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(ProcessInstanceScopeBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__SCOPE_PROCESS_INSTANCE = new FieldRef(ProcessInstanceScopeBean.class, FIELD__SCOPE_PROCESS_INSTANCE);
   public static final FieldRef FR__ROOT_PROCESS_INSTANCE = new FieldRef(ProcessInstanceScopeBean.class, FIELD__ROOT_PROCESS_INSTANCE);

   public static final String TABLE_NAME = "procinst_scope";
   public static final String DEFAULT_ALIAS = "pis";
   private static final String[] PK_FIELD = new String[] {FIELD__PROCESS_INSTANCE};
   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] procinst_scope_i1_UNIQUE_INDEX = new String[]{FIELD__PROCESS_INSTANCE, FIELD__SCOPE_PROCESS_INSTANCE};
   public static final String[] procinst_scope_i2_UNIQUE_INDEX = new String[]{FIELD__SCOPE_PROCESS_INSTANCE, FIELD__PROCESS_INSTANCE};
   public static final String[] procinst_scope_i3_INDEX = new String[]{FIELD__ROOT_PROCESS_INSTANCE};

   private ProcessInstanceBean processInstance;
   private static final String processInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String processInstance_MANDATORY = Boolean.TRUE.toString();

   private ProcessInstanceBean scopeProcessInstance;
   private static final String scopeProcessInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String scopeProcessInstance_MANDATORY = Boolean.TRUE.toString();

   private ProcessInstanceBean rootProcessInstance;
   private static final String rootProcessInstance_EAGER_FETCH = Boolean.FALSE.toString();
   private static final String rootProcessInstance_MANDATORY = Boolean.TRUE.toString();

   public ProcessInstanceScopeBean()
   {
   }

   public ProcessInstanceScopeBean(IProcessInstance processInstance,
         IProcessInstance scopeProcessInstance, IProcessInstance rootProcessInstance)
   {
      super();

      this.processInstance = (ProcessInstanceBean) processInstance;
      this.scopeProcessInstance = (ProcessInstanceBean) scopeProcessInstance;
      this.rootProcessInstance = (ProcessInstanceBean) rootProcessInstance;

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      session.cluster(this);
   }

   public static void delete(IProcessInstance processInstance)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      MultiPartPredicateTerm term = new AndTerm()//
      .add(Predicates.isEqual(FR__PROCESS_INSTANCE, processInstance.getOID()))//
      .add(Predicates.isEqual(FR__SCOPE_PROCESS_INSTANCE, processInstance.getScopeProcessInstanceOID()))//
      .add(Predicates.isEqual(FR__ROOT_PROCESS_INSTANCE, processInstance.getRootProcessInstanceOID()));
      session.delete(ProcessInstanceScopeBean.class, term, false);
   }

   public IProcessInstance getProcessInstance()
   {
      fetchLink(FIELD__PROCESS_INSTANCE);
      return processInstance;
   }

   public IProcessInstance getRootProcessInstance()
   {
      fetchLink(FIELD__ROOT_PROCESS_INSTANCE);
      return rootProcessInstance;
   }

   public IProcessInstance getScopeProcessInstance()
   {
      fetchLink(FIELD__SCOPE_PROCESS_INSTANCE);
      return scopeProcessInstance;
   }
}
