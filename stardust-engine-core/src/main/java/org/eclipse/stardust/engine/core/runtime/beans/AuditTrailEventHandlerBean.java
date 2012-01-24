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
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
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
public class AuditTrailEventHandlerBean extends IdentifiablePersistentBean implements PersistentModelElement
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__ID = "id";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__PROCESS_DEFINITION = "processDefinition";
   public static final String FIELD__ACTIVITY = "activity";

   public static final FieldRef FR__OID = new FieldRef(AuditTrailEventHandlerBean.class, FIELD__OID);
   public static final FieldRef FR__MODEL = new FieldRef(AuditTrailEventHandlerBean.class, FIELD__MODEL);
   public static final FieldRef FR__ID = new FieldRef(AuditTrailEventHandlerBean.class, FIELD__ID);
   public static final FieldRef FR__NAME = new FieldRef(AuditTrailEventHandlerBean.class, FIELD__NAME);
   public static final FieldRef FR__PROCESS_DEFINITION = new FieldRef(AuditTrailEventHandlerBean.class, FIELD__PROCESS_DEFINITION);
   public static final FieldRef FR__ACTIVITY = new FieldRef(AuditTrailEventHandlerBean.class, FIELD__ACTIVITY);

   private static final int id_COLUMN_LENGTH = 50;
   private static final int name_COLUMN_LENGTH = 100;

   public static final String TABLE_NAME = "event_handler";
   public static final String DEFAULT_ALIAS = "eh";
   public static final String[] PK_FIELD = new String[] {FIELD__OID, FIELD__MODEL};
   public static final String[] event_handler_idx1_UNIQUE_INDEX =
         new String[]{FIELD__OID, FIELD__MODEL};
   public static final String[] event_handler_idx2_INDEX =
         new String[]{FIELD__ID, FIELD__OID, FIELD__MODEL};

   private long model;
   private String id;
   private String name;
   private long processDefinition;
   private long activity;

   public static Iterator findAll(short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            AuditTrailEventHandlerBean.class,
            new QueryExtension() //
                  .addJoin(new Join(ModelPersistorBean.class) //
                        .on(AuditTrailEventHandlerBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID) //
                        .where(
                              Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                                    partitionOid))));
   }

   public static AuditTrailEventHandlerBean findByOid(long rtOid, long modelOid)
   {
      return (AuditTrailEventHandlerBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(
            AuditTrailEventHandlerBean.class, QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OID, rtOid),
                  Predicates.isEqual(FR__MODEL, modelOid))));
   }

   public static int getMaxIdLength()
   {
      return id_COLUMN_LENGTH;
   }

   public AuditTrailEventHandlerBean()
   {
   }

   public AuditTrailEventHandlerBean(long rtOid, long modelOid, IEventHandler handler)
   {
      setOID(rtOid);
      this.model = modelOid;

      update(handler);
   }

   public long getModel()
   {
      return model;
   }
   
   public String getId()
   {
      return id;
   }
   
   public String getName()
   {
      return name;
   }
   
   public long getParent()
   {
      long parent = getActivity();
      return parent == 0 ? getProcessDefinition() : parent;
   }
   
   public long getProcessDefinition()
   {
      return processDefinition;
   }

   public long getActivity()
   {
      return activity;
   }
   
   public void update(IEventHandler handler)
   {
      markModified();
      
      this.id = org.eclipse.stardust.common.StringUtils.cutString(handler.getId(),
            id_COLUMN_LENGTH);
      this.name = org.eclipse.stardust.common.StringUtils.cutString(handler.getName(),
            name_COLUMN_LENGTH);
      IdentifiableElement parent = (IdentifiableElement) handler.getParent();
      if (parent instanceof IProcessDefinition)
      {
         this.processDefinition = ModelManagerFactory.getCurrent().getRuntimeOid(parent);
         this.activity = 0;
      }
      else if (parent instanceof IActivity)
      {
         this.processDefinition = ModelManagerFactory.getCurrent().getRuntimeOid(
               ((IActivity) parent).getProcessDefinition());
         this.activity = ModelManagerFactory.getCurrent().getRuntimeOid(parent);
      }
      else
      {
         throw new InternalException("Unsupported event handler parent: " + parent);
      }
   }
}
