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

import org.eclipse.stardust.engine.core.runtime.beans.SecurityContextAwareAction;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class GetDaemonLogAction extends SecurityContextAwareAction
{
   private String type;
   private int code;

   public static GetDaemonLogAction getStartLog(DaemonCarrier carrier)
   {
      return new GetDaemonLogAction(carrier, DaemonLog.START);
   }

   public static GetDaemonLogAction getLastExecutionLog(DaemonCarrier carrier)
   {
      return new GetDaemonLogAction(carrier, DaemonLog.LAST_EXECUTION);
   }

   private GetDaemonLogAction(DaemonCarrier carrier, int code)
   {
      super(carrier);
      
      this.code = code;
      this.type = carrier.getType();
   }

   public Object execute()
   {
      DaemonLog result = DaemonLog.find(type, code, SecurityProperties.getPartitionOid());
      if (result == null)
      {
         result = new DaemonLog();
      }
      return result;
   }

   public String toString()
   {
      return "Get daemon stamp: " + type + "/" + code; 
   }
}
