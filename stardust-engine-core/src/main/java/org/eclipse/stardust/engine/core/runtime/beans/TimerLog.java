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

import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


// @todo (france, ub): review
/**
 * Helper class to log timer daemon activities. It stores an object for every
 * timer-based trigger in a model with the oid of the timer-based trigger
 * as primary key.
 */
public class TimerLog extends IdentifiablePersistentBean
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__TRIGGER_OID = "triggerOID";
   public static final String FIELD__STAMP = "stamp";

   public static final FieldRef FR__OID = new FieldRef(TimerLog.class, FIELD__OID);
   public static final FieldRef FR__MODEL = new FieldRef(TimerLog.class, FIELD__MODEL);
   public static final FieldRef FR__TRIGGER_OID = new FieldRef(TimerLog.class, FIELD__TRIGGER_OID);
   public static final FieldRef FR__STAMP = new FieldRef(TimerLog.class, FIELD__STAMP);

   public static final String TABLE_NAME = "timer_log";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "timer_log_seq";
   public static final String[] timer_log_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};

   public long model;
   public long triggerOID;
   /**
    * Timestamp of the last execution of the timer-based trigger.
    */
   public long stamp;

   /**
    *
    */
   public static TimerLog findOrCreate(ITrigger trigger)
   {
      TimerLog log = (TimerLog) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(
                  TimerLog.class,
                  QueryExtension.where(Predicates.andTerm(
                        Predicates.isEqual(FR__TRIGGER_OID, ModelManagerFactory
                              .getCurrent().getRuntimeOid(trigger)),
                        Predicates.isEqual(FR__MODEL,
                              trigger.getModel().getModelOID()))));

      if (null == log)
      {
         log = new TimerLog(trigger);
      }
      return log;
   }

   /**
    *
    */
   public TimerLog()
   {
   }

   /**
    *
    */
   public TimerLog(ITrigger trigger)
   {
      this.model = trigger.getModel().getModelOID();
      this.triggerOID = ModelManagerFactory.getCurrent().getRuntimeOid(trigger);
      this.stamp = Unknown.LONG;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   /**
    *
    */
   public long getTimeStamp()
   {
      fetch();
      return stamp;
   }

   /**
    *
    */
   public void setTimeStamp(long stamp)
   {
      fetch();
      if (this.stamp != stamp)
      {
         markModified(FIELD__STAMP);
         this.stamp = stamp;
      }
   }
}
