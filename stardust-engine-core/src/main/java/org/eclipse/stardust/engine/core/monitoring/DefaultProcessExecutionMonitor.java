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


import org.eclipse.stardust.engine.core.persistence.archive.ArchiveManagerFactory;
import org.eclipse.stardust.engine.core.persistence.archive.ExportImportSupport;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementExporter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.monitoring.IProcessExecutionMonitor;


public class DefaultProcessExecutionMonitor implements IProcessExecutionMonitor
{

   public static final ThreadLocal<IProcessInstance> ArchiveProcessInstance = new ThreadLocal<IProcessInstance>();
   
   public void processStarted(IProcessInstance process)
   {
   }

   public void processCompleted(IProcessInstance process)
   {
      ProcessInstanceUtils.checkGroupTermination(process);
      if (ArchiveManagerFactory.autoArchive())
      {
         String uuid = ExportImportSupport.getUUID(process);
         process.createProperty(ProcessElementExporter.EXPORT_PROCESS_ID, uuid);
         ArchiveProcessInstance.set(process);
      }
   }

   public void processAborted(IProcessInstance process)
   {
      ProcessInstanceUtils.checkGroupTermination(process);
      if (ArchiveManagerFactory.autoArchive())
      {
         String uuid = ExportImportSupport.getUUID(process);
         process.createProperty(ProcessElementExporter.EXPORT_PROCESS_ID, uuid);
         ArchiveProcessInstance.set(process);
      }
   }
   
   public void processInterrupted(IProcessInstance process)
   {
   }
}
