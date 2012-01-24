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

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor;


/**
 * @author sauer
 * @version $Revision: $
 */
public class ProcessExecutionMonitorMediator implements IProcessExecutionMonitor
{
   private static final Logger trace = LogManager.getLogger(ProcessExecutionMonitorMediator.class);

   private final List<IProcessExecutionMonitor> monitors;
   
   public ProcessExecutionMonitorMediator(List<IProcessExecutionMonitor> monitors)
   {
      this.monitors = monitors;
   }

   public void processStarted(IProcessInstance process)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IProcessExecutionMonitor monitor = monitors.get(i);
         try
         {
            monitor.processStarted(process);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting process execution monitor event.", e);
         }
      }
   }
   
   public void processCompleted(IProcessInstance process)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IProcessExecutionMonitor monitor = monitors.get(i);
         try
         {
            monitor.processCompleted(process);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting process execution monitor event.", e);
         }
      }
   }
   
   public void processAborted(IProcessInstance process)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IProcessExecutionMonitor monitor = monitors.get(i);
         try
         {
            monitor.processAborted(process);
         }
         catch (Exception e)
         {
            trace.warn("Failed broadcasting process execution monitor event.", e);
         }
      }
   }

}
