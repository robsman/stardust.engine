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

import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.PersistentModelElement;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 *
 */
public class AuditTrailTransitionBean extends IdentifiablePersistentBean implements PersistentModelElement
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__ID = "id";
   public static final String FIELD__PROCESS_DEFINITION = "processDefinition";
   public static final String FIELD__SRC_ACTIVITY = "sourceActivity";
   public static final String FIELD__TGT_ACTIVITY = "targetActivity";
   public static final String FIELD__CONDITION = "condition";

   public static final FieldRef FR__OID = new FieldRef(AuditTrailTransitionBean.class, FIELD__OID);
   public static final FieldRef FR__MODEL = new FieldRef(AuditTrailTransitionBean.class, FIELD__MODEL);
   public static final FieldRef FR__ID = new FieldRef(AuditTrailTransitionBean.class, FIELD__ID);
   public static final FieldRef FR__PROCESS_DEFINITION = new FieldRef(AuditTrailTransitionBean.class, FIELD__PROCESS_DEFINITION);
   public static final FieldRef FR__SRC_ACTIVITY = new FieldRef(AuditTrailTransitionBean.class, FIELD__SRC_ACTIVITY);
   public static final FieldRef FR__TGT_ACTIVITY = new FieldRef(AuditTrailTransitionBean.class, FIELD__TGT_ACTIVITY);
   public static final FieldRef FR__CONDITION = new FieldRef(AuditTrailTransitionBean.class, FIELD__CONDITION);

   public static final String TABLE_NAME = "transition";
   public static final String DEFAULT_ALIAS = "td";
   public static final String[] PK_FIELD = new String[] {FIELD__OID, FIELD__MODEL};
   public static final String[] trans_idx1_UNIQUE_INDEX = new String[] {
         FIELD__OID, FIELD__MODEL};
   public static final String[] trans_idx2_INDEX = new String[] {
         FIELD__ID, FIELD__OID, FIELD__MODEL};

   private long model;
   
   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private long processDefinition;
   private long sourceActivity;
   private long targetActivity;

   private static final int condition_COLUMN_LENGTH = 200;
   private String condition;

   public static Iterator findAll(short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            AuditTrailTransitionBean.class,
            new QueryExtension() //
                  .addJoin(new Join(ModelPersistorBean.class) //
                        .on(AuditTrailTransitionBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID) //
                        .where(
                              Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                                    partitionOid))));
   }

   public static AuditTrailTransitionBean findByOid(long rtOid, long modelOid)
   {
      return (AuditTrailTransitionBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(
            AuditTrailTransitionBean.class, QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OID, rtOid),
                  Predicates.isEqual(FR__MODEL, modelOid))));
   }

   public static int getMaxIdLength()
   {
      return id_COLUMN_LENGTH;
   }

   public AuditTrailTransitionBean()
   {
   }

   public AuditTrailTransitionBean(long rtOid, long modelOid, ITransition transition)
   {
      setOID(rtOid);
      this.model = modelOid;
      
      update(transition);
   }

   public long getModel()
   {
      return model;
   }
   
   public String getId()
   {
      return id;
   }
   
   public long getParent()
   {
      return getProcessDefinition();
   }
   
   public long getProcessDefinition()
   {
      return processDefinition;
   }

   public long getSourceActivity()
   {
      return sourceActivity;
   }
   
   public long getTargetActivity()
   {
      return targetActivity;
   }
   
   public String getCondition()
   {
      return condition;
   }

   public void update(ITransition transition)
   {
      markModified();
      
      this.id = org.eclipse.stardust.common.StringUtils.cutString(transition.getId(), id_COLUMN_LENGTH);
      this.processDefinition = ModelManagerFactory.getCurrent().getRuntimeOid(
            transition.getProcessDefinition());
      IActivity activity = transition.getFromActivity();
      this.sourceActivity = activity == null ? -1 :
            ModelManagerFactory.getCurrent().getRuntimeOid(activity);
      activity = transition.getToActivity();
      this.targetActivity = activity == null ? -1 :
            ModelManagerFactory.getCurrent().getRuntimeOid(activity);
      this.condition = org.eclipse.stardust.common.StringUtils.cutString(transition.getCondition(),
            condition_COLUMN_LENGTH);
   }
}
