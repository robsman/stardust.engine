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

import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 *
 */
public class TransitionInstanceBean extends IdentifiablePersistentBean
      implements ITransitionInstance
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__TRANSITION = "transition";
   public static final String FIELD__SOURCE = "source";
   public static final String FIELD__TARGET = "target";

   public static final FieldRef FR__OID = new FieldRef(TransitionInstanceBean.class, FIELD__OID);
   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(TransitionInstanceBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__MODEL = new FieldRef(TransitionInstanceBean.class, FIELD__MODEL);
   public static final FieldRef FR__TRANSITION = new FieldRef(TransitionInstanceBean.class, FIELD__TRANSITION);
   public static final FieldRef FR__SOURCE = new FieldRef(TransitionInstanceBean.class, FIELD__SOURCE);
   public static final FieldRef FR__TARGET = new FieldRef(TransitionInstanceBean.class, FIELD__TARGET);

   public static final String TABLE_NAME = "trans_inst";
   public static final String DEFAULT_ALIAS = "ti";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "trans_inst_seq";

   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] trans_inst_idx1_INDEX = new String[] {FIELD__PROCESS_INSTANCE};
   public static final String[] trans_inst_idx2_UNIQUE_INDEX = new String[] {FIELD__OID};

   public ProcessInstanceBean processInstance;

   public long model;
   public long transition;

   public ActivityInstanceBean source;
   public ActivityInstanceBean target;

   /**
    * "Internal" constructor for persistence framework.
    */
   public TransitionInstanceBean()
   {
   }

   public TransitionInstanceBean(IProcessInstance processInstance, ITransition transition,
         IActivityInstance startActivity, IActivityInstance endActivity)
   {
      this.processInstance = (ProcessInstanceBean) processInstance;
      this.model = (null != transition) ? transition.getModel().getModelOID() : 0;
      this.transition = (null != transition) ? ModelManagerFactory.getCurrent()
            .getRuntimeOid(transition) : -1;
      this.source = (ActivityInstanceBean) startActivity;
      this.target = (ActivityInstanceBean) endActivity;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public IProcessInstance getProcessInstance()
   {
      fetchLink(FIELD__PROCESS_INSTANCE);

      return processInstance;
   }

   public ITransition getTransition()
   {
      fetch();

      return ModelManagerFactory.getCurrent().findTransition(model, transition);
   }

   public IActivityInstance getStartActivity()
   {
      fetchLink(FIELD__SOURCE);

      return source;
   }

   public IActivityInstance getEndActivity()
   {
      fetchLink(FIELD__TARGET);

      return target;
   }
}
