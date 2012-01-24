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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationJobInfo;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;

import com.sungard.infinity.bpm.vfs.IMigrationReport;


public class DmsMigrationReportBean implements RepositoryMigrationReport, Serializable
{

   private static final long serialVersionUID = 1L;

   private final RepositoryMigrationJobInfo currentMigrationJob;

   private int currentRepositoryVersion;

   private int targetRepositoryVersion;

   private long resourcesDone;

   private long totalCount;

   public DmsMigrationReportBean(IMigrationReport migrationReport)
   {
      this.currentRepositoryVersion = migrationReport.getCurrentRepositoryVersion();
      this.targetRepositoryVersion = migrationReport.getTargetRepositoryVersion();
      this.resourcesDone = migrationReport.getResourcesDone();
      this.totalCount = migrationReport.getTotalCount();
      this.currentMigrationJob = new DmsMigrationJobInfoBean(
            migrationReport.getCurrentMigrationJobInfo());
   }

   public RepositoryMigrationJobInfo getCurrentMigrationJob()
   {
      return currentMigrationJob;
   }

   public int getCurrentRepositoryVersion()
   {
      return currentRepositoryVersion;
   }

   public int getTargetRepositoryVersion()
   {
      return targetRepositoryVersion;
   }

   public long getResourcesDone()
   {
      return resourcesDone;
   }

   public long getTotalCount()
   {
      return totalCount;
   }

}
