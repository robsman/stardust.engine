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

import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventBindingBean extends IdentifiablePersistentBean
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__OBJECT_OID = "objectOID";
   public static final String FIELD__TYPE = "type";
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__HANDLER_OID = "handlerOID";
   public static final String FIELD__BIND_STAMP = "bindStamp";
   public static final String FIELD__TARGET_STAMP = "targetStamp";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(EventBindingBean.class, FIELD__OID);
   public static final FieldRef FR__OBJECT_OID = new FieldRef(EventBindingBean.class, FIELD__OBJECT_OID);
   public static final FieldRef FR__TYPE = new FieldRef(EventBindingBean.class, FIELD__TYPE);
   public static final FieldRef FR__MODEL = new FieldRef(EventBindingBean.class, FIELD__MODEL);
   public static final FieldRef FR__HANDLER_OID = new FieldRef(EventBindingBean.class, FIELD__HANDLER_OID);
   public static final FieldRef FR__BIND_STAMP = new FieldRef(EventBindingBean.class, FIELD__BIND_STAMP);
   public static final FieldRef FR__TARGET_STAMP = new FieldRef(EventBindingBean.class, FIELD__TARGET_STAMP);
   public static final FieldRef FR__PARTITION = new FieldRef(EventBindingBean.class, FIELD__PARTITION);

   public static final String TABLE_NAME = "event_binding";
   public static final String PK_FIELD = FIELD__OID;
   private static final String PK_SEQUENCE = "event_binding_seq";

   public static final String[] event_binding_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] event_binding_idx2_INDEX = new String[] {
         FIELD__OBJECT_OID, FIELD__TYPE, FIELD__HANDLER_OID, FIELD__MODEL, FIELD__PARTITION};
   public static final String[] event_binding_idx3_INDEX = new String[] {
         FIELD__TARGET_STAMP, FIELD__PARTITION };

   static final boolean type_USE_LITERALS = true;
   
   private long objectOID;
   private int type;

   private long model;
   private long handlerOID;

   private long bindStamp;
   private long targetStamp;
   
   private long partition;

   public static EventBindingBean find(int objectType, long objectOID,
         IEventHandler handler, short partitionOid)
   {
      return (EventBindingBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(
                  EventBindingBean.class, //
                  QueryExtension.where(Predicates.andTerm( //
                        Predicates.isEqual(FR__TYPE, objectType),//
                        Predicates.isEqual(FR__OBJECT_OID, objectOID),//
                        Predicates.isEqual(FR__HANDLER_OID, ModelManagerFactory
                              .getCurrent().getRuntimeOid(handler)),//
                        Predicates.isEqual(FR__MODEL, handler.getModel().getModelOID()),//
                        Predicates.isEqual(FR__PARTITION, partitionOid))));
   }

   public static Iterator findAllNonTimerEvents(short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            EventBindingBean.class, // 
            QueryExtension.where(Predicates.andTerm( //
                  Predicates.lessThan(FR__TYPE, EventUtils.DEACTIVE_TYPE),//
                  Predicates.isEqual(FR__TARGET_STAMP, 0),//
                  Predicates.isEqual(FR__PARTITION, partitionOid))));
   }

   public static Iterator findAllTimerEvents(long timeStamp, short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            EventBindingBean.class, //
            QueryExtension.where(Predicates.andTerm( //
                  Predicates.lessThan(FR__TYPE, EventUtils.DEACTIVE_TYPE),//
                  Predicates.lessOrEqual(FR__TARGET_STAMP, timeStamp),//
                  Predicates.isEqual(FR__PARTITION, partitionOid))).addOrderBy(
                  FR__TARGET_STAMP));
   }

   public EventBindingBean()
   {
      partition = -1;
   }

   public EventBindingBean(int objectType, long objectOID, IEventHandler handler,
         short partitionOid)
   {
      this(objectType, objectOID, handler, 0, partitionOid);
   }

   public EventBindingBean(int objectType, long objectOID, IEventHandler handler,
         long targetStamp, short partitionOid)
   {
      this.type = objectType;
      this.objectOID = objectOID;
      this.model = handler.getModel().getModelOID();
      this.handlerOID = ModelManagerFactory.getCurrent().getRuntimeOid(handler);
      this.bindStamp = TimestampProviderUtils.getTimeStamp().getTime();
      this.targetStamp = targetStamp;
      this.partition = partitionOid;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public long getBindStamp()
   {
      fetch();
      return bindStamp;
   }

   public long getModel()
   {
      fetch();
      return model;
   }

   public long getHandlerOID()
   {
      fetch();
      return handlerOID;
   }

   public long getObjectOID()
   {
      fetch();
      return objectOID;
   }

   public long getTargetStamp()
   {
      fetch();
      return targetStamp;
   }

   public int getType()
   {
      fetch();
      return type;
   }

   public void setType(int newType)
   {
      fetch();
      if(type != newType)
      {
         type = newType;         
         markModified(FIELD__TYPE);
      }
   }
   
   public void setTargetStamp(long stamp)
   {
      fetch();
      if (this.targetStamp != stamp)
      {
         markModified(FIELD__TARGET_STAMP);
         this.targetStamp = stamp;
      }
   }

   public short getPartition()
   {
      fetch();
      return (short) partition;
   }
}