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

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.api.runtime.AcknowledgementState;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.SecurityContextAwareAction;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * Sets attributes of a damon log entry.
 * Will always set ackstate, will set time stamp if != -1
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SetDaemonLogAction extends SecurityContextAwareAction
{
   private String type;

   private int code;

   private long stamp;

   private int state;

   public static SetDaemonLogAction setStartLog(DaemonCarrier carrier,
         AcknowledgementState ack)
   {
      return new SetDaemonLogAction(carrier, DaemonLog.START, ack.getValue());
   }

   public static SetDaemonLogAction setLastExecutionLog(DaemonCarrier carrier,
         DaemonExecutionState des)
   {
      return new SetDaemonLogAction(carrier, DaemonLog.LAST_EXECUTION,
            des == null ? -1 : des.getValue());
   }

   private SetDaemonLogAction(DaemonCarrier carrier, int code, int state)
   {
      super(carrier);

      this.code = code;
      this.stamp = carrier.getStartTimeStamp();
      this.type = carrier.getType();
      this.state = state;
   }

   public Object execute()
   {
      short partitionOid = SecurityProperties.getPartitionOid();
      
      // @todo (france, ub): configure timeout?
      DaemonLog daemonLog = DaemonLog.find(type, code, partitionOid);
      if (daemonLog != null)
      {
         if (stamp != -1)
         {
            daemonLog.setTimeStamp(stamp);
         }
         if (state != -1)
         {
            daemonLog.setState(state);
         }
      }
      else
      {
         new DaemonLog(type, code, stamp, state, partitionOid);
      }
      return new Long(stamp);
   }

   public String toString()
   {
      return "Stamp daemon: " + type + "/" + code + ": " + stamp + "/" + state;
   }

}
