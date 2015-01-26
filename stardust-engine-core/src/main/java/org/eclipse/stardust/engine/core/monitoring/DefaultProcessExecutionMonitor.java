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
package org.eclipse.stardust.engine.core.monitoring;


import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor;


public class DefaultProcessExecutionMonitor implements IProcessExecutionMonitor
{
   public void processStarted(IProcessInstance process)
   {
   }

   public void processCompleted(IProcessInstance process)
   {
      ProcessInstanceUtils.checkGroupTermination(process);
   }

   public void processAborted(IProcessInstance process)
   {
      ProcessInstanceUtils.checkGroupTermination(process);
   }
   
   public void processInterrupted(IProcessInstance process)
   {
   }
}
