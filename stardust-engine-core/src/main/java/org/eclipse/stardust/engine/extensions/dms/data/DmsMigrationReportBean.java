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

import org.eclipse.stardust.vfs.IMigrationJobInfo;
import org.eclipse.stardust.vfs.IMigrationReport;


public class DmsMigrationReportBean implements RepositoryMigrationReport, Serializable
{

   private static final long serialVersionUID = 1L;

   private RepositoryMigrationJobInfo currentMigrationJob;

   private int currentRepositoryVersion;

   private int targetRepositoryVersion;

   private long resourcesDone;

   private long totalCount;

   private int currentStructureVersion;

   private int targetStructureVersion;

   public DmsMigrationReportBean(IMigrationReport migrationReport, int currentStructureVersion, int targetStructureVersion, Long totalCount, Long resourcesDone, RepositoryMigrationJobInfo migrationJobInfo)
   {
      this.currentStructureVersion = currentStructureVersion;
      this.targetStructureVersion = targetStructureVersion;
      this.currentRepositoryVersion = migrationReport.getCurrentRepositoryVersion();
      this.targetRepositoryVersion = migrationReport.getTargetRepositoryVersion();
      this.resourcesDone = resourcesDone == null ? migrationReport.getResourcesDone() : resourcesDone;
      this.totalCount = totalCount == null ? migrationReport.getTotalCount() : totalCount;

      IMigrationJobInfo currentMigrationJobInfo = migrationReport.getCurrentMigrationJobInfo();
      if (migrationJobInfo != null)
      {
         this.currentMigrationJob = migrationJobInfo;
      }
      else if (currentMigrationJobInfo != null)
      {
         this.currentMigrationJob = new DmsMigrationJobInfoBean(currentMigrationJobInfo);
      }

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

   public int getCurrentRepositoryStructureVersion()
   {
      return currentStructureVersion;
   }

   public int getTargetRepositoryStructureVersion()
   {
      return targetStructureVersion;
   }

}
