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
package org.eclipse.stardust.engine.core.runtime.beans.daemons;

import org.eclipse.stardust.common.IntKey;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.AcknowledgementState;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * Helper class to log daemon activities.
 * 
 * @author mgille
 * @version $Revision$
 */
public class DaemonLog extends IdentifiablePersistentBean
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__TYPE = "type";
   public static final String FIELD__CODE = "code";
   public static final String FIELD__STAMP = "stamp";
   public static final String FIELD__STATE = "state";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(DaemonLog.class, FIELD__OID);
   public static final FieldRef FR__TYPE = new FieldRef(DaemonLog.class, FIELD__TYPE);
   public static final FieldRef FR__CODE = new FieldRef(DaemonLog.class, FIELD__CODE);
   public static final FieldRef FR__STAMP = new FieldRef(DaemonLog.class, FIELD__STAMP);
   public static final FieldRef FR__STATE = new FieldRef(DaemonLog.class, FIELD__STATE);
   public static final FieldRef FR__PARTITION = new FieldRef(DaemonLog.class, FIELD__PARTITION);

   public static final int START = 0;
   public static final int LAST_EXECUTION = 1;

   public static final String TABLE_NAME = "daemon_log";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "daemon_log_seq";
   public static final String[] daemon_log_idx1_UNIQUE_INDEX = new String[]{FIELD__OID};
   public static final String[] daemon_log_idx2_INDEX = new String[] { FIELD__CODE, FIELD__PARTITION, FIELD__TYPE };
   
   public static final String LOCK_TABLE_NAME = "daemon_log_lck";
   public static final String LOCK_INDEX_NAME = "daemon_log_lck_idx";
   
   public static final String DEFAULT_ALIAS = "dl";
   
   static final boolean type_USE_LITERALS = true;
   static final boolean code_USE_LITERALS = true;
   static final boolean state_USE_LITERALS = true;
   
   private static final int type_COLUMN_LENGTH = 100;
   private String type;
   private int code;
   private long stamp;
   private int state;
   private long partition;

   public static DaemonLog find(String type, int code, short partitionOid)
   {
      int timeout = Parameters.instance().getInteger(
            KernelTweakingProperties.FIND_DAEMON_LOG_QUERY_TIMEOUT, 5);
      return find(type, code, timeout, partitionOid);
   }

   public static DaemonLog find(String type, int code, int queryTimeout,
         short partitionOid)
   {
      return (DaemonLog) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(DaemonLog.class,
                  QueryExtension.where(
                        Predicates.andTerm(
                              Predicates.isEqual(FR__TYPE, type),
                              Predicates.isEqual(FR__CODE, code),
                              Predicates.isEqual(FR__PARTITION, partitionOid)
                              )
                        ),
                        queryTimeout);
   }

   public DaemonLog()
   {
      partition = -1;
   }

   public DaemonLog(String type, int code, long stamp, int state,
         short partitionOid)
   {
      this.type = StringUtils.cutString(type, type_COLUMN_LENGTH);
      this.code = code;
      this.stamp = stamp;
      if (state != -1)
      {
         this.state = state;
      }
      this.partition = partitionOid;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public long getTimeStamp()
   {
      fetch();
      return stamp;
   }

   public void setTimeStamp(long stamp)
   {
      fetch();
      if (this.stamp != stamp)
      {
         markModified(FIELD__STAMP);
         this.stamp = stamp;
      }
   }

   public void setState(int state)
   {
      fetch();
      if (this.state != state)
      {
         markModified(FIELD__STATE);
         this.state = state;
      }
   }
   
   public short getPartition()
   {
      fetch();
      return (short) partition;
   }

   public String getType()
   {
      fetch();
      return type;
   }

   public AcknowledgementState getAcknowledgementState()
   {
      return (AcknowledgementState) IntKey.getKey(AcknowledgementState.class, state);
   }

   public DaemonExecutionState getDaemonExecutionState()
   {
      return (DaemonExecutionState) IntKey.getKey(DaemonExecutionState.class, state);
   }
}
