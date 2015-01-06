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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
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
public class AuditTrailTriggerBean extends IdentifiablePersistentBean implements PersistentModelElement
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__ID = "id";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__PROCESS_DEFINITION = "processDefinition";

   public static final FieldRef FR__OID = new FieldRef(AuditTrailTriggerBean.class, FIELD__OID);
   public static final FieldRef FR__MODEL = new FieldRef(AuditTrailTriggerBean.class, FIELD__MODEL);
   public static final FieldRef FR__ID = new FieldRef(AuditTrailTriggerBean.class, FIELD__ID);
   public static final FieldRef FR__NAME = new FieldRef(AuditTrailTriggerBean.class, FIELD__NAME);
   public static final FieldRef FR__PROCESS_DEFINITION = new FieldRef(AuditTrailTriggerBean.class, FIELD__PROCESS_DEFINITION);

   public static final String TABLE_NAME = "process_trigger";
   public static final String DEFAULT_ALIAS = "pt";
   public static final String[] PK_FIELD = new String[] {FIELD__OID, FIELD__MODEL};
   public static final String[] proc_trigger_idx1_UNIQUE_INDEX = new String[] {
         FIELD__OID, FIELD__MODEL};
   public static final String[] proc_trigger_idx2_INDEX = new String[] {
         FIELD__ID, FIELD__OID, FIELD__MODEL};

   private long model;
   
   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private static final int name_COLUMN_LENGTH = 100;
   private String name;

   private long processDefinition;

   public static Iterator findAll(short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            AuditTrailTriggerBean.class,
            new QueryExtension() //
                  .addJoin(new Join(ModelPersistorBean.class) //
                        .on(AuditTrailTriggerBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID) //
                        .where(
                              Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                                    partitionOid))));
   }

   public static AuditTrailTriggerBean findByOid(long rtOid, long modelOid)
   {
      return (AuditTrailTriggerBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(
            AuditTrailTriggerBean.class, QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OID, rtOid),
                  Predicates.isEqual(FR__MODEL, modelOid))));
   }

   public static int getMaxIdLength()
   {
      return id_COLUMN_LENGTH;
   }

   public AuditTrailTriggerBean()
   {
   }

   public AuditTrailTriggerBean(long rtOid, long modelOid, ITrigger trigger)
   {
      setOID(rtOid);
      this.model = modelOid;

      update(trigger);
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
   
   public String getName()
   {
      return name;
   }
   
   public long getProcessDefinition()
   {
      return processDefinition;
   }

   public void update(ITrigger trigger)
   {
      markModified();

      this.id = org.eclipse.stardust.common.StringUtils.cutString(trigger.getId(),
            id_COLUMN_LENGTH);
      this.name = org.eclipse.stardust.common.StringUtils.cutString(trigger.getName(),
            name_COLUMN_LENGTH);
      ModelElement parent = trigger.getParent();
      if (parent instanceof IProcessDefinition)
      {
         this.processDefinition = ModelManagerFactory.getCurrent().getRuntimeOid(
               (IProcessDefinition) parent);
      }
      else
      {
         throw new InternalException("Unsupported trigger parent: " + parent);
      }
   }
}
