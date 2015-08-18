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

import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * An log entry is the persistent, transactional representation of
 * something which happened during workflow execution.
 * <p>
 * It is thought to log exceptions, important events.
 * <p>
 * The log entry might be bound to a specific process instance or even
 * a specific activity instance.
 */
public class LogEntryBean extends IdentifiablePersistentBean implements ILogEntry
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__TYPE = "type";
   public static final String FIELD__CODE = "code";
   public static final String FIELD__SUBJECT = "subject";
   public static final String FIELD__STAMP = "stamp";
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__ACTIVITY_INSTANCE = "activityInstance";
   public static final String FIELD__USER = "workflowUser";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(LogEntryBean.class, FIELD__OID);
   public static final FieldRef FR__TYPE = new FieldRef(LogEntryBean.class, FIELD__TYPE);
   public static final FieldRef FR__CODE = new FieldRef(LogEntryBean.class, FIELD__CODE);
   public static final FieldRef FR__SUBJECT = new FieldRef(LogEntryBean.class, FIELD__SUBJECT);
   public static final FieldRef FR__STAMP = new FieldRef(LogEntryBean.class, FIELD__STAMP);
   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(LogEntryBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__ACTIVITY_INSTANCE = new FieldRef(LogEntryBean.class, FIELD__ACTIVITY_INSTANCE);
   public static final FieldRef FR__USER = new FieldRef(LogEntryBean.class, FIELD__USER);
   public static final FieldRef FR__PARTITION = new FieldRef(LogEntryBean.class, FIELD__PARTITION);

   public static final String TABLE_NAME = "log_entry";
   public static final String DEFAULT_ALIAS = "le";
   private static final String PK_FIELD = FIELD__OID;
   private static final String PK_SEQUENCE = "log_entry_seq";
   public static final boolean TRY_DEFERRED_INSERT = true;
   public static final String[] log_entry_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] log_entry_idx2_INDEX = new String[] {FIELD__ACTIVITY_INSTANCE};
   public static final String[] log_entry_idx3_INDEX = new String[] {FIELD__PROCESS_INSTANCE};

   static final boolean type_USE_LITERALS = true;
   static final boolean code_USE_LITERALS = true;

   private int type;
   private int code;

   private static final int subject_COLUMN_LENGTH = 300;
   private String subject;

   private long stamp;
   private long processInstance;
   private long activityInstance;
   private long workflowUser;
   private long partition;

   public LogEntryBean()
   {
   }

   /**
    *
    */
   public LogEntryBean(int type, int code, String subject,
         long processInstance, long activityInstance, long user, short partition)
   {
      if ( !StringUtils.isEmpty(subject))
      {
         String origSubject = subject.substring(0, Math.min(subject.length(),
               subject_COLUMN_LENGTH));
         // Creating a new string from the bytes of the original string is done because
         // characters which are not valid for the default encoding will be converted to '?'.
         // This prevents character conversion exceptions from Sybase.
         this.subject = new String(origSubject.getBytes());
      }
      stamp = TimestampProviderUtils.getTimeStamp().getTime();
      this.processInstance = processInstance;
      this.activityInstance = activityInstance;
      this.workflowUser = user;
      this.partition = partition;
      this.type = type;
      this.code = code;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public String toString()
   {
      return "LogEntry: oid = " + getOID() + ", subject = " + getSubject();
   }

   public Date getTimeStamp()
   {
      fetch();
      return new Date(stamp);
   }
   /*
    * Returns the (human readable) subject of the log entry.
    */
   public String getSubject()
   {
      fetch();

      return subject;
   }
   /*
    * The process instance for which the log entry is created
    * if there is one.
    */
   public long getProcessInstanceOID()
   {
      fetch();

      return processInstance;
   }
   /*
    * The process instance for which the log entry is created
    * if there is one.
    */
   public long getActivityInstanceOID()
   {
      fetch();

      return activityInstance;
   }

   public long getUserOID()
   {
      fetch();
      return workflowUser;
   }

   public short getPartitionOid()
   {
      fetch();
      return (short) partition;
   }

   public int getType()
   {
      return type;
   }

   public int getCode()
   {
      return code;
   }
}
